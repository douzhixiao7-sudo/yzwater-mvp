<template>
  <div class="river-page">
    <ContentWrap class="query-wrap">
      <el-form class="query-form -mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="河长名称" prop="headName">
          <el-input
            v-model="queryParams.headName"
            placeholder="请输入河长名称"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item label="河长级别" prop="headLevel">
          <el-select v-model="queryParams.headLevel" placeholder="请选择河长级别" clearable class="!w-240px">
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
        <el-form-item label="行政区划" prop="administrativeRegion">
          <el-tree-select
            v-model="queryParams.administrativeRegion"
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
            :max-collapse-tags="2"
            placeholder="请选择（可搜索）"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleSearch"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="handleReset"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
          <el-button type="primary" plain @click="openCreateDialog"><Icon icon="ep:plus" class="mr-5px" /> 新增</el-button>
          <el-button type="success" plain @click="handleExport"><Icon icon="ep:download" class="mr-5px" /> 导出</el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="table-wrap">
      <el-table class="river-table" v-loading="tableLoading" :data="tableData">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="河长名称" align="center" min-width="140" prop="headName" show-overflow-tooltip />
        <el-table-column label="河长级别" align="center" min-width="120">
          <template #default="{ row }">
            {{ resolveHeadLevelLabel(row.headLevel) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="关联设施" align="center" min-width="120">
          <template #default="{ row }">
            {{ row.referenceTypeLabel || resolveReferenceTypeLabel(row.referenceType) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="关联设施名称" align="center" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.referenceName || '-' }}
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

    <!-- 新增/编辑/详情 -->
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

          <el-col :span="12">
            <el-form-item label="关联设施" prop="referenceType">
              <el-select v-model="createForm.referenceType" placeholder="请选择" class="!w-240px" @change="handleReferenceTypeChange">
                <el-option label="河道" value="river" />
                <el-option label="水库" value="reservoir" />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 河道：树形选择（父=河道，子=河段） -->
          <el-col v-if="createForm.referenceType === 'river'" :span="24">
            <el-form-item label="关联设施名称" prop="referenceId">
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
                :disabled="formReadonly"
                placeholder="请选择河道或河段"
                class="!w-460px"
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
            </el-form-item>
          </el-col>

          <!-- 水库：弹窗选择 -->
          <el-col v-if="createForm.referenceType === 'reservoir'" :span="12">
            <el-form-item label="关联设施名称" prop="referenceId">
              <div class="flex items-center gap-8px w-full">
                <el-input v-model="referenceNameDisplay" placeholder="请选择关联设施" readonly />
                <el-button type="primary" plain @click="openReferencePicker" :disabled="formReadonly">选择</el-button>
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
          <el-col :span="24">
            <el-form-item label="行政区划" prop="administrativeRegion">
              <el-tree-select
                v-model="createForm.administrativeRegion"
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
                :max-collapse-tags="3"
                placeholder="请选择（可搜索）"
                class="select-long"
                :disabled="formReadonly"
              />
            </el-form-item>
          </el-col>
          <el-col v-if="createForm.referenceType === 'reservoir' && formMode === 'create'" :span="24">
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
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import download from '@/utils/download'
import {
  createRiverChiefManagement,
  deleteRiverChiefManagement,
  exportRiverChiefManagementExcel,
  getRiverChiefManagementDetail,
  getRiverChiefManagementPage,
  updateRiverChiefManagement,
  type RiverChiefManagementDetailRespVO,
  type RiverChiefManagementPageReqVO,
  type RiverChiefManagementPageRespVO,
  type RiverChiefManagementSaveReqVO
} from '@/api/gis/riverChiefManagement'
import {
  getRiverChannelSimpleList,
  getRiverDict,
  getRiverSectionWithChannelSimpleList,
  type DictDataItemRespVO,
  type RiverChannelSimpleRespVO,
  type RiverSectionWithChannelSimpleRespVO
} from '@/api/gis/riverChannel'
import { getReservoirPage, type ReservoirPageRespVO } from '@/api/gis/reservoir'
import { getAreaTree } from '@/api/system/area'

const queryParams = reactive<RiverChiefManagementPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  headName: '',
  headLevel: '',
  administrativeRegion: [],
  referenceType: '',
  referenceName: ''
})
const queryFormRef = ref<FormInstance>()

const tableData = ref<RiverChiefManagementPageRespVO[]>([])
const total = ref(0)
const tableLoading = ref(false)

