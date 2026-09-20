<template>
  <div class="tianditu-editor">
    <div v-if="showToolbar && !compactToolbar" class="tianditu-editor__toolbar">
      <el-space wrap>
          <el-button size="small" :loading="fileLoading" @click="triggerUpload">上传 geometry.json</el-button>
          <el-button size="small" :disabled="!mapReady" @click="fitToGeometry">定位</el-button>
          <el-button size="small" :disabled="!mapReady" @click="clearGeometry">清空图形</el-button>
          <el-button v-if="canDrawPoint" size="small" :disabled="!mapReady || isDrawing" @click="startDraw('POINT')">
            点
          </el-button>
          <el-button v-if="canDrawLine" size="small" :disabled="!mapReady || isDrawing" @click="startDraw('LINESTRING')">
            线
          </el-button>
          <el-button v-if="canDrawPolygon" size="small" :disabled="!mapReady || isDrawing" @click="startDraw('POLYGON')">
            面
          </el-button>
          <template v-if="isDrawing">
            <el-button size="small" type="primary" @click="finishActiveDraw">完成绘制</el-button>
            <el-button size="small" @click="cancelActiveDraw">取消绘制</el-button>
          </template>
          <template v-if="enhancedLineEdit && isLineGeometry && !isDrawing">
            <el-button size="small" type="warning" :disabled="!mapReady" @click="toggleInsertVertexMode">
              {{ insertVertexMode ? '取消插入' : '插入节点' }}
            </el-button>
            <el-button size="small" :disabled="!mapReady || selectedVertexIndex < 0" @click="deleteSelectedVertex">
              删除节点
            </el-button>
          </template>
          <template v-else>
            <el-button size="small" type="warning" :disabled="!mapReady || !hasGeometry" @click="enableEdit">
              编辑图形
            </el-button>
            <el-button size="small" type="danger" :disabled="!mapReady || !editing" @click="disableEdit">
              退出编辑
            </el-button>
          </template>
          <el-button size="small" type="primary" :disabled="!mapReady" @click="toggleFullscreen">
            {{ fullscreen ? '退出全屏' : '全屏' }}
          </el-button>
        </el-space>
        <input
          ref="fileInputRef"
          class="tianditu-editor__file-input"
          type="file"
          accept=".json,.geojson,application/json,application/geo+json"
          @change="handleFileChange"
        />
    </div>

    <div
      :class="[
        'tianditu-editor__stage',
        {
          'is-fullscreen-fallback': fullscreenFallback,
          'has-meta-panel': metaPanelLayoutVisible && !embedSideMeta,
          'has-side-meta': metaPanelLayoutVisible && embedSideMeta
        }
      ]"
    >
      <div ref="mapWrapRef" class="tianditu-editor__wrap" :style="wrapStyle">
        <div
          v-if="compactToolbar && showToolbar"
          class="tianditu-editor__map-toolbar"
        >
          <div class="tianditu-editor__compact-toolbar">
            <el-button-group class="tianditu-editor__compact-toolbar-group">
              <el-button size="small" :disabled="!mapReady" @click="toggleFullscreen">
                {{ fullscreen ? '退出全屏' : '全屏' }}
              </el-button>
            </el-button-group>
            <el-button-group class="tianditu-editor__compact-toolbar-group">
              <el-button
                v-if="startAsHazardPoint"
                size="small"
                :type="isPlacingHazardPoint ? 'warning' : 'default'"
                :disabled="!mapReady || isDrawing"
                @click="handlePlaceHazardPoint"
              >
                {{ isPlacingHazardPoint ? '取消' : '隐患点' }}
              </el-button>
              <el-button
                size="small"
                :type="isDrawing || warehouseLinkSlotAvailable ? 'warning' : 'primary'"
                :disabled="
                  !mapReady ||
                  isPlacingHazardPoint ||
                  (!isDrawing &&
                    !warehouseLinkSlotAvailable &&
                    startAsHazardPoint &&
                    !hasSharedHazard)
                "
                @click="handleCompactDraw"
              >
                {{
                  isDrawing
                    ? '取消绘制'
                    : warehouseLinkSlotAvailable
                      ? '取消关联'
                      : systemWarehouseOnly
                        ? '关联仓库'
                        : '绘制'
                }}
              </el-button>
              <el-button
                v-if="enableWarehousePicker"
                size="small"
                :type="warehousePickerVisible ? 'info' : 'default'"
                plain
                :loading="warehousePickerLoading"
                :disabled="!mapReady"
                @click="toggleWarehousePicker"
              >
                {{ warehousePickerVisible ? '隐藏仓库' : '仓库' }}
              </el-button>
              <el-button size="small" type="danger" :disabled="!mapReady" @click="handleClearAll">
                清除
              </el-button>
            </el-button-group>
          </div>
        </div>
        <div ref="mapRef" class="tianditu-editor__map"></div>
        <LineSegmentLegend
          v-if="showLineLegend && mapReady && !metaPanelLayoutVisible"
          :class="[
            'tianditu-editor__legend tianditu-editor__legend--inline',
            { 'tianditu-editor__legend--float-bottom': fullscreen }
          ]"
          :show-risk="segmentRiskMark && !startAsHazardPoint"
          :show-endpoint="lineEndpointAsLocation !== false"
          :show-arrow="lineVertexAsArrow"
          :start-as-hazard-point="startAsHazardPoint"
          :route-count="lineCount"
          :route-colors="legendRouteColors"
          :route-labels="legendRouteLabels"
        />
        <div v-if="!mapReady" class="tianditu-editor__mask">
          <el-empty description="正在加载地图..." />
        </div>
        <div v-if="fullscreen && !compactToolbar" class="tianditu-editor__fullscreen-actions">
          <el-button size="small" type="primary" @click="toggleFullscreen">退出全屏</el-button>
        </div>
      </div>

      <div v-if="metaPanelLayoutVisible" class="tianditu-editor__meta-panel">
        <div
          v-if="hasMetaFieldsSlot"
          class="tianditu-editor__meta-block tianditu-editor__meta-block--task-fields"
        >
          <div class="tianditu-editor__meta-block-label">基本信息</div>
          <div class="tianditu-editor__task-fields">
            <slot name="meta-fields"></slot>
          </div>
        </div>

        <LineSegmentLegend
          v-if="showLineLegend && !hasMetaFieldsSlot"
          class="tianditu-editor__meta-legend"
          :show-risk="segmentRiskMark && !startAsHazardPoint"
          :show-endpoint="lineEndpointAsLocation !== false"
          :show-arrow="lineVertexAsArrow"
          :start-as-hazard-point="startAsHazardPoint"
          :route-count="lineCount"
          :route-colors="legendRouteColors"
          :route-labels="legendRouteLabels"
        />

        <div v-if="multiLineDraw && lineCount > 0" class="tianditu-editor__meta-block tianditu-editor__meta-block--route">
          <div class="tianditu-editor__route-head">
            <div class="tianditu-editor__route-head-main">
              <span class="tianditu-editor__route-head-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none">
                  <path
                    d="M4 6.5A2.5 2.5 0 0 1 6.5 4h11A2.5 2.5 0 0 1 20 6.5v11A2.5 2.5 0 0 1 17.5 20h-11A2.5 2.5 0 0 1 4 17.5v-11Z"
                    stroke="currentColor"
                    stroke-width="1.6"
                  />
                  <path d="M8 9h8M8 12.5h5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                </svg>
              </span>
              <div>
                <div class="tianditu-editor__route-head-title">路线管理</div>
                <div class="tianditu-editor__route-head-tip">
                  {{
                    startAsHazardPoint
                      ? '多条路线从同一隐患点出发，分别通往不同仓库'
                      : '切换路线后，编辑下方起终点'
                  }}
                </div>
              </div>
            </div>
            <span class="tianditu-editor__route-head-badge">共 {{ lineCount }} 条</span>
          </div>
          <div class="tianditu-editor__route-bar">
            <div class="tianditu-editor__route-current">
              <span class="tianditu-editor__route-current-label">当前路线</span>
              <div class="tianditu-editor__route-current-actions">
                <el-select
                  :model-value="activeLineIndex"
                  size="default"
                  class="tianditu-editor__route-select"
                  @change="onActiveLineChange"
                >
                  <el-option
                    v-for="(_, index) in lineCount"
                    :key="`route-${index}`"
                    :label="`路线 ${index + 1}`"
                    :value="index"
                  />
                </el-select>
              </div>
            </div>
            <el-button plain type="danger" @click="removeActiveLine">删除当前路线</el-button>
          </div>
        </div>

        <div
          v-if="showEndpointMetaBlock"
          class="tianditu-editor__meta-block"
          :class="{ 'is-under-route': multiLineDraw && lineCount > 0 }"
        >
          <div class="tianditu-editor__meta-block-label">
            {{ endpointMetaBlockTitle }}
          </div>
          <p v-if="endpointMetaBlockTip" class="tianditu-editor__meta-block-tip">
            {{ endpointMetaBlockTip }}
          </p>
          <div
            class="tianditu-editor__endpoint-grid"
            :class="{ 'is-warehouse-only': hasMetaFieldsSlot }"
          >
            <div
              v-if="!hasMetaFieldsSlot"
              class="tianditu-editor__endpoint-field"
              :class="startAsHazardPoint ? 'is-hazard' : 'is-start'"
            >
              <label class="tianditu-editor__field-label">
                {{ startFieldLabel }}
                <span v-if="isSharedHazardStartMode" class="tianditu-editor__field-tag">共用</span>
              </label>
              <el-input
                v-model="startLabel"
                size="small"
                :placeholder="startPlaceholder"
                clearable
                maxlength="32"
                @change="applyEndpointLabels"
                @clear="applyEndpointLabels"
              />
            </div>
            <div
              class="tianditu-editor__endpoint-field is-end"
              :class="{ 'is-system-warehouse': isActiveLineSystemWarehouse }"
              @click="onWarehouseNameFieldClick"
            >
              <label class="tianditu-editor__field-label">
                {{ endFieldLabel }}
                <span
                  v-if="startAsHazardPoint && !systemWarehouseOnly"
                  class="tianditu-editor__field-tag"
                  :class="{ 'is-system': isActiveLineSystemWarehouse }"
                >
                  {{ activeWarehouseNameTag }}
                </span>
                <span
                  v-else-if="startAsHazardPoint && systemWarehouseOnly"
                  class="tianditu-editor__field-tag is-system"
                >
                  系统仓库
                </span>
              </label>
              <el-input
                v-model="endLabel"
                size="small"
                :disabled="isActiveLineSystemWarehouse || systemWarehouseOnly"
                :placeholder="
                  systemWarehouseOnly || isActiveLineSystemWarehouse
                    ? '请通过地图关联系统仓库'
                    : endPlaceholder
                "
                :clearable="!isActiveLineSystemWarehouse && !systemWarehouseOnly"
                maxlength="32"
                @change="applyEndpointLabels"
                @clear="applyEndpointLabels"
              />
            </div>
          </div>
        </div>

        <div
          v-if="segmentRiskMark"
          class="tianditu-editor__meta-block tianditu-editor__meta-block--segments"
          :class="{ 'is-under-route': multiLineDraw && lineCount > 0 }"
        >
          <div class="tianditu-editor__meta-block-head">
            <div class="tianditu-editor__meta-block-label">隐患段</div>
            <span class="tianditu-editor__meta-block-tip">勾选线段标记隐患，可填写名称与描述</span>
          </div>
          <div v-if="segmentRiskChecked.length === 0" class="tianditu-editor__meta-empty">
            暂无线段，请先绘制折线
          </div>
          <div v-else class="tianditu-editor__segment-list">
            <div
              v-for="(_, index) in segmentRiskChecked"
              :key="`segment-${index}`"
              class="tianditu-editor__segment-item"
              :class="{
                'is-active': hoveredSegmentIndex === index,
                'is-risk': segmentRiskChecked[index],
                'is-expanded': segmentRiskChecked[index]
              }"
              @mouseenter="onSegmentListHover(index)"
              @mouseleave="onSegmentListHover(null)"
            >
              <div class="tianditu-editor__segment-head">
                <span class="tianditu-editor__segment-index">{{ index + 1 }}</span>
                <el-checkbox
                  class="tianditu-editor__segment-check"
                  :model-value="segmentRiskChecked[index]"
                  @update:model-value="(checked) => onSegmentRiskChange(index, Boolean(checked))"
                >
                  {{ getSegmentDisplayLabel(index) }}
                </el-checkbox>
                <el-button
                  plain
                  type="danger"
                  size="small"
                  class="tianditu-editor__segment-delete"
                  :disabled="segmentRiskChecked.length <= 1"
                  @click.stop="removeSegment(index)"
                >
                  删除
                </el-button>
              </div>
              <div v-if="segmentRiskChecked[index]" class="tianditu-editor__segment-body">
                <div class="tianditu-editor__segment-field">
                  <label class="tianditu-editor__field-label">名称</label>
                  <el-input
                    v-model="segmentRiskNames[index]"
                    size="small"
                    class="tianditu-editor__segment-name"
                    placeholder="选填"
                    clearable
                    maxlength="64"
                    @change="onSegmentRiskNameChange(index)"
                    @clear="onSegmentRiskNameChange(index)"
                  />
                </div>
                <div class="tianditu-editor__segment-field">
                  <label class="tianditu-editor__field-label">描述</label>
                  <el-input
                    v-model="segmentRiskDescriptions[index]"
                    type="textarea"
                    :rows="2"
                    size="small"
                    class="tianditu-editor__segment-desc"
                    placeholder="选填"
                    clearable
                    maxlength="512"
                    show-word-limit
                    @change="onSegmentRiskDescriptionChange(index)"
                    @clear="onSegmentRiskDescriptionChange(index)"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!compactToolbar && isDrawing" class="tianditu-editor__hint">
      点击地图添加节点；至少 2 个点后双击任意位置，或点「完成绘制」结束。
    </div>
    <div v-else-if="!compactToolbar && enhancedLineEdit && isLineGeometry" class="tianditu-editor__hint">
      拖动箭头节点调节位置；点击线段中点「+」可新增节点；{{
        segmentRiskMark ? '点击某一段折线可切换「风险隐患段」标记（黄色高亮）。' : ''
      }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, useSlots, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTiandituKey } from './tiandituKey'
import { createTiandituImageryLayerGroup, DEFAULT_TIANDITU_KEY } from './tiandituLayers'
import {
  attachLineVertexEditor,
  buildLineGeometryPayload,
  buildMultiLineGeometryPayload,
  clearLineRiskSegmentMeta,
  createRiskSegmentNames,
  createRiskSegmentDescriptions,
  createRiskSegments,
  createVertexLabels,
  createVertexMarkers,
  syncRiskSegmentCenters,
  LINE_SEGMENT_COLOR_DEFAULT,
  LINE_SEGMENT_WEIGHT_DEFAULT,
  applyRoutePaletteToLines,
  getRouteLineColor,
  getRouteHoverColorFromBaseColor,
  getDefaultWarehouseName,
  DEFAULT_HAZARD_POINT_NAME,
  createLocationDivIcon,
  resolveHazardEndpointLabels,
  parseLineGeometryPayload,
  parseMultiLineGeometryPayload,
  type LineGeometryPayload,
  type LineVertexEditorController,
  type LineVertexStyleOptions
} from './tiandituLineVertexEditor'
import LineSegmentLegend from './LineSegmentLegend.vue'
import {
  buildWarehousePickerMarkerHtml,
  assignWarehouseRouteColor,
  estimateWarehousePickerIconBox,
  WAREHOUSE_PICKER_ICON_SIZE,
  type WarehouseMapPoint
} from './tiandituWarehousePicker'
import {
  getFxWzMaterialMapPoints,
  type FxWzMaterialMapPointRespVO
} from '@/api/fx/wz'

let leafletCorePromise: Promise<void> | null = null
let leafletDrawPromise: Promise<void> | null = null

type DrawMode = 'POINT' | 'LINESTRING' | 'POLYGON'
type GeoJsonGeometry = {
  type: string
  coordinates?: unknown
  geometries?: GeoJsonGeometry[]
}

const GEOJSON_GEOMETRY_TYPES = new Set([
  'Point',
  'MultiPoint',
  'LineString',
  'MultiLineString',
  'Polygon',
  'MultiPolygon',
  'GeometryCollection'
])

