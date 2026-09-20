import { ElMessage } from 'element-plus'
import startpointImg from '@/assets/imgs/startpoint.png'
import endpointImg from '@/assets/imgs/endpoint.png'

/** 线段节点样式 */
export type VertexMarkerType = 'default' | 'arrow' | 'location'

/** 线段节点展示配置 */
export type LineVertexStyleOptions = {
  middleAsArrow?: boolean
  endpointAsLocation?: boolean
}

export const ARROW_VERTEX_SIZE = 26
export const LOCATION_MARKER_IMAGE_SIZE = 36

const escapeHtml = (text: string) =>
  text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')

const resolveLineVertexStyle = (style: LineVertexStyleOptions | boolean): LineVertexStyleOptions => {
  if (typeof style === 'boolean') {
    return { middleAsArrow: style, endpointAsLocation: style }
  }
  return {
    middleAsArrow: Boolean(style.middleAsArrow),
    endpointAsLocation: style.endpointAsLocation !== false
  }
}

export const isLineEndpointIndex = (index: number, count: number) =>
  count > 0 && (index === 0 || index === count - 1)

export const resolveVertexMarkerType = (
  index: number,
  count: number,
  style: LineVertexStyleOptions | boolean = {},
  rawType?: VertexMarkerType
): VertexMarkerType => {
  const resolved = resolveLineVertexStyle(style)
  const isEndpoint = isLineEndpointIndex(index, count)
  if (resolved.endpointAsLocation && isEndpoint) return 'location'
  if (resolved.middleAsArrow) return 'arrow'
  if (rawType === 'location') return 'location'
  return 'default'
}

/** 普通线段颜色 / 风险隐患段颜色 / 列表悬停高亮 */
export const LINE_SEGMENT_COLOR_DEFAULT = '#1677ff'
export const LINE_SEGMENT_COLOR_RISK = '#fadb14'
export const LINE_SEGMENT_COLOR_HOVER = '#4096ff'
/** 路线固定色板（5 色，无红/橙/黄等告警敏感色） */
export const ROUTE_PALETTE_COLORS = [
  '#1677ff',
  '#13c2c2',
  '#52c41a',
  '#722ed1',
  '#2f54eb'
] as const

/** 超出色板条数的路线使用默认色 */
export const DEFAULT_ROUTE_LINE_COLOR = '#597ef7'

export const ROUTE_PALETTE_HOVER_COLORS = [
  '#4096ff',
  '#36cfc9',
  '#73d13d',
  '#9254de',
  '#597ef7'
] as const

export const DEFAULT_ROUTE_LINE_HOVER_COLOR = '#85a5ff'

/** @deprecated 兼容旧引用，等同色板 */
export const ROUTE_LINE_COLORS = ROUTE_PALETTE_COLORS

export const ROUTE_LINE_HOVER_COLORS = ROUTE_PALETTE_HOVER_COLORS

export const getRouteLineColor = (lineIndex: number) => {
  const idx = Math.max(0, lineIndex)
  if (idx < ROUTE_PALETTE_COLORS.length) {
    return ROUTE_PALETTE_COLORS[idx]
  }
  return DEFAULT_ROUTE_LINE_COLOR
}

export const getRouteLineHoverColor = (lineIndex: number) => {
  const idx = Math.max(0, lineIndex)
  if (idx < ROUTE_PALETTE_HOVER_COLORS.length) {
    return ROUTE_PALETTE_HOVER_COLORS[idx]
  }
  return DEFAULT_ROUTE_LINE_HOVER_COLOR
}

export const getRouteHoverColorFromBaseColor = (routeColor: string) => {
  const idx = ROUTE_PALETTE_COLORS.indexOf(routeColor as (typeof ROUTE_PALETTE_COLORS)[number])
  if (idx >= 0) return ROUTE_PALETTE_HOVER_COLORS[idx]
  if (routeColor === DEFAULT_ROUTE_LINE_COLOR) return DEFAULT_ROUTE_LINE_HOVER_COLOR
  return routeColor
}

/** 按路线序号写入色板颜色（忽略仓库/历史单色） */
export const applyRoutePaletteToLines = (lines: LineGeometryPayload[]) => {
  lines.forEach((line, index) => {
    if (line?.coordinates?.length >= 2) {
      line.routeColor = getRouteLineColor(index)
    }
  })
}
export const DEFAULT_HAZARD_POINT_NAME = '隐患点'
export const getDefaultWarehouseName = (lineIndex: number) => `仓库${Math.max(1, lineIndex + 1)}`
export const resolveHazardEndpointLabels = (
  lineIndex: number,
  startAsHazardPoint: boolean,
  endpointLabels: [string, string] = ['起点', '终点']
): [string, string] => {
  if (!startAsHazardPoint) {
    return [
      endpointLabels[0]?.trim() || '起点',
      endpointLabels[1]?.trim() || '终点'
    ]
  }
  return [endpointLabels[0]?.trim() || DEFAULT_HAZARD_POINT_NAME, getDefaultWarehouseName(lineIndex)]
}
export const LINE_SEGMENT_WEIGHT_DEFAULT = 8
export const LINE_SEGMENT_WEIGHT_RISK = 9

export type SegmentLayerPair = {
  base: any
  flowArrows: SegmentFlowArrowsHandle | null
}

export type SegmentVisualStyle = {
  base: { color: string; weight: number; opacity: number }
}

export type SegmentFlowArrowsHandle = {
  markers: Array<{ marker: any; phase: number }>
  start: any
  end: any
  rotationDeg: number
  isRisk: boolean
  phase: number
  /** 流动方向与折线绘制方向相反（隐患点模式：仓库 → 隐患点） */
  reverseFlow?: boolean
  /** 流动速度倍率（当前路线高亮时加快） */
  speed?: number
  map?: any
  group?: any
  leaflet?: any
  routeColor?: string
}

export type RouteActiveHighlightOptions = {
  active?: boolean
  dimmed?: boolean
  /** 短暂醒目闪烁（约 2~3 秒） */
  flash?: boolean
}

const FLOW_ARROW_SIZE = 20
const FLOW_ARROWS_PER_SEGMENT = 3
const FLOW_ARROW_SPEED = 0.0015
/** 屏幕上相邻流动箭头的最小间距（px），过密则减少数量或隐藏 */
const FLOW_ARROW_MIN_SPACING_PX = 32
const flowArrowRegistry = new Set<SegmentFlowArrowsHandle>()
let flowArrowAnimId: number | null = null

const computeSegmentScreenLength = (map: any, start: any, end: any) => {
  if (!map?.latLngToContainerPoint) return Number.POSITIVE_INFINITY
  try {
    const p1 = map.latLngToContainerPoint(start)
    const p2 = map.latLngToContainerPoint(end)
    return Math.hypot(p2.x - p1.x, p2.y - p1.y)
  } catch {
    return Number.POSITIVE_INFINITY
  }
}

const resolveFlowArrowCount = (map: any, start: any, end: any) => {
  const screenLen = computeSegmentScreenLength(map, start, end)
  if (screenLen < FLOW_ARROW_MIN_SPACING_PX * 0.75) return 0
  return Math.min(FLOW_ARROWS_PER_SEGMENT, Math.max(1, Math.floor(screenLen / FLOW_ARROW_MIN_SPACING_PX)))
}

const buildFlowArrowIcon = (L: any, rotationDeg: number, isRisk: boolean, routeColor?: string) =>
  L.divIcon({
    className: 'tianditu-flow-arrow-marker',
    html: buildFlowChevronHtml(rotationDeg, isRisk, routeColor),
    iconSize: [FLOW_ARROW_SIZE, FLOW_ARROW_SIZE],
    iconAnchor: [FLOW_ARROW_SIZE / 2, FLOW_ARROW_SIZE / 2]
  })

const redistributeFlowArrowPhases = (markers: Array<{ phase: number }>, count: number) => {
  markers.forEach((entry, index) => {
    entry.phase = count > 0 ? index / count : 0
  })
}

const updateFlowArrowMarkerPositions = (L: any, handle: SegmentFlowArrowsHandle) => {
  handle.markers.forEach(({ marker, phase }) => {
    const t = (phase + handle.phase) % 1
    const pos = interpolateSegmentLatLng(handle.start, handle.end, t)
    marker.setLatLng(L.latLng(pos.lat, pos.lng))
  })
}

const syncFlowArrowRegistry = (handle: SegmentFlowArrowsHandle) => {
  if (handle.markers.length === 0) {
    flowArrowRegistry.delete(handle)
    return
  }
  if (!flowArrowRegistry.has(handle)) {
    flowArrowRegistry.add(handle)
    if (handle.leaflet) ensureFlowArrowAnimation(handle.leaflet)
  }
}

const resizeFlowArrowMarkers = (
  L: any,
  group: any,
  handle: SegmentFlowArrowsHandle,
  targetCount: number,
  icon: any
) => {
  while (handle.markers.length > targetCount) {
    const removed = handle.markers.pop()
    if (removed?.marker) group.removeLayer(removed.marker)
  }
  while (handle.markers.length < targetCount) {
    const marker = L.marker(L.latLng(handle.start.lat, handle.start.lng), {
      icon,
      interactive: false,
      bubblingMouseEvents: false,
      keyboard: false
    })
    group.addLayer(marker)
    handle.markers.push({ marker, phase: 0 })
  }
  redistributeFlowArrowPhases(handle.markers, targetCount)
}

/** 流动箭头：与顶点箭头相同的回旋镖造型 */
export const buildFlowChevronHtml = (rotationDeg: number, isRisk = false, routeColor?: string) => {
  const fill = isRisk ? '#fff7e6' : '#ffffff'
  const stroke = isRisk ? '#d48806' : routeColor || '#1677ff'
  const half = FLOW_ARROW_SIZE / 2
  return `<svg class="tianditu-flow-arrow-marker__svg" viewBox="0 0 22 22" width="${FLOW_ARROW_SIZE}" height="${FLOW_ARROW_SIZE}" style="transform: rotate(${rotationDeg}deg); transform-origin: ${half}px ${half}px">
    <path d="M11 3 L17 19 L11 15 L5 19 Z" fill="${fill}" stroke="${stroke}" stroke-width="1.6" stroke-linejoin="round"/>
  </svg>`
}

