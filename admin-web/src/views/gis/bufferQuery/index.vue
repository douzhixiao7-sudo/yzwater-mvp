<template>
  <div class="buffer-page">
    <el-card class="buffer-page__card">
      <template #header>
        <div class="buffer-page__title">缓冲区管理</div>
      </template>
      <el-table v-loading="pageLoading" :data="pageList" stripe>
        <el-table-column prop="statsTime" label="统计时间" width="170" />
        <el-table-column label="半径(米)" width="110">
          <template #default="{ row }">
            {{ row.radiusMeters || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="面积" width="120">
          <template #default="{ row }">
            {{ formatAreaText(row.bufferAreaM2) }}
          </template>
        </el-table-column>
        <el-table-column prop="facilityCount" label="设施数量" width="100" />
        <el-table-column label="涉及行政区" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ (row.adminAreaNames || []).join('、') || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="设施类型" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatFacilityTypes(row.facilityTypes || []) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="buffer-page__pagination">
        <el-pagination
          v-model:current-page="pageParams.pageNo"
          v-model:page-size="pageParams.pageSize"
          layout="total, prev, pager, next"
          :total="pageTotal"
          @current-change="loadPage"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" width="980px" :destroy-on-close="true" align-center>
      <template #header>
        <div class="buffer-page__detail-title">缓冲区详情</div>
      </template>
      <div v-loading="detailLoading" class="buffer-page__detail-body">
        <div class="buffer-page__detail-kpi">
          <div class="buffer-page__detail-kpi-row">
            <span>统计时间</span>
            <span>{{ detailData?.statsTime || '-' }}</span>
          </div>
          <div class="buffer-page__detail-kpi-row">
            <span>缓冲区半径</span>
            <span>{{ detailData?.radiusMeters || '-' }} 米</span>
          </div>
          <div class="buffer-page__detail-kpi-row">
            <span>缓冲区面积</span>
            <span>{{ formatAreaText(detailData?.bufferAreaM2) }}</span>
          </div>
          <div class="buffer-page__detail-kpi-row">
            <span>设施数量</span>
            <span>{{ detailData?.facilityCount || 0 }} 条</span>
          </div>
        </div>

        <div class="buffer-page__detail-section">
          <div class="buffer-page__detail-section-title">缓冲区范围</div>
          <TiandituGeoJsonPreview
            :geo-json="detailData?.bufferGeoJson"
            :active="detailVisible"
            :height="320"
            :highlight="true"
          />
        </div>

        <div class="buffer-page__detail-section">
          <div class="buffer-page__detail-section-title">设施列表</div>
          <el-table :data="detailFacilityList" size="small" stripe @row-click="openFacilityDetail">
            <el-table-column prop="facilityName" label="名称" min-width="160" show-overflow-tooltip />
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                {{ facilityTypeLabelMap[String(row.facilityType || '')] || row.facilityType || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="adminRegionCode" label="行政区划" width="120" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="facilityDetailVisible" width="760px" :destroy-on-close="true" align-center>
      <template #header>
        <div class="buffer-page__detail-title">{{ facilityDetail?.facilityName || '设施详情' }}</div>
      </template>
      <div v-loading="facilityDetailLoading" class="buffer-page__facility-detail">
        <TiandituGeoJsonPreview
          :geo-json="facilityDetail?.geometryGeoJson"
          :active="facilityDetailVisible"
          :height="280"
          :highlight="true"
          :label-text="facilityDetail?.facilityName || ''"
        />
        <el-descriptions :column="1" size="small" border class="buffer-page__facility-desc">
          <el-descriptions-item label="设施类型">
            {{ facilityDetail?.facilityType || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="行政区划">
            {{ facilityDetail?.adminRegion || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="设施编码">
            {{ facilityDetail?.facilityCode || '-' }}
          </el-descriptions-item>
        </el-descriptions>
        <div class="buffer-page__facility-attr-title">属性信息</div>
        <el-table v-if="facilityAttrRows.length" :data="facilityAttrRows" size="small" border>
          <el-table-column prop="key" label="属性" width="160" />
          <el-table-column prop="value" label="值" />
        </el-table>
        <div v-else class="buffer-page__empty">暂无属性</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import {
  getBufferQueryDetail,
  getBufferQueryPage,
  type GisBufferQueryFacilityRespVO,
  type GisBufferQueryPageItemRespVO
} from '@/api/gis/bufferQuery'
import { getWaterFacilityDetail, getWaterFacilityDict, type WaterFacilityDetailRespVO } from '@/api/gis/waterFacility'

defineOptions({ name: 'GisBufferQuery' })

type DictOption = { label: string; value: string }

const pageParams = reactive({
  pageNo: 1,
  pageSize: 10
})
const pageLoading = ref(false)
const pageTotal = ref(0)
const pageList = ref<GisBufferQueryPageItemRespVO[]>([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<any | null>(null)

const facilityDetailVisible = ref(false)
const facilityDetailLoading = ref(false)
const facilityDetail = ref<WaterFacilityDetailRespVO | null>(null)

const facilityTypeOptions = ref<DictOption[]>([])
const facilityTypeLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  for (const item of facilityTypeOptions.value) {
    if (!item?.value) continue
    map[item.value] = item.label
  }
  return map
})

const detailFacilityList = computed<GisBufferQueryFacilityRespVO[]>(() => {
  return (detailData.value?.facilities || []) as GisBufferQueryFacilityRespVO[]
})

const facilityAttrRows = computed(() => {
  const attrs = (facilityDetail.value?.attributes || {}) as Record<string, any>
  return Object.keys(attrs).map((key) => ({
    key,
    value: attrs[key] == null ? '-' : String(attrs[key])
  }))
})

const loadFacilityTypes = async () => {
  const list: any[] = (await getWaterFacilityDict('zd_sslb')) as any
  facilityTypeOptions.value = (Array.isArray(list) ? list : [])
    .filter(
      (it: any) =>
        it &&
        typeof it.value === 'string' &&
        typeof it.label === 'string' &&
        (it.status === undefined || it.status === 0)
    )
    .map((it: any) => ({ label: it.label, value: it.value }))
}

const loadPage = async () => {
  pageLoading.value = true
  try {
    const data = await getBufferQueryPage(pageParams)
    pageList.value = data?.list || []
    pageTotal.value = Number(data?.total || 0)
  } finally {
    pageLoading.value = false
  }
}

const openDetail = async (row: any) => {
  const id = row?.id
  if (!id) return
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const data = await getBufferQueryDetail(id)
    detailData.value = data || null
  } finally {
    detailLoading.value = false
  }
}

const openFacilityDetail = async (row: GisBufferQueryFacilityRespVO) => {
  const id = row?.facilityId
  if (!id) return
  facilityDetailVisible.value = true
  facilityDetailLoading.value = true
  facilityDetail.value = null
  try {
    const detail = await getWaterFacilityDetail(String(id))
    facilityDetail.value = detail || null
  } finally {
    facilityDetailLoading.value = false
  }
}

const formatAreaText = (areaM2?: number) => {
  const area = Number(areaM2)
  if (!Number.isFinite(area) || area <= 0) return '-'
  if (area >= 1_000_000) {
    return `${(area / 1_000_000).toFixed(2)} km²`
  }
  return `${area.toFixed(0)} ㎡`
}

const formatFacilityTypes = (types: string[]) => {
  if (!types || types.length === 0) return '全部'
  return types.map((t) => facilityTypeLabelMap.value[String(t)] || t).join('、')
}

onMounted(async () => {
  try {
    await Promise.all([loadFacilityTypes(), loadPage()])
  } catch (e: any) {
    ElMessage.error(e?.message || '缓冲区数据加载失败')
  }
})
</script>

<style scoped>
.buffer-page__card {
  border-radius: 8px;
}

.buffer-page__title {
  font-weight: 700;
  font-size: 16px;
  color: #1f2937;
}

.buffer-page__pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}

.buffer-page__detail-title {
  font-weight: 700;
  font-size: 16px;
  color: #111827;
}

.buffer-page__detail-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.buffer-page__detail-kpi {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
  background: #eaf2fc;
  padding: 12px 16px;
  border-radius: 6px;
}

.buffer-page__detail-kpi-row {
  display: flex;
  gap: 6px;
  font-size: 13px;
  color: #374151;
}

.buffer-page__detail-section-title {
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
}

.buffer-page__facility-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.buffer-page__facility-desc {
  margin-top: 6px;
}

.buffer-page__facility-attr-title {
  font-weight: 700;
  font-size: 13px;
  color: #111827;
}

.buffer-page__empty {
  color: #9ca3af;
  font-size: 12px;
}
</style>
