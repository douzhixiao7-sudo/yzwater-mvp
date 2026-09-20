<!-- 短链接反馈详情页面 -->
<template>
    <div class="feedBack-detail-container">
        <PageHeader v-if="!isWeChatBrowser" title="仪征河长制" @close="handleClose" />
        <div class="content-layer" :class="{ 'has-header': !isWeChatBrowser }">
            <div class="paddingContent">
                <!-- 加载状态 -->
                <div v-if="loading" class="loading-container">
                    <div class="loading-text">加载中...</div>
                </div>

                <!-- 反馈详情卡片 -->
                <div class="feedBackDetailCard" v-if="detailData && !loading">
                    <!-- 问题描述模块（参考 feedBackDetail.vue section-2） -->
                    <div class="section-2">
                        <div class="section-2-content">
                            <!-- 标题部分 -->
                            <div class="section-2-title">
                                <div class="feedback-type-badge" v-if="detailData.feedbackTypeLabel"
                                    :style="{ background: getFeedbackTypeColor(detailData.feedbackTypeLabel) }">
                                    {{ detailData.feedbackTypeLabel }}
                    </div>
                                <div class="title-text">
                                    {{ formatTitleText(detailData) }}
                        </div>
                        </div>
                            <!-- 内容部分 -->
                            <div class="section-2-content-area">
                                {{ detailData.feedbackContent || '--' }}
                        </div>
                        </div>
                    </div>

                    <!-- 反馈文件模块（参考 feedBackDetail.vue section-3） -->
                    <div class="section-3">
                        <div class="section-3-content">
                            <!-- 标题 -->
                            <div class="section-3-title">
                                <span>反馈文件</span>
                            </div>
                            <!-- 内容部分：图片和视频 -->
                            <div class="section-3-content-area">
                                <div v-if="detailData.uploadedFiles && detailData.uploadedFiles.length > 0" class="file-list">
                                    <div 
                                        class="file-item" 
                                        v-for="(fileUrl, index) in detailData.uploadedFiles" 
                                        :key="index"
                                    @click="previewFile(fileUrl)">
                                        <img v-if="isImage(fileUrl)" :src="fileUrl" alt="反馈图片" class="file-image" />
                                        <div v-else class="file-video">
                                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                                            <path d="M8 5v14l11-7z" fill="#fff" />
                                        </svg>
                                    </div>
                                </div>
                            </div>
                                <div v-else class="no-files">
                                    暂无文件
                        </div>
                    </div>
                        </div>
                        </div>

                    <!-- 问题点位模块（参考 feedBackDetail.vue section-4） -->
                    <div class="section-4">
                        <div class="section-4-content">
                            <!-- 标题 -->
                            <div class="section-4-title">
                                <span>问题点位</span>
                        </div>
                            <!-- 地图容器 -->
                            <div class="section-4-map-wrapper">
                                <div id="problem-location-map" ref="problemLocationMap" class="problem-location-map"></div>
                            </div>
                        </div>
                    </div>

                    <!-- 时间线容器 -->
                    <div class="timeline-container" v-if="timelineData && timelineData.length > 0">
                        <div class="timeline-title">
                            <span>处理进度</span>
                        </div>
                        <div class="timeline-list">
                            <div class="timeline-item" v-for="(item, index) in timelineData" :key="index">
                                <div class="timeline-line" v-if="index < timelineData.length - 1"></div>
                                <div class="timeline-line-wrapper">
                                    <div class="timeline-dot"></div>
                                </div>
                                <div class="timeline-content">
                                    <div class="timeline-header">
                                        <span class="timeline-status">{{ getDisplayStatus(item.status) }}</span>
                                        <span class="timeline-time" v-if="item.status === '已反馈' || item.status === '已受理' || item.status === '已办结' || item.status === '已驳回'">{{ formatTimelineTime(item.time) }}</span>
                                    </div>
                                    <!-- 时间线中的文件列表 -->
                                    <div class="timeline-files" v-if="item.status !== '已反馈' && item.uploadedFiles && item.uploadedFiles.length > 0">
                                        <div class="fileList">
                                            <div class="fileItem" v-for="(fileUrl, fileIndex) in item.uploadedFiles" :key="fileIndex"
                                                @click="previewFile(fileUrl)">
                                                <img v-if="isImage(fileUrl)" 
                                                    :src="fileUrl" 
                                                    alt="反馈图片" 
                                                    loading="lazy"
                                                    @error="handleImageError"
                                                    @load="handleImageLoad" />
                                                <div v-else class="video-icon">
                                                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                                                        <path d="M8 5v14l11-7z" fill="#fff" />
                                                    </svg>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="timeline-description" 
                                         v-if="item.status !== '已反馈' && item.description"
                                         :class="{ 'has-background': item.status === '已受理' || item.status === '已办结' || item.status === '已驳回' }">
                                        {{ item.description }}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!-- 底部按钮 -->
        <div class="bottom-action-bar">
            <button class="go-to-list-btn" @click="goToFeedBackList">
                <img src="@/assets/img/backArrow.png" alt="" class="back-arrow-icon">
                <span>去反馈列表</span>
            </button>
        </div>
    </div>
