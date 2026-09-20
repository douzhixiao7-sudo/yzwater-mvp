<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="130px"
      v-loading="formLoading"
    >
      <el-form-item label="物模型属性" prop="thingModelId">
        <el-select
          v-model="formData.thingModelId"
          placeholder="请选择物模型属性"
          filterable
          class="!w-full"
          :no-data-text="thingModelSelectEmptyText"
        >
          <el-option
            v-for="item in selectablePropertyList"
            :key="item.id!"
            :label="`${item.name} (${item.identifier})`"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="报文字段" prop="payloadKey">
        <el-input
          v-model="formData.payloadKey"
          placeholder="请输入 MQTT JSON 字段，例如 ZMQBJ3_rise"
        />
      </el-form-item>
      <el-form-item label="排序号" prop="sort">
        <el-input-number
          v-model="formData.sort"
          :min="0"
          :step="1"
          controls-position="right"
          class="!w-full"
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
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitForm" :loading="formLoading">确定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { ThingModelData } from '@/api/iot/thingmodel'
import { DeviceMqttMappingApi, DeviceMqttMappingVO } from '@/api/iot/device/mqtt/mapping'
import { IoTThingModelTypeEnum } from '@/views/iot/utils/constants'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'

defineOptions({ name: 'DeviceMqttMappingForm' })

const props = defineProps<{
  deviceId: number
  thingModelList: ThingModelData[]
  availableThingModelList: ThingModelData[]
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const { t } = useI18n()
const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<DeviceMqttMappingVO>({
  deviceId: props.deviceId,
  thingModelId: undefined,
  payloadKey: '',
  sort: 0,
  status: CommonStatusEnum.ENABLE,
  remark: ''
})
const formRules = {
  thingModelId: [{ required: true, message: '请选择物模型属性', trigger: 'change' }],
  payloadKey: [{ required: true, message: '请输入 MQTT 报文字段', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}
const formRef = ref()

const propertyList = computed(() => {
  return props.thingModelList.filter((item) => item.type === IoTThingModelTypeEnum.PROPERTY)
})

const selectablePropertyList = computed(() => {
  if (formType.value !== 'create') {
    return propertyList.value
  }
  return props.availableThingModelList
})

const thingModelSelectEmptyText = computed(() => {
  if (formType.value === 'create' && selectablePropertyList.value.length === 0) {
    return '当前设备的物模型属性已全部配置为 MQTT 映射'
  }
  return '暂无可选物模型属性'
})

const open = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  formType.value = type
  dialogTitle.value = t('action.' + type)
  resetForm()
  if (type === 'update' && id) {
    formLoading.value = true
    try {
      formData.value = await DeviceMqttMappingApi.getMqttMapping(id)
    } finally {
      formLoading.value = false
    }
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    formData.value.deviceId = props.deviceId
    if (formType.value === 'create') {
      await DeviceMqttMappingApi.createMqttMapping(formData.value)
      message.success('创建成功')
    } else {
      await DeviceMqttMappingApi.updateMqttMapping(formData.value)
      message.success('更新成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    deviceId: props.deviceId,
    thingModelId: undefined,
    payloadKey: '',
    sort: 0,
    status: CommonStatusEnum.ENABLE,
    remark: ''
  }
  formRef.value?.resetFields()
}

defineExpose({ open })
</script>
