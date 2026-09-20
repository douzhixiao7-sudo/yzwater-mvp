<template>
  <div class="dispatch-plan-page">
    <ContentWrap>
      <el-form
        ref="queryFormRef"
        :inline="true"
        :model="queryParams"
        class="-mb-15px"
        label-width="100px"
      >
        <el-form-item label="方案名称" prop="planName">
          <el-input
            v-model="queryParams.planName"
            clearable
            placeholder="请输入方案名称"
            class="!w-220px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="方案类型" prop="planType">
          <el-select v-model="queryParams.planType" clearable placeholder="请选择方案类型" class="!w-180px">
            <el-option
              v-for="item in PLAN_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
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
        <el-form-item label="编制人" prop="prepareUserName">
          <el-input
            v-model="queryParams.prepareUserName"
            clearable
            placeholder="请输入编制人"
            class="!w-180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="方案状态" prop="planStatus">
          <el-select v-model="queryParams.planStatus" clearable placeholder="请选择方案状态" class="!w-160px">
            <el-option
              v-for="item in PLAN_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="编制时间" prop="prepareTime">
          <el-date-picker
            v-model="prepareTimeRange"
            type="daterange"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD"
            start-placeholder="编制开始时间"
            end-placeholder="编制结束时间"
            class="!w-300px"
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
            v-hasPermi="['iot:dispatch-plan:create']"
            type="primary"
            plain
            @click="openForm('create')"
          >
            <Icon icon="ep:plus" class="mr-4px" />
            新增调度方案
          </el-button>
          <el-button
            v-hasPermi="['iot:dispatch-plan:export']"
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

    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="方案名称" prop="planName" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="name-text">{{ row.planName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="方案类型" min-width="120">
          <template #default="{ row }">
            <el-tag effect="light" type="primary">{{ resolvePlanTypeLabel(row.planType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属站点" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ resolveStationLabel(row.stationId) || '-' }}</template>
        </el-table-column>
        <el-table-column label="编制人/单位" min-width="180">
          <template #default="{ row }">
            <span>{{ row.prepareUserName || '-' }}</span>
            <span class="split-text">/</span>
            <span>{{ row.prepareOrgName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="编制时间" prop="prepareTime" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.prepareTime) }}</template>
        </el-table-column>
        <el-table-column label="核心建议目标" prop="coreTarget" min-width="220" show-overflow-tooltip />
        <el-table-column label="涉及工程" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ resolveProjectLabel(row.projectName) }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="resolveStatusTagType(row.planStatus)" effect="plain">
              {{ resolveStatusLabel(row.planStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="info" @click="openForm('detail', row.id)">详情</el-button>
            <el-button
              v-hasPermi="['iot:dispatch-plan:update']"
              link
              type="primary"
              :disabled="!canEdit(row)"
              @click="openForm('update', row.id)"
            >
              编辑
            </el-button>
            <el-button
              v-hasPermi="['iot:dispatch-plan:delete']"
              link
              type="danger"
              :disabled="!canEdit(row)"
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

    <Dialog v-model="formVisible" :title="formTitle" width="1080px">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="96px"
        class="dispatch-plan-form"
        v-loading="formLoading"
      >
        <section class="form-section">
          <div class="section-title">基础信息</div>
          <el-row :gutter="18">
            <el-col :span="12">
              <el-form-item label="方案名称" prop="planName">
                <el-input
                  v-model="formData.planName"
                  maxlength="60"
                  show-word-limit
                  placeholder="请输入方案名称"
                  :disabled="formReadonly"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="方案类型" prop="planType">
                <el-select v-model="formData.planType" placeholder="请选择方案类型" :disabled="formReadonly">
                  <el-option
                    v-for="item in PLAN_TYPE_OPTIONS"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="编制单位" prop="prepareOrgName">
                <el-input v-model="formData.prepareOrgName" placeholder="请输入编制单位" :disabled="formReadonly" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="编制人" prop="prepareUserName">
                <el-input v-model="formData.prepareUserName" placeholder="请输入编制人" :disabled="formReadonly" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="编制时间">
                <el-date-picker
                  v-model="formData.prepareTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择日期时间"
                  class="!w-full"
                  :disabled="formReadonly"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="涉及工程" prop="projectName">
                <el-select
                  v-model="formData.projectName"
                  filterable
                  multiple
                  collapse-tags
                  :max-collapse-tags="4"
                  collapse-tags-tooltip
                  placeholder="请选择涉及工程（可多选）"
                  :disabled="formReadonly"
                >
                  <el-option
                    v-for="item in projectOptions"
                    :key="String(item.value)"
                    :label="item.label"
                    :value="String(item.value)"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属站点" prop="stationId">
                <el-select
                  v-model="formData.stationId"
                  filterable
                  clearable
                  placeholder="请选择所属站点"
                  :disabled="formReadonly"
                  @change="handlePlanStationChange"
                >
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
              <el-form-item label="方案状态" prop="planStatus">
                <el-radio-group v-model="formData.planStatus" :disabled="formReadonly">
                  <el-radio v-for="item in PLAN_STATUS_OPTIONS" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="建议目标" prop="coreTarget">
                <el-input
                  v-model="formData.coreTarget"
                  type="textarea"
                  :rows="2"
                  maxlength="200"
                  show-word-limit
                  placeholder="请输入核心建议目标"
                  :disabled="formReadonly"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section object-section">
          <div class="section-title section-title-row">
            <span>方案配置</span>
            <div v-if="!formReadonly" class="object-tools">
              <el-button type="primary" plain size="small" @click="addObject(1)">
                + 设备对象
              </el-button>
              <el-button type="primary" plain size="small" @click="addObject(2)">
                + 自定义项
              </el-button>
            </div>
          </div>
          <div
            v-for="(obj, objIndex) in formData.objects"
            :key="`obj_${objIndex}`"
            class="object-card"
            :class="{ 'object-card--custom': obj.objectType === 2 }"
          >
            <div class="object-card-head">
              <div class="object-card-title">
                <span class="object-card-title__text">{{ resolveObjectLabel(obj, objIndex) }}</span>
                <el-tag size="small" effect="plain" :type="obj.objectType === 1 ? 'primary' : 'success'">
                  {{ obj.objectType === 1 ? '设备对象' : '自定义项' }}
                </el-tag>
              </div>
              <el-button
                v-if="!formReadonly"
                link
                type="danger"
                :disabled="formData.objects.length <= 1"
                @click="removeObject(objIndex)"
              >
                <Icon icon="ep:delete" />
              </el-button>
            </div>
            <el-row v-if="obj.objectType === 1" :gutter="16">
              <el-col :span="24">
                <el-form-item label="设备对象" required label-width="84px">
                  <el-select
                    v-model="obj.deviceId"
                    filterable
                    remote
                    reserve-keyword
                    clearable
                    placeholder="请先在基础信息选择所属站点后搜索设备"
                    :remote-method="(keyword) => handleDeviceRemoteSearch(keyword, obj)"
                    :loading="deviceLoading"
                    :disabled="formReadonly || !formData.stationId"
                    @change="(val) => handleDeviceChange(val, obj)"
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
            </el-row>
            <el-row v-else :gutter="16">
              <el-col :span="12">
                <el-form-item label="自定义项" required label-width="84px">
                  <el-input
                    v-model="obj.objectName"
                    maxlength="128"
                    placeholder="请输入自定义项名称"
                    :disabled="formReadonly"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="说明" label-width="84px">
                  <el-input
                    v-model="obj.remark"
                    maxlength="128"
                    placeholder="请输入说明（选填）"
                    :disabled="formReadonly"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <div class="param-block">
              <div class="param-head">
                <span>{{ obj.objectType === 1 ? '运行参数' : '自定义字段' }}</span>
                <el-button v-if="!formReadonly" type="primary" plain size="small" @click="addParam(obj)">
                  + {{ obj.objectType === 1 ? '添加参数' : '添加字段' }}
                </el-button>
              </div>
              <div
                v-for="(param, paramIndex) in obj.params"
                :key="`param_${objIndex}_${paramIndex}`"
                class="param-row"
              >
                <span class="param-index">{{ resolveParamLabel(obj, paramIndex) }}</span>
                <el-input
                  v-model="param.paramName"
                  class="param-input"
                  placeholder="参数名称"
                  :disabled="formReadonly"
                />
                <el-input
                  v-model="param.paramValue"
                  class="param-input"
                  placeholder="标准值/阈值等"
                  :disabled="formReadonly"
                />
                <el-input
                  v-model="param.paramUnit"
                  class="param-unit"
                  placeholder="单位"
                  :disabled="formReadonly"
                />
                <el-button
                  v-if="!formReadonly"
                  link
                  type="danger"
                  class="param-remove"
                  :disabled="obj.params.length <= 1"
                  @click="removeParam(obj, paramIndex)"
                >
                  <Icon icon="ep:delete" />
                </el-button>
              </div>
            </div>
          </div>
        </section>

        <section class="form-section">
          <div class="section-title">补充说明</div>
          <el-row :gutter="16">
            <el-col :span="24">
              <el-form-item label="预期效果">
                <el-input
                  v-model="formData.expectedEffect"
                  type="textarea"
                  :rows="2"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入预期效果分析"
                  :disabled="formReadonly"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="编制说明">
                <el-input
                  v-model="formData.prepareDesc"
                  type="textarea"
                  :rows="2"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入编制说明"
                  :disabled="formReadonly"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="附件">
                <div class="attachment-block">
                  <UploadFile
                    v-model="formData.attachments"
                    :limit="5"
                    :file-type="['pdf', 'doc', 'docx', 'png', 'jpg', 'jpeg']"
                    :disabled="formReadonly"
                  />
                  <p class="attachment-tip">支持 PDF、Word、图片，最多上传 5 个文件，每个文件不大于 2MB</p>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注">
                <el-input
                  v-model="formData.remark"
                  type="textarea"
                  :rows="2"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入备注"
                  :disabled="formReadonly"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">{{ formReadonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!formReadonly" type="primary" :loading="submitLoading" @click="submitForm">
          保存方案
        </el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { UploadFile } from '@/components/UploadFile'
import {
  DispatchPlanApi,
  type DispatchPlanObjectVO,
  type DispatchPlanPageReqVO,
  type DispatchPlanParamVO,
  type DispatchPlanVO
} from '@/api/iot/dispatch/plan'
import { DeviceApi } from '@/api/iot/device/device'
import { useUserStore } from '@/store/modules/user'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'IotDispatchPlan' })

type FormMode = 'create' | 'update' | 'detail'

interface OptionItem {
  label: string
  value: string | number
}

interface DeviceOptionItem {
  id: string | number
  label: string
  stationId?: string
  stationLabel?: string
}

interface DispatchPlanObjectFormVO extends DispatchPlanObjectVO {
  stationId?: string
}

interface DispatchPlanEditVO {
  id?: number
  planNo?: string
  planName: string
  planType: string
  stationId?: string
  prepareUserName: string
  prepareOrgName: string
  prepareTime: string
  planStatus: number
  coreTarget: string
  projectName: string[]
  expectedEffect: string
  prepareDesc: string
  attachments: string[]
  objects: DispatchPlanObjectFormVO[]
  remark: string
}

const PLAN_TYPE_OPTIONS: OptionItem[] = [
  { label: '日常调度', value: 'routine' },
  { label: '防洪调度', value: 'flood' },
  { label: '抗旱调度', value: 'drought' },
  { label: '生态补水', value: 'eco_replenishment' },
  { label: '应急调度', value: 'emergency' }
]

const PLAN_STATUS_OPTIONS: OptionItem[] = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 }
]

const stationOptions = computed<OptionItem[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const projectOptions = computed<OptionItem[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_WATER_ENGINEERING)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const message = useMessage()
const { t } = useI18n()
const userStore = useUserStore()

const loading = ref(false)
const exportLoading = ref(false)
const list = ref<DispatchPlanVO[]>([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()

const queryParams = reactive<DispatchPlanPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  planName: undefined,
  planType: undefined,
  stationId: undefined,
  prepareUserName: undefined,
  planStatus: undefined,
  prepareTime: undefined
})
const prepareTimeRange = ref<string[]>([])

const buildQueryParams = () => {
  return {
    ...queryParams,
    prepareTime: prepareTimeRange.value && prepareTimeRange.value.length === 2 ? prepareTimeRange.value : undefined
  }
}

const resolvePlanTypeLabel = (type?: string) => {
  return PLAN_TYPE_OPTIONS.find((item) => item.value === type)?.label || type || '-'
}

const resolveProjectLabel = (projectValue?: string) => {
  const selectedValues = parseProjectNames(projectValue)
  if (!selectedValues.length) return '-'
  return selectedValues
    .map((value) => projectOptions.value.find((item) => String(item.value) === value)?.label || value)
    .join('、')
}

const resolveStationLabel = (stationId?: string) => {
  if (!stationId) return ''
  return stationOptions.value.find((item) => String(item.value) === String(stationId))?.label || stationId
}

const resolveStatusLabel = (status?: number) => {
  if (status === undefined || status === null) return '-'
  return Number(status) === 0 ? '启用' : '禁用'
}

const resolveStatusTagType = (status?: number) => {
  if (status === undefined || status === null) return 'info'
  return Number(status) === 0 ? 'success' : 'info'
}

const formatDateTime = (value?: string) => {
  if (!value) return '-'
  return dayjs(value).isValid() ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : value
}

const canEdit = (row: DispatchPlanVO) => {
  const usedCount = Number(row.usedCount || 0)
  return ![2, 3].includes(Number(row.planStatus)) && usedCount === 0
}

const resolveObjectLabel = (obj: DispatchPlanObjectFormVO, index: number) => {
  if (Number(obj.objectType) === 2) {
    return `自定义项${index + 1}`
  }
  return `设备对象${index + 1}`
}

const resolveParamLabel = (obj: DispatchPlanObjectFormVO, index: number) => {
  if (Number(obj.objectType) === 2) {
    return `字段${index + 1}`
  }
  return `运行参数${index + 1}`
}

const normalizePlanStatus = (status?: number | null) => {
  return Number(status) === 0 ? 0 : 1
}

const parseProjectNames = (value?: string) => {
  if (!value) return []
  return value
    .split(/[,，;；]/)
    .map((item) => item.trim())
    .filter((item, index, arr) => item && arr.indexOf(item) === index)
}

const serializeProjectNames = (values?: string[]) => {
  if (!values || !values.length) return ''
  return values
    .map((item) => String(item).trim())
    .filter((item, index, arr) => item && arr.indexOf(item) === index)
    .join(',')
}

const getList = async () => {
  loading.value = true
  try {
    const data = await DispatchPlanApi.getDispatchPlanPage(buildQueryParams())
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
  prepareTimeRange.value = []
  queryParams.pageNo = 1
  getList()
}

const handleDelete = async (id?: number) => {
  if (!id) return
  try {
    await message.delConfirm()
    await DispatchPlanApi.deleteDispatchPlan(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await DispatchPlanApi.exportDispatchPlanExcel(buildQueryParams())
    download.excel(data, '调度方案.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const createEmptyParam = (): DispatchPlanParamVO => ({
  paramName: '',
  paramValue: '',
  paramUnit: '',
  remark: ''
})

const createEmptyObject = (): DispatchPlanObjectFormVO => ({
  objectType: 1,
  stationId: undefined,
  objectName: '',
  deviceId: undefined,
  locationId: undefined,
  remark: '',
  params: [createEmptyParam()]
})

const createEmptyFormData = (): DispatchPlanEditVO => ({
  id: undefined,
  planNo: '',
  planName: '',
  planType: PLAN_TYPE_OPTIONS[0].value as string,
  stationId: undefined,
  prepareUserName: userStore.getUser.nickname || '',
  prepareOrgName: '',
  prepareTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  planStatus: 0,
  coreTarget: '',
  projectName: [],
  expectedEffect: '',
  prepareDesc: '',
  attachments: [],
  objects: [createEmptyObject()],
  remark: ''
})

const formVisible = ref(false)
const formMode = ref<FormMode>('create')
const formTitle = computed(() => {
  if (formMode.value === 'create') return '新增调度方案'
  if (formMode.value === 'update') return '编辑调度方案'
  return '调度方案详情'
})
const formReadonly = computed(() => formMode.value === 'detail')
const formLoading = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive<DispatchPlanEditVO>(createEmptyFormData())

const formRules: FormRules<DispatchPlanEditVO> = {
  planName: [{ required: true, message: '方案名称不能为空', trigger: 'blur' }],
  planType: [{ required: true, message: '方案类型不能为空', trigger: 'change' }],
  stationId: [{ required: true, message: '所属站点不能为空', trigger: 'change' }],
  prepareUserName: [{ required: true, message: '编制人不能为空', trigger: 'blur' }],
  prepareOrgName: [{ required: true, message: '编制单位不能为空', trigger: 'blur' }],
  coreTarget: [{ required: true, message: '核心建议目标不能为空', trigger: 'blur' }],
  projectName: [{ required: true, message: '涉及工程不能为空', trigger: 'change' }]
}

const resetForm = () => {
  Object.assign(formData, createEmptyFormData())
  formRef.value?.clearValidate()
}

const deviceLoading = ref(false)
const deviceOptions = ref<DeviceOptionItem[]>([])
const deviceNameCache = reactive<Record<string, string>>({})
const deviceStationCache = reactive<Record<string, string>>({})
const deviceOptionCache = reactive<Record<string, DeviceOptionItem>>({})

const loadDeviceOptions = async (keyword?: string, stationId?: string) => {
  deviceLoading.value = true
  try {
    const data = await DeviceApi.getDeviceList({
      nickname: keyword?.trim() || undefined,
      stationId: stationId || undefined
    })
    const options = (data || []).map((item: any) => {
      const deviceName = item.nickname || item.deviceName || `设备-${item.id}`
      const stationValue = item.stationId ? String(item.stationId) : ''
      deviceNameCache[String(item.id)] = deviceName
      if (stationValue) {
        deviceStationCache[String(item.id)] = stationValue
      }
      const option: DeviceOptionItem = {
        id: item.id,
        label: deviceName,
        stationId: stationValue || undefined
      }
      deviceOptionCache[String(item.id)] = option
      return {
        ...option
      }
    })
    deviceOptions.value = options
  } finally {
    deviceLoading.value = false
  }
}

const handleDeviceRemoteSearch = async (keyword: string, obj: DispatchPlanObjectFormVO) => {
  if (!formData.stationId) {
    deviceOptions.value = []
    return
  }
  await loadDeviceOptions(keyword, formData.stationId)
  if (obj.deviceId && !deviceOptions.value.some((item) => String(item.id) === String(obj.deviceId))) {
    const option = deviceOptionCache[String(obj.deviceId)]
    if (option) {
      deviceOptions.value.push(option)
      return
    }
    const label = deviceNameCache[String(obj.deviceId)]
    if (
      label &&
      (!formData.stationId || deviceStationCache[String(obj.deviceId)] === formData.stationId)
    ) {
      deviceOptions.value.push({ id: obj.deviceId, label })
    }
  }
}

const handleDeviceChange = (value: string | number | undefined, obj: DispatchPlanObjectFormVO) => {
  if (!value) {
    obj.objectName = ''
    return
  }
  const stationId = deviceStationCache[String(value)]
  if (stationId) {
    obj.stationId = stationId
  } else if (formData.stationId) {
    obj.stationId = formData.stationId
  }
  obj.objectName = deviceNameCache[String(value)] || obj.objectName || ''
}

const handlePlanStationChange = async () => {
  if (formReadonly.value) return
  formData.objects.forEach((obj) => {
    if (Number(obj.objectType) !== 1) return
    obj.deviceId = undefined
    obj.objectName = ''
    obj.stationId = formData.stationId
  })
  if (!formData.stationId) {
    deviceOptions.value = []
    return
  }
  await loadDeviceOptions(undefined, formData.stationId)
}

const addObject = (objectType: 1 | 2 = 1) => {
  const next = createEmptyObject()
  next.objectType = objectType
  if (objectType === 2) {
    next.deviceId = undefined
    next.locationId = undefined
  } else {
    next.stationId = formData.stationId
  }
  formData.objects.push(next)
}

const removeObject = (index: number) => {
  if (formData.objects.length <= 1) {
    message.warning('至少保留一个操作对象')
    return
  }
  formData.objects.splice(index, 1)
}

const addParam = (obj: DispatchPlanObjectFormVO) => {
  obj.params.push(createEmptyParam())
}

const removeParam = (obj: DispatchPlanObjectFormVO, index: number) => {
  if (obj.params.length <= 1) {
    message.warning('至少保留一个参数')
    return
  }
  obj.params.splice(index, 1)
}

const normalizeObjects = () => {
  return formData.objects.map((obj) => ({
    id: obj.id,
    objectType: Number(obj.objectType),
    objectName:
      obj.objectType === 1
        ? deviceNameCache[String(obj.deviceId || '')] || obj.objectName || ''
        : obj.objectName || '',
    deviceId: obj.objectType === 1 ? obj.deviceId : undefined,
    locationId: undefined,
    remark: obj.remark || '',
    params: (obj.params || []).map((param) => ({
      id: param.id,
      paramName: param.paramName?.trim() || '',
      paramValue: param.paramValue?.trim() || '',
      paramUnit: param.paramUnit?.trim() || '',
      remark: param.remark?.trim() || ''
    }))
  }))
}

const validateObjectConfigs = () => {
  if (!formData.stationId) {
    message.warning('请先在基础信息选择所属站点')
    return false
  }
  if (!formData.objects.length) {
    message.warning('请至少配置一个操作对象')
    return false
  }
  for (let i = 0; i < formData.objects.length; i++) {
    const obj = formData.objects[i]
    const indexLabel = `第${i + 1}个操作对象`
    if (Number(obj.objectType) === 1 && !obj.deviceId) {
      message.warning(`${indexLabel}未选择设备`)
      return false
    }
    if (Number(obj.objectType) === 1) {
      const selectedDeviceStation = deviceStationCache[String(obj.deviceId || '')]
      if (selectedDeviceStation && String(selectedDeviceStation) !== String(formData.stationId)) {
        message.warning(`${indexLabel}设备不属于当前所属站点，请重新选择`)
        return false
      }
    }
    if (Number(obj.objectType) === 2 && !obj.objectName?.trim()) {
      message.warning(`${indexLabel}未填写自定义对象名称`)
      return false
    }
    if (!obj.params || !obj.params.length) {
      message.warning(`${indexLabel}未配置控制参数`)
      return false
    }
    for (const param of obj.params) {
      if (!param.paramName?.trim() || !param.paramValue?.trim()) {
        message.warning(`${indexLabel}存在未填写完整的参数`)
        return false
      }
    }
  }
  return true
}

const buildPayload = (): DispatchPlanVO => {
  return {
    id: formData.id,
    planName: formData.planName.trim(),
    planType: formData.planType,
    stationId: formData.stationId,
    prepareUserName: formData.prepareUserName.trim(),
    prepareOrgName: formData.prepareOrgName.trim(),
    prepareTime: formData.prepareTime,
    planStatus: Number(formData.planStatus),
    coreTarget: formData.coreTarget.trim(),
    projectName: serializeProjectNames(formData.projectName),
    expectedEffect: formData.expectedEffect?.trim() || '',
    prepareDesc: formData.prepareDesc?.trim() || '',
    attachments: formData.attachments || [],
    objects: normalizeObjects(),
    remark: formData.remark?.trim() || ''
  }
}

const openForm = async (mode: FormMode, id?: number) => {
  formMode.value = mode
  formVisible.value = true
  resetForm()
  if (mode === 'create') {
    return
  }
  if (!id) return

  formLoading.value = true
  try {
    const data = await DispatchPlanApi.getDispatchPlan(id)
    const stationId = data.stationId ? String(data.stationId) : undefined
    Object.assign(formData, {
      id: data.id,
      planNo: data.planNo,
      planName: data.planName || '',
      planType: data.planType || PLAN_TYPE_OPTIONS[0].value,
      stationId,
      prepareUserName: data.prepareUserName || '',
      prepareOrgName: data.prepareOrgName || '',
      prepareTime: data.prepareTime
        ? dayjs(data.prepareTime).format('YYYY-MM-DD HH:mm:ss')
        : dayjs().format('YYYY-MM-DD HH:mm:ss'),
      planStatus: normalizePlanStatus(data.planStatus),
      coreTarget: data.coreTarget || '',
      projectName: parseProjectNames(data.projectName),
      expectedEffect: data.expectedEffect || '',
      prepareDesc: data.prepareDesc || '',
      attachments: data.attachments || [],
      remark: data.remark || ''
    })
    const objects = (data.objects || []).map((obj) => {
      if (obj.deviceId && obj.objectName) {
        deviceNameCache[String(obj.deviceId)] = obj.objectName
      }
      const stationId =
        (obj as any).stationId ||
        (obj.deviceId ? deviceStationCache[String(obj.deviceId)] : undefined) ||
        undefined
      return {
        id: obj.id,
        objectSort: obj.objectSort,
        objectType: Number(obj.objectType || 1),
        stationId,
        objectName: obj.objectName || '',
        deviceId: obj.deviceId,
        locationId: undefined,
        remark: obj.remark || '',
        params: (obj.params || []).map((param) => ({
          id: param.id,
          paramSort: param.paramSort,
          paramName: param.paramName || '',
          paramValue: param.paramValue || '',
          paramUnit: param.paramUnit || '',
          remark: param.remark || ''
        }))
      } as DispatchPlanObjectFormVO
    })
    formData.objects = objects.length ? objects : [createEmptyObject()]
    const firstDeviceObject = formData.objects.find((item) => Number(item.objectType) === 1)
    formData.stationId = formData.stationId || firstDeviceObject?.stationId
    formData.objects.forEach((item) => {
      if (Number(item.objectType) === 1) {
        item.stationId = formData.stationId
      }
    })
    if (formData.stationId) {
      await loadDeviceOptions(undefined, formData.stationId)
    }
  } finally {
    formLoading.value = false
  }
}

const submitForm = async () => {
  if (formReadonly.value) return
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }
  if (!validateObjectConfigs()) {
    return
  }
  submitLoading.value = true
  try {
    const payload = buildPayload()
    if (formMode.value === 'create') {
      delete payload.id
      await DispatchPlanApi.createDispatchPlan(payload)
      message.success('新增成功')
    } else {
      await DispatchPlanApi.updateDispatchPlan(payload)
      message.success('更新成功')
    }
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([getList(), loadDeviceOptions()])
})
</script>

<style scoped lang="scss">
.dispatch-plan-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dispatch-plan-form {
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  :deep(.el-form-item__label) {
    color: #4c5a70;
    font-weight: 600;
  }

  :deep(.el-textarea__inner),
  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    border-radius: 8px;
  }
}

.name-text {
  color: #173a61;
  font-weight: 600;
}

.split-text {
  margin: 0 4px;
  color: #94a3b8;
}

.form-section {
  border: 1px solid #e2e8f3;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 12px;
  background: #fff;
}

.section-title {
  margin-bottom: 14px;
  font-size: 15px;
  font-weight: 700;
  color: #5b83b8;
  padding-left: 10px;
  border-left: 3px solid #7daee6;
  line-height: 1;
}

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.object-card {
  border: 1px solid #dbe4f2;
  border-radius: 10px;
  background: #fff;
  padding: 12px;
  margin-bottom: 12px;
}

.object-card--custom {
  border-style: dashed;
}

.object-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.object-tools {
  display: flex;
  align-items: center;
  gap: 8px;
}

.object-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.object-card-title__text {
  font-size: 14px;
  font-weight: 700;
  color: #264f80;
}

.param-block {
  border-top: 1px dashed #d4dfef;
  margin-top: 4px;
  padding-top: 10px;
}

.param-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #355981;
  font-size: 13px;
  font-weight: 600;
}

.param-row {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr) minmax(0, 1fr) 120px auto;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.param-row:last-child {
  margin-bottom: 0;
}

.param-index {
  color: #5d789a;
  font-size: 13px;
  white-space: nowrap;
}

.param-input {
  min-width: 0;
}

.param-unit {
  width: 120px;
}

.param-remove {
  font-size: 16px;
}

.attachment-block {
  width: 100%;
}

.attachment-tip {
  margin: 6px 0 0;
  font-size: 12px;
  color: #8a97ab;
}

@media (max-width: 992px) {
  .param-row {
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  .param-index {
    grid-column: 1 / -1;
  }

  .param-unit {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .object-tools {
    width: 100%;
    justify-content: flex-end;
  }

  .section-title-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .param-row {
    grid-template-columns: 1fr;
  }

  .param-remove {
    justify-self: flex-end;
  }

  :deep(.el-table__body-wrapper) {
    overflow-x: auto;
  }

  :deep(.el-dialog) {
    width: calc(100vw - 24px) !important;
  }
}
</style>
