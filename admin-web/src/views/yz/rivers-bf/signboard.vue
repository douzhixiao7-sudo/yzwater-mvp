<template>
  <div class="river-page">
      <ContentWrap class="query-wrap">
        <el-form
          class="query-form -mb-15px"
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          label-width="90px"
        >
          <el-form-item label="公示牌名称" prop="signboardName">
            <el-input
              v-model="queryParams.signboardName"
              placeholder="请输入公示牌名称关键词"
              clearable
              @keyup.enter="handleSearch"
              class="!w-240px"
            />
          </el-form-item>
          <el-form-item label="公示牌代码" prop="signboardCode">
            <el-input
              v-model="queryParams.signboardCode"
              placeholder="请输入公示牌代码关键词"
              clearable
              @keyup.enter="handleSearch"
              class="!w-240px"
            />
          </el-form-item>
          <el-form-item label="关联设施" prop="riverKeyword">
            <el-input
              v-model="queryParams.riverKeyword"
              placeholder="请输入关联设施关键词"
              clearable
              @keyup.enter="handleSearch"
              class="!w-240px"
            />
          </el-form-item>
          <el-form-item label="公示牌等级" prop="signboardLevel">
            <el-select
              v-model="queryParams.signboardLevel"
              multiple
              collapse-tags
              collapse-tags-tooltip
              :max-collapse-tags="2"
              placeholder="请选择"
              clearable
              class="!w-240px"
            >
              <el-option v-for="item in signboardLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="大屏展示" prop="isScreenDisplay">
            <el-select v-model="queryParams.isScreenDisplay" placeholder="请选择" clearable class="!w-240px">
              <el-option label="展示" :value="1" />
              <el-option label="不展示" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="handleSearch"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-button @click="handleReset"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
            <el-button type="primary" plain @click="openCreateDialog">
              <Icon icon="ep:plus" class="mr-5px" /> 新增
            </el-button>
            <el-button type="warning" plain @click="openImportDialog">
              <Icon icon="ep:upload" class="mr-5px" /> 导入
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
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="河道/水库" align="center" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              {{ formatFacilityName(row) }}
            </template>
          </el-table-column>
          <el-table-column label="公示牌代码" align="center" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.signboardCode || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="公示牌等级" align="center" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.signboardLevelLabel || row.signboardLevel || '-' }}
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

    <el-dialog v-model="importVisible" title="导入公示牌" width="520px">
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
            <div>仅允许导入 xls、xlsx 格式文件。</div>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="submitImport">开始导入</el-button>
      </template>
    </el-dialog>

    <el-dialog class="facility-dialog" v-model="createDialogVisible" :title="dialogTitle" width="820px" destroy-on-close :close-on-click-modal="false">
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="115px"
        label-position="left"
        :disabled="formReadonly"
      >
        <el-row :gutter="12">
          <el-col :span="24">
            <div class="group-title">基础信息</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公示牌代码" prop="signboardCode">
              <el-input v-model="createForm.signboardCode" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公示牌名称" prop="signboardName">
              <el-input v-model="createForm.signboardName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公示牌类型" prop="signboardType">
              <el-select v-model="createForm.signboardType" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in signboardTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公示牌等级" prop="signboardLevel">
              <el-select v-model="createForm.signboardLevel" placeholder="请选择" clearable class="select-long">
                <el-option v-for="item in signboardLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否大屏展示" prop="isScreenDisplay">
              <el-radio-group v-model="createForm.isScreenDisplay">
                <el-radio :value="1">展示</el-radio>
                <el-radio :value="0">不展示</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="关联河道/河段" prop="relationValue">
              <el-tree-select
                v-model="createForm.relationValue"
                :data="riverTreeData"
                :props="riverTreeProps"
                node-key="value"
                check-strictly
                filterable
                :filter-node-method="filterRiverTreeNode"
                clearable
                :loading="riverTreeLoading"
                :default-expanded-keys="riverTreeExpandedKeys"
                placeholder="请选择关联河道/河段"
                class="select-long"
                @change="handleRelationValueChange"
              >
                <template #default="{ data }">
                  <div class="river-reference-node">
                    <span class="river-reference-box" :class="{ 'is-checked': createForm.relationValue === data.value }"></span>
                    <span class="river-reference-label">{{ data.label }}</span>
                  </div>
                </template>
              </el-tree-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="关联水库" prop="waterReservoirId">
              <el-select
                v-model="createForm.waterReservoirId"
                placeholder="请选择"
                clearable
                filterable
                class="select-long"
                @change="handleReservoirChange"
              >
                <el-option v-for="item in reservoirOptions" :key="item.id" :label="item.reservoirName" :value="String(item.id)" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="具体位置" prop="specificLocation">
              <el-input v-model="createForm.specificLocation" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="行政区划" prop="adminRegion">
              <el-tree-select
                v-model="createForm.adminRegion"
                :data="areaTreeData"
                :props="areaTreeProps"
                node-key="value"
                check-strictly
                check-on-click-node
                filterable
                :filter-node-method="filterAreaTreeNode"
                clearable
                placeholder="请选择（可搜索）"
                class="select-long"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维护单位" prop="maintenanceUnit">
              <el-select
                v-model="createForm.maintenanceUnit"
                multiple
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="6"
                clearable
                placeholder="请选择（可多选）"
                class="select-long"
              >
                <el-option v-for="item in maintenanceUnitOptions" :key="item.value" :label="item.label" :value="item.value" />
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
            <div class="group-title">公示牌位置</div>
            <TiandituGeoJsonPreview
              v-if="formReadonly"
              :geo-json="signboardGeoJson"
              :active="createDialogVisible"
              :height="360"
              :center="signboardMapCenter"
              :zoom="14"
              :label-text="createForm.signboardName || ''"
              :highlight="true"
              empty-text="暂无位置信息"
            />
            <div v-else>
              <TiandituGeoJsonEditor
                v-model="signboardGeoJson"
                :active="createDialogVisible"
                :height="420"
                :center="signboardMapCenter"
                :zoom="14"
                :draw-modes="['POINT']"
              />
              <div class="map-tip">仅支持绘制点位；可直接输入经纬度，或在地图上绘制点位自动回填到经纬度。</div>
            </div>
          </el-col>
          <el-col :span="24">
            <div class="group-title">管理与附件</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="责任人" prop="responsiblePerson">
              <el-input v-model="createForm.responsiblePerson" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="管理单位" prop="managementUnit">
              <el-select
                v-model="createForm.managementUnit"
                multiple
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="6"
                clearable
                placeholder="请选择（可多选）"
                class="select-long"
              >
                <el-option v-for="item in managementUnitOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权属单位" prop="ownershipUnit">
              <el-select
                v-model="createForm.ownershipUnit"
                multiple
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="6"
                clearable
                placeholder="请选择（可多选）"
                class="select-long"
              >
                <el-option v-for="item in ownershipUnitOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col v-if="formReadonly" :span="24">
            <div class="qr-wrap">
              <div class="qr-item">
                <div class="qr-label">河道信息码</div>
                <div class="qr-body">
                  <Qrcode v-if="riverInfoQrUrl" tag="img" :text="riverInfoQrUrl" :width="160" />
                  <div v-else class="qr-empty">-</div>
                </div>
              </div>
              <div class="qr-item">
                <div class="qr-label">公众反馈码</div>
                <div class="qr-body">
                  <Qrcode v-if="riverIssueQrUrl" tag="img" :text="riverIssueQrUrl" :width="160" />
                  <div v-else class="qr-empty">-</div>
                </div>
              </div>
              <div class="qr-item">
                <div class="qr-label">移动端后台</div>
                <div class="qr-body">
                  <div class="qr-static-frame">
                    <img :src="h5AdminLoginQrImg" alt="扫码进入移动端后台" class="qr-static-img" />
                  </div>
                </div>
              </div>
            </div>
          </el-col>
          <el-col :span="24">
            <el-form-item label="公示牌图片" prop="signboardImages">
              <UploadImgs v-model="createForm.signboardImages" :limit="5" :file-size="5" :drag="false" :disabled="formReadonly">
                <template #tip>
                  <span>最多上传 5 张图片，每张不超过 5MB，支持 jpg/png/gif</span>
                </template>
              </UploadImgs>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="公示牌内容" prop="content">
              <el-input v-model="createForm.content" type="textarea" :rows="3" placeholder="请输入" />
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
        <el-space>
          <el-button @click="createDialogVisible = false">{{ formReadonly ? '关闭' : '取消' }}</el-button>
          <el-button v-if="!formReadonly" type="primary" :loading="createSubmitting" @click="submitCreate">保存</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadFile, type UploadFiles } from 'element-plus'
