<template>
  <div class="work-condition-page">
    <ContentWrap class="query-wrap">
      <el-form
        ref="queryFormRef"
        :model="queryParams"
        :inline="true"
        label-width="90px"
        class="-mb-15px"
      >
        <el-form-item label="所属站点" prop="stationId">
          <el-select
            v-model="queryParams.stationId"
            placeholder="请选择所属站点"
            clearable
            filterable
            class="!w-240px"
            @change="handleStationChange"
          >
            <el-option
              v-for="item in stationOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select
            v-model="queryParams.deviceType"
            placeholder="全部类型"
            clearable
            filterable
            class="!w-200px"
            :disabled="!queryParams.stationId"
            @change="handleDeviceTypeChange"
          >
            <el-option
              v-for="dict in deviceTypeOptions"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-select
            v-model="queryParams.deviceName"
            :placeholder="
              queryParams.stationId ? '不选则查询该类型全部设备' : '请先选择所属站点'
            "
            clearable
            filterable
            class="!w-280px"
            :disabled="!queryParams.stationId"
          >
            <el-option
              v-for="item in deviceOptions"
              :key="`${item.deviceType ?? ''}-${item.value}`"
              :label="formatDeviceOptionLabel(item)"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围" prop="timeRange">
          <el-date-picker
            v-model="queryParams.timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="!w-380px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="handleReset">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="table-wrap">
      <div class="section-header">
        <div class="section-header__title">
          <span class="section-header__dot"></span>
          工情明细
        </div>
        <div class="section-header__meta" v-if="tableData.length">
          <span>{{ uniqueDeviceCount }} 台设备</span>
          <span class="section-header__divider"></span>
          <span>{{ tableData.length }} 条记录</span>
          <span class="section-header__divider"></span>
          <span>{{ visibleMetricColumns.length }} 项指标</span>
        </div>
      </div>

      <el-table
        class="work-condition-table"
        v-loading="tableLoading"
        :data="pagedTableData"
        stripe
        empty-text="暂无工情数据，请选择站点与时间后查询"
        :header-cell-style="tableHeaderStyle"
        :row-style="tableRowStyle"
      >
        <el-table-column
          type="index"
          label="序号"
          width="72"
          align="center"
          fixed="left"
          :index="indexMethod"
        />
        <el-table-column
          label="所属站点"
          prop="stationName"
          min-width="120"
          fixed="left"
          show-overflow-tooltip
        />
        <el-table-column
          label="设备名称"
          prop="deviceName"
          min-width="160"
          fixed="left"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span class="device-name">{{ row.deviceName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="设备类型" min-width="110" align="center">
          <template #default="{ row }">
            <span class="type-tag">{{ formatMetric(row.deviceTypeName) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" prop="time" min-width="170" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatMetric(row.time) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-for="column in visibleMetricColumns"
          :key="column.prop"
          :label="column.label"
          :min-width="column.minWidth"
          align="center"
        >
          <template #default="{ row }">
            <span
              v-if="column.format === 'status'"
              class="status-pill"
              :class="statusPillClass(resolveMetricValue(row as Record<string, unknown>, column.prop))"
            >
              {{ formatStatusMetric(resolveMetricValue(row as Record<string, unknown>, column.prop)) }}
            </span>
            <span
              v-else
              class="metric-value"
              :class="{
                'is-empty': !hasMetricValue(
                  resolveMetricValue(row as Record<string, unknown>, column.prop)
                ) && column.format !== 'current'
              }"
            >
              {{ formatCellValue(row, column) }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-pagination" v-if="tableData.length">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="tableData.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import dayjs from 'dayjs'
import {
  IotScreenStatisticsApi,
  type IotScreenDeviceOptionRespVO,
  type IotScreenStationOptionRespVO,
  type IotScreenWorkConditionRespVO
} from '@/api/iot/statistics/screen'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

type QueryParams = {
  stationId: string
  deviceType?: number
  deviceName?: string
  timeRange: string[]
}

type DeviceSelectOption = IotScreenDeviceOptionRespVO & {
  label: string
  value: string
}

type MetricColumn = {
  prop: string
  label: string
  minWidth: number
  format?: 'status' | 'current' | 'default'
}

const METRIC_COLUMNS: MetricColumn[] = [
  { prop: 'activePowerKw', label: '有功功率(Kw)', minWidth: 130 },
  { prop: 'reactivePowerKvar', label: '无功功率(Kvar)', minWidth: 140 },
  { prop: 'powerFactor', label: '功率因素', minWidth: 110 },
  { prop: 'frequency', label: '频率', minWidth: 100 },
  { prop: 'abVoltage', label: 'AB相电压', minWidth: 120 },
  { prop: 'bcVoltage', label: 'BC相电压', minWidth: 120 },
  { prop: 'caVoltage', label: 'CA相电压', minWidth: 120 },
  { prop: 'aCurrent', label: 'A相电流', minWidth: 110, format: 'current' },
  { prop: 'bCurrent', label: 'B相电流', minWidth: 110, format: 'current' },
  { prop: 'cCurrent', label: 'C相电流', minWidth: 110, format: 'current' },
  { prop: 'isGateOpenAll', label: '全开状态', minWidth: 100, format: 'status' },
  { prop: 'isGateCloseAll', label: '全关状态', minWidth: 100, format: 'status' },
  { prop: 'isGateUp', label: '上升状态', minWidth: 100, format: 'status' },
  { prop: 'isGateDown', label: '下降状态', minWidth: 100, format: 'status' },
  { prop: 'isGateFailure', label: '故障状态', minWidth: 100, format: 'status' },
  { prop: 'isGatePowerOn', label: '电源合闸', minWidth: 100, format: 'status' },
  { prop: 'floodgateOpening', label: '闸门开度', minWidth: 110 },
  { prop: 'waterLevel', label: '水位(m)', minWidth: 110 },
  { prop: 'pressValue', label: '扬压力', minWidth: 120 },
  { prop: 'aStatorTemp1', label: '主机定子U1温度', minWidth: 140 },
  { prop: 'bStatorTemp1', label: '主机定子V1温度', minWidth: 140 },
  { prop: 'cStatorTemp1', label: '主机定子W1温度', minWidth: 140 },
  { prop: 'aStatorTemp2', label: '主机定子U2温度', minWidth: 140 },
  { prop: 'bStatorTemp2', label: '主机定子V2温度', minWidth: 140 },
  { prop: 'cStatorTemp2', label: '主机定子W2温度', minWidth: 140 }
]

/** 按设备类型只展示对应指标，避免表头串类 */
const DEVICE_TYPE_METRIC_PROPS: Record<number, string[]> = {
  // 主机
  3: [
    'activePowerKw',
    'reactivePowerKvar',
    'powerFactor',
    'frequency',
    'abVoltage',
    'bcVoltage',
    'caVoltage',
    'aCurrent',
    'bCurrent',
    'cCurrent',
    'aStatorTemp1',
    'bStatorTemp1',
    'cStatorTemp1',
    'aStatorTemp2',
    'bStatorTemp2',
    'cStatorTemp2'
  ],
  // 水位计
  6: ['waterLevel'],
  // 闸门
  7: [
    'abVoltage',
    'bcVoltage',
    'caVoltage',
    'aCurrent',
    'bCurrent',
    'cCurrent',
    'activePowerKw',
    'reactivePowerKvar',
    'isGateOpenAll',
    'isGateCloseAll',
    'isGateUp',
    'isGateDown',
    'isGateFailure',
    'isGatePowerOn',
    'floodgateOpening'
  ],
  // 扬压力
  9: ['pressValue']
}

/** 单指标类型：即使暂无值也固定展示该列 */
const ALWAYS_SHOW_METRIC_TYPES = new Set([6, 9])

const queryFormRef = ref<FormInstance>()
const tableLoading = ref(false)
const stationOptions = ref<IotScreenStationOptionRespVO[]>([])
const deviceOptions = ref<DeviceSelectOption[]>([])
const tableData = ref<IotScreenWorkConditionRespVO[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20
})

const tableHeaderStyle = {
  background: '#F3F8FC',
  color: '#334155',
  fontWeight: 600,
  borderBottom: '1px solid #D6E4F0'
}

const tableRowStyle = {
  height: '48px'
}

const buildDefaultRange = (): string[] => {
  const endTime = dayjs()
  return [
    endTime.subtract(30, 'minute').format('YYYY-MM-DD HH:mm:ss'),
    endTime.format('YYYY-MM-DD HH:mm:ss')
  ]
}

const queryParams = reactive<QueryParams>({
  stationId: '',
  deviceType: undefined,
  deviceName: undefined,
  timeRange: buildDefaultRange()
})

const deviceTypeOptions = computed(() => getIntDictOptions(DICT_TYPE.IOT_DEVICE_TYPE))

const uniqueDeviceCount = computed(() => {
  const names = new Set(
    tableData.value.map((row) => row.deviceName).filter((name): name is string => !!name)
  )
  return names.size
})

const pagedTableData = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return tableData.value.slice(start, start + pagination.pageSize)
})

const indexMethod = (index: number) => (pagination.page - 1) * pagination.pageSize + index + 1

const hasMetricValue = (value: unknown) => !(value === null || value === undefined || value === '')

const resolveMetricValue = (row: Record<string, unknown> | undefined, field: string) => {
  if (!row || !field) return undefined
  const snakeCaseField = field.replace(/[A-Z]/g, (match) => `_${match.toLowerCase()}`)
  if (row[field] !== undefined) return row[field]
  if (row[field.toLowerCase()] !== undefined) return row[field.toLowerCase()]
  if (row[snakeCaseField] !== undefined) return row[snakeCaseField]
  const normalizeKey = (key: string) => key.replace(/_/g, '').toLowerCase()
  const target = normalizeKey(field)
  const matchedKey = Object.keys(row).find((key) => normalizeKey(key) === target)
  return matchedKey ? row[matchedKey] : undefined
}

const resolveResultDeviceTypes = () => {
  if (queryParams.deviceType != null) {
    return [queryParams.deviceType]
  }
  const types = new Set<number>()
  tableData.value.forEach((row) => {
    if (typeof row.deviceType === 'number') {
      types.add(row.deviceType)
    }
  })
  return [...types]
}

const resolveCandidateMetricProps = (deviceTypes: number[]) => {
  const props = new Set<string>()
  let hasMappedType = false
  deviceTypes.forEach((type) => {
    const typeProps = DEVICE_TYPE_METRIC_PROPS[type]
    if (!typeProps?.length) return
    hasMappedType = true
    typeProps.forEach((prop) => props.add(prop))
  })
  // 未配置映射的类型：回退全量指标，再靠「有值才展示」收敛
  if (!hasMappedType) {
    METRIC_COLUMNS.forEach((column) => props.add(column.prop))
  }
  return props
}

const visibleMetricColumns = computed(() => {
  const rows = tableData.value
  if (!rows.length) return []

  const deviceTypes = resolveResultDeviceTypes()
  const candidateProps = resolveCandidateMetricProps(deviceTypes)
  const candidates = METRIC_COLUMNS.filter((column) => candidateProps.has(column.prop))

  const withData = candidates.filter((column) =>
    rows.some((row) => hasMetricValue(resolveMetricValue(row as Record<string, unknown>, column.prop)))
  )
  if (withData.length) return withData

  // 扬压力 / 水位：固定展示对应列（与潘家河展示一致）
  if (deviceTypes.some((type) => ALWAYS_SHOW_METRIC_TYPES.has(type))) {
    return candidates
  }
  // 闸门全空时仍给默认电气/状态列
  if (deviceTypes.includes(7)) {
    return candidates
  }
  return candidates
})

const formatMetric = (value: unknown) => {
  if (!hasMetricValue(value)) return '-'
  if (typeof value === 'number' && Number.isFinite(value)) {
    return Number.isInteger(value) ? String(value) : String(Number(value.toFixed(4)))
  }
  const asNumber = Number(value)
  if (typeof value === 'string' && value.trim() !== '' && Number.isFinite(asNumber) && /^-?\d+(\.\d+)?$/.test(value.trim())) {
    return Number.isInteger(asNumber) ? String(asNumber) : String(Number(asNumber.toFixed(4)))
  }
  return String(value)
}

const formatStatusMetric = (value: unknown) => {
  if (!hasMetricValue(value)) return '-'
  const normalized = String(value).trim().toLowerCase()
  if (['1', 'true', 'yes', 'on', 'open'].includes(normalized)) return '是'
  if (['0', 'false', 'no', 'off', 'close'].includes(normalized)) return '否'
  return String(value)
}

const statusPillClass = (value: unknown) => {
  if (!hasMetricValue(value)) return 'is-empty'
  const text = formatStatusMetric(value)
  if (text === '是') return 'is-yes'
  if (text === '否') return 'is-no'
  return 'is-raw'
}

const formatCellValue = (row: IotScreenWorkConditionRespVO, column: MetricColumn) => {
  const value = resolveMetricValue(row as Record<string, unknown>, column.prop)
  if (column.format === 'status') return formatStatusMetric(value)
  if (column.format === 'current') return hasMetricValue(value) ? formatMetric(value) : '0'
  return formatMetric(value)
}

const formatDeviceOptionLabel = (item: DeviceSelectOption) => {
  if (queryParams.deviceType != null) return item.label
  const typeName = (item.deviceTypeName || '').trim()
  return typeName ? `${item.label}（${typeName}）` : item.label
}

const loadStationOptions = async () => {
  stationOptions.value = (await IotScreenStatisticsApi.getStationOptions()) || []
}

const normalizeLeadingDigits = (text: string) => {
  const chineseDigitMap: Record<string, string> = {
    零: '0',
    〇: '0',
    一: '1',
    二: '2',
    三: '3',
    四: '4',
    五: '5',
    六: '6',
    七: '7',
    八: '8',
    九: '9'
  }
  return text
    .replace(/[零〇一二三四五六七八九]/g, (ch) => chineseDigitMap[ch] || ch)
    .replace(/[０-９]/g, (ch) => String.fromCharCode(ch.charCodeAt(0) - 65248))
}

const getLeadingNumber = (label: string) => {
  const normalized = normalizeLeadingDigits((label || '').trim())
  const match = normalized.match(/^(\d+)/)
  return match ? Number(match[1]) : null
}

const compareDeviceOption = (a: DeviceSelectOption, b: DeviceSelectOption) => {
  const leftType = a.deviceType ?? Number.MAX_SAFE_INTEGER
  const rightType = b.deviceType ?? Number.MAX_SAFE_INTEGER
  if (leftType !== rightType) return leftType - rightType
  const leftLabel = (a.label || '').trim()
  const rightLabel = (b.label || '').trim()
  const leftNum = getLeadingNumber(leftLabel)
  const rightNum = getLeadingNumber(rightLabel)
  if (leftNum !== null && rightNum !== null && leftNum !== rightNum) return leftNum - rightNum
  return leftLabel.localeCompare(rightLabel, 'zh-Hans-CN', { numeric: true, sensitivity: 'base' })
}

const loadDeviceOptionsByStation = async () => {
  const stationId = queryParams.stationId?.trim() || ''
  if (!stationId) {
    deviceOptions.value = []
    queryParams.deviceName = undefined
    return
  }
  const deviceType =
    queryParams.deviceType === undefined || queryParams.deviceType === null
      ? -1 // 全部类型（兼容大屏：不传 deviceType 仍默认水位计）
      : queryParams.deviceType
  const list = (await IotScreenStatisticsApi.getDeviceOptions(stationId, deviceType)) || []
  deviceOptions.value = list
    .map((item) => ({ ...item, label: item.label, value: item.value }))
    .sort(compareDeviceOption)
  if (!deviceOptions.value.some((item) => item.value === queryParams.deviceName)) {
    queryParams.deviceName = undefined
  }
}

const handleStationChange = async () => {
  queryParams.deviceName = undefined
  await loadDeviceOptionsByStation()
}

const handleDeviceTypeChange = async () => {
  queryParams.deviceName = undefined
  await loadDeviceOptionsByStation()
}

const fetchData = async () => {
  if (!queryParams.stationId) {
    ElMessage.warning('请先选择所属站点')
    return
  }
  if (!queryParams.timeRange || queryParams.timeRange.length !== 2) {
    ElMessage.warning('请选择开始时间和结束时间')
    return
  }
  pagination.page = 1
  tableLoading.value = true
  try {
    const [startTime, endTime] = queryParams.timeRange
    tableData.value =
      (await IotScreenStatisticsApi.getWorkConditionList({
        stationId: queryParams.stationId,
        deviceType: queryParams.deviceType,
        deviceName: queryParams.deviceName || undefined,
        startTime,
        endTime
      })) || []
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => fetchData()

const handleReset = () => {
  queryFormRef.value?.resetFields()
  queryParams.stationId = ''
  queryParams.deviceType = undefined
  queryParams.deviceName = undefined
  queryParams.timeRange = buildDefaultRange()
  deviceOptions.value = []
  tableData.value = []
  pagination.page = 1
}

watch(
  () => pagination.pageSize,
  () => {
    pagination.page = 1
  }
)

onMounted(async () => {
  await loadStationOptions()
})
</script>

<style scoped>
.work-condition-page {
  --wc-border: #d6e4f0;
  --wc-primary: #0b7fda;
  --wc-text: #334155;
  --wc-muted: #64748b;
}

.query-wrap {
  margin-bottom: 12px;
}

.table-wrap {
  margin-bottom: 12px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  min-height: 28px;
}

.section-header__title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--wc-text);
}

.section-header__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--wc-primary);
}

.section-header__meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--wc-muted);
}

.section-header__divider {
  width: 1px;
  height: 12px;
  background: #cbd5e1;
}

.work-condition-table {
  width: 100%;
  border: 1px solid var(--wc-border);
  border-radius: 8px;
  overflow: hidden;
}

.work-condition-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.work-condition-table :deep(.el-table__cell) {
  padding-top: 10px;
  padding-bottom: 10px;
}

.work-condition-table :deep(.el-table__row:hover > td.el-table__cell) {
  background: #f5f9fc !important;
}

.device-name {
  color: var(--wc-text);
  font-weight: 500;
}

.time-text {
  color: var(--wc-muted);
  font-variant-numeric: tabular-nums;
}

.type-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f1f5f9;
  color: #475569;
  font-size: 12px;
}

.metric-value {
  color: var(--wc-text);
  font-variant-numeric: tabular-nums;
}

.metric-value.is-empty {
  color: #94a3b8;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  padding: 1px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-pill.is-yes {
  background: #ecfdf5;
  color: #047857;
}

.status-pill.is-no {
  background: #f1f5f9;
  color: #64748b;
}

.status-pill.is-empty {
  color: #94a3b8;
  background: transparent;
}

.status-pill.is-raw {
  color: var(--wc-text);
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
