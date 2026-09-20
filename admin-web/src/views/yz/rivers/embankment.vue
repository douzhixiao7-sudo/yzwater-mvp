<template>
  <div class="river-page">
    <ContentWrap class="query-wrap">
      <el-form class="query-form -mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="堤防名称" prop="embankmentName">
          <el-input
            v-model="queryParams.embankmentName"
            placeholder="请输入堤防名称关键字"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item label="堤防级别" prop="embankmentLevel">
          <el-select v-model="queryParams.embankmentLevel" placeholder="请选择堤防级别" clearable class="!w-240px">
            <el-option v-for="item in embankmentLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="堤防类型" prop="embankmentType">
          <el-select v-model="queryParams.embankmentType" placeholder="请选择堤防类型" clearable class="!w-240px">
            <el-option v-for="item in embankmentTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
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
          <el-button type="warning" plain @click="openImportDialog">
            <Icon icon="ep:upload" class="mr-5px" /> 导入
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 列表 -->
    <ContentWrap class="table-wrap">
      <el-table class="river-table" v-loading="tableLoading" :data="tableData">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="堤防名称" align="center" min-width="160" prop="embankmentName" show-overflow-tooltip />
        <el-table-column label="河流岸别" align="center" min-width="120">
          <template #default="{ row }">
            {{ row.riverBankSideLabel || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="堤防级别" align="center" min-width="120">
          <template #default="{ row }">
            {{ row.embankmentLevelLabel || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="设计高潮位(m)" align="center" min-width="140" prop="designHighTide" show-overflow-tooltip />
        <el-table-column label="堤防长度(m)" align="center" min-width="140" prop="lengthM">
          <template #default="{ row }">
            {{ formatNumber(row.lengthM) }}
          </template>
        </el-table-column>
        <el-table-column label="起点" align="center" min-width="180" prop="startPoint" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.startPoint || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="终点" align="center" min-width="180" prop="endPoint" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.endPoint || '-' }}
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
        label-width="140px"
        label-position="left"
        :disabled="formReadonly"
      >
        <el-row :gutter="12">
          <el-col :span="24">
            <div class="group-title">基础信息</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堤防名称" prop="embankmentName">
              <el-input v-model="createForm.embankmentName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="所在河道/河段" prop="relationValue">
              <el-tree-select
                v-model="createForm.relationValue"
                :data="riverTreeData"
                :props="riverTreeProps"
                node-key="value"
                lazy
                check-strictly
                filterable
                clearable
                :load="loadRiverTreeNode"
                :loading="riverTreeLoading"
                placeholder="请选择所在河道/河段（可不选）"
                class="select-long"
                @change="handleRelationValueChange"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="区划代码" prop="divisionCode">
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
                placeholder="请选择区划"
                class="select-long"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河流岸别" prop="riverBankSide">
              <el-select v-model="createForm.riverBankSide" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in riverBankSideOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
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
            <div class="group-title">堤防位置</div>
            <TiandituGeoJsonPreview
              v-if="formReadonly"
              :geo-json="embankmentGeoJson"
              :active="createDialogVisible"
              :height="360"
              :center="embankmentMapCenter"
              :zoom="14"
              :label-text="createForm.embankmentName || ''"
              :highlight="true"
              empty-text="暂无位置信息"
            />
            <div v-else>
              <TiandituGeoJsonEditor
                v-model="embankmentGeoJson"
                :active="createDialogVisible"
                :height="420"
                :center="embankmentMapCenter"
                :zoom="14"
                :draw-modes="['POINT']"
              />
              <div class="map-tip">仅支持绘制点位；可直接输入经纬度，或在地图上绘制点位自动回填到经纬度。</div>
            </div>
          </el-col>

          <el-col :span="12">
            <el-form-item label="堤防跨界情况" prop="crossBoundaryStatus">
              <el-input v-model="createForm.crossBoundaryStatus" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堤防类型" prop="embankmentType">
              <el-select v-model="createForm.embankmentType" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in embankmentTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堤防形式" prop="embankmentForm">
              <el-select v-model="createForm.embankmentForm" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in embankmentFormOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堤防级别" prop="embankmentLevel">
              <el-select v-model="createForm.embankmentLevel" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in embankmentLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="group-title">工程参数</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="防洪标准" prop="floodStandard">
              <el-input v-model="createForm.floodStandard" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设计重现期(年)" prop="designReturnPeriod">
              <el-input-number v-model="createForm.designReturnPeriod" :min="0" :precision="0" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堤防长度(m)" prop="lengthM">
              <el-input-number v-model="createForm.lengthM" :min="0" :precision="2" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准长度(m)" prop="standardLengthM">
              <el-input-number v-model="createForm.standardLengthM" :min="0" :precision="2" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="高程系统" prop="elevationSystem">
              <el-input v-model="createForm.elevationSystem" placeholder="请输入" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="设计高潮位(m)" prop="designHighTide">
              <el-input v-model="createForm.designHighTide" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堤防最大高度(m)" prop="maxHeight">
              <el-input-number v-model="createForm.maxHeight" :min="0" :precision="2" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堤防最小高度(m)" prop="minHeight">
              <el-input-number v-model="createForm.minHeight" :min="0" :precision="2" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堤防最大宽度(m)" prop="maxWidth">
              <el-input-number v-model="createForm.maxWidth" :min="0" :precision="2" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堤防最小宽度(m)" prop="minWidth">
              <el-input-number v-model="createForm.minWidth" :min="0" :precision="2" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="堤顶高程(m)" prop="crestElevation">
              <el-input-number v-model="createForm.crestElevation" :precision="2" controls-position="right" class="select-long" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="起点" prop="startPoint">
              <el-input v-model="createForm.startPoint" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="终点" prop="endPoint">
              <el-input v-model="createForm.endPoint" placeholder="请输入" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="group-title">建设与管理</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工程任务" prop="projectTask">
              <el-input v-model="createForm.projectTask" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="终点所在位置" prop="endLocation">
              <el-input v-model="createForm.endLocation" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工程建设情况" prop="constructionStatus">
              <el-input v-model="createForm.constructionStatus" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归口管理部门" prop="managementDepartment">
              <el-input v-model="createForm.managementDepartment" placeholder="请输入" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="堤防图片" prop="embankmentImages">
              <UploadImgs v-model="createForm.embankmentImages" :limit="5" :file-size="5" :drag="false" :disabled="formReadonly">
                <template #tip>
                  <span>最多上传 5 张图片，每张不超过 5MB，支持 jpg/png/gif</span>
                </template>
              </UploadImgs>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remarks">
              <el-input v-model="createForm.remarks" type="textarea" :rows="3" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">{{ formReadonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!formReadonly" type="primary" :loading="createSubmitting" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="导入堤防" width="520px">
      <el-upload
        class="import-uploader"
        drag
        action="#"
        :auto-upload="false"
        :file-list="importFileList"
        :limit="1"
        accept=".xls,.xlsx"
        :on-change="handleImportFileChange"
        :on-remove="handleImportFileRemove"
        :on-exceed="handleImportExceed"
      >
        <Icon icon="ep:upload" class="mb-8px" />
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">
            <el-link :underline="false" type="primary" @click="downloadImportTemplate">下载导入模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="submitImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadFile, type UploadFiles } from 'element-plus'
import UploadImgs from '@/components/UploadFile/src/UploadImgs.vue'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import {
  createEmbankment,
  deleteEmbankment,
  getEmbankmentImportTemplate,
  getEmbankmentPage,
  getEmbankmentDetail,
  importEmbankmentExcel,
  exportEmbankmentExcel,
  updateEmbankment,
  type EmbankmentPageReqVO,
  type EmbankmentPageRespVO,
  type EmbankmentSaveReqVO
} from '@/api/gis/embankment'
import {
  getRiverChannelSimpleList,
  getRiverDict,
  getRiverSections,
  type DictDataItemRespVO,
  type RiverChannelSimpleRespVO,
  type RiverSectionSimpleRespVO
} from '@/api/gis/riverChannel'
import { getAreaTree } from '@/api/system/area'
import download from '@/utils/download'

const queryParams = reactive<EmbankmentPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  embankmentName: '',
  embankmentLevel: '',
  embankmentType: ''
})
const queryFormRef = ref<FormInstance>()