const interpolateSegmentLatLng = (start: any, end: any, t: number) => ({
  lat: start.lat + (end.lat - start.lat) * t,
  lng: start.lng + (end.lng - start.lng) * t
})

const ensureFlowArrowAnimation = (L: any) => {
  if (flowArrowAnimId != null) return
  const tick = () => {
    if (flowArrowRegistry.size === 0) {
      flowArrowAnimId = null
      return
    }
    flowArrowRegistry.forEach((handle) => {
      const speed = handle.speed ?? FLOW_ARROW_SPEED
      handle.phase = (handle.phase + speed) % 1
      handle.markers.forEach(({ marker, phase }) => {
        const t = (phase + handle.phase) % 1
        const pos = interpolateSegmentLatLng(handle.start, handle.end, t)
        marker.setLatLng(L.latLng(pos.lat, pos.lng))
      })
    })
    flowArrowAnimId = requestAnimationFrame(tick)
  }
  flowArrowAnimId = requestAnimationFrame(tick)
}

export const unregisterSegmentFlowArrows = (handles: SegmentFlowArrowsHandle[]) => {
  handles.forEach((handle) => flowArrowRegistry.delete(handle))
  if (flowArrowRegistry.size === 0 && flowArrowAnimId != null) {
    cancelAnimationFrame(flowArrowAnimId)
    flowArrowAnimId = null
  }
}

export const clearAllSegmentFlowArrows = () => {
  flowArrowRegistry.clear()
  if (flowArrowAnimId != null) {
    cancelAnimationFrame(flowArrowAnimId)
    flowArrowAnimId = null
  }
}

export const createSegmentFlowArrows = (
  L: any,
  group: any,
  map: any,
  latlngs: any[],
  isRisk: boolean,
  routeColor?: string,
  reverseFlow = false
): SegmentFlowArrowsHandle | null => {
  if (latlngs.length < 2) return null
  const flow = resolveFlowArrowGeometry(map, L, latlngs, reverseFlow)
  const { start, end } = flow
  const activeRotationDeg = flow.rotationDeg
  const targetCount = resolveFlowArrowCount(map, start, end)
  if (targetCount === 0) return null

  const icon = buildFlowArrowIcon(L, activeRotationDeg, isRisk, routeColor)
  const handle: SegmentFlowArrowsHandle = {
    markers: [],
    start,
    end,
    rotationDeg: activeRotationDeg,
    isRisk,
    phase: 0,
    reverseFlow,
    map,
    group,
    leaflet: L,
    routeColor
  }
  resizeFlowArrowMarkers(L, group, handle, targetCount, icon)
  updateFlowArrowMarkerPositions(L, handle)
  syncFlowArrowRegistry(handle)
  return handle
}

export const syncSegmentFlowArrows = (
  L: any,
  handle: SegmentFlowArrowsHandle | null,
  latlngs: any[],
  isRisk: boolean,
  routeColor?: string,
  map?: any
) => {
  if (!handle || latlngs.length < 2) return
  const activeMap = map ?? handle.map
  const activeRouteColor = routeColor ?? handle.routeColor
  const reverseFlow = Boolean(handle.reverseFlow)
  const flow = resolveFlowArrowGeometry(activeMap, L, latlngs, reverseFlow)
  handle.start = flow.start
  handle.end = flow.end
  handle.map = activeMap
  handle.routeColor = activeRouteColor
  handle.leaflet = L

  const activeRotationDeg = flow.rotationDeg
  const targetCount = resolveFlowArrowCount(activeMap, handle.start, handle.end)
  const icon = buildFlowArrowIcon(L, activeRotationDeg, isRisk, activeRouteColor)
  if (handle.rotationDeg !== activeRotationDeg || handle.isRisk !== isRisk) {
    handle.rotationDeg = activeRotationDeg
    handle.isRisk = isRisk
    handle.markers.forEach(({ marker }) => marker.setIcon(icon))
  }

  if (handle.group) {
    resizeFlowArrowMarkers(L, handle.group, handle, targetCount, icon)
  }
  updateFlowArrowMarkerPositions(L, handle)
  syncFlowArrowRegistry(handle)
}

export const resolveSegmentVisualStyle = (
  isRisk: boolean,
  isHover: boolean,
  routeColor?: string,
  routeHoverColor?: string,
  routeHighlight?: RouteActiveHighlightOptions
): SegmentVisualStyle => {
  const routeActive = Boolean(routeHighlight?.active)
  const routeFlash = routeActive && Boolean(routeHighlight?.flash)
  const routeDimmed = Boolean(routeHighlight?.dimmed)
  if (isHover) {
    return {
      base: {
        color: routeHoverColor || LINE_SEGMENT_COLOR_HOVER,
        weight: isRisk ? LINE_SEGMENT_WEIGHT_RISK + 1 : LINE_SEGMENT_WEIGHT_DEFAULT + 1,
        opacity: 1
      }
    }
  }
  if (routeActive) {
    const extraWeight = routeFlash ? 5 : 3
    return {
      base: {
        color: routeHoverColor || routeColor || LINE_SEGMENT_COLOR_HOVER,
        weight: isRisk ? LINE_SEGMENT_WEIGHT_RISK + extraWeight : LINE_SEGMENT_WEIGHT_DEFAULT + extraWeight,
        opacity: 1
      }
    }
  }
  if (isRisk) {
    return {
      base: {
        color: LINE_SEGMENT_COLOR_RISK,
        weight: LINE_SEGMENT_WEIGHT_RISK,
        opacity: routeDimmed ? 0.45 : 1
      }
    }
  }
  return {
    base: {
      color: routeColor || LINE_SEGMENT_COLOR_DEFAULT,
      weight: LINE_SEGMENT_WEIGHT_DEFAULT,
      opacity: routeDimmed ? 0.38 : 1
    }
  }
}

export const createSegmentLineLayers = (
  L: any,
  latlngs: any[],
  isRisk: boolean,
  isHover: boolean,
  options: {
    interactive?: boolean
    clickableClassName?: string
    enableFlow?: boolean
    reverseFlow?: boolean
    map?: any
    group?: any
    routeColor?: string
    routeHoverColor?: string
  } = {}
): SegmentLayerPair => {
  const style = resolveSegmentVisualStyle(isRisk, isHover, options.routeColor, options.routeHoverColor)
  const enableFlow = options.enableFlow !== false
  const reverseFlow = Boolean(options.reverseFlow)
  const base = L.polyline(latlngs, {
    color: style.base.color,
    weight: style.base.weight,
    opacity: style.base.opacity,
    lineCap: 'round',
    lineJoin: 'round',
    interactive: Boolean(options.interactive),
    className: options.interactive && options.clickableClassName ? options.clickableClassName : ''
  })
  let flowArrows: SegmentFlowArrowsHandle | null = null
  if (enableFlow && options.group) {
    flowArrows = createSegmentFlowArrows(
      L,
      options.group,
      options.map,
      latlngs,
      isRisk,
      options.routeColor,
      reverseFlow
    )
  }
  return { base, flowArrows }
}

export const addSegmentLayersToGroup = (
  L: any,
  group: any,
  latlngs: any[],
  isRisk: boolean,
  isHover = false,
  options?: {
    interactive?: boolean
    clickableClassName?: string
    enableFlow?: boolean
    reverseFlow?: boolean
    map?: any
    routeColor?: string
    routeHoverColor?: string
  }
): SegmentLayerPair => {
  const pair = createSegmentLineLayers(L, latlngs, isRisk, isHover, {
    ...options,
    group
  })
  group.addLayer(pair.base)
  return pair
}

export const applySegmentLayerStyles = (
  L: any,
  map: any,
  pair: SegmentLayerPair,
  latlngs: any[],
  isRisk: boolean,
  isHover: boolean,
  routeColor?: string,
  routeHoverColor?: string,
  routeHighlight?: RouteActiveHighlightOptions
) => {
  if (!pair?.base) return
  const style = resolveSegmentVisualStyle(isRisk, isHover, routeColor, routeHoverColor, routeHighlight)
  pair.base.setStyle?.({
    color: style.base.color,
    weight: style.base.weight,
    opacity: style.base.opacity
  })
  const pathEl = pair.base.getElement?.() as SVGPathElement | undefined
  if (pathEl) {
    const active = Boolean(routeHighlight?.active)
    pathEl.classList.toggle('tianditu-line-segment--active-route', active)
    pathEl.classList.toggle('is-route-flash', active && Boolean(routeHighlight?.flash))
  }
  if (!pair.flowArrows) return
  syncSegmentFlowArrows(L, pair.flowArrows, latlngs, isRisk, routeColor, map)
}

export const syncSegmentLayerLatLngs = (
  L: any,
  map: any,
  pair: SegmentLayerPair,
  latlngs: any[],
  isRisk: boolean,
  routeColor?: string,
  group?: any,
  reverseFlow = false
) => {
  pair.base?.setLatLngs?.(latlngs)
  if (!pair.flowArrows) {
    const flow = resolveFlowArrowGeometry(map, L, latlngs, reverseFlow)
    if (group && resolveFlowArrowCount(map, flow.start, flow.end) > 0) {
      pair.flowArrows = createSegmentFlowArrows(
        L,
        group,
        map,
        latlngs,
        isRisk,
        routeColor,
        reverseFlow
      )
    }
    return
  }
  syncSegmentFlowArrows(L, pair.flowArrows, latlngs, isRisk, routeColor, map)
  if (pair.flowArrows.markers.length === 0) {
    pair.flowArrows = null
  }
}

/** 隐患段中心点 [经度, 纬度] */
export type RiskSegmentCenter = [number, number] | null

export type ExtendedLineGeometry = {
  type: 'LineString'
  coordinates: number[][]
  vertexMarkers?: VertexMarkerType[]
  vertexLabels?: string[]
  riskSegments?: boolean[]
  /** 隐患段名称（与线段索引对齐，暂不地图展示） */
  riskSegmentNames?: string[]
  /** 隐患段描述（与线段索引对齐） */
  riskSegmentDescriptions?: string[]
  /** 隐患段中心点经纬度（与线段索引对齐） */
  riskSegmentCenters?: RiskSegmentCenter[]
  /** 单条路线颜色 */
  routeColor?: string
  /** 单条路线关联的系统仓库 id */
  linkedWarehouseId?: string | number
  /** 多条路线颜色（与 coordinates 索引对齐） */
  routeColors?: (string | null)[]
  /** 多条路线关联的系统仓库 id */
  linkedWarehouseIds?: (string | number | null)[]
}

