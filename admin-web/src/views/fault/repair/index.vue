<template>
  <ContentWrap>
    <!-- 查询区域 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="110px"
    >
      <el-form-item label="设备名称" prop="deviceName">
        <el-input
          v-model="queryParams.deviceName"
          clearable
          class="!w-220px"
          placeholder="请输入设备名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备类型" prop="deviceType">
        <el-input
          v-model="queryParams.deviceType"
          clearable
          class="!w-220px"
          placeholder="请输入设备类型"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="处理状态" prop="status">
        <el-select v-model="queryParams.status" clearable class="!w-220px" placeholder="请选择处理状态">
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.IOT_DEVICE_FAULT_STATUS)"
            :key="String(dict.value)"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="上报人" prop="reporterName">
        <el-input
          v-model="queryParams.reporterName"
          clearable
          class="!w-220px"
          placeholder="请输入上报人"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="处理人" prop="repairName">
        <el-input
          v-model="queryParams.repairName"
          clearable
          class="!w-220px"
          placeholder="请输入处理人"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="故障时间" prop="faultTime">
        <el-date-picker
          v-model="queryParams.faultTime"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
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
        <el-button type="primary" plain :disabled="!canCreateAction" @click="openForm('create')">
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
        <el-button
          v-hasPermi="['iot:fault-repair:export']"
          type="success"
          plain
          :loading="exportLoading"
          @click="handleExport"
        >
          <Icon icon="ep:download" class="mr-5px" />
          导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="设备名称" align="center" prop="deviceName" min-width="140" />
      <el-table-column label="设备类型" align="center" prop="deviceType" min-width="120">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_DEVICE_TYPE" :value="scope.row.deviceType" />
        </template>
      </el-table-column>
      <el-table-column label="故障类型" align="center" prop="faultType" min-width="120">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_DEVICE_FAULT_TYPE" :value="scope.row.faultType" />
        </template>
      </el-table-column>
      <el-table-column label="工单编号" align="center" prop="orderNo" min-width="140" />
      <el-table-column label="故障时间" align="center" prop="faultTime" min-width="160" :formatter="dateFormatter" />
      <el-table-column label="上报人" align="center" prop="reporterName" width="120" />
      <el-table-column label="处理人" align="center" prop="repairName" width="120" />
      <el-table-column label="处理状态" align="center" prop="status" width="120">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_DEVICE_FAULT_STATUS" :value="normalizeStatus(scope.row.status)" />
        </template>
      </el-table-column>
      <el-table-column label="完成时间" align="center" prop="finishTime" min-width="160" :formatter="dateFormatter" />
      <el-table-column label="操作" align="center" min-width="220">
        <template #default="scope">
          <el-button
            link
            type="primary"
            :disabled="!canOpenEditOrView(scope.row)"
            @click="openForm(isCompleted(scope.row) ? 'view' : 'update', scope.row.id)"
          >
            {{ isCompleted(scope.row) ? '详情' : '编辑' }}
          </el-button>
          <el-button
            link
            type="warning"
            :disabled="!canAuditPermi || !canAudit(scope.row)"
            @click="openAudit(scope.row)"
          >
            派工
          </el-button>
          <el-button
            v-hasPermi="['iot:fault-repair:feedback']"
            link
            type="success"
            :disabled="!canFeedback(scope.row)"
            @click="openFeedback(scope.row)"
          >
            提交结果
          </el-button>
          <el-button
            link
            type="danger"
            :disabled="!canDeleteAction || isCompleted(scope.row)"
            @click="handleDelete(scope.row.id)"
          >
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

  <el-dialog
    v-model="formVisible"
    :title="formTitle"
    width="980px"
    destroy-on-close
    :close-on-click-modal="false"
    class="fault-repair-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      label-position="left"
      :disabled="isView"
      class="fault-main-form"
    >
      <div class="form-section">
        <div class="section-title">故障信息</div>
        <el-row :gutter="12">

          <el-col :span="12">
            <el-form-item label="所属站点">
              <el-select
                v-model="selectedStationId"
                clearable
                placeholder="请选择所属站点"
                class="!w-full"
                @change="handleStationChange"
              >
                <el-option
                  v-for="dict in getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)"
                  :key="String(dict.value)"
                  :label="dict.label"
                  :value="String(dict.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备" prop="deviceId">
              <el-select
                v-model="formData.deviceId"
                filterable
                clearable
                placeholder="请选择设备"
                class="!w-full"
                @change="handleDeviceChange"
              >
                <el-option
                  v-for="item in deviceOptions"
                  :key="String(item.id)"
                  :label="item.label"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类型" prop="deviceType">
              <el-select
                v-model="formData.deviceType"
                disabled
                placeholder="自动带出设备类型"
                class="!w-full"
              >
                <el-option
                  v-for="dict in getStrDictOptions(DICT_TYPE.IOT_DEVICE_TYPE)"
                  :key="String(dict.value)"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障类型" prop="faultType">
              <el-select
                v-model="formData.faultType"
                clearable
                placeholder="请选择故障类型"
                class="!w-full"
              >
                <el-option
                  v-for="dict in getStrDictOptions(DICT_TYPE.IOT_DEVICE_FAULT_TYPE)"
                  :key="String(dict.value)"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="formData.orderNo" :span="12">
            <el-form-item label="工单编号">
              <el-input v-model="formData.orderNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障时间" prop="faultTime">
              <el-date-picker
                v-model="formData.faultTime"
                type="datetime"
                value-format="x"
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择故障时间"
                class="!w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上报人" prop="reporterUserId">
              <el-select
                v-model="formData.reporterUserId"
                filterable
                clearable
                placeholder="请选择上报人"
                class="!w-full"
                @change="handleReporterChange"
              >
                <el-option
                  v-for="user in userOptions"
                  :key="String(user.id)"
                  :label="user.nickname"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障现象" prop="faultSymptom">
              <el-input
                v-model="formData.faultSymptom"
                type="textarea"
                :rows="2"
                placeholder="请输入故障现象"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障图片" prop="faultImages">
              <UploadImgs v-model="formData.faultImages" :limit="6" :drag="false" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div v-if="formMode !== 'create'" class="form-section form-section--action">
      <div class="section-title">备件使用明细</div>
        <el-table
          :data="formData.spareUsages"
          border
          class="spare-usage-table"
          :header-cell-style="{ background: '#fafafa' }"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="备件" min-width="240">
            <template #default="scope">
              <el-select
                v-model="scope.row.spareId"
                filterable
                placeholder="请选择备件"
                class="!w-full"
              >
                <el-option
                  v-for="item in spareOptions"
                  :key="item.id"
                  :label="item.label"
                  :value="item.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="160">
            <template #default="scope">
              <el-input-number v-model="scope.row.qty" :min="1" class="!w-full" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" align="center">
            <template #default="scope">
              <el-button link type="danger" :disabled="isView" @click="removeSpareUsage(scope.$index)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="items-action">
          <el-button type="primary" plain :disabled="isView" @click="addSpareUsage">
            + 添加备件使用
          </el-button>
        </div>
        <div class="items-tip">提示：仅用于记录本次维修使用的备件，不会自动出库。</div>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
      <el-button v-if="!isView" type="primary" :loading="formLoading" @click="submitForm">
        确定
      </el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="auditVisible"
    title="派工"
    width="860px"
    destroy-on-close
    :close-on-click-modal="false"
    class="fault-repair-dialog fault-repair-dialog--audit"
  >
    <div v-if="detailInfo" class="form-section">
      <div class="section-title">故障信息</div>
      <div class="fault-dialog-meta">
        <div class="fault-dialog-meta__item">
          <span class="fault-dialog-meta__label">工单编号</span>
          <span class="fault-dialog-meta__value">{{ detailInfo.orderNo || '-' }}</span>
        </div>
        <div class="fault-dialog-meta__item">
          <span class="fault-dialog-meta__label">故障时间</span>
          <span class="fault-dialog-meta__value">{{ formatDateTime(detailInfo.faultTime) || '-' }}</span>
        </div>
      </div>
      <el-form :model="detailInfo" label-width="100px" label-position="left" class="fault-info-form">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="所属站点">
              <dict-tag :type="DICT_TYPE.IOT_ZD_SBZD" :value="detailStationId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备">
              <el-input v-model="detailInfo.deviceName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类型">
              <dict-tag :type="DICT_TYPE.IOT_DEVICE_TYPE" :value="detailInfo.deviceType" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障类型">
              <dict-tag :type="DICT_TYPE.IOT_DEVICE_FAULT_TYPE" :value="detailInfo.faultType" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单编号">
              <el-input v-model="detailInfo.orderNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障时间">
              <el-input :model-value="formatDateTime(detailInfo.faultTime)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上报人">
              <el-input v-model="detailInfo.reporterName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障现象">
              <el-input v-model="detailInfo.faultSymptom" type="textarea" :rows="2" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障图片">
              <UploadImgs v-model="detailInfo.faultImages" :limit="6" :drag="false" :disabled="true" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="detailInfo.remark" type="textarea" :rows="2" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <div class="form-section form-section--action">
      <div class="section-title">派工信息</div>
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="100px" class="fault-action-form">
      <el-form-item label="处理人" prop="repairUserId">
        <el-select
          v-model="auditForm.repairUserId"
          filterable
          clearable
          placeholder="请选择处理人"
          class="!w-full"
          @change="handleAuditRepairChange"
        >
          <el-option
            v-for="user in userOptions"
            :key="String(user.id)"
            :label="user.nickname"
            :value="user.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计划完成" prop="planFinishTime">
        <el-date-picker
          v-model="auditForm.planFinishTime"
          type="datetime"
          value-format="x"
          format="YYYY-MM-DD HH:mm:ss"
          placeholder="选择计划完成时间"
          class="!w-full"
        />
      </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="auditVisible = false">取消</el-button>
      <el-button type="primary" :loading="auditLoading" @click="submitAudit">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="feedbackVisible"
    title="提交结果"
    width="860px"
    destroy-on-close
    :close-on-click-modal="false"
    class="fault-repair-dialog fault-repair-dialog--feedback"
  >
    <div v-if="detailInfo" class="form-section">
      <div class="section-title">故障信息</div>
      <div class="fault-dialog-meta">
        <div class="fault-dialog-meta__item">
          <span class="fault-dialog-meta__label">工单编号</span>
          <span class="fault-dialog-meta__value">{{ detailInfo.orderNo || '-' }}</span>
        </div>
        <div class="fault-dialog-meta__item">
          <span class="fault-dialog-meta__label">故障时间</span>
          <span class="fault-dialog-meta__value">{{ formatDateTime(detailInfo.faultTime) || '-' }}</span>
        </div>
      </div>
      <el-form :model="detailInfo" label-width="100px" label-position="left" class="fault-info-form">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="所属站点">
              <dict-tag :type="DICT_TYPE.IOT_ZD_SBZD" :value="detailStationId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备">
              <el-input v-model="detailInfo.deviceName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类型">
              <dict-tag :type="DICT_TYPE.IOT_DEVICE_TYPE" :value="detailInfo.deviceType" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障类型">
              <dict-tag :type="DICT_TYPE.IOT_DEVICE_FAULT_TYPE" :value="detailInfo.faultType" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单编号">
              <el-input v-model="detailInfo.orderNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障时间">
              <el-input :model-value="formatDateTime(detailInfo.faultTime)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上报人">
              <el-input v-model="detailInfo.reporterName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障现象">
              <el-input v-model="detailInfo.faultSymptom" type="textarea" :rows="2" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障图片">
              <UploadImgs v-model="detailInfo.faultImages" :limit="6" :drag="false" :disabled="true" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="detailInfo.remark" type="textarea" :rows="2" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <div class="form-section form-section--action">
      <div class="section-title">提交结果</div>
      <el-form
        ref="feedbackFormRef"
        :model="feedbackForm"
        :rules="feedbackRules"
        label-width="100px"
        class="fault-action-form"
      >
      <el-form-item label="故障状态" prop="resolved">
        <el-checkbox v-model="feedbackForm.resolved">已解决</el-checkbox>
      </el-form-item>
      <el-form-item label="备件消耗">
        <el-table
          :data="feedbackForm.spareUsages"
          border
          class="spare-usage-table"
          :header-cell-style="{ background: '#fafafa' }"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="备件" min-width="220">
            <template #default="scope">
              <el-select
                v-model="scope.row.spareId"
                filterable
                clearable
                placeholder="请选择备件"
                class="!w-full"
                @change="handleFeedbackSpareChange(scope.row)"
              >
                <el-option
                  v-for="item in spareOptions"
                  :key="item.id"
                  :label="item.label"
                  :value="item.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="140">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.qty"
                :min="1"
                class="!w-full"
                @change="handleFeedbackQtyChange(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="库存数量" width="120" align="center">
            <template #default="scope">
              <span>{{ formatStockQty(scope.row.stockQty) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="扣减后库存" width="140" align="center">
            <template #default="scope">
              <span :class="{ 'stock-danger': isStockNotEnough(scope.row) }">
                {{ formatStockQty(getRemainingStock(scope.row)) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" align="center">
            <template #default="scope">
              <el-button
                link
                type="danger"
                :disabled="feedbackForm.spareUsages.length === 1"
                @click="removeFeedbackSpareUsage(scope.$index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="items-action">
          <el-button type="primary" plain @click="addFeedbackSpareUsage">
            + 添加备件消耗
          </el-button>
        </div>
      </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="feedbackVisible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="feedbackLoading"
        :disabled="!feedbackForm.resolved"
        @click="submitFeedback"
      >
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import download from '@/utils/download'
import { UploadImgs } from '@/components/UploadFile'
import { DeviceApi } from '@/api/iot/device/device'
import { SpareApi } from '@/api/iot/spare'
import { dateFormatter, formatDate } from '@/utils/formatTime'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import * as UserApi from '@/api/system/user'
import {
  FaultRepairApi,
  type FaultRepairVO,
  type FaultRepairPageReqVO,
  type FaultRepairAuditAssignReqVO,
  type FaultRepairResultReqVO,
  type FaultRepairSpareUsageVO
} from '@/api/iot/fault-repair'
import { checkPermi } from '@/utils/permission'
import { useUserStore } from '@/store/modules/user'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'EquipmentFaultRepair' })

const message = useMessage()
const { t } = useI18n()
const canQueryPermi = checkPermi(['iot:fault-repair:query'])
const canCreatePermi = checkPermi(['iot:fault-repair:create'])
const canUpdatePermi = checkPermi(['iot:fault-repair:update'])
const canAuditPermi = checkPermi(['iot:fault-repair:audit'])
const canDeletePermi = checkPermi(['iot:fault-repair:delete'])
const userStore = useUserStore()
const hasAdminRole = computed(() => {
  const roleSet = new Set(userStore.getRoles || [])
  return roleSet.has('super_admin') || roleSet.has('tenant_admin') || roleSet.has('crm_admin')
})
const canCreateAction = computed(() => hasAdminRole.value && canCreatePermi)
const canDeleteAction = computed(() => hasAdminRole.value && canDeletePermi)

const loading = ref(true)
const list = ref<FaultRepairVO[]>([])
const total = ref(0)
const exportLoading = ref(false)
const queryFormRef = ref<FormInstance>()

const detailInfo = ref<FaultRepairVO>()
const detailStationId = computed(() => {
  const deviceId = detailInfo.value?.deviceId
  if (deviceId === undefined || deviceId === null) {
    return undefined
  }
  const matched = allDeviceOptions.value.find((item) => item.id === deviceId)
  return matched?.stationId
})

const normalizeUserId = (value?: number) => {
  if (!value || value <= 0) {
    return undefined
  }
  return value
}

const formatDateTime = (value?: number | string) => {
  if (value === undefined || value === null || value === '') {
    return ''
  }
  return formatDate(value)
}
const normalizeStatus = (value?: string) => {
  if (value === 'assigned') {
    return 'processing'
  }
  return value || ''
}

const queryParams = reactive<FaultRepairPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  deviceName: undefined,
  deviceType: undefined,
  reporterName: undefined,
  repairName: undefined,
  status: undefined,
  faultTime: []
})