import download from '@/utils/download'
import UploadImgs from '@/components/UploadFile/src/UploadImgs.vue'
import { Qrcode } from '@/components/Qrcode'
import h5AdminLoginQrImg from '@/assets/imgs/h5_adminLogin.png'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import {
  createSignboard,
  deleteSignboard,
  exportSignboardExcel,
  getSignboardImportTemplate,
  getSignboardDetail,
  getSignboardPage,
  importSignboardExcel,
  updateSignboard,
  type SignboardPageReqVO,
  type SignboardPageRespVO,
  type SignboardSaveReqVO
} from '@/api/gis/signboardBf'
import {
  getRiverChannelSimpleList,
  getRiverSectionWithChannelSimpleList,
  getRiverDict,
  type DictDataItemRespVO,
  type RiverChannelSimpleRespVO,
  type RiverSectionWithChannelSimpleRespVO
} from '@/api/gis/riverChannelBf'
import { getAreaTree } from '@/api/system/area'
import { getConfigKey } from '@/api/infra/config'
import { getReservoirSimpleList, type ReservoirSimpleRespVO } from '@/api/gis/reservoirBf'

const queryParams = reactive<SignboardPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  signboardName: '',
  signboardCode: '',
  riverKeyword: '',
  signboardLevel: [],
  isScreenDisplay: undefined
})
const queryFormRef = ref<FormInstance>()

