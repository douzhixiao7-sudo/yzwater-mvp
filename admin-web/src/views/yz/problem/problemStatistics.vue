<template>
  <div class="river-page">
    <ContentWrap class="problem-statistics-wrap">
      <el-row :gutter="12" class="dashboard-row">
        <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
          <el-card v-loading="dashboardLoading[periodStatus]" shadow="never" class="dashboard-card">
            <template #header>
              <div class="dashboard-header">
                <span class="dashboard-title">问题统计</span>
                <el-radio-group v-model="periodStatus" size="small" @change="() => ensureDashboard(periodStatus, true)">
                  <el-radio-button label="month">本月</el-radio-button>
                  <el-radio-button label="quarter">本季度</el-radio-button>
                  <el-radio-button label="year">本年度</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <el-row :gutter="8">
              <el-col :span="12">
                <div class="stat-box stat-blue">
                  <div class="stat-label">待受理</div>
                  <div class="stat-value">{{ statusStat.pendingCount }}<span class="stat-unit">个</span></div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-box stat-green">
                  <div class="stat-label">处理中</div>
                  <div class="stat-value">{{ statusStat.processingCount }}<span class="stat-unit">个</span></div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-box stat-orange">
                  <div class="stat-label">待核验</div>
                  <div class="stat-value">{{ statusStat.pendingVerifyCount }}<span class="stat-unit">个</span></div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-box stat-red">
                  <div class="stat-label">已办结</div>
                  <div class="stat-value">{{ statusStat.finishedCount }}<span class="stat-unit">个</span></div>
                </div>
              </el-col>
            </el-row>
          </el-card>
        </el-col>

        <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
          <el-card v-loading="dashboardLoading[periodType]" shadow="never" class="dashboard-card">
            <template #header>
              <div class="dashboard-header">
                <span class="dashboard-title">类型占比</span>
                <el-radio-group v-model="periodType" size="small" @change="() => ensureDashboard(periodType, true)">
                  <el-radio-button label="month">本月</el-radio-button>
                  <el-radio-button label="quarter">本季度</el-radio-button>
                  <el-radio-button label="year">本年度</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <Echart :options="typePieOptions" :height="200" />
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="12" class="dashboard-row">
        <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
          <el-card v-loading="dashboardLoading[periodTop5]" shadow="never" class="dashboard-card">
            <template #header>
              <div class="dashboard-header">
                <span class="dashboard-title">问题TOP5</span>
                <el-radio-group v-model="periodTop5" size="small" @change="() => ensureDashboard(periodTop5, true)">
                  <el-radio-button label="month">本月</el-radio-button>
                  <el-radio-button label="quarter">本季度</el-radio-button>
                  <el-radio-button label="year">本年度</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <el-table :data="top5List" size="small" border height="200" class="top5-table">
              <el-table-column label="设施名称" prop="referenceName" min-width="180" show-overflow-tooltip />
              <el-table-column label="问题次数" prop="count" width="90" align="center" />
            </el-table>
          </el-card>
        </el-col>

        <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
          <el-card v-loading="dashboardLoading[periodParticipation]" shadow="never" class="dashboard-card">
            <template #header>
              <div class="dashboard-header">
                <span class="dashboard-title">公众参与度</span>
                <el-radio-group
                  v-model="periodParticipation"
                  size="small"
                  @change="() => ensureDashboard(periodParticipation, true)"
                >
                  <el-radio-button label="month">本月</el-radio-button>
                  <el-radio-button label="quarter">本季度</el-radio-button>
                  <el-radio-button label="year">本年度</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div class="participation-wrap">
              <div class="circle-metric">
                <div class="circle-number">{{ participation.totalFeedbackCount }}</div>
                <div class="circle-label">反馈总次数</div>
              </div>
              <div class="circle-progress">
                <el-progress type="dashboard" :percentage="participation.realNameRate" :width="98" :stroke-width="10" />
                <div class="circle-label">实名率</div>
              </div>
              <div class="circle-metric circle-green">
                <div class="circle-number">{{ participation.feedbackPersonCount }}</div>
                <div class="circle-label">反馈人数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { EChartsOption } from 'echarts'
import { getProblemFeedbackDashboard, type ProblemFeedbackDashboardRespVO } from '@/api/yz/problemFeedback'

type PeriodType = 'month' | 'quarter' | 'year'

const periodStatus = ref<PeriodType>('month')
const periodType = ref<PeriodType>('month')
const periodTop5 = ref<PeriodType>('month')
const periodParticipation = ref<PeriodType>('month')

const dashboardCache = reactive<Record<PeriodType, ProblemFeedbackDashboardRespVO | null>>({
  month: null,
  quarter: null,
  year: null
})
const dashboardLoading = reactive<Record<PeriodType, boolean>>({
  month: false,
  quarter: false,
  year: false
})

const ensureDashboard = async (period: PeriodType, force = false) => {
  if (!force && dashboardCache[period]) return
  if (dashboardLoading[period]) return
  dashboardLoading[period] = true
  try {
    dashboardCache[period] = (await getProblemFeedbackDashboard(period)) || null
  } finally {
    dashboardLoading[period] = false
  }
}

const statusStat = computed(() => {
  const data = dashboardCache[periodStatus.value]?.statusStat
  return {
    pendingCount: data?.pendingCount || 0,
    processingCount: data?.processingCount || 0,
    pendingVerifyCount: data?.pendingVerifyCount || 0,
    finishedCount: data?.finishedCount || 0
  }
})

const top5List = computed(() => {
  return dashboardCache[periodTop5.value]?.top5List || []
})

