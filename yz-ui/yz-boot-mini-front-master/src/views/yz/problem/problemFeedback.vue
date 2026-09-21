<template>
  <div class="river-page">
    <ContentWrap class="feedback-query-wrap">
      <el-form
        class="-mb-15px feedback-query-form"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="80px"
      >
        <el-form-item label="设施名称" prop="facilityName">
          <el-input
            v-model="queryParams.facilityName"
            placeholder="请输入关键词"
            clearable
            @keyup.enter="handleSearch"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item label="设施类型" prop="facilityType">
          <el-select v-model="queryParams.facilityType" placeholder="请选择设施类型" clearable class="!w-240px">
            <el-option v-for="item in facilityTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择日期" prop="createTime">
          <el-date-picker
            v-model="createDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            clearable
            class="!w-320px"
          />
        </el-form-item>
        <el-form-item label="问题类型" prop="feedbackType">
          <el-select v-model="queryParams.feedbackType" placeholder="请选择问题类型" clearable class="!w-240px">
            <el-option v-for="item in feedbackTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="反馈进度" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择反馈进度" clearable class="!w-240px">
            <el-option v-for="item in progressOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><Icon icon="ep:search" class="mr-5px" /> 查询</el-button>
          <el-button @click="handleReset"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
          <el-button type="success" plain @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="feedback-table-wrap">
      <el-table v-loading="tableLoading" :data="tableData" class="feedback-table">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="设施名称" align="center" min-width="140" prop="facilityName" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.facilityName || row.referenceName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="设施类型" align="center" min-width="120" prop="referenceType" show-overflow-tooltip>
          <template #default="{ row }">
            {{ facilityTypeLabel(row.referenceType) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="问题类型" align="center" min-width="120" prop="feedbackTypeLabel" show-overflow-tooltip />
<!--        <el-table-column label="反馈内容" align="center" min-width="200" prop="feedbackContent" show-overflow-tooltip />-->
<!--        <el-table-column label="反馈人" align="center" min-width="110" prop="feedbackPerson" show-overflow-tooltip />
        <el-table-column label="图片/视频" align="center" min-width="180">
          <template #default="{ row }">
            <template v-if="row.uploadedFiles && row.uploadedFiles.length">
              <el-button
                v-for="(url, idx) in row.uploadedFiles"
                :key="`${url}-${idx}`"
                type="primary"
                size="small"
                @click="openFile(url)"
              >
                {{ fileLabel(url, idx) }}
              </el-button>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>-->
        <el-table-column label="具体位置" align="center" min-width="220" prop="issueSpecificLocation" show-overflow-tooltip />
        <el-table-column label="反馈日期" align="center" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="处理进度" align="center" min-width="120">
          <template #default="{ row }">
            <el-text :type="progressTextType(row.statusLabel)">{{ row.statusLabel || '-' }}</el-text>
          </template>
        </el-table-column>
        <el-table-column label="处理人" align="center" min-width="110" prop="assignedPersonName" show-overflow-tooltip />
        <el-table-column label="是否超时" align="center" min-width="110">
          <template #default="{ row }">
            <template v-if="isRowProcessing(row)">
              <el-tag :type="isOverdue(row) ? 'danger' : 'success'">
                {{ isOverdue(row) ? '已超时' : '未超时' }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="催办状态" align="center" min-width="110">
          <template #default="{ row }">
            <template v-if="isRowProcessing(row)">
              <el-tag :type="row.expedited === 1 ? 'warning' : 'info'">
                {{ row.expedited === 1 ? '已催办' : '未催办' }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="showRemindButton(row)" link type="warning" @click="handleRemind(row)">催办</el-button>
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="fetchTable"
      />
    </ContentWrap>

    <el-dialog
      v-model="detailDialogVisible"
      class="feedback-detail-dialog"
      title="反馈详情"
      width="980px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-skeleton :loading="detailLoading" animated>
        <template #default>
          <el-form label-width="120px" label-position="left">
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="设施名称" class="pf-detail-item-readonly">
                  <el-input
                    class="pf-detail-readonly-input"
                    :model-value="resolveDetailFacilityName(detailData) || '—'"
                    readonly
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="设施类型" class="pf-detail-item-readonly">
                  <el-input
                    class="pf-detail-readonly-input"
                    :model-value="facilityTypeLabel(detailData?.referenceType) || '—'"
                    readonly
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="设施编码" class="pf-detail-item-readonly">
                  <el-input
                    class="pf-detail-readonly-input"
                    :model-value="resolveDetailFacilityCode(detailData) || '—'"
                    readonly
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="问题类型" class="pf-detail-item-readonly">
                  <el-input
                    class="pf-detail-readonly-input"
                    :model-value="detailData?.feedbackTypeLabel || '—'"
                    readonly
                  />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="反馈内容" class="pf-detail-item-readonly">
                  <el-input
                    class="pf-detail-readonly-input pf-detail-readonly-input--textarea"
                    type="textarea"
                    :autosize="{ minRows: 3, maxRows: 12 }"
                    :model-value="detailData?.feedbackContent || '—'"
                    readonly
                  />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="图片/视频">
                  <template v-if="detailData?.uploadedFiles && detailData.uploadedFiles.length">
                    <div class="media-grid">
                      <template v-for="(url, idx) in detailData.uploadedFiles" :key="`${url}-${idx}`">
                        <el-image
                          v-if="isImage(url)"
                          class="media-thumb"
                          :src="url"
                          :alt="fileLabel(url, idx)"
                          :title="fileLabel(url, idx)"
                          :preview-src-list="feedbackImagePreviewList"
                          :initial-index="feedbackImagePreviewList.indexOf(url)"
                          preview-teleported
                          fit="cover"
                        />
                        <div v-else class="media-video media-thumb" :title="fileLabel(url, idx)" @click="openFile(url)">
                          <video class="media-video-el" :src="url" muted playsinline></video>
                          <div class="media-video-mask">
                            <Icon icon="ep:video-play" />
                            <span class="media-video-text">视频</span>
                          </div>
                        </div>
                      </template>
                    </div>
                  </template>
                  <span v-else>-</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="反馈时间" class="pf-detail-item-readonly">
                  <el-input
                    class="pf-detail-readonly-input"
                    :model-value="formatDateTime(detailData?.createTime) || '—'"
                    readonly
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="具体位置" class="pf-detail-item-readonly">
                  <el-input
                    class="pf-detail-readonly-input pf-detail-readonly-input--textarea"
                    type="textarea"
                    :autosize="{ minRows: 2, maxRows: 8 }"
                    :model-value="detailData?.issueSpecificLocation || '—'"
                    readonly
                  />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="问题点位">
                  <TiandituGeoJsonPreview
                    :geo-json="problemPointGeoJson"
                    :active="detailDialogVisible"
                    :height="260"
                    :zoom="15"
                    :label-text="problemPointLabel"
                    highlight
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="反馈人" class="pf-detail-item-readonly">
                  <el-input
                    class="pf-detail-readonly-input"
                    :model-value="detailData?.feedbackPerson || '—'"
                    readonly
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="处理进度" class="pf-detail-item-readonly">
                  <el-input
                    class="pf-detail-readonly-input"
                    :model-value="detailData?.statusLabel || '—'"
                    readonly
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">处理流程</el-divider>
            <el-timeline class="task-timeline">
              <el-timeline-item
                :type="hasAudit ? 'success' : showAuditForm ? 'primary' : 'info'"
                :timestamp="hasAudit ? formatDateTime(auditTask?.reviewerPersonTime || auditTask?.statusDescriptionTime) : ''"
                placement="top"
              >
                <div class="timeline-title-row">
                  <div class="timeline-title">受理指派</div>
                  <div class="timeline-meta">
                    <el-tag v-if="hasAudit" type="success" size="small">已受理</el-tag>
                    <el-tag v-else-if="showAuditForm" type="primary" size="small">待受理</el-tag>
                    <el-tag v-else type="info" size="small">未开始</el-tag>
                  </div>
                </div>

                <template v-if="hasAudit">
                  <el-row :gutter="12" class="timeline-content">
                    <el-col :span="12">
                      <el-form-item label="受理人">
                        <span>{{ auditTask?.reviewerPersonName || '-' }}</span>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="受理时间">
                        <span>{{ formatDateTime(auditTask?.reviewerPersonTime) || '-' }}</span>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="指派人">
                        <span>{{ auditTask?.assignedPersonName || '-' }}</span>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="计划完成时间">
                        <span>{{ formatDateTime(auditTask?.plannedCompletionTime) || '-' }}</span>
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="回复内容">
                        <span>{{ auditTask?.statusDescription || '-' }}</span>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </template>

                <template v-else-if="showAuditForm">
                  <el-row :gutter="12" class="timeline-content">
                    <el-col :span="12">
                      <el-form-item label="指派人" required>
                        <el-select
                          v-model="auditForm.assignedPersonId"
                          placeholder="请选择"
                          clearable
                          class="!w-240px"
                          popper-class="assign-user-popper"
                        >
                          <el-option
                            v-for="item in assignUserOptions"
                            :key="item.id"
                            :label="formatAssignUserLabel(item)"
                            :value="item.id"
                          >
                            <div class="assign-option">
                              <div class="assign-option-main">
                                <span class="assign-option-name">{{ item.nickname }}</span>
                              </div>
                              <div class="assign-option-meta">
                                <span class="assign-option-role">{{ item.roleNames || '未分配角色' }}</span>
                              </div>
                            </div>
                          </el-option>
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col v-if="auditForm.assignedPersonId" :span="12">
                      <el-form-item label="计划完成时间" required>
                        <el-date-picker
                          v-model="auditForm.plannedCompletionTime"
                          type="datetime"
                          placeholder="请选择"
                          format="YYYY-MM-DD HH:mm"
                          value-format="YYYY-MM-DD HH:mm:ss"
                          :disabled-date="disablePlannedCompletionDate"
                          :disabled-time="disablePlannedCompletionTime"
                          class="!w-240px"
                        />
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="回复内容" required>
                        <el-input
                          v-model="auditForm.statusDescription"
                          type="textarea"
                          :rows="3"
                          placeholder="请输入回复内容"
                          maxlength="500"
                          show-word-limit
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </template>
              </el-timeline-item>

              <el-timeline-item
                v-if="detailData"
                :type="hasProcess ? 'success' : showProcessForm ? 'primary' : 'info'"
                :timestamp="hasProcess ? formatDateTime(processTask?.processingTime) : ''"
                placement="top"
              >
                <div class="timeline-title-row">
                  <div class="timeline-title">处理完成</div>
                  <div class="timeline-meta">
                    <el-tag v-if="hasProcess" type="success" size="small">已处理</el-tag>
                    <el-tag v-else-if="showProcessForm" type="primary" size="small">待处理</el-tag>
                    <el-tag v-else type="info" size="small">未开始</el-tag>
                  </div>
                </div>

                <template v-if="hasProcess">
                  <el-row :gutter="12" class="timeline-content">
                    <el-col :span="12">
                      <el-form-item label="处理人">
                        <span>{{ processTask?.assignedPersonName || '-' }}</span>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="处理时间">
                        <span>{{ formatDateTime(processTask?.processingTime) || '-' }}</span>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="是否无需处理">
                        <span>
                          {{
                            processTask?.noNeedHandle === true
                              ? '是'
                              : processTask?.noNeedHandle === false
                                ? '否'
                                : '-'
                          }}
                        </span>
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="处理结果">
                        <span>{{ processTask?.resolutionDescription || '-' }}</span>
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="处理图片">
                        <template v-if="processTask?.problemHandleImages && processTask.problemHandleImages.length">
                          <el-image
                            v-for="(url, idx) in processTask.problemHandleImages"
                            :key="`${url}-${idx}`"
                            class="media-thumb"
                            :src="url"
                            :preview-src-list="processTask.problemHandleImages"
                            preview-teleported
                            fit="cover"
                          />
                        </template>
                        <span v-else>-</span>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </template>

                <template v-else-if="showProcessForm">
                  <el-row :gutter="12" class="timeline-content">
                    <el-col :span="12">
                      <el-form-item label="处理结果" required>
                        <el-radio-group v-model="processForm.handleResult">
                          <el-radio label="无需处理">无需处理</el-radio>
                          <el-radio label="已处理">已处理</el-radio>
                        </el-radio-group>
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="处理描述" required>
                        <el-input
                          v-model="processForm.resolutionDescription"
                          type="textarea"
                          :rows="3"
                          placeholder="请输入处理描述"
                          maxlength="1000"
                          show-word-limit
                        />
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="处理图片">
                        <el-upload
                          v-model:file-list="processUploadFileList"
                          :action="uploadUrl"
                          :http-request="httpRequest"
                          list-type="picture-card"
                          :limit="3"
                          accept=".jpg,.jpeg,.png"
                          :before-upload="beforeImageUpload"
                          :on-success="handleProcessUploadSuccess"
                          :on-remove="handleProcessUploadRemove"
                        >
                          <Icon icon="ep:plus" />
                        </el-upload>
                        <div class="upload-tip">最多上传3张图片，每张不超过5MB，仅支持jpg/png</div>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </template>
              </el-timeline-item>

              <el-timeline-item
                v-if="detailData"
                :type="hasVerify ? (detailIsRejected ? 'danger' : 'success') : showVerifyForm ? 'warning' : 'info'"
                :timestamp="hasVerify ? formatDateTime(verifyTask?.verificationTime || verifyTask?.completionTime) : ''"
                placement="top"
              >
                <div class="timeline-title-row">
                  <div class="timeline-title">结果核验</div>
                  <div class="timeline-meta">
                    <el-tag
                      v-if="hasVerify"
                      :type="detailIsRejected ? 'danger' : 'success'"
                      size="small"
                    >
                      {{ detailIsRejected ? '已驳回' : '已办结' }}
                    </el-tag>
                    <el-tag v-else-if="showVerifyForm" type="warning" size="small">待核验</el-tag>
                    <el-tag v-else type="info" size="small">未开始</el-tag>
                  </div>
                </div>

                <template v-if="hasVerify">
                  <el-row :gutter="12" class="timeline-content">
                    <el-col :span="12">
                      <el-form-item label="核验人">
                        <span>{{ verifyTask?.verificationName || '-' }}</span>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="核验时间">
                        <span>{{ formatDateTime(verifyTask?.verificationTime || verifyTask?.completionTime) || '-' }}</span>
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="核验结果">
                        <span>{{ verifyTask?.verificationResult || '-' }}</span>
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="核验图片">
                        <template v-if="verifyTask?.verifyHandleImages && verifyTask.verifyHandleImages.length">
                          <el-image
                            v-for="(url, idx) in verifyTask.verifyHandleImages"
                            :key="`${url}-${idx}`"
                            class="media-thumb"
                            :src="url"
                            :preview-src-list="verifyTask.verifyHandleImages"
                            preview-teleported
                            fit="cover"
                          />
                        </template>
                        <span v-else>-</span>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </template>

                <template v-else-if="showVerifyForm">
                  <el-row :gutter="12" class="timeline-content">
                    <el-col :span="12">
                      <el-form-item label="核验结论" required>
                        <el-radio-group v-model="verifyForm.solved">
                          <el-radio :label="false">问题驳回</el-radio>
                          <el-radio :label="true">问题已解决</el-radio>
                        </el-radio-group>
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="核验结果" required>
                        <el-input
                          v-model="verifyForm.verificationResult"
                          type="textarea"
                          :rows="3"
                          placeholder="请输入核验结果"
                          maxlength="1000"
                          show-word-limit
                        />
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="核验图片">
                        <el-upload
                          v-model:file-list="verifyUploadFileList"
                          :action="uploadUrl"
                          :http-request="httpRequest"
                          list-type="picture-card"
                          :limit="3"
                          accept=".jpg,.jpeg,.png"
                          :before-upload="beforeImageUpload"
                          :on-success="handleVerifyUploadSuccess"
                          :on-remove="handleVerifyUploadRemove"
                        >
                          <Icon icon="ep:plus" />
                        </el-upload>
                        <div class="upload-tip">最多上传3张图片，每张不超过5MB，仅支持jpg/png</div>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </template>
              </el-timeline-item>
            </el-timeline>
          </el-form>
        </template>
      </el-skeleton>

      <template #footer>
        <el-button @click="detailDialogVisible = false">取消</el-button>
        <el-button v-if="showAuditForm" type="primary" :loading="detailSubmitting" @click="submitAudit">提交</el-button>
        <el-button v-if="showProcessForm" type="primary" :loading="detailSubmitting" @click="submitProcess">提交</el-button>
        <el-button v-if="showVerifyForm" type="primary" :loading="detailSubmitting" @click="submitVerify">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import type { UploadFile, UploadFiles } from 'element-plus'
import dayjs from 'dayjs'
import download from '@/utils/download'
import { useUpload } from '@/components/UploadFile/src/useUpload'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import { useUserStore } from '@/store/modules/user'
import { getRiverDict, type DictDataItemRespVO } from '@/api/gis/riverChannel'
import {
  auditAndAssignProblemFeedback,
  deleteProblemFeedback,
  exportProblemFeedbackExcel,
  getProblemFeedbackDetail,
  getProblemFeedbackPage,
  getProblemFeedbackUsers,
  notifyProblemFeedbackAssignSms,
  notifyProblemFeedbackFinishSms,
  notifyProblemFeedbackOverdueSms,
  notifyProblemFeedbackRejectSms,
  processProblemFeedback,
  type ProblemFeedbackPageReqVO,
  type ProblemFeedbackPageRespVO,
  type ProblemFeedbackDetailRespVO,
  type ProblemFeedbackNotifyRespVO,
  type ProblemFeedbackStatusTaskRespVO,
  type ProblemFeedbackUserSimpleRespVO,
  verifyProblemFeedback
} from '@/api/yz/problemFeedback'

const queryParams = reactive<ProblemFeedbackPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  facilityName: '',
  facilityType: '',
  feedbackType: '',
  status: ''
})

const queryFormRef = ref<FormInstance>()
const tableData = ref<ProblemFeedbackPageRespVO[]>([])
const total = ref(0)
const tableLoading = ref(false)

const feedbackTypeOptions = ref<DictDataItemRespVO[]>([])
const progressOptions = ref<DictDataItemRespVO[]>([])
const facilityTypeOptions = ref<DictDataItemRespVO[]>([])
const createDateRange = ref<string[]>([])

const userStore = useUserStore()
const currentUserId = computed(() => userStore.getUser.id)
const roleCodes = computed(() => userStore.getRoles || [])
const isAdminRole = computed(() => {
  const set = new Set(roleCodes.value)
  return (
    set.has('super_admin') ||
    set.has('tenant_admin') ||
    set.has('crm_admin') ||
    set.has('sladmin') ||
    set.has('yzadmin') ||
    set.has('river_head')
  )
})

// 短信短链尚未接入完成，先在该页面临时禁用短信发送（保留代码便于后续恢复）
const SMS_ENABLED = true

const STATUS_REJECTED = 1
const STATUS_PROCESSING = 2

const feedbackTypeLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  for (const item of feedbackTypeOptions.value) {
    if (item?.value) map[item.value] = item.label || item.value
  }
  return map
})

const progressLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  for (const item of progressOptions.value) {
    if (item?.value) map[item.value] = item.label || item.value
  }
  return map
})

const facilityTypeLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  for (const item of facilityTypeOptions.value) {
    if (item?.value) map[item.value] = item.label || item.value
  }
  return map
})

const facilityTypeLabel = (value?: string) => {
  if (!value) return ''
  return facilityTypeLabelMap.value[value] || value
}

const resolveDetailFacilityName = (data?: ProblemFeedbackDetailRespVO) => {
  if (!data) return ''
  return data.facilityName || data.referenceName || ''
}

const resolveDetailFacilityCode = (data?: ProblemFeedbackDetailRespVO) => {
  if (!data) return ''
  return data.facilityCode || ''
}

const formatDateTime = (val?: string) => {
  if (!val) return ''
  const d = dayjs(val)
  return d.isValid() ? d.format('YYYY年MM月DD日 HH:mm') : val
}

const isVideo = (url: string) => {
  return /\.(mp4|mov|avi|wmv|flv|mkv|webm)$/i.test(url)
}

const isImage = (url: string) => {
  return /\.(png|jpe?g|gif|bmp|webp)$/i.test(url)
}

const fileLabel = (url: string, idx: number) => {
  return isVideo(url) ? `视频${idx + 1}` : `图${idx + 1}`
}

