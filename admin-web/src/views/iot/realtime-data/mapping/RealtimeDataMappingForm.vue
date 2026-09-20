<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-form-item label="采集源编号" prop="sourceId">
        <el-input v-model="formData.sourceId" disabled />
      </el-form-item>
      <el-form-item label="设备编号" prop="deviceId">
        <el-select
          v-model="formData.deviceId"
          placeholder="请选择设备"
          filterable
          clearable
          :loading="deviceLoading"
          @change="handleDeviceChange"
        >
          <el-option
            v-for="item in deviceOptions"
            :key="String(item.id)"
            :label="item.deviceName ? `${item.deviceName}(${item.id})` : String(item.id)"
            :value="String(item.id)"
          />
        </el-select>
      </el-form-item>

      <el-form-item v-if="formType === 'create'" label="批量导入">
        <el-button type="primary" plain :disabled="!formData.deviceId" @click="openBatchImport">
          一键批量导入
        </el-button>
      </el-form-item>

      <el-form-item label="点位名称" prop="pointName">
        <el-select
          v-model="formData.pointName"
          placeholder="请选择或输入点位名称"
          filterable
          allow-create
          default-first-option
          clearable
          :disabled="!formData.deviceId"
          :loading="propertyLoading"
          @change="handlePointNameChange"
        >
          <el-option
            v-for="item in availablePointNameOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="标识符" prop="identifier">
        <el-select
          v-model="formData.identifier"
          placeholder="请选择标识符"
          filterable
          clearable
          :disabled="!formData.deviceId || identifierLocked"
          :loading="propertyLoading"
        >
          <el-option
            v-for="item in identifierOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="启用状态" prop="enabled">
        <el-switch v-model="formData.enabled" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确认</el-button>
      <el-button @click="dialogVisible = false">取消</el-button>
    </template>
  </Dialog>

      <Dialog title="批量导入点位" v-model="batchDialogVisible" width="640px">
    <div class="mb-8px text-sm text-gray-600">待导入数量：{{ batchItems.length }}</div>
    <el-table
      v-if="batchItems.length > 0"
      v-loading="batchLoading"
      :data="batchItems"
      size="small"
      border
      max-height="360"
    >
      <el-table-column label="点位名称" prop="pointName" min-width="200" />
      <el-table-column label="标识符" prop="identifier" min-width="160" />
    </el-table>
    <el-empty v-else-if="!batchLoading" description="暂无可导入点位" />
    <template #footer>
      <el-button @click="batchDialogVisible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="batchImportLoading"
        :disabled="batchItems.length === 0"
        @click="submitBatchImport"
      >
        确认导入
      </el-button>
    </template>
  </Dialog>



</template>

<script setup lang="ts">
import {
  RealtimeDataMappingApi,
  RealtimeDataMapping,
  RealtimeDataMappingBatchImportReq,
  RealtimeDataMappingBatchImportResp,
  RealtimeDataMappingImportItem
} from '@/api/iot/realtime-data/mapping'
import { DeviceApi, DeviceVO, IotDevicePropertyDetailRespVO } from '@/api/iot/device/device'
import { RealtimeDataSourceApi } from '@/api/iot/realtime-data/source'

