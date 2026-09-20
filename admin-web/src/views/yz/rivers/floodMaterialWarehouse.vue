<template>
  <div class="river-page">
    <ContentWrap class="query-wrap">
      <el-form class="query-form -mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="储备单位" prop="warehouseName">
          <el-input
            v-model="queryParams.warehouseName"
            placeholder="请输入储备单位关键词"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item label="归属单位" prop="belongUnit">
          <el-input
            v-model="queryParams.belongUnit"
            placeholder="请输入归属单位关键词"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item label="行政划分" prop="divisionCode">
          <el-tree-select
            v-model="queryParams.divisionCode"
            :data="areaTreeData"
            :props="areaTreeProps"
            node-key="value"
            check-strictly
            filterable
            :filter-node-method="filterAreaTreeNode"
            clearable
            placeholder="请选择（可搜索）"
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

    <ContentWrap class="table-wrap">
      <el-table class="river-table" v-loading="tableLoading" :data="tableData">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="储备单位" align="center" min-width="160" prop="warehouseName" show-overflow-tooltip />
        <el-table-column label="具体位置" align="center" min-width="220" prop="specificLocation" show-overflow-tooltip />
<!--        <el-table-column label="归属单位" align="center" min-width="180" prop="belongUnit" show-overflow-tooltip />-->
        <el-table-column label="负责人姓名" align="center" min-width="120" prop="leaderName" show-overflow-tooltip />
        <el-table-column label="负责人电话" align="center" min-width="140" prop="leaderPhone" show-overflow-tooltip />
        <el-table-column label="行政划分" align="center" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatDivision(row?.divisionCode) }}
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

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="fetchTable"
      />
    </ContentWrap>

    <el-dialog class="facility-dialog" v-model="dialogVisible" :title="dialogTitle" width="900px" destroy-on-close :close-on-click-modal="false">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="110px"
        label-position="left"
        :disabled="formReadonly"
      >
        <el-row :gutter="16">
          <el-col :span="24">
            <div class="group-title">基础信息</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="储备单位" prop="warehouseName">
              <el-input v-model="form.warehouseName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属单位" prop="belongUnit">
              <el-input v-model="form.belongUnit" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人姓名" prop="leaderName">
              <el-input v-model="form.leaderName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人电话" prop="leaderPhone">
              <el-input v-model="form.leaderPhone" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="具体位置" prop="specificLocation">
              <el-input v-model="form.specificLocation" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="行政划分" prop="divisionCode">
              <el-tree-select
                v-model="form.divisionCode"
                multiple
                show-checkbox
                :data="areaTreeData"
                :props="areaTreeProps"
                node-key="value"
                check-strictly
                filterable
                :filter-node-method="filterAreaTreeNode"
                clearable
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="6"
                placeholder="请选择（可搜索）"
                class="select-long"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="group-title">位置与地图</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number
                v-model="form.longitude"
                :precision="6"
                :step="0.000001"
                controls-position="right"
                class="select-long"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number
                v-model="form.latitude"
                :precision="6"
                :step="0.000001"
                controls-position="right"
                class="select-long"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="section-title">储备单位地址</div>
            <TiandituGeoJsonPreview
              v-if="formReadonly"
              :geo-json="warehouseGeoJson"
              :active="dialogVisible"
              :height="360"
              :center="mapCenter"
              :zoom="14"
              :label-text="form.warehouseName || ''"
              :highlight="true"
              empty-text="暂无位置信息"
            />
            <div v-else>
              <TiandituGeoJsonEditor
                v-model="warehouseGeoJson"
                :active="dialogVisible"
                :height="420"
                :center="mapCenter"
                :zoom="14"
                :draw-modes="['POINT']"
              />
              <div class="map-tip">仅支持绘制点位；可直接输入经纬度，或在地图上绘制点位自动回填到经纬度。</div>
            </div>
          </el-col>

          <el-col :span="24">
            <div class="group-title">物资与附件</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物资种类" prop="materialType">
              <el-input v-model="form.materialType" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否代储" prop="isDelegateStorage">
              <el-radio-group v-model="form.isDelegateStorage">
                <el-radio :label="0">否</el-radio>
                <el-radio :label="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="仓库图片" prop="warehouseImages">
              <UploadImgs v-model="form.warehouseImages" :limit="5" :file-size="5" :drag="false" :disabled="formReadonly">
                <template #tip>
                  <span>最多上传 5 张图片，每张不超过 5MB，支持 jpg/png/gif</span>
                </template>
              </UploadImgs>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remarks">
              <el-input v-model="form.remarks" type="textarea" :rows="3" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="dialogVisible = false">{{ formReadonly ? '关闭' : '取消' }}</el-button>
          <el-button v-if="!formReadonly" type="primary" :loading="submitting" @click="submitForm">保存</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import download from '@/utils/download'
import UploadImgs from '@/components/UploadFile/src/UploadImgs.vue'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import { getAreaTree } from '@/api/system/area'
import {
  createFloodMaterialWarehouse,
  deleteFloodMaterialWarehouse,
  exportFloodMaterialWarehouseExcel,
  getFloodMaterialWarehouseDetail,
  getFloodMaterialWarehousePage,
  updateFloodMaterialWarehouse,
  type FloodMaterialWarehousePageReqVO,
  type FloodMaterialWarehousePageRespVO,
  type FloodMaterialWarehouseSaveReqVO
} from '@/api/gis/floodMaterialWarehouse'

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
const areaNameMap = reactive<Record<string, string>>({})