const participation = computed(() => {
  const data = dashboardCache[periodParticipation.value]?.participation
  return {
    totalFeedbackCount: data?.totalFeedbackCount || 0,
    feedbackPersonCount: data?.feedbackPersonCount || 0,
    realNameRate: data?.realNameRate || 0
  }
})

const typePieOptions = computed<EChartsOption>(() => {
  const list = dashboardCache[periodType.value]?.typeStatList || []
  const data = list.map((it) => ({
    name: it.feedbackTypeLabel || it.feedbackType,
    value: it.count
  }))
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}<br/>数量：{c}次<br/>占比：{d}%'
    },
    legend: {
      left: 'center',
      bottom: 0,
      width: '92%',
      type: 'scroll'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '68%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        labelLayout: { hideOverlap: false },
        label: {
          show: true,
          fontSize: 12,
          lineHeight: 16,
          formatter: (params: any) => {
            const name = String(params?.name ?? '').trim()
            const value = Number(params?.value ?? 0)
            const percentRaw = Number(params?.percent ?? 0)
            const percent = Number.isFinite(percentRaw) ? percentRaw.toFixed(1).replace(/\.0$/, '') : '0'
            return `${name}\n${percent}% (${Number.isFinite(value) ? value : 0}次)`
          }
        },
        labelLine: { show: true, length: 10, length2: 8 },
        emphasis: {
          scale: true,
          scaleSize: 8,
          label: { show: true, formatter: '{b}\n{d}% ({c}次)', fontSize: 12 }
        },
        data
      }
    ]
  }
})

onMounted(async () => {
  await ensureDashboard('month', true)
})
</script>

<style scoped>
.river-page {
  --ps-bg: #f2f6fc;
  --ps-surface: #ffffff;
  --ps-border: #d6e2f5;
  --ps-text: #1f2d3d;
  --ps-text-soft: #4e6a8a;
  --ps-accent: #2f74ff;
  --ps-shadow: 0 8px 20px rgba(23, 72, 151, 0.08);
  --ps-shadow-hover: 0 12px 26px rgba(23, 72, 151, 0.12);
  padding: 0;
  background: transparent;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}

.problem-statistics-wrap {
  border: none;
  background: transparent;
  box-shadow: none;
}

.dashboard-row + .dashboard-row {
  margin-top: 12px;
}

.dashboard-card {
  height: 260px;
  border: 1px solid #d6e2f5;
  border-radius: 14px;
  box-shadow: 0 8px 18px rgba(23, 72, 151, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.dashboard-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--ps-shadow-hover);
}

.dashboard-card :deep(.el-card__header) {
  padding: 10px 12px;
  border-bottom: 1px solid #dce7f8;
  background: linear-gradient(90deg, rgba(47, 116, 255, 0.08), rgba(86, 168, 255, 0.08));
}

.dashboard-card :deep(.el-card__body) {
  padding: 12px;
}

.dashboard-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.dashboard-title {
  font-weight: 700;
  color: var(--ps-text);
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  letter-spacing: 0.2px;
}

.dashboard-header :deep(.el-radio-group) {
  --el-border-radius-base: 9px;
}

.dashboard-header :deep(.el-radio-button__inner) {
  border-color: #cfd8e5;
  color: #2b4f7f;
  font-weight: 600;
}

.dashboard-header :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #2f74ff;
  border-color: #2f74ff;
  box-shadow: -1px 0 0 0 #2f74ff;
}

.stat-box {
  border-radius: 12px;
  padding: 12px;
  color: #fff;
  min-height: 78px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border: 1px solid rgba(255, 255, 255, 0.24);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.06), 0 8px 18px rgba(15, 23, 42, 0.12);
}

.stat-label {
  font-size: 13px;
  opacity: 0.96;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}

.stat-unit {
  margin-left: 2px;
  font-size: 12px;
  font-weight: 400;
  opacity: 0.9;
}

.stat-blue {
  background: linear-gradient(135deg, #1e4fae 0%, #2f74ff 100%);
}

.stat-green {
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 100%);
}

.stat-orange {
  background: linear-gradient(135deg, #d97706 0%, #f59e0b 100%);
}

.stat-red {
  background: linear-gradient(135deg, #b91c1c 0%, #ef4444 100%);
}

.top5-table {
  --el-table-border-color: #dbe4ef;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f8fbff;
}

.top5-table :deep(.el-table__header-wrapper th) {
  color: #2b4f7f;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  font-weight: 700;
}

.participation-wrap {
  display: flex;
  align-items: center;
  justify-content: space-around;
  gap: 10px;
  height: 200px;
}

.circle-metric {
  width: 102px;
  height: 102px;
  border-radius: 50%;
  border: 2px solid #2f74ff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background: rgba(47, 116, 255, 0.08);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.44);
}

.circle-metric.circle-green {
  border-color: #2c9cf0;
  background: rgba(44, 156, 240, 0.08);
}

.circle-number {
  font-size: 24px;
  font-weight: 700;
  color: #2f74ff;
  line-height: 1;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}

.circle-metric.circle-green .circle-number {
  color: #2c9cf0;
}

.circle-label {
  font-size: 12px;
  color: var(--ps-text-soft);
  text-align: center;
}

.circle-progress {
  width: 110px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.circle-progress :deep(.el-progress-dashboard__path) {
  stroke: #e2e8f0;
}

.circle-progress :deep(.el-progress-dashboard__trail) {
  stroke: #2f74ff;
}

@media (max-width: 1024px) {
  .dashboard-card {
    height: auto;
    min-height: 260px;
  }

  .participation-wrap {
    height: auto;
    padding: 10px 0;
    flex-wrap: wrap;
  }
}

@media (prefers-reduced-motion: reduce) {
  .dashboard-card {
    transition: none;
  }
}
</style>
