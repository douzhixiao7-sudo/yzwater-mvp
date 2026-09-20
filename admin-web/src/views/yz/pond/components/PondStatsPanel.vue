<template>
  <div class="pond-stats-wrap" v-loading="loading">
    <div class="pond-stats-cards">
      <div class="pond-stat-card pond-stat-card--total">
        <div class="pond-stat-card__label">河塘总数</div>
        <div class="pond-stat-card__value">{{ formatInt(stats.totalCount) }}<span class="pond-stat-card__unit">个</span></div>
        <div class="pond-stat-card__foot">台账登记量</div>
      </div>
      <div class="pond-stat-card pond-stat-card--area">
        <div class="pond-stat-card__label">实测面积合计</div>
        <div class="pond-stat-card__value">{{ formatArea(stats.totalAreaSqm) }}<span class="pond-stat-card__unit">㎡</span></div>
        <div class="pond-stat-card__foot">约 {{ formatMu(stats.totalAreaMu) }} 亩</div>
      </div>
      <div class="pond-stat-card pond-stat-card--town">
        <div class="pond-stat-card__label">覆盖镇 / 街道</div>
        <div class="pond-stat-card__value">{{ formatInt(stats.townCount) }}<span class="pond-stat-card__unit">个</span></div>
        <div class="pond-stat-card__foot">按区划树汇总</div>
      </div>
      <div class="pond-stat-card pond-stat-card--village">
        <div class="pond-stat-card__label">覆盖村 / 社区</div>
        <div class="pond-stat-card__value">{{ formatInt(stats.villageCount) }}<span class="pond-stat-card__unit">个</span></div>
        <div class="pond-stat-card__foot">有挂接区划编码</div>
      </div>
    </div>

    <el-row :gutter="12" class="pond-stats-charts">
      <el-col :xl="14" :lg="14" :md="24" :sm="24" :xs="24">
        <div class="pond-chart-card">
          <div class="pond-chart-card__head">
            <span class="pond-chart-card__title">区划河塘分布</span>
            <el-radio-group v-model="areaDim" size="small">
              <el-radio-button label="town">按镇</el-radio-button>
              <el-radio-button label="village">按村</el-radio-button>
            </el-radio-group>
          </div>
          <Echart :options="areaBarOptions" :height="300" />
        </div>
      </el-col>
      <el-col :xl="10" :lg="10" :md="24" :sm="24" :xs="24">
        <div class="pond-chart-card">
          <div class="pond-chart-card__head">
            <span class="pond-chart-card__title">属性占比</span>
            <el-radio-group v-model="attrDim" size="small">
              <el-radio-button label="ownership">土地权属</el-radio-button>
              <el-radio-button label="usage">使用状态</el-radio-button>
            </el-radio-group>
          </div>
          <Echart :options="attrPieOptions" :height="340" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { EChartsOption } from 'echarts'
import { Echart } from '@/components/Echart'
import {
  getWaterPondStats,
  type WaterPondPageReqVO,
  type WaterPondStatsNameCountItem,
  type WaterPondStatsRespVO
} from '@/api/gis/waterPond'

const props = defineProps<{
  query?: Partial<WaterPondPageReqVO>
}>()

const loading = ref(false)
const areaDim = ref<'town' | 'village'>('town')
const attrDim = ref<'ownership' | 'usage'>('ownership')
const stats = ref<WaterPondStatsRespVO>({
  totalCount: 0,
  totalAreaSqm: 0,
  totalAreaMu: 0,
  townCount: 0,
  villageCount: 0,
  townStats: [],
  villageStats: [],
  ownershipStats: [],
  usageStatusStats: [],
  resourceTypeStats: []
})

const emptyStats = (): WaterPondStatsRespVO => ({
  totalCount: 0,
  totalAreaSqm: 0,
  totalAreaMu: 0,
  townCount: 0,
  villageCount: 0,
  townStats: [],
  villageStats: [],
  ownershipStats: [],
  usageStatusStats: [],
  resourceTypeStats: []
})

const formatInt = (value?: number | null) => {
  const n = Number(value || 0)
  return Number.isFinite(n) ? n.toLocaleString('zh-CN') : '0'
}

