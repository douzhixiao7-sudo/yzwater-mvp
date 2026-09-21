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
      <el-form-item label="备件名称" prop="spareName">
        <el-input
          v-model="queryParams.spareName"
          clearable
          class="!w-220px"
          placeholder="请输入备件名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="备件分类" prop="spareType">
        <el-select
          v-model="queryParams.spareType"
          clearable
          class="!w-220px"
          placeholder="请选择备件分类"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.IOT_SPARE_TYPE)"
            :key="String(dict.value)"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="仅看预警">
        <el-switch v-model="warningOnly" />
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
          v-hasPermi="['iot:spare:create']"
          type="primary"
          plain
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
        <el-button
          v-hasPermi="['iot:spare-io:create']"
          type="warning"
          plain
          @click="openSpareIoCreate"
        >
          <Icon icon="ep:document" class="mr-5px" />
          出入库
        </el-button>
        <el-button
          v-hasPermi="['iot:spare-check:create']"
          type="info"
          plain
          @click="openSpareCheckCreate"
        >
          <Icon icon="ep:circle-check" class="mr-5px" />
          库存盘点
        </el-button>
        <el-button
          v-hasPermi="['iot:spare:export']"
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
      <el-table-column label="备件名称" align="center" prop="spareName" min-width="140" />
      <el-table-column label="关联设备" align="center" prop="deviceId" min-width="120">
        <template #default="scope">
          {{ resolveDeviceName(scope.row.deviceId) }}
        </template>
      </el-table-column>
      <el-table-column label="备件规格" align="center" prop="spareSpec" min-width="120" />
      <el-table-column label="备件型号" align="center" prop="spareModel" min-width="120" />
      <el-table-column label="备件分类" align="center" prop="spareType" min-width="120">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_SPARE_TYPE" :value="scope.row.spareType" />
        </template>
      </el-table-column>
      <el-table-column label="库存数量" align="center" prop="stockQty" width="100" />
      <el-table-column label="最低库存阈值" align="center" prop="minStock" width="120" />
      <el-table-column label="库管员" align="center" prop="keeperName" min-width="120" />
      <el-table-column label="处理状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.warning" type="danger">预警</el-tag>
          <el-tag v-else type="success">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="180px">
        <template #default="scope">
          <el-button
            v-hasPermi="['iot:spare:query']"
            link
            type="primary"
            @click="openForm('view', scope.row.id)"
          >
            详情
          </el-button>
          <el-button
            v-hasPermi="['iot:spare:update']"
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
          >
            编辑
          </el-button>
          <el-button
            v-hasPermi="['iot:spare:delete']"
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
      :disabled="formType === 'view'"
    >
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="备件名称" prop="spareName">
            <el-input v-model="formData.spareName" placeholder="请输入备件名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="备件规格" prop="spareSpec">
            <el-input v-model="formData.spareSpec" placeholder="请输入备件规格" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="备件型号" prop="spareModel">
            <el-input v-model="formData.spareModel" placeholder="请输入备件型号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="备件分类" prop="spareType">
            <el-select v-model="formData.spareType" clearable placeholder="请选择备件分类">
              <el-option
                v-for="dict in getStrDictOptions(DICT_TYPE.IOT_SPARE_TYPE)"
                :key="String(dict.value)"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属站点" prop="stationId">
            <el-select
              v-model="formData.stationId"
              clearable
              placeholder="请选择所属站点"
              @change="handleStationChange"
            >
              <el-option
                v-for="dict in getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)"
                :key="String(dict.value)"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="关联设备" prop="deviceId">
            <el-select
              v-model="formData.deviceId"
              clearable
              placeholder="请选择关联设备"
              :disabled="!formData.stationId"
            >
              <el-option
                v-for="device in deviceOptions"
                :key="String(device.id)"
                :label="device.deviceName || device.nickname || device.serialNumber"
                :value="String(device.id)"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生产厂家" prop="manufacturer">
            <el-input v-model="formData.manufacturer" placeholder="请输入生产厂家" />
          </el-form-item>
        </el-col>
        <el-col v-if="formType !== 'create'" :span="12">
          <el-form-item label="库存数量" prop="stockQty">
            <el-input-number v-model="formData.stockQty" :min="0" :controls="true" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最低库存阈值" prop="minStock">
            <el-input-number v-model="formData.minStock" :min="0" :controls="true" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="存放位置" prop="storageLocation">
            <el-tree-select
              v-model="formData.storageLocation"
              :data="locationTreeOptions"
              :props="defaultProps"
              check-strictly
              default-expand-all
              clearable
              placeholder="请选择存放位置"
              value-key="id"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="库管员" prop="keeperName">
            <el-input v-model="formData.keeperName" placeholder="请输入库管员" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备件图片" prop="spareImages">
            <UploadImgs v-model="formData.spareImages" :limit="6" :drag="false" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="2"
              placeholder="请输入备注"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">{{ formType === 'view' ? '关闭' : '取消' }}</el-button>
      <el-button
        v-if="formType !== 'view'"
        type="primary"
        :loading="formLoading"
        @click="submitForm"
      >
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import download from '@/utils/download'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { SpareApi, type SparePageReqVO, type SpareVO } from '@/api/iot/spare'
import { DeviceApi, DeviceVO } from '@/api/iot/device/device'
import { DeviceLocationApi, DeviceLocationNodeRespVO } from '@/api/iot/device/location'
import { UploadImgs } from '@/components/UploadFile'
import { defaultProps } from '@/utils/tree'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'IotSpareLedger' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<SpareVO[]>([])
const total = ref(0)
const exportLoading = ref(false)
const queryFormRef = ref<FormInstance>()

