<template>
  <div class="pond-spatial-map">
    <div class="pond-spatial-map__toolbar">
      <el-button
        size="small"
        type="warning"
        :disabled="!mapReady || !hasGeometry || editing"
        @click="enableEdit"
      >
        编辑
      </el-button>
      <el-button size="small" type="primary" :disabled="!editing" @click="finishEdit">
        完成编辑
      </el-button>
      <span v-if="editing" class="pond-spatial-map__hint">拖动白色节点调整范围，中间半透明点可插入新节点</span>
    </div>
    <div ref="mapRef" class="pond-spatial-map__canvas" :style="{ height: normalizedHeight }"></div>
    <div v-if="!hasGeometry" class="pond-spatial-map__empty" :style="{ height: normalizedHeight }">
      <el-empty description="暂无矢量面数据" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getTiandituKey } from '@/components/Gis/tiandituKey'
import { createTiandituImageryLayerGroup } from '@/components/Gis/tiandituLayers'

type RingLatLng = { lat: number; lng: number }

const props = withDefaults(
  defineProps<{
    modelValue?: string
    active?: boolean
    height?: number | string
    center?: [number, number]
    zoom?: number
  }>(),
  {
    modelValue: '',
    active: false,
    height: 480,
    center: () => [32.39, 119.16],
    zoom: 14
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const mapRef = ref<HTMLDivElement>()
const mapReady = ref(false)
const editing = ref(false)

let Leaflet: any = null
let mapInstance: any = null
/** 可编辑的 Polygon 图层（不含 MultiPolygon） */
let polygonLayers: any[] = []
/** 编辑态顶点/中点标记 */
let vertexMarkers: any[] = []

const POLYGON_STYLE = {
  color: '#1677ff',
  weight: 3,
  fillColor: '#1677ff',
  fillOpacity: 0.28
}

const EDIT_POLYGON_STYLE = {
  color: '#fa8c16',
  weight: 3,
  fillColor: '#fa8c16',
  fillOpacity: 0.22
}

const normalizedHeight = computed(() => {
  const h = props.height
  return typeof h === 'number' ? `${h}px` : h
})

const hasGeometry = computed(() => Boolean(String(props.modelValue || '').trim()))

const loadStyleOnce = (id: string, href: string) =>
  new Promise<void>((resolve, reject) => {
    if (document.getElementById(id)) {
      resolve()
      return
    }
    const link = document.createElement('link')
    link.id = id
    link.rel = 'stylesheet'
    link.href = href
    link.onload = () => resolve()
    link.onerror = () => reject(new Error(`样式加载失败：${href}`))
    document.head.appendChild(link)
  })

const loadScriptOnce = (id: string, src: string) =>
  new Promise<void>((resolve, reject) => {
    if (document.getElementById(id)) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.id = id
    script.src = src
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error(`脚本加载失败：${src}`))
    document.body.appendChild(script)
  })

const loadLeafletAssets = async () => {
  if ((window as any).L) return
  await loadStyleOnce('leaflet-style', 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css')
  await loadScriptOnce('leaflet-script', 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js')
  if (!(window as any).L) {
    throw new Error('Leaflet 加载失败')
  }
}

/** 从 GeoJSON 提取外环列表：每个元素为 [lon, lat][] */
const extractOuterRings = (raw: string): number[][][] => {
  const parsed = JSON.parse(raw)
  if (!parsed || typeof parsed !== 'object') return []

  let geometry = parsed
  if (parsed.type === 'Feature') geometry = parsed.geometry
  else if (parsed.type === 'FeatureCollection') {
    geometry = parsed.features?.[0]?.geometry
  }
  if (!geometry?.coordinates) return []

  const type = String(geometry.type || '')
  if (type === 'Polygon') {
    const outer = geometry.coordinates?.[0]
    return Array.isArray(outer) && outer.length >= 3 ? [outer] : []
  }
  if (type === 'MultiPolygon') {
    const rings: number[][][] = []
    for (const poly of geometry.coordinates || []) {
      const outer = poly?.[0]
      if (Array.isArray(outer) && outer.length >= 3) rings.push(outer)
    }
    return rings
  }
  return []
}

/** 去掉闭合重复点，转为 Leaflet LatLng 数组 */
const coordsToLatLngs = (ring: number[][]): RingLatLng[] => {
  const points: RingLatLng[] = []
  for (const pt of ring) {
    if (!Array.isArray(pt) || pt.length < 2) continue
    const lon = Number(pt[0])
    const lat = Number(pt[1])
    if (!Number.isFinite(lon) || !Number.isFinite(lat)) continue
    points.push({ lat, lng: lon })
  }
  // 去掉首尾闭合重复点
  if (points.length >= 2) {
    const first = points[0]
    const last = points[points.length - 1]
    if (first.lat === last.lat && first.lng === last.lng) {
      points.pop()
    }
  }
  return points
}

const clearVertexMarkers = () => {
  for (const marker of vertexMarkers) {
    try {
      mapInstance?.removeLayer(marker)
    } catch {
      // ignore
    }
  }
  vertexMarkers = []
}

const clearPolygonLayers = () => {
  clearVertexMarkers()
  for (const layer of polygonLayers) {
    try {
      mapInstance?.removeLayer(layer)
    } catch {
      // ignore
    }
  }
  polygonLayers = []
}

const exportGeometryGeoJson = (): string => {
  const polygons: number[][][][] = []
  for (const layer of polygonLayers) {
    const latlngs = layer.getLatLngs?.()
    // 单环 Polygon：[[LatLng,...]] 或 [LatLng,...]
    let ring: any[] = []
    if (Array.isArray(latlngs?.[0]) && typeof latlngs[0][0]?.lat === 'number') {
      ring = latlngs[0]
    } else if (Array.isArray(latlngs) && typeof latlngs[0]?.lat === 'number') {
      ring = latlngs
    }
    if (ring.length < 3) continue
    const coords = ring.map((p: any) => [Number(p.lng), Number(p.lat)])
    const first = coords[0]
    const last = coords[coords.length - 1]
    if (first[0] !== last[0] || first[1] !== last[1]) {
      coords.push([first[0], first[1]])
    }
    polygons.push([coords])
  }
  if (!polygons.length) return ''
  if (polygons.length === 1) {
    return JSON.stringify({ type: 'Polygon', coordinates: polygons[0] })
  }
  return JSON.stringify({ type: 'MultiPolygon', coordinates: polygons })
}

const syncFromLayers = () => {
  const exported = exportGeometryGeoJson()
  if (exported) emit('update:modelValue', exported)
}

const fitToPolygons = () => {
  if (!mapInstance || !Leaflet || !polygonLayers.length) return
  const group = Leaflet.featureGroup(polygonLayers)
  const bounds = group.getBounds?.()
  if (bounds?.isValid?.()) {
    mapInstance.fitBounds(bounds, { padding: [48, 48], maxZoom: 17 })
  }
}

const rebuildVertexMarkers = () => {
  clearVertexMarkers()
  if (!mapInstance || !Leaflet || !editing.value) return

  polygonLayers.forEach((layer, layerIndex) => {
    const latlngs = layer.getLatLngs?.()
    let ring: any[] = []
    if (Array.isArray(latlngs?.[0]) && typeof latlngs[0][0]?.lat === 'number') {
      ring = latlngs[0]
    } else if (Array.isArray(latlngs) && typeof latlngs[0]?.lat === 'number') {
      ring = latlngs
    }
    if (ring.length < 3) return

    ring.forEach((ll: any, index: number) => {
      const marker = Leaflet.circleMarker(ll, {
        radius: 7,
        color: '#fff',
        weight: 2,
        fillColor: '#1677ff',
        fillOpacity: 1,
        draggable: false
      })
      // Leaflet circleMarker 本身不可拖，用自定义拖拽
      marker.on('mousedown', (e: any) => {
        Leaflet.DomEvent.stopPropagation(e)
        Leaflet.DomEvent.preventDefault(e)
        mapInstance.dragging.disable()

        const onMove = (ev: any) => {
          const latlng = ev.latlng
          marker.setLatLng(latlng)
          const current = layer.getLatLngs()
          let currentRing: any[] = []
          if (Array.isArray(current?.[0]) && typeof current[0][0]?.lat === 'number') {
            currentRing = [...current[0]]
          } else {
            currentRing = [...(current || [])]
          }
          currentRing[index] = latlng
          layer.setLatLngs([currentRing])
        }
        const onUp = () => {
          mapInstance.off('mousemove', onMove)
          mapInstance.off('mouseup', onUp)
          mapInstance.dragging.enable()
          rebuildVertexMarkers()
        }
        mapInstance.on('mousemove', onMove)
        mapInstance.on('mouseup', onUp)
      })
      marker.addTo(mapInstance)
      vertexMarkers.push(marker)

      // 中点：插入新节点
      const next = ring[(index + 1) % ring.length]
      const mid = Leaflet.latLng((ll.lat + next.lat) / 2, (ll.lng + next.lng) / 2)
      const midMarker = Leaflet.circleMarker(mid, {
        radius: 5,
        color: '#1677ff',
        weight: 1,
        fillColor: '#fff',
        fillOpacity: 0.9,
        opacity: 0.85
      })
      midMarker.on('click', (e: any) => {
        Leaflet.DomEvent.stopPropagation(e)
        const current = layer.getLatLngs()
        let currentRing: any[] = []
        if (Array.isArray(current?.[0]) && typeof current[0][0]?.lat === 'number') {
          currentRing = [...current[0]]
        } else {
          currentRing = [...(current || [])]
        }
        currentRing.splice(index + 1, 0, mid)
        layer.setLatLngs([currentRing])
        rebuildVertexMarkers()
      })
      midMarker.addTo(mapInstance)
      vertexMarkers.push(midMarker)
    })

    // 双击顶点删除（至少保留 3 个点）
    void layerIndex
  })
}

const loadGeometryToMap = async () => {
  if (!props.active || editing.value) return
  await ensureMap()
  if (!mapInstance || !Leaflet) return

  clearPolygonLayers()
  const raw = String(props.modelValue || '').trim()
  if (!raw) return

  let rings: number[][][]
  try {
    rings = extractOuterRings(raw)
  } catch {
    ElMessage.warning('GeoJSON 解析失败')
    return
  }
  if (!rings.length) {
    ElMessage.warning('仅支持 Polygon / MultiPolygon 面数据')
    return
  }

  for (const ring of rings) {
    const latlngs = coordsToLatLngs(ring)
    if (latlngs.length < 3) continue
    const polygon = Leaflet.polygon(latlngs, POLYGON_STYLE)
    polygon.addTo(mapInstance)
    polygonLayers.push(polygon)
  }

  if (!polygonLayers.length) {
    ElMessage.warning('面数据无效，无法渲染')
    return
  }

  fitToPolygons()
  await scheduleResize()
}

const initMap = async () => {
  if (!mapRef.value || mapInstance) return
  Leaflet = (window as any).L
  if (!Leaflet) {
    throw new Error('Leaflet 未加载')
  }

  const tk = await getTiandituKey()
  mapInstance = Leaflet.map(mapRef.value, {
    center: props.center,
    zoom: props.zoom,
    zoomControl: true,
    attributionControl: false
  })
  createTiandituImageryLayerGroup(Leaflet, tk).addTo(mapInstance)
  mapReady.value = true
}

const ensureMap = async () => {
  if (!mapRef.value) return
  if (!mapReady.value) {
    await loadLeafletAssets()
    await initMap()
  }
  await scheduleResize(false)
}

const scheduleResize = async (fitBounds = true) => {
  await nextTick()
  mapInstance?.invalidateSize()
  window.setTimeout(() => {
    mapInstance?.invalidateSize()
    if (fitBounds) fitToPolygons()
  }, 120)
}

const enableEdit = async () => {
  await ensureMap()
  if (!polygonLayers.length) {
    await loadGeometryToMap()
  }
  if (!polygonLayers.length) {
    ElMessage.warning('当前没有可编辑的矢量面')
    return
  }
  editing.value = true
  for (const layer of polygonLayers) {
    layer.setStyle?.(EDIT_POLYGON_STYLE)
  }
  if (mapInstance?.doubleClickZoom?.enabled()) {
    mapInstance.doubleClickZoom.disable()
  }
  rebuildVertexMarkers()
  ElMessage.success('已进入编辑：拖动蓝色节点调整范围')
}

const finishEdit = () => {
  clearVertexMarkers()
  editing.value = false
  for (const layer of polygonLayers) {
    layer.setStyle?.(POLYGON_STYLE)
  }
  if (mapInstance?.doubleClickZoom && !mapInstance.doubleClickZoom.enabled()) {
    mapInstance.doubleClickZoom.enable()
  }
  syncFromLayers()
}

const destroyMap = () => {
  if (editing.value) {
    clearVertexMarkers()
    editing.value = false
  }
  clearPolygonLayers()
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
  mapReady.value = false
  Leaflet = null
}

watch(
  () => props.active,
  async (active) => {
    if (active) {
      await loadGeometryToMap()
    } else {
      destroyMap()
    }
  }
)

watch(
  () => props.modelValue,
  async () => {
    if (!props.active || editing.value) return
    await loadGeometryToMap()
  }
)

onBeforeUnmount(() => {
  destroyMap()
})

defineExpose({
  enableEdit,
  finishEdit,
  getGeometryGeoJson: exportGeometryGeoJson
})
</script>

<style scoped>
.pond-spatial-map {
  position: relative;
  width: 100%;
}

.pond-spatial-map__toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.pond-spatial-map__hint {
  font-size: 12px;
  color: #909399;
}

.pond-spatial-map__canvas {
  width: 100%;
  border-radius: 6px;
  overflow: hidden;
  background: #e8eef5;
}

.pond-spatial-map__empty {
  position: absolute;
  inset: 0;
  top: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  border-radius: 6px;
  pointer-events: none;
}

.pond-spatial-map__canvas :deep(.leaflet-interactive) {
  cursor: pointer;
}
</style>