</template>

<script>
import PageHeader from '@/components/commonCom/PageHeader.vue';
import { ElMessage } from 'element-plus';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import tiandituMixin from '@/mixins/tiandituMixin';
import position3Icon from '@/assets/img/position3.png';

// 确保 global 对象存在（浏览器环境兼容）
if (typeof global === 'undefined') {
    window.global = window;
}

export default {
    name: 'ShortUrlDetail',
    components: {
        PageHeader
    },
    mixins: [tiandituMixin],
    data() {
        return {
            // 反馈详情数据
            detailData: null,
            // 时间线数据
            timelineData: [],
            // 加载状态
            loading: false,
            problemMap: null, // 问题点位地图实例
            problemMarker: null, // 问题点位标记
            tiandituVecLayer: null, // 天地图矢量底图图层
            tiandituCvaLayer: null // 天地图矢量标注图层
        };
    },
    computed: {
        // 判断是否为微信浏览器
        isWeChatBrowser() {
            return /micromessenger/i.test(navigator.userAgent);
        }
    },
    mounted() {
        // document.title = '反馈详情';
        // 从 URL 查询参数获取 mark 和 code
        const mark = this.$route.query.mark;
        const code = this.$route.query.code;

  

        if (!mark || !code) {
            console.error('缺少必要参数: mark 或 code');
            console.error('mark 是否存在:', !!mark);
            console.error('code 是否存在:', !!code);
            ElMessage.error('链接参数不完整');
            return;
        }

        // 将 mark 存储为 token
        localStorage.setItem('X-Access-Token', mark);
        this.resolveShortLink(code);
    },
    methods: {
        // 解析短链接，获取 bizId
        async resolveShortLink(code) {
            try {
                this.loading = true;
                // 直接传递参数对象，不需要嵌套 params
                const response = await this.$http.get(`/admin-api/h5/short-link/resolve`, { code });

                if (response && response.data) {
                    const bizId = response.data.bizId;
                    if (bizId) {
                        // 使用 bizId 获取反馈详情
                        this.loadFeedbackDetail(bizId);
                    } else {
                        console.error('未获取到 bizId');
                        ElMessage.error('链接无效或已过期');
                    }
                } else {
                    console.error('短链接解析失败');
                    ElMessage.error('链接解析失败');
                }
            } catch (error) {
                console.error('解析短链接失败:', error);
                
                // 检查是否是 400 状态码（token过期）
                if (error.response && error.response.status === 401) {
                    console.log('Token过期，跳转到登录页面');
                    // 清除token
                    localStorage.removeItem('X-Access-Token');
                    // 跳转到listLogin
                    this.$router.push({ name: 'ListLogin' });
                    ElMessage.error('登录已过期，请重新登录');
                    return;
                }
                
                // 检查响应数据中的 code 是否为 400
                if (error.response && error.response.data && error.response.data.code === 400) {
                    console.log('Token过期（响应code=400），跳转到登录页面');
                    // 清除token
                    localStorage.removeItem('X-Access-Token');
                    // 跳转到listLogin
                    this.$router.push({ name: 'ListLogin' });
                    ElMessage.error('登录已过期，请重新登录');
                    return;
                }
                
                const errorMsg = error.response?.data?.msg || error.message || '链接解析失败';
                ElMessage.error(errorMsg);
            } finally {
                this.loading = false;
            }
        },

        // 加载反馈详情
        async loadFeedbackDetail(bizId) {
            try {
                this.loading = true;
                // 获取反馈详情
                const detailResponse = await this.$http.get(`/app-api/problem/feedback/${bizId}`);
                console.log('反馈详情响应:', detailResponse);

                if (detailResponse && detailResponse.data) {
                    this.detailData = detailResponse.data;
                    // 获取问题进展（时间线）
                    this.getProblemProgress(bizId);
                    
                    // 数据加载后初始化地图
                    this.$nextTick(() => {
                        this.initProblemMap();
                    });
                } else {
                    console.error('获取反馈详情失败');
                    ElMessage.error('获取反馈详情失败');
                }
            } catch (error) {
                console.error('获取反馈详情失败:', error);
                
                // 检查是否是 400 状态码（token过期）
                if (error.response && error.response.status === 400) {
                    console.log('Token过期，跳转到登录页面');
                    // 清除token
                    localStorage.removeItem('X-Access-Token');
                    // 跳转到listLogin
                    this.$router.push({ name: 'ListLogin' });
                    ElMessage.error('登录已过期，请重新登录');
                    return;
                }
                
                // 检查响应数据中的 code 是否为 400
                if (error.response && error.response.data && error.response.data.code === 400) {
                    console.log('Token过期（响应code=400），跳转到登录页面');
                    // 清除token
                    localStorage.removeItem('X-Access-Token');
                    // 跳转到listLogin
                    this.$router.push({ name: 'ListLogin' });
                    ElMessage.error('登录已过期，请重新登录');
                    return;
                }
                
                const errorMsg = error.response?.data?.msg || error.message || '获取反馈详情失败';
                ElMessage.error(errorMsg);
            } finally {
                this.loading = false;
            }
        },

        // 查询问题进展（时间线）
        getProblemProgress(bizId) {
            this.$http.get(`/app-api/problem/feedback/${bizId}`).then(res => {
                console.log('问题进展', res.data);
                if (res.data && res.data.statusTasks && Array.isArray(res.data.statusTasks)) {
                    this.timelineData = this.formatStatusTasksToTimeline(res.data.statusTasks);
                } else {
                    this.timelineData = [];
                }
            }).catch(error => {
                console.error('获取问题进展失败:', error);
                
                // 检查是否是 400 状态码（token过期）
                if (error.response && error.response.status === 400) {
                    console.log('Token过期，跳转到登录页面');
                    // 清除token
                    localStorage.removeItem('X-Access-Token');
                    // 跳转到listLogin
                    this.$router.push({ name: 'ListLogin' });
                    ElMessage.error('登录已过期，请重新登录');
                    return;
                }
                
                // 检查响应数据中的 code 是否为 400
                if (error.response && error.response.data && error.response.data.code === 400) {
                    console.log('Token过期（响应code=400），跳转到登录页面');
                    // 清除token
                    localStorage.removeItem('X-Access-Token');
                    // 跳转到listLogin
                    this.$router.push({ name: 'ListLogin' });
                    ElMessage.error('登录已过期，请重新登录');
                    return;
                }
                
                this.timelineData = [];
            });
        },

        // 将statusTasks转换为时间线数据格式
        // 只展示「已反馈」「已受理」「已办结/已驳回」三个节点（待核验 status=3 与已受理同节点，展示受理时间）
        formatStatusTasksToTimeline(statusTasks) {
            if (!statusTasks || !Array.isArray(statusTasks)) {
                return [];
            }

            // 将时间转换为时间戳的辅助函数
                    const toTimestamp = (time) => {
                        if (!time) return null;
                        if (typeof time === 'number') return time;
                        const timestamp = new Date(time).getTime();
                        return isNaN(timestamp) ? null : timestamp;
                    };

            // 找到最新的任务（通常是已办结或已驳回状态的任务）
            const latestTask = statusTasks.find(task => task.status === 4 || task.status === 1) || 
                              statusTasks[statusTasks.length - 1];

            const result = [];

            // 第一个节点：已反馈（始终存在）
            const feedbackTime = toTimestamp(this.detailData?.createTime) || Date.now();
            result.push({
                status: '已反馈',
                time: feedbackTime,
                assignee: '',
                description: '',
                uploadedFiles: []
            });

            // 如果没有任务数据，只返回已反馈节点
            if (!latestTask) {
                return result;
            }

            // 第二个节点：已受理（含待核验：展示「已受理」+ 受理时间）
            if (latestTask.status === 4 || latestTask.status === 1 || latestTask.status === 2 || latestTask.status === 3) {
                let processingTime;
                if (latestTask.status === 4) {
                    processingTime = toTimestamp(latestTask.processingTime) || 
                                   toTimestamp(latestTask.statusDescriptionTime) || 
                                   toTimestamp(latestTask.createTime) || 
                                          Date.now();
                } else if (latestTask.status === 1) {
                    processingTime = toTimestamp(latestTask.reviewerPersonTime) || 
                                   toTimestamp(latestTask.statusDescriptionTime) || 
                                   toTimestamp(latestTask.createTime) || 
                                   Date.now();
                } else if (latestTask.status === 3) {
                    const timeSource = statusTasks.find(t => t.status === 2) || latestTask;
                    processingTime = toTimestamp(timeSource.processingTime) || 
                                   toTimestamp(timeSource.statusDescriptionTime) || 
                                   toTimestamp(timeSource.createTime) || 
                                   Date.now();
                } else {
                    processingTime = toTimestamp(latestTask.processingTime) || 
                                   toTimestamp(latestTask.statusDescriptionTime) || 
                                   toTimestamp(latestTask.createTime) || 
                                   Date.now();
                }

                const acceptedDetailSource = latestTask.status === 3
                    ? (statusTasks.find(t => t.status === 2) || latestTask)
                    : latestTask;

                result.push({
                        status: '已受理',
                        time: processingTime,
                        assignee: '',
                    description: acceptedDetailSource.statusDescription || '',
                    uploadedFiles: acceptedDetailSource.problemReviewImages || []
                });
            }

            // 第三个节点：已办结或已驳回（根据最终状态）
            if (latestTask.status === 4) {
                // 已办结
                const verificationTime = toTimestamp(latestTask.verificationTime) || 
                                       toTimestamp(latestTask.completionTime) || 
                                            Date.now();

                result.push({
                        status: '已办结',
                        time: verificationTime,
                        assignee: '',
                    description: latestTask.verificationResult || '',
                    uploadedFiles: latestTask.problemHandleImages || []
                });
            } else if (latestTask.status === 1) {
                // 已驳回
                const verificationTime = toTimestamp(latestTask.verificationTime) || 
                                       toTimestamp(latestTask.completionTime) || 
                                       Date.now();

                result.push({
                    status: '已驳回',
                    time: verificationTime,
                        assignee: '',
                    description: latestTask.verificationResult || '',
                    uploadedFiles: latestTask.verifyHandleImages || []
                });
            }

            // 按时间排序
            return result.sort((a, b) => a.time - b.time);
        },

        // 获取任务时间（时间戳）
        getTaskTime(task) {
            // 将时间转换为时间戳的辅助函数
            const toTimestamp = (time) => {
                if (!time) return null;
                // 如果是数字，直接返回
                if (typeof time === 'number') {
                    return time;
                }
                // 如果是字符串，尝试转换为时间戳
                const timestamp = new Date(time).getTime();
                // 检查转换是否有效
                return isNaN(timestamp) ? null : timestamp;
            };

            // 如果是已受理状态，优先使用 processingTime
            if (task.status === 2) {
                return toTimestamp(task.processingTime) ||
                       toTimestamp(task.statusDescriptionTime) ||
                       toTimestamp(task.createTime) ||
                       Date.now();
            }

            // 其他状态：优先使用 statusDescriptionTime，其次 processingTime，最后 completionTime 或 createTime
            return toTimestamp(task.statusDescriptionTime) ||
                   toTimestamp(task.processingTime) ||
                   toTimestamp(task.completionTime) ||
                   toTimestamp(task.createTime) ||
                   Date.now();
        },

        // 根据状态码获取状态名称
        getStatusLabelByCode(status) {
            const statusMap = {
                0: '待审核',
                1: '已驳回',
                2: '已受理',
                3: '已受理',
                4: '已办结'
            };
            return statusMap[status] || '未知状态';
        },

        // 获取任务描述内容
        getTaskDescription(task) {
            // 如果是已驳回状态，直接使用 statusDescription
            if (task.status === 1) {
                return task.statusDescription || '';
            }

            // 如果是已受理状态，直接使用 statusDescription
            if (task.status === 2) {
                return task.statusDescription || '';
            }

            if (task.status === 3) {
                return task.statusDescription || '';
            }

            const descriptions = [];

            // 根据状态和字段优先级组合描述
            if (task.statusDescription) {
                descriptions.push(task.statusDescription);
            }

            // 如果是已办结状态，添加处理结果描述
            if (task.status === 4 && task.resolutionDescription) {
                if (descriptions.length > 0) {
                    descriptions.push('\n处理结果：' + task.resolutionDescription);
                } else {
                    descriptions.push(task.resolutionDescription);
                }
            }

            // 如果是已办结状态，添加核验结果
            if (task.status === 4 && task.verificationResult) {
                if (descriptions.length > 0) {
                    descriptions.push('\n核验结果：' + task.verificationResult);
                } else {
                    descriptions.push('核验结果：' + task.verificationResult);
                }
            }

            return descriptions.join('');
        },

        // 关闭页面
        handleClose() {
            this.$router.go(-1);
        },

        // 前往反馈列表
        goToFeedBackList() {
            this.$router.push({ 
                path: '/user_feedBack/feedBackList',
                query: { from: 'shortUrlDetail' }
            });
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
                // 预览图片
                window.open(fileUrl, '_blank');
            } else {
                // 预览视频
                window.open(fileUrl, '_blank');
            }
        },

        // 处理图片加载错误
        handleImageError(event) {
            // 图片加载失败时，可以显示占位图或隐藏图片
            const img = event.target;
            img.style.display = 'none';
        },

        // 处理图片加载完成
        handleImageLoad(event) {
            // 图片加载成功，可以在这里做一些处理
        },

        // 格式化时间线时间
        formatTimelineTime(timestamp) {
            if (!timestamp) return '--';
            // 使用单个 M 和 d 确保月份和日期不带前导零
            return this.$tool.dateFormat(timestamp, 'yyyy.M.d hh:mm:ss');
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

        // 格式化标题文本：包含河道/河段/水库名称
        formatTitleText(item) {
            if (!item) return '--';
            
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
            
            return title || '--';
        },

        // 获取反馈类型色块颜色（与 adminFeedBack.vue 保持一致）
        getFeedbackTypeColor(feedbackTypeLabel) {
            if (!feedbackTypeLabel) return '#2F88DB';

            const typeMap = {
                '漂浮物': '#369EFF',
                '设施损坏': '#3CB298',
                '设备损坏': '#3CB298'
            };

            return typeMap[feedbackTypeLabel] || '#2F88DB'; // 其他类型默认为 #2F88DB
        },

        // 获取显示状态（处理中 -> 已受理）
        getDisplayStatus(status) {
            if (status === '处理中' || status === '待核验') {
                return '已受理';
            }
            return status;
        },

        // 初始化问题点位地图
        initProblemMap() {
            const mapContainer = Array.isArray(this.$refs.problemLocationMap) 
                ? this.$refs.problemLocationMap[0] 
                : this.$refs.problemLocationMap;

            if (!mapContainer) {
                console.warn('地图容器不存在，等待容器渲染');
                setTimeout(() => {
                    this.initProblemMap();
                }, 300);
                return;
            }

            if (!this.detailData) {
                console.warn('详情数据为空，无法初始化地图');
                return;
            }

            const longitude = this.detailData.problemLongitude;
            const latitude = this.detailData.problemLatitude;
            const issueSpecificLocation = this.detailData.issueSpecificLocation || '问题位置';

            if (!longitude || !latitude) {
                console.warn('问题位置缺少经纬度信息', { longitude, latitude });
                return;
            }

            // 如果地图已存在，先销毁
            if (this.problemMap) {
                this.destroyMap();
            }

            // 确保容器可见且有尺寸
            if (mapContainer.offsetWidth === 0 || mapContainer.offsetHeight === 0) {
                console.warn('地图容器尺寸为0，等待容器渲染');
                setTimeout(() => {
                    this.initProblemMap();
                }, 300);
                return;
            }

            // 坐标转换：接口返回的坐标可能是 GCJ-02，天地图使用 WGS84
            const [wgsLng, wgsLat] = this.transformGCJ02ToWGS84(
                parseFloat(longitude),
                parseFloat(latitude)
            );
            
            // 初始化地图 - Leaflet 使用 [纬度, 经度] 顺序
            // 向上偏移地图中心点，让标记点稍微偏下，显示更多上方区域
            const offsetLat = 0.002; // 大约向上移动 200 米
            const center = [wgsLat + offsetLat, wgsLng];

            // 创建地图实例（只读模式，不允许用户操作）
            this.problemMap = L.map(mapContainer, {
                attributionControl: false,
                zoomControl: false,
                preferCanvas: true,
                tap: false,
                bounceAtZoomLimits: false,
                // 禁用所有交互功能，地图仅用于展示
                dragging: false,
                touchZoom: false,
                doubleClickZoom: false,
                scrollWheelZoom: false,
                boxZoom: false,
                keyboard: false
            });

            // 设置地图视图和中心点
            this.problemMap.setView(center, 14);

            // 使用天地图矢量地图服务
            this.initTiandituVectorMap();

            // 确保地图正确显示
            this.$nextTick(() => {
                setTimeout(() => {
                    if (this.problemMap) {
                        this.problemMap.invalidateSize();
                        // 再次设置视图以确保地图正确显示（使用偏移后的中心点）
                        this.problemMap.setView(center, 14);
                        // 在地图完全初始化后再添加标记（标记位置使用原始坐标，不偏移）
                        const markerPosition = [wgsLat, wgsLng];
                        this.addProblemMarker(markerPosition, issueSpecificLocation);
                    }
                }, 500);
            });
        },

        // 初始化天地图矢量地图服务
        initTiandituVectorMap() {
            if (!this.problemMap) {
                console.error('地图实例不存在，无法初始化天地图');
                return;
            }

            // 创建天地图矢量底图图层
            this.tiandituVecLayer = this.createTiandituLayer(L, {
                layer: 'vec',
                attribution: '© 国家基础地理信息中心',
                maxZoom: 18,
                tileSize: 256
            });
            
            if (this.tiandituVecLayer) {
                this.tiandituVecLayer.addTo(this.problemMap);
            }

            // 创建天地图矢量标注图层
            this.tiandituCvaLayer = this.createTiandituLayer(L, {
                layer: 'cva',
                attribution: '© 国家基础地理信息中心',
                maxZoom: 18,
                tileSize: 256
            });
            
            if (this.tiandituCvaLayer) {
                this.tiandituCvaLayer.addTo(this.problemMap);
            }
        },

        // 添加问题点位标记
        addProblemMarker(latlng, issueSpecificLocation) {
            // 如果已存在标记，先移除
            if (this.problemMarker) {
                if (this.problemMap) {
                    this.problemMap.removeLayer(this.problemMarker);
                }
            }

            // 创建自定义 HTML 图标
            const customDivIcon = L.divIcon({
                className: 'custom-problem-marker-container',
                html: `
                    <div class="problem-marker-wrapper">
                        <div class="problem-marker-top-block">
                            <div class="problem-marker-content-wrapper">
                                <div class="problem-marker-location">${issueSpecificLocation}</div>
                            </div>
                        </div>
                        <div class="problem-marker-bottom-block">
                            <img src="${position3Icon}" alt="图标" class="problem-marker-icon" />
                        </div>
                    </div>
                `,
                iconSize: [100, 110],
                iconAnchor: [50, 55],
            });

            // 添加标记到地图
            this.problemMarker = L.marker(latlng, { icon: customDivIcon }).addTo(this.problemMap);
        },

        // GCJ-02 转 WGS84 坐标转换函数
        transformGCJ02ToWGS84(lng, lat) {
            let dLat = 0;
            let dLng = 0;
            let outLat = lat;
            let outLng = lng;
            
            for (let i = 0; i < 10; i++) {
                const [gcjLng, gcjLat] = this.transformWGS84ToGCJ02(outLng, outLat);
                dLat = lat - gcjLat;
                dLng = lng - gcjLng;
                outLat += dLat;
                outLng += dLng;
                
                if (Math.abs(dLat) < 1e-6 && Math.abs(dLng) < 1e-6) {
                    break;
                }
            }
            
            return [outLng, outLat];
        },
        
        // WGS84 转 GCJ-02 坐标转换函数
        transformWGS84ToGCJ02(lng, lat) {
            const a = 6378245.0;
            const ee = 0.00669342162296594323;

            let dLat = this.transformLat(lng - 105.0, lat - 35.0);
            let dLng = this.transformLng(lng - 105.0, lat - 35.0);
            const radLat = lat / 180.0 * Math.PI;
            let magic = Math.sin(radLat);
            magic = 1 - ee * magic * magic;
            const sqrtMagic = Math.sqrt(magic);
            dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
            dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
            const mgLat = lat + dLat;
            const mgLng = lng + dLng;
            return [mgLng, mgLat];
        },

        transformLat(lng, lat) {
            let ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
            ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0;
            ret += (160.0 * Math.sin(lat / 12.0 * Math.PI) + 320 * Math.sin(lat * Math.PI / 30.0)) * 2.0 / 3.0;
            return ret;
        },

        transformLng(lng, lat) {
            let ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
            ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(lng * Math.PI) + 40.0 * Math.sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0;
            ret += (150.0 * Math.sin(lng / 12.0 * Math.PI) + 300.0 * Math.sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0;
            return ret;
        },

        // 销毁地图
        destroyMap() {
            if (this.problemMarker) {
                if (this.problemMap) {
                    this.problemMap.removeLayer(this.problemMarker);
                }
                this.problemMarker = null;
            }

            if (this.tiandituCvaLayer && this.problemMap) {
                this.problemMap.removeLayer(this.tiandituCvaLayer);
                this.tiandituCvaLayer = null;
            }

            if (this.tiandituVecLayer && this.problemMap) {
                this.problemMap.removeLayer(this.tiandituVecLayer);
                this.tiandituVecLayer = null;
            }

            if (this.problemMap) {
                this.problemMap.remove();
                this.problemMap = null;
            }
        }
    },
    beforeUnmount() {
        // 组件销毁前清理地图
        this.destroyMap();
    }
}
</script>

<style lang="scss" scoped>
.feedBack-detail-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    height: 100dvh; // 使用动态视口高度，适配移动端浏览器UI
    overflow-y: auto;
    overflow-x: hidden;
    -webkit-overflow-scrolling: touch;
    background-color: #f5f5f5;

    .content-layer {
        width: 100%;
        min-height: 100vh;
        min-height: 100dvh; // 使用动态视口高度
        padding: 16px 24px 40px 24px;
        padding-bottom: calc(160px + env(safe-area-inset-bottom, 0px)); // 底部按钮高度(88px) + padding(24px*2) + 安全区域
        box-sizing: border-box;

        // &.has-header {
        //     padding-top: 116px; // 100px (PageHeader高度) + 16px (原有padding-top)
        // }

        .paddingContent {
            padding: 0;
            display: flex;
            flex-direction: column;
            align-items: center;

            .loading-container {
                width: 702px;
                padding: 60px 24px;
                text-align: center;
                margin-top: 16px;

                .loading-text {
                    font-size: 28px;
                    color: #999999;
                }
            }

            .feedBackDetailCard {
                width: 702px;
                max-width: 100%;
                // background-color: #ffffff;
                // padding: 0px 24px 24px 24px;
                margin-top: 16px;
                box-sizing: border-box;
                overflow: hidden;

                // 问题描述模块（参考 feedBackDetail.vue section-2）
                .section-2 {
                    height: 205px;
                    opacity: 1;
                    background: #ffffff;
                    margin-bottom: 16px;
                    padding: 0px 24px 16px 16px;
                    box-sizing: border-box;
                    

                    .section-2-content {
                        width: 100%;
                        height: 100%;
                        max-width: 100%;
                    display: flex;
                    flex-direction: column;
                        box-sizing: border-box;
                        overflow: hidden;

                        .section-2-title {
                            height: 81px;
                        display: flex;
                            align-items: center;
                            gap: 4px 12px 12px 4px;
                            border-bottom: 1px solid #E6E6E6;
                            width: 100%;
                            max-width: 100%;
                            box-sizing: border-box;
                            overflow: hidden;

                            .feedback-type-badge {
                                padding: 4px 6px;
                                height: 32px;
                                color: #ffffff;
                                margin-right: 12px;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                font-size: 24px;
                            font-weight: 500;
                                border-radius: 4px;
                            flex-shrink: 0;
                                white-space: nowrap;
                                min-width: fit-content;
                        }

                            .title-text {
                            flex: 1;
                                color: #3d3d3d;
                                font-size: 32px;
                                font-weight: 500;
                                line-height: 1.4;
                                overflow: hidden;
                                text-overflow: ellipsis;
                                display: -webkit-box;
                                -webkit-line-clamp: 2;
                                line-clamp: 2;
                                -webkit-box-orient: vertical;
                                word-wrap: break-word;
                            word-break: break-all;
                                min-width: 0;
                                max-width: 100%;
                            }
                        }

                        .section-2-content-area {
                            flex: 1;
                            margin-top: 16px;
                            background: #f8f8f8;
                            padding: 8px 16px 0px 16px;
                            border-radius: 8px;
                            color: #3d3d3d;
                            font-size: 28px;
                            font-weight: 400;
                            line-height: 1.6;
                            overflow-y: auto;
                            overflow-x: hidden;
                            word-break: break-all;
                            word-wrap: break-word;
                            overflow-wrap: break-word;
                            width: 100%;
                            max-width: 100%;
                            box-sizing: border-box;
                            min-width: 0;
                        }
                    }
                }

                // 反馈文件模块（参考 feedBackDetail.vue section-3）
                .section-3 {
                    height: 209px;
                    opacity: 1;
                    background: #ffffff;
                    margin-bottom: 16px;
                    padding: 0px 24px 16px 16px;
                    box-sizing: border-box;

                    .section-3-content {
                        width: 100%;
                        height: 100%;
                        display: flex;
                        flex-direction: column;

                        .section-3-title {
                            height: 81px;
                            display: flex;
                            align-items: center;
                            border-bottom: 1px solid #E6E6E6;

                            span {
                            color: #3d3d3d;
                                font-size: 32px;
                            font-weight: 500;
                                line-height: 1.4;
                            }
                        }

                        .section-3-content-area {
                            flex: 1;
                            margin-top: 16px;
                            overflow-y: auto;

                            .file-list {
                                display: flex;
                            flex-wrap: wrap;
                                gap: 20px;

                                .file-item {
                                width: 80px;
                                height: 80px;
                                background-color: #f5f5f5;
                                border-radius: 8px;
                                overflow: hidden;
                                cursor: pointer;
                                position: relative;
                                    flex-shrink: 0;

                                    .file-image {
                                    width: 100%;
                                    height: 100%;
                                    object-fit: cover;
                                }

                                    .file-video {
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

                                &:active {
                                    opacity: 0.8;
                                }
                                }
                            }

                            .no-files {
                                color: #999999;
                                font-size: 28px;
                                text-align: center;
                                padding: 20px 0;
                            }
                        }
                    }
                }

                // 问题点位模块（参考 feedBackDetail.vue section-4）
                .section-4 {
                    height: 317px;
                    opacity: 1;
                    background: #ffffff;
                    margin-bottom: 16px;
                    padding: 0px 24px 16px 16px;
                    box-sizing: border-box;

                    .section-4-content {
                        width: 100%;
                        height: 100%;
                        display: flex;
                        flex-direction: column;

                        .section-4-title {
                            height: 81px;
                            display: flex;
                            align-items: center;
                            border-bottom: 1px solid #E6E6E6;

                            span {
                                color: #3d3d3d;
                                font-size: 32px;
                                font-weight: 500;
                            }
                        }

                        .section-4-map-wrapper {
                            flex: 1;
                            margin-top: 16px;
                            height: 188px;
                            min-height: 188px;
                            border-radius: 8px;
                            overflow: hidden;
                            position: relative;
                            background: #f0f0f0;

                            .problem-location-map {
                                width: 100%;
                                height: 100%;
                                min-height: 188px;
                                display: block;
                            }
                        }
                    }
                }

                // 问题点位标记样式
                :deep(.custom-problem-marker-container) {
                    background: none !important;
                    border: none !important;

                    .problem-marker-wrapper {
                        display: flex;
                        flex-direction: column;
                        width: fit-content;
                        min-width: 200px;
                        border-radius: 4px;
                        overflow: visible;

                        .problem-marker-top-block {
                            width: fit-content;
                            min-width: 200px;
                            border: 1px solid #A5CFF8;
                            background: #ffffff80;
                            margin-bottom: 10px;
                            display: flex;
                            flex-direction: column;
                            padding: 8px 12px;
                            box-sizing: border-box;
                            position: relative;

                            .problem-marker-content-wrapper {
                                display: flex;
                                flex-direction: column;
                                width: 100%;
                                gap: 4px;
                            }

                            .problem-marker-location {
                                text-align: center;
                                font-size: 24px;
                                color: #3D3D3D;
                                font-weight: 500;
                                line-height: 1.2;
                                white-space: nowrap;
                                overflow: hidden;
                                text-overflow: ellipsis;
                            }
                        }

                        .problem-marker-bottom-block {
                            width: 100%;
                            height: 80px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            position: relative;

                            .problem-marker-icon {
                                width: 80px;
                                height: 80px;
                                object-fit: contain;
                            }
                        }
                    }
                }

                // 时间线容器
                .timeline-container {
                    width: 100%;
                    padding: 0px 16px 0px 16px;
                    border-top: 1px solid rgba(238, 238, 238, 0.5);
                    background: #ffffff;

                    .timeline-title {
                        height: 81px;
                        display: flex;
                        align-items: center;
                        border-bottom: 1px solid #E6E6E6;
                        margin-bottom: 0;

                        span {
                            color: #3d3d3d;
                        font-size: 32px;
                        font-weight: 500;
                        }
                    }

                    .timeline-list {
                        position: relative;
                        padding: 18px;
                        background: #ffffff;

                        .timeline-item {
                            display: flex;
                            align-items: flex-start;
                            margin-bottom: 32px;
                            position: relative;

                            &:last-child {
                                margin-bottom: 0;
                            }

                            .timeline-line {
                                position: absolute;
                                top: 16px; // 节点中心位置：4px (padding-top) + 12px (节点高度的一半)
                                left: 16px; // wrapper宽度的一半 (32px / 2) = 16px
                                transform: translateX(-50%);
                                width: 2px;
                                // 从当前节点中心延伸到下一个节点的中心
                                // item高度 + margin-bottom(32px) = 从当前节点中心到下一个节点中心的距离
                                height: calc(100% + 32px);
                                background-color: #E0F2FF;
                                z-index: 1;
                            }

                            .timeline-line-wrapper {
                                position: relative;
                                width: 32px;
                                margin-right: 20px;
                                display: flex;
                                flex-direction: column;
                                align-items: center;
                                flex-shrink: 0;
                                padding-top: 4px; // 微调对齐，确保节点和文字在同一高度
                                z-index: 2;

                                .timeline-dot {
                                    width: 24px;
                                    height: 24px;
                                    border-radius: 50%;
                                    background: #349dff4d;
                                    position: relative;
                                    z-index: 2;
                                    display: flex;
                                    align-items: center;
                                    justify-content: center;

                                    &::after {
                                        content: '';
                                        width: 10px;
                                        height: 10px;
                                        border-radius: 50%;
                                        background-color: #349DFF;
                                    }
                                }
                            }

                            .timeline-content {
                                flex: 1;
                                padding-bottom: 0;

                                .timeline-header {
                                    display: flex;
                                    align-items: center;
                                    margin-bottom: 8px;
                                    width: 100%;
                                    line-height: 28px; // 与字体大小一致，确保对齐

                                    .timeline-status {
                                        font-size: 28px;
                                        font-weight: 500;
                                        color: #3d3d3d;
                                        margin-right: 16px;
                                        flex-shrink: 0;
                                        line-height: 28px;
                                    }

                                    .timeline-time {
                                        font-size: 24px;
                                        font-weight: 400;
                                        color: #999999;
                                        margin-right: 16px;
                                        flex-shrink: 0;
                                    }

                                    .timeline-assignee {
                                        font-size: 28px;
                                        font-weight: 400;
                                        color: #3d3d3d;
                                        margin-left: auto;
                                        flex-shrink: 0;
                                    }
                                }

                                .timeline-files {
                                    margin-top: 8px;
                                    margin-bottom: 8px;

                                    .fileList {
                                        display: flex;
                                        gap: 14px;
                                        flex-wrap: wrap;
                                        margin-top: 12px;

                                        .fileItem {
                                            width: 70px;
                                            height: 70px;
                                            background-color: #f5f5f5;
                                            // border-radius: 8px;
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

                                            &:active {
                                                opacity: 0.8;
                                            }
                                        }
                                    }
                                }

                                .timeline-description {
                                    font-size: 28px;
                                    font-weight: 400;
                                    color: #666666;
                                    line-height: 1.6;
                                    word-wrap: break-word;
                                    word-break: break-all;
                                    white-space: pre-wrap;
                                    margin-top: 8px;
                                    width: 100%;
                                    box-sizing: border-box;
                                    overflow-wrap: break-word;
                                    
                                    &.has-background {
                                        background-color: #f8f8f8;
                                        padding: 16px;
                                        border-radius: 8px;
                                        margin-top: 12px;
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }

    // 底部按钮栏
    .bottom-action-bar {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        width: 100%;
        padding: 24px 24px 40px 24px;
        padding-bottom: calc(24px + env(safe-area-inset-bottom, 0px));
        box-sizing: border-box;
        display: flex;
        justify-content: center;
        align-items: center;
        background: #ffffff;
        z-index: 1000;
        box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);

        .go-to-list-btn {
            width: 100%;
            max-width: 600px;
            height: 88px;
            background: #349DFF;
            color: #ffffff;
            font-size: 32px;
            font-weight: 500;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;

            .back-arrow-icon {
                width: 32px;
                height: 32px;
                object-fit: contain;
            }

            span {
                line-height: 1;
            }

            &:active {
                opacity: 0.8;
                transform: scale(0.98);
            }

            &:hover {
                background: #2a8ce6;
            }
        }
    }
}
</style>