const getList = async () => {
  loading.value = true
  try {
    const data = await FaultRepairApi.getFaultRepairPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await FaultRepairApi.exportFaultRepairExcel(queryParams)
    download.excel(data, '故障维修记录.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await FaultRepairApi.deleteFaultRepair(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const canAudit = (row: FaultRepairVO) => row.status === 'pending'
const isCompleted = (row: FaultRepairVO) => row.status === 'completed'
const canFeedback = (row: FaultRepairVO) => row.status === 'processing' || row.status === 'assigned'
const canOpenEditOrView = (row: FaultRepairVO) =>
  isCompleted(row) ? canQueryPermi : canUpdatePermi

const formVisible = ref(false)
const formTitle = ref('')
const formMode = ref<'create' | 'update' | 'view'>('create')
const isView = computed(() => formMode.value === 'view')
const formLoading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive<FaultRepairVO>({
  id: undefined,
  orderNo: '',
  deviceId: undefined,
  deviceName: '',
  deviceType: '',
  faultType: undefined,
  faultTime: undefined,
  faultSymptom: '',
  reporterUserId: undefined,
  reporterName: '',
  repairUserId: undefined,
  repairName: '',
  status: 'pending',
  finishTime: undefined,
  planFinishTime: undefined,
  faultImages: [],
  remark: '',
  spareUsages: []
})

const formRules: FormRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  deviceType: [{ required: true, message: '请输入设备类型', trigger: 'blur' }],
  faultType: [{ required: true, message: '请选择故障类型', trigger: 'change' }],
  faultTime: [{ required: true, message: '请选择故障时间', trigger: 'change' }],
  faultSymptom: [{ required: true, message: '请输入故障现象', trigger: 'blur' }],
  reporterUserId: [{ required: true, message: '请选择上报人', trigger: 'change' }],
  faultImages: [{ required: true, message: '请上传故障图片', trigger: 'change' }]
}

const selectedStationId = ref<string | undefined>(undefined)
const allDeviceOptions = ref<{ id: number | string; label: string; deviceType?: string; stationId?: string }[]>([])
const deviceOptions = computed(() => {
  if (!selectedStationId.value) {
    return allDeviceOptions.value
  }
  return allDeviceOptions.value.filter(
    (item) => String(item.stationId) === String(selectedStationId.value)
  )
})
const spareOptions = ref<{ id: number; label: string }[]>([])
const userOptions = ref<UserApi.UserVO[]>([])

interface SpareStockInfo {
  stockQty: number
}

const spareStockMap = ref<Record<string, SpareStockInfo>>({})

const loadSpareStockInfo = async (spareId?: number) => {
  if (!spareId && spareId !== 0) {
    return undefined
  }
  const key = String(spareId)
  if (Object.prototype.hasOwnProperty.call(spareStockMap.value, key)) {
    return spareStockMap.value[key]
  }
  const data = await SpareApi.getSpare(spareId)
  const info: SpareStockInfo = {
    stockQty: data?.stockQty ?? 0
  }
  spareStockMap.value[key] = info
  return info
}

const formatStockQty = (stockQty?: number) => {
  if (stockQty === undefined || stockQty === null) {
    return '-'
  }
  return stockQty
}

const loadDeviceOptions = async () => {
  const data = await DeviceApi.getSimpleDeviceList()
  allDeviceOptions.value = (data || []).map((item: any) => ({
    id: item.id,
    label: item.nickname || item.deviceName || item.serialNumber || String(item.id),
    deviceType: item.deviceType !== undefined && item.deviceType !== null ? String(item.deviceType) : '',
    stationId: item.stationId ? String(item.stationId) : undefined
  }))
}

const loadSpareOptions = async () => {
  const data = await SpareApi.getSpareSimpleList()
  spareOptions.value = (data || []).map((item: any) => ({
    id: item.id,
    label: `${item.spareName || ''} ${item.spareSpec || ''} ${item.spareModel || ''}`.trim()
  }))
}

const loadUserOptions = async () => {
  userOptions.value = await UserApi.getSimpleUserList({ excludeRoleName: '游客' })
}

const getUserNickname = (userId?: number) => {
  if (!userId && userId !== 0) {
    return ''
  }
  const matched = userOptions.value.find((user) => user.id === userId)
  return matched?.nickname || ''
}

const handleDeviceChange = (value: number | string) => {
  const selected = allDeviceOptions.value.find((item) => item.id === value)
  if (!selected) {
    return
  }
  formData.deviceName = selected.label
  formData.deviceType = selected.deviceType || ''
  if (selected.stationId && selectedStationId.value !== selected.stationId) {
    selectedStationId.value = selected.stationId
  }
}

const handleReporterChange = (value?: number) => {
  formData.reporterUserId = value
  formData.reporterName = getUserNickname(value)
}

const handleStationChange = (value?: string) => {
  selectedStationId.value = value
  if (!value) {
    return
  }
  if (formData.deviceId) {
    const matched = deviceOptions.value.find((item) => item.id === formData.deviceId)
    if (!matched) {
      formData.deviceId = undefined
      formData.deviceName = ''
      formData.deviceType = ''
    }
  }
}

const syncStationByDevice = (deviceId?: number | string) => {
  if (!deviceId) {
    selectedStationId.value = undefined
    return
  }
  const matched = allDeviceOptions.value.find((item) => item.id === deviceId)
  selectedStationId.value = matched?.stationId
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    orderNo: '',
    deviceId: undefined,
    deviceName: '',
    deviceType: '',
    faultType: undefined,
    faultTime: undefined,
    faultSymptom: '',
    reporterUserId: undefined,
    reporterName: '',
    repairUserId: undefined,
    repairName: '',
    status: 'pending',
    finishTime: undefined,
    planFinishTime: undefined,
    faultImages: [],
    remark: '',
    spareUsages: []
  })
  formRef.value?.clearValidate()
}

