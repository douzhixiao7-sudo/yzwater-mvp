<template>
  <Dialog title="编辑 GENESIS64 连接配置" v-model="dialogVisible" width="640px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="130px"
      v-loading="formLoading"
      autocomplete="off"
    >
      <input
        class="hidden"
        type="text"
        name="genesis64-fake-username"
        autocomplete="username"
        tabindex="-1"
      />
      <input
        class="hidden"
        type="password"
        name="genesis64-fake-password"
        autocomplete="new-password"
        tabindex="-1"
      />
      <el-form-item label="接口地址" prop="baseUrl">
        <el-input v-model="formData.baseUrl" placeholder="请输入 GENESIS64 实时数据接口地址" />
      </el-form-item>
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="formData.username"
          name="genesis64-username"
          autocomplete="off"
          placeholder="请输入用户名"
        />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="formData.password"
          name="genesis64-password"
          type="password"
          show-password
          autocomplete="new-password"
          placeholder="请输入密码"
        />
      </el-form-item>
      <el-form-item label="请求超时(ms)" prop="timeout">
        <el-input-number
          v-model="formData.timeout"
          :min="1000"
          :step="1000"
          controls-position="right"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="采集频率(ms)" prop="collectInterval">
        <el-input-number
          v-model="formData.collectInterval"
          :min="1000"
          :step="1000"
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
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注，例如 300000 表示 5 分钟"
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
import { DeviceGenesisConfigApi, DeviceGenesisConfigVO } from '@/api/iot/device/genesis/config'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import { nextTick } from 'vue'

defineOptions({ name: 'DeviceGenesisConfigForm' })

const props = defineProps<{
  deviceId: number
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const message = useMessage()
const dialogVisible = ref(false)
const formLoading = ref(false)
const formData = ref<DeviceGenesisConfigVO>({
  deviceId: props.deviceId,
  baseUrl: '',
  username: '',
  password: '',
  timeout: 5000,
  collectInterval: 15000,
  status: CommonStatusEnum.ENABLE,
  remark: ''
})
const formRules = {
  baseUrl: [{ required: true, message: '请输入接口地址', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  timeout: [{ required: true, message: '请输入请求超时时间', trigger: 'blur' }],
  collectInterval: [{ required: true, message: '请输入采集频率', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}
const formRef = ref()
let clearAutofillTimer: ReturnType<typeof setTimeout> | undefined

const clearCreateCredentials = () => {
  formData.value.username = ''
  formData.value.password = ''
  formRef.value?.clearValidate(['username', 'password'])
}

const open = async (data?: DeviceGenesisConfigVO) => {
  dialogVisible.value = true
  resetForm()
  clearTimeout(clearAutofillTimer)
  if (data && data.id) {
    formData.value = { ...data }
    return
  }
  await nextTick()
  clearCreateCredentials()
  clearAutofillTimer = setTimeout(() => {
    if (!dialogVisible.value) {
      return
    }
    clearCreateCredentials()
  }, 80)
}

const resetForm = () => {
  formData.value = {
    deviceId: props.deviceId,
    baseUrl: '',
    username: '',
    password: '',
    timeout: 5000,
    collectInterval: 15000,
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
    await DeviceGenesisConfigApi.saveGenesisConfig(formData.value)
    message.success('保存成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

defineExpose({ open })
</script>
