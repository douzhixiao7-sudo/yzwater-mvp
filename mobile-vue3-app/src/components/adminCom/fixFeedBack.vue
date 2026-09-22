<!-- 反馈处理页面 -->
<template>
    <div class="fix-feedBack-container">
        <!-- <PageHeader title="反馈处理" @close="handleClose" /> -->
        <div class="content-layer" :class="{ 'has-bottom-nav': hasBottomNav, 'has-bottom-button': hasBottomButton }">
            <!-- 问题位置地图 - 固定在顶部 -->
            <div class="map-wrapper">
                <CommonMap v-if="detailData" :detail-data="detailData" class="problem-location-map" />
                <!-- 地图内的按钮 - 右下角偏上 -->
                <button class="map-button" @click="handleTopRightButtonClick"></button>
            </div>
            
            <div class="paddingContent">
                <!-- 反馈详情卡片 -->
                <div class="feedBackDetailCard" v-if="detailData">
                    <div class="itemTitle">
                        <span class="type-badge feedback-type-badge" v-if="detailData.feedbackTypeLabel"
                            :style="{ background: getFeedbackTypeColor(detailData.feedbackTypeLabel) }">
                            {{ detailData.feedbackTypeLabel }}
                        </span>
                        <span class="title-text">{{ getTitleText(detailData) }}</span>
                    </div>

                    <div class="feedBackBox">
                        <!-- 反馈时间 -->
                        <!-- <div class="feedBackItem-row">
                            <span class="itemLabel">反馈时间:</span>
                            <span class="itemVal">{{ formatTime(detailData.createTime) }}</span>
                        </div> -->
                        <!-- 反馈人员 -->
                        <div class="feedBackItem-row" v-if="detailData.feedbackPerson">
                            <span class="itemLabel">反馈人员:</span>
                            <span class="itemVal">{{ detailData.feedbackPerson }}</span>
                        </div>
                        <!-- 问题描述 -->
                        <div class="feedBackItem-row">
                            <span class="itemLabel">问题描述:</span>
                            <span class="itemVal">{{ detailData.feedbackContent }}</span>
                        </div>
                        <!-- 问题定位 -->
                        <div class="feedBackItem-row">
                            <span class="itemLabel">问题定位:</span>
                            <span class="itemVal location-val">
                                <img src="@/assets/img/positionIcon.png" alt="问题定位" class="location-icon">
                                <span>{{ detailData.issueSpecificLocation || detailData.specificLocation || '--' }}</span>
                            </span>
                        </div>
                        <!-- 反馈文件 -->
                        <div class="feedBackItem-file"
                            v-if="detailData.uploadedFiles && detailData.uploadedFiles.length > 0">
                            <span class="itemLabel">反馈文件:</span>
                            <div class="fileList">
                                <div class="fileItem" v-for="(fileUrl, index) in detailData.uploadedFiles" :key="index"
                                    @click="previewFile(fileUrl)">
                                    <img v-if="isImage(fileUrl)" :src="resolveFileUrl(fileUrl)" alt="反馈图片" />
                                    <div v-else class="video-icon">
                                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                                            <path d="M8 5v14l11-7z" fill="#fff" />
                                        </svg>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 处理进度模块 (独立容器) -->
                <div style="padding-top: 0px;" class="feedBackProcessCard" :class="{ 'status-pending-card': detailData.status === 0 }" v-if="detailData">
                    <!-- 超时图片 -->
                    <img v-if="isOverdue" :src="isOverTimeImg" class="overtime-mark" alt="已超时">

                    <!-- 处理进度标题 -->
                    <div class="process-title" v-if="detailData.status !== 0">
                        <span class="title-text">处理进度</span>
                    </div>

                    <!-- 标题容器 -->
                    <div class="process-card-header" v-if="detailData.status !== 0">
                        <div class="title-text">
                            计划完成时间
                            <span class="task-time-val" v-if="plannedCompletionTimeFormatted && plannedCompletionTimeFormatted !== '--'">{{ plannedCompletionTimeFormatted }}</span>
                        </div>
                        <div class="header-right" v-if="detailData.status === 2 && roleName !== '普通角色' && isOverdue">
                            <!-- 未催办时显示催办按钮 -->
                            <div v-if="!isUrged" class="urge-btn-new" @click="handleUrge">催办</div>
                            <!-- 已催办时显示已催办按钮（灰色不可点击） -->
                            <div v-else class="urge-btn-new urged">已催办</div>
                        </div>
                    </div>

                    <div class="timeline-container" :class="{ 'status-pending': detailData.status === 0 }">
                        <!-- 使用拆分后的状态组件 -->
                        <PendingStatus v-if="detailData.status === 0" ref="pendingStatusRef"
                            :detail-data="detailData"
                            v-model:exists-reason="existsReason" v-model:selected-assignee="selectedAssignee"
                            v-model:task-time="taskTime" v-model:exists-file-list="existsFileList"
                            :assignee-list="assigneeList"
                            @exists-upload-success="handleExistsUploadSuccess"
                            @exists-upload-delete="handleExistsUploadDelete"
                            @exists-upload-status-change="handleUploadStatusChange" />

                        <RejectedStatus v-else-if="detailData.status === 1" :detail-data="detailData" @preview-file="previewFile" />

                        <ProcessingStatus v-else-if="detailData.status === 2" :detail-data="detailData"
                            v-model:processor-result-type="processorResultType"
                            v-model:treatment-file-list="treatmentFileList" v-model:treatment-content="treatmentContent"
                            @upload-success="handleTreatmentUploadSuccess" @upload-delete="handleTreatmentUploadDelete"
                            @upload-status-change="handleUploadStatusChange" @preview-file="previewFile" />

                        <VerifyingStatus v-else-if="detailData.status === 3" :detail-data="detailData"
                            v-model:is-verification-passed="isVerificationPassed"
                            v-model:verification-file-list="verificationFileList"
                            v-model:verification-content="verificationContent"
                            @upload-success="handleVerificationUploadSuccess"
                            @upload-delete="handleVerificationUploadDelete"
                            @upload-status-change="handleUploadStatusChange" @preview-file="previewFile" />

                        <CompletedStatus v-else-if="detailData.status === 4" :detail-data="detailData"
                            @preview-file="previewFile" />
                        
                        <!-- 调试信息：如果 status 不在预期范围内，显示调试信息 -->
                        <div v-else class="debug-info" style="padding: 24px; color: #ff0000; font-size: 24px;">
                            <p>状态值异常: status = {{ detailData.status }} (类型: {{ typeof detailData.status }})</p>
                            <p>请检查数据格式是否正确</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 底部操作按钮栏 -->
        <div class="bottom-action-bar" :class="{ 'has-bottom-nav': hasBottomNav }" v-if="detailData && detailData.status !== 4 && detailData.status !== 1">
            <button class="btn-cancel" @click="handleClose">取消</button>
            <button class="btn-submit" :disabled="!isReadyToSubmit || isSubmitting" @click="handleSubmit">
                <span v-if="isSubmitting" class="loading-spinner"></span>
                <span>{{ isSubmitting ? '提交中...' : '提交' }}</span>
            </button>
        </div>

        <!-- 二次确认弹窗 -->
        <transition name="fade">
            <div class="confirm-modal-mask" v-if="showConfirmModal" @click="showConfirmModal = false">
                <div class="confirm-modal-content" @click.stop>
                    <div class="modal-title">关闭提示</div>
                    <div class="modal-desc">问题无需处理，确定关闭问题？</div>
                    <div class="modal-footer">
                        <button class="modal-btn-cancel" @click="showConfirmModal = false">取消</button>
                        <button class="modal-btn-confirm" @click="handleFinalSubmit">确定</button>
                    </div>
                </div>
            </div>
        </transition>

        <!-- 催办成功弹窗 -->
        <transition name="fade">
            <div class="confirm-modal-mask" v-if="showUrgeModal" @click="showUrgeModal = false">
                <div class="confirm-modal-content urge-modal-content" @click.stop>
                    <img :src="hurrpUpImg" class="urge-success-img" alt="催办成功">
                    <div class="modal-title" style="">问题未及时处理，是否发短信催办？</div>
                    <div class="modal-footer">
                        <button class="modal-btn-cancel" @click="showUrgeModal = false">取消</button>
                        <button class="modal-btn-confirm" @click="handleUrgeConfirm">确定</button>
                    </div>
                </div>
            </div>
        </transition>
    </div>