const props = withDefaults(
  defineProps<{
    modelValue?: string
    active?: boolean
    tiandituKey?: string
    height?: number | string
    center?: [number, number]
    zoom?: number
    drawModes?: DrawMode[]
    /** 线段增强编辑：节点拖动 / 插入 / 删除 */
    enhancedLineEdit?: boolean
    /** 中间节点为指向性箭头 */
    lineVertexAsArrow?: boolean
    /** 起终点为位置图标（默认开启） */
    lineEndpointAsLocation?: boolean
    /** 起终点默认显示名称 [起点名, 终点名]，如隐患点模式下为 ['隐患点', '仓库'] */
    endpointLabels?: [string, string]
    /** 点击线段可标记风险隐患段并分色（旧模式，fx 任务已改用 startAsHazardPoint） */
    segmentRiskMark?: boolean
    /** 起点即隐患点：从隐患点绘制至终点，无隐患段 */
    startAsHazardPoint?: boolean
    /** 精简工具栏：全屏 / 隐患点 / 绘制 / 仓库 / 清除 */
    compactToolbar?: boolean
    showToolbar?: boolean
    /** 地图下方起终点与隐患段面板（需 enhancedLineEdit + segmentRiskMark） */
    showLineMetaPanel?: boolean
    /** 支持连续绘制多条折线（MultiLineString） */
    multiLineDraw?: boolean
    /** 险工模式：展示仓库点位并支持点击快速关联路线 */
    enableWarehousePicker?: boolean
    /** 侧栏嵌入 meta-fields 等（地图左、表单/路线右） */
    embedSideMeta?: boolean
    /** 加载到已有图形后自动进入节点编辑 */
    autoEdit?: boolean
  }>(),
  {
    modelValue: '',
    active: true,
    tiandituKey: DEFAULT_TIANDITU_KEY,
    height: 420,
    center: () => [32.4, 119.2],
    zoom: 11,
    drawModes: () => ['POINT', 'LINESTRING', 'POLYGON'],
    enhancedLineEdit: false,
    lineVertexAsArrow: false,
    lineEndpointAsLocation: true,
    segmentRiskMark: false,
    startAsHazardPoint: false,
    endpointLabels: () => ['起点', '终点'] as [string, string],
    compactToolbar: false,
    showToolbar: true,
    showLineMetaPanel: true,
    multiLineDraw: false,
    enableWarehousePicker: false,
    embedSideMeta: false,
    autoEdit: false
  }
)

const slots = useSlots()
const hasMetaFieldsSlot = computed(() => Boolean(slots['meta-fields']))

const lineVertexStyle = computed<LineVertexStyleOptions>(() => ({
  middleAsArrow: props.lineVertexAsArrow,
  endpointAsLocation: props.lineEndpointAsLocation !== false
}))

const emit = defineEmits<{
  (e: 'update:modelValue', v: string): void
  /** 地图侧隐患点名称变更（与工程名称等外部字段双向同步时使用） */
  (e: 'hazard-label-change', v: string): void
}>()

let syncingStartLabelFromProp = false

const mapRef = ref<HTMLDivElement>()
const mapWrapRef = ref<HTMLDivElement>()
const fullscreenFallback = ref(false)
const fileInputRef = ref<HTMLInputElement>()
const mapReady = ref(false)
const fullscreen = ref(false)
const editing = ref(false)
const fileLoading = ref(false)
const resolvedTiandituKey = ref<string>(props.tiandituKey || DEFAULT_TIANDITU_KEY)

let Leaflet: any = null
let mapInstance: any = null
let drawControl: any = null
let editableGroup: any = null
let activeDrawHandler: any = null
const lineVertexEditors: (LineVertexEditorController | null)[] = []
const lineLayerGroups: any[] = []
const linePayloadsCache = ref<LineGeometryPayload[]>([])
const activeLineIndex = ref(0)
let activeLineLayerGroup: any = null
let customLineDrawCoords: [number, number][] = []
let customLineDrawPreview: any = null
let customLineDrawVertexLayer: any = null
let customLineDrawClickHandler: ((e: any) => void) | null = null
let customLineDrawMoveHandler: ((e: any) => void) | null = null
let customLineDrawDblClickHandler: ((e: any) => void) | null = null
let customLineDrawMouseDownHandler: ((e: MouseEvent) => void) | null = null
let customLineDrawFinishing = false
let customLineDrawContainerEl: HTMLElement | null = null
let customLineDrawLastMouseDownTime = 0
let customLineDrawMoveRaf = 0
let customLineDrawMoveCursor: { lat: number; lng: number } | null = null
let mapDraggingWasEnabled = false

const insertVertexMode = ref(false)
const selectedVertexIndex = ref(-1)
const isLineGeometry = ref(false)
const isDrawing = ref(false)
const drawingMode = ref<DrawMode | null>(null)
const startLabel = ref(props.endpointLabels?.[0] || (props.startAsHazardPoint ? DEFAULT_HAZARD_POINT_NAME : '起点'))
const endLabel = ref(
  props.endpointLabels?.[1] || (props.startAsHazardPoint ? getDefaultWarehouseName(0) : '终点')
)
const segmentRiskChecked = ref<boolean[]>([])
const segmentRiskNames = ref<string[]>([])
const segmentRiskDescriptions = ref<string[]>([])
const hoveredSegmentIndex = ref<number | null>(null)

const lineCount = computed(() => linePayloadsCache.value.length)

const legendRouteColors = computed(() =>
  linePayloadsCache.value.map((line, index) => line.routeColor?.trim() || getRouteLineColor(index))
)

const legendRouteLabels = computed(() =>
  linePayloadsCache.value.map((line, index) => {
    const endLabel = (line.vertexLabels[line.vertexLabels.length - 1] || '').trim()
    return endLabel || `路线 ${index + 1}`
  })
)

const isLineSystemWarehouse = (line?: LineGeometryPayload | null) => {
  if (!props.enableWarehousePicker || !line || line.coordinates.length < 2) return false
  if (line.linkedWarehouseId != null && String(line.linkedWarehouseId).trim()) return true
  const end = line.coordinates[line.coordinates.length - 1]
  const endLabel = line.vertexLabels[line.vertexLabels.length - 1]
  return Boolean(findWarehouseMapPoint(end[0], end[1], endLabel))
}

const isActiveLineSystemWarehouse = computed(() =>
  isLineSystemWarehouse(linePayloadsCache.value[activeLineIndex.value])
)

const activeWarehouseNameTag = computed(() =>
  isActiveLineSystemWarehouse.value ? '系统仓库' : '自定义仓库'
)

const onWarehouseNameFieldClick = () => {
  if (systemWarehouseOnly.value || isActiveLineSystemWarehouse.value) {
    ElMessage.info('路线终点仅支持系统仓库，请通过「关联仓库」选择')
  }
}

const pendingRouteLineColor = computed(() => getRouteLineColor(linePayloadsCache.value.length))

const getActiveLineEditor = () => lineVertexEditors[activeLineIndex.value] || null

const lineMetaPanelVisible = computed(() => {
  if (props.showLineMetaPanel === false) return false
  return Boolean(
    props.enhancedLineEdit &&
      isLineGeometry.value &&
      (props.segmentRiskMark || props.startAsHazardPoint)
  )
})

const metaPanelVisible = computed(() => {
  if (props.showLineMetaPanel === false) return false
  if (props.embedSideMeta) {
    if (hasMetaFieldsSlot.value) return true
    if (props.startAsHazardPoint) return true
  }
  return lineMetaPanelVisible.value
})

/** 全屏时仅展示地图区域，避免底部 meta 挤占画布触发反复 resize */
const metaPanelLayoutVisible = computed(() => metaPanelVisible.value && !fullscreen.value)

const showEndpointMetaBlock = computed(() => {
  if (!props.startAsHazardPoint && !props.enhancedLineEdit) return false
  if (hasMetaFieldsSlot.value) return lineCount.value > 0
  return lineMetaPanelVisible.value || (props.startAsHazardPoint && hasSharedHazard.value)
})

const endpointMetaBlockTitle = computed(() => {
  if (hasMetaFieldsSlot.value) return '路线终点'
  return props.startAsHazardPoint ? '隐患点与仓库' : '起终点'
})

const endpointMetaBlockTip = computed(() => {
  if (hasMetaFieldsSlot.value) {
    return systemWarehouseOnly.value
      ? '路线终点仅支持系统仓库；先标「隐患点」，再点「关联仓库」后点击地图仓库'
      : '系统仓库不可改名；可先点地图「仓库」再绘制或关联'
  }
  if (!isSharedHazardStartMode.value) return ''
  if (systemWarehouseOnly.value) {
    return '隐患点全部路线共用；终点仅支持系统仓库。先标「隐患点」，再点「关联仓库」选择仓库'
  }
  let tip = '隐患点全部路线共用；终点分为系统仓库（不可改名）与自定义仓库'
  if (props.enableWarehousePicker) {
    tip += '。先标「隐患点」，再「绘制」或点仓库关联'
  }
  return tip
})

/** 地图标记默认显示名称（可被用户输入覆盖） */
const defaultStartName = computed(
  () => props.endpointLabels?.[0] || (props.startAsHazardPoint ? DEFAULT_HAZARD_POINT_NAME : '起点')
)
const defaultEndName = computed(() => {
  if (props.startAsHazardPoint) {
    return getDefaultWarehouseName(activeLineIndex.value)
  }
  return props.endpointLabels?.[1] || '终点'
})
const endpointLabelsForLine = (lineIndex: number): [string, string] =>
  resolveHazardEndpointLabels(lineIndex, props.startAsHazardPoint, [
    props.endpointLabels?.[0] || '起点',
    props.endpointLabels?.[1] || '终点'
  ])
/** 表单字段标签 */
const startFieldLabel = computed(() => (props.startAsHazardPoint ? '隐患点名称' : '起点'))
const endFieldLabel = computed(() => (props.startAsHazardPoint ? '仓库名称' : '终点'))
const startPlaceholder = computed(() =>
  props.startAsHazardPoint ? '请输入隐患点名称' : '请输入起点名称'
)
const endPlaceholder = computed(() =>
  props.startAsHazardPoint
    ? `请输入仓库名称，如：${getDefaultWarehouseName(activeLineIndex.value)}`
    : '请输入终点名称'
)
const resolvedEndpointLabels = computed<[string, string]>(() => [
  defaultStartName.value,
  defaultEndName.value
])

/** 多线模式 + 隐患点：所有路线共用同一隐患点，终点为不同仓库 */
const isSharedHazardStartMode = computed(
  () => Boolean(props.startAsHazardPoint && props.multiLineDraw)
)
/** 仅允许关联系统仓库，禁止手绘至自定义终点 */
const systemWarehouseOnly = computed(
  () => Boolean(props.enableWarehousePicker && props.startAsHazardPoint)
)
const hasSharedHazard = computed(() => Boolean(getSharedHazardCoordinate()))
let syncingSharedHazard = false
/** 独立标注的隐患点（可先打点再绘制路线） */
const sharedHazardCoordinate = ref<[number, number] | null>(null)
let hazardPointMarker: any = null
let hazardPointPlacementHandler: ((event: any) => void) | null = null
const isPlacingHazardPoint = ref(false)
/** 地图上点击展开完整名称（与标记标签同风格，非 Leaflet 默认 Tooltip） */
let expandedHazardNameOnMap = false
let expandedWarehouseMapId: string | number | null = null
let mapCollapseExpandedNamesHandler: ((event?: any) => void) | null = null

const isMapNameExpandClickTarget = (event: any) => {
  const target = (event?.originalEvent?.target || event?.target) as HTMLElement | undefined
  if (!target?.closest) return false
  return Boolean(
    target.closest('.tianditu-warehouse-picker-marker') ||
      target.closest('.tianditu-vertex-handle') ||
      target.closest('.tianditu-warehouse-picker-handle')
  )
}

const collapseAllExpandedMapNames = () => {
  expandedHazardNameOnMap = false
  expandedWarehouseMapId = null
  lineVertexEditors.forEach((editor) => editor?.collapseExpandedName?.())
  renderHazardPointMarker()
  if (warehousePickerVisible.value) {
    renderWarehousePickerMarkers()
  }
}

const getSharedHazardCoordinate = (): [number, number] | null => {
  if (sharedHazardCoordinate.value) {
    return [sharedHazardCoordinate.value[0], sharedHazardCoordinate.value[1]]
  }
  const start = linePayloadsCache.value[0]?.coordinates?.[0]
  return start ? ([start[0], start[1]] as [number, number]) : null
}

const buildHazardPointGeoJson = (): string => {
  const coord = getSharedHazardCoordinate()
  if (!coord) return ''
  const label = (startLabel.value || defaultStartName.value).trim() || defaultStartName.value
  return JSON.stringify({
    type: 'Point',
    coordinates: coord,
    vertexLabels: [label]
  })
}

const removeHazardPointMarker = () => {
  if (hazardPointMarker && mapInstance) {
    mapInstance.removeLayer(hazardPointMarker)
  }
  hazardPointMarker = null
}

const renderHazardPointMarker = () => {
  if (!Leaflet || !mapInstance || !props.startAsHazardPoint) return
  if (linePayloadsCache.value.length > 0) {
    removeHazardPointMarker()
    return
  }
  const coord = getSharedHazardCoordinate()
  if (!coord) {
    removeHazardPointMarker()
    return
  }
  const label = (startLabel.value || defaultStartName.value).trim() || defaultStartName.value
  const latlng = Leaflet.latLng(coord[1], coord[0])
  const bindHazardMarkerClick = (marker: any) => {
    marker.off('click')
    marker.on('click', (event: any) => {
      Leaflet.DomEvent.stopPropagation(event)
      expandedHazardNameOnMap = !expandedHazardNameOnMap
      expandedWarehouseMapId = null
      lineVertexEditors.forEach((editor) => editor?.collapseExpandedName?.())
      renderHazardPointMarker()
      if (warehousePickerVisible.value) {
        renderWarehousePickerMarkers()
      }
    })
  }
  if (hazardPointMarker) {
    hazardPointMarker.setLatLng(latlng)
    hazardPointMarker.setIcon(createLocationDivIcon(Leaflet, label, 'hazard', expandedHazardNameOnMap))
    hazardPointMarker.setZIndexOffset(expandedHazardNameOnMap ? 1400 : 1200)
    bindHazardMarkerClick(hazardPointMarker)
    return
  }
  hazardPointMarker = Leaflet.marker(latlng, {
    draggable: true,
    icon: createLocationDivIcon(Leaflet, label, 'hazard', expandedHazardNameOnMap),
    zIndexOffset: expandedHazardNameOnMap ? 1400 : 1200
  })
  hazardPointMarker.on('dragend', () => {
    const pos = hazardPointMarker.getLatLng()
    setSharedHazardCoordinate([pos.lng, pos.lat], { syncLines: true })
  })
  bindHazardMarkerClick(hazardPointMarker)
  hazardPointMarker.addTo(mapInstance)
}

const clearSharedHazardState = () => {
  sharedHazardCoordinate.value = null
  removeHazardPointMarker()
}

const setSharedHazardCoordinate = (
  coord: [number, number],
  options: { syncLines?: boolean; emitModel?: boolean } = {}
) => {
  const { syncLines = true, emitModel = true } = options
  sharedHazardCoordinate.value = [coord[0], coord[1]]
  renderHazardPointMarker()
  if (syncLines && linePayloadsCache.value.length > 0) {
    syncingSharedHazard = true
    try {
      const sharedLabel = linePayloadsCache.value[0]?.vertexLabels?.[0] || defaultStartName.value
      linePayloadsCache.value.forEach((payload) => {
        if (payload.coordinates.length === 0) return
        payload.coordinates[0] = [coord[0], coord[1]]
        if (payload.vertexLabels.length > 0) {
          payload.vertexLabels[0] = sharedLabel
        }
      })
      linePayloadsCache.value.forEach((_, index) => {
        lineVertexEditors[index]?.setStartCoordinate([coord[0], coord[1]], false)
        if (index > 0) {
          lineVertexEditors[index]?.updateStartLabel(sharedLabel)
        }
      })
    } finally {
      syncingSharedHazard = false
    }
  }
  if (emitModel) {
    syncModelFromAllLines()
  }
}

const stopHazardPointPlacement = (silent = false) => {
  if (mapInstance && hazardPointPlacementHandler) {
    mapInstance.off('click', hazardPointPlacementHandler)
  }
  hazardPointPlacementHandler = null
  isPlacingHazardPoint.value = false
  if (!silent) {
    ElMessage.info('已取消隐患点标注')
  }
}

const startHazardPointPlacement = () => {
  if (!Leaflet || !mapInstance) return
  cancelActiveDraw(true)
  stopHazardPointPlacement(true)
  isPlacingHazardPoint.value = true
  ElMessage.info('请在地图上单击放置隐患点')
  hazardPointPlacementHandler = (event: any) => {
    setSharedHazardCoordinate([event.latlng.lng, event.latlng.lat])
    stopHazardPointPlacement(true)
    ElMessage.success(
      systemWarehouseOnly.value
        ? '隐患点已标注，可点击「关联仓库」选择系统仓库'
        : '隐患点已标注，可点击「绘制」添加物资路线'
    )
  }
  mapInstance.on('click', hazardPointPlacementHandler)
}

