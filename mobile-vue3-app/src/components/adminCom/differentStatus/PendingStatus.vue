<template>
    <div class="timeline-list">
        <!-- 节点 1：已反馈（用户提交反馈的初始节点） -->
        <div class="timeline-item">
            <div class="timeline-line"></div>
            <div class="timeline-line-wrapper">
                <div class="timeline-dot active"></div>
            </div>
            <div class="timeline-content">
                <div class="timeline-header">
                    <span class="timeline-status">已反馈</span>
                    <span class="timeline-time">{{ formatTimelineTime(detailData.createTime) }}</span>
                </div>
                <!-- <div class="timeline-description">用户已提交反馈</div> -->
            </div>
        </div>

        <!-- 节点 2：待受理（管理员操作） -->
        <div class="timeline-item pending-node">
            <div class="timeline-line"></div>
            <div class="timeline-line-wrapper">
                <div class="timeline-dot active"></div>
            </div>
            <div class="timeline-content">
                <div class="timeline-header">
                    <span class="timeline-status">待受理</span>
                </div>
                <!-- <div class="timeline-description">请受理并指派处理人</div> -->

                <!-- 管理员操作区域 -->
                <div v-if="isAdmin">
                    <!-- 问题确认说明 -->
                    <div class="treatment-input-wrapper mB_T8" :class="{ 'has-error': showExistsReasonError }">
                        <textarea 
                            :value="existsReason" 
                            @input="e => updateExistsReason(e.target.value)" 
                            @blur="validateExistsReason"
                            class="treatment-textarea"
                            placeholder="填写审核意见与结论，及时向市民反馈处理进度" 
                            maxlength="200">
                        </textarea>
                        <div class="word-count">{{ (existsReason || '').length }}/200</div>
                    </div>
                    <div v-if="showExistsReasonError" class="error-tip">请输入问题确认说明</div>
                    
                    <!-- 文件上传 -->
                    <!-- <div class="upload-area">
                        <MediaUpload 
                            :model-value="existsFileList"
                            @update:model-value="val => $emit('update:existsFileList', val)" 
                            :max-files="6"
                            @upload-success="onExistsUploadSuccess" 
                            @upload-delete="onExistsUploadDelete"
                            @upload-status-change="onExistsUploadStatusChange" />
                    </div> -->

                    <!-- 指派人选择 -->
                    <div class="timeline-header m24">
                        <span class="timeline-status">指派人</span>
                    </div>
                    <div class="assignee-select-wrapper" :class="{ 'has-error': showAssigneeError }">
                        <el-select 
                            :model-value="selectedAssignee" 
                            @update:model-value="updateSelectedAssignee"
                            @blur="validateAssignee"
                            placeholder="请选择指派人" 
                            class="assignee-select">
                            <el-option 
                                v-for="item in assigneeList" 
                                :key="item.value" 
                                :label="item.label"
                                :value="item.value" />
                        </el-select>
                    </div>
                    <div v-if="showAssigneeError" class="error-tip">请选择指派人</div>

                    <!-- 计划完成时间选择 -->
                    <div class="timeline-header m24">
                        <span class="timeline-status">计划完成时间</span>
                    </div>
                    <div class="assignee-select-wrapper" :class="{ 'has-error': showTaskTimeError }">
                        <el-date-picker 
                            :model-value="taskTime" 
                            @update:model-value="updateTaskTime" 
                            @blur="validateTaskTime"
                            type="datetime"
                            placeholder="请选择计划完成时间" 
                            format="YYYY-MM-DD HH:mm:ss" 
                            value-format="YYYY-MM-DD HH:mm:ss"
                            :disabled-date="disabledDate" 
                            :disabled-time="disabledTime" 
                            :editable="false"
                            style="width: 100%;" />
                    </div>
                    <div v-if="showTaskTimeError" class="error-tip">请选择计划完成时间</div>
                </div>
                
                <!-- 处理人视角：只读展示 -->
                <div v-else class="readonly-info">
                    <div class="info-text">等待管理员受理并指派处理人</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ElSelect, ElOption, ElDatePicker, ElMessage } from 'element-plus';
import MediaUpload from '@/components/commonCom/MediaUpload.vue';

