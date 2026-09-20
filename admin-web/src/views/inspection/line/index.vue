<template>
  <div class="inspection-line-page">
    <ContentWrap>
      <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="所属站点" prop="stationId">
          <el-select v-model="queryParams.stationId" clearable filterable placeholder="请选择所属站点" class="!w-180px">
            <el-option
              v-for="option in stationOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="巡检类型" prop="inspectionType">
          <el-select
            v-model="queryParams.inspectionType"
            clearable
            filterable
            placeholder="请选择巡检类型"
            class="!w-160px"
          >
            <el-option
              v-for="option in inspectionTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="适用区域" prop="areaType">
          <el-select v-model="queryParams.areaType" clearable placeholder="请选择适用区域" class="!w-160px">
            <el-option
              v-for="option in areaTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="线路名称" prop="lineName">
          <el-input
            v-model="queryParams.lineName"
            clearable
            placeholder="请输入线路名称"
            class="!w-180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="点位数" class="query-range-item">
          <el-input-number v-model="queryParams.minPointCount" :min="0" :step="1" controls-position="right" class="!w-110px" />
          <span class="query-range-sep">-</span>
          <el-input-number v-model="queryParams.maxPointCount" :min="0" :step="1" controls-position="right" class="!w-110px" />
        </el-form-item>
        <el-form-item label="使用次数" class="query-range-item">
          <el-input-number v-model="queryParams.minUseCount" :min="0" :step="1" controls-position="right" class="!w-110px" />
          <span class="query-range-sep">-</span>
          <el-input-number v-model="queryParams.maxUseCount" :min="0" :step="1" controls-position="right" class="!w-110px" />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
          <el-button
            v-hasPermi="['iot:inspection-line:create']"
            type="primary"
            plain
            @click="openForm('create')"
          >
            <Icon icon="ep:plus" class="mr-5px" />
            新增线路
          </el-button>
        </el-form-item>
      </el-form>
      <p class="query-tip">筛选支持巡检类型、点位总数、使用次数和线路名称组合查询。</p>
    </ContentWrap>

    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="线路名称" prop="lineName" min-width="180" />
        <el-table-column label="适用类型" prop="inspectionType" min-width="120">
          <template #default="scope">
            <el-tag effect="light" type="primary">{{ getInspectionTypeLabel(scope.row.inspectionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="适用区域" prop="areaType" min-width="120">
          <template #default="scope">
            {{ getAreaTypeLabel(scope.row.areaType) }}
          </template>
        </el-table-column>
        <el-table-column label="点位总数" prop="pointCount" width="90" align="center" />
        <el-table-column label="总距离" prop="totalLengthMeter" width="120">
          <template #default="scope">
            {{ formatLength(scope.row.totalLengthMeter) }}
          </template>
        </el-table-column>
        <el-table-column label="使用次数" prop="useCount" width="90" align="center" />
        <el-table-column label="状态" prop="status" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'info'" effect="plain">
              {{ scope.row.status === 0 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170">
          <template #default="scope">{{ formatCreateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="scope">
            <el-button type="info" link @click="openForm('detail', scope.row.id)">详情</el-button>
            <el-button v-hasPermi="['iot:inspection-line:update']" type="primary" link @click="openForm('update', scope.row.id)">
              编辑
            </el-button>
            <el-button v-hasPermi="['iot:inspection-line:delete']" type="danger" link @click="handleDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <Dialog v-model="formVisible" :title="formTitle" width="1260px" class="inspection-line-dialog" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" :disabled="formReadonly" label-width="108px" v-loading="formLoading">
        <section class="form-panel">
          <h3 class="form-panel__title">线路基础信息</h3>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="所属站点" prop="stationId">
                <el-select
                  v-model="formData.stationId"
                  filterable
                  clearable
                  placeholder="请选择所属站点"
                  @change="handleFormStationChange"
                >
                  <el-option
                    v-for="option in stationOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="线路名称" prop="lineName">
                <el-input v-model="formData.lineName" maxlength="100" show-word-limit placeholder="请输入线路名称" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="巡检类型" prop="inspectionType">
                <el-select v-model="formData.inspectionType" clearable placeholder="请选择巡检类型">
                  <el-option
                    v-for="option in inspectionTypeOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="适用区域" prop="areaType">
                <el-select v-model="formData.areaType" clearable placeholder="请选择适用区域">
                  <el-option
                    v-for="option in areaTypeOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="起点坐标" required>
                <div class="coord-text">{{ formatCoord(formData.startLongitude, formData.startLatitude) }}</div>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="终点坐标" required>
                <div class="coord-text">{{ formatCoord(formData.endLongitude, formData.endLatitude) }}</div>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="总长度">
                <div class="coord-text">{{ formatLength(formData.totalLengthMeter) }}</div>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="点位总数">
                <div class="coord-text">{{ formData.points.length }}</div>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="状态" prop="status">
                <el-switch v-model="formData.status" :active-value="0" :inactive-value="1" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="线路描述" prop="lineDesc">
                <el-input
                  v-model="formData.lineDesc"
                  type="textarea"
                  :rows="2"
                  maxlength="1000"
                  show-word-limit
                  placeholder="请输入线路描述"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-panel">
          <div class="item-header">
            <h3 class="form-panel__title">检查点配置</h3>
            <div class="item-header__actions">
              <el-input
                v-model="pointKeyword"
                clearable
                :disabled="formReadonly"
                placeholder="搜索设备点位"
                class="point-search"
                @keyup.enter="loadPointOptions"
              >
                <template #append>
                  <el-button :disabled="formReadonly" @click="loadPointOptions">查询</el-button>
                </template>
              </el-input>
              <el-button type="primary" plain :disabled="formReadonly" @click="loadPointOptions">刷新点位</el-button>
            </div>
          </div>

          <div class="point-board">
            <div class="point-tree-panel">
              <div class="panel-subtitle">选择点位（已选：{{ selectedDeviceCount }}）</div>
              <div v-if="!formData.stationId" class="empty-holder">请先选择所属站点</div>
              <div v-else-if="pointOptionsLoading" class="empty-holder">点位加载中...</div>
              <div v-else-if="!deviceTreeData.length" class="empty-holder">暂无可选设备点位</div>
              <el-tree
                v-else
                ref="treeRef"
                :data="deviceTreeData"
                node-key="id"
                :show-checkbox="!formReadonly"
                check-strictly
                default-expand-all
                :props="{ label: 'label', children: 'children' }"
                @check-change="handleTreeCheckChange"
              >
                <template #default="{ data }">
                  <div class="point-tree-node">
                    <span>{{ data.label }}</span>
                    <span v-if="data.nodeType === 'device'" class="point-tree-node__tip">
                      {{ data.pointOption?.longitude && data.pointOption?.latitude ? '有坐标' : '坐标缺失' }}
                    </span>
                  </div>
                </template>
              </el-tree>
            </div>

            <div class="point-map-panel">
              <div class="panel-subtitle">
                {{ formReadonly ? '线路预览（详情只读）' : '线路预览（点击地图设备可同步勾选到左侧树）' }}
              </div>
              <div class="line-map-wrap">
                <div ref="lineMapRef" class="line-map"></div>
                <div class="line-map-toolbar">
                  <div class="line-map-toolbar__title">地图控制</div>
                  <div class="line-map-toolbar__actions">
                    <el-button size="small" :disabled="formReadonly" @click="handleMapZoomIn">放大</el-button>
                    <el-button size="small" :disabled="formReadonly" @click="handleMapZoomOut">缩小</el-button>
                    <el-button
                      size="small"
                      :type="manualPointPicking ? 'warning' : 'primary'"
                      :disabled="formReadonly"
                      plain
                      @click="activateManualPointMode"
                    >
                      添加点位
                    </el-button>
                    <el-button size="small" plain :disabled="formReadonly" @click="clearAllPoints">清除路线</el-button>
                  </div>
                  <div v-if="manualPointPicking" class="line-map-toolbar__tip">请在地图上点击位置，弹框编辑后保存点位</div>
                </div>
                <div v-if="mapLoading" class="line-map-mask">地图加载中...</div>
              </div>

              <div class="point-selected-panel">
                <div class="panel-subtitle">已选点位（支持排序/删除）</div>
                <div v-if="!formData.points.length" class="empty-holder">请至少选择一个点位</div>
                <div v-else class="selected-point-list">
                  <div v-for="(point, index) in formData.points" :key="point.__uuid" class="selected-point-item">
                    <div class="selected-point-item__index">{{ index + 1 }}</div>
                    <div class="selected-point-item__main">
                      <div class="selected-point-item__name">
                        {{ point.pointName || `点位${index + 1}` }}
                        <el-tag size="small" :type="point.pointType === 3 ? 'warning' : 'primary'" effect="plain">
                          {{ point.pointType === 3 ? '手动点位' : '设备点位' }}
                        </el-tag>
                      </div>
                      <div class="selected-point-item__meta">
                        {{ formatCoord(point.longitude, point.latitude) }}
                      </div>
                    </div>
                    <div class="selected-point-item__actions">
                      <el-button type="primary" link :disabled="formReadonly || index === 0" @click="movePoint(index, -1)">上移</el-button>
                      <el-button
                        type="primary"
                        link
                        :disabled="formReadonly || index === formData.points.length - 1"
                        @click="movePoint(index, 1)"
                      >
                        下移
                      </el-button>
                      <el-button type="danger" link :disabled="formReadonly" @click="removePoint(index)">删除</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>


          <p class="panel-foot-tip">
            提示：系统会根据点位顺序自动计算起终点坐标与线路总长度；若设备无坐标，请补充手动点位。
          </p>
        </section>
      </el-form>

      <template #footer>
        <el-button @click="formVisible = false">{{ formReadonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!formReadonly" type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { getTiandituKey } from '@/components/Gis/tiandituKey'
import {
  InspectionLineApi,
  type InspectionLinePageReqVO,
  type InspectionLinePointOptionVO,
  type InspectionLinePointVO,
  type InspectionLineVO
} from '@/api/iot/inspection/line'
import type { FormInstance, FormRules } from 'element-plus'

interface SelectOption {
  label: string
  value: string | number
}

type FormMode = 'create' | 'update' | 'detail'

interface DeviceTreeNode {
  id: string
  label: string
  nodeType: 'group' | 'device'
  pointOption?: InspectionLinePointOptionVO
  children?: DeviceTreeNode[]
}

interface InspectionLinePointEditVO extends InspectionLinePointVO {
  __uuid: string
}

interface InspectionLineEditVO extends Omit<InspectionLineVO, 'points'> {
  points: InspectionLinePointEditVO[]
}

const DEFAULT_MAP_CENTER: [number, number] = [32.272, 119.184]
const DEFAULT_MAP_ZOOM = 13
const MAP_FIT_MAX_ZOOM = 18
let leafletCorePromise: Promise<void> | null = null

const message = useMessage()

const stationOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const inspectionTypeOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_INSPECTION_TYPE)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const areaTypeOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_INSPECTION_LINE_AREA)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const getInspectionTypeLabel = (value?: string) => {
  if (!value) return '-'
  return inspectionTypeOptions.value.find((item) => item.value === value)?.label || value
}

const getAreaTypeLabel = (value?: string) => {
  if (!value) return '-'
  return areaTypeOptions.value.find((item) => item.value === value)?.label || value
}

const formatCreateTime = (value?: string | number) => (value ? formatDate(value) : '-')

const formatLength = (lengthMeter?: number) => {
  const value = Number(lengthMeter || 0)
  if (!value) return '-'
  if (value >= 1000) {
    return `${(value / 1000).toFixed(2)} km`
  }
  return `${value.toFixed(2)} m`
}

const formatCoord = (longitude?: number, latitude?: number) => {
  if (longitude === undefined || longitude === null || latitude === undefined || latitude === null) {
    return '-'
  }
  return `${Number(longitude).toFixed(6)}, ${Number(latitude).toFixed(6)}`
}

const loading = ref(false)
const total = ref(0)
const list = ref<InspectionLineVO[]>([])
const queryFormRef = ref<FormInstance>()
const queryParams = reactive<InspectionLinePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  stationId: undefined,
  lineName: undefined,
  inspectionType: undefined,
  areaType: undefined,
  minPointCount: undefined,
  maxPointCount: undefined,
  minUseCount: undefined,
  maxUseCount: undefined,
  status: undefined,
  createTime: undefined
})

const getList = async () => {
  loading.value = true
  try {
    const data = await InspectionLineApi.getInspectionLinePage(queryParams)
    list.value = data.list || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  await getList()
}

const handleDelete = async (id?: number) => {
  if (!id) return
  try {
    await message.delConfirm('确认删除该巡检线路吗？')
    await InspectionLineApi.deleteInspectionLine(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

const formVisible = ref(false)
const formLoading = ref(false)
const submitLoading = ref(false)
const formMode = ref<FormMode>('create')
const formRef = ref<FormInstance>()

const treeRef = ref<any>()
const pointOptionsLoading = ref(false)
const pointOptions = ref<InspectionLinePointOptionVO[]>([])
const pointKeyword = ref('')
const syncingTreeChecked = ref(false)

const lineMapRef = ref<HTMLDivElement>()
const mapLoading = ref(false)
const mapReady = ref(false)
const manualPointPicking = ref(false)

const manualPointDraft = reactive({
  pointName: '',
  longitude: '',
  latitude: '',
  remark: ''
})

let Leaflet: any = null
let lineMapInstance: any = null
let lineDeviceLayer: any = null
let lineRouteLayer: any = null
let lineOrderLayer: any = null
let manualPointPopup: any = null

const createUuid = () => `${Date.now()}_${Math.random().toString(16).slice(2)}`

const createEmptyFormData = (): InspectionLineEditVO => ({
  id: undefined,
  stationId: '',
  lineName: '',
  inspectionType: '',
  areaType: '',
  lineDesc: '',
  startLongitude: undefined as unknown as number,
  startLatitude: undefined as unknown as number,
  endLongitude: undefined as unknown as number,
  endLatitude: undefined as unknown as number,
  totalLengthMeter: 0,
  pointCount: 0,
  useCount: 0,
  status: 0,
  remark: '',
  points: []
})

const formData = reactive<InspectionLineEditVO>(createEmptyFormData())
const selectedDeviceCount = computed(() => formData.points.filter((item) => item.pointType === 1).length)
const formReadonly = computed(() => formMode.value === 'detail')

const formRules: FormRules = {
  stationId: [{ required: true, message: '所属站点不能为空', trigger: 'change' }],
  lineName: [{ required: true, message: '线路名称不能为空', trigger: 'blur' }],
  inspectionType: [{ required: true, message: '巡检类型不能为空', trigger: 'change' }],
  areaType: [{ required: true, message: '适用区域不能为空', trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
}

const formTitle = computed(() => {
  if (formMode.value === 'create') return '新建巡检线路'
  if (formMode.value === 'detail') return '巡检线路详情'
  return '编辑巡检线路'
})

const resetForm = () => {
  Object.assign(formData, createEmptyFormData())
  pointOptions.value = []
  pointKeyword.value = ''
  closeManualPointPopup(true)
  formRef.value?.clearValidate()
}

const resetManualPointDraft = () => {
  manualPointDraft.pointName = ''
  manualPointDraft.longitude = ''
  manualPointDraft.latitude = ''
  manualPointDraft.remark = ''
}

const closeManualPointPopup = (resetDraft = false) => {
  if (lineMapInstance && manualPointPopup) {
    lineMapInstance.closePopup(manualPointPopup)
  }
  manualPointPopup = null
  manualPointPicking.value = false
  if (resetDraft) {
    resetManualPointDraft()
  }
}

const cancelManualPointInput = () => {
  closeManualPointPopup(true)
}

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
  await loadStyleOnce('leaflet-style', 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css')
  if ((window as any).L) return
  if (!leafletCorePromise) {
    leafletCorePromise = (async () => {
      await loadScriptOnce('leaflet-script', 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js')
      if (!(window as any).L) {
        throw new Error('Leaflet 脚本加载完成但 window.L 不存在')
      }
    })()
  }
  await leafletCorePromise
}

const destroyLineMap = () => {
  if (lineMapInstance) {
    lineMapInstance.off()
    lineMapInstance.remove()
  }
  lineMapInstance = null
  lineDeviceLayer = null
  lineRouteLayer = null
  lineOrderLayer = null
  manualPointPopup = null
  Leaflet = null
  mapReady.value = false
  mapLoading.value = false
  manualPointPicking.value = false
  resetManualPointDraft()
}

const toLatLng = (longitude?: number, latitude?: number): [number, number] | null => {
  const lng = Number(longitude)
  const lat = Number(latitude)
  if (!Number.isFinite(lng) || !Number.isFinite(lat)) return null
  return [lat, lng]
}

const createOrderIcon = (order: number, pointType: number) => {
  const bgColor = pointType === 3 ? '#84cc16' : '#3b82f6'
  return Leaflet.divIcon({
    className: 'inspection-line-order-icon',
    html: `<div style=\"width:28px;height:28px;border-radius:50%;display:flex;align-items:center;justify-content:center;background:${bgColor};color:#fff;font-size:13px;font-weight:700;border:2px solid #fff;box-shadow:0 2px 8px rgba(15,23,42,.22);\">${order}</div>`,
    iconSize: [28, 28],
    iconAnchor: [14, 14]
  })
}

const renderLineMap = (fitBounds = false) => {
  if (!Leaflet || !lineMapInstance || !lineDeviceLayer || !lineRouteLayer || !lineOrderLayer) return
  lineDeviceLayer.clearLayers()
  lineRouteLayer.clearLayers()
  lineOrderLayer.clearLayers()

  const selectedDeviceIdSet = new Set(
    formData.points
      .filter((item) => item.pointType === 1 && item.deviceId)
      .map((item) => Number(item.deviceId))
  )

  const allLatLngs: [number, number][] = []
  pointOptions.value.forEach((option) => {
    const latLng = toLatLng(option.longitude, option.latitude)
    if (!latLng) return
    allLatLngs.push(latLng)
    const selected = selectedDeviceIdSet.has(Number(option.deviceId))
    const marker = Leaflet.circleMarker(latLng, {
      radius: selected ? 8 : 6,
      color: selected ? '#16a34a' : '#3b82f6',
      weight: selected ? 3 : 2,
      fillColor: selected ? '#22c55e' : '#60a5fa',
      fillOpacity: selected ? 0.95 : 0.85
    }).addTo(lineDeviceLayer)
    marker.bindTooltip(option.pointName || `设备${option.deviceId}`, {
      direction: 'top',
      offset: [0, -8]
    })
    marker.on('click', (event: any) => {
      if (formReadonly.value) return
      if (event?.originalEvent && Leaflet?.DomEvent?.stop) {
        Leaflet.DomEvent.stop(event.originalEvent)
      }
      if (!formData.stationId) {
        message.warning('请先选择所属站点')
        return
      }
      if (selectedDeviceIdSet.has(Number(option.deviceId))) {
        return
      }
      addDevicePoint(option)
      if (treeRef.value) {
        syncingTreeChecked.value = true
        treeRef.value.setChecked(`device_${option.deviceId}`, true, false)
        syncingTreeChecked.value = false
      }
    })
  })

  const routeLatLngs: [number, number][] = []
  formData.points.forEach((point, index) => {
    const latLng = toLatLng(point.longitude, point.latitude)
    if (!latLng) return
    routeLatLngs.push(latLng)
    const marker = Leaflet.marker(latLng, {
      icon: createOrderIcon(index + 1, point.pointType)
    }).addTo(lineOrderLayer)
    marker.bindTooltip(`${index + 1}# ${point.pointName || `点位${index + 1}`}`, {
      direction: 'bottom',
      offset: [0, 14],
      className: 'inspection-line-route-tooltip'
    })
  })

  if (routeLatLngs.length >= 2) {
    Leaflet.polyline(routeLatLngs, {
      color: '#2fb39c',
      weight: 5,
      opacity: 0.9,
      lineJoin: 'round'
    }).addTo(lineRouteLayer)
  }

  if (fitBounds) {
    const targetLatLngs = routeLatLngs.length > 0 ? routeLatLngs : allLatLngs
    if (targetLatLngs.length === 1) {
      lineMapInstance.setView(targetLatLngs[0], MAP_FIT_MAX_ZOOM)
      return
    }
    if (targetLatLngs.length > 1) {
      lineMapInstance.fitBounds(Leaflet.latLngBounds(targetLatLngs), {
        padding: [48, 48],
        maxZoom: MAP_FIT_MAX_ZOOM
      })
    }
  }
}

const saveManualPointFromPopup = () => {
  if (formReadonly.value) return
  const pointName = manualPointDraft.pointName.trim()
  const longitude = parseNumber(manualPointDraft.longitude)
  const latitude = parseNumber(manualPointDraft.latitude)
  if (!pointName) {
    message.error('手动点位名称不能为空')
    return
  }
  if (longitude === undefined || latitude === undefined) {
    message.error('请输入有效经纬度')
    return
  }
  formData.points.push({
    __uuid: createUuid(),
    pointSort: formData.points.length + 1,
    pointType: 3,
    pointName,
    longitude,
    latitude,
    remark: manualPointDraft.remark?.trim() || ''
  })
  normalizePointsAfterChange()
  closeManualPointPopup(true)
  message.success('手动点位已加入线路')
}

const buildManualPointPopupContent = (lat: number, lng: number) => {
  const root = document.createElement('div')
  root.className = 'manual-point-popup'

  const title = document.createElement('div')
  title.className = 'manual-point-popup__title'
  title.textContent = '新增手动点位'

  const coordText = document.createElement('div')
  coordText.className = 'manual-point-popup__coord'
  coordText.textContent = `地图坐标：${lng.toFixed(6)}, ${lat.toFixed(6)}`

  const nameField = document.createElement('label')
  nameField.className = 'manual-point-popup__field'
  const nameLabel = document.createElement('span')
  nameLabel.className = 'manual-point-popup__label'
  nameLabel.textContent = '点位名称'
  const nameInput = document.createElement('input')
  nameInput.className = 'manual-point-popup__input'
  nameInput.type = 'text'
  nameInput.maxLength = 100
  nameInput.placeholder = '请输入手动点位名称'
  nameInput.value = manualPointDraft.pointName
  nameInput.oninput = (event) => {
    manualPointDraft.pointName = (event.target as HTMLInputElement).value
  }
  nameField.append(nameLabel, nameInput)

  const coordFieldWrap = document.createElement('div')
  coordFieldWrap.className = 'manual-point-popup__grid'

  const lngField = document.createElement('label')
  lngField.className = 'manual-point-popup__field'
  const lngLabel = document.createElement('span')
  lngLabel.className = 'manual-point-popup__label'
  lngLabel.textContent = '经度'
  const lngInput = document.createElement('input')
  lngInput.className = 'manual-point-popup__input'
  lngInput.type = 'text'
  lngInput.placeholder = '例如 119.184000'
  lngInput.value = manualPointDraft.longitude
  lngInput.oninput = (event) => {
    manualPointDraft.longitude = (event.target as HTMLInputElement).value
  }
  lngField.append(lngLabel, lngInput)

  const latField = document.createElement('label')
  latField.className = 'manual-point-popup__field'
  const latLabel = document.createElement('span')
  latLabel.className = 'manual-point-popup__label'
  latLabel.textContent = '纬度'
  const latInput = document.createElement('input')
  latInput.className = 'manual-point-popup__input'
  latInput.type = 'text'
  latInput.placeholder = '例如 32.272000'
  latInput.value = manualPointDraft.latitude
  latInput.oninput = (event) => {
    manualPointDraft.latitude = (event.target as HTMLInputElement).value
  }
  latField.append(latLabel, latInput)

  coordFieldWrap.append(lngField, latField)

  const remarkField = document.createElement('label')
  remarkField.className = 'manual-point-popup__field'
  const remarkLabel = document.createElement('span')
  remarkLabel.className = 'manual-point-popup__label'
  remarkLabel.textContent = '备注（可选）'
  const remarkInput = document.createElement('textarea')
  remarkInput.className = 'manual-point-popup__textarea'
  remarkInput.rows = 2
  remarkInput.maxLength = 200
  remarkInput.placeholder = '请输入备注信息'
  remarkInput.value = manualPointDraft.remark
  remarkInput.oninput = (event) => {
    manualPointDraft.remark = (event.target as HTMLTextAreaElement).value
  }
  remarkField.append(remarkLabel, remarkInput)

  const actions = document.createElement('div')
  actions.className = 'manual-point-popup__actions'

  const saveBtn = document.createElement('button')
  saveBtn.type = 'button'
  saveBtn.className = 'manual-point-popup__btn manual-point-popup__btn--primary'
  saveBtn.textContent = '保存点位'
  saveBtn.onclick = () => saveManualPointFromPopup()

  const cancelBtn = document.createElement('button')
  cancelBtn.type = 'button'
  cancelBtn.className = 'manual-point-popup__btn manual-point-popup__btn--plain'
  cancelBtn.textContent = '取消'
  cancelBtn.onclick = () => closeManualPointPopup(true)

  actions.append(saveBtn, cancelBtn)
  root.append(title, coordText, nameField, coordFieldWrap, remarkField, actions)
  return root
}

const openManualPointPopup = (lat: number, lng: number) => {
  if (!Leaflet || !lineMapInstance) return
  if (manualPointPopup) {
    lineMapInstance.closePopup(manualPointPopup)
    manualPointPopup = null
  }
  manualPointDraft.longitude = lng.toFixed(6)
  manualPointDraft.latitude = lat.toFixed(6)
  if (!manualPointDraft.pointName.trim()) {
    manualPointDraft.pointName = `手动点位${formData.points.length + 1}`
  }
  const content = buildManualPointPopupContent(lat, lng)
  if (Leaflet?.DomEvent?.disableClickPropagation) {
    Leaflet.DomEvent.disableClickPropagation(content)
  }
  manualPointPopup = Leaflet.popup({
    className: 'inspection-line-manual-popup',
    autoClose: false,
    closeOnClick: false,
    closeButton: true,
    offset: [0, -8]
  })
    .setLatLng([lat, lng])
    .setContent(content)
    .openOn(lineMapInstance)
  manualPointPopup.on('remove', () => {
    manualPointPopup = null
    manualPointPicking.value = false
    resetManualPointDraft()
  })
  manualPointPicking.value = false
  message.success('坐标已选取，请在弹框中编辑并保存点位')
}

const initLineMap = async () => {
  if (!lineMapRef.value || mapReady.value || mapLoading.value) return
  mapLoading.value = true
  try {
    await loadLeafletAssets()
    Leaflet = (window as any).L
    if (!Leaflet) {
      throw new Error('Leaflet 资源加载失败')
    }
    const tiandituKey = await getTiandituKey()
    lineMapInstance = Leaflet.map(lineMapRef.value, {
      center: DEFAULT_MAP_CENTER,
      zoom: DEFAULT_MAP_ZOOM,
      zoomControl: false
    })
    const vecLayer = Leaflet.tileLayer(
      `https://t{s}.tianditu.gov.cn/vec_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=vec&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=${tiandituKey}`,
      { subdomains: ['0', '1', '2', '3', '4', '5', '6', '7'] }
    )
    const cvaLayer = Leaflet.tileLayer(
      `https://t{s}.tianditu.gov.cn/cva_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cva&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=${tiandituKey}`,
      { subdomains: ['0', '1', '2', '3', '4', '5', '6', '7'] }
    )
    Leaflet.layerGroup([vecLayer, cvaLayer]).addTo(lineMapInstance)
    lineDeviceLayer = Leaflet.layerGroup().addTo(lineMapInstance)
    lineRouteLayer = Leaflet.layerGroup().addTo(lineMapInstance)
    lineOrderLayer = Leaflet.layerGroup().addTo(lineMapInstance)
    lineMapInstance.on('click', (event: any) => {
      if (formReadonly.value) return
      if (!manualPointPicking.value) return
      const lng = Number(event?.latlng?.lng)
      const lat = Number(event?.latlng?.lat)
      if (!Number.isFinite(lng) || !Number.isFinite(lat)) return
      openManualPointPopup(lat, lng)
    })
    mapReady.value = true
    renderLineMap(true)
  } catch (error) {
    console.error(error)
    message.error('天地图加载失败，请稍后重试')
    destroyLineMap()
  } finally {
    mapLoading.value = false
  }
}

const handleMapZoomIn = () => {
  lineMapInstance?.zoomIn?.()
}

const handleMapZoomOut = () => {
  lineMapInstance?.zoomOut?.()
}

const deviceTreeData = computed<DeviceTreeNode[]>(() => {
  if (!pointOptions.value.length) return []
  const groupMap = new Map<string, DeviceTreeNode>()
  pointOptions.value.forEach((option) => {
    const groupLabel = option.address?.trim() || '未分组设备'
    const groupKey = `group_${groupLabel}`
    if (!groupMap.has(groupKey)) {
      groupMap.set(groupKey, {
        id: groupKey,
        label: groupLabel,
        nodeType: 'group',
        children: []
      })
    }
    groupMap.get(groupKey)!.children!.push({
      id: `device_${option.deviceId}`,
      label: option.pointName,
      nodeType: 'device',
      pointOption: option
    })
  })
  return Array.from(groupMap.values())
})

const syncTreeCheckedByPoints = async () => {
  await nextTick()
  if (!treeRef.value) return
  syncingTreeChecked.value = true
  const checkedKeys = formData.points
    .filter((item) => item.pointType === 1 && item.deviceId)
    .map((item) => `device_${item.deviceId}`)
  treeRef.value.setCheckedKeys(checkedKeys)
  syncingTreeChecked.value = false
}

const loadPointOptions = async () => {
  if (!formData.stationId) {
    pointOptions.value = []
    renderLineMap()
    return
  }
  pointOptionsLoading.value = true
  try {
    pointOptions.value = await InspectionLineApi.getPointOptions({
      stationId: formData.stationId,
      keyword: pointKeyword.value?.trim() || undefined,
      limit: 1000
    })
  } finally {
    pointOptionsLoading.value = false
  }
  await syncTreeCheckedByPoints()
  renderLineMap(true)
}

const handleFormStationChange = async () => {
  if (formReadonly.value) return
  formData.points = []
  pointOptions.value = []
  pointKeyword.value = ''
  closeManualPointPopup(true)
  normalizePointsAfterChange()
  await loadPointOptions()
}

const activateManualPointMode = async () => {
  if (formReadonly.value) return
  if (!formData.stationId) {
    message.warning('请先选择所属站点')
    return
  }
  if (!mapReady.value) {
    await initLineMap()
    if (!mapReady.value) return
  }
  if (manualPointPicking.value) {
    closeManualPointPopup(true)
    message.info('已取消手动点位拾取')
    return
  }
  closeManualPointPopup(true)
  manualPointPicking.value = true
  message.info('请在地图上单击选择坐标，系统会弹框供你编辑并保存点位')
}

const clearAllPoints = async () => {
  if (formReadonly.value) return
  formData.points = []
  normalizePointsAfterChange()
  closeManualPointPopup(true)
  await syncTreeCheckedByPoints()
  renderLineMap()
}

const parseNumber = (value: string) => {
  if (value === undefined || value === null || value === '') return undefined
  const parsed = Number(value)
  if (Number.isNaN(parsed)) return undefined
  return parsed
}

const addDevicePoint = (option: InspectionLinePointOptionVO) => {
  if (formReadonly.value) return
  const exists = formData.points.some((item) => item.pointType === 1 && item.deviceId === option.deviceId)
  if (exists) return
  formData.points.push({
    __uuid: createUuid(),
    pointSort: formData.points.length + 1,
    pointType: 1,
    deviceId: option.deviceId,
    pointName: option.pointName,
    longitude: option.longitude,
    latitude: option.latitude,
    remark: option.address || ''
  })
  normalizePointsAfterChange()
}

const removeDevicePoint = (deviceId?: number) => {
  if (formReadonly.value) return
  if (!deviceId) return
  formData.points = formData.points.filter((item) => !(item.pointType === 1 && item.deviceId === deviceId))
  normalizePointsAfterChange()
}

const handleTreeCheckChange = (data: DeviceTreeNode, checked: boolean) => {
  if (formReadonly.value) return
  if (syncingTreeChecked.value || data.nodeType !== 'device' || !data.pointOption) return
  if (checked) {
    addDevicePoint(data.pointOption)
  } else {
    removeDevicePoint(data.pointOption.deviceId)
  }
}

const removePoint = (index: number) => {
  if (formReadonly.value) return
  const removed = formData.points[index]
  formData.points.splice(index, 1)
  normalizePointsAfterChange()
  if (removed?.pointType === 1 && removed.deviceId && treeRef.value) {
    syncingTreeChecked.value = true
    treeRef.value.setChecked(`device_${removed.deviceId}`, false, false)
    syncingTreeChecked.value = false
  }
}

const movePoint = (index: number, offset: number) => {
  if (formReadonly.value) return
  const targetIndex = index + offset
  if (targetIndex < 0 || targetIndex >= formData.points.length) return
  const current = formData.points[index]
  formData.points[index] = formData.points[targetIndex]
  formData.points[targetIndex] = current
  normalizePointsAfterChange()
}

const hasCoord = (point?: InspectionLinePointEditVO) => {
  if (!point) return false
  return point.longitude !== undefined && point.longitude !== null && point.latitude !== undefined && point.latitude !== null
}

const calcDistanceMeter = (a: InspectionLinePointEditVO, b: InspectionLinePointEditVO) => {
  if (!hasCoord(a) || !hasCoord(b)) return 0
  const lat1 = (Number(a.latitude) * Math.PI) / 180
  const lat2 = (Number(b.latitude) * Math.PI) / 180
  const deltaLat = lat2 - lat1
  const deltaLng = ((Number(b.longitude) - Number(a.longitude)) * Math.PI) / 180
  const x =
    Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
    Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2)
  const y = 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x))
  return 6378137 * y
}

// 维护点位排序、首尾坐标和总长度，避免前后端计算口径不一致。
const normalizePointsAfterChange = () => {
  formData.points.forEach((point, index) => {
    point.pointSort = index + 1
  })
  formData.pointCount = formData.points.length

  const firstPoint = formData.points.find((item) => hasCoord(item))
  const reversePoints = [...formData.points].reverse()
  const lastPoint = reversePoints.find((item) => hasCoord(item))
  formData.startLongitude = firstPoint?.longitude as number
  formData.startLatitude = firstPoint?.latitude as number
  formData.endLongitude = lastPoint?.longitude as number
  formData.endLatitude = lastPoint?.latitude as number

  let totalLength = 0
  for (let i = 1; i < formData.points.length; i++) {
    totalLength += calcDistanceMeter(formData.points[i - 1], formData.points[i])
  }
  formData.totalLengthMeter = Number(totalLength.toFixed(2))
  // 点位变化后自动聚焦，确保新选点位不会挤在一起看不清。
  renderLineMap(true)
}

const openForm = async (mode: FormMode, id?: number) => {
  formMode.value = mode
  formVisible.value = true
  resetForm()
  if (mode === 'create') return
  if (!id) return

  formLoading.value = true
  try {
    const data = await InspectionLineApi.getInspectionLine(id)
    Object.assign(formData, {
      id: data.id,
      stationId: data.stationId || '',
      lineName: data.lineName || '',
      inspectionType: data.inspectionType || '',
      areaType: data.areaType || '',
      lineDesc: data.lineDesc || '',
      startLongitude: data.startLongitude,
      startLatitude: data.startLatitude,
      endLongitude: data.endLongitude,
      endLatitude: data.endLatitude,
      totalLengthMeter: Number(data.totalLengthMeter || 0),
      pointCount: Number(data.pointCount || 0),
      useCount: Number(data.useCount || 0),
      status: data.status ?? 0,
      remark: data.remark || '',
      points: (data.points || []).map((point) => ({
        __uuid: createUuid(),
        id: point.id,
        pointSort: point.pointSort,
        pointName: point.pointName,
        pointType: point.pointType,
        deviceId: point.deviceId,
        locationId: point.locationId,
        longitude: point.longitude,
        latitude: point.latitude,
        remark: point.remark || ''
      }))
    })
    normalizePointsAfterChange()
    await loadPointOptions()
  } finally {
    formLoading.value = false
  }
}

const buildPayload = (): InspectionLineVO => {
  return {
    id: formData.id,
    stationId: formData.stationId,
    lineName: formData.lineName.trim(),
    inspectionType: formData.inspectionType,
    areaType: formData.areaType,
    lineDesc: formData.lineDesc?.trim() || '',
    startLongitude: Number(formData.startLongitude),
    startLatitude: Number(formData.startLatitude),
    endLongitude: Number(formData.endLongitude),
    endLatitude: Number(formData.endLatitude),
    totalLengthMeter: Number(formData.totalLengthMeter || 0),
    pointCount: formData.points.length,
    status: formData.status ?? 0,
    remark: formData.remark?.trim() || '',
    points: formData.points.map((point, index) => ({
      id: point.id,
      pointSort: index + 1,
      pointName: point.pointName?.trim() || `点位${index + 1}`,
      pointType: point.pointType,
      deviceId: point.pointType === 1 ? point.deviceId : undefined,
      locationId: point.pointType === 2 ? point.locationId : undefined,
      longitude: point.longitude,
      latitude: point.latitude,
      remark: point.remark?.trim() || ''
    }))
  }
}

const submitForm = async () => {
  if (formReadonly.value) return
  const valid = await formRef.value?.validate()
  if (!valid) return

  if (!formData.points.length) {
    message.error('请至少选择一个线路点位')
    return
  }
  if (!hasCoord(formData.points[0]) || !hasCoord(formData.points[formData.points.length - 1])) {
    message.error('首尾点位需要包含有效坐标，请调整点位顺序或补充手动点位')
    return
  }
  if (
    formData.startLongitude === undefined ||
    formData.startLatitude === undefined ||
    formData.endLongitude === undefined ||
    formData.endLatitude === undefined
  ) {
    message.error('起点或终点坐标缺失，请检查点位坐标')
    return
  }

  submitLoading.value = true
  try {
    const payload = buildPayload()
    if (formMode.value === 'create') {
      delete payload.id
      await InspectionLineApi.createInspectionLine(payload)
      message.success('新增成功')
    } else {
      await InspectionLineApi.updateInspectionLine(payload)
      message.success('更新成功')
    }
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

watch(
  () => formVisible.value,
  async (visible) => {
    if (visible) {
      await nextTick()
      await initLineMap()
      renderLineMap(true)
      return
    }
    cancelManualPointInput()
    destroyLineMap()
  }
)

watch(
  () =>
    pointOptions.value
      .map((item) => `${item.deviceId}_${item.longitude ?? ''}_${item.latitude ?? ''}`)
      .join('|'),
  () => {
    renderLineMap()
  }
)

onMounted(async () => {
  await getList()
})

onBeforeUnmount(() => {
  destroyLineMap()
})
</script>

<style scoped lang="scss">
.inspection-line-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.query-tip {
  margin-top: 8px;
  color: #ef4444;
  font-size: 13px;
}

.query-range-item {
  :deep(.el-form-item__content) {
    display: flex;
    align-items: center;
  }
}

.query-range-sep {
  color: #94a3b8;
  margin: 0 8px;
}

.form-panel {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 14px;
  background: #fff;
}

.form-panel__title {
  margin: 0 0 14px;
  font-size: 16px;
  font-weight: 600;
  color: #1e3a8a;
}

.coord-text {
  width: 100%;
  min-height: 32px;
  line-height: 32px;
  padding: 0 10px;
  border: 1px solid #dbe5f1;
  border-radius: 6px;
  color: #334155;
  background: #f8fafc;
}

.item-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.item-header__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.point-search {
  width: 280px;
}

.point-board {
  display: grid;
  grid-template-columns: 320px minmax(640px, 1fr);
  gap: 12px;
}

.point-tree-panel {
  border: 1px solid #dbe5f1;
  border-radius: 10px;
  padding: 12px;
  min-height: 620px;
  background: #f8fbff;
  overflow: auto;
}

.point-map-panel {
  border: 1px solid #dbe5f1;
  border-radius: 10px;
  padding: 12px;
  background: #f8fbff;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 620px;
}

.line-map-wrap {
  position: relative;
  height: 470px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  overflow: hidden;
  background: #e2e8f0;
}

.line-map {
  width: 100%;
  height: 100%;
}

.line-map-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #334155;
  font-size: 14px;
  background: rgba(248, 250, 252, 0.85);
  z-index: 400;
}

.line-map-toolbar {
  position: absolute;
  top: 12px;
  right: 12px;
  min-width: 270px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid rgba(148, 163, 184, 0.45);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.12);
  z-index: 420;
}

.line-map-toolbar__title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.line-map-toolbar__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.line-map-toolbar__tip {
  margin-top: 8px;
  color: #ef4444;
  font-size: 12px;
}

.panel-subtitle {
  margin-bottom: 10px;
  font-size: 13px;
  color: #334155;
  font-weight: 600;
}

.point-tree-node {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.point-tree-node__tip {
  color: #94a3b8;
  font-size: 12px;
}

.point-selected-panel {
  border: 1px solid #dbe5f1;
  border-radius: 10px;
  padding: 12px;
  background: #f8fbff;
}

.selected-point-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 250px;
  overflow-y: auto;
}

.selected-point-item {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #dbe5f1;
  border-radius: 8px;
  padding: 10px 12px;
  background: #fff;
  transition: border-color 0.2s ease;

  &:hover {
    border-color: #93c5fd;
  }
}

.selected-point-item__index {
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 999px;
  background: #dbeafe;
  color: #1d4ed8;
  font-weight: 600;
}

.selected-point-item__main {
  flex: 1;
  min-width: 0;
}

.selected-point-item__name {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #0f172a;
  font-weight: 500;
}

.selected-point-item__meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.selected-point-item__actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.inspection-line-manual-popup .leaflet-popup-content-wrapper) {
  border-radius: 12px;
  border: 1px solid #bfdbfe;
  box-shadow: 0 10px 30px rgba(30, 64, 175, 0.2);
}

:deep(.inspection-line-manual-popup .leaflet-popup-content) {
  margin: 0;
  width: 320px;
}

:deep(.inspection-line-manual-popup .leaflet-popup-tip) {
  background: #fff;
}

:deep(.manual-point-popup) {
  padding: 12px;
  background: #fff;
}

:deep(.manual-point-popup__title) {
  margin-bottom: 6px;
  color: #1d4ed8;
  font-size: 14px;
  font-weight: 700;
}

:deep(.manual-point-popup__coord) {
  margin-bottom: 10px;
  font-size: 12px;
  color: #64748b;
}

:deep(.manual-point-popup__grid) {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

:deep(.manual-point-popup__field) {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}

:deep(.manual-point-popup__label) {
  color: #334155;
  font-size: 12px;
  font-weight: 600;
}

:deep(.manual-point-popup__input),
:deep(.manual-point-popup__textarea) {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 6px 8px;
  font-size: 13px;
  color: #0f172a;
  transition: border-color 0.2s ease;
}

:deep(.manual-point-popup__input:focus),
:deep(.manual-point-popup__textarea:focus) {
  border-color: #3b82f6;
  outline: none;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15);
}

:deep(.manual-point-popup__textarea) {
  resize: vertical;
  min-height: 56px;
}

:deep(.manual-point-popup__actions) {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

:deep(.manual-point-popup__btn) {
  cursor: pointer;
  border-radius: 8px;
  border: 1px solid transparent;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.2s ease;
}

:deep(.manual-point-popup__btn--primary) {
  color: #fff;
  background: #2563eb;
}

:deep(.manual-point-popup__btn--primary:hover) {
  background: #1d4ed8;
}

:deep(.manual-point-popup__btn--plain) {
  color: #334155;
  border-color: #cbd5e1;
  background: #fff;
}

:deep(.manual-point-popup__btn--plain:hover) {
  border-color: #94a3b8;
  color: #1f2937;
}

.panel-foot-tip {
  margin: 12px 0 0;
  font-size: 12px;
  color: #64748b;
}

.empty-holder {
  min-height: 120px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

@media (max-width: 1200px) {
  .point-board {
    grid-template-columns: 1fr;
  }

  .point-tree-panel,
  .point-map-panel {
    min-height: auto;
  }

  .line-map-wrap {
    height: 400px;
  }
}

:deep(.inspection-line-dialog .el-dialog__body) {
  max-height: 72vh;
  overflow-y: auto;
  padding-top: 10px;
}

:deep(.line-map .leaflet-control-container .leaflet-top.leaflet-left) {
  display: none;
}

:deep(.line-map .leaflet-container) {
  font-family: 'Microsoft YaHei', sans-serif;
}

:deep(.inspection-line-route-tooltip) {
  border: none;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.18);
  color: #0f172a;
  font-size: 12px;
  font-weight: 500;
}
</style>
