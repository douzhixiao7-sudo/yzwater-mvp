<template>
  <div class="inspection-plan-page">
    <ContentWrap>
      <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="巡检类型" prop="inspectionType">
          <el-select
            v-model="queryParams.inspectionType"
            clearable
            filterable
            placeholder="所有巡检类型"
            class="!w-190px"
          >
            <el-option
              v-for="option in inspectionTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="巡检目标" prop="objectType">
          <el-select
            v-model="queryParams.objectType"
            clearable
            filterable
            placeholder="所有巡检目标"
            class="!w-190px"
          >
            <el-option
              v-for="option in objectTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间周期" prop="cycleMonth">
          <el-date-picker
            v-model="queryParams.cycleMonth"
            type="month"
            value-format="YYYY-MM"
            clearable
            placeholder="时间周期（如2025-03）"
            class="!w-220px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            筛选
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
          <el-button
            v-hasPermi="['iot:inspection-plan:create']"
            type="primary"
            class="create-btn"
            @click="openForm('create')"
          >
            <Icon icon="ep:plus" class="mr-5px" />
            新建巡检计划
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="计划名称" prop="planName" min-width="200" show-overflow-tooltip />
        <el-table-column label="巡检类型" prop="inspectionType" min-width="120">
          <template #default="{ row }">
            <el-tag effect="light" type="primary">
              {{ getInspectionTypeLabel(row.inspectionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="巡检目标" min-width="230">
          <template #default="{ row }">
            <div class="target-tags">
              <el-tag
                v-for="(targetName, index) in resolveTargetNames(row)"
                :key="`${row.id}_${index}`"
                type="info"
                effect="light"
                class="target-tag"
              >
                {{ targetName }}
              </el-tag>
              <span v-if="resolveTargetNames(row).length === 0">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="巡检标准" prop="standardName" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.standardName || '-' }}</template>
        </el-table-column>
        <el-table-column label="计划开始" prop="planStartDate" min-width="170">
          <template #default="{ row }">{{ formatPlanDateTime(row.planStartDate) }}</template>
        </el-table-column>
        <el-table-column label="计划结束" prop="planEndDate" min-width="170">
          <template #default="{ row }">{{ formatPlanDateTime(row.planEndDate) }}</template>
        </el-table-column>
        <el-table-column label="计划周期" prop="cycleUnit" min-width="90">
          <template #default="{ row }">{{ getCycleUnitLabel(row.cycleUnit) }}</template>
        </el-table-column>
        <el-table-column label="状态" prop="planStatus" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.planStatus)" effect="plain">
              {{ getStatusLabel(row.planStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="执行人" prop="executorName" min-width="100" />
        <el-table-column label="执行班组" prop="executeDeptName" min-width="130">
          <template #default="{ row }">{{ row.executeDeptName || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link @click="openForm('detail', row.id)">详情</el-button>
            <el-button
              v-hasPermi="['iot:inspection-plan:update']"
              type="primary"
              link
              :disabled="!canEditPlan(row)"
              @click="openForm('update', row.id, row)"
            >
              编辑
            </el-button>
            <el-button
              v-hasPermi="['iot:inspection-plan:delete']"
              type="danger"
              link
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </ContentWrap>

    <Dialog v-model="formVisible" :title="formTitle" width="860px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="108px" v-loading="formLoading">
        <section class="form-section">
          <div class="section-title">
            <Icon icon="ep:document-checked" />
            <span>巡检计划信息</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="计划名称" prop="planName">
                <el-input
                  v-model="formData.planName"
                  maxlength="100"
                  show-word-limit
                  placeholder="请输入计划名称"
                  :disabled="formReadonly"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="巡检类型" prop="inspectionType">
                <el-select
                  v-model="formData.inspectionType"
                  filterable
                  clearable
                  placeholder="请选择巡检类型"
                  :disabled="formReadonly"
                  @change="handleInspectionTypeChange"
                >
                  <el-option
                    v-for="option in inspectionTypeOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="巡检标准" prop="standardId" :required="formMode === 'create'">
                <el-select
                  v-model="formData.standardId"
                  clearable
                  filterable
                  remote
                  reserve-keyword
                  placeholder="请选择巡检标准"
                  :disabled="formReadonly"
                  :loading="standardLoading"
                  :remote-method="handleStandardRemoteSearch"
                >
                  <el-option
                    v-for="option in standardOptions"
                    :key="option.id"
                    :label="option.name"
                    :value="option.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="巡检线路" prop="lineId" :required="formMode === 'create'">
                <div class="line-select-wrap">
                  <el-select
                    v-model="formData.lineId"
                    clearable
                    filterable
                    remote
                    reserve-keyword
                    placeholder="请选择巡检线路"
                    :disabled="formReadonly"
                    :loading="lineLoading"
                    :remote-method="handleLineRemoteSearch"
                  >
                    <el-option
                      v-for="option in lineOptions"
                      :key="option.id"
                      :label="option.name"
                      :value="option.id"
                    />
                  </el-select>
                  <el-button
                    class="line-preview-btn"
                    plain
                    :disabled="!formData.lineId"
                    :loading="linePreviewLoading"
                    @click="openLinePreview"
                  >
                    线路预览
                  </el-button>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="巡检目标" prop="targetIds" class="target-form-item">
                <div class="target-picker-panel">
                  <div class="target-picker-head">
                    <div class="target-type-switch">
                      <span class="target-switch-label">巡检目标：</span>
                      <el-radio-group
                        v-model="formData.objectType"
                        :disabled="formReadonly"
                        @change="handleObjectTypeChange"
                      >
                        <el-radio
                          v-for="option in objectTypeOptions"
                          :key="option.value"
                          :value="option.value"
                        >
                          {{ option.label }}
                        </el-radio>
                      </el-radio-group>
                    </div>
                    <el-button
                      :disabled="formReadonly"
                      :loading="targetLoading"
                      class="target-select-btn"
                      @click="openTargetPicker"
                    >
                      <Icon icon="ep:plus" class="mr-4px" />
                      {{ targetSelectButtonText }}
                    </el-button>
                  </div>
                  <div class="target-picker-body">
                    <p class="target-count-text">{{ selectedTargetCountText }}</p>
                    <div v-if="formData.targetIds.length > 0" class="target-chip-list">
                      <el-tag
                        v-for="targetId in formData.targetIds"
                        :key="targetId"
                        :closable="!formReadonly"
                        :disable-transitions="true"
                        class="target-chip"
                        @close="removeTarget(targetId)"
                      >
                        {{ getTargetName(targetId) }}
                      </el-tag>
                    </div>
                    <div v-else class="target-empty-text">尚未选择{{ targetTypeLabel }}</div>
                  </div>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划开始" prop="planStartDate">
                <el-date-picker
                  v-model="formData.planStartDate"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择开始时间"
                  :disabled="formReadonly"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划结束" prop="planEndDate">
                <el-date-picker
                  v-model="formData.planEndDate"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择结束时间"
                  :disabled="formReadonly"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划周期" prop="cycleUnit">
                <el-select
                  v-model="formData.cycleUnit"
                  clearable
                  placeholder="请选择计划周期"
                  :disabled="formReadonly"
                >
                  <el-option
                    v-for="option in cycleUnitOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="执行人" prop="executorUserId">
                <el-select
                  v-model="formData.executorUserId"
                  filterable
                  clearable
                  placeholder="请选择执行人"
                  :disabled="formReadonly"
                  @change="handleExecutorChange"
                >
                  <el-option
                    v-for="option in userOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="执行班组" prop="executeDeptId">
                <el-select
                  v-model="formData.executeDeptId"
                  filterable
                  clearable
                  placeholder="请选择执行班组"
                  :disabled="formReadonly"
                  @change="handleDeptChange"
                >
                  <el-option
                    v-for="option in executeTeamOptions"
                    :key="option.id"
                    :label="option.name"
                    :value="option.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="班组长">
                <el-input :model-value="executeTeamLeaderName" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="班组成员">
                <div class="execute-team-member-list">
                  <template v-if="executeTeamMembers.length">
                    <el-tag
                      v-for="member in executeTeamMembers"
                      :key="`${member.userId}`"
                      size="small"
                      class="execute-team-member-tag"
                      :type="member.leader ? 'warning' : 'info'"
                    >
                      {{ member.userName || member.userId }}{{ member.leader ? '（班组长）' : '' }}
                    </el-tag>
                  </template>
                  <span v-else class="text-gray-500">
                    {{ executeTeamDetailLoading ? '班组成员加载中...' : '暂无班组成员' }}
                  </span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注说明" prop="remark">
                <el-input
                  v-model="formData.remark"
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入备注说明"
                  :disabled="formReadonly"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </section>
      </el-form>

      <template #footer>
        <el-button @click="formVisible = false">{{ formReadonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!formReadonly" type="primary" :loading="submitLoading" @click="submitForm">
          保存
        </el-button>
      </template>
    </Dialog>

    <Dialog v-model="targetPickerVisible" :title="targetPickerTitle" width="720px">
      <div class="target-picker-dialog">
        <div class="target-picker-toolbar">
          <el-select
            v-if="Number(formData.objectType) === 1"
            v-model="targetStationId"
            clearable
            filterable
            placeholder="按所属站点过滤"
            class="!w-220px"
            @change="handleTargetStationChange"
          >
            <el-option
              v-for="option in stationOptions"
              :key="option.value"
              :label="option.label"
              :value="String(option.value)"
            />
          </el-select>
          <el-input
            v-model="targetKeyword"
            clearable
            :placeholder="`请输入${targetTypeLabel}名称关键词`"
            @keyup.enter="searchTargetOptions"
          >
            <template #append>
              <el-button :loading="targetLoading" @click="searchTargetOptions">查询</el-button>
            </template>
          </el-input>
        </div>
        <el-table
          v-if="Number(formData.objectType) === 1"
          ref="targetPickerTableRef"
          v-loading="targetLoading"
          :data="targetOptions"
          row-key="id"
          max-height="360"
          @selection-change="handleTargetPickerSelectionChange"
        >
          <el-table-column type="selection" :reserve-selection="true" width="55" />
          <el-table-column label="设备名称" prop="name" min-width="260" />
          <el-table-column label="所属站点" prop="stationId" min-width="180">
            <template #default="{ row }">{{ getStationLabel(row.stationId) }}</template>
          </el-table-column>
        </el-table>
        <div v-else v-loading="locationTreeLoading" class="location-tree-wrap">
          <el-tree
            ref="locationTreeRef"
            :data="locationTreeData"
            node-key="id"
            show-checkbox
            check-strictly
            default-expand-all
            :props="{ label: 'name', children: 'children' }"
            :filter-node-method="filterLocationTreeNode"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="targetPickerVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmTargetPicker">确定</el-button>
      </template>
    </Dialog>

    <Dialog v-model="linePreviewVisible" :title="linePreviewTitle" width="980px">
      <div class="line-preview-dialog">
        <div class="line-preview-meta">
          <div class="line-preview-meta__item">
            <span>线路名称</span>
            <strong>{{ linePreviewData?.lineName || currentLineName || '-' }}</strong>
          </div>
          <div class="line-preview-meta__item">
            <span>巡检类型</span>
            <strong>{{ getInspectionTypeLabel(linePreviewData?.inspectionType || formData.inspectionType) }}</strong>
          </div>
          <div class="line-preview-meta__item">
            <span>点位数量</span>
            <strong>{{ linePreviewValidPointCount }}/{{ linePreviewPointCount }}</strong>
          </div>
        </div>
        <div class="line-preview-map-wrap">
          <div ref="linePreviewMapRef" class="line-preview-map"></div>
          <div class="line-preview-toolbar">
            <div class="line-preview-toolbar__title">地图控制</div>
            <div class="line-preview-toolbar__actions">
              <el-button size="small" @click="handleLinePreviewZoomIn">放大</el-button>
              <el-button size="small" @click="handleLinePreviewZoomOut">缩小</el-button>
              <el-button size="small" plain @click="renderLinePreviewMap(true)">重置视角</el-button>
            </div>
          </div>
          <div v-if="linePreviewMapLoading || linePreviewLoading" class="line-preview-map-mask">
            地图加载中...
          </div>
          <div
            v-else-if="linePreviewPointCount > 0 && linePreviewValidPointCount === 0"
            class="line-preview-map-mask"
          >
            线路点位缺少坐标，暂无法预览地图
          </div>
          <div v-else-if="linePreviewPointCount === 0" class="line-preview-map-mask">
            该线路暂无点位数据
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="linePreviewVisible = false">关闭</el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { getTiandituKey } from '@/components/Gis/tiandituKey'
import {
  InspectionPlanApi,
  type InspectionPlanPageReqVO,
  type InspectionPlanLineOptionVO,
  type InspectionPlanStandardOptionVO,
  type InspectionPlanTargetOptionVO,
  type InspectionPlanTargetVO,
  type InspectionPlanVO
} from '@/api/iot/inspection/plan'
import { InspectionLineApi, type InspectionLinePointVO, type InspectionLineVO } from '@/api/iot/inspection/line'
import { DeviceLocationApi, type DeviceLocationNodeRespVO } from '@/api/iot/device/location'
import * as UserApi from '@/api/system/user'
import { ShiftTeamApi, type ShiftTeamMemberVO, type ShiftTeamVO } from '@/api/iot/shift/team'
import type { FormInstance, FormRules } from 'element-plus'

interface SelectOption {
  label: string
  value: string | number
}

interface ExecuteTeamOption {
  id: string
  name: string
  leaderUserName?: string
  memberCount?: number
}

interface ExecuteTeamDetailState {
  leaderUserName: string
  members: ShiftTeamMemberVO[]
}

type TargetIdValue = string | number

type FormMode = 'create' | 'update' | 'detail'

interface InspectionPlanEditVO {
  id?: number
  planName: string
  inspectionType: string
  objectType?: number
  standardId?: number
  lineId?: number
  planStartDate: string
  planEndDate: string
  cycleUnit: string
  cycleValue: number
  executorUserId?: string | number
  executorName?: string
  executeDeptId?: string | number
  executeDeptName?: string
  stationId?: string
  targetIds: string[]
  remark: string
}

const OBJECT_TYPE_OPTIONS: SelectOption[] = [
  { label: '设备', value: 1 },
  { label: '区域', value: 2 }
]

const CYCLE_UNIT_OPTIONS: SelectOption[] = [
  { label: '日', value: 'DAY' },
  { label: '周', value: 'WEEK' },
  { label: '月', value: 'MONTH' },
  { label: '季度', value: 'QUARTER' },
  { label: '年', value: 'YEAR' }
]

const STATUS_META: Record<number, { label: string; tagType: 'info' | 'warning' | 'success' | 'danger' }> = {
  0: { label: '未开始', tagType: 'info' },
  1: { label: '未完成', tagType: 'warning' },
  2: { label: '已完成', tagType: 'success' },
  3: { label: '已逾期', tagType: 'danger' }
}

const DEFAULT_MAP_CENTER: [number, number] = [32.272, 119.184]
const DEFAULT_MAP_ZOOM = 13
const MAP_FIT_MAX_ZOOM = 18
let leafletCorePromise: Promise<void> | null = null

const createEmptyFormData = (): InspectionPlanEditVO => ({
  id: undefined,
  planName: '',
  inspectionType: '',
  objectType: 1,
  standardId: undefined,
  lineId: undefined,
  planStartDate: '',
  planEndDate: '',
  cycleUnit: 'MONTH',
  cycleValue: 1,
  executorUserId: undefined,
  executorName: '',
  executeDeptId: undefined,
  executeDeptName: '',
  stationId: '',
  targetIds: [],
  remark: ''
})

const message = useMessage()

const inspectionTypeOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_INSPECTION_TYPE)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})
const stationOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})
const objectTypeOptions = OBJECT_TYPE_OPTIONS
const cycleUnitOptions = CYCLE_UNIT_OPTIONS