const headLevelOptions = ref<DictDataItemRespVO[]>([])
const headLevelLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  for (const item of headLevelOptions.value) {
    if (!item?.value) continue
    map[item.value] = item.label || item.value
  }
  return map
})

type AreaTreeNode = {
  id: string | number
  name: string
  children?: AreaTreeNode[]
}

type AreaTreeSelectNode = {
  label: string
  value: string
  children?: AreaTreeSelectNode[]
}

const areaTreeData = ref<AreaTreeSelectNode[]>([])
const areaTreeProps = {
  label: 'label',
  value: 'value',
  children: 'children'
}

const areaLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  const walk = (nodes?: AreaTreeSelectNode[]) => {
    if (!nodes || !nodes.length) return
    for (const node of nodes) {
      if (!node) continue
      if (node.value) map[String(node.value)] = node.label || String(node.value)
      if (node.children && node.children.length) walk(node.children)
    }
  }
  walk(areaTreeData.value)
  return map
})

const formatAdministrativeRegion = (ids?: string[]) => {
  if (!ids || !ids.length) return ''
  const labels = ids
    .filter((v) => !!v)
    .map((v) => areaLabelMap.value[String(v)] || String(v))
    .filter((v) => !!v)
  return labels.join('、')
}

const filterAreaTreeNode = (keyword: string, data: any) => {
  const k = String(keyword || '').trim()
  if (!k) return true
  return String(data?.label || '').includes(k)
}

const loadAreaTree = async () => {
  try {
    const res = (await getAreaTree()) as any
    const data = (res && (res.data || res)) as AreaTreeNode[]
    const build = (nodes?: AreaTreeNode[]): AreaTreeSelectNode[] => {
      if (!nodes || !nodes.length) return []
      return nodes
        .filter((n) => !!n)
        .map((node) => ({
          label: node.name,
          value: String(node.id),
          children: build(node.children)
        }))
    }
    areaTreeData.value = build(data || [])
  } catch {
    areaTreeData.value = []
  }
}

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