const queryParams = reactive<SparePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  spareName: undefined,
  spareType: undefined,
  warning: undefined
})

const deviceOptions = ref<DeviceVO[]>([])
const deviceNameMap = ref<Record<string, string>>({})
const locationTree = ref<DeviceLocationNodeRespVO[]>([])
const locationTreeOptions = computed(() => locationTree.value || [])

const warningOnly = ref(false)
watch(warningOnly, (val) => {
  queryParams.warning = val ? true : undefined
})

const buildDeviceNameMap = (devices: DeviceVO[]) => {
  const map: Record<string, string> = {}
  devices?.forEach((device) => {
    if (device?.id === undefined || device?.id === null) return
    map[String(device.id)] = device.deviceName || device.nickname || device.serialNumber || ''
  })
  return map
}

const resolveDeviceName = (value?: string | number) => {
  if (value === undefined || value === null || value === '') return '-'
  return deviceNameMap.value[String(value)] || '-'
}

const loadDeviceNameMap = async () => {
  const data = await DeviceApi.getDeviceList()
  deviceNameMap.value = buildDeviceNameMap(data || [])
}

const loadDeviceOptions = async (stationId?: string) => {
  if (!stationId) {
    deviceOptions.value = []
    return
  }
  const data = await DeviceApi.getDeviceList({ stationId })
  deviceOptions.value = data || []
}

const handleStationChange = async (stationId?: string) => {
  formData.deviceId = undefined
  await loadDeviceOptions(stationId)
}

const loadLocationTree = async () => {
  locationTree.value = await DeviceLocationApi.getDeviceLocationTree()
}

const getList = async () => {
  loading.value = true
  try {
    const data = await SpareApi.getSparePage(queryParams)
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
  warningOnly.value = false
  queryParams.warning = undefined
  handleQuery()
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await SpareApi.exportSpareExcel(queryParams)
    download.excel(data, '备件台账.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await SpareApi.deleteSpare(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const { push } = useRouter()
const openSpareIoCreate = () => {
  push({ path: '/spare/io', query: { action: 'create' } })
}

const openSpareCheckCreate = () => {
  push({ path: '/spare/check', query: { action: 'create' } })
}

const formVisible = ref(false)
const formTitle = ref('')
const formType = ref<'create' | 'update' | 'view'>('create')
const formLoading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive<SpareVO>({
  id: undefined,
  spareName: '',
  spareSpec: '',
  spareModel: '',
  spareType: undefined,
  stationId: undefined,
  deviceId: undefined,
  manufacturer: undefined,
  stockQty: 0,
  minStock: 0,
  storageLocation: '',
  keeperName: '',
  spareImages: [],
  remark: ''
})

const formRules: FormRules = {
  spareName: [{ required: true, message: '备件名称不能为空', trigger: 'blur' }],
  spareSpec: [{ required: true, message: '备件规格不能为空', trigger: 'blur' }],
  spareModel: [{ required: true, message: '备件型号不能为空', trigger: 'blur' }],
  stockQty: [{ required: true, message: '库存数量不能为空', trigger: 'blur' }],
  minStock: [{ required: true, message: '最低库存阈值不能为空', trigger: 'blur' }],
  keeperName: [{ required: true, message: '库管员不能为空', trigger: 'blur' }],
  spareImages: [{ required: true, message: '备件图片不能为空', trigger: 'change' }]
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    spareName: '',
    spareSpec: '',
    spareModel: '',
    spareType: undefined,
    stationId: undefined,
    deviceId: undefined,
    manufacturer: undefined,
    stockQty: 0,
    minStock: 0,
    storageLocation: '',
    keeperName: '',
    spareImages: [],
    remark: ''
  })
  formRef.value?.clearValidate()
}

const openForm = async (type: 'create' | 'update' | 'view', id?: number) => {
  formType.value = type
  formTitle.value =
    type === 'create' ? '新增备件台账' : type === 'update' ? '编辑备件台账' : '备件台账详情'
  formVisible.value = true
  resetForm()
  await loadLocationTree()
  deviceOptions.value = []
  if ((type === 'update' || type === 'view') && id) {
    const data = await SpareApi.getSpare(id)
    Object.assign(formData, data)
    formData.storageLocation = formData.storageLocation ? String(formData.storageLocation) : ''
    formData.deviceId = formData.deviceId ? String(formData.deviceId) : undefined
    if (formData.stationId) {
      await loadDeviceOptions(formData.stationId)
    }
  }
}

const submitForm = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }
  formLoading.value = true
  try {
    const payload: SpareVO = { ...formData }
    payload.storageLocation = payload.storageLocation ? String(payload.storageLocation) : undefined
    payload.deviceId = payload.deviceId || undefined
    payload.stationId = payload.stationId || undefined
    if (formType.value === 'create') {
      await SpareApi.createSpare(payload)
      message.success(t('common.createSuccess'))
    } else {
      await SpareApi.updateSpare(payload)
      message.success(t('common.updateSuccess'))
    }
    formVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

onMounted(async () => {
  await loadLocationTree()
  await loadDeviceNameMap()
  getList()
})
</script>

<style scoped>
.spare-ledger-dialog :deep(.el-dialog__body) {
  padding-top: 8px;
}

.form-section {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px 16px 4px;
  margin-bottom: 12px;
  background-color: #fff;
}

.section-title {
  position: relative;
  padding-left: 10px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #303133;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  width: 3px;
  height: 14px;
  border-radius: 2px;
  background-color: #409eff;
}

</style>
