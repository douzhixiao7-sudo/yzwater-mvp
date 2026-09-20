<template>
  <div class="inspection-standard-page">
    <ContentWrap>
      <el-form
        ref="queryFormRef"
        :inline="true"
        :model="queryParams"
        class="-mb-15px"
        label-width="100px"
      >
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
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标准名称" prop="standardName">
          <el-input
            v-model="queryParams.standardName"
            clearable
            class="!w-220px"
            placeholder="请输入标准名称"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="巡检类型" prop="inspectionType">
          <el-select
            v-model="queryParams.inspectionType"
            clearable
            class="!w-180px"
            placeholder="请选择巡检类型"
          >
            <el-option
              v-for="option in inspectionTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="适用设施对象" prop="targetType">
          <el-select
            v-model="queryParams.targetType"
            clearable
            class="!w-200px"
            placeholder="请选择适用设施对象"
          >
            <el-option
              v-for="option in targetTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable class="!w-160px" placeholder="请选择状态">
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
          <el-button
            v-hasPermi="['iot:inspection-standard:create']"
            type="primary"
            plain
            @click="openForm('create')"
          >
            <Icon icon="ep:plus" class="mr-5px" />
            新增标准
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="标准名称" prop="standardName" min-width="170" />
        <el-table-column label="所属站点" prop="stationId" min-width="140">
          <template #default="scope">
            {{ getStationLabel(scope.row.stationId) }}
          </template>
        </el-table-column>
        <el-table-column label="巡检类型" prop="inspectionType" min-width="120">
          <template #default="scope">
            {{ getInspectionTypeLabel(scope.row.inspectionType) }}
          </template>
        </el-table-column>
        <el-table-column label="适用设施对象" min-width="180">
          <template #default="scope">
            {{ getTargetTypeNames(scope.row.targetTypes) }}
          </template>
        </el-table-column>
        <el-table-column label="对象名称" min-width="160" show-overflow-tooltip>
          <template #default="scope">
            {{ getTargetNames(scope.row.targetNames) }}
          </template>
        </el-table-column>
        <el-table-column label="检查项数" prop="itemCount" width="88" align="center" />
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'info'" effect="plain">
              {{ scope.row.status === 0 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170">
          <template #default="scope">{{ formatCreateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="scope">
            <el-button
              type="primary"
              link
              v-hasPermi="['iot:inspection-standard:query']"
              @click="openForm('view', scope.row.id)"
            >
              详情
            </el-button>
            <el-button
              type="primary"
              link
              v-hasPermi="['iot:inspection-standard:update']"
              @click="openForm('update', scope.row.id)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              v-hasPermi="['iot:inspection-standard:delete']"
              @click="handleDelete(scope.row.id)"
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

    <Dialog
      v-model="formVisible"
      :title="formTitle"
      width="1360px"
      class="inspection-standard-dialog"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        v-loading="formLoading"
        :model="formData"
        :rules="formRules"
        label-width="110px"
      >
        <section class="form-panel">
          <h3 class="form-panel__title">基础信息</h3>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="标准名称" prop="standardName">
                <el-input
                  v-model="formData.standardName"
                  maxlength="100"
                  show-word-limit
                  placeholder="请输入标准名称"
                  :disabled="isView"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="所属站点" prop="stationId">
                <el-select
                  v-model="formData.stationId"
                  filterable
                  clearable
                  placeholder="请选择所属站点"
                  :disabled="isView"
                  @change="handleFormStationChange"
                >
                  <el-option
                    v-for="option in stationOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="巡检类型" prop="inspectionType">
                <el-select
                  v-model="formData.inspectionType"
                  placeholder="请选择巡检类型"
                  :disabled="isView"
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
            <el-col :span="8">
              <el-form-item label="建议检查周期" prop="suggestCycleUnit">
                <el-select
                  v-model="formData.suggestCycleUnit"
                  placeholder="请选择建议检查周期"
                  :disabled="isView"
                >
                  <el-option
                    v-for="option in inspectionPeriodOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="formData.status" :disabled="isView">
                  <el-radio v-for="option in statusOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注" prop="remark">
                <el-input
                  v-model="formData.remark"
                  type="textarea"
                  :rows="2"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入备注"
                  :disabled="isView"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-panel">
          <div class="item-header">
            <h3 class="form-panel__title">检查项目</h3>
            <el-button v-if="!isView" type="primary" plain @click="addTarget">
              <Icon icon="ep:plus" class="mr-5px" />
              新增适用对象
            </el-button>
          </div>

          <div v-if="!formData.targets.length" class="empty-holder">
            <span>请至少新增一个适用对象</span>
          </div>

          <div v-for="(target, targetIndex) in formData.targets" :key="target.__uuid" class="target-card">
            <div class="target-card__head">
              <div class="target-card__title">适用对象 {{ targetIndex + 1 }}</div>
              <el-button v-if="!isView" type="danger" link @click="removeTarget(targetIndex)">
                删除对象
              </el-button>
            </div>

            <el-row :gutter="16">
              <el-col :span="6">
                <el-form-item label="适用设施对象" required>
                  <el-select
                    v-model="target.targetType"
                    placeholder="请选择适用设施对象"
                    :disabled="isView"
                    @change="handleTargetTypeChange(target)"
                  >
                    <el-option
                      v-for="option in targetTypeOptions"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="18">
                <el-form-item :label="target.targetType === DEVICE_TARGET_TYPE ? '关联设备' : '关联对象'" required>
                  <el-select
                    v-model="target.targetId"
                    filterable
                    remote
                    reserve-keyword
                    clearable
                    :disabled="isView || !target.targetType || isTargetSelectDisabled(target)"
                    :loading="target.optionLoading"
                    :placeholder="target.targetType === DEVICE_TARGET_TYPE ? '请选择关联设备' : '请选择关联对象'"
                    :remote-method="(keyword) => handleTargetSearch(target, keyword)"
                    @visible-change="(visible) => handleTargetDropdownVisible(target, visible)"
                  >
                    <el-option
                      v-for="option in target.targetOptions"
                      :key="`${target.__uuid}_${option.value}`"
                      :label="option.label"
                      :value="option.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <div class="item-header item-header--inner">
              <h4 class="form-panel__subtitle">检查项目</h4>
              <el-button v-if="!isView" type="primary" link @click="addItem(target)">
                <Icon icon="ep:plus" class="mr-5px" />
                新增检查项目
              </el-button>
            </div>

            <div v-if="!target.items.length" class="empty-holder empty-holder--inner">
              <span>请至少新增一个检查项目</span>
            </div>

            <div v-for="(item, itemIndex) in target.items" :key="item.__uuid" class="item-card">
              <div class="item-card__head">
                <div class="item-card__title">{{ item.itemName?.trim() || `检查项目 ${itemIndex + 1}` }}</div>
                <el-button v-if="!isView" type="danger" link @click="removeItem(target, itemIndex)">
                  删除项目
                </el-button>
              </div>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="项目名称" required>
                    <el-input v-model="item.itemName" placeholder="请输入项目名称" :disabled="isView" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="是否上传附件">
                    <el-switch
                      v-model="item.needUploadAttachment"
                      :active-value="1"
                      :inactive-value="0"
                      :disabled="isView"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="合格标准" required>
                    <el-input
                      v-model="item.qualifiedRule"
                      type="textarea"
                      :rows="2"
                      maxlength="1000"
                      show-word-limit
                      placeholder="请输入合格标准"
                      :disabled="isView"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="项目描述" required>
                    <el-input
                      v-model="item.itemDesc"
                      type="textarea"
                      :rows="2"
                      maxlength="500"
                      show-word-limit
                      placeholder="请输入项目描述"
                      :disabled="isView"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="record-box">
                <div class="record-box__header">
                  <h4>记录项</h4>
                  <el-button v-if="!isView" type="primary" link @click="addRecord(item)">
                    <Icon icon="ep:plus" class="mr-5px" />
                    新增记录项
                  </el-button>
                </div>
                <el-table :data="item.recordTemplates" border class="record-table" empty-text="暂无记录项">
                  <el-table-column label="属性名称" min-width="160">
                    <template #default="scope">
                      <el-input v-model="scope.row.attrName" :disabled="isView" placeholder="例如：压力" />
                    </template>
                  </el-table-column>
                  <el-table-column label="具体数值" min-width="160">
                    <template #default="scope">
                      <el-input v-model="scope.row.defaultValue" :disabled="isView" placeholder="请输入具体数值" />
                    </template>
                  </el-table-column>
                  <el-table-column label="单位" min-width="120">
                    <template #default="scope">
                      <el-input v-model="scope.row.attrUnit" :disabled="isView" placeholder="例如：MPa" />
                    </template>
                  </el-table-column>
                  <el-table-column label="是否必填" width="100" align="center">
                    <template #default="scope">
                      <el-switch
                        v-model="scope.row.requiredFlag"
                        :active-value="1"
                        :inactive-value="0"
                        :disabled="isView"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="备注说明" min-width="180">
                    <template #default="scope">
                      <el-input
                        v-model="scope.row.remark"
                        maxlength="200"
                        show-word-limit
                        :disabled="isView"
                        placeholder="请输入备注说明"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column v-if="!isView" label="操作" width="80" fixed="right">
                    <template #default="scope">
                      <el-button type="danger" link @click="removeRecord(item, scope.$index)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>

            <div class="item-header item-header--inner item-header--compact">
              <h4 class="form-panel__subtitle">检查结果等级</h4>
              <span class="panel-tip">勾选后，任务执行时会展示对应结果项，描述支持按对象调整。</span>
            </div>
            <div v-if="!target.checkResultConfigs.length" class="empty-holder empty-holder--inner">
              <span>未查询到检查结果等级字典，请检查 iot_inspection_check_result 配置</span>
            </div>
            <div v-else class="check-result-grid check-result-grid--target">
              <div
                v-for="config in target.checkResultConfigs"
                :key="`${target.__uuid}_${config.value}`"
                class="check-result-item"
                :class="{ 'check-result-item--active': config.checked }"
              >
                <div class="check-result-item__meta">
                  <el-checkbox v-model="config.checked" :disabled="isView" />
                  <el-tag :type="getCheckResultTagType(config.value)" effect="light">
                    {{ config.label }}
                  </el-tag>
                </div>
                <el-input
                  v-model="config.remark"
                  maxlength="120"
                  show-word-limit
                  :disabled="isView || !config.checked"
                  :placeholder="`请输入${config.label}结果描述`"
                />
              </div>
            </div>
            <p class="check-result-tip">每个适用对象至少选择一个结果等级，且描述不能为空。</p>
          </div>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="submitForm">
          确定
        </el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import {
  InspectionStandardApi,
  type InspectionCheckResultConfigVO,
  type InspectionStandardItemRecordVO,
  type InspectionStandardItemVO,
  type InspectionStandardPageReqVO,
  type InspectionStandardTargetVO,
  type InspectionStandardVO
} from '@/api/iot/inspection/standard'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { getDictDataByType, type DictDataVO } from '@/api/system/dict/dict.data'
import type { FormInstance, FormRules } from 'element-plus'

interface SelectOption {
  label: string
  value: string | number
}

interface TargetSelectOption extends SelectOption {
  stationId?: string
}

type TargetIdValue = string | number
type FormMode = 'create' | 'update' | 'view'

interface InspectionCheckResultOption {
  value: string
  label: string
  remark: string
}

interface InspectionCheckResultConfigEditVO extends InspectionCheckResultConfigVO {
  checked: boolean
}

interface InspectionStandardItemRecordEditVO extends InspectionStandardItemRecordVO {
  __uuid: string
}

interface InspectionStandardItemEditVO extends Omit<InspectionStandardItemVO, 'recordTemplates'> {
  __uuid: string
  recordTemplates: InspectionStandardItemRecordEditVO[]
}

interface InspectionStandardTargetEditVO
  extends Omit<InspectionStandardTargetVO, 'items' | 'targetId' | 'stationId' | 'checkResultConfigs'> {
  targetId?: TargetIdValue
  __uuid: string
  items: InspectionStandardItemEditVO[]
  checkResultConfigs: InspectionCheckResultConfigEditVO[]
  targetOptions: TargetSelectOption[]
  optionLoading: boolean
}

interface InspectionStandardEditVO extends Omit<InspectionStandardVO, 'targets'> {
  targets: InspectionStandardTargetEditVO[]
}

const DEVICE_TARGET_TYPE = 'device'
const SUPPORTED_FACILITY_TYPES = new Set([
  'river',
  'river_section',
  'reservoir',
  'irrigation',
  'signboard',
  'pump_station',
  'dike',
  'flood_prevention_material'
])
const statusOptions: SelectOption[] = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 }
]
const inspectionCheckResultOptions = ref<InspectionCheckResultOption[]>([])