const openForm = async (type: 'create' | 'update' | 'view', id?: number) => {
  formMode.value = type
  if (type === 'create') {
    formTitle.value = '新增故障维修'
  } else if (type === 'view') {
    formTitle.value = '故障维修详情'
  } else {
    formTitle.value = '编辑故障维修'
  }
  formVisible.value = true
  resetForm()
  selectedStationId.value = undefined
  if ((type === 'update' || type === 'view') && id) {
    const data = await FaultRepairApi.getFaultRepair(id)
    Object.assign(formData, {
      id: data.id,
      orderNo: data.orderNo || '',
      deviceId: data.deviceId,
      deviceName: data.deviceName || '',
      deviceType: data.deviceType || '',
      faultType: data.faultType ?? undefined,
      faultTime: data.faultTime ?? undefined,
      faultSymptom: data.faultSymptom || '',
      reporterUserId: data.reporterUserId,
      reporterName: data.reporterName || '',
      repairUserId: data.repairUserId,
      repairName: data.repairName || '',
      status: data.status || 'pending',
      finishTime: data.finishTime ?? undefined,
      planFinishTime: data.planFinishTime ?? undefined,
      faultImages: data.faultImages || [],
      remark: data.remark || '',
      spareUsages: (data.spareUsages || []) as FaultRepairSpareUsageVO[]
    })
    syncStationByDevice(data.deviceId)
  }
}

