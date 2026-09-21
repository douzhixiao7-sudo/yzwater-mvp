<template>
  <Dialog title="编辑 MQTT 采集配置" v-model="dialogVisible" width="640px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="130px"
      v-loading="formLoading"
    >
      <el-form-item label="MQTT数据源" prop="sourceId">
        <el-select
          v-model="formData.sourceId"
          placeholder="请选择 MQTT 数据源"
          filterable
          class="!w-full"
        >
          <el-option
            v-for="source in sourceList"
            :key="source.id"
            :label="`${source.name} (${source.brokerHost}:${source.brokerPort})`"
            :value="source.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="订阅主题" prop="topic">
        <el-input v-model="formData.topic" placeholder="请输入订阅主题，例如 data/test/v1" />
      </el-form-item>
      <el-form-item label="负载模式" prop="payloadMode">
        <el-select v-model="formData.payloadMode" class="!w-full">
          <el-option label="扁平 JSON" value="flat_json" />
        </el-select>
      </el-form-item>
      <el-form-item label="报文时间字段" prop="reportTimeKey">
        <el-input v-model="formData.reportTimeKey" placeholder="可为空，默认使用接收时间" />
      </el-form-item>
      <el-form-item label="报文时间格式" prop="reportTimeFormat">
        <el-input
          v-model="formData.reportTimeFormat"
          placeholder="例如 yyyy-MM-dd HH:mm:ss；为空时自动解析常见格式"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注，例如：固定主题 data/test/v1"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitForm" :loading="formLoading">确定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DeviceMqttConfigApi, DeviceMqttConfigVO } from '@/api/iot/device/mqtt/config'
import { MqttSourceApi, MqttSourceSimpleVO } from '@/api/iot/realtime-data/mqtt-source'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'

defineOptions({ name: 'DeviceMqttConfigForm' })

const props = defineProps<{
  deviceId: number
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const message = useMessage()
const dialogVisible = ref(false)
const formLoading = ref(false)
const sourceList = ref<MqttSourceSimpleVO[]>([])
const formData = ref<DeviceMqttConfigVO>({
  deviceId: props.deviceId,
  sourceId: undefined,
  topic: 'data/test/v1',
  payloadMode: 'flat_json',
  reportTimeKey: '',
  reportTimeFormat: 'yyyy-MM-dd HH:mm:ss',
  status: CommonStatusEnum.ENABLE,
  remark: ''
})
const formRules = {
  sourceId: [{ required: true, message: '请选择 MQTT 数据源', trigger: 'change' }],
  topic: [{ required: true, message: '请输入订阅主题', trigger: 'blur' }],
  payloadMode: [{ required: true, message: '请选择负载模式', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}
const formRef = ref()

const open = async (data?: DeviceMqttConfigVO) => {
  dialogVisible.value = true
  resetForm()
  sourceList.value = (await MqttSourceApi.getSimpleMqttSourceList(true)) || []
  if (data && data.id) {
    formData.value = { ...data }
  }
}

const resetForm = () => {
  formData.value = {
    deviceId: props.deviceId,
    sourceId: undefined,
    topic: 'data/test/v1',
    payloadMode: 'flat_json',
    reportTimeKey: '',
    reportTimeFormat: 'yyyy-MM-dd HH:mm:ss',
    status: CommonStatusEnum.ENABLE,
    remark: ''
  }
  formRef.value?.resetFields()
}

const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    formData.value.deviceId = props.deviceId
    await DeviceMqttConfigApi.saveMqttConfig(formData.value)
    message.success('保存成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

defineExpose({ open })
</script>
