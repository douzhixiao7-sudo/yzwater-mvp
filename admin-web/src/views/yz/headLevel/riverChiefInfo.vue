<template>
  <div class="river-page">
    <ContentWrap class="summary-wrap">
      <div class="summary-hero">
        <div class="summary-title">总河长</div>
        <el-button type="primary" @click="openCreateTotalChiefDialog">
          <Icon icon="ep:plus" class="mr-5px" />
          新增总河长
        </el-button>
      </div>

      <div v-loading="totalChiefLoading" class="summary-board">
        <div v-if="totalChiefList.length" class="summary-grid">
          <div v-for="item in totalChiefList" :key="item.id" class="summary-card">
            <div class="summary-card__main">
              <div class="summary-card__name">{{ item.headName || '-' }}</div>
              <div class="summary-card__level">
                河长级别：{{ resolveHeadLevelLabel(item.headLevel) || '未填写' }}
              </div>
              <div class="summary-card__position">{{ item.headPosition || '未填写职务' }}</div>
            </div>
            <div class="summary-card__actions">
              <el-button link type="primary" @click="openEditTotalChiefDialog(item)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteTotalChief(item)">删除</el-button>
            </div>
          </div>
        </div>

        <el-empty v-else description="暂无总河长信息，可点击右上角新增" :image-size="84" />
      </div>
    </ContentWrap>

    <ContentWrap class="query-wrap">
      <el-form class="query-form -mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="河长姓名" prop="headName">
          <el-input
            v-model="queryParams.headName"
            placeholder="请输入河长姓名"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item label="河长级别" prop="headLevel">
          <el-select
            v-model="queryParams.headLevel"
            multiple
            collapse-tags
            collapse-tags-tooltip
            :max-collapse-tags="2"
            placeholder="请选择河长级别"
            clearable
            class="!w-240px"
          >
            <el-option v-for="item in headLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="设施名称" prop="referenceName">
          <div class="flex items-center gap-8px nowrap-inline">
            <el-select v-model="queryParams.referenceType" placeholder="选择类型" clearable class="!w-120px">
              <el-option label="水库" value="reservoir" />
              <el-option label="河道" value="river" />
            </el-select>
            <el-input
              v-model="queryParams.referenceName"
              placeholder="请输入设施名称"
              clearable
              @keyup.enter="handleSearch"
              class="!w-240px"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleSearch"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="handleReset"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
          <el-button type="primary" plain @click="openCreateDialog"><Icon icon="ep:plus" class="mr-5px" /> 新增</el-button>
          <el-button type="success" plain @click="openImportDialog"><Icon icon="ep:upload" class="mr-5px" /> 导入</el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="table-wrap">
      <el-table class="river-table" v-loading="tableLoading" :data="tableData">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="河长姓名" align="center" min-width="140" prop="headName" show-overflow-tooltip />
        <el-table-column label="河长级别" align="center" min-width="120">
          <template #default="{ row }">
            {{ resolveHeadLevelLabel(row.headLevel) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="河长职务" align="center" min-width="150" prop="headPosition" show-overflow-tooltip />
        <el-table-column label="关联设施" align="center" min-width="320" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.facilitySummary || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="warning" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="fetchTable" />
    </ContentWrap>

    <el-dialog
      class="facility-dialog"
      v-model="detailDialogVisible"
      title="河长详情"
      width="860px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div v-loading="detailLoading" class="detail-body">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="河长姓名">{{ detailData?.headName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="河长级别">{{ resolveHeadLevelLabel(detailData?.headLevel) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="河长职务">{{ detailData?.headPosition || '-' }}</el-descriptions-item>
          <el-descriptions-item label="工作单位">{{ detailData?.headUnit || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section-title">关联设施</div>
        <el-table class="river-table" :data="detailData?.facilities || []" max-height="320">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="关联设施" align="center" min-width="120">
            <template #default="{ row }">
              {{ row.referenceTypeLabel || resolveReferenceTypeLabel(row.referenceType) || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="关联设施名称" align="center" min-width="240" prop="referenceName" show-overflow-tooltip />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑 -->
    <el-dialog class="facility-dialog" v-model="createDialogVisible" :title="dialogTitle" width="860px" destroy-on-close :close-on-click-modal="false">
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="120px"
        label-position="left"
        :disabled="formReadonly"
      >
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="河长姓名" prop="headName">
              <el-input v-model="createForm.headName" placeholder="请输入河长姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="河长级别" prop="headLevel">
              <el-select v-model="createForm.headLevel" placeholder="请选择" clearable class="!w-240px">
                <el-option v-for="opt in headLevelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="关联设施">
              <div class="facility-panel">
                <div class="facility-draft-row">
                  <el-select
                    v-model="createForm.referenceType"
                    placeholder="请选择设施类型"
                    class="!w-140px"
                    @change="handleReferenceTypeChange"
                    :disabled="referenceReadonly"
                  >
                    <el-option label="河道/河段" value="river" />
                    <el-option label="水库" value="reservoir" />
                  </el-select>

                  <template v-if="createForm.referenceType === 'river'">
                    <el-tree-select
                      v-model="riverReferenceValue"
                      :data="riverReferenceTreeData"
                      :props="riverReferenceProps"
                      node-key="value"
                      check-strictly
                      filterable
                      :filter-node-method="filterRiverReferenceNode"
                      clearable
                      :default-expanded-keys="riverReferenceExpandedKeys"
                      :disabled="referenceReadonly"
                      placeholder="请选择河道或河段"
                      class="draft-selector"
                      @visible-change="handleRiverReferenceVisibleChange"
                      @change="handleRiverReferenceChange"
                    >
                      <template #default="{ data }">
                        <div class="river-reference-node">
                          <span class="river-reference-box" :class="{ 'is-checked': riverReferenceValue === data.value }"></span>
                          <span class="river-reference-label">{{ data.label }}</span>
                        </div>
                      </template>
                    </el-tree-select>
                    <el-button type="primary" plain @click="handleAddRiverFacility">添加</el-button>
                  </template>

                  <template v-else>
                    <el-input v-model="referenceNameDisplay" placeholder="请选择水库" class="draft-selector" readonly />
                    <el-button type="primary" plain @click="openReferencePicker" :disabled="referenceReadonly">选择水库</el-button>
                  </template>
                </div>
                <div class="facility-tip">支持关联多个河道、河段、水库；编辑时可增删关联。</div>
                <el-table class="river-table mt-8px" :data="selectedFacilities" max-height="260">
                  <el-table-column type="index" label="序号" width="70" align="center" />
                  <el-table-column label="关联设施" align="center" min-width="120" prop="referenceTypeLabel" />
                  <el-table-column label="关联设施名称" align="center" min-width="260" prop="referenceName" show-overflow-tooltip />
                  <el-table-column label="操作" width="100" align="center">
                    <template #default="{ row }">
                      <el-button link type="danger" @click="removeFacility(row)">移除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="河长职务" prop="headPosition">
              <el-input v-model="createForm.headPosition" placeholder="请输入河长职务" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作单位" prop="headUnit">
              <el-input v-model="createForm.headUnit" placeholder="请输入工作单位" />
            </el-form-item>
          </el-col>
          <el-col v-if="formMode === 'create' && hasReservoirFacility" :span="24">
            <el-form-item label="河长职责" prop="responsibilities">
              <el-input v-model="createForm.responsibilities" type="textarea" :rows="4" placeholder="请输入河长职责" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">关闭</el-button>
        <el-button v-if="!formReadonly" type="primary" :loading="createSubmitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      class="facility-dialog total-chief-dialog"
      v-model="totalChiefDialogVisible"
      :title="totalChiefDialogTitle"
      width="760px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="total-chief-dialog__shell">
        <el-form
          ref="totalChiefFormRef"
          :model="totalChiefForm"
          :rules="totalChiefRules"
          label-width="108px"
          label-position="left"
        >
          <el-row :gutter="14">
            <el-col :span="12">
              <el-form-item label="河长姓名" prop="headName">
                <el-input v-model="totalChiefForm.headName" placeholder="请输入河长姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="河长级别" prop="headLevel">
                <el-select
                  v-model="totalChiefForm.headLevel"
                  placeholder="请选择河长级别"
                  clearable
                  class="!w-full"
                >
                  <el-option v-for="opt in headLevelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="河长职务" prop="headPosition">
                <el-input v-model="totalChiefForm.headPosition" placeholder="请输入河长职务" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="totalChiefDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="totalChiefSubmitting" @click="handleSubmitTotalChief">保存</el-button>
      </template>
    </el-dialog>

    <!-- 关联设施选择 -->
    <el-dialog class="facility-dialog" v-model="pickerVisible" :title="pickerTitle" width="860px" destroy-on-close :close-on-click-modal="false">
      <el-form :inline="true" class="-mb-15px" :model="pickerQuery">
        <el-form-item label="名称">
          <el-input v-model="pickerQuery.keyword" placeholder="请输入名称关键字" clearable @keyup.enter="fetchPickerTable" class="!w-260px" />
        </el-form-item>
        <el-form-item>
          <el-button @click="fetchPickerTable"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="resetPickerQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        </el-form-item>
      </el-form>

      <el-table class="river-table" v-loading="pickerLoading" :data="pickerTableData" @row-dblclick="confirmPick">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="reservoirName" label="水库名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="confirmPick(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="pickerTotal"
        v-model:page="pickerQuery.pageNo"
        v-model:limit="pickerQuery.pageSize"
        @pagination="fetchPickerTable"
      />

      <template #footer>
        <el-button @click="pickerVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog class="facility-dialog" v-model="importVisible" title="导入河长信息" width="560px" destroy-on-close :close-on-click-modal="false">
      <el-upload
        class="import-uploader"
        drag
        :limit="1"
        :auto-upload="false"
        :show-file-list="true"
        accept=".xls,.xlsx"
        :file-list="importFileList"
        :on-change="handleImportFileChange"
        :on-remove="handleImportFileRemove"
        :on-exceed="handleImportExceed"
      >
        <Icon icon="ep:upload" class="mb-8px" />
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">
            <div>仅允许导入 xls、xlsx 格式文件。</div>
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
import {
  createRiverChiefInfo,
  createRiverChiefTotalChief,
  deleteRiverChiefInfo,
  deleteRiverChiefTotalChief,
  getRiverChiefInfoDetail,
  getRiverChiefInfoPage,
  getRiverChiefTotalChiefList,
  importRiverChiefInfoExcel,
  updateRiverChiefInfo,
  updateRiverChiefTotalChief,
  type RiverChiefInfoCreateReqVO,
  type RiverChiefInfoDetailRespVO,
  type RiverChiefInfoFacilitySaveReqVO,
  type RiverChiefInfoImportRespVO,
  type RiverChiefInfoPageReqVO,
  type RiverChiefInfoPageRespVO,
  type RiverChiefInfoTotalChiefRespVO,
  type RiverChiefInfoTotalChiefSaveReqVO,
  type RiverChiefInfoUpdateReqVO
} from '@/api/gis/riverChiefInfo'
import {
  getRiverChannelSimpleList,
  getRiverDict,
  getRiverSectionWithChannelSimpleList,
  type DictDataItemRespVO,
  type RiverChannelSimpleRespVO,
  type RiverSectionWithChannelSimpleRespVO
} from '@/api/gis/riverChannel'
import { getReservoirPage, type ReservoirPageRespVO } from '@/api/gis/reservoir'

const queryParams = reactive<RiverChiefInfoPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  headName: '',
  headLevel: [],
  referenceType: '',
  referenceName: ''
})
const queryFormRef = ref<FormInstance>()

const tableData = ref<RiverChiefInfoPageRespVO[]>([])
const total = ref(0)
const tableLoading = ref(false)

const totalChiefList = ref<RiverChiefInfoTotalChiefRespVO[]>([])
const totalChiefLoading = ref(false)

const headLevelOptions = ref<DictDataItemRespVO[]>([])
const headLevelLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  for (const item of headLevelOptions.value) {
    if (!item?.value) continue
    map[item.value] = item.label || item.value
  }
  return map
})

const resolveHeadLevelLabel = (val?: string) => {
  if (!val) return ''
  return headLevelLabelMap.value[val] || val
}

const resolveReferenceTypeLabel = (type?: string) => {
  if (!type) return ''
  if (type === 'river') return '河道'
  if (type === 'river_section') return '河段'
  if (type === 'reservoir') return '水库'
  return type
}

const fetchTotalChiefList = async () => {
  totalChiefLoading.value = true
  try {
    totalChiefList.value = (await getRiverChiefTotalChiefList()) || []
  } finally {
    totalChiefLoading.value = false
  }
}

const fetchTable = async () => {
  if (!validateReferenceFilter()) {
    return
  }
  tableLoading.value = true
  try {
    const res = await getRiverChiefInfoPage(queryParams)
    tableData.value = res?.list || []
    total.value = res?.total || 0
  } finally {
    tableLoading.value = false
  }
}

const loadDict = async () => {
  const hzjb = await getRiverDict('zd_hzjb')
  headLevelOptions.value = hzjb || []
}

onMounted(async () => {
  await Promise.all([loadDict(), fetchTotalChiefList()])
  fetchTable()
})

const validateReferenceFilter = () => {
  const hasName = !!(queryParams.referenceName || '').trim()
  const hasType = !!(queryParams.referenceType || '').trim()
  if (hasName && !hasType) {
    ElMessage.warning('请先选择关联设施类型，再输入设施名称')
    return false
  }
  return true
}

const handleSearch = () => {
  queryParams.pageNo = 1
  fetchTable()
}

const handleReset = () => {
  queryFormRef.value?.resetFields()
  queryParams.referenceType = ''
  queryParams.referenceName = ''
  queryParams.pageNo = 1
  queryParams.pageSize = 10
  fetchTable()
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

const submitImport = async () => {
  if (!importRawFile.value) {
    ElMessage.warning('请先上传导入文件')
    return
  }
  importLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', importRawFile.value)
    const result = (await importRiverChiefInfoExcel(formData)) as unknown as RiverChiefInfoImportRespVO
    const errors = Array.isArray(result?.errors) ? result.errors : []
    const errorText = errors.length ? `<br/>${errors.map((item) => `- ${item}`).join('<br/>')}` : ''
    await ElMessageBox.alert(
      `导入完成：总计 ${result?.totalCount || 0} 条，成功 ${result?.successCount || 0} 条，跳过 ${result?.skipCount || 0} 条，失败 ${result?.failureCount || 0} 条${errorText}`,
      '导入结果',
      { dangerouslyUseHTMLString: true }
    )
    importVisible.value = false
    importFileList.value = []
    importRawFile.value = undefined
    await fetchTable()
  } finally {
    importLoading.value = false
  }
}

const handleDelete = async (row: RiverChiefInfoPageRespVO) => {
  await ElMessageBox.confirm(`确认删除河长【${row.headName || ''}】吗？`, '提示', { type: 'warning' })
  await deleteRiverChiefInfo(row.id)
  ElMessage.success('删除成功')
  fetchTable()
}

type TotalChiefFormModel = {
  id?: string
  headName: string
  headLevel: string
  headPosition: string
}

const totalChiefDialogVisible = ref(false)
const totalChiefSubmitting = ref(false)
const totalChiefFormRef = ref<FormInstance>()
const totalChiefMode = ref<'create' | 'edit'>('create')
const totalChiefDialogTitle = computed(() => (totalChiefMode.value === 'edit' ? '编辑总河长' : '新增总河长'))
const totalChiefForm = reactive<TotalChiefFormModel>({
  id: undefined,
  headName: '',
  headLevel: '',
  headPosition: ''
})

const totalChiefRules: FormRules = {
  headName: [{ required: true, message: '请输入河长姓名', trigger: 'blur' }],
  headLevel: [{ required: true, message: '请选择河长级别', trigger: 'change' }]
}

const resetTotalChiefForm = () => {
  totalChiefForm.id = undefined
  totalChiefForm.headName = ''
  totalChiefForm.headLevel = ''
  totalChiefForm.headPosition = ''
}

const openCreateTotalChiefDialog = () => {
  totalChiefMode.value = 'create'
  resetTotalChiefForm()
  totalChiefDialogVisible.value = true
}

const openEditTotalChiefDialog = (row: RiverChiefInfoTotalChiefRespVO) => {
  totalChiefMode.value = 'edit'
  resetTotalChiefForm()
  totalChiefForm.id = String(row.id || '')
  totalChiefForm.headName = row.headName || ''
  totalChiefForm.headLevel = row.headLevel || ''
  totalChiefForm.headPosition = row.headPosition || ''
  totalChiefDialogVisible.value = true
}

const handleSubmitTotalChief = async () => {
  await totalChiefFormRef.value?.validate()
  totalChiefSubmitting.value = true
  try {
    const payload: RiverChiefInfoTotalChiefSaveReqVO = {
      id: totalChiefMode.value === 'edit' ? totalChiefForm.id : undefined,
      headName: totalChiefForm.headName,
      headLevel: totalChiefForm.headLevel,
      headPosition: totalChiefForm.headPosition || undefined
    }
    if (totalChiefMode.value === 'edit') {
      await updateRiverChiefTotalChief(payload)
      ElMessage.success('总河长信息已更新')
    } else {
      await createRiverChiefTotalChief(payload)
      ElMessage.success('总河长已新增')
    }
    totalChiefDialogVisible.value = false
    await fetchTotalChiefList()
  } finally {
    totalChiefSubmitting.value = false
  }
}

const handleDeleteTotalChief = async (row: RiverChiefInfoTotalChiefRespVO) => {
  await ElMessageBox.confirm(`确认删除总河长【${row.headName || ''}】吗？`, '提示', { type: 'warning' })
  await deleteRiverChiefTotalChief(row.id)
  ElMessage.success('总河长已删除')
  await fetchTotalChiefList()
}

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<RiverChiefInfoDetailRespVO>()

const handleDetail = async (row: RiverChiefInfoPageRespVO) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  try {
    detailData.value = await getRiverChiefInfoDetail(row.id)
  } finally {
    detailLoading.value = false
  }
}

const handleEdit = async (row: RiverChiefInfoPageRespVO) => {
  await openEditDialog(String(row.id))
}

// region 新增/编辑/详情弹窗

const createDialogVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref<FormInstance>()
const formMode = ref<'create' | 'edit'>('create')
const dialogTitle = computed(() => {
  if (formMode.value === 'edit') return '编辑河长'
  return '新增河长'
})
const formReadonly = computed(() => false)
const referenceReadonly = computed(() => false)

type RiverChiefInfoFormModel = {
  id: string
  referenceType: 'river' | 'reservoir'
  referenceId: string
  headName: string
  headLevel: string
  headPosition: string
  headUnit: string
  responsibilities: string
  remarks: string
}

type SelectedFacilityItem = {
  key: string
  referenceType: 'river' | 'river_section' | 'reservoir'
  referenceId: string
  referenceName: string
  referenceTypeLabel: string
}

const createForm = reactive<RiverChiefInfoFormModel>({
  id: '',
  referenceType: 'river',
  referenceId: '',
  headName: '',
  headLevel: '',
  headPosition: '',
  headUnit: '',
  responsibilities: '',
  remarks: ''
})

const selectedFacilities = ref<SelectedFacilityItem[]>([])
const hasReservoirFacility = computed(() =>
  selectedFacilities.value.some((item) => item.referenceType === 'reservoir')
)

const referenceNameDisplay = ref('')
type RiverReferenceType = 'river' | 'river_section'
type RiverReferenceTreeNode = {
  label: string
  value: string
  nodeType: RiverReferenceType
  referenceId: string
  sortTime: number
  children?: RiverReferenceTreeNode[]
}
const riverReferenceTreeData = ref<RiverReferenceTreeNode[]>([])
const riverReferenceProps = {
  label: 'label',
  value: 'value',
  children: 'children'
}
const riverReferenceTreeLoaded = ref(false)
const riverReferenceValue = ref('')
const riverReferenceExpandedKeys = computed<string[]>(() =>
  (riverReferenceTreeData.value || []).filter((item) => (item.children || []).length > 0).map((item) => item.value)
)

const createRules: FormRules = {
  headName: [{ required: true, message: '请输入河长姓名', trigger: 'blur' }],
  headLevel: [{ required: true, message: '请选择河长级别', trigger: 'change' }]
}

const resetCreateForm = () => {
  createForm.id = ''
  createForm.referenceType = 'river'
  createForm.referenceId = ''
  createForm.headName = ''
  createForm.headLevel = ''
  createForm.headPosition = ''
  createForm.headUnit = ''
  createForm.responsibilities = ''
  createForm.remarks = ''
  referenceNameDisplay.value = ''
  riverReferenceValue.value = ''
  selectedFacilities.value = []
}

const openCreateDialog = () => {
  resetCreateForm()
  formMode.value = 'create'
  createDialogVisible.value = true
  void ensureRiverReferenceTreeLoaded()
}

const normalizeFacilityType = (type?: string): SelectedFacilityItem['referenceType'] | '' => {
  const raw = String(type || '').trim().toLowerCase()
  if (raw === 'river' || raw === 'river_section' || raw === 'reservoir') {
    return raw
  }
  return ''
}

const appendFacility = (item: SelectedFacilityItem, silence = false) => {
  if (!item.referenceType || !item.referenceId) {
    return false
  }
  const exists = selectedFacilities.value.some((facility) => facility.key === item.key)
  if (exists) {
    if (!silence) {
      ElMessage.warning('该设施已添加，无需重复关联')
    }
    return false
  }
  selectedFacilities.value.push(item)
  if (!silence) {
    ElMessage.success('已添加关联设施')
  }
  return true
}

const fillCreateFormByDetail = async (detail: RiverChiefInfoDetailRespVO) => {
  createForm.id = detail.id || ''
  createForm.referenceType = 'river'
  createForm.referenceId = ''
  createForm.headName = detail.headName || ''
  createForm.headLevel = detail.headLevel || ''
  createForm.headPosition = detail.headPosition || ''
  createForm.headUnit = detail.headUnit || ''
  createForm.responsibilities = ''
  createForm.remarks = detail.remarks || ''
  selectedFacilities.value = []
  const facilities = Array.isArray(detail.facilities) ? detail.facilities : []
  for (const facility of facilities) {
    const type = normalizeFacilityType(facility.referenceType)
    const referenceId = String(facility.referenceId || '')
    if (!type || !referenceId) {
      continue
    }
    appendFacility(
      {
        key: `${type}:${referenceId}`,
        referenceType: type,
        referenceId,
        referenceName: facility.referenceName || '-',
        referenceTypeLabel: facility.referenceTypeLabel || resolveReferenceTypeLabel(type)
      },
      true
    )
  }
  const firstReservoir = selectedFacilities.value.find((item) => item.referenceType === 'reservoir')
  if (firstReservoir) {
    createForm.referenceType = 'reservoir'
  }
  createForm.referenceId = ''
  referenceNameDisplay.value = ''
  riverReferenceValue.value = ''
  await ensureRiverReferenceTreeLoaded()
}

const openEditDialog = async (id: string) => {
  resetCreateForm()
  formMode.value = 'edit'
  createDialogVisible.value = true
  const detail = await getRiverChiefInfoDetail(id)
  await fillCreateFormByDetail(detail)
}

const handleReferenceTypeChange = () => {
  createForm.referenceId = ''
  referenceNameDisplay.value = ''
  riverReferenceValue.value = ''
  if (createForm.referenceType === 'river') {
    void ensureRiverReferenceTreeLoaded()
  }
}

const removeFacility = (item: SelectedFacilityItem) => {
  selectedFacilities.value = selectedFacilities.value.filter((facility) => facility.key !== item.key)
}

const handleAddRiverFacility = () => {
  const parsed = parseRiverReferenceValue(riverReferenceValue.value)
  if (!parsed) {
    ElMessage.warning('请先选择河道或河段')
    return
  }
  const selectedNode = riverReferenceNodeMap.value[String(riverReferenceValue.value)]
  appendFacility({
    key: `${parsed.nodeType}:${parsed.referenceId}`,
    referenceType: parsed.nodeType,
    referenceId: parsed.referenceId,
    referenceName: selectedNode?.label || '-',
    referenceTypeLabel: resolveReferenceTypeLabel(parsed.nodeType)
  })
  createForm.referenceId = ''
  referenceNameDisplay.value = ''
  riverReferenceValue.value = ''
}

const buildRiverReferenceValue = (type: RiverReferenceType, referenceId: string) => `${type}:${referenceId}`

const buildRiverSectionDisplayLabel = (riverName: string, sectionName: string) => {
  const river = String(riverName || '').trim()
  const section = String(sectionName || '').trim()
  if (!section) return river
  if (!river) return section
  return `${river}/${section}`
}

const parseRiverReferenceValue = (value?: string) => {
  const raw = String(value || '')
  const [type, ...rest] = raw.split(':')
  const referenceId = rest.join(':')
  if (!referenceId) return null
  const nodeType: RiverReferenceType = type === 'river_section' ? 'river_section' : 'river'
  return { nodeType, referenceId }
}

const parseSortTime = (value?: string) => {
  const ms = Date.parse(String(value || ''))
  return Number.isFinite(ms) ? ms : 0
}

const filterRiverReferenceNode = (keyword: string, data: RiverReferenceTreeNode, node: any) => {
  const k = String(keyword || '').trim()
  if (!k) return true
  const label = String(data?.label || '')
  if (label.includes(k)) return true

  const children = Array.isArray(data?.children) ? data.children : []
  if (children.some((item) => String(item?.label || '').includes(k))) {
    return true
  }

  const parentLabel = String(node?.parent?.data?.label || '')
  if (parentLabel.includes(k)) {
    return true
  }
  return false
}

const riverReferenceNodeMap = computed<Record<string, RiverReferenceTreeNode>>(() => {
  const map: Record<string, RiverReferenceTreeNode> = {}
  const walk = (nodes?: RiverReferenceTreeNode[]) => {
    if (!nodes?.length) return
    for (const node of nodes) {
      if (!node) continue
      map[node.value] = node
      if (node.children?.length) walk(node.children)
    }
  }
  walk(riverReferenceTreeData.value)
  return map
})

const handleRiverReferenceVisibleChange = async (visible: boolean) => {
  if (!visible) return
  if (createForm.referenceType !== 'river') return
  await ensureRiverReferenceTreeLoaded()
}

const handleRiverReferenceChange = (value?: string) => {
  const parsed = parseRiverReferenceValue(value)
  if (!parsed) {
    createForm.referenceId = ''
    referenceNameDisplay.value = ''
    return
  }
  createForm.referenceId = parsed.referenceId
  const selectedNode = riverReferenceNodeMap.value[String(value)]
  referenceNameDisplay.value = selectedNode?.label || ''
}

const ensureRiverReferenceTreeLoaded = async () => {
  if (riverReferenceTreeLoaded.value) return
  try {
    const [riverList, sectionList] = await Promise.all([
      getRiverChannelSimpleList() as unknown as Promise<RiverChannelSimpleRespVO[]>,
      getRiverSectionWithChannelSimpleList() as unknown as Promise<RiverSectionWithChannelSimpleRespVO[]>
    ])
    const roots = new Map<string, RiverReferenceTreeNode>()
    for (const river of Array.isArray(riverList) ? riverList : []) {
      if (!river?.id) continue
      const riverId = String(river.id)
      roots.set(riverId, {
        label: river.riverName || riverId,
        value: buildRiverReferenceValue('river', riverId),
        nodeType: 'river',
        referenceId: riverId,
        sortTime: parseSortTime(river.updateTime),
        children: []
      })
    }

    for (const section of Array.isArray(sectionList) ? sectionList : []) {
      if (!section?.id) continue
      const riverId = String(section.riverChannelId || '')
      if (!riverId) continue
      let root = roots.get(riverId)
      if (!root) {
        const fallbackRiverName = section.riverName || `河道-${riverId}`
        root = {
          label: fallbackRiverName,
          value: buildRiverReferenceValue('river', riverId),
          nodeType: 'river',
          referenceId: riverId,
          sortTime: 0,
          children: []
        }
        roots.set(riverId, root)
      }
      const sectionName = section.sectionName || String(section.id)
      root.children = root.children || []
      root.children.push({
        label: buildRiverSectionDisplayLabel(root.label, sectionName),
        value: buildRiverReferenceValue('river_section', String(section.id)),
        nodeType: 'river_section',
        referenceId: String(section.id),
        sortTime: parseSortTime(section.updateTime)
      })
    }

    const sorted = Array.from(roots.values())
      .map((root) => ({
        ...root,
        children: (root.children || []).sort((a, b) => {
          if (b.sortTime !== a.sortTime) return b.sortTime - a.sortTime
          return String(a.label).localeCompare(String(b.label), 'zh-CN')
        })
      }))
      .sort((a, b) => {
        if (b.sortTime !== a.sortTime) return b.sortTime - a.sortTime
        return String(a.label).localeCompare(String(b.label), 'zh-CN')
      })
    riverReferenceTreeData.value = sorted
  } catch {
    riverReferenceTreeData.value = []
  } finally {
    riverReferenceTreeLoaded.value = true
  }
}

const handleSubmit = async () => {
  await createFormRef.value?.validate()
  if (!selectedFacilities.value.length && formMode.value !== 'edit') {
    ElMessage.warning('请至少选择一个关联设施')
    return
  }
  createSubmitting.value = true
  try {
    const facilities: RiverChiefInfoFacilitySaveReqVO[] = selectedFacilities.value.map((item) => ({
      referenceType: item.referenceType,
      referenceId: item.referenceId
    }))
    if (formMode.value === 'edit') {
      const payload: RiverChiefInfoUpdateReqVO = {
        id: createForm.id as string,
        headName: createForm.headName,
        headLevel: createForm.headLevel as string,
        headPosition: createForm.headPosition || undefined,
        headUnit: createForm.headUnit || undefined,
        remarks: createForm.remarks || undefined,
        facilities
      }
      await updateRiverChiefInfo(payload)
      ElMessage.success('编辑成功')
    } else {
      const payload: RiverChiefInfoCreateReqVO = {
        headName: createForm.headName,
        headLevel: createForm.headLevel,
        headPosition: createForm.headPosition || undefined,
        headUnit: createForm.headUnit || undefined,
        responsibilities: hasReservoirFacility.value ? createForm.responsibilities || undefined : undefined,
        remarks: createForm.remarks || undefined,
        facilities
      }
      await createRiverChiefInfo(payload)
      ElMessage.success('新增成功')
    }
    createDialogVisible.value = false
    fetchTable()
  } finally {
    createSubmitting.value = false
  }
}

// endregion

// region 水库选择器

const pickerVisible = ref(false)
const pickerTitle = computed(() => '选择水库')
const pickerQuery = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: ''
})
const pickerLoading = ref(false)
const pickerTotal = ref(0)
const pickerTableData = ref<ReservoirPageRespVO[]>([])

