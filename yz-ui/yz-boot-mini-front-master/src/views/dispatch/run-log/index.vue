<template>
  <div class="run-log-page">
    <ContentWrap class="query-wrap">
      <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="92px">
        <el-form-item label="记录编号" prop="logNo">
          <el-input
            v-model="queryParams.logNo"
            clearable
            class="!w-220px"
            placeholder="请输入记录编号"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="值班班组" prop="dutyTeamName">
          <el-input
            v-model="queryParams.dutyTeamName"
            clearable
            class="!w-220px"
            placeholder="请输入值班班组"
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
        <el-form-item label="关联任务" prop="dispatchKeyword">
          <el-input
            v-model="queryParams.dispatchKeyword"
            clearable
            class="!w-220px"
            placeholder="请输入调令编号/任务名称"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="所属站点" prop="stationId">
          <el-select
            v-model="queryParams.stationId"
            clearable
            filterable
            class="!w-220px"
            placeholder="请选择所属站点"
          >
            <el-option
              v-for="option in stationOptions"
              :key="String(option.value)"
              :label="option.label"
              :value="String(option.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="运行时段" prop="runTime">
          <el-date-picker
            v-model="runTimeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD HH:mm"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="!w-360px"
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
          <el-button v-hasPermi="['iot:run-log:create']" type="primary" plain @click="openForm('create')">
            <Icon icon="ep:plus" class="mr-4px" />
            新增运行日志
          </el-button>
          <el-button
            v-hasPermi="['iot:run-log:export']"
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
      <el-table v-loading="loading" :data="list" stripe :fit="false">
        <el-table-column label="记录编号" min-width="160">
          <template #default="{ row }">
            <span class="log-no">{{ row.logNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="任务名称" prop="taskName" min-width="200" show-overflow-tooltip />
        <el-table-column label="值班班组" prop="dutyTeamName" min-width="140" show-overflow-tooltip />
        <el-table-column label="记录人" prop="recorderUserName" min-width="110" show-overflow-tooltip />
        <el-table-column label="记录时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.recordTime) }}</template>
        </el-table-column>
        <el-table-column label="运行时段" min-width="300">
          <template #default="{ row }">{{ formatRunPeriod(row) }}</template>
        </el-table-column>
        <el-table-column label="检查时段" prop="checkPeriod" min-width="130" show-overflow-tooltip />
        <el-table-column label="所属站点" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ resolveStationLabel(row.stationId) }}</template>
        </el-table-column>
        <el-table-column label="巡检标准" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.inspectionStandardName || '-' }}</template>
        </el-table-column>
        <el-table-column label="关联调令" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ resolveDispatchText(row) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="info" @click="openForm('detail', row.id)">详情</el-button>
            <el-button v-hasPermi="['iot:run-log:update']" link type="primary" @click="openForm('update', row.id)">
              编辑
            </el-button>
            <el-button v-hasPermi="['iot:run-log:delete']" link type="danger" @click="handleDelete(row.id)">
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
        label-width="102px"
        :disabled="formReadonly"
        v-loading="formLoading"
      >
        <section class="form-section">
          <div class="section-title">基础信息</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="记录编号">
                <el-input :model-value="formData.logNo || '系统自动生成'" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="任务名称" prop="taskName">
                <el-input v-model="formData.taskName" maxlength="100" placeholder="请输入任务名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属站点" prop="stationId">
                <el-input v-if="formReadonly" :model-value="resolveStationLabel(formData.stationId)" disabled />
                <el-select
                  v-else
                  v-model="formData.stationId"
                  clearable
                  filterable
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
            <el-col :span="12">
              <el-form-item label="值班班组" prop="dutyTeamId">
                <el-select
                  v-model="formData.dutyTeamId"
                  clearable
                  filterable
                  class="!w-full"
                  placeholder="请选择值班班组"
                  @change="handleDutyTeamChange"
                >
                  <el-option v-for="item in dutyTeamOptions" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="记录人" prop="recorderUserName">
                <el-input v-model="formData.recorderUserName" maxlength="64" placeholder="请输入记录人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="班组长">
                <el-input :model-value="dutyTeamLeaderName" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="成员人数">
                <el-input :model-value="String(dutyTeamMembers.length)" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="班组成员">
                <div class="team-member-list">
                  <template v-if="dutyTeamMembers.length">
                    <el-tag
                      v-for="member in dutyTeamMembers"
                      :key="`${member.userId}`"
                      size="small"
                      class="team-member-tag"
                      :type="member.leader ? 'warning' : 'info'"
                    >
                      {{ member.userName || member.userId }}{{ member.leader ? '（班组长）' : '' }}
                    </el-tag>
                  </template>
                  <span v-else class="text-gray-500">{{
                    dutyTeamDetailLoading ? '班组成员加载中...' : '暂无班组成员'
                  }}</span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="记录时间" prop="recordTime">
                <el-date-picker
                  v-model="formData.recordTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  class="!w-full"
                  placeholder="请选择记录时间"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="巡检类型" prop="inspectionType">
                <el-input v-if="formReadonly" :model-value="getInspectionTypeLabel(formData.inspectionType)" disabled />
                <el-select
                  v-else
                  v-model="formData.inspectionType"
                  clearable
                  filterable
                  class="!w-full"
                  placeholder="请选择巡检类型"
                  @change="handleInspectionTypeChange"
                >
                  <el-option
                    v-for="option in inspectionTypeOptions"
                    :key="String(option.value)"
                    :label="option.label"
                    :value="String(option.value)"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="巡检标准" prop="inspectionStandardId">
                <el-select
                  v-model="formData.inspectionStandardId"
                  filterable
                  remote
                  reserve-keyword
                  clearable
                  class="!w-full"
                  placeholder="请选择巡检标准"
                  :loading="standardLoading"
                  :disabled="!formData.stationId || !formData.inspectionType"
                  :remote-method="loadStandardOptions"
                  @change="handleStandardChange"
                  @visible-change="handleStandardVisibleChange"
                >
                  <el-option v-for="item in standardOptions" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="检查时段" prop="runTimeRange">
                <el-date-picker
                  v-model="formRunTimeRange"
                  type="datetimerange"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  format="YYYY-MM-DD HH:mm"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="section-title">巡检结果</div>
          <el-form-item prop="inspectionResultItems" label-width="0">
            <div class="inspection-result-container">
              <el-empty
                v-if="!formData.inspectionStandardId"
                description="请先选择巡检标准"
                :image-size="80"
              />
              <el-empty
                v-else-if="!inspectionResultItems.length"
                description="当前标准未配置检查项目"
                :image-size="80"
              />
              <div v-else class="inspection-result-list">
                <div v-for="item in inspectionResultItems" :key="item.panelName" class="result-item-card">
                  <div class="result-item-card__head">
                    <div class="result-item-card__title">
                      <span>{{ item.itemName || '-' }}</span>
                    </div>
                    <div class="result-item-card__target">
                      适用对象：{{ item.targetName || '-' }}
                    </div>
                  </div>
                  <div class="result-item-card__summary">
                    <span class="result-item-card__summary-item">
                      项目描述：{{ item.itemDesc || '-' }}
                    </span>
                    <span class="result-item-card__summary-item">
                      合格标准：{{ item.qualifiedRule || '-' }}
                    </span>
                  </div>
                  <div class="result-item-card__records-table-wrap">
                    <table class="result-item-card__records-table">
                      <thead>
                        <tr>
                          <th>属性名称</th>
                          <th>具体数值</th>
                          <th>单位</th>
                          <th>实际测量值</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(record, idx) in item.records" :key="`${item.panelName}_${idx}`">
                          <td>{{ record.attrName || '-' }}</td>
                          <td>{{ record.standardValue || '-' }}</td>
                          <td>{{ record.attrUnit || '-' }}</td>
                          <td>
                            <el-input
                              v-model="record.actualValue"
                              :disabled="formReadonly"
                              placeholder="请输入实际测量值"
                              maxlength="64"
                            />
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <div class="result-item-card__result">
                    <span class="result-item-card__label">检查结果：</span>
                    <div class="result-level-grid">
                      <div
                        v-for="option in item.resultOptions"
                        :key="`${item.panelName}_${option.value}`"
                        class="result-level-card"
                        :class="[
                          getResultLevelToneClass(option),
                          {
                            'result-level-card--active': item.checkResult === option.value,
                            'result-level-card--readonly': formReadonly
                          }
                        ]"
                        role="radio"
                        :aria-checked="item.checkResult === option.value"
                        :tabindex="formReadonly ? -1 : 0"
                        @click="!formReadonly && (item.checkResult = option.value)"
                        @keydown.enter.prevent="!formReadonly && (item.checkResult = option.value)"
                        @keydown.space.prevent="!formReadonly && (item.checkResult = option.value)"
                      >
                        <div class="result-level-card__title">{{ option.label }}</div>
                        <div class="result-level-card__desc">{{ option.remark || '-' }}</div>
                      </div>
                    </div>
                  </div>
                  <div class="result-item-card__remark">
                    <div class="result-item-card__remark-label">
                      <span class="result-item-card__label">检查备注</span>
                      <span v-if="!formReadonly && isAbnormalResult(item)" class="result-item-card__required">*</span>
                    </div>
                    <el-input
                      v-model="item.checkRemark"
                      type="textarea"
                      :rows="2"
                      :disabled="formReadonly"
                      :placeholder="isAbnormalResult(item) ? '请输入检查备注（必填）' : '请输入检查备注（选填）'"
                      maxlength="500"
                      show-word-limit
                    />
                  </div>
                </div>
              </div>
            </div>
          </el-form-item>
        </section>
        <section class="form-section">
          <div class="section-title">关联信息</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="关联调令" prop="dispatchInstructionId">
                <el-select
                  v-model="formData.dispatchInstructionId"
                  filterable
                  remote
                  reserve-keyword
                  clearable
                  class="!w-full"
                  :disabled="!formData.stationId"
                  placeholder="请输入调令编号/名称搜索"
                  :loading="dispatchOptionLoading"
                  :remote-method="loadDispatchOptions"
                >
                  <el-option
                    v-for="item in dispatchOptions"
                    :key="item.id"
                    :label="item.instructionName || item.title"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </section>
        <section class="form-section">
          <div class="section-title">补充信息</div>
          <el-row :gutter="16">
            <el-col :span="24">
              <el-form-item label="附件" prop="attachments">
                <div class="attachment-block">
                  <UploadFile
                    v-model="formData.attachments"
                    :limit="10"
                    :file-type="['jpg', 'jpeg', 'png', 'pdf', 'doc', 'docx', 'xls', 'xlsx']"
                    :disabled="formReadonly"
                  />
                  <p class="attachment-tip">支持 jpg、png、pdf、doc、xls 等格式，最多上传 10 个文件。</p>
                </div>
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
                  :disabled="formReadonly"
                  placeholder="请输入备注"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">{{ formReadonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!formReadonly" type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </Dialog>
    <Dialog v-model="submitAbnormalReminderVisible" title="提交提醒" width="900px">
      <div class="submit-abnormal-reminder__desc">以下异常项将自动生成故障记录，请确认：</div>
      <el-table :data="submitAbnormalReminderItems" border class="submit-abnormal-reminder-table">
        <el-table-column label="设备" prop="deviceName" min-width="180" show-overflow-tooltip />
        <el-table-column label="检查项目" prop="itemName" min-width="220" show-overflow-tooltip />
        <el-table-column label="检查结果" prop="checkResultLabel" min-width="120" />
        <el-table-column label="检查备注" prop="checkRemark" min-width="280" show-overflow-tooltip />
      </el-table>
      <el-checkbox v-model="submitAbnormalReminderAutoCreateFaultRecords" class="submit-abnormal-reminder__checkbox">
        勾选后，以上异常项将自动生成故障记录
      </el-checkbox>
      <template #footer>
        <el-button @click="submitAbnormalReminderVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          :disabled="!submitAbnormalReminderAutoCreateFaultRecords"
          @click="confirmSubmitFormWithReminder"
        >
          确定并保存
        </el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { UploadFile } from '@/components/UploadFile'