const fetchTable = async () => {
  if (!validateReferenceFilter()) {
    return
  }
  tableLoading.value = true
  try {
    const res = await getRiverChiefManagementPage(queryParams)
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
  await Promise.all([loadDict(), loadAreaTree()])
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

const handleExport = async () => {
  if (!validateReferenceFilter()) {
    return
  }
  try {
    const data = await exportRiverChiefManagementExcel(queryParams)
    download.excel(data, '河长管理.xls')
  } catch {
    // 下载失败时，错误提示由全局拦截器处理
  }
}

const handleDelete = async (row: RiverChiefManagementPageRespVO) => {
  await ElMessageBox.confirm(`确认删除河长【${row.headName || ''}】吗？`, '提示', { type: 'warning' })
  await deleteRiverChiefManagement(row.id)
  ElMessage.success('删除成功')
  fetchTable()
}

const handleDetail = async (row: RiverChiefManagementPageRespVO) => {
  await openEditDialog(String(row.id), 'view')
}

const handleEdit = async (row: RiverChiefManagementPageRespVO) => {
  await openEditDialog(String(row.id), 'edit')
}

// region 新增/编辑/详情弹窗

const createDialogVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref<FormInstance>()
const formMode = ref<'create' | 'edit' | 'view'>('create')
const dialogTitle = computed(() => {
  if (formMode.value === 'edit') return '编辑河长'
  if (formMode.value === 'view') return '河长详情'
  return '新增河长'
})
const formReadonly = computed(() => formMode.value === 'view')

const createForm = reactive<RiverChiefManagementSaveReqVO>({
  id: '',
  referenceType: 'river',
  referenceId: '',
  headName: '',
  headLevel: '',
  headPosition: '',
  headUnit: '',
  responsibilities: '',
  administrativeRegion: [],
  remarks: ''
})

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
  referenceType: [{ required: true, message: '请选择关联设施', trigger: 'change' }],
  referenceId: [{ required: true, message: '请选择关联设施', trigger: 'change' }],
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
  createForm.administrativeRegion = []
  createForm.remarks = ''
  referenceNameDisplay.value = ''
  riverReferenceValue.value = ''
}

const openCreateDialog = () => {
  resetCreateForm()
  formMode.value = 'create'
  createDialogVisible.value = true
  void ensureRiverReferenceTreeLoaded()
}

const fillCreateFormByDetail = async (detail: RiverChiefManagementDetailRespVO) => {
  createForm.id = detail.id || ''
  createForm.referenceType = detail.referenceType === 'reservoir' ? 'reservoir' : 'river'
  createForm.referenceId = detail.referenceId || ''
  createForm.headName = detail.headName || ''
  createForm.headLevel = detail.headLevel || ''
  createForm.headPosition = detail.headPosition || ''
  createForm.headUnit = detail.headUnit || ''
  createForm.responsibilities = createForm.referenceType === 'reservoir' ? detail.responsibilities || '' : ''
  createForm.administrativeRegion = Array.isArray(detail.administrativeRegion) ? detail.administrativeRegion : []
  createForm.remarks = detail.remarks || ''
  referenceNameDisplay.value = detail.referenceName || ''
  riverReferenceValue.value = ''
  if (createForm.referenceType === 'river' && createForm.referenceId) {
    await ensureRiverReferenceTreeLoaded()
    const selectedType: RiverReferenceType = detail.referenceType === 'river_section' ? 'river_section' : 'river'
    riverReferenceValue.value = buildRiverReferenceValue(selectedType, createForm.referenceId)
    const selectedNode = riverReferenceNodeMap.value[riverReferenceValue.value]
    referenceNameDisplay.value = selectedNode?.label || detail.referenceName || ''
  }
}

const openEditDialog = async (id: string, mode: 'edit' | 'view') => {
  resetCreateForm()
  formMode.value = mode
  createDialogVisible.value = true
  const detail = await getRiverChiefManagementDetail(id)
  await fillCreateFormByDetail(detail)
}

const handleReferenceTypeChange = () => {
  createForm.referenceId = ''
  referenceNameDisplay.value = ''
  riverReferenceValue.value = ''
  if (createForm.referenceType === 'river') {
    void ensureRiverReferenceTreeLoaded()
  }
  if (createForm.referenceType !== 'reservoir') {
    createForm.responsibilities = ''
  }
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
  createSubmitting.value = true
  try {
    const parsedRiverRef = parseRiverReferenceValue(riverReferenceValue.value)
    const submitReferenceType =
      createForm.referenceType === 'river'
        ? parsedRiverRef?.nodeType || 'river'
        : createForm.referenceType
    const submitReferenceId =
      createForm.referenceType === 'river'
        ? parsedRiverRef?.referenceId || createForm.referenceId
        : createForm.referenceId
    const payload: RiverChiefManagementSaveReqVO = {
      id: createForm.id || undefined,
      referenceType: submitReferenceType,
      referenceId: submitReferenceId,
      headName: createForm.headName,
      headLevel: createForm.headLevel || undefined,
      headPosition: createForm.headPosition || undefined,
      headUnit: createForm.headUnit || undefined,
      responsibilities: submitReferenceType === 'reservoir' ? createForm.responsibilities || undefined : undefined,
      administrativeRegion: Array.isArray(createForm.administrativeRegion) ? createForm.administrativeRegion : undefined,
      remarks: createForm.remarks || undefined
    }
    if (formMode.value === 'edit') {
      await updateRiverChiefManagement(payload)
      ElMessage.success('编辑成功')
    } else {
      await createRiverChiefManagement(payload)
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

const confirmPick = async (row: any) => {
  if (!row) return

  // 水库：直接回填 referenceId
  createForm.referenceId = String(row.id)
  referenceNameDisplay.value = row.reservoirName || ''
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
.table-wrap {
  background: var(--head-card-bg);
  border: 1px solid var(--head-border);
  border-radius: 14px;
  box-shadow: var(--head-shadow);
}

.table-wrap {
  margin-top: 12px;
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

:deep(.facility-dialog .el-dialog),
:deep(.timeline-drawer .el-drawer) {
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.2);
}

:deep(.facility-dialog .el-dialog__header),
:deep(.timeline-drawer .el-drawer__header) {
  padding: 16px 20px;
  margin-bottom: 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.24);
  background: linear-gradient(90deg, #f8fbff 0%, #f3f7ff 100%);
}

:deep(.facility-dialog .el-dialog__title),
:deep(.timeline-drawer .el-drawer__title) {
  font-weight: 700;
  color: #1e3a8a;
}

:deep(.facility-dialog .el-dialog__body),
:deep(.timeline-drawer .el-drawer__body) {
  padding: 16px 20px;
}

:deep(.facility-dialog .el-dialog__footer) {
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  padding: 12px 20px;
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

.nowrap-inline {
  flex-wrap: nowrap;
  white-space: nowrap;
}
</style>