const filterAreaTreeNode = (keyword: string, data: any) => {
  const k = String(keyword || '').trim()
  if (!k) return true
  return String(data?.label || '').includes(k)
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
  const data = (res && (res.data || res)) as AreaTreeNode[]
  areaTreeData.value = buildAreaTree(data || [])
  Object.keys(areaNameMap).forEach((key) => delete areaNameMap[key])
  const flatten = (nodes: TreeSelectNode[]) => {
    ;(nodes || []).forEach((n) => {
      areaNameMap[String(n.value)] = n.label
      if (n.children && n.children.length) flatten(n.children)
    })
  }
  flatten(areaTreeData.value)
}

const queryParams = reactive<FloodMaterialWarehousePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  warehouseName: '',
  belongUnit: '',
  divisionCode: ''
})
const queryFormRef = ref<FormInstance>()

const tableData = ref<FloodMaterialWarehousePageRespVO[]>([])
const total = ref(0)
const tableLoading = ref(false)

const fetchTable = async () => {
  tableLoading.value = true
  try {
    const res = await getFloodMaterialWarehousePage(queryParams)
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

const handleExport = async () => {
  try {
    const data = await exportFloodMaterialWarehouseExcel(queryParams)
    download.excel(data, '防汛物资仓库.xls')
  } catch {
    // 下载失败时，错误提示由全局拦截器处理
  }
}

const formatDivision = (codes?: string[]) => {
  const list = (codes || []).map((it) => String(it)).filter((it) => it)
  if (list.length === 0) return '-'
  return list.map((id) => areaNameMap[id] || id).join('、')
}

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const formMode = ref<'create' | 'edit' | 'view'>('create')
const dialogTitle = computed(() => {
  if (formMode.value === 'edit') return '编辑防汛物资仓库'
  if (formMode.value === 'view') return '防汛物资仓库详情'
  return '新增防汛物资仓库'
})
const formReadonly = computed(() => formMode.value === 'view')

const warehouseGeoJson = ref('')
const DEFAULT_CENTER = [32.272258, 119.184766] as [number, number]

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

const mapCenter = computed<[number, number]>(() => {
  const lon = normalizeNumber(form.longitude)
  const lat = normalizeNumber(form.latitude)
  if (Number.isFinite(lon) && Number.isFinite(lat)) {
    return [lat, lon]
  }
  return DEFAULT_CENTER
})

const form = reactive<FloodMaterialWarehouseSaveReqVO>({
  id: '',
  warehouseName: '',
  specificLocation: '',
  longitude: undefined,
  latitude: undefined,
  belongUnit: '',
  leaderName: '',
  leaderPhone: '',
  materialType: '',
  isDelegateStorage: 0,
  warehouseImages: [],
  remarks: '',
  divisionCode: []
})

const rules: FormRules = {
  warehouseName: [{ required: true, message: '请输入储备单位', trigger: 'blur' }]
}

const resetForm = () => {
  form.id = ''
  form.warehouseName = ''
  form.specificLocation = ''
  form.longitude = undefined
  form.latitude = undefined
  form.belongUnit = ''
  form.leaderName = ''
  form.leaderPhone = ''
  form.materialType = ''
  form.isDelegateStorage = 0
  form.warehouseImages = []
  form.remarks = ''
  form.divisionCode = []
  warehouseGeoJson.value = ''
}

const openCreateDialog = () => {
  formMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = async (id: string | number) => {
  formMode.value = 'edit'
  resetForm()
  dialogVisible.value = true
  const data = await getFloodMaterialWarehouseDetail(id)
  Object.assign(form, data || {})
  form.isDelegateStorage = Number(form.isDelegateStorage ?? 0)
  warehouseGeoJson.value = buildPointGeoJson(form.longitude, form.latitude)
}

const openDetailDialog = async (id: string | number) => {
  formMode.value = 'view'
  resetForm()
  dialogVisible.value = true
  const data = await getFloodMaterialWarehouseDetail(id)
  Object.assign(form, data || {})
  form.isDelegateStorage = Number(form.isDelegateStorage ?? 0)
  warehouseGeoJson.value = buildPointGeoJson(form.longitude, form.latitude)
}

const handleDetail = (row: FloodMaterialWarehousePageRespVO) => {
  if (!row?.id) return
  openDetailDialog(row.id)
}

const handleEdit = (row: FloodMaterialWarehousePageRespVO) => {
  if (!row?.id) return
  openEditDialog(row.id)
}

const handleDelete = async (row: FloodMaterialWarehousePageRespVO) => {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm(`确认删除仓库「${row.warehouseName}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteFloodMaterialWarehouse(row.id)
  ElMessage.success('删除成功')
  fetchTable()
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload: FloodMaterialWarehouseSaveReqVO = {
      ...form,
      isDelegateStorage: Number(form.isDelegateStorage ?? 0),
      warehouseImages: Array.isArray(form.warehouseImages) ? form.warehouseImages : [],
      divisionCode: Array.isArray(form.divisionCode) ? form.divisionCode : []
    }
    if (formMode.value === 'edit') {
      await updateFloodMaterialWarehouse(payload)
      ElMessage.success('编辑成功')
    } else {
      await createFloodMaterialWarehouse(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchTable()
  } finally {
    submitting.value = false
  }
}

watch(
  () => [form.longitude, form.latitude],
  ([lon, lat]) => {
    if (formReadonly.value) return
    const next = buildPointGeoJson(lon as any, lat as any)
    if (next === warehouseGeoJson.value) return
    warehouseGeoJson.value = next
  }
)

watch(
  () => warehouseGeoJson.value,
  (geo) => {
    if (formReadonly.value) return
    const parsed = parsePointGeoJson(geo)
    if (!parsed) return
    if (Number(form.longitude) === parsed.lon && Number(form.latitude) === parsed.lat) return
    form.longitude = parsed.lon
    form.latitude = parsed.lat
  }
)

onMounted(async () => {
  await loadAreaTree()
  await fetchTable()
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
</style>