export type LineGeometryPayload = {
  coordinates: [number, number][]
  vertexMarkers: VertexMarkerType[]
  vertexLabels: string[]
  riskSegments: boolean[]
  riskSegmentNames: string[]
  riskSegmentDescriptions: string[]
  riskSegmentCenters: RiskSegmentCenter[]
  /** 物资路线颜色（由关联仓库决定，持久化到 GeoJSON） */
  routeColor?: string
  /** 关联的防汛物资 id（来自防汛物资仓库地址） */
  linkedWarehouseId?: string | number
}

export const createVertexMarkers = (
  count: number,
  style: LineVertexStyleOptions | boolean = {},
  rawMarkers: VertexMarkerType[] = []
): VertexMarkerType[] => {
  return Array.from({ length: count }, (_, index) =>
    resolveVertexMarkerType(index, count, style, rawMarkers[index])
  )
}

export const createVertexLabels = (
  count: number,
  rawLabels: string[] = [],
  endpointLabels: [string, string] = ['起点', '终点']
): string[] => {
  return Array.from({ length: count }, (_, index) => {
    const raw = (rawLabels[index] || '').trim()
    if (raw) return raw
    if (index === 0) return (endpointLabels[0] || '起点').trim() || '起点'
    if (index === count - 1 && count > 1) return (endpointLabels[1] || '终点').trim() || '终点'
    return ''
  })
}

export const createRiskSegments = (segmentCount: number, raw: boolean[] = []): boolean[] => {
  return Array.from({ length: segmentCount }, (_, index) => Boolean(raw[index]))
}

/** 清除线段级隐患标记（起点即隐患点模式使用） */
export const clearLineRiskSegmentMeta = (payload: LineGeometryPayload): LineGeometryPayload => {
  const segmentCount = Math.max(0, payload.coordinates.length - 1)
  return {
    ...payload,
    riskSegments: createRiskSegments(segmentCount),
    riskSegmentNames: createRiskSegmentNames(segmentCount),
    riskSegmentDescriptions: createRiskSegmentDescriptions(segmentCount),
    riskSegmentCenters: Array.from({ length: segmentCount }, () => null)
  }
}

export const computeSegmentCenter = (
  coordinates: [number, number][],
  segmentIndex: number
): [number, number] => {
  const a = coordinates[segmentIndex]
  const b = coordinates[segmentIndex + 1]
  return [(a[0] + b[0]) / 2, (a[1] + b[1]) / 2]
}

const parseRiskSegmentCenter = (item: unknown): RiskSegmentCenter => {
  if (!Array.isArray(item) || item.length < 2) return null
  const lng = Number(item[0])
  const lat = Number(item[1])
  if (!Number.isFinite(lng) || !Number.isFinite(lat)) return null
  return [lng, lat]
}

export const createRiskSegmentNames = (segmentCount: number, raw: string[] = []): string[] => {
  return Array.from({ length: segmentCount }, (_, index) => String(raw[index] ?? '').trim())
}

export const createRiskSegmentDescriptions = (segmentCount: number, raw: string[] = []): string[] => {
  return Array.from({ length: segmentCount }, (_, index) => String(raw[index] ?? '').trim())
}

export const createRiskSegmentCenters = (
  coordinates: [number, number][],
  riskSegments: boolean[],
  rawCenters: RiskSegmentCenter[] = []
): RiskSegmentCenter[] => {
  const segmentCount = Math.max(0, coordinates.length - 1)
  return Array.from({ length: segmentCount }, (_, index) => {
    if (!riskSegments[index]) return null
    const parsed = parseRiskSegmentCenter(rawCenters[index])
    if (parsed) return parsed
    return computeSegmentCenter(coordinates, index)
  })
}

export const syncRiskSegmentCenters = (
  coordinates: [number, number][],
  riskSegments: boolean[]
): RiskSegmentCenter[] => {
  const segmentCount = Math.max(0, coordinates.length - 1)
  return Array.from({ length: segmentCount }, (_, index) =>
    riskSegments[index] ? computeSegmentCenter(coordinates, index) : null
  )
}

export const parseLineGeometryPayload = (
  geoJson?: string,
  style: LineVertexStyleOptions | boolean = {},
  endpointLabels: [string, string] = ['起点', '终点']
): LineGeometryPayload | null => {
  if (!geoJson) return null
  try {
    const parsed = JSON.parse(geoJson)
    if (!parsed || parsed.type !== 'LineString' || !Array.isArray(parsed.coordinates)) return null
    const coordinates = parsed.coordinates
      .map((item: unknown) => {
        if (!Array.isArray(item) || item.length < 2) return null
        const lon = Number(item[0])
        const lat = Number(item[1])
        if (!Number.isFinite(lon) || !Number.isFinite(lat)) return null
        return [lon, lat] as [number, number]
      })
      .filter(Boolean) as [number, number][]
    if (coordinates.length < 2) return null
    const rawMarkers = Array.isArray(parsed.vertexMarkers) ? (parsed.vertexMarkers as VertexMarkerType[]) : []
    const rawLabels = Array.isArray(parsed.vertexLabels) ? parsed.vertexLabels.map((v: unknown) => String(v ?? '')) : []
    const rawRisk = Array.isArray(parsed.riskSegments) ? parsed.riskSegments.map((v: unknown) => Boolean(v)) : []
    const rawRiskNames = Array.isArray(parsed.riskSegmentNames)
      ? parsed.riskSegmentNames.map((v: unknown) => String(v ?? '').trim())
      : []
    const rawRiskDescriptions = Array.isArray(parsed.riskSegmentDescriptions)
      ? parsed.riskSegmentDescriptions.map((v: unknown) => String(v ?? '').trim())
      : []
    const rawRiskCenters = Array.isArray(parsed.riskSegmentCenters)
      ? parsed.riskSegmentCenters.map((v: unknown) => parseRiskSegmentCenter(v))
      : []
    const vertexMarkers = createVertexMarkers(coordinates.length, style, rawMarkers)
    const vertexLabels = createVertexLabels(coordinates.length, rawLabels, endpointLabels)
    const riskSegments = createRiskSegments(coordinates.length - 1, rawRisk)
    const riskSegmentNames = createRiskSegmentNames(riskSegments.length, rawRiskNames)
    const riskSegmentDescriptions = createRiskSegmentDescriptions(riskSegments.length, rawRiskDescriptions)
    const riskSegmentCenters = createRiskSegmentCenters(coordinates, riskSegments, rawRiskCenters)
    const routeColor =
      typeof parsed.routeColor === 'string' && parsed.routeColor.trim()
        ? parsed.routeColor.trim()
        : undefined
    const linkedWarehouseId = parseLinkedWarehouseIdFromRoot(parsed, 0)
    return {
      coordinates,
      vertexMarkers,
      vertexLabels,
      riskSegments,
      riskSegmentNames,
      riskSegmentDescriptions,
      riskSegmentCenters,
      routeColor,
      linkedWarehouseId
    }
  } catch {
    return null
  }
}

export const buildLineGeometryPayload = (
  coordinates: [number, number][],
  vertexMarkers: VertexMarkerType[],
  style: LineVertexStyleOptions | boolean = {},
  riskSegments: boolean[] = [],
  vertexLabels: string[] = [],
  endpointLabels: [string, string] = ['起点', '终点'],
  riskSegmentNames: string[] = [],
  _riskSegmentCenters: RiskSegmentCenter[] = [],
  riskSegmentDescriptions: string[] = [],
  routeColor?: string,
  linkedWarehouseId?: string | number
): string => {
  const segmentCount = Math.max(0, coordinates.length - 1)
  const syncedRiskSegments = createRiskSegments(segmentCount, riskSegments)
  const syncedRiskNames = createRiskSegmentNames(segmentCount, riskSegmentNames)
  const syncedRiskDescriptions = createRiskSegmentDescriptions(segmentCount, riskSegmentDescriptions)
  const syncedRiskCenters = syncRiskSegmentCenters(coordinates, syncedRiskSegments)
  const payload: ExtendedLineGeometry = {
    type: 'LineString',
    coordinates,
    vertexMarkers: createVertexMarkers(coordinates.length, style, vertexMarkers),
    vertexLabels: createVertexLabels(coordinates.length, vertexLabels, endpointLabels),
    riskSegments: syncedRiskSegments,
    riskSegmentNames: syncedRiskNames,
    riskSegmentDescriptions: syncedRiskDescriptions,
    riskSegmentCenters: syncedRiskCenters
  }
  if (routeColor?.trim()) {
    payload.routeColor = routeColor.trim()
  }
  if (linkedWarehouseId != null && String(linkedWarehouseId).trim()) {
    payload.linkedWarehouseId = linkedWarehouseId
  }
  return JSON.stringify(payload)
}

export type MultiLineGeometryPayload = {
  lines: LineGeometryPayload[]
}

const parseCoordinatePair = (item: unknown): [number, number] | null => {
  if (!Array.isArray(item) || item.length < 2) return null
  const lon = Number(item[0])
  const lat = Number(item[1])
  if (!Number.isFinite(lon) || !Number.isFinite(lat)) return null
  return [lon, lat]
}

const parseLineMetaFromRoot = (
  parsed: Record<string, unknown>,
  lineIndex: number,
  pointCount: number,
  segmentCount: number,
  style: LineVertexStyleOptions | boolean,
  endpointLabels: [string, string]
): Pick<
  LineGeometryPayload,
  'vertexMarkers' | 'vertexLabels' | 'riskSegments' | 'riskSegmentNames' | 'riskSegmentDescriptions' | 'riskSegmentCenters'
