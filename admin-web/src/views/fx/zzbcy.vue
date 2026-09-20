<template>
  <div class="zzbcy-page">
    <ContentWrap>
      <el-form ref="queryFormRef" class="query-form -mb-15px" :model="queryParams" :inline="true" label-width="70px">
        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入姓名"
            clearable
            @keyup.enter="handleSearch"
            class="!w-220px"
          />
        </el-form-item>
        <el-form-item label="职务" prop="title">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入职务"
            clearable
            @keyup.enter="handleSearch"
            class="!w-220px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleSearch">
            <Icon icon="ep:search" class="mr-5px" />
            搜索
          </el-button>
          <el-button @click="handleReset">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <div class="header-row">
        <div class="title">防汛防旱指挥部成员</div>
        <div class="actions">
          <el-button type="primary" @click="openCreateDialog">+ 添加</el-button>
          <el-button type="warning" plain @click="openImportDialog">
            <Icon icon="ep:upload" class="mr-5px" />
            导入
          </el-button>
          <el-button type="success" plain :loading="exportLoading" @click="handleExport">
            导出
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        row-key="id"
        :span-method="spanMethod"
        class="member-table"
      >
        <el-table-column prop="name" label="姓名" align="center" min-width="140" />
        <el-table-column prop="title" label="职务" align="center" min-width="220" />
        <el-table-column prop="positionLabel" label="担任防指职务" align="center" min-width="160" />
        <el-table-column label="操作" align="center" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">修改</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && tableData.length === 0"
        description="暂无成员信息"
        class="empty-block"
      />
    </ContentWrap>

    <el-dialog
      v-model="dialogVisible"
      title="指挥部成员-表单"
      width="600px"
      class="zzbcy-dialog"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        class="zzbcy-form"
        :model="form"
        :rules="rules"
        label-width="130px"
        label-position="left"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="职务" prop="title">
          <el-input v-model="form.title" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="担任防指职务" prop="position">
          <el-select v-model="form.position" placeholder="请选择" filterable>
            <el-option
              v-for="item in positionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
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
      v-model="importVisible"
      title="批量导入防汛防旱指挥部成员"
      width="560px"
      destroy-on-close
      :close-on-click-modal="false"
    >
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
            <div>仅允许导入 xls、xlsx 格式文件。</div>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadFile, type UploadFiles } from 'element-plus'
import download from '@/utils/download'
import { getStrDictOptions } from '@/utils/dict'
import {
  createFxZzbcy,
  deleteFxZzbcy,
  exportFxZzbcyExcel,
  getFxZzbcyDetail,
  getFxZzbcyImportTemplate,
  getFxZzbcyList,
  importFxZzbcyExcel,
  updateFxZzbcy,
  type FxZzbcyImportRespVO,
  type FxZzbcyListReqVO,
  type FxZzbcyListRespVO,
  type FxZzbcySaveReqVO
} from '@/api/fx/zzbcy'

defineOptions({ name: 'FxZzbcy' })

const loading = ref(false)
const exportLoading = ref(false)
const list = ref<FxZzbcyListRespVO[]>([])
const rowSpanMap = ref<number[]>([])
const queryFormRef = ref<FormInstance>()

const queryParams = reactive<FxZzbcyListReqVO>({
  name: '',
  title: ''
})

const positionOptions = computed(() => getStrDictOptions('zd_zzbgw'))
const positionLabelMap = computed(() => {
  const map = new Map<string, string>()
  positionOptions.value.forEach((item) => {
    map.set(String(item.value), item.label)
  })
  return map
})

const tableData = computed(() =>
  list.value.map((item) => ({
    ...item,
    positionLabel: positionLabelMap.value.get(item.position) || item.position || '-'
  }))
)

// 仅合并“担任防指职务”列，保持展示与排序一致
const spanMethod = ({ column, rowIndex }: { column: { property?: string }; rowIndex: number }) => {
  if (column.property !== 'positionLabel') return
  const rowspan = rowSpanMap.value[rowIndex] ?? 1
  return { rowspan, colspan: rowspan > 0 ? 1 : 0 }
}

