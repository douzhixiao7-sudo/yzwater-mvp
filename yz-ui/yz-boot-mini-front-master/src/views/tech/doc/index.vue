<template>
  <ContentWrap>
    <!-- 查询区域 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="110px"
    >
      <el-form-item label="设备编号" prop="deviceCode">
        <el-input
          v-model="queryParams.deviceCode"
          clearable
          class="!w-220px"
          placeholder="请输入设备编号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
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
        <el-select
          v-model="queryParams.deviceType"
          clearable
          class="!w-220px"
          placeholder="请选择设备类型"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.IOT_DEVICE_TYPE)"
            :key="String(dict.value)"
            :label="dict.label"
            :value="String(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="资料类型" prop="docType">
        <el-select
          v-model="queryParams.docType"
          clearable
          class="!w-220px"
          placeholder="请选择资料类型"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.IOT_DEVICE_DOC_TYPE)"
            :key="String(dict.value)"
            :label="dict.label"
            :value="String(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="资料名称" prop="docName">
        <el-input
          v-model="queryParams.docName"
          clearable
          class="!w-220px"
          placeholder="请输入资料名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="上传日期" prop="createTime">
        <el-date-picker
          v-model="createDateRange"
          type="daterange"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
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
          v-hasPermi="['iot:device-doc:create']"
          type="primary"
          plain
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
        <el-button
          v-hasPermi="['iot:device-doc:export']"
          type="success"
          plain
          :loading="exportLoading"
          @click="handleExport"
        >
          <Icon icon="ep:download" class="mr-5px" />
          导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="设备编号" align="center" prop="deviceCode" min-width="140" />
      <el-table-column label="设备名称" align="center" prop="deviceName" min-width="140" />
      <el-table-column label="设备型号" align="center" prop="equipmentModel" min-width="120" />
      <el-table-column label="设备类型" align="center" prop="deviceType" min-width="120">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_DEVICE_TYPE" :value="scope.row.deviceType" />
        </template>
      </el-table-column>
      <el-table-column label="资料类型" align="center" prop="docType" min-width="120">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_DEVICE_DOC_TYPE" :value="scope.row.docType" />
        </template>
      </el-table-column>
      <el-table-column label="资料名称" align="center" prop="docName" min-width="160" />
      <el-table-column label="资料格式" align="center" prop="fileFormat" width="100" />
      <el-table-column label="上传人" align="center" prop="creator" min-width="120" />
      <el-table-column
        label="上传时间"
        align="center"
        prop="createTime"
        min-width="120"
        :formatter="dateFormatter"
      />
      <el-table-column label="资料文件" align="center" min-width="140">
        <template #default="scope">
          <el-link
            v-if="scope.row.fileUrl"
            :href="scope.row.fileUrl"
            target="_blank"
            download
            type="primary"
          >
            下载
          </el-link>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="160">
        <template #default="scope">
          <el-button
            v-hasPermi="['iot:device-doc:query']"
            link
            type="primary"
            @click="openForm('view', scope.row.id)"
          >
            详情
          </el-button>
          <el-button
            v-hasPermi="['iot:device-doc:update']"
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
          >
            编辑
          </el-button>
          <el-button
            v-hasPermi="['iot:device-doc:delete']"
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
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

  <el-dialog
    v-model="formVisible"
    :title="formTitle"
    width="900px"
    destroy-on-close
    :close-on-click-modal="false"
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
        <div class="section-title">关联设备</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="所属站点" prop="stationId">
              <el-select
                v-model="formData.stationId"
                clearable
                placeholder="请选择所属站点"
                class="!w-full"
                :disabled="isView"
                @change="handleStationChange"
              >
                <el-option
                  v-for="dict in getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)"
                  :key="String(dict.value)"
                  :label="dict.label"
                  :value="String(dict.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联设备" prop="deviceId">
              <el-select
                v-model="formData.deviceId"
                filterable
                clearable
                :placeholder="formData.stationId ? '请选择设备' : '请先选择所属站点'"
                class="!w-full"
                :disabled="isView || !formData.stationId"
                @change="handleDeviceChange"
              >
                <el-option
                  v-for="item in deviceOptions"
                  :key="String(item.id)"
                  :label="item.label"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备编号">
              <el-input v-model="formData.deviceCode" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备型号">
              <el-input v-model="formData.equipmentModel" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类型">
              <el-input :model-value="getDeviceTypeLabel(formData.deviceType)" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="form-section">
        <div class="section-title">资料信息</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="资料类型" prop="docType">
              <el-select
                v-model="formData.docType"
                clearable
                placeholder="请选择资料类型"
                class="!w-full"
                :disabled="isView"
              >
                <el-option
                  v-for="dict in getStrDictOptions(DICT_TYPE.IOT_DEVICE_DOC_TYPE)"
                  :key="String(dict.value)"
                  :label="dict.label"
                  :value="String(dict.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资料名称" prop="docName">
              <el-input v-model="formData.docName" placeholder="请输入资料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="资料文件" prop="fileUrl">
              <UploadFile
                v-model="formData.fileUrl"
                :limit="1"
                :file-type="fileTypes"
                :disabled="isView"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资料格式">
              <el-input v-model="formData.fileFormat" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上传人">
              <el-input v-model="formData.creator" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上传时间" prop="createTime">
              <el-date-picker
                v-model="formData.createTime"
                type="datetime"
                value-format="x"
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择上传时间"
                class="!w-full"
                :disabled="isView"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="资料备注" prop="remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="2"
                placeholder="请输入资料备注"
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
import download from '@/utils/download'
import { DICT_TYPE, getDictLabel, getStrDictOptions } from '@/utils/dict'
import { dateFormatter, getDateRange } from '@/utils/formatTime'
import { DeviceApi } from '@/api/iot/device/device'
import {
  DeviceDocApi,
  type DeviceDocPageReqVO,
  type DeviceDocSaveReqVO,
  type DeviceDocVO
} from '@/api/iot/device-doc'
import { UploadFile } from '@/components/UploadFile'
import { useUserStore } from '@/store/modules/user'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'IotDeviceDoc' })

