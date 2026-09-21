<template>
  <div class="shift-handover-page">
    <ContentWrap class="query-wrap">
      <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
        <el-form-item label="交接日期" prop="handoverDateRange">
          <el-date-picker
            v-model="queryDateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="~"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="!w-260px"
          />
        </el-form-item>
        <el-form-item label="交班人" prop="handoverUserId">
          <el-select
            v-model="queryParams.handoverUserId"
            clearable
            filterable
            class="!w-220px"
            placeholder="请选择交班人"
          >
            <el-option
              v-for="user in userOptions"
              :key="String(user.id)"
              :label="buildUserLabel(user)"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="值班班组" prop="teamId">
          <el-select
            v-model="queryParams.teamId"
            clearable
            filterable
            class="!w-220px"
            placeholder="请选择值班班组"
          >
            <el-option
              v-for="team in teamOptions"
              :key="team.id"
              :label="team.teamName || '-'"
              :value="team.id"
            />
          </el-select>
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
          <el-button v-hasPermi="['iot:shift-handover:create']" type="primary" plain @click="openForm('create')">
            <Icon icon="ep:plus" class="mr-4px" />
            提交值班日志
          </el-button>
          <el-button
            v-hasPermi="['iot:shift-handover:export']"
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
        <el-table-column type="index" label="序号" width="70" />
        <el-table-column label="交接班编号" prop="handoverNo" min-width="150" show-overflow-tooltip />
        <el-table-column label="交接班时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.handoverTime) }}</template>
        </el-table-column>
        <el-table-column label="值班班次" prop="shiftName" min-width="120" show-overflow-tooltip />
        <el-table-column label="值班班组" prop="teamName" min-width="120" show-overflow-tooltip />
        <el-table-column label="交班人" prop="handoverUserName" min-width="110" />
        <el-table-column label="接班人" prop="takeoverUserName" min-width="110" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="resolveStatusType(row.status)">{{ resolveStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="info" @click="openForm('detail', row.id)">详情</el-button>
            <el-button v-hasPermi="['iot:shift-handover:update']" link type="primary" @click="openForm('update', row.id)">
              编辑
            </el-button>
            <el-button v-hasPermi="['iot:shift-handover:delete']" link type="danger" @click="handleDelete(row.id)">
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

    <Dialog v-model="formVisible" :title="formTitle" width="880px">
      <el-form
        ref="formRef"
        v-loading="formLoading"
        :model="formData"
        :rules="formRules"
        :disabled="formReadonly"
        label-width="128px"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="交接班编号">
              <el-input :model-value="formData.handoverNo || '系统自动生成'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交接班时间" prop="handoverTime">
              <el-date-picker
                v-model="formData.handoverTime"
                class="!w-full"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择交接班时间"
                :disabled="formReadonly || lockHandoverCoreFields"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="值班班次">
              <el-input :model-value="formData.shiftName || '-'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="值班班组">
              <el-input :model-value="formData.teamName || '-'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交班人" prop="handoverUserId">
              <el-select
                v-model="formData.handoverUserId"
                class="!w-full"
                filterable
                clearable
                placeholder="请选择交班人"
                :disabled="formReadonly || lockHandoverCoreFields"
              >
                <el-option
                  v-for="user in userOptions"
                  :key="String(user.id)"
                  :label="buildUserLabel(user)"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              class="takeover-user-form-item"
              label="接班人(下班组长)"
              label-width="150px"
              prop="takeoverUserId"
            >
              <el-select
                v-model="formData.takeoverUserId"
                class="!w-full"
                filterable
                clearable
                placeholder="系统默认下一个班组长"
                :disabled="formReadonly || lockHandoverCoreFields"
              >
                <el-option
                  v-for="user in userOptions"
                  :key="String(user.id)"
                  :label="buildUserLabel(user)"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="值班日志" prop="dutyLog">
              <el-input
                v-model="formData.dutyLog"
                type="textarea"
                :rows="3"
                maxlength="2000"
                show-word-limit
                placeholder="请填写当班值班日志"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="待关注事项" prop="pendingItems">
              <el-input
                v-model="formData.pendingItems"
                type="textarea"
                :rows="3"
                maxlength="2000"
                show-word-limit
                placeholder="请填写待关注事项"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="关联调度任务" prop="dispatchInstructionId">
              <el-select
                v-model="formData.dispatchInstructionId"
                filterable
                remote
                reserve-keyword
                clearable
                class="!w-full"
                placeholder="请选择调度编号/指令名称"
                :loading="dispatchOptionLoading"
                :remote-method="loadDispatchOptions"
              >
                <el-option
                  v-for="item in dispatchOptions"
                  :key="item.id"
                  :label="buildDispatchOptionLabel(item)"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="formData.dispatchInstructionId" :span="24">
            <el-form-item label="关联运行日志">
              <div class="linked-log-panel">
                <div class="linked-log-panel__head">
                  <span>已关联调度任务下的运行日志</span>
                  <el-button link type="primary" @click="loadLinkedRunLogs">刷新</el-button>
                </div>
                <el-table
                  v-loading="linkedRunLogLoading"
                  :data="linkedRunLogs"
                  border
                  size="small"
                  max-height="240"
                  empty-text="该调度任务下暂无运行日志"
                >
                  <el-table-column label="日志编号" prop="logNo" min-width="140" show-overflow-tooltip />
                  <el-table-column label="值班班组" prop="dutyTeamName" min-width="120" show-overflow-tooltip />
                  <el-table-column label="记录时间" min-width="170">
                    <template #default="{ row }">{{ formatDateTime(row.recordTime) }}</template>
                  </el-table-column>
                  <el-table-column label="设备名称" prop="deviceName" min-width="130" show-overflow-tooltip />
                  <el-table-column label="操作" width="90" fixed="right">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="openRunLogDetail(row)">查看</el-button>
                    </template>
                  </el-table-column>
                </el-table>
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

    <Dialog v-model="runLogDetailVisible" title="运行日志详情" width="760px">
      <el-descriptions v-loading="runLogDetailLoading" :column="2" border size="small" class="run-log-detail">
        <el-descriptions-item label="日志编号">{{ runLogDetail?.logNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ runLogDetail?.taskName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="值班班组">{{ runLogDetail?.dutyTeamName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="记录人">{{ runLogDetail?.recorderUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="记录时间">{{ formatDateTime(runLogDetail?.recordTime) }}</el-descriptions-item>
        <el-descriptions-item label="检查时段">{{ runLogDetail?.checkPeriod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ runLogDetail?.deviceName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联调度">{{ runLogDetail?.dispatchInstructionNo || '-' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="运行参数">{{ runLogDetail?.runParamsText || '-' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="事件描述">{{ runLogDetail?.eventDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="备注">{{ runLogDetail?.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="runLogDetailVisible = false">关闭</el-button>
      </template>
    </Dialog>

    <Dialog v-model="reminderVisible" title="交接班提醒" width="520px" @close="handleReminderClose">
      <div class="reminder-box">
        <p class="reminder-box__message">{{ reminderData?.message || '当前已进入交接班提醒窗口。' }}</p>
        <p class="reminder-box__tip">
          提示：仅支持系统自动匹配交接班次提交，接班人默认取下一班班组长。
        </p>
        <div class="reminder-box__item">
          <span>交班人：</span>
          <span>{{ reminderData?.handoverDefault?.handoverUserName || '-' }}</span>
        </div>
        <div class="reminder-box__item">
          <span>接班人：</span>
          <span>{{ reminderData?.handoverDefault?.takeoverUserName || '-' }}</span>
        </div>
        <div class="reminder-box__item">
          <span>下一班次开始：</span>
          <span>{{ formatDateTime(reminderData?.handoverDefault?.nextShiftStartTime) }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="handleReminderClose">稍后处理</el-button>
        <el-button type="primary" @click="openCreateByReminder">提交值班日志</el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import * as UserApi from '@/api/system/user'
import type { UserVO } from '@/api/system/user'
import {
  ShiftHandoverApi,
  type ShiftHandoverDefaultRespVO,
  type ShiftHandoverPageReqVO,
  type ShiftHandoverReminderRespVO,
  type ShiftHandoverVO
} from '@/api/iot/shift/handover'
import {
  RunLogApi,
  type RunLogDispatchOptionVO,
  type RunLogVO
} from '@/api/iot/run/log'
import { DispatchReceiveApi } from '@/api/iot/dispatch/receive'
import { ShiftScheduleApi, type ShiftScheduleTeamOptionVO } from '@/api/iot/shift/schedule'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'IotShiftHandover' })

type FormMode = 'create' | 'update' | 'detail'

const message = useMessage()
const loading = ref(false)
const exportLoading = ref(false)
const list = ref<ShiftHandoverVO[]>([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()
const queryDateRange = ref<string[]>([])
const queryParams = reactive<ShiftHandoverPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  keyword: undefined,
  teamId: undefined,
  shiftId: undefined,
  handoverUserId: undefined,
  takeoverUserId: undefined,
  status: undefined,
  handoverDateRange: undefined
})

const teamOptions = ref<ShiftScheduleTeamOptionVO[]>([])
const userOptions = ref<UserVO[]>([])
const dispatchOptions = ref<RunLogDispatchOptionVO[]>([])
const dispatchOptionLoading = ref(false)
const linkedRunLogs = ref<RunLogVO[]>([])
const linkedRunLogLoading = ref(false)
const runLogDetailVisible = ref(false)
const runLogDetailLoading = ref(false)
const runLogDetail = ref<RunLogVO>()

const buildUserLabel = (user?: UserVO | null) => {
  if (!user) return '-'
  return user.nickname || user.username || String(user.id)
}

const findUserName = (userId?: number) => {
  if (!userId) return ''
  const target = userOptions.value.find((item) => Number(item.id) === Number(userId))
  return target ? buildUserLabel(target) : ''
}

const buildDispatchOptionLabel = (option?: RunLogDispatchOptionVO | null) => {
  if (!option) return '-'
  const instructionNo = String(option.instructionNo || '').trim()
  const instructionName = String(option.instructionName || '').trim()
  if (instructionNo && instructionName) return `${instructionNo} / ${instructionName}`
  return instructionNo || instructionName || option.title || `调令#${option.id}`
}

// Long ID 统一转字符串比较，避免 Number 强转导致精度丢失
const normalizeId = (value?: string | number | null) =>
  value === undefined || value === null || value === '' ? '' : String(value)

const formatDateTime = (value?: string | number) => {
  if (value === undefined || value === null || value === '') return '-'
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : String(value)
}

const pad2 = (value: number) => String(value).padStart(2, '0')

const normalizeFormDateTime = (value?: string | number | null | unknown) => {
  if (value === undefined || value === null || value === '') return ''
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value.map((item) => Number(item))
    if ([year, month, day, hour, minute, second].every((item) => Number.isFinite(item))) {
      return `${year}-${pad2(month)}-${pad2(day)} ${pad2(hour)}:${pad2(minute)}:${pad2(second)}`
    }
  }
  if (typeof value === 'object') {
    const raw = value as Record<string, unknown>
    const year = Number(raw.year)
    const month = Number(raw.monthValue ?? raw.month)
    const day = Number(raw.dayOfMonth ?? raw.day)
    const hour = Number(raw.hour ?? 0)
    const minute = Number(raw.minute ?? 0)
    const second = Number(raw.second ?? 0)
    if ([year, month, day, hour, minute, second].every((item) => Number.isFinite(item))) {
      return `${year}-${pad2(month)}-${pad2(day)} ${pad2(hour)}:${pad2(minute)}:${pad2(second)}`
    }
  }
  const parsed = dayjs(value)
  if (parsed.isValid()) {
    return parsed.format('YYYY-MM-DD HH:mm:ss')
  }
  const raw = String(value).trim()
  if (!raw) return ''
  return raw.replace('T', ' ').slice(0, 19)
}

const resolveStatusName = (status?: number) => (status === 1 ? '已交接' : '待交接')
const resolveStatusType = (status?: number) => (status === 1 ? 'success' : 'warning')

const buildPageParams = (): ShiftHandoverPageReqVO => ({
  ...queryParams,
  handoverDateRange: queryDateRange.value?.length === 2 ? queryDateRange.value : undefined
})

const getList = async () => {
  loading.value = true
  try {
    const data = await ShiftHandoverApi.getPage(buildPageParams())
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
  queryDateRange.value = []
  queryParams.pageNo = 1
  await getList()
}

const handleDelete = async (id?: number) => {
  if (!id) return
  await message.delConfirm('确认删除该交接班记录吗？')
  await ShiftHandoverApi.remove(id)
  message.success('删除成功')
  await getList()
}

const handleExport = async () => {
  await message.exportConfirm()
  exportLoading.value = true
  try {
    const data = await ShiftHandoverApi.exportExcel(buildPageParams())
    download.excel(data, `交接班记录_${dayjs().format('YYYYMMDD_HHmmss')}.xls`)
  } finally {
    exportLoading.value = false
  }
}

const formVisible = ref(false)
const formMode = ref<FormMode>('create')
const formRef = ref<FormInstance>()
const formLoading = ref(false)
const submitLoading = ref(false)

const createEmptyFormData = (): ShiftHandoverVO => ({
  id: undefined,
  handoverNo: '',
  scheduleId: undefined,
  handoverTime: '',
  shiftId: undefined,
  shiftName: '',
  teamId: undefined,
  teamName: '',
  handoverUserId: undefined,
  handoverUserName: '',
  takeoverUserId: undefined,
  takeoverUserName: '',
  dutyLog: '',
  pendingItems: '',
  dispatchInstructionId: undefined,
  dispatchInstructionNo: '',
  dispatchInstructionName: '',
  defectTicketFlag: '',
  remark: ''
})
const formData = reactive<ShiftHandoverVO>(createEmptyFormData())

const formReadonly = computed(() => formMode.value === 'detail')
const lockHandoverCoreFields = computed(() => formMode.value === 'create' || formMode.value === 'update')
const handoverCoreFieldSnapshot = ref<
  Pick<ShiftHandoverVO, 'handoverTime' | 'handoverUserId' | 'takeoverUserId'>
>({})
const formTitle = computed(() => {
  if (formMode.value === 'create') return '提交值班日志'
  if (formMode.value === 'update') return '编辑交接班'
  return '交接班详情'
})

const formRules: FormRules<ShiftHandoverVO> = {
  handoverTime: [{ required: true, message: '交接班时间不能为空', trigger: 'change' }],
  handoverUserId: [{ required: true, message: '交班人不能为空', trigger: 'change' }],
  takeoverUserId: [{ required: true, message: '接班人不能为空', trigger: 'change' }],
  dutyLog: [{ required: true, message: '值班日志不能为空', trigger: 'blur' }],
  pendingItems: [{ required: true, message: '待关注事项不能为空', trigger: 'blur' }]
}

watch(
  () => formData.handoverUserId,
  (value) => {
    formData.handoverUserName = findUserName(value)
  }
)

watch(
  () => formData.takeoverUserId,
  (value) => {
    formData.takeoverUserName = findUserName(value)
  }
)

watch(
  () => formData.dispatchInstructionId,
  (value) => {
    if (!value) {
      formData.dispatchInstructionNo = ''
      formData.dispatchInstructionName = ''
      linkedRunLogs.value = []
      return
    }
    const target = dispatchOptions.value.find((item) => normalizeId(item.id) === normalizeId(value))
    if (target) {
      formData.dispatchInstructionNo = target.instructionNo || ''
      formData.dispatchInstructionName = target.instructionName || ''
    }
    if (formVisible.value) {
      void loadLinkedRunLogs()
    }
  }
)

const resetFormData = () => {
  Object.assign(formData, createEmptyFormData())
  handoverCoreFieldSnapshot.value = {}
  linkedRunLogs.value = []
  runLogDetail.value = undefined
  runLogDetailVisible.value = false
  formRef.value?.clearValidate()
}

const loadDispatchOptions = async (keyword?: string) => {
  dispatchOptionLoading.value = true
  try {
    const options = await RunLogApi.getDispatchOptions(keyword)
    dispatchOptions.value = options || []
  } finally {
    dispatchOptionLoading.value = false
  }
}

const ensureDispatchOptionEcho = () => {
  if (!formData.dispatchInstructionId) return
  const existed = dispatchOptions.value.find((item) => normalizeId(item.id) === normalizeId(formData.dispatchInstructionId))
  if (existed) return
  const instructionNo = formData.dispatchInstructionNo || ''
  const instructionName = formData.dispatchInstructionName || ''
  dispatchOptions.value.unshift({
    id: formData.dispatchInstructionId,
    instructionNo,
    instructionName,
    title: instructionNo || instructionName ? `${instructionNo} / ${instructionName}` : `调令#${formData.dispatchInstructionId}`
  })
}

const mergeRunLogs = (primary: RunLogVO[], secondary: RunLogVO[]) => {
  const merged = new Map<string, RunLogVO>()
  ;[...(primary || []), ...(secondary || [])].forEach((item) => {
    if (!item) return
    const key = normalizeId(item.id) || `logNo:${String(item.logNo || '')}`
    if (!key) return
    merged.set(key, item)
  })
  return Array.from(merged.values()).sort((a, b) => {
    const aTime = a?.recordTime ? dayjs(a.recordTime).valueOf() : 0
    const bTime = b?.recordTime ? dayjs(b.recordTime).valueOf() : 0
    return bTime - aTime
  })
}

// 参考调令接收详情逻辑：优先按调令接收回填的 runLogIds 展示，避免出现“未关联却查出日志”
const loadRunLogsByDispatchReceive = async (dispatchInstructionId: string | number) => {
  try {
    const receive = await DispatchReceiveApi.get(dispatchInstructionId)
    const runLogIds = (receive?.runLogIds || []).filter((id) => !!normalizeId(id))
    if (!runLogIds.length) {
      return {
        matchedReceive: true,
        logs: [] as RunLogVO[]
      }
    }
    const details = await Promise.allSettled(runLogIds.map((id) => RunLogApi.get(id)))
    return {
      matchedReceive: true,
      logs: details
        .filter((item): item is PromiseFulfilledResult<RunLogVO> => item.status === 'fulfilled')
        .map((item) => item.value)
    }
  } catch {
    return {
      matchedReceive: false,
      logs: [] as RunLogVO[]
    }
  }
}

const loadLinkedRunLogs = async () => {
  if (!formData.dispatchInstructionId) {
    linkedRunLogs.value = []
    return
  }
  linkedRunLogLoading.value = true
  try {
    const dispatchInstructionId = formData.dispatchInstructionId
    const receiveResult = await loadRunLogsByDispatchReceive(dispatchInstructionId)
    if (receiveResult.matchedReceive) {
      linkedRunLogs.value = mergeRunLogs(receiveResult.logs, [])
      return
    }

    const data = await RunLogApi.getPage({
      pageNo: 1,
      pageSize: 100,
      dispatchInstructionId
    })
    linkedRunLogs.value = mergeRunLogs(data?.list || [], [])
  } finally {
    linkedRunLogLoading.value = false
  }
}

const openRunLogDetail = async (row: RunLogVO) => {
  if (!row?.id) return
  runLogDetailVisible.value = true
  runLogDetailLoading.value = true
  try {
    runLogDetail.value = await RunLogApi.get(row.id)
  } finally {
    runLogDetailLoading.value = false
  }
}

const fillFormByDefault = (defaultData?: ShiftHandoverDefaultRespVO | null) => {
  if (!defaultData?.scheduleId) return false
  Object.assign(formData, {
    ...createEmptyFormData(),
    scheduleId: defaultData.scheduleId,
    shiftId: defaultData.shiftId,
    shiftName: defaultData.shiftName || '',
    teamId: defaultData.teamId,
    teamName: defaultData.teamName || '',
    // 交班人按当前班组长、接班人按下一班组长自动回填（创建态不允许手动修改）
    handoverUserId: defaultData.handoverUserId,
    handoverUserName: defaultData.handoverUserName || findUserName(defaultData.handoverUserId),
    takeoverUserId: defaultData.takeoverUserId,
    takeoverUserName: defaultData.takeoverUserName || findUserName(defaultData.takeoverUserId),
    handoverTime: normalizeFormDateTime(defaultData.handoverTime)
  })
  handoverCoreFieldSnapshot.value = {
    handoverTime: formData.handoverTime,
    handoverUserId: formData.handoverUserId,
    takeoverUserId: formData.takeoverUserId
  }
  return true
}

const openForm = async (mode: FormMode, id?: number) => {
  formMode.value = mode
  formVisible.value = true
  resetFormData()

  if (mode === 'create') {
    formLoading.value = true
    try {
      const [defaults] = await Promise.all([ShiftHandoverApi.getDefaults(), loadDispatchOptions()])
      const filled = fillFormByDefault(defaults)
      if (!filled) {
        formVisible.value = false
        message.warning('当前不可提交值班日志，请检查当前值班、下一排班及提交时间窗口约束')
        return
      }
      await loadLinkedRunLogs()
    } catch {
      formVisible.value = false
    } finally {
      formLoading.value = false
    }
    return
  }

  if (!id) return
  formLoading.value = true
  try {
    await loadDispatchOptions()
    const data = await ShiftHandoverApi.get(id)
    Object.assign(formData, {
      ...createEmptyFormData(),
      ...data,
      handoverTime: normalizeFormDateTime(data.handoverTime)
    })
    handoverCoreFieldSnapshot.value = {
      handoverTime: formData.handoverTime,
      handoverUserId: formData.handoverUserId,
      takeoverUserId: formData.takeoverUserId
    }
    ensureDispatchOptionEcho()
    await loadLinkedRunLogs()
  } finally {
    formLoading.value = false
  }
}

const submitForm = async () => {
  if (formReadonly.value) return
  await formRef.value?.validate()
  if (!formData.scheduleId) {
    message.warning('未匹配到交班排班，无法提交')
    return
  }
  submitLoading.value = true
  try {
    const payload: ShiftHandoverVO = {
      id: formData.id,
      scheduleId: formData.scheduleId,
      handoverUserId: formData.handoverUserId,
      takeoverUserId: formData.takeoverUserId,
      handoverTime: formData.handoverTime,
      dutyLog: String(formData.dutyLog || '').trim(),
      pendingItems: String(formData.pendingItems || '').trim(),
      dispatchInstructionId: formData.dispatchInstructionId,
      dispatchInstructionNo: String(formData.dispatchInstructionNo || '').trim(),
      dispatchInstructionName: String(formData.dispatchInstructionName || '').trim(),
      remark: String(formData.remark || '').trim()
    }
    if (formMode.value === 'create') {
      delete payload.id
      await ShiftHandoverApi.create(payload)
      message.success('提交成功')
    } else {
      payload.handoverTime = handoverCoreFieldSnapshot.value.handoverTime ?? formData.handoverTime
      payload.handoverUserId = handoverCoreFieldSnapshot.value.handoverUserId ?? formData.handoverUserId
      payload.takeoverUserId = handoverCoreFieldSnapshot.value.takeoverUserId ?? formData.takeoverUserId
      await ShiftHandoverApi.update(payload)
      message.success('更新成功')
    }
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const reminderVisible = ref(false)
const reminderData = ref<ShiftHandoverReminderRespVO>()
const dismissedReminderKeys = new Set<string>()
const reminderTimer = ref<number>()
const currentReminderKey = ref('')

const buildReminderKey = (defaultData?: ShiftHandoverDefaultRespVO) => {
  if (!defaultData?.scheduleId) return ''
  return `${defaultData.scheduleId}_${defaultData.nextShiftStartTime || ''}`
}

const pollReminder = async () => {
  try {
    const data = await ShiftHandoverApi.getReminder()
    const defaultData = data?.handoverDefault
    if (!data?.needRemind || !defaultData?.scheduleId) return
    const key = buildReminderKey(defaultData)
    if (!key || dismissedReminderKeys.has(key)) return
    if (formVisible.value && formMode.value === 'create') return
    reminderData.value = data
    currentReminderKey.value = key
    reminderVisible.value = true
  } catch {
    // 提醒接口异常不阻塞页面主流程
  }
}

const handleReminderClose = () => {
  if (currentReminderKey.value) {
    dismissedReminderKeys.add(currentReminderKey.value)
  }
  reminderVisible.value = false
}

const openCreateByReminder = async () => {
  const defaultData = reminderData.value?.handoverDefault
  if (!defaultData?.scheduleId) {
    message.warning('未获取到可交接班次')
    return
  }
  reminderVisible.value = false
  formMode.value = 'create'
  formVisible.value = true
  resetFormData()
  await loadDispatchOptions()
  fillFormByDefault(defaultData)
  await loadLinkedRunLogs()
}

const loadBaseOptions = async () => {
  const [teams, users] = await Promise.all([
    ShiftScheduleApi.getTeamOptions(),
    UserApi.getSimpleUserList({ excludeRoleName: '游客' })
  ])
  teamOptions.value = teams || []
  userOptions.value = users || []
}

onMounted(async () => {
  await loadBaseOptions()
  await getList()
  await pollReminder()
  reminderTimer.value = window.setInterval(() => {
    void pollReminder()
  }, 60_000)
})

onBeforeUnmount(() => {
  if (reminderTimer.value) {
    window.clearInterval(reminderTimer.value)
    reminderTimer.value = undefined
  }
})
</script>

<style scoped lang="scss">
.shift-handover-page {
  .query-wrap {
    border: 1px solid rgba(148, 163, 184, 0.28);
    border-radius: 12px;
    background: #fff;
    box-shadow: 0 8px 22px rgba(15, 23, 42, 0.06);
  }

  .table-wrap {
    margin-top: 12px;
  }

  .linked-log-panel {
    width: 100%;
    border: 1px solid rgba(148, 163, 184, 0.3);
    border-radius: 10px;
    padding: 10px 12px 12px;
    background: #f8fafc;

    &__head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      color: #1e293b;
      font-size: 13px;
      font-weight: 600;
    }
  }

  .run-log-detail {
    :deep(.el-descriptions__label) {
      width: 130px;
    }
  }

  .takeover-user-form-item {
    :deep(.el-form-item__label) {
      white-space: nowrap;
    }
  }

  .reminder-box {
    padding: 4px 4px 0;

    &__message {
      margin: 0 0 12px;
      color: #1e293b;
      font-size: 14px;
      line-height: 22px;
    }

    &__tip {
      margin: 0 0 12px;
      padding: 8px 10px;
      border-radius: 8px;
      background: rgba(59, 130, 246, 0.08);
      color: #1d4ed8;
      font-size: 13px;
      line-height: 20px;
    }

    &__item {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #334155;
      margin-bottom: 8px;
      font-size: 14px;
    }
  }
}
</style>

