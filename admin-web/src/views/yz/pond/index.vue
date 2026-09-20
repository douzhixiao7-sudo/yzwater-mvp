<template>
  <div class="river-page">
    <ContentWrap class="query-wrap">
      <div class="pond-section-head">
        <h3 class="pond-section-title">条件筛选</h3>
      </div>
      <el-form class="query-form -mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="100px">
        <el-form-item label="资源名称" prop="resourceName">
          <el-input v-model="queryParams.resourceName" placeholder="资源名称" clearable class="!w-220px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="资源编号" prop="resourceCode">
          <el-input v-model="queryParams.resourceCode" placeholder="资源编号" clearable class="!w-220px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="行政区划" prop="villageCode">
          <el-tree-select
            v-model="queryParams.villageCode"
            :data="areaTreeData"
            :props="areaTreeProps"
            node-key="value"
            check-strictly
            filterable
            clearable
            :default-expanded-keys="queryAreaExpandedKeys"
            placeholder="请选择行政区划"
            class="!w-220px"
          />
        </el-form-item>
        <el-form-item label="坐落位置" prop="locationDesc">
          <el-input v-model="queryParams.locationDesc" placeholder="到组一级，如××村××组" clearable class="!w-220px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="权属单位" prop="ownerUnit">
          <el-input v-model="queryParams.ownerUnit" placeholder="权属单位名" clearable class="!w-220px" @keyup.enter="handleSearch" />
        </el-form-item>
        <template v-if="showAdvancedQuery">
          <el-form-item label="土地权属" prop="ownershipType">
            <el-select v-model="queryParams.ownershipType" placeholder="土地权属" clearable filterable class="!w-220px">
              <el-option v-for="item in ownershipTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="资源类型" prop="resourceType">
            <el-select v-model="queryParams.resourceType" placeholder="资源类型" clearable filterable class="!w-220px">
              <el-option v-for="item in resourceTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="使用状态" prop="usageStatus">
            <el-select v-model="queryParams.usageStatus" placeholder="使用状态" clearable filterable class="!w-220px">
              <el-option v-for="item in usageStatusOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="资源性质" prop="resourceNature">
            <el-select v-model="queryParams.resourceNature" placeholder="资源性质" clearable filterable class="!w-220px">
              <el-option v-for="item in resourceNatureOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="占用情况" prop="occupationStatus">
            <el-select v-model="queryParams.occupationStatus" placeholder="占用情况" clearable filterable class="!w-220px">
              <el-option v-for="item in occupationStatusOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="queryParams.remark" placeholder="备注" clearable class="!w-220px" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="调查员" prop="surveyor">
            <el-input v-model="queryParams.surveyor" placeholder="调查员姓名" clearable class="!w-220px" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="实测面积(㎡)">
            <div class="area-range">
              <el-input-number v-model="queryParams.areaSqmMin" :min="0" :precision="2" controls-position="right" placeholder="最小" class="area-range__input" />
              <span class="area-range__sep">-</span>
              <el-input-number v-model="queryParams.areaSqmMax" :min="0" :precision="2" controls-position="right" placeholder="最大" class="area-range__input" />
            </div>
          </el-form-item>
          <el-form-item label="占农登权面积">
            <div class="area-range">
              <el-input-number v-model="queryParams.occupyFarmAreaMin" :min="0" :precision="2" controls-position="right" placeholder="最小" class="area-range__input" />
              <span class="area-range__sep">-</span>
              <el-input-number v-model="queryParams.occupyFarmAreaMax" :min="0" :precision="2" controls-position="right" placeholder="最大" class="area-range__input" />
            </div>
          </el-form-item>
        </template>
        <el-form-item>
          <el-button @click="handleSearch"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="handleReset"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
          <el-button link type="primary" @click="showAdvancedQuery = !showAdvancedQuery">
            {{ showAdvancedQuery ? '收起筛选' : '更多筛选' }}
            <Icon :icon="showAdvancedQuery ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-4px" />
          </el-button>
          <el-button type="primary" plain @click="openCreate">
            <Icon icon="ep:plus" class="mr-5px" /> 新增
          </el-button>
          <el-button type="warning" plain @click="importVisible = true">
            <Icon icon="ep:upload" class="mr-5px" /> 导入
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="stats-wrap">
      <div class="pond-section-head">
        <h3 class="pond-section-title">图表概览</h3>
        <el-button link type="primary" @click="statsVisible = !statsVisible">
          {{ statsVisible ? '收起' : '展开' }}
          <Icon :icon="statsVisible ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-4px" />
        </el-button>
      </div>
      <PondStatsPanel v-show="statsVisible" ref="statsPanelRef" :query="statsQuery" />
    </ContentWrap>

    <ContentWrap class="table-wrap">
      <div class="pond-section-head">
        <h3 class="pond-section-title">数据台账</h3>
      </div>
      <el-table class="river-table" v-loading="tableLoading" :data="tableData">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="资源名称" prop="resourceName" min-width="160" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.resourceName) }}</template>
        </el-table-column>
        <el-table-column label="资源编号" prop="resourceCode" min-width="170" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.resourceCode) }}</template>
        </el-table-column>
        <el-table-column label="土地权属" prop="ownershipType" min-width="110" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.ownershipType) }}</template>
        </el-table-column>
        <el-table-column label="占农登权面积" prop="occupyFarmArea" min-width="130" align="center">
          <template #default="{ row }">{{ formatNumber(row.occupyFarmArea) }}</template>
        </el-table-column>
        <el-table-column label="行政区划" prop="villageCode" min-width="180" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ formatAreaLabel(row.villageCode, row.villageName, row.locationDesc) }}</template>
        </el-table-column>
        <el-table-column label="资源类型" prop="resourceType" min-width="120" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.resourceType) }}</template>
        </el-table-column>
        <el-table-column label="四至东" prop="eastTo" min-width="110" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.eastTo) }}</template>
        </el-table-column>
        <el-table-column label="四至南" prop="southTo" min-width="110" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.southTo) }}</template>
        </el-table-column>
        <el-table-column label="四至西" prop="westTo" min-width="110" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.westTo) }}</template>
        </el-table-column>
        <el-table-column label="四至北" prop="northTo" min-width="110" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.northTo) }}</template>
        </el-table-column>
        <el-table-column label="坐落位置" prop="locationDesc" min-width="200" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.locationDesc) }}</template>
        </el-table-column>
        <el-table-column label="实测面积" prop="areaSqm" min-width="120" align="center">
          <template #default="{ row }">{{ formatNumber(row.areaSqm) }}</template>
        </el-table-column>
        <el-table-column label="权属单位名" prop="ownerUnit" min-width="120" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.ownerUnit) }}</template>
        </el-table-column>
        <el-table-column label="使用状态" prop="usageStatus" min-width="120" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.usageStatus) }}</template>
        </el-table-column>
        <el-table-column label="资源性质" prop="resourceNature" min-width="120" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.resourceNature) }}</template>
        </el-table-column>
        <el-table-column label="占用情况" prop="occupationStatus" min-width="120" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.occupationStatus) }}</template>
        </el-table-column>
        <el-table-column label="调查员姓名" prop="surveyor" min-width="120" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.surveyor) }}</template>
        </el-table-column>
        <el-table-column label="调查员联系方式" prop="surveyorPhone" min-width="140" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.surveyorPhone) }}</template>
        </el-table-column>
        <el-table-column label="中心点坐标" min-width="200" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ formatCenterCoord(row.centerLon, row.centerLat) }}</template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="140" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ displayText(row.remark) }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" fixed="right" class-name="river-table__action-column">
          <template #default="{ row }">
            <div class="river-table__action-cell">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="warning" @click="openSpatial(row)">空间微调</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
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

    <!-- 新增 / 编辑属性 -->
    <el-dialog
      v-model="editVisible"
      class="facility-dialog"
      :title="editIsCreate ? '新增河塘' : '编辑河塘'"
      width="920px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="110px"
        label-position="left"
        v-loading="editLoading"
      >
        <el-row :gutter="16">
          <el-col :span="24">
            <div class="group-title">基础信息</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资源名称" prop="resourceName">
              <el-input v-model="editForm.resourceName" placeholder="请输入资源名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资源编号">
              <el-input
                v-model="editForm.resourceCode"
                :disabled="!editIsCreate"
                :placeholder="editIsCreate ? '选填，不填则系统自动生成' : ''"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="行政区划" prop="villageCode">
              <el-tree-select
                :key="editAreaTreeKey"
                v-model="editForm.villageCode"
                :data="areaTreeData"
                :props="areaTreeProps"
                node-key="value"
                check-strictly
                filterable
                clearable
                :default-expanded-keys="editAreaExpandedKeys"
                placeholder="请选择系统行政区划"
                class="full-input"
                @change="handleEditAreaChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="坐落位置" prop="locationDesc">
              <el-input
                v-model="editForm.locationDesc"
                placeholder="建议填写到组一级，如：月塘镇赵府村小洪组"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资源类型" prop="resourceType">
              <el-input v-model="editForm.resourceType" placeholder="如：坑塘水面" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="editForm.remark" placeholder="备注" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="group-title">权属与规模</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权属单位名" prop="ownerUnit">
              <el-input v-model="editForm.ownerUnit" placeholder="权属单位名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="土地权属" prop="ownershipType">
              <el-input v-model="editForm.ownershipType" placeholder="土地权属" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="国土地类" prop="landType">
              <el-input v-model="editForm.landType" placeholder="国土地类" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实测面积(㎡)" prop="areaSqm">
              <el-input-number v-model="editForm.areaSqm" :min="0" :precision="2" controls-position="right" class="full-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积(亩)" prop="areaMu">
              <el-input-number v-model="editForm.areaMu" :min="0" :precision="2" controls-position="right" class="full-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="占农登权面积" prop="occupyFarmArea">
              <el-input-number v-model="editForm.occupyFarmArea" :min="0" :precision="2" controls-position="right" class="full-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用状态" prop="usageStatus">
              <el-input v-model="editForm.usageStatus" placeholder="使用状态" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资源性质" prop="resourceNature">
              <el-input v-model="editForm.resourceNature" placeholder="资源性质" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="占用情况" prop="occupationStatus">
              <el-input v-model="editForm.occupationStatus" placeholder="占用情况" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="group-title">中心点坐标</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="中心点经度" prop="centerLon">
              <el-input-number v-model="editForm.centerLon" :precision="8" :step="0.000001" controls-position="right" class="full-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="中心点纬度" prop="centerLat">
              <el-input-number v-model="editForm.centerLat" :precision="8" :step="0.000001" controls-position="right" class="full-input" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <div class="field-tip">空间微调保存后会自动根据矢量面更新中心点；也可手动维护。</div>
          </el-col>

          <el-col :span="24">
            <div class="group-title">四至范围</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="东至" prop="eastTo">
              <el-input v-model="editForm.eastTo" placeholder="东至" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="南至" prop="southTo">
              <el-input v-model="editForm.southTo" placeholder="南至" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="西至" prop="westTo">
              <el-input v-model="editForm.westTo" placeholder="西至" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="北至" prop="northTo">
              <el-input v-model="editForm.northTo" placeholder="北至" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="group-title">调查信息</div>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调查员" prop="surveyor">
              <el-input v-model="editForm.surveyor" placeholder="请输入调查员" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式" prop="surveyorPhone">
              <el-input v-model="editForm.surveyorPhone" placeholder="请输入联系方式" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="submitEdit">{{ editIsCreate ? '提交' : '保存' }}</el-button>
      </template>
    </el-dialog>

    <!-- 空间微调 -->
    <el-dialog
      v-model="spatialVisible"
      class="facility-dialog"
      :title="`空间微调 - ${spatialTitle || '河塘'}`"
      width="960px"
      destroy-on-close
      :close-on-click-modal="false"
      @opened="onSpatialOpened"
      @closed="onSpatialClosed"
    >
      <div v-loading="spatialLoading" class="spatial-wrap">
        <div class="group-title">河塘矢量面</div>
        <PondSpatialMap
          ref="spatialMapRef"
          v-model="spatialGeoJson"
          :active="spatialVisible && spatialMapActive"
          :height="480"
          :center="spatialMapCenter"
        />
        <div class="map-tip">点击「编辑」后会出现可拖动节点；拖动调整范围后点「完成编辑」，再点「保存几何」。</div>
      </div>
      <template #footer>
        <el-button @click="spatialVisible = false">取消</el-button>
        <el-button type="primary" :loading="spatialSubmitting" @click="submitSpatial">保存几何</el-button>
      </template>
    </el-dialog>

    <!-- 导入 -->
    <el-dialog
      v-model="importVisible"
      class="facility-dialog"
      title="导入河塘 GeoJSON"
      width="560px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-upload
        class="import-uploader"
        drag
        action="#"
        :auto-upload="false"
        :file-list="importFileList"
        :limit="1"
        accept=".geojson,.json"
        :on-change="handleImportFileChange"
        :on-remove="handleImportFileRemove"
        :on-exceed="handleImportExceed"
      >
        <Icon icon="ep:upload-filled" :size="40" class="import-uploader__icon" />
        <div class="el-upload__text">将 GeoJSON 拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">
            仅支持 .geojson / .json；按 CHBH 同步：已存在则覆盖更新，不存在则新增；文件内重复 CHBH 跳过。
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="submitImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
  type UploadFile,
  type UploadFiles,
  type UploadUserFile
} from 'element-plus'
import PondSpatialMap from './components/PondSpatialMap.vue'
import PondStatsPanel from './components/PondStatsPanel.vue'
import { getAreaTree } from '@/api/system/area'
import {
  createWaterPond,
  deleteWaterPond,
  getWaterPondDetail,
  getWaterPondFilterOptions,
  getWaterPondPage,
  importWaterPondGeoJson,
  updateWaterPond,
  updateWaterPondGeometry,
  type WaterPondPageReqVO,
  type WaterPondPageRespVO,
  type WaterPondSaveReqVO
} from '@/api/gis/waterPond'