import { ShiftTeamApi, type ShiftTeamMemberVO, type ShiftTeamVO } from '@/api/iot/shift/team'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { RunLogApi, type RunLogDispatchOptionVO, type RunLogPageReqVO, type RunLogVO } from '@/api/iot/run/log'
import { InspectionPlanApi, type InspectionPlanStandardOptionVO } from '@/api/iot/inspection/plan'
import {
  InspectionStandardApi,
  type InspectionCheckResultConfigVO,
  type InspectionStandardVO,
  type InspectionStandardTargetVO
} from '@/api/iot/inspection/standard'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'IotRunLog' })

type FormMode = 'create' | 'update' | 'detail'

interface SelectOption {
  label: string
  value: string | number
}

interface DutyTeamOptionItem {
  id: string
  name: string
}

interface DutyTeamDetailState {
  leaderUserName: string
  members: ShiftTeamMemberVO[]
}

interface CheckResultOption {
  value: string
  label: string
  remark: string
}

type CheckResultTone = 'success' | 'primary' | 'warning' | 'danger' | 'info'

interface RunLogInspectionResultRecordForm {
  attrName: string
  attrUnit: string
  standardValue: string
  actualValue: string
}

interface RunLogInspectionResultItemForm {
  panelName: string
  itemId?: number
  itemName: string
  itemDesc: string
  qualifiedRule: string
  targetType?: string
  targetId?: string | number
  targetName?: string
  checkResult: string
  checkRemark: string
  resultOptions: CheckResultOption[]
  records: RunLogInspectionResultRecordForm[]
}

