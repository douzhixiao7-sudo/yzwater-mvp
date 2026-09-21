<template>
  <div class="zbb-page">
    <ContentWrap>
      <div class="header-row">
        <div class="title">值班安排</div>
        <div class="actions">
          <el-button type="primary" @click="openCreateDialog">+ 添加</el-button>
        </div>
      </div>

      <div v-if="displayList.length" class="schedule-list">
        <div v-for="item in displayList" :key="item.id" class="schedule-card">
          <div class="card-header">
            <div class="date-range">排班日期：{{ formatRange(item.startDate, item.endDate) }}</div>
            <div class="card-actions">
              <el-button link type="primary" @click="openEditDialog(item.id)">设置</el-button>
              <el-button
                link
                type="success"
                :loading="exportLoadingId === item.id"
                @click="handleExport(item)"
              >
                导出
              </el-button>
              <el-button link type="danger" @click="handleDelete(item.id)">删除</el-button>
            </div>
          </div>
          <div class="description">值班说明：{{ item.description || '-' }}</div>
          <table class="schedule-table">
            <thead>
              <tr>
                <th>日期</th>
                <th v-for="day in weekDays" :key="day.value">{{ day.label }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="role in roleRows" :key="role.key">
                <td class="role-cell">{{ role.label }}</td>
                <td v-for="day in weekDays" :key="day.value">
                  <div class="cell-name">
                    {{ resolveCellName(item.dayMap, day.value, role.key) }}
                  </div>
                  <div v-if="resolveCellPhone(item.dayMap, day.value, role.key)" class="cell-phone">
                    {{ resolveCellPhone(item.dayMap, day.value, role.key) }}
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <el-empty v-else :description="loading ? '正在加载...' : '暂无值班安排'" class="empty-block" />
    </ContentWrap>

    <el-dialog
      v-model="dialogVisible"
      title="值班安排"
      width="1120px"
      class="zbb-dialog"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div v-loading="dialogLoading">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="90px"
          label-position="left"
          class="zbb-form"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="开始日期" prop="startDate">
                <el-date-picker
                  v-model="form.startDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择"
                  class="date-input"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束日期" prop="endDate">
                <el-date-picker
                  v-model="form.endDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择"
                  class="date-input"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="值班说明">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入" />
          </el-form-item>
        </el-form>

        <el-table :data="form.items" border class="edit-table">
          <el-table-column label="日期" align="center" width="110">
            <template #default="{ row }">
              {{ getWeekDayLabel(row.weekDay) }}
            </template>
          </el-table-column>
          <el-table-column label="值班长" align="center">
            <el-table-column label="姓名" align="center" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.dutyChiefName" placeholder="请输入" />
              </template>
            </el-table-column>
            <el-table-column label="电话" align="center" min-width="160">
              <template #default="{ row }">
                <el-input v-model="row.dutyChiefMobile" placeholder="请输入" />
              </template>
            </el-table-column>
          </el-table-column>
          <el-table-column label="白班人员" align="center">
            <el-table-column label="姓名" align="center" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.sectionChiefName" placeholder="请输入" />
              </template>
            </el-table-column>
            <el-table-column label="电话" align="center" min-width="160">
              <template #default="{ row }">
                <el-input v-model="row.sectionChiefMobile" placeholder="请输入" />
              </template>
            </el-table-column>
          </el-table-column>
          <el-table-column label="夜班人员" align="center">
            <el-table-column label="姓名" align="center" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.dutyStaffName" placeholder="请输入" />
              </template>
            </el-table-column>
            <el-table-column label="电话" align="center" min-width="160">
              <template #default="{ row }">
                <el-input v-model="row.dutyStaffMobile" placeholder="请输入" />
              </template>
            </el-table-column>
          </el-table-column>
        </el-table>
      </div>

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
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import download from '@/utils/download'
import {
  createFxZbb,
  deleteFxZbb,
  exportFxZbbExcel,
  getFxZbbDetail,
  getFxZbbList,
  updateFxZbb,
  type FxZbbItemRespVO,
  type FxZbbListRespVO,
  type FxZbbSaveItemReqVO,
  type FxZbbSaveReqVO
} from '@/api/fx/zbb'

defineOptions({ name: 'FxZbb' })

const weekDays = [
  { value: 1, label: '星期一' },
  { value: 2, label: '星期二' },
  { value: 3, label: '星期三' },
  { value: 4, label: '星期四' },
  { value: 5, label: '星期五' },
  { value: 6, label: '星期六' },
  { value: 0, label: '星期日' }
]

const roleRows = [
  { key: 'dutyChief', label: '值班长' },
  { key: 'sectionChief', label: '白班人员' },
  { key: 'dutyStaff', label: '夜班人员' }
]

const loading = ref(false)
const list = ref<FxZbbListRespVO[]>([])
const exportLoadingId = ref('')
const dialogLoading = ref(false)
const itemsDirty = ref(false)
const suspendItemsWatch = ref(false)

type DisplayItem = FxZbbListRespVO & {
  dayMap: Record<number, FxZbbItemRespVO>
}

const buildDayMap = (items?: FxZbbItemRespVO[]) => {
  const map: Record<number, FxZbbItemRespVO> = {}
  ;(items || []).forEach((item) => {
    if (!item || typeof item.weekDay !== 'number') {
      return
    }
    const key = item.weekDay === 7 ? 0 : item.weekDay
    map[key] = item
  })
  return map
}

const displayList = computed<DisplayItem[]>(() =>
  (list.value || []).map((item) => ({
    ...item,
    dayMap: buildDayMap(item.items)
  }))
)

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getFxZbbList()
    list.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

const dialogVisible = ref(false)
const submitting = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()

const form = reactive<FxZbbSaveReqVO>({
  id: '',
  startDate: '',
  endDate: '',
  description: '',
  items: []
})

const rules: FormRules = {
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' },
    {
      validator: (_rule, value, callback) => {
        if (!form.startDate || !value) {
          callback()
          return
        }
        if (value < form.startDate) {
          callback(new Error('结束日期不能早于开始日期'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}

const createDefaultItems = (): FxZbbSaveItemReqVO[] =>
  weekDays.map((day) => ({
    weekDay: day.value,
    dutyChiefName: '',
    dutyChiefMobile: '',
    sectionChiefName: '',
    sectionChiefMobile: '',
    dutyStaffName: '',
    dutyStaffMobile: ''
  }))

const mergeItems = (items?: FxZbbSaveItemReqVO[]) => {
  const map: Record<number, FxZbbSaveItemReqVO> = {}
  ;(items || []).forEach((item) => {
    if (item && typeof item.weekDay === 'number') {
      const key = item.weekDay === 7 ? 0 : item.weekDay
      map[key] = { ...item, weekDay: key }
    }
  })
  return weekDays.map((day) => {
    const current = map[day.value]
    return {
      id: current?.id,
      weekDay: day.value,
      dutyChiefName: current?.dutyChiefName || '',
      dutyChiefMobile: current?.dutyChiefMobile || '',
      sectionChiefName: current?.sectionChiefName || '',
      sectionChiefMobile: current?.sectionChiefMobile || '',
      dutyStaffName: current?.dutyStaffName || '',
      dutyStaffMobile: current?.dutyStaffMobile || ''
    }
  })
}

const setFormItems = (items: FxZbbSaveItemReqVO[]) => {
  suspendItemsWatch.value = true
  form.items = items
  itemsDirty.value = false
  nextTick(() => {
    suspendItemsWatch.value = false
  })
}

watch(
  () => form.items,
  () => {
    if (suspendItemsWatch.value) {
      return
    }
    itemsDirty.value = true
  },
  { deep: true }
)

const resetForm = () => {
  form.id = ''
  form.startDate = ''
  form.endDate = ''
  form.description = ''
  setFormItems(createDefaultItems())
  formRef.value?.clearValidate()
}

const openCreateDialog = () => {
  formMode.value = 'create'
  resetForm()
  dialogLoading.value = false
  dialogVisible.value = true
}

const openEditDialog = async (id: string) => {
  formMode.value = 'edit'
  resetForm()
  dialogVisible.value = true
  const cached = list.value.find((item) => item.id === id)
  const cachedItems = cached?.items || []
  if (cached) {
    form.id = cached.id
    form.startDate = cached.startDate || ''
    form.endDate = cached.endDate || ''
    form.description = cached.description || ''
    setFormItems(mergeItems(cachedItems))
  }
  dialogLoading.value = true
  try {
    const detail = await getFxZbbDetail(id)
    form.id = detail.id || ''
    form.startDate = detail.startDate || ''
    form.endDate = detail.endDate || ''
    form.description = detail.description || ''
    if (detail.items && detail.items.length) {
      setFormItems(mergeItems(detail.items))
    } else if (cachedItems.length) {
      setFormItems(mergeItems(cachedItems))
    }
  } finally {
    dialogLoading.value = false
  }
}

const normalizeText = (value?: string) => String(value || '').trim()

const buildItemsPayload = () =>
  (form.items || [])
    .map((item) => ({
      id: item.id || undefined,
      weekDay: item.weekDay,
      dutyChiefName: normalizeText(item.dutyChiefName),
      dutyChiefMobile: normalizeText(item.dutyChiefMobile),
      sectionChiefName: normalizeText(item.sectionChiefName),
      sectionChiefMobile: normalizeText(item.sectionChiefMobile),
      dutyStaffName: normalizeText(item.dutyStaffName),
      dutyStaffMobile: normalizeText(item.dutyStaffMobile)
    }))
    .filter(
      (item) =>
        item.dutyChiefName ||
        item.dutyChiefMobile ||
        item.sectionChiefName ||
        item.sectionChiefMobile ||
        item.dutyStaffName ||
        item.dutyStaffMobile
    )

const buildPayload = (includeItems: boolean): FxZbbSaveReqVO => ({
  id: form.id || undefined,
  startDate: normalizeText(form.startDate),
  endDate: normalizeText(form.endDate),
  description: normalizeText(form.description),
  items: includeItems ? buildItemsPayload() : []
})

const submitForm = async () => {
  if (dialogLoading.value) {
    ElMessage.warning('正在加载值班数据，请稍后再保存')
    return
  }
  await formRef.value?.validate()
  submitting.value = true
  try {
    const includeItems = formMode.value === 'create' || itemsDirty.value
    const payload = buildPayload(includeItems)
    if (formMode.value === 'edit') {
      await updateFxZbb(payload)
      ElMessage.success('修改成功')
    } else {
      await createFxZbb(payload)
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
    await ElMessageBox.confirm('确认删除该值班安排吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteFxZbb(id)
  ElMessage.success('删除成功')
  await fetchList()
}

const handleExport = async (item: FxZbbListRespVO) => {
  exportLoadingId.value = item.id
  try {
    const data = await exportFxZbbExcel(item.id)
    const filename = `值班安排_${item.startDate || '开始'}_${item.endDate || '结束'}.xlsx`
    download.excel(data, filename)
  } finally {
    exportLoadingId.value = ''
  }
}

const resolveCellName = (
  dayMap: Record<number, FxZbbItemRespVO>,
  day: number,
  role: string
) => {
  const item = dayMap[day]
  const name = getRoleName(item, role)
  const phone = getRolePhone(item, role)
  if (!name && !phone) {
    return '-'
  }
  return name || '-'
}

const resolveCellPhone = (
  dayMap: Record<number, FxZbbItemRespVO>,
  day: number,
  role: string
) => {
  const item = dayMap[day]
  return getRolePhone(item, role)
}

const getRoleName = (item: FxZbbItemRespVO | undefined, role: string) => {
  if (!item) return ''
  if (role === 'dutyChief') return item.dutyChiefName || ''
  if (role === 'sectionChief') return item.sectionChiefName || ''
  return item.dutyStaffName || ''
}

const getRolePhone = (item: FxZbbItemRespVO | undefined, role: string) => {
  if (!item) return ''
  if (role === 'dutyChief') return item.dutyChiefMobile || ''
  if (role === 'sectionChief') return item.sectionChiefMobile || ''
  return item.dutyStaffMobile || ''
}

const getWeekDayLabel = (weekDay: number) => {
  const normalized = weekDay === 7 ? 0 : weekDay
  return weekDays.find((day) => day.value === normalized)?.label || '-'
}

const formatRange = (start?: string, end?: string) => {
  return `${start || '-'} 至 ${end || '-'}`
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.zbb-page {
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
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.schedule-card {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.date-range {
  font-weight: 600;
  color: #303133;
}

.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.description {
  margin: 10px 0 14px;
  color: #606266;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  text-align: center;
  font-size: 14px;
  color: #303133;
}

.schedule-table th,
.schedule-table td {
  border: 1px solid #dcdfe6;
  padding: 8px 6px;
  vertical-align: middle;
}

.schedule-table th {
  background: #f7f9fc;
  font-weight: 600;
}

.role-cell {
  font-weight: 600;
  width: 90px;
}

.cell-name {
  font-weight: 500;
  line-height: 20px;
}

.cell-phone {
  color: #909399;
  line-height: 18px;
  margin-top: 4px;
}

.empty-block {
  margin: 18px 0;
}

.zbb-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
}

.zbb-form .date-input {
  width: 100%;
}

.edit-table {
  margin-top: 14px;
}

.edit-table :deep(.el-input__wrapper) {
  width: 100%;
}
</style>
