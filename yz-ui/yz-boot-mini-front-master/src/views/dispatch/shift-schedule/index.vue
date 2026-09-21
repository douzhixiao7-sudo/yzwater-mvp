<template>
  <div class="shift-schedule-page">
    <ContentWrap class="query-wrap">
      <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
        <el-form-item label="值班日期" prop="scheduleDateRange">
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
        <el-form-item label="值班人员" prop="dutyUserId">
          <el-select
            v-model="queryParams.dutyUserId"
            clearable
            filterable
            class="!w-220px"
            placeholder="请选择值班人员"
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
              :label="buildFormTeamLabel(team)"
              :value="team.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="值班班次" prop="shiftId">
          <el-select
            v-model="queryParams.shiftId"
            clearable
            filterable
            class="!w-220px"
            placeholder="请选择值班班次"
          >
            <el-option
              v-for="shift in shiftOptions"
              :key="shift.id"
              :label="buildFormShiftLabel(shift)"
              :value="shift.id"
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
          <el-button v-hasPermi="['iot:shift-schedule:create']" type="primary" plain @click="openForm('create')">
            <Icon icon="ep:plus" class="mr-4px" />
            添加排班
          </el-button>
          <el-button v-hasPermi="['iot:shift-schedule:import']" type="warning" plain @click="openImportDialog">
            <Icon icon="ep:upload" class="mr-4px" />
            批量导入
          </el-button>
          <el-button
            v-hasPermi="['iot:shift-schedule:export']"
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

    <ContentWrap class="calendar-wrap">
      <el-calendar v-model="calendarValue">
        <template #header>
          <div class="calendar-header">
            <el-button class="month-nav-btn" text @click="handlePrevMonth">
              <Icon icon="ep:arrow-left" />
            </el-button>
            <span class="calendar-header__title">{{ calendarTitle }}</span>
            <el-button class="month-nav-btn" text @click="handleNextMonth">
              <Icon icon="ep:arrow-right" />
            </el-button>
            <el-button class="today-btn" plain @click="handleCurrentMonth">回到本月</el-button>
          </div>
        </template>
        <template #date-cell="{ data }">
          <div
            class="calendar-cell"
            :class="{ 'calendar-cell--other-month': data.type !== 'current-month' }"
            @click="openDayDetail(data.day)"
          >
            <div class="calendar-cell__day">{{ dayjs(data.day).format('D') }}</div>
            <div class="calendar-cell__list">
              <template v-if="getDaySchedules(data.day).length">
                <div
                  v-for="(item, index) in getDaySchedules(data.day).slice(0, 3)"
                  :key="`item-${item.id}-${index}`"
                  class="calendar-item"
                  :class="`calendar-item--${index % 3}`"
                >
                  {{ formatCalendarItem(item) }}
                </div>
                <div v-if="getDaySchedules(data.day).length > 3" class="calendar-item calendar-item--more">
                  +{{ getDaySchedules(data.day).length - 3 }} 条
                </div>
              </template>
            </div>
          </div>
        </template>
      </el-calendar>
    </ContentWrap>

    <ContentWrap class="table-wrap">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="排班编号" prop="scheduleNo" min-width="140" show-overflow-tooltip />
        <el-table-column label="值班日期" min-width="120">
          <template #default="{ row }">{{ formatScheduleDate(row.scheduleDate) }}</template>
        </el-table-column>
        <el-table-column label="所属站点" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ resolveStationLabel(row.stationId) }}</template>
        </el-table-column>
        <el-table-column label="值班班次" prop="shiftName" min-width="140" show-overflow-tooltip />
        <el-table-column label="值班班组" prop="teamName" min-width="140" show-overflow-tooltip />
        <el-table-column label="值班人员" prop="dutyUserName" min-width="120" show-overflow-tooltip />
        <el-table-column label="组长联系方式" prop="dutyMobile" min-width="130" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="resolveStatusType(row.status)">{{ resolveStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="info" @click="openForm('detail', row.id)">详情</el-button>
            <el-button v-hasPermi="['iot:shift-schedule:update']" link type="primary" @click="openForm('update', row.id)">
              编辑
            </el-button>
            <el-button v-hasPermi="['iot:shift-schedule:delete']" link type="danger" @click="handleDelete(row.id)">
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

    <Dialog v-model="detailVisible" :title="detailTitle" width="760px">
      <div class="detail-box">
        <template v-if="dayDetailList.length">
          <div v-for="item in dayDetailList" :key="item.id" class="detail-card">
            <div class="detail-card__header">
              <div class="detail-card__title-wrap">
                <span class="detail-card__title">{{ item.shiftName || '-' }}</span>
                <span class="detail-card__split">/</span>
                <span class="detail-card__team">{{ item.teamName || '-' }}</span>
              </div>
              <el-tag size="small" :type="resolveStatusType(item.status)">
                {{ resolveStatusName(item.status) }}
              </el-tag>
            </div>
            <div class="detail-card__period">
              <Icon icon="ep:clock" />
              <span>{{ formatDutyPeriod(item) }}</span>
            </div>
            <div class="detail-card__meta">
              <div class="detail-meta-item">
                <span class="detail-meta-item__label">值班人员</span>
                <span class="detail-meta-item__value">{{ item.dutyUserName || '-' }}</span>
              </div>
              <div class="detail-meta-item">
                <span class="detail-meta-item__label">组长名称</span>
                <span class="detail-meta-item__value">{{ resolveDutyLeaderName(item) }}</span>
              </div>
              <div class="detail-meta-item">
                <span class="detail-meta-item__label">组长联系方式</span>
                <span class="detail-meta-item__value">{{ item.dutyMobile || '-' }}</span>
              </div>
              <div class="detail-meta-item">
                <span class="detail-meta-item__label">所属站点</span>
                <span class="detail-meta-item__value">{{ resolveStationLabel(item.stationId) }}</span>
              </div>
            </div>
            <div class="detail-card__block">
              <div class="detail-card__block-title">值班描述</div>
              <div class="detail-card__block-content">{{ item.dutyLog || '-' }}</div>
            </div>
            <div class="detail-card__footer">
              <el-button link type="info" @click="openForm('detail', item.id)">详情</el-button>
              <el-button v-hasPermi="['iot:shift-schedule:update']" link type="primary" @click="openForm('update', item.id)">
                编辑
              </el-button>
              <el-button v-hasPermi="['iot:shift-schedule:delete']" link type="danger" @click="handleDelete(item.id)">
                删除
              </el-button>
            </div>
          </div>
        </template>
        <el-empty v-else description="当日暂无排班记录" />
      </div>
    </Dialog>

    <Dialog v-model="formVisible" :title="formTitle" width="860px">
      <el-form
        ref="formRef"
        v-loading="formLoading"
        :model="formData"
        :rules="formRules"
        :disabled="formReadonly"
        label-width="96px"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排班编号">
              <el-input :model-value="formData.scheduleNo || '系统自动生成'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属站点" prop="stationId">
              <el-select
                v-model="formData.stationId"
                class="!w-full"
                filterable
                clearable
                placeholder="请选择所属站点"
                :disabled="formReadonly"
                @change="handleFormStationChange"
              >
                <el-option
                  v-for="station in stationOptions"
                  :key="String(station.value)"
                  :label="station.label"
                  :value="String(station.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="值班日期" prop="scheduleDateRange">
              <el-date-picker
                v-model="formData.scheduleDateRange"
                class="!w-full shift-date-range-picker"
                type="datetimerange"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                :editable="true"
                unlink-panels
                :default-time="defaultFormDateRangeTime"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="值班班次" prop="shiftId">
              <el-select
                v-model="formData.shiftId"
                class="!w-full"
                filterable
                clearable
                :loading="shiftOptionsLoading"
                placeholder="请选择值班班次"
              >
                <el-option
                  v-for="shift in shiftOptions"
                  :key="shift.id"
                  :label="buildFormShiftLabel(shift)"
                  :value="shift.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="值班班组" prop="teamId">
              <el-select
                v-model="formData.teamId"
                class="!w-full"
                filterable
                clearable
                :loading="teamOptionsLoading"
                placeholder="请选择值班班组"
              >
                <el-option
                  v-for="team in teamOptions"
                  :key="team.id"
                  :label="buildFormTeamLabel(team)"
                  :value="team.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="值班人员" prop="dutyUserId">
              <el-input
                :model-value="formData.dutyUserName || ''"
                class="!w-full"
                type="textarea"
                :rows="2"
                readonly
                disabled
                placeholder="选择值班班组后自动回填班组成员"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组长名称">
              <el-input
                v-model="formData.dutyLeaderName"
                placeholder="选择值班班组后自动回填组长名称"
                readonly
                disabled
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组长联系方式" prop="dutyMobile">
              <el-input
                v-model="formData.dutyMobile"
                placeholder="选择值班班组后自动回填组长联系方式"
                readonly
                disabled
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="值班描述" prop="dutyLog">
              <el-input
                v-model="formData.dutyLog"
                type="textarea"
                :rows="4"
                maxlength="2000"
                show-word-limit
                placeholder="请填写当班值班描述"
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

    <Dialog v-model="importVisible" title="批量导入排班" width="520px">
      <el-upload
        class="import-uploader"
        drag
        action="#"
        :auto-upload="false"
        :file-list="importFileList"
        :limit="1"
        accept=".xls,.xlsx"
        :on-change="handleImportFileChange"
        :on-remove="handleImportFileRemove"
        :on-exceed="handleImportExceed"
      >
        <Icon icon="ep:upload" class="mb-8px" />
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">
            <div>仅允许导入 xls、xlsx 格式文件。</div>
            <el-link
              v-hasPermi="['iot:shift-schedule:import-template']"
              :underline="false"
              type="primary"
              @click="downloadImportTemplate"
            >
              下载导入模板
            </el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="submitImport">开始导入</el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import * as UserApi from '@/api/system/user'
import type { UserVO } from '@/api/system/user'
import { ShiftTeamApi, type ShiftTeamMemberVO } from '@/api/iot/shift/team'
import {
  ShiftScheduleApi,
  type ShiftScheduleCalendarReqVO,
  type ShiftSchedulePageReqVO,
  type ShiftScheduleShiftOptionVO,
  type ShiftScheduleTeamOptionVO,
  type ShiftScheduleVO
} from '@/api/iot/shift/schedule'
import type { FormInstance, FormRules, UploadFile, UploadFiles } from 'element-plus'

defineOptions({ name: 'IotShiftSchedule' })

type FormMode = 'create' | 'update' | 'detail'
type ShiftScheduleFormVO = ShiftScheduleVO & {
  scheduleDateRange?: string[]
  dutyLeaderName?: string
}

type FormDutyUserOption = {
  id: string
  userName: string
  mobile?: string
  leader?: boolean
}

const message = useMessage()

const loading = ref(false)
const exportLoading = ref(false)
const list = ref<ShiftScheduleVO[]>([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()
const queryDateRange = ref<string[]>([])
const queryParams = reactive<ShiftSchedulePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  keyword: undefined,
  teamId: undefined,
  shiftId: undefined,
  dutyUserId: undefined,
  dutyUserName: undefined,
  status: undefined,
  scheduleDateRange: undefined
})

const shiftOptions = ref<ShiftScheduleShiftOptionVO[]>([])
const teamOptions = ref<ShiftScheduleTeamOptionVO[]>([])
const userOptions = ref<UserVO[]>([])
const formDutyUserOptions = ref<FormDutyUserOption[]>([])
const teamUserLoading = ref(false)
const shiftOptionsLoading = ref(false)
const teamOptionsLoading = ref(false)

const teamUserCache = new Map<string, FormDutyUserOption[]>()
const userDetailCache = new Map<string, UserVO>()
let teamUserRequestId = 0

const allUserMap = computed(() => {
  const map = new Map<string, UserVO>()
  ;(userOptions.value || []).forEach((user) => {
    const id = normalizeId(user.id)
    if (id) {
      map.set(id, user)
    }
  })
  return map
})

const calendarValue = ref(new Date())
const calendarList = ref<ShiftScheduleVO[]>([])
const stationOptions = computed(() => {
  return getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD).map((dict) => ({
    label: dict.label,
    value: String(dict.value)
  }))
})

const calendarTitle = computed(() => dayjs(calendarValue.value).format('YYYY年M月'))

const normalizeDateKey = (value: unknown) => {
  if (value === undefined || value === null || value === '') return ''
  if (Array.isArray(value)) {
    if (value.length < 3) return ''
    const [year, month, day] = value
    const y = Number(year)
    const m = Number(month)
    const d = Number(day)
    if (!Number.isFinite(y) || !Number.isFinite(m) || !Number.isFinite(d)) return ''
    return `${String(y).padStart(4, '0')}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
  }

  const raw = String(value).trim()
  if (!raw) return ''
  const dateMatch = raw.match(/^(\d{4})[-/](\d{1,2})[-/](\d{1,2})/)
  if (dateMatch) {
    return `${dateMatch[1]}-${dateMatch[2].padStart(2, '0')}-${dateMatch[3].padStart(2, '0')}`
  }
  const parsed = dayjs(raw)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD') : ''
}

const formatScheduleDate = (value: unknown) => normalizeDateKey(value) || '-'

const calendarMap = computed(() => {
  const map = new Map<string, ShiftScheduleVO[]>()
  ;(calendarList.value || []).forEach((item) => {
    const key = normalizeDateKey(item.scheduleDate)
    if (!key) return
    const current = map.get(key) || []
    current.push(item)
    map.set(key, current)
  })
  return map
})

const getDaySchedules = (day: string) => {
  const key = normalizeDateKey(day)
  if (!key) return []
  return calendarMap.value.get(key) || []
}

const parseShiftTime = (time?: string) => {
  if (!time) return null
  const matched = String(time)
    .trim()
    .match(/^(\d{1,2})\D+(\d{1,2})(?:\D+\d{1,2})?$/)
  if (!matched) return null
  const hour = Number(matched[1])
  const minute = Number(matched[2])
  if (!Number.isInteger(hour) || !Number.isInteger(minute)) return null
  if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null
  return { hour, minute }
}

const formatShiftPointText = (time?: string) => {
  const parsed = parseShiftTime(time)
  if (!parsed) return String(time || '--')
  const period = parsed.hour < 12 ? '上午' : '下午'
  const minute = String(parsed.minute).padStart(2, '0')
  return `${period}${parsed.hour}点${minute}`
}

const buildFormShiftLabel = (shift: ShiftScheduleShiftOptionVO) => {
  if (!shift) return '-'
  if (!shift.startTime || !shift.endTime) {
    return shift.shiftName || '-'
  }
  const startText = formatShiftPointText(shift.startTime)
  const endText = formatShiftPointText(shift.endTime)
  const period = shift.crossDay ? `${startText}到次日${endText}` : `${startText}到${endText}`
  return `${shift.shiftName || '-'}（${period}）`
}

const buildFormTeamLabel = (team: ShiftScheduleTeamOptionVO) => {
  if (!team) return '-'
  return team.teamName || '-'
}

const buildUserLabel = (user?: UserVO | null) => {
  if (!user) return '-'
  return user.nickname || user.username || String(user.id)
}

const resolveScheduleDateValue = (value: unknown, fallback?: unknown) => {
  return normalizeDateKey(value) || normalizeDateKey(fallback) || ''
}

const normalizeDateTimeMinute = (value: unknown) => {
  if (value === undefined || value === null || value === '') return ''
  const raw = typeof value === 'string' && /^\d+$/.test(value) ? Number(value) : value
  const timestamp = typeof raw === 'number' && raw > 0 && raw < 1000000000000 ? raw * 1000 : raw
  const parsed = dayjs(timestamp)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : ''
}

const normalizeDateTimeSecond = (value: unknown) => {
  return normalizeDateTimeMinute(value)
}

const resolveScheduleDateRangeValue = (value: unknown, fallbackStart?: unknown, fallbackEnd?: unknown) => {
  if (Array.isArray(value) && value.length === 2) {
    const start = normalizeDateTimeMinute(value[0])
    const end = normalizeDateTimeMinute(value[1])
    if (start && end) {
      return dayjs(start).isAfter(dayjs(end)) ? [end, start] : [start, end]
    }
  }
  const fallbackStartDate = resolveScheduleDateValue(fallbackStart)
  const fallbackEndDate = resolveScheduleDateValue(fallbackEnd)
  const startFallback =
    normalizeDateTimeMinute(fallbackStart) || (fallbackStartDate ? `${fallbackStartDate} 00:00:00` : '')
  const endFallback =
    normalizeDateTimeMinute(fallbackEnd) || (fallbackEndDate ? `${fallbackEndDate} 23:59:59` : '')
  return startFallback && endFallback ? [startFallback, endFallback] : []
}

const normalizeId = (value?: string | number | null) => {
  if (value === undefined || value === null) return undefined
  const id = String(value).trim()
  if (!id || id === 'undefined' || id === 'null') return undefined
  return id
}

const resolveStationLabel = (stationId?: string | number | null) => {
  const normalizedStationId = normalizeId(stationId)
  if (!normalizedStationId) return '-'
  return (
    stationOptions.value.find((item) => String(item.value) === normalizedStationId)?.label ||
    normalizedStationId
  )
}

const resolveDutyLeaderName = (item: ShiftScheduleVO) => {
  const dutyLeaderName = String(item.dutyLeaderName || '').trim()
  if (dutyLeaderName) return dutyLeaderName
  const leaderUserId = normalizeId(item.dutyUserId)
  if (!leaderUserId) return '-'
  const leaderUser = allUserMap.value.get(leaderUserId)
  const leaderName = String(leaderUser?.nickname || leaderUser?.username || '').trim()
  return leaderName || leaderUserId
}

const buildFormDutyUserOption = (member: ShiftTeamMemberVO): FormDutyUserOption | null => {
  const id = normalizeId(member.userId)
  if (!id) return null
  const localUser = allUserMap.value.get(id)
  return {
    id,
    userName: member.userName || localUser?.nickname || localUser?.username || String(id),
    mobile: member.mobile || localUser?.mobile || '',
    leader: !!member.leader
  }
}

const loadFormDutyUsersByTeam = async (teamId?: string | number) => {
  const normalizedTeamId = normalizeId(teamId)
  if (!normalizedTeamId) {
    formDutyUserOptions.value = []
    return
  }
  const requestId = ++teamUserRequestId
  if (teamUserCache.has(normalizedTeamId)) {
    formDutyUserOptions.value = teamUserCache.get(normalizedTeamId) || []
    return
  }

  teamUserLoading.value = true
  try {
    const teamDetail = await ShiftTeamApi.get(normalizedTeamId)
    if (requestId !== teamUserRequestId) {
      return
    }
    const options = (teamDetail.members || [])
      .map((item) => buildFormDutyUserOption(item))
      .filter((item): item is FormDutyUserOption => !!item)
      .sort((a, b) => {
        if (!!a.leader === !!b.leader) {
          return a.userName.localeCompare(b.userName, 'zh-Hans-CN')
        }
        return a.leader ? -1 : 1
      })
    teamUserCache.set(normalizedTeamId, options)
    formDutyUserOptions.value = options
  } catch (error) {
    if (requestId === teamUserRequestId) {
      formDutyUserOptions.value = []
      message.warning('获取班组成员失败，请确认班组是否有效')
    }
  } finally {
    if (requestId === teamUserRequestId) {
      teamUserLoading.value = false
    }
  }
}

const resolveTeamLeaderOption = () => {
  return formDutyUserOptions.value.find((item) => item.leader)
}

const clearDutyUserFormFields = () => {
  formData.dutyUserId = undefined
  formData.dutyUserName = ''
  formData.dutyLeaderName = ''
  formData.dutyMobile = ''
}

const resolveCurrentMonthRange = () => {
  const currentMonth = dayjs(calendarValue.value)
  return [currentMonth.startOf('month').format('YYYY-MM-DD'), currentMonth.endOf('month').format('YYYY-MM-DD')]
}

const buildPageParams = (): ShiftSchedulePageReqVO => {
  const dateRange = queryDateRange.value?.length === 2 ? queryDateRange.value : resolveCurrentMonthRange()
  return {
    ...queryParams,
    scheduleDateRange: dateRange
  }
}

const buildCalendarParams = (): ShiftScheduleCalendarReqVO => {
  return {
    month: dayjs(calendarValue.value).format('YYYY-MM'),
    keyword: queryParams.keyword,
    teamId: queryParams.teamId,
    shiftId: queryParams.shiftId,
    dutyUserId: queryParams.dutyUserId
  }
}

const getUserDetailById = async (userId?: string | number | null) => {
  const normalizedUserId = normalizeId(userId)
  if (!normalizedUserId) return undefined
  if (userDetailCache.has(normalizedUserId)) {
    return userDetailCache.get(normalizedUserId)
  }
  try {
    const user = await UserApi.getUser(normalizedUserId as any)
    if (user) {
      userDetailCache.set(normalizedUserId, user as UserVO)
    }
    return user as UserVO | undefined
  } catch (error) {
    return undefined
  }
}

const syncDutyInfoByTeam = async (teamId?: string | number | null) => {
  const normalizedTeamId = normalizeId(teamId)
  if (!normalizedTeamId) {
    formDutyUserOptions.value = []
    clearDutyUserFormFields()
    return
  }

  await loadFormDutyUsersByTeam(normalizedTeamId)
  if (!formDutyUserOptions.value.length) {
    clearDutyUserFormFields()
    return
  }

  const leader = resolveTeamLeaderOption()
  if (!leader) {
    clearDutyUserFormFields()
    if (!formReadonly.value) {
      message.warning('当前班组未配置组长，无法自动回填值班信息')
    }
    return
  }

  formData.dutyUserId = leader.id as any
  formData.dutyUserName = formDutyUserOptions.value
    .map((item) => String(item.userName || '').trim())
    .filter((item) => !!item)
    .join('、')
  const leaderUserId = normalizeId(leader.id)
  const leaderSimpleUser = leaderUserId ? allUserMap.value.get(leaderUserId) : undefined
  formData.dutyLeaderName = String(
    leader.userName || leaderSimpleUser?.nickname || leaderSimpleUser?.username || leader.id
  ).trim()
  formData.dutyMobile = String(leader.mobile || leaderSimpleUser?.mobile || '').trim()
  if (!formData.dutyMobile && leaderUserId) {
    const detailUser = await getUserDetailById(leaderUserId)
    if (normalizeId(formData.dutyUserId) === leaderUserId && detailUser) {
      formData.dutyMobile = String(detailUser.mobile || '').trim()
    }
  }
}

const alignViewToScheduleDate = (scheduleDate?: string) => {
  const target = dayjs(String(scheduleDate || ''))
  if (!target.isValid()) return

  const dateRange = queryDateRange.value
  if (Array.isArray(dateRange) && dateRange.length === 2 && dateRange[0] && dateRange[1]) {
    const start = dayjs(dateRange[0])
    const end = dayjs(dateRange[1])
    const inRange =
      start.isValid() &&
      end.isValid() &&
      !target.isBefore(start.startOf('day')) &&
      !target.isAfter(end.endOf('day'))
    if (!inRange) {
      // 当前日期筛选不包含新保存的排班，恢复为“按月历月份过滤”
      queryDateRange.value = []
    }
  }

  calendarValue.value = target.toDate()
  queryParams.pageNo = 1
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ShiftScheduleApi.getPage(buildPageParams())
    list.value = data.list || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

const getCalendarList = async () => {
  calendarList.value = await ShiftScheduleApi.getCalendar(buildCalendarParams())
}

const refreshAll = async () => {
  await Promise.all([getList(), getCalendarList()])
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await refreshAll()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  queryDateRange.value = []
  queryParams.pageNo = 1
  await refreshAll()
}

const handlePrevMonth = async () => {
  calendarValue.value = dayjs(calendarValue.value).subtract(1, 'month').toDate()
  await refreshAll()
}

const handleNextMonth = async () => {
  calendarValue.value = dayjs(calendarValue.value).add(1, 'month').toDate()
  await refreshAll()
}

const handleCurrentMonth = async () => {
  calendarValue.value = new Date()
  await refreshAll()
}

const formatDateTime = (value?: string | number) => {
  if (value === undefined || value === null || value === '') return ''
  const raw = typeof value === 'string' && /^\d+$/.test(value) ? Number(value) : value
  const timestamp = typeof raw === 'number' && raw > 0 && raw < 1000000000000 ? raw * 1000 : raw
  const parsed = dayjs(timestamp)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm') : ''
}

const formatCalendarItem = (item: ShiftScheduleVO) => {
  const startText = formatDateTime(item.dutyStartTime)
  const endText = formatDateTime(item.dutyEndTime)
  const startTime = startText ? startText.slice(11, 16) : '--:--'
  const endTime = endText ? endText.slice(11, 16) : '--:--'
  return `${startTime}-${endTime} ${item.dutyUserName || '-'}`
}

const formatDutyPeriod = (item: ShiftScheduleVO) => {
  const startText = formatDateTime(item.dutyStartTime)
  const endText = formatDateTime(item.dutyEndTime)
  if (startText && endText) {
    return `${startText} 至 ${endText}`
  }
  return startText || endText || '-'
}

const resolveStatusName = (status?: number) => {
  if (status === 1) return '值班中'
  if (status === 2) return '已完成'
  if (status === 3) return '已取消'
  return '待值班'
}

const resolveStatusType = (status?: number) => {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  if (status === 3) return 'danger'
  return 'warning'
}

const detailVisible = ref(false)
const selectedDay = ref('')
const dayDetailList = ref<ShiftScheduleVO[]>([])
const detailTitle = computed(() => `值班详情（${selectedDay.value || '-'}）`)

const openDayDetail = (day: string) => {
  const normalizedDay = normalizeDateKey(day)
  selectedDay.value = normalizedDay || day
  dayDetailList.value = getDaySchedules(day)
  detailVisible.value = true
}

const handleDelete = async (id?: number) => {
  if (!id) return
  await message.delConfirm('确认删除该排班记录吗？')
  await ShiftScheduleApi.remove(id)
  message.success('删除成功')
  if (detailVisible.value && selectedDay.value) {
    dayDetailList.value = dayDetailList.value.filter((item) => item.id !== id)
  }
  await refreshAll()
}

const handleExport = async () => {
  await message.exportConfirm()
  exportLoading.value = true
  try {
    const data = await ShiftScheduleApi.exportExcel(buildPageParams())
    download.excel(data, `员工排班表_${dayjs().format('YYYYMMDD_HHmmss')}.xls`)
  } finally {
    exportLoading.value = false
  }
}

const formVisible = ref(false)
const formMode = ref<FormMode>('create')
const formRef = ref<FormInstance>()
const formLoading = ref(false)
const submitLoading = ref(false)
const formInitializing = ref(false)
const defaultFormDateRangeTime = [
  new Date(2000, 0, 1, 0, 0, 0),
  new Date(2000, 0, 1, 23, 59, 59)
]

const createEmptyFormData = (): ShiftScheduleFormVO => ({
  id: undefined,
  scheduleNo: '',
  scheduleDate: '',
  scheduleDateRange: [],
  stationId: undefined,
  shiftId: undefined,
  shiftName: '',
  teamId: undefined,
  teamName: '',
  dutyUserId: undefined,
  dutyUserName: '',
  dutyLeaderName: '',
  dutyMobile: '',
  dutyLog: '',
  remark: ''
})
const formData = reactive<ShiftScheduleFormVO>(createEmptyFormData())

const formReadonly = computed(() => formMode.value === 'detail')
const formTitle = computed(() => {
  if (formMode.value === 'create') return '新增排班'
  if (formMode.value === 'update') return '编辑排班'
  return '排班详情'
})

const resetFormData = () => {
  Object.assign(formData, createEmptyFormData())
  formDutyUserOptions.value = []
  formRef.value?.clearValidate()
}

const validateStationId = (_rule: any, _value: string, callback: (error?: Error) => void) => {
  if (formReadonly.value) {
    callback()
    return
  }
  if (normalizeId(formData.stationId)) {
    callback()
    return
  }
  callback(new Error('所属站点不能为空'))
}

const validateScheduleDateRange = (_rule: any, _value: string, callback: (error?: Error) => void) => {
  if (formReadonly.value) {
    callback()
    return
  }
  const range = resolveScheduleDateRangeValue(formData.scheduleDateRange)
  if (range.length === 2) {
    callback()
    return
  }
  callback(new Error('值班日期不能为空'))
}

const formRules: FormRules<ShiftScheduleFormVO> = {
  stationId: [{ validator: validateStationId, trigger: 'change' }],
  scheduleDateRange: [{ validator: validateScheduleDateRange, trigger: 'change' }],
  shiftId: [{ required: true, message: '值班班次不能为空', trigger: 'change' }],
  teamId: [{ required: true, message: '值班班组不能为空', trigger: 'change' }],
  dutyUserId: [{ required: true, message: '值班人员不能为空', trigger: 'change' }]
}

watch(
  () => formData.teamId,
  (value) => {
    if (formInitializing.value) {
      return
    }
    void syncDutyInfoByTeam(value)
  }
)

const openForm = async (mode: FormMode, id?: number) => {
  formMode.value = mode
  formVisible.value = true
  detailVisible.value = false
  resetFormData()
  formInitializing.value = true
  await Promise.all([loadShiftOptions(), loadTeamOptions()])

  if (mode === 'create') {
    const defaultDate = resolveScheduleDateValue(
      selectedDay.value,
      dayjs(calendarValue.value).format('YYYY-MM-DD')
    )
    formData.scheduleDate = defaultDate
    formData.scheduleDateRange = defaultDate
      ? [`${defaultDate} 00:00:00`, `${defaultDate} 23:59:59`]
      : []
    formInitializing.value = false
    return
  }
  if (!id) {
    formInitializing.value = false
    return
  }

  formLoading.value = true
  try {
    const data = await ShiftScheduleApi.get(id)
    Object.assign(formData, {
      ...createEmptyFormData(),
      ...data
    })
    // 统一转为字符串，避免 el-select 因 number/string 类型不一致而回显为原始 ID
    formData.stationId = normalizeId(formData.stationId)
    formData.dutyUserId = normalizeId(formData.dutyUserId) as any
    await Promise.all([
      loadShiftOptions(false, formData.stationId),
      loadTeamOptions(false, formData.stationId)
    ])
    formData.dutyLeaderName = resolveDutyLeaderName(formData)
    formData.scheduleDate = resolveScheduleDateValue(data.scheduleDate, data.dutyStartTime)
    const dutyStart = normalizeDateTimeMinute(data.dutyStartTime)
    const dutyEnd = normalizeDateTimeMinute(data.dutyEndTime)
    formData.scheduleDateRange = resolveScheduleDateRangeValue(
      [dutyStart, dutyEnd],
      formData.scheduleDate,
      formData.scheduleDate
    )
    const normalizedShiftId = normalizeId(formData.shiftId)
    const shiftExists = normalizedShiftId
      ? shiftOptions.value.some((item) => normalizeId(item.id) === normalizedShiftId)
      : false
    if (!shiftExists && normalizedShiftId && formData.shiftName) {
      shiftOptions.value.unshift({
        id: formData.shiftId as any,
        shiftName: formData.shiftName
      })
    }
    const normalizedTeamId = normalizeId(formData.teamId)
    const teamExists = normalizedTeamId
      ? teamOptions.value.some((item) => normalizeId(item.id) === normalizedTeamId)
      : false
    if (!teamExists && normalizedTeamId && formData.teamName) {
      teamOptions.value.unshift({
        id: formData.teamId as any,
        teamName: formData.teamName
      })
    }
    await syncDutyInfoByTeam(formData.teamId)
  } finally {
    formLoading.value = false
    formInitializing.value = false
  }
}

const submitForm = async () => {
  if (formReadonly.value) return
  await formRef.value?.validate()

  submitLoading.value = true
  try {
    const payload: ShiftScheduleVO = {
      stationId: normalizeId(formData.stationId),
      shiftId: formData.shiftId,
      teamId: formData.teamId,
      dutyUserId: formData.dutyUserId,
      dutyMobile: String(formData.dutyMobile || '').trim(),
      dutyLog: String(formData.dutyLog || '').trim(),
      remark: String(formData.remark || '').trim()
    }

    let savedScheduleDate = ''
    const isCreateMode = formMode.value === 'create' && !formData.id
    if (isCreateMode) {
      const normalizedDateRange = resolveScheduleDateRangeValue(
        formData.scheduleDateRange,
        formData.scheduleDate,
        formData.scheduleDate
      )
      if (normalizedDateRange.length !== 2) {
        message.warning('值班日期不能为空')
        return
      }
      const scheduleDate = resolveScheduleDateValue(normalizedDateRange[0])
      if (!scheduleDate) {
        message.warning('值班日期不能为空')
        return
      }
      savedScheduleDate = scheduleDate
      await ShiftScheduleApi.create({
        ...payload,
        scheduleDate,
        dutyStartTime: normalizeDateTimeSecond(normalizedDateRange[0]),
        dutyEndTime: normalizeDateTimeSecond(normalizedDateRange[1])
      })
      message.success('新增成功')
    } else {
      if (!formData.id) {
        message.error('缺少排班ID，无法更新')
        return
      }
      const normalizedDateRange = resolveScheduleDateRangeValue(formData.scheduleDateRange)
      if (normalizedDateRange.length !== 2) {
        message.warning('值班日期不能为空')
        return
      }
      const scheduleDate = resolveScheduleDateValue(normalizedDateRange[0])
      if (!scheduleDate) {
        message.warning('值班日期不能为空')
        return
      }
      savedScheduleDate = scheduleDate
      await ShiftScheduleApi.update({
        ...payload,
        id: formData.id,
        scheduleDate,
        dutyStartTime: normalizeDateTimeSecond(normalizedDateRange[0]),
        dutyEndTime: normalizeDateTimeSecond(normalizedDateRange[1])
      })
      message.success('更新成功')
    }

    formVisible.value = false
    alignViewToScheduleDate(savedScheduleDate)
    await refreshAll()
  } finally {
    submitLoading.value = false
  }
}

const importVisible = ref(false)
const importLoading = ref(false)
const importFileList = ref<UploadFile[]>([])
const importRawFile = ref<File>()

const openImportDialog = () => {
  importVisible.value = true
  importFileList.value = []
  importRawFile.value = undefined
}

const handleImportFileChange = (file: UploadFile, fileList: UploadFiles) => {
  importFileList.value = fileList.slice(-1)
  importRawFile.value = file.raw
}

const handleImportFileRemove = () => {
  importRawFile.value = undefined
}

const handleImportExceed = () => {
  message.warning('最多只能上传一个文件')
}

const downloadImportTemplate = async () => {
  const data = await ShiftScheduleApi.getImportTemplate()
  download.excel(data, '员工排班导入模板.xls')
}

const submitImport = async () => {
  if (!importRawFile.value) {
    message.warning('请先上传导入文件')
    return
  }
  importLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', importRawFile.value)
    const result = await ShiftScheduleApi.importExcel(formData)
    const failureText =
      result.failureMessages && result.failureMessages.length
        ? `<br/>${result.failureMessages.map((item) => `- ${item}`).join('<br/>')}`
        : ''
    await message.alert(
      `导入完成：成功 ${result.successCount || 0} 条，失败 ${result.failureCount || 0} 条${failureText}`
    )
    importVisible.value = false
    await refreshAll()
  } finally {
    importLoading.value = false
  }
}

const loadBaseOptions = async () => {
  const users = await UserApi.getSimpleUserList({ excludeRoleName: '游客' })
  userOptions.value = users || []
  await Promise.all([loadShiftOptions(true), loadTeamOptions(true)])
}

const loadShiftOptions = async (silent = false, stationId?: string | number | null) => {
  shiftOptionsLoading.value = true
  try {
    const shifts = await ShiftScheduleApi.getShiftOptions(normalizeId(stationId))
    shiftOptions.value = shifts || []
  } catch (error) {
    if (!silent) {
      message.warning('获取值班班次失败，请稍后重试')
    }
  } finally {
    shiftOptionsLoading.value = false
  }
}

const loadTeamOptions = async (silent = false, stationId?: string | number | null) => {
  teamOptionsLoading.value = true
  try {
    const teams = await ShiftScheduleApi.getTeamOptions(normalizeId(stationId))
    teamOptions.value = teams || []
    // 班组列表刷新后，清空成员缓存，确保选择班组时拿到最新成员
    teamUserCache.clear()
  } catch (error) {
    if (!silent) {
      message.warning('获取值班班组失败，请稍后重试')
    }
  } finally {
    teamOptionsLoading.value = false
  }
}

const handleFormStationChange = async (stationId?: string | number | null) => {
  if (formInitializing.value) {
    return
  }
  formData.stationId = normalizeId(stationId)
  formData.shiftId = undefined
  formData.shiftName = ''
  formData.teamId = undefined
  formData.teamName = ''
  formDutyUserOptions.value = []
  clearDutyUserFormFields()
  await Promise.all([
    loadShiftOptions(false, formData.stationId),
    loadTeamOptions(false, formData.stationId)
  ])
}

onMounted(async () => {
  await loadBaseOptions()
  await refreshAll()
})
</script>

<style scoped lang="scss">
.shift-schedule-page {
  .query-wrap {
    border: 1px solid rgba(148, 163, 184, 0.32);
    border-radius: 14px;
    background: #ffffff;
    box-shadow: 0 8px 28px rgba(15, 23, 42, 0.08);
  }

  .calendar-wrap {
    margin-top: 12px;
    padding: 0;
    overflow: hidden;
    border-radius: 14px;
    border: 1px solid rgba(59, 130, 246, 0.3);
    background: linear-gradient(180deg, rgba(59, 130, 246, 0.08), rgba(255, 255, 255, 0.96));
    box-shadow: 0 8px 28px rgba(15, 23, 42, 0.08);

    :deep(.el-calendar__header) {
      border-bottom: none;
      padding: 12px 20px;
      background: linear-gradient(90deg, #2f6fd8, #3b82f6);
      color: #fff;
    }

    :deep(.el-calendar-table th) {
      background: #edf3ff;
      color: #334155;
      border-color: #d8e5ff;
    }

    :deep(.el-calendar-table td) {
      border-color: #e2e8f0;
    }

    :deep(.el-calendar-day) {
      padding: 0;
      height: 118px;
    }
  }

  .calendar-header {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 100%;

    &__title {
      font-size: 28px;
      font-weight: 700;
      letter-spacing: 1px;
      margin-right: auto;
    }
  }

  .month-nav-btn {
    color: #fff;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.18);
  }

  .today-btn {
    border-color: rgba(255, 255, 255, 0.58);
    color: #fff;
    background: rgba(255, 255, 255, 0.18);
  }

  .calendar-cell {
    height: 118px;
    padding: 8px;
    cursor: pointer;
    transition: background-color 0.2s ease;

    &:hover {
      background: rgba(59, 130, 246, 0.08);
    }

    &__day {
      font-size: 14px;
      font-weight: 600;
      color: #334155;
      margin-bottom: 4px;
    }

    &__list {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    &--other-month {
      .calendar-cell__day {
        color: #94a3b8;
        font-weight: 500;
      }
    }
  }

  .calendar-item {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 12px;
    line-height: 18px;
    border-radius: 4px;
    padding: 0 6px;
    color: #fff;
    background: #3b82f6;

    &--0 {
      background: #2563eb;
    }

    &--1 {
      background: #22c55e;
    }

    &--2 {
      background: #f97316;
    }

    &--more {
      background: #94a3b8;
    }
  }

  .table-wrap {
    margin-top: 12px;
  }

  .detail-box {
    display: flex;
    flex-direction: column;
    gap: 14px;
    max-height: 520px;
    overflow-y: auto;
    padding-right: 2px;
  }

  .detail-card {
    border: 1px solid rgba(59, 130, 246, 0.26);
    border-radius: 14px;
    background: linear-gradient(165deg, #f8fbff, #f2f7ff);
    box-shadow: 0 8px 22px rgba(37, 99, 235, 0.08);
    padding: 14px 16px;
    transition: box-shadow 0.2s ease, transform 0.2s ease;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 12px 26px rgba(37, 99, 235, 0.12);
    }

    &__header {
      display: flex;
      justify-content: space-between;
      gap: 12px;
      align-items: center;
      margin-bottom: 10px;
    }

    &__title-wrap {
      display: flex;
      align-items: baseline;
      gap: 6px;
      min-width: 0;
    }

    &__title {
      font-size: 20px;
      font-weight: 700;
      color: #1d4ed8;
    }

    &__split {
      color: #64748b;
      font-size: 16px;
      font-weight: 500;
    }

    &__team {
      font-size: 20px;
      font-weight: 700;
      color: #2563eb;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__period {
      margin-bottom: 12px;
      padding: 8px 10px;
      border-radius: 10px;
      border: 1px dashed rgba(59, 130, 246, 0.34);
      background: rgba(255, 255, 255, 0.78);
      color: #334155;
      font-size: 14px;
      line-height: 22px;
      display: flex;
      align-items: center;
      gap: 8px;

      .iconify {
        color: #2563eb;
      }
    }

    &__meta {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 10px;
      margin-bottom: 12px;
    }

    &__block {
      border: 1px solid rgba(148, 163, 184, 0.22);
      border-radius: 10px;
      background: rgba(255, 255, 255, 0.72);
      padding: 10px 12px;
      margin-bottom: 10px;
    }

    &__block-title {
      font-size: 12px;
      color: #64748b;
      margin-bottom: 4px;
    }

    &__block-content {
      color: #334155;
      line-height: 22px;
      font-size: 14px;
      word-break: break-word;
      white-space: pre-wrap;
    }

    &__footer {
      margin-top: 4px;
      border-top: 1px dashed rgba(148, 163, 184, 0.36);
      padding-top: 8px;
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
  }

  .detail-meta-item {
    border: 1px solid rgba(148, 163, 184, 0.24);
    border-radius: 10px;
    background: #fff;
    padding: 8px 10px;
    min-height: 66px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 4px;

    &__label {
      color: #64748b;
      font-size: 12px;
      line-height: 18px;
    }

    &__value {
      color: #1e293b;
      font-size: 18px;
      line-height: 24px;
      font-weight: 600;
      word-break: break-all;
    }
  }

  .shift-date-range-picker {
    :deep(.el-input__wrapper) {
      padding-right: 8px;
    }

    :deep(.el-range-input) {
      min-width: 148px;
      font-size: 13px;
    }

    :deep(.el-range-separator) {
      padding: 0 8px;
      color: #475569;
      font-weight: 600;
    }
  }

  @media (max-width: 860px) {
    .detail-card {
      &__meta {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }

    .shift-date-range-picker {
      :deep(.el-range-input) {
        min-width: 118px;
        font-size: 12px;
      }
    }
  }

  @media (max-width: 640px) {
    .detail-card {
      padding: 12px;

      &__header {
        align-items: flex-start;
        flex-direction: column;
      }

      &__title,
      &__team {
        font-size: 18px;
      }

      &__meta {
        grid-template-columns: 1fr;
      }
    }
  }

  .import-uploader {
    :deep(.el-upload-dragger) {
      width: 100%;
    }

    :deep(.el-upload__tip) {
      margin-top: 8px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;
      color: #64748b;
    }
  }
}
</style>