</template>

<script>
import PageHeader from '@/components/commonCom/PageHeader.vue';
import { ElSelect, ElOption, ElMessage, ElCheckbox, ElDatePicker } from 'element-plus';
import MediaUpload from '@/components/commonCom/MediaUpload.vue';
import isOverTimeImg from '@/assets/img/isOverTimeImg.png';
import hurrpUpImg from '@/assets/img/hurrpUpImg.png';

// 导入状态组件
import PendingStatus from './differentStatus/PendingStatus.vue';
import RejectedStatus from './differentStatus/RejectedStatus.vue';
import ProcessingStatus from './differentStatus/ProcessingStatus.vue';
import VerifyingStatus from './differentStatus/VerifyingStatus.vue';
import CompletedStatus from './differentStatus/CompletedStatus.vue';
import CommonMap from './differentStatus/commonMap.vue';
import tool from '@/utils/tools';
import { getCurrentLocationByAmap, transformWGS84ToGCJ02 } from '@/utils/amapLocation';

export default {
    name: 'FixFeedBack',
  components: {
      PageHeader,
      ElSelect,
      ElOption,
      ElCheckbox,
      ElDatePicker,
      MediaUpload,
      PendingStatus,
      RejectedStatus,
      ProcessingStatus,
      VerifyingStatus,
      CompletedStatus,
      CommonMap
  },
    data() {
        return {
            isOverTimeImg,
            hurrpUpImg,
            detailData: null,
            feedbackDetail: null, // 存储从接口获取的完整反馈详情
            // 情景 1 数据
            problemStatus: null, // 'exists' | 'not_exists'
            selectedAssignee: '',
            taskTime: '',
            assigneeList: [],
            rejectReason: '',
            existsReason: '',
            existsFileList: [],
            existsUploadResults: [],
            notExistsFileList: [],
            notExistsUploadResults: [],
            // 情景 2 数据
            processorResultType: '', // 'processed' 或 'no_need'
            treatmentContent: '',
            treatmentFileList: [],
            treatmentUploadResults: [],
            // 情景 3 数据
            isVerificationPassed: false,
            verificationContent: '',
            verificationFileList: [],
            verificationUploadResults: [],
            hasUploading: false,
            showConfirmModal: false,
            showUrgeModal: false,
            isUrged: false, // 是否已催办
            isSubmitting: false // 是否正在提交
        };
    },
    computed: {
        // 判断是否有底部返回按钮（安卓设备且路由 meta.showBackButton 为 true）
        hasBottomNav() {
            
            const isAndroid = tool.isAndroid();
            const hasMeta = this.$route && this.$route.meta && this.$route.meta.showBackButton === true;
            const result = isAndroid && hasMeta;
            
            // 调试日志
            console.log('[fixFeedBack] hasBottomNav 计算:', {
                isAndroid,
                routeName: this.$route?.name,
                hasMeta,
                result
            });
            
            return result;
        },
        // 判断是否有底部操作按钮（status !== 4 且 status !== 1 时显示）
        hasBottomButton() {
            if (!this.detailData) return false;
            return this.detailData.status !== 4 && this.detailData.status !== 1;
        },
        // 获取当前用户角色名称
        roleName() {
            return this.$store.getters.getRoleName || '';
        },
        // 判断是否超时（基于 plannedCompletionTime）
        isOverdue() {
            // 只判断处理中（status === 2）和待核验（status === 3）阶段
            if (!this.detailData || (this.detailData.status !== 2 && this.detailData.status !== 3)) {
                return false;
            }

            // 优先使用 detailData.plannedCompletionTime（如果列表数据中有）
            if (this.detailData.plannedCompletionTime) {
                const currentTime = new Date().getTime();
                const plannedTime = this.detailData.plannedCompletionTime;
                return currentTime > plannedTime;
            }

            // 如果 detailData 中没有，从 feedbackDetail 的 statusTasks 中获取
            // plannedCompletionTime 是在 status === 2（处理中）阶段设置的
            if (!this.feedbackDetail || !this.feedbackDetail.statusTasks || !Array.isArray(this.feedbackDetail.statusTasks)) {
                return false;
            }

            // 查找 status === 2 的任务（处理中阶段的任务，包含 plannedCompletionTime）
            const processingTask = this.feedbackDetail.statusTasks.find(task => task.status === 2);
            if (!processingTask || !processingTask.plannedCompletionTime) {
                return false;
            }

            // 判断是否超时：当前时间 > plannedCompletionTime
            const currentTime = new Date().getTime();
            const plannedTime = processingTask.plannedCompletionTime;

            return currentTime > plannedTime;
        },
        // 获取计划完成时间（格式化显示）
        plannedCompletionTimeFormatted() {
            // 优先使用 detailData.plannedCompletionTime（如果列表数据中有）
            if (this.detailData && this.detailData.plannedCompletionTime) {
                const timestamp = this.detailData.plannedCompletionTime;
                const date = new Date(timestamp);
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                const hours = String(date.getHours()).padStart(2, '0');
                const minutes = String(date.getMinutes()).padStart(2, '0');
                const seconds = String(date.getSeconds()).padStart(2, '0');
                return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
            }
            
            // 如果 detailData 中没有，则从 feedbackDetail 中查找
            if (!this.feedbackDetail || !this.feedbackDetail.statusTasks || !Array.isArray(this.feedbackDetail.statusTasks)) {
                return '--';
            }
            
            // 查找 status === 2 的任务（处理中阶段的任务，包含 plannedCompletionTime）
            const processingTask = this.feedbackDetail.statusTasks.find(task => task.status === 2);
            if (!processingTask || !processingTask.plannedCompletionTime) {
                return '--';
            }
            
            // 格式化时间：YYYY-MM-DD HH:mm:ss
            const timestamp = processingTask.plannedCompletionTime;
            const date = new Date(timestamp);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            const seconds = String(date.getSeconds()).padStart(2, '0');
            
            return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
        },
        // 判断是否满足提交条件
        isReadyToSubmit() {
            if (!this.detailData) return false;
            
            // 待受理状态：需要填写问题确认说明、指派人、计划完成时间
            if (this.detailData.status === 0) {
                return this.existsReason && this.existsReason.trim().length > 0 
                    && this.selectedAssignee 
                    && this.taskTime;
            }
            
            // 情景 2
            if (this.detailData.status === 2) {
                if (!this.processorResultType) {
                    return false;
                }
                // 已处理和无需处理都需要填写说明和上传附件
                const hasContent = this.treatmentContent && this.treatmentContent.trim();
                const hasFiles = this.treatmentUploadResults && this.treatmentUploadResults.length > 0;
                return hasContent && hasFiles;
            }

            // 情景 3
            if (this.detailData.status === 3) {
                const hasFiles = this.verificationUploadResults && this.verificationUploadResults.length > 0;
                return this.isVerificationPassed && hasFiles;
            }
            
            return false;
        }
    },
    async mounted() {
        // document.title = '反馈处理';
        // 从路由参数获取数据
        const dataStr = this.$route.query.data;

        if (dataStr) {
            try {
                this.detailData = JSON.parse(dataStr);
                
                // 确保 status 是数字类型
                if (typeof this.detailData.status === 'string') {
                    this.detailData.status = parseInt(this.detailData.status, 10);
                }
                
                // 如果 status 是 NaN 或无效，尝试从接口获取完整数据
                if (isNaN(this.detailData.status) && this.detailData.id) {
                    console.warn('[fixFeedBack] status 字段无效，从接口获取完整数据');
                    // 先获取完整数据，再继续初始化
                    await this.loadFeedbackDetail();
                    // 如果接口返回的数据中有 status，使用接口的数据
                    if (this.feedbackDetail && this.feedbackDetail.status !== undefined) {
                        this.detailData.status = this.feedbackDetail.status;
                        console.log('[fixFeedBack] 从接口获取 status:', this.detailData.status);
                    } else {
                        // 如果接口也没有 status，尝试从 detailData.id 获取
                        console.error('[fixFeedBack] 无法获取有效的 status 值');
                    }
                }
                
                // 调试日志：打印接收到的数据
                console.log('[fixFeedBack] 接收到的数据:', this.detailData);
                console.log('[fixFeedBack] status 值:', this.detailData.status, '类型:', typeof this.detailData.status);
                
                // 如果状态为待受理（status === 0），默认选中"问题存在"
                if (this.detailData.status === 0) {
                    this.problemStatus = 'exists';
                }
                // 如果状态为处理中（status === 2），默认选择"已处理"
                if (this.detailData.status === 2 && !this.processorResultType) {
                    this.processorResultType = 'processed';
                }
                // 如果状态为待核验（status === 3），默认激活"问题已经确认"
                if (this.detailData.status === 3 && !this.isVerificationPassed) {
                    this.isVerificationPassed = true;
                }
                // 获取完整的反馈详情（用于获取 plannedCompletionTime）
                // 如果之前已经调用过，这里不会重复调用（loadFeedbackDetail 内部有判断）
                if (!this.feedbackDetail) {
                    this.loadFeedbackDetail();
                }
            } catch (error) {
                console.error('解析详情数据失败:', error);
                this.handleClose();
            }
        } else {
            this.handleClose();
        }

        // 加载指派人列表
        this.loadAssigneeList();
        
        // 检测底部导航栏是否存在（备选方案）
        this.$nextTick(() => {
            const navBar = document.querySelector('.navigation-bar');
            const bottomActionBar = this.$el.querySelector('.bottom-action-bar');
            if (navBar && bottomActionBar) {
                console.log('[fixFeedBack] 检测到导航栏，调整按钮位置');
                bottomActionBar.classList.add('has-bottom-nav');
            }
        });
    },
    methods: {
        // 加载反馈详情（用于获取 plannedCompletionTime）
        async loadFeedbackDetail() {
            if (!this.detailData || !this.detailData.id) {
                return;
            }

            try {
                const id = this.detailData.id;
                const response = await this.$http.get(`/admin-api/problem/feedback/${id}`);

                if (response && response.data) {
                    this.feedbackDetail = response.data;
                    
                    // 如果 detailData 中没有有效的 status，使用接口返回的 status
                    if (isNaN(this.detailData.status) && response.data.status !== undefined) {
                        this.detailData.status = response.data.status;
                        console.log('[fixFeedBack] 从接口获取 status:', this.detailData.status);
                    }
                    
                    console.log('fixFeedBack - 反馈详情已加载:', this.feedbackDetail);
                }
            } catch (error) {
                console.error('fixFeedBack - 获取反馈详情失败:', error);
            }
        },
        /** 指派人：roleNames 严格为「普通角色」，或昵称为陈丽的普通角色特例 */
        isAssignableFeedbackUser(user) {
            if (!user) return false;
            if (user.roleNames === '普通角色') return true;
            const displayName = String(user.nickname || user.name || user.username || '').trim();
            return displayName === '陈丽';
        },
        // 加载指派人列表
        loadAssigneeList() {
            this.$http.get('/admin-api/problem/feedback/users').then(res => {
                console.log('指派人接口返回:', res.data);

                // 处理不同的返回数据结构
                let userList = [];

                if (res.data) {
                    // 如果直接返回数组
                    if (Array.isArray(res.data)) {
                        userList = res.data;
                    }
                    // 如果返回对象，检查 data 字段
                    else if (res.data.data && Array.isArray(res.data.data)) {
                        userList = res.data.data;
                    }
                    // 如果返回对象，检查 list 字段
                    else if (res.data.list && Array.isArray(res.data.list)) {
                        userList = res.data.list;
                    }
                    // 如果 code 为 0 或 200，且有 data 字段
                    else if ((res.data.code === 0 || res.data.code === 200) && res.data.data) {
                        userList = Array.isArray(res.data.data) ? res.data.data : [];
                    }
                }

                if (userList && userList.length > 0) {
                    // 仅 roleNames 完全等于「普通角色」的用户可指派；特例：陈丽（普通角色）一并纳入
                    userList = userList.filter(user => this.isAssignableFeedbackUser(user));

                    // 将返回的数据转换为 assigneeList 格式
                    this.assigneeList = userList.map(user => ({
                        label: user.nickname || `用户${user.id}`,
                        value: user.id
                    }));
                    console.log('转换后的指派人列表:', this.assigneeList);
                } else {
                    console.warn('指派人列表为空或格式不正确');
                    this.assigneeList = [];
                }
            }).catch(error => {
                console.error('获取指派人列表失败:', error);
                this.assigneeList = [];
            });
        },

        // 关闭页面
        handleClose() {
            this.$router.go(-1);
        },

        // 处理问题状态切换
        handleProblemStatus(status) {
            this.problemStatus = status;
            if (status === 'not_exists') {
                this.selectedAssignee = ''; // 不存在则清空选择
            }
        },

        // 提交处理
        handleSubmit() {
            // 检查是否有文件正在上传
            if (this.hasUploading) {
                ElMessage.warning('您有资源正在上传，请稍后');
                return;
            }

            // 如果是待受理状态，进行表单校验
            if (this.detailData.status === 0) {
                // 触发子组件的所有校验
                if (this.$refs.pendingStatusRef) {
                    const isValid = this.$refs.pendingStatusRef.validateAll();
                    if (!isValid) {
                        // 校验失败，错误提示已在子组件中显示
                        return;
                    }
                }
                
                // 问题确认说明校验
                if (!this.existsReason || !this.existsReason.trim()) {
                    ElMessage.warning('请输入问题确认说明');
                    return;
                }
                // 指派人校验
                if (!this.selectedAssignee) {
                    ElMessage.warning('请选择指派人');
                    return;
                }
                // 计划完成时间校验
                if (!this.taskTime) {
                    ElMessage.warning('请选择计划完成时间');
                    return;
                }
                // 验证计划完成时间不能早于当前时间
                const selectedTime = new Date(this.taskTime.replace(/-/g, '/'));
                const currentTime = new Date();
                if (selectedTime < currentTime) {
                    ElMessage.warning('计划时间不可以早于当前');
                    return;
                }
            }

            // 如果是处理中状态（status === 2），进行表单校验
            if (this.detailData.status === 2) {
                if (!this.processorResultType) {
                    ElMessage.warning('请选择处理结果');
                    return;
                }
                // 已处理和无需处理都需要填写说明和上传附件
                if (!this.treatmentContent || !this.treatmentContent.trim()) {
                    const message = this.processorResultType === 'processed' 
                        ? '请输入处理情况说明' 
                        : '请输入无需处理的原因说明';
                    ElMessage.warning(message);
                    return;
                }
                const hasFiles = this.treatmentUploadResults && this.treatmentUploadResults.length > 0;
                if (!hasFiles) {
                    ElMessage.warning('请至少上传一个附件');
                    return;
                }
            }

            // 如果是待核验状态（status === 3），进行表单校验
            if (this.detailData.status === 3) {
                if (!this.isVerificationPassed) {
                    ElMessage.warning('请勾选"核验通过"');
                    return;
                }
                if (!this.verificationContent || !this.verificationContent.trim()) {
                    ElMessage.warning('请输入核验情况说明');
                    return;
                }
                const hasFiles = this.verificationUploadResults && this.verificationUploadResults.length > 0;
                if (!hasFiles) {
                    ElMessage.warning('请至少上传一个核验附件');
                    return;
                }
            }

            // 其他状态的校验（保持原有逻辑）
            if (!this.isReadyToSubmit) return;

            this.handleFinalSubmit();
        },

        // 最终执行提交
        async handleFinalSubmit() {
            this.showConfirmModal = false;
            this.isSubmitting = true; // 开始提交，显示 loading
            
            // 强制让页面上的输入框/下拉框失去焦点，防止 Element 内部事件冲突
            if (document.activeElement && typeof document.activeElement.blur === 'function') {
                document.activeElement.blur();
            }

            // 如果是待受理状态，调用提交接口
            if (this.detailData.status === 0) {
                try {
                    // 构建请求数据
                    const requestData = {
                        id: String(this.detailData.id),
                        problemExists: this.problemStatus === 'exists',
                        statusDescription: this.problemStatus === 'exists'
                            ? this.existsReason.trim()
                            : this.rejectReason.trim()
                    };

                    // 如果问题存在，添加指派人ID、计划完成时间和文件上传
                    if (this.problemStatus === 'exists') {
                        if (this.selectedAssignee) {
                            requestData.assignedPersonId = String(this.selectedAssignee);
                        }
                        if (this.taskTime) {
                            // 确保时间格式为 YYYY-MM-DD HH:mm:ss
                            let dateTime = '';
                            if (this.taskTime.includes('T')) {
                                // 如果已经是 ISO 格式，转换为空格格式
                                dateTime = this.taskTime.replace('T', ' ').substring(0, 19);
                            } else if (this.taskTime.includes(' ')) {
                                // 如果已经是空格格式，确保格式正确
                                dateTime = this.taskTime.substring(0, 19);
                            } else {
                                // 如果是 YYYY-MM-DD 格式，添加时间部分
                                dateTime = `${this.taskTime} 00:00:00`;
                            }
                            requestData.plannedCompletionTime = dateTime;
                        }
                        // 添加文件上传字段
                        if (this.existsUploadResults && this.existsUploadResults.length > 0) {
                            requestData.problemReviewImages = this.existsUploadResults.map(item => item.fileUrl || item.url).filter(url => url);
                        }
                    } else {
                        // 问题不存在时，也添加文件上传字段（如果有）
                        if (this.notExistsUploadResults && this.notExistsUploadResults.length > 0) {
                            requestData.problemReviewImages = this.notExistsUploadResults.map(item => item.fileUrl || item.url).filter(url => url);
                        }
                    }

                    console.log('提交数据:', requestData);

                    // 先调用提交接口
                    const response = await this.$http.post('/admin-api/problem/feedback/audit-assign', requestData);

                    // 判断提交是否成功
                    // 接口返回格式: {code: 0, msg: "", data: true}
                    // 根据其他文件（MediaUpload.vue, login.vue）的用法，$http.post 返回的 response 直接包含 code
                    // 直接使用 response.code 判断，兼容 response.data.code
                    const code = response.code !== undefined ? response.code : (response.data?.code);

                    if (code === 0 || code === 200 || code === '0' || code === '200') {
                        // 表单提交成功后，立即显示成功消息并返回
                        let successMsg = '';
                if (this.problemStatus === 'exists') {
                    successMsg = '操作成功, 稍后将为您发送短信通知指派人';
                } else {
                    successMsg = '操作成功，反馈已驳回';
                }

                        ElMessage.success({
                            message: successMsg,
                            duration: 2000
                        });

                        // 立即返回列表，不等待
                        setTimeout(() => {
                            this.$router.go(-1);
                        }, 500);

                        // 后台异步发送问题确认通知请求，不阻塞用户界面
                        this.$http.post('/admin-api/problem/feedback/notify-assign', {
                            id: this.detailData.id
                        }).then(() => {
                            console.log('问题确认通知发送成功');
                        }).catch((notifyError) => {
                            console.error('发送问题确认通知失败:', notifyError);
                            // 通知失败不影响主流程
                        });
                    } else {
                        const errorMsg = response.msg || response.data?.msg || response.message || response.data?.message || '提交失败，请重试';
                        console.error('提交失败 - code:', code, 'errorMsg:', errorMsg, '完整响应:', response);
                        ElMessage.error(errorMsg);
                        this.isSubmitting = false; // 提交失败，重置 loading
                    }
                } catch (error) {
                    console.error('提交失败:', error);
                    const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '提交失败，请重试';
                    ElMessage.error(errorMsg);
                    this.isSubmitting = false; // 提交失败，重置 loading
                }
                return;
            }

            // 如果是处理中状态（status === 2），调用处理结果提交接口
            if (this.detailData.status === 2) {
                try {
                    // 构建请求数据
                    const requestData = {
                        id: this.detailData.id,  // 问题反馈ID (integer)
                        handleResult: this.processorResultType === 'processed' ? '已处理' : '无需处理',  // 处理结果（必填）
                        resolutionDescription: '',  // 处理描述（必填）
                    };

                    // 已处理和无需处理都需要处理描述和附件
                    requestData.resolutionDescription = this.treatmentContent.trim();  // 处理描述
                    // 添加处理附件URL列表（可选）
                    if (this.treatmentUploadResults && this.treatmentUploadResults.length > 0) {
                        requestData.uploadedFiles = this.treatmentUploadResults.map(item => item.fileUrl || item.url).filter(url => url);
                    }

                    console.log('提交处理结果数据:', requestData);

                    // 调用处理结果提交接口
                    const response = await this.$http.post('/admin-api/problem/feedback/process', requestData);

                    console.log('处理结果提交响应:', response);

                    // 判断提交是否成功
                    const code = response.code !== undefined ? response.code : (response.data?.code);

                    if (code === 0 || code === 200 || code === '0' || code === '200') {
                        ElMessage.success({
                            message: '提交成功,等待管理员核验',
                            duration: 2000
                        });

                        // 立即返回列表
                        setTimeout(() => {
                            this.$router.go(-1);
                        }, 500);
                    } else {
                        const errorMsg = response.msg || response.data?.msg || response.message || response.data?.message || '提交失败，请重试';
                        console.error('提交处理结果失败 - code:', code, 'errorMsg:', errorMsg);
                        ElMessage.error(errorMsg);
                        this.isSubmitting = false; // 提交失败，重置 loading
                    }
                } catch (error) {
                    console.error('提交处理结果失败:', error);
                    const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '提交失败，请重试';
                    ElMessage.error(errorMsg);
                    this.isSubmitting = false; // 提交失败，重置 loading
                }
                return;
            }

            // 如果是待核验状态（status === 3），调用核验提交接口
            if (this.detailData.status === 3) {
                try {
                    // 获取处理任务信息，判断是否"无需处理"
                    let noNeedHandle = false;
                    if (this.feedbackDetail && this.feedbackDetail.statusTasks && Array.isArray(this.feedbackDetail.statusTasks)) {
                        // 查找处理阶段的任务（status === 2 或 status === 3）
                        const processingTask = this.feedbackDetail.statusTasks.find(task => task.status === 2 || task.status === 3);
                        if (processingTask && processingTask.noNeedHandle === true) {
                            noNeedHandle = true;
                        }
                    }
                    
                    // 如果 noNeedHandle 是 true，solved 需要传递 false
                    // 否则根据管理员的核验选择来决定
                    const solved = noNeedHandle ? false : this.isVerificationPassed;
                    
                    // 构建请求数据
                    const requestData = {
                        id: this.detailData.id,  // 问题反馈ID
                        solved: solved,  // 核验结论（true=问题已解决，false=问题驳回）
                        verificationResult: this.verificationContent.trim()  // 核验结果
                    };

                    // 添加核验附件URL列表（如果有）
                    if (this.verificationUploadResults && this.verificationUploadResults.length > 0) {
                        requestData.uploadedFiles = this.verificationUploadResults.map(item => item.fileUrl || item.url).filter(url => url);
                    }

                    console.log('提交核验结果数据:', requestData);

                    // 调用核验提交接口
                    const response = await this.$http.post('/admin-api/problem/feedback/verify', requestData);

                    console.log('核验结果提交响应:', response);

                    // 判断提交是否成功
                    const code = response.code !== undefined ? response.code : (response.data?.code);

                    if (code === 0 || code === 200 || code === '0' || code === '200') {
                        // 根据是否"无需处理"显示不同的提示消息
                        const message = noNeedHandle 
                            ? '问题已驳回,稍后将通知反馈人' 
                            : '问题已办结, 稍后将通知反馈人';
                        ElMessage.success({
                            message: message,
                            duration: 2000
                        });

                        // 立即返回列表
                        setTimeout(() => {
                            this.$router.go(-1);
                        }, 500);

                        // 后台异步发送核验通知请求，不阻塞用户界面（已注释：暂不发给反馈人短信）
                        // // 根据核验结果（solved）决定发送哪个接口
                        // const notifyEndpoint = solved
                        //     ? '/admin-api/problem/feedback/notify-finish'  // 已办结
                        //     : '/admin-api/problem/feedback/notify-reject';  // 已驳回
                        //
                        // this.$http.post(notifyEndpoint, {
                        //     id: this.detailData.id
                        // }).then(() => {
                        //     console.log(solved ? '核验完成通知发送成功' : '核验驳回通知发送成功');
                        // }).catch((notifyError) => {
                        //     console.error(solved ? '发送核验完成通知失败:' : '发送核验驳回通知失败:', notifyError);
                        //     // 通知失败不影响主流程
                        // });
                    } else {
                        const errorMsg = response.msg || response.data?.msg || response.message || response.data?.message || '提交失败，请重试';
                        console.error('提交核验结果失败 - code:', code, 'errorMsg:', errorMsg);
                        ElMessage.error(errorMsg);
                        this.isSubmitting = false; // 提交失败，重置 loading
                    }
                } catch (error) {
                    console.error('提交核验结果失败:', error);
                    const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '提交失败，请重试';
                    ElMessage.error(errorMsg);
                    this.isSubmitting = false; // 提交失败，重置 loading
                }
                return;
            }

            // 其他状态的处理（保持原有逻辑）
            let successMsg = '';

            // 使用导入的 ElMessage 替代 this.$message
            ElMessage.success({
                message: successMsg,
                duration: 2000
            });
            
            // 等待 2s 后返回列表
            setTimeout(() => {
                this.$router.go(-1);
            }, 2000);
        },

        // 处理催办（只显示弹窗，不更新状态）
        handleUrge() {
            this.showUrgeModal = true;
        },
        // 确认催办（点击确定按钮后更新状态）
        async handleUrgeConfirm() {
            try {
                // 调用催办通知接口
                await this.$http.post('/admin-api/problem/feedback/notify-overdue', {
                    id: this.detailData.id
                });
                
                // 显示成功提示
                ElMessage.success('催办通知发送成功');
                                
                // 更新状态并关闭弹窗
                this.isUrged = true;
                this.showUrgeModal = false;
            } catch (error) {
                console.error('发送催办通知失败:', error);
                const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '催办通知发送失败';
                ElMessage.error(errorMsg);
                // 即使通知失败，也关闭弹窗（可选：根据业务需求决定是否更新状态）
                this.showUrgeModal = false;
            }
        },

        // 处理核验成功的上传
        handleVerificationUploadSuccess(uploadResult) {
            this.verificationUploadResults.push(uploadResult);
        },

        // 处理核验删除
        handleVerificationUploadDelete(deleteInfo) {
            const index = this.verificationUploadResults.findIndex(item => item.fileUrl === deleteInfo.fileUrl);
            if (index !== -1) {
                this.verificationUploadResults.splice(index, 1);
            }
        },

        // 处理处理成功的上传
        handleTreatmentUploadSuccess(uploadResult) {
            this.treatmentUploadResults.push(uploadResult);
        },

        // 处理上传删除
        handleTreatmentUploadDelete(deleteInfo) {
            const index = this.treatmentUploadResults.findIndex(item => item.fileUrl === deleteInfo.fileUrl);
            if (index !== -1) {
                this.treatmentUploadResults.splice(index, 1);
            }
        },

        // 处理上传状态变化
        handleUploadStatusChange(hasUploading) {
            this.hasUploading = hasUploading;
        },

        // 处理问题存在时的文件上传成功
        handleExistsUploadSuccess(uploadResult) {
            this.existsUploadResults.push(uploadResult);
        },

        // 处理问题存在时的文件删除
        handleExistsUploadDelete(deleteInfo) {
            const index = this.existsUploadResults.findIndex(item => item.fileUrl === deleteInfo.fileUrl);
            if (index !== -1) {
                this.existsUploadResults.splice(index, 1);
            }
        },

        // 处理问题不存在时的文件上传成功
        handleNotExistsUploadSuccess(uploadResult) {
            this.notExistsUploadResults.push(uploadResult);
        },

        // 处理问题不存在时的文件删除
        handleNotExistsUploadDelete(deleteInfo) {
            const index = this.notExistsUploadResults.findIndex(item => item.fileUrl === deleteInfo.fileUrl);
            if (index !== -1) {
                this.notExistsUploadResults.splice(index, 1);
            }
        },

        // 格式化时间戳
        formatTime(timestamp) {
            if (!timestamp) return '--';
            return this.$tool.dateFormat(timestamp, 'yyyy-MM-dd hh:mm:ss');
        },

        // 判断是否为图片
        isImage(url) {
            if (!url) return false;
            const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
            const lowerUrl = url.toLowerCase();
            return imageExtensions.some(ext => lowerUrl.includes(ext));
        },

        // 预览文件
        previewFile(fileUrl) {
            if (this.isImage(fileUrl)) {
                window.open(this.resolveFileUrl(fileUrl), '_blank');
            } else {
                window.open(this.resolveFileUrl(fileUrl), '_blank');
            }
        },

        // 格式化日期（仅日期）
        formatTimeDate(timestamp) {
            if (!timestamp) return '--';
            return this.$tool.dateFormat(timestamp, 'yyyy-MM-dd');
        },

        // 获取 referenceType 对应的中文字段
        getReferenceTypeLabel(referenceType) {
            const typeMap = {
                'river': '河道',
                'river_section': '河段',
                'reservoir': '水库'
            };
            return typeMap[referenceType] || '--';
        },

        // 获取 referenceType 拼接 '名称' 的标签
        getReferenceTypeNameLabel(referenceType) {
            const typeMap = {
                'river': '河道名称',
                'river_section': '河段名称',
                'reservoir': '水库名称'
            };
            return typeMap[referenceType] || '名称';
        },
        
        // 获取类型标签文字（用于标题处的色块）
        getReferenceTypeBadgeLabel(referenceType) {
            const typeMap = {
                'river': '河道',
                'reservoir': '水库'
            };
            return typeMap[referenceType] || '';
        },
        
        // 获取类型色块的类名（用于设置不同颜色）
        getTypeBadgeClass(referenceType) {
            if (!referenceType) return '';
            const classMap = {
                'river': 'type-badge-river',      // 河道 - 蓝色
                'reservoir': 'type-badge-reservoir' // 水库 - 绿色
            };
            return classMap[referenceType] || 'type-badge-river'; // 默认蓝色
        },
        
        // 获取反馈类型色块颜色
        getFeedbackTypeColor(feedbackTypeLabel) {
            if (!feedbackTypeLabel) return '#2F88DB';

            const typeMap = {
                '漂浮物': '#369EFF',
                '设施损坏': '#3CB298',
                '设备损坏': '#3CB298'
            };

            return typeMap[feedbackTypeLabel] || '#2F88DB'; // 其他类型默认为 #2F88DB
        },
        
        // 获取标题文案：位置名称发现feedbackType问题
        getTitleText(item) {
            if (!item) return '';
            
            let locationText = '';
            
            // 根据 referenceType 获取对应的名称
            if (item.referenceType === 'river') {
                // 河道：显示 riverName
                if (item.riverName) {
                    locationText = item.riverName;
                    // 如果有河段名称，添加分隔符和河段名称（无空格）
                    if (item.riverSectionName) {
                        locationText += `|${item.riverSectionName}`;
                    }
                }
            } else if (item.referenceType === 'reservoir') {
                // 水库：显示 reservoirName 或 referenceName
                if (item.reservoirName) {
                    locationText = item.reservoirName;
                } else if (item.referenceName) {
                    locationText = item.referenceName;
                }
            } else {
                // 其他类型：使用 referenceName
                if (item.referenceName) {
                    locationText = item.referenceName;
                }
            }
            
            // 组合标题：位置名称发现反馈类型问题（无空格）
            const feedbackType = item.feedbackTypeLabel || '';
            let title = '';
            if (locationText && feedbackType) {
                title = `${locationText}发现${feedbackType}问题`;
            } else if (locationText) {
                title = locationText;
            } else if (feedbackType) {
                title = `发现${feedbackType}问题`;
            }
            
            return title || '创建的问题';
        },
        
        // 右上角按钮点击事件 - 获取当前位置并打开高德地图导航
        async handleTopRightButtonClick() {
            try {
                // 使用高德 API 获取当前位置（只获取经纬度，不需要地址）
                const locationData = await getCurrentLocationByAmap();
                
                // 获取当前位置（WGS84坐标系）
                const currentLat = locationData.latitude;
                const currentLng = locationData.longitude;
                
                console.info('获取当前位置成功:', {
                    latitude: currentLat,
                    longitude: currentLng
                });
                
                // 获取问题位置（终点）
                if (!this.detailData) {
                    ElMessage.warning('问题数据不存在');
                    return;
                }
                
                const problemLat = this.detailData.problemLatitude;
                const problemLng = this.detailData.problemLongitude;
                
                if (!problemLat || !problemLng) {
                    ElMessage.warning('问题位置信息不存在');
                    return;
                }
                
                console.info('问题位置:', {
                    latitude: problemLat,
                    longitude: problemLng
                });
                
                // 将 WGS84 坐标转换为 GCJ-02 坐标（高德地图使用 GCJ-02）
                const [currentGcjLng, currentGcjLat] = transformWGS84ToGCJ02(currentLng, currentLat);
                const [problemGcjLng, problemGcjLat] = transformWGS84ToGCJ02(problemLng, problemLat);
                
                console.info('转换后的坐标 (GCJ-02):', {
                    起点: { latitude: currentGcjLat, longitude: currentGcjLng },
                    终点: { latitude: problemGcjLat, longitude: problemGcjLng }
                });
                
                // 打开高德地图网页版导航
                // 高德地图导航URL格式：https://uri.amap.com/navigation?from=起点经度,起点纬度&to=终点经度,终点纬度&mode=driving
                // 注意：高德地图使用 GCJ-02 坐标系，已经转换过了
                // 坐标格式：经度,纬度（注意顺序）
                const fromPoint = `${currentGcjLng.toFixed(6)},${currentGcjLat.toFixed(6)}`;
                const toPoint = `${problemGcjLng.toFixed(6)},${problemGcjLat.toFixed(6)}`;
                const amapUrl = `https://uri.amap.com/navigation?from=${fromPoint}&to=${toPoint}&mode=driving`;
                
                console.info('高德地图导航URL:', amapUrl);
                console.info('起点坐标 (GCJ-02):', fromPoint);
                console.info('终点坐标 (GCJ-02):', toPoint);
                
                // 尝试打开高德地图
                try {
                    // 先尝试使用 window.open
                    const newWindow = window.open(amapUrl, '_blank');
                    
                    // 检查是否被浏览器阻止（弹窗拦截）
                    if (!newWindow || newWindow.closed || typeof newWindow.closed === 'undefined') {
                        // 如果被阻止，使用 location.href 在当前窗口打开
                        console.info('浏览器阻止了弹窗，使用 location.href 打开');
                        window.location.href = amapUrl;
                    } else {
                        console.info('成功打开高德地图导航窗口');
                        ElMessage.success('正在打开高德地图导航...');
                    }
                } catch (openError) {
                    console.info('window.open 失败，使用 location.href:', openError);
                    // 如果 window.open 失败，直接跳转
                    window.location.href = amapUrl;
                }
                
            } catch (error) {
                const errorMsg = error.message || '获取位置失败';
                console.info('获取当前位置失败:', errorMsg);
                ElMessage.error(errorMsg);
            }
        }
    }
}
</script>

