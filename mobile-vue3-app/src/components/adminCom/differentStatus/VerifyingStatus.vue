<template>
    <div class="timeline-list">
        <!-- 节点 1：已反馈（用户提交反馈的初始节点） -->
        <div class="timeline-item feedback-item">
            <div class="timeline-line"></div>
            <div class="timeline-line-wrapper">
                <div class="timeline-dot active"></div>
            </div>
            <div class="timeline-content">
                <div class="timeline-header">
                    <span class="timeline-status">已反馈</span>
                    <span class="timeline-time">{{ formatTimelineTime(detailData.createTime) }}</span>
                </div>
            </div>
        </div>

        <!-- 节点 2：已受理 -->
        <div class="timeline-item">
            <div class="timeline-line"></div>
            <div class="timeline-line-wrapper">
                <div class="timeline-dot active"></div>
            </div>
            <div class="timeline-content">
                <div class="timeline-header">
                    <span class="timeline-status">已受理</span>
                    <span class="timeline-time">{{ formatTimelineTime(currentTask?.reviewerPersonTime) }}</span>
                    <span class="timeline-assignee" style="margin-left: auto;">审核人: {{ currentTask?.reviewerPersonName || '--' }}</span>
                </div>
                <!-- 图片/视频列表 -->
                <div class="fileList" v-if="currentTask?.problemReviewImages && currentTask.problemReviewImages.length > 0">
                    <div class="fileItem" v-for="(fileUrl, index) in currentTask.problemReviewImages" :key="index"
                        @click="previewFile(fileUrl)">
                        <img v-if="isImage(fileUrl)" :src="resolveFileUrl(fileUrl)" alt="审核图片" />
                        <div v-else class="video-icon">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                                <path d="M8 5v14l11-7z" fill="#fff" />
                            </svg>
                        </div>
                    </div>
                </div>
                <!-- 文本信息 -->
                <div class="static-treatment-result" v-if="currentTask?.statusDescription">
                    <div class="result-text-box">
                        {{ currentTask.statusDescription }}
                    </div>
                </div>
            </div>
        </div>

        <!-- 节点 3：处理结果 (展示审核人处理的结果) -->
        <div class="timeline-item">
            <div class="timeline-line"></div>
            <div class="timeline-line-wrapper">
                <div class="timeline-dot active"></div>
            </div>
            <div class="timeline-content">
                <div class="timeline-header">
                    <span class="timeline-status">{{ getProcessingStatusText(processingTask) }}</span>
                    <span class="timeline-time">{{ formatTimelineTime(processingTask?.processingTime) }}</span>
                    <span class="timeline-assignee" style="margin-left: auto;">处理人: {{ processingTask?.assignedPersonName || '--' }}</span>
                </div>
                <!-- 图片/视频列表 -->
                <div class="fileList" v-if="processingTask?.problemHandleImages && processingTask.problemHandleImages.length > 0">
                    <div class="fileItem" v-for="(fileUrl, index) in processingTask.problemHandleImages" :key="index"
                        @click="previewFile(fileUrl)">
                        <img v-if="isImage(fileUrl)" :src="resolveFileUrl(fileUrl)" alt="处理图片" />
                        <div v-else class="video-icon">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                                <path d="M8 5v14l11-7z" fill="#fff" />
                            </svg>
                        </div>
                    </div>
                </div>
                <!-- 文本信息 -->
                <div class="static-treatment-result">
                    <div class="result-text-box">
                        {{ processingTask?.resolutionDescription || '已按要求完成处理工作。' }}
                    </div>
                </div>
            </div>
        </div>

        <!-- 节点 4：待核验 -->
        <div class="timeline-item verifying-node" ref="verifyingNodeRef">
            <div class="timeline-line" ref="verifyingLineRef" :style="{ height: verifyingLineHeight }"></div>
            <div class="timeline-line-wrapper">
                <div class="timeline-dot active"></div>
            </div>
            <div class="timeline-content">
                <div class="timeline-header treatment-result-header">
                    <div class="header-left">
                        <span class="timeline-status">待核验</span>
                        <span class="header-info">(最多传6个文件,不超过200k)</span>
                    </div>
                    <!-- 自定义按钮式勾选框 -->
                    <div class="custom-checkbox-btn" 
                        :class="{ 'is-checked': isVerificationPassed, 'disabled': isNormalRole }"
                        @click="handleToggleVerification">
                        <div class="checkbox-inner">
                            <svg v-if="isVerificationPassed" width="14" height="14" viewBox="0 0 24 24" fill="none"
                                stroke="currentColor" stroke-width="4" stroke-linecap="round" stroke-linejoin="round">
                                <polyline points="20 6 9 17 4 12"></polyline>
                            </svg>
                        </div>
                        <span>问题已处理</span>
                    </div>
                </div>

                <!-- 仅在勾选后展示 -->
                <div class="treatment-form-container" v-if="isVerificationPassed">
                    <div class="upload-area" v-if="!isNormalRole">
                        <MediaUpload :model-value="verificationFileList"
                            @update:model-value="val => $emit('update:verificationFileList', val)" :max-files="6"
                            @upload-success="onUploadSuccess" @upload-delete="onUploadDelete"
                            @upload-status-change="onUploadStatusChange" />
                    </div>

                    <!-- 核验情况说明 -->
                    <div class="treatment-input-wrapper">
                        <textarea :value="verificationContent"
                            @input="e => !isNormalRole && $emit('update:verificationContent', e.target.value)" 
                            class="treatment-textarea"
                            :disabled="isNormalRole"
                            @click="handleTextareaClick"
                            placeholder="填写最终处理结果，告知市民问题处理情况" maxlength="200"></textarea>
                        <div class="word-count">{{ (verificationContent || '').length }}/200</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import MediaUpload from '@/components/commonCom/MediaUpload.vue';
