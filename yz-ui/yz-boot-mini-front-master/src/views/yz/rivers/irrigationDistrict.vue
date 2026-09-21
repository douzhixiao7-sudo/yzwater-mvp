<template>
  <div class="river-page">
    <ContentWrap class="query-wrap">
      <el-form class="query-form -mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="90px">
        <el-form-item label="灌区名称" prop="irrigationDistrictName">
          <el-input
            v-model="queryParams.irrigationDistrictName"
            placeholder="请输入灌区名称关键词"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item label="所在流域" prop="basinCode">
          <el-select v-model="queryParams.basinCode" placeholder="请选择所在流域" clearable filterable class="!w-240px">
            <el-option v-for="item in basinOptions" :key="item.value" :label="item.label" :value="item.value" />
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
<!--        <el-form-item label="生态红线" prop="isEcologicalRedLine">
          <el-select v-model="queryParams.isEcologicalRedLine" placeholder="请选择" clearable class="!w-240px">
            <el-option label="否" :value="0" />
            <el-option label="是" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="开发边界" prop="isDevelopmentBoundary">
          <el-select v-model="queryParams.isDevelopmentBoundary" placeholder="请选择" clearable class="!w-240px">
            <el-option label="否" :value="0" />
            <el-option label="是" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="leaderName">
          <el-input v-model="queryParams.leaderName" placeholder="请输入负责人" clearable @keyup.enter="handleSearch" class="!w-240px" />
        </el-form-item>
        <el-form-item label="联系电话" prop="leaderPhone">
          <el-input v-model="queryParams.leaderPhone" placeholder="请输入联系电话" clearable @keyup.enter="handleSearch" class="!w-240px" />
        </el-form-item>-->
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

    <ContentWrap class="table-wrap">
      <el-table class="river-table" v-loading="tableLoading" :data="tableData">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="灌区名称" align="center" min-width="160" prop="irrigationDistrictName" show-overflow-tooltip />
        <el-table-column label="所在流域" align="center" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ resolveLabel(row.basinCode, basinLabelMap) }}
          </template>
        </el-table-column>
        <el-table-column align="center" min-width="180">
          <template #header>
            <span class="nowrap">设计灌溉面积（万亩）</span>
          </template>
          <template #default="{ row }">
            {{ formatPlainNumber(row.designIrrigationArea) }}
          </template>
        </el-table-column>
        <el-table-column label="行政区划" align="center" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatDivision(row.divisionCode) }}
          </template>
        </el-table-column>