> => {
  const pickLineArray = (field: string): unknown[] => {
    const root = parsed[field]
    if (!Array.isArray(root) || root.length === 0) return []
    if (Array.isArray(root[0])) {
      const lineArr = root[lineIndex]
      return Array.isArray(lineArr) ? lineArr : []
    }
    return lineIndex === 0 ? root : []
  }

  const rawMarkers = pickLineArray('vertexMarkers') as VertexMarkerType[]
  const rawLabels = pickLineArray('vertexLabels').map((v) => String(v ?? ''))
  const rawRisk = pickLineArray('riskSegments').map((v) => Boolean(v))
  const rawRiskNames = pickLineArray('riskSegmentNames').map((v) => String(v ?? '').trim())
  const rawRiskDescriptions = pickLineArray('riskSegmentDescriptions').map((v) => String(v ?? '').trim())
  const rawRiskCenters = pickLineArray('riskSegmentCenters').map((v) => parseRiskSegmentCenter(v))

  return {
    vertexMarkers: createVertexMarkers(pointCount, style, rawMarkers),
    vertexLabels: createVertexLabels(pointCount, rawLabels, endpointLabels),
    riskSegments: createRiskSegments(segmentCount, rawRisk),
    riskSegmentNames: createRiskSegmentNames(segmentCount, rawRiskNames),
    riskSegmentDescriptions: createRiskSegmentDescriptions(segmentCount, rawRiskDescriptions),
    riskSegmentCenters: createRiskSegmentCenters(
      [] as [number, number][],
      createRiskSegments(segmentCount, rawRisk),
      rawRiskCenters
    )
  }
}

const parseRouteColorFromRoot = (parsed: Record<string, unknown>, lineIndex: number): string | undefined => {
  const colors = parsed.routeColors
  if (Array.isArray(colors) && colors.length > 0) {
    const item = colors[lineIndex]
    if (typeof item === 'string' && item.trim()) return item.trim()
  }
  if (lineIndex === 0 && typeof parsed.routeColor === 'string' && parsed.routeColor.trim()) {
    return parsed.routeColor.trim()
  }
  return undefined
}

const parseLinkedWarehouseIdFromRoot = (
  parsed: Record<string, unknown>,
  lineIndex: number
): string | number | undefined => {
  const ids = parsed.linkedWarehouseIds
  if (Array.isArray(ids) && ids.length > 0) {
    const item = ids[lineIndex]
    if (item != null && String(item).trim()) return item as string | number
  }
  if (lineIndex === 0 && parsed.linkedWarehouseId != null && String(parsed.linkedWarehouseId).trim()) {
    return parsed.linkedWarehouseId as string | number
  }
  return undefined
}

const parseSingleLineFromCoordinates = (
  coordinatesRaw: unknown,
  parsed: Record<string, unknown>,
  lineIndex: number,
  style: LineVertexStyleOptions | boolean,
  endpointLabels: [string, string]
): LineGeometryPayload | null => {
  if (!Array.isArray(coordinatesRaw)) return null
  const coordinates = coordinatesRaw
    .map((item) => parseCoordinatePair(item))
    .filter(Boolean) as [number, number][]
  if (coordinates.length < 2) return null
  const segmentCount = coordinates.length - 1
  const meta = parseLineMetaFromRoot(parsed, lineIndex, coordinates.length, segmentCount, style, endpointLabels)
  return {
    coordinates,
    ...meta,
    riskSegmentCenters: createRiskSegmentCenters(coordinates, meta.riskSegments, meta.riskSegmentCenters),
    routeColor: parseRouteColorFromRoot(parsed, lineIndex),
    linkedWarehouseId: parseLinkedWarehouseIdFromRoot(parsed, lineIndex)
  }
}

/** 解析 LineString / MultiLineString 为多条折线载荷 */
export const parseMultiLineGeometryPayload = (
  geoJson?: string,
  style: LineVertexStyleOptions | boolean = {},
  endpointLabels: [string, string] = ['起点', '终点']
): MultiLineGeometryPayload | null => {
  if (!geoJson) return null
  try {
    const parsed = JSON.parse(geoJson) as Record<string, unknown>
    if (!parsed?.type) return null
    if (parsed.type === 'LineString') {
      const line = parseSingleLineFromCoordinates(parsed.coordinates, parsed, 0, style, endpointLabels)
      return line ? { lines: [line] } : null
    }
    if (parsed.type === 'MultiLineString' && Array.isArray(parsed.coordinates)) {
      const lines = (parsed.coordinates as unknown[])
        .map((coords, index) => parseSingleLineFromCoordinates(coords, parsed, index, style, endpointLabels))
        .filter(Boolean) as LineGeometryPayload[]
      return lines.length > 0 ? { lines } : null
    }
    return null
  } catch {
    return null
  }
}

/** 多条折线合并为 GeoJSON 字符串（单条仍输出 LineString） */
export const buildMultiLineGeometryPayload = (
  lines: LineGeometryPayload[],
  style: LineVertexStyleOptions | boolean = {},
  endpointLabels: [string, string] = ['起点', '终点']
): string => {
  const normalized = (lines || []).filter((line) => line?.coordinates?.length >= 2)
  if (normalized.length === 0) return ''
  if (normalized.length === 1) {
    const line = normalized[0]
    return buildLineGeometryPayload(
      line.coordinates,
      line.vertexMarkers,
      style,
      line.riskSegments,
      line.vertexLabels,
      endpointLabels,
      line.riskSegmentNames,
      line.riskSegmentCenters,
      line.riskSegmentDescriptions,
      line.routeColor,
      line.linkedWarehouseId
    )
  }
  const coordinates = normalized.map((line) => line.coordinates)
  const vertexMarkers = normalized.map((line) =>
    createVertexMarkers(line.coordinates.length, style, line.vertexMarkers)
  )
  const vertexLabels = normalized.map((line) =>
    createVertexLabels(line.coordinates.length, line.vertexLabels, endpointLabels)
  )
  const riskSegments = normalized.map((line) => createRiskSegments(line.coordinates.length - 1, line.riskSegments))
  const riskSegmentNames = normalized.map((line, lineIndex) =>
    createRiskSegmentNames(riskSegments[lineIndex].length, line.riskSegmentNames)
  )
  const riskSegmentDescriptions = normalized.map((line, lineIndex) =>
    createRiskSegmentDescriptions(riskSegments[lineIndex].length, line.riskSegmentDescriptions)
  )
  const riskSegmentCenters = normalized.map((line, lineIndex) =>
    syncRiskSegmentCenters(line.coordinates, riskSegments[lineIndex])
  )
  const routeColors = normalized.map((line) => line.routeColor?.trim() || null)
  const linkedWarehouseIds = normalized.map((line) =>
    line.linkedWarehouseId != null && String(line.linkedWarehouseId).trim()
      ? line.linkedWarehouseId
      : null
  )
  const payload: Record<string, unknown> = {
    type: 'MultiLineString',
    coordinates,
    vertexMarkers,
    vertexLabels,
    riskSegments,
    riskSegmentNames,
    riskSegmentDescriptions,
    riskSegmentCenters
  }
  if (routeColors.some(Boolean)) {
    payload.routeColors = routeColors
  }
  if (linkedWarehouseIds.some((item) => item != null)) {
    payload.linkedWarehouseIds = linkedWarehouseIds
  }
  return JSON.stringify(payload)
}

/** 箭头图标默认朝上，与屏幕坐标系 atan2 差 90° */
const ARROW_ICON_ROTATION_OFFSET = 90

/**
 * 折线节点用于朝向计算的线段端点（经纬度 [lon, lat]）。
 * 非末点：当前点 → 下一节点；末点：上一节点 → 当前点。
 */
export const getLineVertexSegmentEnds = (
  coordinates: [number, number][],
  index: number
): { from: [number, number]; to: [number, number] } | null => {
  if (coordinates.length < 2 || index < 0 || index >= coordinates.length) return null
  if (index < coordinates.length - 1) {
    return { from: coordinates[index], to: coordinates[index + 1] }
  }
  return { from: coordinates[index - 1], to: coordinates[index] }
}

/**
 * 地理方位角（北=0°、顺时针），仅作无地图实例时的回退。
 */
export const computeLineVertexBearing = (coordinates: [number, number][], index: number) => {
  const ends = getLineVertexSegmentEnds(coordinates, index)
  if (!ends) return 0
  const [fromLon, fromLat] = ends.from
  const [toLon, toLat] = ends.to
  return computeBearingDeg({ lat: fromLat, lng: fromLon }, { lat: toLat, lng: toLon })
}

/**
 * 按地图上的实际线段方向计算箭头 CSS 旋转角，避免经纬度方位与屏幕朝向偏差。
 */
export const computeLineVertexRotationDeg = (
  map: any,
  L: any,
  coordinates: [number, number][],
  index: number
): number => {
  const ends = getLineVertexSegmentEnds(coordinates, index)
  if (!ends) return 0
  if (map && L && typeof map.latLngToContainerPoint === 'function') {
    const from = L.latLng(ends.from[1], ends.from[0])
    const to = L.latLng(ends.to[1], ends.to[0])
    const p1 = map.latLngToContainerPoint(from)
    const p2 = map.latLngToContainerPoint(to)
    if (p1 && p2) {
      const dx = p2.x - p1.x
      const dy = p2.y - p1.y
      if (dx !== 0 || dy !== 0) {
        const screenDeg = (Math.atan2(dy, dx) * 180) / Math.PI
        return (screenDeg + ARROW_ICON_ROTATION_OFFSET + 360) % 360
      }
    }
  }
  return (computeLineVertexBearing(coordinates, index) + ARROW_ICON_ROTATION_OFFSET + 360) % 360
}

const computeSegmentRotationDeg = (map: any, L: any, latlngs: any[]) => {
  const coords: [number, number][] = [
    [latlngs[0].lng, latlngs[0].lat],
    [latlngs[1].lng, latlngs[1].lat]
  ]
  return computeLineVertexRotationDeg(map, L, coords, 0)
}

const resolveFlowArrowGeometry = (map: any, L: any, latlngs: any[], reverseFlow = false) => {
  const flowLatlngs = reverseFlow ? [latlngs[1], latlngs[0]] : latlngs
  return {
    start: flowLatlngs[0],
    end: flowLatlngs[1],
    rotationDeg: computeSegmentRotationDeg(map, L, flowLatlngs)
  }
}

export const buildArrowIconHtml = (rotationDeg: number, strokeColor = '#1677ff') => {
  const half = ARROW_VERTEX_SIZE / 2
  return `<svg class="tianditu-vertex-handle__arrow-svg" viewBox="0 0 22 22" width="${ARROW_VERTEX_SIZE}" height="${ARROW_VERTEX_SIZE}" style="transform: rotate(${rotationDeg}deg); transform-origin: ${half}px ${half}px">
    <path d="M11 3 L17 19 L11 15 L5 19 Z" fill="#ffffff" stroke="${strokeColor}" stroke-width="1.6" stroke-linejoin="round"/>
  </svg>`
}

