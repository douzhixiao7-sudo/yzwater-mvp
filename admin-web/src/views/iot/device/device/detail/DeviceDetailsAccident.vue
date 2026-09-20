<template>
  <div class="device-accident">
    <div class="accident-card">
      <div class="accident-title">事故登记</div>
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
        class="accident-form"
      >
        <div class="accident-form-body">
          <el-row :gutter="16" class="accident-form-row">
          <el-col :span="8">
            <el-form-item label="事故时间" prop="accidentTime">
              <el-date-picker
                v-model="formData.accidentTime"
                type="datetime"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择事故发生时间"
                class="!w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="事故类型" prop="accidentType">
              <el-select v-model="formData.accidentType" placeholder="请选择事故类型" clearable class="!w-full">
                <el-option
                  v-for="item in accidentOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="事故地点" prop="accidentLocation">
              <el-input v-model="formData.accidentLocation" placeholder="请输入事故地点" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="责任人" prop="responsibleName">
              <el-input v-model="formData.responsibleName" placeholder="请输入责任人" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="损失评估" prop="lossAssessment">
              <el-input
                v-model="formData.lossAssessment"
                type="textarea"
                :rows="2"
                placeholder="请输入损失评估"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="事故图片" prop="attachments">
              <UploadImgs
                v-model="formData.attachments"
                :limit="6"
                :file-type="accidentImageTypes"
                :file-size="10"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="事故描述" prop="accidentDesc">
              <el-input
                v-model="formData.accidentDesc"
                type="textarea"
                :rows="3"
                placeholder="请输入事故描述"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理结果" prop="handleResult">
              <el-input
                v-model="formData.handleResult"
                type="textarea"
                :rows="3"
                placeholder="请输入处理结果"
              />
            </el-form-item>
          </el-col>
          </el-row>
        </div>
        <div class="accident-actions">
          <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
        </div>
      </el-form>
    </div>

    <div class="accident-card">
      <div class="accident-title">历史事故</div>
      <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
        <el-table-column label="事故时间" prop="accidentTime" min-width="160">
          <template #default="scope">
            {{ formatAccidentTime(scope.row.accidentTime) }}
          </template>
        </el-table-column>
        <el-table-column label="事故类型" prop="accidentType" min-width="120">
          <template #default="scope">
            <dict-tag :type="DICT_TYPE.IOT_DEVICE_ACCIDENT_TYPE" :value="scope.row.accidentType" />
          </template>
        </el-table-column>
        <el-table-column label="事故地点" prop="accidentLocation" min-width="160" />
        <el-table-column label="事故描述" prop="accidentDesc" min-width="200" />
        <el-table-column label="处理结果" prop="handleResult" min-width="160" />
        <el-table-column label="损失评估" prop="lossAssessment" min-width="160" />
        <el-table-column label="责任人" prop="responsibleName" min-width="120" />
        <el-table-column label="登记人" prop="creator" min-width="120">
          <template #default="scope">
            {{ scope.row.creatorName || scope.row.creator || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="登记时间" prop="createTime" min-width="160">
          <template #default="scope">
            {{ scope.row.createTime ? formatDate(scope.row.createTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="事故图片" min-width="120">
          <template #default="scope">
            <el-popover
              v-if="resolveAttachments(scope.row).length"
              placement="top"
              trigger="click"
              :width="260"
            >
              <div class="attachment-list">
                <el-link
                  v-for="(file, index) in resolveAttachments(scope.row)"
                  :key="file + index"
                  :href="file"
                  target="_blank"
                  type="primary"
                  :underline="false"
                >
                  {{ getFileName(file) }}
                </el-link>
              </div>
              <template #reference>
                <el-link type="primary" :underline="false">查看</el-link>
              </template>
            </el-popover>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        :total="total"
        v-model:page="pageInfo.pageNo"
        v-model:limit="pageInfo.pageSize"
        @pagination="getList"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { UploadImgs } from '@/components/UploadFile'
import { DeviceAccidentApi, type DeviceAccidentVO } from '@/api/iot/device-accident'
import { DeviceLocationApi } from '@/api/iot/device/location'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'DeviceDetailsAccident' })

const props = defineProps<{
  deviceId?: number | string
  deviceAddress?: number | string
}>()

const message = useMessage()

const route = useRoute()
const normalizeDeviceId = computed(() => {
  const raw = props.deviceId ?? route.params.id
  if (raw === undefined || raw === null || raw === '') {
    return undefined
  }
  return String(raw)
})

const accidentImageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']

const accidentOptionFallback = [
  { label: '设备故障', value: 'device_fault' },
  { label: '操作失误', value: 'operation_error' },
  { label: '其他', value: 'other' }
]

const accidentOptions = computed(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_DEVICE_ACCIDENT_TYPE)
  if (dictOptions.length) {
    return dictOptions.map((item) => ({ label: item.label, value: String(item.value) }))
  }
  return accidentOptionFallback
})

const deviceLocationName = ref('')

const loadDeviceLocationName = async () => {
  if (props.deviceAddress === undefined || props.deviceAddress === null || props.deviceAddress === '') {
    deviceLocationName.value = ''
    return
  }
  const raw = String(props.deviceAddress)
  if (!/^\d+$/.test(raw)) {
    deviceLocationName.value = raw
    return
  }
  try {
    const info = await DeviceLocationApi.getDeviceLocation(raw)
    deviceLocationName.value = info?.name || raw
  } catch {
    deviceLocationName.value = raw
  }
}

const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const formData = reactive<DeviceAccidentVO>({
  deviceId: undefined,
  accidentTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  accidentLocation: '',
  accidentType: '',
  accidentDesc: '',
  lossAssessment: '',
  handleResult: '',
  responsibleName: '',
  attachments: []
})

const formRules: FormRules = {
  accidentTime: [{ required: true, message: '请选择事故发生时间', trigger: 'change' }],
  accidentLocation: [{ required: true, message: '请输入事故地点', trigger: 'blur' }],
  accidentType: [{ required: true, message: '请选择事故类型', trigger: 'change' }],
  accidentDesc: [{ required: true, message: '请输入事故描述', trigger: 'blur' }],
  lossAssessment: [{ required: true, message: '请输入损失评估', trigger: 'blur' }]
}

const list = ref<DeviceAccidentVO[]>([])
const total = ref(0)
const loading = ref(false)
const pageInfo = reactive({ pageNo: 1, pageSize: 10 })

const normalizeAttachments = (
  attachments?: string[] | string | Array<{ url?: string }>
) => {
  if (!attachments) return []
  if (Array.isArray(attachments)) {
    return attachments
      .map((item) => (typeof item === 'string' ? item : item?.url))
      .filter((item): item is string => Boolean(item) && !item.startsWith('blob:'))
  }
  return attachments
    .split(',')
    .filter((item) => item && !item.startsWith('blob:'))
}

const resetForm = () => {
  Object.assign(formData, {
    deviceId: normalizeDeviceId.value,
    accidentTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    accidentLocation: deviceLocationName.value || '',
    accidentType: '',
    accidentDesc: '',
    lossAssessment: '',
    handleResult: '',
    responsibleName: '',
    attachments: []
  })
  formRef.value?.clearValidate()
}

const getList = async () => {
  if (!normalizeDeviceId.value) {
    list.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const data = await DeviceAccidentApi.getDeviceAccidentPage({
      pageNo: pageInfo.pageNo,
      pageSize: pageInfo.pageSize,
      deviceId: normalizeDeviceId.value
    })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  if (!normalizeDeviceId.value) {
    message.warning('设备信息缺失，无法登记事故')
    return
  }
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }
  submitLoading.value = true
  try {
    const payload: DeviceAccidentVO = {
      deviceId: normalizeDeviceId.value,
      accidentTime: formData.accidentTime,
      accidentLocation: formData.accidentLocation,
      accidentType: formData.accidentType,
      accidentDesc: formData.accidentDesc,
      lossAssessment: formData.lossAssessment,
      handleResult: formData.handleResult || undefined,
      responsibleName: formData.responsibleName || undefined,
      attachments: normalizeAttachments(formData.attachments)
    }
    await DeviceAccidentApi.createDeviceAccident(payload)
    message.success('保存成功')
    pageInfo.pageNo = 1
    await getList()
    resetForm()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (id?: number) => {
  if (!id) return
  try {
    await message.delConfirm()
    await DeviceAccidentApi.deleteDeviceAccident(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

const resolveAttachments = (row: DeviceAccidentVO) => {
  return normalizeAttachments(row.attachments)
}

const getFileName = (url?: string) => {
  if (!url) return '-'
  const cleanUrl = url.split('?')[0]
  const rawName = cleanUrl.substring(cleanUrl.lastIndexOf('/') + 1)
  if (!rawName) return '-'
  try {
    return decodeURIComponent(rawName)
  } catch {
    return rawName
  }
}

const formatAccidentTime = (value?: string | number | Date | Array<number>) => {
  if (!value) return '-'
  const formatted = formatDate(value, 'YYYY-MM-DD HH:mm:ss')
  return formatted || String(value)
}

watch(
  () => [props.deviceId, props.deviceAddress],
  async () => {
    await loadDeviceLocationName()
    pageInfo.pageNo = 1
    resetForm()
    getList()
  },
  { immediate: true }
)
</script>

<style scoped>
.device-accident {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.accident-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 16px;
  background-color: var(--el-bg-color);
}

.accident-title {
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
}

.accident-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.accident-form-body {
  padding: 12px;
  border-radius: 10px;
  border: 1px dashed var(--el-border-color-lighter);
}

.accident-form-row {
  row-gap: 12px;
}

.accident-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.accident-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--el-text-color-regular);
}

.accident-form :deep(.el-input__wrapper),
.accident-form :deep(.el-textarea__inner) {
  background: transparent;
}

.accident-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 4px;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
</style>