<!--        <el-table-column label="干渠长度(m)" align="center" min-width="140">
          <template #default="{ row }">
            {{ formatNumber(row.mainCanalLengthM) }}
          </template>
        </el-table-column>
        <el-table-column label="负责人" align="center" min-width="120" prop="leaderName" show-overflow-tooltip />
        <el-table-column label="联系电话" align="center" min-width="140" prop="leaderPhone" show-overflow-tooltip />-->
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="warning" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="fetchTable"
      />
    </ContentWrap>

    <el-dialog class="facility-dialog" v-model="dialogVisible" :title="dialogTitle" width="980px" destroy-on-close :close-on-click-modal="false">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="140px"
        label-position="left"
        :disabled="formReadonly"
      >
        <el-row :gutter="16">
          <el-col :span="24">
            <div class="group-title">基础信息</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="灌区名称" prop="irrigationDistrictName">
              <el-input v-model="formData.irrigationDistrictName" placeholder="请输入灌区名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在流域" prop="basinCode">
              <el-select v-model="formData.basinCode" placeholder="请选择所在流域" clearable filterable class="!w-240px">
                <el-option v-for="item in basinOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="灌区类型" prop="irrigationDistrictType">
              <el-select v-model="formData.irrigationDistrictType" placeholder="请选择灌区类型" clearable filterable class="!w-240px">
                <el-option v-for="item in irrigationDistrictTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <div class="group-title">规模指标</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际空间面积(k㎡)" prop="actualIrrigableArea">
              <el-input-number v-model="formData.actualIrrigableArea" :min="0" :precision="4" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="基本农田面积(k㎡)" prop="basicFarmlandAreaKm2">
              <el-input-number
                v-model="formData.basicFarmlandAreaKm2"
                :min="0"
                :precision="4"
                controls-position="right"
                class="!w-240px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生态红线" prop="isEcologicalRedLine">
              <el-select v-model="formData.isEcologicalRedLine" placeholder="请选择" clearable class="!w-240px">
                <el-option label="否" :value="0" />
                <el-option label="是" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开发边界" prop="isDevelopmentBoundary">
              <el-select v-model="formData.isDevelopmentBoundary" placeholder="请选择" clearable class="!w-240px">
                <el-option label="否" :value="0" />
                <el-option label="是" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="干渠长度(m)" prop="mainCanalLengthM">
              <el-input-number v-model="formData.mainCanalLengthM" :min="0" :precision="4" controls-position="right" class="!w-240px" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="设计灌溉面积（万亩）"
              prop="designIrrigationArea"
              label-width="190px"
              class="label-nowrap-item"
            >
              <el-input-number
                v-model="formData.designIrrigationArea"
                :min="0"
                :step="0.0001"
                controls-position="right"
                class="!w-240px"
                @change="handleDesignIrrigationAreaChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <div class="group-title">管理信息</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="leaderName">
              <el-input v-model="formData.leaderName" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="leaderPhone">
              <el-input v-model="formData.leaderPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="行政区划" prop="divisionCode">
              <el-tree-select
                v-model="formData.divisionCode"
                :data="areaTreeData"
                :props="areaTreeProps"
                node-key="value"
                multiple
                show-checkbox
                check-strictly
                filterable
                clearable
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="6"
                placeholder="请选择行政区划（可多选）"
                class="select-long"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="管理单位" prop="managementUnitText">
              <el-input v-model="formData.managementUnitText" placeholder="请输入管理单位（多个用逗号分隔）" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="group-title">灌区位置</div>
            <TiandituGeoJsonPreview
              v-if="formMode === 'view'"
              :geo-json="formData.geometryGeoJson || ''"
              :active="dialogVisible"
              :height="360"
              :center="mapCenter"
            />
            <div v-else>
              <TiandituGeoJsonEditor v-model="formData.geometryGeoJson" :active="dialogVisible" :height="420" :center="mapCenter" />
              <div class="map-tip">支持上传 geometry.json、缩放、全屏、绘制点/线/面，保存后自动入库。</div>
            </div>
          </el-col>

          <el-col :span="24">
            <div class="group-title">附件与备注</div>
          </el-col>
          <el-col :span="24">
            <el-form-item label="灌区图片" prop="irrigationDistrictImages">
              <UploadImgs v-model="formData.irrigationDistrictImages" :limit="10" :file-size="5" :drag="false" :disabled="formReadonly" />
              <div class="upload-tip">最多 10 张，单张不超过 5MB</div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remarks">
              <el-input v-model="formData.remarks" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!formReadonly" type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import UploadImgs from '@/components/UploadFile/src/UploadImgs.vue'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import { getAreaTree } from '@/api/system/area'
import { getRiverDict, type DictDataItemRespVO } from '@/api/gis/riverChannel'
import download from '@/utils/download'
import {
  createIrrigationDistrict,
  deleteIrrigationDistrict,
  exportIrrigationDistrictExcel,
  getIrrigationDistrictDetail,
  getIrrigationDistrictPage,
  updateIrrigationDistrict,
  type IrrigationDistrictPageReqVO,
  type IrrigationDistrictPageRespVO,
  type IrrigationDistrictSaveReqVO
} from '@/api/gis/irrigationDistrict'

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

const queryParams = reactive<IrrigationDistrictPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  irrigationDistrictName: '',
  basinCode: '',
  divisionCode: '',
  isEcologicalRedLine: undefined,
  isDevelopmentBoundary: undefined,
  leaderName: '',
  leaderPhone: '',
  irrigationDistrictType: ''
})

const queryFormRef = ref<FormInstance>()
const tableData = ref<IrrigationDistrictPageRespVO[]>([])
const total = ref(0)
const tableLoading = ref(false)

const areaTreeData = ref<TreeSelectNode[]>([])
const areaNameMap = ref<Record<string, string>>({})
const basinOptions = ref<DictDataItemRespVO[]>([])
const irrigationDistrictTypeOptions = ref<DictDataItemRespVO[]>([])

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

const buildAreaNameMap = (nodes: TreeSelectNode[], map: Record<string, string>) => {
  for (const item of nodes || []) {
    if (item?.value) map[item.value] = item.label || item.value
    if (item.children && item.children.length) {
      buildAreaNameMap(item.children, map)
    }
  }
}

const loadAreaTree = async () => {
  const res = (await getAreaTree()) as any
  const data = (res && (res.data || res)) as AreaTreeNode[]
  areaTreeData.value = buildAreaTree(data || [])
  const map: Record<string, string> = {}
  buildAreaNameMap(areaTreeData.value, map)
  areaNameMap.value = map
}

const loadBasinOptions = async () => {
  basinOptions.value = (await getRiverDict('zd_szly')) || []
}

