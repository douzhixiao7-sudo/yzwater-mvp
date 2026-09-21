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
      <el-form-item label="盘点结果" prop="resultStatus">
        <el-select
          v-model="queryParams.resultStatus"
          clearable
          class="!w-220px"
          placeholder="请选择盘点结果"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.IOT_SPARE_CHECK_RESULT)"
            :key="String(dict.value)"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="盘点时间" prop="checkTime">
        <el-date-picker
          v-model="queryParams.checkTime"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
          format="YYYY-MM-DD"
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
          v-hasPermi="['iot:spare-check:create']"
          type="primary"
          plain
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="备件名称" align="center" prop="spareName" min-width="120" />
      <el-table-column label="备件规格" align="center" prop="spareSpec" min-width="110" />
      <el-table-column label="备件型号" align="center" prop="spareModel" min-width="110" />
      <el-table-column
        label="盘点时间"
        align="center"
        prop="checkTime"
        width="140"
        :formatter="dateFormatter2"
      />
      <el-table-column label="系统库存" align="center" prop="systemQty" width="100" />
      <el-table-column label="实盘数量" align="center" prop="actualQty" width="100" />
      <el-table-column label="差异数量" align="center" prop="diffQty" width="100" />
      <el-table-column label="盘点结果" align="center" prop="resultStatus" width="110">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_SPARE_CHECK_RESULT" :value="scope.row.resultStatus" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="160">
        <template #default="scope">
          <el-button
            v-hasPermi="['iot:spare-check:query']"
            link
            type="primary"
            @click="openForm('detail', scope.row.id)"
          >
            详情
          </el-button>
          <el-button
            v-hasPermi="['iot:spare-check:update']"
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
          >
            编辑
          </el-button>
          <el-button
            v-hasPermi="['iot:spare-check:delete']"
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
    width="860px"
    destroy-on-close
    :close-on-click-modal="false"
    class="spare-check-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      label-position="left"
    >
      <div class="form-section">
        <div class="section-title">盘点信息</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="盘点时间" prop="checkTime">
              <el-date-picker
                v-model="formData.checkTime"
                type="date"
                value-format="x"
                format="YYYY-MM-DD"
                placeholder="请选择时间"
                class="!w-full"
                :disabled="isDetail"
              />
            </el-form-item>
          </el-col>
        </el-row>
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

      <div class="form-section">
        <div class="section-title">备件明细</div>
        <el-table
          :data="formData.items"
          border
          class="spare-check-items-table"
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
          <el-table-column label="账面库存" width="120">
            <template #default="scope">
              {{ formatQty(scope.row.systemQty) }}
            </template>
          </el-table-column>
          <el-table-column label="实盘数量" width="160">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.actualQty"
                :min="0"
                class="!w-full"
                :disabled="isDetail"
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
          <el-button type="primary" plain @click="addItem">+ 添加盘点明细</el-button>
        </div>
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
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { dateFormatter2 } from '@/utils/formatTime'
import { SpareApi } from '@/api/iot/spare'
import {
  SpareCheckApi,
  type SpareCheckVO,
  type SpareCheckPageReqVO
} from '@/api/iot/spare-check'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'IotSpareCheck' })

const message = useMessage()
const { t } = useI18n()
const { currentRoute, replace } = useRouter()