defineOptions({ name: 'WaterPondManage' })

const DEFAULT_MAP_CENTER: [number, number] = [32.39, 119.16]

type AreaTreeNode = {
  name: string
  id: number
  children?: AreaTreeNode[]
}

type TreeSelectNode = {
  label: string
  value: string
  children?: TreeSelectNode[]
}

const queryFormRef = ref<FormInstance>()
const statsPanelRef = ref<InstanceType<typeof PondStatsPanel>>()
const statsVisible = ref(true)
const showAdvancedQuery = ref(false)
const queryParams = reactive<WaterPondPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  resourceName: '',
  resourceCode: '',
  villageCode: '',
  locationDesc: '',
  ownerUnit: '',
  ownershipType: '',
  resourceType: '',
  landType: '',
  usageStatus: '',
  resourceNature: '',
  occupationStatus: '',
  remark: '',
  surveyor: '',
  areaMuMin: undefined,
  areaMuMax: undefined,
  areaSqmMin: undefined,
  areaSqmMax: undefined,
  occupyFarmAreaMin: undefined,
  occupyFarmAreaMax: undefined
})

const areaTreeData = ref<TreeSelectNode[]>([])
const areaNameMap = ref<Record<string, string>>({})
const areaParentMap = ref<Record<string, string>>({})
const areaTreeProps = {
  label: 'label',
  value: 'value',
  children: 'children'
}
const usageStatusOptions = ref<string[]>([])
const resourceNatureOptions = ref<string[]>([])
const ownershipTypeOptions = ref<string[]>([])
const occupationStatusOptions = ref<string[]>([])
const resourceTypeOptions = ref<string[]>([])