import { ElMessage } from 'element-plus';

export default {
    name: 'VerifyingStatus',
    components: {
        MediaUpload
    },
    props: {
        detailData: {
            type: Object,
            required: true
        },
        isVerificationPassed: Boolean,
        verificationFileList: Array,
        verificationContent: String
    },
    emits: ['update:isVerificationPassed', 'update:verificationFileList', 'update:verificationContent', 'upload-success', 'upload-delete', 'upload-status-change', 'preview-file'],
    computed: {
        // 判断是否为普通角色
        isNormalRole() {

            console.log('当前角色:');
            const roleName = this.$store.getters.getRoleName;
            return roleName === '普通角色';
        }
    },
    data() {
        return {
            feedbackDetail: null,  // 存储查询到的反馈详情
            currentTask: null,  // 存储当前状态的任务信息（status === 3）
            processingTask: null,  // 存储处理中阶段的任务信息（status === 2），用于获取处理结果相关数据
            verifyingLineHeight: '0px',  // 待核验节点的timeline-line高度
            resizeObserver: null  // ResizeObserver实例
        };
    },
    mounted() {
        // 根据 id 查询反馈详情
        if (this.detailData && this.detailData.id) {
            this.loadFeedbackDetail();
        }
        // 初始化timeline-line高度计算，使用延迟确保内容已渲染
        setTimeout(() => {
            this.updateVerifyingLineHeight();
            this.setupResizeObserver();
        }, 100);
    },
    updated() {
        // 当组件更新时重新计算高度
        setTimeout(() => {
            this.updateVerifyingLineHeight();
        }, 100);
    },
    watch: {
        // 监听核验状态变化，重新计算高度
        isVerificationPassed() {
            this.$nextTick(() => {
                setTimeout(() => {
                    this.updateVerifyingLineHeight();
                }, 100);
            });
        }
    },
    beforeUnmount() {
        // 清理ResizeObserver
        if (this.resizeObserver) {
            this.resizeObserver.disconnect();
            this.resizeObserver = null;
        }
    },
    methods: {
        // 根据 id 查询反馈详情
        async loadFeedbackDetail() {
            try {
                const id = this.detailData.id;
                const response = await this.$http.get(`/admin-api/problem/feedback/${id}`);
                
                // 保存查询结果
                // response.data 就是内容，不需要再取 response.data.data
                if (response && response.data) {
                    this.feedbackDetail = response.data;
                    
                    // 从 statusTasks 中找到各个阶段的任务
                    if (this.feedbackDetail.statusTasks && Array.isArray(this.feedbackDetail.statusTasks)) {
                        // 优先查找 status === 2 的任务（已受理阶段的任务），用于获取审核信息
                        let taskForAccepted = this.feedbackDetail.statusTasks.find(task => task.status === 2);
                        // 如果没有 status === 2 的任务，尝试查找 status === 3 的任务（待核验状态的任务可能包含之前的数据）
                        if (!taskForAccepted) {
                            taskForAccepted = this.feedbackDetail.statusTasks.find(task => task.status === 3);
                        }
                        this.currentTask = taskForAccepted;
                        
                        // 查找处理结果数据：优先查找 status === 2 的任务，如果没有则查找 status === 3 的任务
                        let taskForProcessing = this.feedbackDetail.statusTasks.find(task => task.status === 2);
                        if (!taskForProcessing) {
                            taskForProcessing = this.feedbackDetail.statusTasks.find(task => task.status === 3);
                        }
                        this.processingTask = taskForProcessing;
                    }
                }
            } catch (error) {
                console.error('VerifyingStatus - 获取反馈详情失败:', error);
            }
        },
        formatTimelineTime(timestamp) {
            if (!timestamp || isNaN(timestamp)) return '--';
            return this.$tool.dateFormat(timestamp, 'yyyy.M.d hh:mm:ss');
        },
        // 获取处理状态文案：优先使用 handleResult，否则根据 noNeedHandle 判断
        getProcessingStatusText(processingTask) {
            if (!processingTask) return '已处理';
            // 优先使用 handleResult 字段
            if (processingTask.handleResult) {
                return processingTask.handleResult;
            }
            // 如果没有 handleResult，则根据 noNeedHandle 判断
            return processingTask.noNeedHandle ? '无需处理' : '已处理';
        },
        isImage(url) {
            if (!url) return false;
            const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
            const lowerUrl = url.toLowerCase();
            return imageExtensions.some(ext => lowerUrl.includes(ext));
        },
        previewFile(url) {
            this.$emit('preview-file', url);
        },
        toggleVerificationPassed() {
            this.$emit('update:isVerificationPassed', !this.isVerificationPassed);
        },
        handleToggleVerification() {
            if (this.isNormalRole) {
                ElMessage.warning('权限不足，无法操作');
                return;
            }
            this.toggleVerificationPassed();
        },
        handleTextareaClick() {
            if (this.isNormalRole) {
                ElMessage.warning('权限不足，无法操作');
            }
        },
        onUploadSuccess(res) {
            this.$emit('upload-success', res);
        },
        onUploadDelete(info) {
            this.$emit('upload-delete', info);
        },
        onUploadStatusChange(status) {
            this.$emit('upload-status-change', status);
        },
        // 更新待核验节点的timeline-line高度
        updateVerifyingLineHeight() {
            if (this.$refs.verifyingNodeRef) {
                // 获取待核验节点的timeline-item容器高度
                const timelineItemHeight = this.$refs.verifyingNodeRef.offsetHeight;
                console.log('待核验节点 timeline-item 高度:', timelineItemHeight);
                // 高度 = timeline-item高度 - 20px
                const calculatedHeight = timelineItemHeight - 20;
                this.verifyingLineHeight = `${calculatedHeight}px`;
                console.log('计算后的 timeline-line 高度:', this.verifyingLineHeight);
            } else {
                console.warn('verifyingNodeRef 未找到');
            }
        },
        // 设置ResizeObserver监听父容器高度变化
        setupResizeObserver() {
            if (typeof ResizeObserver !== 'undefined' && this.$refs.verifyingNodeRef) {
                this.resizeObserver = new ResizeObserver(() => {
                    this.updateVerifyingLineHeight();
                });
                this.resizeObserver.observe(this.$refs.verifyingNodeRef);
            }
        }
    }
}
</script>