const addSpareUsage = () => {
  formData.spareUsages = formData.spareUsages || []
  formData.spareUsages.push({ spareId: undefined, qty: 1 })
}

const removeSpareUsage = (index: number) => {
  if (!formData.spareUsages) {
    return
  }
  formData.spareUsages.splice(index, 1)
}

const normalizeSpareUsages = () => {
  return (formData.spareUsages || []).filter((item) => item.spareId && item.qty)
}

const validateSpareUsages = () => {
  if (!formData.spareUsages || !formData.spareUsages.length) {
    return true
  }
  for (let i = 0; i < formData.spareUsages.length; i++) {
    const item = formData.spareUsages[i]
    if (!item.spareId) {
      message.error(`第 ${i + 1} 行请选择备件`)
      return false
    }
    if (!item.qty || item.qty < 1) {
      message.error(`第 ${i + 1} 行请选择备件`)
      return false
    }
  }
  return true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }
  if (!validateSpareUsages()) {
    return
  }
  formLoading.value = true
  try {
    if (!formData.reporterName && formData.reporterUserId) {
      formData.reporterName = getUserNickname(formData.reporterUserId)
    }
    const payload = {
      ...formData,
      spareUsages: normalizeSpareUsages()
    }
    if (formMode.value === 'create') {
      await FaultRepairApi.createFaultRepair(payload)
      message.success(t('common.createSuccess'))
    } else {
      await FaultRepairApi.updateFaultRepair(payload)
      message.success(t('common.updateSuccess'))
    }
    formVisible.value = false
    await getList()
  } finally {
    formLoading.value = false
  }
}