const tableData = ref<EmbankmentPageRespVO[]>([])
const total = ref(0)
const tableLoading = ref(false)

const embankmentLevelOptions = ref<DictDataItemRespVO[]>([])
const embankmentTypeOptions = ref<DictDataItemRespVO[]>([])
const embankmentFormOptions = ref<DictDataItemRespVO[]>([])
const riverBankSideOptions = ref<DictDataItemRespVO[]>([])

const formatNumber = (val?: number | null) => {
  if (val === undefined || val === null) return '-'
  const num = Number(val)
  if (Number.isNaN(num)) return '-'
  return num.toFixed(2)
}

const fetchTable = async () => {
  tableLoading.value = true
  try {
    const res = await getEmbankmentPage(queryParams)
    tableData.value = res?.list || []
    total.value = res?.total || 0
  } finally {
    tableLoading.value = false
  }
}

const loadDict = async () => {
  const [levels, types, forms, bankSides] = await Promise.all([
    getRiverDict('zd_dfjb'),
    getRiverDict('zd_dflx'),
    getRiverDict('zd_dfxs'),
    getRiverDict('zd_hlab')
  ])
  embankmentLevelOptions.value = levels || []
  embankmentTypeOptions.value = types || []
  embankmentFormOptions.value = forms || []
  riverBankSideOptions.value = bankSides || []
}