const openFile = (url: string) => {
  if (!url) return
  window.open(url, '_blank')
}

const progressTextType = (label?: string) => {
  if (!label) return 'info'
  if (label.includes('待核验')) return 'warning'
  if (label.includes('处理') || label.includes('进行')) return 'primary'
  if (label.includes('办结') || label.includes('完成')) return 'success'
  if (label.includes('驳回') || label.includes('拒绝')) return 'danger'
  return 'info'
}

const feedbackImagePreviewList = computed(() => {
  return (detailData.value?.uploadedFiles || []).filter((url) => isImage(url))
})

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailSubmitting = ref(false)
const detailData = ref<ProblemFeedbackDetailRespVO>()

const toNumber = (value: unknown) => {
  const num = typeof value === 'number' ? value : Number(value)
  if (!Number.isFinite(num)) return null
  return num
}

/** 反馈主单状态归一化为数字，避免接口返回字符串 "0" 导致 === 0 失败 */
const normalizedFeedbackStatus = (value: unknown): number | null => {
  if (value === undefined || value === null || value === '') return null
  const n = typeof value === 'number' ? value : Number(value)
  return Number.isFinite(n) ? n : null
}

/** 雪花 ID 等在接口中可能为 string/number，统一字符串比较 */
const isSameId = (a: unknown, b: unknown) => String(a ?? '') === String(b ?? '')