<style lang="scss" scoped>
@use "../fixFeedBack.scss" as *;

// 附件列表样式（独立容器）
.fileList {
    margin-top: 12px;
    margin-bottom: 24px;
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    
    .fileItem {
        width: 80px;
        height: 80px;
        background-color: #f5f5f5;
        border-radius: 4px;
        overflow: hidden;
        cursor: pointer;
        position: relative;
        
        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        
        .video-icon {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: rgba(0, 0, 0, 0.5);
            
            svg {
                width: 32px;
                height: 32px;
            }
        }
    }
}

// 已反馈节点样式（减少底部间距）
.timeline-item.feedback-item {
    margin-bottom: 24px;
}

// 待核验节点的timeline-line样式（动态高度，灰色样式）
.timeline-item.verifying-node {
    .timeline-line {
        background-color: #E0E0E0 !important; // 灰色线条
    }
    
    .timeline-line-wrapper {
        .timeline-dot {
            background: #E0E0E0 !important; // 灰色背景
            
            &::after {
                background-color: #999999 !important; // 灰色圆点
            }
        }
    }
    
    .timeline-content {
        .timeline-header {
            .timeline-status {
                color: #999999 !important; // 灰色文字
            }
        }
    }
}

// 文本信息样式（独立容器）
.static-treatment-result {
    margin-top: 0;
    background: #f8f8f8;
    padding: 20px;
    border-radius: 8px;
    
    .result-text-box {
        font-size: 26px;
        color: #666;
        line-height: 1.6;
        word-break: break-all;
        margin: 0;
    }
}

// 权限相关样式
:deep(.custom-checkbox-btn) {
    &.disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }
}

.treatment-textarea:disabled {
    background-color: #f5f5f5;
    color: #999999;
    cursor: not-allowed;
}
</style>