const buildAreaTree = (list: AreaTreeNode[]): TreeSelectNode[] => {
  return (list || []).map((item) => ({
    label: item.name,
    value: String(item.id),
    children: item.children && item.children.length ? buildAreaTree(item.children) : undefined
  }))
}

const buildAreaNameMap = (nodes: TreeSelectNode[], map: Record<string, string>) => {
  for (const item of nodes || []) {
    if (item?.value) map[item.value] = item.label || item.value
    if (item.children?.length) buildAreaNameMap(item.children, map)
  }
}

const buildAreaParentMap = (nodes: TreeSelectNode[], parentValue?: string) => {
  for (const item of nodes || []) {
    const value = item?.value != null ? String(item.value) : ''
    if (!value) continue
    if (parentValue) areaParentMap.value[value] = parentValue
    if (item.children?.length) buildAreaParentMap(item.children, value)
  }
}

const isTownLevelName = (name?: string) => {
  const n = String(name || '').trim()
  return /(镇|街道|乡|办事处)$/.test(n)
}

const isVillageLevelName = (name?: string) => {
  const n = String(name || '').trim()
  return /(村|社区|居委会|村委会)$/.test(n)
}

const findTownAncestorName = (code?: string) => {
  const c = code ? String(code).trim() : ''
  if (!c) return ''

  const leafName = areaNameMap.value[c] || ''
  if (leafName && isTownLevelName(leafName)) return leafName

  let parentCode = areaParentMap.value[c]
  const visited = new Set<string>()
  while (parentCode && !visited.has(parentCode)) {
    visited.add(parentCode)
    const parentName = areaNameMap.value[parentCode] || ''
    if (parentName && isTownLevelName(parentName)) return parentName
    parentCode = areaParentMap.value[parentCode]
  }
  return ''
}