const resetPickerQuery = () => {
  pickerQuery.pageNo = 1
  pickerQuery.pageSize = 10
  pickerQuery.keyword = ''
  fetchPickerTable()
}

const openReferencePicker = () => {
  if (createForm.referenceType !== 'reservoir') {
    return
  }
  pickerQuery.pageNo = 1
  pickerQuery.pageSize = 10
  pickerQuery.keyword = ''
  pickerVisible.value = true
  fetchPickerTable()
}

const fetchPickerTable = async () => {
  pickerLoading.value = true
  try {
    const res = await getReservoirPage({
      pageNo: pickerQuery.pageNo,
      pageSize: pickerQuery.pageSize,
      reservoirName: pickerQuery.keyword || undefined
    })
    pickerTableData.value = res?.list || []
    pickerTotal.value = res?.total || 0
  } finally {
    pickerLoading.value = false
  }
}

const handleAddReservoirFacility = () => {
  const referenceId = String(createForm.referenceId || '')
  if (!referenceId) {
    ElMessage.warning('请先选择水库')
    return
  }
  appendFacility({
    key: `reservoir:${referenceId}`,
    referenceType: 'reservoir',
    referenceId,
    referenceName: referenceNameDisplay.value || '-',
    referenceTypeLabel: '水库'
  })
  createForm.referenceId = ''
  referenceNameDisplay.value = ''
}