export default {
    name: 'PendingStatus',
    components: {
        ElSelect,
        ElOption,
        ElDatePicker,
        MediaUpload
    },
    props: {
        detailData: {
            type: Object,
            required: true
        },
        existsReason: String,
        selectedAssignee: String,
        taskTime: String,
        assigneeList: {
            type: Array,
            default: () => []
        },
        existsFileList: {
            type: Array,
            default: () => []
        }
    },
    emits: ['update:existsReason', 'update:selectedAssignee', 'update:taskTime', 
            'update:existsFileList',
            'exists-upload-success', 'exists-upload-delete', 'exists-upload-status-change'],
    data() {
        return {
            showExistsReasonError: false,
            showAssigneeError: false,
            showTaskTimeError: false
        };
    },
    computed: {
        isAdmin() {
            const roleName = this.$store.getters.getRoleName || '';
            return roleName !== '普通角色';
        }
    },
    methods: {
        formatTimelineTime(timestamp) {
            if (!timestamp || isNaN(timestamp)) return '--';
            return this.$tool.dateFormat(timestamp, 'yyyy.M.d hh:mm:ss');
        },
        updateExistsReason(val) {
            this.$emit('update:existsReason', val);
            // 如果已经有值，清除错误提示
            if (val && val.trim()) {
                this.showExistsReasonError = false;
            }
        },
        validateExistsReason() {
            if (!this.existsReason || !this.existsReason.trim()) {
                this.showExistsReasonError = true;
            } else {
                this.showExistsReasonError = false;
            }
        },
        updateSelectedAssignee(val) {
            this.$emit('update:selectedAssignee', val);
            // 如果已选择，清除错误提示
            if (val) {
                this.showAssigneeError = false;
            }
        },
        validateAssignee() {
            if (!this.selectedAssignee) {
                this.showAssigneeError = true;
            } else {
                this.showAssigneeError = false;
            }
        },
        updateTaskTime(val) {
            // 如果选择了日期，默认将时间设置为 23:59:59
            if (val) {
                const selectedTime = new Date(val.replace(/-/g, '/'));
                const hours = selectedTime.getHours();
                const minutes = selectedTime.getMinutes();
                const seconds = selectedTime.getSeconds();
                
                // 如果时间部分是 00:00:00（默认值），自动设置为 23:59:59
                if (hours === 0 && minutes === 0 && seconds === 0) {
                    selectedTime.setHours(23, 59, 59);
                    // 格式化为 YYYY-MM-DD HH:mm:ss
                    const year = selectedTime.getFullYear();
                    const month = String(selectedTime.getMonth() + 1).padStart(2, '0');
                    const day = String(selectedTime.getDate()).padStart(2, '0');
                    const formattedTime = `${year}-${month}-${day} 23:59:59`;
                    val = formattedTime;
                }
                
                // 验证选择的时间不能早于当前时间（精确到秒）
                const currentTime = new Date();
                
                // 精确到秒级别比较：只有当选择的时间严格小于当前时间（精确到秒）时才提示错误
                // 如果选择的时间等于或大于当前时间，都允许
                const selectedTimeSeconds = Math.floor(selectedTime.getTime() / 1000);
                const currentTimeSeconds = Math.floor(currentTime.getTime() / 1000);
                
                if (selectedTimeSeconds < currentTimeSeconds) {
                    ElMessage.warning('计划时间不可以早于当前');
                    this.$emit('update:taskTime', null); // 清空选择
                    return;
                }
            }
            this.$emit('update:taskTime', val);
            // 如果已选择，清除错误提示
            if (val) {
                this.showTaskTimeError = false;
            }
        },
        validateTaskTime() {
            if (!this.taskTime) {
                this.showTaskTimeError = true;
            } else {
                this.showTaskTimeError = false;
            }
        },
        // 触发所有字段的校验
        validateAll() {
            this.validateExistsReason();
            this.validateAssignee();
            this.validateTaskTime();
            return !this.showExistsReasonError && !this.showAssigneeError && !this.showTaskTimeError;
        },
        // 禁用过去的日期（不包括今天，因为今天可能还有未来的时间）
        disabledDate(time) {
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            return time.getTime() < today.getTime();
        },
        // 禁用过去的时间（当天）
        disabledTime(time) {
            const now = new Date();
            const selectedDate = new Date(time);
            // 如果是今天，禁用当前时间之前的时间
            if (selectedDate.toDateString() === now.toDateString()) {
                const currentHour = now.getHours();
                const currentMinute = now.getMinutes();
                return {
                    disabledHours: () => {
                        const hours = [];
                        for (let i = 0; i < currentHour; i++) {
                            hours.push(i);
                        }
                        return hours;
                    },
                    disabledMinutes: (selectedHour) => {
                        if (selectedHour === currentHour) {
                            const minutes = [];
                            for (let i = 0; i <= currentMinute; i++) {
                                minutes.push(i);
                            }
                            return minutes;
                        }
                        return [];
                    }
                };
            }
            return {};
        },
        onExistsUploadSuccess(res) {
            this.$emit('exists-upload-success', res);
        },
        onExistsUploadDelete(info) {
            this.$emit('exists-upload-delete', info);
        },
        onExistsUploadStatusChange(status) {
            this.$emit('exists-upload-status-change', status);
        },
    }
}
</script>

<style lang="scss" scoped>
@use "../fixFeedBack.scss" as *;


.m24{

    margin-top:24px;
}

.mB_T8{
    margin:8px 0px;
}

.readonly-info {
    padding: 24px 0;
    text-align: center;
    
    .info-text {
        font-size: 28px;
        color: #999;
    }
}

// 错误提示样式
.error-tip {
    color: #ff4d4f;
    font-size: 24px;
    margin-top: 8px;
    padding-left: 4px;
}

// 错误状态样式
.treatment-input-wrapper.has-error {
    // border: 1px solid #ff4d4f;
    border-radius: 4px;
}

.assignee-select-wrapper.has-error {
    :deep(.el-select) {
        .el-input__wrapper {
            border-color: #ff4d4f;
        }
    }
    
    :deep(.el-date-editor) {
        .el-input__wrapper {
            border-color: #ff4d4f;
        }
    }
}

// 待受理节点的时间线单独设置 - 灰色样式
.timeline-item.pending-node {
    .timeline-line {
        height: 450px; // 设置一个合适的固定高度
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
</style>