const auditVisible = ref(false)
const auditLoading = ref(false)
const auditFormRef = ref<FormInstance>()
const auditForm = reactive<FaultRepairAuditAssignReqVO>({
  id: 0,
  repairUserId: undefined,
  repairName: '',
  planFinishTime: undefined,
  remark: ''
})
const auditRules: FormRules = {
  repairUserId: [
    {
      required: true,
      trigger: 'change',
      validator: (_rule, value, callback) => {
        if (value) {
          callback()
          return
        }
        callback(new Error('请选择处理人'))
      }
    }
  ],
  planFinishTime: [{ required: true, message: '请选择计划完成时间', trigger: 'change' }]
}

const handleAuditRepairChange = (value?: number) => {
  auditForm.repairUserId = value ?? undefined
  auditForm.repairName = getUserNickname(value)
}

const openAudit = async (row: FaultRepairVO) => {
  auditForm.id = row.id || 0
  auditForm.repairUserId = normalizeUserId(row.repairUserId)
  auditForm.repairName = row.repairName || ''
  auditForm.planFinishTime = row.planFinishTime ?? undefined
  auditForm.remark = ''
  detailInfo.value = row.id ? await FaultRepairApi.getFaultRepair(row.id) : undefined
  auditVisible.value = true
  auditFormRef.value?.clearValidate()
}

