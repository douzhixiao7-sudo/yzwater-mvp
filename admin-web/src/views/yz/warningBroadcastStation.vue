<template>
  <div class="warning-station-page">
    <ContentWrap class="query-wrap">
      <el-form ref="queryFormRef" class="query-form -mb-15px" :model="queryParams" :inline="true" label-width="90px">
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入名称"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item label="代码" prop="code">
          <el-input
            v-model="queryParams.code"
            placeholder="请输入代码"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item label="行政区划" prop="adminDivision">
          <el-tree-select
            v-model="queryParams.adminDivision"
            :data="areaTreeData"
            :props="areaTreeProps"
            node-key="value"
            check-strictly
            check-on-click-node
            filterable
            clearable
            :filter-node-method="filterAreaTreeNode"
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
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="table-wrap">
      <el-table class="river-table" v-loading="loading" :data="list" row-key="id">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="name" label="名称" align="center" min-width="180" show-overflow-tooltip />
        <el-table-column prop="longitude" label="经度" align="center" min-width="120" />
        <el-table-column prop="latitude" label="纬度" align="center" min-width="120" />
        <el-table-column label="行政区划" align="center" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ areaNameMap[row.adminDivision] || row.adminDivision || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="code" label="代码" align="center" min-width="140" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" align="center" min-width="100" />
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetailDialog(row.id)">详情</el-button>
            <el-button link type="warning" @click="openEditDialog(row.id)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && list.length === 0" description="暂无数据" class="empty-block" />
    </ContentWrap>

    <el-dialog
      class="facility-dialog"
      v-model="dialogVisible"
      :title="dialogTitle"
      width="820px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="left"
        :disabled="formReadonly"
      >
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="代码" prop="code">
              <el-input v-model="formData.code" placeholder="请输入代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="formData.quantity" :min="0" :controls="false" class="full-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="行政区划" prop="adminDivision">
              <el-tree-select
                v-model="formData.adminDivision"
                :data="areaTreeData"
                :props="areaTreeProps"
                node-key="value"
                check-strictly
                check-on-click-node
                filterable
                clearable
                :filter-node-method="filterAreaTreeNode"
                placeholder="请选择（可搜索）"
                class="full-input"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number
                v-model="formData.longitude"
                :precision="6"
                :step="0.000001"
                :controls="false"
                class="full-input"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number
                v-model="formData.latitude"
                :precision="6"
                :step="0.000001"
                :controls="false"
                class="full-input"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <div class="group-title">点位地图</div>
            <TiandituGeoJsonPreview
              v-if="formReadonly"
              :geo-json="stationGeoJson"
              :active="dialogVisible"
              :height="320"
              :center="stationMapCenter"
              :zoom="14"
              :label-text="formData.name || ''"
              :highlight="true"
              empty-text="暂无位置信息"
            />
            <div v-else>
              <TiandituGeoJsonEditor
                v-model="stationGeoJson"
                :active="dialogVisible"
                :height="360"
                :center="stationMapCenter"
                :zoom="14"
                :draw-modes="['POINT']"
              />
              <div class="map-tip">可直接输入经纬度，或在地图上绘制点位，二者会自动同步。</div>
            </div>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button v-if="!formReadonly" type="primary" :loading="submitting" @click="submitForm">确定</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import { getAreaTree } from '@/api/system/area'
import {
  createWarningBroadcastStation,
  deleteWarningBroadcastStation,
  getWarningBroadcastStationDetail,
  getWarningBroadcastStationList,
  updateWarningBroadcastStation,
  type WarningBroadcastStationListReqVO,
  type WarningBroadcastStationListRespVO,
  type WarningBroadcastStationSaveReqVO
} from '@/api/yz/warningBroadcastStation'

defineOptions({ name: 'YzWarningBroadcastStation' })

type AreaTreeNode = {
  id: number
  name: string
  children?: AreaTreeNode[]
}

type TreeSelectNode = {
  label: string
  value: string
  children?: TreeSelectNode[]
}

const loading = ref(false)
const list = ref<WarningBroadcastStationListRespVO[]>([])
const queryFormRef = ref<FormInstance>()
const queryParams = reactive<WarningBroadcastStationListReqVO>({
  name: '',
  code: '',
  adminDivision: ''
})

const areaTreeData = ref<TreeSelectNode[]>([])
const areaNameMap = reactive<Record<string, string>>({})
const areaTreeProps = {
  label: 'label',
  value: 'value',
  children: 'children'
}

const filterAreaTreeNode = (keyword: string, data: TreeSelectNode) => {
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
      if (n.children && n.children.length) {
        flatten(n.children)
      }
    })
  }
  flatten(areaTreeData.value)
}

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getWarningBroadcastStationList({ ...queryParams })
    list.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  await fetchList()
}

const handleReset = async () => {
  queryFormRef.value?.resetFields()
  await fetchList()
}

const dialogVisible = ref(false)
const submitting = ref(false)
const dialogMode = ref<'create' | 'edit' | 'detail'>('create')
const formRef = ref<FormInstance>()