export type LocationPinVariant = 'start' | 'end' | 'hazard'

export const buildLocationIconHtml = (
  label: string,
  variant: LocationPinVariant = 'start',
  expanded = false
) => {
  const text = escapeHtml(label.trim())
  const isEnd = variant === 'end'
  const src = isEnd ? endpointImg : startpointImg
  const imgClass = isEnd
    ? 'tianditu-vertex-handle__location-img is-end'
    : 'tianditu-vertex-handle__location-img is-start'
  const labelClass = `tianditu-vertex-handle__location-label is-${variant}${expanded ? ' is-expanded' : ''}`
  return `<div class="tianditu-vertex-handle__location-wrap${expanded ? ' is-expanded' : ''}">
    <img class="${imgClass}" src="${src}" width="${LOCATION_MARKER_IMAGE_SIZE}" height="${LOCATION_MARKER_IMAGE_SIZE}" alt="" draggable="false" />
    ${text ? `<div class="${labelClass}">${text}</div>` : ''}
  </div>`
}

const estimateLocationIconBox = (label: string, expanded: boolean) => {
  if (!expanded) {
    return {
      width: LOCATION_MARKER_IMAGE_SIZE,
      height: LOCATION_MARKER_IMAGE_SIZE,
      anchorY: LOCATION_MARKER_IMAGE_SIZE
    }
  }
  const textLen = label.trim().length
  const width = Math.min(280, Math.max(LOCATION_MARKER_IMAGE_SIZE + 8, textLen * 7 + 28))
  const lineCount = Math.max(1, Math.ceil(textLen / 14))
  const labelHeight = lineCount * 16 + 10
  return {
    width,
    height: LOCATION_MARKER_IMAGE_SIZE + labelHeight,
    anchorY: LOCATION_MARKER_IMAGE_SIZE
  }
}

export const createLocationDivIcon = (
  L: any,
  label: string,
  variant: LocationPinVariant,
  expanded = false
) => {
  const box = estimateLocationIconBox(label, expanded)
  return L.divIcon({
    className: `tianditu-vertex-handle tianditu-vertex-handle--location tianditu-vertex-handle--location-${variant}${
      expanded ? ' is-name-expanded' : ''
    }`,
    html: buildLocationIconHtml(label, variant, expanded),
    iconSize: [box.width, box.height],
    iconAnchor: [box.width / 2, box.anchorY],
    popupAnchor: [0, -box.anchorY]
  })
}

const computeBearingDeg = (from: { lat: number; lng: number }, to: { lat: number; lng: number }) => {
  const lat1 = (from.lat * Math.PI) / 180
  const lat2 = (to.lat * Math.PI) / 180
  const dLon = ((to.lng - from.lng) * Math.PI) / 180
  const y = Math.sin(dLon) * Math.cos(lat2)
  const x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
  return ((Math.atan2(y, x) * 180) / Math.PI + 360) % 360
}

const closestPointOnSegment = (
  p: { lat: number; lng: number },
  a: { lat: number; lng: number },
  b: { lat: number; lng: number }
) => {
  const ax = a.lng
  const ay = a.lat
  const bx = b.lng
  const by = b.lat
  const px = p.lng
  const py = p.lat
  const abx = bx - ax
  const aby = by - ay
  const apx = px - ax
  const apy = py - ay
  const abLen2 = abx * abx + aby * aby
  const t = abLen2 === 0 ? 0 : Math.max(0, Math.min(1, (apx * abx + apy * aby) / abLen2))
  return { lat: ay + aby * t, lng: ax + abx * t }
}

export type LineVertexEditorOptions = {
  L: any
  map: any
  /** 线段图层组（按段绘制，支持分色） */
  lineLayerGroup: any
  coordinates: [number, number][]
  vertexMarkers: VertexMarkerType[]
  riskSegments?: boolean[]
  riskSegmentNames?: string[]
  riskSegmentDescriptions?: string[]
  riskSegmentCenters?: RiskSegmentCenter[]
  /** 中间节点显示为指向性箭头 */
  lineVertexAsArrow?: boolean
  /** 起终点显示为位置图标（默认 true） */
  endpointAsLocation?: boolean
  vertexLabels?: string[]
  endpointLabels?: [string, string]
  /** 起点作为隐患点（橙色图钉，不再使用隐患段） */
  startAsHazardPoint?: boolean
  /** 禁止删除起点（共享隐患点模式） */
  lockStartVertex?: boolean
  /** 禁止拖动/删除终点（关联系统仓库） */
  lockEndVertex?: boolean
  /** 系统仓库固定坐标 [lon, lat] */
  lockedEndCoordinate?: [number, number]
  /** 起点坐标变更时回调（用于多线共享隐患点联动） */
  onStartCoordinateChange?: (coord: [number, number]) => void
  /** 允许点击线段标记风险隐患段 */
  segmentRiskMark?: boolean
  /** 多线路线序号，用于分色（0 起）；可被 routeColor 覆盖 */
  lineIndex?: number
  /** 路线颜色（优先于 lineIndex 配色，通常由仓库决定） */
  routeColor?: string
  /** 路线悬停颜色 */
  routeHoverColor?: string
  /** 关联的系统仓库 id */
  linkedWarehouseId?: string | number
  /** 路线终点与仓库重合时隐藏终点图标（由仓库 picker 层展示） */
  shouldHideRouteEndpointIcon?: (lon: number, lat: number) => boolean
  onChange: (payload: LineGeometryPayload) => void
  onSelect?: (index: number) => void
}

export type LineVertexEditorController = {
  refresh: () => void
  destroy: () => void
  setInsertMode: (enabled: boolean) => boolean
  deleteSelectedVertex: () => boolean
  deleteSegment: (segmentIndex: number) => boolean
  getSelectedIndex: () => number
  updateEndpointLabels: (labels: [string, string]) => void
  updateStartLabel: (label: string) => void
  setStartCoordinate: (coord: [number, number], emit?: boolean) => void
  setRiskSegment: (segmentIndex: number, isRisk: boolean) => void
  setRiskSegmentName: (segmentIndex: number, name: string) => void
  setRiskSegmentDescription: (segmentIndex: number, description: string) => void
  highlightSegment: (segmentIndex: number | null) => void
  setRouteActiveHighlight: (options?: RouteActiveHighlightOptions) => void
  getRiskSegments: () => boolean[]
  collapseExpandedName: () => void
  clearVertexSelection: () => void
  /** 更新系统仓库锁定状态与终点坐标并回正路线 */
  syncLockedEndCoordinate: (coord?: [number, number], linkedWarehouseId?: string | number) => void
}