const loadIrrigationDistrictTypeOptions = async () => {
  irrigationDistrictTypeOptions.value = (await getRiverDict('zd_gqlx')) || []
}

const basinLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  for (const item of basinOptions.value || []) {
    if (!item?.value) continue
    map[item.value] = item.label || item.value
  }
  return map
})

const irrigationDistrictTypeLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  for (const item of irrigationDistrictTypeOptions.value || []) {
    if (!item?.value) continue
    map[item.value] = item.label || item.value
  }
  return map
})

const resolveLabel = (value?: string, map?: Record<string, string>) => {
  const v = String(value || '').trim()
  if (!v) return '-'
  return map?.[v] || v
}

const fetchTable = async () => {
  tableLoading.value = true
  try {
    const res = await getIrrigationDistrictPage(queryParams)
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
  queryParams.isEcologicalRedLine = undefined
  queryParams.isDevelopmentBoundary = undefined
  fetchTable()
}

const formatNumber = (val?: number | null) => {
  if (val === undefined || val === null) return '-'
  const num = Number(val)
  if (Number.isNaN(num)) return '-'
  return num.toFixed(4)
}

const formatPlainNumber = (val?: number | null) => {
  if (val === undefined || val === null) return '-'
  const num = Number(val)
  if (Number.isNaN(num)) return '-'
  return String(num)
}

const formatFlag = (val?: number | null) => {
  if (val === 0) return '否'
  if (val === 1) return '是'
  return '-'
}

const formatDivision = (codes?: string[]) => {
  const list = (codes || []).map((v) => String(v || '').trim()).filter((v) => v)
  if (!list.length) return '-'
  const names = list.map((id) => areaNameMap.value[id] || id)
  return names.join('，')
}

const handleExport = async () => {
  const blob = await exportIrrigationDistrictExcel(queryParams)
  download.excel(blob, '灌区信息.xls')
}

type FormMode = 'create' | 'edit' | 'view'
const formMode = ref<FormMode>('create')

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

type IrrigationDistrictFormModel = {
  id?: number
  facilityId?: number
  irrigationDistrictName: string
  basinCode: string
  irrigationDistrictType: string
  divisionCode: string[]
  irrigationDistrictImages: string[]
  remarks: string
  designIrrigationArea?: number
  actualIrrigableArea?: number
  basicFarmlandAreaKm2?: number
  isEcologicalRedLine?: number
  isDevelopmentBoundary?: number
  mainCanalLengthM?: number
  leaderName: string
  leaderPhone: string
  managementUnit: string[]
  managementUnitText: string
  geometryGeoJson: string
  srid: number
}

const formData = reactive<IrrigationDistrictFormModel>({
  id: undefined,
  facilityId: undefined,
  irrigationDistrictName: '',
  basinCode: '',
  irrigationDistrictType: '',
  divisionCode: [],
  irrigationDistrictImages: [],
  remarks: '',
  designIrrigationArea: undefined,
  actualIrrigableArea: undefined,
  basicFarmlandAreaKm2: undefined,
  isEcologicalRedLine: undefined,
  isDevelopmentBoundary: undefined,
  mainCanalLengthM: undefined,
  leaderName: '',
  leaderPhone: '',
  managementUnit: [],
  managementUnitText: '',
  geometryGeoJson: '',
  srid: 4490
})

const resetForm = () => {
  formData.id = undefined
  formData.facilityId = undefined
  formData.irrigationDistrictName = ''
  formData.basinCode = ''
  formData.irrigationDistrictType = ''
  formData.divisionCode = []
  formData.irrigationDistrictImages = []
  formData.remarks = ''
  formData.designIrrigationArea = undefined
  formData.actualIrrigableArea = undefined
  formData.basicFarmlandAreaKm2 = undefined
  formData.isEcologicalRedLine = undefined
  formData.isDevelopmentBoundary = undefined
  formData.mainCanalLengthM = undefined
  formData.leaderName = ''
  formData.leaderPhone = ''
  formData.managementUnit = []
  formData.managementUnitText = ''
  formData.geometryGeoJson = ''
  formData.srid = 4490
}

const dialogTitle = computed(() => {
  if (formMode.value === 'view') return '灌区详情'
  if (formMode.value === 'edit') return '编辑灌区'
  return '新增灌区'
})

const formReadonly = computed(() => formMode.value === 'view')

const formRules: FormRules = {
  irrigationDistrictName: [{ required: true, message: '请输入灌区名称', trigger: 'blur' }]
}

const mapCenter = computed<[number, number]>(() => [32.4, 119.2])

const fillFormByDetail = async (id: string | number) => {
  const data = await getIrrigationDistrictDetail(id)
  resetForm()
  formData.id = data.id
  formData.facilityId = data.facilityId
  formData.irrigationDistrictName = data.irrigationDistrictName || ''
  formData.basinCode = data.basinCode || ''
  formData.irrigationDistrictType = data.irrigationDistrictType || ''
  formData.divisionCode = Array.isArray(data.divisionCode) ? data.divisionCode : []
  formData.managementUnit = Array.isArray(data.managementUnit) ? data.managementUnit : []
  formData.managementUnitText = normalizeStringList(formData.managementUnit).join('，')
  formData.irrigationDistrictImages = Array.isArray(data.irrigationDistrictImages) ? data.irrigationDistrictImages : []
  formData.remarks = data.remarks || ''
  formData.designIrrigationArea = data.designIrrigationArea
  formData.actualIrrigableArea = data.actualIrrigableArea
  formData.basicFarmlandAreaKm2 = data.basicFarmlandAreaKm2
  formData.isEcologicalRedLine = data.isEcologicalRedLine
  formData.isDevelopmentBoundary = data.isDevelopmentBoundary
  formData.mainCanalLengthM = data.mainCanalLengthM
  formData.leaderName = data.leaderName || ''
  formData.leaderPhone = data.leaderPhone || ''
  formData.geometryGeoJson = data.geometryGeoJson || ''
  formData.srid = data.srid || 4490
}

const openCreateDialog = async () => {
  formMode.value = 'create'
  resetForm()
  await loadAreaTree()
  dialogVisible.value = true
}

const openEditDialog = async (id: string | number, mode: FormMode) => {
  formMode.value = mode
  await loadAreaTree()
  await fillFormByDetail(id)
  dialogVisible.value = true
}

const handleDetail = (row: any) => {
  openEditDialog(row.id, 'view')
}

const handleEdit = (row: any) => {
  openEditDialog(row.id, 'edit')
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(`确认删除灌区「${row.irrigationDistrictName}」吗？`, '提示', {
    type: 'warning'
  })
  await deleteIrrigationDistrict(row.id)
  ElMessage.success('删除成功')
  fetchTable()
}

const normalizeString = (val?: string) => {
  const v = String(val || '').trim()
  return v ? v : undefined
}

const normalizeStringList = (vals?: string[]) => {
  return (vals || []).map((v) => String(v || '').trim()).filter((v) => v)
}

const roundToFourDecimals = (val?: number | null) => {
  if (val === undefined || val === null) return undefined
  const num = Number(val)
  if (Number.isNaN(num)) return undefined
  return Math.round(num * 10000) / 10000
}

const handleDesignIrrigationAreaChange = (val?: number) => {
  formData.designIrrigationArea = roundToFourDecimals(val)
}

const normalizeStringListText = (val?: string) => {
  return normalizeStringList(String(val || '').split(/[\n\r,，;；、]+/g))
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const managementUnitList = normalizeStringListText(formData.managementUnitText)
    formData.managementUnit = managementUnitList

    const payload: IrrigationDistrictSaveReqVO = {
      id: formMode.value === 'edit' ? formData.id : undefined,
      facilityId: formData.facilityId,
      irrigationDistrictName: formData.irrigationDistrictName,
      basinCode: normalizeString(formData.basinCode),
      irrigationDistrictType: normalizeString(formData.irrigationDistrictType),
      divisionCode: normalizeStringList(formData.divisionCode),
      irrigationDistrictImages: normalizeStringList(formData.irrigationDistrictImages),
      remarks: normalizeString(formData.remarks),
      designIrrigationArea: formData.designIrrigationArea,
      actualIrrigableArea: formData.actualIrrigableArea,
      basicFarmlandAreaKm2: formData.basicFarmlandAreaKm2,
      isEcologicalRedLine: formData.isEcologicalRedLine,
      isDevelopmentBoundary: formData.isDevelopmentBoundary,
      mainCanalLengthM: formData.mainCanalLengthM,
      leaderName: normalizeString(formData.leaderName),
      leaderPhone: normalizeString(formData.leaderPhone),
      managementUnit: managementUnitList,
      geometryGeoJson: normalizeString(formData.geometryGeoJson),
      srid: formData.srid || 4490
    }

    if (formMode.value === 'edit') {
      await updateIrrigationDistrict(payload)
      ElMessage.success('保存成功')
    } else {
      await createIrrigationDistrict(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchTable()
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadAreaTree(), loadBasinOptions(), loadIrrigationDistrictTypeOptions()])
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
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

.upload-tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.nowrap {
  white-space: nowrap;
}

.label-nowrap-item :deep(.el-form-item__label) {
  white-space: nowrap;
}
</style>