/** IoT 实时数据点位映射 表单 */
defineOptions({ name: 'RealtimeDataMappingForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const deviceLoading = ref(false)
const deviceOptions = ref<DeviceVO[]>([])
const sourceStationId = ref<string | undefined>(undefined)
const propertyLoading = ref(false)
const propertyOptions = ref<IotDevicePropertyDetailRespVO[]>([])
const existingPointNames = ref<string[]>([])
const identifierLocked = ref(false)

const batchDialogVisible = ref(false)
const batchLoading = ref(false)
const batchImportLoading = ref(false)
const batchItems = ref<RealtimeDataMappingImportItem[]>([])

const formData = ref({
  id: undefined,
  sourceId: undefined,
  pointName: undefined,
  deviceId: undefined,
  identifier: undefined,
  enabled: true,
  sort: 0
})
const formRules = reactive({
  sourceId: [{ required: true, message: '采集源编号不能为空', trigger: 'blur' }],
  deviceId: [{ required: true, message: '设备编号不能为空', trigger: 'change' }],
  pointName: [{ required: true, message: '点位名称不能为空', trigger: 'change' }],
  identifier: [{ required: true, message: '标识符不能为空', trigger: 'change' }],
  enabled: [{ required: true, message: '启用状态不能为空', trigger: 'change' }]
})
const formRef = ref()

const buildPointLabel = (item: IotDevicePropertyDetailRespVO) =>
  (item.tagName || '').trim()

const pointNameOptions = computed(() => {
  const values = new Set<string>()
  propertyOptions.value.forEach((item) => {
    const label = buildPointLabel(item)
    if (label) {
      values.add(label)
    }
  })
  return Array.from(values)
})

const availablePointNameOptions = computed(() => {
  const currentPointName = (formData.value.pointName || '').trim()
  const usedSet = new Set(
    existingPointNames.value.map((item) => (item || '').trim()).filter((item) => item.length > 0)
  )
  return pointNameOptions.value.filter((item) => !usedSet.has(item) || item === currentPointName)
})

const identifierOptions = computed(() => {
  const values = new Set<string>()
  propertyOptions.value.forEach((item) => {
    const identifier = (item.identifier || '').trim()
    if (identifier) {
      values.add(identifier)
    }
  })
  return Array.from(values)
})

const pointNameIdentifierMap = computed(() => {
  const map = new Map<string, string>()
  propertyOptions.value.forEach((item) => {
    const label = buildPointLabel(item)
    const identifier = (item.identifier || '').trim()
    if (label && identifier && !map.has(label)) {
      map.set(label, identifier)
    }
  })
  return map
})

const loadDeviceOptions = async (stationId?: string) => {
  deviceLoading.value = true
  try {
    deviceOptions.value = await DeviceApi.getDeviceList({
      stationId: stationId || undefined
    })
  } finally {
    deviceLoading.value = false
  }
}

const loadSourceStation = async (sourceId: string) => {
  try {
    const source = await RealtimeDataSourceApi.getSource(sourceId)
    if (source?.name !== undefined && source?.name !== null) {
      sourceStationId.value = String(source.name)
    } else {
      sourceStationId.value = undefined
    }
  } catch {
    sourceStationId.value = undefined
  }
}

const loadPropertyOptions = async (deviceId?: string | number) => {
  if (!deviceId) {
    propertyOptions.value = []
    return
  }
  propertyLoading.value = true
  try {
    propertyOptions.value = await DeviceApi.getLatestDeviceProperties({ deviceId })
  } finally {
    propertyLoading.value = false
  }
}

const loadExistingPointNames = async () => {
  const sourceId = formData.value.sourceId
  const deviceId = formData.value.deviceId
  if (!sourceId || !deviceId) {
    existingPointNames.value = []
    return
  }
  try {
    existingPointNames.value = await RealtimeDataMappingApi.getPointNameList({
      sourceId,
      deviceId
    })
  } catch {
    existingPointNames.value = []
  }
}

const handleDeviceChange = async () => {
  await loadPropertyOptions(formData.value.deviceId)
  await loadExistingPointNames()
  batchItems.value = []
  batchDialogVisible.value = false
  if (formLoading.value) {
    return
  }
  formData.value.pointName = undefined
  formData.value.identifier = undefined
  identifierLocked.value = false
}

const handlePointNameChange = (value?: string) => {
  const pointName = (value || '').trim()
  if (!pointName) {
    formData.value.identifier = undefined
    identifierLocked.value = false
    return
  }
  const matchedIdentifier = pointNameIdentifierMap.value.get(pointName)
  if (matchedIdentifier) {
    formData.value.identifier = matchedIdentifier
    identifierLocked.value = true
    return
  }
  if (identifierLocked.value) {
    formData.value.identifier = undefined
  }
  identifierLocked.value = false
}

const syncIdentifierLock = () => {
  const pointName = (formData.value.pointName || '').trim()
  const identifier = (formData.value.identifier || '').trim()
  if (!pointName) {
    identifierLocked.value = false
    return
  }
  const matchedIdentifier = pointNameIdentifierMap.value.get(pointName)
  identifierLocked.value = Boolean(matchedIdentifier && matchedIdentifier === identifier)
}

const openBatchImport = async () => {
  if (formType.value !== 'create') {
    return
  }
  const sourceId = formData.value.sourceId
  const deviceId = formData.value.deviceId
  if (!sourceId || !deviceId) {
    message.error('请先选择设备')
    return
  }
  batchLoading.value = true
  try {
    batchItems.value = await RealtimeDataMappingApi.getImportPreviewByTag({ sourceId, deviceId })
    if (batchItems.value.length === 0) {
      message.warning('当前设备没有可导入点位')
      return
    }
    batchDialogVisible.value = true
  } finally {
    batchLoading.value = false
  }
}

const submitBatchImport = async () => {
  const sourceId = formData.value.sourceId
  const deviceId = formData.value.deviceId
  if (!sourceId || !deviceId) {
    message.error('请先选择设备')
    return
  }
  if (batchItems.value.length === 0) {
    message.warning('暂无可导入点位')
    return
  }
  batchImportLoading.value = true
  try {
    const req: RealtimeDataMappingBatchImportReq = {
      sourceId,
      deviceId,
      items: batchItems.value
    }
    const resp = (await RealtimeDataMappingApi.importBatch(req)) as RealtimeDataMappingBatchImportResp
    message.success(`成功导入${resp.importedCount}条，跳过${resp.skippedCount}条`)
    batchDialogVisible.value = false
    dialogVisible.value = false
    emit('success')
  } finally {
    batchImportLoading.value = false
  }
}


const open = async (type: string, sourceId: string, id?: string) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  await loadSourceStation(sourceId)
  await loadDeviceOptions(sourceStationId.value)
  formData.value.sourceId = sourceId

  if (id) {
    formLoading.value = true
    try {
      formData.value = await RealtimeDataMappingApi.getMapping(id)
      if (formData.value.deviceId !== undefined && formData.value.deviceId !== null) {
        formData.value.deviceId = String(formData.value.deviceId)
      }
      await loadPropertyOptions(formData.value.deviceId)
      await loadExistingPointNames()
      syncIdentifierLock()
    } finally {
      formLoading.value = false
    }
  }
}

defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as RealtimeDataMapping
    if (formType.value === 'create') {
      await RealtimeDataMappingApi.createMapping(data)
      message.success(t('common.createSuccess'))
    } else {
      await RealtimeDataMappingApi.updateMapping(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    sourceId: undefined,
    pointName: undefined,
    deviceId: undefined,
    identifier: undefined,
    enabled: true,
    sort: 0
  }
  propertyOptions.value = []
  identifierLocked.value = false
  batchItems.value = []
  batchDialogVisible.value = false
  batchLoading.value = false
  batchImportLoading.value = false
  formRef.value?.resetFields()
}
</script>
