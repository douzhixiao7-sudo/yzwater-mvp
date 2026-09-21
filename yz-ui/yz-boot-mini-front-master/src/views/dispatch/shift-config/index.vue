<template>
  <div class="shift-config-page">
    <ContentWrap class="query-wrap">
      <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
        <el-form-item label="班次名称" prop="shiftName">
          <el-input
            v-model="queryParams.shiftName"
            clearable
            class="!w-220px"
            placeholder="请输入班次名称"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="所属站点" prop="stationId">
          <el-select v-model="queryParams.stationId" clearable filterable class="!w-180px" placeholder="请选择所属站点">
            <el-option
              v-for="option in stationOptions"
              :key="String(option.value)"
              :label="option.label"
              :value="String(option.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间段" prop="timeRange">
          <el-time-picker
            v-model="timeRange"
            is-range
            class="!w-280px"
            value-format="HH:mm"
            format="HH:mm"
            range-separator="至"
            start-placeholder="起始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-4px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-4px" />
            重置
          </el-button>
          <el-button
            v-hasPermi="['iot:shift-config:create']"
            type="primary"
            plain
            @click="openForm('create')"
          >
            <Icon icon="ep:plus" class="mr-4px" />
            新增班次
          </el-button>
          <el-button
            v-hasPermi="['iot:shift-config:export']"
            type="success"
            plain
            :loading="exportLoading"
            @click="handleExport"
          >
            <Icon icon="ep:download" class="mr-4px" />
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="table-wrap">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="班次编号" min-width="140">
          <template #default="{ row }">
            <span class="shift-no">{{ row.shiftNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="班次名称" prop="shiftName" min-width="150" show-overflow-tooltip />
        <el-table-column label="所属站点" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ resolveStationLabel(row.stationId) || '-' }}</template>
        </el-table-column>
        <el-table-column label="起始时间" min-width="110">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" min-width="110">
          <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="跨天" min-width="90">
          <template #default="{ row }">
            <el-tag :type="row.crossDay ? 'warning' : 'info'">{{ row.crossDay ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="220" show-overflow-tooltip />
        <el-table-column label="创建人" prop="creator" min-width="120" show-overflow-tooltip />
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="info" @click="openForm('detail', row.id)">详情</el-button>
            <el-button
              v-hasPermi="['iot:shift-config:update']"
              link
              type="primary"
              @click="openForm('update', row.id)"
            >
              编辑
            </el-button>
            <el-button
              v-hasPermi="['iot:shift-config:delete']"
              link
              type="danger"
              @click="handleDelete(row.id)"
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

    <Dialog v-model="formVisible" :title="formTitle" width="760px">
      <el-form
        ref="formRef"
        v-loading="formLoading"
        :model="formData"
        :rules="formRules"
        :disabled="formReadonly"
        label-width="96px"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="班次编号">
              <el-input :model-value="formData.shiftNo || '系统自动生成'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班次名称" prop="shiftName">
              <el-input v-model="formData.shiftName" maxlength="64" placeholder="请输入班次名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属站点" prop="stationId">
              <el-select v-model="formData.stationId" filterable clearable class="!w-full" placeholder="请选择所属站点">
                <el-option
                  v-for="option in stationOptions"
                  :key="String(option.value)"
                  :label="option.label"
                  :value="String(option.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="起始时间" prop="startTime">
              <el-time-picker
                v-model="formData.startTime"
                class="!w-full"
                value-format="HH:mm"
                format="HH:mm"
                placeholder="请选择起始时间"
                :disabled-hours="getStartDisabledHours"
                :disabled-minutes="getStartDisabledMinutes"
              />
              <div v-if="createStartTimeHint" class="form-item-tip">{{ createStartTimeHint }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-time-picker
                v-model="formData.endTime"
                class="!w-full"
                value-format="HH:mm"
                format="HH:mm"
                placeholder="请选择结束时间"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="跨天标识" prop="crossDay">
              <el-switch v-model="formData.crossDay" active-text="是" inactive-text="否" @change="handleCrossDayChange" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                placeholder="请输入备注"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">{{ formReadonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!formReadonly" type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { ShiftConfigApi, type ShiftConfigPageReqVO, type ShiftConfigVO } from '@/api/iot/shift/config'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'IotShiftConfig' })

type FormMode = 'create' | 'update' | 'detail'
type OptionItem = {
  label: string
  value: string | number
}

const message = useMessage()
const stationOptions = computed<OptionItem[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const loading = ref(false)
const exportLoading = ref(false)
const list = ref<ShiftConfigVO[]>([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()
const timeRange = ref<string[]>([])
const queryParams = reactive<ShiftConfigPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  stationId: undefined,
  shiftName: undefined,
  timeRange: undefined
})

const buildQueryParams = (): ShiftConfigPageReqVO => {
  return {
    ...queryParams,
    timeRange: timeRange.value?.length === 2 ? timeRange.value : undefined
  }
}

const formatDateTime = (value?: string | number) => {
  if (value === undefined || value === null || value === '') return '-'
  const raw = typeof value === 'string' && /^\d+$/.test(value) ? Number(value) : value
  const timestamp = typeof raw === 'number' && raw > 0 && raw < 1000000000000 ? raw * 1000 : raw
  const parsed = dayjs(timestamp)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : '-'
}

const normalizeTime = (value?: string | null) => {
  if (!value) return ''
  const text = String(value).trim()
  const directMatch = text.match(/^(\d{2}:\d{2})(?::\d{2})?$/)
  if (directMatch) return directMatch[1]
  const parsed = dayjs(text)
  return parsed.isValid() ? parsed.format('HH:mm') : ''
}

const formatTime = (value?: string | null) => normalizeTime(value) || '-'
const resolveStationLabel = (stationId?: string) => {
  if (!stationId) return ''
  return stationOptions.value.find((item) => String(item.value) === String(stationId))?.label || stationId
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ShiftConfigApi.getPage(buildQueryParams())
    list.value = data.list || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  timeRange.value = []
  queryParams.pageNo = 1
  await getList()
}

const handleDelete = async (id?: number) => {
  if (!id) return
  await message.delConfirm('确认删除该班次吗？')
  await ShiftConfigApi.remove(id)
  message.success('删除成功')
  await getList()
}

const handleExport = async () => {
  await message.exportConfirm()
  exportLoading.value = true
  try {
    const data = await ShiftConfigApi.exportExcel(buildQueryParams())
    download.excel(data, `班次管理_${dayjs().format('YYYYMMDD_HHmmss')}.xls`)
  } finally {
    exportLoading.value = false
  }
}

const formVisible = ref(false)
const formMode = ref<FormMode>('create')
const formRef = ref<FormInstance>()
const formLoading = ref(false)
const submitLoading = ref(false)

const createEmptyFormData = (): ShiftConfigVO => ({
  id: undefined,
  shiftNo: '',
  shiftName: '',
  stationId: undefined,
  startTime: '',
  endTime: '',
  crossDay: false,
  remark: ''
})
const formData = reactive<ShiftConfigVO>(createEmptyFormData())
const createStartMinTime = ref('')

const parseTimeToMinutes = (value?: string | null) => {
  const text = normalizeTime(value)
  if (!text) return -1
  const [hourText, minuteText] = text.split(':')
  const hour = Number(hourText)
  const minute = Number(minuteText)
  if (!Number.isInteger(hour) || !Number.isInteger(minute)) return -1
  if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return -1
  return hour * 60 + minute
}

const createStartTimeHint = computed(() => {
  if (formReadonly.value || !createStartMinTime.value) return ''
  return `同一天班次起始时间不可早于 ${createStartMinTime.value}（允许与上一班次最多重叠30分钟）`
})

const getStartDisabledHours = () => {
  if (formReadonly.value) return []
  const minMinutes = parseTimeToMinutes(createStartMinTime.value)
  if (minMinutes < 0) return []
  const hour = Math.floor(minMinutes / 60)
  const minute = minMinutes % 60
  const disabled: number[] = []
  for (let index = 0; index < hour; index += 1) {
    disabled.push(index)
  }
  if (minute >= 59) {
    disabled.push(hour)
  }
  return disabled
}

const getStartDisabledMinutes = (selectedHour: number) => {
  if (formReadonly.value) return []
  const minMinutes = parseTimeToMinutes(createStartMinTime.value)
  if (minMinutes < 0) return []
  const hour = Math.floor(minMinutes / 60)
  const minute = minMinutes % 60
  if (selectedHour !== hour) return []
  const disabled: number[] = []
  for (let index = 0; index < minute; index += 1) {
    disabled.push(index)
  }
  return disabled
}

const loadCreateStartMinTime = async (excludeId?: number) => {
  createStartMinTime.value = ''
  const pageSize = 100
  let pageNo = 1
  let allShifts: ShiftConfigVO[] = []
  while (pageNo <= 50) {
    const pageResult = await ShiftConfigApi.getPage({ pageNo, pageSize, shiftName: undefined, timeRange: undefined })
    const currentList = (pageResult.list || []) as ShiftConfigVO[]
    allShifts = allShifts.concat(currentList)
    const totalCount = Number(pageResult.total || 0)
    if (allShifts.length >= totalCount || currentList.length < pageSize) {
      break
    }
    pageNo += 1
  }
  const latestEndMinutes = allShifts
    .filter((item) => !item.crossDay && (excludeId === undefined || Number(item.id) !== Number(excludeId)))
    .map((item) => parseTimeToMinutes(item.endTime))
    .filter((item) => item >= 0)
    .reduce((max, value) => (value > max ? value : max), -1)
  if (latestEndMinutes < 0) return
  const earliestStartMinutes = Math.max(0, latestEndMinutes - 30)
  const hour = String(Math.floor(earliestStartMinutes / 60)).padStart(2, '0')
  const minute = String(earliestStartMinutes % 60).padStart(2, '0')
  createStartMinTime.value = `${hour}:${minute}`
}

const validateTimeRule = (_rule: any, _value: any, callback: (error?: Error) => void) => {
  if (!formData.startTime || !formData.endTime) {
    callback(new Error('起始时间和结束时间不能为空'))
    return
  }
  const start = dayjs(`2000-01-01 ${formData.startTime}`)
  const end = dayjs(`2000-01-01 ${formData.endTime}`)
  if (!start.isValid() || !end.isValid()) {
    callback(new Error('时间格式不正确'))
    return
  }
  if (end.isAfter(start)) {
    callback()
    return
  }
  if (formData.crossDay) {
    callback()
    return
  }
  callback(new Error('结束时间早于或等于起始时间时，请勾选跨天标识'))
}

const validateCreateStartTimeRule = (_rule: any, _value: any, callback: (error?: Error) => void) => {
  if (formReadonly.value || !createStartMinTime.value || !formData.startTime) {
    callback()
    return
  }
  const startMinutes = parseTimeToMinutes(formData.startTime)
  const minMinutes = parseTimeToMinutes(createStartMinTime.value)
  if (startMinutes >= minMinutes) {
    callback()
    return
  }
  callback(new Error(`同一天班次起始时间不可早于 ${createStartMinTime.value}`))
}

const formRules: FormRules<ShiftConfigVO> = {
  shiftName: [{ required: true, message: '班次名称不能为空', trigger: 'blur' }],
  stationId: [{ required: true, message: '所属站点不能为空', trigger: 'change' }],
  startTime: [
    { required: true, message: '起始时间不能为空', trigger: 'change' },
    { validator: validateCreateStartTimeRule, trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '结束时间不能为空', trigger: 'change' },
    { validator: validateTimeRule, trigger: 'change' }
  ]
}

const formReadonly = computed(() => formMode.value === 'detail')
const formTitle = computed(() => {
  if (formMode.value === 'create') return '新增班次'
  if (formMode.value === 'update') return '编辑班次'
  return '班次详情'
})

const resetFormData = () => {
  Object.assign(formData, createEmptyFormData())
  createStartMinTime.value = ''
  formRef.value?.clearValidate()
}

const openForm = async (mode: FormMode, id?: number) => {
  formMode.value = mode
  formVisible.value = true
  formLoading.value = true
  resetFormData()
  try {
    if (mode === 'create') {
      try {
        await loadCreateStartMinTime()
      } catch (error) {
        createStartMinTime.value = ''
        message.warning('未能加载已配置班次时间，请手动确认起始时间')
      }
      return
    }
    if (!id) return
    const data = await ShiftConfigApi.get(id)
    Object.assign(formData, {
      ...createEmptyFormData(),
      ...data
    })
    formData.startTime = normalizeTime(data.startTime)
    formData.endTime = normalizeTime(data.endTime)
    formData.crossDay = Boolean(data.crossDay)
    try {
      await loadCreateStartMinTime(Number(data.id))
    } catch (error) {
      createStartMinTime.value = ''
      message.warning('未能加载班次时间规则，请手动确认起始时间')
    }
  } finally {
    formLoading.value = false
  }
}

const handleCrossDayChange = () => {
  if (!formReadonly.value) {
    formRef.value?.validateField('endTime')
  }
}

const submitForm = async () => {
  if (formReadonly.value) return
  await formRef.value?.validate()

  submitLoading.value = true
  try {
    const payload: ShiftConfigVO = {
      id: formData.id,
      shiftName: formData.shiftName?.trim(),
      stationId: formData.stationId,
      startTime: formData.startTime,
      endTime: formData.endTime,
      crossDay: Boolean(formData.crossDay),
      remark: formData.remark?.trim()
    }
    if (formMode.value === 'create') {
      delete payload.id
      await ShiftConfigApi.create(payload)
      message.success('新增成功')
    } else {
      await ShiftConfigApi.update(payload)
      message.success('更新成功')
    }
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.shift-config-page {
  .query-wrap {
    border: 1px solid rgba(148, 163, 184, 0.32);
    border-radius: 14px;
    background: #ffffff;
    box-shadow: 0 8px 28px rgba(15, 23, 42, 0.08);
  }

  .shift-no {
    display: inline-flex;
    align-items: center;
    height: 24px;
    padding: 0 10px;
    border-radius: 999px;
    border: 1px solid #e6e6ff;
    background: #f6f7ff;
    color: #4338ca;
    font-size: 12px;
    font-weight: 600;
  }

  .form-item-tip {
    margin-top: 6px;
    color: #d97706;
    font-size: 12px;
    line-height: 1.35;
  }
}
</style>