const LOG_PREFIX = '[problemFeedback]'

const problemPointGeoJson = computed(() => {
  const lng = toNumber(detailData.value?.problemLongitude)
  const lat = toNumber(detailData.value?.problemLatitude)
  if (lng === null || lat === null) return ''
  if (Math.abs(lng) > 180 || Math.abs(lat) > 90) return ''
  return JSON.stringify({
    type: 'FeatureCollection',
    features: [
      {
        type: 'Feature',
        properties: {},
        geometry: { type: 'Point', coordinates: [lng, lat] }
      }
    ]
  })
})

const problemPointLabel = computed(() => {
  const raw = String(detailData.value?.issueSpecificLocation || '').trim()
  if (!raw) return '问题点'
  return raw.length > 16 ? `${raw.slice(0, 16)}...` : raw
})

const auditForm = reactive<{
  statusDescription: string
  assignedPersonId?: number
  plannedCompletionTime?: string
}>({
  statusDescription: '',
  assignedPersonId: undefined,
  plannedCompletionTime: '',
})
const processForm = reactive<{ handleResult: string; resolutionDescription: string; uploadedFiles: string[] }>({
  handleResult: '',
  resolutionDescription: '',
  uploadedFiles: []
})
const verifyForm = reactive<{ solved: boolean | undefined; verificationResult: string; uploadedFiles: string[] }>({
  solved: undefined,
  verificationResult: '',
  uploadedFiles: []
})

const assignUserOptions = ref<ProblemFeedbackUserSimpleRespVO[]>([])

/** 指派人下拉：仅展示「普通角色」或昵称为「陈丽」的用户 */
const isAssignableUser = (user: ProblemFeedbackUserSimpleRespVO) => {
  if (user.roleNames === '普通角色') return true
  const displayName = (user.nickname || user.name || user.username || '').trim()
  return displayName === '陈丽'
}

const filterAssignableUsers = (users: ProblemFeedbackUserSimpleRespVO[]) =>
  (users || []).filter(isAssignableUser)

const formatAssignUserLabel = (item?: ProblemFeedbackUserSimpleRespVO) => {
  if (!item) return ''
  const roleText = item.roleNames || '未分配角色'
  return roleText ? `${item.nickname}（${roleText}）` : item.nickname
}

const { uploadUrl, httpRequest } = useUpload('problem-feedback')
const processUploadFileList = ref<UploadFile[]>([])
const verifyUploadFileList = ref<UploadFile[]>([])

// 状态任务与问题反馈是一对一关系，详情仅返回一条任务记录
const statusTask = computed<ProblemFeedbackStatusTaskRespVO | undefined>(() => {
  const list = detailData.value?.statusTasks || []
  return list.length ? list[0] : undefined
})

const auditTask = computed(() => statusTask.value)
const processTask = computed(() => statusTask.value)
const verifyTask = computed(() => statusTask.value)

