<template>
  <div>
    <ContentWrap>
      <div class="flex items-center justify-between mb-4">
        <span class="text-lg font-medium">连接配置</span>
        <el-button type="primary" @click="handleEditConfig" v-hasPermi="['iot:device:update']">
          编辑
        </el-button>
      </div>

      <el-descriptions :column="3" border direction="horizontal">
        <el-descriptions-item label="MQTT数据源">
          {{ mqttConfig.sourceId ? sourceName : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="订阅主题">
          {{ mqttConfig.topic || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="负载模式">
          {{ formatPayloadMode(mqttConfig.payloadMode) }}
        </el-descriptions-item>
        <el-descriptions-item label="报文时间字段">
          {{ mqttConfig.reportTimeKey || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="报文时间格式">
          {{ mqttConfig.reportTimeFormat || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="mqttConfig.status" />
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">
          {{ mqttConfig.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </ContentWrap>

    <ContentWrap class="mt-4">
      <div class="flex items-center justify-between mb-4">
        <span class="text-lg font-medium">属性映射</span>
        <el-button type="primary" @click="handleAddMapping" v-hasPermi="['iot:device:update']">
          <Icon icon="ep:plus" class="mr-1" />
          新增映射
        </el-button>
      </div>

      <el-form :model="queryParams" :inline="true" class="-mb-15px">
        <el-form-item label="属性名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入属性名称"
            clearable
            class="!w-200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="标识符" prop="identifier">
          <el-input
            v-model="queryParams.identifier"
            placeholder="请输入标识符"
            clearable
            class="!w-200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="报文字段" prop="payloadKey">
          <el-input
            v-model="queryParams.payloadKey"
            placeholder="请输入 MQTT 报文字段"
            clearable
            class="!w-240px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="mappingLoading" :data="mappingList" :stripe="true" class="mt-4">
        <el-table-column label="属性名称" align="center" prop="name" min-width="120" />
        <el-table-column label="标识符" align="center" prop="identifier" min-width="120">
          <template #default="scope">
            <el-tag size="small" type="primary">{{ scope.row.identifier }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="报文字段"
          align="center"
          prop="payloadKey"
          min-width="220"
          show-overflow-tooltip
        />
        <el-table-column label="数据类型" align="center" prop="valueType" min-width="90" />
        <el-table-column label="排序号" align="center" prop="sort" min-width="90" />
        <el-table-column label="状态" align="center" prop="status" min-width="80">
          <template #default="scope">
            <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" width="120">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="handleEditMapping(scope.row)"
              v-hasPermi="['iot:device:update']"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDeleteMapping(scope.row.id!, scope.row.name || scope.row.payloadKey)"
              v-hasPermi="['iot:device:delete']"
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
        @pagination="getMappingPage"
      />
    </ContentWrap>

    <DeviceMqttConfigForm
      ref="configFormRef"
      :device-id="device.id"
      @success="getMqttConfig"
    />
    <DeviceMqttMappingForm
      ref="mappingFormRef"
      :device-id="device.id"
      :thing-model-list="thingModelList"
      :available-thing-model-list="availableThingModelList"
      @success="getMappingPage"
    />
  </div>
</template>

<script lang="ts" setup>
import { DeviceVO } from '@/api/iot/device/device'
import { ThingModelData } from '@/api/iot/thingmodel'
import { DeviceMqttConfigApi, DeviceMqttConfigVO } from '@/api/iot/device/mqtt/config'
import {
  DeviceMqttMappingApi,
  DeviceMqttMappingPageReqVO,
  DeviceMqttMappingVO
} from '@/api/iot/device/mqtt/mapping'
import { MqttSourceApi, MqttSourceSimpleVO } from '@/api/iot/realtime-data/mqtt-source'
import { DICT_TYPE } from '@/utils/dict'
import DeviceMqttConfigForm from './DeviceMqttConfigForm.vue'
import DeviceMqttMappingForm from './DeviceMqttMappingForm.vue'

defineOptions({ name: 'DeviceMqttConfig' })

const props = defineProps<{
  device: DeviceVO
  thingModelList: ThingModelData[]
}>()

const message = useMessage()
const sourceList = ref<MqttSourceSimpleVO[]>([])
const mqttConfig = ref<DeviceMqttConfigVO>({
  deviceId: props.device.id,
  sourceId: undefined,
  topic: 'data/test/v1',
  payloadMode: 'flat_json',
  reportTimeKey: '',
  reportTimeFormat: 'yyyy-MM-dd HH:mm:ss',
  status: 0,
  remark: ''
})

const sourceName = computed(() => {
  const source = sourceList.value.find((item) => item.id === mqttConfig.value.sourceId)
  return source
    ? `${source.name} (${source.brokerHost}:${source.brokerPort})`
    : String(mqttConfig.value.sourceId || '-')
})

const getMqttConfig = async () => {
  mqttConfig.value =
    (await DeviceMqttConfigApi.getMqttConfig(props.device.id)) || {
      deviceId: props.device.id,
      sourceId: undefined,
      topic: 'data/test/v1',
      payloadMode: 'flat_json',
      reportTimeKey: '',
      reportTimeFormat: 'yyyy-MM-dd HH:mm:ss',
      status: 0,
      remark: ''
    }
}

const getSourceList = async () => {
  sourceList.value = (await MqttSourceApi.getSimpleMqttSourceList(true)) || []
}

const formatPayloadMode = (mode?: string) => {
  if (mode === 'flat_json') {
    return '扁平 JSON'
  }
  return mode || '-'
}

const configFormRef = ref()
const handleEditConfig = async () => {
  await getSourceList()
  configFormRef.value?.open(mqttConfig.value)
}

const mappingLoading = ref(false)
const mappingList = ref<DeviceMqttMappingVO[]>([])
const total = ref(0)
const availableThingModelList = ref<ThingModelData[]>([])
const queryParams = reactive<DeviceMqttMappingPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  deviceId: props.device.id,
  name: undefined,
  identifier: undefined,
  payloadKey: undefined
})

const getMappingPage = async () => {
  mappingLoading.value = true
  try {
    const data = await DeviceMqttMappingApi.getMqttMappingPage(queryParams)
    mappingList.value = data.list
    total.value = data.total
  } finally {
    mappingLoading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getMappingPage()
}

const resetQuery = () => {
  queryParams.name = undefined
  queryParams.identifier = undefined
  queryParams.payloadKey = undefined
  handleQuery()
}

const mappingFormRef = ref()
const loadAvailableThingModelList = async () => {
  const data = await DeviceMqttMappingApi.getMqttMappingList({
    deviceId: props.device.id
  })
  availableThingModelList.value = data || []
}

const handleAddMapping = async () => {
  await loadAvailableThingModelList()
  if (availableThingModelList.value.length === 0) {
    message.warning('当前设备的物模型属性已全部配置为 MQTT 映射')
    return
  }
  mappingFormRef.value?.open('create')
}

const handleEditMapping = (row: DeviceMqttMappingVO) => {
  mappingFormRef.value?.open('update', row.id)
}

const handleDeleteMapping = async (id: number, name: string) => {
  try {
    await message.delConfirm(`确定要删除 MQTT 映射【${name}】吗？`)
    await DeviceMqttMappingApi.deleteMqttMapping(id)
    message.success('删除成功')
    await getMappingPage()
  } catch {}
}

onMounted(async () => {
  await getSourceList()
  await getMqttConfig()
  await getMappingPage()
})
</script>