const submitAudit = async () => {
  const valid = await auditFormRef.value?.validate()
  if (!valid) {
    return
  }
  auditLoading.value = true
  try {
    if (!auditForm.repairName) {
      auditForm.repairName = getUserNickname(auditForm.repairUserId)
    }
    await FaultRepairApi.auditAssignFaultRepair(auditForm)
    message.success('派工成功')
    auditVisible.value = false
    await getList()
  } finally {
    auditLoading.value = false
  }
}

const feedbackVisible = ref(false)
const feedbackLoading = ref(false)
const feedbackFormRef = ref<FormInstance>()
interface FeedbackSpareUsage extends FaultRepairSpareUsageVO {
  stockQty?: number
}

const feedbackForm = reactive<
  FaultRepairResultReqVO & {
    resolved: boolean
    spareUsages: FeedbackSpareUsage[]
  }
>({
  id: 0,
  finishTime: undefined,
  status: 'completed',
  spareUsages: [{ spareId: undefined, qty: 1, stockQty: undefined }],
  resolved: false
})

const feedbackRules: FormRules = {
  resolved: [
    {
      trigger: 'change',
      validator: (_rule, value, callback) => {
        if (value) {
          callback()
          return
        }
        callback(new Error('请勾选已解决'))
      }
    }
  ]
}

const addFeedbackSpareUsage = () => {
  feedbackForm.spareUsages.push({ spareId: undefined, qty: 1, stockQty: undefined })
}

const removeFeedbackSpareUsage = (index: number) => {
  if (feedbackForm.spareUsages.length <= 1) {
    return
  }
  feedbackForm.spareUsages.splice(index, 1)
}

const handleFeedbackSpareChange = async (row: FeedbackSpareUsage) => {
  if (!row?.spareId) {
    row.stockQty = undefined
    return
  }
  const info = await loadSpareStockInfo(row.spareId)
  row.stockQty = info?.stockQty
  if (row.qty && row.stockQty !== undefined && row.qty > row.stockQty) {
    message.warning(`备件库存不足，当前库存：${row.stockQty}`)
  }
}

const handleFeedbackQtyChange = (row: FeedbackSpareUsage) => {
  if (row?.spareId && row?.stockQty !== undefined && row?.qty && row.qty > row.stockQty) {
    message.warning(`备件库存不足，当前库存：${row.stockQty}`)
  }
}