<style lang="scss" scoped>
@use "./fixFeedBack.scss" as *;

.fix-feedBack-container {
    position: relative;
    width: 100%;
    height: 100vh;
    height: 100dvh; // 使用动态视口高度，适配移动端浏览器
    background-color: #f5f5f5;
    display: flex;
    flex-direction: column;
    overflow: hidden; // 防止整个容器滚动

    .content-layer {
        width: 100%;
        // 默认高度：撑满整个屏幕（没有底部按钮时）
        height: 100vh;
        height: 100dvh; // 动态视口高度
        display: flex;
        flex-direction: column;
        overflow-y: auto; // 内容区域可滚动
        overflow-x: hidden;
        -webkit-overflow-scrolling: touch; // iOS 平滑滚动
        position: relative;
        z-index: 1; // 确保内容在按钮下方，但可以滚动
        // 确保可以滚动
        overscroll-behavior: contain;
        
        // 当有底部按钮时，减去按钮高度（120px + 安全区域）
        &.has-bottom-button {
            height: calc(100vh - 120px - env(safe-area-inset-bottom, 0px));
            height: calc(100dvh - 120px - env(safe-area-inset-bottom, 0px));
            
            // 当同时有底部按钮和底部导航栏时，需要减去导航栏高度（95px）
            &.has-bottom-nav {
                height: calc(100vh - 120px - 95px - env(safe-area-inset-bottom, 0px));
                height: calc(100dvh - 120px - 95px - env(safe-area-inset-bottom, 0px));
            }
        }
        
        // 确保滚动条样式统一
        &::-webkit-scrollbar {
            width: 0;
            background: transparent;
        }
        
        /* Firefox */
        scrollbar-width: none;

        // 地图容器
        .map-wrapper {
            width: 100%;
            height: 300px;
            min-height: 300px;
            flex-shrink: 0;
            position: relative;
        }

        // 问题位置地图 - 参与滚动
        .problem-location-map {
            width: 100%;
            height: 100%;
            border-radius: 0; // 去掉圆角，与页面边缘对齐
            overflow: hidden;
            position: relative; // 普通定位，参与滚动
            z-index: 0; // 地图在底层
        }

        // 地图内的按钮 - 右下角偏上
        .map-button {
            position: absolute;
            bottom: 60px; // 距离底部60px，偏上一点
            right: 50px; // 距离右边24px
            width: 148px;
            height: 52px;
            border-radius: 26px;
            opacity: 1;
            background: url('@/assets/img/gotoImg.png') no-repeat center center / 100% 100%;
            background-size: 100% 100%;
            z-index: 1000; // 确保在地图之上
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.5);
            border: none;
            cursor: pointer;
        }

        .paddingContent {
            flex: 1 1 auto; // 允许增长和收缩
            overflow: visible; // 取消滚动，由父容器滚动
            padding: 0;
            display: flex;
            flex-direction: column;
            align-items: center;
            min-height: min-content; // 允许内容自然增长
            width: 100%;
            margin-top: -40px; // 向上移动，覆盖地图一部分
            position: relative;
            z-index: 10; // 提高层级，确保内容完全盖住地图
            // 添加底部留白，确保最后的内容可以完全显示
            padding-bottom: 40px;

            .feedBackDetailCard,
            .feedBackProcessCard {
                position: relative;
                width: 100%;
                max-width: 702px; // 最大宽度，小屏幕自适应
                background-color: #ffffff;
                padding: 24px 24px 0px 24px;
                margin-top: 16px;
                box-sizing: border-box;
                
                // 待受理状态时，减少上内边距
                &.status-pending-card {
                    padding-top:0px;
                }

                .overtime-mark {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 85px;
                    height: 28px;
                    z-index: 5;
                    pointer-events: none; // 防止图片阻挡点击
                }

                .process-card-header {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    padding: 20px 0px;
                    border-bottom: 1px solid rgba(238, 238, 238, 0.5);
                    margin-bottom: 0px;

                    .title-text {
                        font-size: 28px;
                        font-weight: 500;
                        color: #3d3d3d;
                        display: flex;
                        align-items: center;
                        gap: 16px;
                        flex: 1;
                        min-width: 0; // 允许文本收缩

                        .task-time-val {
                            font-size: 28px;
                            color: #666;
                            font-weight: 400;
                            white-space: nowrap;
                        }
                    }

                    .header-right {
                        display: flex;
                        align-items: center;
                        gap: 16px;
                        flex-shrink: 0; // 防止按钮被压缩

                        .urge-btn-new {
                            width: 80px;
                            height: 48px;
                            background: #ff5100;
                            border-radius: 6px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            color: #fff;
                            font-size: 24px;
                            font-weight: 500;
                            cursor: pointer;

                            &:active {
                                opacity: 0.8;
                            }

                            // 已催办状态样式
                            &.urged {
                                width: 80px;
                                height: 48px;
                                border-radius: 6px;
                                opacity: 1;
                                border: 1px solid #999999;
                                background: #ffffff;
                                color:#999999;
                                cursor: not-allowed;

                                &:active {
                                    opacity: 1;
                                }
                            }
                        }
                    }
                }

                .itemTitle {
                    color: #3d3d3d;
                    font-size: 32px;
                    font-weight: 500;
                    line-height: 32px;
                    padding-bottom: 16px;
                    border-bottom: 1px solid rgba(238, 238, 238, 0.5);
                    margin-bottom: 16px;
                    display: flex;
                    align-items: center;
                    gap: 16px;

                    .type-badge {
                        height: 32px;
                        border-radius: 4px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: #ffffff;
                        font-size: 24px;
                        font-weight: 500;
                        padding: 4px 6px;
                        flex-shrink: 0;
                        white-space: nowrap;
                        
                        // 河道/水库类型色块
                        &.type-badge-river {
                            width: 56px;
                            background: #369eff;
                        }
                        
                        &.type-badge-reservoir {
                            width: 56px;
                            background: #3CB298;
                        }
                    }

                    .title-text {
                        flex: 1;
                        font-size: 32px;
                        font-weight: 500;
                        color: #3d3d3d;
                        word-break: break-all;
                        min-width: 0; // 允许文本收缩
                    }
                }

                .feedBackBox {
                    display: flex;
                    flex-direction: column;
                    gap: 16px;
                    margin-bottom: 24px;

                    .feedBackItem-row {
                        display: flex;
                        align-items: flex-start;
                        justify-content: flex-start;
                        color: #3d3d3d;
                        font-size: 28px;
                        font-weight: 500;
                        line-height: 40px;

                        .itemLabel {
                            font-weight: 500;
                            flex-shrink: 0;
                            width: 140px;
                        }

                        .itemVal {
                            font-weight: 400;
                            text-align: right;
                            flex: 1;
                            margin-left: 16px;
                            word-break: break-all;
                            min-width: 0; // 允许文本收缩

                            &.location-val {
                                display: flex;
                                align-items: center;
                                gap: 0;
                                justify-content: flex-end;

                                .location-icon {
                                    width: 25px;
                                    height: 25px;
                                    flex-shrink: 0;
                                }

                                span {
                                    flex-shrink: 0;
                                }
                            }
                        }
                    }

                    .feedBackItem-file {
                        margin-top: -3px;

                        .itemLabel {
                            color: #3d3d3d;
                            font-size: 28px;
                            font-weight: 500;
                            line-height: 28px;
                        }

                        .fileList {
                            display: flex;
                            gap: 20px;
                            margin-top: 16px;
                            flex-wrap: wrap;

                            .fileItem {
                                width: 80px;
                                height: 80px;
                                background-color: #f5f5f5;
                                border-radius: 8px;
                                overflow: hidden;
                                cursor: pointer;
                                position: relative;
                                flex-shrink: 0;

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
                    }
                }

                .process-title {
                    height: 81px;
                    line-height: 81px;
                    display: flex;
                    align-items: center;
                    border-bottom: 1px solid rgba(238, 238, 238, 0.5);
                    
                    .title-text {
                        font-size: 32px;
                        font-weight: 500;
                        color: #3d3d3d;
                    }
                }

                .timeline-container {
                    width: 100%;
                    // 添加底部留白，确保最后的内容可以完全显示
                    padding-bottom: 10px;
                    
                    &.status-pending {
                        margin-top: 0;
                        padding-top: 0;
                    }
                }
            }
        }
    }

    /* 底部操作按钮栏 */
    .bottom-action-bar {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        height: 120px;
        min-height: 120px; // 确保最小高度
        background-color: #ffffff;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 24px;
        padding-bottom: env(safe-area-inset-bottom, 0px); // 适配安全区域
        box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
        z-index: 100;
        pointer-events: auto; // 确保按钮可以点击
        
        /* 当有底部导航栏时，向上移动 */
        &.has-bottom-nav {
            bottom: 95px;
            bottom: calc(95px + env(safe-area-inset-bottom, 0px));
        }
    }

    button {
        width: 335px;
        height: 88px;
        border-radius: 8px;
        font-size: 32px;
        font-weight: 500;
        border: none;
        cursor: pointer;
        transition: opacity 0.3s;
        flex-shrink: 0; // 防止按钮被压缩

        &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }

        &:not(:disabled):active {
            opacity: 0.8;
        }
    }

    .btn-cancel {
        background-color: #f5f5f5;
        color: #666666;
    }

    .btn-submit {
        background-color: #349DFF;
        color: #ffffff;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;

        .loading-spinner {
            width: 16px;
            height: 16px;
            border: 2px solid rgba(255, 255, 255, 0.3);
            border-top-color: #ffffff;
            border-radius: 50%;
            animation: spin 0.8s linear infinite;
        }
    }

    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }

    /* 二次确认弹窗样式 */
    .confirm-modal-mask {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.6);
        z-index: 2000;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 40px;
    }

    .confirm-modal-content {
        width: 100%;
        max-width: 600px;
        background: #fff;
        border-radius: 16px;
        padding: 48px 40px;
        display: flex;
        flex-direction: column;
        align-items: center;

        &.urge-modal-content {
            width: 638px;
            height: 395px;
            max-width: none;
            padding: 25px 40px; // 恢复原始 padding
            box-sizing: border-box;
            justify-content: space-between; // 让内容均匀分布
            
            .urge-success-img {
                width: 140px;
                height: 140px;
                object-fit: contain;
                margin-bottom: 0;
            }

            .modal-title {
                margin: 0 !important;
                line-height: 1;
            }

            .modal-footer {
                margin: 0 !important;
                padding-top: 24px !important;
                border-top: 1px solid rgba(238, 238, 238, 0.8) !important;
            }
        }

        .modal-title {
            font-size: 28px;
            font-weight: 400;
            color: #666666;
            margin-bottom: 32px;
        }

        .modal-desc {
            font-size: 28px;
            color: #666666;
            font-weight: 400;
            text-align: center;
            line-height: 1.6;
            margin-bottom: 48px;
            padding: 0 20px;
        }

        .modal-footer {
            width: 100%;
            display: flex;
            gap: 24px;

            button {
                flex: 1;
                height: 88px;
                border-radius: 8px;
                font-size: 32px;
                font-weight: 500;
                border: none;
                cursor: pointer;
            }

            .modal-btn-cancel {
                background: #f5f5f5;
                color: #666;
            }

            .modal-btn-confirm {
                background: #349DFF;
                color: #fff;
            }
        }
    }

    /* 动画 */
    .fade-enter-active,
    .fade-leave-active {
        transition: opacity 0.3s;
    }

    .fade-enter-from,
    .fade-leave-to {
        opacity: 0;
    }

    /* 响应式优化 */
    // @media screen and (max-width: 750px) {
    //     .content-layer {
    //         .paddingContent {
    //             .feedBackDetailCard,
    //             .feedBackProcessCard {
    //                 max-width: calc(100% - 48px); // 小屏幕时减少最大宽度，留出左右边距
    //                 margin-left: 24px;
    //                 margin-right: 24px;
    //             }
    //         }
    //     }

    //     .bottom-action-bar {
    //         padding: 0 16px; // 小屏幕时减少左右内边距
    //         padding-bottom: env(safe-area-inset-bottom, 0px);

    //         button {
    //             width: calc(50% - 8px); // 按钮宽度自适应
    //             max-width: 335px; // 但不超过最大宽度
    //         }
    //     }
    // }

    // /* 超小屏幕优化 */
    // @media screen and (max-width: 375px) {
    //     .content-layer {
    //         .paddingContent {
    //             .feedBackDetailCard,
    //             .feedBackProcessCard {
    //                 padding: 16px 16px 0px 16px; // 减少内边距
    //             }
    //         }
    //     }
    // }
}
</style>