const buildRowSpanMap = (rows: FxZzbcyListRespVO[]) => {
  rowSpanMap.value = []
  let lastPosition = ''
  let lastIndex = -1
  rows.forEach((row, index) => {
    const current = row.position || ''
    if (index === 0 || current !== lastPosition) {
      rowSpanMap.value[index] = 1
      lastPosition = current
      lastIndex = index
      return
    }
    rowSpanMap.value[lastIndex] += 1
    rowSpanMap.value[index] = 0
  })
}

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getFxZzbcyList({
      name: queryParams.name?.trim() || undefined,
      title: queryParams.title?.trim() || undefined
    })
    list.value = Array.isArray(data) ? data : []
    buildRowSpanMap(list.value)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchList()
}

const handleReset = () => {
  queryFormRef.value?.resetFields()
  fetchList()
}

const dialogVisible = ref(false)
const submitting = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()

const form = reactive<FxZzbcySaveReqVO>({
  id: '',
  position: '',
  name: '',
  title: ''
})

const rules: FormRules = {
  position: [{ required: true, message: '请选择担任防指职务', trigger: 'change' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const resetForm = () => {
  form.id = ''
  form.position = ''
  form.name = ''
  form.title = ''
  formRef.value?.clearValidate()
}

const openCreateDialog = () => {
  formMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = async (row: FxZzbcyListRespVO) => {
  formMode.value = 'edit'
  resetForm()
  dialogVisible.value = true
  const detail = await getFxZzbcyDetail(row.id)
  form.id = detail.id || ''
  form.position = detail.position || ''
  form.name = detail.name || ''
  form.title = detail.title || ''
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload: FxZzbcySaveReqVO = { ...form }
    if (formMode.value === 'edit') {
      await updateFxZzbcy(payload)
      ElMessage.success('修改成功')
    } else {
      await createFxZzbcy(payload)
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
    await ElMessageBox.confirm('确认删除该成员吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteFxZzbcy(id)
  ElMessage.success('删除成功')
  await fetchList()
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    const data = await exportFxZzbcyExcel()
    download.excel(data, '防汛防旱指挥部成员.xls')
  } finally {
    exportLoading.value = false
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
  const data = await getFxZzbcyImportTemplate()
  download.excel(data, '防汛防旱指挥部成员导入模板.xls')
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
    const result = (await importFxZzbcyExcel(formData)) as FxZzbcyImportRespVO
    const errors = Array.isArray(result?.errors) ? result.errors : []
    const errorText = errors.length ? `<br/>${errors.map((item) => `- ${item}`).join('<br/>')}` : ''
    await ElMessageBox.alert(
      `导入完成：总计 ${result?.totalCount || 0} 条，成功 ${result?.successCount || 0} 条，失败 ${result?.failureCount || 0} 条${errorText}`,
      '导入结果',
      { dangerouslyUseHTMLString: true }
    )
    importVisible.value = false
    importFileList.value = []
    importRawFile.value = undefined
    await fetchList()
  } finally {
    importLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.zzbcy-page {
  min-height: 100%;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.query-form {
  margin-bottom: 10px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.member-table {
  margin-top: 8px;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.zzbcy-form :deep(.el-form-item__content) {
  max-width: 360px;
}

.zzbcy-form :deep(.el-form-item__label) {
  white-space: nowrap;
}

.zzbcy-form :deep(.el-input),
.zzbcy-form :deep(.el-select) {
  width: 360px;
}

.member-table :deep(.el-table__cell) {
  border-right: 1px solid var(--el-table-border-color);
}

.member-table :deep(.el-table__header .el-table__cell) {
  border-top: 1px solid var(--el-table-border-color);
}

.member-table :deep(.el-table__header .el-table__cell:first-child),
.member-table :deep(.el-table__body .el-table__cell:first-child) {
  border-left: 1px solid var(--el-table-border-color);
}

.empty-block {
  margin: 12px 0;
}
</style>