const confirmPick = async (row: any) => {
  if (!row) return

  // 水库：回填并添加到已关联列表
  createForm.referenceId = String(row.id)
  referenceNameDisplay.value = row.reservoirName || ''
  handleAddReservoirFacility()
  pickerVisible.value = false
}

// endregion
</script>

<style scoped>
.river-page {
  --head-bg: #f4f7fc;
  --head-card-bg: #ffffff;
  --head-border: rgba(148, 163, 184, 0.28);
  --head-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
  --head-primary: #1e40af;
  --head-primary-soft: rgba(30, 64, 175, 0.08);
  --head-text-main: #1f2a37;
  --head-text-muted: #5b677a;

  padding: 4px 2px 10px;
  background: linear-gradient(180deg, #f8fbff 0%, var(--head-bg) 100%);
}

.query-wrap,
.table-wrap,
.summary-wrap {
  background: var(--head-card-bg);
  border: 1px solid var(--head-border);
  border-radius: 14px;
  box-shadow: var(--head-shadow);
}

.summary-wrap {
  overflow: hidden;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.12), transparent 34%),
    linear-gradient(135deg, #ffffff 0%, #f6faff 100%);
}

.query-wrap {
  margin-top: 14px;
}

.table-wrap {
  margin-top: 12px;
}

