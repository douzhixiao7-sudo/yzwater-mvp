<template>
  <div class="zrr-page">
    <ContentWrap>
      <div class="section-header">
        <div class="title">市级防汛抗旱责任人名单</div>
        <div class="actions">
          <el-button type="primary" link @click="openCityDialog">
            {{ cityList.length ? '修改' : '新增' }}
          </el-button>
          <el-button type="success" link :loading="exportLoading.city" @click="handleExport('1')">
            导出
          </el-button>
          <el-button
            v-if="cityList.length"
            type="danger"
            link
            :loading="deleteLoading"
            @click="handleDeleteCity"
          >
            删除
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading.city" :data="cityList" class="zrr-table" border>
        <el-table-column label="行政责任人" align="center">
          <el-table-column prop="administrativeName" label="姓名" align="center" min-width="140" />
          <el-table-column prop="administrativeTitle" label="职务" align="center" min-width="220" />
        </el-table-column>
        <el-table-column label="技术责任人" align="center">
          <el-table-column prop="technicalName" label="姓名" align="center" min-width="140" />
          <el-table-column prop="technicalTitle" label="职务" align="center" min-width="220" />
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading.city && cityList.length === 0"
        description="暂无市级责任人信息"
        class="empty-block"
      />
    </ContentWrap>

    <ContentWrap class="section-gap">
      <div class="section-header">
        <div class="title">各园镇防汛抗旱责任人名单</div>
        <div class="actions">
          <el-button type="primary" link @click="openParkDialog">
            {{ parkList.length ? '修改' : '新增' }}
          </el-button>
          <el-button type="success" link :loading="exportLoading.park" @click="handleExport('2')">
            导出
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading.park" :data="parkList" class="zrr-table" border>
        <el-table-column prop="divisionCode" label="区划名称" align="center" min-width="180">
          <template #default="{ row }">
            {{ areaNameMap[row.divisionCode] || row.divisionCode || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="行政责任人" align="center">
          <el-table-column prop="administrativeName" label="姓名" align="center" min-width="140" />
          <el-table-column prop="administrativeTitle" label="职务" align="center" min-width="220" />
        </el-table-column>
        <el-table-column label="技术责任人" align="center">
          <el-table-column prop="technicalName" label="姓名" align="center" min-width="140" />
          <el-table-column prop="technicalTitle" label="职务" align="center" min-width="220" />
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading.park && parkList.length === 0"
        description="暂无园镇责任人信息"
        class="empty-block"
      />
    </ContentWrap>

    <el-dialog
      v-model="cityDialogVisible"
      title="市级防汛抗旱责任人名单"
      width="720px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-table :data="[cityForm]" border class="edit-table">
        <el-table-column label="行政责任人" align="center">
          <el-table-column label="姓名" align="center">
            <template #default>
              <el-input v-model="cityForm.administrativeName" placeholder="请输入" />
            </template>
          </el-table-column>
          <el-table-column label="职务" align="center">
            <template #default>
              <el-input v-model="cityForm.administrativeTitle" placeholder="请输入" />
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column label="技术责任人" align="center">
          <el-table-column label="姓名" align="center">
            <template #default>
              <el-input v-model="cityForm.technicalName" placeholder="请输入" />
            </template>
          </el-table-column>
          <el-table-column label="职务" align="center">
            <template #default>
              <el-input v-model="cityForm.technicalTitle" placeholder="请输入" />
            </template>
          </el-table-column>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-space>
          <el-button @click="cityDialogVisible = false">关闭</el-button>
          <el-button type="primary" :loading="submitLoading.city" @click="submitCityForm">
            确定
          </el-button>
        </el-space>
      </template>
    </el-dialog>

    <el-dialog
      v-model="parkDialogVisible"
      title="各园镇防汛抗旱责任人名单"
      width="1040px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="dialog-actions">
        <el-button type="primary" plain @click="addParkRow">
          <Icon icon="ep:plus" class="mr-5px" /> 添加
        </el-button>
      </div>
      <el-table :data="parkEditList" border class="edit-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="区划代码" align="center" min-width="200">
          <template #default="{ row }">
            <el-tree-select
              v-model="row.divisionCode"
              :data="areaTreeData"
              :props="areaTreeProps"
              node-key="value"
              check-strictly
              check-on-click-node
              filterable
              :filter-node-method="filterAreaTreeNode"
              clearable
              placeholder="请选择"
              class="tree-select"
            />
          </template>
        </el-table-column>
        <el-table-column label="行政责任人" align="center">
          <el-table-column label="姓名" align="center" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.administrativeName" placeholder="请输入" />
            </template>
          </el-table-column>
          <el-table-column label="职务" align="center" min-width="220">
            <template #default="{ row }">
              <el-input v-model="row.administrativeTitle" placeholder="请输入" />
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column label="技术责任人" align="center">
          <el-table-column label="姓名" align="center" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.technicalName" placeholder="请输入" />
            </template>
          </el-table-column>
          <el-table-column label="职务" align="center" min-width="220">
            <template #default="{ row }">
              <el-input v-model="row.technicalTitle" placeholder="请输入" />
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column label="操作" align="center" width="80">
          <template #default="{ $index }">
            <el-button link type="danger" @click="removeParkRow($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-space>
          <el-button @click="parkDialogVisible = false">关闭</el-button>
          <el-button type="primary" :loading="submitLoading.park" @click="submitParkForm">
            确定
          </el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import download from '@/utils/download'
import { getAreaTree } from '@/api/system/area'
import {
  batchSaveFxZrr,
  createFxZrr,
  deleteFxZrr,
  exportFxZrrExcel,
  getFxZrrList,
  updateFxZrr,
  type FxZrrBatchSaveReqVO,
  type FxZrrListRespVO,
  type FxZrrSaveReqVO
} from '@/api/fx/zrr'

defineOptions({ name: 'FxZrr' })

const loading = reactive({ city: false, park: false })
const submitLoading = reactive({ city: false, park: false })
const exportLoading = reactive({ city: false, park: false })
const deleteLoading = ref(false)

const cityList = ref<FxZrrListRespVO[]>([])
const parkList = ref<FxZrrListRespVO[]>([])

type TreeSelectNode = {
  label: string
  value: string
  children?: TreeSelectNode[]
}

type AreaTreeNode = {
  id: number
  name: string
  children?: AreaTreeNode[]
}

const areaNameMap = reactive<Record<string, string>>({})
const areaTreeData = ref<TreeSelectNode[]>([])
const areaTreeProps = {
  label: 'label',
  value: 'value',
  children: 'children'
}

const filterAreaTreeNode = (keyword: string, data: any) => {
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
    ;(nodes || []).forEach((node) => {
      areaNameMap[String(node.value)] = node.label
      if (node.children && node.children.length) {
        flatten(node.children)
      }
    })
  }
  flatten(areaTreeData.value)
}

const cityDialogVisible = ref(false)
const parkDialogVisible = ref(false)

const cityForm = reactive<FxZrrSaveReqVO>({
  id: '',
  type: '1',
  administrativeName: '',
  administrativeTitle: '',
  technicalName: '',
  technicalTitle: ''
})

const parkEditList = ref<FxZrrBatchSaveReqVO['items']>([])

const normalizeText = (value?: string) => String(value || '').trim()

const fetchCityList = async () => {
  loading.city = true
  try {
    const data = await getFxZrrList('1')
    cityList.value = Array.isArray(data) ? data : []
  } finally {
    loading.city = false
  }
}

const fetchParkList = async () => {
  loading.park = true
  try {
    const data = await getFxZrrList('2')
    parkList.value = Array.isArray(data) ? data : []
  } finally {
    loading.park = false
  }
}

const openCityDialog = () => {
  const current = cityList.value[0]
  cityForm.id = current?.id || ''
  cityForm.type = '1'
  cityForm.administrativeName = current?.administrativeName || ''
  cityForm.administrativeTitle = current?.administrativeTitle || ''
  cityForm.technicalName = current?.technicalName || ''
  cityForm.technicalTitle = current?.technicalTitle || ''
  cityDialogVisible.value = true
}

const submitCityForm = async () => {
  const adminName = normalizeText(cityForm.administrativeName)
  const techName = normalizeText(cityForm.technicalName)
  if (!adminName || !techName) {
    ElMessage.warning('请填写行政责任人和技术责任人姓名')
    return
  }
  submitLoading.city = true
  try {
    const payload: FxZrrSaveReqVO = {
      ...cityForm,
      type: '1',
      administrativeName: adminName,
      administrativeTitle: normalizeText(cityForm.administrativeTitle),
      technicalName: techName,
      technicalTitle: normalizeText(cityForm.technicalTitle)
    }
    if (payload.id) {
      await updateFxZrr(payload)
      ElMessage.success('修改成功')
    } else {
      await createFxZrr(payload)
      ElMessage.success('新增成功')
    }
    cityDialogVisible.value = false
    await fetchCityList()
  } finally {
    submitLoading.city = false
  }
}

const handleDeleteCity = async () => {
  const current = cityList.value[0]
  if (!current) return
  try {
    await ElMessageBox.confirm('确认删除市级责任人信息吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  deleteLoading.value = true
  try {
    await deleteFxZrr(current.id)
    ElMessage.success('删除成功')
    await fetchCityList()
  } finally {
    deleteLoading.value = false
  }
}

const openParkDialog = () => {
  parkEditList.value = (parkList.value || []).map((item) => ({
    id: item.id,
    divisionCode: item.divisionCode || '',
    administrativeName: item.administrativeName || '',
    administrativeTitle: item.administrativeTitle || '',
    technicalName: item.technicalName || '',
    technicalTitle: item.technicalTitle || ''
  }))
  if (!parkEditList.value.length) {
    addParkRow()
  }
  parkDialogVisible.value = true
}

const addParkRow = () => {
  parkEditList.value.push({
    divisionCode: '',
    administrativeName: '',
    administrativeTitle: '',
    technicalName: '',
    technicalTitle: ''
  })
}

const removeParkRow = (index: number) => {
  if (index < 0 || index >= parkEditList.value.length) return
  parkEditList.value.splice(index, 1)
}

const buildParkPayload = () => {
  const items = (parkEditList.value || [])
    .map((item) => ({
      ...item,
      divisionCode: normalizeText(item.divisionCode),
      administrativeName: normalizeText(item.administrativeName),
      administrativeTitle: normalizeText(item.administrativeTitle),
      technicalName: normalizeText(item.technicalName),
      technicalTitle: normalizeText(item.technicalTitle)
    }))
    .filter((item) => {
      const hasContent =
        item.divisionCode ||
        item.administrativeName ||
        item.administrativeTitle ||
        item.technicalName ||
        item.technicalTitle
      return Boolean(hasContent)
    })
  const invalid = items.find((item) => !item.divisionCode)
  if (invalid) {
    ElMessage.warning('园镇记录请选择区划代码')
    return null
  }
  return items
}

const submitParkForm = async () => {
  const items = buildParkPayload()
  if (items === null) return
  submitLoading.park = true
  try {
    const payload: FxZrrBatchSaveReqVO = {
      type: '2',
      items
    }
    await batchSaveFxZrr(payload)
    ElMessage.success('保存成功')
    parkDialogVisible.value = false
    await fetchParkList()
  } finally {
    submitLoading.park = false
  }
}

const handleExport = async (type: '1' | '2') => {
  if (type === '1') {
    exportLoading.city = true
  } else {
    exportLoading.park = true
  }
  try {
    const data = await exportFxZrrExcel(type)
    const filename = type === '1' ? '市级防汛抗旱责任人名单.xls' : '园镇防汛抗旱责任人名单.xls'
    download.excel(data, filename)
  } finally {
    exportLoading.city = false
    exportLoading.park = false
  }
}

onMounted(async () => {
  await Promise.all([loadAreaTree(), fetchCityList(), fetchParkList()])
})
</script>

<style scoped>
.zrr-page {
  min-height: 100%;
}

.section-gap {
  margin-top: 14px;
}

.section-header {
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

.empty-block {
  margin: 12px 0;
}

.dialog-actions {
  margin-bottom: 10px;
}

.tree-select {
  width: 100%;
}

.edit-table :deep(.el-input__wrapper) {
  width: 100%;
}
</style>
