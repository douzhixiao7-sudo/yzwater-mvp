<template>
  <div class="inspection-task-page">
    <ContentWrap>
      <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input
            v-model="queryParams.taskName"
            clearable
            placeholder="请输入任务名称"
            class="!w-180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="巡检类型" prop="inspectionType">
          <el-select
            v-model="queryParams.inspectionType"
            clearable
            filterable
            placeholder="所有巡检类型"
            class="!w-180px"
          >
            <el-option
              v-for="option in inspectionTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务状态" prop="taskStatus">
          <el-select
            v-model="queryParams.taskStatus"
            clearable
            filterable
            placeholder="所有状态"
            class="!w-160px"
          >
            <el-option
              v-for="option in taskStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="流程状态" prop="workflowStatus">
          <el-select
            v-model="queryParams.workflowStatus"
            clearable
            filterable
            placeholder="所有流程状态"
            class="!w-170px"
          >
            <el-option
              v-for="option in workflowStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务来源" prop="sourceType">
          <el-select
            v-model="queryParams.sourceType"
            clearable
            filterable
            placeholder="所有来源"
            class="!w-160px"
          >
            <el-option
              v-for="option in sourceTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="计划时间" prop="planTimeRange">
          <el-date-picker
            v-model="queryParams.planTimeRange"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="datetimerange"
            start-placeholder="计划时间（起）"
            end-placeholder="计划时间（止）"
            class="!w-360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
          <el-button
            v-hasPermi="['iot:inspection-task:create']"
            type="primary"
            class="create-btn"
            @click="openCreateForm"
          >
            <Icon icon="ep:plus" class="mr-5px" />
            新建巡检任务
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="任务编号" prop="taskNo" min-width="150" />
        <el-table-column label="任务名称" prop="taskName" min-width="180" show-overflow-tooltip />
        <el-table-column label="巡检类型" prop="inspectionType" min-width="120">
          <template #default="{ row }">
            <el-tag effect="light" type="primary">
              {{ getInspectionTypeLabel(row.inspectionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="巡检线路" prop="lineName" min-width="150">
          <template #default="{ row }">{{ row.lineName || '-' }}</template>
        </el-table-column>
        <el-table-column label="计划开始" prop="planStartTime" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.planStartTime) }}</template>
        </el-table-column>
        <el-table-column label="计划完成" prop="planEndTime" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.planEndTime) }}</template>
        </el-table-column>
        <el-table-column label="执行人" prop="executorName" min-width="120" />
        <el-table-column label="状态" prop="taskStatus" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTaskStatusTagType(row.taskStatus)" effect="plain">
              {{ getTaskStatusLabel(row.taskStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="流程状态" prop="workflowStatus" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getWorkflowStatusTagType(row.workflowStatus)" effect="plain">
              {{ getWorkflowStatusLabel(row.workflowStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="异常点数" prop="abnormalCount" width="100" align="center" />
        <el-table-column label="来源" prop="sourceType" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.sourceType === 1 ? 'success' : 'info'" effect="plain">
              {{ getSourceTypeLabel(row.sourceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <div class="task-action-group">
              <el-button type="primary" link @click="openDetail(row.id)">详情</el-button>
              <el-button
                v-hasPermi="['iot:inspection-task:submit']"
                type="success"
                link
                :disabled="!canSubmitResult(row)"
                @click="openSubmitDialog(row)"
              >
                提交结果
              </el-button>
              <el-button
                v-hasPermi="['iot:inspection-task:update']"
                v-if="isTaskManagerRole"
                type="warning"
                link
                :disabled="!canOperateTask(row)"
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                v-hasPermi="['iot:inspection-task:delete']"
                v-if="isTaskManagerRole"
                type="danger"
                link
                :disabled="!canOperateTask(row)"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
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

    <Dialog v-model="formVisible" :title="formDialogTitle" width="920px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="108px" v-loading="formLoading">
        <section class="form-section">
          <div class="section-title">
            <Icon icon="ep:document-checked" />
            <span>巡检任务信息</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="所属闸站" prop="stationId">
                <el-select v-model="formData.stationId" clearable filterable placeholder="请选择所属闸站" @change="handleStationChange">
                  <el-option
                    v-for="option in stationOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="String(option.value)"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="任务名称" prop="taskName">
                <el-input v-model="formData.taskName" maxlength="100" show-word-limit placeholder="请输入任务名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="巡检类型" prop="inspectionType">
                <el-select v-model="formData.inspectionType" clearable filterable placeholder="请选择巡检类型" @change="handleInspectionTypeChange">
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
              <el-form-item label="巡检标准" prop="standardId">
                <el-select
                  v-model="formData.standardId"
                  clearable
                  filterable
                  remote
                  reserve-keyword
                  placeholder="请选择巡检标准"
                  :loading="standardLoading"
                  :remote-method="handleStandardRemoteSearch"
                >
                  <el-option v-for="option in standardOptions" :key="option.id" :label="option.name" :value="option.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="巡检线路" prop="lineId">
                <div class="line-select-wrap">
                  <el-select
                    v-model="formData.lineId"
                    clearable
                    filterable
                    remote
                    reserve-keyword
                    placeholder="请选择巡检线路"
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
            <el-col :span="12">
              <el-form-item label="执行人" prop="executorUserId">
                <el-select
                  v-model="formData.executorUserId"
                  filterable
                  clearable
                  placeholder="请选择执行人"
                  @change="handleExecutorChange"
                >
                  <el-option v-for="option in userOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划开始" prop="planStartTime">
                <el-date-picker
                  v-model="formData.planStartTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择计划开始时间"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划完成" prop="planEndTime">
                <el-date-picker
                  v-model="formData.planEndTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择计划完成时间"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="巡检对象" prop="targetIds" class="target-form-item">
                <div class="target-picker-panel">
                  <div class="target-picker-head">
                    <div class="target-type-switch">
                      <span class="target-switch-label">巡检对象：</span>
                      <el-radio-group v-model="formData.objectType" @change="handleObjectTypeChange">
                        <el-radio v-for="option in objectTypeOptions" :key="option.value" :value="option.value">
                          {{ option.label }}
                        </el-radio>
                      </el-radio-group>
                    </div>
                    <el-button :loading="targetLoading" class="target-select-btn" @click="openTargetPicker">
                      <Icon icon="ep:plus" class="mr-4px" />
                      {{ targetSelectButtonText }}
                    </el-button>
                  </div>
                  <div class="target-picker-body">
                    <p class="target-count-text">已选择 {{ formData.targetIds.length }} 个{{ targetTypeLabel }}</p>
                    <div v-if="formData.targetIds.length > 0" class="target-chip-list">
                      <el-tag
                        v-for="targetId in formData.targetIds"
                        :key="targetId"
                        closable
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
            <el-col :span="24">
              <el-form-item label="任务描述" prop="taskDesc">
                <el-input v-model="formData.taskDesc" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入任务描述" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>
      </el-form>

      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">{{ formSubmitButtonText }}</el-button>
      </template>
    </Dialog>

    <Dialog v-model="detailVisible" title="巡检任务详情" width="1120px" class="inspection-task-detail-dialog">
      <el-skeleton v-if="detailLoading" :rows="8" animated />
      <el-row v-else :gutter="16" class="task-detail-layout">
        <el-col :span="24">
          <el-descriptions :column="2" border>
                <el-descriptions-item label="任务编号">{{ detailData?.taskNo || '-' }}</el-descriptions-item>
                <el-descriptions-item label="任务名称">{{ detailData?.taskName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="所属闸站">{{ getStationLabel(detailData?.stationId) }}</el-descriptions-item>
                <el-descriptions-item label="巡检类型">
                  {{ getInspectionTypeLabel(detailData?.inspectionType) }}
                </el-descriptions-item>
                <el-descriptions-item label="巡检标准">{{ detailData?.standardName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="巡检线路">{{ detailData?.lineName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="计划开始">
                  {{ formatDateTime(detailData?.planStartTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="计划完成">
                  {{ formatDateTime(detailData?.planEndTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="执行人">{{ detailData?.executorName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="任务状态">
                  <el-tag :type="getTaskStatusTagType(detailData?.taskStatus)" effect="plain">
                    {{ getTaskStatusLabel(detailData?.taskStatus) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="异常点数">
                  <span class="abnormal-ratio">{{ detailAbnormalCountDisplay }}</span>
                  <span v-if="detailHasSubmitResult" class="abnormal-ratio-tip">（前者为不合格项数量，后者为检查项目总数）</span>
                </el-descriptions-item>
                <el-descriptions-item label="任务来源">
                  {{ getSourceTypeLabel(detailData?.sourceType) }}
                </el-descriptions-item>
                <el-descriptions-item label="巡检对象" :span="2">
                  <div class="target-tags">
                    <el-tag
                      v-for="(name, index) in detailData?.targetNames || []"
                      :key="`${name}_${index}`"
                      class="target-tag"
                      effect="light"
                      type="info"
                    >
                      {{ name }}
                    </el-tag>
                    <span v-if="!detailData?.targetNames?.length">-</span>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="任务描述" :span="2">
                  {{ detailData?.taskDesc || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="提交结果" :span="2">
                  <div class="detail-submit-wrap" v-loading="detailSubmitResultLoading">
                    <div class="detail-submit-head">
                      <div class="detail-submit-stat">
                        <span class="detail-submit-stat__label">异常点数：</span>
                        <span class="detail-submit-stat__value">{{ detailAbnormalCountDisplay }}</span>
                      </div>
                      <div class="detail-submit-stat detail-submit-stat--time">
                        <span class="detail-submit-stat__label">提交时间：</span>
                        <span class="detail-submit-stat__value detail-submit-stat__value--time">
                          {{ formatDateTime(detailData?.submitTime) }}
                        </span>
                      </div>
                      <div class="detail-submit-stat detail-submit-stat--remark">
                        <span class="detail-submit-stat__label">提交说明：</span>
                        <span class="detail-submit-stat__value">{{ detailData?.remark || '-' }}</span>
                      </div>
                    </div>
                    <el-empty
                      v-if="!detailSubmitResultLoading && !detailSubmitResultItems.length"
                      description="暂无提交结果明细"
                    />
                    <el-collapse v-else v-model="detailSubmitActivePanels" class="detail-submit-collapse">
                      <el-collapse-item
                        v-for="(item, index) in detailSubmitResultItems"
                        :key="item.panelName"
                        :name="item.panelName"
                      >
                        <template #title>
                          <div class="detail-submit-collapse-title">
                            <span class="detail-submit-collapse-title__index">{{ index + 1 }}</span>
                            <span class="detail-submit-collapse-title__name">{{ item.itemName || '-' }}</span>
                            <el-tag
                              :type="getCheckResultTagType(item.checkResult)"
                              size="small"
                              effect="light"
                            >
                              {{ getCheckResultLabel(item) }}
                            </el-tag>
                          </div>
                        </template>
                        <div class="detail-submit-item-body">
                          <div class="detail-submit-item-line">
                            <span class="detail-submit-item-line__label">检查结果等级：</span>
                            <el-tag :type="getCheckResultTagType(item.checkResult)" size="small" effect="light">
                              {{ getCheckResultLabel(item) }}
                            </el-tag>
                          </div>
                          <div class="detail-submit-item-line">
                            <span class="detail-submit-item-line__label">检查备注：</span>
                            <span class="detail-submit-item-line__value">{{ item.checkRemark || '-' }}</span>
                          </div>
                          <div class="detail-submit-item-line detail-submit-item-line--attachment">
                            <span class="detail-submit-item-line__label">上传附件：</span>
                            <div class="detail-submit-attachment-list">
                              <el-link
                                v-for="(attachment, attachmentIndex) in item.attachments || []"
                                :key="`${item.panelName}_attachment_${attachmentIndex}`"
                                :href="attachment"
                                target="_blank"
                                type="primary"
                                class="detail-submit-attachment-link"
                              >
                                {{ getAttachmentDisplayName(attachment, attachmentIndex) }}
                              </el-link>
                              <span
                                v-if="!(item.attachments && item.attachments.length)"
                                class="detail-submit-item-line__value"
                              >
                                -
                              </span>
                            </div>
                          </div>
                          <div class="detail-submit-record-list">
                            <div
                              v-for="(record, recordIndex) in item.records"
                              :key="`${item.panelName}_record_${recordIndex}`"
                              class="detail-submit-record-item"
                            >
                              <span class="detail-submit-record-item__cell">
                                <span class="detail-submit-record-item__field">属性名称：</span>
                                <span class="detail-submit-record-item__name">{{ record.attrName || '-' }}</span>
                              </span>
                              <span class="detail-submit-record-item__cell">
                                <span class="detail-submit-record-item__field">实际测量值：</span>
                                <span class="detail-submit-record-item__value">{{ record.actualValue || '-' }}</span>
                              </span>
                              <span class="detail-submit-record-item__cell">
                                <span class="detail-submit-record-item__field">单位：</span>
                                <span class="detail-submit-record-item__unit">{{ record.attrUnit || '-' }}</span>
                              </span>
                              <span class="detail-submit-record-item__cell">
                                <span class="detail-submit-record-item__field">标准值：</span>
                                <span class="detail-submit-record-item__standard">{{ record.standardValue || '-' }}</span>
                              </span>
                            </div>
                          </div>
                        </div>
                      </el-collapse-item>
                    </el-collapse>
                  </div>
                </el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </Dialog>

    <Dialog v-model="submitResultVisible" title="提交巡检结果" width="1120px" class="submit-result-dialog">
      <div class="submit-result-wrapper" v-loading="submitResultLoading">
        <div class="submit-result-summary">
          <div class="summary-grid">
            <div class="summary-item">
              <span class="summary-label">任务名称</span>
              <span class="summary-value">{{ submitResultTaskSummary.taskName || '-' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">巡检类型</span>
              <span class="summary-value">{{ submitResultTaskSummary.inspectionTypeName || '-' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">执行人</span>
              <span class="summary-value">{{ submitResultTaskSummary.executorName || '-' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">计划时间</span>
              <span class="summary-value">{{ submitResultTaskSummary.planTimeRange || '-' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">巡检标准</span>
              <span class="summary-value">{{ submitResultTaskSummary.standardName || '-' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">巡检线路</span>
              <span class="summary-value">{{ submitResultTaskSummary.lineName || '-' }}</span>
            </div>
            <div class="summary-item summary-item--count">
              <span class="summary-label">项目数量</span>
              <span class="summary-count">{{ submitResultItems.length }}</span>
            </div>
          </div>
        </div>

        <el-empty
          v-if="!submitResultLoading && !submitResultItems.length"
          description="当前任务未查询到检查项目，无法提交结果"
        />

        <el-collapse v-else v-model="submitResultActivePanels" class="submit-result-collapse">
          <el-collapse-item
            v-for="(item, index) in submitResultItems"
            :key="item.panelName"
            :name="item.panelName"
          >
            <template #title>
              <div class="collapse-title">
                <span class="collapse-index">{{ index + 1 }}</span>
                <span class="collapse-name">{{ item.itemName || `检查项目${index + 1}` }}</span>
                <span v-if="item.targetName" class="collapse-target">({{ item.targetName }})</span>
              </div>
            </template>

            <div class="submit-item-panel">
              <div class="item-meta-grid">
                <div class="item-meta-line">
                  <span class="item-meta-label">项目描述：</span>
                  <span class="item-meta-value">{{ item.itemDesc || '-' }}</span>
                </div>
                <div class="item-meta-line">
                  <span class="item-meta-label">合格标准：</span>
                  <span class="item-meta-value">{{ item.qualifiedRule || '-' }}</span>
                </div>
              </div>

              <el-table :data="item.records" border class="item-record-table" empty-text="暂无记录项">
                <el-table-column label="属性名称" prop="attrName" min-width="160" />
                <el-table-column label="标准值" prop="standardValue" min-width="140" />
                <el-table-column label="单位" prop="attrUnit" width="100" align="center" />
                <el-table-column label="实际测量值" min-width="220">
                  <template #default="{ row: record }">
                    <el-input
                      v-model="record.actualValue"
                      placeholder="请输入实际测量值"
                      maxlength="120"
                      clearable
                    />
                  </template>
                </el-table-column>
              </el-table>

              <div class="item-block">
                <div class="item-block-label item-block-label--required">检查结果等级</div>
                <div class="result-level-grid">
                  <div
                    v-for="option in item.resultOptions"
                    :key="`${item.panelName}_${option.value}`"
                    class="result-level-card"
                    :class="[
                      getResultLevelToneClass(option),
                      { 'result-level-card--active': item.checkResult === option.value }
                    ]"
                    @click="item.checkResult = option.value"
                  >
                    <div class="result-level-name">{{ option.label }}</div>
                    <div class="result-level-remark">{{ option.remark || '-' }}</div>
                  </div>
                </div>
              </div>

              <div class="item-block">
                <div class="item-block-label" :class="{ 'item-block-label--required': isAbnormalResult(item) }">
                  检查备注
                </div>
                <el-input
                  v-model="item.checkRemark"
                  type="textarea"
                  :rows="3"
                  maxlength="200"
                  show-word-limit
                  :placeholder="
                    isAbnormalResult(item) ? '该检查结果等级要求必填检查备注' : '请输入检查备注（选填）'
                  "
                />
              </div>

              <div class="item-block">
                <div class="item-block-label" :class="{ 'item-block-label--required': item.needUploadAttachment === 1 }">
                  附件上传
                </div>
                <UploadFile
                  v-model="item.attachments"
                  :limit="20"
                  :file-size="20"
                  :file-type="['jpg', 'png', 'pdf', 'mp4']"
                  :drag="true"
                />
                <p class="upload-tip">支持 JPG、PNG、PDF、MP4，单个文件不超过 20MB，可上传多个文件</p>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
      <template #footer>
        <el-button @click="submitResultVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitResultLoading" @click="submitResult">提交</el-button>
      </template>
    </Dialog>

    <Dialog
      v-model="submitAbnormalReminderVisible"
      title="提交提醒"
      width="980px"
      class="submit-abnormal-reminder-dialog"
    >
      <div class="submit-abnormal-reminder">
        <el-alert
          title="本次提交包含异常检查项，确认后将提交巡检结果；可勾选自动生成故障记录。"
          type="warning"
          :closable="false"
          show-icon
        />
        <el-table :data="submitAbnormalReminderItems" border class="submit-abnormal-reminder-table">
          <el-table-column label="设备" prop="deviceName" min-width="180" show-overflow-tooltip />
          <el-table-column label="检查项目" prop="itemName" min-width="220" show-overflow-tooltip />
          <el-table-column label="检查结果" prop="checkResultLabel" min-width="140" show-overflow-tooltip />
          <el-table-column label="检查备注" prop="checkRemark" min-width="300" show-overflow-tooltip />
        </el-table>
        <el-checkbox v-model="submitAbnormalReminderAutoCreateFaultRecords" class="submit-abnormal-reminder-checkbox">
          勾选后，以上异常项将自动生成故障记录
        </el-checkbox>
      </div>
      <template #footer>
        <el-button @click="submitAbnormalReminderVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitResultLoading"
          :disabled="!submitAbnormalReminderAutoCreateFaultRecords"
          @click="confirmSubmitResultWithReminder"
        >
          提交
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
  InspectionTaskApi,
  type InspectionTaskPageReqVO,
  type InspectionTaskSubmitResultReqVO,
  type InspectionTaskTargetVO,
  type InspectionTaskVO
} from '@/api/iot/inspection/task'
import {
  InspectionPlanApi,
  type InspectionPlanLineOptionVO,
  type InspectionPlanStandardOptionVO,
  type InspectionPlanTargetOptionVO
} from '@/api/iot/inspection/plan'
import { InspectionLineApi, type InspectionLinePointVO, type InspectionLineVO } from '@/api/iot/inspection/line'
import {
  InspectionStandardApi,
  type InspectionCheckResultConfigVO,
  type InspectionStandardTargetVO,
  type InspectionStandardVO
} from '@/api/iot/inspection/standard'
import { DeviceLocationApi, type DeviceLocationNodeRespVO } from '@/api/iot/device/location'
import { formatPast2 } from '@/utils/formatTime'
import { UploadFile } from '@/components/UploadFile'
import * as UserApi from '@/api/system/user'
import { useUserStoreWithOut } from '@/store/modules/user'
import dayjs from 'dayjs'
import type { FormInstance, FormRules } from 'element-plus'

interface SelectOption {
  label: string
  value: string | number
}

type TargetIdValue = string | number
type InspectionTaskQueryParams = InspectionTaskPageReqVO & {
  planTimeRange?: string[]
}

interface InspectionTaskEditVO {
  stationId: string
  taskName: string
  inspectionType: string
  objectType: number
  standardId?: string | number
  lineId?: string | number
  planStartTime: string
  planEndTime: string
  executorUserId?: string | number
  executorName?: string
  targetIds: string[]
  taskDesc: string
}

interface CheckResultOption {
  value: string
  label: string
  remark: string
}
type CheckResultTone = 'success' | 'primary' | 'warning' | 'danger' | 'info'

interface SubmitResultRecordForm {
  attrName: string
  attrUnit: string
  standardValue: string
  actualValue: string
  requiredFlag: number
}

interface SubmitResultItemForm {
  panelName: string
  itemId?: number
  itemName: string
  itemDesc: string
  qualifiedRule: string
  targetType?: string
  targetId?: string | number
  targetName?: string
  needUploadAttachment: number
  checkResult: string
  checkRemark: string
  attachments: string[]
  resultOptions: CheckResultOption[]
  records: SubmitResultRecordForm[]
}

interface SubmitResultTaskSummary {
  taskName: string
  inspectionTypeName: string
  executorName: string
  planTimeRange: string
  standardName: string
  lineName: string
}

interface SubmitAbnormalReminderItem {
  deviceName: string
  itemName: string
  checkResultLabel: string
  checkRemark: string
}

const OBJECT_TYPE_OPTIONS: SelectOption[] = [
  { label: '设备', value: 1 },
  { label: '区域', value: 2 }
]

const SOURCE_TYPE_OPTIONS: SelectOption[] = [
  { label: '计划生成', value: 1 },
  { label: '人工创建', value: 2 }
]
const STANDARD_DEVICE_TARGET_TYPE = 'device'
const SOURCE_TYPE_MANUAL = 2
const TASK_STATUS_NOT_STARTED = 0
const TASK_MANAGER_ROLE_CODES = ['super_admin', 'tenant_admin', 'crm_admin']

const FALLBACK_CHECK_RESULT_OPTIONS: CheckResultOption[] = [
  { value: 'excellent', label: '优秀', remark: '各项指标均优于标准要求' },
  { value: 'good', label: '良好', remark: '各项指标符合标准要求' },
  { value: 'qualified', label: '合格', remark: '基本符合标准，有轻微缺陷' },
  { value: 'unqualified', label: '不合格', remark: '存在严重缺陷或安全隐患' }
]

const CHECK_RESULT_TAG_TYPE_MAP: Record<
  string,
  CheckResultTone
> = {
  excellent: 'success',
  good: 'primary',
  qualified: 'warning',
  unqualified: 'danger'
}

const TASK_STATUS_META: Record<number, { label: string; tagType: 'info' | 'warning' | 'success' | 'danger' }> = {
  0: { label: '未开始', tagType: 'info' },
  1: { label: '未完成', tagType: 'warning' },
  2: { label: '已完成', tagType: 'success' },
  3: { label: '已逾期', tagType: 'danger' }
}

const WORKFLOW_STATUS_META: Record<number, { label: string; tagType: 'info' | 'warning' | 'success' }> = {
  0: { label: '未发起', tagType: 'info' },
  1: { label: '进行中', tagType: 'warning' },
  2: { label: '已结束', tagType: 'success' }
}
const DEFAULT_MAP_CENTER: [number, number] = [32.272, 119.184]
const DEFAULT_MAP_ZOOM = 13
const MAP_FIT_MAX_ZOOM = 18
let leafletCorePromise: Promise<void> | null = null

const createEmptyFormData = (): InspectionTaskEditVO => ({
  stationId: '',
  taskName: '',
  inspectionType: '',
  objectType: 1,
  standardId: undefined,
  lineId: undefined,
  planStartTime: '',
  planEndTime: '',
  executorUserId: undefined,
  executorName: '',
  targetIds: [],
  taskDesc: ''
})

const message = useMessage()
const userStore = useUserStoreWithOut()
const currentUserId = computed(() => Number(userStore.getUser.id || 0))
const currentUserRoleCodes = computed(() => userStore.getRoles || [])
const isTaskManagerRole = computed(() => {
  const roleCodeSet = new Set(currentUserRoleCodes.value)
  return TASK_MANAGER_ROLE_CODES.some((roleCode) => roleCodeSet.has(roleCode))
})
const inspectionTypeOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_INSPECTION_TYPE)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})
const stationOptions = computed<SelectOption[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})
const taskStatusOptions = computed<SelectOption[]>(() => [
  { label: '未开始', value: 0 },
  { label: '未完成', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已逾期', value: 3 }
])
const workflowStatusOptions = computed<SelectOption[]>(() => [
  { label: '未发起', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已结束', value: 2 }
])
const sourceTypeOptions = SOURCE_TYPE_OPTIONS
const objectTypeOptions = OBJECT_TYPE_OPTIONS

const getInspectionTypeLabel = (value?: string) => {
  if (!value) return '-'
  return inspectionTypeOptions.value.find((item) => item.value === value)?.label || value
}
const getTaskStatusLabel = (status?: number) => {
  if (status === undefined || status === null) return '-'
  return TASK_STATUS_META[status]?.label || '未知'
}
const getTaskStatusTagType = (status?: number) => {
  if (status === undefined || status === null) return 'info'
  return TASK_STATUS_META[status]?.tagType || 'info'
}
const getWorkflowStatusLabel = (status?: number) => {
  if (status === undefined || status === null) return '-'
  return WORKFLOW_STATUS_META[status]?.label || '未知'
}
const getWorkflowStatusTagType = (status?: number) => {
  if (status === undefined || status === null) return 'info'
  return WORKFLOW_STATUS_META[status]?.tagType || 'info'
}
const getSourceTypeLabel = (sourceType?: number) => {
  if (!sourceType) return '-'
  return sourceTypeOptions.find((item) => Number(item.value) === Number(sourceType))?.label || '未知'
}
const getCheckResultTagType = (value?: string) => {
  if (!value) return 'info'
  return CHECK_RESULT_TAG_TYPE_MAP[value] || 'info'
}
const getCheckResultTone = (option: CheckResultOption): CheckResultTone => {
  const value = String(option.value || '').trim()
  if (value && CHECK_RESULT_TAG_TYPE_MAP[value]) {
    return CHECK_RESULT_TAG_TYPE_MAP[value]
  }
  const label = String(option.label || '').trim()
  if (/险情|紧急/.test(label)) return 'danger'
  if (/重大|严重/.test(label)) return 'warning'
  if (/一般|良好/.test(label)) return 'primary'
  if (/正常|优秀|合格/.test(label)) return 'success'
  return 'info'
}
const getResultLevelToneClass = (option: CheckResultOption) => {
  return `result-level-card--tone-${getCheckResultTone(option)}`
}
const getCheckResultLabel = (item: SubmitResultItemForm) => {
  if (!item.checkResult) return '未填写'
  const option = item.resultOptions.find((config) => config.value === item.checkResult)
  return option?.label || item.checkResult
}
const getAttachmentDisplayName = (url?: string, index = 0) => {
  const text = String(url || '').trim()
  if (!text) {
    return `附件${index + 1}`
  }
  const cleanUrl = text.split('?')[0] || text
  const segments = cleanUrl.split('/')
  return decodeURIComponent(segments[segments.length - 1] || `附件${index + 1}`)
}
const detailHasSubmitResult = computed(() => {
  const submitTime = detailData.value?.submitTime
  return submitTime !== undefined && submitTime !== null && String(submitTime).trim() !== ''
})
const detailAbnormalCountDisplay = computed(() => {
  if (!detailHasSubmitResult.value) {
    return '-'
  }
  const abnormalCount = Number(detailData.value?.abnormalCount || 0)
  const totalCount = detailSubmitResultItems.value.length
  return `${abnormalCount}/${totalCount}`
})
const formatDateTime = (value?: string | number | null) => {
  if (value === undefined || value === null || value === '') return '-'
  const text = String(value).trim()
  if (!text) return '-'
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(text)) {
    return text
  }
  if (/^\d{10,13}$/.test(text)) {
    const unix = Number(text)
    if (Number.isFinite(unix)) {
      const parsedByTimestamp = dayjs(text.length === 10 ? unix * 1000 : unix)
      if (parsedByTimestamp.isValid()) {
        return parsedByTimestamp.format('YYYY-MM-DD HH:mm:ss')
      }
    }
  }
  const parsed = dayjs(text)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : text
}
const getStationLabel = (value?: string) => {
  if (!value) return '-'
  return stationOptions.value.find((item) => String(item.value) === String(value))?.label || value
}
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
const list = ref<InspectionTaskVO[]>([])
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<InspectionTaskVO>()
const detailSubmitResultLoading = ref(false)
const detailSubmitResultItems = ref<SubmitResultItemForm[]>([])
const detailSubmitActivePanels = ref<string[]>([])
const submitResultVisible = ref(false)
const submitResultLoading = ref(false)
const submitResultFormData = reactive<InspectionTaskSubmitResultReqVO>({
  id: 0
})
const submitAbnormalReminderVisible = ref(false)
const submitAbnormalReminderAutoCreateFaultRecords = ref(false)
const submitAbnormalReminderItems = ref<SubmitAbnormalReminderItem[]>([])
const submitResultPayloadCache = reactive<Record<string, InspectionTaskSubmitResultReqVO['items']>>({})
const submitResultTaskSummary = reactive<SubmitResultTaskSummary>({
  taskName: '',
  inspectionTypeName: '',
  executorName: '',
  planTimeRange: '',
  standardName: '',
  lineName: ''
})
const submitResultItems = ref<SubmitResultItemForm[]>([])
const submitResultActivePanels = ref<string[]>([])
const queryParams = reactive<InspectionTaskQueryParams>({
  pageNo: 1,
  pageSize: 10,
  taskNo: undefined,
  taskName: undefined,
  inspectionType: undefined,
  taskStatus: undefined,
  workflowStatus: undefined,
  sourceType: undefined,
  stationId: undefined,
  executorUserId: undefined,
  planTimeRange: undefined
})

const getList = async () => {
  loading.value = true
  try {
    const { planTimeRange, ...restParams } = queryParams
    const pageParams: InspectionTaskPageReqVO = {
      ...restParams,
      planStartTime: planTimeRange,
      planEndTime: planTimeRange
    }
    const data = await InspectionTaskApi.getInspectionTaskPage(pageParams)
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
  queryParams.pageNo = 1
  await getList()
}
const isManualSourceTask = (row?: InspectionTaskVO) => Number(row?.sourceType) === SOURCE_TYPE_MANUAL
const canManageTask = (row?: InspectionTaskVO) => isTaskManagerRole.value && isManualSourceTask(row)
const isTaskNotStarted = (row?: InspectionTaskVO) => Number(row?.taskStatus) === TASK_STATUS_NOT_STARTED
const canOperateTask = (row?: InspectionTaskVO) => canManageTask(row) && isTaskNotStarted(row)

const handleDelete = async (row?: InspectionTaskVO) => {
  if (!row?.id) return
  if (!canOperateTask(row)) {
    message.warning('仅管理人可删除未开始的人工创建巡检任务')
    return
  }
  try {
    await message.delConfirm('确认删除该巡检任务吗？')
    await InspectionTaskApi.deleteInspectionTask(row.id)
    message.success('删除成功')
    await getList()
  } catch {}
}
const openDetail = async (id?: number) => {
  if (!id) return
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = undefined
  resetDetailSubmitResultState()
  try {
    detailData.value = await InspectionTaskApi.getInspectionTask(id)
    await loadDetailSubmitResult(detailData.value)
  } catch (error) {
    detailVisible.value = false
    resetDetailSubmitResultState()
    message.error('获取任务详情失败，请稍后重试')
  } finally {
    detailLoading.value = false
  }
}

const createEmptySubmitResultSummary = (): SubmitResultTaskSummary => ({
  taskName: '',
  inspectionTypeName: '',
  executorName: '',
  planTimeRange: '',
  standardName: '',
  lineName: ''
})

const formatPlanTimeRange = (startTime?: string | number, endTime?: string | number) => {
  const start = formatDateTime(startTime)
  const end = formatDateTime(endTime)
  if (start === '-' && end === '-') {
    return '-'
  }
  if (start === '-') {
    return end
  }
  if (end === '-') {
    return start
  }
  return `${start} ~ ${end}`
}

const normalizePositiveIntegerId = (value?: string | number | null) => {
  if (value === undefined || value === null || value === '') {
    return ''
  }
  const text = String(value).trim()
  return /^\d+$/.test(text) ? text : ''
}

const normalizeCheckResultOptions = (configs?: InspectionCheckResultConfigVO[]): CheckResultOption[] => {
  const optionMap = new Map<string, CheckResultOption>()
  ;(configs || []).forEach((config) => {
    const value = String(config?.value || '').trim()
    const label = String(config?.label || '').trim()
    const key = value || label
    if (!key) {
      return
    }
    const fallback = FALLBACK_CHECK_RESULT_OPTIONS.find(
      (item) => item.value === value || item.label === label
    )
    optionMap.set(key, {
      value: value || fallback?.value || key,
      label: label || fallback?.label || key,
      remark: String(config?.remark || fallback?.remark || '').trim()
    })
  })
  if (!optionMap.size) {
    return FALLBACK_CHECK_RESULT_OPTIONS.map((item) => ({ ...item }))
  }
  return Array.from(optionMap.values())
}

const buildSubmitResultItems = (
  taskDetail: InspectionTaskVO,
  standardDetail: InspectionStandardVO
): SubmitResultItemForm[] => {
  const standardTargets = standardDetail.targets || []
  // 提交巡检结果以巡检标准为准展开全部适用对象，避免多对象场景被误过滤
  const selectedTargets: InspectionStandardTargetVO[] = standardTargets
  const taskTargetNameMap = new Map<string, string>()
  ;(taskDetail.targets || []).forEach((target) => {
    const targetId = normalizeTargetId(target.targetId)
    if (!targetId) {
      return
    }
    taskTargetNameMap.set(targetId, String(target.targetName || '').trim())
  })
  const resultItems: SubmitResultItemForm[] = []

  selectedTargets.forEach((target, targetIndex) => {
    const resultOptions = normalizeCheckResultOptions(target.checkResultConfigs)
    const targetId = normalizeTargetId(target.targetId)
    const taskTargetName = targetId ? taskTargetNameMap.get(targetId) : ''
    const displayTargetName = taskTargetName || target.targetName || ''
    ;(target.items || []).forEach((item, itemIndex) => {
      const panelName = `target_${targetIndex + 1}_item_${itemIndex + 1}_${item.id || 'new'}`
      const records: SubmitResultRecordForm[] = (item.recordTemplates || []).map((record) => ({
        attrName: record.attrName || '-',
        attrUnit: record.attrUnit || '-',
        standardValue: record.defaultValue || '-',
        actualValue: '',
        requiredFlag: Number(record.requiredFlag || 0)
      }))
      resultItems.push({
        panelName,
        itemId: item.id,
        itemName: item.itemName || '',
        itemDesc: item.itemDesc || '',
        qualifiedRule: item.qualifiedRule || '',
        targetType: String(target.targetType || '').trim(),
        targetId: target.targetId,
        targetName: displayTargetName,
        needUploadAttachment: Number(item.needUploadAttachment || 0),
        checkResult: '',
        checkRemark: '',
        attachments: [],
        resultOptions,
        records
      })
    })
  })

  return resultItems
}

const resetSubmitResultDialogData = () => {
  submitResultFormData.id = 0
  submitResultFormData.autoCreateFaultRecords = false
  Object.assign(submitResultTaskSummary, createEmptySubmitResultSummary())
  submitResultItems.value = []
  submitResultActivePanels.value = []
  submitAbnormalReminderVisible.value = false
  submitAbnormalReminderAutoCreateFaultRecords.value = false
  submitAbnormalReminderItems.value = []
}

const fillSubmitResultSummary = (task: InspectionTaskVO) => {
  submitResultTaskSummary.taskName = task.taskName || ''
  submitResultTaskSummary.inspectionTypeName = getInspectionTypeLabel(task.inspectionType)
  submitResultTaskSummary.executorName = task.executorName || '-'
  submitResultTaskSummary.planTimeRange = formatPlanTimeRange(task.planStartTime, task.planEndTime)
  submitResultTaskSummary.standardName = task.standardName || '-'
  submitResultTaskSummary.lineName = task.lineName || '-'
}

const validateSubmitResultItems = () => {
  if (!submitResultItems.value.length) {
    message.warning('未查询到检查项目，请先维护巡检标准后再提交任务')
    return false
  }

  for (let i = 0; i < submitResultItems.value.length; i++) {
    const item = submitResultItems.value[i]
    if (!item.checkResult) {
      submitResultActivePanels.value = [item.panelName]
      message.warning(`请填写第 ${i + 1} 个检查项目的检查结果等级`)
      return false
    }
    if (isAbnormalResult(item) && !String(item.checkRemark || '').trim()) {
      submitResultActivePanels.value = [item.panelName]
      message.warning(`第 ${i + 1} 个检查项目的检查结果为异常，检查备注必须填写`)
      return false
    }
    for (let j = 0; j < item.records.length; j++) {
      const record = item.records[j]
      const actualValue = String(record.actualValue || '').trim()
      if (!actualValue) {
        submitResultActivePanels.value = [item.panelName]
        message.warning(`请填写第 ${i + 1} 个检查项目第 ${j + 1} 条记录的实际测量值`)
        return false
      }
    }
    if (item.needUploadAttachment === 1 && !item.attachments.length) {
      submitResultActivePanels.value = [item.panelName]
      message.warning(`第 ${i + 1} 个检查项目要求上传附件，请先上传后再提交`)
      return false
    }
  }
  return true
}

const isAbnormalResult = (item: SubmitResultItemForm) => {
  const normalizedValue = String(item.checkResult || '')
    .trim()
    .toLowerCase()
  return normalizedValue === 'qualified' || normalizedValue === 'unqualified'
}

const isDeviceTargetItem = (item: SubmitResultItemForm) => {
  return (
    String(item.targetType || '')
      .trim()
      .toLowerCase() === STANDARD_DEVICE_TARGET_TYPE
  )
}

const getResultOptionLabel = (item: SubmitResultItemForm) => {
  const selected = item.resultOptions.find(
    (option) => String(option.value || '').trim() === String(item.checkResult || '').trim()
  )
  return selected?.label || item.checkResult || '-'
}

const buildSubmitAbnormalReminderItems = (): SubmitAbnormalReminderItem[] => {
  return submitResultItems.value
    .filter((item) => isAbnormalResult(item) && isDeviceTargetItem(item))
    .map((item) => ({
      deviceName: item.targetName || '-',
      itemName: item.itemName || '-',
      checkResultLabel: getResultOptionLabel(item),
      checkRemark: item.checkRemark?.trim() || '-'
    }))
}

const buildSubmitResultPayloadItems = (): InspectionTaskSubmitResultReqVO['items'] => {
  return submitResultItems.value.map((item) => ({
    itemId: item.itemId,
    itemName: item.itemName,
    targetId: item.targetId,
    targetName: item.targetName,
    checkResult: item.checkResult,
    checkRemark: item.checkRemark?.trim() || '',
    attachments: item.attachments || [],
    records: item.records.map((record) => ({
      attrName: record.attrName,
      attrUnit: record.attrUnit,
      standardValue: record.standardValue,
      actualValue: record.actualValue?.trim() || ''
    }))
  }))
}

const buildResultItemMatchKey = (
  item?: {
    itemId?: number
    targetId?: string | number
    itemName?: string
    targetName?: string
  } | null
) => {
  if (!item) return ''
  const targetId = normalizeTargetId(item.targetId)
  return `${item.itemId || item.itemName || ''}__${targetId || item.targetName || ''}`
}

const mergeSubmitResultItemsWithCache = (
  baseItems: SubmitResultItemForm[],
  cachedItems: InspectionTaskSubmitResultReqVO['items']
) => {
  if (!cachedItems?.length) {
    return baseItems
  }

  const cachedMap = new Map<string, NonNullable<InspectionTaskSubmitResultReqVO['items']>[number]>()
  cachedItems.forEach((item) => {
    const key = buildResultItemMatchKey({
      itemId: item.itemId,
      itemName: item.itemName,
      targetId: item.targetId,
      targetName: item.targetName
    })
    if (key) {
      cachedMap.set(key, item)
    }
  })

  return baseItems.map((item) => {
    const cacheItem = cachedMap.get(
      buildResultItemMatchKey({
        itemId: item.itemId,
        itemName: item.itemName,
        targetId: item.targetId,
        targetName: item.targetName
      })
    )
    if (!cacheItem) {
      return item
    }

    const recordValueMap = new Map<string, string>()
    ;(cacheItem.records || []).forEach((record, index) => {
      const key = String(record.attrName || '').trim() || `index_${index}`
      recordValueMap.set(key, String(record.actualValue || '').trim())
    })

    return {
      ...item,
      checkResult: cacheItem.checkResult || '',
      checkRemark: cacheItem.checkRemark || '',
      attachments: cacheItem.attachments || [],
      records: item.records.map((record, index) => {
        const key = String(record.attrName || '').trim() || `index_${index}`
        return {
          ...record,
          actualValue: recordValueMap.get(key) || ''
        }
      })
    }
  })
}

const resetDetailSubmitResultState = () => {
  detailSubmitResultItems.value = []
  detailSubmitActivePanels.value = []
}

const loadDetailSubmitResult = async (task?: InspectionTaskVO) => {
  resetDetailSubmitResultState()
  if (!task?.id) {
    return
  }

  const standardId = normalizePositiveIntegerId(task.standardId as string | number | undefined)
  if (!standardId) {
    return
  }

  detailSubmitResultLoading.value = true
  try {
    const standardDetail = await InspectionStandardApi.getInspectionStandard(standardId)
    if (!standardDetail) {
      return
    }
    const baseItems = buildSubmitResultItems(task, standardDetail)
    const persistedItems = task.items || []
    const cachedItems = submitResultPayloadCache[String(task.id)] || []
    const mergedItems = mergeSubmitResultItemsWithCache(
      mergeSubmitResultItemsWithCache(baseItems, persistedItems),
      cachedItems
    )
    detailSubmitResultItems.value = mergedItems
    detailSubmitActivePanels.value = mergedItems[0] ? [mergedItems[0].panelName] : []
  } finally {
    detailSubmitResultLoading.value = false
  }
}

const getSubmitBlockReason = (row: InspectionTaskVO) => {
  const executorMatched = Number(row.executorUserId || 0) === currentUserId.value
  if (!executorMatched) {
    return '仅执行人可在任务进行中提交巡检结果'
  }
  const submitted = !!String(row.submitTime || '').trim()
  if (submitted) {
    return '该任务已提交结果，不可重复提交'
  }
  const taskStatusAllowed = Number(row.taskStatus) === 1 || Number(row.taskStatus) === 3
  if (!taskStatusAllowed) {
    return '当前任务状态不允许提交结果'
  }
  const workflowRunning = Number(row.workflowStatus) === 1
  if (!workflowRunning) {
    return '当前流程状态不允许提交结果'
  }
  return ''
}

const canSubmitResult = (row: InspectionTaskVO) => {
  return !getSubmitBlockReason(row)
}

const openSubmitDialog = async (row: InspectionTaskVO) => {
  if (!row?.id) return
  const blockReason = getSubmitBlockReason(row)
  if (blockReason) {
    message.warning(blockReason)
    return
  }

  submitResultVisible.value = true
  submitResultLoading.value = true
  resetSubmitResultDialogData()
  submitResultFormData.id = row.id

  try {
    const taskDetail = await InspectionTaskApi.getInspectionTask(row.id)
    const latestBlockReason = getSubmitBlockReason(taskDetail)
    if (latestBlockReason) {
      submitResultVisible.value = false
      message.warning(latestBlockReason)
      return
    }
    fillSubmitResultSummary(taskDetail)

    const standardId = normalizePositiveIntegerId(taskDetail.standardId as string | number | undefined)
    if (!standardId) {
      message.warning('当前任务未关联巡检标准，无法提交检查项目结果')
      return
    }

    const standardDetail = await InspectionStandardApi.getInspectionStandard(standardId)
    if (!standardDetail) {
      message.warning('巡检标准不存在或已被删除，请先修复任务关联标准')
      return
    }
    const itemForms = buildSubmitResultItems(taskDetail, standardDetail)
    submitResultItems.value = itemForms
    submitResultActivePanels.value = itemForms[0] ? [itemForms[0].panelName] : []

    if (!itemForms.length) {
      message.warning('当前巡检标准未维护检查项目，请先完善后再提交任务')
    }
  } catch (error) {
    submitResultVisible.value = false
    message.error('加载提交表单失败，请稍后重试')
  } finally {
    submitResultLoading.value = false
  }
}

const doSubmitResult = async (autoCreateFaultRecords: boolean) => {
  submitResultLoading.value = true
  try {
    const payloadItems = buildSubmitResultPayloadItems()
    await InspectionTaskApi.submitInspectionTaskResult({
      id: submitResultFormData.id,
      abnormalCount: submitResultItems.value.filter((item) => isAbnormalResult(item)).length,
      remark: `共提交 ${submitResultItems.value.length} 个检查项目`,
      autoCreateFaultRecords,
      items: payloadItems
    })
    submitResultPayloadCache[String(submitResultFormData.id)] = payloadItems || []
    message.success('提交结果成功')
    submitAbnormalReminderVisible.value = false
    submitResultVisible.value = false
    await getList()
    if (detailVisible.value && detailData.value?.id === submitResultFormData.id) {
      await openDetail(submitResultFormData.id)
    }
  } finally {
    submitResultLoading.value = false
  }
}

const confirmSubmitResultWithReminder = async () => {
  if (!submitResultFormData.id) {
    return
  }
  if (!submitAbnormalReminderAutoCreateFaultRecords.value) {
    message.warning('请先勾选“以上异常项将自动生成故障记录”后再提交')
    return
  }
  await doSubmitResult(!!submitAbnormalReminderAutoCreateFaultRecords.value)
}

const submitResult = async () => {
  if (!submitResultFormData.id) {
    return
  }
  if (!validateSubmitResultItems()) {
    return
  }
  const abnormalItems = buildSubmitAbnormalReminderItems()
  if (!abnormalItems.length) {
    await doSubmitResult(false)
    return
  }
  submitAbnormalReminderItems.value = abnormalItems
  submitAbnormalReminderAutoCreateFaultRecords.value = false
  submitAbnormalReminderVisible.value = true
}

const handleEdit = async (row: InspectionTaskVO) => {
  if (!row?.id) return
  if (!canOperateTask(row)) {
    message.warning('仅管理人可编辑未开始的人工创建巡检任务')
    return
  }
  await openEditForm(row.id)
}

const formVisible = ref(false)
const formLoading = ref(false)
const submitLoading = ref(false)
const formMode = ref<'create' | 'update'>('create')
const currentEditTaskId = ref<number>()
const formRef = ref<FormInstance>()
const formData = reactive<InspectionTaskEditVO>(createEmptyFormData())

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
const standardLabelCache = reactive<Record<string, string>>({})
const lineLabelCache = reactive<Record<string, string>>({})
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

const targetTypeLabel = computed(() => (Number(formData.objectType) === 2 ? '区域' : '设备'))
const targetSelectButtonText = computed(() => `选择${targetTypeLabel.value}`)
const targetPickerTitle = computed(() => `${targetSelectButtonText.value}`)
const currentLineName = computed(() => {
  const normalizedLineId = normalizeTargetId(formData.lineId)
  if (!normalizedLineId) return ''
  return lineLabelCache[normalizedLineId] || `线路-${normalizedLineId}`
})
const linePreviewTitle = computed(() =>
  currentLineName.value ? `巡检线路预览 - ${currentLineName.value}` : '巡检线路预览'
)
const isEditMode = computed(() => formMode.value === 'update')
const formDialogTitle = computed(() => (isEditMode.value ? '编辑巡检任务' : '新建巡检任务'))
const formSubmitButtonText = computed(() => (isEditMode.value ? '保存修改' : '保存'))
const normalizeLinePoints = (points: InspectionLinePointVO[] = []) => {
  return [...points].sort((a, b) => Number(a.pointSort || 0) - Number(b.pointSort || 0))
}
const linePreviewPointList = computed(() => normalizeLinePoints(linePreviewData.value?.points || []))
const linePreviewPointCount = computed(() => linePreviewPointList.value.length)

const formRules: FormRules = {
  stationId: [{ required: true, message: '所属闸站不能为空', trigger: 'change' }],
  taskName: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
  inspectionType: [{ required: true, message: '巡检类型不能为空', trigger: 'change' }],
  objectType: [{ required: true, message: '巡检对象类型不能为空', trigger: 'change' }],
  standardId: [{ required: true, message: '巡检标准不能为空', trigger: 'change' }],
  planStartTime: [{ required: true, message: '计划开始时间不能为空', trigger: 'change' }],
  planEndTime: [{ required: true, message: '计划完成时间不能为空', trigger: 'change' }],
  executorUserId: [{ required: true, message: '执行人不能为空', trigger: 'change' }],
  targetIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一个巡检对象', trigger: 'change' }]
}

const resetForm = () => {
  Object.assign(formData, createEmptyFormData())
  currentEditTaskId.value = undefined
  linePreviewVisible.value = false
  linePreviewData.value = undefined
  destroyLinePreviewMap()
  targetOptions.value = []
  standardOptions.value = []
  lineOptions.value = []
  locationTreeData.value = []
  targetPickerSelection.value = []
  targetPickerVisible.value = false
  targetStationId.value = ''
  targetKeyword.value = ''
  standardKeyword.value = ''
  lineKeyword.value = ''
  Object.keys(targetLabelCache).forEach((key) => delete targetLabelCache[key])
  Object.keys(targetStationCache).forEach((key) => delete targetStationCache[key])
  Object.keys(standardLabelCache).forEach((key) => delete standardLabelCache[key])
  Object.keys(lineLabelCache).forEach((key) => delete lineLabelCache[key])
  formRef.value?.clearValidate()
}

const loadUserOptions = async () => {
  const users = await UserApi.getSimpleUserList({ excludeRoleName: '游客' })
  userOptions.value = (users || []).map((item) => ({ label: item.nickname || item.username, value: item.id }))
}
const handleExecutorChange = (userId?: number) => {
  const selected = userOptions.value.find((item) => Number(item.value) === Number(userId))
  formData.executorName = selected?.label || ''
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
    const normalizedId = normalizeTargetId(item.id)
    if (!normalizedId) return
    standardLabelCache[normalizedId] = item.name || `标准-${normalizedId}`
  })
}
const mergeLineCache = (options: InspectionPlanLineOptionVO[]) => {
  options.forEach((item) => {
    const normalizedId = normalizeTargetId(item.id)
    if (!normalizedId) return
    lineLabelCache[normalizedId] = item.name || `线路-${normalizedId}`
  })
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
      const selectedStandardId = normalizeTargetId(formData.standardId)
      const exists = merged.some((item) => normalizeTargetId(item.id) === selectedStandardId)
      if (!exists) {
        merged.push({
          id: formData.standardId as unknown as number,
          name: standardLabelCache[selectedStandardId || ''] || `标准-${selectedStandardId}`,
          inspectionType: formData.inspectionType || ''
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
      const selectedLineId = normalizeTargetId(formData.lineId)
      const exists = merged.some((item) => normalizeTargetId(item.id) === selectedLineId)
      if (!exists) {
        merged.push({
          id: formData.lineId as unknown as number,
          name: lineLabelCache[selectedLineId || ''] || `线路-${selectedLineId}`
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
    className: 'inspection-task-line-order-icon',
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
      className: 'inspection-task-line-route-tooltip'
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
      const normalizedLineId = normalizeTargetId(formData.lineId)
      if (normalizedLineId) {
        lineLabelCache[normalizedLineId] = lineData.lineName
      }
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

const handleStandardRemoteSearch = async (keyword: string) => {
  standardKeyword.value = keyword
  await loadStandardOptions(keyword)
}
const handleLineRemoteSearch = async (keyword: string) => {
  lineKeyword.value = keyword
  await loadLineOptions(keyword)
}
const handleInspectionTypeChange = async () => {
  formData.standardId = undefined
  formData.lineId = undefined
  await Promise.all([loadStandardOptions(standardKeyword.value), loadLineOptions(lineKeyword.value)])
}
const handleStationChange = async () => {
  targetStationId.value = formData.stationId || ''
  formData.lineId = undefined
  await Promise.all([loadLineOptions(lineKeyword.value), loadTargetOptions(targetKeyword.value)])
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
  if (!formData.objectType) {
    message.warning('请先选择巡检对象类型')
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
const handleTargetStationChange = async () => {
  formData.stationId = targetStationId.value || formData.stationId
  await searchTargetOptions()
}
const handleObjectTypeChange = async () => {
  formData.targetIds = []
  targetPickerSelection.value = []
  targetOptions.value = []
  locationTreeData.value = []
  targetKeyword.value = ''
  targetPickerVisible.value = false
  if (Number(formData.objectType) === 1) {
    await loadTargetOptions()
  } else {
    await loadLocationTree()
  }
}
const removeTarget = (targetId: TargetIdValue) => {
  const normalizedTargetId = normalizeTargetId(targetId)
  if (!normalizedTargetId) return
  formData.targetIds = formData.targetIds.filter((id) => id !== normalizedTargetId)
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

const buildTargetPayload = (): InspectionTaskTargetVO[] => {
  return formData.targetIds.map((targetId, index) => ({
    targetId,
    targetSort: index + 1,
    targetName: targetLabelCache[targetId] || `对象-${targetId}`
  }))
}
const buildPayload = (id?: number): InspectionTaskVO => {
  return {
    id,
    stationId: formData.stationId,
    taskName: formData.taskName.trim(),
    inspectionType: formData.inspectionType,
    objectType: Number(formData.objectType),
    standardId: formData.standardId as string | number,
    lineId: formData.lineId,
    planStartTime: formData.planStartTime,
    planEndTime: formData.planEndTime,
    executorUserId: formData.executorUserId as string | number,
    executorName: formData.executorName || '',
    targetIds: [...formData.targetIds],
    targets: buildTargetPayload(),
    taskDesc: formData.taskDesc?.trim() || ''
  }
}

const fillFormByTask = (task: InspectionTaskVO) => {
  currentEditTaskId.value = task.id
  formData.stationId = String(task.stationId || '')
  formData.taskName = task.taskName || ''
  formData.inspectionType = task.inspectionType || ''
  formData.objectType = Number(task.objectType || 1)
  formData.standardId = task.standardId
  formData.lineId = task.lineId
  formData.planStartTime = formatDateTime(task.planStartTime) === '-' ? '' : formatDateTime(task.planStartTime)
  formData.planEndTime = formatDateTime(task.planEndTime) === '-' ? '' : formatDateTime(task.planEndTime)
  formData.executorUserId = task.executorUserId
  formData.executorName = task.executorName || ''
  formData.taskDesc = task.taskDesc || ''
  formData.targetIds = (task.targets || [])
    .map((item) => normalizeTargetId(item.targetId))
    .filter((item): item is string => !!item)

  if (task.standardId) {
    const normalizedStandardId = normalizeTargetId(task.standardId)
    if (normalizedStandardId) {
      standardLabelCache[normalizedStandardId] = task.standardName || `标准-${normalizedStandardId}`
    }
  }
  if (task.lineId) {
    const normalizedLineId = normalizeTargetId(task.lineId)
    if (normalizedLineId) {
      lineLabelCache[normalizedLineId] = task.lineName || `线路-${normalizedLineId}`
    }
  }
  ;(task.targets || []).forEach((target) => {
    const normalizedTargetId = normalizeTargetId(target.targetId)
    if (!normalizedTargetId) return
    targetLabelCache[normalizedTargetId] = target.targetName || `对象-${normalizedTargetId}`
    if (target.stationId) {
      targetStationCache[normalizedTargetId] = target.stationId
    }
  })
  targetStationId.value = formData.stationId || ''
}

const loadFormDependencyOptions = async () => {
  await Promise.all([loadStandardOptions(), loadLineOptions()])
  if (Number(formData.objectType) === 1) {
    await loadTargetOptions()
    return
  }
  await loadLocationTree()
}

const openCreateForm = async () => {
  formMode.value = 'create'
  formVisible.value = true
  formLoading.value = true
  resetForm()
  try {
    await loadFormDependencyOptions()
  } finally {
    formLoading.value = false
  }
}
const openEditForm = async (id?: number) => {
  if (!id) return
  formMode.value = 'update'
  formVisible.value = true
  formLoading.value = true
  resetForm()
  try {
    const task = await InspectionTaskApi.getInspectionTask(id)
    if (!task) {
      message.warning('巡检任务不存在或已被删除')
      formVisible.value = false
      return
    }
    if (task.sourceType === 1) {
      message.warning('计划自动生成任务暂不支持编辑')
      formVisible.value = false
      return
    }
    fillFormByTask(task)
    await loadFormDependencyOptions()
  } catch (error) {
    formVisible.value = false
    message.error('加载编辑数据失败，请稍后重试')
  } finally {
    formLoading.value = false
  }
}
const submitForm = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  if (formData.planStartTime && formData.planEndTime && formData.planStartTime > formData.planEndTime) {
    message.error('计划完成时间不能早于计划开始时间')
    return
  }
  submitLoading.value = true
  try {
    if (isEditMode.value) {
      if (!currentEditTaskId.value) {
        message.error('缺少任务ID，无法保存')
        return
      }
      await InspectionTaskApi.updateInspectionTask(buildPayload(currentEditTaskId.value))
      message.success('修改成功')
    } else {
      await InspectionTaskApi.createInspectionTask(buildPayload())
      message.success('新增成功')
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
  await Promise.all([loadUserOptions(), getList()])
})
</script>

<style scoped lang="scss">
.inspection-task-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.create-btn {
  border-radius: 999px;
  padding: 0 18px;
}

.task-action-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 10px;
}

:deep(.task-action-group .el-button + .el-button) {
  margin-left: 0;
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

:deep(.inspection-task-line-route-tooltip) {
  border: none;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.18);
  color: #0f172a;
  font-size: 12px;
  font-weight: 500;
}

.submit-task-name {
  color: #1e293b;
  font-weight: 600;
}

.abnormal-ratio {
  color: #b45309;
  font-weight: 700;
}

.abnormal-ratio-tip {
  margin-left: 6px;
  color: #64748b;
  font-size: 12px;
}

.detail-submit-wrap {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-submit-head {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px 12px;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  padding: 10px 12px;
  background: linear-gradient(135deg, #f8fbff 0%, #f8fafc 100%);
}

.detail-submit-stat {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-submit-stat--remark {
  grid-column: 1 / -1;
}

.detail-submit-stat--time {
  min-width: 260px;
}

.detail-submit-stat__label {
  color: #64748b;
  font-size: 12px;
  flex-shrink: 0;
}

.detail-submit-stat__value {
  color: #1e293b;
  font-size: 13px;
  font-weight: 600;
  word-break: break-all;
}

.detail-submit-stat__value--time {
  white-space: nowrap;
  word-break: normal;
  overflow: visible;
  text-overflow: clip;
}

.detail-submit-collapse {
  border: 1px solid #dbeafe;
  border-radius: 10px;
  overflow: hidden;

  :deep(.el-collapse-item__header) {
    min-height: 44px;
    padding: 0 12px;
    background: #f8fbff;
  }

  :deep(.el-collapse-item__content) {
    padding: 10px 12px 14px;
    background: #fff;
  }
}

.detail-submit-collapse-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-submit-collapse-title__index {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #3b82f6;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.detail-submit-collapse-title__name {
  color: #1e293b;
  font-size: 13px;
  font-weight: 600;
}

.detail-submit-item-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-submit-item-line {
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-submit-item-line--attachment {
  align-items: flex-start;
}

.detail-submit-item-line__label {
  color: #64748b;
  font-size: 12px;
  flex-shrink: 0;
}

.detail-submit-item-line__value {
  color: #1e293b;
  font-size: 13px;
  font-weight: 500;
  word-break: break-all;
}

.detail-submit-attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 10px;
}

.detail-submit-attachment-link {
  max-width: 280px;
  font-size: 12px;
  text-decoration: none;
}

.detail-submit-record-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-submit-record-item {
  display: grid;
  grid-template-columns: 1.2fr 1fr 0.6fr 1.4fr;
  gap: 8px;
  align-items: center;
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
}

.detail-submit-record-item__cell {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 2px;
  min-width: 0;
}

.detail-submit-record-item__field {
  color: #64748b;
  font-size: 12px;
  flex-shrink: 0;
}

.detail-submit-record-item__name {
  color: #334155;
  font-size: 12px;
  font-weight: 600;
  word-break: break-all;
  overflow-wrap: anywhere;
}

.detail-submit-record-item__value {
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
  word-break: break-all;
  overflow-wrap: anywhere;
}

.detail-submit-record-item__unit,
.detail-submit-record-item__standard {
  color: #64748b;
  font-size: 12px;
  word-break: break-all;
  overflow-wrap: anywhere;
}

.submit-result-dialog {
  :deep(.el-dialog__body) {
    max-height: 72vh;
    overflow-y: auto;
    padding-top: 10px;
  }
}

.submit-result-wrapper {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.submit-result-summary {
  border: 1px solid #dbeafe;
  border-radius: 10px;
  padding: 12px;
  background: linear-gradient(135deg, #f8fbff 0%, #f9fafb 100%);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px 12px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.summary-item--count {
  justify-content: center;
  align-items: flex-start;
}

.summary-label {
  font-size: 12px;
  color: #64748b;
  line-height: 1;
}

.summary-value {
  font-size: 14px;
  color: #1e293b;
  font-weight: 600;
  line-height: 1.4;
  word-break: break-all;
}

.summary-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 42px;
  height: 30px;
  border: 1px solid #93c5fd;
  border-radius: 8px;
  color: #1d4ed8;
  font-weight: 700;
  font-size: 15px;
  background: #eff6ff;
}

.submit-result-collapse {
  border-radius: 10px;
  border: 1px solid #dbeafe;
  overflow: hidden;

  :deep(.el-collapse-item__header) {
    min-height: 52px;
    padding: 0 14px;
    background: #f8fbff;
  }

  :deep(.el-collapse-item__content) {
    padding: 12px 14px 16px;
    background: #fff;
  }
}

.collapse-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.collapse-index {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #3b82f6;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.collapse-name {
  color: #1e293b;
  font-size: 14px;
  font-weight: 600;
}

.collapse-target {
  color: #64748b;
  font-size: 13px;
}

.submit-item-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 12px;
  background: #f8fafc;
}

.item-meta-line {
  min-width: 0;
  font-size: 13px;
  color: #334155;
  display: flex;
  gap: 2px;
}

.item-meta-label {
  color: #64748b;
  flex-shrink: 0;
}

.item-meta-value {
  color: #1e293b;
  font-weight: 500;
  word-break: break-all;
}

.item-record-table {
  :deep(.el-input__wrapper) {
    width: 100%;
  }
}

.item-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-block-label {
  color: #334155;
  font-size: 13px;
  font-weight: 600;
}

.item-block-label--required::before {
  content: '*';
  color: #ef4444;
  margin-right: 4px;
}

.result-level-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.result-level-card {
  --result-card-border: #dbe2ea;
  --result-card-bg: #f8fafc;
  --result-card-hover-border: #93c5fd;
  --result-card-hover-bg: #eff6ff;
  --result-card-shadow: rgba(59, 130, 246, 0.14);
  --result-card-title: #1e293b;
  --result-card-desc: #64748b;
  --result-card-active-border: #3b82f6;
  --result-card-active-bg: #eff6ff;
  --result-card-active-shadow: rgba(59, 130, 246, 0.3);
  --result-card-active-title: #1e3a8a;
  --result-card-active-desc: #1e40af;
  --result-card-focus-ring: rgba(59, 130, 246, 0.24);
  border: 1px solid var(--result-card-border);
  border-radius: 8px;
  padding: 10px;
  background: var(--result-card-bg);
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease,
    box-shadow 0.2s ease;
}

.result-level-card:hover {
  border-color: var(--result-card-hover-border);
  background: var(--result-card-hover-bg);
  box-shadow: 0 6px 16px -14px var(--result-card-shadow);
}

.result-level-card--active {
  border-color: var(--result-card-active-border);
  background: var(--result-card-active-bg);
  box-shadow:
    0 0 0 1px var(--result-card-focus-ring),
    0 10px 24px -18px var(--result-card-active-shadow);
}

.result-level-name {
  color: var(--result-card-title);
  font-weight: 700;
  font-size: 14px;
}

.result-level-remark {
  margin-top: 4px;
  color: var(--result-card-desc);
  font-size: 12px;
  line-height: 1.45;
}

.result-level-card--active .result-level-name {
  color: var(--result-card-active-title);
}

.result-level-card--active .result-level-remark {
  color: var(--result-card-active-desc);
}

.result-level-card--tone-success {
  --result-card-border: #bbf7d0;
  --result-card-bg: #f0fdf4;
  --result-card-hover-border: #86efac;
  --result-card-hover-bg: #dcfce7;
  --result-card-shadow: rgba(22, 163, 74, 0.14);
  --result-card-title: #166534;
  --result-card-desc: #3f6212;
  --result-card-active-border: #22c55e;
  --result-card-active-bg: #dcfce7;
  --result-card-active-shadow: rgba(34, 197, 94, 0.32);
  --result-card-active-title: #166534;
  --result-card-active-desc: #365314;
  --result-card-focus-ring: rgba(34, 197, 94, 0.24);
}

.result-level-card--tone-primary {
  --result-card-border: #bfdbfe;
  --result-card-bg: #eff6ff;
  --result-card-hover-border: #93c5fd;
  --result-card-hover-bg: #dbeafe;
  --result-card-shadow: rgba(37, 99, 235, 0.14);
  --result-card-title: #1e3a8a;
  --result-card-desc: #1e40af;
  --result-card-active-border: #3b82f6;
  --result-card-active-bg: #dbeafe;
  --result-card-active-shadow: rgba(59, 130, 246, 0.3);
  --result-card-active-title: #1d4ed8;
  --result-card-active-desc: #1e40af;
  --result-card-focus-ring: rgba(59, 130, 246, 0.24);
}

.result-level-card--tone-warning {
  --result-card-border: #fde68a;
  --result-card-bg: #fffbeb;
  --result-card-hover-border: #fcd34d;
  --result-card-hover-bg: #fef3c7;
  --result-card-shadow: rgba(217, 119, 6, 0.14);
  --result-card-title: #92400e;
  --result-card-desc: #b45309;
  --result-card-active-border: #f59e0b;
  --result-card-active-bg: #fef3c7;
  --result-card-active-shadow: rgba(245, 158, 11, 0.3);
  --result-card-active-title: #92400e;
  --result-card-active-desc: #92400e;
  --result-card-focus-ring: rgba(245, 158, 11, 0.24);
}

.result-level-card--tone-danger {
  --result-card-border: #fecaca;
  --result-card-bg: #fef2f2;
  --result-card-hover-border: #fca5a5;
  --result-card-hover-bg: #fee2e2;
  --result-card-shadow: rgba(220, 38, 38, 0.14);
  --result-card-title: #991b1b;
  --result-card-desc: #b91c1c;
  --result-card-active-border: #ef4444;
  --result-card-active-bg: #fee2e2;
  --result-card-active-shadow: rgba(239, 68, 68, 0.3);
  --result-card-active-title: #991b1b;
  --result-card-active-desc: #b91c1c;
  --result-card-focus-ring: rgba(239, 68, 68, 0.24);
}

.result-level-card--tone-info {
  --result-card-border: #dbe3f0;
  --result-card-bg: #f8fafc;
  --result-card-hover-border: #cbd5e1;
  --result-card-hover-bg: #f1f5f9;
  --result-card-shadow: rgba(71, 85, 105, 0.12);
  --result-card-title: #334155;
  --result-card-desc: #64748b;
  --result-card-active-border: #94a3b8;
  --result-card-active-bg: #f1f5f9;
  --result-card-active-shadow: rgba(148, 163, 184, 0.32);
  --result-card-active-title: #334155;
  --result-card-active-desc: #475569;
  --result-card-focus-ring: rgba(148, 163, 184, 0.24);
}

.upload-tip {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}

.submit-abnormal-reminder {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.submit-abnormal-reminder-table {
  :deep(.el-table__header th) {
    background: #f8fafc;
  }
}

.submit-abnormal-reminder-checkbox {
  color: #334155;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .detail-submit-head {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .detail-submit-record-item {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .summary-grid,
  .item-meta-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .result-level-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .detail-submit-record-item {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .summary-grid,
  .item-meta-grid,
  .result-level-grid {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
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

.task-detail-layout {
  min-height: auto;
}

:deep(.inspection-task-detail-dialog .el-dialog) {
  max-width: 1120px;
}

:deep(.inspection-task-detail-dialog .el-dialog__body) {
  max-height: 72vh;
  overflow-y: auto;
  padding-top: 10px;
}

.target-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.task-process-empty {
  min-height: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
  background: #f8fafc;
}

.task-process-panel {
  position: relative;
  min-height: 520px;
  height: 100%;
  border: 1px solid #dbe5f1;
  border-radius: 12px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 38%);
  overflow: visible;
}

.task-process-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px 8px;
  border-bottom: 1px solid #edf2f7;
}

.task-process-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e3a5f;
}

.task-process-stamp {
  position: absolute;
  right: 10px;
  top: 50px;
  width: 150px;
  pointer-events: none;
  opacity: 0.96;
}

.task-process-scroll {
  height: calc(520px - 52px);

  :deep(.el-scrollbar__view) {
    box-sizing: border-box;
    padding: 0 10px 0 20px;
  }

  :deep(.el-timeline) {
    margin-left: 4px;
  }
}

.task-process-empty-inner {
  min-height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-tabs {
  :deep(.el-tabs__content) {
    padding-top: 8px;
  }
}

.process-pane {
  height: 520px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.process-empty-wrap {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
  background: #f8fafc;
}

.record-table {
  min-height: 320px;
}
</style>