const getRemainingStock = (row: FeedbackSpareUsage) => {
  if (row?.stockQty === undefined || row?.stockQty === null) {
    return undefined
  }
  if (row?.qty === undefined || row?.qty === null) {
    return row.stockQty
  }
  return row.stockQty - row.qty
}

const isStockNotEnough = (row: FeedbackSpareUsage) => {
  if (row?.stockQty === undefined || row?.stockQty === null || row?.qty === undefined || row?.qty === null) {
    return false
  }
  return row.qty > row.stockQty
}

const normalizeFeedbackSpareUsages = () => {
  return (feedbackForm.spareUsages || [])
    .filter((item) => item?.spareId && item?.qty)
    .map((item) => ({ spareId: item.spareId, qty: item.qty }))
}

const validateFeedbackSpareUsages = async () => {
  if (!feedbackForm.spareUsages || !feedbackForm.spareUsages.length) {
    return true
  }
  for (let i = 0; i < feedbackForm.spareUsages.length; i++) {
    const item = feedbackForm.spareUsages[i]
    if (!item?.spareId) {
      continue
    }
    if (!item.qty || item.qty < 1) {
      message.error(`第 ${i + 1} 行请输入备件数量`)
      return false
    }
    if (item.stockQty === undefined) {
      const info = await loadSpareStockInfo(item.spareId)
      item.stockQty = info?.stockQty
    }
    if (item.stockQty !== undefined && item.qty > item.stockQty) {
      message.error(`第 ${i + 1} 行备件库存不足（当前库存：${item.stockQty}）`)
      return false
    }
  }
  return true
}

const openFeedback = async (row: FaultRepairVO) => {
  feedbackForm.id = row.id || 0
  feedbackForm.finishTime = undefined
  feedbackForm.status = 'completed'
  feedbackForm.resolved = false
  feedbackForm.spareUsages = [{ spareId: undefined, qty: 1, stockQty: undefined }]
  if (row.id) {
    const data = await FaultRepairApi.getFaultRepair(row.id)
    detailInfo.value = data
    if (data.spareUsages && data.spareUsages.length) {
      feedbackForm.spareUsages = data.spareUsages.map((item) => ({
        spareId: item.spareId,
        qty: item.qty,
        stockQty: undefined
      }))
    }
  }
  for (const usage of feedbackForm.spareUsages) {
    if (usage?.spareId) {
      const info = await loadSpareStockInfo(usage.spareId)
      usage.stockQty = info?.stockQty
    }
  }
  feedbackVisible.value = true
  feedbackFormRef.value?.clearValidate()
}

const submitFeedback = async () => {
  const valid = await feedbackFormRef.value?.validate()
  if (!valid) {
    return
  }
  if (!feedbackForm.resolved) {
    message.error('请先勾选已解决')
    return
  }
  if (!(await validateFeedbackSpareUsages())) {
    return
  }
  feedbackLoading.value = true
  try {
    feedbackForm.status = 'completed'
    feedbackForm.finishTime = Date.now()
    await FaultRepairApi.submitFaultRepairResult({
      id: feedbackForm.id,
      finishTime: feedbackForm.finishTime,
      status: feedbackForm.status,
      spareUsages: normalizeFeedbackSpareUsages()
    })
    message.success('反馈成功')
    feedbackVisible.value = false
    await getList()
  } finally {
    feedbackLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadDeviceOptions(), loadSpareOptions(), loadUserOptions()])
  await getList()
})
</script>

<style scoped>
.fault-repair-dialog :deep(.el-dialog) {
  width: min(980px, calc(100vw - 24px)) !important;
  border: 1px solid #d5e0ee;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f8fd;
  box-shadow: 0 14px 34px rgba(15, 35, 63, 0.18);
}

.fault-repair-dialog--audit :deep(.el-dialog),
.fault-repair-dialog--feedback :deep(.el-dialog) {
  width: min(860px, calc(100vw - 24px)) !important;
}

.fault-repair-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 14px 18px;
  border-bottom: 1px solid #dbe6f2;
  background: linear-gradient(90deg, #edf3fb 0%, #f8fbff 100%);
}

.fault-repair-dialog :deep(.el-dialog__title) {
  color: #102a43;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.fault-repair-dialog :deep(.el-dialog__body) {
  padding: 14px 18px 10px;
}

.fault-repair-dialog :deep(.el-dialog__footer) {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 18px 14px;
  border-top: 1px solid #dbe6f2;
  background: #fff;
}

.fault-repair-dialog :deep(.el-dialog__footer .el-button + .el-button) {
  margin-left: 0;
}

.fault-repair-dialog :deep(.el-dialog__footer .el-button) {
  min-width: 92px;
  border-radius: 8px;
  font-weight: 600;
}

.fault-repair-dialog :deep(.el-button--primary) {
  border-color: #0b69b3;
  background: #0b69b3;
}

