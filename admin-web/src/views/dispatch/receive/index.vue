<template>
  <div class="dispatch-receive-page">
    <ContentWrap class="query-wrap">
      <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="90px">
        <el-form-item label="发令单位" prop="issueOrgName">
          <el-input
            v-model="queryParams.issueOrgName"
            clearable
            class="!w-200px"
            placeholder="请输入发令单位"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="执行情况" prop="executionStatus">
          <el-select v-model="queryParams.executionStatus" clearable class="!w-160px" placeholder="请选择执行情况">
            <el-option
              v-for="item in EXECUTION_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调度编号" prop="instructionNo">
          <el-input
            v-model="queryParams.instructionNo"
            clearable
            class="!w-200px"
            placeholder="请输入调度编号"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="指令名称" prop="instructionName">
          <el-input
            v-model="queryParams.instructionName"
            clearable
            class="!w-200px"
            placeholder="请输入指令名称"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="方案名称" prop="planName">
          <el-input
            v-model="queryParams.planName"
            clearable
            class="!w-200px"
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
        <el-form-item label="下发时间" prop="issueTime">
          <el-date-picker
            v-model="issueTimeRange"
            type="daterange"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="!w-300px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
          <el-button
            v-hasPermi="['iot:dispatch-receive:export']"
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
        <el-table-column label="序号" type="index" width="70" align="center" />
        <el-table-column label="发令单位" prop="issueOrgName" width="170" show-overflow-tooltip />
        <el-table-column label="调度编号" prop="instructionNo" width="150" show-overflow-tooltip />
        <el-table-column label="指令名称" prop="instructionName" min-width="170" show-overflow-tooltip />
        <el-table-column label="所属站点" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ resolveStationLabel(row.stationId) || '-' }}</template>
        </el-table-column>
        <el-table-column label="调度内容" prop="instructionContent" min-width="300" show-overflow-tooltip />
        <el-table-column label="接收人" prop="receiverUserName" width="120" show-overflow-tooltip />
        <el-table-column label="执行人" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.executorUserName || row.receiverUserName || '-' }}</template>
        </el-table-column>
        <el-table-column label="调令执行情况" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="resolveExecutionStatusTag(row.executionStatus)" effect="light">
              {{ row.executionStatusName || resolveExecutionStatusName(row.executionStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下发时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.issueTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
            <el-button
              v-if="canAccept(row)"
              v-hasPermi="['iot:dispatch-receive:accept']"
              link
              type="warning"
              @click="openAcceptDetail(row)"
            >
              接受调令
            </el-button>
            <el-button
              v-if="canExecute(row)"
              v-hasPermi="['iot:dispatch-receive:submit']"
              link
              type="success"
              @click="openExecute(row)"
            >
              去执行
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

    <Dialog v-model="detailVisible" title="指令详情" width="760px">
      <div v-if="detailData" class="instruction-detail">
        <div class="instruction-detail__grid">
          <div class="instruction-detail__item">
            <div class="instruction-detail__label">发令单位：</div>
            <div class="instruction-detail__value">{{ detailData.issueOrgName || '-' }}</div>
          </div>
          <div class="instruction-detail__item">
            <div class="instruction-detail__label">发令人：</div>
            <div class="instruction-detail__value">{{ detailData.issueUserName || '-' }}</div>
          </div>
          <div class="instruction-detail__item">
            <div class="instruction-detail__label">指令名称：</div>
            <div class="instruction-detail__value">{{ detailData.instructionName || '-' }}</div>
          </div>
          <div class="instruction-detail__item instruction-detail__item--full">
            <div class="instruction-detail__label">方案名称：</div>
            <div class="instruction-detail__value">{{ resolvePlanNamesText(detailData.planNames) }}</div>
          </div>
          <div class="instruction-detail__item instruction-detail__item--full">
            <div class="instruction-detail__label">调度内容：</div>
            <div class="instruction-detail__value instruction-detail__value--textarea">
              {{ detailData.instructionContent || '-' }}
            </div>
          </div>
          <div class="instruction-detail__item">
            <div class="instruction-detail__label">发令时间：</div>
            <div class="instruction-detail__value">{{ formatDateTime(detailData.issueTime) }}</div>
          </div>
          <div class="instruction-detail__item">
            <div class="instruction-detail__label">所属站点：</div>
            <div class="instruction-detail__value">{{ resolveStationLabel(detailData.stationId) || '-' }}</div>
          </div>
          <div class="instruction-detail__item">
            <div class="instruction-detail__label">接收单位：</div>
            <div class="instruction-detail__value">{{ detailData.receiverDeptName || '-' }}</div>
          </div>
          <div class="instruction-detail__item">
            <div class="instruction-detail__label">接收人：</div>
            <div class="instruction-detail__value">{{ detailData.receiverUserName || '-' }}</div>
          </div>
          <div class="instruction-detail__item">
            <div class="instruction-detail__label">执行人：</div>
            <div class="instruction-detail__value">
              {{ detailData.executorUserName || detailData.receiverUserName || '-' }}
            </div>
          </div>
        </div>

        <div v-if="detailMode === 'view'" class="instruction-detail__feedback">
          <div class="instruction-detail__section-title">执行反馈</div>
          <div class="instruction-detail__grid">
            <div class="instruction-detail__item">
              <div class="instruction-detail__label">执行情况：</div>
              <div class="instruction-detail__value">{{ resolveExecuteFlagName(detailData.executeFlag) }}</div>
            </div>
            <div class="instruction-detail__item">
              <div class="instruction-detail__label">完成时间：</div>
              <div class="instruction-detail__value">{{ formatDateTime(detailData.finishTime) }}</div>
            </div>
            <div class="instruction-detail__item instruction-detail__item--full">
              <div class="instruction-detail__label">备注：</div>
              <div class="instruction-detail__value instruction-detail__value--textarea">
                {{ detailData.remark || '-' }}
              </div>
            </div>
            <div class="instruction-detail__item instruction-detail__item--full">
              <div class="instruction-detail__label">运行日志：</div>
              <div class="instruction-detail__value instruction-detail__value--logs">
                <template v-if="detailRunLogItems.length">
                  <el-button
                    v-for="item in detailRunLogItems"
                    :key="String(item.id)"
                    link
                    type="primary"
                    class="instruction-detail__run-log-btn"
                    @click="openRunLogDetail(item.id)"
                  >
                    {{ item.title }}
                  </el-button>
                </template>
                <span v-else>-</span>
              </div>
            </div>
            <div class="instruction-detail__item instruction-detail__item--full">
              <div class="instruction-detail__label">附件：</div>
              <div class="instruction-detail__value">
                <div v-if="(detailData.attachments || []).length" class="instruction-detail__file-list">
                  <el-link
                    v-for="(url, index) in detailData.attachments || []"
                    :key="`${url}-${index}`"
                    :href="url"
                    target="_blank"
                    type="primary"
                  >
                    {{ resolveFileName(url, index) }}
                  </el-link>
                </div>
                <span v-else>-</span>
              </div>
            </div>
          </div>
        </div>

        <div
          v-if="detailMode === 'accept' && canAccept(detailData)"
          class="instruction-detail__actions"
        >
          <el-button
            v-hasPermi="['iot:dispatch-receive:accept']"
            type="primary"
            :loading="acceptLoading"
            @click="handleAcceptConfirm"
          >
            接受调令
          </el-button>
        </div>
      </div>
      <template #footer>
        <div v-if="detailMode === 'view'" class="instruction-detail__footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button
            v-if="detailData && canAccept(detailData)"
            v-hasPermi="['iot:dispatch-receive:accept']"
            type="primary"
            @click="openAcceptDetail(detailData)"
          >
            接受调令
          </el-button>
        </div>
      </template>
    </Dialog>

    <Dialog v-model="executeVisible" title="调度指令" width="760px">
      <el-form
        ref="executeFormRef"
        :model="executeFormData"
        :rules="executeRules"
        label-width="94px"
        v-loading="executeLoading"
      >
        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="发令单位">
              <el-input :model-value="executeSource.issueOrgName || '-'" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发令人">
              <el-input :model-value="executeSource.issueUserName || '-'" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="指令名称">
              <el-input :model-value="executeSource.instructionName || '-'" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="方案名称">
              <el-input :model-value="resolvePlanNamesText(executeSource.planNames)" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="调度内容">
              <el-input :model-value="executeSource.instructionContent || '-'" type="textarea" :rows="3" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发令时间">
              <el-input :model-value="formatDateTime(executeSource.issueTime)" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属站点">
              <el-input :model-value="resolveStationLabel(executeSource.stationId) || '-'" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接收单位">
              <el-input :model-value="executeSource.receiverDeptName || '-'" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接收人">
              <el-input :model-value="executeSource.receiverUserName || '-'" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="执行人">
              <el-input :model-value="executeSource.executorUserName || executeSource.receiverUserName || '-'" readonly />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="split-line"></div>

        <el-form-item label="执行情况" prop="executeFlag">
          <el-switch
            v-model="executeFormData.executeFlag"
            :active-value="1"
            :inactive-value="0"
            active-text="完成"
            inactive-text="未完成"
          />
        </el-form-item>
        <el-form-item label="完成时间" prop="finishTime">
          <el-date-picker
            v-model="executeFormData.finishTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="!w-full"
            placeholder="请选择完成时间"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="executeFormData.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入备注"
          />
        </el-form-item>
        <el-form-item label="运行日志" prop="runLogIds">
          <el-select v-model="executeFormData.runLogIds" multiple filterable class="!w-full" placeholder="请选择运行日志（可选）">
            <el-option v-for="item in runLogOptions" :key="item.id" :label="item.title" :value="item.id">
              <div class="run-log-option-item">
                <span class="run-log-option-item__title">{{ item.title }}</span>
                <el-button link type="primary" @click.stop="openRunLogDetail(item.id)">详情</el-button>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="附件" prop="attachments">
          <div class="attachment-block">
            <UploadFile
              v-model="executeFormData.attachments"
              :limit="6"
              :file-type="executeAttachmentTypes"
            />
            <p class="attachment-tip">仅支持 PDF、Word、Excel、JPG、PNG 格式，最多上传 6 个文件</p>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="executeVisible = false">取消</el-button>
        <el-button type="primary" :loading="executeSubmitLoading" @click="handleSubmitExecute">提交</el-button>
      </template>
    </Dialog>

    <Dialog v-model="runLogDetailVisible" title="运行日志详情" width="920px">
      <div v-loading="runLogDetailLoading" class="run-log-detail-dialog">
        <template v-if="runLogDetailData">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="记录编号">{{ runLogDetailData.logNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="任务名称">{{ runLogDetailData.taskName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="值班班组">{{ runLogDetailData.dutyTeamName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="记录人">{{ runLogDetailData.recorderUserName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="记录时间">{{ formatDateTime(runLogDetailData.recordTime) }}</el-descriptions-item>
            <el-descriptions-item label="检查时段">{{ runLogDetailData.checkPeriod || '-' }}</el-descriptions-item>
            <el-descriptions-item label="运行时段" :span="2">
              {{ formatRunLogPeriod(runLogDetailData.runStartTime, runLogDetailData.runEndTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="所属站点">{{ runLogStationName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="设备名称">{{ runLogDetailData.deviceName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="关联调令">{{ runLogDetailData.dispatchInstructionName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="运行参数" :span="2">
              <div class="run-log-detail-dialog__textarea">{{ runLogDetailData.runParamsText || '-' }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="事件描述" :span="2">
              <div class="run-log-detail-dialog__textarea">{{ runLogDetailData.eventDesc || '-' }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">
              <div class="run-log-detail-dialog__textarea">{{ runLogDetailData.remark || '-' }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="附件" :span="2">
              <div v-if="(runLogDetailData.attachments || []).length" class="instruction-detail__file-list">
                <el-link
                  v-for="(url, index) in runLogDetailData.attachments || []"
                  :key="`${url}-${index}`"
                  :href="url"
                  target="_blank"
                  type="primary"
                >
                  {{ resolveFileName(url, index) }}
                </el-link>
              </div>
              <span v-else>-</span>
            </el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
      <template #footer>
        <el-button @click="runLogDetailVisible = false">关闭</el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import type { FormInstance, FormRules } from 'element-plus'
import { DeviceApi } from '@/api/iot/device/device'
import { useUserStoreWithOut } from '@/store/modules/user'
import {
  DispatchReceiveApi,
  type DispatchReceivePageReqVO,
  type DispatchReceiveRunLogOptionVO,
  type DispatchReceiveSubmitReqVO,
  type DispatchReceiveVO
} from '@/api/iot/dispatch/receive'
import { RunLogApi, type RunLogVO } from '@/api/iot/run/log'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'

defineOptions({ name: 'IotDispatchReceive' })

const message = useMessage()
const userStore = useUserStoreWithOut()

type DispatchReceiveId = string | number

const EXECUTION_STATUS_OPTIONS = [
  { label: '待接收', value: 0, tag: 'danger' },
  { label: '待执行', value: 1, tag: 'warning' },
  { label: '已执行', value: 2, tag: 'success' },
  { label: '已逾期', value: 3, tag: 'danger' }
]

const executeAttachmentTypes = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png']

const queryFormRef = ref<FormInstance>()
const loading = ref(false)
const exportLoading = ref(false)
const total = ref(0)
const list = ref<DispatchReceiveVO[]>([])
const issueTimeRange = ref<string[]>([])
const queryParams = reactive<DispatchReceivePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  instructionNo: undefined,
  instructionName: undefined,
  issueOrgName: undefined,
  planName: undefined,
  stationId: undefined,
  executionStatus: undefined,
  issueTime: undefined
})

const detailVisible = ref(false)
const detailData = ref<DispatchReceiveVO>()
const detailMode = ref<'view' | 'accept'>('view')
const acceptLoading = ref(false)

const executeVisible = ref(false)
const executeLoading = ref(false)
const executeSubmitLoading = ref(false)
const executeFormRef = ref<FormInstance>()
const executeSource = ref<DispatchReceiveVO>({})
const executeFormData = reactive<DispatchReceiveSubmitReqVO>({
  id: 0,
  executeFlag: 1,
  finishTime: '',
  attachments: [],
  remark: '',
  runLogIds: []
})
const runLogOptions = ref<DispatchReceiveRunLogOptionVO[]>([])
const runLogDetailVisible = ref(false)
const runLogDetailLoading = ref(false)
const runLogDetailData = ref<RunLogVO>()
const runLogStationName = ref('')

const stationOptions = computed(() => {
  return getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD).map((item) => ({
    label: item.label,
    value: String(item.value)
  }))
})

const executeRules: FormRules<DispatchReceiveSubmitReqVO> = {
  executeFlag: [{ required: true, message: '执行情况不能为空', trigger: 'change' }],
  finishTime: [{ required: true, message: '完成时间不能为空', trigger: 'change' }],
  attachments: [
    {
      required: true,
      type: 'array',
      min: 1,
      message: '请至少上传一个附件',
      trigger: 'change'
    }
  ]
}

const hasValidId = (id?: DispatchReceiveId) => {
  return !(id === undefined || id === null || id === '')
}

const isCurrentUser = (userId?: DispatchReceiveId) => {
  if (!hasValidId(userId)) return false
  return String(userStore.getUser.id) === String(userId)
}

const resolveExecutionStatusName = (status?: number) => {
  return EXECUTION_STATUS_OPTIONS.find((item) => item.value === Number(status))?.label || '待执行'
}

const resolveExecutionStatusTag = (status?: number) => {
  return EXECUTION_STATUS_OPTIONS.find((item) => item.value === Number(status))?.tag || 'info'
}

const resolveExecuteFlagName = (executeFlag?: number) => {
  if (executeFlag === 1) return '完成'
  if (executeFlag === 0) return '未完成'
  return '-'
}

const resolveRunLogItems = (runLogIds?: Array<string | number>) => {
  if (!runLogIds || !runLogIds.length) return [] as Array<{ id: string | number; title: string }>
  return runLogIds.map((id) => {
    const matched = runLogOptions.value.find((item) => String(item.id) === String(id))
    return {
      id,
      title: matched?.title || `运行日志#${id}`
    }
  })
}

const detailRunLogItems = computed(() => resolveRunLogItems(detailData.value?.runLogIds))

const resolveFileName = (url: string, index: number) => {
  if (!url) return `附件${index + 1}`
  const segments = url.split('/')
  return decodeURIComponent(segments[segments.length - 1] || `附件${index + 1}`)
}

const resolvePlanNamesText = (planNames?: string[]) => {
  if (!planNames || !planNames.length) return '-'
  return planNames.join('，')
}

const canAccept = (row?: DispatchReceiveVO) => {
  if (!row) return false
  return Number(row.executionStatus) === 0 && isCurrentUser(row.executorUserId)
}

const canExecute = (row?: DispatchReceiveVO) => {
  if (!row) return false
  const executionStatus = Number(row.executionStatus)
  return (executionStatus === 1 || executionStatus === 3) && isCurrentUser(row.executorUserId)
}

const formatDateTime = (value?: string | number) => {
  if (!value) return '-'
  return dayjs(value).isValid() ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : String(value)
}

const normalizeDateTimeValue = (value?: string | number) => {
  if (value === undefined || value === null || value === '') return ''
  return dayjs(value).isValid() ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : String(value)
}

const buildQueryParams = (): DispatchReceivePageReqVO => {
  return {
    ...queryParams,
    issueTime: issueTimeRange.value?.length ? issueTimeRange.value : undefined
  }
}

const getList = async () => {
  loading.value = true
  try {
    const data = await DispatchReceiveApi.getPage(buildQueryParams())
    list.value = data.list || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

const getRunLogOptions = async (stationId?: string | number) => {
  const normalizedStationId =
    stationId === undefined || stationId === null || stationId === '' ? undefined : String(stationId)
  runLogOptions.value = await DispatchReceiveApi.getRunLogOptions(normalizedStationId)
}

const formatRunLogPeriod = (start?: string | number, end?: string | number) => {
  const startText = formatDateTime(start)
  const endText = formatDateTime(end)
  if (startText === '-' && endText === '-') return '-'
  return `${startText} ~ ${endText}`
}

const resolveStationLabel = (stationId?: string | number) => {
  if (stationId === undefined || stationId === null || stationId === '') return ''
  return stationOptions.value.find((item) => item.value === String(stationId))?.label || ''
}

const queryRunLogStationNameByDevice = async (deviceName?: string) => {
  const keyword = String(deviceName || '').trim()
  if (!keyword) return ''
  const [byNickname, byDeviceName] = await Promise.all([
    DeviceApi.getDeviceList({ nickname: keyword }),
    DeviceApi.getDeviceList({ deviceName: keyword })
  ])
  const merged = [...(Array.isArray(byNickname) ? byNickname : []), ...(Array.isArray(byDeviceName) ? byDeviceName : [])]
  if (!merged.length) return ''
  const exact =
    merged.find((item: any) => item?.nickname === keyword || item?.deviceName === keyword) || merged[0]
  return resolveStationLabel(exact?.stationId)
}

const resolveRunLogStationName = async (log?: RunLogVO) => {
  runLogStationName.value = ''
  if (!log) return
  const snapshotName = String((log as any).stationName || '').trim()
  if (snapshotName) {
    runLogStationName.value = snapshotName
    return
  }
  const stationById = resolveStationLabel((log as any).stationId)
  if (stationById) {
    runLogStationName.value = stationById
    return
  }
  runLogStationName.value = await queryRunLogStationNameByDevice(log.deviceName)
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  issueTimeRange.value = []
  await handleQuery()
}

const openDetail = async (id?: DispatchReceiveId, mode: 'view' | 'accept' = 'view') => {
  if (!hasValidId(id)) return
  detailMode.value = mode
  detailVisible.value = true
  await getRunLogOptions()
  const data = await DispatchReceiveApi.get(id)
  detailData.value = {
    ...data,
    finishTime: normalizeDateTimeValue(data.finishTime)
  }
}

const openRunLogDetail = async (id?: string | number) => {
  if (!hasValidId(id)) return
  runLogDetailVisible.value = true
  runLogDetailLoading.value = true
  try {
    const detail = await RunLogApi.get(id)
    runLogDetailData.value = detail
    await resolveRunLogStationName(detail)
  } finally {
    runLogDetailLoading.value = false
  }
}

const openAcceptDetail = async (row?: DispatchReceiveVO) => {
  const id = row?.id
  if (!hasValidId(id)) return
  await openDetail(id, 'accept')
}

const handleAcceptConfirm = async () => {
  const id = detailData.value?.id
  if (!hasValidId(id) || !canAccept(detailData.value)) return
  acceptLoading.value = true
  try {
    await DispatchReceiveApi.accept(id)
    message.success('接收成功')
    detailVisible.value = false
    await getList()
  } finally {
    acceptLoading.value = false
  }
}

const openExecute = async (row?: DispatchReceiveVO) => {
  const id = row?.id
  if (!hasValidId(id)) return
  if (!canExecute(row)) {
    message.warning('仅执行人可提交执行结果')
    return
  }
  executeVisible.value = true
  executeLoading.value = true
  executeFormRef.value?.resetFields()
  try {
    const detail = await DispatchReceiveApi.get(id)
    await getRunLogOptions(detail.stationId)
    const validRunLogIds = (detail.runLogIds || []).filter((logId) =>
      runLogOptions.value.some((item) => String(item.id) === String(logId))
    )
    executeSource.value = detail
    Object.assign(executeFormData, {
      id,
      executeFlag: detail.executeFlag === 0 ? 0 : 1,
      finishTime: normalizeDateTimeValue(detail.finishTime || dayjs().format('YYYY-MM-DD HH:mm:ss')),
      attachments: detail.attachments || [],
      remark: detail.remark || '',
      runLogIds: validRunLogIds
    })
  } finally {
    executeLoading.value = false
  }
}

const handleSubmitExecute = async () => {
  if (!canExecute(executeSource.value)) {
    message.warning('仅执行人可提交执行结果')
    return
  }
  await executeFormRef.value?.validate()
  executeSubmitLoading.value = true
  try {
    await DispatchReceiveApi.submitResult({
      id: executeFormData.id,
      executeFlag: executeFormData.executeFlag,
      finishTime: executeFormData.finishTime,
      attachments: executeFormData.attachments || [],
      remark: executeFormData.remark || '',
      runLogIds: executeFormData.runLogIds || []
    })
    message.success('提交成功')
    executeVisible.value = false
    if (detailVisible.value && detailData.value?.id === executeFormData.id) {
      await openDetail(executeFormData.id)
    }
    await getList()
  } finally {
    executeSubmitLoading.value = false
  }
}

const handleExport = async () => {
  await message.exportConfirm()
  exportLoading.value = true
  try {
    const data = await DispatchReceiveApi.exportExcel(buildQueryParams())
    download.excel(data, `调令接受_${dayjs().format('YYYYMMDD_HHmmss')}.xls`)
  } finally {
    exportLoading.value = false
  }
}

onMounted(async () => {
  await getList()
})
</script>

<style scoped lang="scss">
.dispatch-receive-page {
  .query-wrap {
    border: 1px solid #e8edf8;
    border-radius: 12px;
  }

  .table-wrap {
    border-radius: 12px;
  }

  .instruction-detail {
    padding: 2px 2px 4px;

    &__grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 12px 16px;
    }

    &__item {
      display: flex;
      align-items: center;
      gap: 10px;
      min-height: 36px;
    }

    &__item--full {
      grid-column: 1 / -1;
      align-items: flex-start;
    }

    &__label {
      width: 72px;
      color: #2f3a50;
      font-size: 14px;
      line-height: 20px;
      text-align: right;
      flex-shrink: 0;
    }

    &__value {
      flex: 1;
      min-height: 36px;
      padding: 8px 12px;
      border-radius: 4px;
      border: 1px solid #dfe4ef;
      background: #f3f5f9;
      color: #5b6475;
      font-size: 14px;
      line-height: 20px;
      word-break: break-all;
    }

    &__value--textarea {
      min-height: 76px;
      white-space: pre-wrap;
    }

    &__value--logs {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 6px 10px;
    }

    &__run-log-btn {
      height: 22px;
      padding: 0;
      color: #2563eb;
      transition: color 0.2s ease;

      &:hover {
        color: #1d4ed8;
      }
    }

    &__feedback {
      margin-top: 14px;
      padding-top: 12px;
      border-top: 1px dashed #d8deea;
    }

    &__section-title {
      margin-bottom: 10px;
      color: #2f3a50;
      font-size: 14px;
      font-weight: 600;
      line-height: 20px;
    }

    &__file-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px 14px;
    }

    &__actions { 
      display: flex;
      justify-content: center;
      margin-top: 18px;
    }

    &__footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      width: 100%;
    }
  }

  .attachment-block {
    width: 100%;
  }

  .attachment-tip {
    margin-top: 6px;
    color: #637086;
    font-size: 12px;
    line-height: 18px;
  }

  .run-log-option-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;

    &__title {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .run-log-detail-dialog {
    &__textarea {
      min-height: 54px;
      white-space: pre-wrap;
      line-height: 20px;
      color: #5b6475;
    }
  }

  .split-line {
    border-top: 1px dashed #d8deea;
    margin: 8px 0 14px;
  }

  @media (max-width: 768px) {
    .instruction-detail {
      &__grid {
        grid-template-columns: 1fr;
      }

      &__item {
        align-items: flex-start;
      }

      &__label {
        width: 64px;
      }
    }
  }
}
</style>