const tableData = ref<SignboardPageRespVO[]>([])
const total = ref(0)
const tableLoading = ref(false)
const areaNameMap = reactive<Record<string, string>>({})

const fetchTable = async () => {
  tableLoading.value = true
  try {
    const res = await getSignboardPage(queryParams)
    tableData.value = res?.list || []
    total.value = res?.total || 0
  } finally {
    tableLoading.value = false
  }
}

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

const normalizeReferenceType = (value?: string) => String(value || '').trim().toLowerCase()

const formatFacilityName = (row: SignboardPageRespVO) => {
  const referenceType = normalizeReferenceType(row?.referenceType)
  const rawReferenceName = String((row as any)?.referenceName || (row as any)?.facilityName || '').trim()
  const reservoirName = String(row?.waterReservoirName || '').trim()
  const channelName = String(row?.riverChannelName || row?.riverName || '').trim()
  const sectionName = String(row?.riverSectionName || '').trim()
  if (referenceType === 'reservoir') return reservoirName || '-'
  if (referenceType === 'river') return channelName || sectionName || '-'
  if (referenceType === 'river_section') return channelName && sectionName ? `${channelName}/${sectionName}` : (sectionName || channelName || '-')

  if (rawReferenceName) return rawReferenceName
  if (reservoirName) return reservoirName
  if (sectionName) return channelName ? `${channelName}/${sectionName}` : sectionName
  return channelName || '-'
}

