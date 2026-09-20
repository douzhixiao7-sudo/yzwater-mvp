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
                        <img v-if="isImage(fileUrl)" :src="fileUrl" alt="审核图片" />
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

        <!-- 节点 3：处理结果 -->
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
                        <img v-if="isImage(fileUrl)" :src="fileUrl" alt="处理图片" />
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

        <!-- 节点 4：已核验 -->
        <div class="timeline-item">
            <div class="timeline-line"></div>
            <div class="timeline-line-wrapper">
                <div class="timeline-dot active"></div>
            </div>
            <div class="timeline-content">
                <div class="timeline-header">
                    <span class="timeline-status">已核验</span>
                    <span class="timeline-time">{{ formatTimelineTime(verificationTask?.verificationTime) }}</span>
                    <span class="timeline-assignee" style="margin-left: auto;">核验人: {{ verificationTask?.verificationName || '--' }}</span>
                </div>
                <!-- 图片/视频列表 -->
                <div class="fileList" v-if="verificationTask?.verifyHandleImages && verificationTask.verifyHandleImages.length > 0">
                    <div class="fileItem" v-for="(fileUrl, index) in verificationTask.verifyHandleImages" :key="index"
                        @click="previewFile(fileUrl)">
                        <img v-if="isImage(fileUrl)" :src="fileUrl" alt="核验图片" />
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
                        {{ verificationTask?.verificationResult || '现场核验通过，处理质量符合要求。' }}
                    </div>
                </div>
            </div>
        </div>

        <!-- 节点 5：已办结 -->
        <div class="timeline-item">
            <div class="timeline-line-wrapper">
                <div class="timeline-dot active"></div>
            </div>
            <div class="timeline-content">
                <div class="timeline-header">
                    <span class="timeline-status">已办结</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'CompletedStatus',
    props: {
        detailData: {
            type: Object,
            required: true
        }
    },
    emits: ['preview-file'],
    data() {
        return {
            feedbackDetail: null,  // 存储查询到的反馈详情
            currentTask: null,  // 存储接受任务的任务信息（status === 2）
            processingTask: null,  // 存储处理中阶段的任务信息（status === 2），用于获取处理结果相关数据
            verificationTask: null  // 存储核验阶段的任务信息（status === 3），用于获取核验相关数据
        };
    },
    mounted() {
        // 根据 id 查询反馈详情
        if (this.detailData && this.detailData.id) {
            this.loadFeedbackDetail();
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
                    
                    // 从 statusTasks 中查找各个阶段的任务
                    if (this.feedbackDetail.statusTasks && Array.isArray(this.feedbackDetail.statusTasks)) {
                        // 查找 status === 4 的任务（已办结状态的任务，同时包含已受理、已处理、已核验的数据）
                        const completedTask = this.feedbackDetail.statusTasks.find(task => task.status === 4);
                        if (completedTask) {
                            // 已受理节点：使用 status === 4 任务中的审核相关字段
                            this.currentTask = completedTask;
                            // 已处理节点：使用 status === 4 任务中的处理相关字段
                            this.processingTask = completedTask;
                            // 已核验节点：使用 status === 4 任务中的核验相关字段
                            this.verificationTask = completedTask;
                        } else {
                            // 如果没有 status === 4 的任务，尝试查找其他状态的任务
                            // 查找 status === 2 的任务（已受理阶段的任务，包含审核人信息）
                            this.currentTask = this.feedbackDetail.statusTasks.find(task => task.status === 2);
                            // 查找 status === 2 的任务（处理中阶段的任务，用于获取处理结果相关数据）
                            this.processingTask = this.feedbackDetail.statusTasks.find(task => task.status === 2);
                            // 查找 status === 3 的任务（核验阶段的任务，用于获取核验相关数据）
                            this.verificationTask = this.feedbackDetail.statusTasks.find(task => task.status === 3);
                        }
                    }
                }
            } catch (error) {
                console.error('CompletedStatus - 获取反馈详情失败:', error);
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
        }
    }
}
</script>

<style lang="scss" scoped>
@use "../fixFeedBack.scss" as *;

// 已反馈节点样式（减少底部间距）
.timeline-item.feedback-item {
    margin-bottom: 24px;
}

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
</style>