/** 从坐落位置中解析村名（导入数据区划常为镇级，村名在坐落位置里） */
const extractVillageFromLocation = (locationDesc?: string, townName?: string) => {
  const loc = String(locationDesc || '').trim()
  if (!loc) return ''

  const town = String(townName || '').trim()
  const matches = loc.match(/[\u4e00-\u9fa5]{1,20}(?:村|社区)/g)
  if (!matches?.length) return ''

  for (const item of matches) {
    if (town && item === town) continue
    return item
  }
  return matches[matches.length - 1]
}

const combineTownVillage = (town?: string, village?: string) => {
  const t = String(town || '').trim()
  const v = String(village || '').trim()
  if (!t && !v) return ''
  if (!t) return v
  if (!v || t === v) return t
  if (v.startsWith(t)) return v
  return `${t}/${v}`
}

/** 收集所有可展开节点（有子节点）的 key，用于默认展开整棵树 */
const collectAllExpandableAreaKeys = (nodes: TreeSelectNode[]): string[] => {
  const keys: string[] = []
  for (const node of nodes || []) {
    const value = node.value != null ? String(node.value) : ''
    if (node.children?.length) {
      if (value) keys.push(value)
      keys.push(...collectAllExpandableAreaKeys(node.children))
    }
  }
  return keys
}

/** 查找目标区划从根到该节点的完整路径 key */
const findAreaNodePathKeys = (nodes: TreeSelectNode[], target: string, path: string[] = []): string[] | null => {
  for (const node of nodes || []) {
    const value = node.value != null ? String(node.value) : ''
    if (!value) continue
    const nextPath = [...path, value]
    if (value === target) return nextPath
    if (node.children?.length) {
      const found = findAreaNodePathKeys(node.children, target, nextPath)
      if (found) return found
    }
  }
  return null
}

/** 查看/编辑时：展开到所选区划的全部层级；无选中时仅展开首层 */
const resolveAreaExpandedKeys = (targetCode?: string) => {
  const code = targetCode ? String(targetCode).trim() : ''
  if (!code) {
    return (areaTreeData.value || [])
      .filter((item) => (item.children || []).length > 0)
      .map((item) => String(item.value))
  }
  const path = findAreaNodePathKeys(areaTreeData.value, code)
  if (!path?.length) return collectAllExpandableAreaKeys(areaTreeData.value)
  // 展开路径上所有有子级的节点，确保从根到当前选中项各层均可见
  const expandableOnPath = path.filter((key) => {
    const node = findAreaTreeNode(areaTreeData.value, key)
    return (node?.children || []).length > 0
  })
  return expandableOnPath.length ? expandableOnPath : collectAllExpandableAreaKeys(areaTreeData.value)
}

const findAreaTreeNode = (nodes: TreeSelectNode[], target: string): TreeSelectNode | null => {
  for (const node of nodes || []) {
    const value = node.value != null ? String(node.value) : ''
    if (value === target) return node
    if (node.children?.length) {
      const found = findAreaTreeNode(node.children, target)
      if (found) return found
    }
  }
  return null
}

const editAreaExpandedKeys = ref<string[]>([])
const editAreaTreeKey = ref('edit-area-init')

const queryAreaExpandedKeys = computed(() => resolveAreaExpandedKeys(queryParams.villageCode))

const refreshEditAreaExpandedKeys = () => {
  editAreaExpandedKeys.value = resolveAreaExpandedKeys(editForm.villageCode)
  editAreaTreeKey.value = `edit-area-${editForm.id || 'new'}-${editForm.villageCode || 'none'}`
}

const loadAreaTree = async () => {
  const res = (await getAreaTree()) as any
  const data = (res && (res.data || res)) as AreaTreeNode[]
  areaTreeData.value = buildAreaTree(data || [])
  const map: Record<string, string> = {}
  areaParentMap.value = {}
  buildAreaNameMap(areaTreeData.value, map)
  buildAreaParentMap(areaTreeData.value)
  areaNameMap.value = map
}

