<template>
  <div class="device-rating">
    <div class="rating-card">
      <div class="rating-title">设备评级</div>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="设备评级" prop="ratingResult">
              <el-select v-model="formData.ratingResult" placeholder="请选择评级标准" clearable class="!w-full">
                <el-option
                  v-for="item in ratingOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评级日期" prop="ratingTime">
              <el-date-picker
                v-model="formData.ratingTime"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                placeholder="请选择评级日期"
                class="!w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评级人员" prop="ratingUserName">
              <el-input v-model="formData.ratingUserName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评级说明" prop="ratingBasis">
              <el-input
                v-model="formData.ratingBasis"
                type="textarea"
                :rows="3"
                placeholder="请填写评级依据说明"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评级附件" prop="attachments">
              <UploadFile
                v-model="formData.attachments"
                :limit="5"
                :file-type="ratingFileTypes"
                :file-size="10"
              />
            </el-form-item>
          </el-col>
          <el-col v-if="isUnqualified" :span="12">
            <el-form-item label="整改建议" prop="rectifyAdvice">
              <el-input
                v-model="formData.rectifyAdvice"
                type="textarea"
                :rows="3"
                placeholder="不合格需填写整改建议"
              />
            </el-form-item>
          </el-col>
          <el-col v-if="isUnqualified" :span="12">
            <el-form-item label="整改期限" prop="rectifyDeadline">
              <el-date-picker
                v-model="formData.rectifyDeadline"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                placeholder="请选择整改期限"
                class="!w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="rating-actions">
          <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
        </div>
      </el-form>
    </div>

    <div class="rating-card">
      <div class="rating-title">历史评级</div>
      <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
        <el-table-column label="评级时间" prop="ratingTime" min-width="140">
          <template #default="scope">
            {{ scope.row.ratingTime ? formatDate(scope.row.ratingTime, 'YYYY-MM-DD') : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="评级等级" prop="ratingResult" min-width="120">
          <template #default="scope">
            <el-tag :type="resolveRatingTag(scope.row.ratingResult)">
              {{ getRatingLabel(scope.row.ratingResult) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评级人员" prop="ratingUserName" min-width="120" />
        <el-table-column label="评级说明" prop="ratingBasis" min-width="180" />
        <el-table-column label="附件" min-width="120">
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
import { UploadFile } from '@/components/UploadFile'
import { DeviceRatingApi, type DeviceRatingVO } from '@/api/iot/device-rating'
import { DICT_TYPE, getDictLabel, getStrDictOptions } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import { useUserStore } from '@/store/modules/user'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'DeviceDetailsRating' })

const props = defineProps<{
  deviceId?: number | string
}>()

const message = useMessage()
const userStore = useUserStore()

const normalizeDeviceId = computed(() => {
  if (props.deviceId === undefined || props.deviceId === null || props.deviceId === '') {
    return undefined
  }
  return String(props.deviceId)
})

const ratingFileTypes = ['png', 'jpg', 'jpeg', 'pdf', 'doc', 'docx']

const ratingOptionFallback = [
  { label: '优秀', value: 'excellent' },
  { label: '良好', value: 'good' },
  { label: '合格', value: 'qualified' },
  { label: '不合格', value: 'unqualified' }
]

const ratingOptions = computed(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_DEVICE_RATING)
  if (dictOptions.length) {
    return dictOptions.map((item) => ({ label: item.label, value: String(item.value) }))
  }
  return ratingOptionFallback
})

const ratingLabelMap = ratingOptionFallback.reduce<Record<string, string>>((map, item) => {
  map[item.value] = item.label
  return map
}, {})

const getRatingLabel = (value?: string) => {
  if (!value) return '-'
  return getDictLabel(DICT_TYPE.IOT_DEVICE_RATING, value) || ratingLabelMap[value] || value
}

const resolveRatingTag = (value?: string) => {
  const label = getRatingLabel(value)
  if (label === '优秀') return 'success'
  if (label === '良好') return 'info'
  if (label === '合格') return 'warning'
  if (label === '不合格') return 'danger'
  return ''
}

const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const formData = reactive<DeviceRatingVO>({
  deviceId: undefined,
  ratingResult: '',
  ratingTime: dayjs().format('YYYY-MM-DD'),
  ratingUserName: userStore.getUser.nickname || '',
  ratingBasis: '',
  rectifyAdvice: '',
  rectifyDeadline: '',
  attachments: []
})