const checkResultTagTypeMap: Record<string, 'success' | 'primary' | 'warning' | 'danger' | 'info'> = {
  excellent: 'success',
  good: 'primary',
  qualified: 'warning',
  unqualified: 'danger'
}

const message = useMessage()

const inspectionTypeOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_INSPECTION_TYPE)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const inspectionPeriodOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_INSPECTION_PERIOD)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const stationOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const facilityTypeOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.ZD_SSLB)
  const dedupSet = new Set<string>()
  const options: SelectOption[] = []
  dictOptions.forEach((dict) => {
    const value = String(dict.value || '').trim()
    if (!value || dedupSet.has(value) || !SUPPORTED_FACILITY_TYPES.has(value)) {
      return
    }
    dedupSet.add(value)
    options.push({
      label: dict.label,
      value
    })
  })
  return options
})

const targetTypeOptions = computed<SelectOption[]>(() => {
  return [{ label: '设备', value: DEVICE_TARGET_TYPE }, ...facilityTypeOptions.value]
})

const targetTypeLabelMap = computed(() => {
  const map = new Map<string, string>()
  targetTypeOptions.value.forEach((item) => map.set(String(item.value), item.label))
  return map
})

const loading = ref(false)
const total = ref(0)
const list = ref<InspectionStandardVO[]>([])
const queryFormRef = ref<FormInstance>()
const queryParams = reactive<InspectionStandardPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  stationId: undefined,
  standardName: undefined,
  inspectionType: undefined,
  targetType: undefined,
  status: undefined,
  createTime: undefined
})