const hasAudit = computed(() => {
  const t = statusTask.value
  if (!t) return false
  return !!t.statusDescriptionTime || !!t.statusDescription
})
const hasProcess = computed(() => {
  const t = statusTask.value
  if (!t) return false
  return !!t.processingTime || !!t.resolutionDescription || (t.problemHandleImages && t.problemHandleImages.length > 0)
})
const hasVerify = computed(() => {
  const t = statusTask.value
  if (!t) return false
  return !!t.completionTime || !!t.verificationResult || (t.verifyHandleImages && t.verifyHandleImages.length > 0)
})

const currentAssigneeId = computed(() => statusTask.value?.assignedPersonId)

/** 详情主单状态（数字），供表单显隐统一使用 */
const detailStatus = computed(() => normalizedFeedbackStatus(detailData.value?.status))

const detailIsRejected = computed(() => detailStatus.value === STATUS_REJECTED)

const isRowProcessing = (row: ProblemFeedbackPageRespVO) =>
  normalizedFeedbackStatus(row?.status) === STATUS_PROCESSING

const showAuditForm = computed(() => {
  const st = detailStatus.value
  const admin = isAdminRole.value
  return !!detailData.value && st === 0 && admin
})
const showProcessForm = computed(() => {
  const st = detailStatus.value
  return (
    !!detailData.value &&
    st === STATUS_PROCESSING &&
    isSameId(currentAssigneeId.value, currentUserId.value)
  )
})
const showVerifyForm = computed(() => {
  const st = detailStatus.value
  return !!detailData.value && st === 3 && isAdminRole.value
})

watch(
  () => auditForm.assignedPersonId,
  (val) => {
    if (!val) {
      auditForm.plannedCompletionTime = ''
    }
  }
)

const disablePlannedCompletionDate = (date: Date) => {
  return dayjs(date).isBefore(dayjs().startOf('day'))
}

const disablePlannedCompletionTime = (date: Date) => {
  const selected = dayjs(date)
  const now = dayjs()
  if (!selected.isValid()) return {}
  if (!selected.isSame(now, 'day')) return {}
  const currentHour = now.hour()
  const currentMinute = now.minute()
  return {
    disabledHours: () => Array.from({ length: currentHour }, (_, i) => i),
    disabledMinutes: (hour: number) => {
      if (hour < currentHour) return Array.from({ length: 60 }, (_, i) => i)
      if (hour === currentHour) return Array.from({ length: currentMinute + 1 }, (_, i) => i)
      return []
    }
  }
}

const isOverdue = (row: ProblemFeedbackPageRespVO) => {
  if (!row || normalizedFeedbackStatus(row.status) !== STATUS_PROCESSING) return false
  if (!row.plannedCompletionTime) return false
  const planned = dayjs(row.plannedCompletionTime)
  if (!planned.isValid()) return false
  return dayjs().isAfter(planned)
}

const showRemindButton = (row: ProblemFeedbackPageRespVO) => {
  if (!isAdminRole.value) return false
  if (!row || normalizedFeedbackStatus(row.status) !== STATUS_PROCESSING) return false
  if (!isOverdue(row)) return false
  if (row.expedited === 1) return false
  if (row.assignedPersonId != null && isSameId(row.assignedPersonId, currentUserId.value)) return false
  return true
}