const handleEditAreaChange = () => {
  syncVillageNameFromArea()
}

/** 根据所选区划代码自动同步行政区名（仅后台保存，界面不单独展示） */
const syncVillageNameFromArea = () => {
  const code = editForm.villageCode ? String(editForm.villageCode) : ''
  if (!code) {
    editForm.villageName = ''
    return
  }
  editForm.villageName = areaNameMap.value[code] || editForm.villageName || ''
}

const formatAreaLabel = (code?: string, name?: string, locationDesc?: string) => {
  const c = code ? String(code).trim() : ''
  const n = name ? String(name).trim() : ''
  const loc = locationDesc ? String(locationDesc).trim() : ''

  const treeLeaf = c ? areaNameMap.value[c] : ''
  const town = findTownAncestorName(c) || (isTownLevelName(treeLeaf) ? treeLeaf : '') || (isTownLevelName(n) ? n : '')

  let village = ''
  if (treeLeaf && isVillageLevelName(treeLeaf)) {
    village = treeLeaf
  } else if (n && isVillageLevelName(n)) {
    village = n
  } else if (n && n !== town && n !== treeLeaf && !isTownLevelName(n)) {
    village = n
  } else {
    village = extractVillageFromLocation(loc, town || treeLeaf)
  }

  const result = combineTownVillage(town, village)
  return displayText(result || treeLeaf || n || '')
}

const loadFilterOptions = async () => {
  try {
    const data = await getWaterPondFilterOptions()
    usageStatusOptions.value = data?.usageStatusList || []
    resourceNatureOptions.value = data?.resourceNatureList || []
    ownershipTypeOptions.value = data?.ownershipTypeList || []
    occupationStatusOptions.value = data?.occupationStatusList || []
    resourceTypeOptions.value = data?.resourceTypeList || []
  } catch {
    usageStatusOptions.value = []
    resourceNatureOptions.value = []
    ownershipTypeOptions.value = []
    occupationStatusOptions.value = []
    resourceTypeOptions.value = []
  }
}

const tableLoading = ref(false)
const tableData = ref<WaterPondPageRespVO[]>([])
const total = ref(0)

const importVisible = ref(false)
const importLoading = ref(false)
const importFileList = ref<UploadUserFile[]>([])
const importRawFile = ref<File | null>(null)

const editVisible = ref(false)
const editIsCreate = ref(false)
const editLoading = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive<WaterPondSaveReqVO>({
  id: undefined,
  resourceName: '',
  resourceCode: '',
  villageName: '',
  villageCode: '',
  locationDesc: '',
  ownerUnit: '',
  ownershipType: '',
  landType: '',
  areaSqm: undefined,
  areaMu: undefined,
  occupyFarmArea: undefined,
  usageStatus: '',
  resourceNature: '',
  occupationStatus: '',
  remark: '',
  resourceType: '',
  centerLon: undefined,
  centerLat: undefined,
  eastTo: '',
  southTo: '',
  westTo: '',
  northTo: '',
  surveyor: '',
  surveyorPhone: ''
})
const editRules: FormRules = {
  resourceName: [{ required: true, message: '请输入资源名称', trigger: 'blur' }]
}

const spatialVisible = ref(false)
const spatialLoading = ref(false)
const spatialSubmitting = ref(false)
const spatialMapActive = ref(false)
const spatialId = ref<string>('')
const spatialTitle = ref('')
const spatialGeoJson = ref('')
const spatialMapRef = ref<{
  finishEdit?: () => void
  getGeometryGeoJson?: () => string
} | null>(null)
const spatialMapCenter = computed<[number, number]>(() => parseGeometryCenter(spatialGeoJson.value) || DEFAULT_MAP_CENTER)

const normalizeGeoJsonText = (value: unknown): string => {
  if (!value) return ''
  if (typeof value === 'string') return value.trim()
  if (typeof value === 'object') {
    try {
      return JSON.stringify(value)
    } catch {
      return ''
    }
  }
  return ''
}

const displayText = (value?: string | number | null) => {
  if (value === null || value === undefined || value === '') return '-'
  return String(value)
}