const handlePlaceHazardPoint = () => {
  if (!mapReady.value) return
  if (isPlacingHazardPoint.value) {
    stopHazardPointPlacement()
    return
  }
  startHazardPointPlacement()
}

const fitToHazardPoint = () => {
  if (!mapInstance || !Leaflet) return
  const coord = getSharedHazardCoordinate()
  if (!coord) return
  mapInstance.setView(Leaflet.latLng(coord[1], coord[0]), 16)
  mapInstance.invalidateSize()
}

const syncSharedHazardCoordinateToAllLines = (coord: [number, number], sourceLineIndex: number) => {
  if (!props.startAsHazardPoint || syncingSharedHazard) return
  syncingSharedHazard = true
  try {
    sharedHazardCoordinate.value = [coord[0], coord[1]]
    linePayloadsCache.value.forEach((payload, index) => {
      if (payload.coordinates.length === 0) return
      payload.coordinates[0] = [coord[0], coord[1]]
      if (index !== sourceLineIndex) {
        lineVertexEditors[index]?.setStartCoordinate(coord, false)
      }
    })
    renderHazardPointMarker()
    syncModelFromAllLines()
  } finally {
    syncingSharedHazard = false
  }
}

const normalizeLineCoordinatesForSharedHazard = (
  coordinates: [number, number][],
  lineIndex: number
): [number, number][] => {
  const normalized = coordinates.map((item) => [item[0], item[1]] as [number, number])
  if (!isSharedHazardStartMode.value || lineIndex === 0) return normalized
  const shared = getSharedHazardCoordinate()
  if (shared && normalized.length > 0) {
    normalized[0] = [shared[0], shared[1]]
  }
  return normalized
}

/** 导出 GeoJSON 前校正缓存中的共用隐患点；勿调编辑器 API，避免 onChange → syncModel 死循环 */
const alignSharedHazardInCache = () => {
  if (!isSharedHazardStartMode.value || linePayloadsCache.value.length === 0) return
  const sharedCoord: [number, number] | null = sharedHazardCoordinate.value
    ? [sharedHazardCoordinate.value[0], sharedHazardCoordinate.value[1]]
    : linePayloadsCache.value[0]?.coordinates?.[0]
      ? ([
          linePayloadsCache.value[0].coordinates[0][0],
          linePayloadsCache.value[0].coordinates[0][1]
        ] as [number, number])
      : null
  const sharedLabel = linePayloadsCache.value[0]?.vertexLabels?.[0] || defaultStartName.value
  if (!sharedCoord) return
  linePayloadsCache.value.forEach((payload) => {
    if (payload.coordinates.length === 0) return
    payload.coordinates[0] = [sharedCoord[0], sharedCoord[1]]
    if (payload.vertexLabels.length > 0) {
      payload.vertexLabels[0] = sharedLabel
    }
  })
  sharedHazardCoordinate.value = [sharedCoord[0], sharedCoord[1]]
}

let warehousePickerLayerGroup: any = null
const warehousePickerVisible = ref(false)
const warehousePickerLoading = ref(false)
const warehouseMapPoints = ref<WarehouseMapPoint[]>([])
const warehousePickerMarkers: any[] = []
/** 点击「绘制」后允许关联一个仓库，关联成功后需再次点击「绘制」 */
const warehouseLinkSlotAvailable = ref(false)

const resetWarehouseLinkSlot = () => {
  warehouseLinkSlotAvailable.value = false
}

const tryEnableWarehouseLinkSlot = (notify = true) => {
  if (!props.enableWarehousePicker || !warehousePickerVisible.value) return false
  if (!getSharedHazardCoordinate()) return false
  warehouseLinkSlotAvailable.value = true
  renderWarehousePickerMarkers()
  if (notify) {
    ElMessage.info(
      systemWarehouseOnly.value
        ? '请点击地图上未连接的系统仓库完成路线关联'
        : '请将鼠标移到未连接的仓库上，点击完成关联'
    )
  }
  return true
}

const ensureWarehousePickerForLink = async () => {
  if (!props.enableWarehousePicker) return false
  try {
    await ensureWarehouseMapPoints()
  } catch (error: any) {
    ElMessage.error(error?.message || '仓库点位加载失败')
    return false
  }
  if (warehouseMapPoints.value.length === 0) {
    ElMessage.warning('暂无含坐标的防汛物资仓库地址')
    return false
  }
  if (!warehousePickerVisible.value) {
    warehousePickerVisible.value = true
    renderWarehousePickerMarkers()
    fitToWarehousePickerView()
  }
  return true
}

const startSystemWarehouseLink = async () => {
  if (!getSharedHazardCoordinate()) {
    ElMessage.warning('请先点击「隐患点」在地图上标注位置')
    return
  }
  const ready = await ensureWarehousePickerForLink()
  if (!ready) return
  tryEnableWarehouseLinkSlot(true)
}

const getWarehouseLinkedLineIndex = (warehouse: WarehouseMapPoint) => {
  const name = warehouse.warehouseName.trim()
  return linePayloadsCache.value.findIndex((line) => {
    if (line.coordinates.length < 2) return false
    const end = line.coordinates[line.coordinates.length - 1]
    if (coordsNear(end[0], end[1], warehouse.longitude, warehouse.latitude)) return true
    return (line.vertexLabels[line.vertexLabels.length - 1] || '').trim() === name
  })
}

const coordsNear = (lon1: number, lat1: number, lon2: number, lat2: number, eps = 1e-4) =>
  Math.abs(lon1 - lon2) < eps && Math.abs(lat1 - lat2) < eps

const isRegisteredWarehouseAt = (lon: number, lat: number) =>
  warehouseMapPoints.value.some((item) => coordsNear(item.longitude, item.latitude, lon, lat))

const shouldHideRouteEndpointIconAt = (lon: number, lat: number) =>
  props.enableWarehousePicker && warehousePickerVisible.value && isRegisteredWarehouseAt(lon, lat)

let syncingWarehouseDisplayRefresh = false

const refreshLineEditorsForWarehouseDisplay = () => {
  if (syncingWarehouseDisplayRefresh) return
  syncingWarehouseDisplayRefresh = true
  try {
    linePayloadsCache.value.forEach((line, index) => {
      const editor = lineVertexEditors[index]
      if (!editor) return
      if (!isLineSystemWarehouse(line)) {
        editor.syncLockedEndCoordinate?.(undefined, undefined)
        return
      }
      const lockedEnd = resolveLineLockedEndCoordinate(line)
      if (lockedEnd && line.coordinates.length >= 2) {
        const last = line.coordinates.length - 1
        line.coordinates[last] = [lockedEnd[0], lockedEnd[1]]
      }
      editor.syncLockedEndCoordinate?.(lockedEnd, line.linkedWarehouseId)
    })
  } finally {
    syncingWarehouseDisplayRefresh = false
  }
}

const getWarehouseMarkerVisual = (warehouse: WarehouseMapPoint) => {
  const linkedIndex = getWarehouseLinkedLineIndex(warehouse)
  return {
    linked: linkedIndex >= 0,
    linkable: linkedIndex < 0 && warehouseLinkSlotAvailable.value,
    lineIndex: linkedIndex
  }
}

const findWarehouseMapPoint = (lon: number, lat: number, name?: string) =>
  warehouseMapPoints.value.find((item) => {
    if (coordsNear(item.longitude, item.latitude, lon, lat)) return true
    const label = (name || '').trim()
    return label.length > 0 && item.warehouseName.trim() === label
  })

const resolveLineLockedEndCoordinate = (line?: LineGeometryPayload | null): [number, number] | undefined => {
  if (!props.enableWarehousePicker || !line || line.coordinates.length < 2) return undefined
  const linkedId = line.linkedWarehouseId
  if (linkedId != null && String(linkedId).trim()) {
    const byId = warehouseMapPoints.value.find((item) => String(item.id) === String(linkedId))
    if (byId) return [byId.longitude, byId.latitude]
  }
  if (isLineSystemWarehouse(line)) {
    const end = line.coordinates[line.coordinates.length - 1]
    const endLabel = line.vertexLabels[line.vertexLabels.length - 1]
    const byCoord = findWarehouseMapPoint(end[0], end[1], endLabel)
    if (byCoord) return [byCoord.longitude, byCoord.latitude]
  }
  return undefined
}

const enrichLineWarehouseMeta = (lines: LineGeometryPayload[]) => {
  if (!props.enableWarehousePicker) return
  lines.forEach((line, lineIndex) => {
    if (line.coordinates.length < 2) return
    const end = line.coordinates[line.coordinates.length - 1]
    const endLabel = line.vertexLabels[line.vertexLabels.length - 1]
    let warehouse = findWarehouseMapPoint(end[0], end[1], endLabel)
    if (!warehouse && line.linkedWarehouseId != null && String(line.linkedWarehouseId).trim()) {
      warehouse =
        warehouseMapPoints.value.find((item) => String(item.id) === String(line.linkedWarehouseId)) ||
        undefined
    }
    if (!warehouse) return
    if (line.linkedWarehouseId == null) {
      line.linkedWarehouseId = warehouse.id
    }
    const last = line.vertexLabels.length - 1
    if (last < 0) return
    const current = (line.vertexLabels[last] || '').trim()
    const fallback = getDefaultWarehouseName(lineIndex)
    if (!current || current === fallback) {
      line.vertexLabels[last] = warehouse.warehouseName
    }
  })
}

const normalizeWarehouseMapPoint = (
  item: FxWzMaterialMapPointRespVO,
  index: number
): WarehouseMapPoint | null => {
  const lon = Number(item.longitude)
  const lat = Number(item.latitude)
  if (!Number.isFinite(lon) || !Number.isFinite(lat)) return null
  const materialName = (item.materialName || '').trim()
  const storageUnit = (item.storageUnit || '').trim()
  const warehouseAddress = (item.warehouseAddress || '').trim()
  const name = warehouseAddress || storageUnit || materialName
  if (!name) return null
  const colors = assignWarehouseRouteColor(item.id, index)
  return {
    id: item.id,
    warehouseName: name,
    longitude: lon,
    latitude: lat,
    routeColor: colors.routeColor,
    routeHoverColor: colors.routeHoverColor
  }
}

const clearWarehousePickerLayer = () => {
  warehousePickerMarkers.length = 0
  if (warehousePickerLayerGroup && mapInstance) {
    mapInstance.removeLayer(warehousePickerLayerGroup)
  }
  warehousePickerLayerGroup = null
}

const hideWarehousePicker = () => {
  warehousePickerVisible.value = false
  resetWarehouseLinkSlot()
  clearWarehousePickerLayer()
  refreshLineEditorsForWarehouseDisplay()
}

const ensureWarehouseMapPoints = async () => {
  if (warehouseMapPoints.value.length > 0) return
  warehousePickerLoading.value = true
  try {
    const list = await getFxWzMaterialMapPoints()
    warehouseMapPoints.value = (list || [])
      .map((item, index) => normalizeWarehouseMapPoint(item, index))
      .filter(Boolean) as WarehouseMapPoint[]
  } catch (error: any) {
    warehouseMapPoints.value = []
    throw error
  } finally {
    warehousePickerLoading.value = false
  }
}

const fitToWarehousePickerView = () => {
  if (!mapInstance || !Leaflet) return
  const latLngs: any[] = []
  const hazard = getSharedHazardCoordinate()
  if (hazard) {
    latLngs.push(Leaflet.latLng(hazard[1], hazard[0]))
  }
  warehouseMapPoints.value.forEach((item) => {
    latLngs.push(Leaflet.latLng(item.latitude, item.longitude))
  })
  linePayloadsCache.value.forEach((line) => {
    line.coordinates.forEach(([lon, lat]) => {
      latLngs.push(Leaflet.latLng(lat, lon))
    })
  })
  if (latLngs.length === 0) return
  if (latLngs.length === 1) {
    mapInstance.setView(latLngs[0], 14)
  } else {
    mapInstance.fitBounds(Leaflet.latLngBounds(latLngs), { padding: [48, 48], maxZoom: 16 })
  }
  mapInstance.invalidateSize()
}

const renderWarehousePickerMarkers = () => {
  if (!props.enableWarehousePicker || !mapInstance || !Leaflet || !warehousePickerVisible.value) return
  clearWarehousePickerLayer()
  if (warehouseMapPoints.value.length === 0) return
  warehousePickerLayerGroup = Leaflet.layerGroup().addTo(mapInstance)
  warehouseMapPoints.value.forEach((warehouse) => {
    const visual = getWarehouseMarkerVisual(warehouse)
    const nameExpanded = expandedWarehouseMapId != null && String(expandedWarehouseMapId) === String(warehouse.id)
    const iconBox = estimateWarehousePickerIconBox(warehouse.warehouseName, nameExpanded)
    const marker = Leaflet.marker(Leaflet.latLng(warehouse.latitude, warehouse.longitude), {
      icon: Leaflet.divIcon({
        className: `tianditu-warehouse-picker-handle${nameExpanded ? ' is-warehouse-name-expanded' : ''}`,
        html: buildWarehousePickerMarkerHtml(warehouse.warehouseName, {
          linked: visual.linked,
          linkable: visual.linkable,
          nameExpanded
        }),
        iconSize: [iconBox.width, iconBox.height],
        iconAnchor: [iconBox.width / 2, WAREHOUSE_PICKER_ICON_SIZE]
      }),
      zIndexOffset: nameExpanded ? 1350 : visual.linkable ? 900 : visual.linked ? 860 : 820
    })
    marker.on('click', (event: any) => {
      Leaflet.DomEvent.stopPropagation(event)
      Leaflet.DomEvent.preventDefault(event)
      collapseRouteVertexSelectionForWarehouse()
      if (visual.linkable && warehouseLinkSlotAvailable.value) {
        linkWarehouseFromPicker(warehouse)
        return
      }
      const linkedIndex = getWarehouseLinkedLineIndex(warehouse)
      if (linkedIndex >= 0) {
        onActiveLineChange(linkedIndex)
      }
      expandedWarehouseMapId =
        nameExpanded ? null : warehouse.id
      expandedHazardNameOnMap = false
      collapseRouteVertexSelectionForWarehouse()
      renderHazardPointMarker()
      renderWarehousePickerMarkers()
    })
    warehousePickerLayerGroup.addLayer(marker)
    warehousePickerMarkers.push(marker)
  })
  if (!isCustomLineDrawing()) {
    refreshLineEditorsForWarehouseDisplay()
  }
}

const collapseRouteVertexSelectionForWarehouse = () => {
  selectedVertexIndex.value = -1
  lineVertexEditors.forEach((editor) => {
    editor?.clearVertexSelection?.()
    editor?.collapseExpandedName?.()
  })
}

const findLineIndexByWarehouseCoord = (lon: number, lat: number) =>
  linePayloadsCache.value.findIndex((line) => {
    if (line.coordinates.length < 2) return false
    const end = line.coordinates[line.coordinates.length - 1]
    return coordsNear(end[0], end[1], lon, lat)
  })

const linkWarehouseFromPicker = (warehouse: WarehouseMapPoint) => {
  const hazard = getSharedHazardCoordinate()
  if (!hazard) {
    ElMessage.warning('请先点击「绘制隐患点」标注位置，再点击仓库关联')
    return
  }
  const duplicatedIndex = getWarehouseLinkedLineIndex(warehouse)
  if (duplicatedIndex >= 0) {
    ElMessage.info('该仓库已经存在路线')
    onActiveLineChange(duplicatedIndex)
    return
  }
  if (!warehouseLinkSlotAvailable.value) {
    ElMessage.warning(
      systemWarehouseOnly.value
        ? '请先点击「关联仓库」，再选择系统仓库'
        : '请先点击「绘制」，再选择一个仓库进行关联'
    )
    return
  }
  const coordinates: [number, number][] = [
    [hazard[0], hazard[1]],
    [warehouse.longitude, warehouse.latitude]
  ]
  if (isCustomLineDrawing()) {
    cancelCustomLineDraw(true)
  }
  appendLineFromCoordinates(coordinates, warehouse.warehouseName, undefined, warehouse.id)
  warehouseLinkSlotAvailable.value = false
  renderWarehousePickerMarkers()
  const linkedLineIndex = linePayloadsCache.value.length - 1
  lineVertexEditors[linkedLineIndex]?.syncLockedEndCoordinate?.(
    [warehouse.longitude, warehouse.latitude],
    warehouse.id
  )
  ElMessage.success(
    `已关联至「${warehouse.warehouseName}」${
      systemWarehouseOnly.value ? '，可继续点「关联仓库」添加其他仓库路线' : '，可拖动中间拐点调整路线'
    }`
  )
}