const handleRemind = (row: ProblemFeedbackPageRespVO) => {
  if (!row?.id) return
  ElMessageBox.confirm(`确认对该问题进行催办并发送短信给处理人吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      if (SMS_ENABLED) {
        void sendNotifyOverdueSms(row.id)
      }
    })
    .catch(() => {})
}

const beforeImageUpload = (rawFile: File) => {
  const isJpgOrPng = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
  if (!isJpgOrPng) {
    ElMessage.error('仅支持上传jpg/png格式图片')
    return false
  }
  const isLt5M = rawFile.size / 1024 / 1024 <= 5
  if (!isLt5M) {
    ElMessage.error('单张图片大小不能超过5MB')
    return false
  }
  return true
}

const extractUploadUrl = (res: any) => {
  if (!res) return ''
  if (typeof res === 'string') return res
  if (typeof res?.data === 'string') return res.data
  if (typeof res?.url === 'string') return res.url
  return ''
}


const handleProcessUploadSuccess = (res: any, file: UploadFile, fileList: UploadFiles) => {
  const url = extractUploadUrl(res)
  if (url) {
    file.url = url
    processForm.uploadedFiles = Array.from(new Set([...processForm.uploadedFiles, url]))
  }
  processUploadFileList.value = fileList as UploadFile[]
}
const handleProcessUploadRemove = (file: UploadFile) => {
  const url = file.url || ''
  if (url) {
    processForm.uploadedFiles = processForm.uploadedFiles.filter((it) => it !== url)
  }
}

const handleVerifyUploadSuccess = (res: any, file: UploadFile, fileList: UploadFiles) => {
  const url = extractUploadUrl(res)
  if (url) {
    file.url = url
    verifyForm.uploadedFiles = Array.from(new Set([...verifyForm.uploadedFiles, url]))
  }
  verifyUploadFileList.value = fileList as UploadFile[]
}
const handleVerifyUploadRemove = (file: UploadFile) => {
  const url = file.url || ''
  if (url) {
    verifyForm.uploadedFiles = verifyForm.uploadedFiles.filter((it) => it !== url)
  }
}

const fetchTable = async () => {
  tableLoading.value = true
  try {
    const statusStr = queryParams.status ? String(queryParams.status) : ''
    const statusParam = /^\d+$/.test(statusStr) ? Number(statusStr) : undefined
    const createTimeParam =
      Array.isArray(createDateRange.value) && createDateRange.value.length === 2
        ? `${createDateRange.value[0]},${createDateRange.value[1]}`
        : undefined
    const res = await getProblemFeedbackPage({ ...queryParams, status: statusParam, createTime: createTimeParam })
    tableData.value = (res?.list || []).map((item) => {
      const cloned = { ...item }
      if (!cloned.feedbackTypeLabel && cloned.feedbackType) {
        cloned.feedbackTypeLabel = feedbackTypeLabelMap.value[cloned.feedbackType] || cloned.feedbackType
      }
      if (!cloned.statusLabel) {
        const statusValue = cloned.status !== undefined && cloned.status !== null ? String(cloned.status) : ''
        cloned.statusLabel = progressLabelMap.value[statusValue] || cloned.statusLabel
      }
      return cloned
    })
    total.value = res?.total || 0
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNo = 1
  fetchTable()
}

const handleReset = () => {
  queryParams.pageNo = 1
  queryParams.facilityName = ''
  queryParams.facilityType = ''
  queryParams.feedbackType = ''
  queryParams.status = ''
  createDateRange.value = []
  queryFormRef.value?.clearValidate?.()
  fetchTable()
}

const handleExport = async () => {
  try {
    const statusStr = queryParams.status ? String(queryParams.status) : ''
    const statusParam = /^\d+$/.test(statusStr) ? Number(statusStr) : undefined
    const createTimeParam =
      Array.isArray(createDateRange.value) && createDateRange.value.length === 2
        ? `${createDateRange.value[0]},${createDateRange.value[1]}`
        : undefined
    const data = await exportProblemFeedbackExcel({ ...queryParams, status: statusParam, createTime: createTimeParam })
    download.excel(data, '问题反馈.xls')
  } catch {
    // 下载失败时，错误提示由全局拦截器处理
  }
}

const handleDelete = async (row: ProblemFeedbackPageRespVO) => {
  if (!row?.id) return
  await ElMessageBox.confirm(`确认删除该条问题反馈吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteProblemFeedback(row.id)
  ElMessage.success('删除成功')
  fetchTable()
}

const openDetailDialog = async (id: string | number) => {
  const idStr = String(id)
  console.log(LOG_PREFIX, 'openDetail:start', {
    feedbackId: idStr,
    isAdminRole: isAdminRole.value,
    roles: roleCodes.value
  })
  detailDialogVisible.value = true
  detailLoading.value = true
  detailData.value = undefined
  assignUserOptions.value = []
  try {
    const data = await getProblemFeedbackDetail(idStr)
    detailData.value = data
    const st = normalizedFeedbackStatus(data?.status)
    console.log(LOG_PREFIX, 'openDetail:loaded', {
      feedbackId: idStr,
      statusRaw: data?.status,
      statusNormalized: st,
      isAdminRole: isAdminRole.value,
      willLoadAssignUsers: st === 0 && isAdminRole.value,
      showAuditForm: !!data && st === 0 && isAdminRole.value
    })
    auditForm.statusDescription = ''
    auditForm.assignedPersonId = undefined
    auditForm.plannedCompletionTime = ''
    processForm.handleResult = ''
    processForm.resolutionDescription = ''
    processForm.uploadedFiles = []
    processUploadFileList.value = []
    verifyForm.solved = undefined
    verifyForm.verificationResult = ''
    verifyForm.uploadedFiles = []
    verifyUploadFileList.value = []

    if (st === 0 && isAdminRole.value) {
      const rawAssignUsers = (await getProblemFeedbackUsers(data.id)) || []
      assignUserOptions.value = filterAssignableUsers(rawAssignUsers)
      console.log(LOG_PREFIX, 'openDetail:assignUsers', {
        feedbackId: idStr,
        rawCount: rawAssignUsers.length,
        count: assignUserOptions.value.length
      })
    } else {
      console.log(LOG_PREFIX, 'openDetail:skipAssignUsers', {
        feedbackId: idStr,
        reason: st !== 0 ? 'status_not_pending' : 'not_admin_role',
        statusNormalized: st,
        isAdminRole: isAdminRole.value
      })
    }
  } catch (err) {
    console.error(LOG_PREFIX, 'openDetail:failed', { feedbackId: idStr, err })
    detailData.value = undefined
    assignUserOptions.value = []
    ElMessage.error('加载反馈详情失败，请稍后重试')
  } finally {
    detailLoading.value = false
  }
}

const handleDetail = (row: ProblemFeedbackPageRespVO) => {
  if (!row?.id) return
  openDetailDialog(row.id)
}

const showNotifyResult = (resp: ProblemFeedbackNotifyRespVO | undefined | null, includeHandler: boolean) => {
  if (!resp) {
    ElMessage.warning('短信发送失败：未获取到发送结果')
    return
  }
  const publicOk = resp.publicSent === true
  const handlerOk = includeHandler ? resp.handlerSent === true : true
  const publicText = publicOk ? '反馈人：已发送' : `反馈人：未发送（${resp.publicReason || '失败'}）`
  const handlerText = includeHandler
    ? handlerOk
      ? '处理人：已发送'
      : `处理人：未发送（${resp.handlerReason || '失败'}）`
    : ''
  const summary = includeHandler ? `${publicText}；${handlerText}` : publicText
  if (publicOk && handlerOk) {
    ElMessage.success(`短信发送成功`)
  } else {
    // ElMessage.warning(`短信发送不完整，${summary}`)
    // ElMessage.success(`短信发送成功`)

  }
}

const showHandlerNotifyResult = (resp: ProblemFeedbackNotifyRespVO | undefined | null) => {
  if (!resp) {
    ElMessage.warning('短信发送失败：未获取到发送结果')
    return
  }
  if (resp.handlerSent === true) {
    ElMessage.success('催办短信已发送给处理人')
    return
  }
  ElMessage.warning(`催办短信未发送（${resp.handlerReason || '失败'}）`)
}

const sendNotifyAssignSms = async (feedbackId: string | number) => {
  try {
    const notifyResp = await notifyProblemFeedbackAssignSms(feedbackId)
    showNotifyResult(notifyResp, true)
  } catch (err: any) {
    ElMessage.warning(`短信发送失败：${err?.message || '请检查短信配置与权限'}`)
  }
}

const sendNotifyOverdueSms = async (feedbackId: string | number) => {
  try {
    const notifyResp = await notifyProblemFeedbackOverdueSms(feedbackId)
    showHandlerNotifyResult(notifyResp)
    if (notifyResp?.handlerSent === true) {
      fetchTable()
    }
  } catch (err: any) {
    ElMessage.warning(`短信发送失败：${err?.message || '请检查短信配置与权限'}`)
  }
}

const sendNotifyFinishSms = async (feedbackId: string | number) => {
  try {
    const notifyResp = await notifyProblemFeedbackFinishSms(feedbackId)
    showNotifyResult(notifyResp, false)
  } catch (err: any) {
    ElMessage.warning(`短信发送失败：${err?.message || '请检查短信配置与权限'}`)
  }
}

const sendNotifyRejectSms = async (feedbackId: string | number) => {
  try {
    const notifyResp = await notifyProblemFeedbackRejectSms(feedbackId)
    showNotifyResult(notifyResp, false)
  } catch (err: any) {
    ElMessage.warning(`短信发送失败：${err?.message || '请检查短信配置与权限'}`)
  }
}

const submitAudit = async () => {
  if (!detailData.value?.id) return
  if (!auditForm.statusDescription?.trim()) {
    ElMessage.warning('请填写回复内容')
    return
  }
  if (!auditForm.assignedPersonId) {
    ElMessage.warning('请选择指派人')
    return
  }
  if (!auditForm.plannedCompletionTime) {
    ElMessage.warning('请选择计划完成时间')
    return
  }
  const planned = dayjs(auditForm.plannedCompletionTime)
  if (!planned.isValid() || !planned.isAfter(dayjs())) {
    ElMessage.warning('计划完成时间必须晚于当前时间')
    return
  }
  detailSubmitting.value = true
  try {
    await auditAndAssignProblemFeedback({
      id: detailData.value.id,
      assignedPersonId: auditForm.assignedPersonId,
      plannedCompletionTime: auditForm.plannedCompletionTime,
      statusDescription: auditForm.statusDescription,
    })
    ElMessage.success('提交成功')
    detailDialogVisible.value = false
    fetchTable()
    if (SMS_ENABLED) {
      void sendNotifyAssignSms(detailData.value.id)
    }
  } finally {
    detailSubmitting.value = false
  }
}

const submitProcess = async () => {
  if (!detailData.value?.id) return
  if (!processForm.handleResult) {
    ElMessage.warning('请选择处理结果')
    return
  }
  if (!processForm.resolutionDescription?.trim()) {
    ElMessage.warning('请填写处理描述')
    return
  }
  detailSubmitting.value = true
  try {
    await processProblemFeedback({
      id: detailData.value.id,
      handleResult: processForm.handleResult,
      resolutionDescription: processForm.resolutionDescription,
      uploadedFiles: processForm.uploadedFiles
    })
    ElMessage.success('提交成功')
    detailDialogVisible.value = false
    fetchTable()
  } finally {
    detailSubmitting.value = false
  }
}

const submitVerify = async () => {
  if (!detailData.value?.id) return
  if (verifyForm.solved !== true && verifyForm.solved !== false) {
    ElMessage.warning('请选择核验结论')
    return
  }
  if (!verifyForm.verificationResult?.trim()) {
    ElMessage.warning('请填写核验结果')
    return
  }
  detailSubmitting.value = true
  try {
    await verifyProblemFeedback({
      id: detailData.value.id,
      solved: verifyForm.solved,
      verificationResult: verifyForm.verificationResult,
      uploadedFiles: verifyForm.uploadedFiles
    })
    ElMessage.success(verifyForm.solved ? '办结成功' : '驳回成功')
    detailDialogVisible.value = false
    fetchTable()
    if (SMS_ENABLED) {
      if (verifyForm.solved) {
        void sendNotifyFinishSms(detailData.value.id)
      } else {
        void sendNotifyRejectSms(detailData.value.id)
      }
    }
  } finally {
    detailSubmitting.value = false
  }
}

const initDict = async () => {
  const [typeList, progressList, facilityList] = await Promise.all([
    getRiverDict('zd_fklx'),
    getRiverDict('zd_wtjd'),
    getRiverDict('zd_sslb')
  ])
  const enabledFeedbackTypes = (typeList || []).filter((item) => Number(item?.status) === 0)
  const enabledFacilityTypes = (facilityList || []).filter((item) => Number(item?.status) === 0)
  feedbackTypeOptions.value = enabledFeedbackTypes
  progressOptions.value = progressList || []
  facilityTypeOptions.value = enabledFacilityTypes
}

onMounted(async () => {
  await initDict()
  await fetchTable()
})
</script>

<style scoped>
.river-page {
  --pf-bg: #f2f6fc;
  --pf-surface: #ffffff;
  --pf-border: rgba(148, 163, 184, 0.28);
  --pf-text: #1f2d3d;
  --pf-text-soft: #4e6a8a;
  --pf-accent: #2f74ff;
  --pf-accent-soft: rgba(47, 116, 255, 0.12);
  --pf-primary: #1e4fae;
  --pf-success: #16a34a;
  --pf-danger: #dc2626;
  --pf-shadow: 0 10px 28px rgba(15, 23, 42, 0.07);
  --pf-shadow-hover: 0 12px 26px rgba(23, 72, 151, 0.12);
  padding: 0;
  background: transparent;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}

.feedback-query-wrap,
.feedback-table-wrap {
  border: 1px solid var(--pf-border);
  border-radius: 14px;
  background: var(--pf-surface);
  box-shadow: var(--pf-shadow);
}

.feedback-query-wrap {
  position: relative;
  overflow: hidden;
}

.feedback-query-wrap::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 1px;
  background: rgba(47, 116, 255, 0.18);
}

