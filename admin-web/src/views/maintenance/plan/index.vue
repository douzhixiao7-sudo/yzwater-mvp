<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="110px"
    >
      <el-form-item label="设备名称" prop="deviceName">
        <el-input
          v-model="queryParams.deviceName"
          clearable
          class="!w-220px"
          placeholder="请输入设备名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备类型" prop="deviceType">
        <el-input
          v-model="queryParams.deviceType"
          clearable
          class="!w-220px"
          placeholder="请输入设备类型"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属闸站" prop="stationId">
        <el-select
          v-model="queryParams.stationId"
          clearable
          class="!w-220px"
          placeholder="请选择所属闸站"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)"
            :key="String(dict.value)"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="养护状态" prop="status">
        <el-select v-model="queryParams.status" clearable class="!w-220px" placeholder="请选择状态">
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="养护类型" prop="maintainType">
        <el-select v-model="queryParams.maintainType" clearable class="!w-220px" placeholder="请选择养护类型">
          <el-option
            v-for="option in maintainTypeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计划养护日期" prop="planDate">
        <el-date-picker
          v-model="queryParams.planDate"
          type="daterange"
          value-format="YYYY-MM-DD"
          format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="!w-240px"
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
        <el-button
          v-hasPermi="['iot:maintenance-plan:create']"
          type="primary"
          plain
          @click="openCreate"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="设备名称" align="center" prop="deviceName" min-width="140" />
      <el-table-column label="设备类型" align="center" prop="deviceType" min-width="120">
        <template #default="scope">
          {{ getDictLabel(DICT_TYPE.IOT_DEVICE_TYPE, scope.row.deviceType) || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="所属闸站" align="center" prop="stationId" min-width="140">
        <template #default="scope">
          {{ getDictLabel(DICT_TYPE.IOT_ZD_SBZD, scope.row.stationId) || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="计划养护日期" align="center" prop="planDate" min-width="140" :formatter="dateFormatter2" />
      <el-table-column label="养护类型" align="center" prop="maintainType" min-width="140">
        <template #default="scope">
          {{ getMaintainTypeLabel(scope.row.maintainType) }}
        </template>
      </el-table-column>
      <el-table-column label="来源" align="center" prop="sourceType" width="120">
        <template #default="scope">
          {{ getSourceTypeLabel(scope.row.sourceType) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="120">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" min-width="160" :formatter="dateFormatter2" />
      <el-table-column label="操作" align="center" min-width="160">
        <template #default="scope">
          <el-button
            v-hasPermi="['iot:maintenance-plan:submit']"
            link
            type="primary"
            @click="openForm(scope.row)"
          >
            {{ isCompleted(scope.row) ? '详情' : '提交养护' }}
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

  <el-dialog
    v-model="formVisible"
    :title="formTitle"
    width="980px"
    destroy-on-close
    :close-on-click-modal="false"
    class="maintenance-plan-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      label-position="left"
      :disabled="isView"
    >
      <div class="form-section">
        <div class="section-title">设备信息</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="所属闸站" prop="stationId">
              <el-select
                v-if="isCreate"
                v-model="formData.stationId"
                clearable
                placeholder="请选择所属闸站"
                class="!w-full"
                @change="handleStationChange"
              >
                <el-option
                  v-for="dict in getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)"
                  :key="String(dict.value)"
                  :label="dict.label"
                  :value="String(dict.value)"
                />
              </el-select>
              <el-input
                v-else
                :model-value="getDictLabel(DICT_TYPE.IOT_ZD_SBZD, formData.stationId) || '-'"
                disabled
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类型">
              <el-input :model-value="getDeviceTypeLabel(formData.deviceType)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="deviceId">
              <el-select
                v-if="isCreate"
                v-model="formData.deviceId"
                filterable
                clearable
                :placeholder="formData.stationId ? '请选择设备' : '请先选择所属闸站'"
                class="!w-full"
                :disabled="!formData.stationId"
                @change="handleDeviceChange"
              >
                <el-option
                  v-for="item in deviceOptions"
                  :key="String(item.id)"
                  :label="item.label"
                  :value="item.id"
                />
              </el-select>
              <el-input v-else v-model="formData.deviceName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划养护日期" prop="planDate">
              <el-date-picker
                v-model="formData.planDate"
                type="date"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                class="!w-full"
                :disabled="!isCreate"
                placeholder="请选择计划日期"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源">
              <el-input :model-value="getSourceTypeLabel(formData.sourceType)" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div v-if="!isCreate" class="form-section">
        <div class="section-title">养护信息</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="养护类型" prop="maintainType">
              <el-select
                v-model="formData.maintainType"
                placeholder="请选择养护类型"
                class="!w-full"
                :disabled="isView"
              >
                <el-option
                  v-for="option in maintainTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="养护人" prop="maintainerName">
              <el-select
                v-model="formData.maintainerName"
                filterable
                clearable
                placeholder="请选择养护人"
                class="!w-full"
                :disabled="isView"
              >
                <el-option
                  v-for="user in userOptions"
                  :key="String(user.id)"
                  :label="user.nickname"
                  :value="user.nickname"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="完成日期">
              <el-date-picker
                v-model="formData.finishTime"
                type="date"
                value-format="x"
                format="YYYY-MM-DD"
                placeholder="请选择完成日期"
                class="!w-full"
                :disabled="isView"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="养护项目">
              <el-input
                v-model="formData.maintainItems"
                type="textarea"
                :rows="2"
                placeholder="请输入养护项目"
                :disabled="isView"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="配件消耗">
              <el-table :data="formData.spareUsages || []" border size="small">
                <el-table-column label="备件" min-width="260">
                  <template #default="scope">
                    <el-select
                      v-model="scope.row.spareId"
                      filterable
                      clearable
                      placeholder="请选择备件"
                      class="!w-full"
                      :disabled="isView"
                      @change="handleSpareChange(scope.row)"
                    >
                      <el-option
                        v-for="item in spareOptions"
                        :key="item.id"
                        :label="item.label"
                        :value="item.id"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="数量" width="160">
                  <template #default="scope">
                    <el-input-number
                      v-model="scope.row.qty"
                      :min="1"
                      class="!w-full"
                      :disabled="isView"
                      @change="handleSpareQtyChange(scope.row)"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="库存数量" width="120" align="center">
                  <template #default="scope">
                    <span>{{ formatStockQty(scope.row.stockQty) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="扣减后库存" width="140" align="center">
                  <template #default="scope">
                    <span :class="{ 'stock-danger': isStockNotEnough(scope.row) }">
                      {{ formatStockQty(getRemainingStock(scope.row)) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="90" align="center">
                  <template #default="scope">
                    <el-button link type="danger" :disabled="isView" @click="removeSpareUsage(scope.$index)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="items-action">
                <el-button type="primary" plain :disabled="isView" @click="addSpareUsage">
                  + 添加备件消耗
                </el-button>
              </div>
              <div class="items-tip">提示：提交后将自动出库并扣减库存。</div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注"
                :disabled="isView"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
      <el-button v-if="!isView" type="primary" :loading="formLoading" @click="submitForm">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { SpareApi } from '@/api/iot/spare'
import { DeviceApi } from '@/api/iot/device/device'
import { dateFormatter2 } from '@/utils/formatTime'
import { DICT_TYPE, getDictLabel, getStrDictOptions } from '@/utils/dict'
import * as UserApi from '@/api/system/user'
import {
  MaintenancePlanApi,
  type MaintenancePlanVO,
  type MaintenancePlanPageReqVO,
  type MaintenancePlanCreateReqVO,
  type MaintenancePlanSubmitReqVO,
  type MaintenancePlanSpareUsageVO
} from '@/api/iot/maintenance-plan'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'EquipmentMaintenancePlan' })

const message = useMessage()

const loading = ref(true)
const list = ref<MaintenancePlanVO[]>([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()

const statusOptions = [
  { label: '未完成', value: 'pending', tagType: 'warning' },
  { label: '已完成', value: 'completed', tagType: 'success' }
]

const maintainTypeFallbackOptions = [
  { label: '日常常规养护', value: 'routine' },
  { label: '周期定检养护', value: 'periodic' },
  { label: '专项场景养护', value: 'special' },
  { label: '故障联动养护', value: 'fault_linked' }
]

const sourceTypeOptions = [
  { label: '自动生成', value: 'auto' },
  { label: '手动新增', value: 'manual' }
]

const maintainTypeOptions = computed(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_MAINTENANCE_TYPE)
  return dictOptions.length ? dictOptions : maintainTypeFallbackOptions
})

const getMaintainTypeLabel = (value?: string) => {
  if (!value) return '-'
  return maintainTypeOptions.value.find((item) => item.value === value)?.label || value
}

const getStatusLabel = (value?: string) => {
  return statusOptions.find((item) => item.value === value)?.label || '-'
}

const getStatusTagType = (value?: string) => {
  return statusOptions.find((item) => item.value === value)?.tagType || 'info'
}

const isCompleted = (row: MaintenancePlanVO) => row.status === 'completed'
const getSourceTypeLabel = (value?: string) => {
  return sourceTypeOptions.find((item) => item.value === value)?.label || '-'
}
const getDeviceTypeLabel = (value?: string) => {
  return getDictLabel(DICT_TYPE.IOT_DEVICE_TYPE, value) || '-'
}

const queryParams = reactive<MaintenancePlanPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  deviceName: undefined,
  deviceType: undefined,
  stationId: undefined,
  status: undefined,
  maintainType: undefined,
  planDate: []
})

const sortByUpdateTimeDesc = (items: MaintenancePlanVO[]) => {
  return [...items].sort((a, b) => {
    const aTime = a.updateTime ? dayjs(a.updateTime).valueOf() : 0
    const bTime = b.updateTime ? dayjs(b.updateTime).valueOf() : 0
    return bTime - aTime
  })
}

const getList = async () => {
  loading.value = true
  try {
    const data = await MaintenancePlanApi.getMaintenancePlanPage(queryParams)
    list.value = sortByUpdateTimeDesc(data.list || [])
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const formVisible = ref(false)
const formTitle = ref('')
const formMode = ref<'create' | 'submit' | 'view'>('submit')
const isView = computed(() => formMode.value === 'view')
const isCreate = computed(() => formMode.value === 'create')
const formLoading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive<MaintenancePlanVO>({
  id: undefined,
  deviceId: undefined,
  deviceName: '',
  deviceType: '',
  stationId: undefined,
  planDate: '',
  maintainType: '',
  maintainItems: '',
  spareUsages: [],
  maintainerName: '',
  finishTime: undefined,
  status: 'pending',
  remark: '',
  sourceType: 'auto'
})

const requireWhenNotCreate = (message: string) => {
  return {
    validator: (_rule: any, value: any, callback: any) => {
      if (!isCreate.value && (!value || String(value).trim() === '')) {
        callback(new Error(message))
        return
      }
      callback()
    },
    trigger: 'blur'
  }
}

const requireWhenCreate = (message: string) => {
  return {
    validator: (_rule: any, value: any, callback: any) => {
      if (isCreate.value && (!value || String(value).trim() === '')) {
        callback(new Error(message))
        return
      }
      callback()
    },
    trigger: 'change'
  }
}

const formRules: FormRules = {
  stationId: [requireWhenCreate('请选择所属闸站')],
  deviceId: [requireWhenCreate('请选择设备')],
  planDate: [requireWhenCreate('请选择计划养护日期')],
  maintainType: [requireWhenNotCreate('请选择养护类型')],
  maintainerName: [requireWhenNotCreate('请选择养护人')],
  remark: [requireWhenNotCreate('请输入备注')]
}

const spareOptions = ref<{ id: number; label: string }[]>([])
const allDeviceOptions = ref<{ id: number | string; label: string; deviceType?: string; stationId?: string }[]>([])
const userOptions = ref<UserApi.UserVO[]>([])
const deviceOptions = computed(() => {
  if (!formData.stationId) return []
  return allDeviceOptions.value.filter(
    (item) => String(item.stationId) === String(formData.stationId)
  )
})
const loadSpareOptions = async () => {
  const data = await SpareApi.getSpareSimpleList()
  spareOptions.value = (data || []).map((item: any) => ({
    id: item.id,
    label: `${item.spareName || ''} ${item.spareSpec || ''} ${item.spareModel || ''}`.trim()
  }))
}

const loadUserOptions = async () => {
  userOptions.value = await UserApi.getSimpleUserList({ excludeRoleName: '游客' })
}

interface SpareStockInfo {
  stockQty: number
}

const spareStockMap = ref<Record<string, SpareStockInfo>>({})

const loadSpareStockInfo = async (spareId?: number) => {
  if (!spareId && spareId !== 0) {
    return undefined
  }
  const key = String(spareId)
  if (Object.prototype.hasOwnProperty.call(spareStockMap.value, key)) {
    return spareStockMap.value[key]
  }
  const data = await SpareApi.getSpare(spareId)
  const info: SpareStockInfo = {
    stockQty: data?.stockQty ?? 0
  }
  spareStockMap.value[key] = info
  return info
}

const invalidateSpareStockCache = (spareId?: number | null) => {
  if (spareId === undefined || spareId === null) {
    return
  }
  const key = String(spareId)
  if (Object.prototype.hasOwnProperty.call(spareStockMap.value, key)) {
    const { [key]: _omit, ...rest } = spareStockMap.value
    spareStockMap.value = rest
  }
}

const formatStockQty = (stockQty?: number) => {
  if (stockQty === undefined || stockQty === null) {
    return '-'
  }
  return stockQty
}

const getRemainingStock = (row: MaintenancePlanSpareUsageItem) => {
  if (row?.stockQty === undefined || row?.stockQty === null) {
    return undefined
  }
  if (row?.qty === undefined || row?.qty === null) {
    return row.stockQty
  }
  return row.stockQty - row.qty
}

const isStockNotEnough = (row: MaintenancePlanSpareUsageItem) => {
  if (row?.stockQty === undefined || row?.stockQty === null || row?.qty === undefined || row?.qty === null) {
    return false
  }
  return row.qty > row.stockQty
}

const handleSpareChange = async (row: MaintenancePlanSpareUsageItem) => {
  if (!row?.spareId) {
    row.stockQty = undefined
    return
  }
  const info = await loadSpareStockInfo(row.spareId)
  row.stockQty = info?.stockQty
  if (row.qty && row.stockQty !== undefined && row.qty > row.stockQty) {
    message.warning(`备件库存不足，当前库存：${row.stockQty}`)
  }
}

const handleSpareQtyChange = (row: MaintenancePlanSpareUsageItem) => {
  if (row?.spareId && row?.stockQty !== undefined && row?.qty && row.qty > row.stockQty) {
    message.warning(`备件库存不足，当前库存：${row.stockQty}`)
  }
}

interface MaintenancePlanSpareUsageItem extends MaintenancePlanSpareUsageVO {
  stockQty?: number
}

const loadDeviceOptions = async () => {
  const data = await DeviceApi.getSimpleDeviceList()
  allDeviceOptions.value = (data || []).map((item: any) => ({
    id: item.id,
    label: item.nickname || item.deviceName || item.serialNumber || String(item.id),
    deviceType: item.deviceType !== undefined && item.deviceType !== null ? String(item.deviceType) : '',
    stationId:
      item.stationId === undefined || item.stationId === null ? undefined : String(item.stationId)
  }))
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    deviceId: undefined,
    deviceName: '',
    deviceType: '',
    stationId: undefined,
    planDate: '',
    maintainType: '',
    maintainItems: '',
    spareUsages: [],
    maintainerName: '',
    finishTime: undefined,
    status: 'pending',
    remark: '',
    sourceType: 'auto'
  })
  formRef.value?.clearValidate()
}

const openCreate = () => {
  formMode.value = 'create'
  formTitle.value = '新增养护计划'
  formVisible.value = true
  resetForm()
  formData.sourceType = 'manual'
}

const openForm = async (row: MaintenancePlanVO) => {
  formMode.value = isCompleted(row) ? 'view' : 'submit'
  formTitle.value = isCompleted(row) ? '养护详情' : '提交养护'
  formVisible.value = true
  resetForm()
  if (!row.id) {
    return
  }
  const data = await MaintenancePlanApi.getMaintenancePlan(row.id)
  Object.assign(formData, {
    id: data.id,
    deviceId: data.deviceId,
    deviceName: data.deviceName || '',
    deviceType: data.deviceType || '',
    stationId: data.stationId,
    planDate: data.planDate || '',
    maintainType: data.maintainType || '',
    maintainItems: data.maintainItems || '',
    spareUsages: (data.spareUsages || []).map((item: MaintenancePlanSpareUsageVO) => ({
      spareId: item.spareId,
      qty: item.qty,
      stockQty: undefined
    })) as MaintenancePlanSpareUsageItem[],
    maintainerName: data.maintainerName || '',
    finishTime: data.finishTime ?? undefined,
    status: data.status || 'pending',
    remark: data.remark || '',
    sourceType: data.sourceType || 'auto'
  })
  if (formData.spareUsages && formData.spareUsages.length) {
    for (const item of formData.spareUsages as MaintenancePlanSpareUsageItem[]) {
      if (item?.spareId) {
        const info = await loadSpareStockInfo(item.spareId)
        item.stockQty = info?.stockQty
      }
    }
  }
}

const handleDeviceChange = async (value: number | string) => {
  const selected = allDeviceOptions.value.find((item) => String(item.id) === String(value))
  if (!selected) {
    formData.deviceName = ''
    formData.deviceType = ''
    return
  }
  formData.deviceName = selected.label
  formData.deviceType = selected.deviceType || ''
  if (!formData.deviceType) {
    const detail = await DeviceApi.getDevice(value)
    if (detail?.deviceType !== undefined && detail?.deviceType !== null) {
      formData.deviceType = String(detail.deviceType)
    }
  }
  if (selected.stationId !== undefined) {
    formData.stationId = String(selected.stationId)
  }
}

const handleStationChange = (value?: string) => {
  formData.stationId = value
  formData.deviceId = undefined
  formData.deviceName = ''
  formData.deviceType = ''
}

const addSpareUsage = () => {
  formData.spareUsages = formData.spareUsages || []
  formData.spareUsages.push({ spareId: undefined, qty: 1, stockQty: undefined })
}

const removeSpareUsage = (index: number) => {
  if (!formData.spareUsages) {
    return
  }
  formData.spareUsages.splice(index, 1)
}

const normalizeSpareUsages = () => {
  return (formData.spareUsages || [])
    .filter((item) => item.spareId && item.qty)
    .map((item) => ({ spareId: item.spareId, qty: item.qty }))
}

const validateSpareUsages = async () => {
  if (!formData.spareUsages || !formData.spareUsages.length) {
    return true
  }
  for (let i = 0; i < formData.spareUsages.length; i++) {
    const item = formData.spareUsages[i] as MaintenancePlanSpareUsageItem
    if (!item.spareId) {
      message.error(`第 ${i + 1} 行请选择备件`)
      return false
    }
    if (!item.qty || item.qty < 1) {
      message.error(`第 ${i + 1} 行请填写数量`)
      return false
    }
    if (item.stockQty === undefined) {
      const info = await loadSpareStockInfo(item.spareId)
      item.stockQty = info?.stockQty
    }
    if (item.stockQty !== undefined && item.qty > item.stockQty) {
      message.error(`第 ${i + 1} 行备件库存不足（当前库存：${item.stockQty}）`)
      return false
    }
  }
  return true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }
  if (!(await validateSpareUsages())) {
    return
  }
  formLoading.value = true
  try {
    if (isCreate.value) {
      const payload: MaintenancePlanCreateReqVO = {
        deviceId: formData.deviceId as number,
        planDate: formData.planDate || '',
        maintainType: formData.maintainType || undefined,
        maintainItems: formData.maintainItems || undefined,
        remark: formData.remark || undefined
      }
      await MaintenancePlanApi.createMaintenancePlan(payload)
      message.success('新增成功')
    } else {
      const payload: MaintenancePlanSubmitReqVO = {
        id: formData.id as number,
        maintainType: formData.maintainType || '',
        maintainItems: formData.maintainItems || '',
        spareUsages: normalizeSpareUsages(),
        maintainerName: formData.maintainerName || '',
        finishTime: formData.finishTime,
        remark: formData.remark || ''
      }
      await MaintenancePlanApi.submitMaintenancePlan(payload)
      message.success('提交成功')
      ;(payload.spareUsages || []).forEach((item) => invalidateSpareStockCache(item.spareId))
    }
    formVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadSpareOptions(), loadDeviceOptions(), loadUserOptions()])
  await getList()
})
</script>

<style scoped lang="scss">
.form-section {
  margin-bottom: 16px;
  padding: 12px 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
}

.section-title {
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
}

.items-action {
  margin-top: 10px;
}

.items-tip {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  text-align: center;
  font-size: 12px;
}
</style>