const formatNumber = (value?: number | null) => {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '-'
  return Number(value).toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

const formatCoord = (value?: number | null) => {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '-'
  return Number(value).toFixed(6)
}

const formatCenterCoord = (lon?: number | null, lat?: number | null) => {
  const lonText = formatCoord(lon)
  const latText = formatCoord(lat)
  if (lonText === '-' && latText === '-') return '-'
  return `${lonText}, ${latText}`
}

const getList = async () => {
  tableLoading.value = true
  try {
    const data = await getWaterPondPage(queryParams)
    tableData.value = data?.list || []
    total.value = data?.total || 0
  } finally {
    tableLoading.value = false
  }
}

const refreshStats = () => {
  statsPanelRef.value?.refresh?.(statsQuery.value)
}

const statsQuery = computed(() => ({
  resourceName: queryParams.resourceName,
  resourceCode: queryParams.resourceCode,
  villageCode: queryParams.villageCode,
  locationDesc: queryParams.locationDesc,
  ownerUnit: queryParams.ownerUnit,
  ownershipType: queryParams.ownershipType,
  resourceType: queryParams.resourceType,
  landType: queryParams.landType,
  usageStatus: queryParams.usageStatus,
  resourceNature: queryParams.resourceNature,
  occupationStatus: queryParams.occupationStatus,
  remark: queryParams.remark,
  surveyor: queryParams.surveyor,
  areaMuMin: queryParams.areaMuMin,
  areaMuMax: queryParams.areaMuMax,
  areaSqmMin: queryParams.areaSqmMin,
  areaSqmMax: queryParams.areaSqmMax,
  occupyFarmAreaMin: queryParams.occupyFarmAreaMin,
  occupyFarmAreaMax: queryParams.occupyFarmAreaMax
}))

watch(statsVisible, async (visible) => {
  if (!visible) return
  await nextTick()
  window.dispatchEvent(new Event('resize'))
})

const handleSearch = () => {
  queryParams.pageNo = 1
  getList()
  refreshStats()
}

const handleReset = () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  queryParams.pageSize = 10
  queryParams.resourceName = ''
  queryParams.resourceCode = ''
  queryParams.villageCode = ''
  queryParams.locationDesc = ''
  queryParams.ownerUnit = ''
  queryParams.ownershipType = ''
  queryParams.resourceType = ''
  queryParams.landType = ''
  queryParams.usageStatus = ''
  queryParams.resourceNature = ''
  queryParams.occupationStatus = ''
  queryParams.remark = ''
  queryParams.surveyor = ''
  queryParams.areaMuMin = undefined
  queryParams.areaMuMax = undefined
  queryParams.areaSqmMin = undefined
  queryParams.areaSqmMax = undefined
  queryParams.occupyFarmAreaMin = undefined
  queryParams.occupyFarmAreaMax = undefined
  getList()
  refreshStats()
}

const fillEditForm = (detail: WaterPondSaveReqVO) => {
  editForm.id = detail.id
  editForm.facilityId = detail.facilityId
  editForm.resourceName = detail.resourceName || ''
  editForm.resourceCode = detail.resourceCode || ''
  editForm.villageName = detail.villageName || ''
  editForm.villageCode = detail.villageCode || ''
  syncVillageNameFromArea()
  if (!editForm.villageName && detail.villageName) {
    editForm.villageName = detail.villageName
  }
  editForm.locationDesc = detail.locationDesc || ''
  editForm.ownerUnit = detail.ownerUnit || ''
  editForm.ownershipType = detail.ownershipType || ''
  editForm.landType = detail.landType || ''
  editForm.areaSqm = detail.areaSqm
  editForm.areaMu = detail.areaMu
  editForm.occupyFarmArea = detail.occupyFarmArea
  editForm.usageStatus = detail.usageStatus || ''
  editForm.resourceNature = detail.resourceNature || ''
  editForm.occupationStatus = detail.occupationStatus || ''
  editForm.remark = detail.remark || ''
  editForm.resourceType = detail.resourceType || ''
  editForm.centerLon = detail.centerLon
  editForm.centerLat = detail.centerLat
  editForm.eastTo = detail.eastTo || ''
  editForm.southTo = detail.southTo || ''
  editForm.westTo = detail.westTo || ''
  editForm.northTo = detail.northTo || ''
  editForm.surveyor = detail.surveyor || ''
  editForm.surveyorPhone = detail.surveyorPhone || ''
  refreshEditAreaExpandedKeys()
}

const resetEditForm = () => {
  editForm.id = undefined
  editForm.facilityId = undefined
  editForm.resourceName = ''
  editForm.resourceCode = ''
  editForm.villageName = ''
  editForm.villageCode = ''
  editForm.locationDesc = ''
  editForm.ownerUnit = ''
  editForm.ownershipType = ''
  editForm.landType = ''
  editForm.areaSqm = undefined
  editForm.areaMu = undefined
  editForm.occupyFarmArea = undefined
  editForm.usageStatus = ''
  editForm.resourceNature = ''
  editForm.occupationStatus = ''
  editForm.remark = ''
  editForm.resourceType = '坑塘水面'
  editForm.centerLon = undefined
  editForm.centerLat = undefined
  editForm.eastTo = ''
  editForm.southTo = ''
  editForm.westTo = ''
  editForm.northTo = ''
  editForm.surveyor = ''
  editForm.surveyorPhone = ''
  editAreaExpandedKeys.value = []
  editAreaTreeKey.value = 'edit-area-new'
  nextTick(() => editFormRef.value?.clearValidate())
}

const openCreate = () => {
  editIsCreate.value = true
  resetEditForm()
  editVisible.value = true
  editLoading.value = false
}

const openEdit = async (row: WaterPondPageRespVO) => {
  if (!row?.id) return
  editIsCreate.value = false
  editVisible.value = true
  editLoading.value = true
  editAreaExpandedKeys.value = resolveAreaExpandedKeys(row.villageCode)
  try {
    const detail = (await getWaterPondDetail(String(row.id))) as WaterPondSaveReqVO
    fillEditForm(detail || {})
  } catch (e: any) {
    ElMessage.error(e?.message || '加载详情失败')
    editVisible.value = false
  } finally {
    editLoading.value = false
  }
}

