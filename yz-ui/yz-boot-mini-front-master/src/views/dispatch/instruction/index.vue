<template>
  <div class="dispatch-manage-page">
    <ContentWrap class="query-wrap">
      <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="90px">
        <el-form-item label="调度编号" prop="instructionNo">
          <el-input
            v-model="queryParams.instructionNo"
            clearable
            class="!w-220px"
            placeholder="请输入调度编号"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="发令单位" prop="issueOrgName">
          <el-input
            v-model="queryParams.issueOrgName"
            clearable
            class="!w-220px"
            placeholder="请输入发令单位"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="方案名称" prop="planName">
          <el-input
            v-model="queryParams.planName"
            clearable
            class="!w-220px"
            placeholder="请输入方案名称"
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
        <el-form-item label="执行状态" prop="status">
          <el-select v-model="queryParams.status" clearable class="!w-180px" placeholder="请选择执行状态">
            <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划完成" prop="plannedFinishTime">
          <el-date-picker
            v-model="plannedFinishTimeRange"
            type="daterange"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="!w-300px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
          <el-button v-hasPermi="['iot:dispatch-manage:create']" type="primary" plain @click="openForm('create')">
            <Icon icon="ep:plus" class="mr-5px" />
            新增调度
          </el-button>
          <el-button
            v-hasPermi="['iot:dispatch-manage:export']"
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

    <ContentWrap class="table-wrap">
      <el-table v-loading="loading" :data="list" stripe :fit="false">
        <el-table-column label="调度编号" min-width="160">
          <template #default="{ row }">
            <span class="instruction-no">{{ row.instructionNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="指令名称" prop="instructionName" min-width="170" show-overflow-tooltip />
        <el-table-column label="发令单位" prop="issueOrgName" width="140" show-overflow-tooltip />
        <el-table-column label="所属站点" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ resolveStationLabel(row.stationId) || '-' }}</template>
        </el-table-column>
        <el-table-column label="调度内容" prop="instructionContent" width="220" show-overflow-tooltip />
        <el-table-column label="调度方案" width="210">
          <template #default="{ row }">
            <div class="plan-tags">
              <el-tag
                v-for="(name, index) in (row.planNames || []).slice(0, 2)"
                :key="`${row.id}-${index}`"
                effect="light"
              >
                {{ name }}
              </el-tag>
              <el-tooltip
                v-if="(row.planNames || []).length > 2"
                :content="(row.planNames || []).join('，')"
                placement="top"
              >
                <el-tag type="info" effect="plain">+{{ (row.planNames || []).length - 2 }}</el-tag>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发令人" prop="issueUserName" width="110" show-overflow-tooltip />
        <el-table-column label="计划完成时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.plannedFinishTime) }}</template>
        </el-table-column>
        <el-table-column label="接收信息" min-width="220">
          <template #default="{ row }">
            <div>单位：{{ row.receiverDeptName || '-' }}</div>
            <div class="sub-line">接收：{{ row.receiverUserName || '-' }}</div>
            <div class="sub-line">执行：{{ row.executorUserName || row.receiverUserName || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="执行状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="resolveStatusTag(row.status)" effect="light">
              {{ row.statusName || resolveStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="info" @click="openForm('detail', row.id)">详情</el-button>
            <el-button
              v-hasPermi="['iot:dispatch-manage:update']"
              link
              type="primary"
              :disabled="!canEditDelete(row)"
              @click="openForm('update', row.id)"
            >
              编辑
            </el-button>
            <el-button
              v-hasPermi="['iot:dispatch-manage:delete']"
              link
              type="danger"
              :disabled="!canEditDelete(row)"
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

    <Dialog v-model="formVisible" :title="formTitle" width="980px">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        :disabled="formReadonly"
        v-loading="formLoading"
      >
        <section class="form-section">
          <div class="section-title">调度信息</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="指令名称" prop="instructionName">
                <el-input v-model="formData.instructionName" maxlength="120" placeholder="请输入指令名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划完成" prop="plannedFinishTime">
                <el-date-picker
                  v-model="formData.plannedFinishTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  class="!w-full"
                  placeholder="请选择计划完成时间"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="发令单位" prop="issueOrgName">
                <el-input v-model="formData.issueOrgName" maxlength="128" placeholder="请输入发令单位" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="发令人" prop="issueUserName">
                <el-input v-model="formData.issueUserName" maxlength="64" placeholder="请输入发令人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属站点" prop="stationId">
                <el-select
                  v-model="formData.stationId"
                  filterable
                  clearable
                  class="!w-full"
                  placeholder="请选择所属站点"
                  @change="handleStationChange"
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
            <el-col :span="24">
              <el-form-item label="调度内容" prop="instructionContent">
                <el-input
                  v-model="formData.instructionContent"
                  type="textarea"
                  :rows="4"
                  maxlength="2000"
                  show-word-limit
                  placeholder="请输入调度内容"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="调度方案" prop="planIds">
                <el-select v-model="selectedPlanId" filterable class="!w-full" placeholder="请选择调度方案">
                  <el-option
                    v-for="item in planOptions"
                    :key="item.id"
                    :label="item.planName || '-'"
                    :value="item.id"
                  >
                    <div class="plan-option">
                      <span class="plan-option__name">{{ item.planName || '-' }}</span>
                      <el-tag size="small" effect="light" type="primary">
                        {{ resolvePlanTypeLabel(item.planType) }}
                      </el-tag>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="section-title">接收信息</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="接收单位" prop="receiverDeptId">
                <el-select v-model="formData.receiverDeptId" filterable class="!w-full" placeholder="请选择接收单位" @change="handleReceiverDeptChange">
                  <el-option v-for="item in deptOptions" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="接收人" prop="receiverUserId">
                <el-select
                  v-model="formData.receiverUserId"
                  filterable
                  class="!w-full"
                  :placeholder="formData.receiverDeptId ? '请选择接收人' : '请先选择接收单位'"
                  :disabled="!formData.receiverDeptId"
                >
                  <el-option v-for="item in receiverUserOptions" :key="item.id" :label="item.nickname" :value="item.id" />
                </el-select>
                <div class="form-item-tip"></div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="执行人" prop="executorUserId">
                <el-select
                  v-model="formData.executorUserId"
                  filterable
                  class="!w-full"
                  placeholder="请选择执行人"
                >
                  <el-option v-for="item in executorUserOptions" :key="item.id" :label="item.nickname" :value="item.id" />
                </el-select>
                <div class="form-item-tip"></div>
              </el-form-item>
            </el-col>
            <!-- 操作票链接暂未启用，先隐藏 -->
            <!--
            <el-col :span="24">
              <el-form-item label="操作票链接" prop="operationTicketUrl">
                <el-input v-model="formData.operationTicketUrl" maxlength="500" placeholder="请输入操作票链接（选填）" />
              </el-form-item>
            </el-col>
            -->
            <el-col :span="24">
              <el-form-item label="备注" prop="remark">
                <el-input v-model="formData.remark" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="请输入备注（选填）" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button v-if="!formReadonly" type="primary" :loading="formSubmitLoading" @click="submitForm">保存</el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import type { FormInstance, FormRules } from 'element-plus'
import { getSimpleDeptList, type DeptVO } from '@/api/system/dept'
import { useUserStoreWithOut } from '@/store/modules/user'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import {
  DispatchManageApi,
  type DispatchManagePageReqVO,
  type DispatchManagePlanOptionVO,
  type DispatchManageReceiverUserVO,
  type DispatchManageVO
} from '@/api/iot/dispatch/manage'

defineOptions({ name: 'IotDispatchManage' })

const message = useMessage()
const userStore = useUserStoreWithOut()
type OptionItem = { label: string; value: string | number }

const PLAN_TYPE_OPTIONS = [
  { label: '日常调度', value: 'routine' },
  { label: '防洪调度', value: 'flood' },
  { label: '抗旱调度', value: 'drought' },
  { label: '生态补水', value: 'eco_replenishment' },
  { label: '应急调度', value: 'emergency' }
]
const ADMIN_ROLE_CODES = ['super_admin', 'tenant_admin']

const STATUS_OPTIONS = [
  { label: '待执行', value: 1, tag: 'warning' },
  { label: '已执行', value: 2, tag: 'success' },
  { label: '已逾期', value: 3, tag: 'danger' }
]

const queryFormRef = ref<FormInstance>()
const loading = ref(false)
const exportLoading = ref(false)
const total = ref(0)
const list = ref<DispatchManageVO[]>([])
const plannedFinishTimeRange = ref<string[]>([])
const queryParams = reactive<DispatchManagePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  stationId: undefined,
  instructionNo: undefined,
  issueOrgName: undefined,
  planName: undefined,
  status: undefined,
  plannedFinishTime: undefined
})

const planOptions = ref<DispatchManagePlanOptionVO[]>([])
const deptOptions = ref<DeptVO[]>([])
const receiverUserOptions = ref<DispatchManageReceiverUserVO[]>([])
const executorUserOptions = ref<DispatchManageReceiverUserVO[]>([])
const stationOptions = computed<OptionItem[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const formVisible = ref(false)
const formLoading = ref(false)
const formSubmitLoading = ref(false)
const formType = ref<'create' | 'update' | 'detail'>('create')
const formRef = ref<FormInstance>()
const formData = reactive<DispatchManageVO>({
  id: undefined,
  stationId: undefined,
  instructionName: '',
  issueOrgName: '',
  issueUserName: '',
  instructionContent: '',
  planIds: [],
  planNames: [],
  plannedFinishTime: '',
  receiverDeptId: undefined,
  receiverUserId: undefined,
  executorUserId: undefined,
  operationTicketUrl: '',
  remark: ''
})

const formRules: FormRules<DispatchManageVO> = {
  instructionName: [{ required: true, message: '指令名称不能为空', trigger: 'blur' }],
  stationId: [{ required: true, message: '所属站点不能为空', trigger: 'change' }],
  issueUserName: [{ required: true, message: '发令人不能为空', trigger: 'blur' }],
  instructionContent: [{ required: true, message: '调度内容不能为空', trigger: 'blur' }],
  planIds: [{ required: true, type: 'array', min: 1, message: '请选择调度方案', trigger: 'change' }],
  plannedFinishTime: [{ required: true, message: '计划完成时间不能为空', trigger: 'change' }],
  receiverDeptId: [{ required: true, message: '接收单位不能为空', trigger: 'change' }],
  receiverUserId: [{ required: true, message: '接收人不能为空', trigger: 'change' }],
  executorUserId: [{ required: true, message: '执行人不能为空', trigger: 'change' }]
}

const formTitle = computed(() => {
  if (formType.value === 'create') return '新增调度指令'
  if (formType.value === 'update') return '编辑调度指令'
  return '调度指令详情'
})
const formReadonly = computed(() => formType.value === 'detail')
const selectedPlanId = computed<number | undefined>({
  get: () => formData.planIds?.[0],
  set: (value) => {
    formData.planIds = value ? [value] : []
  }
})
const hasAdminRole = computed(() => userStore.getRoles.some((role) => ADMIN_ROLE_CODES.includes(role)))

const resolveStatusName = (status?: number) => {
  return STATUS_OPTIONS.find((item) => item.value === Number(status))?.label || '-'
}

const resolvePlanTypeLabel = (planType?: string) => {
  return PLAN_TYPE_OPTIONS.find((item) => item.value === planType)?.label || planType || '未知类型'
}

const resolveStationLabel = (stationId?: string) => {
  if (!stationId) return ''
  return stationOptions.value.find((item) => String(item.value) === String(stationId))?.label || stationId
}

const resolveStatusTag = (status?: number) => {
  return STATUS_OPTIONS.find((item) => item.value === Number(status))?.tag || 'info'
}

const formatDateTime = (value?: string | number) => {
  if (!value) return '-'
  return dayjs(value).isValid() ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : String(value)
}

const normalizeDateTimeValue = (value?: string | number) => {
  if (value === undefined || value === null || value === '') return ''
  return dayjs(value).isValid() ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : String(value)
}

const canEditDelete = (row: DispatchManageVO) => {
  return hasAdminRole.value && Number(row.status) !== 2
}

const buildQueryParams = (): DispatchManagePageReqVO => {
  return {
    ...queryParams,
    plannedFinishTime: plannedFinishTimeRange.value?.length ? plannedFinishTimeRange.value : undefined
  }
}

const resetFormData = () => {
  Object.assign(formData, {
    id: undefined,
    stationId: undefined,
    instructionName: '',
    issueOrgName: '',
    issueUserName: '',
    instructionContent: '',
    planIds: [],
    planNames: [],
    plannedFinishTime: '',
    receiverDeptId: undefined,
    receiverUserId: undefined,
    executorUserId: undefined,
    operationTicketUrl: '',
    remark: ''
  })
  receiverUserOptions.value = []
  executorUserOptions.value = []
}

const getList = async () => {
  loading.value = true
  try {
    const data = await DispatchManageApi.getPage(buildQueryParams())
    list.value = data.list || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

const appendCurrentPlanOption = () => {
  const planId = formData.planIds?.[0]
  if (!planId || planOptions.value.some((item) => item.id === planId)) {
    return
  }
  const planName = formData.planNames?.[0] || `方案#${planId}`
  planOptions.value = [
    {
      id: planId,
      planNo: '',
      planName,
      planType: undefined
    },
    ...planOptions.value
  ]
}

const getPlanOptions = async (stationId?: string) => {
  planOptions.value = await DispatchManageApi.getPlanOptions(0, stationId ? String(stationId) : undefined)
  appendCurrentPlanOption()
}

const handleStationChange = async () => {
  formData.planIds = []
  formData.planNames = []
  await getPlanOptions(formData.stationId)
}

const getDeptOptions = async () => {
  const deptList = await getSimpleDeptList()
  deptOptions.value = (deptList || []).filter((item) => (item.name || '').trim() !== '群众部门')
}

const getReceiverUserOptions = async (deptId: number | undefined) => {
  if (!deptId) {
    receiverUserOptions.value = []
    return
  }
  receiverUserOptions.value = await DispatchManageApi.getReceiverUserList(deptId)
}

const getExecutorUserOptions = async () => {
  executorUserOptions.value = await DispatchManageApi.getExecutorUserList()
}

const handleReceiverDeptChange = async () => {
  await getReceiverUserOptions(formData.receiverDeptId)
  if (!receiverUserOptions.value.some((item) => item.id === formData.receiverUserId)) {
    formData.receiverUserId = undefined
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  plannedFinishTimeRange.value = []
  await handleQuery()
}

const openForm = async (type: 'create' | 'update' | 'detail', id?: number) => {
  formType.value = type
  formVisible.value = true
  formLoading.value = true
  resetFormData()
  formRef.value?.resetFields()
  try {
    await getExecutorUserOptions()
    if (!id) {
      await getPlanOptions()
      return
    }
    const data = await DispatchManageApi.get(id)
    Object.assign(formData, {
      id: data.id,
      stationId: data.stationId,
      instructionName: data.instructionName || '',
      issueOrgName: data.issueOrgName || '',
      issueUserName: data.issueUserName || '',
      instructionContent: data.instructionContent || '',
      planIds: (data.planIds || []).slice(0, 1),
      planNames: (data.planNames || []).slice(0, 1),
      plannedFinishTime: normalizeDateTimeValue(data.plannedFinishTime),
      receiverDeptId: data.receiverDeptId,
      receiverUserId: data.receiverUserId,
      executorUserId: data.executorUserId || data.receiverUserId,
      operationTicketUrl: data.operationTicketUrl || '',
      remark: data.remark || ''
    })
    await getPlanOptions(formData.stationId)
    await getReceiverUserOptions(formData.receiverDeptId)
  } finally {
    formLoading.value = false
  }
}

const submitForm = async () => {
  await formRef.value?.validate()
  formSubmitLoading.value = true
  try {
    const payload: DispatchManageVO = {
      id: formData.id,
      stationId: formData.stationId,
      instructionName: formData.instructionName || '',
      issueOrgName: formData.issueOrgName || '',
      issueUserName: formData.issueUserName || '',
      instructionContent: formData.instructionContent || '',
      planIds: (formData.planIds || []).slice(0, 1),
      plannedFinishTime: formData.plannedFinishTime || '',
      receiverDeptId: formData.receiverDeptId,
      receiverUserId: formData.receiverUserId,
      executorUserId: formData.executorUserId,
      operationTicketUrl: formData.operationTicketUrl || '',
      remark: formData.remark || ''
    }
    if (formType.value === 'create') {
      await DispatchManageApi.create(payload)
      message.success('新增成功')
    } else {
      await DispatchManageApi.update(payload)
      message.success('更新成功')
    }
    formVisible.value = false
    await getList()
  } finally {
    formSubmitLoading.value = false
  }
}

const handleDelete = async (id?: number) => {
  if (!id) return
  await message.delConfirm('确认删除该调度指令吗？')
  await DispatchManageApi.remove(id)
  message.success('删除成功')
  await getList()
}

const handleExport = async () => {
  await message.exportConfirm()
  exportLoading.value = true
  try {
    const data = await DispatchManageApi.exportExcel(buildQueryParams())
    download.excel(data, `调度管理_${dayjs().format('YYYYMMDD_HHmmss')}.xls`)
  } finally {
    exportLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([getPlanOptions(), getDeptOptions()])
  await getList()
})
</script>

<style scoped lang="scss">
.dispatch-manage-page {
  .query-wrap {
    border: 1px solid #e6ebf5;
    border-radius: 12px;
  }

  .table-wrap {
    border-radius: 12px;
  }

  .instruction-no {
    display: inline-flex;
    align-items: center;
    height: 24px;
    padding: 0 10px;
    border: 1px solid #dbeafe;
    border-radius: 999px;
    background: linear-gradient(135deg, #eff6ff, #eef2ff);
    color: #1d4ed8;
    font-size: 12px;
    font-weight: 600;
  }

  .plan-tags {
    display: flex;
    gap: 6px;
    align-items: center;
    flex-wrap: wrap;
  }

  .plan-option {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    width: 100%;
  }

  .plan-option__name {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .sub-line {
    margin-top: 2px;
    color: #6b7280;
    font-size: 12px;
  }

  .form-section {
    padding: 14px 14px 2px;
    border: 1px solid #ecf0f7;
    border-radius: 10px;
    margin-bottom: 14px;
    background: #fcfdff;
  }

  .section-title {
    margin-bottom: 12px;
    color: #334155;
    font-size: 14px;
    font-weight: 600;
  }

  .form-item-tip {
    margin-top: 6px;
    color: #64748b;
    font-size: 12px;
    line-height: 18px;
  }
}
</style>