const getInspectionTypeLabel = (value?: string) => {
  if (!value) return '-'
  return inspectionTypeOptions.value.find((item) => item.value === value)?.label || value
}

const getCycleUnitLabel = (value?: string) => {
  if (!value) return '-'
  return cycleUnitOptions.find((item) => item.value === value)?.label || value
}

const getStationLabel = (value?: string) => {
  if (!value) return '-'
  return stationOptions.value.find((item) => String(item.value) === String(value))?.label || value
}

const getStatusLabel = (status?: number) => {
  if (status === undefined || status === null) return '-'
  return STATUS_META[status]?.label || '未知'
}

const getStatusTagType = (status?: number) => {
  if (status === undefined || status === null) return 'info'
  return STATUS_META[status]?.tagType || 'info'
}

const formatPlanDateTime = (dateValue?: string | number | number[] | null) => {
  if (!dateValue) {
    return ''
  }
  if (Array.isArray(dateValue)) {
    if (dateValue.length < 3) {
      return ''
    }
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateValue
    return `${String(year).padStart(4, '0')}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`
  }
  const normalized = String(dateValue).trim()
  if (!normalized) {
    return ''
  }
  if (/^\d{10,17}$/.test(normalized)) {
    const parsed = Number(normalized)
    if (Number.isFinite(parsed)) {
      let timestamp = parsed
      if (normalized.length === 10) {
        timestamp = parsed * 1000
      } else if (normalized.length > 13) {
        timestamp = Math.floor(parsed / 10 ** (normalized.length - 13))
      }
      const date = new Date(timestamp)
      if (!Number.isNaN(date.getTime())) {
        const year = String(date.getFullYear()).padStart(4, '0')
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hour = String(date.getHours()).padStart(2, '0')
        const minute = String(date.getMinutes()).padStart(2, '0')
        const second = String(date.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hour}:${minute}:${second}`
      }
    }
  }
  if (normalized.includes(',')) {
    const [year, month, day, hour = '0', minute = '0', second = '0'] = normalized
      .split(',')
      .map((item) => item.trim())
    if (year && month && day) {
      return `${year.padStart(4, '0')}-${month.padStart(2, '0')}-${day.padStart(2, '0')} ${hour.padStart(2, '0')}:${minute.padStart(2, '0')}:${second.padStart(2, '0')}`
    }
  }
  if (/^\d{4}-\d{1,2}-\d{1,2}$/.test(normalized)) {
    const [year, month, day] = normalized.split('-')
    return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')} 00:00:00`
  }
  if (/^\d{4}-\d{1,2}-\d{1,2}[ T]\d{1,2}:\d{1,2}(:\d{1,2})?/.test(normalized)) {
    const normalizedText = normalized.replace('T', ' ')
    const [datePart, timePart] = normalizedText.split(' ')
    const [year, month, day] = datePart.split('-')
    const [hour = '0', minute = '0', second = '0'] = (timePart || '').split(':')
    return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')} ${hour.padStart(2, '0')}:${minute.padStart(2, '0')}:${second.padStart(2, '0')}`
  }
  return normalized
}

// 统一按字符串处理巡检目标ID，避免 Long 转 Number 后精度丢失导致名称匹配失败。
const normalizeTargetId = (targetId?: TargetIdValue | null): string | undefined => {
  if (targetId === undefined || targetId === null) {
    return undefined
  }
  const normalizedId = String(targetId).trim()
  return normalizedId || undefined
}

const queryFormRef = ref<FormInstance>()
const loading = ref(false)
const total = ref(0)
const list = ref<InspectionPlanVO[]>([])
const queryParams = reactive<InspectionPlanPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  inspectionType: undefined,
  objectType: undefined,
  cycleMonth: undefined
})