interface RunLogAbnormalReminderItem {
  deviceName: string
  itemName: string
  checkResultLabel: string
  checkRemark: string
}

type RunLogFormVO = RunLogVO & {
  runTimeRange?: string[]
  stationId?: string
  inspectionType?: string
  inspectionStandardId?: string | number
  inspectionResultItems?: RunLogInspectionResultItemForm[]
}

const FALLBACK_CHECK_RESULT_OPTIONS: CheckResultOption[] = [
  { value: 'excellent', label: '优秀', remark: '各项指标均优于标准要求' },
  { value: 'good', label: '良好', remark: '各项指标符合标准要求' },
  { value: 'qualified', label: '合格', remark: '基本符合标准，存在轻微偏差' },
  { value: 'unqualified', label: '不合格', remark: '存在严重缺陷或安全隐患' }
]

const checkResultToneMap: Record<string, CheckResultTone> = {
  excellent: 'success',
  good: 'primary',
  qualified: 'warning',
  unqualified: 'danger'
}

const STANDARD_DEVICE_TARGET_TYPE = 'device'

const message = useMessage()

const loading = ref(false)
const exportLoading = ref(false)
const queryFormRef = ref<FormInstance>()
const list = ref<RunLogVO[]>([])
const total = ref(0)
const runTimeRange = ref<string[]>([])

const stationOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const inspectionTypeOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_INSPECTION_TYPE)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const getInspectionTypeLabel = (value?: string) => {
  if (!value) return '-'
  return inspectionTypeOptions.value.find((item) => String(item.value) === String(value))?.label || value
}

const getCheckResultTone = (option: CheckResultOption): CheckResultTone => {
  const value = String(option.value || '').trim()
  if (value && checkResultToneMap[value]) {
    return checkResultToneMap[value]
  }
  const label = String(option.label || '').trim()
  if (/险情|紧急/.test(label)) return 'danger'
  if (/重大|严重/.test(label)) return 'warning'
  if (/一般|良好/.test(label)) return 'primary'
  if (/正常|优秀|合格/.test(label)) return 'success'
  return 'info'
}

const getResultLevelToneClass = (option: CheckResultOption) => {
  return `result-level-card--tone-${getCheckResultTone(option)}`
}

const queryParams = reactive<RunLogPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  logNo: undefined,
  dutyTeamName: undefined,
  checkPeriod: undefined,
  stationId: undefined,
  deviceName: undefined,
  dispatchKeyword: undefined,
  runTime: undefined
})

const buildQueryParams = (): RunLogPageReqVO => {
  return {
    ...queryParams,
    runTime: runTimeRange.value?.length === 2 ? runTimeRange.value : undefined
  }
}

const normalizeDateTimeValue = (value?: string | number | Date | null) => {
  if (value === undefined || value === null || value === '') return ''
  const raw = typeof value === 'string' && /^\d+$/.test(value) ? Number(value) : value
  const timestamp = typeof raw === 'number' && raw > 0 && raw < 1000000000000 ? raw * 1000 : raw
  const parsed = dayjs(timestamp)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : ''
}

const formatDateTime = (value?: string | number) => {
  const normalized = normalizeDateTimeValue(value)
  return normalized || '-'
}

const formatRunPeriod = (row: RunLogVO) => {
  const start = formatDateTime(row.runStartTime)
  const end = formatDateTime(row.runEndTime)
  if (start === '-' && end === '-') return '-'
  return `${start} ~ ${end}`
}

const resolveDispatchText = (row: RunLogVO) => {
  if (!row.dispatchInstructionId) return '-'
  return row.dispatchInstructionName || '-'
}

const resolveStationLabel = (stationId?: string | number) => {
  if (!stationId) return '-'
  return stationOptions.value.find((item) => String(item.value) === String(stationId))?.label || '-'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await RunLogApi.getPage(buildQueryParams())
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
  runTimeRange.value = []
  queryParams.pageNo = 1
  await getList()
}

const handleDelete = async (id?: number) => {
  if (!id) return
  await message.delConfirm('确认删除该运行日志吗？')
  await RunLogApi.remove(id)
  message.success('删除成功')
  await getList()
}

const handleExport = async () => {
  await message.exportConfirm()
  exportLoading.value = true
  try {
    const data = await RunLogApi.exportExcel(buildQueryParams())
    download.excel(data, `运行日志_${dayjs().format('YYYYMMDD_HHmmss')}.xls`)
  } finally {
    exportLoading.value = false
  }
}

const dutyTeamOptions = ref<DutyTeamOptionItem[]>([])
const dispatchOptions = ref<RunLogDispatchOptionVO[]>([])
const dispatchOptionLoading = ref(false)
const dutyTeamDetailLoading = ref(false)
const dutyTeamDetail = ref<DutyTeamDetailState>({
  leaderUserName: '',
  members: []
})
const dutyTeamDetailCache = new Map<string, DutyTeamDetailState>()

const standardOptions = ref<InspectionPlanStandardOptionVO[]>([])
const standardLoading = ref(false)
const standardKeyword = ref('')
const standardLabelCache = reactive<Record<string, string>>({})

const dutyTeamLeaderName = computed(() => dutyTeamDetail.value.leaderUserName || '-')
const dutyTeamMembers = computed(() => dutyTeamDetail.value.members || [])

const formVisible = ref(false)
const formMode = ref<FormMode>('create')
const formLoading = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const formRunTimeRange = ref<string[]>([])
const inspectionResultItems = ref<RunLogInspectionResultItemForm[]>([])
const submitAbnormalReminderVisible = ref(false)
const submitAbnormalReminderAutoCreateFaultRecords = ref(false)
const submitAbnormalReminderItems = ref<RunLogAbnormalReminderItem[]>([])