const getInspectionTypeLabel = (value?: string) => {
  if (!value) {
    return '-'
  }
  return inspectionTypeOptions.value.find((item) => item.value === value)?.label || value
}

const getStationLabel = (value?: string) => {
  if (!value) {
    return '-'
  }
  return stationOptions.value.find((item) => item.value === value)?.label || value
}

const getTargetTypeNames = (targetTypes?: string[]) => {
  if (!targetTypes?.length) {
    return '-'
  }
  return targetTypes
    .map((targetType) => targetTypeLabelMap.value.get(String(targetType)) || targetType)
    .join('、')
}

const getTargetNames = (targetNames?: string[]) => {
  if (!targetNames?.length) {
    return '-'
  }
  return targetNames.join('、')
}

const formatCreateTime = (value?: string | number) => {
  return value ? formatDate(value) : '-'
}

const getCheckResultTagType = (value: string) => {
  return checkResultTagTypeMap[value] || 'info'
}

const createCheckResultConfigItems = (
  selectedConfigs: InspectionCheckResultConfigVO[] = []
): InspectionCheckResultConfigEditVO[] => {
  const selectedConfigMap = new Map(
    selectedConfigs
      .filter((item) => item?.value)
      .map((item) => [String(item.value).trim(), item])
  )
  return inspectionCheckResultOptions.value.map((option) => {
    const selectedConfig = selectedConfigMap.get(option.value)
    return {
      value: option.value,
      label: option.label,
      checked: !!selectedConfig,
      remark: selectedConfig?.remark?.trim() || option.remark || ''
    }
  })
}

