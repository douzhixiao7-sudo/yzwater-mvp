<template>
  <div class="wz-page">
    <ContentWrap class="query-wrap">
      <el-form :model="queryParams" inline class="-mb-15px" @submit.prevent>
          <el-form-item label="品名">
            <el-input
              v-model="queryParams.materialName"
              placeholder="请输入品名"
              clearable
              class="!w-220px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="单位">
            <el-select v-model="queryParams.unit" placeholder="请选择单位" clearable class="!w-220px">
              <el-option
                v-for="item in unitOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <Icon icon="ep:search" class="mr-5px" />
              搜索
            </el-button>
            <el-button @click="handleReset">
              <Icon icon="ep:refresh" class="mr-5px" />
              重置
            </el-button>
            <el-button type="primary" plain @click="openCreateDialog">
              <Icon icon="ep:plus" class="mr-5px" />
              新增
            </el-button>
          </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="table-wrap">
      <el-table v-loading="loading" :data="list" row-key="id" class="wz-table">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="materialName" label="品名" align="center" min-width="180" show-overflow-tooltip />
        <el-table-column label="数量" align="center" min-width="120">
          <template #default="{ row }">
            {{ formatQuantity(row.quantity) }}
          </template>
        </el-table-column>
        <el-table-column label="单位" align="center" min-width="100" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getUnitLabel(row.unit) }}
          </template>
        </el-table-column>
        <el-table-column label="是否代储" align="center" min-width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.isDelegateStorage ?? 0) === 1 ? 'warning' : 'info'">
              {{ getDelegateStorageLabel(row.isDelegateStorage) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="storageUnit"
          label="储备单位"
          align="center"
          min-width="220"
          show-overflow-tooltip
        />

        <el-table-column label="操作" align="center" width="210">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetailDialog(row.id)">详情</el-button>
            <el-button link type="primary" @click="openEditDialog(row.id)">修改</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无物资数据" class="empty-block" />
    </ContentWrap>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="680px"
      class="wz-dialog"
      destroy-on-close
      :close-on-click-modal="false"
    > 
      <el-form ref="formRef" class="wz-form" :model="form" :rules="rules" label-width="98px" label-position="left">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="品名" prop="materialName">
              <el-input v-model="form.materialName" placeholder="请输入品名" maxlength="256" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number
                v-model="form.quantity"
                :min="0"
                :precision="2"
                :step="1"
                :controls="true"
                controls-position="right"
                class="full-width"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-select v-model="form.unit" placeholder="请选择单位" class="full-width" clearable>
                <el-option
                  v-for="item in unitOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
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
          <el-col :span="12">
            <el-form-item label="储备单位" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择储备单位" filterable class="full-width">
                <el-option
                  v-for="item in warehouseOptions"
                  :key="item.id"
                  :label="item.warehouseName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" maxlength="255" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式" prop="contactInfo">
              <el-input v-model="form.contactInfo" placeholder="请输入联系方式" maxlength="255" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="仓库地址" prop="warehouseAddress">
              <el-input
                v-model="form.warehouseAddress"
                placeholder="请输入仓库地址"
                maxlength="1024"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number
                v-model="form.longitude"
                :precision="6"
                :step="0.000001"
                controls-position="right"
                class="full-width"
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
                class="full-width"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <div class="section-title">仓库位置</div>
            <TiandituGeoJsonEditor
              v-model="warehouseGeoJson"
              :active="dialogVisible"
              :height="360"
              :center="mapCenter"
              :zoom="14"
              :draw-modes="['POINT']"
            />
            <div class="map-tip">仅支持绘制点位；可直接输入经纬度，或在地图上绘制点位自动回填到经纬度。</div>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注"
                maxlength="1024"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
        </el-space>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailVisible"
      title="物资详情"
      width="680px"
      class="wz-dialog"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="品名">{{ detailData.materialName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ formatQuantity(detailData.quantity) }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ getUnitLabel(detailData.unit) }}</el-descriptions-item>
        <el-descriptions-item label="是否代储">
          {{ getDelegateStorageLabel(detailData.isDelegateStorage) }}
        </el-descriptions-item>
        <el-descriptions-item label="储备单位">{{ detailData.storageUnit || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detailData.contactPerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ detailData.contactInfo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="仓库地址">{{ detailData.warehouseAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经度">{{ detailData.longitude ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="纬度">{{ detailData.latitude ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-map-wrap">
        <div class="section-title">仓库位置</div>
        <TiandituGeoJsonPreview
          :geo-json="detailGeoJson"
          :active="detailVisible"
          :height="320"
          :center="detailMapCenter"
          :zoom="14"
          :label-text="detailData.materialName || ''"
          :highlight="true"
          empty-text="暂无位置信息"
        />
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getDictLabel, getStrDictOptions } from '@/utils/dict'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import {
  createFxWz,
  deleteFxWz,
  getFxWzDetail,
  getFxWzList,
  getFxWzWarehouseOptions,
  updateFxWz,
  type FxWzListReqVO,
  type FxWzListRespVO,
  type FxWzSaveReqVO,
  type FxWzWarehouseOptionRespVO
} from '@/api/fx/wz'

defineOptions({ name: 'FxWz' })

const loading = ref(false)
const list = ref<FxWzListRespVO[]>([])
const queryParams = reactive<FxWzListReqVO>({
  materialName: '',
  unit: ''
})

const warehouseOptions = ref<FxWzWarehouseOptionRespVO[]>([])
const unitOptions = computed(() => getStrDictOptions('zd_wzdw'))

const fetchWarehouseOptions = async () => {
  const data = await getFxWzWarehouseOptions()
  warehouseOptions.value = Array.isArray(data) ? data : []
}

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getFxWzList({
      materialName: queryParams.materialName?.trim() || undefined,
      unit: queryParams.unit || undefined
    })
    list.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  await fetchList()
}

const handleReset = async () => {
  queryParams.materialName = ''
  queryParams.unit = ''
  await fetchList()
}

const dialogVisible = ref(false)
const submitting = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const warehouseGeoJson = ref('')
const detailGeoJson = ref('')
const DEFAULT_CENTER: [number, number] = [32.272258, 119.184766]

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

const form = reactive<FxWzSaveReqVO>({
  id: '',
  materialName: '',
  quantity: 0,
  unit: '',
  isDelegateStorage: 0,
  warehouseId: '',
  remark: '',
  contactPerson: '',
  contactInfo: '',
  warehouseAddress: '',
  longitude: undefined,
  latitude: undefined,
  sort: 0
})

const mapCenter = computed<[number, number]>(() => {
  const lon = normalizeNumber(form.longitude)
  const lat = normalizeNumber(form.latitude)
  if (Number.isFinite(lon) && Number.isFinite(lat)) {
    return [lat, lon]
  }
  return DEFAULT_CENTER
})

const dialogTitle = computed(() => (formMode.value === 'edit' ? '物资管理-修改' : '物资管理-新增'))

const rules: FormRules = {
  materialName: [{ required: true, message: '请输入品名', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'change' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
  isDelegateStorage: [{ required: true, message: '请选择是否代储', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择储备单位', trigger: 'change' }]
}

const resetForm = () => {
  form.id = ''
  form.materialName = ''
  form.quantity = 0
  form.unit = ''
  form.isDelegateStorage = 0
  form.warehouseId = ''
  form.remark = ''
  form.contactPerson = ''
  form.contactInfo = ''
  form.warehouseAddress = ''
  form.longitude = undefined
  form.latitude = undefined
  form.sort = 0
  warehouseGeoJson.value = ''
  formRef.value?.clearValidate()
}

const openCreateDialog = async () => {
  formMode.value = 'create'
  resetForm()
  if (warehouseOptions.value.length === 0) {
    await fetchWarehouseOptions()
  }
  dialogVisible.value = true
}

const openEditDialog = async (id: string) => {
  formMode.value = 'edit'
  resetForm()
  if (warehouseOptions.value.length === 0) {
    await fetchWarehouseOptions()
  }
  dialogVisible.value = true
  const detail = await getFxWzDetail(id)
  form.id = detail.id || ''
  form.materialName = detail.materialName || ''
  form.quantity = detail.quantity ?? 0
  form.unit = detail.unit || ''
  form.isDelegateStorage = Number(detail.isDelegateStorage ?? 0)
  form.warehouseId = detail.warehouseId || ''
  form.remark = detail.remark || ''
  form.contactPerson = detail.contactPerson || ''
  form.contactInfo = detail.contactInfo || ''
  form.warehouseAddress = detail.warehouseAddress || ''
  form.longitude = detail.longitude
  form.latitude = detail.latitude
  form.sort = detail.sort ?? 0
  warehouseGeoJson.value = buildPointGeoJson(form.longitude, form.latitude)
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload: FxWzSaveReqVO = {
      id: form.id,
      materialName: form.materialName.trim(),
      quantity: form.quantity,
      unit: form.unit,
      isDelegateStorage: Number(form.isDelegateStorage ?? 0),
      warehouseId: form.warehouseId,
      remark: form.remark?.trim() || '',
      contactPerson: form.contactPerson?.trim() || '',
      contactInfo: form.contactInfo?.trim() || '',
      warehouseAddress: form.warehouseAddress?.trim() || '',
      longitude: form.longitude,
      latitude: form.latitude,
      sort: form.sort ?? 0
    }
    if (formMode.value === 'edit') {
      await updateFxWz(payload)
      ElMessage.success('修改成功')
    } else {
      await createFxWz(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchList()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确认删除该物资吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteFxWz(id)
  ElMessage.success('删除成功')
  await fetchList()
}

const detailVisible = ref(false)
const detailData = reactive<{
  materialName: string
  quantity: number | null
  unit: string
  isDelegateStorage: number
  storageUnit: string
  remark: string
  contactPerson: string
  contactInfo: string
  warehouseAddress: string
  longitude: number | undefined
  latitude: number | undefined
}>({
  materialName: '',
  quantity: 0,
  unit: '',
  isDelegateStorage: 0,
  storageUnit: '',
  remark: '',
  contactPerson: '',
  contactInfo: '',
  warehouseAddress: '',
  longitude: undefined,
  latitude: undefined
})

const detailMapCenter = computed<[number, number]>(() => {
  const lon = normalizeNumber(detailData.longitude)
  const lat = normalizeNumber(detailData.latitude)
  if (Number.isFinite(lon) && Number.isFinite(lat)) {
    return [lat, lon]
  }
  return DEFAULT_CENTER
})

const getWarehouseName = (warehouseId?: string) => {
  if (!warehouseId) {
    return ''
  }
  const hit = warehouseOptions.value.find((item) => item.id === warehouseId)
  return hit?.warehouseName || ''
}

const getUnitLabel = (value?: string) => getDictLabel('zd_wzdw', value) || value || '-'

const getDelegateStorageLabel = (value?: number | null) => (Number(value ?? 0) === 1 ? '是' : '否')

const openDetailDialog = async (id: string) => {
  if (warehouseOptions.value.length === 0) {
    await fetchWarehouseOptions()
  }
  const detail = await getFxWzDetail(id)
  detailData.materialName = detail.materialName || ''
  detailData.quantity = detail.quantity ?? 0
  detailData.unit = detail.unit || ''
  detailData.isDelegateStorage = Number(detail.isDelegateStorage ?? 0)
  detailData.storageUnit = getWarehouseName(detail.warehouseId)
  detailData.contactPerson = detail.contactPerson || ''
  detailData.contactInfo = detail.contactInfo || ''
  detailData.remark = detail.remark || ''
  detailData.warehouseAddress = detail.warehouseAddress || ''
  detailData.longitude = detail.longitude
  detailData.latitude = detail.latitude
  detailGeoJson.value = buildPointGeoJson(detailData.longitude, detailData.latitude)
  detailVisible.value = true
}

const formatQuantity = (value?: number | null) => {
  if (value === null || value === undefined) {
    return '-'
  }
  const num = Number(value)
  return Number.isNaN(num) ? '-' : num.toFixed(2)
}

watch(
  () => [form.longitude, form.latitude],
  ([lon, lat]) => {
    const next = buildPointGeoJson(lon, lat)
    if (next === warehouseGeoJson.value) return
    warehouseGeoJson.value = next
  }
)

watch(
  () => warehouseGeoJson.value,
  (geo) => {
    const parsed = parsePointGeoJson(geo)
    if (!parsed) return
    if (Number(form.longitude) === parsed.lon && Number(form.latitude) === parsed.lat) return
    form.longitude = parsed.lon
    form.latitude = parsed.lat
  }
)

onMounted(async () => {
  await Promise.all([fetchWarehouseOptions(), fetchList()])
})
</script>

<style scoped>
.wz-page {
  min-height: 100%;
}

.query-wrap {
  margin-bottom: 12px;
}

.table-wrap {
  padding-top: 2px;
}

.wz-table {
  border: 1px solid #dbe6f3;
  border-radius: 8px;
}

.wz-table :deep(.el-table__cell) {
  padding-top: 8px;
  padding-bottom: 8px;
  border-right: 1px solid var(--el-table-border-color);
}

.wz-table :deep(.el-table__header .el-table__cell:first-child),
.wz-table :deep(.el-table__body .el-table__cell:first-child) {
  border-left: 1px solid var(--el-table-border-color);
}

.wz-form :deep(.el-form-item__content) {
  max-width: 100%;
}

.full-width {
  width: 100%;
}

.section-title {
  margin: 2px 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.map-tip {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.detail-map-wrap {
  margin-top: 12px;
}

.empty-block {
  margin: 12px 0;
}

@media (max-width: 768px) {
  .query-wrap :deep(.el-form) {
    display: block;
  }
}
</style>
