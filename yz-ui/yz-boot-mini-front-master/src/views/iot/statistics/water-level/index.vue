<template>
  <div class="water-level-page">
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
          >
            <el-option
              v-for="item in stationOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceNames">
          <el-select
            v-model="queryParams.deviceNames"
            :placeholder="queryParams.stationId ? '请选择设备名称' : '请先选择所属站点'"
            :disabled="!queryParams.stationId"
            clearable
            filterable
            multiple
            collapse-tags
            collapse-tags-tooltip
            class="!w-280px"
          >
            <el-option
              v-for="item in deviceOptions"
              :key="item.value"
              :label="item.label"
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

    <ContentWrap class="chart-wrap">
      <div class="section-header">
        <div class="section-header__title">
          <span class="section-header__dot"></span>
          水位趋势
        </div>
        <div class="section-header__meta" v-if="tableData.length">
          <span>{{ deviceSeriesCount }} 台设备</span>
          <span class="section-header__divider"></span>
          <span>{{ tableData.length }} 条记录</span>
        </div>
      </div>
      <div v-loading="tableLoading" class="chart-panel">
        <Echart :key="chartRenderKey" :options="chartOptions" :height="440" />
      </div>
    </ContentWrap>

    <ContentWrap class="table-wrap">
      <div class="section-header">
        <div class="section-header__title">
          <span class="section-header__dot section-header__dot--table"></span>
          水位明细
        </div>
        <div class="section-header__meta" v-if="tableData.length">
          共 {{ tableData.length }} 条
        </div>
      </div>
      <el-table
        class="water-table"
        v-loading="tableLoading"
        :data="pagedTableData"
        stripe
        empty-text="暂无水位数据，请先选择站点并查询"
        :header-cell-style="tableHeaderStyle"
        :row-style="tableRowStyle"
      >
        <el-table-column type="index" label="序号" width="72" align="center" :index="indexMethod" />
        <el-table-column label="所属站点" prop="stationName" min-width="160" show-overflow-tooltip />
        <el-table-column label="设备名称" prop="deviceName" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="device-name">{{ row.deviceName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" prop="time" min-width="180" align="center" />
        <el-table-column label="水位 (m)" min-width="140" align="center">
          <template #default="{ row }">
            <span class="water-value" :class="{ 'is-empty': formatWaterLevel(row.waterLevel) === '-' }">
              {{ formatWaterLevel(row.waterLevel) }}
              <span v-if="formatWaterLevel(row.waterLevel) !== '-'" class="water-unit">m</span>
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
import type { EChartsOption } from 'echarts'
import {
  IotScreenStatisticsApi,
  type IotScreenDeviceOptionRespVO,
  type IotScreenStationOptionRespVO,
  type IotScreenStreamWaterRespVO
} from '@/api/iot/statistics/screen'

type QueryParams = {
  stationId: string
  deviceNames: string[]
  timeRange: string[]
}

const SERIES_COLORS = ['#0B7FDA', '#14B8A6', '#3B82F6', '#0EA5E9', '#6366F1', '#0891B2']

const queryFormRef = ref<FormInstance>()
const tableLoading = ref(false)
const stationOptions = ref<IotScreenStationOptionRespVO[]>([])
const deviceOptions = ref<IotScreenDeviceOptionRespVO[]>([])
const tableData = ref<IotScreenStreamWaterRespVO[]>([])
const chartRenderKey = ref(0)

const pagination = reactive({
  page: 1,
  pageSize: 20
})

const buildDefaultRange = (): string[] => [
  dayjs().subtract(24, 'hour').format('YYYY-MM-DD HH:mm:ss'),
  dayjs().format('YYYY-MM-DD HH:mm:ss')
]

const queryParams = reactive<QueryParams>({
  stationId: '',
  deviceNames: [],
  timeRange: buildDefaultRange()
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

const toNumberWaterLevel = (value: unknown): number | null => {
  if (value === null || value === undefined || value === '') {
    return null
  }
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

const formatWaterLevel = (value: unknown) => {
  const parsed = toNumberWaterLevel(value)
  if (parsed !== null) {
    return parsed.toFixed(2)
  }
  return value === null || value === undefined || value === '' ? '-' : String(value)
}

/** 按数据范围自动计算 Y 轴，使曲线尽量落在图表中部 */
const resolveYAxisRange = (values: number[]) => {
  if (!values.length) {
    return { min: undefined as number | undefined, max: undefined as number | undefined }
  }
  let dataMin = Math.min(...values)
  let dataMax = Math.max(...values)

  if (dataMin === dataMax) {
    const pad = Math.max(Math.abs(dataMin) * 0.08, 0.2)
    return {
      min: Number((dataMin - pad).toFixed(2)),
      max: Number((dataMax + pad).toFixed(2))
    }
  }

  const span = dataMax - dataMin
  // 上下各留约 35% 余量，数据约占可视高度 40%~60% 中带
  const pad = Math.max(span * 0.35, 0.05)
  return {
    min: Number((dataMin - pad).toFixed(2)),
    max: Number((dataMax + pad).toFixed(2))
  }
}

const pickDefaultStationId = () => {
  const list = stationOptions.value
  if (!list.length) {
    return ''
  }
  const preferred = list.find((item) => (item.label || '').includes('卧虎'))
  return preferred?.value || list[0].value
}

const deviceSeriesCount = computed(() => {
  const names = new Set(tableData.value.map((item) => item.deviceName || '未命名设备'))
  return names.size
})

const pagedTableData = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return tableData.value.slice(start, start + pagination.pageSize)
})

const indexMethod = (index: number) => (pagination.page - 1) * pagination.pageSize + index + 1

const buildAreaStyle = (color: string) => ({
  color: {
    type: 'linear',
    x: 0,
    y: 0,
    x2: 0,
    y2: 1,
    colorStops: [
      { offset: 0, color: `${color}33` },
      { offset: 1, color: `${color}05` }
    ]
  }
})

const chartOptions = computed<EChartsOption>(() => {
  const list = [...tableData.value].filter((item) => !!item.time)
  if (!list.length) {
    return {
      title: {
        show: true,
        text: '暂无水位数据',
        subtext: '选择站点与时间范围后点击查询',
        left: 'center',
        top: 'middle',
        textStyle: {
          fontSize: 15,
          color: '#64748B',
          fontWeight: 500
        },
        subtextStyle: {
          fontSize: 12,
          color: '#94A3B8'
        }
      },
      legend: { show: false },
      dataZoom: [],
      xAxis: { type: 'category', show: false, data: [] },
      yAxis: { type: 'value', show: false },
      series: []
    }
  }

  const sortedTimeList = Array.from(new Set(list.map((item) => item.time))).sort((a, b) =>
    a.localeCompare(b)
  )
  const deviceValueMap = new Map<string, Map<string, number | null>>()
  const numericValues: number[] = []
  list.forEach((item) => {
    const deviceName = item.deviceName || '未命名设备'
    if (!deviceValueMap.has(deviceName)) {
      deviceValueMap.set(deviceName, new Map<string, number | null>())
    }
    const waterLevel = toNumberWaterLevel(item.waterLevel)
    deviceValueMap.get(deviceName)!.set(item.time, waterLevel)
    if (waterLevel !== null) {
      numericValues.push(waterLevel)
    }
  })
  const yAxisRange = resolveYAxisRange(numericValues)

  return {
    // Echart 组件 setOption 默认 merge，必须显式关闭空状态残留项
    title: { show: false, text: '', subtext: '' },
    color: SERIES_COLORS,
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: '#D6E4F0',
      borderWidth: 1,
      padding: [10, 14],
      textStyle: {
        color: '#334155',
        fontSize: 12
      },
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#94A3B8'
        },
        lineStyle: {
          color: '#94A3B8',
          type: 'dashed'
        }
      },
      valueFormatter: (value) => {
        if (value === null || value === undefined || value === '') {
          return '-'
        }
        const num = Number(value)
        return Number.isFinite(num) ? `${num.toFixed(2)} m` : String(value)
      }
    },
    legend: {
      show: true,
      top: 4,
      type: 'scroll',
      icon: 'roundRect',
      itemWidth: 12,
      itemHeight: 8,
      itemGap: 16,
      textStyle: {
        color: '#475569',
        fontSize: 12
      }
    },
    grid: {
      left: 28,
      right: 28,
      bottom: 56,
      top: 52,
      containLabel: true
    },
    dataZoom: [
      {
        type: 'inside',
        xAxisIndex: 0,
        filterMode: 'none'
      },
      {
        type: 'slider',
        height: 18,
        bottom: 8,
        borderColor: 'transparent',
        backgroundColor: '#F1F5F9',
        fillerColor: 'rgba(11, 127, 218, 0.16)',
        handleStyle: {
          color: '#0B7FDA',
          borderColor: '#0B7FDA'
        },
        dataBackground: {
          lineStyle: { color: '#94A3B8' },
          areaStyle: { color: '#E2E8F0' }
        },
        textStyle: {
          color: '#64748B',
          fontSize: 11
        }
      }
    ],
    xAxis: {
      type: 'category',
      show: true,
      boundaryGap: false,
      data: sortedTimeList,
      axisLine: {
        lineStyle: { color: '#CBD5E1' }
      },
      axisTick: { show: false },
      axisLabel: {
        show: true,
        color: '#64748B',
        margin: 12,
        hideOverlap: true,
        formatter: (value: string) => {
          const text = String(value || '')
          const [datePart, timePart] = text.split(' ')
          if (datePart && timePart) {
            return `${datePart}\n${timePart}`
          }
          return text
        }
      },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value',
      show: true,
      scale: true,
      min: yAxisRange.min,
      max: yAxisRange.max,
      name: '水位 (m)',
      nameTextStyle: {
        color: '#64748B',
        padding: [0, 0, 0, 8]
      },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        show: true,
        color: '#64748B',
        formatter: (value: number) => Number(value).toFixed(2)
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: '#E8EEF5',
          type: 'dashed'
        }
      }
    },
    series: Array.from(deviceValueMap.entries()).map(([deviceName, valueMap], index) => {
      const color = SERIES_COLORS[index % SERIES_COLORS.length]
      return {
        name: deviceName,
        type: 'line',
        smooth: true,
        showSymbol: sortedTimeList.length <= 24,
        symbol: 'circle',
        symbolSize: 6,
        connectNulls: false,
        lineStyle: {
          width: 2.5,
          color
        },
        itemStyle: {
          color,
          borderColor: '#fff',
          borderWidth: 1.5
        },
        areaStyle: buildAreaStyle(color),
        emphasis: {
          focus: 'series',
          itemStyle: {
            borderWidth: 2
          }
        },
        data: sortedTimeList.map((time) => (valueMap.has(time) ? valueMap.get(time) : null))
      }
    })
  }
})

