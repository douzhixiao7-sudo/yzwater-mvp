<template>
  <div class="fx-zhb-page">
    <ContentWrap>
      <div class="header-row">
        <div class="title-area">
          <div class="name-row">
            <span class="name">{{ currentDetail?.name || '未配置指挥部' }}</span>
            <span v-if="currentDetail" class="meta">
              （区号：{{ currentDetail?.areaCode || '-' }} 邮编：{{ currentDetail?.zipCode || '-' }}）
            </span>
            <el-button v-if="currentDetail" link type="primary" @click="openEditDialog">修改</el-button>
          </div>
          <div v-if="showSelector" class="selector-row">
            <span class="label">指挥部：</span>
            <el-select v-model="currentId" placeholder="请选择指挥部" class="select-long">
              <el-option v-for="item in departmentList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </div>
        </div>
        <div class="actions">
          <el-button type="primary" plain @click="openCreateDialog">新增</el-button>
          <el-button type="success" plain :disabled="departmentList.length === 0" @click="handleExport">
            导出指挥部
          </el-button>
          <el-button type="success" plain :disabled="!currentId" @click="handleExportMembers">
            导出成员
          </el-button>
          <el-button type="danger" plain :disabled="!currentId" @click="handleDelete">删除</el-button>
        </div>
      </div>

      <div class="info-block">
        <div class="info-line">
          <span class="label">地址：</span>
          <span>{{ currentDetail?.addr || '-' }}</span>
        </div>
        <div class="info-line">
          <span class="label">值班电话：</span>
          <span>{{ currentDetail?.tel || '-' }}</span>
          <span class="label second">传真电话：</span>
          <span>{{ currentDetail?.fax || '-' }}</span>
        </div>
      </div>

      <el-divider />

      <el-table v-loading="tableLoading" :data="memberList" class="member-table">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="名称" align="center" min-width="160" prop="name" show-overflow-tooltip />
        <el-table-column label="职务" align="center" min-width="200" prop="title" show-overflow-tooltip />
        <el-table-column label="电话" align="center" min-width="160" prop="tel" show-overflow-tooltip />
      </el-table>

      <el-empty v-if="!tableLoading && memberList.length === 0" description="暂无成员信息" class="empty-block" />
    </ContentWrap>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="920px" destroy-on-close :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" label-position="left">
        <el-row :gutter="16">
          <el-col :span="24">
            <div class="group-title">基础信息</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入单位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="区号" prop="areaCode">
              <el-input v-model="form.areaCode" placeholder="请输入区号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮政编码" prop="zipCode">
              <el-input v-model="form.zipCode" placeholder="请输入邮政编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="值班电话" prop="tel">
              <el-input v-model="form.tel" placeholder="请输入值班电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="传真电话" prop="fax">
              <el-input v-model="form.fax" placeholder="请输入传真电话" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="地址" prop="addr">
              <el-input v-model="form.addr" placeholder="请输入地址" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="group-title member-title">
              <span>成员信息</span>
              <el-button link type="primary" @click="addMember">+ 添加成员</el-button>
            </div>
          </el-col>
          <el-col :span="24">
            <el-table :data="form.members" border class="member-edit-table">
              <el-table-column type="index" label="序号" width="70" align="center" />
              <el-table-column label="名称" min-width="160">
                <template #default="{ row }">
                  <el-input v-model="row.name" placeholder="请输入姓名" />
                </template>
              </el-table-column>
              <el-table-column label="职务" min-width="220">
                <template #default="{ row }">
                  <el-input v-model="row.title" placeholder="请输入职务" />
                </template>
              </el-table-column>
              <el-table-column label="电话" min-width="160">
                <template #default="{ row }">
                  <el-input v-model="row.tel" placeholder="请输入电话" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" @click="removeMember($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import download from '@/utils/download'
import {
  createFxZhb,
  deleteFxZhb,
  exportFxZhbExcel,
  exportFxZhbMemberExcel,
  getFxZhbDetail,
  getFxZhbPage,
  type FxZhbDetailRespVO,
  type FxZhbMemberSaveReqVO,
  type FxZhbMemberExportReqVO,
  type FxZhbPageReqVO,
  type FxZhbPageRespVO,
  type FxZhbSaveReqVO,
  updateFxZhb
} from '@/api/fx/commandDepartment'

const queryParams = reactive<FxZhbPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: '',
  areaCode: ''
})

const departmentList = ref<FxZhbPageRespVO[]>([])
const currentId = ref<string>('')
const currentDetail = ref<FxZhbDetailRespVO | null>(null)
const tableLoading = ref(false)

const showSelector = computed(() => departmentList.value.length > 1)
const memberList = computed(() => currentDetail.value?.members || [])

