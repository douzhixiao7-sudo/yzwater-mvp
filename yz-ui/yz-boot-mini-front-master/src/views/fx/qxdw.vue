<template>
  <div class="qxdw-page">
    <ContentWrap>
      <div class="header-row">
        <div class="title">市级防汛抢险队伍计划表</div>
        <div class="actions">
          <el-button type="primary" @click="openCreateDialog">+ 添加</el-button>
          <el-button type="success" plain :loading="exportLoading" @click="handleExport">
            导出
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="list" row-key="id" class="qxdw-table">
        <el-table-column type="index" label="序号" align="center" width="70" />
        <el-table-column prop="unitName" label="单位" align="center" min-width="160" />
        <el-table-column
          v-if="showTeamName"
          prop="teamName"
          label="队伍名称"
          align="center"
          min-width="160"
        />
        <el-table-column prop="planCount" label="人数" align="center" min-width="110" />
        <el-table-column prop="contactName" label="联系人" align="center" min-width="120" />
        <el-table-column prop="contactPhone" label="联系电话" align="center" min-width="150" />
        <el-table-column prop="remark" label="备注" align="center" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" align="center" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row.id)">修改</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无抢险队伍" class="empty-block" />
    </ContentWrap>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="680px"
      class="qxdw-dialog"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        class="qxdw-form"
        :model="form"
        :rules="rules"
        label-width="90px"
        label-position="left"
      >
        <el-form-item label="单位" prop="unitName">
          <el-input v-model="form.unitName" placeholder="请输入" />
        </el-form-item>
        <el-form-item v-if="showTeamName" label="队伍名称" prop="teamName">
          <el-input v-model="form.teamName" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="人数" prop="planCount">
          <el-input-number v-model="form.planCount" :min="0" :controls="false" class="full-input" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactName" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="请输入备注"
          />
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
import download from '@/utils/download'
import {
  createFxQxdw,
  deleteFxQxdw,
  exportFxQxdwExcel,
  getFxQxdwDetail,
  getFxQxdwList,
  updateFxQxdw,
  type FxQxdwListRespVO,
  type FxQxdwSaveReqVO
} from '@/api/fx/qxdw'

defineOptions({ name: 'FxQxdw' })

/** 队伍名称：暂时隐藏，改回 `true` 可恢复 */
const showTeamName = false

const loading = ref(false)
const exportLoading = ref(false)
const list = ref<FxQxdwListRespVO[]>([])

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getFxQxdwList()
    list.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

const dialogVisible = ref(false)
const submitting = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()

const form = reactive<FxQxdwSaveReqVO>({
  id: '',
  unitName: '',
  teamName: '',
  planCount: 0,
  contactName: '',
  contactPhone: '',
  remark: ''
})

const dialogTitle = computed(() => (formMode.value === 'edit' ? '抢险队伍-修改' : '抢险队伍-新增'))

const rules: FormRules = {
  unitName: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  planCount: [{ type: 'number', message: '人数需为数字', trigger: 'change' }]
}

const resetForm = () => {
  form.id = ''
  form.unitName = ''
  form.teamName = ''
  form.planCount = 0
  form.contactName = ''
  form.contactPhone = ''
  form.remark = ''
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
  const detail = await getFxQxdwDetail(id)
  form.id = detail.id || ''
  form.unitName = detail.unitName || ''
  form.teamName = detail.teamName || ''
  form.planCount = detail.planCount ?? 0
  form.contactName = detail.contactName || ''
  form.contactPhone = detail.contactPhone || ''
  form.remark = detail.remark || ''
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload: FxQxdwSaveReqVO = { ...form }
    if (formMode.value === 'edit') {
      await updateFxQxdw(payload)
      ElMessage.success('修改成功')
    } else {
      await createFxQxdw(payload)
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
    await ElMessageBox.confirm('确认删除该队伍吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteFxQxdw(id)
  ElMessage.success('删除成功')
  await fetchList()
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    const data = await exportFxQxdwExcel()
    download.excel(data, '市级防汛抢险队伍计划表.xls')
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.qxdw-page {
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

.qxdw-table {
  margin-top: 8px;
}

.qxdw-table :deep(.el-table__header .el-table__cell) {
  border-top: 1px solid var(--el-table-border-color);
}

.qxdw-table :deep(.el-table__cell) {
  border-right: 1px solid var(--el-table-border-color);
}

.qxdw-table :deep(.el-table__header .el-table__cell:first-child),
.qxdw-table :deep(.el-table__body .el-table__cell:first-child) {
  border-left: 1px solid var(--el-table-border-color);
}

.qxdw-form :deep(.el-form-item__content) {
  max-width: 420px;
}

.qxdw-form :deep(.el-input),
.qxdw-form :deep(.el-input-number) {
  width: 420px;
}

.full-input {
  width: 100%;
}

.empty-block {
  margin: 12px 0;
}
</style>