onMounted(async () => {
  await loadDict()
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

const createDialogVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref<FormInstance>()
const formMode = ref<'create' | 'edit' | 'view'>('create')
const dialogTitle = computed(() => {
  if (formMode.value === 'edit') return '编辑堤防'
  if (formMode.value === 'view') return '堤防详情'
  return '新增堤防'
})
const formReadonly = computed(() => formMode.value === 'view')

const embankmentGeoJson = ref('')
const DEFAULT_EMBANKMENT_CENTER = [32.272258, 119.184766] as [number, number]

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

const embankmentMapCenter = computed<[number, number]>(() => {
  const lon = normalizeNumber(createForm.longitude)
  const lat = normalizeNumber(createForm.latitude)
  if (Number.isFinite(lon) && Number.isFinite(lat)) {
    return [lat, lon]
  }
  return DEFAULT_EMBANKMENT_CENTER
})

type RiverTreeNode = {
  label: string
  value: string
  leaf?: boolean
  children?: RiverTreeNode[]
}

const riverTreeData = ref<RiverTreeNode[]>([])
const riverTreeLoading = ref(false)
const riverSectionCache = ref<Record<string, RiverSectionSimpleRespVO[]>>({})

const riverTreeProps = {
  label: 'label',
  value: 'value',
  children: 'children',
  isLeaf: 'leaf'
}

const loadRiverTree = async () => {
  riverTreeLoading.value = true
  try {
    const list = (await getRiverChannelSimpleList()) || []
    riverTreeData.value = (list as RiverChannelSimpleRespVO[]).map((item) => ({
      label: item.riverName || item.riverCode || '',
      value: `channel:${item.id}`,
      leaf: false
    }))
  } finally {
    riverTreeLoading.value = false
  }
}

const loadRiverTreeNode = async (node: any, resolve: (data: RiverTreeNode[]) => void) => {
  const value: string = node?.data?.value || ''
  if (!value.startsWith('channel:')) {
    resolve([])
    return
  }
  const channelId = value.split(':')[1]
  const channelName = node?.data?.label || ''
  if (riverSectionCache.value[channelId]) {
    const cached = riverSectionCache.value[channelId]
    resolve(
      (cached || []).map((item) => ({
        label: channelName ? `${channelName}/${item.sectionName}` : item.sectionName,
        value: `section:${channelId}:${item.id}`,
        leaf: true
      }))
    )
    return
  }
  const sections = (await getRiverSections(channelId)) || []
  riverSectionCache.value[channelId] = sections as RiverSectionSimpleRespVO[]
  resolve(
    (sections as RiverSectionSimpleRespVO[]).map((item) => ({
      label: channelName ? `${channelName}/${item.sectionName}` : item.sectionName,
      value: `section:${channelId}:${item.id}`,
      leaf: true
    }))
  )
}

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

type EmbankmentCreateForm = EmbankmentSaveReqVO & {
  relationValue: string
}

const createForm = reactive<EmbankmentCreateForm>({
  id: '',
  relationValue: '',
  embankmentName: '',
  embankmentCode: '',
  divisionCode: [],
  riverChannelId: '',
  riverSectionId: '',
  longitude: undefined,
  latitude: undefined,
  riverBankSide: '',
  crossBoundaryStatus: '',
  embankmentType: '',
  embankmentForm: '',
  embankmentLevel: '',
  floodStandard: '',
  designReturnPeriod: undefined,
  lengthM: undefined,
  standardLengthM: undefined,
  elevationSystem: '',
  designHighTide: '',
  maxHeight: undefined,
  minHeight: undefined,
  maxWidth: undefined,
  minWidth: undefined,
  crestElevation: undefined,
  startPoint: '',
  endPoint: '',
  projectTask: '',
  endLocation: '',
  constructionStatus: '',
  managementDepartment: '',
  embankmentImages: [],
  remarks: ''
})

const createRules: FormRules = {
  embankmentName: [{ required: true, message: '请输入堤防名称', trigger: 'blur' }]
}

const handleRelationValueChange = async (val: string) => {
  createForm.riverChannelId = ''
  createForm.riverSectionId = ''
  if (!val) {
    return
  }
  if (val.startsWith('section:')) {
    const parts = val.split(':')
    createForm.riverChannelId = parts[1] || ''
    createForm.riverSectionId = parts[2] || ''
    return
  }
  if (val.startsWith('channel:')) {
    const channelId = val.split(':')[1]
    // 选择河道后，如果存在河段，提示用户选择具体河段
    const sections = riverSectionCache.value[channelId] || ((await getRiverSections(channelId)) as RiverSectionSimpleRespVO[])
    riverSectionCache.value[channelId] = sections || []
    if (sections && sections.length > 0) {
      ElMessage.info('该河道下存在河段，请选择具体河段')
      createForm.relationValue = ''
      return
    }
    createForm.riverChannelId = channelId
  }
}

const resetCreateForm = () => {
  createForm.id = ''
  createForm.relationValue = ''
  createForm.embankmentName = ''
  createForm.embankmentCode = ''
  createForm.divisionCode = []
  createForm.riverChannelId = ''
  createForm.riverSectionId = ''
  createForm.longitude = undefined
  createForm.latitude = undefined
  createForm.riverBankSide = ''
  createForm.crossBoundaryStatus = ''
  createForm.embankmentType = ''
  createForm.embankmentForm = ''
  createForm.embankmentLevel = ''
  createForm.floodStandard = ''
  createForm.designReturnPeriod = undefined
  createForm.lengthM = undefined
  createForm.standardLengthM = undefined
  createForm.elevationSystem = ''
  createForm.designHighTide = ''
  createForm.maxHeight = undefined
  createForm.minHeight = undefined
  createForm.maxWidth = undefined
  createForm.minWidth = undefined
  createForm.crestElevation = undefined
  createForm.startPoint = ''
  createForm.endPoint = ''
  createForm.projectTask = ''
  createForm.endLocation = ''
  createForm.constructionStatus = ''
  createForm.managementDepartment = ''
  createForm.embankmentImages = []
  createForm.remarks = ''
  embankmentGeoJson.value = ''
}

const openCreateDialog = async () => {
  formMode.value = 'create'
  resetCreateForm()
  createDialogVisible.value = true
  await Promise.all([loadRiverTree(), loadAreaTree(), loadDict()])
}

const ensureChannelChildrenLoaded = async (channelId: string) => {
  const channelNode = riverTreeData.value.find((n) => n.value === `channel:${channelId}`)
  if (!channelNode) return
  const channelName = channelNode.label || ''
  const sections = riverSectionCache.value[channelId] || ((await getRiverSections(channelId)) as RiverSectionSimpleRespVO[])
  riverSectionCache.value[channelId] = sections || []
  if (!sections || sections.length === 0) {
    channelNode.children = []
    channelNode.leaf = true
    return
  }
  channelNode.leaf = false
  channelNode.children = (sections as RiverSectionSimpleRespVO[]).map((item) => ({
    label: channelName ? `${channelName}/${item.sectionName}` : item.sectionName,
    value: `section:${channelId}:${item.id}`,
    leaf: true
  }))
}

const fillFormByDetail = async (id: string | number) => {
  const data: any = await getEmbankmentDetail(id)
  resetCreateForm()

  createForm.id = String(data.id ?? id)
  createForm.embankmentName = data.embankmentName || ''
  createForm.embankmentCode = data.embankmentCode || ''
  createForm.divisionCode = Array.isArray(data.divisionCode)
    ? data.divisionCode.map((v: any) => String(v))
    : data.divisionCode
      ? [String(data.divisionCode)]
      : []
  createForm.longitude = data.longitude
  createForm.latitude = data.latitude
  createForm.riverBankSide = data.riverBankSide || ''
  createForm.crossBoundaryStatus = data.crossBoundaryStatus || ''
  createForm.embankmentType = data.embankmentType || ''
  createForm.embankmentForm = data.embankmentForm || ''
  createForm.embankmentLevel = data.embankmentLevel || ''
  createForm.floodStandard = data.floodStandard || ''
  createForm.designReturnPeriod = data.designReturnPeriod
  createForm.lengthM = data.lengthM
  createForm.standardLengthM = data.standardLengthM
  createForm.elevationSystem = data.elevationSystem || ''
  createForm.designHighTide = data.designHighTide
  createForm.maxHeight = data.maxHeight
  createForm.minHeight = data.minHeight
  createForm.maxWidth = data.maxWidth
  createForm.minWidth = data.minWidth
  createForm.crestElevation = data.crestElevation
  createForm.startPoint = data.startPoint || ''
  createForm.endPoint = data.endPoint || ''
  createForm.projectTask = data.projectTask || ''
  createForm.endLocation = data.endLocation || ''
  createForm.constructionStatus = data.constructionStatus || ''
  createForm.managementDepartment = data.managementDepartment || ''
  createForm.embankmentImages = data.embankmentImages || []
  createForm.remarks = data.remarks || ''

  createForm.riverChannelId = data.riverChannelId ? String(data.riverChannelId) : ''
  createForm.riverSectionId = data.riverSectionId ? String(data.riverSectionId) : ''
  if (createForm.riverChannelId && createForm.riverSectionId) {
    await ensureChannelChildrenLoaded(createForm.riverChannelId)
    createForm.relationValue = `section:${createForm.riverChannelId}:${createForm.riverSectionId}`
  } else if (createForm.riverChannelId) {
    createForm.relationValue = `channel:${createForm.riverChannelId}`
  } else {
    createForm.relationValue = ''
  }

  embankmentGeoJson.value = buildPointGeoJson(createForm.longitude, createForm.latitude)
}

const openEditDialog = async (id: string | number) => {
  formMode.value = 'edit'
  createDialogVisible.value = true
  await Promise.all([loadRiverTree(), loadAreaTree(), loadDict()])
  await fillFormByDetail(id)
}

const openDetailDialog = async (id: string | number) => {
  formMode.value = 'view'
  createDialogVisible.value = true
  await Promise.all([loadRiverTree(), loadAreaTree(), loadDict()])
  await fillFormByDetail(id)
}

let syncingFromGeoJson = false
let syncingFromLonLat = false

watch(
  () => [createForm.longitude, createForm.latitude],
  ([lon, lat]) => {
    if (syncingFromGeoJson) return
    const next = buildPointGeoJson(lon, lat)
    if (next === embankmentGeoJson.value) return
    syncingFromLonLat = true
    embankmentGeoJson.value = next
    syncingFromLonLat = false
  }
)

watch(
  () => embankmentGeoJson.value,
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

const submitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate()
  createSubmitting.value = true
  try {
    const normalizeId = (val?: string) => (val && val.trim() ? val : undefined)
    const normalizeIds = (list?: string[]) => {
      const cleaned = (list || []).map((v) => String(v ?? '').trim()).filter((v) => !!v)
      return cleaned.length ? cleaned : undefined
    }

    // 统一从 relationValue 推导河道/河段，避免编辑时残留 riverSectionId 导致未置空
    const resolveRelation = (val?: string) => {
      const relation = (val || '').trim()
      if (!relation) {
        return { riverChannelId: undefined, riverSectionId: undefined }
      }
      if (relation.startsWith('section:')) {
        const parts = relation.split(':')
        const channelId = parts[1] || ''
        const sectionId = parts[2] || ''
        return { riverChannelId: normalizeId(channelId), riverSectionId: normalizeId(sectionId) }
      }
      if (relation.startsWith('channel:')) {
        const channelId = relation.split(':')[1] || ''
        return { riverChannelId: normalizeId(channelId), riverSectionId: undefined }
      }
      return { riverChannelId: undefined, riverSectionId: undefined }
    }

    const relationResolved = resolveRelation(createForm.relationValue)
    const { relationValue: _relationValue, ...rest } = createForm as any
    const payload: EmbankmentSaveReqVO = {
      ...rest,
      id: normalizeId(createForm.id),
      divisionCode: normalizeIds(createForm.divisionCode),
      riverChannelId: relationResolved.riverChannelId,
      riverSectionId: relationResolved.riverSectionId
    }
    if (formMode.value === 'edit') {
      await updateEmbankment(payload)
      ElMessage.success('保存成功')
    } else {
      await createEmbankment(payload)
      ElMessage.success('新增成功')
    }
    createDialogVisible.value = false
    fetchTable()
  } finally {
    createSubmitting.value = false
  }
}

const handleExport = async () => {
  try {
    const data = await exportEmbankmentExcel(queryParams)
    download.excel(data, '堤防信息.xls')
  } catch {
    // 下载失败时，错误提示由全局拦截器处理
  }
}

const importVisible = ref(false)
const importLoading = ref(false)
const importFileList = ref<UploadFile[]>([])
const importRawFile = ref<File>()

const openImportDialog = () => {
  importVisible.value = true
  importFileList.value = []
  importRawFile.value = undefined
}

const handleImportFileChange = (file: UploadFile, fileList: UploadFiles) => {
  importFileList.value = fileList.slice(-1)
  importRawFile.value = file.raw
}

const handleImportFileRemove = () => {
  importRawFile.value = undefined
}

const handleImportExceed = () => {
  ElMessage.warning('最多只能上传一个文件')
}

const downloadImportTemplate = async () => {
  const data = await getEmbankmentImportTemplate()
  download.excel(data, '堤防导入模版.xlsx')
}

const submitImport = async () => {
  if (!importRawFile.value) {
    ElMessage.warning('请先上传导入文件')
    return
  }
  importLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', importRawFile.value)
    const result: any = await importEmbankmentExcel(formData)
    const errors = Array.isArray(result?.errors) ? result.errors : []
    const errorText = errors.length ? `<br/>${errors.map((item: string) => `- ${item}`).join('<br/>')}` : ''
    await ElMessageBox.alert(
      `导入完成：总计 ${result?.totalCount || 0} 条，成功 ${result?.successCount || 0} 条，失败 ${result?.failureCount || 0} 条${errorText}`,
      '导入结果',
      { dangerouslyUseHTMLString: true }
    )
    importVisible.value = false
    await fetchTable()
  } finally {
    importLoading.value = false
  }
}

const handleDetail = (row: EmbankmentPageRespVO) => {
  if (!row?.id) return
  openDetailDialog(row.id)
}

const handleEdit = (row: EmbankmentPageRespVO) => {
  if (!row?.id) return
  openEditDialog(row.id)
}

const handleDelete = async (row: EmbankmentPageRespVO) => {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm(`确认删除堤防「${row.embankmentName || row.embankmentCode}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteEmbankment(row.id)
  ElMessage.success('删除成功')
  fetchTable()
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
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}
.import-uploader :deep(.el-upload-dragger) {
  width: 100%;
}

.import-uploader :deep(.el-upload__tip) {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  color: #64748b;
}
</style>