.fault-repair-dialog :deep(.el-button--primary:hover) {
  border-color: #0f7ed4;
  background: #0f7ed4;
}

.fault-repair-dialog :deep(.el-button.is-link.is-disabled) {
  color: #a8b3c2 !important;
  cursor: not-allowed;
}

.fault-repair-dialog :deep(.el-form-item__label) {
  color: #334155;
  font-weight: 600;
}

.fault-repair-dialog :deep(.el-input__wrapper),
.fault-repair-dialog :deep(.el-select__wrapper),
.fault-repair-dialog :deep(.el-date-editor.el-input__wrapper),
.fault-repair-dialog :deep(.el-input-number .el-input__wrapper) {
  min-height: 34px;
  border: 1px solid #c8d7ea;
  border-radius: 8px;
  box-shadow: none;
}

.fault-repair-dialog :deep(.el-textarea__inner) {
  border: 1px solid #c8d7ea;
  border-radius: 8px;
  box-shadow: none;
}

.fault-repair-dialog :deep(.el-input__wrapper.is-focus),
.fault-repair-dialog :deep(.el-select__wrapper.is-focused),
.fault-repair-dialog :deep(.el-textarea__inner:focus),
.fault-repair-dialog :deep(.el-date-editor.el-input__wrapper.is-focus) {
  border-color: #0b69b3;
  box-shadow: 0 0 0 3px rgba(11, 105, 179, 0.12);
}

.fault-repair-dialog :deep(.is-disabled .el-input__wrapper),
.fault-repair-dialog :deep(.is-disabled .el-textarea__inner),
.fault-repair-dialog :deep(.el-select__wrapper.is-disabled) {
  color: #475569;
  border-color: #d8e2ef;
  background: #f6f8fb;
}

.fault-repair-dialog :deep(.el-form-item__content) {
  align-items: center;
}

.fault-main-form,
.fault-action-form {
  width: 100%;
}

.fault-action-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.form-section {
  margin-bottom: 12px;
  padding: 14px 16px 6px;
  border: 1px solid #d9e3ef;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 3px 10px rgba(15, 35, 63, 0.06);
}

.form-section--action {
  border-left: 4px solid #0b69b3;
}

.section-title {
  position: relative;
  margin-bottom: 12px;
  padding-left: 12px;
  color: #1f3a5a;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.2;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 3px;
  width: 4px;
  height: 14px;
  border-radius: 999px;
  background: #0b69b3;
}

.fault-dialog-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  margin-bottom: 10px;
  padding: 10px 12px;
  border: 1px solid #dae5f3;
  border-radius: 8px;
  background: #f7faff;
}

.fault-dialog-meta__item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.fault-dialog-meta__label {
  color: #5b6b7d;
  font-size: 12px;
}

.fault-dialog-meta__value {
  color: #102a43;
  font-size: 13px;
  font-weight: 600;
}

.fault-info-form :deep(.el-form-item) {
  margin-bottom: 10px;
}

.fault-info-form :deep(.el-input.is-disabled .el-input__wrapper) {
  border-color: #d6e0ec;
  background: #f8fafc;
}

.spare-usage-table {
  margin-bottom: 10px;
}

.spare-usage-table :deep(.el-table__header-wrapper th.el-table__cell) {
  color: #20364d;
  font-weight: 700;
  border-bottom-color: #d5e2f1;
  background: #ecf2f9 !important;
}

.spare-usage-table :deep(.el-table__row td.el-table__cell) {
  background: #fff;
}

.spare-usage-table :deep(.el-table__cell .cell) {
  white-space: normal;
  word-break: break-word;
  line-height: 1.45;
}

.spare-usage-table :deep(.el-table--border::before),
.spare-usage-table :deep(.el-table--border::after),
.spare-usage-table :deep(.el-table__inner-wrapper::before) {
  background-color: #d5e2f1;
}

.items-action {
  display: flex;
  justify-content: flex-start;
  margin-top: 8px;
}

.items-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.fault-action-form :deep(.el-checkbox__label) {
  color: #1f3a5a;
  font-weight: 600;
}

.stock-danger {
  color: #dc2626;
  font-weight: 700;
}

@media (max-width: 768px) {
  .fault-repair-dialog :deep(.el-dialog) {
    width: calc(100vw - 16px) !important;
    margin-top: 3vh;
  }

  .fault-repair-dialog :deep(.el-dialog__header),
  .fault-repair-dialog :deep(.el-dialog__body),
  .fault-repair-dialog :deep(.el-dialog__footer) {
    padding-left: 12px;
    padding-right: 12px;
  }

  .fault-repair-dialog :deep(.el-col-12) {
    max-width: 100%;
    flex: 0 0 100%;
  }

  .form-section {
    padding: 12px 12px 4px;
  }

  .fault-dialog-meta {
    padding: 8px 10px;
  }

  .fault-dialog-meta__item {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