const loading = ref(true)
const list = ref<SpareCheckVO[]>([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()

const queryParams = reactive<SpareCheckPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  spareId: undefined,
  resultStatus: undefined,
  checkTime: []
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
    const data = await SpareCheckApi.getSpareCheckPage(queryParams)
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
  handleQuery()
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await SpareCheckApi.deleteSpareCheck(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const formVisible = ref(false)
const formTitle = ref('新增盘点记录')
const formMode = ref<'create' | 'update' | 'detail'>('create')
const isDetail = computed(() => formMode.value === 'detail')
const formLoading = ref(false)
const formRef = ref<FormInstance>()

interface SpareCheckItem {
  spareId?: number
  actualQty?: number
  systemQty?: number
}

interface SpareCheckFormData {
  id?: number
  checkTime?: number | string
  remark?: string
  items: SpareCheckItem[]
}

const formData = reactive<SpareCheckFormData>({
  id: undefined,
  checkTime: '',
  remark: '',
  items: [{ spareId: undefined, actualQty: 0, systemQty: undefined }]
})

const formRules: FormRules = {
  checkTime: [{ required: true, message: '盘点时间不能为空', trigger: 'change' }]
}

const formatQty = (value?: number) => {
  if (value === undefined || value === null) {
    return '-'
  }
  return value
}

const normalizeCheckTime = (value?: string | number) => {
  if (value === undefined || value === null || value === '') {
    return ''
  }
  if (typeof value === 'number') {
    const ts = value < 1_000_000_000_000 ? value * 1000 : value
    return String(ts)
  }
  if (typeof value === 'string') {
    if (/^\d+$/.test(value)) {
      const parsed = Number(value)
      if (!Number.isNaN(parsed)) {
        const ts = parsed < 1_000_000_000_000 ? parsed * 1000 : parsed
        return String(ts)
      }
    }
    const parsed = dayjs(value, 'YYYY-MM-DD')
    if (parsed.isValid()) {
      return String(parsed.valueOf())
    }
    const fallback = dayjs(value)
    return fallback.isValid() ? String(fallback.valueOf()) : ''
  }
  return ''
}

const toCheckTimeTimestamp = (value?: string | number) => {
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
    const parsed = dayjs(value, 'YYYY-MM-DD')
    if (parsed.isValid()) {
      return parsed.valueOf()
    }
    const fallback = dayjs(value)
    return fallback.isValid() ? fallback.valueOf() : undefined
  }
  return undefined
}

const loadSystemQty = async (spareId?: number) => {
  if (!spareId && spareId !== 0) {
    return undefined
  }
  const data = await SpareApi.getSpare(spareId)
  return data?.stockQty ?? 0
}

const handleSpareChange = async (row: SpareCheckItem) => {
  if (!row?.spareId) {
    row.systemQty = undefined
    return
  }
  row.systemQty = await loadSystemQty(row.spareId)
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
    checkTime: '',
    remark: '',
    items: [{ spareId: undefined, actualQty: 0, systemQty: undefined }]
  })
  formRef.value?.clearValidate()
}

const openForm = async (type: 'create' | 'update' | 'detail', id?: number) => {
  formMode.value = type
  formTitle.value =
    type === 'create' ? '新增盘点记录' : type === 'detail' ? '盘点详情' : '编辑盘点记录'
  resetForm()
  formVisible.value = true
  if ((type === 'detail' || type === 'update') && id) {
    const data = await SpareCheckApi.getSpareCheck(id)
    Object.assign(formData, {
      id: data.id,
      checkTime: normalizeCheckTime(data.checkTime),
      remark: data.remark || '',
      items: [
        {
          spareId: data.spareId,
          actualQty: data.actualQty,
          systemQty: data.systemQty
        }
      ]
    })
  }
}

const addItem = () => {
  formData.items.push({
    spareId: undefined,
    actualQty: 0,
    systemQty: undefined
  })
}

const removeItem = (index: number) => {
  if (formData.items.length <= 1) {
    return
  }
  formData.items.splice(index, 1)
}

const validateItems = () => {
  if (!formData.items.length) {
    message.error('请至少添加一条盘点明细')
    return false
  }
  for (let i = 0; i < formData.items.length; i++) {
    const item = formData.items[i]
    if (!item.spareId) {
      message.error(`第 ${i + 1} 行请选择备件`)
      return false
    }
    if (item.actualQty === undefined || item.actualQty === null || item.actualQty < 0) {
      message.error(`第 ${i + 1} 行请输入实盘数量`)
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
  if (!validateItems()) {
    return
  }
  formLoading.value = true
  try {
    if (formMode.value === 'create') {
      for (const item of formData.items) {
        await SpareCheckApi.createSpareCheck({
          spareId: item.spareId,
          checkTime: toCheckTimeTimestamp(formData.checkTime),
          actualQty: item.actualQty,
          applyResult: false,
          remark: formData.remark
        })
      }
      message.success(t('common.createSuccess'))
    } else {
      const item = formData.items[0]
      await SpareCheckApi.updateSpareCheck({
        id: formData.id,
        spareId: item?.spareId,
        checkTime: toCheckTimeTimestamp(formData.checkTime),
        actualQty: item?.actualQty,
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

onMounted(async () => {
  await loadSpareOptions()
  await getList()
  await openCreateFromRoute()
})

watch(
  () => currentRoute.value.query?.action,
  (action) => {
    if (action === 'create') {
      openCreateFromRoute()
    }
  }
)
</script>

<style scoped>
.spare-check-dialog :deep(.el-dialog__body) {
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

.spare-check-items-table {
  margin-bottom: 10px;
}

.items-action {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

</style>
