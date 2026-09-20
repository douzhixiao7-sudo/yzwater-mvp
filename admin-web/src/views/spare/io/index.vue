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
      <el-form-item label="备件" prop="spareId">
        <el-select
          v-model="queryParams.spareId"
          filterable
          clearable
          class="!w-240px"
          placeholder="请选择备件"
        >
          <el-option
            v-for="item in spareOptions"
            :key="item.id"
            :label="item.label"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="出入库类型" prop="ioType">
        <el-select
          v-model="queryParams.ioType"
          clearable
          class="!w-220px"
          placeholder="请选择出入库类型"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.IOT_SPARE_IO_TYPE)"
            :key="String(dict.value)"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="审批状态" prop="auditStatus">
        <el-select
          v-model="queryParams.auditStatus"
          clearable
          class="!w-220px"
          placeholder="请选择审批状态"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.IOT_SPARE_IO_AUDIT_STATUS)"
            :key="String(dict.value)"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="出库用途" prop="usageType">
        <el-select
          v-model="queryParams.usageType"
          clearable
          class="!w-220px"
          placeholder="请选择出库用途"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.IOT_SPARE_USAGE_TYPE)"
            :key="String(dict.value)"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="出入库时间" prop="ioTime">
        <el-date-picker
          v-model="queryParams.ioTime"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
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
          v-hasPermi="['iot:spare-io:create']"
          type="primary"
          plain
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
        <el-button
          v-hasPermi="['iot:spare-io:export']"
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
      <el-table-column label="备件名称" align="center" prop="spareName" min-width="120" />
      <el-table-column label="备件规格" align="center" prop="spareSpec" min-width="110" />
      <el-table-column label="备件型号" align="center" prop="spareModel" min-width="110" />
      <el-table-column label="库存量" align="center" prop="stockQty" width="100">
        <template #default="scope">
          <span>{{ formatStockQty(scope.row.stockQty) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="出入库类型" align="center" prop="ioType" width="100">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_SPARE_IO_TYPE" :value="scope.row.ioType" />
        </template>
      </el-table-column>
      <el-table-column label="出入库时间" align="center" prop="ioTime" min-width="160">
        <template #default="scope">
          <span>{{ normalizeIoTime(scope.row.ioTime) || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="出入库数量" align="center" prop="ioQty" width="100">
        <template #default="scope">
          <span>{{ formatIoQty(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="出库用途" align="center" prop="usageType" width="120">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_SPARE_USAGE_TYPE" :value="scope.row.usageType" />
        </template>
      </el-table-column>
      <el-table-column label="审批状态" align="center" prop="auditStatus" width="110">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_SPARE_IO_AUDIT_STATUS" :value="scope.row.auditStatus" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="160">
        <template #default="scope">
          <el-button
            v-hasPermi="['iot:spare-io:query']"
            link
            type="primary"
            @click="openForm('detail', scope.row.id)"
          >
            详情
          </el-button>
          <el-button
            v-hasPermi="['iot:spare-io:update']"
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
          >
            编辑
          </el-button>
          <el-button
            v-hasPermi="['iot:spare-io:delete']"
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

  <!-- 新增/编辑 -->
  <el-dialog
    v-model="formVisible"
    :title="formTitle"
    width="820px"
    destroy-on-close
    :close-on-click-modal="false"
    class="spare-io-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      label-position="left"
    >
      <div class="form-section">
        <div class="section-title">出入库信息</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="出入库类型" prop="ioType">
              <el-select
                v-model="formData.ioType"
                placeholder="请选择出入库类型"
                class="!w-full"
                :disabled="isDetail"
              >
                <el-option
                  v-for="dict in getStrDictOptions(DICT_TYPE.IOT_SPARE_IO_TYPE)"
                  :key="String(dict.value)"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出入库时间" prop="ioTime">
              <el-date-picker
                v-model="formData.ioTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择时间"
                class="!w-full"
                :disabled="isDetail"
              />
            </el-form-item>
          </el-col>
          <el-col v-if="formData.ioType === 'OUT'" :span="12">
            <el-form-item label="出库用途" prop="usageType">
              <el-select
                v-model="formData.usageType"
                clearable
                placeholder="请选择出库用途"
                class="!w-full"
                :disabled="isDetail"
              >
                <el-option
                  v-for="dict in getStrDictOptions(DICT_TYPE.IOT_SPARE_USAGE_TYPE)"
                  :key="String(dict.value)"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="formData.ioType === 'OUT'" :span="12">
            <el-form-item label="用途关联ID" prop="usageId">
              <el-input v-model="formData.usageId" placeholder="请输入关联ID" :disabled="isDetail" />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="form-section">
        <div class="section-title">备件明细</div>
        <el-table
          :data="formData.items"
          border
          class="spare-io-items-table"
          :header-cell-style="{ background: '#fafafa' }"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="备件" min-width="260">
            <template #default="scope">
              <el-select
                v-model="scope.row.spareId"
                filterable
                placeholder="请选择备件"
                class="!w-full"
                :disabled="isDetail"
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
          <el-table-column
            v-if="formData.ioType === 'OUT' || formData.ioType === 'IN'"
            label="实际库存"
            width="160"
          >
            <template #default="scope">
              <div>{{ formatStockQty(scope.row.stockQty) }}</div>
              <div
                v-if="formData.ioType === 'IN' && isLowStock(scope.row)"
                class="stock-warning-tip"
              >
                库存数量低于预警值，请及时补充
              </div>
            </template>
          </el-table-column>
          <el-table-column v-if="formData.ioType === 'IN'" label="预警库存" width="120">
            <template #default="scope">
              {{ formatStockQty(scope.row.minStock) }}
            </template>
          </el-table-column>
          <el-table-column label="出入库数量" width="160">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.ioQty"
                :min="1"
                class="!w-full"
                :disabled="isDetail"
                @change="handleIoQtyChange(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column v-if="formMode === 'create'" label="操作" width="90" align="center">
            <template #default="scope">
              <el-button
                link
                type="danger"
                :disabled="formData.items.length === 1"
                @click="removeItem(scope.$index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="formMode === 'create'" class="items-action">
          <el-button type="primary" plain @click="addItem">+ 添加备件明细</el-button>
        </div>
      </div>

      <div class="form-section">
        <div class="section-title">备注</div>
        <el-row :gutter="12">
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="2"
                placeholder="请输入备注"
                :readonly="isDetail"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">{{ isDetail ? '关闭' : '取消' }}</el-button>
      <el-button v-if="!isDetail" type="primary" :loading="formLoading" @click="submitForm">
        确定
      </el-button>
    </template>
  </el-dialog>

</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { SpareApi } from '@/api/iot/spare'
import {
  SpareIoApi,
  type SpareIoVO,
  type SpareIoPageReqVO,
} from '@/api/iot/spare-io'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'IotSpareIo' })

const message = useMessage()
const { t } = useI18n()
const { currentRoute, replace } = useRouter()

const loading = ref(true)
const list = ref<SpareIoVO[]>([])
const total = ref(0)
const exportLoading = ref(false)
const queryFormRef = ref<FormInstance>()

const queryParams = reactive<SpareIoPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  spareId: undefined,
  ioType: undefined,
  auditStatus: undefined,
  usageType: undefined,
  ioTime: []
})

const spareOptions = ref<{ id: number; label: string }[]>([])

const loadSpareOptions = async () => {
  const data = await SpareApi.getSpareSimpleList()
  spareOptions.value = (data || []).map((item: any) => ({
    id: item.id,
    label: `${item.spareName || ''} ${item.spareSpec || ''} ${item.spareModel || ''}`.trim()
  }))
}

const getList = async () => {
  loading.value = true
  try {
    const data = await SpareIoApi.getSpareIoPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
    clearSpareStockCache()
    await fillStockQtyForList(list.value)
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

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await SpareIoApi.exportSpareIoExcel(queryParams)
    download.excel(data, '备件出入库记录.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await SpareIoApi.deleteSpareIo(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const formVisible = ref(false)
const formTitle = ref('')
const formMode = ref<'create' | 'update' | 'detail'>('create')
const isDetail = computed(() => formMode.value === 'detail')
const formLoading = ref(false)
const formRef = ref<FormInstance>()

interface SpareIoItem {
  spareId?: number
  ioQty?: number
  stockQty?: number
  minStock?: number
}

interface SpareIoFormData {
  id?: number
  ioType: string
  ioTime?: string
  usageType?: string
  usageId?: number
  remark?: string
  items: SpareIoItem[]
}

const formData = reactive<SpareIoFormData>({
  id: undefined,
  ioType: '',
  ioTime: '',
  usageType: undefined,
  usageId: undefined,
  remark: '',
  items: [{ spareId: undefined, ioQty: 1, stockQty: undefined, minStock: undefined }]
})

const formRules: FormRules = {
  ioType: [{ required: true, message: '出入库类型不能为空', trigger: 'change' }],
  ioTime: [{ required: true, message: '出入库时间不能为空', trigger: 'change' }]
}

interface SpareStockInfo {
  stockQty: number
  minStock: number
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
    stockQty: data?.stockQty ?? 0,
    minStock: data?.minStock ?? 0
  }
  spareStockMap.value[key] = info
  return info
}

const clearSpareStockCache = () => {
  spareStockMap.value = {}
}

const fillStockQtyForList = async (rows: SpareIoVO[]) => {
  const spareIdSet = new Set<number>()
  rows?.forEach((row) => {
    if (row?.spareId !== undefined && row?.spareId !== null) {
      spareIdSet.add(row.spareId)
    }
  })
  for (const spareId of spareIdSet) {
    const info = await loadSpareStockInfo(spareId)
    if (!info) continue
    rows.forEach((row) => {
      if (row?.spareId === spareId) {
        row.stockQty = info.stockQty
      }
    })
  }
}

const formatStockQty = (stockQty?: number) => {
  if (stockQty === undefined || stockQty === null) {
    return '-'
  }
  return stockQty
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

const formatIoQty = (row: SpareIoVO) => {
  if (!row || row.ioQty === undefined || row.ioQty === null) {
    return '-'
  }
  const sign = row.ioType === 'IN' ? '+' : row.ioType === 'OUT' ? '-' : ''
  return `${sign}${row.ioQty}`
}

const normalizeIoTime = (value?: string | number) => {
  if (value === undefined || value === null || value === '') {
    return ''
  }
  if (typeof value === 'number') {
    const ts = value < 1_000_000_000_000 ? value * 1000 : value
    return dayjs(ts).format('YYYY-MM-DD HH:mm:ss')
  }
  if (typeof value === 'string') {
    if (/^\d+$/.test(value)) {
      const parsed = Number(value)
      if (!Number.isNaN(parsed)) {
        const ts = parsed < 1_000_000_000_000 ? parsed * 1000 : parsed
        return dayjs(ts).format('YYYY-MM-DD HH:mm:ss')
      }
    }
    const parsed = dayjs(value)
    return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : value
  }
  return ''
}

const toIoTimeTimestamp = (value?: string | number) => {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  if (typeof value === 'number') {
    return value < 1_000_000_000_000 ? value * 1000 : value
  }
  if (typeof value === 'string') {
    if (/^\d+$/.test(value)) {
      const parsed = Number(value)
      if (!Number.isNaN(parsed)) {
        return parsed < 1_000_000_000_000 ? parsed * 1000 : parsed
      }
    }
    const parsed = dayjs(value, 'YYYY-MM-DD HH:mm:ss')
    if (parsed.isValid()) {
      return parsed.valueOf()
    }
    const fallback = dayjs(value)
    return fallback.isValid() ? fallback.valueOf() : undefined
  }
  return undefined
}

const isLowStock = (row: SpareIoItem) => {
  if (row.stockQty === undefined || row.stockQty === null) {
    return false
  }
  if (row.minStock === undefined || row.minStock === null) {
    return false
  }
  return row.stockQty < row.minStock
}

const handleSpareChange = async (row: SpareIoItem) => {
  if (!row?.spareId) {
    row.stockQty = undefined
    row.minStock = undefined
    return
  }
  const info = await loadSpareStockInfo(row.spareId)
  row.stockQty = info?.stockQty
  row.minStock = info?.minStock
  if (
    formData.ioType === 'OUT' &&
    row.ioQty &&
    row.stockQty !== undefined &&
    row.ioQty > row.stockQty
  ) {
    message.warning(`出库数量不能大于实际库存（${row.stockQty}）`)
  }
}

const handleIoQtyChange = (row: SpareIoItem) => {
  if (
    formData.ioType === 'OUT' &&
    row?.ioQty !== undefined &&
    row?.stockQty !== undefined &&
    row.ioQty > row.stockQty
  ) {
    message.warning(`出库数量不能大于实际库存（${row.stockQty}）`)
  }
}

const openCreateFromRoute = async () => {
  if (currentRoute.value.query?.action !== 'create') {
    return
  }
  await openForm('create')
  const query = { ...currentRoute.value.query }
  delete query.action
  replace({ query })
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    ioType: '',
    ioTime: '',
    usageType: undefined,
    usageId: undefined,
    remark: '',
    items: [{ spareId: undefined, ioQty: 1, stockQty: undefined, minStock: undefined }]
  })
  formRef.value?.clearValidate()
}

const openForm = async (type: 'create' | 'update' | 'detail', id?: number) => {
  formMode.value = type
  formTitle.value =
    type === 'create' ? '新增出入库记录' : type === 'detail' ? '出入库详情' : '编辑出入库记录'
  formVisible.value = true
  resetForm()
  await loadSpareOptions()
  if ((type === 'update' || type === 'detail') && id) {
    const data = await SpareIoApi.getSpareIo(id)
    Object.assign(formData, {
      id: data.id,
      ioType: data.ioType || '',
      ioTime: normalizeIoTime(data.ioTime),
      usageType: data.usageType,
      usageId: data.usageId,
      remark: data.remark || '',
      items: [
        { spareId: data.spareId, ioQty: data.ioQty || 1, stockQty: undefined, minStock: undefined }
      ]
    })
    const item = formData.items[0]
    if (item?.spareId) {
      const info = await loadSpareStockInfo(item.spareId)
      item.stockQty = info?.stockQty
      item.minStock = info?.minStock
    }
  }
}

const addItem = () => {
  formData.items.push({ spareId: undefined, ioQty: 1, stockQty: undefined, minStock: undefined })
}

const removeItem = (index: number) => {
  if (formData.items.length <= 1) {
    return
  }
  formData.items.splice(index, 1)
}

const validateItems = async () => {
  if (!formData.items.length) {
    message.error('请至少添加一条备件明细')
    return false
  }
  for (let i = 0; i < formData.items.length; i++) {
    const item = formData.items[i]
    if (!item.spareId) {
      message.error(`第 ${i + 1} 行请选择备件`)
      return false
    }
    if (!item.ioQty || item.ioQty < 1) {
      message.error(`第 ${i + 1} 行请输入出入库数量`)
      return false
    }
    if (formData.ioType === 'OUT') {
      let stockQty = item.stockQty
      if (stockQty === undefined) {
        const info = await loadSpareStockInfo(item.spareId)
        stockQty = info?.stockQty
        item.stockQty = stockQty
      }
      if (stockQty === undefined || stockQty === null) {
        message.error(`第 ${i + 1} 行无法获取实际库存`)
        return false
      }
      if (item.ioQty > stockQty) {
        message.error(`第 ${i + 1} 行出库数量不能大于实际库存（${stockQty}）`)
        return false
      }
    }
  }
  return true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }
  if (!(await validateItems())) {
    return
  }
  formLoading.value = true
  try {
    if (formMode.value === 'create') {
      for (const item of formData.items) {
        await SpareIoApi.createSpareIo({
          spareId: item.spareId,
          ioType: formData.ioType,
          ioTime: toIoTimeTimestamp(formData.ioTime),
          ioQty: item.ioQty,
          usageType: formData.usageType,
          usageId: formData.usageId,
          remark: formData.remark
        })
      }
      message.success(t('common.createSuccess'))
    } else {
      const item = formData.items[0]
      await SpareIoApi.updateSpareIo({
        id: formData.id,
        spareId: item?.spareId,
        ioType: formData.ioType,
        ioTime: toIoTimeTimestamp(formData.ioTime),
        ioQty: item?.ioQty,
        usageType: formData.usageType,
        usageId: formData.usageId,
        remark: formData.remark
      })
      message.success(t('common.updateSuccess'))
    }
    formVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

watch(
  () => formData.ioType,
  async (val) => {
    if (val !== 'OUT') {
      formData.usageType = undefined
      formData.usageId = undefined
    }
    if (val !== 'OUT' && val !== 'IN') {
      return
    }
    for (const item of formData.items) {
      if (item?.spareId) {
        const info = await loadSpareStockInfo(item.spareId)
        item.stockQty = info?.stockQty
        item.minStock = info?.minStock
      }
    }
  }

)

watch(
  () => currentRoute.value.query?.action,
  (action) => {
    if (action === 'create') {
      openCreateFromRoute()
    }
  }
)
onMounted(async () => {
  await loadSpareOptions()
  await getList()
  await openCreateFromRoute()
})
</script>

<style scoped>
.spare-io-dialog :deep(.el-dialog__body) {
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

.spare-io-items-table {
  margin-bottom: 10px;
}

.items-action {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.stock-warning-tip {
  font-size: 12px;
  color: #e6a23c;
  line-height: 1.2;
}

</style>
