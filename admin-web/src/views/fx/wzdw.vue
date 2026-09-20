<template>
  <div class="wzdw-page">
    <ContentWrap>
      <div class="header-row">
        <div class="title">防汛物资单位</div>
        <div class="actions">
          <el-button type="primary" @click="openCreateDialog">+ 新增</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="list" row-key="id" class="wzdw-table">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="unitName" label="单位" align="center" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" align="center" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row.id)">修改</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无单位数据" class="empty-block" />
    </ContentWrap>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="550px"
      class="wzdw-dialog"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" class="wzdw-form" :model="form" :rules="rules" label-width="90px" label-position="left">
        <el-form-item label="单位名称" prop="unitName" class="unit-item">
          <el-input v-model="form.unitName" placeholder="请输入" class="unit-input" />
        </el-form-item>
        <el-form-item label="位置" prop="geometryGeoJson" class="map-item" required>
          <TiandituGeoJsonEditor
            v-model="form.geometryGeoJson"
            :active="dialogVisible"
            :height="520"
            :center="mapCenter"
            :zoom="14"
            :draw-modes="['POINT']"
          />
          <div class="map-tip">在地图中点击即可打点，可拖动点位进行调整。</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import {
  createFxWzDw,
  deleteFxWzDw,
  getFxWzDwDetail,
  getFxWzDwList,
  updateFxWzDw,
  type FxWzDwListRespVO,
  type FxWzDwSaveReqVO
} from '@/api/fx/wzdw'

defineOptions({ name: 'FxWzDw' })

const loading = ref(false)
const list = ref<FxWzDwListRespVO[]>([])

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getFxWzDwList()
    list.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

const dialogVisible = ref(false)
const submitting = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()

const form = reactive<FxWzDwSaveReqVO>({
  id: '',
  unitName: '',
  geometryGeoJson: ''
})

const dialogTitle = computed(() => (formMode.value === 'edit' ? '防汛物资单位-修改' : '防汛物资单位-新增'))

const DEFAULT_CENTER = [32.272258, 119.184766] as [number, number]

const parsePointGeoJson = (geoJson?: string) => {
  if (!geoJson) return undefined
  try {
    const parsed = JSON.parse(geoJson)
    if (!parsed || parsed.type !== 'Point' || !Array.isArray(parsed.coordinates)) return undefined
    const lon = Number(parsed.coordinates[0])
    const lat = Number(parsed.coordinates[1])
    if (!Number.isFinite(lon) || !Number.isFinite(lat)) return undefined
    return { lon, lat }
  } catch {
    return undefined
  }
}

const mapCenter = computed<[number, number]>(() => {
  const point = parsePointGeoJson(form.geometryGeoJson)
  if (point) {
    return [point.lat, point.lon]
  }
  return DEFAULT_CENTER
})

const validateGeometry = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  const point = parsePointGeoJson(value)
  if (!point) {
    callback(new Error('请在地图中选择位置'))
    return
  }
  callback()
}

const rules: FormRules = {
  unitName: [{ required: true, message: '请输入单位名称', trigger: 'blur' }],
  geometryGeoJson: [{ validator: validateGeometry, trigger: 'change' }]
}

const resetForm = () => {
  form.id = ''
  form.unitName = ''
  form.geometryGeoJson = ''
  formRef.value?.clearValidate()
}

const openCreateDialog = () => {
  formMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = async (id: string) => {
  formMode.value = 'edit'
  resetForm()
  dialogVisible.value = true
  const detail = await getFxWzDwDetail(id)
  form.id = detail.id || ''
  form.unitName = detail.unitName || ''
  form.geometryGeoJson = detail.geometryGeoJson || ''
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload: FxWzDwSaveReqVO = { ...form }
    if (formMode.value === 'edit') {
      await updateFxWzDw(payload)
      ElMessage.success('修改成功')
    } else {
      await createFxWzDw(payload)
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
    await ElMessageBox.confirm('确认删除该单位吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteFxWzDw(id)
  ElMessage.success('删除成功')
  await fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.wzdw-page {
  min-height: 100%;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.wzdw-table {
  margin-top: 8px;
}

.wzdw-table :deep(.el-table__cell) {
  border-right: 1px solid var(--el-table-border-color);
}

.wzdw-table :deep(.el-table__header .el-table__cell:first-child),
.wzdw-table :deep(.el-table__body .el-table__cell:first-child) {
  border-left: 1px solid var(--el-table-border-color);
}

.wzdw-dialog :deep(.el-dialog__body) {
  max-height: 70vh;
  overflow: auto;
  padding: 12px 16px 6px;
}

.wzdw-form :deep(.el-form-item__content) {
  max-width: 100%;
}

.wzdw-form :deep(.el-input) {
  width: 100%;
}

.wzdw-form :deep(.unit-input) {
  width: 360px;
}

.wzdw-form :deep(.map-item) {
  align-items: flex-start;
}

.wzdw-form :deep(.map-item .el-form-item__label) {
  padding-top: 6px;
}

.wzdw-form :deep(.map-item .el-form-item__content) {
  width: 100%;
  max-width: none;
  flex: 1;
  min-width: 0;
}

.wzdw-form :deep(.tianditu-editor) {
  width: 100%;
}

.map-tip {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

.empty-block {
  margin: 12px 0;
}
</style>