const formatArea = (value?: number | null) => {
  const n = Number(value || 0)
  if (!Number.isFinite(n)) return '0'
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

const formatMu = (value?: number | null) => {
  const n = Number(value || 0)
  if (!Number.isFinite(n)) return '0'
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

const CHART_COLORS = ['#1e40af', '#2563eb', '#0ea5e9', '#14b8a6', '#f59e0b', '#ef4444', '#64748b', '#8b5cf6']

const toChartRows = (list?: WaterPondStatsNameCountItem[], limit = 12) =>
  (list || [])
    .filter((item) => Number(item.count || 0) > 0)
    .slice(0, limit)
    .map((item) => ({
      code: item.code || '',
      name: item.name || '未命名',
      count: Number(item.count || 0),
      areaSqm: Number(item.areaSqm || 0)
    }))

const townRows = computed(() => toChartRows(stats.value.townStats, 12))
const villageRows = computed(() => toChartRows(stats.value.villageStats, 10))
const areaRows = computed(() => (areaDim.value === 'town' ? townRows.value : villageRows.value))

const areaBarOptions = computed<EChartsOption>(() => {
  const isTown = areaDim.value === 'town'
  const rows = [...areaRows.value].reverse()
  const labelWidth = isTown ? 72 : 96
  return {
    color: CHART_COLORS,
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const p = Array.isArray(params) ? params[0] : params
        const row = rows[p?.dataIndex]
        if (!row) return ''
        return `${row.name}<br/>数量：${row.count} 个<br/>面积：${formatArea(row.areaSqm)} ㎡`
      }
    },
    grid: { left: labelWidth + 16, right: 24, top: 16, bottom: 24 },
    xAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#64748b' },
      splitLine: { lineStyle: { color: '#e2e8f0', type: 'dashed' } }
    },
    yAxis: {
      type: 'category',
      data: rows.map((item) => item.name),
      axisLabel: { color: '#334155', width: labelWidth, overflow: 'truncate' },
      axisTick: { show: false },
      axisLine: { show: false }
    },
    series: [
      {
        type: 'bar',
        barMaxWidth: isTown ? 18 : 16,
        data: rows.map((item) => item.count),
        itemStyle: {
          borderRadius: [0, 8, 8, 0],
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: isTown
              ? [
                  { offset: 0, color: '#1e40af' },
                  { offset: 1, color: '#38bdf8' }
                ]
              : [
                  { offset: 0, color: '#0f766e' },
                  { offset: 1, color: '#5eead4' }
                ]
          }
        }
      }
    ]
  }
})

const buildPieOptions = (list?: WaterPondStatsNameCountItem[], title = ''): EChartsOption => {
  const data = toChartRows(list, 8).map((item) => ({ name: item.name, value: item.count }))
  return {
    color: CHART_COLORS,
    tooltip: { trigger: 'item', formatter: '{b}<br/>数量：{c}（{d}%）' },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 0,
      top: 'middle',
      textStyle: { color: '#475569', fontSize: 12 }
    },
    series: [
      {
        name: title,
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['38%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        data
      }
    ]
  }
}

const attrPieOptions = computed(() =>
  attrDim.value === 'ownership'
    ? buildPieOptions(stats.value.ownershipStats, '土地权属')
    : buildPieOptions(stats.value.usageStatusStats, '使用状态')
)

const buildRequestParams = (override?: Partial<WaterPondPageReqVO>) => {
  const source = { ...(props.query || {}), ...(override || {}) }
  const params: Record<string, any> = {}
  Object.keys(source).forEach((key) => {
    const value = (source as any)[key]
    if (value === undefined || value === null || value === '') return
    params[key] = value
  })
  return params
}

const loadStats = async (override?: Partial<WaterPondPageReqVO>) => {
  loading.value = true
  try {
    const data = await getWaterPondStats(buildRequestParams(override))
    stats.value = data || emptyStats()
  } catch {
    stats.value = emptyStats()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})

defineExpose({
  refresh: loadStats
})
</script>

<style scoped>
.pond-stats-wrap {
  margin-bottom: 0;
}

.pond-stats-cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.pond-stat-card {
  position: relative;
  overflow: hidden;
  border-radius: 14px;
  padding: 16px 18px;
  color: #fff;
  min-height: 108px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.1);
}

.pond-stat-card::after {
  content: '';
  position: absolute;
  right: -20px;
  top: -24px;
  width: 110px;
  height: 110px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
}

.pond-stat-card--total {
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
}

.pond-stat-card--area {
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 100%);
}

.pond-stat-card--town {
  background: linear-gradient(135deg, #0369a1 0%, #38bdf8 100%);
}

.pond-stat-card--village {
  background: linear-gradient(135deg, #b45309 0%, #f59e0b 100%);
}

.pond-stat-card__label {
  position: relative;
  z-index: 1;
  font-size: 13px;
  opacity: 0.92;
}

.pond-stat-card__value {
  position: relative;
  z-index: 1;
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: 0.3px;
}

.pond-stat-card__unit {
  margin-left: 4px;
  font-size: 13px;
  font-weight: 500;
  opacity: 0.9;
}

.pond-stat-card__foot {
  position: relative;
  z-index: 1;
  margin-top: 10px;
  font-size: 12px;
  opacity: 0.82;
}

.pond-stats-charts + .pond-stats-charts {
  margin-top: 12px;
}

.pond-chart-card {
  height: 100%;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.05);
  padding: 12px 12px 4px;
}

.pond-chart-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
  padding: 0 4px;
  flex-wrap: wrap;
}

.pond-chart-card__title {
  font-size: 14px;
  font-weight: 700;
  color: #1e3a8a;
}

@media (max-width: 1200px) {
  .pond-stats-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .pond-stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>