const message = useMessage()
const { t } = useI18n()
const userStore = useUserStore()

const loading = ref(true)
const list = ref<DeviceDocVO[]>([])
const total = ref(0)
const exportLoading = ref(false)
const queryFormRef = ref<FormInstance>()

const queryParams = reactive<DeviceDocPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  deviceCode: undefined,
  deviceName: undefined,
  deviceType: undefined,
  docType: undefined,
  docName: undefined
})

const createDateRange = ref<string[]>([])

const fileTypes = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'pdf', 'zip', 'rar']

const deviceOptions = ref<
  { id: number | string; label: string; deviceCode?: string; deviceName?: string; equipmentModel?: string; deviceType?: string }[]
>([])

const loadDeviceOptions = async (stationId?: string) => {
  if (!stationId) {
    deviceOptions.value = []
    return
  }
  const data = await DeviceApi.getDeviceList({ stationId })
  deviceOptions.value = (data || []).map((item: any) => {
    const name = item.nickname || item.deviceName || item.serialNumber || String(item.id)
    const code = item.serialNumber || ''
    const label = code ? `${name}（${code}）` : name
    return {
      id: item.id,
      label,
      deviceCode: item.serialNumber,
      deviceName: item.nickname || item.deviceName,
      equipmentModel: item.equipmentModel,
      deviceType: item.deviceType !== undefined && item.deviceType !== null ? String(item.deviceType) : ''
    }
  })
}

const buildQueryParams = () => {
  const params: DeviceDocPageReqVO = { ...queryParams }
  if (createDateRange.value && createDateRange.value.length === 2) {
    params.createTime = getDateRange(createDateRange.value[0], createDateRange.value[1])
  } else {
    params.createTime = []
  }
  return params
}

