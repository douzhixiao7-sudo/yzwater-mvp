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
        <div class="timeline-item m12">
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

        <!-- 节点 3：处理结果 -->
        <div class="timeline-item processing-node" ref="processingNodeRef" :style="{ '--dynamic-line-height': processingLineHeight }">
            <div class="timeline-line" ref="processingLineRef" :style="{ height: processingLineHeight }"></div>
            <div class="timeline-line-wrapper">
                <div class="timeline-dot active"></div>
            </div>
            <div class="timeline-content">
                <div class="timeline-header">
                    <span class="timeline-status">处理中</span>
                    <span class="header-info">(最多传6个文件,不超过200k)</span>
                </div>
                
                <!-- 处理结果选择按钮 -->
                <div class="result-type-selector">
                    <button 
                        class="result-type-btn" 
                        :class="{ 'active': processorResultType === 'processed' }"
                        @click="updateProcessorResultType('processed')">
                        已处理
                    </button>
                    <button 
                        class="result-type-btn" 
                        :class="{ 'active': processorResultType === 'no_need' }"
                        @click="updateProcessorResultType('no_need')">
                        无需处理
                    </button>
                </div>

                <!-- 选择"已处理"或"无需处理"后都展示 -->
                <div class="treatment-form-container" v-if="processorResultType === 'processed' || processorResultType === 'no_need'">
                    <div class="upload-area">
                        <MediaUpload :model-value="treatmentFileList"
                            @update:model-value="val => $emit('update:treatmentFileList', val)" :max-files="6"
                            @upload-success="onUploadSuccess" @upload-delete="onUploadDelete"
                            @upload-status-change="onUploadStatusChange" />
                    </div>

                    <!-- 处理详情输入 -->
                    <div class="treatment-input-wrapper">
                        <textarea 
                            :value="treatmentContent" 
                            @input="e => $emit('update:treatmentContent', e.target.value)"
                            class="treatment-textarea" 
                            :placeholder="processorResultType === 'processed' ? '填写详细的处理结果' : '填写详细的处理结果'" 
                            maxlength="200"></textarea>
                        <div class="word-count">{{ (treatmentContent || '').length }}/200</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import MediaUpload from '@/components/commonCom/MediaUpload.vue';

export default {
    name: 'ProcessingStatus',
    components: {
        MediaUpload
    },
    props: {
        detailData: {
            type: Object,
            required: true
        },
        processorResultType: {
            type: String,
            default: '' // 'processed' 或 'no_need'
        },
        treatmentFileList: Array,
        treatmentContent: String
    },
    emits: ['update:processorResultType', 'update:treatmentFileList', 'update:treatmentContent', 'upload-success', 'upload-delete', 'upload-status-change', 'preview-file'],
    data() {
        return {
            feedbackDetail: null,  // 存储查询到的反馈详情
            currentTask: null,  // 存储当前状态的任务信息（status === 2）
            processingLineHeight: '0px',  // 节点3的timeline-line高度
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
            this.updateProcessingLineHeight();
            this.setupResizeObserver();
        }, 100);
    },
    updated() {
        // 当组件更新时重新计算高度
        setTimeout(() => {
            this.updateProcessingLineHeight();
        }, 100);
    },
    watch: {
        // 监听处理结果类型变化，重新计算高度
        processorResultType() {
            this.$nextTick(() => {
                setTimeout(() => {
                    this.updateProcessingLineHeight();
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
                    
                    // 从 statusTasks 中找到 status === 2 的任务
                    if (this.feedbackDetail.statusTasks && Array.isArray(this.feedbackDetail.statusTasks)) {
                        this.currentTask = this.feedbackDetail.statusTasks.find(task => task.status === 2);

                    }

                    Object.keys(response.data).forEach(key => {
                    });
                }
            } catch (error) {
                console.error('ProcessingStatus - 获取反馈详情失败:', error);
                console.error('ProcessingStatus - 错误详情:', error.response?.data || error.message);
            }
        },
        formatTimelineTime(timestamp) {
            if (!timestamp || isNaN(timestamp)) return '--';
            return this.$tool.dateFormat(timestamp, 'yyyy.M.d hh:mm:ss');
        },
        updateProcessorResultType(type) {
            this.$emit('update:processorResultType', type);
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
        isImage(url) {
            if (!url) return false;
            const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
            const lowerUrl = url.toLowerCase();
            return imageExtensions.some(ext => lowerUrl.includes(ext));
        },
        previewFile(url) {
            this.$emit('preview-file', url);
        },
        // 更新节点3的timeline-line高度
        updateProcessingLineHeight() {
            if (this.$refs.processingNodeRef) {
                // 获取节点3的timeline-item容器高度
                const timelineItemHeight = this.$refs.processingNodeRef.offsetHeight;
                // 高度 = timeline-item高度 - 20px
                const calculatedHeight = timelineItemHeight - 20;
                this.processingLineHeight = `${calculatedHeight}px`;
            } else {
            }
        },
        // 设置ResizeObserver监听父容器高度变化
        setupResizeObserver() {
            if (typeof ResizeObserver !== 'undefined' && this.$refs.processingNodeRef) {
                this.resizeObserver = new ResizeObserver(() => {
                    this.updateProcessingLineHeight();
                });
                this.resizeObserver.observe(this.$refs.processingNodeRef);
            }
        }
    }
}
</script>

<style lang="scss" scoped>
@use "../fixFeedBack.scss" as *;

.m12{
    margin-bottom: 12px !important;
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

// 已反馈节点样式（减少底部间距）
.timeline-item.feedback-item {
    margin-bottom: 24px;
}

// 节点3的timeline-line样式（动态高度，覆盖默认样式）
.timeline-item.processing-node {
    .timeline-line {
        // 移除默认的 calc(100% + 48px)，使用动态计算的高度
        height: var(--dynamic-line-height, 0px) !important;
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

// header-info 样式
.timeline-header {
    .header-info {
        font-size: 20px;
        color: #999;
        margin-left: 16px;
    }
}

// 处理结果选择按钮样式
.result-type-selector {
    display: flex;
    gap: 20px;
    margin-top: 16px;
    margin-bottom: 24px;
}

.result-type-btn {
    width: 256px;
    height: 68px;
    border-radius: 6px;
    opacity: 1;
    border: 1px solid #349dff;
    background: #ffffffff;
    color: #349dff;
    font-size: 28px;
    font-weight: 400;
    cursor: pointer;
    transition: all 0.3s;
    font-weight: 500;
    
    &.active {
        background: #359dff;
        color: #ffffff;
    }
    
    // &:hover {
    //     opacity: 0.8;
    // }
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

