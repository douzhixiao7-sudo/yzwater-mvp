import { useDebounceFn } from '@vueuse/core'
import {
  IotScreenStatisticsApi,
  IOT_SCREEN_POND_DEFAULT_PAGE_SIZE,
  type IotScreenPondItemVO,
  type IotScreenPondMapBounds,
  type IotScreenPondPageReqVO
} from '@/api/iot/statistics/screen'

export interface IotScreenPondLayerStyle {
  color?: string
  weight?: number
  fillColor?: string
  fillOpacity?: number
}

export interface CreateIotScreenPondMapLayerOptions {
  /** Leaflet 地图实例 */
  getMap: () => any
  /** Leaflet 命名空间 */
  getLeaflet: () => any
  /** 坑塘面图层组（建议单独 layerGroup，不要和点标记混用） */
  getLayerGroup: () => any
  /** 可选：行政区划 id */
  getAreaId?: () => string | number | undefined
  /** 每页条数，默认 500 */
  pageSize?: number
  /** 视口变化防抖毫秒，默认 300 */
  debounceMs?: number
  /** 面样式 */
  style?: IotScreenPondLayerStyle
  /** 点击坑塘面 */
  onItemClick?: (item: IotScreenPondItemVO) => void
  /** 加载进度（当前视口内已加载条数 / 总数） */
  onProgress?: (loaded: number, total: number) => void
}

const DEFAULT_POND_STYLE: IotScreenPondLayerStyle = {
  color: '#20b2aa',
  weight: 2,
  fillColor: '#20b2aa',
  fillOpacity: 0.25
}

/** 从 Leaflet 地图读取当前视口 bbox */
export function getPondBoundsFromLeafletMap(map: any): IotScreenPondMapBounds | null {
  const bounds = map?.getBounds?.()
  if (!bounds) return null
  const sw = bounds.getSouthWest?.()
  const ne = bounds.getNorthEast?.()
  if (!sw || !ne) return null
  return {
    minLon: sw.lng,
    minLat: sw.lat,
    maxLon: ne.lng,
    maxLat: ne.lat
  }
}

/** 拉取某一视口内全部坑塘（自动翻页直到 hasMore=false） */
export async function fetchAllPondsInBounds(
  bounds: IotScreenPondMapBounds,
  options?: {
    areaId?: string | number
    pageSize?: number
    includeGeometry?: boolean
    signal?: AbortSignal
    onProgress?: (loaded: number, total: number) => void
  }
): Promise<IotScreenPondItemVO[]> {
  const pageSize = options?.pageSize ?? IOT_SCREEN_POND_DEFAULT_PAGE_SIZE
  const all: IotScreenPondItemVO[] = []
  let pageNo = 1
  let hasMore = true
  let total = 0

  while (hasMore) {
    const params: IotScreenPondPageReqVO = {
      areaId: options?.areaId,
      pageNo,
      pageSize,
      includeGeometry: options?.includeGeometry ?? true,
      ...(bounds || {})
    }
    const data = await IotScreenStatisticsApi.getPondPage(params, { signal: options?.signal })
    const batch = data?.list || []
    all.push(...batch)
    total = Number(data?.total ?? all.length)
    hasMore = !!data?.hasMore
    options?.onProgress?.(all.length, total)
    pageNo += 1
    if (!batch.length) break
  }

  return all
}

/**
 * 坑塘 GIS 图层控制器：勾选开启、视口变化防抖加载、按 id 去重追加。
 *
 * 用法见文件末尾注释示例。
 */
export function createIotScreenPondMapLayerController(options: CreateIotScreenPondMapLayerOptions) {
  const loadedIds = new Set<string>()
  const layerById = new Map<string, any>()
  let abortController: AbortController | null = null
  let enabled = false

  const renderOne = (item: IotScreenPondItemVO) => {
    const id = String(item.id ?? '')
    if (!id || loadedIds.has(id)) return

    const geoJsonText = String(item.geometryGeoJson || '').trim()
    if (!geoJsonText) return

    const L = options.getLeaflet()
    const group = options.getLayerGroup()
    if (!L || !group) return

    let parsed: unknown
    try {
      parsed = JSON.parse(geoJsonText)
    } catch {
      return
    }

    const style = { ...DEFAULT_POND_STYLE, ...(options.style || {}) }
    const layer = L.geoJSON(parsed, { style })
    layer.addTo(group)

    const name = String(item.resourceName || item.resourceCode || '').trim()
    if (name && layer?.bindTooltip) {
      layer.bindTooltip(name, { permanent: false, direction: 'top' })
    }

    layer.on?.('click', (e: any) => {
      e?.originalEvent?.stopPropagation?.()
      options.onItemClick?.(item)
    })

    loadedIds.add(id)
    layerById.set(id, layer)
  }

  const loadViewport = async () => {
    if (!enabled) return
    const map = options.getMap()
    const bounds = getPondBoundsFromLeafletMap(map)
    if (!bounds) return

    abortController?.abort()
    abortController = new AbortController()
    const signal = abortController.signal

    try {
      const list = await fetchAllPondsInBounds(bounds, {
        areaId: options.getAreaId?.(),
        pageSize: options.pageSize ?? IOT_SCREEN_POND_DEFAULT_PAGE_SIZE,
        signal,
        onProgress: options.onProgress
      })
      for (const item of list) {
        if (signal.aborted) return
        renderOne(item)
      }
    } catch (error: any) {
      const msg = String(error?.message || '')
      if (error?.code === 'ERR_CANCELED' || msg.toLowerCase().includes('abort')) {
        return
      }
      throw error
    }
  }

  const debouncedLoad = useDebounceFn(loadViewport, options.debounceMs ?? 300)

  const enable = () => {
    if (enabled) return
    enabled = true
    const map = options.getMap()
    map?.on?.('moveend', debouncedLoad)
    map?.on?.('zoomend', debouncedLoad)
    debouncedLoad()
  }

  const disable = () => {
    if (!enabled) return
    enabled = false
    abortController?.abort()
    abortController = null

    const map = options.getMap()
    map?.off?.('moveend', debouncedLoad)
    map?.off?.('zoomend', debouncedLoad)

    options.getLayerGroup()?.clearLayers?.()
    loadedIds.clear()
    layerById.clear()
  }

  const reload = () => {
    debouncedLoad()
  }

  return {
    enable,
    disable,
    reload,
    loadedIds,
    layerById
  }
}

/**
 * 坑塘 GeoJSON 分页图层控制器（旧方案，适合少量数据）。
 * 大数据量请改用 MVT：
 *   GET /admin-api/iot/statistics/screen/pond-tiles/{z}/{x}/{y}
 *   图层名 ponds，见 buildPondMvtTileUrlTemplate()
 *
 * GeoJSON 用法示例：
 *
 * ```ts
 * import { createIotScreenPondMapLayerController } from '@/api/iot/statistics/pondMapLayer'
 *
 * const pondLayerGroup = Leaflet.layerGroup().addTo(mapInstance)
 * const pondLayerCtrl = createIotScreenPondMapLayerController({
 *   getMap: () => mapInstance,
 *   getLeaflet: () => Leaflet,
 *   getLayerGroup: () => pondLayerGroup,
 *   getAreaId: () => selectedAreaId.value, // 可选
 *   pageSize: 500,
 *   debounceMs: 300,
 *   onItemClick: (item) => openPondDetail(item)
 * })
 *
 * // 图层勾选 ON
 * watch(showPondLayer, (on) => (on ? pondLayerCtrl.enable() : pondLayerCtrl.disable()))
 * ```
 */