export const attachLineVertexEditor = (options: LineVertexEditorOptions): LineVertexEditorController => {
  const {
    L,
    map,
    lineLayerGroup,
    lineVertexAsArrow = false,
    endpointAsLocation = true,
    segmentRiskMark = false,
    startAsHazardPoint = false,
    lockStartVertex = false,
    lockEndVertex: lockEndVertexOption = false,
    endpointLabels = ['起点', '终点'],
    onChange
  } = options
  let lockEndVertex = Boolean(
    lockEndVertexOption ||
      (options.linkedWarehouseId != null && String(options.linkedWarehouseId).trim())
  )
  let lockedEndCoordinate =
    Array.isArray(options.lockedEndCoordinate) && options.lockedEndCoordinate.length >= 2
      ? ([Number(options.lockedEndCoordinate[0]), Number(options.lockedEndCoordinate[1])] as [
          number,
          number
        ])
      : undefined
  if (
    lockedEndCoordinate &&
    (!Number.isFinite(lockedEndCoordinate[0]) || !Number.isFinite(lockedEndCoordinate[1]))
  ) {
    lockedEndCoordinate = undefined
  }
  const routeLineColor =
    options.routeColor?.trim() ||
    (options.lineIndex != null ? getRouteLineColor(options.lineIndex) : undefined)
  const routeLineHoverColor =
    options.routeHoverColor?.trim() ||
    (routeLineColor ? getRouteHoverColorFromBaseColor(routeLineColor) : undefined) ||
    (options.lineIndex != null ? getRouteLineHoverColor(options.lineIndex) : undefined)
  const linkedWarehouseId = options.linkedWarehouseId
  const effectiveSegmentRiskMark = segmentRiskMark && !startAsHazardPoint
  const isSegmentRisk = (segmentIndex: number) =>
    startAsHazardPoint ? false : Boolean(riskSegments[segmentIndex])
  const lineStyle: LineVertexStyleOptions = { middleAsArrow: lineVertexAsArrow, endpointAsLocation }
  const coordinates = options.coordinates.map((item) => [...item] as [number, number])
  const lineIndex = options.lineIndex ?? 0
  let currentEndpointLabels: [string, string] = startAsHazardPoint
    ? resolveHazardEndpointLabels(lineIndex, true, endpointLabels)
    : [
        endpointLabels[0] || '起点',
        endpointLabels[1] || '终点'
      ]
  const vertexMarkers = createVertexMarkers(coordinates.length, lineStyle, options.vertexMarkers)
  const vertexLabels = createVertexLabels(coordinates.length, options.vertexLabels || [], currentEndpointLabels)
  const riskSegments = createRiskSegments(
    Math.max(0, coordinates.length - 1),
    options.riskSegments || []
  )
  const riskSegmentNames = createRiskSegmentNames(riskSegments.length, options.riskSegmentNames || [])
  const riskSegmentDescriptions = createRiskSegmentDescriptions(
    riskSegments.length,
    options.riskSegmentDescriptions || []
  )
  let riskSegmentCenters = createRiskSegmentCenters(
    coordinates,
    riskSegments,
    options.riskSegmentCenters || []
  )
  if (startAsHazardPoint) {
    riskSegments.fill(false)
    riskSegmentNames.fill('')
    riskSegmentDescriptions.fill('')
    riskSegmentCenters = riskSegmentCenters.map(() => null)
  }
  let selectedIndex = -1
  let expandedNameVertexIndex: number | null = null
  let insertMode = false
  let draggingIndex = -1
  let highlightedSegmentIndex = -1
  let routeActiveHighlight = false
  let routeDimmedHighlight = false
  let routeFlashHighlight = false
  const vertexMarkerList: any[] = []
  const midpointMarkerList: any[] = []
  const segmentPolylineList: SegmentLayerPair[] = []
  const editorFlowArrowHandles: SegmentFlowArrowsHandle[] = []
  const vertexLayerGroup = L.layerGroup().addTo(map)
  const midpointLayerGroup = L.layerGroup().addTo(map)

  const newVertexMarkerType = (): VertexMarkerType => (lineVertexAsArrow ? 'arrow' : 'default')

  const normalizeEndpointMarkers = () => {
    for (let i = 0; i < coordinates.length; i += 1) {
      vertexMarkers[i] = resolveVertexMarkerType(i, coordinates.length, lineStyle, vertexMarkers[i])
    }
  }

  const syncEndpointLabels = () => {
    if (coordinates.length === 0) return
    const defaultStart = currentEndpointLabels[0] || (startAsHazardPoint ? DEFAULT_HAZARD_POINT_NAME : '起点')
    const defaultEnd =
      currentEndpointLabels[1] ||
      (startAsHazardPoint ? getDefaultWarehouseName(lineIndex) : '终点')
    vertexLabels[0] = (vertexLabels[0] || defaultStart).trim() || defaultStart
    if (coordinates.length > 1) {
      const last = coordinates.length - 1
      vertexLabels[last] = (vertexLabels[last] || defaultEnd).trim() || defaultEnd
    }
    while (vertexLabels.length < coordinates.length) vertexLabels.push('')
    vertexLabels.length = coordinates.length
  }

  const getVertexMarkerType = (index: number): VertexMarkerType =>
    resolveVertexMarkerType(index, coordinates.length, lineStyle, vertexMarkers[index])

  const isArrowVertex = (index: number) => getVertexMarkerType(index) === 'arrow'
  const isLocationVertex = (index: number) => getVertexMarkerType(index) === 'location'

  const ensureSegmentMetaLength = () => {
    const expected = Math.max(0, coordinates.length - 1)
    while (riskSegmentNames.length < expected) riskSegmentNames.push('')
    while (riskSegmentDescriptions.length < expected) riskSegmentDescriptions.push('')
    while (riskSegmentCenters.length < expected) riskSegmentCenters.push(null)
    riskSegmentNames.length = expected
    riskSegmentDescriptions.length = expected
    riskSegmentCenters.length = expected
    while (riskSegments.length < expected) riskSegments.push(false)
    riskSegments.length = expected
  }

  const recomputeRiskSegmentCenters = () => {
    ensureSegmentMetaLength()
    riskSegmentCenters = syncRiskSegmentCenters(coordinates, riskSegments)
  }

  const buildChangePayload = (): LineGeometryPayload => {
    if (startAsHazardPoint) {
      ensureSegmentMetaLength()
      riskSegments.fill(false)
      riskSegmentNames.fill('')
      riskSegmentDescriptions.fill('')
      riskSegmentCenters = riskSegmentCenters.map(() => null)
    } else {
      recomputeRiskSegmentCenters()
    }
    return {
      coordinates: coordinates.map((item) => [...item] as [number, number]),
      vertexMarkers: [...vertexMarkers],
      vertexLabels: [...vertexLabels],
      riskSegments: [...riskSegments],
      riskSegmentNames: [...riskSegmentNames],
      riskSegmentDescriptions: [...riskSegmentDescriptions],
      riskSegmentCenters: riskSegmentCenters.map((center) => (center ? ([...center] as [number, number]) : null)),
      ...(routeLineColor ? { routeColor: routeLineColor } : {}),
      ...(linkedWarehouseId != null && String(linkedWarehouseId).trim() ? { linkedWarehouseId } : {})
    }
  }

  const isEndVertexIndex = (index: number) =>
    index === coordinates.length - 1 && coordinates.length >= 2

  /** 系统仓库终点：关联后一律禁止拖动（不依赖 lockedEndCoordinate 是否已解析） */
  const isEndVertexDragDisabled = (index: number) =>
    shouldHideEndLocationIcon(index) || (isEndVertexIndex(index) && lockEndVertex)

  /** 终点由仓库图层展示时，不在路线层再展开名称（避免与仓库图标重影） */
  const canToggleVertexNameOnClick = (index: number) =>
    !shouldHideEndLocationIcon(index) && isLocationVertex(index)

  const clampLockedEndCoordinate = (): boolean => {
    if (!lockEndVertex || !lockedEndCoordinate) return false
    const last = coordinates.length - 1
    if (last < 1) return false
    const [lon, lat] = lockedEndCoordinate
    const prev = coordinates[last]
    const changed =
      !prev || Math.abs(prev[0] - lon) >= 1e-8 || Math.abs(prev[1] - lat) >= 1e-8
    coordinates[last] = [lon, lat]
    const marker = vertexMarkerList[last]
    if (marker?.setLatLng) {
      marker.setLatLng(L.latLng(lat, lon))
    }
    return changed
  }

  const emitChange = () => {
    clampLockedEndCoordinate()
    onChange(buildChangePayload())
  }

  const applySegmentStyle = (pair: SegmentLayerPair, segmentIndex: number) => {
    if (!pair?.base) return
    const isRisk = isSegmentRisk(segmentIndex)
    const isHover = highlightedSegmentIndex === segmentIndex
    const a = coordinates[segmentIndex]
    const b = coordinates[segmentIndex + 1]
    const latlngs = [L.latLng(a[1], a[0]), L.latLng(b[1], b[0])]
    applySegmentLayerStyles(L, map, pair, latlngs, isRisk, isHover, routeLineColor, routeLineHoverColor, {
      active: routeActiveHighlight,
      dimmed: routeDimmedHighlight,
      flash: routeFlashHighlight
    })
  }

  const syncFlowArrowHighlight = () => {
    const active = routeActiveHighlight
    const flash = routeFlashHighlight
    editorFlowArrowHandles.forEach((handle) => {
      handle.speed = active ? FLOW_ARROW_SPEED * (flash ? 3.8 : 2.6) : FLOW_ARROW_SPEED
      handle.markers.forEach(({ marker }) => {
        marker?.setZIndexOffset?.(active ? 1150 : 950)
        const el = marker?.getElement?.() as HTMLElement | undefined
        el?.classList.toggle('is-route-active', active)
        el?.classList.toggle('is-route-flash', active && flash)
      })
    })
  }

  const refreshAllSegmentStyles = () => {
    segmentPolylineList.forEach((pair, index) => applySegmentStyle(pair, index))
  }

  const bindSegmentEvents = (layer: any, segmentIndex: number) => {
    if (!effectiveSegmentRiskMark) return
    layer.on('click', (event: any) => {
      L.DomEvent.stopPropagation(event)
      riskSegments[segmentIndex] = !riskSegments[segmentIndex]
      applySegmentStyle(segmentPolylineList[segmentIndex]!, segmentIndex)
      emitChange()
      ElMessage.success(riskSegments[segmentIndex] ? '已标记为风险隐患段' : '已取消风险隐患段标记')
    })
  }

  const rebuildSegmentPolylines = () => {
    unregisterSegmentFlowArrows(editorFlowArrowHandles)
    editorFlowArrowHandles.length = 0
    lineLayerGroup.clearLayers()
    segmentPolylineList.length = 0
    if (coordinates.length < 2) return
    for (let i = 0; i < coordinates.length - 1; i += 1) {
      const a = coordinates[i]
      const b = coordinates[i + 1]
      const latlngs = [L.latLng(a[1], a[0]), L.latLng(b[1], b[0])]
      const isRisk = isSegmentRisk(i)
      const isHover = highlightedSegmentIndex === i
      const pair = createSegmentLineLayers(L, latlngs, isRisk, isHover, {
        interactive: effectiveSegmentRiskMark,
        clickableClassName: effectiveSegmentRiskMark ? 'tianditu-line-segment--clickable' : '',
        map,
        group: lineLayerGroup,
        routeColor: routeLineColor,
        routeHoverColor: routeLineHoverColor,
        reverseFlow: startAsHazardPoint
      })
      bindSegmentEvents(pair.base, i)
      lineLayerGroup.addLayer(pair.base)
      segmentPolylineList.push(pair)
      if (pair.flowArrows) {
        editorFlowArrowHandles.push(pair.flowArrows)
      }
    }
    if (flowArrowRegistry.size > 0 && flowArrowAnimId == null) {
      ensureFlowArrowAnimation(L)
    }
    syncFlowArrowHighlight()
  }

  const syncSegmentPolylines = (forceRebuild: boolean) => {
    const expected = Math.max(0, coordinates.length - 1)
    if (forceRebuild || segmentPolylineList.length !== expected) {
      ensureSegmentMetaLength()
      rebuildSegmentPolylines()
      return
    }
    for (let i = 0; i < expected; i += 1) {
      const a = coordinates[i]
      const b = coordinates[i + 1]
      const latlngs = [L.latLng(a[1], a[0]), L.latLng(b[1], b[0])]
      const isRisk = isSegmentRisk(i)
      const pair = segmentPolylineList[i]!
      const prevFlow = pair.flowArrows
      syncSegmentLayerLatLngs(
        L,
        map,
        pair,
        latlngs,
        isRisk,
        routeLineColor,
        lineLayerGroup,
        startAsHazardPoint
      )
      if (!prevFlow && pair.flowArrows) {
        editorFlowArrowHandles.push(pair.flowArrows)
      } else if (prevFlow && !pair.flowArrows) {
        unregisterSegmentFlowArrows([prevFlow])
        const flowIdx = editorFlowArrowHandles.indexOf(prevFlow)
        if (flowIdx >= 0) editorFlowArrowHandles.splice(flowIdx, 1)
      }
      applySegmentStyle(pair, i)
    }
  }

  const updateArrowRotationOnMarker = (marker: any, index: number) => {
    if (!marker || !isArrowVertex(index)) return
    const el = marker.getElement()?.querySelector('.tianditu-vertex-handle__arrow-svg') as HTMLElement | null
    if (!el) return
    const deg = computeLineVertexRotationDeg(map, L, coordinates, index)
    el.style.transform = `rotate(${deg}deg)`
    el.style.transformOrigin = `${ARROW_VERTEX_SIZE / 2}px ${ARROW_VERTEX_SIZE / 2}px`
  }

  const updateArrowRotationsAround = (centerIndex: number) => {
    ;[centerIndex - 1, centerIndex, centerIndex + 1].forEach((index) => {
      if (index < 0 || index >= coordinates.length) return
      updateArrowRotationOnMarker(vertexMarkerList[index], index)
    })
  }

  const applySelectedStyle = () => {
    const accent = routeLineColor || '#1677ff'
    vertexMarkerList.forEach((marker, index) => {
      const el = marker?.getElement?.()
      if (!el) return
      el.style.setProperty('--route-line-color', accent)
      if (index === selectedIndex) el.classList.add('is-selected')
      else el.classList.remove('is-selected')
    })
  }

  const createArrowIcon = (index: number) =>
    L.divIcon({
      className: 'tianditu-vertex-handle tianditu-vertex-handle--arrow',
      html: buildArrowIconHtml(
        computeLineVertexRotationDeg(map, L, coordinates, index),
        routeLineColor || '#1677ff'
      ),
      iconSize: [ARROW_VERTEX_SIZE, ARROW_VERTEX_SIZE],
      iconAnchor: [ARROW_VERTEX_SIZE / 2, ARROW_VERTEX_SIZE / 2]
    })

  const resolveVertexDisplayName = (index: number) => {
    const defaultLabel =
      index === 0
        ? currentEndpointLabels[0] || (startAsHazardPoint ? DEFAULT_HAZARD_POINT_NAME : '起点')
        : currentEndpointLabels[1] || (startAsHazardPoint ? getDefaultWarehouseName(lineIndex) : '终点')
    return (vertexLabels[index] || defaultLabel).trim() || defaultLabel
  }

  const createLocationIcon = (index: number) => {
    const variant: LocationPinVariant =
      index === 0 ? (startAsHazardPoint ? 'hazard' : 'start') : 'end'
    const expanded = expandedNameVertexIndex === index
    return createLocationDivIcon(L, resolveVertexDisplayName(index), variant, expanded)
  }

  const createDefaultIcon = () =>
    L.divIcon({
      className: 'tianditu-vertex-handle tianditu-vertex-handle--default',
      html: '<div class="tianditu-vertex-handle__dot"></div>',
      iconSize: [14, 14],
      iconAnchor: [7, 7]
    })

  const createMidpointIcon = () =>
    L.divIcon({
      className: 'tianditu-vertex-handle tianditu-vertex-handle--mid',
      html: '<div class="tianditu-vertex-handle__mid">+</div>',
      iconSize: [12, 12],
      iconAnchor: [6, 6]
    })

  /** 仅在与仓库图层重复展示时隐藏路线终点；系统仓库锁定只禁拖，不隐藏图标 */
  const shouldHideEndLocationIcon = (index: number) => {
    if (index !== coordinates.length - 1 || coordinates.length < 2) return false
    if (getVertexMarkerType(index) !== 'location') return false
    const [lon, lat] = coordinates[index]
    return options.shouldHideRouteEndpointIcon?.(lon, lat) ?? false
  }

  const createWarehouseProxyIcon = () =>
    L.divIcon({
      className: 'tianditu-vertex-handle tianditu-vertex-handle--warehouse-proxy',
      html: '<div class="tianditu-vertex-handle__warehouse-proxy"></div>',
      iconSize: [42, 42],
      iconAnchor: [21, 21]
    })

  const refreshVertexMarkerIcons = () => {
    vertexMarkerList.forEach((marker, index) => {
      marker?.setIcon?.(createVertexIcon(index))
      const expanded = expandedNameVertexIndex === index
      marker?.setZIndexOffset?.(
        expanded ? 1400 : selectedIndex === index ? 1200 : isLocationVertex(index) ? 1100 : 1000
      )
    })
  }

  const toggleVertexNameExpanded = (index: number) => {
    expandedNameVertexIndex = expandedNameVertexIndex === index ? null : index
    refreshVertexMarkerIcons()
  }

  const createVertexIcon = (index: number) => {
    if (shouldHideEndLocationIcon(index)) {
      return createWarehouseProxyIcon()
    }
    const expanded = expandedNameVertexIndex === index
    const type = getVertexMarkerType(index)
    if (type === 'location') return createLocationIcon(index)
    if (type === 'arrow') return createArrowIcon(index)
    return createDefaultIcon()
  }

  const bindVertexMarkerEvents = (marker: any, index: number) => {
    marker.on('dragstart', () => {
      if (isEndVertexDragDisabled(index)) return
      draggingIndex = index
      marker.setZIndexOffset(1300)
    })
    marker.on('drag', () => {
      if (isEndVertexDragDisabled(index)) {
        if (isEndVertexIndex(index) && lockEndVertex) {
          clampLockedEndCoordinate()
          syncSegmentPolylines(false)
        }
        return
      }
      const next = marker.getLatLng()
      coordinates[index] = [next.lng, next.lat]
      syncSegmentPolylines(false)
      updateArrowRotationsAround(index)
      syncMidpointMarkers(false)
    })
    marker.on('dragend', () => {
      if (isEndVertexDragDisabled(index)) {
        if (isEndVertexIndex(index) && lockEndVertex) {
          clampLockedEndCoordinate()
          syncSegmentPolylines(false)
          emitChange()
        }
        draggingIndex = -1
        return
      }
      const next = marker.getLatLng()
      coordinates[index] = [next.lng, next.lat]
      clampLockedEndCoordinate()
      draggingIndex = -1
      marker.setZIndexOffset(selectedIndex === index ? 1200 : 1000)
      if (index === coordinates.length - 1) {
        marker.setIcon(createVertexIcon(index))
      }
      syncSegmentPolylines(false)
      if (index === 0) {
        options.onStartCoordinateChange?.([next.lng, next.lat])
      }
      emitChange()
      syncMidpointMarkers(false)
      applySelectedStyle()
    })
    marker.on('click', (event: any) => {
      L.DomEvent.stopPropagation(event)
      L.DomEvent.preventDefault(event)
      if (canToggleVertexNameOnClick(index)) {
        toggleVertexNameExpanded(index)
      }
      if (!shouldHideEndLocationIcon(index)) {
        selectVertex(index)
      }
    })
  }

  const rebuildVertexMarkers = () => {
    normalizeEndpointMarkers()
    syncEndpointLabels()
    vertexLayerGroup.clearLayers()
    vertexMarkerList.length = 0
    coordinates.forEach(([lon, lat], index) => {
      const dragDisabled = isEndVertexDragDisabled(index)
      const endHiddenForWarehouse = shouldHideEndLocationIcon(index)
      const marker = L.marker(L.latLng(lat, lon), {
        icon: createVertexIcon(index),
        draggable: !dragDisabled,
        interactive: !endHiddenForWarehouse,
        bubblingMouseEvents: !endHiddenForWarehouse,
        zIndexOffset: endHiddenForWarehouse
          ? 500
          : selectedIndex === index
            ? 1200
            : isLocationVertex(index)
              ? 1100
              : 1000
      })
      bindVertexMarkerEvents(marker, index)
      if (dragDisabled) {
        marker.dragging?.disable?.()
      } else {
        marker.dragging?.enable?.()
      }
      vertexLayerGroup.addLayer(marker)
      vertexMarkerList.push(marker)
    })
    applySelectedStyle()
  }

  const rebuildMidpointMarkers = () => {
    midpointLayerGroup.clearLayers()
    midpointMarkerList.length = 0
    if (coordinates.length < 2) return
    for (let i = 0; i < coordinates.length - 1; i += 1) {
      const a = coordinates[i]
      const b = coordinates[i + 1]
      const mid = L.latLng((a[1] + b[1]) / 2, (a[0] + b[0]) / 2)
      const segmentIndex = i
      const marker = L.marker(mid, { icon: createMidpointIcon(), draggable: false, zIndexOffset: 900 })
      marker.on('click', (event: any) => {
        L.DomEvent.stopPropagation(event)
        coordinates.splice(segmentIndex + 1, 0, [mid.lng, mid.lat])
        vertexMarkers.splice(segmentIndex + 1, 0, newVertexMarkerType())
        vertexLabels.splice(segmentIndex + 1, 0, '')
        riskSegments.splice(segmentIndex, 1, Boolean(riskSegments[segmentIndex]), false)
        riskSegmentNames.splice(segmentIndex, 1, riskSegmentNames[segmentIndex] || '', '')
        riskSegmentDescriptions.splice(segmentIndex, 1, riskSegmentDescriptions[segmentIndex] || '', '')
        riskSegmentCenters.splice(segmentIndex, 1, riskSegmentCenters[segmentIndex], null)
        refresh()
      })
      midpointLayerGroup.addLayer(marker)
      midpointMarkerList.push(marker)
    }
  }

  /** 仅更新中点位置，拖动过程中不销毁图层 */
  const syncMidpointMarkers = (forceRebuild: boolean) => {
    const expected = Math.max(0, coordinates.length - 1)
    if (forceRebuild || midpointMarkerList.length !== expected) {
      rebuildMidpointMarkers()
      return
    }
    for (let i = 0; i < expected; i += 1) {
      const a = coordinates[i]
      const b = coordinates[i + 1]
      midpointMarkerList[i]?.setLatLng?.(L.latLng((a[1] + b[1]) / 2, (a[0] + b[0]) / 2))
    }
  }

  const syncOverlayPositions = () => {
    if (vertexMarkerList.length !== coordinates.length) {
      refresh()
      return
    }
    coordinates.forEach(([lon, lat], index) => {
      vertexMarkerList[index]?.setLatLng?.(L.latLng(lat, lon))
      updateArrowRotationOnMarker(vertexMarkerList[index], index)
    })
    syncMidpointMarkers(false)
    syncSegmentPolylines(false)
  }

  const selectVertex = (index: number) => {
    selectedIndex = index
    options.onSelect?.(index)
    vertexMarkerList.forEach((marker, i) => {
      marker?.setZIndexOffset?.(i === selectedIndex ? 1200 : 1000)
    })
    applySelectedStyle()
  }

  const refresh = () => {
    syncSegmentPolylines(true)
    rebuildVertexMarkers()
    rebuildMidpointMarkers()
  }

  const findInsertIndex = (latlng: { lat: number; lng: number }) => {
    let bestIndex = 0
    let bestDistance = Number.POSITIVE_INFINITY
    for (let i = 0; i < coordinates.length - 1; i += 1) {
      const a = { lat: coordinates[i][1], lng: coordinates[i][0] }
      const b = { lat: coordinates[i + 1][1], lng: coordinates[i + 1][0] }
      const projected = closestPointOnSegment(latlng, a, b)
      const dx = latlng.lng - projected.lng
      const dy = latlng.lat - projected.lat
      const distance = dx * dx + dy * dy
      if (distance < bestDistance) {
        bestDistance = distance
        bestIndex = i + 1
      }
    }
    return bestIndex
  }

  const onMapClick = (event: any) => {
    if (!insertMode) return
    const latlng = event.latlng
    const index = findInsertIndex(latlng)
    coordinates.splice(index, 0, [latlng.lng, latlng.lat])
    vertexMarkers.splice(index, 0, newVertexMarkerType())
    vertexLabels.splice(index, 0, '')
    if (index > 0) {
      riskSegments.splice(index - 1, 1, Boolean(riskSegments[index - 1]), false)
      riskSegmentNames.splice(index - 1, 1, riskSegmentNames[index - 1] || '', '')
      riskSegmentDescriptions.splice(index - 1, 1, riskSegmentDescriptions[index - 1] || '', '')
      riskSegmentCenters.splice(index - 1, 1, riskSegmentCenters[index - 1], null)
    }
    insertMode = false
    map.getContainer().style.cursor = ''
    emitChange()
    refresh()
  }

  const onMapViewChange = () => {
    if (draggingIndex >= 0) {
      updateArrowRotationsAround(draggingIndex)
      syncMidpointMarkers(false)
      syncSegmentPolylines(false)
      return
    }
    syncOverlayPositions()
    syncSegmentPolylines(false)
  }

  const isVertexHandleMapClick = (event: any) => {
    const target = (event?.originalEvent?.target || event?.target) as HTMLElement | undefined
    return Boolean(
      target?.closest?.('.tianditu-vertex-handle') ||
        target?.closest?.('.tianditu-warehouse-picker-marker')
    )
  }

  const onMapCollapseExpandedName = (event: any) => {
    if (isVertexHandleMapClick(event)) return
    if (expandedNameVertexIndex == null) return
    expandedNameVertexIndex = null
    refreshVertexMarkerIcons()
  }

  map.on('click', onMapClick)
  map.on('click', onMapCollapseExpandedName)
  map.on('zoomend', onMapViewChange)
  map.on('moveend', onMapViewChange)

  refresh()

  const deleteVertexAt = (idx: number): boolean => {
    if (idx === 0 && lockStartVertex) return false
    if (isEndVertexIndex(idx) && lockEndVertex) return false
    if (idx < 0 || idx >= coordinates.length || coordinates.length <= 2) return false
    const pointCount = coordinates.length
    coordinates.splice(idx, 1)
    vertexMarkers.splice(idx, 1)
    vertexLabels.splice(idx, 1)
    if (idx === 0) {
      riskSegments.splice(0, 1)
      riskSegmentNames.splice(0, 1)
      riskSegmentDescriptions.splice(0, 1)
      riskSegmentCenters.splice(0, 1)
    } else if (idx === pointCount - 1) {
      riskSegments.splice(pointCount - 2, 1)
      riskSegmentNames.splice(pointCount - 2, 1)
      riskSegmentDescriptions.splice(pointCount - 2, 1)
      riskSegmentCenters.splice(pointCount - 2, 1)
    } else {
      const mergedRisk = Boolean(riskSegments[idx - 1]) || Boolean(riskSegments[idx])
      const mergedName = mergedRisk ? riskSegmentNames[idx - 1] || riskSegmentNames[idx] || '' : ''
      const mergedDescription = mergedRisk
        ? riskSegmentDescriptions[idx - 1] || riskSegmentDescriptions[idx] || ''
        : ''
      riskSegments.splice(idx - 1, 2, mergedRisk)
      riskSegmentNames.splice(idx - 1, 2, mergedName)
      riskSegmentDescriptions.splice(idx - 1, 2, mergedDescription)
      riskSegmentCenters.splice(idx - 1, 2, null)
    }
    refresh()
    emitChange()
    return true
  }

  return {
    refresh,
    destroy: () => {
      map.off('click', onMapClick)
      map.off('click', onMapCollapseExpandedName)
      map.off('zoomend', onMapViewChange)
      map.off('moveend', onMapViewChange)
      unregisterSegmentFlowArrows(editorFlowArrowHandles)
      editorFlowArrowHandles.length = 0
      vertexMarkerList.length = 0
      midpointMarkerList.length = 0
      segmentPolylineList.length = 0
      lineLayerGroup.clearLayers()
      vertexLayerGroup.clearLayers()
      midpointLayerGroup.clearLayers()
      map.removeLayer(vertexLayerGroup)
      map.removeLayer(midpointLayerGroup)
    },
    setInsertMode: (enabled: boolean) => {
      insertMode = enabled
      map.getContainer().style.cursor = enabled ? 'crosshair' : ''
      return insertMode
    },
    deleteSelectedVertex: () => {
      if (selectedIndex < 0) return false
      const ok = deleteVertexAt(selectedIndex)
      if (ok) selectedIndex = -1
      return ok
    },
    deleteSegment: (segmentIndex: number) => {
      if (segmentIndex < 0 || segmentIndex >= riskSegments.length) return false
      if (coordinates.length <= 2) return false
      const ok = deleteVertexAt(segmentIndex + 1)
      if (ok) selectedIndex = -1
      return ok
    },
    getSelectedIndex: () => selectedIndex,
    updateEndpointLabels: (labels: [string, string]) => {
      const defaultStart = currentEndpointLabels[0] || (startAsHazardPoint ? DEFAULT_HAZARD_POINT_NAME : '起点')
      const defaultEnd =
        currentEndpointLabels[1] || (startAsHazardPoint ? getDefaultWarehouseName(lineIndex) : '终点')
      currentEndpointLabels = [
        (labels[0] || defaultStart).trim() || defaultStart,
        (labels[1] || defaultEnd).trim() || defaultEnd
      ]
      if (coordinates.length === 0) return
      vertexLabels[0] = currentEndpointLabels[0]
      if (coordinates.length > 1) {
        vertexLabels[coordinates.length - 1] = currentEndpointLabels[1]
      }
      rebuildVertexMarkers()
      emitChange()
    },
    updateStartLabel: (label: string) => {
      const defaultStart = currentEndpointLabels[0] || (startAsHazardPoint ? DEFAULT_HAZARD_POINT_NAME : '起点')
      currentEndpointLabels[0] = (label || defaultStart).trim() || defaultStart
      if (coordinates.length === 0) return
      vertexLabels[0] = currentEndpointLabels[0]
      rebuildVertexMarkers()
      emitChange()
    },
    setStartCoordinate: (coord: [number, number], emit = true) => {
      if (coordinates.length === 0) return
      coordinates[0] = [...coord]
      syncOverlayPositions()
      if (emit) emitChange()
    },
    setRiskSegment: (segmentIndex: number, isRisk: boolean) => {
      if (startAsHazardPoint) return
      if (segmentIndex < 0 || segmentIndex >= riskSegments.length) return
      riskSegments[segmentIndex] = isRisk
      if (!isRisk) {
        riskSegmentNames[segmentIndex] = ''
        riskSegmentDescriptions[segmentIndex] = ''
        riskSegmentCenters[segmentIndex] = null
      } else {
        riskSegmentCenters[segmentIndex] = computeSegmentCenter(coordinates, segmentIndex)
      }
      applySegmentStyle(segmentPolylineList[segmentIndex], segmentIndex)
      emitChange()
    },
    setRiskSegmentName: (segmentIndex: number, name: string) => {
      if (startAsHazardPoint) return
      if (segmentIndex < 0 || segmentIndex >= riskSegments.length) return
      if (!riskSegments[segmentIndex]) return
      riskSegmentNames[segmentIndex] = name.trim()
      riskSegmentCenters[segmentIndex] = computeSegmentCenter(coordinates, segmentIndex)
      emitChange()
    },
    setRiskSegmentDescription: (segmentIndex: number, description: string) => {
      if (startAsHazardPoint) return
      if (segmentIndex < 0 || segmentIndex >= riskSegments.length) return
      if (!riskSegments[segmentIndex]) return
      riskSegmentDescriptions[segmentIndex] = description.trim()
      emitChange()
    },
    highlightSegment: (segmentIndex: number | null) => {
      highlightedSegmentIndex = segmentIndex === null ? -1 : segmentIndex
      refreshAllSegmentStyles()
    },
    setRouteActiveHighlight: (options: RouteActiveHighlightOptions = {}) => {
      routeActiveHighlight = Boolean(options.active)
      routeDimmedHighlight = Boolean(options.dimmed)
      routeFlashHighlight = Boolean(options.active && options.flash)
      refreshAllSegmentStyles()
      syncFlowArrowHighlight()
      if (routeActiveHighlight) {
        lineLayerGroup.bringToFront?.()
        vertexLayerGroup.bringToFront?.()
      }
    },
    getRiskSegments: () => [...riskSegments],
    collapseExpandedName: () => {
      if (expandedNameVertexIndex == null) return
      expandedNameVertexIndex = null
      refreshVertexMarkerIcons()
    },
    clearVertexSelection: () => {
      if (selectedIndex < 0) return
      selectedIndex = -1
      vertexMarkerList.forEach((marker, i) => {
        marker?.setZIndexOffset?.(expandedNameVertexIndex === i ? 1400 : isLocationVertex(i) ? 1100 : 1000)
      })
      applySelectedStyle()
    },
    /** 仅回正终点并刷新图层，不触发 onChange（避免与仓库图层刷新互相递归） */
    syncLockedEndCoordinate: (coord?: [number, number], nextLinkedWarehouseId?: string | number) => {
      const hasLinkedId =
        nextLinkedWarehouseId != null && String(nextLinkedWarehouseId).trim().length > 0
      lockEndVertex = Boolean(lockEndVertexOption || hasLinkedId)
      if (coord && coord.length >= 2 && Number.isFinite(coord[0]) && Number.isFinite(coord[1])) {
        lockedEndCoordinate = [Number(coord[0]), Number(coord[1])]
      }
      if (!lockEndVertex) {
        lockedEndCoordinate = undefined
      }
      clampLockedEndCoordinate()
      refresh()
    }
  }
}