.feedback-query-form {
  padding-top: 6px;
}

.feedback-query-form :deep(.el-form-item__label) {
  color: var(--pf-text-soft);
  font-weight: 600;
}

.feedback-query-form :deep(.el-input__wrapper),
.feedback-query-form :deep(.el-select__wrapper),
.feedback-query-form :deep(.el-date-editor.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: inset 0 0 0 1px #d9e1ec;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.feedback-query-form :deep(.el-input__wrapper:hover),
.feedback-query-form :deep(.el-select__wrapper:hover),
.feedback-query-form :deep(.el-date-editor.el-input__wrapper:hover) {
  transform: translateY(-1px);
  box-shadow: inset 0 0 0 1px #b9c6d8;
}

.feedback-query-form :deep(.el-button) {
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 0.3px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.feedback-query-form :deep(.el-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(30, 41, 59, 0.16);
}

.feedback-query-form :deep(.el-button--primary) {
  border-color: #2f74ff;
  background: #2f74ff;
}

.feedback-query-form :deep(.el-button--primary:hover) {
  border-color: #1e5ee0;
  background: #1e5ee0;
}

.feedback-query-form :deep(.el-button--success.is-plain) {
  border-color: rgba(47, 116, 255, 0.35);
  color: #1e5ee0;
  background: rgba(47, 116, 255, 0.08);
}

.feedback-table-wrap {
  margin-top: 12px;
}

.feedback-table {
  --el-table-border-color: #e2e8f0;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f8fbff;
}

.feedback-table :deep(.el-table__header-wrapper th) {
  color: var(--pf-primary);
  font-weight: 700;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  letter-spacing: 0.2px;
}

.feedback-table :deep(.el-table__body td) {
  color: var(--pf-text);
}

.feedback-table :deep(.el-table__body tr) {
  transition: background-color 0.2s ease;
}

.feedback-table :deep(.el-tag) {
  border-radius: 999px;
  padding: 0 9px;
  font-weight: 600;
}

.feedback-table :deep(.el-button.is-link) {
  font-weight: 600;
  letter-spacing: 0.2px;
}

.feedback-table-wrap :deep(.el-pagination) {
  margin-top: 12px;
}

.feedback-table-wrap :deep(.el-pagination.is-background .btn-prev),
.feedback-table-wrap :deep(.el-pagination.is-background .btn-next),
.feedback-table-wrap :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 8px;
}

.feedback-detail-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #dce5f0;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.2);
}

