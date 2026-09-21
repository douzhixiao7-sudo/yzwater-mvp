<template>
  <div class="river-page">
    <ContentWrap class="query-wrap">
      <el-form class="query-form -mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="泵站名称" prop="pumpStationName">
          <el-input
            v-model="queryParams.pumpStationName"
            placeholder="请输入泵站名称关键字"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item label="泵站类型" prop="pumpStationType">
          <el-select v-model="queryParams.pumpStationType" placeholder="请选择泵站类型" clearable class="!w-240px">
            <el-option v-for="item in pumpStationTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="行政区划" prop="divisionCode">
          <el-tree-select
            v-model="queryParams.divisionCode"
            :data="areaTreeData"
            :props="areaTreeProps"
            node-key="value"
            check-strictly
            filterable
            clearable
            placeholder="请选择区划"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleSearch"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="handleReset"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
          <el-button type="primary" plain @click="openCreateDialog">
            <Icon icon="ep:plus" class="mr-5px" /> 新增
          </el-button>
          <el-button type="success" plain @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 列表 -->
    <ContentWrap class="table-wrap">
      <el-table class="river-table" v-loading="tableLoading" :data="tableData">
        <el-table-column label="序号" type="index" align="center" width="72">
          <template #default="{ $index }">
            {{ (queryParams.pageNo - 1) * queryParams.pageSize + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column label="闸站名称" align="center" min-width="180" prop="pumpStationName" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="station-name-cell">{{ row.pumpStationName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="抽排流量(m³/s)" align="center" min-width="132">
          <template #default="{ row }">
            {{ formatDecimal(row.pumpingFlow) }}
          </template>
        </el-table-column>
        <el-table-column label="抽引流量(m³/s)" align="center" min-width="132">
          <template #default="{ row }">
            {{ formatDecimal(row.installedFlow) }}
          </template>
        </el-table-column>
        <el-table-column label="自排流量(m³/s)" align="center" min-width="132">
          <template #default="{ row }">
            {{ formatDecimal(row.selfFlow) }}
          </template>
        </el-table-column>
        <el-table-column label="常水位(m)" align="center" min-width="116">
          <template #default="{ row }">
            {{ formatDecimal(row.normalWaterLevel) }}
          </template>
        </el-table-column>
        <el-table-column label="防办预降水位(m)" align="center" min-width="148">
          <template #default="{ row }">
            {{ formatDecimal(row.preDropWaterLevel) }}
          </template>
        </el-table-column>
        <el-table-column label="最低运行水位(m)" align="center" min-width="148">
          <template #default="{ row }">
            {{ formatDecimal(row.minimumOperatingWaterLevel) }}
          </template>
        </el-table-column>
        <el-table-column label="机组数量" align="center" min-width="100">
          <template #default="{ row }">
            {{ row.unitCount ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="单机组功率(KW)" align="center" min-width="140">
          <template #default="{ row }">
            {{ formatInteger(row.singleUnitPower) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="warning" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="fetchTable"
      />
    </ContentWrap>

    <el-dialog class="facility-dialog" v-model="createDialogVisible" :title="dialogTitle" width="980px" destroy-on-close :close-on-click-modal="false">
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="120px"
        label-position="left"
        :disabled="formReadonly"
      >
        <el-row :gutter="16">
          <el-col :span="24">
            <div class="group-title">基础信息</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="泵站代码" prop="pumpStationCode">
              <el-input v-model="createForm.pumpStationCode" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="泵站名称" prop="pumpStationName">
              <el-input v-model="createForm.pumpStationName" placeholder="请输入" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="行政区划" prop="divisionCode">
              <el-tree-select
                v-model="createForm.divisionCode"
                :data="areaTreeData"
                :props="areaTreeProps"
                node-key="value"
                multiple
                show-checkbox
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="6"
                check-strictly
                filterable
                clearable
                placeholder="请选择区划（可多选）"
                class="select-long"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="泵站类型" prop="pumpStationType">
              <el-select v-model="createForm.pumpStationType" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in pumpStationTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="group-title">位置与地图</div>
          </el-col>
          <el-col :span="24">
            <el-form-item label="具体位置" prop="pumpStationPosition">
              <el-input v-model="createForm.pumpStationPosition" placeholder="请输入" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number v-model="createForm.longitude" :precision="6" :step="0.000001" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number v-model="createForm.latitude" :precision="6" :step="0.000001" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="section-title">泵站位置</div>
            <TiandituGeoJsonPreview
              v-if="formReadonly"
              :geo-json="pumpStationGeoJson"
              :active="createDialogVisible"
              :height="360"
              :center="pumpStationMapCenter"
              :zoom="14"
              :label-text="createForm.pumpStationName || ''"
              :highlight="true"
              empty-text="暂无位置信息"
            />
            <div v-else>
              <TiandituGeoJsonEditor
                v-model="pumpStationGeoJson"
                :active="createDialogVisible"
                :height="420"
                :center="pumpStationMapCenter"
                :zoom="14"
                :draw-modes="['POINT']"
              />
              <div class="map-tip">仅支持绘制点位；可直接输入经纬度，或在地图上绘制点位自动回填到经纬度。</div>
            </div>
          </el-col>

          <el-col :span="24">
            <div class="group-title">工程参数</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="装机功率(KW)" prop="installedCapacityKw">
              <el-input-number v-model="createForm.installedCapacityKw" :min="0" :precision="0" :step="1" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="装机流量(m³/s)" prop="capacityFlow">
              <el-input-number
                v-model="createForm.capacityFlow"
                :min="0"
                :precision="formReadonly ? 2 : undefined"
                :step="0.01"
                controls-position="right"
                class="select-long"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="机组数量" prop="unitCount">
              <el-input-number v-model="createForm.unitCount" :min="0" :precision="0" :step="1" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="常水位(m)" prop="normalWaterLevel">
              <el-input-number v-model="createForm.normalWaterLevel" :precision="2" :step="0.01" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="防办预降水位(m)" prop="preDropWaterLevel">
              <el-input-number v-model="createForm.preDropWaterLevel" :precision="2" :step="0.01" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低运行水位(m)" prop="minimumOperatingWaterLevel">
              <el-input-number
                v-model="createForm.minimumOperatingWaterLevel"
                :precision="2"
                :step="0.01"
                controls-position="right"
                class="select-long"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单机组功率(KW)" prop="singleUnitPower">
              <el-input-number v-model="createForm.singleUnitPower" :min="0" :precision="0" :step="1" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="工程等别" prop="engineeringGrade">
              <el-select v-model="createForm.engineeringGrade" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in engineeringGradeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="闸站规模" prop="engineeringScale">
              <el-input v-model="createForm.engineeringScale" placeholder="请输入" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="防洪设计标准" prop="floodControlDesignStandard">
              <el-input v-model="createForm.floodControlDesignStandard" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="建设时间" prop="constructionTime">
              <el-date-picker
                v-model="createForm.constructionTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择"
                class="select-long"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="自排流量(m³/s)" prop="selfFlow">
              <el-input-number v-model="createForm.selfFlow" :min="0" :precision="4" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抽引流量(m³/s)" prop="installedFlow">
              <el-input-number v-model="createForm.installedFlow" :min="0" :precision="4" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抽排流量(m³/s)" prop="pumpingFlow">
              <el-input-number v-model="createForm.pumpingFlow" :min="0" :precision="4" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="group-title">管理与资料</div>
          </el-col>
          <el-col :span="24">
            <el-form-item label="归口管理部门" prop="managementDepartment">
              <el-select
                v-model="createForm.managementDepartment"
                multiple
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="6"
                clearable
                placeholder="请选择（可多选）"
                class="select-long"
              >
                <el-option v-for="item in managementDepartmentOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="泵站图片" prop="pumpStationImages">
              <UploadImgs v-model="createForm.pumpStationImages" :limit="5" :fileSize="5" :disabled="formReadonly">
                <template #tip>
                  <div class="upload-tip">最多上传 5 张图片，每张不超过 5M。</div>
                </template>
              </UploadImgs>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="泵站概览" prop="pumpStationOverview">
              <el-input v-model="createForm.pumpStationOverview" type="textarea" :rows="4" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">关闭</el-button>
        <el-button v-if="!formReadonly" type="primary" :loading="createSubmitting" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import UploadImgs from '@/components/UploadFile/src/UploadImgs.vue'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import { getAreaTree } from '@/api/system/area'
import { getRiverDict, type DictDataItemRespVO } from '@/api/gis/riverChannel'
import download from '@/utils/download'
import {
  createPumpStation,
  deletePumpStation,
  exportPumpStationExcel,
  getPumpStationDetail,
  getPumpStationPage,
  updatePumpStation,
  type PumpStationPageReqVO,
  type PumpStationPageRespVO,
  type PumpStationSaveReqVO
} from '@/api/gis/pumpStation'

const queryParams = reactive<PumpStationPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  pumpStationName: '',
  pumpStationType: '',
  divisionCode: ''
})
const queryFormRef = ref<FormInstance>()

const tableData = ref<PumpStationPageRespVO[]>([])
const total = ref(0)
const tableLoading = ref(false)

const pumpStationTypeOptions = ref<DictDataItemRespVO[]>([])
const engineeringGradeOptions = ref<DictDataItemRespVO[]>([])
const managementDepartmentOptions = ref<DictDataItemRespVO[]>([])

type AreaTreeNode = {
  name: string
  id: number
  children?: AreaTreeNode[]
}

type TreeSelectNode = {
  label: string
  value: string
  children?: TreeSelectNode[]
}

const areaTreeData = ref<TreeSelectNode[]>([])
const areaTreeProps = {
  label: 'label',
  value: 'value',
  children: 'children'
}

const buildAreaTree = (list: AreaTreeNode[]): TreeSelectNode[] => {
  return (list || []).map((item) => ({
    label: item.name,
    value: String(item.id),
    children: item.children && item.children.length ? buildAreaTree(item.children) : undefined
  }))
}

const loadAreaTree = async () => {
  const res = (await getAreaTree()) as any
  // 兼容接口返回结构：{ code, msg, data }
  const data = (res && (res.data || res)) as AreaTreeNode[]
  areaTreeData.value = buildAreaTree(data || [])
}

const areaLabelMap = computed(() => {
  const labelMap = new Map<string, string>()
  const collect = (nodes?: TreeSelectNode[]) => {
    if (!nodes?.length) return
    nodes.forEach((node) => {
      labelMap.set(String(node.value), node.label)
      collect(node.children)
    })
  }
  collect(areaTreeData.value)
  return labelMap
})

const normalizeCodes = (codes?: string[] | string) => {
  if (Array.isArray(codes)) {
    return codes.map((item) => String(item || '').trim()).filter((item) => !!item)
  }
  const singleCode = String(codes || '').trim()
  return singleCode ? [singleCode] : []
}

const formatDivisionCodes = (codes?: string[] | string) => {
  const normalizedCodes = normalizeCodes(codes)
  if (!normalizedCodes.length) return '-'
  return normalizedCodes
    .map((code) => areaLabelMap.value.get(code) || code)
    .join('、')
}

const formatInteger = (val?: number | null) => {
  if (val === undefined || val === null) return '-'
  const num = Number(val)
  if (Number.isNaN(num)) return '-'
  return String(Math.round(num))
}

const truncateDecimalNumber = (val: number, precision = 2) => {
  const factor = 10 ** precision
  return val >= 0 ? Math.floor(val * factor) / factor : Math.ceil(val * factor) / factor
}

const formatDecimal = (val?: number | null, precision = 2) => {
  if (val === undefined || val === null) return '-'
  const num = Number(val)
  if (Number.isNaN(num)) return '-'
  return truncateDecimalNumber(num, precision).toFixed(precision)
}

const formatCoordinate = (val?: number | null) => {
  if (val === undefined || val === null) return '-'
  const num = Number(val)
  if (Number.isNaN(num)) return '-'
  return num.toFixed(6)
}

const fetchTable = async () => {
  tableLoading.value = true
  try {
    const res = await getPumpStationPage(queryParams)
    tableData.value = res?.list || []
    total.value = res?.total || 0
  } finally {
    tableLoading.value = false
  }
}

const loadDict = async () => {
  const [types, grades, depts] = await Promise.all([getRiverDict('zd_bzlx'), getRiverDict('zd_gcdb'), getRiverDict('zd_gldw')])
  pumpStationTypeOptions.value = types || []
  engineeringGradeOptions.value = grades || []
  managementDepartmentOptions.value = depts || []
}

onMounted(async () => {
  await Promise.all([loadDict(), loadAreaTree()])
  fetchTable()
})

const handleSearch = () => {
  queryParams.pageNo = 1
  fetchTable()
}

const handleReset = () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  queryParams.pageSize = 10
  fetchTable()
}

const handleExport = async () => {
  try {
    const data = await exportPumpStationExcel(queryParams)
    download.excel(data, '泵站信息.xls')
  } catch {
    // 下载失败时，错误提示由全局拦截器处理
  }
}

const handleDetail = (row: PumpStationPageRespVO) => {
  if (!row?.id) return
  openDetailDialog(row.id)
}

const handleEdit = (row: PumpStationPageRespVO) => {
  if (!row?.id) return
  openEditDialog(row.id)
}

const handleDelete = async (row: PumpStationPageRespVO) => {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm(`确认删除泵站「${row.pumpStationName || row.id}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deletePumpStation(row.id)
  ElMessage.success('删除成功')
  fetchTable()
}

const createDialogVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref<FormInstance>()

const formMode = ref<'create' | 'edit' | 'view'>('create')
const dialogTitle = computed(() => {
  if (formMode.value === 'edit') return '编辑泵站'
  if (formMode.value === 'view') return '泵站详情'
  return '新增泵站'
})
const formReadonly = computed(() => formMode.value === 'view')

const pumpStationGeoJson = ref('')
const DEFAULT_PUMP_STATION_CENTER = [32.272258, 119.184766] as [number, number]

const normalizeNumber = (val: unknown) => {
  if (val === null || val === undefined) return NaN
  if (typeof val === 'number') return val
  if (typeof val === 'string') return Number(val)
  return NaN
}

const buildPointGeoJson = (longitude?: unknown, latitude?: unknown) => {
  const lon = normalizeNumber(longitude)
  const lat = normalizeNumber(latitude)
  if (!Number.isFinite(lon) || !Number.isFinite(lat)) return ''
  return JSON.stringify({ type: 'Point', coordinates: [lon, lat] })
}

const parsePointGeoJson = (geoJson?: string) => {
  if (!geoJson) return undefined
  let parsed: any
  try {
    parsed = JSON.parse(geoJson)
  } catch {
    return undefined
  }
  if (!parsed || parsed.type !== 'Point' || !Array.isArray(parsed.coordinates) || parsed.coordinates.length < 2) return undefined
  const lon = normalizeNumber(parsed.coordinates[0])
  const lat = normalizeNumber(parsed.coordinates[1])
  if (!Number.isFinite(lon) || !Number.isFinite(lat)) return undefined
  return { lon, lat }
}

const pumpStationMapCenter = computed<[number, number]>(() => {
  const lon = normalizeNumber(createForm.longitude)
  const lat = normalizeNumber(createForm.latitude)
  if (Number.isFinite(lon) && Number.isFinite(lat)) {
    return [lat, lon]
  }
  return DEFAULT_PUMP_STATION_CENTER
})

type PumpStationCreateForm = Omit<PumpStationSaveReqVO, 'id' | 'divisionCode' | 'managementDepartment' | 'pumpStationImages'> & {
  id: string
  divisionCode: string[]
  managementDepartment: string[]
  pumpStationImages: string[]
  longitude?: number
  latitude?: number
}

const createForm = reactive<PumpStationCreateForm>({
  id: '',
  pumpStationCode: '',
  pumpStationName: '',
  divisionCode: [],
  pumpStationType: '',
  pumpStationPosition: '',
  longitude: undefined,
  latitude: undefined,
  engineeringGrade: '',
  engineeringScale: '',
  floodControlDesignStandard: '',
  installedCapacityKw: undefined,
  capacityFlow: undefined,
  unitCount: undefined,
  normalWaterLevel: undefined,
  preDropWaterLevel: undefined,
  minimumOperatingWaterLevel: undefined,
  singleUnitPower: undefined,
  selfFlow: undefined,
  installedFlow: undefined,
  pumpingFlow: undefined,
  constructionTime: '',
  managementDepartment: [],
  pumpStationImages: [],
  pumpStationOverview: ''
})

const createRules: FormRules = {
  pumpStationName: [{ required: true, message: '请输入泵站名称', trigger: 'blur' }]
}

const resetCreateForm = () => {
  createForm.id = ''
  createForm.pumpStationCode = ''
  createForm.pumpStationName = ''
  createForm.divisionCode = []
  createForm.pumpStationType = ''
  createForm.pumpStationPosition = ''
  createForm.longitude = undefined
  createForm.latitude = undefined
  createForm.engineeringGrade = ''
  createForm.engineeringScale = ''
  createForm.floodControlDesignStandard = ''
  createForm.installedCapacityKw = undefined
  createForm.capacityFlow = undefined
  createForm.unitCount = undefined
  createForm.normalWaterLevel = undefined
  createForm.preDropWaterLevel = undefined
  createForm.minimumOperatingWaterLevel = undefined
  createForm.singleUnitPower = undefined
  createForm.selfFlow = undefined
  createForm.installedFlow = undefined
  createForm.pumpingFlow = undefined
  createForm.constructionTime = ''
  createForm.managementDepartment = []
  createForm.pumpStationImages = []
  createForm.pumpStationOverview = ''
  pumpStationGeoJson.value = ''
  createFormRef.value?.clearValidate()
}

const openCreateDialog = async () => {
  formMode.value = 'create'
  resetCreateForm()
  createDialogVisible.value = true
  await Promise.all([loadDict(), loadAreaTree()])
}

const fillFormByDetail = async (id: string | number) => {
  const data: any = await getPumpStationDetail(id)
  resetCreateForm()

  createForm.id = String(data.id ?? id)
  createForm.pumpStationCode = data.pumpStationCode || ''
  createForm.pumpStationName = data.pumpStationName || ''
  createForm.divisionCode = Array.isArray(data.divisionCode) ? data.divisionCode.map((v: any) => String(v)) : []
  createForm.pumpStationType = data.pumpStationType || ''
  createForm.pumpStationPosition = data.pumpStationPosition || ''
  createForm.longitude = data.longitude
  createForm.latitude = data.latitude
  createForm.engineeringGrade = data.engineeringGrade || ''
  createForm.engineeringScale = data.engineeringScale || ''
  createForm.floodControlDesignStandard = data.floodControlDesignStandard || ''
  createForm.installedCapacityKw = data.installedCapacityKw
  createForm.capacityFlow = data.capacityFlow
  createForm.unitCount = data.unitCount
  createForm.normalWaterLevel = data.normalWaterLevel
  createForm.preDropWaterLevel = data.preDropWaterLevel
  createForm.minimumOperatingWaterLevel = data.minimumOperatingWaterLevel
  createForm.singleUnitPower = data.singleUnitPower
  createForm.selfFlow = data.selfFlow
  createForm.installedFlow = data.installedFlow
  createForm.pumpingFlow = data.pumpingFlow
  createForm.constructionTime = data.constructionTime || ''
  createForm.managementDepartment = Array.isArray(data.managementDepartment) ? data.managementDepartment : []
  createForm.pumpStationImages = Array.isArray(data.pumpStationImages) ? data.pumpStationImages : []
  createForm.pumpStationOverview = data.pumpStationOverview || ''

  pumpStationGeoJson.value = buildPointGeoJson(createForm.longitude, createForm.latitude)
}

const openEditDialog = async (id: string | number) => {
  formMode.value = 'edit'
  createDialogVisible.value = true
  await Promise.all([loadDict(), loadAreaTree()])
  await fillFormByDetail(id)
}

const openDetailDialog = async (id: string | number) => {
  formMode.value = 'view'
  createDialogVisible.value = true
  await Promise.all([loadDict(), loadAreaTree()])
  await fillFormByDetail(id)
}

let syncingFromGeoJson = false
let syncingFromLonLat = false

watch(
  () => [createForm.longitude, createForm.latitude],
  ([lon, lat]) => {
    if (syncingFromGeoJson) return
    const next = buildPointGeoJson(lon, lat)
    if (next === pumpStationGeoJson.value) return
    syncingFromLonLat = true
    pumpStationGeoJson.value = next
    syncingFromLonLat = false
  }
)

watch(
  () => pumpStationGeoJson.value,
  (val) => {
    if (syncingFromLonLat) return
    const point = parsePointGeoJson(val)
    syncingFromGeoJson = true
    if (!point) {
      createForm.longitude = undefined
      createForm.latitude = undefined
    } else {
      createForm.longitude = Number(point.lon.toFixed(6))
      createForm.latitude = Number(point.lat.toFixed(6))
    }
    syncingFromGeoJson = false
  }
)

watch(
  () => createForm.capacityFlow,
  (val) => {
    if (val === undefined || val === null) return
    const num = Number(val)
    if (Number.isNaN(num)) return
    const truncated = truncateDecimalNumber(num, 2)
    if (truncated !== num) {
      createForm.capacityFlow = truncated
    }
  }
)

const normalizeId = (val?: string) => (val && val.trim() ? val : undefined)

const normalizeIds = (vals?: string[]) => {
  return (vals || []).map((v) => String(v || '').trim()).filter((v) => v)
}

const submitCreate = async () => {
  await createFormRef.value?.validate()
  createSubmitting.value = true
  try {
    const payload: PumpStationSaveReqVO = {
      id: formMode.value === 'edit' ? normalizeId(createForm.id) : undefined,
      pumpStationCode: normalizeId(createForm.pumpStationCode),
      pumpStationName: createForm.pumpStationName,
      divisionCode: normalizeIds(createForm.divisionCode),
      pumpStationType: normalizeId(createForm.pumpStationType),
      pumpStationPosition: normalizeId(createForm.pumpStationPosition),
      longitude: createForm.longitude,
      latitude: createForm.latitude,
      installedCapacityKw: createForm.installedCapacityKw,
      capacityFlow:
        createForm.capacityFlow === undefined || createForm.capacityFlow === null
          ? undefined
          : truncateDecimalNumber(Number(createForm.capacityFlow), 2),
      unitCount: createForm.unitCount,
      normalWaterLevel: createForm.normalWaterLevel,
      preDropWaterLevel: createForm.preDropWaterLevel,
      minimumOperatingWaterLevel: createForm.minimumOperatingWaterLevel,
      singleUnitPower: createForm.singleUnitPower,
      selfFlow: createForm.selfFlow,
      installedFlow: createForm.installedFlow,
      pumpingFlow: createForm.pumpingFlow,
      engineeringGrade: normalizeId(createForm.engineeringGrade),
      engineeringScale: normalizeId(createForm.engineeringScale),
      floodControlDesignStandard: normalizeId(createForm.floodControlDesignStandard),
      constructionTime: normalizeId(createForm.constructionTime),
      managementDepartment: normalizeIds(createForm.managementDepartment),
      pumpStationImages: normalizeIds(createForm.pumpStationImages),
      pumpStationOverview: createForm.pumpStationOverview
    }

    if (formMode.value === 'edit') {
      await updatePumpStation(payload)
      ElMessage.success('保存成功')
    } else {
      await createPumpStation(payload)
      ElMessage.success('新增成功')
    }
    createDialogVisible.value = false
    fetchTable()
  } finally {
    createSubmitting.value = false
  }
}

</script>

<style scoped>
.river-page {
  --river-bg: #f3f7fc;
  --river-card-bg: #ffffff;
  --river-border: rgba(148, 163, 184, 0.28);
  --river-shadow: 0 10px 28px rgba(15, 23, 42, 0.07);
  --river-primary: #1e40af;
  --river-primary-soft: rgba(30, 64, 175, 0.08);
  --river-text-main: #1f2a37;

  padding: 4px 2px 10px;
  background: linear-gradient(180deg, #f8fbff 0%, var(--river-bg) 100%);
}

.query-wrap,
.table-wrap {
  background: var(--river-card-bg);
  border: 1px solid var(--river-border);
  border-radius: 14px;
  box-shadow: var(--river-shadow);
}

.table-wrap {
  margin-top: 12px;
}

.query-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.query-form :deep(.el-form-item__label) {
  color: var(--river-text-main);
  font-weight: 600;
}

.query-form :deep(.el-input__wrapper),
.query-form :deep(.el-select__wrapper),
.query-form :deep(.el-tree-select__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.32) inset;
  transition: box-shadow 0.2s ease, background-color 0.2s ease;
}

.query-form :deep(.el-input__wrapper:hover),
.query-form :deep(.el-select__wrapper:hover),
.query-form :deep(.el-tree-select__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(30, 64, 175, 0.42) inset;
}

.query-form :deep(.el-input__wrapper.is-focus),
.query-form :deep(.el-select__wrapper.is-focused),
.query-form :deep(.el-tree-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--river-primary) inset;
}

.query-form :deep(.el-button) {
  border-radius: 10px;
  font-weight: 600;
}

.river-table {
  border-radius: 12px;
  overflow: hidden;
}

.river-table :deep(.el-table__header th) {
  background: #f5f8ff;
  color: #1e3a8a;
  font-weight: 700;
}

.river-table :deep(.el-table__row > td) {
  transition: background-color 0.2s ease;
}

.river-table :deep(.el-table__body tr:hover > td) {
  background: var(--river-primary-soft);
}

.station-name-cell {
  display: inline-block;
  max-width: 100%;
  font-weight: 700;
  color: #163172;
  letter-spacing: 0.02em;
}

.table-wrap :deep(.el-pagination) {
  margin-top: 14px;
  justify-content: flex-end;
}

:deep(.facility-dialog .el-dialog) {
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.2);
}

:deep(.facility-dialog .el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.24);
  background: linear-gradient(90deg, #f8fbff 0%, #f3f7ff 100%);
}

:deep(.facility-dialog .el-dialog__title) {
  font-weight: 700;
  color: #1e3a8a;
}

:deep(.facility-dialog .el-dialog__body) {
  padding: 16px 20px;
}

:deep(.facility-dialog .el-dialog__footer) {
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  padding: 12px 20px;
}

@media (max-width: 768px) {
  .query-form :deep(.el-input),
  .query-form :deep(.el-select),
  .query-form :deep(.el-tree-select) {
    width: 100% !important;
  }

  .query-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }
}

.select-long {
  width: 100%;
}

.section-title {
  font-weight: 600;
  margin: 8px 0;
  color: #303133;
}

.group-title {
  font-weight: 600;
  margin: 10px 0 12px;
  padding: 6px 10px;
  color: #303133;
  background: #f5f7fa;
  border-left: 4px solid #409eff;
  border-radius: 4px;
}

.map-tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
