<template>
  <ContentWrap>
    <div class="mb-12px text-sm text-gray-600">
      采集源：{{ sourceLabel }}
    </div>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="点位名称" prop="pointName">
        <el-input
          v-model="queryParams.pointName"
          placeholder="请输入点位名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-select
          v-model="queryParams.deviceName"
          placeholder="请选择设备名称"
          filterable
          remote
          reserve-keyword
          clearable
          :remote-method="loadDeviceNameOptions"
          :loading="deviceNameLoading"
          @visible-change="handleDeviceNameVisibleChange"
          class="!w-240px"
        >
          <el-option
            v-for="item in deviceNameOptions"
            :key="String(item.id)"
            :label="item.deviceName"
            :value="item.deviceName"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="标识符" prop="identifier">
        <el-input
          v-model="queryParams.identifier"
          placeholder="请输入标识符"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="启用状态" prop="enabled">
        <el-select v-model="queryParams.enabled" placeholder="请选择启用状态" clearable class="!w-160px">
          <el-option
            v-for="item in enabledOptions"
            :key="String(item.value)"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="warning"
          plain
          @click="handleBatchAddPreview"
        >
          <Icon icon="ep:upload" class="mr-5px" /> 批量新增
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="selectedIds.length === 0"
          @click="handleDeleteList"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="点位名称" align="center" prop="pointName" min-width="180" />
      <el-table-column label="设备名称" align="center" prop="deviceName" min-width="180">
        <template #default="scope">
          {{ scope.row.deviceName || scope.row.deviceId || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="标识符" align="center" prop="identifier" min-width="120" />
      <el-table-column label="启用状态" align="center" prop="enabled" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.enabled ? 'success' : 'info'">
            {{ scope.row.enabled ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <RealtimeDataMappingForm ref="formRef" @success="getList" />

  <Dialog title="批量新增预览" v-model="batchPreviewVisible" width="900px">
    <div class="mb-8px text-sm text-gray-600">
      设备数：{{ batchPreviewList.length }}，待新增点位：{{ batchPreviewTotal }}
    </div>
    <el-table
      v-if="batchPreviewList.length > 0"
      v-loading="batchPreviewLoading"
      :data="batchPreviewList"
      size="small"
      border
      max-height="460"
    >
      <el-table-column type="expand" width="40">
        <template #default="scope">
          <el-table
            v-if="scope.row.items && scope.row.items.length > 0"
            :data="scope.row.items"
            size="small"
            border
          >
            <el-table-column label="点位名称" prop="pointName" min-width="200" />
            <el-table-column label="标识符" prop="identifier" min-width="160" />
          </el-table>
          <el-empty v-else description="暂无可新增点位" />
        </template>
      </el-table-column>
      <el-table-column label="设备名称" min-width="180">
        <template #default="scope">
          {{ scope.row.deviceName || scope.row.deviceId || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="设备编号" prop="deviceId" width="120" />
      <el-table-column label="当前点位数" prop="existingCount" width="120" />
      <el-table-column label="可新增点位数" prop="importCount" width="140" />
    </el-table>
    <el-empty v-else-if="!batchPreviewLoading" description="暂无可新增点位" />
    <template #footer>
      <el-button @click="batchPreviewVisible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="batchConfirmLoading"
        :disabled="batchPreviewTotal === 0"
        @click="handleBatchAddConfirm"
      >
        确认新增
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import {
  RealtimeDataMappingApi,
  RealtimeDataMapping,
  RealtimeDataMappingBatchAddByStationPreview,
  RealtimeDataMappingBatchAddByStationResp
} from '@/api/iot/realtime-data/mapping'
import { RealtimeDataSourceApi, RealtimeDataSource } from '@/api/iot/realtime-data/source'
import { DeviceApi, DeviceVO } from '@/api/iot/device/device'
import RealtimeDataMappingForm from './RealtimeDataMappingForm.vue'
import { useRoute } from 'vue-router'
import { DICT_TYPE, getDictLabel } from '@/utils/dict'

/** IoT 实时数据点位映射 列表 */
defineOptions({ name: 'IotRealtimeDataMapping' })

const message = useMessage()
const { t } = useI18n()
const route = useRoute()

const loading = ref(true)
const list = ref<RealtimeDataMapping[]>([])
const total = ref(0)
const selectedIds = ref<Array<string | number>>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  sourceId: undefined as string | undefined,
  pointName: undefined as string | undefined,
  deviceName: undefined as string | undefined,
  identifier: undefined as string | undefined,
  enabled: undefined as boolean | undefined
})
const queryFormRef = ref()

const deviceNameLoading = ref(false)
const deviceNameOptions = ref<DeviceVO[]>([])

const sourceInfo = ref<RealtimeDataSource | null>(null)
const stationId = computed(() =>
  sourceInfo.value?.name !== undefined && sourceInfo.value?.name !== null
    ? String(sourceInfo.value.name)
    : ''
)
const sourceLabel = computed(() => {
  if (sourceInfo.value?.name) {
    const label = getDictLabel(DICT_TYPE.IOT_ZD_SBZD, sourceInfo.value.name)
    return `${label || sourceInfo.value.name}（ID: ${sourceInfo.value.id}）`
  }
  if (queryParams.sourceId) {
    return `ID: ${queryParams.sourceId}`
  }
  return '未指定'
})

const enabledOptions = [
  { label: '启用', value: true },
  { label: '停用', value: false }
]

const batchPreviewVisible = ref(false)
const batchPreviewLoading = ref(false)
const batchConfirmLoading = ref(false)
const batchPreviewList = ref<RealtimeDataMappingBatchAddByStationPreview[]>([])
const batchPreviewTotal = computed(() =>
  batchPreviewList.value.reduce((sum, item) => sum + (item.importCount || 0), 0)
)

const loadDeviceNameOptions = async (keyword?: string) => {
  deviceNameLoading.value = true
  try {
    const deviceName = (keyword || '').trim()
    deviceNameOptions.value = await DeviceApi.getDeviceList({
      deviceName: deviceName || undefined
    })
  } finally {
    deviceNameLoading.value = false
  }
}

const handleDeviceNameVisibleChange = async (visible: boolean) => {
  if (!visible || deviceNameOptions.value.length > 0) {
    return
  }
  await loadDeviceNameOptions('')
}

const getList = async () => {
  if (!queryParams.sourceId) {
    return
  }
  loading.value = true
  try {
    const data = await RealtimeDataMappingApi.getMappingPage(queryParams)
    list.value = data.list
    total.value = data.total
    selectedIds.value = []
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  selectedIds.value = []
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: string) => {
  if (!queryParams.sourceId) {
    message.error('请先选择采集源')
    return
  }
  formRef.value.open(type, queryParams.sourceId, id)
}

const handleDelete = async (id: string) => {
  try {
    await message.delConfirm()
    await RealtimeDataMappingApi.deleteMapping(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handleSelectionChange = (selection: RealtimeDataMapping[]) => {
  selectedIds.value = selection.map((item) => item.id).filter(Boolean) as Array<string | number>
}

const handleDeleteList = async () => {
  try {
    await message.delConfirm()
    await RealtimeDataMappingApi.deleteMappingList(selectedIds.value)
    selectedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handleBatchAddPreview = async () => {
  if (!queryParams.sourceId) {
    message.error('请先选择采集源')
    return
  }
  if (!stationId.value) {
    message.error('当前采集源未绑定站点')
    return
  }
  batchPreviewLoading.value = true
  try {
    batchPreviewList.value = await RealtimeDataMappingApi.getBatchAddPreview({
      sourceId: queryParams.sourceId,
      stationId: stationId.value
    })
    if (batchPreviewList.value.length === 0) {
      message.warning('该站点暂无可新增点位')
    }
    batchPreviewVisible.value = true
  } finally {
    batchPreviewLoading.value = false
  }
}

const handleBatchAddConfirm = async () => {
  if (!queryParams.sourceId) {
    message.error('请先选择采集源')
    return
  }
  if (!stationId.value) {
    message.error('当前采集源未绑定站点')
    return
  }
  if (batchPreviewTotal.value === 0) {
    message.warning('暂无可新增点位')
    return
  }
  batchConfirmLoading.value = true
  try {
    const resp = (await RealtimeDataMappingApi.batchAddByStation({
      sourceId: queryParams.sourceId,
      stationId: stationId.value
    })) as RealtimeDataMappingBatchAddByStationResp
    message.success(
      `批量新增完成：新增${resp.importedCount}条，删除旧点位${resp.deletedCount}条，未新增设备${resp.skippedDeviceCount}台`
    )
    batchPreviewVisible.value = false
    await getList()
  } finally {
    batchConfirmLoading.value = false
  }
}

const loadSource = async () => {
  if (!queryParams.sourceId) {
    return
  }
  try {
    sourceInfo.value = await RealtimeDataSourceApi.getSource(queryParams.sourceId)
  } catch {
    sourceInfo.value = null
  }
}

onMounted(async () => {
  const sourceId = String(route.query.sourceId || '').trim()
  if (!sourceId) {
    message.error('请先从采集源进入点位配置')
    return
  }
  queryParams.sourceId = sourceId
  await loadSource()
  await getList()
})
</script>