const normalizePositiveIntegerId = (value?: string | number | null) => {
  if (value === undefined || value === null || value === '') {
    return ''
  }
  const text = String(value).trim()
  return /^\d+$/.test(text) ? text : ''
}

const normalizeTargetId = (value?: string | number | null) => {
  if (value === undefined || value === null || value === '') {
    return ''
  }
  return String(value).trim()
}

const normalizeCheckResultOptions = (configs?: InspectionCheckResultConfigVO[]): CheckResultOption[] => {
  const optionMap = new Map<string, CheckResultOption>()
  ;(configs || []).forEach((config) => {
    const value = String(config?.value || '').trim()
    const label = String(config?.label || '').trim()
    const key = value || label
    if (!key) return
    const fallback = FALLBACK_CHECK_RESULT_OPTIONS.find((item) => item.value === value || item.label === label)
    optionMap.set(key, {
      value: value || fallback?.value || key,
      label: label || fallback?.label || key,
      remark: String(config?.remark || fallback?.remark || '').trim()
    })
  })
  if (!optionMap.size) {
    return FALLBACK_CHECK_RESULT_OPTIONS.map((item) => ({ ...item }))
  }
  return Array.from(optionMap.values())
}

const buildResultItemMatchKey = (
  item?: {
    itemId?: number
    targetId?: string | number
    itemName?: string
    targetName?: string
  } | null
) => {
  if (!item) return ''
  const targetId = normalizeTargetId(item.targetId)
  return `${item.itemId || item.itemName || ''}__${targetId || item.targetName || ''}`
}

const buildInspectionResultItems = (standardDetail: InspectionStandardVO): RunLogInspectionResultItemForm[] => {
  const resultItems: RunLogInspectionResultItemForm[] = []
  ;(standardDetail.targets || []).forEach((target: InspectionStandardTargetVO, targetIndex) => {
    const resultOptions = normalizeCheckResultOptions(target.checkResultConfigs)
    ;(target.items || []).forEach((item, itemIndex) => {
      const panelName = `target_${targetIndex + 1}_item_${itemIndex + 1}_${item.id || 'new'}`
      resultItems.push({
        panelName,
        itemId: item.id,
        itemName: item.itemName || '',
        itemDesc: item.itemDesc || '',
        qualifiedRule: item.qualifiedRule || '',
        targetType: target.targetType || '',
        targetId: target.targetId,
        targetName: target.targetName || '',
        checkResult: '',
        checkRemark: '',
        resultOptions,
        records: (item.recordTemplates || []).map((record) => ({
          attrName: record.attrName || '-',
          attrUnit: record.attrUnit || '-',
          standardValue: record.defaultValue || '-',
          actualValue: ''
        }))
      })
    })
  })
  return resultItems
}

const mergeInspectionResultItems = (
  baseItems: RunLogInspectionResultItemForm[],
  savedItems?: RunLogFormVO['inspectionResultItems']
) => {
  const cacheMap = new Map<string, RunLogInspectionResultItemForm>()
  ;(savedItems || []).forEach((item) => {
    const key = buildResultItemMatchKey(item as any)
    if (key) {
      cacheMap.set(key, item as RunLogInspectionResultItemForm)
    }
  })

  return baseItems.map((base) => {
    const cache = cacheMap.get(buildResultItemMatchKey(base as any))
    if (!cache) return base
    const recordValueMap = new Map<string, string>()
    ;(cache.records || []).forEach((record) => {
      const key = `${record.attrName || ''}__${record.attrUnit || ''}`
      recordValueMap.set(key, String(record.actualValue || '').trim())
    })
    return {
      ...base,
      checkResult: cache.checkResult || '',
      checkRemark: cache.checkRemark || '',
      records: base.records.map((record) => {
        const key = `${record.attrName || ''}__${record.attrUnit || ''}`
        return {
          ...record,
          actualValue: recordValueMap.get(key) || ''
        }
      })
    }
  })
}

const buildSubmitInspectionResultItems = () => {
  return inspectionResultItems.value.map((item) => ({
    itemId: item.itemId,
    itemName: item.itemName,
    targetType: item.targetType,
    targetId: item.targetId,
    targetName: item.targetName,
    checkResult: item.checkResult,
    checkRemark: item.checkRemark?.trim() || '',
    records: item.records.map((record) => ({
      attrName: record.attrName,
      attrUnit: record.attrUnit,
      standardValue: record.standardValue,
      actualValue: record.actualValue?.trim() || ''
    }))
  }))
}

const isAbnormalResult = (item: RunLogInspectionResultItemForm) => {
  const normalizedValue = String(item.checkResult || '')
    .trim()
    .toLowerCase()
  return normalizedValue === 'qualified' || normalizedValue === 'unqualified'
}

const isDeviceTargetItem = (item: RunLogInspectionResultItemForm) => {
  return (
    String(item.targetType || '')
      .trim()
      .toLowerCase() === STANDARD_DEVICE_TARGET_TYPE
  )
}

const getResultOptionLabel = (item: RunLogInspectionResultItemForm) => {
  const selected = item.resultOptions.find(
    (option) => String(option.value || '').trim() === String(item.checkResult || '').trim()
  )
  return selected?.label || item.checkResult || '-'
}

const buildSubmitAbnormalReminderItems = (): RunLogAbnormalReminderItem[] => {
  return inspectionResultItems.value
    .filter((item) => isAbnormalResult(item) && isDeviceTargetItem(item))
    .map((item) => ({
      deviceName: item.targetName || '-',
      itemName: item.itemName || '-',
      checkResultLabel: getResultOptionLabel(item),
      checkRemark: item.checkRemark?.trim() || '-'
    }))
}

const validateInspectionResultItems = (): string => {
  if (!formData.inspectionStandardId) {
    return ''
  }
  if (!inspectionResultItems.value.length) {
    return '请先维护巡检标准检查项目后再保存'
  }
  for (let i = 0; i < inspectionResultItems.value.length; i++) {
    const item = inspectionResultItems.value[i]
    if (!item.checkResult) {
      return `请填写第 ${i + 1} 个检查项目的检查结果等级`
    }
    if (isAbnormalResult(item) && !String(item.checkRemark || '').trim()) {
      return `第 ${i + 1} 个检查项目的检查结果为异常，检查备注必填`
    }
    for (let j = 0; j < item.records.length; j++) {
      const record = item.records[j]
      if (!String(record.actualValue || '').trim()) {
        return `请填写第 ${i + 1} 个检查项目第 ${j + 1} 条记录的实际测量值`
      }
    }
  }
  return ''
}