const isUnqualified = computed(() => getRatingLabel(formData.ratingResult) === '不合格')

const validateRectifyAdvice = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (!isUnqualified.value) {
    callback()
    return
  }
  if (!value) {
    callback(new Error('整改建议不能为空'))
    return
  }
  callback()
}

const validateRectifyDeadline = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (!isUnqualified.value) {
    callback()
    return
  }
  if (!value) {
    callback(new Error('整改期限不能为空'))
    return
  }
  callback()
}

const validateAttachments = (_rule: any, value: string[] | string, callback: (error?: Error) => void) => {
  if (!isUnqualified.value) {
    callback()
    return
  }
  if (!value || (Array.isArray(value) && value.length === 0)) {
    callback(new Error('请上传附件'))
    return
  }
  if (!Array.isArray(value) && String(value).trim() === '') {
    callback(new Error('请上传附件'))
    return
  }
  callback()
}

const formRules: FormRules = {
  ratingResult: [{ required: true, message: '请选择评级标准', trigger: 'change' }],
  ratingTime: [{ required: true, message: '请选择评级日期', trigger: 'change' }],
  ratingUserName: [{ required: true, message: '评级人员不能为空', trigger: 'blur' }],
  ratingBasis: [{ required: true, message: '评级说明不能为空', trigger: 'blur' }],
  rectifyAdvice: [{ validator: validateRectifyAdvice, trigger: 'blur' }],
  rectifyDeadline: [{ validator: validateRectifyDeadline, trigger: 'change' }],
  attachments: [{ validator: validateAttachments, trigger: 'change' }]
}

const list = ref<DeviceRatingVO[]>([])
const total = ref(0)
const loading = ref(false)
const pageInfo = reactive({ pageNo: 1, pageSize: 10 })

const getList = async () => {
  if (!normalizeDeviceId.value) {
    list.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const data = await DeviceRatingApi.getDeviceRatingPage({
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

const normalizeAttachments = (
  attachments?: string[] | string | Array<{ url?: string }>
) => {
  if (!attachments) return []
  if (Array.isArray(attachments)) {
    return attachments
      .map((item) => (typeof item === 'string' ? item : item?.url))
      .filter((item): item is string => Boolean(item))
  }
  return attachments.split(',').filter(Boolean)
}

const resetForm = () => {
  Object.assign(formData, {
    deviceId: normalizeDeviceId.value,
    ratingResult: '',
    ratingTime: dayjs().format('YYYY-MM-DD'),
    ratingUserName: userStore.getUser.nickname || '',
    ratingBasis: '',
    rectifyAdvice: '',
    rectifyDeadline: '',
    attachments: []
  })
  formRef.value?.clearValidate()
}

const submitForm = async () => {
  if (!normalizeDeviceId.value) {
    message.warning('设备信息缺失，无法评级')
    return
  }
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }
  submitLoading.value = true
  try {
    const payload: DeviceRatingVO = {
      deviceId: normalizeDeviceId.value,
      ratingResult: formData.ratingResult,
      ratingTime: formData.ratingTime,
      ratingUserName: formData.ratingUserName,
      ratingBasis: formData.ratingBasis,
      rectifyAdvice: formData.rectifyAdvice || undefined,
      rectifyDeadline: formData.rectifyDeadline || undefined,
      attachments: normalizeAttachments(formData.attachments)
    }
    await DeviceRatingApi.createDeviceRating(payload)
    message.success('保存成功')
    pageInfo.pageNo = 1
    await getList()
    resetForm()
  } finally {
    submitLoading.value = false
  }
}

const resolveAttachments = (row: DeviceRatingVO) => {
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

watch(
  () => props.deviceId,
  () => {
    pageInfo.pageNo = 1
    resetForm()
    getList()
  },
  { immediate: true }
)

watch(
  () => formData.ratingResult,
  () => {
    formRef.value?.clearValidate(['rectifyAdvice', 'rectifyDeadline', 'attachments'])
  }
)
</script>

<style scoped>
.device-rating {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rating-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 16px;
  background-color: var(--el-bg-color);
}

.rating-title {
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
}

.rating-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 8px;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
</style>