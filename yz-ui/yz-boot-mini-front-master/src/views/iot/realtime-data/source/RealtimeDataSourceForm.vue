<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-form-item label="采集源名称" prop="name">
        <el-select v-model="formData.name" placeholder="请选择采集源名称" clearable>
          <el-option
            v-for="dict in stationOptions"
            :key="String(dict.value)"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="启用状态" prop="enabled">
        <el-switch v-model="formData.enabled" />
      </el-form-item>
      <el-form-item label="拉取地址" prop="url">
        <el-input v-model="formData.url" placeholder="请输入拉取地址" />
      </el-form-item>
      <el-form-item label="用户名" prop="username">
        <el-input v-model="formData.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="formData.password" placeholder="请输入密码" type="password" show-password />
      </el-form-item>
      <el-form-item label="请求体" prop="requestBody">
        <el-input
          v-model="formData.requestBody"
          placeholder="请输入请求体 JSON（可选）"
          type="textarea"
          :rows="4"
        />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="请输入备注" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { RealtimeDataSourceApi, RealtimeDataSource } from '@/api/iot/realtime-data/source'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'

/** IoT 实时数据采集源 表单 */
defineOptions({ name: 'RealtimeDataSourceForm' })

const { t } = useI18n()
const message = useMessage()

const stationOptions = computed(() => getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD))

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  name: undefined,
  code: undefined,
  enabled: true,
  url: undefined,
  username: undefined,
  password: undefined,
  requestBody: undefined,
  cron: '0 */5 * * * ?',
  remark: undefined
})
const formRules = reactive({
  name: [{ required: true, message: '采集源名称不能为空', trigger: 'change' }],
  enabled: [{ required: true, message: '启用状态不能为空', trigger: 'change' }],
  url: [{ required: true, message: '拉取地址不能为空', trigger: 'blur' }]
})
const formRef = ref()

const open = async (type: string, data?: string | RealtimeDataSource) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()

  if (!data) {
    return
  }
  if (typeof data !== 'string') {
    formData.value = { ...data }
    if (formData.value.name !== undefined && formData.value.name !== null) {
      formData.value.name = String(formData.value.name)
    }
    return
  }
  formLoading.value = true
  try {
    formData.value = await RealtimeDataSourceApi.getSource(data)
    if (formData.value.name !== undefined && formData.value.name !== null) {
      formData.value.name = String(formData.value.name)
    }
  } finally {
    formLoading.value = false
  }
}

defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as RealtimeDataSource
    if (formType.value === 'create') {
      await RealtimeDataSourceApi.createSource(data)
      message.success(t('common.createSuccess'))
    } else {
      await RealtimeDataSourceApi.updateSource(data)
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
    name: undefined,
    code: undefined,
    enabled: true,
    url: undefined,
    username: undefined,
    password: undefined,
    requestBody: undefined,
    cron: '0 */5 * * * ?',
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