const loadStationOptions = async () => {
  stationOptions.value = (await IotScreenStatisticsApi.getStationOptions()) || []
}

const loadDeviceOptions = async (stationId: string) => {
  if (!stationId) {
    deviceOptions.value = []
    queryParams.deviceNames = []
    return
  }
  deviceOptions.value = (await IotScreenStatisticsApi.getDeviceOptions(stationId, 6)) || []
  // 默认勾选该站全部水位计，让选择框直接展示设备名称
  queryParams.deviceNames = deviceOptions.value
    .map((item) => item.value)
    .filter((value): value is string => !!value)
}

const isBootstrapping = ref(false)

watch(
  () => queryParams.stationId,
  async (stationId) => {
    if (isBootstrapping.value) {
      return
    }
    await loadDeviceOptions(stationId)
  }
)

watch(
  () => pagination.pageSize,
  () => {
    pagination.page = 1
  }
)

const fetchData = async (options?: { silent?: boolean }) => {
  if (!queryParams.stationId) {
    if (!options?.silent) {
      ElMessage.warning('请先选择所属站点')
    }
    return
  }
  if (!queryParams.timeRange || queryParams.timeRange.length !== 2) {
    if (!options?.silent) {
      ElMessage.warning('请选择开始时间和结束时间')
    }
    return
  }
  pagination.page = 1
  tableLoading.value = true
  try {
    const [startTime, endTime] = queryParams.timeRange
    const data = await IotScreenStatisticsApi.getStreamWaterList({
      stationId: queryParams.stationId,
      deviceNames: queryParams.deviceNames.length ? queryParams.deviceNames : undefined,
      startTime,
      endTime
    })
    const normalizedData = Array.isArray(data) ? data : []
    tableData.value = normalizedData
      .slice()
      .sort((a, b) => (b.time || '').localeCompare(a.time || ''))
    // 数据就绪后再重建图表，避免空状态与有数据状态 merge 残留
    chartRenderKey.value += 1
  } catch {
    tableData.value = []
    chartRenderKey.value += 1
    if (!options?.silent) {
      ElMessage.error('水位数据查询失败，请稍后重试')
    }
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  fetchData()
}

const handleReset = async () => {
  queryParams.timeRange = buildDefaultRange()
  pagination.page = 1
  const defaultStationId = pickDefaultStationId()
  isBootstrapping.value = true
  queryParams.stationId = defaultStationId
  await loadDeviceOptions(defaultStationId)
  isBootstrapping.value = false
  if (defaultStationId) {
    await fetchData()
  } else {
    queryParams.deviceNames = []
    tableData.value = []
    chartRenderKey.value += 1
  }
}

const initDefaultQuery = async () => {
  await loadStationOptions()
  const defaultStationId = pickDefaultStationId()
  if (!defaultStationId) {
    return
  }
  isBootstrapping.value = true
  queryParams.stationId = defaultStationId
  await loadDeviceOptions(defaultStationId)
  isBootstrapping.value = false
  await fetchData({ silent: true })
}

onMounted(async () => {
  await initDefaultQuery()
})
</script>

<style scoped>
.water-level-page {
  --wl-border: #d6e4f0;
  --wl-primary: #0b7fda;
  --wl-text: #334155;
  --wl-muted: #64748b;
}

.query-wrap {
  margin-bottom: 12px;
}

.chart-wrap,
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
  color: var(--wl-text);
  letter-spacing: 0.02em;
}

