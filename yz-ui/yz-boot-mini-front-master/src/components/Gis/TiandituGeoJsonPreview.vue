<template>
  <div class="tianditu-preview">
    <div ref="mapRef" class="tianditu-preview__map" :style="{ height: normalizedHeight }"></div>
    <LineSegmentLegend
      v-if="showLineLegend && mapReady && geoJson"
      class="tianditu-preview__legend"
      :show-risk="segmentRiskMark && !startAsHazardPoint"
      :show-endpoint="lineEndpointAsLocation !== false"
      :show-arrow="lineVertexAsArrow"
      :start-as-hazard-point="startAsHazardPoint"
      :route-count="routeLineCount"
      :route-colors="legendRouteColors"
      :route-labels="legendRouteLabels"
    />
    <div v-if="!geoJson" class="tianditu-preview__mask" :style="{ height: normalizedHeight }">
      <el-empty :description="emptyText" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getTiandituKey } from './tiandituKey'
import { createTiandituImageryLayerGroup, DEFAULT_TIANDITU_KEY } from './tiandituLayers'
import {
  clearAllSegmentFlowArrows,
  addSegmentLayersToGroup,
  createLocationDivIcon,
  getRouteLineColor,
  getDefaultWarehouseName,
  DEFAULT_HAZARD_POINT_NAME,
  isLineEndpointIndex,
  parseLineGeometryPayload,
  parseMultiLineGeometryPayload,
  resolveVertexMarkerType,
  type LineGeometryPayload,
  type LineVertexStyleOptions,
  type LocationPinVariant
} from './tiandituLineVertexEditor'
import LineSegmentLegend from './LineSegmentLegend.vue'

const props = withDefaults(
  defineProps<{
    geoJson?: string
    active?: boolean
    height?: number | string
    tiandituKey?: string
    emptyText?: string
    center?: [number, number]
    zoom?: number
    labelText?: string
    highlight?: boolean
    /** 线段节点统一展示为指向性箭头 */
    lineVertexAsArrow?: boolean
    lineEndpointAsLocation?: boolean
    segmentRiskMark?: boolean
    /** 起点即隐患点 */
    startAsHazardPoint?: boolean
    endpointLabels?: [string, string]
  }>(),
  {
    geoJson: '',
    active: false,
    height: 360,
    tiandituKey: DEFAULT_TIANDITU_KEY,
    emptyText: '暂无位置信息',
    center: () => [32.4, 119.2],
    zoom: 11,
    labelText: '',
    highlight: false,
    lineVertexAsArrow: false,
    lineEndpointAsLocation: true,
    segmentRiskMark: false,
    startAsHazardPoint: false,
    endpointLabels: () => ['起点', '终点'] as [string, string]
  }
)

const lineVertexStyle = computed<LineVertexStyleOptions>(() => ({
  middleAsArrow: props.lineVertexAsArrow,
  endpointAsLocation: props.lineEndpointAsLocation !== false
}))

const mapRef = ref<HTMLDivElement>()
const mapReady = ref(false)
const resolvedTiandituKey = ref<string>(props.tiandituKey || DEFAULT_TIANDITU_KEY)

let Leaflet: any = null
let mapInstance: any = null
let geoLayer: any = null
let mapViewRefreshHandler: (() => void) | null = null
let previewExpandedVertexKey: string | null = null
let previewCollapseExpandedHandler: (() => void) | null = null

const buildPreviewVertexKey = (lineIndex: number, vertexIndex: number) => `${lineIndex}-${vertexIndex}`

const normalizedHeight = computed(() => {
  const h = props.height
  return typeof h === 'number' ? `${h}px` : h
})

const showLineLegend = computed(() => props.startAsHazardPoint && Boolean(props.geoJson))

const routeLineCount = computed(() => {
  if (!props.geoJson) return 0
  const multi = parseMultiLineGeometryPayload(props.geoJson, lineVertexStyle.value, props.endpointLabels)
  return multi?.lines.length || 0
})

const parsedLinePayloads = computed(() => {
  if (!props.geoJson) return [] as LineGeometryPayload[]
  const multi = parseMultiLineGeometryPayload(props.geoJson, lineVertexStyle.value, props.endpointLabels)
  return multi?.lines || []
})

const legendRouteColors = computed(() =>
  parsedLinePayloads.value.map((_, index) => getRouteLineColor(index))
)

