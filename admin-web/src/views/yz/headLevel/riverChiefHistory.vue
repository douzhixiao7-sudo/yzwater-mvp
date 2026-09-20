<template>
  <div class="river-page">
    <ContentWrap class="query-wrap">
      <el-form class="query-form -mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
        <el-form-item label="关联设施" prop="referenceType">
          <el-select v-model="queryParams.referenceType" placeholder="请选择" clearable class="!w-240px">
            <el-option label="河道" value="river" />
            <el-option label="河段" value="river_section" />
            <el-option label="水库" value="reservoir" />
          </el-select>
        </el-form-item>
        <el-form-item label="设施名称" prop="referenceName">
          <el-input
            v-model="queryParams.referenceName"
            placeholder="请输入设施名称关键字"
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
        <el-form-item label="河长姓名" prop="headName">
          <el-input
            v-model="queryParams.headName"
            placeholder="请输入河长姓名关键字"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleSearch"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="handleReset"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
          <el-button type="success" plain @click="handleExport"><Icon icon="ep:download" class="mr-5px" /> 导出</el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="table-wrap">
      <el-table class="river-table" v-loading="tableLoading" :data="tableData">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="关联设施" align="center" min-width="120">
          <template #default="{ row }">
            {{ row.referenceTypeLabel || resolveReferenceTypeLabel(row.referenceType) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="关联设施名称" align="center" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.referenceName || '' }}
          </template>
        </el-table-column>
        <el-table-column label="当前河长姓名" align="center" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.currentHeadNames || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="河长级别" align="center" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.headLevelLabel || resolveHeadLevelLabel(row.headLevel) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openTimeline(row)">历史河长</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="fetchTable"
      />
    </ContentWrap>

    <el-drawer class="timeline-drawer" v-model="timelineVisible" title="河长变更历史" size="860px" :with-header="true" destroy-on-close>
      <div class="drawer-body" v-loading="timelineLoading">
        <el-descriptions :column="1" border class="mb-12px">
          <el-descriptions-item label="关联设施">
            {{ timelineFacility.referenceTypeLabel || resolveReferenceTypeLabel(timelineFacility.referenceType) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="关联设施名称">
            {{ timelineFacility.referenceName || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-timeline v-if="timelineItems.length" class="chief-timeline">
          <el-timeline-item
            v-for="item in timelineItems"
            :key="String(item.id)"
            :timestamp="timelineTimestamp(item)"
            :type="item.effectiveTo ? 'info' : 'primary'"
          >
            <el-card shadow="never" class="timeline-card">
              <div class="timeline-title">
                <div class="name">
                  {{ item.headName || '-' }}
                  <span class="level">{{ item.headLevelLabel || resolveHeadLevelLabel(item.headLevel) }}</span>
                </div>
                <el-tag v-if="!item.effectiveTo" type="success" effect="plain">当前</el-tag>
                <el-tag v-else type="info" effect="plain">历史</el-tag>
              </div>
              <el-descriptions :column="2" size="small" border>
                <el-descriptions-item label="河长职务">{{ item.headPosition || '-' }}</el-descriptions-item>
                <el-descriptions-item label="工作单位">{{ item.headUnit || '-' }}</el-descriptions-item>
                <el-descriptions-item label="行政区划">{{ formatAdministrativeRegion(item.administrativeRegion) || '-' }}</el-descriptions-item>
                <el-descriptions-item label="失效时间">
                  {{ item.effectiveTo ? formatToDate(item.effectiveTo) : '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="关联设施">
                  {{ item.referenceTypeLabel || resolveReferenceTypeLabel(item.referenceType) || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="关联设施名称">{{ item.referenceName || '-' }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-timeline-item>
        </el-timeline>

        <el-empty v-else description="暂无历史河长信息" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import download from '@/utils/download'
import { formatToDate } from '@/utils/dateUtil'
import { getRiverDict, type DictDataItemRespVO } from '@/api/gis/riverChannel'
import { getAreaTree } from '@/api/system/area'
import {
  exportRiverChiefHistoryExcel,
  getRiverChiefHistoryPage,
  getRiverChiefHistoryTimeline,
  type RiverChiefHistoryPageReqVO,
  type RiverChiefHistoryPageRespVO,
  type RiverChiefHistoryTimelineItemRespVO
} from '@/api/gis/riverChiefHistory'

type AreaTreeNode = {
  id: string | number
  name: string
  children?: AreaTreeNode[]
}

const queryParams = reactive<RiverChiefHistoryPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  referenceType: '',
  referenceName: '',
  headLevel: '',
  headName: ''
})
const queryFormRef = ref<FormInstance>()

const tableLoading = ref(false)
const tableData = ref<RiverChiefHistoryPageRespVO[]>([])
const total = ref(0)

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

const areaNameMap = ref<Record<string, string>>({})
const loadAreaTree = async () => {
  try {
    const res = (await getAreaTree()) as any
    const data = (res && (res.data || res)) as AreaTreeNode[]
    const map: Record<string, string> = {}
    const walk = (nodes?: AreaTreeNode[]) => {
      if (!nodes || !nodes.length) return
      for (const node of nodes) {
        if (!node) continue
        map[String(node.id)] = node.name
        walk(node.children)
      }
    }
    walk(data || [])
    areaNameMap.value = map
  } catch {
    areaNameMap.value = {}
  }
}

const formatAdministrativeRegion = (codes?: string[]) => {
  if (!codes || !codes.length) return ''
  const labels = codes
    .filter((v) => !!v)
    .map((v) => areaNameMap.value[String(v)] || String(v))
    .filter((v) => !!v)
  return labels.length ? Array.from(new Set(labels)).join('、') : ''
}

const fetchTable = async () => {
  tableLoading.value = true
  try {
    const res = await getRiverChiefHistoryPage(queryParams)
    tableData.value = res?.list || []
    total.value = res?.total || 0
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNo = 1
  fetchTable()
}

const handleReset = () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  queryParams.pageSize = 10
  fetchTable()
}

const handleExport = async () => {
  try {
    const data = await exportRiverChiefHistoryExcel(queryParams)
    download.excel(data, '历史河长信息.xls')
  } catch {
    // 下载失败时，错误提示由全局拦截器处理
  }
}

// region 时间轴

const timelineVisible = ref(false)
const timelineLoading = ref(false)
const timelineItems = ref<RiverChiefHistoryTimelineItemRespVO[]>([])
const timelineFacility = reactive<{
  referenceType?: string
  referenceTypeLabel?: string
  referenceId?: string | number
  referenceName?: string
}>({
  referenceType: '',
  referenceTypeLabel: '',
  referenceId: '',
  referenceName: ''
})

const timelineTimestamp = (item: RiverChiefHistoryTimelineItemRespVO) => {
  // 按有效期结束时间形成时间轴：当前记录以“当前”展示
  if (!item.effectiveTo) return '当前'
  return formatToDate(item.effectiveTo)
}

const openTimeline = async (row: RiverChiefHistoryPageRespVO) => {
  const type = row.referenceType || ''
  const id = row.referenceId ?? ''
  if (!type || !id) {
    ElMessage.warning('缺少关联设施信息')
    return
  }
  timelineFacility.referenceType = type
  timelineFacility.referenceTypeLabel = row.referenceTypeLabel
  timelineFacility.referenceId = id
  timelineFacility.referenceName = row.referenceName
  timelineItems.value = []
  timelineVisible.value = true
  timelineLoading.value = true
  try {
    const list = await getRiverChiefHistoryTimeline({ referenceType: type, referenceId: id })
    timelineItems.value = Array.isArray(list) ? list : []
  } finally {
    timelineLoading.value = false
  }
}

// endregion

const loadDict = async () => {
  const hzjb = await getRiverDict('zd_hzjb')
  headLevelOptions.value = hzjb || []
}

onMounted(async () => {
  await Promise.all([loadDict(), loadAreaTree()])
  fetchTable()
})
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

.drawer-body {
  padding: 4px 2px;
}

.chief-timeline {
  margin-top: 10px;
}

.timeline-card {
  border-radius: 8px;
}

.timeline-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.timeline-title .name {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.timeline-title .level {
  margin-left: 10px;
  font-weight: 400;
  color: var(--el-text-color-secondary);
}
</style>