.section-header__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0b7fda, #14b8a6);
  box-shadow: 0 0 0 3px rgba(11, 127, 218, 0.12);
}

.section-header__dot--table {
  background: linear-gradient(135deg, #0ea5e9, #6366f1);
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.12);
}

.section-header__meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--wl-muted);
}

.section-header__divider {
  width: 1px;
  height: 12px;
  background: #cbd5e1;
}

.chart-panel {
  min-height: 440px;
  padding: 4px 2px 0;
  border: 1px solid var(--wl-border);
  border-radius: 10px;
  background:
    linear-gradient(180deg, rgba(243, 248, 252, 0.9) 0%, rgba(255, 255, 255, 0.95) 42%),
    #fff;
  overflow: hidden;
}

.water-table {
  width: 100%;
  border: 1px solid var(--wl-border);
  border-radius: 10px;
  overflow: hidden;
}

.water-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.water-table :deep(.el-table__cell) {
  padding-top: 10px;
  padding-bottom: 10px;
}

.water-table :deep(.el-table__row:hover > td.el-table__cell) {
  background: #f0f7fc !important;
}

.device-name {
  color: var(--wl-text);
  font-weight: 500;
}

.water-value {
  display: inline-flex;
  align-items: baseline;
  gap: 3px;
  min-width: 64px;
  justify-content: center;
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(11, 127, 218, 0.08);
  color: var(--wl-primary);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.01em;
}

.water-value.is-empty {
  background: #f1f5f9;
  color: #94a3b8;
  font-weight: 500;
}

.water-unit {
  font-size: 11px;
  font-weight: 500;
  color: #64748b;
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