const legendRouteLabels = computed(() =>
  parsedLinePayloads.value.map((line, index) => {
    const endLabel = (line.vertexLabels[line.vertexLabels.length - 1] || '').trim()
    return endLabel || `路线 ${index + 1}`
  })
)

const loadLeafletAssets = async () => {
  if ((window as any).L) return
  await Promise.all([
    new Promise((resolve, reject) => {
      const existing = document.getElementById('leaflet-style')
      if (existing) return resolve(true)
      const link = document.createElement('link')
      link.id = 'leaflet-style'
      link.rel = 'stylesheet'
      link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'
      link.onload = () => resolve(true)
      link.onerror = reject
      document.head.appendChild(link)
    }),
    new Promise((resolve, reject) => {
      const existing = document.getElementById('leaflet-script')
      if (existing) return resolve(true)
      const script = document.createElement('script')
      script.id = 'leaflet-script'
      script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
      script.onload = () => resolve(true)
      script.onerror = reject
      document.body.appendChild(script)
    })
  ])
}

const initMap = () => {
  if (!mapRef.value) return
  Leaflet = (window as any).L
  if (!Leaflet) {
    throw new Error('Leaflet 资源加载失败')
  }
  mapInstance = Leaflet.map(mapRef.value, {
    center: props.center,
    zoom: props.zoom,
    zoomControl: true
  })
  createTiandituImageryLayerGroup(Leaflet, resolvedTiandituKey.value).addTo(mapInstance)
  previewCollapseExpandedHandler = () => {
    if (previewExpandedVertexKey == null) return
    previewExpandedVertexKey = null
    if (props.geoJson) {
      void renderGeometryOnMap()
    }
  }
  mapInstance.on('click', previewCollapseExpandedHandler)
  mapReady.value = true
  bindMapViewRefresh()
}

const bindMapViewRefresh = () => {
  if (!mapInstance || mapViewRefreshHandler) return
  mapViewRefreshHandler = () => {
    if (!props.active || !props.geoJson) return
    if (!parseMultiLineGeometryPayload(props.geoJson, lineVertexStyle.value, props.endpointLabels)) return
    renderGeometryOnMap()
  }
  mapInstance.on('zoomend', mapViewRefreshHandler)
  mapInstance.on('moveend', mapViewRefreshHandler)
}

const unbindMapViewRefresh = () => {
  if (mapInstance && mapViewRefreshHandler) {
    mapInstance.off('zoomend', mapViewRefreshHandler)
    mapInstance.off('moveend', mapViewRefreshHandler)
  }
  mapViewRefreshHandler = null
}

const ensureTiandituKey = async () => {
  if (props.tiandituKey) {
    resolvedTiandituKey.value = props.tiandituKey
    return
  }
  resolvedTiandituKey.value = await getTiandituKey()
}

const ensureMap = async () => {
  if (!mapRef.value) return
  if (!mapReady.value) {
    await ensureTiandituKey()
    await loadLeafletAssets()
    initMap()
  }
  await scheduleResize()
}

const clearGeometryLayer = () => {
  clearAllSegmentFlowArrows()
  previewExpandedVertexKey = null
  if (geoLayer && mapInstance) {
    mapInstance.removeLayer(geoLayer)
    geoLayer = null
  }
}