const submitEdit = async () => {
  await editFormRef.value?.validate()
  syncVillageNameFromArea()
  editSubmitting.value = true
  try {
    const payload = { ...editForm }
    if (editIsCreate.value) {
      delete payload.id
      delete payload.facilityId
      await createWaterPond(payload)
      ElMessage.success('新增成功')
    } else {
      await updateWaterPond(payload)
      ElMessage.success('保存成功')
    }
    editVisible.value = false
    await getList()
    refreshStats()
  } catch (e: any) {
    ElMessage.error(e?.message || (editIsCreate.value ? '新增失败' : '保存失败'))
  } finally {
    editSubmitting.value = false
  }
}

const openSpatial = async (row: WaterPondPageRespVO) => {
  if (!row?.id) return
  spatialId.value = String(row.id)
  spatialTitle.value = row.resourceName || row.resourceCode || ''
  spatialGeoJson.value = ''
  spatialMapActive.value = false
  spatialVisible.value = true
  spatialLoading.value = true
  try {
    const detail = (await getWaterPondDetail(String(row.id))) as WaterPondSaveReqVO
    spatialGeoJson.value = normalizeGeoJsonText(detail?.geometryGeoJson)
    spatialTitle.value = detail?.resourceName || spatialTitle.value
    if (!spatialGeoJson.value) {
      ElMessage.warning('该河塘库内暂无矢量，请先绘制面后再保存')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载空间数据失败')
  } finally {
    spatialLoading.value = false
  }
}

const onSpatialOpened = async () => {
  await nextTick()
  spatialMapActive.value = true
}

const onSpatialClosed = () => {
  spatialMapActive.value = false
  spatialId.value = ''
  spatialGeoJson.value = ''
}

const submitSpatial = async () => {
  if (!spatialId.value) return
  spatialMapRef.value?.finishEdit?.()
  await nextTick()
  const geometryGeoJson = spatialMapRef.value?.getGeometryGeoJson?.() || spatialGeoJson.value || ''
  if (!geometryGeoJson) {
    ElMessage.warning('当前没有可保存的河塘面')
    return
  }
  spatialSubmitting.value = true
  try {
    await updateWaterPondGeometry(spatialId.value, geometryGeoJson)
    ElMessage.success('库内矢量已更新')
    spatialVisible.value = false
    await getList()
    refreshStats()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存几何失败')
  } finally {
    spatialSubmitting.value = false
  }
}

const handleDelete = async (row: WaterPondPageRespVO) => {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm(`确认删除河塘「${row.resourceName || row.resourceCode}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await deleteWaterPond(String(row.id))
    ElMessage.success('已删除')
    await getList()
    refreshStats()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

const handleImportFileChange = (file: UploadFile, files: UploadFiles) => {
  importFileList.value = files.slice(-1) as UploadUserFile[]
  importRawFile.value = (file.raw as File) || null
}

const handleImportFileRemove = () => {
  importFileList.value = []
  importRawFile.value = null
}

const handleImportExceed = () => {
  ElMessage.warning('一次只能选择一个文件')
}

const submitImport = async () => {
  if (!importRawFile.value) {
    ElMessage.warning('请先选择 GeoJSON 文件')
    return
  }
  const name = importRawFile.value.name?.toLowerCase() || ''
  if (!name.endsWith('.geojson') && !name.endsWith('.json')) {
    ElMessage.warning('仅支持 .geojson / .json 文件')
    return
  }
  const formData = new FormData()
  formData.append('file', importRawFile.value)
  importLoading.value = true
  try {
    const resp = await importWaterPondGeoJson(formData)
    ElMessage.success(resp?.message || '导入完成')
    importVisible.value = false
    importFileList.value = []
    importRawFile.value = null
    await getList()
    refreshStats()
  } catch (e: any) {
    ElMessage.error(e?.message || '导入失败')
  } finally {
    importLoading.value = false
  }
}

/** 从 GeoJSON geometry 估算地图中心 [lat, lon] */
function parseGeometryCenter(geoJson?: string): [number, number] | null {
  if (!geoJson) return null
  try {
    const parsed = JSON.parse(geoJson)
    const coords = parsed?.coordinates
    if (!coords) return null
    const flat: number[][] = []
    const walk = (node: any) => {
      if (!Array.isArray(node) || node.length === 0) return
      if (typeof node[0] === 'number' && typeof node[1] === 'number') {
        flat.push([node[0], node[1]])
        return
      }
      node.forEach(walk)
    }
    walk(coords)
    if (!flat.length) return null
    let sumLon = 0
    let sumLat = 0
    for (const [lon, lat] of flat) {
      sumLon += lon
      sumLat += lat
    }
    return [sumLat / flat.length, sumLon / flat.length]
  } catch {
    return null
  }
}

onMounted(async () => {
  await Promise.all([loadAreaTree(), loadFilterOptions()])
  getList()
})
</script>

<style scoped>
.pond-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.22);
}

.pond-section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1e3a8a;
  line-height: 1.3;
  letter-spacing: 0.2px;
}

.pond-section-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 14px;
  margin-right: 8px;
  border-radius: 2px;
  background: #2563eb;
  vertical-align: -2px;
}

.river-page {
  --river-bg: #f3f7fc;
  --river-card-bg: #ffffff;
  --river-border: rgba(148, 163, 184, 0.28);
  --river-shadow: 0 10px 28px rgba(15, 23, 42, 0.07);
  --river-primary: #1e40af;
  --river-primary-soft: rgba(30, 64, 175, 0.08);
  --river-row-hover-solid: #eef2ff;
  --river-text-main: #1f2a37;

  padding: 4px 2px 10px;
  background: linear-gradient(180deg, #f8fbff 0%, var(--river-bg) 100%);
}

.query-wrap,
.stats-wrap,
.table-wrap {
  background: var(--river-card-bg);
  border: 1px solid var(--river-border);
  border-radius: 14px;
  box-shadow: var(--river-shadow);
}

.stats-wrap,
.table-wrap {
  margin-top: 12px;
}

.query-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.query-form :deep(.el-form-item__label) {
  color: var(--river-text-main);
  font-weight: 600;
}

.query-form :deep(.el-input__wrapper),
.query-form :deep(.el-select__wrapper),
.query-form :deep(.el-tree-select__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.32) inset;
  transition: box-shadow 0.2s ease, background-color 0.2s ease;
}

.query-form :deep(.el-input__wrapper:hover),
.query-form :deep(.el-select__wrapper:hover),
.query-form :deep(.el-tree-select__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(30, 64, 175, 0.42) inset;
}

.query-form :deep(.el-input__wrapper.is-focus),
.query-form :deep(.el-select__wrapper.is-focused),
.query-form :deep(.el-tree-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--river-primary) inset;
}

.query-form :deep(.el-button) {
  border-radius: 10px;
  font-weight: 600;
}

.river-table {
  border-radius: 12px;
}

.river-table :deep(.el-table__header th) {
  background: #f5f8ff;
  color: #1e3a8a;
  font-weight: 700;
}

.river-table :deep(.el-table__row > td) {
  transition: background-color 0.2s ease;
}

.river-table :deep(.el-table__body tr:hover > td) {
  background: var(--river-primary-soft);
}

.river-table :deep(.el-table__fixed-right),
.river-table :deep(.el-table__fixed-right-patch),
.river-table :deep(.el-table__fixed-right .el-table__body-wrapper),
.river-table :deep(.el-table__fixed-right .el-table__fixed-body-wrapper) {
  background-color: #fff !important;
  z-index: 5;
}

.river-table :deep(.el-table__fixed-right .el-table__header-wrapper th.el-table__cell),
.river-table :deep(.el-table__header th.el-table-fixed-column--right) {
  background-color: #f5f8ff !important;
}

.river-table :deep(.el-table__fixed-right .el-table__body-wrapper td.el-table__cell),
.river-table :deep(td.el-table-fixed-column--right),
.river-table :deep(.river-table__action-column.el-table__cell) {
  background-color: #fff !important;
}

.river-table :deep(.el-table__fixed-right .el-table__body tr:hover > td.el-table__cell),
.river-table :deep(.el-table__body tr:hover > td.el-table-fixed-column--right),
.river-table :deep(.el-table__body tr:hover > .river-table__action-column.el-table__cell) {
  background-color: var(--river-row-hover-solid) !important;
}

.river-table :deep(.river-table__action-column .cell) {
  padding: 0 !important;
  overflow: hidden;
  background-color: #fff !important;
}

.river-table :deep(.el-table__body tr:hover .river-table__action-column .cell) {
  background-color: var(--river-row-hover-solid) !important;
}

.river-table__action-cell {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  width: 100%;
  box-sizing: border-box;
  min-height: 40px;
  padding: 8px 10px;
  background-color: #fff;
  white-space: nowrap;
}

.river-table :deep(.el-table__body tr:hover .river-table__action-cell) {
  background-color: var(--river-row-hover-solid);
}

.river-table :deep(.el-button.is-link) {
  font-weight: 600;
}

.table-wrap :deep(.el-pagination) {
  margin-top: 14px;
  justify-content: flex-end;
}

:deep(.facility-dialog .el-dialog) {
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.2);
}

:deep(.facility-dialog .el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.24);
  background: linear-gradient(90deg, #f8fbff 0%, #f3f7ff 100%);
}

:deep(.facility-dialog .el-dialog__title) {
  font-weight: 700;
  color: #1e3a8a;
}

:deep(.facility-dialog .el-dialog__body) {
  padding: 16px 20px;
}

:deep(.facility-dialog .el-dialog__footer) {
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  padding: 12px 20px;
}

.group-title {
  font-weight: 600;
  margin: 10px 0 12px;
  padding: 6px 10px;
  color: #303133;
  background: #f5f7fa;
  border-left: 4px solid #409eff;
  border-radius: 4px;
}

.map-tip {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

.spatial-wrap {
  min-height: 480px;
}

.field-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.full-input {
  width: 100%;
}

.area-range {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.area-range__input {
  width: 110px;
}

.area-range__sep {
  color: #909399;
}

.import-uploader :deep(.el-upload) {
  width: 100%;
}

.import-uploader :deep(.el-upload-dragger) {
  width: 100%;
  border-radius: 12px;
  border-color: rgba(148, 163, 184, 0.45);
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
}

.import-uploader :deep(.el-upload-dragger:hover) {
  border-color: var(--river-primary);
}

.import-uploader__icon {
  color: #409eff;
  margin-bottom: 8px;
}

.import-uploader :deep(.el-upload__tip) {
  line-height: 1.5;
  margin-top: 8px;
  color: #909399;
}

@media (max-width: 768px) {
  .query-form :deep(.el-input),
  .query-form :deep(.el-select),
  .query-form :deep(.el-tree-select) {
    width: 100% !important;
  }

  .query-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }
}
</style>