const createEmptyFormData = (): RunLogFormVO => ({
  id: undefined,
  logNo: '',
  taskName: '',
  dutyTeamId: undefined as any,
  dutyTeamName: '',
  recorderUserId: undefined,
  recorderUserName: '',
  recordTime: '',
  runStartTime: '',
  runEndTime: '',
  checkPeriod: '',
  stationId: '',
  inspectionType: '',
  inspectionStandardId: undefined,
  inspectionStandardName: '',
  inspectionResultItems: [],
  dispatchInstructionId: undefined,
  dispatchInstructionNo: '',
  dispatchInstructionName: '',
  attachments: [],
  remark: ''
})

const formData = reactive<RunLogFormVO>(createEmptyFormData())

const formRules: FormRules<RunLogFormVO> = {
  taskName: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
  stationId: [{ required: true, message: '所属站点不能为空', trigger: 'change' }],
  inspectionType: [{ required: true, message: '巡检类型不能为空', trigger: 'change' }],
  inspectionStandardId: [{ required: true, message: '巡检标准不能为空', trigger: 'change' }],
  recorderUserName: [{ required: true, message: '记录人不能为空', trigger: 'blur' }],
  recordTime: [{ required: true, message: '记录时间不能为空', trigger: 'change' }],
  runTimeRange: [
    {
      validator: (_rule, _value, callback) => {
        if (!formRunTimeRange.value || formRunTimeRange.value.length !== 2) {
          callback(new Error('检查时段不能为空'))
          return
        }
        const [start, end] = formRunTimeRange.value
        if (!start || !end) {
          callback(new Error('检查时段不能为空'))
          return
        }
        if (dayjs(end).isBefore(dayjs(start))) {
          callback(new Error('运行结束时间不能早于开始时间'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  inspectionResultItems: [
    {
      validator: (_rule, _value, callback) => {
        const errorMessage = validateInspectionResultItems()
        if (!errorMessage) {
          callback()
          return
        }
        callback(new Error(errorMessage))
      },
      trigger: 'change'
    }
  ]
}

const formReadonly = computed(() => formMode.value === 'detail')
const formTitle = computed(() => {
  if (formMode.value === 'create') return '新增运行日志'
  if (formMode.value === 'update') return '编辑运行日志'
  return '运行日志详情'
})

const resetDutyTeamDetail = () => {
  dutyTeamDetail.value = {
    leaderUserName: '',
    members: []
  }
}

const normalizeDutyTeamId = (teamId?: string | number | null) => {
  if (teamId === undefined || teamId === null) return undefined
  const normalizedId = String(teamId).trim()
  if (!normalizedId || normalizedId === 'undefined' || normalizedId === 'null') return undefined
  return normalizedId
}

const loadDutyTeamOptions = async () => {
  const pageSize = 100
  let pageNo = 1
  let total = 0
  const allTeams: ShiftTeamVO[] = []
  do {
    const data = await ShiftTeamApi.getPage({ pageNo, pageSize })
    const currentList = data?.list || []
    allTeams.push(...currentList)
    total = Number(data?.total || 0)
    if (!currentList.length) {
      break
    }
    pageNo += 1
  } while (allTeams.length < total)

  const uniqueTeamMap = new Map<string, DutyTeamOptionItem>()
  allTeams.forEach((item) => {
    const teamId = normalizeDutyTeamId(item.id)
    if (!teamId || uniqueTeamMap.has(teamId)) {
      return
    }
    uniqueTeamMap.set(teamId, {
      id: teamId,
      name: item.teamName || ''
    })
  })
  dutyTeamOptions.value = Array.from(uniqueTeamMap.values())
}

const syncDutyTeamDetail = async (teamId?: string | number | null) => {
  const normalizedId = normalizeDutyTeamId(teamId)
  if (!normalizedId) {
    resetDutyTeamDetail()
    return
  }
  const cached = dutyTeamDetailCache.get(normalizedId)
  if (cached) {
    dutyTeamDetail.value = cached
    return
  }
  dutyTeamDetailLoading.value = true
  try {
    const team = await ShiftTeamApi.get(normalizedId)
    const normalizedDetail: DutyTeamDetailState = {
      leaderUserName: team?.leaderUserName || '',
      members: team?.members || []
    }
    dutyTeamDetailCache.set(normalizedId, normalizedDetail)
    dutyTeamDetail.value = normalizedDetail
    if (!formData.dutyTeamName) {
      formData.dutyTeamName = team?.teamName || ''
    }
  } catch {
    resetDutyTeamDetail()
  } finally {
    dutyTeamDetailLoading.value = false
  }
}

const loadDispatchOptions = async (keyword?: string) => {
  const stationId = formData.stationId ? String(formData.stationId) : ''
  if (!stationId) {
    dispatchOptions.value = []
    return
  }
  dispatchOptionLoading.value = true
  try {
    const options = await RunLogApi.getDispatchOptions(keyword, stationId)
    dispatchOptions.value = (options || []).map((item) => ({
      ...item,
      title: item.instructionName || item.title || `调令#${item.id}`
    }))
  } finally {
    dispatchOptionLoading.value = false
  }
}

const loadStandardOptions = async (keyword?: string) => {
  standardKeyword.value = keyword || ''
  standardLoading.value = true
  try {
    const options = await InspectionPlanApi.getStandardOptions({
      stationId: formData.stationId || undefined,
      inspectionType: formData.inspectionType || undefined,
      keyword: keyword?.trim() || undefined,
      limit: 200
    })
    ;(options || []).forEach((item) => {
      standardLabelCache[String(item.id)] = item.name || `标准-${item.id}`
    })
    let merged = options || []
    if (formData.inspectionStandardId) {
      const selectedId = normalizePositiveIntegerId(formData.inspectionStandardId as any)
      const exists = merged.some((item) => String(item.id) === selectedId)
      if (!exists && selectedId) {
        merged = [
          ...merged,
          {
            id: Number(selectedId),
            name: formData.inspectionStandardName || standardLabelCache[selectedId] || `标准-${selectedId}`,
            inspectionType: formData.inspectionType || ''
          }
        ]
      }
    }
    standardOptions.value = merged
  } finally {
    standardLoading.value = false
  }
}

const handleStandardVisibleChange = async (visible: boolean) => {
  if (!visible) return
  await loadStandardOptions(standardKeyword.value)
}

const loadInspectionResultByStandard = async (
  standardId: string | number,
  savedItems?: RunLogFormVO['inspectionResultItems']
) => {
  const normalizedId = normalizePositiveIntegerId(standardId as any)
  if (!normalizedId) {
    inspectionResultItems.value = []
    formData.inspectionResultItems = []
    return
  }
  const standardDetail = await InspectionStandardApi.getInspectionStandard(normalizedId)
  const baseItems = buildInspectionResultItems(standardDetail)
  const mergedItems = mergeInspectionResultItems(baseItems, savedItems)
  inspectionResultItems.value = mergedItems
  formData.inspectionResultItems = mergedItems
}

const handleStandardChange = async (standardId?: string | number) => {
  formData.inspectionStandardId = standardId
  if (!standardId) {
    formData.inspectionStandardName = ''
    inspectionResultItems.value = []
    formData.inspectionResultItems = []
    return
  }
  const selected = standardOptions.value.find((item) => String(item.id) === String(standardId))
  formData.inspectionStandardName = selected?.name || formData.inspectionStandardName || ''
  await loadInspectionResultByStandard(standardId)
}

const handleStationChange = async (stationId?: string) => {
  formData.stationId = stationId || ''
  formData.dispatchInstructionId = undefined
  formData.dispatchInstructionNo = ''
  formData.dispatchInstructionName = ''
  dispatchOptions.value = []
  formData.inspectionStandardId = undefined
  formData.inspectionStandardName = ''
  inspectionResultItems.value = []
  formData.inspectionResultItems = []
  if (!formData.stationId) {
    standardOptions.value = []
    return
  }
  await loadDispatchOptions()
  if (!formData.inspectionType) {
    standardOptions.value = []
    return
  }
  await loadStandardOptions()
}

const handleInspectionTypeChange = async (inspectionType?: string) => {
  formData.inspectionType = inspectionType || ''
  formData.inspectionStandardId = undefined
  formData.inspectionStandardName = ''
  inspectionResultItems.value = []
  formData.inspectionResultItems = []
  if (!formData.stationId || !formData.inspectionType) {
    standardOptions.value = []
    return
  }
  await loadStandardOptions()
}

const handleDutyTeamChange = async (teamId?: string | number | null) => {
  const normalizedTeamId = normalizeDutyTeamId(teamId)
  formData.dutyTeamId = normalizedTeamId as any
  const option = dutyTeamOptions.value.find((item) => item.id === normalizedTeamId)
  formData.dutyTeamName = option?.name || ''
  await syncDutyTeamDetail(normalizedTeamId)
}

const resetFormData = () => {
  Object.assign(formData, createEmptyFormData())
  formRunTimeRange.value = []
  inspectionResultItems.value = []
  submitAbnormalReminderVisible.value = false
  submitAbnormalReminderAutoCreateFaultRecords.value = false
  submitAbnormalReminderItems.value = []
  standardOptions.value = []
  standardKeyword.value = ''
  resetDutyTeamDetail()
  formRef.value?.clearValidate()
}

const appendCurrentDispatchOption = () => {
  if (!formData.dispatchInstructionId) return
  if (dispatchOptions.value.some((item) => item.id === formData.dispatchInstructionId)) return
  dispatchOptions.value.unshift({
    id: formData.dispatchInstructionId as number,
    instructionNo: formData.dispatchInstructionNo,
    instructionName: formData.dispatchInstructionName,
    title: formData.dispatchInstructionName || `调令#${formData.dispatchInstructionId}`
  })
}

const fillDefaults = async () => {
  const defaults = await RunLogApi.getDefaults()
  const defaultDutyTeamId = normalizeDutyTeamId(defaults.dutyTeamId as any)
  const dutyTeamExists = dutyTeamOptions.value.some((item) => item.id === defaultDutyTeamId)
  formData.dutyTeamId = dutyTeamExists ? (defaultDutyTeamId as any) : undefined
  formData.dutyTeamName = dutyTeamExists
    ? dutyTeamOptions.value.find((item) => item.id === defaultDutyTeamId)?.name || defaults.dutyTeamName || ''
    : ''
  formData.recorderUserId = defaults.recorderUserId
  formData.recorderUserName = defaults.recorderUserName || ''
  formData.recordTime = normalizeDateTimeValue(defaults.recordTime) || dayjs().format('YYYY-MM-DD HH:mm:ss')
  await syncDutyTeamDetail(formData.dutyTeamId as any)
}

const openForm = async (mode: FormMode, id?: number) => {
  formMode.value = mode
  formVisible.value = true
  resetFormData()
  formLoading.value = true
  try {
    if (!dutyTeamOptions.value.length) {
      await loadDutyTeamOptions()
    }
    if (mode === 'create') {
      await fillDefaults()
      await loadDispatchOptions()
      return
    }
    if (!id) return

    const data = await RunLogApi.get(id)
    Object.assign(formData, {
      ...createEmptyFormData(),
      ...data,
      attachments: data.attachments || []
    })
    formData.recordTime = normalizeDateTimeValue(data.recordTime)
    formData.runStartTime = normalizeDateTimeValue(data.runStartTime)
    formData.runEndTime = normalizeDateTimeValue(data.runEndTime)
    formData.dutyTeamId = normalizeDutyTeamId(data.dutyTeamId as any) as any
    formData.stationId = data.stationId ? String(data.stationId) : ''
    formData.inspectionType = data.inspectionType || ''
    formData.inspectionStandardId = data.inspectionStandardId
    formData.inspectionStandardName = data.inspectionStandardName || ''
    formData.inspectionResultItems = (data.inspectionResultItems || []) as any

    if (formData.dutyTeamId) {
      const dutyTeamExists = dutyTeamOptions.value.some((item) => item.id === formData.dutyTeamId)
      if (!dutyTeamExists && formData.dutyTeamName) {
        dutyTeamOptions.value.unshift({
          id: String(formData.dutyTeamId),
          name: formData.dutyTeamName
        })
      }
    }
    await syncDutyTeamDetail(formData.dutyTeamId as any)
    formRunTimeRange.value =
      formData.runStartTime && formData.runEndTime
        ? [String(formData.runStartTime), String(formData.runEndTime)]
        : []
    await loadDispatchOptions()
    appendCurrentDispatchOption()
    await loadStandardOptions()
    if (formData.inspectionStandardId) {
      await loadInspectionResultByStandard(formData.inspectionStandardId, formData.inspectionResultItems)
    }
  } finally {
    formLoading.value = false
  }
}

const doSubmitForm = async (autoCreateFaultRecords: boolean) => {
  submitLoading.value = true
  try {
    const payload: RunLogVO = {
      id: formData.id,
      logNo: formData.logNo,
      taskName: formData.taskName,
      dutyTeamId: formData.dutyTeamId ? String(formData.dutyTeamId) : undefined,
      dutyTeamName: formData.dutyTeamName,
      recorderUserId: formData.recorderUserId,
      recorderUserName: formData.recorderUserName,
      recordTime: formData.recordTime,
      runStartTime: formData.runStartTime,
      runEndTime: formData.runEndTime,
      checkPeriod: formData.checkPeriod,
      stationId: formData.stationId,
      inspectionType: formData.inspectionType,
      inspectionStandardId: formData.inspectionStandardId,
      inspectionStandardName: formData.inspectionStandardName,
      inspectionResultItems: buildSubmitInspectionResultItems(),
      autoCreateFaultRecords,
      dispatchInstructionId: formData.dispatchInstructionId,
      dispatchInstructionNo: formData.dispatchInstructionNo,
      dispatchInstructionName: formData.dispatchInstructionName,
      attachments: formData.attachments || [],
      remark: formData.remark
    }
    if (formMode.value === 'create') {
      delete payload.id
      await RunLogApi.create(payload)
      message.success('新增成功')
    } else {
      await RunLogApi.update(payload)
      message.success('更新成功')
    }
    submitAbnormalReminderVisible.value = false
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const confirmSubmitFormWithReminder = async () => {
  if (!submitAbnormalReminderAutoCreateFaultRecords.value) {
    message.warning('请先勾选“以上异常项将自动生成故障记录”后再保存')
    return
  }
  await doSubmitForm(true)
}

const submitForm = async () => {
  if (formReadonly.value) return
  formData.inspectionResultItems = inspectionResultItems.value
  await formRef.value?.validate()

  const [runStartTime, runEndTime] = formRunTimeRange.value
  formData.runStartTime = runStartTime
  formData.runEndTime = runEndTime

  if (formMode.value !== 'create') {
    await doSubmitForm(false)
    return
  }

  const abnormalItems = buildSubmitAbnormalReminderItems()
  if (!abnormalItems.length) {
    await doSubmitForm(false)
    return
  }
  submitAbnormalReminderItems.value = abnormalItems
  submitAbnormalReminderAutoCreateFaultRecords.value = false
  submitAbnormalReminderVisible.value = true
}

let autoRefreshTimer: ReturnType<typeof setInterval> | undefined

onMounted(async () => {
  await Promise.all([loadDutyTeamOptions(), getList()])
  autoRefreshTimer = setInterval(() => {
    getList()
  }, 30000)
})

onBeforeUnmount(() => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = undefined
  }
})
</script>

<style scoped lang="scss">
.run-log-page {
  .query-wrap {
    border: 1px solid rgba(148, 163, 184, 0.32);
    border-radius: 14px;
    background: #ffffff;
    box-shadow: 0 8px 28px rgba(15, 23, 42, 0.08);
    backdrop-filter: blur(10px);
  }

  .table-wrap {
    border-radius: 12px;
  }

  .log-no {
    display: inline-flex;
    align-items: center;
    height: 24px;
    padding: 0 10px;
    border-radius: 999px;
    border: 1px solid #d7e8ff;
    background: linear-gradient(135deg, #eff6ff, #f8fbff);
    color: #1d4ed8;
    font-weight: 600;
    font-size: 12px;
  }

  .form-section {
    padding: 14px 14px 2px;
    border: 1px solid #e8edf6;
    border-radius: 10px;
    margin-bottom: 14px;
    background: #fcfdff;
  }

  .section-title {
    margin-bottom: 12px;
    font-size: 14px;
    font-weight: 700;
    color: #1e3a5f;
  }

  .team-member-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    width: 100%;
    min-height: 32px;
    align-items: center;
  }

  .team-member-tag {
    margin: 0;
  }

  .attachment-block {
    width: 100%;
  }

  .attachment-tip {
    margin: 6px 0 0;
    font-size: 12px;
    color: #6b7280;
  }

  .inspection-result-container {
    width: 100%;
  }

  .inspection-result-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .result-item-card {
    border: 1px solid #dbeafe;
    border-radius: 10px;
    background: linear-gradient(180deg, #ffffff, #f8fbff);
    padding: 12px;
  }

  .result-item-card__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;
  }

  .result-item-card__title {
    font-size: 14px;
    font-weight: 700;
    color: #1e3a5f;
  }

  .result-item-card__target {
    color: #475569;
    font-size: 12px;
  }

  .result-item-card__summary {
    margin-bottom: 10px;
    padding: 10px 12px;
    border-radius: 8px;
    background: #eff6ff;
    border: 1px solid #dbeafe;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    flex-wrap: wrap;
  }

  .result-item-card__summary-item {
    color: #334155;
    font-size: 13px;
    line-height: 1.4;
  }

  .result-item-card__records-table-wrap {
    margin-bottom: 10px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    overflow: hidden;
    background: #ffffff;
  }

  .result-item-card__records-table {
    width: 100%;
    border-collapse: collapse;

    th {
      background: #f8fafc;
      color: #334155;
      font-weight: 600;
      font-size: 13px;
      text-align: left;
      padding: 10px 12px;
      border-bottom: 1px solid #e2e8f0;
    }

    td {
      color: #0f172a;
      font-size: 13px;
      padding: 10px 12px;
      border-bottom: 1px solid #edf2f7;
      vertical-align: middle;
    }

    tr:last-child td {
      border-bottom: none;
    }

    th:nth-child(1),
    td:nth-child(1) {
      width: 28%;
      min-width: 150px;
    }

    th:nth-child(2),
    td:nth-child(2) {
      width: 22%;
      min-width: 120px;
    }

    th:nth-child(3),
    td:nth-child(3) {
      width: 10%;
      min-width: 80px;
    }

    th:nth-child(4),
    td:nth-child(4) {
      width: 40%;
      min-width: 220px;
    }
  }

  .result-item-card__result {
    margin-bottom: 10px;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .result-item-card__remark-label {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 6px;
  }

  .result-item-card__required {
    color: #dc2626;
    font-size: 14px;
    font-weight: 700;
    line-height: 1;
  }

  .result-item-card__label {
    color: #0f172a;
    font-weight: 600;
  }

  .result-level-grid {
    width: 100%;
    display: grid;
    gap: 10px;
    grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  }

  .result-level-card {
    --result-card-border: #dbe3f0;
    --result-card-bg: #f8fafc;
    --result-card-hover-border: #cbd5e1;
    --result-card-hover-bg: #f1f5f9;
    --result-card-shadow: rgba(15, 23, 42, 0.1);
    --result-card-title: #0f172a;
    --result-card-desc: #64748b;
    --result-card-active-border: #94a3b8;
    --result-card-active-bg: #f1f5f9;
    --result-card-active-shadow: rgba(148, 163, 184, 0.35);
    --result-card-active-title: #0f172a;
    --result-card-active-desc: #334155;
    --result-card-focus-ring: rgba(148, 163, 184, 0.25);
    border: 1px solid var(--result-card-border);
    border-radius: 10px;
    padding: 10px 12px;
    background: var(--result-card-bg);
    transition: all 0.18s ease;
    cursor: pointer;
    outline: none;
    min-height: 74px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 6px;

    &:hover {
      border-color: var(--result-card-hover-border);
      background: var(--result-card-hover-bg);
      box-shadow: 0 4px 12px var(--result-card-shadow);
    }

    &:focus-visible {
      border-color: var(--result-card-active-border);
      box-shadow: 0 0 0 2px var(--result-card-focus-ring);
    }
  }

  .result-level-card__title {
    color: var(--result-card-title);
    font-size: 13px;
    font-weight: 700;
    line-height: 1.3;
  }

  .result-level-card__desc {
    color: var(--result-card-desc);
    font-size: 12px;
    line-height: 1.35;
  }

  .result-level-card--active {
    border-color: var(--result-card-active-border);
    background: var(--result-card-active-bg);
    box-shadow: 0 0 0 1px var(--result-card-active-shadow);

    .result-level-card__title {
      color: var(--result-card-active-title);
    }

    .result-level-card__desc {
      color: var(--result-card-active-desc);
    }
  }

  .result-level-card--readonly {
    cursor: default;

    &:hover {
      border-color: var(--result-card-border);
      background: var(--result-card-bg);
      box-shadow: none;
    }
  }

  .result-level-card--tone-success {
    --result-card-border: #bbf7d0;
    --result-card-bg: #f0fdf4;
    --result-card-hover-border: #86efac;
    --result-card-hover-bg: #dcfce7;
    --result-card-shadow: rgba(22, 163, 74, 0.14);
    --result-card-title: #166534;
    --result-card-desc: #3f6212;
    --result-card-active-border: #22c55e;
    --result-card-active-bg: #dcfce7;
    --result-card-active-shadow: rgba(34, 197, 94, 0.32);
    --result-card-active-title: #166534;
    --result-card-active-desc: #365314;
    --result-card-focus-ring: rgba(34, 197, 94, 0.24);
  }

  .result-level-card--tone-primary {
    --result-card-border: #bfdbfe;
    --result-card-bg: #eff6ff;
    --result-card-hover-border: #93c5fd;
    --result-card-hover-bg: #dbeafe;
    --result-card-shadow: rgba(37, 99, 235, 0.14);
    --result-card-title: #1e3a8a;
    --result-card-desc: #1e40af;
    --result-card-active-border: #3b82f6;
    --result-card-active-bg: #dbeafe;
    --result-card-active-shadow: rgba(59, 130, 246, 0.3);
    --result-card-active-title: #1d4ed8;
    --result-card-active-desc: #1e40af;
    --result-card-focus-ring: rgba(59, 130, 246, 0.24);
  }

  .result-level-card--tone-warning {
    --result-card-border: #fde68a;
    --result-card-bg: #fffbeb;
    --result-card-hover-border: #fcd34d;
    --result-card-hover-bg: #fef3c7;
    --result-card-shadow: rgba(217, 119, 6, 0.14);
    --result-card-title: #92400e;
    --result-card-desc: #b45309;
    --result-card-active-border: #f59e0b;
    --result-card-active-bg: #fef3c7;
    --result-card-active-shadow: rgba(245, 158, 11, 0.3);
    --result-card-active-title: #92400e;
    --result-card-active-desc: #92400e;
    --result-card-focus-ring: rgba(245, 158, 11, 0.24);
  }

  .result-level-card--tone-danger {
    --result-card-border: #fecaca;
    --result-card-bg: #fef2f2;
    --result-card-hover-border: #fca5a5;
    --result-card-hover-bg: #fee2e2;
    --result-card-shadow: rgba(220, 38, 38, 0.14);
    --result-card-title: #991b1b;
    --result-card-desc: #b91c1c;
    --result-card-active-border: #ef4444;
    --result-card-active-bg: #fee2e2;
    --result-card-active-shadow: rgba(239, 68, 68, 0.3);
    --result-card-active-title: #991b1b;
    --result-card-active-desc: #b91c1c;
    --result-card-focus-ring: rgba(239, 68, 68, 0.24);
  }

  .result-level-card--tone-info {
    --result-card-border: #dbe3f0;
    --result-card-bg: #f8fafc;
    --result-card-hover-border: #cbd5e1;
    --result-card-hover-bg: #f1f5f9;
    --result-card-shadow: rgba(71, 85, 105, 0.12);
    --result-card-title: #334155;
    --result-card-desc: #64748b;
    --result-card-active-border: #94a3b8;
    --result-card-active-bg: #f1f5f9;
    --result-card-active-shadow: rgba(148, 163, 184, 0.32);
    --result-card-active-title: #334155;
    --result-card-active-desc: #475569;
    --result-card-focus-ring: rgba(148, 163, 184, 0.24);
  }

  .submit-abnormal-reminder__desc {
    margin-bottom: 12px;
    font-size: 13px;
    color: #334155;
  }

  .submit-abnormal-reminder-table {
    margin-bottom: 12px;
  }

  .submit-abnormal-reminder__checkbox {
    margin: 2px 0 4px;
  }

  @media (max-width: 900px) {
    .result-item-card__summary {
      flex-direction: column;
      align-items: flex-start;
    }

    .result-item-card__records-table-wrap {
      overflow-x: auto;
    }

    .result-item-card__records-table {
      min-width: 620px;
    }

    .result-level-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 640px) {
    .result-level-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