.summary-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 4px 2px 16px;
}

.summary-title {
  color: #163b76;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.summary-board {
  padding: 6px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 14px;
  padding: 18px;
}

.summary-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  min-height: 112px;
  padding: 18px 20px;
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.summary-card__main {
  min-width: 0;
}

.summary-card__name {
  color: #172554;
  font-size: 22px;
  font-weight: 800;
  line-height: 1.2;
}

.summary-card__position {
  margin-top: 10px;
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
}

.summary-card__level {
  display: inline-flex;
  align-items: center;
  margin-top: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
  background: rgba(59, 130, 246, 0.1);
}

.summary-card__actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 4px;
  flex-shrink: 0;
}

.query-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.query-form :deep(.el-form-item__label) {
  color: var(--head-text-main);
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
  box-shadow: 0 0 0 1px var(--head-primary) inset;
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
  background: var(--head-primary-soft);
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
  margin-bottom: 0;
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

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-section-title {
  margin-top: 4px;
  color: #1e3a8a;
  font-weight: 700;
}

.facility-panel {
  width: 100%;
  border: 1px dashed rgba(30, 64, 175, 0.35);
  border-radius: 10px;
  padding: 10px;
  background: rgba(30, 64, 175, 0.03);
}

.facility-draft-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.draft-selector {
  width: 360px;
  max-width: 100%;
}

.facility-tip {
  margin-top: 8px;
  color: var(--head-text-muted);
  font-size: 12px;
}

.total-chief-dialog__shell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.import-uploader :deep(.el-upload-dragger) {
  border-radius: 12px;
  border-color: rgba(30, 64, 175, 0.3);
}

.import-uploader :deep(.el-upload__tip) {
  margin-top: 8px;
  line-height: 1.7;
  color: var(--head-text-muted);
}

.river-reference-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 100%;
}

.river-reference-box {
  width: 14px;
  height: 14px;
  border-radius: 2px;
  border: 1px solid #dcdfe6;
  box-sizing: border-box;
  background: #fff;
  flex: 0 0 14px;
  position: relative;
}

.river-reference-box.is-checked {
  border-color: #409eff;
  background: #409eff;
}

.river-reference-box.is-checked::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 1px;
  width: 3px;
  height: 7px;
  border: solid #fff;
  border-width: 0 1px 1px 0;
  transform: rotate(45deg);
}

.river-reference-label {
  color: var(--head-text-main);
}

:deep(.el-tree-select__popper .river-reference-box.is-checked) {
  border-color: #409eff;
  background-color: #409eff;
}

@media (max-width: 768px) {
  .summary-hero,
  .summary-board,
  .summary-card {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }

  .summary-card__actions {
    flex-direction: row;
    justify-content: flex-start;
  }

  .query-form :deep(.el-input),
  .query-form :deep(.el-select),
  .query-form :deep(.el-tree-select) {
    width: 100% !important;
  }

  .query-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .facility-draft-row {
    align-items: stretch;
  }

  .draft-selector {
    width: 100%;
  }
}

.select-long {
  width: 100%;
}
</style>