const toggleWarehousePicker = async () => {
  if (!props.enableWarehousePicker) return
  if (warehousePickerVisible.value) {
    hideWarehousePicker()
    return
  }
  try {
    await ensureWarehouseMapPoints()
  } catch (error: any) {
    ElMessage.error(error?.message || '仓库点位加载失败')
    return
  }
  if (warehouseMapPoints.value.length === 0) {
    ElMessage.warning('暂无含坐标的防汛物资仓库')
    return
  }
  warehousePickerVisible.value = true
  resetWarehouseLinkSlot()
  renderWarehousePickerMarkers()
  fitToWarehousePickerView()
  ElMessage.info(
    systemWarehouseOnly.value
      ? '请先点击「隐患点」，再点「关联仓库」选择系统仓库'
      : '请先点击「绘制隐患点」，再点「绘制」或点击未连接的仓库进行关联'
  )
}

const sharedHazardDrawMinPoints = () =>
  isSharedHazardStartMode.value && getSharedHazardCoordinate() ? 1 : 0

/** Leaflet 图层非响应式，需用计数驱动 hasGeometry，否则会永久缓存为 false */
const geometryLayerCount = ref(0)
const syncGeometryLayerCount = () => {
  geometryLayerCount.value = editableGroup?.getLayers?.()?.length || 0
}
const hasGeometry = computed(() => geometryLayerCount.value > 0)

const drawModeSet = computed(() => new Set(props.drawModes || []))
const canDrawPoint = computed(() => drawModeSet.value.has('POINT'))
const canDrawLine = computed(() => drawModeSet.value.has('LINESTRING'))
const showLineLegend = computed(
  () => props.startAsHazardPoint && isLineGeometry.value && canDrawLine.value
)
const canDrawPolygon = computed(() => drawModeSet.value.has('POLYGON'))

const normalizedHeight = computed(() => {
  const h = props.height
  return typeof h === 'number' ? `${h}px` : h
})

const wrapStyle = computed(() => {
  if (fullscreen.value && !fullscreenFallback.value) {
    return { width: '100%', height: '100%', minHeight: '0' }
  }
  if (fullscreenFallback.value) {
    return { flex: '1 1 auto', minHeight: '0', height: 'auto', width: '100%' }
  }
  return { height: normalizedHeight.value }
})

const loadStyleOnce = (id: string, href: string) => {
  const existing = document.getElementById(id)
  if (existing) return Promise.resolve()
  return new Promise<void>((resolve, reject) => {
    const link = document.createElement('link')
    link.id = id
    link.rel = 'stylesheet'
    link.href = href
    link.onload = () => resolve()
    link.onerror = () => reject(new Error(`样式加载失败：${href}`))
    document.head.appendChild(link)
  })
}

const loadScriptOnce = (id: string, src: string) => {
  const existing = document.getElementById(id) as HTMLScriptElement | null
  if (existing) return Promise.resolve()
  return new Promise<void>((resolve, reject) => {
    const script = document.createElement('script')
    script.id = id
    script.src = src
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error(`脚本加载失败：${src}`))
    document.body.appendChild(script)
  })
}

const loadLeafletAssets = async () => {
  if ((window as any).L && (window as any).L.Control && (window as any).L.Control.Draw) return
  await loadStyleOnce('leaflet-style', 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css')
  await loadStyleOnce('leaflet-draw-style', 'https://cdnjs.cloudflare.com/ajax/libs/leaflet.draw/1.0.4/leaflet.draw.css')

  if (!leafletCorePromise) {
    leafletCorePromise = (async () => {
      await loadScriptOnce('leaflet-script', 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js')
      if (!(window as any).L) {
        throw new Error('Leaflet 脚本加载完成但 window.L 不存在')
      }
    })()
  }
  await leafletCorePromise

  if (!leafletDrawPromise) {
    leafletDrawPromise = (async () => {
      await loadScriptOnce('leaflet-draw-script', 'https://cdnjs.cloudflare.com/ajax/libs/leaflet.draw/1.0.4/leaflet.draw.js')
      const L = (window as any).L
      if (!L || !L.Control || typeof L.Control.Draw !== 'function') {
        throw new Error('Leaflet.draw 加载失败或版本不兼容')
      }
    })()
  }
  await leafletDrawPromise
}

const applyDrawLocaleChinese = (L: any) => {
  if (!L || !L.drawLocal) return
  const locale = L.drawLocal
  locale.draw = locale.draw || {}
  locale.edit = locale.edit || {}
  locale.draw.toolbar = locale.draw.toolbar || {}
  locale.draw.toolbar.actions = locale.draw.toolbar.actions || {}
  locale.draw.toolbar.finish = locale.draw.toolbar.finish || {}
  locale.draw.toolbar.undo = locale.draw.toolbar.undo || {}
  locale.draw.toolbar.buttons = locale.draw.toolbar.buttons || {}
  locale.draw.handlers = locale.draw.handlers || {}
  locale.draw.handlers.marker = locale.draw.handlers.marker || {}
  locale.draw.handlers.marker.tooltip = locale.draw.handlers.marker.tooltip || {}
  locale.draw.handlers.polyline = locale.draw.handlers.polyline || {}
  locale.draw.handlers.polyline.tooltip = locale.draw.handlers.polyline.tooltip || {}
  locale.draw.handlers.polygon = locale.draw.handlers.polygon || {}
  locale.draw.handlers.polygon.tooltip = locale.draw.handlers.polygon.tooltip || {}
  locale.draw.handlers.rectangle = locale.draw.handlers.rectangle || {}
  locale.draw.handlers.rectangle.tooltip = locale.draw.handlers.rectangle.tooltip || {}
  locale.draw.handlers.simpleshape = locale.draw.handlers.simpleshape || {}
  locale.draw.handlers.simpleshape.tooltip = locale.draw.handlers.simpleshape.tooltip || {}
  locale.edit.toolbar = locale.edit.toolbar || {}
  locale.edit.toolbar.actions = locale.edit.toolbar.actions || {}
  locale.edit.toolbar.actions.save = locale.edit.toolbar.actions.save || {}
  locale.edit.toolbar.actions.cancel = locale.edit.toolbar.actions.cancel || {}
  locale.edit.toolbar.actions.clearAll = locale.edit.toolbar.actions.clearAll || {}
  locale.edit.toolbar.buttons = locale.edit.toolbar.buttons || {}
  locale.edit.handlers = locale.edit.handlers || {}
  locale.edit.handlers.edit = locale.edit.handlers.edit || {}
  locale.edit.handlers.edit.tooltip = locale.edit.handlers.edit.tooltip || {}
  locale.edit.handlers.remove = locale.edit.handlers.remove || {}
  locale.edit.handlers.remove.tooltip = locale.edit.handlers.remove.tooltip || {}
  locale.edit.handlers.remove.tooltip.text = locale.edit.handlers.remove.tooltip.text || ''

  locale.draw.toolbar.actions = {
    title: '取消绘制',
    text: '取消'
  }
  locale.draw.toolbar.finish = {
    title: '完成绘制',
    text: '完成'
  }
  locale.draw.toolbar.undo = {
    title: '撤销上一步',
    text: '撤销'
  }
  locale.draw.toolbar.buttons = {
    polyline: '绘制折线',
    polygon: '绘制多边形',
    rectangle: '绘制矩形',
    circle: '绘制圆形',
    marker: '绘制点',
    circlemarker: '绘制圆点'
  }
  locale.draw.handlers.marker.tooltip.start = '点击地图放置点'
  locale.draw.handlers.polyline.tooltip = {
    start: '点击地图开始绘制线',
    cont: '继续点击绘制下一节点',
    end: '双击或点击最后一个点结束绘制'
  }
  locale.draw.handlers.polygon.tooltip = {
    start: '点击地图开始绘制面',
    cont: '继续点击绘制下一节点',
    end: '点击起点结束绘制'
  }
  locale.draw.handlers.rectangle.tooltip.start = '按下鼠标并拖动绘制矩形'
  locale.draw.handlers.simpleshape.tooltip.end = '松开鼠标完成绘制'

  locale.edit.toolbar.actions = {
    save: {
      title: '保存变更',
      text: '保存'
    },
    cancel: {
      title: '取消编辑',
      text: '取消'
    },
    clearAll: {
      title: '清空全部图形',
      text: '清空'
    }
  }
  locale.edit.toolbar.buttons = {
    edit: '编辑图形',
    editDisabled: '无可编辑图形',
    remove: '删除图形',
    removeDisabled: '无可删除图形'
  }
  locale.edit.handlers.edit.tooltip = {
    text: '拖动节点以修改图形',
    subtext: '点击取消可撤销变更'
  }
  locale.edit.handlers.remove.tooltip.text = '点击选中要删除的图形'
}

const initMap = () => {
  if (!mapRef.value) return
  Leaflet = (window as any).L
  if (!Leaflet) {
    throw new Error('Leaflet 资源加载失败')
  }
  if (!Leaflet.Control || typeof Leaflet.Control.Draw !== 'function') {
    throw new Error('Leaflet.draw 未正确加载')
  }
  applyDrawLocaleChinese(Leaflet)
  mapInstance = Leaflet.map(mapRef.value, {
    center: props.center,
    zoom: props.zoom,
    zoomControl: true
  })
  createTiandituImageryLayerGroup(Leaflet, resolvedTiandituKey.value).addTo(mapInstance)
  editableGroup = Leaflet.featureGroup().addTo(mapInstance)

  drawControl = new Leaflet.Control.Draw({
    edit: {
      featureGroup: editableGroup,
      remove: true
    },
    draw: {
      marker: canDrawPoint.value,
      polyline: canDrawLine.value,
      polygon: canDrawPolygon.value,
      rectangle: false,
      circle: false,
      circlemarker: false
    }
  })
  if (!props.compactToolbar) {
    mapInstance.addControl(drawControl)
  }
  mapInstance.on(Leaflet.Draw.Event.CREATED, onDrawCreated)
  mapInstance.on(Leaflet.Draw.Event.EDITED, syncModelFromLayers)
  mapInstance.on(Leaflet.Draw.Event.DELETED, syncModelFromLayers)
  mapCollapseExpandedNamesHandler = (event: any) => {
    if (isMapNameExpandClickTarget(event)) return
    collapseAllExpandedMapNames()
  }
  mapInstance.on('click', mapCollapseExpandedNamesHandler)

  mapReady.value = true
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
  await nextTick()
  mapInstance?.invalidateSize()
}

const scheduleResize = async () => {
  await nextTick()
  mapInstance?.invalidateSize()
  if (fullscreen.value || isDrawing.value) return
  window.setTimeout(() => {
    mapInstance?.invalidateSize()
    if (hasGeometry.value && !isDrawing.value) fitToGeometry()
  }, 80)
  window.setTimeout(() => {
    mapInstance?.invalidateSize()
    if (hasGeometry.value && !isDrawing.value) fitToGeometry()
  }, 200)
}

const resetFileInput = () => {
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const isGeoJsonGeometry = (value: any): value is GeoJsonGeometry => {
  if (!value || typeof value !== 'object') return false
  if (!GEOJSON_GEOMETRY_TYPES.has(String(value.type || ''))) return false
  if (value.type === 'GeometryCollection') {
    return Array.isArray(value.geometries)
  }
  return 'coordinates' in value
}

const extractGeometry = (payload: any): GeoJsonGeometry => {
  if (isGeoJsonGeometry(payload)) {
    return payload
  }
  if (!payload || typeof payload !== 'object') {
    throw new Error('上传文件不是有效的 GeoJSON 对象')
  }
  if (payload.type === 'Feature') {
    if (!isGeoJsonGeometry(payload.geometry)) {
      throw new Error('GeoJSON Feature 缺少有效的 geometry')
    }
    return payload.geometry
  }
  if (payload.type === 'FeatureCollection') {
    const features = Array.isArray(payload.features) ? payload.features.filter(Boolean) : []
    if (features.length !== 1) {
      throw new Error('仅支持上传单个图形，请使用单个灌区的 geometry.json')
    }
    return extractGeometry(features[0])
  }
  throw new Error('仅支持上传 geometry、Feature 或单要素 FeatureCollection')
}

const readFileAsText = (file: File) => {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result || ''))
    reader.onerror = () => reject(new Error('文件读取失败，请重新选择'))
    reader.readAsText(file, 'utf-8')
  })
}

const applyGeometryValue = async (geometryText: string) => {
  emit('update:modelValue', geometryText)
  await ensureMap()
  loadGeometry(geometryText)
  await scheduleResize()
}

const triggerUpload = () => {
  if (fileLoading.value) return
  fileInputRef.value?.click()
}

const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement | null
  const file = target?.files?.[0]
  if (!file) return
  fileLoading.value = true
  try {
    const text = await readFileAsText(file)
    const parsed = JSON.parse(text)
    const geometry = extractGeometry(parsed)
    const geometryText = JSON.stringify(geometry)
    await applyGeometryValue(geometryText)
    ElMessage.success('GIS 图形已加载，保存后即可入库')
  } catch (error: any) {
    ElMessage.error(error?.message || 'geometry.json 解析失败')
  } finally {
    fileLoading.value = false
    resetFileInput()
  }
}

const detachLineVertexEditor = () => {
  detachAllLineVertexEditors()
}

const detachAllLineVertexEditors = () => {
  lineVertexEditors.forEach((editor) => {
    editor?.highlightSegment(null)
    editor?.destroy()
  })
  lineVertexEditors.length = 0
  lineLayerGroups.length = 0
  linePayloadsCache.value = []
  activeLineIndex.value = 0
  activeLineLayerGroup = null
  isLineGeometry.value = false
  insertVertexMode.value = false
  selectedVertexIndex.value = -1
  hoveredSegmentIndex.value = null
  segmentRiskChecked.value = []
  segmentRiskNames.value = []
  segmentRiskDescriptions.value = []
  startLabel.value = defaultStartName.value
  endLabel.value = defaultEndName.value
}

const syncLineMetaFromPayload = (payload: LineGeometryPayload | null) => {
  if (!payload) {
    segmentRiskChecked.value = []
    segmentRiskNames.value = []
    segmentRiskDescriptions.value = []
    return
  }
  startLabel.value = isSharedHazardStartMode.value
    ? linePayloadsCache.value[0]?.vertexLabels?.[0] || defaultStartName.value
    : payload.vertexLabels[0] || defaultStartName.value
  endLabel.value = payload.vertexLabels[payload.vertexLabels.length - 1] || defaultEndName.value
  if (!props.segmentRiskMark) return
  segmentRiskChecked.value = [...payload.riskSegments]
  segmentRiskNames.value = [...payload.riskSegmentNames]
  segmentRiskDescriptions.value = [...payload.riskSegmentDescriptions]
}

let suppressModelValueReload = 0

const countLinesInGeoJson = (val?: string): number => {
  if (!val) return 0
  try {
    const parsed = JSON.parse(val) as { type?: string; coordinates?: unknown }
    if (parsed?.type === 'LineString' && Array.isArray(parsed.coordinates)) {
      return parsed.coordinates.length >= 2 ? 1 : 0
    }
    if (parsed?.type === 'MultiLineString' && Array.isArray(parsed.coordinates)) {
      return (parsed.coordinates as unknown[]).filter(
        (line) => Array.isArray(line) && line.length >= 2
      ).length
    }
  } catch {
    return 0
  }
  return 0
}

const syncModelFromAllLines = () => {
  if (linePayloadsCache.value.length === 0) {
    const hazardJson = buildHazardPointGeoJson()
    suppressModelValueReload += 1
    emit('update:modelValue', hazardJson)
    void nextTick(() => {
      suppressModelValueReload = Math.max(0, suppressModelValueReload - 1)
    })
    isLineGeometry.value = false
    return
  }
  isLineGeometry.value = true
  suppressModelValueReload += 1
  emit('update:modelValue', getModelValueFromCache())
  void nextTick(() => {
    suppressModelValueReload = Math.max(0, suppressModelValueReload - 1)
  })
}

const getModelValueFromCache = (): string => {
  if (linePayloadsCache.value.length === 0) return buildHazardPointGeoJson()
  alignSharedHazardInCache()
  if (props.multiLineDraw || linePayloadsCache.value.length > 1) {
    return buildMultiLineGeometryPayload(linePayloadsCache.value, lineVertexStyle.value, resolvedEndpointLabels.value)
  }
  const line = linePayloadsCache.value[0]
  return buildLineGeometryPayload(
    line.coordinates,
    line.vertexMarkers,
    lineVertexStyle.value,
    line.riskSegments,
    line.vertexLabels,
    resolvedEndpointLabels.value,
    line.riskSegmentNames,
    line.riskSegmentCenters,
    line.riskSegmentDescriptions,
    line.routeColor,
    line.linkedWarehouseId
  )
}