const formData = reactive<WarningBroadcastStationSaveReqVO>({
  id: '',
  name: '',
  longitude: undefined,
  latitude: undefined,
  adminDivision: '',
  code: '',
  quantity: 0
})

const stationGeoJson = ref('')
const DEFAULT_STATION_CENTER = [32.272258, 119.184766] as [number, number]

const normalizeNumber = (val: unknown) => {
  if (val === null || val === undefined) return Number.NaN
  if (typeof val === 'number') return val
  if (typeof val === 'string') return Number(val)
  return Number.NaN
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
  if (!parsed || parsed.type !== 'Point' || !Array.isArray(parsed.coordinates) || parsed.coordinates.length < 2) {
    return undefined
  }
  const lon = normalizeNumber(parsed.coordinates[0])
  const lat = normalizeNumber(parsed.coordinates[1])
  if (!Number.isFinite(lon) || !Number.isFinite(lat)) return undefined
  return { lon, lat }
}

const stationMapCenter = computed<[number, number]>(() => {
  const lon = normalizeNumber(formData.longitude)
  const lat = normalizeNumber(formData.latitude)
  if (Number.isFinite(lon) && Number.isFinite(lat)) {
    return [lat, lon]
  }
  return DEFAULT_STATION_CENTER
})

const formReadonly = computed(() => dialogMode.value === 'detail')
const dialogTitle = computed(() => {
  if (dialogMode.value === 'edit') return '预警广播站-编辑'
  if (dialogMode.value === 'detail') return '预警广播站-详情'
  return '预警广播站-新增'
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入代码', trigger: 'blur' }],
  adminDivision: [{ required: true, message: '请选择行政区划', trigger: 'change' }],
  quantity: [{ type: 'number', message: '数量需为数字', trigger: 'change' }]
}

let syncingFromGeoJson = false
let syncingFromLonLat = false

watch(
  () => [formData.longitude, formData.latitude],
  ([lon, lat]) => {
    if (syncingFromGeoJson) return
    const next = buildPointGeoJson(lon, lat)
    if (next === stationGeoJson.value) return
    syncingFromLonLat = true
    stationGeoJson.value = next
    syncingFromLonLat = false
  }
)

watch(
  () => stationGeoJson.value,
  (val) => {
    if (syncingFromLonLat) return
    const point = parsePointGeoJson(val)
    syncingFromGeoJson = true
    if (!point) {
      formData.longitude = undefined
      formData.latitude = undefined
    } else {
      formData.longitude = Number(point.lon.toFixed(6))
      formData.latitude = Number(point.lat.toFixed(6))
    }
    syncingFromGeoJson = false
  }
)

const resetForm = () => {
  formData.id = ''
  formData.name = ''
  formData.longitude = undefined
  formData.latitude = undefined
  formData.adminDivision = ''
  formData.code = ''
  formData.quantity = 0
  stationGeoJson.value = ''
  formRef.value?.clearValidate()
}

const openCreateDialog = async () => {
  dialogMode.value = 'create'
  resetForm()
  await loadAreaTree()
  dialogVisible.value = true
}

const fillFormById = async (id: string) => {
  const detail = await getWarningBroadcastStationDetail(id)
  formData.id = detail.id || ''
  formData.name = detail.name || ''
  formData.longitude = detail.longitude
  formData.latitude = detail.latitude
  formData.adminDivision = detail.adminDivision || ''
  formData.code = detail.code || ''
  formData.quantity = detail.quantity ?? 0
  stationGeoJson.value = buildPointGeoJson(formData.longitude, formData.latitude)
}

const openEditDialog = async (id: string) => {
  dialogMode.value = 'edit'
  resetForm()
  await loadAreaTree()
  dialogVisible.value = true
  await fillFormById(id)
}

const openDetailDialog = async (id: string) => {
  dialogMode.value = 'detail'
  resetForm()
  await loadAreaTree()
  dialogVisible.value = true
  await fillFormById(id)
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload: WarningBroadcastStationSaveReqVO = { ...formData }
    if (dialogMode.value === 'edit') {
      await updateWarningBroadcastStation(payload)
      ElMessage.success('编辑成功')
    } else {
      await createWarningBroadcastStation(payload)
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
    await ElMessageBox.confirm('确认删除该条数据吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteWarningBroadcastStation(id)
  ElMessage.success('删除成功')
  await fetchList()
}

onMounted(async () => {
  await loadAreaTree()
  await fetchList()
})
</script>

<style scoped>
.warning-station-page {
  --river-bg: #f3f7fc;
  --river-card-bg: #ffffff;
  --river-border: rgba(148, 163, 184, 0.28);
  --river-shadow: 0 10px 28px rgba(15, 23, 42, 0.07);
  --river-primary: #1e40af;
  --river-primary-soft: rgba(30, 64, 175, 0.08);
  --river-text-main: #1f2a37;

  min-height: 100%;
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

.full-input {
  width: 100%;
}

.empty-block {
  margin: 12px 0;
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
</style>