const handleExport = async () => {
  try {
    const data = await exportSignboardExcel(queryParams)
    download.excel(data, '公示牌信息.xls')
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
  const data = await getSignboardImportTemplate()
  download.excel(data, '公示牌导入模版.xlsx')
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
    const result: any = await importSignboardExcel(formData)
    const errors = Array.isArray(result?.errors) ? result.errors : []
    const errorText = errors.length ? `<br/>${errors.map((item: string) => `- ${item}`).join('<br/>')}` : ''
    await ElMessageBox.alert(
      `导入完成：总计 ${result?.totalCount || 0} 条，成功 ${result?.successCount || 0} 条，跳过 ${result?.skipCount || 0} 条，失败 ${result?.failureCount || 0} 条${errorText}`,
      '导入结果',
      { dangerouslyUseHTMLString: true }
    )
    importVisible.value = false
    await fetchTable()
  } finally {
    importLoading.value = false
  }
}

const handleDetail = (row: SignboardPageRespVO) => {
  if (!row?.id) return
  openDetailDialog(row.id)
}

const handleEdit = (row: SignboardPageRespVO) => {
  if (!row?.id) return
  openEditDialog(row.id)
}

const handleDelete = async (row: SignboardPageRespVO) => {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm(`确认删除公示牌「${row.signboardName || row.signboardCode}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteSignboard(row.id)
  ElMessage.success('删除成功')
  fetchTable()
}

const createDialogVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref<FormInstance>()
const formMode = ref<'create' | 'edit' | 'view'>('create')
const dialogTitle = computed(() => {
  if (formMode.value === 'edit') return '编辑公示牌'
  if (formMode.value === 'view') return '公示牌详情'
  return '新增公示牌'
})
const formReadonly = computed(() => formMode.value === 'view')

const signboardGeoJson = ref('')
const DEFAULT_SIGNBOARD_CENTER = [32.272258, 119.184766] as [number, number]

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

const signboardMapCenter = computed<[number, number]>(() => {
  const lon = normalizeNumber(createForm.longitude)
  const lat = normalizeNumber(createForm.latitude)
  if (Number.isFinite(lon) && Number.isFinite(lat)) {
    return [lat, lon]
  }
  return DEFAULT_SIGNBOARD_CENTER
})

type SignboardCreateForm = Omit<
  SignboardSaveReqVO,
  'maintenanceUnit' | 'managementUnit' | 'ownershipUnit' | 'signboardImages' | 'signboardLevel'
> & {
  relationValue: string
  maintenanceUnit: string[]
  managementUnit: string[]
  ownershipUnit: string[]
  signboardImages: string[]
  signboardLevel: string
}

const createForm = reactive<SignboardCreateForm>({
  id: '',
  relationValue: '',
  signboardCode: '',
  signboardName: '',
  signboardType: '',
  signboardLevel: '',
  isScreenDisplay: 0,
  riverChannelId: '',
  riverSectionId: '',
  waterReservoirId: '',
  waterReservoirName: '',
  qrCode: '',
  longitude: undefined,
  latitude: undefined,
  specificLocation: '',
  adminRegion: '',
  maintenanceUnit: [],
  responsiblePerson: '',
  managementUnit: [],
  ownershipUnit: [],
  signboardImages: [],
  content: '',
  remarks: ''
})

const riverInfoQrCodeAddress = ref('')
const riverIssueQrCodeAddress = ref('')

const unwrapConfigValue = (res: any) => {
  return (res && (res.data ?? res)) as string
}

const joinQrUrl = (base: string, qrCode: string) => {
  const baseClean = (base || '').trim()
  const codeClean = (qrCode || '').trim()
  if (!baseClean || !codeClean) return ''
  let url = baseClean
  if (url.includes('?')) {
    if (!url.endsWith('?') && !url.endsWith('&')) {
      url += '&'
    }
  } else {
    url += '?'
  }
  return `${url}qrcode=${encodeURIComponent(codeClean)}`
}

const riverInfoQrUrl = computed(() => joinQrUrl(riverInfoQrCodeAddress.value, createForm.qrCode || ''))
const riverIssueQrUrl = computed(() => joinQrUrl(riverIssueQrCodeAddress.value, createForm.qrCode || ''))

const loadQrCodeAddress = async () => {
  if (riverInfoQrCodeAddress.value && riverIssueQrCodeAddress.value) return
  const [riverInfo, riverIssue] = await Promise.all([
    getConfigKey('river_info_qr_code_address'),
    getConfigKey('river_issue_qr_code_address')
  ])
  riverInfoQrCodeAddress.value = unwrapConfigValue(riverInfo) || ''
  riverIssueQrCodeAddress.value = unwrapConfigValue(riverIssue) || ''
}

type RiverTreeNode = {
  label: string
  value: string
  sortTime?: number
  leaf?: boolean
  children?: RiverTreeNode[]
}

const createRules: FormRules = {
  signboardCode: [{ required: true, message: '请输入公示牌代码', trigger: 'blur' }],
  signboardName: [{ required: true, message: '请输入公示牌名称', trigger: 'blur' }],
  signboardType: [{ required: true, message: '请选择公示牌类型', trigger: 'change' }],
  signboardLevel: [{ required: true, message: '请选择公示牌等级', trigger: 'change' }],
  isScreenDisplay: [{ required: true, message: '请选择是否大屏展示', trigger: 'change' }],
  relationValue: []
}

const signboardTypeOptions = ref<DictDataItemRespVO[]>([])
const signboardLevelOptions = ref<DictDataItemRespVO[]>([])
const maintenanceUnitOptions = ref<DictDataItemRespVO[]>([])
const managementUnitOptions = ref<DictDataItemRespVO[]>([])
const ownershipUnitOptions = ref<DictDataItemRespVO[]>([])
const reservoirOptions = ref<ReservoirSimpleRespVO[]>([])

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

const filterAreaTreeNode = (keyword: string, data: any) => {
  const k = String(keyword || '').trim()
  if (!k) return true
  return String(data?.label || '').includes(k)
}

const loadSignboardTypeOptions = async () => {
  const [signboardTypesResult, signboardLevelsResult] = await Promise.allSettled([
    getRiverDict('zd_gsplx'),
    getRiverDict('zd_hljb')
  ])
  signboardTypeOptions.value = signboardTypesResult.status === 'fulfilled' ? signboardTypesResult.value || [] : []
  signboardLevelOptions.value = signboardLevelsResult.status === 'fulfilled' ? signboardLevelsResult.value || [] : []
}

const loadUnitOptions = async () => {
  const [whdw, gldw, qsdw] = await Promise.all([getRiverDict('zd_whdw'), getRiverDict('zd_gldw'), getRiverDict('zd_qsdw')])
  maintenanceUnitOptions.value = whdw || []
  managementUnitOptions.value = gldw || []
  ownershipUnitOptions.value = qsdw || []
}

const ensureCurrentReservoirOption = () => {
  const currentId = (createForm.waterReservoirId || '').trim()
  const currentName = (createForm.waterReservoirName || '').trim()
  if (!currentId || !currentName) return
  const exists = reservoirOptions.value.some((item) => String(item.id) === currentId)
  if (exists) return
  reservoirOptions.value = [{ id: currentId, reservoirName: currentName }, ...reservoirOptions.value]
}

const loadReservoirOptions = async () => {
  const list = (await getReservoirSimpleList()) as any
  reservoirOptions.value = (list || []).map((item: any) => ({
    id: String(item.id),
    reservoirName: item.reservoirName || ''
  }))
  ensureCurrentReservoirOption()
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
  Object.keys(areaNameMap).forEach((key) => delete areaNameMap[key])
  const flatten = (nodes: TreeSelectNode[]) => {
    ;(nodes || []).forEach((n) => {
      areaNameMap[String(n.value)] = n.label
      if (n.children && n.children.length) {
        flatten(n.children)
      }
    })
  }
  flatten(areaTreeData.value)
}

const riverTreeData = ref<RiverTreeNode[]>([])
const riverTreeLoading = ref(false)

const riverTreeProps = {
  label: 'label',
  value: 'value',
  children: 'children',
  isLeaf: 'leaf'
}

const riverTreeExpandedKeys = computed<string[]>(() =>
  (riverTreeData.value || []).filter((item) => (item.children || []).length > 0).map((item) => item.value)
)

const parseSortTime = (value?: string) => {
  const ms = Date.parse(String(value || ''))
  return Number.isFinite(ms) ? ms : 0
}

const filterRiverTreeNode = (keyword: string, data: RiverTreeNode, node: any) => {
  const k = String(keyword || '').trim()
  if (!k) return true
  const label = String(data?.label || '')
  if (label.includes(k)) return true

  const children = Array.isArray(data?.children) ? data.children : []
  if (children.some((item) => String(item?.label || '').includes(k))) {
    return true
  }

  const parentLabel = String(node?.parent?.data?.label || '')
  if (parentLabel.includes(k)) {
    return true
  }
  return false
}

const loadRiverTree = async () => {
  riverTreeLoading.value = true
  try {
    const [riverList, sectionList] = await Promise.all([
      getRiverChannelSimpleList() as unknown as Promise<RiverChannelSimpleRespVO[]>,
      getRiverSectionWithChannelSimpleList() as unknown as Promise<RiverSectionWithChannelSimpleRespVO[]>
    ])
    const roots = new Map<string, RiverTreeNode>()
    for (const river of Array.isArray(riverList) ? riverList : []) {
      if (!river?.id) continue
      const riverId = String(river.id)
      roots.set(riverId, {
        label: river.riverName || river.riverCode || riverId,
        value: `channel:${riverId}`,
        sortTime: parseSortTime(river.updateTime),
        leaf: false,
        children: []
      })
    }
    for (const section of Array.isArray(sectionList) ? sectionList : []) {
      if (!section?.id) continue
      const riverId = String(section.riverChannelId || '')
      if (!riverId) continue
      let root = roots.get(riverId)
      if (!root) {
        root = {
          label: section.riverName || `河道-${riverId}`,
          value: `channel:${riverId}`,
          sortTime: 0,
          leaf: false,
          children: []
        }
        roots.set(riverId, root)
      }
      root.children = root.children || []
      root.children.push({
        label: section.sectionName || String(section.id),
        value: `section:${riverId}:${section.id}`,
        sortTime: parseSortTime(section.updateTime),
        leaf: true
      })
    }
    const sorted = Array.from(roots.values())
      .map((root) => ({
        ...root,
        leaf: !(root.children && root.children.length),
        children: (root.children || []).sort((a, b) => {
          if ((b.sortTime || 0) !== (a.sortTime || 0)) return (b.sortTime || 0) - (a.sortTime || 0)
          return String(a.label).localeCompare(String(b.label), 'zh-CN')
        })
      }))
      .sort((a, b) => {
        if ((b.sortTime || 0) !== (a.sortTime || 0)) return (b.sortTime || 0) - (a.sortTime || 0)
        return String(a.label).localeCompare(String(b.label), 'zh-CN')
      })
    riverTreeData.value = sorted
  } finally {
    riverTreeLoading.value = false
  }
}

const handleReservoirChange = (val: string) => {
  // 选择水库时，清空河道/河段关联，避免同时关联导致 reference 字段歧义
  const next = (val || '').trim()
  if (!next) {
    return
  }
  createForm.relationValue = ''
  createForm.riverChannelId = ''
  createForm.riverSectionId = ''
}

const handleRelationValueChange = async (val: string) => {
  createForm.riverChannelId = ''
  createForm.riverSectionId = ''
  if (!val) {
    return
  }
  // 选择河道/河段时，清空水库关联，避免同时关联导致 reference 字段歧义
  createForm.waterReservoirId = ''
  createForm.waterReservoirName = ''
  if (val.startsWith('section:')) {
    const parts = val.split(':')
    createForm.riverChannelId = parts[1] || ''
    createForm.riverSectionId = parts[2] || ''
    return
  }
  if (val.startsWith('channel:')) {
    const channelId = val.split(':')[1]
    createForm.riverChannelId = channelId
  }
}

const resetCreateForm = () => {
  createForm.id = ''
  createForm.relationValue = ''
  createForm.signboardCode = ''
  createForm.signboardName = ''
  createForm.signboardType = ''
  createForm.signboardLevel = ''
  createForm.isScreenDisplay = 0
  createForm.riverChannelId = ''
  createForm.riverSectionId = ''
  createForm.waterReservoirId = ''
  createForm.waterReservoirName = ''
  createForm.qrCode = ''
  createForm.longitude = undefined
  createForm.latitude = undefined
  createForm.specificLocation = ''
  createForm.adminRegion = ''
  createForm.maintenanceUnit = []
  createForm.responsiblePerson = ''
  createForm.managementUnit = []
  createForm.ownershipUnit = []
  createForm.signboardImages = []
  createForm.content = ''
  createForm.remarks = ''
  signboardGeoJson.value = ''
}

const openCreateDialog = async () => {
  formMode.value = 'create'
  resetCreateForm()
  createDialogVisible.value = true
  await Promise.all([loadRiverTree(), loadSignboardTypeOptions(), loadUnitOptions(), loadAreaTree(), loadReservoirOptions()])
}

const fillFormByDetail = async (id: string | number) => {
  const data: any = await getSignboardDetail(id)
  resetCreateForm()

  createForm.id = String(data.id ?? id)
  createForm.signboardCode = data.signboardCode || ''
  createForm.signboardName = data.signboardName || ''
  createForm.signboardType = data.signboardType || ''
  createForm.signboardLevel = data.signboardLevel || ''
  createForm.isScreenDisplay = String(data.isScreenDisplay) === '1' ? 1 : 0
  createForm.qrCode = data.qrCode || ''
  createForm.longitude = data.longitude
  createForm.latitude = data.latitude
  createForm.specificLocation = data.specificLocation || ''
  createForm.adminRegion = data.adminRegion ? String(data.adminRegion) : ''
  createForm.maintenanceUnit = data.maintenanceUnit || []
  createForm.responsiblePerson = data.responsiblePerson || ''
  createForm.managementUnit = data.managementUnit || []
  createForm.ownershipUnit = data.ownershipUnit || []
  createForm.signboardImages = data.signboardImages || []
  createForm.content = data.content || ''
  createForm.remarks = data.remarks || ''

  createForm.riverChannelId = data.riverChannelId ? String(data.riverChannelId) : ''
  createForm.riverSectionId = data.riverSectionId ? String(data.riverSectionId) : ''
  createForm.waterReservoirId = data.waterReservoirId ? String(data.waterReservoirId) : ''
  createForm.waterReservoirName = data.waterReservoirName || ''
  ensureCurrentReservoirOption()
  if (createForm.riverChannelId && createForm.riverSectionId) {
    createForm.relationValue = `section:${createForm.riverChannelId}:${createForm.riverSectionId}`
  } else if (createForm.riverChannelId) {
    createForm.relationValue = `channel:${createForm.riverChannelId}`
  } else {
    createForm.relationValue = ''
  }

  signboardGeoJson.value = buildPointGeoJson(createForm.longitude, createForm.latitude)
}

const openEditDialog = async (id: string | number) => {
  formMode.value = 'edit'
  createDialogVisible.value = true
  await Promise.all([loadRiverTree(), loadSignboardTypeOptions(), loadUnitOptions(), loadAreaTree(), loadReservoirOptions()])
  await fillFormByDetail(id)
}

const openDetailDialog = async (id: string | number) => {
  formMode.value = 'view'
  createDialogVisible.value = true
  await Promise.all([
    loadRiverTree(),
    loadSignboardTypeOptions(),
    loadUnitOptions(),
    loadAreaTree(),
    loadReservoirOptions(),
    loadQrCodeAddress()
  ])
  await fillFormByDetail(id)
}

let syncingFromGeoJson = false
let syncingFromLonLat = false

watch(
  () => [createForm.longitude, createForm.latitude],
  ([lon, lat]) => {
    if (syncingFromGeoJson) return
    const next = buildPointGeoJson(lon, lat)
    if (next === signboardGeoJson.value) return
    syncingFromLonLat = true
    signboardGeoJson.value = next
    syncingFromLonLat = false
  }
)

watch(
  () => signboardGeoJson.value,
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
    const reservoirId = normalizeId(createForm.waterReservoirId)
    const finalRiverChannelId = reservoirId ? undefined : relationResolved.riverChannelId
    const finalRiverSectionId = reservoirId ? undefined : relationResolved.riverSectionId
    const finalReservoirId = reservoirId

    let referenceType: string | undefined
    let referenceId: string | undefined
    if (finalReservoirId) {
      referenceType = 'reservoir'
      referenceId = finalReservoirId
    } else if (finalRiverSectionId) {
      referenceType = 'river_section'
      referenceId = finalRiverSectionId
    } else if (finalRiverChannelId) {
      referenceType = 'river'
      referenceId = finalRiverChannelId
    }

    const { relationValue: _relationValue, ...rest } = createForm as any
    const payload: SignboardSaveReqVO = {
      ...rest,
      signboardLevel: createForm.signboardLevel || undefined,
      isScreenDisplay: String(createForm.isScreenDisplay) === '1' ? 1 : 0,
      waterReservoirId: finalReservoirId,
      waterReservoirName: undefined,
      riverChannelId: finalRiverChannelId,
      riverSectionId: finalRiverSectionId,
      referenceType,
      referenceId
    }
    if (formMode.value === 'edit') {
      await updateSignboard(payload)
      ElMessage.success('保存成功')
    } else {
      await createSignboard(payload)
      ElMessage.success('新增成功')
    }
    createDialogVisible.value = false
    fetchTable()
  } finally {
    createSubmitting.value = false
  }
}

onMounted(() => {
  loadSignboardTypeOptions()
  loadAreaTree()
  loadReservoirOptions()
  fetchTable()
})
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

.river-reference-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 100%;
}

.river-reference-box {
  width: 14px;
  height: 14px;
  border-radius: 2px;
  border: 2px solid #94a3b8;
  box-sizing: border-box;
  background: #fff;
  transition: all 0.2s ease;
  flex: 0 0 14px;
  position: relative;
}

.river-reference-box.is-checked {
  border-color: var(--river-primary);
  background: var(--river-primary);
}

.river-reference-box.is-checked::after {
  content: '';
  position: absolute;
  left: 2px;
  top: 0px;
  width: 5px;
  height: 8px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.river-reference-label {
  color: var(--river-text-main);
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

.qr-wrap {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px 20px;
  padding: 16px;
  margin-bottom: 4px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-extra-light);
}

@media (max-width: 900px) {
  .qr-wrap {
    grid-template-columns: 1fr;
  }
}

.qr-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
  min-width: 0;
}

.qr-label {
  width: 100%;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  line-height: 1.4;
}

.qr-body {
  flex-shrink: 0;
}

.qr-empty {
  width: 160px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  color: var(--el-text-color-placeholder);
  box-sizing: border-box;
}

.qr-static-frame {
  width: 160px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  background: #fff;
  box-sizing: border-box;
}

.qr-static-img {
  display: block;
  width: 148px;
  height: 148px;
  object-fit: contain;
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
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}
</style>