const isModelValueSyncedWithCache = (val: string) => {
  const cacheLineCount = linePayloadsCache.value.filter((line) => line?.coordinates?.length >= 2).length
  if (cacheLineCount === 0) {
    const hazardJson = buildHazardPointGeoJson()
    return val === hazardJson
  }
  const incomingLineCount = countLinesInGeoJson(val)
  // 父组件 v-model 仍是一条线时，地图已有多条路线，勿用旧 GeoJSON 重载并丢掉新路线
  if (incomingLineCount > 0 && incomingLineCount < cacheLineCount) return true
  if (incomingLineCount > cacheLineCount) return false
  return val === getModelValueFromCache()
}

const ROUTE_HIGHLIGHT_FLASH_MS = 2600
let routeHighlightFlashTimer: ReturnType<typeof setTimeout> | null = null

const clearActiveRouteHighlight = () => {
  lineVertexEditors.forEach((editor) => {
    editor?.setRouteActiveHighlight?.({ active: false, dimmed: false, flash: false })
  })
}

const flashActiveRouteHighlight = (lineIndex = activeLineIndex.value) => {
  if (routeHighlightFlashTimer) {
    clearTimeout(routeHighlightFlashTimer)
    routeHighlightFlashTimer = null
  }
  const multi = linePayloadsCache.value.length > 1
  lineVertexEditors.forEach((editor, editorIndex) => {
    editor?.setRouteActiveHighlight?.({
      active: editorIndex === lineIndex,
      dimmed: multi && editorIndex !== lineIndex,
      flash: editorIndex === lineIndex
    })
  })
  routeHighlightFlashTimer = setTimeout(() => {
    routeHighlightFlashTimer = null
    clearActiveRouteHighlight()
  }, ROUTE_HIGHLIGHT_FLASH_MS)
}

const onActiveLineChange = (index: number) => {
  activeLineIndex.value = index
  activeLineLayerGroup = lineLayerGroups[index] || null
  syncLineMetaFromPayload(linePayloadsCache.value[index] || null)
  selectedVertexIndex.value = -1
  insertVertexMode.value = false
  collapseRouteVertexSelectionForWarehouse()
  lineVertexEditors.forEach((editor, editorIndex) => {
    editor?.highlightSegment(null)
    if (editorIndex !== index) {
      editor?.setInsertMode(false)
    }
  })
  void nextTick(() => fitToActiveLine(index))
}

const removeActiveLine = async () => {
  const index = activeLineIndex.value
  if (linePayloadsCache.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定删除路线${index + 1}吗？`, '删除路线', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  lineVertexEditors[index]?.destroy()
  lineVertexEditors.splice(index, 1)
  const layerGroup = lineLayerGroups[index]
  if (layerGroup && editableGroup) {
    editableGroup.removeLayer(layerGroup)
  }
  lineLayerGroups.splice(index, 1)
  linePayloadsCache.value.splice(index, 1)
  if (linePayloadsCache.value.length === 0) {
    detachAllLineVertexEditors()
    renderHazardPointMarker()
    syncModelFromAllLines()
    return
  }
  activeLineIndex.value = Math.min(index, linePayloadsCache.value.length - 1)
  activeLineLayerGroup = lineLayerGroups[activeLineIndex.value] || null
  syncLineMetaFromPayload(linePayloadsCache.value[activeLineIndex.value])
  syncModelFromAllLines()
  fitToActiveLine()
  if (props.enableWarehousePicker && warehousePickerVisible.value) {
    renderWarehousePickerMarkers()
  }
  ElMessage.success('已删除当前路线')
}

const ensureSegmentRiskNamesLength = () => {
  const expected = segmentRiskChecked.value.length
  while (segmentRiskNames.value.length < expected) segmentRiskNames.value.push('')
  segmentRiskNames.value.length = expected
}

const ensureSegmentRiskDescriptionsLength = () => {
  const expected = segmentRiskChecked.value.length
  while (segmentRiskDescriptions.value.length < expected) segmentRiskDescriptions.value.push('')
  segmentRiskDescriptions.value.length = expected
}

const applyEndpointLabels = () => {
  const start = startLabel.value.trim() || defaultStartName.value
  const activeLine = linePayloadsCache.value[activeLineIndex.value]
  const endLocked = isLineSystemWarehouse(activeLine)
  let end = endLabel.value.trim() || defaultEndName.value
  if (endLocked) {
    end = (activeLine?.vertexLabels[activeLine.vertexLabels.length - 1] || end).trim()
    endLabel.value = end
  }
  if (isSharedHazardStartMode.value) {
    linePayloadsCache.value.forEach((payload, index) => {
      if (payload.vertexLabels.length > 0) payload.vertexLabels[0] = start
      if (index === activeLineIndex.value && payload.vertexLabels.length > 0 && !endLocked) {
        payload.vertexLabels[payload.vertexLabels.length - 1] = end
      }
    })
    lineVertexEditors.forEach((editor, index) => {
      if (index === activeLineIndex.value) {
        editor?.updateEndpointLabels([start, end])
      } else {
        editor?.updateStartLabel(start)
      }
    })
    renderHazardPointMarker()
    syncModelFromAllLines()
    return
  }
  if (props.startAsHazardPoint) {
    renderHazardPointMarker()
  }
  if (!endLocked) {
    getActiveLineEditor()?.updateEndpointLabels([start, end])
  } else {
    getActiveLineEditor()?.updateEndpointLabels([
      start,
      activeLine?.vertexLabels[activeLine.vertexLabels.length - 1] || end
    ])
  }
}

const onSegmentRiskChange = (index: number, checked: boolean) => {
  segmentRiskChecked.value[index] = checked
  ensureSegmentRiskNamesLength()
  ensureSegmentRiskDescriptionsLength()
  if (!checked) {
    segmentRiskNames.value[index] = ''
    segmentRiskDescriptions.value[index] = ''
  }
  getActiveLineEditor()?.setRiskSegment(index, checked)
}

const onSegmentRiskNameChange = (index: number) => {
  ensureSegmentRiskNamesLength()
  getActiveLineEditor()?.setRiskSegmentName(index, segmentRiskNames.value[index] || '')
}

const onSegmentRiskDescriptionChange = (index: number) => {
  ensureSegmentRiskDescriptionsLength()
  getActiveLineEditor()?.setRiskSegmentDescription(index, segmentRiskDescriptions.value[index] || '')
}

const removeSegment = async (index: number) => {
  if (segmentRiskChecked.value.length <= 1) {
    ElMessage.warning('至少需要保留一段线段')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除线段${index + 1}吗？删除后相邻线段将自动连接。`, '删除线段', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  const editor = getActiveLineEditor()
  if (!editor?.deleteSegment(index)) {
    ElMessage.warning('删除失败，请至少保留一段线段')
    return
  }
  selectedVertexIndex.value = -1
  hoveredSegmentIndex.value = null
  editor.highlightSegment(null)
  ElMessage.success(`已删除线段${index + 1}`)
}

const onSegmentListHover = (index: number | null) => {
  hoveredSegmentIndex.value = index
  getActiveLineEditor()?.highlightSegment(index)
}

const getSegmentDisplayLabel = (index: number) => {
  const customName = (segmentRiskNames.value[index] || '').trim()
  if (customName) return customName
  return `线段${index + 1}`
}

const clearGeometry = () => {
  if (!editableGroup) return
  stopHazardPointPlacement(true)
  cancelActiveDraw(true)
  hideWarehousePicker()
  clearSharedHazardState()
  detachAllLineVertexEditors()
  editableGroup.clearLayers()
  syncGeometryLayerCount()
  editing.value = false
  emit('update:modelValue', '')
}

const handleCompactDraw = () => {
  if (!mapReady.value) return
  if (isPlacingHazardPoint.value) {
    stopHazardPointPlacement()
    return
  }
  if (warehouseLinkSlotAvailable.value) {
    resetWarehouseLinkSlot()
    renderWarehousePickerMarkers()
    return
  }
  if (isDrawing.value) {
    cancelActiveDraw()
    return
  }
  if (canDrawLine.value) {
    if (systemWarehouseOnly.value) {
      void startSystemWarehouseLink()
      return
    }
    if (props.startAsHazardPoint && !getSharedHazardCoordinate()) {
      ElMessage.warning('请先点击「绘制隐患点」在地图上标注位置')
      return
    }
    startDraw('LINESTRING', { notifyWarehouseLink: true })
    return
  }
  if (canDrawPoint.value) {
    startDraw('POINT')
    return
  }
  ElMessage.warning('当前不支持绘制')
}

const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确定清除地图上的全部标绘内容吗？', '全部清除', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    clearGeometry()
    ElMessage.success('已清除全部标绘')
  } catch {
    // 用户取消
  }
}

const attachLineEditorAt = (lineIndex: number, payload: LineGeometryPayload) => {
  if (!Leaflet || !mapInstance || !props.enhancedLineEdit) return
  const lineLayerGroup = lineLayerGroups[lineIndex]
  if (!lineLayerGroup) return
  if (lineVertexEditors[lineIndex]) {
    lineVertexEditors[lineIndex]?.destroy()
  }
  const normalizedPayload = props.startAsHazardPoint ? clearLineRiskSegmentMeta(payload) : payload
  linePayloadsCache.value[lineIndex] = normalizedPayload
  const lineRouteColor = getRouteLineColor(lineIndex)
  normalizedPayload.routeColor = lineRouteColor
  linePayloadsCache.value[lineIndex] = normalizedPayload
  activeLineLayerGroup = lineLayerGroup
  lineVertexEditors[lineIndex] = attachLineVertexEditor({
    L: Leaflet,
    map: mapInstance,
    lineLayerGroup,
    coordinates: normalizedPayload.coordinates,
    vertexMarkers: normalizedPayload.vertexMarkers,
    vertexLabels: normalizedPayload.vertexLabels,
    riskSegments: normalizedPayload.riskSegments,
    riskSegmentNames: normalizedPayload.riskSegmentNames,
    riskSegmentDescriptions: normalizedPayload.riskSegmentDescriptions,
    riskSegmentCenters: normalizedPayload.riskSegmentCenters,
    lineVertexAsArrow: props.lineVertexAsArrow,
    endpointAsLocation: props.lineEndpointAsLocation !== false,
    endpointLabels: endpointLabelsForLine(lineIndex),
    startAsHazardPoint: props.startAsHazardPoint,
    lineIndex,
    routeColor: lineRouteColor,
    routeHoverColor: getRouteHoverColorFromBaseColor(lineRouteColor),
    linkedWarehouseId: normalizedPayload.linkedWarehouseId,
    shouldHideRouteEndpointIcon: shouldHideRouteEndpointIconAt,
    lockStartVertex: props.startAsHazardPoint,
    lockEndVertex: isLineSystemWarehouse(normalizedPayload),
    lockedEndCoordinate: resolveLineLockedEndCoordinate(normalizedPayload),
    onStartCoordinateChange: props.startAsHazardPoint
      ? (coord) => syncSharedHazardCoordinateToAllLines(coord, lineIndex)
      : undefined,
    segmentRiskMark: props.segmentRiskMark && !props.startAsHazardPoint,
    onChange: (nextPayload) => {
      const prev = linePayloadsCache.value[lineIndex]
      let linkedWarehouseId = prev?.linkedWarehouseId
      if (linkedWarehouseId != null && nextPayload.coordinates.length >= 2) {
        const warehouse = warehouseMapPoints.value.find(
          (item) => String(item.id) === String(linkedWarehouseId)
        )
        if (warehouse) {
          const last = nextPayload.coordinates.length - 1
          nextPayload.coordinates[last] = [warehouse.longitude, warehouse.latitude]
        }
      }
      linePayloadsCache.value[lineIndex] = {
        ...nextPayload,
        routeColor: nextPayload.routeColor || prev?.routeColor || lineRouteColor,
        linkedWarehouseId
      }
      if (activeLineIndex.value === lineIndex) {
        startLabel.value = isSharedHazardStartMode.value
          ? linePayloadsCache.value[0]?.vertexLabels?.[0] || defaultStartName.value
          : nextPayload.vertexLabels[0] || defaultStartName.value
        endLabel.value =
          nextPayload.vertexLabels[nextPayload.vertexLabels.length - 1] ||
          (props.startAsHazardPoint ? getDefaultWarehouseName(lineIndex) : defaultEndName.value)
        if (prev?.linkedWarehouseId && linkedWarehouseId == null) {
          renderWarehousePickerMarkers()
        }
        if (props.segmentRiskMark) {
          segmentRiskChecked.value = [...nextPayload.riskSegments]
          segmentRiskNames.value = [...nextPayload.riskSegmentNames]
          segmentRiskDescriptions.value = [...nextPayload.riskSegmentDescriptions]
          ensureSegmentRiskNamesLength()
          ensureSegmentRiskDescriptionsLength()
        }
      }
      if (!syncingSharedHazard) {
        syncModelFromAllLines()
        if (props.enableWarehousePicker && warehousePickerVisible.value) {
          renderWarehousePickerMarkers()
        }
      }
    },
    onSelect: (index) => {
      activeLineIndex.value = lineIndex
      selectedVertexIndex.value = index
    }
  })
}

const createInitialLinePayload = (coordinates: [number, number][], lineIndex: number): LineGeometryPayload => {
  const vertexMarkers = createVertexMarkers(coordinates.length, lineVertexStyle.value, [])
  const vertexLabels = createVertexLabels(coordinates.length, [], endpointLabelsForLine(lineIndex))
  const riskSegments = createRiskSegments(coordinates.length - 1)
  const riskSegmentNames = createRiskSegmentNames(riskSegments.length)
  const riskSegmentDescriptions = createRiskSegmentDescriptions(riskSegments.length)
  const riskSegmentCenters = syncRiskSegmentCenters(coordinates, riskSegments)
  return {
    coordinates,
    vertexMarkers,
    vertexLabels,
    riskSegments,
    riskSegmentNames,
    riskSegmentDescriptions,
    riskSegmentCenters
  }
}

const appendLineFromCoordinates = (
  coordinates: [number, number][],
  endLabelOverride?: string,
  routeColorOverride?: string,
  linkedWarehouseIdOverride?: string | number
) => {
  if (!editableGroup || !Leaflet || coordinates.length < 2) return
  if (
    systemWarehouseOnly.value &&
    (linkedWarehouseIdOverride == null || !String(linkedWarehouseIdOverride).trim())
  ) {
    ElMessage.warning('当前仅支持关联系统仓库')
    return
  }
  const lineLayerGroup = Leaflet.layerGroup()
  editableGroup.addLayer(lineLayerGroup)
  const lineIndex = lineLayerGroups.length
  lineLayerGroups.push(lineLayerGroup)
  const normalizedCoordinates = normalizeLineCoordinatesForSharedHazard(coordinates, lineIndex)
  const payload = createInitialLinePayload(normalizedCoordinates, lineIndex)
  if (isSharedHazardStartMode.value && lineIndex > 0) {
    payload.vertexLabels[0] =
      linePayloadsCache.value[0]?.vertexLabels?.[0] || defaultStartName.value
  }
  const endLabelText = (endLabelOverride || '').trim() || getDefaultWarehouseName(lineIndex)
  if (payload.vertexLabels.length > 0) {
    payload.vertexLabels[payload.vertexLabels.length - 1] = endLabelText
  }
  void routeColorOverride
  payload.routeColor = getRouteLineColor(lineIndex)
  if (linkedWarehouseIdOverride != null && String(linkedWarehouseIdOverride).trim()) {
    payload.linkedWarehouseId = linkedWarehouseIdOverride
  }
  if (props.enableWarehousePicker) {
    enrichLineWarehouseMeta([payload])
  }
  attachLineEditorAt(lineIndex, payload)
  activeLineIndex.value = lineIndex
  syncLineMetaFromPayload(payload)
  isLineGeometry.value = true
  syncModelFromAllLines()
  fitToActiveLine(lineIndex)
  if (props.enableWarehousePicker && warehousePickerVisible.value) {
    renderWarehousePickerMarkers()
  }
}