const renderLinePayloadOnGroup = (group: any, linePayload: LineGeometryPayload, lineIndex = 0) => {
  const routeColor = getRouteLineColor(lineIndex)
  const pointCount = linePayload.coordinates.length
  for (let i = 0; i < pointCount - 1; i += 1) {
    const a = linePayload.coordinates[i]
    const b = linePayload.coordinates[i + 1]
    const isRisk = !props.startAsHazardPoint && Boolean(linePayload.riskSegments[i])
    addSegmentLayersToGroup(
      Leaflet,
      group,
      [Leaflet.latLng(a[1], a[0]), Leaflet.latLng(b[1], b[0])],
      isRisk,
      false,
      {
        enableFlow: true,
        reverseFlow: props.startAsHazardPoint,
        map: mapInstance,
        routeColor
      }
    )
  }
  linePayload.coordinates.forEach(([lon, lat], index) => {
    const latlng = Leaflet.latLng(lat, lon)
    const markerType = resolveVertexMarkerType(
      index,
      pointCount,
      lineVertexStyle.value,
      linePayload.vertexMarkers[index]
    )
    if (markerType === 'location') {
      const variant: LocationPinVariant =
        index === 0 ? (props.startAsHazardPoint ? 'hazard' : 'start') : 'end'
      const defaultLabel =
        index === 0
          ? props.endpointLabels?.[0] || (props.startAsHazardPoint ? DEFAULT_HAZARD_POINT_NAME : '起点')
          : props.endpointLabels?.[1] ||
            (props.startAsHazardPoint ? getDefaultWarehouseName(lineIndex) : '终点')
      const label = (linePayload.vertexLabels[index] || defaultLabel).trim() || defaultLabel
      const vertexKey = buildPreviewVertexKey(lineIndex, index)
      const expanded = previewExpandedVertexKey === vertexKey
      const marker = Leaflet.marker(latlng, {
        icon: createLocationDivIcon(Leaflet, label, variant, expanded),
        interactive: true,
        zIndexOffset: expanded ? 1400 : 1100
      })
      marker.on('click', (event: any) => {
        Leaflet.DomEvent.stopPropagation(event)
        previewExpandedVertexKey = expanded ? null : vertexKey
        marker.setIcon(
          createLocationDivIcon(Leaflet, label, variant, previewExpandedVertexKey === vertexKey)
        )
        marker.setZIndexOffset(previewExpandedVertexKey === vertexKey ? 1400 : 1100)
      })
      group.addLayer(marker)
    } else if (
      markerType === 'default' &&
      !(props.startAsHazardPoint && !isLineEndpointIndex(index, pointCount))
    ) {
      group.addLayer(
        Leaflet.circleMarker(latlng, {
          radius: props.highlight ? 7 : 5,
          color: '#1677ff',
          weight: 2,
          fillColor: '#fff',
          fillOpacity: 1
        })
      )
    }
  })
}

const renderGeometryOnMap = async () => {
  if (!props.geoJson) {
    clearGeometryLayer()
    return
  }
  await ensureMap()
  if (!mapInstance || !Leaflet) return

  clearGeometryLayer()
  const multiPayload = parseMultiLineGeometryPayload(props.geoJson, lineVertexStyle.value, props.endpointLabels)
  if (multiPayload && multiPayload.lines.length > 0) {
    const group = Leaflet.layerGroup()
    multiPayload.lines.forEach((linePayload, index) => renderLinePayloadOnGroup(group, linePayload, index))
    geoLayer = group.addTo(mapInstance)
    const bounds = geoLayer.getBounds ? geoLayer.getBounds() : null
    if (bounds && bounds.isValid && bounds.isValid()) {
      mapInstance.fitBounds(bounds, { padding: [20, 20], maxZoom: 16 })
    }
    await scheduleResize()
    return
  }
  const linePayload = parseLineGeometryPayload(props.geoJson, lineVertexStyle.value, props.endpointLabels)
  if (linePayload) {
    const group = Leaflet.layerGroup()
    renderLinePayloadOnGroup(group, linePayload)
    geoLayer = group.addTo(mapInstance)
    const bounds = geoLayer.getBounds ? geoLayer.getBounds() : null
    if (bounds && bounds.isValid && bounds.isValid()) {
      mapInstance.fitBounds(bounds, { padding: [20, 20], maxZoom: 16 })
    }
    await scheduleResize()
    return
  }

  let parsed: any
  try {
    parsed = JSON.parse(props.geoJson)
  } catch {
    ElMessage.warning('GeoJSON 解析失败')
    return
  }

  geoLayer = Leaflet.geoJSON(parsed, {
    style: {
      color: '#ff4d4f',
      weight: props.highlight ? 4 : 3
    },
    pointToLayer: (_feature: any, latlng: any) =>
      Leaflet.circleMarker(latlng, {
        radius: props.highlight ? 9 : 6,
        color: '#ff4d4f',
        weight: props.highlight ? 4 : 3,
        fillColor: '#ff4d4f',
        fillOpacity: 0.4
      }),
    onEachFeature: (_feature: any, layer: any) => {
      const label = (props.labelText || '').trim()
      if (!label) return
      if (!layer || !layer.bindTooltip) return
      layer.bindTooltip(label, {
        permanent: true,
        direction: 'right',
        offset: [12, 0],
        className: 'tianditu-preview__label'
      })
    }
  }).addTo(mapInstance)

  const bounds = geoLayer.getBounds ? geoLayer.getBounds() : null
  if (bounds && bounds.isValid && bounds.isValid()) {
    mapInstance.fitBounds(bounds, { padding: [20, 20], maxZoom: 16 })
  }
  await scheduleResize()
}