const fetchDepartments = async () => {
  tableLoading.value = true
  try {
    const res = await getFxZhbPage(queryParams)
    departmentList.value = res?.list || []
    if (!departmentList.value.length) {
      currentId.value = ''
      currentDetail.value = null
      return
    }
    if (!currentId.value || !departmentList.value.find((item) => item.id === currentId.value)) {
      currentId.value = departmentList.value[0].id
    }
  } finally {
    tableLoading.value = false
  }
}

const fetchDetail = async (id: string) => {
  if (!id) {
    currentDetail.value = null
    return
  }
  tableLoading.value = true
  try {
    const res = await getFxZhbDetail(id)
    currentDetail.value = res || null
  } finally {
    tableLoading.value = false
  }
}

watch(
  () => currentId.value,
  (val) => {
    if (val) {
      fetchDetail(val)
    } else {
      currentDetail.value = null
    }
  }
)

const dialogVisible = ref(false)
const submitting = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()

const form = reactive<FxZhbSaveReqVO>({
  id: '',
  name: '',
  addr: '',
  tel: '',
  fax: '',
  areaCode: '',
  zipCode: '',
  sort: undefined,
  members: []
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入单位名称', trigger: 'blur' }]
}

const dialogTitle = computed(() => (formMode.value === 'edit' ? '编辑防汛指挥部' : '新增防汛指挥部'))

const resetForm = () => {
  form.id = ''
  form.name = ''
  form.addr = ''
  form.tel = ''
  form.fax = ''
  form.areaCode = ''
  form.zipCode = ''
  form.sort = undefined
  form.members = []
}

const openCreateDialog = () => {
  formMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = () => {
  if (!currentDetail.value) return
  formMode.value = 'edit'
  resetForm()
  const detail = currentDetail.value
  form.id = detail.id
  form.name = detail.name || ''
  form.addr = detail.addr || ''
  form.tel = detail.tel || ''
  form.fax = detail.fax || ''
  form.areaCode = detail.areaCode || ''
  form.zipCode = detail.zipCode || ''
  form.sort = detail.sort
  form.members = Array.isArray(detail.members) ? detail.members.map((item) => ({ ...item })) : []
  dialogVisible.value = true
}

const normalizeMembers = (members: FxZhbMemberSaveReqVO[]) => {
  return (members || [])
    .map((item) => ({
      ...item,
      name: String(item?.name || '').trim(),
      title: String(item?.title || '').trim(),
      tel: String(item?.tel || '').trim()
    }))
    .filter((item) => item.name || item.title || item.tel)
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload: FxZhbSaveReqVO = {
      ...form,
      members: normalizeMembers(form.members || [])
    }
    if (formMode.value === 'edit') {
      await updateFxZhb(payload)
      ElMessage.success('编辑成功')
    } else {
      await createFxZhb(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchDepartments()
    if (currentId.value) {
      await fetchDetail(currentId.value)
    }
  } finally {
    submitting.value = false
  }
}

const handleDelete = async () => {
  if (!currentId.value) return
  try {
    await ElMessageBox.confirm('确认删除当前指挥部信息吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteFxZhb(currentId.value)
  ElMessage.success('删除成功')
  await fetchDepartments()
  if (currentId.value) {
    await fetchDetail(currentId.value)
  }
}

const handleExport = async () => {
  try {
    const data = await exportFxZhbExcel(queryParams)
    download.excel(data, '防汛指挥部.xls')
  } catch {
    // 下载失败交给全局拦截提示
  }
}

const handleExportMembers = async () => {
  if (!currentId.value) return
  try {
    const params: FxZhbMemberExportReqVO = {
      commandDepartmentId: currentId.value
    }
    const data = await exportFxZhbMemberExcel(params)
    download.excel(data, '防汛指挥部成员.xls')
  } catch {
    // 下载失败交给全局拦截提示
  }
}

const addMember = () => {
  if (!form.members) {
    form.members = []
  }
  form.members.push({ name: '', title: '', tel: '' })
}

const removeMember = (index: number) => {
  if (!form.members || index < 0 || index >= form.members.length) return
  form.members.splice(index, 1)
}

onMounted(async () => {
  await fetchDepartments()
  if (currentId.value) {
    await fetchDetail(currentId.value)
  }
})
</script>

<style scoped>
.fx-zhb-page {
  min-height: 100%;
}

.header-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.title-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.meta {
  font-size: 13px;
  font-weight: 400;
  color: #909399;
}

.selector-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.select-long {
  width: 260px;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.info-block {
  margin-top: 8px;
  line-height: 1.8;
  color: #303133;
}

.info-line {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.info-line .label {
  font-weight: 600;
  color: #606266;
}

.info-line .label.second {
  margin-left: 24px;
}

.group-title {
  font-weight: 600;
  margin: 10px 0 12px;
  padding: 6px 10px;
  color: #303133;
  background: #f5f7fa;
  border-left: 4px solid #409eff;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.member-table {
  margin-top: 8px;
}

.member-edit-table {
  width: 100%;
}

.empty-block {
  margin: 12px 0;
}
</style>