const loadMultiLineGeometry = async (geoJson: string, preferredActiveIndex = 0) => {
  if (props.enableWarehousePicker) {
    try {
      await ensureWarehouseMapPoints()
    } catch {
      // 仓库列表加载失败时仍继续渲染路线
    }
  }
  const multi = parseMultiLineGeometryPayload(geoJson, lineVertexStyle.value, endpointLabelsForLine(0))
  if (!multi || multi.lines.length === 0) {
    ElMessage.warning('线段数据格式不正确')
    return
  }
  if (isSharedHazardStartMode.value && multi.lines.length > 1) {
    const sharedCoord = multi.lines[0].coordinates[0]
    const sharedLabel = multi.lines[0].vertexLabels[0] || defaultStartName.value
    multi.lines.forEach((line, index) => {
      if (index === 0 || !sharedCoord) return
      if (line.coordinates.length > 0) {
        line.coordinates[0] = [sharedCoord[0], sharedCoord[1]]
      }
      if (line.vertexLabels.length > 0) {
        line.vertexLabels[0] = sharedLabel
      }
    })
  }
  if (props.startAsHazardPoint) {
    multi.lines.forEach((line, index) => {
      if (line.vertexLabels.length === 0) return
      if (!line.vertexLabels[0]?.trim()) {
        line.vertexLabels[0] = defaultStartName.value
      }
      const last = line.vertexLabels.length - 1
      if (!line.vertexLabels[last]?.trim()) {
        line.vertexLabels[last] = getDefaultWarehouseName(index)
      }
    })
  }
  enrichLineWarehouseMeta(multi.lines)
  applyRoutePaletteToLines(multi.lines)
  multi.lines.forEach((line, index) => {
    const lineLayerGroup = Leaflet.layerGroup()
    editableGroup.addLayer(lineLayerGroup)
    lineLayerGroups.push(lineLayerGroup)
    attachLineEditorAt(index, line)
  })
  const nextActive = Math.min(Math.max(0, preferredActiveIndex), multi.lines.length - 1)
  activeLineIndex.value = nextActive
  activeLineLayerGroup = lineLayerGroups[nextActive] || null
  syncLineMetaFromPayload(linePayloadsCache.value[nextActive] || null)
  isLineGeometry.value = true
  if (props.startAsHazardPoint) {
    const hazardCoord = multi.lines[0]?.coordinates?.[0]
    if (hazardCoord) {
      setSharedHazardCoordinate([hazardCoord[0], hazardCoord[1]], { syncLines: false, emitModel: false })
    }
    syncModelFromAllLines()
  }
  fitToActiveLine(nextActive)
  if (props.enableWarehousePicker && warehousePickerVisible.value) {
    renderWarehousePickerMarkers()
  }
  if (props.enableWarehousePicker) {
    void ensureWarehouseMapPoints()
      .then(() => {
        enrichLineWarehouseMeta(linePayloadsCache.value)
        refreshLineEditorsForWarehouseDisplay()
        syncModelFromAllLines()
        if (warehousePickerVisible.value) {
          renderWarehousePickerMarkers()
        }
      })
      .catch(() => undefined)
  }
}

/** 将 GeoJSON 图层展平加入可编辑组（嵌套 FeatureGroup 无法被 Leaflet.draw 编辑） */
const addFlattenedEditableLayer = (layer: any) => {
  if (!layer || !editableGroup) return
  const isLeaf =
    typeof layer.getLatLngs === 'function' ||
    typeof layer.getLatLng === 'function' ||
    (Leaflet &&
      (layer instanceof Leaflet.Polygon ||
        layer instanceof Leaflet.Polyline ||
        layer instanceof Leaflet.Marker ||
        layer instanceof Leaflet.CircleMarker))
  if (!isLeaf && typeof layer.eachLayer === 'function') {
    layer.eachLayer((child: any) => addFlattenedEditableLayer(child))
    return
  }
  editableGroup.addLayer(layer)
}

const maybeAutoEnableEdit = () => {
  if (!props.autoEdit || editing.value || !hasGeometry.value) return
  void nextTick(() => {
    if (!props.autoEdit || editing.value || !hasGeometry.value) return
    enableEdit()
  })
}

const loadGeometry = (geoJson: string, preferredActiveIndex = 0) => {
  if (!editableGroup || !Leaflet) return
  stopHazardPointPlacement(true)
  clearSharedHazardState()
  detachAllLineVertexEditors()
  editableGroup.clearLayers()
  syncGeometryLayerCount()
  if (!geoJson) return
  let text = geoJson
  if (typeof geoJson !== 'string') {
    try {
      text = JSON.stringify(geoJson)
    } catch {
      ElMessage.warning('GeoJSON 格式不正确')
      return
    }
  }
  let parsed: any
  try {
    parsed = JSON.parse(text)
  } catch {
    ElMessage.warning('GeoJSON 解析失败')
    return
  }
  if (
    props.startAsHazardPoint &&
    parsed?.type === 'Point' &&
    Array.isArray(parsed.coordinates) &&
    parsed.coordinates.length >= 2
  ) {
    const lon = Number(parsed.coordinates[0])
    const lat = Number(parsed.coordinates[1])
    if (Number.isFinite(lon) && Number.isFinite(lat)) {
      const labels = parsed.vertexLabels
      if (Array.isArray(labels) && labels[0]) {
        startLabel.value = String(labels[0])
      }
      setSharedHazardCoordinate([lon, lat], { syncLines: false, emitModel: false })
      syncModelFromAllLines()
      syncGeometryLayerCount()
      fitToHazardPoint()
      maybeAutoEnableEdit()
      return
    }
  }
  if (props.enhancedLineEdit && (parsed?.type === 'LineString' || parsed?.type === 'MultiLineString')) {
    void loadMultiLineGeometry(geoJson, preferredActiveIndex).then(() => {
      syncGeometryLayerCount()
      maybeAutoEnableEdit()
    })
    return
  }
  const layer = Leaflet.geoJSON(parsed, {
    style: {
      color: '#ff4d4f',
      weight: 3
    },
    pointToLayer: (_feature: any, latlng: any) => Leaflet.circleMarker(latlng, { radius: 6, color: '#ff4d4f' })
  })
  layer.eachLayer((l: any) => addFlattenedEditableLayer(l))
  syncGeometryLayerCount()
  isLineGeometry.value = parsed?.type === 'LineString' || parsed?.type === 'MultiLineString'
  fitToGeometry()
  maybeAutoEnableEdit()
}

/** 从可编辑图层导出 geometry JSON（面/点等；不含增强折线缓存） */
const exportEditableGroupGeoJson = (): string => {
  if (!editableGroup || typeof editableGroup.toGeoJSON !== 'function') return ''
  const fc = editableGroup.toGeoJSON()
  const features = Array.isArray(fc?.features) ? fc.features.filter(Boolean) : []
  if (!features.length) return ''
  if (features.length === 1) {
    const geometry = features[0]?.geometry
    return geometry ? JSON.stringify(geometry) : ''
  }
  const polygons = features
    .map((f: any) => f?.geometry)
    .filter((g: any) => g && (g.type === 'Polygon' || g.type === 'MultiPolygon'))
  if (polygons.length === 0) {
    const geometry = features[0]?.geometry
    return geometry ? JSON.stringify(geometry) : ''
  }
  const coordinates: any[] = []
  for (const g of polygons) {
    if (g.type === 'Polygon') coordinates.push(g.coordinates)
    else if (g.type === 'MultiPolygon' && Array.isArray(g.coordinates)) {
      coordinates.push(...g.coordinates)
    }
  }
  return JSON.stringify({ type: 'MultiPolygon', coordinates })
}

const syncModelFromLayers = () => {
  if (!editableGroup) return
  const layers = editableGroup.getLayers ? editableGroup.getLayers() : []
  if (!layers || layers.length === 0) {
    emit('update:modelValue', '')
    isLineGeometry.value = false
    return
  }
  const first = layers[0]
  if (!first || !first.toGeoJSON) {
    emit('update:modelValue', '')
    isLineGeometry.value = false
    return
  }
  const feature = first.toGeoJSON()
  const geometry = feature.geometry
  if (geometry?.type === 'LineString' && props.enhancedLineEdit && linePayloadsCache.value.length > 0) {
    return
  }
  if (geometry?.type === 'MultiLineString' && props.enhancedLineEdit && linePayloadsCache.value.length > 0) {
    return
  }
  const exported = exportEditableGroupGeoJson()
  if (!exported) {
    emit('update:modelValue', '')
    isLineGeometry.value = false
    return
  }
  try {
    const parsed = JSON.parse(exported)
    isLineGeometry.value = parsed?.type === 'LineString' || parsed?.type === 'MultiLineString'
  } catch {
    isLineGeometry.value = false
  }
  emit('update:modelValue', exported)
}

const enableDoubleClickZoom = () => {
  if (mapInstance?.doubleClickZoom && !mapInstance.doubleClickZoom.enabled()) {
    mapInstance.doubleClickZoom.enable()
  }
}

const disableDoubleClickZoom = () => {
  if (mapInstance?.doubleClickZoom?.enabled()) {
    mapInstance.doubleClickZoom.disable()
  }
}

const isCustomLineDrawing = () =>
  isDrawing.value && props.enhancedLineEdit && drawingMode.value === 'LINESTRING'

const disableAllLeafletDrawHandlers = () => {
  forceDisableLeafletDrawHandler()
  if (!drawControl || !mapInstance) return
  try {
    const modes = drawControl._toolbars?.draw?._modes
    if (!modes) return
    Object.values(modes).forEach((mode: any) => {
      const handler = mode?.handler
      if (!handler) return
      try {
        if (handler._guideLine) {
          mapInstance.removeLayer(handler._guideLine)
          handler._guideLine = null
        }
        if (handler._poly && handler._poly !== customLineDrawPreview) {
          mapInstance.removeLayer(handler._poly)
          handler._poly = null
        }
        handler.disable?.()
      } catch {
        // 忽略
      }
    })
  } catch {
    // 忽略
  }
}

const clearCustomLineDrawPreview = () => {
  if (customLineDrawPreview && mapInstance) {
    try {
      mapInstance.removeLayer(customLineDrawPreview)
    } catch {
      // 忽略
    }
    customLineDrawPreview = null
  }
  if (customLineDrawVertexLayer && mapInstance) {
    try {
      mapInstance.removeLayer(customLineDrawVertexLayer)
    } catch {
      // 忽略
    }
    customLineDrawVertexLayer = null
  }
}

const forceDisableLeafletDrawHandler = () => {
  if (!activeDrawHandler || !mapInstance) return
  const handler = activeDrawHandler
  try {
    if (handler._markers?.length) {
      handler._markers.forEach((marker: any) => {
        try {
          mapInstance.removeLayer(marker)
        } catch {
          // 忽略
        }
      })
    }
    if (handler._poly) {
      mapInstance.removeLayer(handler._poly)
    }
    if (handler._guideLine) {
      mapInstance.removeLayer(handler._guideLine)
    }
    handler.disable?.()
  } catch {
    try {
      handler.disable?.()
    } catch {
      // 忽略
    }
  }
  activeDrawHandler = null
}

const stopCustomLineDrawListeners = () => {
  if (mapInstance && customLineDrawClickHandler) {
    mapInstance.off('click', customLineDrawClickHandler)
  }
  if (mapInstance && customLineDrawMoveHandler) {
    mapInstance.off('mousemove', customLineDrawMoveHandler)
  }
  if (mapInstance && customLineDrawDblClickHandler) {
    mapInstance.off('dblclick', customLineDrawDblClickHandler)
  }
  if (customLineDrawContainerEl && customLineDrawDblClickHandler) {
    customLineDrawContainerEl.removeEventListener('dblclick', customLineDrawDblClickHandler, true)
  }
  if (customLineDrawContainerEl && customLineDrawMouseDownHandler) {
    customLineDrawContainerEl.removeEventListener('mousedown', customLineDrawMouseDownHandler, true)
    customLineDrawContainerEl = null
  }
  customLineDrawLastMouseDownTime = 0
  if (customLineDrawMoveRaf) {
    cancelAnimationFrame(customLineDrawMoveRaf)
    customLineDrawMoveRaf = 0
  }
  customLineDrawMoveCursor = null
  if (mapInstance?.dragging && mapDraggingWasEnabled) {
    mapInstance.dragging.enable()
  }
  mapDraggingWasEnabled = false
  customLineDrawFinishing = false
  customLineDrawClickHandler = null
  customLineDrawMoveHandler = null
  customLineDrawMouseDownHandler = null
  customLineDrawDblClickHandler = null
  clearCustomLineDrawPreview()
  customLineDrawCoords = []
}

const updateCustomLineDrawPreview = (cursor?: { lat: number; lng: number }) => {
  if (!customLineDrawPreview || !Leaflet) return
  const fixed = customLineDrawCoords.map(([lon, lat]) => Leaflet.latLng(lat, lon))
  if (cursor && fixed.length > 0) {
    fixed.push(Leaflet.latLng(cursor.lat, cursor.lng))
  }
  customLineDrawPreview.setLatLngs(fixed)
}

const addCustomLineDrawPoint = (latlng: { lat: number; lng: number }) => {
  customLineDrawCoords.push([latlng.lng, latlng.lat])
  if (customLineDrawVertexLayer && Leaflet) {
    customLineDrawVertexLayer.addLayer(
      Leaflet.circleMarker(Leaflet.latLng(latlng.lat, latlng.lng), {
        radius: 5,
        color: pendingRouteLineColor.value,
        weight: 2,
        fillColor: '#fff',
        fillOpacity: 1,
        interactive: false,
        bubblingMouseEvents: false
      })
    )
  }
  updateCustomLineDrawPreview()
}

const undoLastCustomLineDrawPoint = () => {
  const minPoints = sharedHazardDrawMinPoints()
  if (customLineDrawCoords.length <= minPoints) return
  customLineDrawCoords.pop()
  if (customLineDrawVertexLayer?.getLayers) {
    const layers = customLineDrawVertexLayer.getLayers()
    const last = layers[layers.length - 1]
    if (last) {
      customLineDrawVertexLayer.removeLayer(last)
    }
  }
  updateCustomLineDrawPreview()
}

const handleCustomLineDrawFinish = (event?: any, undoLastPoint = false) => {
  if (!isCustomLineDrawing() || customLineDrawFinishing) return
  if (event && Leaflet) {
    Leaflet.DomEvent.preventDefault(event)
    Leaflet.DomEvent.stopPropagation(event)
  }
  if (undoLastPoint) {
    undoLastCustomLineDrawPoint()
  }
  customLineDrawFinishing = true
  finishCustomLineDraw()
  customLineDrawFinishing = false
}

const createLineGeometryFromCoordinates = (coordinates: [number, number][]) => {
  if (!props.multiLineDraw) {
    detachAllLineVertexEditors()
    editableGroup?.clearLayers()
  }
  appendLineFromCoordinates(coordinates)
  if (props.startAsHazardPoint) {
    const hazardCoord = linePayloadsCache.value[0]?.coordinates?.[0]
    if (hazardCoord) {
      setSharedHazardCoordinate([hazardCoord[0], hazardCoord[1]], { syncLines: false, emitModel: false })
    }
  }
  editing.value = false
  if (props.enableWarehousePicker && warehousePickerVisible.value) {
    warehouseLinkSlotAvailable.value = false
    renderWarehousePickerMarkers()
  }
  const lineNo = linePayloadsCache.value.length
  ElMessage.success(
    props.multiLineDraw && lineNo > 1
      ? isSharedHazardStartMode.value
        ? `路线${lineNo}已添加（从同一隐患点至仓库），可继续绘制其他仓库路线`
        : `路线${lineNo}绘制完成，可继续点「绘制」添加路线`
      : '线段绘制完成，可拖动节点调节位置'
  )
}

const finishCustomLineDraw = () => {
  if (systemWarehouseOnly.value) {
    cancelCustomLineDraw(true)
    ElMessage.warning('当前仅支持关联系统仓库，请点击「关联仓库」后选择地图上的仓库')
    return
  }
  const coordinates = customLineDrawCoords.map((item) => [...item] as [number, number])
  stopCustomLineDrawListeners()
  cleanupDrawState()
  disableAllLeafletDrawHandlers()
  if (coordinates.length < 2) {
    ElMessage.warning('线段至少需要 2 个点')
    return
  }
  createLineGeometryFromCoordinates(coordinates)
}

const cancelCustomLineDraw = (silent = false) => {
  stopCustomLineDrawListeners()
  cleanupDrawState()
  disableAllLeafletDrawHandlers()
  if (!silent) {
    ElMessage.info('已取消绘制')
  }
}