const getList = async () => {
  loading.value = true
  try {
    const data = await DeviceDocApi.getDeviceDocPage(buildQueryParams())
    list.value = data.list || []
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
  createDateRange.value = []
  handleQuery()
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await DeviceDocApi.exportDeviceDocExcel(buildQueryParams())
    download.excel(data, '技术资料库.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await DeviceDocApi.deleteDeviceDoc(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const formVisible = ref(false)
const formTitle = ref('')
const formMode = ref<'create' | 'update' | 'view'>('create')
const isView = computed(() => formMode.value === 'view')
const formLoading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive<DeviceDocVO & { stationId?: string }>({
  id: undefined,
  stationId: undefined,
  deviceId: undefined,
  deviceCode: '',
  deviceName: '',
  equipmentModel: '',
  deviceType: '',
  docType: '',
  docName: '',
  fileUrl: '',
  fileFormat: '',
  remark: '',
  creator: '',
  createTime: ''
})

const formRules: FormRules = {
  stationId: [{ required: true, message: '请选择所属站点', trigger: 'change' }],
  deviceId: [{ required: true, message: '请选择关联设备', trigger: 'change' }],
  docType: [{ required: true, message: '请选择资料类型', trigger: 'change' }],
  docName: [{ required: true, message: '资料名称不能为空', trigger: 'blur' }],
  fileUrl: [{ required: true, message: '请上传资料文件', trigger: 'change' }],
  createTime: [{ required: true, message: '请选择上传时间', trigger: 'change' }]
}

const getDeviceTypeLabel = (value?: string) => {
  return getDictLabel(DICT_TYPE.IOT_DEVICE_TYPE, value) || '-'
}

const resolveFileFormat = (fileUrl?: string) => {
  if (!fileUrl) return ''
  const pure = fileUrl.split('?')[0]
  const index = pure.lastIndexOf('.')
  if (index < 0 || index === pure.length - 1) {
    return ''
  }
  return pure.substring(index + 1).toLowerCase()
}

const handleStationChange = async (value?: string) => {
  formData.stationId = value
  formData.deviceId = undefined
  formData.deviceCode = ''
  formData.deviceName = ''
  formData.equipmentModel = ''
  formData.deviceType = ''
  await loadDeviceOptions(value)
}

const handleDeviceChange = async (value: number | string) => {
  const selected = deviceOptions.value.find((item) => String(item.id) === String(value))
  if (!selected) {
    formData.deviceCode = ''
    formData.deviceName = ''
    formData.equipmentModel = ''
    formData.deviceType = ''
    return
  }
  formData.deviceCode = selected.deviceCode || ''
  formData.deviceName = selected.deviceName || ''
  formData.equipmentModel = selected.equipmentModel || ''
  formData.deviceType = selected.deviceType || ''
}

const syncStationAndDeviceOptions = async (deviceId?: number | string) => {
  if (!deviceId) {
    formData.stationId = undefined
    deviceOptions.value = []
    return
  }
  const detail = await DeviceApi.getDevice(deviceId)
  formData.stationId = detail?.stationId ? String(detail.stationId) : undefined
  await loadDeviceOptions(formData.stationId)
  if (detail) {
    formData.deviceCode = detail.serialNumber || formData.deviceCode || ''
    formData.deviceName = detail.nickname || detail.deviceName || formData.deviceName || ''
    formData.equipmentModel = detail.equipmentModel || formData.equipmentModel || ''
    formData.deviceType =
      detail.deviceType !== undefined && detail.deviceType !== null
        ? String(detail.deviceType)
        : formData.deviceType || ''
  }
  if (formData.deviceId) {
    await handleDeviceChange(formData.deviceId)
  }
}

watch(
  () => formData.fileUrl,
  (val) => {
    if (!val) {
      formData.fileFormat = ''
      return
    }
    formData.fileFormat = resolveFileFormat(val)
  }
)

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    stationId: undefined,
    deviceId: undefined,
    deviceCode: '',
    deviceName: '',
    equipmentModel: '',
    deviceType: '',
    docType: '',
    docName: '',
    fileUrl: '',
    fileFormat: '',
    remark: '',
    creator: userStore.getUser.nickname || '',
    createTime: dayjs().valueOf()
  })
  deviceOptions.value = []
  formRef.value?.clearValidate()
}

const openForm = async (type: 'create' | 'update' | 'view', id?: number) => {
  formMode.value = type
  formTitle.value =
    type === 'create' ? '新增技术资料' : type === 'update' ? '编辑技术资料' : '技术资料详情'
  formVisible.value = true
  resetForm()
  if ((type === 'update' || type === 'view') && id) {
    const data = await DeviceDocApi.getDeviceDoc(id)
    Object.assign(formData, data)
    formData.deviceId = formData.deviceId || undefined
    if (!formData.fileFormat) {
      formData.fileFormat = resolveFileFormat(formData.fileUrl)
    }
    formData.creator = formData.creator || userStore.getUser.nickname || ''
    if (formData.createTime === undefined || formData.createTime === null || formData.createTime === '') {
      formData.createTime = dayjs().valueOf()
    }
    await syncStationAndDeviceOptions(formData.deviceId)
  }
}

const submitForm = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }
  formLoading.value = true
  try {
    const payload: DeviceDocSaveReqVO = {
      id: formData.id,
      deviceId: formData.deviceId as number,
      docType: formData.docType || '',
      docName: formData.docName || '',
      fileUrl: formData.fileUrl || '',
      fileFormat: formData.fileFormat || undefined,
      remark: formData.remark || undefined,
      createTime: formData.createTime || undefined
    }
    if (formMode.value === 'create') {
      await DeviceDocApi.createDeviceDoc(payload)
      message.success(t('common.createSuccess'))
    } else {
      await DeviceDocApi.updateDeviceDoc(payload)
      message.success(t('common.updateSuccess'))
    }
    formVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

onMounted(async () => {
  await getList()
})
</script>

<style scoped>
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
</style>
