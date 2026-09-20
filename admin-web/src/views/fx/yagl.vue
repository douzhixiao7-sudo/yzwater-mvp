<template>
  <div class="yagl-page">
    <ContentWrap>
      <div class="header-row">
        <div class="title">预案管理</div>
        <div class="actions">
          <el-button type="primary" @click="openCreateDialog">+ 新增预案</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="list" row-key="id" class="yagl-table">
        <el-table-column prop="name" label="预案名称" align="center" min-width="220" />
        <el-table-column label="上传文件" align="left" min-width="420">
          <template #default="{ row }">
            <div v-if="row.files?.length" class="file-name-list">
              <el-tooltip
                v-for="(fileUrl, index) in row.files"
                :key="`${row.id}-${index}-${fileUrl}`"
                :content="getFileName(fileUrl)"
                placement="top-start"
              >
                <el-link :href="fileUrl" download target="_blank" type="primary" class="file-name-link">
                  {{ getFileName(fileUrl) }}
                </el-link>
              </el-tooltip>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="190">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetailDialog(row.id)">详情</el-button>
            <el-button link type="primary" @click="openEditDialog(row.id)">修改</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无预案数据" class="empty-block" />
    </ContentWrap>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="760px"
      class="yagl-dialog"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        class="yagl-form"
        :model="form"
        :rules="rules"
        label-width="98px"
        label-position="left"
      >
        <el-form-item label="预案名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入预案名称" maxlength="256" />
        </el-form-item>
        <el-form-item label="上传文件" prop="files">
          <UploadFile
            v-model="form.files"
            :limit="10"
            :file-size="20"
            :file-type="['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'zip', 'rar']"
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

    <el-dialog
      v-model="detailVisible"
      title="预案详情"
      width="760px"
      class="yagl-dialog"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="预案名称">{{ detailData.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="上传文件">
          <div v-if="detailData.files.length" class="detail-file-list">
            <div
              v-for="(fileUrl, index) in detailData.files"
              :key="`detail-${index}-${fileUrl}`"
              class="detail-file-item"
            >
              <el-link :href="fileUrl" download target="_blank" type="primary">
                {{ getFileName(fileUrl) }}
              </el-link>
            </div>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { UploadFile } from '@/components/UploadFile'
import {
  createFxYagl,
  deleteFxYagl,
  getFxYaglDetail,
  getFxYaglList,
  updateFxYagl,
  type FxYaglListRespVO,
  type FxYaglSaveReqVO
} from '@/api/fx/yagl'

defineOptions({ name: 'FxYagl' })

const loading = ref(false)
const list = ref<FxYaglListRespVO[]>([])

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getFxYaglList()
    list.value = Array.isArray(data)
      ? data.map((item) => ({
          ...item,
          files: Array.isArray(item.files) ? item.files : []
        }))
      : []
  } finally {
    loading.value = false
  }
}

const dialogVisible = ref(false)
const submitting = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()

const form = reactive<FxYaglSaveReqVO>({
  id: '',
  name: '',
  files: [],
  sort: 0
})

const dialogTitle = computed(() => (formMode.value === 'edit' ? '预案管理-修改' : '预案管理-新增'))

const rules: FormRules = {
  name: [{ required: true, message: '请输入预案名称', trigger: 'blur' }],
  files: [{ type: 'array', required: true, message: '请至少上传一个文件', trigger: 'change' }]
}

const resetForm = () => {
  form.id = ''
  form.name = ''
  form.files = []
  form.sort = 0
  formRef.value?.clearValidate()
}

watch(
  () => form.files,
  () => {
    if (dialogVisible.value) {
      formRef.value?.validateField('files').catch(() => undefined)
    }
  },
  { deep: true }
)

const openCreateDialog = () => {
  formMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = async (id: string) => {
  formMode.value = 'edit'
  resetForm()
  dialogVisible.value = true
  const detail = await getFxYaglDetail(id)
  form.id = detail.id || ''
  form.name = detail.name || ''
  form.files = Array.isArray(detail.files) ? detail.files : []
  form.sort = detail.sort ?? 0
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload: FxYaglSaveReqVO = {
      id: form.id,
      name: form.name.trim(),
      files: form.files,
      sort: form.sort ?? 0
    }
    if (formMode.value === 'edit') {
      await updateFxYagl(payload)
      ElMessage.success('修改成功')
    } else {
      await createFxYagl(payload)
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
    await ElMessageBox.confirm('确认删除该预案吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteFxYagl(id)
  ElMessage.success('删除成功')
  await fetchList()
}

const detailVisible = ref(false)
const detailData = reactive<{ name: string; files: string[] }>({
  name: '',
  files: []
})

const openDetailDialog = async (id: string) => {
  const detail = await getFxYaglDetail(id)
  detailData.name = detail.name || ''
  detailData.files = Array.isArray(detail.files) ? detail.files : []
  detailVisible.value = true
}

const getFileName = (fileUrl: string) => {
  const safeUrl = (fileUrl || '').split('?')[0]
  const raw = safeUrl.substring(safeUrl.lastIndexOf('/') + 1) || safeUrl
  try {
    return decodeURIComponent(raw)
  } catch {
    return raw
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.yagl-page {
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

.yagl-table {
  margin-top: 8px;
}

.yagl-table :deep(.el-table__cell) {
  border-right: 1px solid var(--el-table-border-color);
}

.yagl-table :deep(.el-table__header .el-table__cell:first-child),
.yagl-table :deep(.el-table__body .el-table__cell:first-child) {
  border-left: 1px solid var(--el-table-border-color);
}

.yagl-form :deep(.el-form-item__content) {
  max-width: 560px;
}

.yagl-form :deep(.el-input) {
  width: 560px;
}

.file-name-list {
  display: grid;
  grid-template-columns: 1fr;
  align-items: start;
  gap: 6px;
}

.file-name-link {
  display: flex;
  width: 100%;
  max-width: 100%;
  text-align: left;
  align-items: flex-start;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid var(--el-border-color);
  background: var(--el-fill-color);
  font-size: 12px;
  line-height: 18px;
  white-space: normal;
  word-break: break-all;
  overflow-wrap: anywhere;
}

.detail-file-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-file-item :deep(.el-link__inner) {
  white-space: normal;
  word-break: break-all;
  overflow-wrap: anywhere;
  text-align: left;
}

.yagl-dialog :deep(.el-upload-list__item-file-name) {
  max-width: none;
  white-space: normal;
  word-break: break-all;
  overflow-wrap: anywhere;
  line-height: 18px;
}

.empty-block {
  margin: 12px 0;
}
</style>