const startCustomLineDraw = () => {
  stopCustomLineDrawListeners()
  disableAllLeafletDrawHandlers()
  disableDoubleClickZoom()
  if (mapInstance?.dragging) {
    mapDraggingWasEnabled = mapInstance.dragging.enabled()
    mapInstance.dragging.disable()
  }
  isDrawing.value = true
  drawingMode.value = 'LINESTRING'
  customLineDrawCoords = []
  const sharedStart = props.startAsHazardPoint ? getSharedHazardCoordinate() : null
  if (sharedStart) {
    customLineDrawCoords.push([sharedStart[0], sharedStart[1]])
  }
  customLineDrawPreview = Leaflet.polyline([], {
    color: pendingRouteLineColor.value,
    weight: LINE_SEGMENT_WEIGHT_DEFAULT,
    dashArray: '10 8',
    interactive: false
  }).addTo(mapInstance)
  customLineDrawVertexLayer = Leaflet.layerGroup([], { interactive: false }).addTo(mapInstance)
  if (sharedStart) {
    customLineDrawVertexLayer.addLayer(
      Leaflet.circleMarker(Leaflet.latLng(sharedStart[1], sharedStart[0]), {
        radius: 7,
        color: '#fa541c',
        weight: 2,
        fillColor: '#fff7e6',
        fillOpacity: 1,
        interactive: false
      })
    )
    updateCustomLineDrawPreview()
  }

  customLineDrawClickHandler = (event: any) => {
    addCustomLineDrawPoint(event.latlng)
  }

  customLineDrawMoveHandler = (event: any) => {
    if (customLineDrawCoords.length === 0) return
    customLineDrawMoveCursor = event.latlng
    if (customLineDrawMoveRaf) return
    customLineDrawMoveRaf = requestAnimationFrame(() => {
      customLineDrawMoveRaf = 0
      const cursor = customLineDrawMoveCursor
      if (cursor) updateCustomLineDrawPreview(cursor)
    })
  }

  customLineDrawDblClickHandler = (event: any) => {
    // dblclick 前会先触发一次 click 多加一个点，需撤销
    handleCustomLineDrawFinish(event, true)
  }

  customLineDrawMouseDownHandler = (event: MouseEvent) => {
    const now = Date.now()
    if (now - customLineDrawLastMouseDownTime <= 400) {
      event.preventDefault()
      event.stopPropagation()
      customLineDrawLastMouseDownTime = 0
      // mousedown 在 click 之前触发，不会多加节点
      handleCustomLineDrawFinish(event, false)
      return
    }
    customLineDrawLastMouseDownTime = now
  }

  mapInstance.on('click', customLineDrawClickHandler)
  mapInstance.on('mousemove', customLineDrawMoveHandler)
  mapInstance.on('dblclick', customLineDrawDblClickHandler)
  customLineDrawContainerEl = mapInstance.getContainer()
  customLineDrawContainerEl.addEventListener('dblclick', customLineDrawDblClickHandler, true)
  customLineDrawContainerEl.addEventListener('mousedown', customLineDrawMouseDownHandler, true)
  ElMessage.info(
    isSharedHazardStartMode.value
      ? linePayloadsCache.value.length > 0
        ? '从隐患点出发：单击添加路径拐点，双击完成至仓库的路线'
        : '从隐患点出发：单击添加路径拐点，双击完成本段路线'
      : props.startAsHazardPoint
        ? '从隐患点出发：单击添加路径拐点，双击完成至仓库的路线'
        : props.multiLineDraw && linePayloadsCache.value.length > 0
          ? '继续绘制新路线：单击添加节点，双击结束'
          : '单击添加节点，双击结束绘制'
  )
}

const cleanupDrawState = () => {
  stopCustomLineDrawListeners()
  disableAllLeafletDrawHandlers()
  isDrawing.value = false
  drawingMode.value = null
  enableDoubleClickZoom()
  if (mapInstance && Leaflet) {
    mapInstance.off(Leaflet.Draw.Event.DRAWSTOP, onDrawStop)
  }
  if (props.enableWarehousePicker) {
    resetWarehouseLinkSlot()
    if (warehousePickerVisible.value) {
      renderWarehousePickerMarkers()
    }
  }
}

const onDrawStop = () => {
  if (isCustomLineDrawing()) return
  cleanupDrawState()
}

const finishActiveDraw = () => {
  if (isCustomLineDrawing()) {
    finishCustomLineDraw()
    return
  }
  if (!activeDrawHandler) return
  const handler = activeDrawHandler
  try {
    const markerCount = handler._markers?.length ?? 0
    if (drawingMode.value === 'LINESTRING' && markerCount < 2) {
      ElMessage.warning('线段至少需要 2 个点')
      return
    }
    if (typeof handler.completeShape === 'function') {
      handler.completeShape()
      return
    }
    forceDisableLeafletDrawHandler()
  } catch {
    forceDisableLeafletDrawHandler()
  }
  cleanupDrawState()
}

const cancelActiveDraw = (silent = false) => {
  if (isCustomLineDrawing()) {
    cancelCustomLineDraw(silent)
    return
  }
  forceDisableLeafletDrawHandler()
  cleanupDrawState()
  if (!silent) {
    ElMessage.info('已取消绘制')
  }
}

const onDrawCreated = (event: any) => {
  if (!editableGroup) return
  cleanupDrawState()
  if (props.enhancedLineEdit && props.multiLineDraw) {
    // 多路线模式不清除已有图形
  } else {
    detachAllLineVertexEditors()
    editableGroup.clearLayers()
  }
  const layer = event.layer
  if (props.enhancedLineEdit && layer?.getLatLngs) {
    const latlngs = layer.getLatLngs()
    const flatLatlngs = Array.isArray(latlngs[0]) ? latlngs[0] : latlngs
    const coordinates = (flatLatlngs || []).map((item: any) => [item.lng, item.lat] as [number, number])
    if (coordinates.length >= 2) {
      createLineGeometryFromCoordinates(coordinates)
      syncGeometryLayerCount()
      return
    }
  }
  editableGroup.addLayer(layer)
  syncGeometryLayerCount()
  editing.value = false
  syncModelFromLayers()
  fitToGeometry()
}

const toggleInsertVertexMode = () => {
  const editor = getActiveLineEditor()
  if (!editor) {
    ElMessage.warning('请先绘制线段')
    return
  }
  const enabled = editor.setInsertMode(!insertVertexMode.value)
  insertVertexMode.value = enabled
  ElMessage.info(enabled ? '请在地图线段附近点击以插入节点' : '已取消插入节点')
}

const deleteSelectedVertex = () => {
  const editor = getActiveLineEditor()
  if (!editor) return
  const ok = editor.deleteSelectedVertex()
  if (!ok) {
    ElMessage.warning('请先选中节点，且线段至少保留 2 个节点')
    return
  }
  selectedVertexIndex.value = -1
}

const startDraw = (mode: DrawMode, options?: { notifyWarehouseLink?: boolean }) => {
  if (!Leaflet || !mapInstance) return
  if (!drawModeSet.value.has(mode)) {
    ElMessage.warning('当前不支持绘制该类型图形')
    return
  }
  if (mode === 'LINESTRING' && props.startAsHazardPoint && !getSharedHazardCoordinate()) {
    ElMessage.warning('请先点击「绘制隐患点」在地图上标注位置')
    return
  }
  cancelActiveDraw(true)
  if (mode === 'LINESTRING' && systemWarehouseOnly.value) {
    void startSystemWarehouseLink()
    return
  }
  if (mode === 'LINESTRING' && props.enhancedLineEdit) {
    startCustomLineDraw()
    if (props.enableWarehousePicker && warehousePickerVisible.value && getSharedHazardCoordinate()) {
      tryEnableWarehouseLinkSlot(Boolean(options?.notifyWarehouseLink))
    }
    return
  }
  disableDoubleClickZoom()
  isDrawing.value = true
  drawingMode.value = mode
  const shapeOptions = {
    color: LINE_SEGMENT_COLOR_DEFAULT,
    weight: LINE_SEGMENT_WEIGHT_DEFAULT
  }
  if (mode === 'POINT') {
    activeDrawHandler = new Leaflet.Draw.Marker(mapInstance)
  } else if (mode === 'LINESTRING') {
    activeDrawHandler = new Leaflet.Draw.Polyline(mapInstance, {
      shapeOptions,
      repeatMode: false
    })
  } else {
    activeDrawHandler = new Leaflet.Draw.Polygon(mapInstance, { shapeOptions })
  }
  mapInstance.off(Leaflet.Draw.Event.DRAWSTOP, onDrawStop)
  mapInstance.on(Leaflet.Draw.Event.DRAWSTOP, onDrawStop)
  activeDrawHandler.enable()
  if (mode === 'LINESTRING') {
    ElMessage.info('点击添加节点，双击最后一点或点「完成绘制」结束')
  } else {
    ElMessage.info('请在地图上绘制图形')
  }
}

const enableEdit = () => {
  if (!Leaflet || !mapInstance || !editableGroup) return
  syncGeometryLayerCount()
  const layerCount = editableGroup.getLayers?.()?.length || 0
  if (layerCount <= 0) {
    ElMessage.warning('当前没有可编辑的图形')
    return
  }
  // 启用编辑：直接使用 draw 控件的 edit handler
  try {
    // Leaflet.draw 通过 toolbar 打开编辑模式，这里使用内部 API 开启
    const editToolbar: any = (drawControl as any)?._toolbars?.edit
    const handler = editToolbar?._modes?.edit?.handler
    if (!handler?.enable) {
      ElMessage.warning('进入编辑模式失败')
      return
    }
    handler.enable()
    editing.value = true
  } catch {
    ElMessage.warning('进入编辑模式失败')
  }
}

const disableEdit = () => {
  if (!Leaflet || !mapInstance) return
  try {
    const editToolbar: any = (drawControl as any)?._toolbars?.edit
    editToolbar?._modes?.edit?.handler?.disable?.()
    editing.value = false
    syncModelFromLayers()
  } catch {
    editing.value = false
  }
}

const fitToGeometry = () => {
  if (!mapInstance || !editableGroup) return
  const bounds = editableGroup.getBounds ? editableGroup.getBounds() : null
  if (bounds && bounds.isValid && bounds.isValid()) {
    mapInstance.fitBounds(bounds, { padding: [20, 20], maxZoom: 16 })
  }
  mapInstance.invalidateSize()
}

/** 定位当前路线：以终点（仓库）为参考，视野拉远展示整段路线 */
const fitToActiveLine = (lineIndex = activeLineIndex.value) => {
  if (!mapInstance || !Leaflet) return
  const coordinates = linePayloadsCache.value[lineIndex]?.coordinates
  if (!coordinates?.length) return
  const latLngs = coordinates
    .map((item) => {
      if (!Array.isArray(item) || item.length < 2) return null
      const lon = Number(item[0])
      const lat = Number(item[1])
      if (!Number.isFinite(lon) || !Number.isFinite(lat)) return null
      return Leaflet.latLng(lat, lon)
    })
    .filter(Boolean)
  if (latLngs.length === 0) return
  if (latLngs.length === 1) {
    mapInstance.setView(latLngs[0], 16, { animate: true })
  } else {
    const bounds = Leaflet.latLngBounds(latLngs)
    if (bounds?.isValid?.()) {
      mapInstance.fitBounds(bounds, { padding: [100, 100], maxZoom: 16 })
    }
  }
  mapInstance.invalidateSize()
  flashActiveRouteHighlight(lineIndex)
}

const getMapWrapElement = () => mapWrapRef.value

const refreshMapSizeAfterLayout = () => {
  requestAnimationFrame(() => {
    mapInstance?.invalidateSize()
  })
}

const onDocumentFullscreenChange = () => {
  const wrap = getMapWrapElement()
  const inNative = Boolean(wrap && document.fullscreenElement === wrap)
  if (inNative) {
    fullscreenFallback.value = false
    fullscreen.value = true
    refreshMapSizeAfterLayout()
    return
  }
  if (!fullscreenFallback.value && fullscreen.value) {
    fullscreen.value = false
    refreshMapSizeAfterLayout()
  }
}

const onFullscreenEscape = (event: KeyboardEvent) => {
  if (event.key !== 'Escape' || !fullscreen.value) return
  event.preventDefault()
  void exitFullscreen()
}

const exitFullscreen = async () => {
  if (!fullscreen.value) return
  const wrap = getMapWrapElement()
  try {
    if (wrap && document.fullscreenElement === wrap) {
      await document.exitFullscreen()
      return
    }
  } catch {
    // 降级模式
  }
  fullscreen.value = false
  fullscreenFallback.value = false
  await nextTick()
  refreshMapSizeAfterLayout()
}

const toggleFullscreen = async () => {
  if (fullscreen.value) {
    await exitFullscreen()
    return
  }
  const wrap = getMapWrapElement()
  if (!wrap || !mapInstance) return
  fullscreenFallback.value = false
  try {
    if (typeof wrap.requestFullscreen === 'function') {
      await wrap.requestFullscreen()
      fullscreen.value = true
      refreshMapSizeAfterLayout()
      return
    }
  } catch {
    // 浏览器拒绝或策略限制时走 CSS 降级
  }
  fullscreenFallback.value = true
  fullscreen.value = true
  await nextTick()
  refreshMapSizeAfterLayout()
}

watch(fullscreen, (val) => {
  if (val) {
    window.addEventListener('keydown', onFullscreenEscape)
  } else {
    window.removeEventListener('keydown', onFullscreenEscape)
  }
})

watch(
  () => props.endpointLabels?.[0],
  (next) => {
    if (!props.startAsHazardPoint) return
    const label = (next || '').trim() || defaultStartName.value
    if (startLabel.value === label) return
    syncingStartLabelFromProp = true
    startLabel.value = label
    if (mapReady.value) applyEndpointLabels()
    syncingStartLabelFromProp = false
  }
)

watch(startLabel, (val) => {
  if (!props.startAsHazardPoint || syncingStartLabelFromProp || hasMetaFieldsSlot.value) return
  emit('hazard-label-change', (val || '').trim())
})

watch(
  () => props.modelValue,
  async (val) => {
    if (!props.active) return
    if (suppressModelValueReload > 0) return
    // 编辑器内部 emit 回写的 modelValue 与缓存一致时，勿重载地图（否则会重置 activeLineIndex 到路线1）
    if (isModelValueSyncedWithCache(val || '')) return
    const keepActiveLine = activeLineIndex.value
    await ensureMap()
    loadGeometry(val || '', keepActiveLine)
    await scheduleResize()
  }
)

watch(metaPanelLayoutVisible, async (visible) => {
  if (!visible || fullscreen.value || isDrawing.value) return
  await nextTick()
  mapInstance?.invalidateSize()
})

onMounted(async () => {
  document.addEventListener('fullscreenchange', onDocumentFullscreenChange)
  if (!props.active) return
  await ensureMap()
  loadGeometry(props.modelValue || '')
  await scheduleResize()
})

watch(
  () => props.active,
  async (val) => {
    if (!val) {
      hideWarehousePicker()
      return
    }
    await ensureMap()
    loadGeometry(props.modelValue || '')
    await scheduleResize()
  }
)

onBeforeUnmount(() => {
  if (routeHighlightFlashTimer) {
    clearTimeout(routeHighlightFlashTimer)
    routeHighlightFlashTimer = null
  }
  clearActiveRouteHighlight()
  document.removeEventListener('fullscreenchange', onDocumentFullscreenChange)
  window.removeEventListener('keydown', onFullscreenEscape)
  const wrap = getMapWrapElement()
  if (wrap && document.fullscreenElement === wrap) {
    void document.exitFullscreen().catch(() => undefined)
  }
  fullscreen.value = false
  fullscreenFallback.value = false
  cancelActiveDraw()
  hideWarehousePicker()
  detachAllLineVertexEditors()
  if (mapInstance && mapCollapseExpandedNamesHandler) {
    mapInstance.off('click', mapCollapseExpandedNamesHandler)
    mapCollapseExpandedNamesHandler = null
  }
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
  mapReady.value = false
})

defineExpose({
  /**
   * 导出当前地图几何（提交前优先使用，避免 v-model 滞后）
   * - 增强折线：走路线缓存
   * - 面/点等：从可编辑图层实时导出（编辑中未点「退出编辑」也能拿到最新节点）
   */
  getCommittedGeometryGeoJson: () => {
    if (linePayloadsCache.value.length > 0) {
      return getModelValueFromCache()
    }
    const fromLayers = exportEditableGroupGeoJson()
    if (fromLayers) return fromLayers
    return buildHazardPointGeoJson()
  },
  enableEdit,
  disableEdit
})
</script>

<style scoped>
.tianditu-editor {
  width: 100%;
}

.tianditu-editor__toolbar {
  margin-bottom: 8px;
}

.tianditu-editor__stage {
  width: 100%;
}

.tianditu-editor__stage.has-meta-panel .tianditu-editor__wrap {
  border: 1px solid var(--el-border-color-lighter);
  border-bottom: none;
  border-radius: 6px 6px 0 0;
}

.tianditu-editor__wrap {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  overflow: hidden;
}

.tianditu-editor__map {
  flex: 1;
  width: 100%;
  min-height: 180px;
  border-radius: 6px;
  overflow: hidden;
}

.tianditu-editor__stage.has-meta-panel .tianditu-editor__map {
  border-radius: 6px 6px 0 0;
}

.tianditu-editor__legend--inline {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 999;
  max-width: calc(100% - 16px);
  pointer-events: none;
}

.tianditu-editor__legend--inline.tianditu-editor__legend--float-bottom {
  top: auto;
  bottom: 12px;
  left: 12px;
  max-width: calc(100% - 24px);
}

.tianditu-editor__meta-legend {
  margin-bottom: 12px;
}