const getList = async () => {
  loading.value = true
  try {
    const data = await InspectionPlanApi.getInspectionPlanPage(queryParams)
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
  queryParams.cycleMonth = undefined
  queryParams.pageNo = 1
  await getList()
}

const resolveTargetNames = (row: InspectionPlanVO) => {
  if (row.targetNames && row.targetNames.length > 0) {
    return row.targetNames
  }
  if (row.targets && row.targets.length > 0) {
    return row.targets.map((item) => item.targetName || '').filter((item) => !!item)
  }
  return []
}

const handleDelete = async (id?: number) => {
  if (!id) return
  try {
    await message.delConfirm('确认删除该巡检计划吗？')
    await InspectionPlanApi.deleteInspectionPlan(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

const canEditPlan = (plan?: InspectionPlanVO) => {
  if (!plan) return true
  const status = Number(plan.planStatus)
  return status === 0 || status === 1
}

const formVisible = ref(false)
const formLoading = ref(false)
const submitLoading = ref(false)
const formMode = ref<FormMode>('create')
const formRef = ref<FormInstance>()

const formData = reactive<InspectionPlanEditVO>(createEmptyFormData())
const formReadonly = computed(() => formMode.value === 'detail')
const formTitle = computed(() => {
  if (formMode.value === 'create') return '新建巡检计划'
  if (formMode.value === 'detail') return '巡检计划详情'
  return '编辑巡检计划'
})
const targetTypeLabel = computed(() => (Number(formData.objectType) === 2 ? '区域' : '设备'))
const targetSelectButtonText = computed(() => `选择${targetTypeLabel.value}`)
const targetPickerTitle = computed(() => `${targetSelectButtonText.value}`)
const selectedTargetCountText = computed(
  () => `已选择 ${formData.targetIds.length} 个${targetTypeLabel.value}`
)

const targetLoading = ref(false)
const locationTreeLoading = ref(false)
const standardLoading = ref(false)
const lineLoading = ref(false)
const targetOptions = ref<InspectionPlanTargetOptionVO[]>([])
const standardOptions = ref<InspectionPlanStandardOptionVO[]>([])
const lineOptions = ref<InspectionPlanLineOptionVO[]>([])
const locationTreeData = ref<DeviceLocationNodeRespVO[]>([])
const targetPickerVisible = ref(false)
const targetPickerSelection = ref<string[]>([])
const targetPickerTableRef = ref<any>()
const locationTreeRef = ref<any>()
const targetStationId = ref<string>('')
const targetLabelCache = reactive<Record<string, string>>({})
const targetStationCache = reactive<Record<string, string>>({})
const standardLabelCache = reactive<Record<number, string>>({})
const lineLabelCache = reactive<Record<number, string>>({})
const targetKeyword = ref('')
const standardKeyword = ref('')
const lineKeyword = ref('')
const linePreviewVisible = ref(false)
const linePreviewLoading = ref(false)
const linePreviewMapLoading = ref(false)
const linePreviewData = ref<InspectionLineVO>()
const linePreviewMapRef = ref<HTMLDivElement>()
let linePreviewMapInstance: any = null
let linePreviewPointLayer: any = null
let linePreviewOrderLayer: any = null
let linePreviewRouteLayer: any = null
let linePreviewLeaflet: any = null

const userOptions = ref<SelectOption[]>([])
const executeTeamOptions = ref<ExecuteTeamOption[]>([])
const executeTeamDetailLoading = ref(false)
const executeTeamDetail = ref<ExecuteTeamDetailState>({
  leaderUserName: '',
  members: []
})
const executeTeamDetailCache = new Map<string, ExecuteTeamDetailState>()

const executeTeamLeaderName = computed(() => executeTeamDetail.value.leaderUserName || '-')
const executeTeamMembers = computed(() => executeTeamDetail.value.members || [])

const currentLineName = computed(() => {
  if (!formData.lineId) return ''
  return lineLabelCache[formData.lineId] || `线路-${formData.lineId}`
})
const linePreviewTitle = computed(() =>
  currentLineName.value ? `巡检线路预览 - ${currentLineName.value}` : '巡检线路预览'
)

const normalizeLinePoints = (points: InspectionLinePointVO[] = []) => {
  return [...points].sort((a, b) => Number(a.pointSort || 0) - Number(b.pointSort || 0))
}

const linePreviewPointList = computed(() => normalizeLinePoints(linePreviewData.value?.points || []))
const linePreviewPointCount = computed(() => linePreviewPointList.value.length)

const validateCreateRequired = (fieldLabel: string) => {
  return (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
    const isEmpty = value === undefined || value === null || String(value).trim() === ''
    if (formMode.value === 'create' && isEmpty) {
      callback(new Error(`${fieldLabel}不能为空`))
      return
    }
    callback()
  }
}

const formRules: FormRules = {
  planName: [{ required: true, message: '计划名称不能为空', trigger: 'blur' }],
  inspectionType: [{ required: true, message: '巡检类型不能为空', trigger: 'change' }],
  objectType: [{ required: true, message: '巡检目标类型不能为空', trigger: 'change' }],
  standardId: [{ validator: validateCreateRequired('巡检标准'), trigger: 'change' }],
  lineId: [{ validator: validateCreateRequired('巡检线路'), trigger: 'change' }],
  targetIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一个巡检目标', trigger: 'change' }],
  planStartDate: [{ required: true, message: '计划开始时间不能为空', trigger: 'change' }],
  planEndDate: [{ required: true, message: '计划结束时间不能为空', trigger: 'change' }],
  cycleUnit: [{ required: true, message: '计划周期不能为空', trigger: 'change' }],
  executorUserId: [{ required: true, message: '执行人不能为空', trigger: 'change' }]
}

const resetForm = () => {
  Object.assign(formData, createEmptyFormData())
  linePreviewVisible.value = false
  linePreviewData.value = undefined
  destroyLinePreviewMap()
  resetExecuteTeamDetail()
  targetOptions.value = []
  standardOptions.value = []
  lineOptions.value = []
  locationTreeData.value = []
  targetPickerVisible.value = false
  targetPickerSelection.value = []
  targetStationId.value = ''
  targetKeyword.value = ''
  standardKeyword.value = ''
  lineKeyword.value = ''
  Object.keys(targetLabelCache).forEach((key) => delete targetLabelCache[key])
  Object.keys(targetStationCache).forEach((key) => delete targetStationCache[key])
  Object.keys(standardLabelCache).forEach((key) => delete standardLabelCache[Number(key)])
  Object.keys(lineLabelCache).forEach((key) => delete lineLabelCache[Number(key)])
  formRef.value?.clearValidate()
}

const resetExecuteTeamDetail = () => {
  executeTeamDetail.value = {
    leaderUserName: '',
    members: []
  }
}

const normalizeShiftTeamId = (teamId?: string | number | null) => {
  if (teamId === undefined || teamId === null) return undefined
  const normalizedTeamId = String(teamId).trim()
  if (!normalizedTeamId || normalizedTeamId === 'undefined' || normalizedTeamId === 'null') return undefined
  return normalizedTeamId
}

const loadExecuteTeamOptions = async () => {
  const pageSize = 100
  let pageNo = 1
  let total = 0
  const teamList: ShiftTeamVO[] = []

  do {
    const pageData = await ShiftTeamApi.getPage({ pageNo, pageSize })
    const currentList = pageData?.list || []
    teamList.push(...currentList)
    total = Number(pageData?.total || 0)
    if (!currentList.length) {
      break
    }
    pageNo += 1
  } while (teamList.length < total)

  const teamMap = new Map<string, ExecuteTeamOption>()
  teamList.forEach((team) => {
    const teamId = normalizeShiftTeamId(team.id as any)
    if (!teamId || teamMap.has(teamId)) {
      return
    }
    teamMap.set(teamId, {
      id: teamId,
      name: team.teamName || '',
      leaderUserName: team.leaderUserName || '',
      memberCount: team.memberCount || 0
    })
  })
  executeTeamOptions.value = Array.from(teamMap.values())
}

const syncExecuteTeamDetail = async (teamId?: string | number | null) => {
  const normalizedTeamId = normalizeShiftTeamId(teamId)
  if (!normalizedTeamId) {
    resetExecuteTeamDetail()
    return
  }
  const cached = executeTeamDetailCache.get(normalizedTeamId)
  if (cached) {
    executeTeamDetail.value = cached
    return
  }
  executeTeamDetailLoading.value = true
  try {
    const team = await ShiftTeamApi.get(normalizedTeamId)
    const detail: ExecuteTeamDetailState = {
      leaderUserName: team?.leaderUserName || '',
      members: team?.members || []
    }
    executeTeamDetailCache.set(normalizedTeamId, detail)
    executeTeamDetail.value = detail
    if (!formData.executeDeptName) {
      formData.executeDeptName = team?.teamName || ''
    }
  } catch {
    resetExecuteTeamDetail()
  } finally {
    executeTeamDetailLoading.value = false
  }
}

const loadUserAndTeamOptions = async () => {
  const users = await UserApi.getSimpleUserList({ excludeRoleName: '游客' })
  userOptions.value = (users || []).map((item) => ({
    label: item.nickname || item.username,
    value: String(item.id)
  }))
  await loadExecuteTeamOptions()
}

const ensureExecutorOption = (executorUserId?: string | number, executorName?: string) => {
  if (!executorUserId) {
    return
  }
  const exists = userOptions.value.some((item) => String(item.value) === String(executorUserId))
  if (exists) {
    return
  }
  userOptions.value.push({
    value: executorUserId,
    label: executorName?.trim() || String(executorUserId)
  })
}

const mergeTargetCache = (options: InspectionPlanTargetOptionVO[]) => {
  options.forEach((item) => {
    const normalizedTargetId = normalizeTargetId(item.id)
    if (!normalizedTargetId) {
      return
    }
    targetLabelCache[normalizedTargetId] = item.name || `对象-${normalizedTargetId}`
    if (item.stationId) {
      targetStationCache[normalizedTargetId] = item.stationId
    }
  })
}

const mergeStandardCache = (options: InspectionPlanStandardOptionVO[]) => {
  options.forEach((item) => {
    standardLabelCache[item.id] = item.name || `标准-${item.id}`
  })
}

const mergeLineCache = (options: InspectionPlanLineOptionVO[]) => {
  options.forEach((item) => {
    lineLabelCache[item.id] = item.name || `线路-${item.id}`
  })
}

const buildDeviceTargetDisplayName = (targetName: string, stationId?: string) => {
  const normalizedStationId = stationId || ''
  if (!normalizedStationId) {
    return targetName
  }
  const stationName = getStationLabel(normalizedStationId)
  if (!stationName || stationName === '-') {
    return targetName
  }
  return `${stationName}-${targetName}`
}

const getTargetName = (targetId: TargetIdValue) => {
  const normalizedTargetId = normalizeTargetId(targetId)
  if (!normalizedTargetId) {
    return '-'
  }
  const targetName = targetLabelCache[normalizedTargetId] || `对象-${normalizedTargetId}`
  if (Number(formData.objectType) !== 1) {
    return targetName
  }
  return buildDeviceTargetDisplayName(targetName, targetStationCache[normalizedTargetId])
}

const flattenLocationTree = (nodes: DeviceLocationNodeRespVO[]) => {
  const result: Array<{ id: string; name: string }> = []
  const loop = (list: DeviceLocationNodeRespVO[]) => {
    list.forEach((item) => {
      const id = normalizeTargetId(item.id as TargetIdValue)
      if (id) {
        result.push({ id, name: item.name || `区域-${id}` })
      }
      if (item.children && item.children.length > 0) {
        loop(item.children)
      }
    })
  }
  loop(nodes || [])
  return result
}

const loadTargetOptions = async (keyword?: string) => {
  if (Number(formData.objectType) !== 1) {
    targetOptions.value = []
    return
  }
  targetLoading.value = true
  try {
    const options = await InspectionPlanApi.getTargetOptions({
      objectType: formData.objectType,
      stationId: targetStationId.value || undefined,
      keyword: keyword?.trim() || undefined,
      limit: 1000
    })
    mergeTargetCache(options || [])
    const merged = [...(options || [])]
    formData.targetIds.forEach((targetId) => {
      const exists = merged.some((item) => normalizeTargetId(item.id) === targetId)
      if (exists) return
      merged.push({
        id: targetId,
        name: targetLabelCache[targetId] || `对象-${targetId}`,
        objectType: formData.objectType || 1
      })
    })
    targetOptions.value = merged
  } finally {
    targetLoading.value = false
  }
}

const loadLocationTree = async () => {
  locationTreeLoading.value = true
  try {
    locationTreeData.value = await DeviceLocationApi.getDeviceLocationTree()
    flattenLocationTree(locationTreeData.value).forEach((item) => {
      targetLabelCache[item.id] = item.name
    })
  } finally {
    locationTreeLoading.value = false
  }
}

const loadStandardOptions = async (keyword?: string) => {
  standardLoading.value = true
  try {
    const options = await InspectionPlanApi.getStandardOptions({
      inspectionType: formData.inspectionType || undefined,
      keyword: keyword?.trim() || undefined,
      limit: 200
    })
    mergeStandardCache(options || [])
    const merged = [...(options || [])]
    if (formData.standardId) {
      const exists = merged.some((item) => item.id === formData.standardId)
      if (!exists) {
        merged.push({
          id: formData.standardId,
          name: standardLabelCache[formData.standardId] || `标准-${formData.standardId}`,
          inspectionType: formData.inspectionType || '',
          suggestCycleUnit: undefined
        })
      }
    }
    standardOptions.value = merged
  } finally {
    standardLoading.value = false
  }
}

const loadLineOptions = async (keyword?: string) => {
  lineLoading.value = true
  try {
    const options = await InspectionPlanApi.getLineOptions({
      stationId: formData.stationId || undefined,
      inspectionType: formData.inspectionType || undefined,
      keyword: keyword?.trim() || undefined,
      limit: 200
    })
    mergeLineCache(options || [])
    const merged = [...(options || [])]
    if (formData.lineId) {
      const exists = merged.some((item) => item.id === formData.lineId)
      if (!exists) {
        merged.push({
          id: formData.lineId,
          name: lineLabelCache[formData.lineId] || `线路-${formData.lineId}`
        })
      }
    }
    lineOptions.value = merged
  } finally {
    lineLoading.value = false
  }
}

const loadStyleOnce = (id: string, href: string) => {
  const existing = document.getElementById(id)
  if (existing) return Promise.resolve()
  return new Promise<void>((resolve, reject) => {
    const link = document.createElement('link')
    link.id = id
    link.rel = 'stylesheet'
    link.href = href
    link.onload = () => resolve()
    link.onerror = () => reject(new Error(`样式加载失败：${href}`))
    document.head.appendChild(link)
  })
}

const loadScriptOnce = (id: string, src: string) => {
  const existing = document.getElementById(id) as HTMLScriptElement | null
  if (existing) return Promise.resolve()
  return new Promise<void>((resolve, reject) => {
    const script = document.createElement('script')
    script.id = id
    script.src = src
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error(`脚本加载失败：${src}`))
    document.body.appendChild(script)
  })
}

const loadLeafletAssets = async () => {
  await loadStyleOnce('leaflet-style', 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css')
  if ((window as any).L) return
  if (!leafletCorePromise) {
    leafletCorePromise = (async () => {
      await loadScriptOnce('leaflet-script', 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js')
      if (!(window as any).L) {
        throw new Error('Leaflet 脚本加载完成但 window.L 不存在')
      }
    })()
  }
  await leafletCorePromise
}

const destroyLinePreviewMap = () => {
  if (linePreviewMapInstance) {
    linePreviewMapInstance.off()
    linePreviewMapInstance.remove()
  }
  linePreviewMapInstance = null
  linePreviewPointLayer = null
  linePreviewOrderLayer = null
  linePreviewRouteLayer = null
  linePreviewLeaflet = null
  linePreviewMapLoading.value = false
}

const toLatLng = (longitude?: number, latitude?: number): [number, number] | null => {
  const lng = Number(longitude)
  const lat = Number(latitude)
  if (!Number.isFinite(lng) || !Number.isFinite(lat)) return null
  return [lat, lng]
}

const linePreviewValidPointCount = computed(
  () =>
    linePreviewPointList.value.filter((point) => !!toLatLng(point.longitude, point.latitude)).length
)

const createOrderIcon = (order: number) => {
  return linePreviewLeaflet.divIcon({
    className: 'inspection-plan-line-order-icon',
    html: `<div style=\"width:28px;height:28px;border-radius:50%;display:flex;align-items:center;justify-content:center;background:#2563eb;color:#fff;font-size:13px;font-weight:700;border:2px solid #fff;box-shadow:0 2px 8px rgba(15,23,42,.22);\">${order}</div>`,
    iconSize: [28, 28],
    iconAnchor: [14, 14]
  })
}

const renderLinePreviewMap = (fitBounds = false) => {
  if (
    !linePreviewLeaflet ||
    !linePreviewMapInstance ||
    !linePreviewPointLayer ||
    !linePreviewOrderLayer ||
    !linePreviewRouteLayer
  ) {
    return
  }
  linePreviewPointLayer.clearLayers()
  linePreviewOrderLayer.clearLayers()
  linePreviewRouteLayer.clearLayers()

  const routeLatLngs: [number, number][] = []
  linePreviewPointList.value.forEach((point, index) => {
    const latLng = toLatLng(point.longitude, point.latitude)
    if (!latLng) return
    routeLatLngs.push(latLng)
    const marker = linePreviewLeaflet.circleMarker(latLng, {
      radius: 7,
      color: '#2563eb',
      weight: 2,
      fillColor: '#60a5fa',
      fillOpacity: 0.92
    }).addTo(linePreviewPointLayer)
    marker.bindTooltip(point.pointName || `点位${index + 1}`, {
      direction: 'top',
      offset: [0, -8]
    })
    const orderMarker = linePreviewLeaflet.marker(latLng, {
      icon: createOrderIcon(index + 1)
    }).addTo(linePreviewOrderLayer)
    orderMarker.bindTooltip(`${index + 1}# ${point.pointName || `点位${index + 1}`}`, {
      direction: 'bottom',
      offset: [0, 14],
      className: 'inspection-plan-line-route-tooltip'
    })
  })

  if (routeLatLngs.length >= 2) {
    linePreviewLeaflet
      .polyline(routeLatLngs, {
        color: '#14b8a6',
        weight: 5,
        opacity: 0.88,
        lineJoin: 'round'
      })
      .addTo(linePreviewRouteLayer)
  }

  if (fitBounds) {
    if (routeLatLngs.length === 1) {
      linePreviewMapInstance.setView(routeLatLngs[0], MAP_FIT_MAX_ZOOM)
      return
    }
    if (routeLatLngs.length > 1) {
      linePreviewMapInstance.fitBounds(linePreviewLeaflet.latLngBounds(routeLatLngs), {
        padding: [48, 48],
        maxZoom: MAP_FIT_MAX_ZOOM
      })
    }
  }
}

const initLinePreviewMap = async () => {
  if (!linePreviewMapRef.value || linePreviewMapInstance || linePreviewMapLoading.value) return
  linePreviewMapLoading.value = true
  try {
    await loadLeafletAssets()
    linePreviewLeaflet = (window as any).L
    if (!linePreviewLeaflet) {
      throw new Error('Leaflet 资源加载失败')
    }
    const tiandituKey = await getTiandituKey()
    linePreviewMapInstance = linePreviewLeaflet.map(linePreviewMapRef.value, {
      center: DEFAULT_MAP_CENTER,
      zoom: DEFAULT_MAP_ZOOM,
      zoomControl: false
    })
    const vecLayer = linePreviewLeaflet.tileLayer(
      `https://t{s}.tianditu.gov.cn/vec_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=vec&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=${tiandituKey}`,
      { subdomains: ['0', '1', '2', '3', '4', '5', '6', '7'] }
    )
    const cvaLayer = linePreviewLeaflet.tileLayer(
      `https://t{s}.tianditu.gov.cn/cva_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cva&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=${tiandituKey}`,
      { subdomains: ['0', '1', '2', '3', '4', '5', '6', '7'] }
    )
    linePreviewLeaflet.layerGroup([vecLayer, cvaLayer]).addTo(linePreviewMapInstance)
    linePreviewPointLayer = linePreviewLeaflet.layerGroup().addTo(linePreviewMapInstance)
    linePreviewOrderLayer = linePreviewLeaflet.layerGroup().addTo(linePreviewMapInstance)
    linePreviewRouteLayer = linePreviewLeaflet.layerGroup().addTo(linePreviewMapInstance)
  } catch (error) {
    console.error(error)
    message.error('天地图加载失败，请稍后重试')
    destroyLinePreviewMap()
  } finally {
    linePreviewMapLoading.value = false
  }
}

const handleLinePreviewZoomIn = () => {
  linePreviewMapInstance?.zoomIn?.()
}

const handleLinePreviewZoomOut = () => {
  linePreviewMapInstance?.zoomOut?.()
}

const openLinePreview = async () => {
  if (!formData.lineId) {
    message.warning('请先选择巡检线路')
    return
  }
  const lineId = String(formData.lineId).trim()
  if (!lineId) {
    message.warning('请选择有效的巡检线路')
    return
  }
  linePreviewLoading.value = true
  try {
    const lineData = await InspectionLineApi.getInspectionLine(lineId)
    linePreviewData.value = {
      ...lineData,
      points: normalizeLinePoints(lineData?.points || [])
    }
    if (lineData?.lineName) {
      lineLabelCache[Number(formData.lineId)] = lineData.lineName
    }
    linePreviewVisible.value = true
    await nextTick()
    await initLinePreviewMap()
    await nextTick()
    linePreviewMapInstance?.invalidateSize?.()
    renderLinePreviewMap(true)
  } catch (error) {
    console.error(error)
    message.error('加载线路预览失败，请稍后重试')
  } finally {
    linePreviewLoading.value = false
  }
}

const syncTargetPickerSelection = async () => {
  await nextTick()
  if (Number(formData.objectType) !== 1 || !targetPickerTableRef.value) return
  targetPickerTableRef.value.clearSelection()
  const selectedSet = new Set(targetPickerSelection.value)
  targetOptions.value.forEach((option) => {
    const normalizedTargetId = normalizeTargetId(option.id)
    if (!normalizedTargetId || !selectedSet.has(normalizedTargetId)) return
    targetPickerTableRef.value.toggleRowSelection(option, true)
  })
}

const syncLocationTreeSelection = async () => {
  await nextTick()
  if (Number(formData.objectType) !== 2 || !locationTreeRef.value) return
  const keys = targetPickerSelection.value.reduce<Array<string | number>>((result, targetId) => {
    result.push(targetId)
    const asNumber = Number(targetId)
    if (Number.isSafeInteger(asNumber)) {
      result.push(asNumber)
    }
    return result
  }, [])
  locationTreeRef.value.setCheckedKeys(keys, false)
  if (targetKeyword.value?.trim()) {
    locationTreeRef.value.filter(targetKeyword.value.trim())
  } else {
    locationTreeRef.value.filter('')
  }
}

const filterLocationTreeNode = (value: string, data: DeviceLocationNodeRespVO) => {
  if (!value) return true
  return String(data?.name || '').includes(value)
}

const searchTargetOptions = async () => {
  if (Number(formData.objectType) === 1) {
    await loadTargetOptions(targetKeyword.value)
    await syncTargetPickerSelection()
    return
  }
  if (!locationTreeData.value.length) {
    await loadLocationTree()
  }
  await syncLocationTreeSelection()
}

const openTargetPicker = async () => {
  if (formReadonly.value) return
  if (!formData.objectType) {
    message.warning('请先选择巡检目标类型')
    return
  }
  targetPickerSelection.value = [...formData.targetIds]
  if (Number(formData.objectType) === 1 && formData.stationId && !targetStationId.value) {
    targetStationId.value = formData.stationId
  }
  targetPickerVisible.value = true
  await searchTargetOptions()
}

const handleTargetPickerSelectionChange = (rows: InspectionPlanTargetOptionVO[]) => {
  targetPickerSelection.value = rows
    .map((item) => normalizeTargetId(item.id))
    .filter((item): item is string => !!item)
}

const confirmTargetPicker = async () => {
  if (Number(formData.objectType) === 2 && locationTreeRef.value) {
    targetPickerSelection.value = (locationTreeRef.value.getCheckedKeys(false) || [])
      .map((item: unknown) => normalizeTargetId(item as TargetIdValue))
      .filter((item: string | undefined): item is string => !!item)
  }
  formData.targetIds = [...new Set(targetPickerSelection.value)]
  targetPickerVisible.value = false
  await nextTick()
  await formRef.value?.validateField('targetIds')
}

const removeTarget = (targetId: TargetIdValue) => {
  if (formReadonly.value) return
  const normalizedTargetId = normalizeTargetId(targetId)
  if (!normalizedTargetId) return
  formData.targetIds = formData.targetIds.filter((id) => id !== normalizedTargetId)
}

const handleTargetStationChange = async () => {
  if (formReadonly.value || Number(formData.objectType) !== 1) return
  formData.stationId = targetStationId.value || ''
  await loadLineOptions(lineKeyword.value)
  if (targetPickerVisible.value) {
    await searchTargetOptions()
  }
}

const handleStandardRemoteSearch = async (keyword: string) => {
  standardKeyword.value = keyword
  await loadStandardOptions(keyword)
}

const handleLineRemoteSearch = async (keyword: string) => {
  lineKeyword.value = keyword
  await loadLineOptions(keyword)
}

const handleInspectionTypeChange = async () => {
  if (formReadonly.value) return
  formData.standardId = undefined
  formData.lineId = undefined
  await Promise.all([loadStandardOptions(standardKeyword.value), loadLineOptions(lineKeyword.value)])
}

const handleObjectTypeChange = async () => {
  if (formReadonly.value) return
  formData.targetIds = []
  targetPickerSelection.value = []
  targetOptions.value = []
  locationTreeData.value = []
  if (Number(formData.objectType) !== 1) {
    targetStationId.value = ''
    formData.stationId = ''
  }
  targetKeyword.value = ''
  targetPickerVisible.value = false
  if (Number(formData.objectType) === 1) {
    await loadTargetOptions()
  } else if (Number(formData.objectType) === 2) {
    await loadLocationTree()
  }
  await loadLineOptions(lineKeyword.value)
}

const handleExecutorChange = (userId?: string | number) => {
  const selected = userOptions.value.find((item) => String(item.value) === String(userId))
  formData.executorName = selected?.label || ''
}

const handleDeptChange = async (deptId?: string | number | null) => {
  const normalizedTeamId = normalizeShiftTeamId(deptId as any)
  const selected = executeTeamOptions.value.find((item) => item.id === normalizedTeamId)
  formData.executeDeptName = selected?.name || ''
  await syncExecuteTeamDetail(normalizedTeamId)
}

const buildTargetPayload = (): InspectionPlanTargetVO[] => {
  return formData.targetIds.map((targetId, index) => ({
    targetId,
    targetSort: index + 1,
    targetName: targetLabelCache[targetId] || `对象-${targetId}`
  }))
}

const buildPayload = (): InspectionPlanVO => {
  return {
    id: formData.id,
    planName: formData.planName.trim(),
    inspectionType: formData.inspectionType,
    objectType: Number(formData.objectType),
    standardId: formData.standardId,
    lineId: formData.lineId,
    planStartDate: formData.planStartDate,
    planEndDate: formData.planEndDate,
    cycleUnit: formData.cycleUnit,
    cycleValue: Number(formData.cycleValue || 1),
    executorUserId: formData.executorUserId as string | number,
    executorName: formData.executorName || '',
    executeDeptId: normalizeShiftTeamId(formData.executeDeptId as any),
    executeDeptName: formData.executeDeptName || '',
    stationId: formData.stationId || '',
    targetIds: [...formData.targetIds],
    targets: buildTargetPayload(),
    remark: formData.remark?.trim() || ''
  }
}

const openForm = async (mode: FormMode, id?: number, row?: InspectionPlanVO) => {
  if (mode === 'update' && !canEditPlan(row)) {
    message.warning('仅当关联巡检任务状态为未开始或未完成时，才允许编辑巡检计划')
    return
  }
  formMode.value = mode
  formVisible.value = true
  resetForm()
  if (!userOptions.value.length || !executeTeamOptions.value.length) {
    await loadUserAndTeamOptions()
  }
  if (mode === 'create') {
    await Promise.all([loadTargetOptions(), loadStandardOptions(), loadLineOptions()])
    return
  }
  if (!id) return

  formLoading.value = true
  try {
    const data = await InspectionPlanApi.getInspectionPlan(id)
    Object.assign(formData, {
      id: data.id,
      planName: data.planName || '',
      inspectionType: data.inspectionType || '',
      objectType: data.objectType,
      standardId: data.standardId,
      lineId: data.lineId,
      planStartDate: formatPlanDateTime(data.planStartDate),
      planEndDate: formatPlanDateTime(data.planEndDate),
      cycleUnit: data.cycleUnit || 'MONTH',
      cycleValue: Number(data.cycleValue || 1),
      executorUserId: data.executorUserId,
      executorName: data.executorName || '',
      executeDeptId: normalizeShiftTeamId(data.executeDeptId as any),
      executeDeptName: data.executeDeptName || '',
      stationId: data.stationId || '',
      targetIds: (data.targetIds || [])
        .map((item) => normalizeTargetId(item))
        .filter((item: string | undefined): item is string => !!item),
      remark: data.remark || ''
    })
    if (formData.executeDeptId) {
      const executeTeamExists = executeTeamOptions.value.some((item) => item.id === formData.executeDeptId)
      if (!executeTeamExists && formData.executeDeptName) {
        executeTeamOptions.value.unshift({
          id: String(formData.executeDeptId),
          name: formData.executeDeptName
        })
      }
    }
    await syncExecuteTeamDetail(formData.executeDeptId)
    ensureExecutorOption(data.executorUserId, data.executorName)
    ;(data.targets || []).forEach((target) => {
      const targetId = normalizeTargetId(target.targetId || target.deviceId || target.locationId)
      if (targetId && target.targetName) {
        targetLabelCache[targetId] = target.targetName
      }
      if (targetId && target.stationId) {
        targetStationCache[targetId] = target.stationId
      }
    })
    if (data.standardId && data.standardName) {
      standardLabelCache[data.standardId] = data.standardName
    }
    if (data.lineId && data.lineName) {
      lineLabelCache[data.lineId] = data.lineName
    }
    targetStationId.value = data.stationId || ''
    await Promise.all([
      loadTargetOptions(targetKeyword.value),
      loadStandardOptions(standardKeyword.value),
      loadLineOptions(lineKeyword.value)
    ])
  } finally {
    formLoading.value = false
  }
}

const submitForm = async () => {
  if (formReadonly.value) return
  const valid = await formRef.value?.validate()
  if (!valid) return
  if (formData.planStartDate && formData.planEndDate && formData.planStartDate > formData.planEndDate) {
    message.error('计划结束时间不能早于开始时间')
    return
  }

  submitLoading.value = true
  try {
    const payload = buildPayload()
    if (formMode.value === 'create') {
      delete payload.id
      await InspectionPlanApi.createInspectionPlan(payload)
      message.success('新增成功')
    } else {
      await InspectionPlanApi.updateInspectionPlan(payload)
      message.success('更新成功')
    }
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

watch(
  () => linePreviewVisible.value,
  async (visible) => {
    if (visible) {
      await nextTick()
      await initLinePreviewMap()
      await nextTick()
      linePreviewMapInstance?.invalidateSize?.()
      renderLinePreviewMap(true)
      return
    }
    destroyLinePreviewMap()
  }
)

watch(
  () => formData.lineId,
  (lineId, previousLineId) => {
    if (lineId === previousLineId) return
    linePreviewData.value = undefined
    if (!lineId) {
      linePreviewVisible.value = false
    }
  }
)

onBeforeUnmount(() => {
  destroyLinePreviewMap()
})

onMounted(async () => {
  await Promise.all([loadUserAndTeamOptions(), getList()])
})
</script>

<style scoped lang="scss">
.inspection-plan-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.create-btn {
  border-radius: 999px;
  padding: 0 18px;
}

.target-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.target-tag {
  max-width: 140px;
}

.target-form-item {
  :deep(.el-form-item__content) {
    display: block;
  }
}

.target-picker-panel {
  border: 1px solid #dbe3ed;
  border-radius: 10px;
  background: #f9fbfe;
  padding: 10px 12px;
}

.target-picker-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.target-type-switch {
  display: flex;
  align-items: center;
  gap: 8px;
}

.target-switch-label {
  color: #334155;
  font-size: 13px;
}

.target-select-btn {
  border-radius: 8px;
}

.target-picker-body {
  min-height: 78px;
  border: 1px solid #dce6f1;
  border-radius: 8px;
  background: #fff;
  padding: 8px 10px;
}

.target-count-text {
  margin: 0;
  color: #475569;
  font-size: 13px;
}

.target-chip-list {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.target-chip {
  max-width: 180px;
}

.target-empty-text {
  margin-top: 10px;
  color: #94a3b8;
  text-align: center;
  font-size: 13px;
}

.execute-team-member-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  width: 100%;
  min-height: 32px;
  align-items: center;
}

.execute-team-member-tag {
  margin: 0;
}

.target-picker-dialog {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.target-picker-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.line-select-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.line-preview-btn {
  border-radius: 8px;
  padding: 0 14px;
}

.line-preview-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.line-preview-meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.line-preview-meta__item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  padding: 10px 12px;
  background: linear-gradient(135deg, #f8fbff 0%, #f8fafc 100%);
}

.line-preview-meta__item > span {
  color: #475569;
  font-size: 12px;
}

.line-preview-meta__item > strong {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.line-preview-map-wrap {
  position: relative;
  height: 500px;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  overflow: hidden;
  background: #e2e8f0;
}

.line-preview-map {
  width: 100%;
  height: 100%;
}

.line-preview-map-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #334155;
  font-size: 14px;
  background: rgba(248, 250, 252, 0.88);
  z-index: 400;
}

.line-preview-toolbar {
  position: absolute;
  top: 12px;
  right: 12px;
  min-width: 230px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid rgba(148, 163, 184, 0.45);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.12);
  z-index: 420;
}

.line-preview-toolbar__title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.line-preview-toolbar__actions {
  display: flex;
  gap: 8px;
}

.form-section {
  border: 1px solid #dbeafe;
  border-radius: 12px;
  padding: 12px 14px;
  background: linear-gradient(135deg, #f8fbff 0%, #f8fafc 100%);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: #0f4c81;
  margin-bottom: 12px;
}

:deep(.line-select-wrap .el-select) {
  flex: 1;
}

:deep(.line-preview-map .leaflet-control-container .leaflet-top.leaflet-left) {
  display: none;
}

:deep(.line-preview-map .leaflet-container) {
  font-family: 'Microsoft YaHei', sans-serif;
}

:deep(.inspection-plan-line-route-tooltip) {
  border: none;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.18);
  color: #0f172a;
  font-size: 12px;
  font-weight: 500;
}

@media (max-width: 960px) {
  .line-preview-meta {
    grid-template-columns: 1fr;
  }

  .line-preview-map-wrap {
    height: 420px;
  }

  .line-preview-toolbar {
    left: 12px;
    right: 12px;
    min-width: auto;
  }
}
</style>