const syncTargetCheckResultConfigs = (
  target: InspectionStandardTargetEditVO,
  selectedConfigs: InspectionCheckResultConfigVO[] = []
) => {
  target.checkResultConfigs = createCheckResultConfigItems(selectedConfigs)
}

const loadInspectionCheckResultOptions = async () => {
  try {
    const rows = await getDictDataByType('iot_inspection_check_result')
    const optionMap = new Map<string, InspectionCheckResultOption>()
    ;(rows || []).forEach((row: DictDataVO) => {
      const value = String(row.value || '').trim()
      if (!value || optionMap.has(value)) {
        return
      }
      const label = String(row.label || '').trim() || value
      optionMap.set(value, {
        value,
        label,
        remark: String(row.remark || '').trim()
      })
    })
    inspectionCheckResultOptions.value = Array.from(optionMap.values())
  } catch {
    inspectionCheckResultOptions.value = []
  }
  if (!inspectionCheckResultOptions.value.length) {
    message.warning('未查询到检查结果等级字典，请先维护 iot_inspection_check_result')
  }
  formData.targets.forEach((target) => {
    const selectedConfigs = (target.checkResultConfigs || []).filter((item) => item.checked)
    syncTargetCheckResultConfigs(target, selectedConfigs)
  })
}

const getList = async () => {
  loading.value = true
  try {
    const data = await InspectionStandardApi.getInspectionStandardPage(queryParams)
    list.value = (data.list || []).map((item) => ({
      ...item,
      targetTypes: item.targetTypes || [],
      targetNames: item.targetNames || []
    }))
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
  queryParams.pageNo = 1
  await getList()
}

const handleDelete = async (id?: number) => {
  if (!id) {
    return
  }
  try {
    await message.delConfirm('确认删除该巡检标准吗？')
    await InspectionStandardApi.deleteInspectionStandard(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

const formVisible = ref(false)
const formLoading = ref(false)
const submitLoading = ref(false)
const formMode = ref<FormMode>('create')
const formRef = ref<FormInstance>()

const createUuid = () => `${Date.now()}_${Math.random().toString(16).slice(2)}`

const createEmptyRecord = (): InspectionStandardItemRecordEditVO => ({
  __uuid: createUuid(),
  attrName: '',
  attrUnit: '',
  valueType: 'TEXT',
  defaultValue: '',
  remark: '',
  requiredFlag: 1,
  sort: 1
})

const createEmptyItem = (): InspectionStandardItemEditVO => ({
  __uuid: createUuid(),
  itemName: '',
  itemDesc: '',
  qualifiedRule: '',
  needUploadAttachment: 0,
  defaultResult: '',
  sort: 1,
  recordCount: 0,
  recordTemplates: []
})

const createEmptyTarget = (): InspectionStandardTargetEditVO => ({
  __uuid: createUuid(),
  targetType: '',
  targetId: undefined,
  targetName: '',
  sort: 1,
  itemCount: 0,
  items: [],
  checkResultConfigs: createCheckResultConfigItems(),
  targetOptions: [],
  optionLoading: false
})

const createDefaultFormData = (): InspectionStandardEditVO => ({
  id: undefined,
  stationId: '',
  standardName: '',
  inspectionType: '',
  suggestCycleUnit: '',
  suggestCycleValue: 1,
  status: 0,
  targetCount: 0,
  itemCount: 0,
  targetTypes: [],
  targetNames: [],
  remark: '',
  targets: [createEmptyTarget()]
})

const formData = reactive<InspectionStandardEditVO>(createDefaultFormData())

const formRules: FormRules = {
  stationId: [{ required: true, message: '所属站点不能为空', trigger: 'change' }],
  standardName: [{ required: true, message: '标准名称不能为空', trigger: 'blur' }],
  inspectionType: [{ required: true, message: '巡检类型不能为空', trigger: 'change' }],
  suggestCycleUnit: [{ required: true, message: '建议检查周期不能为空', trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
}

const isView = computed(() => formMode.value === 'view')
const formTitle = computed(() => {
  if (formMode.value === 'create') {
    return '新增巡检标准'
  }
  if (formMode.value === 'update') {
    return '编辑巡检标准'
  }
  return '巡检标准详情'
})

const resetForm = () => {
  Object.assign(formData, createDefaultFormData())
  formRef.value?.clearValidate()
}

// 后端 Long 超过 JS 安全整数时会序列化为字符串，这里统一按字符串处理，避免精度丢失。
const normalizeTargetId = (targetId?: TargetIdValue | null): string | undefined => {
  if (targetId === undefined || targetId === null) {
    return undefined
  }
  const normalizedId = String(targetId).trim()
  return normalizedId || undefined
}

const isTargetSelectDisabled = (_target: InspectionStandardTargetEditVO) => {
  return !formData.stationId
}

const ensureTargetOption = (
  target: InspectionStandardTargetEditVO,
  targetId?: TargetIdValue,
  targetName?: string,
  stationId?: string
) => {
  const normalizedTargetId = normalizeTargetId(targetId)
  if (!normalizedTargetId) {
    return
  }
  const exists = target.targetOptions.some(
    (option) => normalizeTargetId(option.value) === normalizedTargetId
  )
  if (exists) {
    return
  }
  target.targetOptions.unshift({
    label:
      targetName || `${targetTypeLabelMap.value.get(target.targetType) || '对象'}-${normalizedTargetId}`,
    value: normalizedTargetId,
    stationId
  })
}

const fetchTargetOptions = async (
  target: InspectionStandardTargetEditVO,
  keyword?: string,
  force = false
) => {
  if (!target.targetType) {
    target.targetOptions = []
    return
  }
  if (!formData.stationId) {
    target.targetOptions = []
    return
  }
  if (!force && !keyword && target.targetOptions.length) {
    return
  }
  target.optionLoading = true
  try {
    const rows = await InspectionStandardApi.getInspectionTargetOptions({
      targetType: target.targetType,
      stationId: formData.stationId || undefined,
      keyword: keyword?.trim() || undefined,
      limit: 200
    })
    target.targetOptions = (rows || []).reduce<TargetSelectOption[]>((result, item) => {
      const normalizedId = normalizeTargetId(item.id)
      if (!normalizedId) {
        return result
      }
      result.push({
        label: item.name,
        value: normalizedId,
        stationId: item.stationId
      })
      return result
    }, [])
    ensureTargetOption(target, target.targetId, target.targetName, formData.stationId || undefined)
  } finally {
    target.optionLoading = false
  }
}

const handleTargetTypeChange = async (target: InspectionStandardTargetEditVO) => {
  target.targetId = undefined
  target.targetName = ''
  target.targetOptions = []
  if (!target.targetType || !formData.stationId) {
    return
  }
  await fetchTargetOptions(target, '', true)
}

const handleFormStationChange = () => {
  formData.targets.forEach((target) => {
    target.targetId = undefined
    target.targetName = ''
    target.targetOptions = []
  })
}

const handleTargetSearch = async (target: InspectionStandardTargetEditVO, keyword: string) => {
  await fetchTargetOptions(target, keyword, true)
}

const handleTargetDropdownVisible = async (target: InspectionStandardTargetEditVO, visible: boolean) => {
  if (!visible) {
    return
  }
  await fetchTargetOptions(target, '', false)
}

const addTarget = () => {
  formData.targets.push(createEmptyTarget())
}

const removeTarget = (index: number) => {
  formData.targets.splice(index, 1)
}

const addItem = (target: InspectionStandardTargetEditVO) => {
  target.items.push(createEmptyItem())
}

const removeItem = (target: InspectionStandardTargetEditVO, index: number) => {
  target.items.splice(index, 1)
}

const addRecord = (item: InspectionStandardItemEditVO) => {
  item.recordTemplates = item.recordTemplates || []
  item.recordTemplates.push(createEmptyRecord())
}

const removeRecord = (item: InspectionStandardItemEditVO, index: number) => {
  item.recordTemplates.splice(index, 1)
}

const convertItemFromResp = (item: InspectionStandardItemVO): InspectionStandardItemEditVO => {
  return {
    __uuid: createUuid(),
    id: item.id,
    itemName: item.itemName || '',
    itemDesc: item.itemDesc || '',
    qualifiedRule: item.qualifiedRule || '',
    needUploadAttachment: item.needUploadAttachment ?? 0,
    defaultResult: item.defaultResult || '',
    sort: item.sort,
    recordCount: item.recordCount,
    recordTemplates: (item.recordTemplates || []).map((record) => ({
      __uuid: createUuid(),
      id: record.id,
      attrName: record.attrName || '',
      attrUnit: record.attrUnit || '',
      valueType: record.valueType || 'TEXT',
      defaultValue: record.defaultValue || '',
      remark: record.remark || '',
      requiredFlag: record.requiredFlag ?? 1,
      sort: record.sort
    }))
  }
}

const convertTargetFromResp = async (
  target: InspectionStandardTargetVO
): Promise<InspectionStandardTargetEditVO> => {
  const editTarget: InspectionStandardTargetEditVO = {
    __uuid: createUuid(),
    id: target.id,
    targetType: target.targetType || '',
    targetId: normalizeTargetId(target.targetId),
    targetName: target.targetName || '',
    sort: target.sort,
    itemCount: target.itemCount,
    items: (target.items || []).map((item) => convertItemFromResp(item)),
    checkResultConfigs: createCheckResultConfigItems(target.checkResultConfigs || []),
    targetOptions: [],
    optionLoading: false
  }
  ensureTargetOption(editTarget, editTarget.targetId, editTarget.targetName, formData.stationId || undefined)
  if (editTarget.targetType) {
    await fetchTargetOptions(editTarget, '', true)
  }
  return editTarget
}

const openForm = async (mode: FormMode, id?: number) => {
  formMode.value = mode
  formVisible.value = true
  resetForm()
  if (mode === 'create') {
    return
  }
  if (!id) {
    return
  }
  formLoading.value = true
  try {
    const data = await InspectionStandardApi.getInspectionStandard(id)
    formData.stationId = data.stationId || data.targets?.[0]?.stationId || ''
    const targetList: InspectionStandardTargetEditVO[] = []
    for (const target of data.targets || []) {
      targetList.push(await convertTargetFromResp(target))
    }
    Object.assign(formData, {
      id: data.id,
      stationId: data.stationId || data.targets?.[0]?.stationId || '',
      standardName: data.standardName || '',
      inspectionType: data.inspectionType || '',
      suggestCycleUnit: data.suggestCycleUnit || '',
      suggestCycleValue: data.suggestCycleValue || 1,
      status: data.status ?? 0,
      remark: data.remark || '',
      targets: targetList.length ? targetList : [createEmptyTarget()]
    })
  } finally {
    formLoading.value = false
  }
}

const validateTargets = (): boolean => {
  if (!formData.stationId) {
    message.error('所属站点不能为空')
    return false
  }
  if (!inspectionCheckResultOptions.value.length) {
    message.error('未查询到检查结果等级字典，请先维护 iot_inspection_check_result')
    return false
  }
  if (!formData.targets.length) {
    message.error('请至少新增一个适用对象')
    return false
  }
  const duplicateSet = new Set<string>()
  for (let i = 0; i < formData.targets.length; i++) {
    const target = formData.targets[i]
    if (!target.targetType) {
      message.error(`第 ${i + 1} 个适用对象未选择对象类型`)
      return false
    }
    const normalizedTargetId = normalizeTargetId(target.targetId)
    if (!normalizedTargetId) {
      message.error(`第 ${i + 1} 个适用对象未选择关联对象`)
      return false
    }
    const duplicateKey = `${target.targetType}_${normalizedTargetId}`
    if (duplicateSet.has(duplicateKey)) {
      message.error(`第 ${i + 1} 个适用对象与前面重复，请调整后重试`)
      return false
    }
    duplicateSet.add(duplicateKey)
    if (!target.items.length) {
      message.error(`第 ${i + 1} 个适用对象下至少需要一个检查项目`)
      return false
    }
    const selectedConfigs = (target.checkResultConfigs || []).filter((item) => item.checked)
    if (!selectedConfigs.length) {
      message.error(`第 ${i + 1} 个适用对象至少选择一个检查结果等级`)
      return false
    }
    for (const config of selectedConfigs) {
      if (!config.remark?.trim()) {
        message.error(`第 ${i + 1} 个适用对象的“${config.label}”结果描述不能为空`)
        return false
      }
    }
    for (let j = 0; j < target.items.length; j++) {
      const item = target.items[j]
      const prefix = `第 ${i + 1} 个对象的第 ${j + 1} 个检查项目`
      if (!item.itemName?.trim()) {
        message.error(`${prefix}的“项目名称”不能为空`)
        return false
      }
      if (!item.itemDesc?.trim()) {
        message.error(`${prefix}的“项目描述”不能为空`)
        return false
      }
      if (!item.qualifiedRule?.trim()) {
        message.error(`${prefix}的“合格标准”不能为空`)
        return false
      }
      for (let k = 0; k < (item.recordTemplates || []).length; k++) {
        const record = item.recordTemplates[k]
        const recordPrefix = `${prefix}的第 ${k + 1} 条记录项`
        if (!record.attrName?.trim()) {
          message.error(`${recordPrefix}“属性名称”不能为空`)
          return false
        }
        if (!record.defaultValue?.trim()) {
          message.error(`${recordPrefix}“具体数值”不能为空`)
          return false
        }
        if (!record.attrUnit?.trim()) {
          message.error(`${recordPrefix}“单位”不能为空`)
          return false
        }
      }
    }
  }
  return true
}

const buildSubmitPayload = (): InspectionStandardVO => {
  const targets = formData.targets.map((target, targetIndex) => ({
    id: target.id,
    targetType: target.targetType,
    targetId: normalizeTargetId(target.targetId)!,
    stationId: formData.stationId,
    sort: targetIndex + 1,
    checkResultConfigs: (target.checkResultConfigs || [])
      .filter((item) => item.checked)
      .map((item) => ({
        value: item.value,
        label: item.label,
        remark: item.remark?.trim() || ''
      })),
    items: (target.items || []).map((item, itemIndex) => ({
      id: item.id,
      itemName: item.itemName.trim(),
      itemDesc: item.itemDesc.trim(),
      qualifiedRule: item.qualifiedRule.trim(),
      needUploadAttachment: item.needUploadAttachment ?? 0,
      sort: itemIndex + 1,
      defaultResult: item.defaultResult?.trim() || undefined,
      recordTemplates: (item.recordTemplates || []).map((record, recordIndex) => ({
        id: record.id,
        attrName: record.attrName.trim(),
        attrUnit: record.attrUnit.trim(),
        valueType: record.valueType || 'TEXT',
        defaultValue: record.defaultValue.trim(),
        remark: record.remark?.trim() || '',
        requiredFlag: record.requiredFlag ?? 1,
        sort: recordIndex + 1
      }))
    }))
  }))
  return {
    id: formData.id,
    stationId: formData.stationId,
    standardName: formData.standardName.trim(),
    inspectionType: formData.inspectionType,
    suggestCycleUnit: formData.suggestCycleUnit,
    suggestCycleValue: 1,
    status: formData.status,
    remark: formData.remark?.trim() || '',
    targets
  }
}

const submitForm = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }
  if (!validateTargets()) {
    return
  }
  submitLoading.value = true
  try {
    const payload = buildSubmitPayload()
    if (formMode.value === 'create') {
      delete payload.id
      await InspectionStandardApi.createInspectionStandard(payload)
      message.success('新增成功')
    } else {
      await InspectionStandardApi.updateInspectionStandard(payload)
      message.success('更新成功')
    }
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await loadInspectionCheckResultOptions()
  await getList()
})
</script>

<style scoped lang="scss">
.inspection-standard-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-panel {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px 18px;
  margin-bottom: 14px;
  background: #fff;
  box-shadow: 0 8px 24px -24px rgba(15, 23, 42, 0.7);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    border-top-left-radius: 12px;
    border-bottom-left-radius: 12px;
    background: linear-gradient(180deg, #60a5fa 0%, #2563eb 100%);
  }

  &__title {
    margin: 0 0 2px;
    font-size: 16px;
    font-weight: 600;
    color: #0f172a;
  }

  &__subtitle {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: #1e293b;
  }
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  gap: 12px;

  &--inner {
    margin-top: 4px;
    margin-bottom: 10px;
  }

  &--compact {
    margin-bottom: 10px;
  }
}

.target-card,
.item-card {
  border: 1px solid #dbe5f1;
  border-radius: 12px;
  padding: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #f8fafc 100%);
  margin-bottom: 12px;
}

.target-card__head,
.item-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.target-card__title,
.item-card__title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.panel-tip {
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.check-result-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(280px, 1fr));
  gap: 12px;

  &--target {
    margin-bottom: 6px;
  }
}

.check-result-item {
  border: 1px solid #dbe5f1;
  border-radius: 10px;
  padding: 12px;
  background: #f8fafc;
  transition: all 0.2s ease;

  &__meta {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
  }

  &--active {
    border-color: #93c5fd;
    background: #eff6ff;
    box-shadow: 0 10px 24px -22px rgba(37, 99, 235, 0.9);
  }
}

.check-result-tip {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 12px;
}

.record-box {
  margin-top: 4px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;

    h4 {
      margin: 0;
      font-size: 14px;
      color: #334155;
      font-weight: 600;
    }
  }
}

.record-table {
  width: 100%;
}

.empty-holder {
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
  min-height: 80px;
  color: #64748b;
  margin-bottom: 12px;

  &--inner {
    min-height: 64px;
  }
}

@media (max-width: 1280px) {
  .check-result-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1024px) {
  .inspection-standard-page {
    gap: 10px;
  }
}

:deep(.inspection-standard-dialog .el-dialog__body) {
  max-height: 72vh;
  overflow-y: auto;
  padding-top: 10px;
}
</style>