.tianditu-editor__meta-panel {
  flex-shrink: 0;
  padding: 12px 14px 14px;
  background: var(--el-fill-color-blank);
  border: 1px solid var(--el-border-color-lighter);
  border-top: none;
  border-radius: 0 0 8px 8px;
}

.tianditu-editor__stage.has-side-meta {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  gap: 0;
}

.tianditu-editor__stage.has-side-meta .tianditu-editor__wrap {
  flex: 1;
  min-width: 0;
  border: 1px solid var(--el-border-color-lighter);
  border-right: none;
  border-radius: 8px 0 0 8px;
}

.tianditu-editor__stage.has-side-meta .tianditu-editor__map {
  border-radius: 8px 0 0 8px;
}

.tianditu-editor__stage.has-side-meta .tianditu-editor__meta-panel {
  width: 300px;
  max-width: 40%;
  min-width: 260px;
  margin: 0;
  border-radius: 0 8px 8px 0;
  border-top: 1px solid var(--el-border-color-lighter);
  border-left: none;
  overflow-x: hidden;
  overflow-y: auto;
}

.tianditu-editor__meta-block--task-fields {
  padding: 10px 10px 12px;
  background: var(--el-fill-color-blank);
  border-color: var(--el-border-color-lighter);
}

.tianditu-editor__task-fields :deep(.el-form-item) {
  margin-bottom: 10px;
}

.tianditu-editor__task-fields :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.tianditu-editor__task-fields :deep(.el-form-item__label) {
  height: auto;
  padding-bottom: 4px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.35;
  color: var(--el-text-color-regular);
}

.tianditu-editor__task-fields :deep(.el-form-item__content) {
  line-height: 1.4;
}

.tianditu-editor__task-fields :deep(.el-input__wrapper),
.tianditu-editor__task-fields :deep(.el-textarea__inner) {
  font-size: 13px;
}

.tianditu-editor__endpoint-grid.is-warehouse-only {
  grid-template-columns: 1fr;
}

@media (max-width: 768px) {
  .tianditu-editor__stage.has-side-meta {
    flex-direction: column;
  }

  .tianditu-editor__stage.has-side-meta .tianditu-editor__wrap {
    border-right: 1px solid var(--el-border-color-lighter);
    border-bottom: none;
    border-radius: 8px 8px 0 0;
  }

  .tianditu-editor__stage.has-side-meta .tianditu-editor__map {
    border-radius: 8px 8px 0 0;
  }

  .tianditu-editor__stage.has-side-meta .tianditu-editor__meta-panel {
    width: 100%;
    max-width: none;
    min-width: 0;
    max-height: 42vh;
    border-left: 1px solid var(--el-border-color-lighter);
    border-top: none;
    border-radius: 0 0 8px 8px;
  }
}

.tianditu-editor__meta-block {
  margin-bottom: 14px;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-extra-light);
}

.tianditu-editor__meta-block:last-child {
  margin-bottom: 0;
}

.tianditu-editor__meta-block--segments {
  padding-bottom: 12px;
}

.tianditu-editor__meta-block--route {
  padding: 14px 14px 12px;
  background: linear-gradient(135deg, rgb(22 119 255 / 10%) 0%, rgb(22 119 255 / 4%) 100%);
  border: 1.5px solid rgb(22 119 255 / 35%);
  box-shadow: 0 2px 8px rgb(22 119 255 / 10%);
}

.tianditu-editor__route-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.tianditu-editor__route-head-main {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  min-width: 0;
}

.tianditu-editor__route-head-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  color: var(--el-color-primary);
  background: rgb(22 119 255 / 14%);
}

.tianditu-editor__route-head-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--el-color-primary);
  line-height: 1.3;
}

.tianditu-editor__route-head-tip {
  margin-top: 2px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

.tianditu-editor__route-head-badge {
  flex-shrink: 0;
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-color-primary);
  background: rgb(22 119 255 / 12%);
  border: 1px solid rgb(22 119 255 / 22%);
  border-radius: 999px;
}

.tianditu-editor__route-current {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tianditu-editor__route-current-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tianditu-editor__route-current-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-regular);
}

.tianditu-editor__meta-block.is-under-route {
  margin-left: 10px;
  padding-left: 14px;
  border-left: 2px dashed rgb(22 119 255 / 28%);
  background: var(--el-fill-color-blank);
}

.tianditu-editor__meta-block-head {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 6px 10px;
  margin-bottom: 10px;
}

.tianditu-editor__meta-block-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.tianditu-editor__meta-block-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.tianditu-editor__route-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 10px 14px;
}

.tianditu-editor__route-select {
  width: 168px;
}

.tianditu-editor__route-select :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px rgb(22 119 255 / 35%) inset;
}

.tianditu-editor__endpoint-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.tianditu-editor__endpoint-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 6px;
  background: var(--el-fill-color-blank);
  border: 1px solid var(--el-border-color-lighter);
}

.tianditu-editor__endpoint-field.is-start {
  border-color: rgb(82 196 26 / 28%);
}

.tianditu-editor__endpoint-field.is-hazard {
  border-color: rgb(250 84 28 / 32%);
}

.tianditu-editor__endpoint-field.is-end {
  border-color: rgb(22 119 255 / 28%);
}

.tianditu-editor__field-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
  line-height: 1.2;
}

.tianditu-editor__field-tag {
  padding: 0 5px;
  font-size: 10px;
  font-weight: 500;
  color: var(--el-color-primary);
  background: rgb(22 119 255 / 10%);
  border: 1px solid rgb(22 119 255 / 22%);
  border-radius: 3px;
  line-height: 1.5;
}

.tianditu-editor__field-tag.is-system {
  color: var(--el-color-warning);
  background: rgb(230 162 60 / 12%);
  border-color: rgb(230 162 60 / 35%);
}

.tianditu-editor__endpoint-field.is-system-warehouse {
  cursor: not-allowed;
}

.tianditu-editor__endpoint-field.is-system-warehouse :deep(.el-input.is-disabled .el-input__wrapper) {
  cursor: not-allowed;
}

.tianditu-editor__meta-block-tip {
  margin: 0 0 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.45;
}

.tianditu-editor__meta-empty {
  padding: 16px;
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  background: var(--el-fill-color-blank);
}

.tianditu-editor__segment-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  align-items: start;
  gap: 10px;
}

.tianditu-editor__segment-item {
  display: flex;
  flex-direction: column;
  gap: 0;
  align-self: start;
  height: auto;
  min-width: 0;
  padding: 0;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
  overflow: hidden;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;
  cursor: default;
}

.tianditu-editor__segment-head {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  padding: 8px 8px 8px 6px;
  background: var(--el-fill-color-light);
  border-bottom: 1px solid transparent;
}

.tianditu-editor__segment-item.is-expanded .tianditu-editor__segment-head {
  border-bottom-color: var(--el-border-color-extra-light);
}

.tianditu-editor__segment-index {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  border: 1px solid var(--el-border-color-lighter);
}

.tianditu-editor__segment-check {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  height: auto !important;
}

.tianditu-editor__segment-check :deep(.el-checkbox__label) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  font-weight: 500;
}

.tianditu-editor__segment-delete {
  flex-shrink: 0;
  padding: 4px 8px !important;
  min-height: 24px;
}

.tianditu-editor__segment-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px;
}

.tianditu-editor__segment-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tianditu-editor__segment-name,
.tianditu-editor__segment-desc {
  width: 100%;
}

.tianditu-editor__segment-desc :deep(.el-textarea__inner) {
  min-height: 52px !important;
  font-size: 12px;
}

.tianditu-editor__segment-item.is-active {
  border-color: #1890ff;
  box-shadow: 0 0 0 1px rgb(24 144 255 / 18%);
}

.tianditu-editor__segment-item.is-risk .tianditu-editor__segment-head {
  background: linear-gradient(180deg, rgb(250 173 20 / 10%) 0%, var(--el-fill-color-light) 100%);
}

.tianditu-editor__segment-item.is-risk .tianditu-editor__segment-index {
  color: #d48806;
  border-color: rgb(250 173 20 / 35%);
  background: rgb(250 173 20 / 12%);
}

.tianditu-editor__segment-item.is-risk :deep(.el-checkbox__label) {
  color: #d48806;
}

.tianditu-editor__meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
  margin-bottom: 10px;
}

.tianditu-editor__meta-field {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 200px;
}

.tianditu-editor__meta-label {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.tianditu-editor__meta-field--route {
  flex: 1 1 100%;
  min-width: 280px;
}

.tianditu-editor__meta-section {
  margin-top: 4px;
}

.tianditu-editor__meta-title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.tianditu-editor__mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 6px;
}

/* 浏览器原生全屏：仅地图容器进入全屏，避免 dialog transform 导致 fixed 错位与卡顿 */
.tianditu-editor__wrap:fullscreen {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #fff;
}

.tianditu-editor__wrap:fullscreen .tianditu-editor__map {
  flex: 1;
  min-height: 0;
  height: auto !important;
  border-radius: 0;
}

.tianditu-editor__wrap:fullscreen .tianditu-editor__compact-toolbar {
  backdrop-filter: none;
  background: rgb(255 255 255 / 98%);
}

/* 不支持 requestFullscreen 时降级：整段 stage 盖住视口 */
.tianditu-editor__stage.is-fullscreen-fallback {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  background: #fff;
  padding: 0;
}

.tianditu-editor__stage.is-fullscreen-fallback .tianditu-editor__wrap {
  flex: 1;
  min-height: 0;
}

.tianditu-editor__stage.is-fullscreen-fallback .tianditu-editor__meta-panel {
  display: none;
}

.tianditu-editor__map-toolbar {
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  z-index: 1000;
  display: flex;
  justify-content: flex-end;
  pointer-events: none;
}

.tianditu-editor__compact-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  max-width: 100%;
  padding: 6px 8px;
  background: rgb(255 255 255 / 96%);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  box-shadow: 0 2px 10px rgb(0 0 0 / 8%);
  pointer-events: auto;
}

.tianditu-editor__compact-toolbar-group :deep(.el-button) {
  margin: 0;
}

.tianditu-editor__fullscreen-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10001;
  pointer-events: auto;
}

.tianditu-editor__file-input {
  display: none;
}

.tianditu-editor__hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}
</style>

<style>
path.tianditu-line-segment--clickable {
  cursor: pointer;
}

.tianditu-flow-arrow-marker {
  background: transparent;
  border: none;
}

.tianditu-flow-arrow-marker__svg {
  display: block;
  filter: drop-shadow(0 1px 2px rgb(0 0 0 / 35%));
  pointer-events: none;
}

.tianditu-vertex-handle {
  background: transparent;
  border: none;
}

.tianditu-vertex-handle__dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #1677ff;
  box-shadow: 0 0 0 2px rgb(22 119 255 / 20%);
}

.tianditu-vertex-handle.is-selected .tianditu-vertex-handle__dot {
  border-color: var(--route-line-color, #1677ff);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--route-line-color, #1677ff) 30%, transparent);
}

.tianditu-vertex-handle.is-selected .tianditu-vertex-handle__arrow-svg {
  filter: drop-shadow(0 0 4px color-mix(in srgb, var(--route-line-color, #1677ff) 65%, transparent));
}

.tianditu-vertex-handle__arrow-svg {
  display: block;
  filter: drop-shadow(0 1px 2px rgb(0 0 0 / 25%));
}

.tianditu-vertex-handle__mid {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #fff;
  border: 1px dashed #1677ff;
  color: #1677ff;
  font-size: 10px;
  line-height: 10px;
  text-align: center;
  cursor: pointer;
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
  transition: max-width 0.15s ease, box-shadow 0.15s ease;
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

.tianditu-vertex-handle.is-selected .tianditu-vertex-handle__location-pin,
.tianditu-vertex-handle.is-selected .tianditu-vertex-handle__location-img {
  filter: drop-shadow(0 2px 5px rgb(0 0 0 / 32%));
}

.tianditu-vertex-handle.is-selected .tianditu-vertex-handle__location-label.is-start {
  background: rgba(82, 196, 26, 0.92);
  opacity: 1;
}

.tianditu-vertex-handle.is-selected .tianditu-vertex-handle__location-label.is-hazard {
  background: rgba(250, 84, 28, 0.92);
  opacity: 1;
}

.tianditu-vertex-handle.is-selected .tianditu-vertex-handle__location-label.is-end {
  background: rgba(22, 119, 255, 0.92);
  opacity: 1;
}

.tianditu-vertex-handle.is-selected .tianditu-vertex-handle__location-label:not(.is-start):not(.is-hazard):not(.is-end) {
  background: var(--route-line-color, #1677ff);
  opacity: 1;
}

.tianditu-vertex-handle--warehouse-proxy {
  background: transparent !important;
  border: none !important;
  pointer-events: none !important;
}

.tianditu-vertex-handle__warehouse-proxy {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: transparent;
  pointer-events: none;
}

.tianditu-vertex-handle.is-selected .tianditu-vertex-handle__warehouse-proxy {
  background: rgb(22 119 255 / 18%);
  border: 2px dashed rgb(22 119 255 / 55%);
}
</style>

<style>
.leaflet-interactive.tianditu-line-segment--clickable {
  cursor: pointer;
}

@keyframes tianditu-route-flash-pulse {
  0%,
  100% {
    stroke-opacity: 1;
    filter: drop-shadow(0 0 5px rgb(255 193 7 / 90%)) drop-shadow(0 0 12px rgb(22 119 255 / 85%));
  }

  50% {
    stroke-opacity: 0.5;
    filter: drop-shadow(0 0 14px rgb(255 193 7 / 100%)) drop-shadow(0 0 22px rgb(22 119 255 / 95%));
  }
}

path.tianditu-line-segment--active-route.is-route-flash,
.leaflet-interactive.tianditu-line-segment--active-route.is-route-flash {
  animation: tianditu-route-flash-pulse 0.72s ease-in-out 3;
}

.tianditu-flow-arrow-marker.is-route-active.is-route-flash {
  filter: drop-shadow(0 0 6px rgb(255 193 7 / 75%)) drop-shadow(0 0 8px rgb(22 119 255 / 65%));
  transform: scale(1.12);
}

.tianditu-warehouse-picker-handle {
  background: transparent !important;
  border: none !important;
}

.tianditu-warehouse-picker-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  pointer-events: auto;
  --warehouse-route-color: #1677ff;
}

.tianditu-warehouse-picker-marker.is-linkable {
  cursor: pointer;
}

.tianditu-warehouse-picker-marker.is-idle,
.tianditu-warehouse-picker-marker.is-linked {
  cursor: default;
}

.tianditu-warehouse-picker-marker.is-linked {
  opacity: 1;
}

.tianditu-warehouse-picker-marker.is-name-expanded .tianditu-warehouse-picker-marker__label {
  box-shadow: none;
  opacity: 1;
  border: none;
}

.tianditu-warehouse-picker-marker.is-name-expanded .tianditu-warehouse-picker-marker__img {
  transform: scale(1.06);
  filter: drop-shadow(0 2px 4px rgb(0 0 0 / 28%));
}

.tianditu-warehouse-picker-marker.is-name-expanded .tianditu-warehouse-picker-marker__color-bar {
  height: 6px;
  box-shadow: none;
}

.tianditu-warehouse-picker-marker__color-bar {
  width: 30px;
  height: 5px;
  margin-top: 2px;
  border-radius: 2px;
  background: var(--warehouse-route-color, #1677ff);
  box-shadow: none;
  pointer-events: none;
  transition: height 0.2s ease;
}

.tianditu-warehouse-picker-marker__label {
  margin-top: 3px;
  max-width: 96px;
  color: #fff;
  background: var(--warehouse-route-color, #1677ff);
  border: none;
  border-radius: 3px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.45;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  box-shadow: none;
  opacity: 1;
  pointer-events: none;
  transition: max-width 0.15s ease;
}

.tianditu-warehouse-picker-marker__label.is-expanded {
  max-width: min(260px, 72vw);
  white-space: normal;
  word-break: break-all;
  overflow: visible;
  text-overflow: unset;
  box-shadow: none;
}

.leaflet-marker-icon.tianditu-warehouse-picker-handle.is-warehouse-name-expanded,
.leaflet-marker-icon.tianditu-warehouse-picker-handle:has(.is-name-expanded) {
  overflow: visible !important;
}

.tianditu-warehouse-picker-marker__img {
  display: block;
  transition: transform 0.22s ease, filter 0.22s ease;
}

.tianditu-warehouse-picker-marker.is-linkable:hover .tianditu-warehouse-picker-marker__img {
  transform: scale(1.2);
  filter: drop-shadow(0 2px 5px rgb(0 0 0 / 30%));
}

.tianditu-warehouse-picker-marker.is-linkable:hover .tianditu-warehouse-picker-marker__color-bar {
  height: 6px;
  box-shadow: none;
}
</style>