.feedback-detail-dialog :deep(.el-dialog__header) {
  padding: 14px 18px 12px;
  margin-right: 0;
  /* 与页面灰蓝区协调、略深于 body，便于看出标题栏 */
  background: #e8edf4 !important;
  border-bottom: 1px solid #d1dce8 !important;
}

.feedback-detail-dialog :deep(.el-dialog__title) {
  color: #0c2744;
  /* YaHei UI 字重档位更全；黑体类作兜底，四字标题更「实」 */
  font-family:
    'Microsoft YaHei UI',
    'Microsoft YaHei',
    'PingFang SC',
    'Hiragino Sans GB',
    'Heiti SC',
    'SimHei',
    sans-serif !important;
  font-size: 18px !important;
  font-weight: 800 !important;
  letter-spacing: 0.06em;
  line-height: 1.35;
  -webkit-font-smoothing: antialiased;
}

.feedback-detail-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #64748b;
}

.feedback-detail-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: var(--pf-primary);
}

.feedback-detail-dialog :deep(.el-dialog__body) {
  padding: 16px 18px;
  background: #fcfdff;
}

/* 详情 - 只读表单项：标签 + 输入框观感 */
.pf-detail-item-readonly :deep(.el-form-item__label) {
  font-weight: 700;
  color: var(--pf-primary);
}

.pf-detail-readonly-input :deep(.el-input__wrapper) {
  cursor: default;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  box-shadow: inset 0 0 0 1px #e2e8f0, inset 0 1px 2px rgba(15, 23, 42, 0.05);
}

.pf-detail-readonly-input :deep(.el-input__wrapper:hover),
.pf-detail-readonly-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: inset 0 0 0 1px #e2e8f0, inset 0 1px 2px rgba(15, 23, 42, 0.05);
}

/* 只读 value 文字色（与浅灰底区分，略偏蓝） */
.pf-detail-readonly-input {
  --pf-readonly-value-color: #1e4b82;
}

.pf-detail-readonly-input :deep(.el-input__inner) {
  cursor: default;
  color: var(--pf-readonly-value-color);
  font-weight: 600;
  -webkit-text-fill-color: var(--pf-readonly-value-color);
}

.pf-detail-readonly-input--textarea :deep(.el-textarea__inner) {
  cursor: default;
  resize: none;
  min-height: 72px;
  line-height: 1.55;
  color: var(--pf-readonly-value-color);
  font-weight: 600;
  -webkit-text-fill-color: var(--pf-readonly-value-color);
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  box-shadow: inset 0 0 0 1px #e2e8f0, inset 0 1px 2px rgba(15, 23, 42, 0.05);
}

.pf-detail-readonly-input--textarea :deep(.el-textarea__inner:hover),
.pf-detail-readonly-input--textarea :deep(.el-textarea__inner:focus) {
  box-shadow: inset 0 0 0 1px #e2e8f0, inset 0 1px 2px rgba(15, 23, 42, 0.05);
}

.feedback-detail-dialog :deep(.el-divider__text) {
  color: var(--pf-primary);
  font-weight: 700;
}

.media-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.media-thumb {
  width: 92px;
  height: 92px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #d9e2ef;
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.1);
}

.media-video {
  position: relative;
  cursor: pointer;
  background: #000;
}

.media-video-el {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-video-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.2), rgba(15, 23, 42, 0.56));
}

.media-video-text {
  font-size: 12px;
}

.upload-tip {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}

.task-timeline {
  padding-left: 6px;
}

.timeline-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 8px 10px;
  border-radius: 10px;
  background: linear-gradient(90deg, rgba(47, 116, 255, 0.08), rgba(86, 168, 255, 0.08));
}

.timeline-title {
  font-weight: 700;
  color: var(--pf-primary);
}

.timeline-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.timeline-content :deep(.el-form-item) {
  margin-bottom: 10px;
}

.assign-option {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

:global(.assign-user-popper .el-select-dropdown__item) {
  height: auto;
  line-height: 1.2;
  padding: 6px 12px;
}

.assign-option-main {
  display: flex;
  align-items: center;
  gap: 6px;
}

.assign-option-name {
  font-weight: 600;
}

.assign-option-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}

@media (max-width: 768px) {
  .feedback-query-form :deep(.el-form-item) {
    margin-right: 8px;
  }

  .feedback-query-form :deep(.el-input),
  .feedback-query-form :deep(.el-select),
  .feedback-query-form :deep(.el-date-editor) {
    width: 100% !important;
  }

  .feedback-detail-dialog :deep(.el-dialog) {
    width: calc(100vw - 20px) !important;
    margin: 10px auto;
  }
}

@media (prefers-reduced-motion: reduce) {
  .feedback-query-form :deep(.el-button),
  .feedback-query-form :deep(.el-input__wrapper),
  .feedback-query-form :deep(.el-select__wrapper),
  .feedback-query-form :deep(.el-date-editor.el-input__wrapper) {
    transition: none;
  }
}
</style>