const fitToGeometry = () => {
  if (!mapInstance || !geoLayer) return
  const bounds = geoLayer.getBounds ? geoLayer.getBounds() : null
  if (bounds && bounds.isValid && bounds.isValid()) {
    mapInstance.fitBounds(bounds, { padding: [20, 20], maxZoom: 16 })
  }
}

const scheduleResize = async () => {
  await nextTick()
  mapInstance?.invalidateSize()
  window.setTimeout(() => {
    mapInstance?.invalidateSize()
    fitToGeometry()
  }, 80)
  window.setTimeout(() => {
    mapInstance?.invalidateSize()
    fitToGeometry()
  }, 200)
}

watch(
  () => props.active,
  async (val) => {
    if (val) {
      await ensureMap()
      await renderGeometryOnMap()
    }
  }
)

watch(
  () => props.geoJson,
  async () => {
    if (!props.active) return
    await renderGeometryOnMap()
  }
)

onMounted(async () => {
  if (props.active) {
    await ensureMap()
    await renderGeometryOnMap()
  }
})

onBeforeUnmount(() => {
  unbindMapViewRefresh()
  clearGeometryLayer()
  if (mapInstance && previewCollapseExpandedHandler) {
    mapInstance.off('click', previewCollapseExpandedHandler)
    previewCollapseExpandedHandler = null
  }
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
  mapReady.value = false
})
</script>

<style scoped>
.tianditu-preview {
  position: relative;
  width: 100%;
}

.tianditu-preview__map {
  width: 100%;
  border-radius: 6px;
  overflow: hidden;
}

.tianditu-preview__legend {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 999;
  max-width: calc(100% - 16px);
}

.tianditu-preview__mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.tianditu-preview :deep(.tianditu-preview__label) {
  padding: 2px 6px;
  color: #fff;
  background: rgba(245, 34, 45, 0.85);
  border: none;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  font-size: 12px;
  white-space: nowrap;
}
</style>

<style>
.tianditu-flow-arrow-marker {
  background: transparent;
  border: none;
}

.tianditu-flow-arrow-marker__svg {
  display: block;
  filter: drop-shadow(0 1px 2px rgb(0 0 0 / 35%));
  pointer-events: none;
}

.tianditu-vertex-handle__arrow-svg {
  display: block;
  filter: drop-shadow(0 1px 2px rgb(0 0 0 / 25%));
}

.tianditu-vertex-handle__location-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  pointer-events: none;
}

.tianditu-vertex-handle__location-pin,
.tianditu-vertex-handle__location-img {
  display: block;
  filter: drop-shadow(0 2px 4px rgb(0 0 0 / 28%));
}

.tianditu-vertex-handle__location-img {
  object-fit: contain;
  pointer-events: none;
  user-select: none;
}

.tianditu-vertex-handle__location-label {
  margin-top: 2px;
  max-width: 96px;
  min-width: 40px;
  color: #fff;
  border-radius: 3px;
  padding: 1px 6px;
  font-size: 11px;
  font-weight: 500;
  line-height: 1.4;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  box-shadow: 0 2px 6px rgb(0 0 0 / 18%);
}

.tianditu-vertex-handle__location-label.is-expanded {
  max-width: min(260px, 72vw);
  white-space: normal;
  word-break: break-all;
  overflow: visible;
  text-overflow: unset;
  box-shadow: 0 4px 12px rgb(0 0 0 / 22%);
}

.leaflet-marker-icon.tianditu-vertex-handle--location.is-name-expanded {
  overflow: visible !important;
}

.tianditu-vertex-handle__location-label.is-start {
  background: rgba(82, 196, 26, 0.92);
}

.tianditu-vertex-handle__location-label.is-hazard {
  background: rgba(250, 84, 28, 0.92);
}

.tianditu-vertex-handle__location-label.is-end {
  background: rgba(22, 119, 255, 0.92);
}
</style>
