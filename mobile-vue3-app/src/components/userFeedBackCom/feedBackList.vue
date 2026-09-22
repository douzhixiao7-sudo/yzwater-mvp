<!-- 反馈列表页面 -->
<template>
    <div class="feedBack-list-container">
        <div class="content-layer">
            <!-- 页面标题组件 -->
            <!-- <PageHeader title="反馈列表" @close="handleClose" /> -->
            
            <div class="paddingContent">
                <!-- 日期范围筛选 -->
                <el-config-provider :locale="zhCn">
                    <div class="filter-section">
                        <div class="date-picker-wrapper">
                            <el-date-picker
                                v-model="startDate"
                                type="date"
                                placeholder="开始日期"
                                format="YYYY-MM-DD"
                                value-format="YYYY-MM-DD"
                                @change="handleDateChange"
                                class="date-picker"
                            />
                            <span class="date-separator">-</span>
                            <el-date-picker
                                v-model="endDate"
                                type="date"
                                placeholder="结束日期"
                                format="YYYY-MM-DD"
                                value-format="YYYY-MM-DD"
                                @change="handleDateChange"
                                class="date-picker"
                            />
                        </div>
                    </div>
                </el-config-provider>

              <!-- 状态筛选 -->
              <div class="status-filter-section">
                  <div 
                      class="status-tab" 
                      v-for="(item, index) in statusTabs" 
                      :key="item.value"
                      :class="{ 'active': currentStatus === item.value }"
                      @click="handleStatusChange(item.value)"
                  >
                      <div class="status-label">{{ item.label }}</div>
                      <div class="status-count" :style="{ color: item.color }">{{ item.count }}</div>
                      <div class="divider" v-if="index < statusTabs.length - 1"></div>
                  </div>
              </div>

                <!-- 反馈列表 -->
                <div class="feedBackList" ref="feedBackListContainer">
                    <div class="feedBackItem" v-for="item in feedBackList" :key="item.id" @click="goToDetail(item)">
                        <div class="itemTitle">
                            <span class="feedback-type-badge" v-if="item.feedbackTypeLabel" :style="{ background: getFeedbackTypeColor(item.feedbackTypeLabel) }">{{ item.feedbackTypeLabel }}</span>
                            <!-- <span class="type-badge" v-if="item.referenceType" :class="{ 'reservoir-badge': item.referenceType === 'reservoir' }">{{ getReferenceTypeBadgeLabel(item.referenceType) }}</span> -->
                            <span class="title-text">{{ getTitleText(item) }}</span>
                            <span class="status-label" v-if="item.status === 3 || item.statusLabel" :style="{ background: getStatusBackgroundColor(item.status) }">
                                <span class="status-text" :style="{ color: getStatusColor(item.status) }">{{ getStatusLabelText(item) }}</span>
                            </span>
                        </div>

                        <div class="feedBackBox">
                            <!-- 文件数量和时间 -->
                            <div class="feedBackItem-row file-time-row">
                                <div class="file-count-wrapper" v-if="item.uploadedFiles && item.uploadedFiles.length > 0">
                                    <span class="file-count">{{ item.uploadedFiles.length }}</span>
                                </div>
                                <span class="time-text">{{ formatTimeShort(item.createTime) }}</span>
                            </div>
                            <!-- 问题定位 -->
                            <div class="feedBackItem-row location-row">
                                <img src="@/assets/img/position2.png" alt="问题定位" class="location-icon">
                                <span class="location-text">{{ item.issueSpecificLocation || item.specificLocation || '--' }}</span>
                            </div>
                        </div>
                    </div>
                    
                    <!-- 加载更多提示 -->
                    <div v-if="loadingMore" class="loading-more">
                        <div class="loading-text">加载中...</div>
                    </div>
                    
                    <!-- 没有更多数据提示 -->
                    <div v-if="!pagination.hasMore && feedBackList.length > 0" class="empty-state">
                        <div class="empty-text">——— 已全部展示 ———</div>
                    </div>
                    
                    <!-- 空状态提示 -->
                    <div v-if="feedBackList.length === 0 && !loading" class="empty-state">
                        <div class="empty-text">暂无数据</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 悬浮反馈按钮 -->
        <div class="floating-feedback-btn" v-if="!isFromShortUrlDetail" @click="goToFeedBack">
            <!-- <span class="btn-text"></span> -->
        </div>

        <!-- 底部菜单栏 -->
        <BottomMenu 
            v-if="currentRoute === 'FeedBackList' || currentRoute === 'MyPage'"
            :show-home="false"
            :show-feed-back="true"
            :show-mine="true"
        />
    </div>
</template>

<script>
import PageHeader from '@/components/commonCom/PageHeader.vue';
import { ElDatePicker, ElConfigProvider, ElMessage, ElLoading } from 'element-plus';
import zhCn from 'element-plus/dist/locale/zh-cn.js';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import BottomMenu from '@/components/commonCom/BottomMenu.vue';
import isOverTimeImg from '@/assets/img/isOverTimeImg.png';

// 设置 Day.js 为中文
dayjs.locale('zh-cn');

export default {
    name: 'FeedBackList',
    components: {
        PageHeader,
        ElDatePicker,
        ElConfigProvider,
        BottomMenu
    },
    data() {
        return {
            // Element Plus 中文语言包
            zhCn,
            // 超时图片
            isOverTimeImg,
            // 开始日期
            startDate: null,
            // 结束日期
            endDate: null,
            // 当前选中的状态
            currentStatus: 'all',
            // 状态标签列表
            statusTabs: [
                { label: '全部', value: 'all', count: 0, color: '#3D3D3D', statusValue: null, background: '#daedff' },
                { label: '待受理', value: 'pending', count: 0, color: '#FF5100', statusValue: 0, background: '#ffe7dc' }, // 待审核
                { label: '已受理', value: 'accepted', count: 0, color: '#349DFF', statusValue: 2, background: '#daedff' }, // 已受理
                { label: '已办结', value: 'processed', count: 0, color: '#3CB298', statusValue: 4, background: '#ddf1ed' }, // 已办结
                { label: '已驳回', value: 'rejected', count: 0, color: '#3D3D3D', statusValue: 1, background: '#daedff' } // 已驳回
            ],
            // 反馈列表（显示的数据）
            feedBackList: [],
            // 全部数据（用于统计和过滤）
            allFeedBackList: [],
            // 分页参数
            pagination: {
                pageNo: 1,
                pageSize: 5,  // 首次加载5条数据
                total: 0,     // 总数据量
                hasMore: true // 是否还有更多数据
            },
            // 加载状态
            loading: false,
            // 加载更多状态
            loadingMore: false,
            // 滚动容器引用
            scrollContainer: null
        };
    },
    computed: {
        // 当前路由名称
        currentRoute() {
            return this.$route.name;
        },
        // 判断是否从 shortUrlDetail 页面跳转而来
        isFromShortUrlDetail() {
            return this.$route.query.from === 'shortUrlDetail';
        }
    },
    mounted() {
        // document.title = '反馈列表';
        
        // 设置默认时间范围：2026-01-01 到当前日期
        this.startDate = '2026-01-01';
        this.endDate = dayjs().format('YYYY-MM-DD');
        
        // 在 nextTick 中初始化滚动容器，确保 DOM 已渲染
        this.$nextTick(() => {
            // 查找滚动容器（.paddingContent）
            this.scrollContainer = this.$el.querySelector('.paddingContent') || 
                                 document.querySelector('.paddingContent') ||
                                 this.$refs.feedBackListContainer?.parentElement;
            
            
            // 如果找不到容器，使用 window
            if (!this.scrollContainer) {
                this.scrollContainer = window;
            } 
            
            // 添加滚动监听
            this.addScrollListener();
            
            // 测试滚动事件是否正常工作
            setTimeout(() => {
                if (this.scrollContainer && this.scrollContainer !== window) {
                    // 尝试触发一次滚动事件
                    const event = new Event('scroll');
                    this.scrollContainer.dispatchEvent(event);
                } else {
                    // 测试 window 滚动
                    const event = new Event('scroll');
                    window.dispatchEvent(event);
                }
            }, 2000);
        });
        // 初始加载数据
        this.loadFeedBackData();
    },
    beforeUnmount() {
        // 移除滚动监听
        this.removeScrollListener();
    },
    methods: {
        // 关闭页面
        handleClose() {
            this.$router.go(-1);
        },

        // 日期变化处理
        handleDateChange() {
            // 验证日期选择：如果清空了任意一个日期，提示用户
            if (!this.startDate || !this.endDate) {
                const missingType = !this.startDate ? '开始' : '结束';
                ElMessage.warning(`请选择${missingType}时间`);
                return;
            }
            
            // 如果两个日期都选择了，验证结束日期不能早于开始日期
            if (this.startDate && this.endDate) {
                const start = new Date(this.startDate);
                const end = new Date(this.endDate);
                if (end < start) {
                    ElMessage.warning('结束日期不能早于开始日期');
                    // 清空结束日期，让用户重新选择
                    this.endDate = null;
                    return;
                }
            }
            
            // 重置分页参数
            this.pagination.pageNo = 1;
            this.pagination.hasMore = true;
            this.pagination.total = 0;
            // 清空已加载的数据
            this.allFeedBackList = [];
            this.feedBackList = [];
            // 重新加载数据（从第1页开始，每次3条）
            this.loadFeedBackData();
        },

        // 状态变化处理
        handleStatusChange(status) {
            this.currentStatus = status;
            // 状态切换时，重置分页并重新加载数据
            this.pagination.pageNo = 1;
            this.pagination.hasMore = true;
            this.pagination.total = 0;
            // 清空已加载的数据
            this.allFeedBackList = [];
            this.feedBackList = [];
            // 重新加载数据（从第1页开始，每次3条，传递对应的statusFilter参数）
            this.loadFeedBackData();
        },
        
        // 添加滚动监听
        addScrollListener() {
            if (this.scrollContainer) {
                this.scrollContainer.addEventListener('scroll', this.handleScroll);
            } else {
                // 如果找不到容器，使用 window
                window.addEventListener('scroll', this.handleScroll);
            }
        },
        
        // 移除滚动监听
        removeScrollListener() {
            if (this.scrollContainer) {
                this.scrollContainer.removeEventListener('scroll', this.handleScroll);
            } else {
                window.removeEventListener('scroll', this.handleScroll);
            }
        },
        
        // 处理滚动事件
        handleScroll() {
            
            // 如果正在加载或没有更多数据，不处理
            if (this.loadingMore) {
                return;
            }
            
            if (!this.pagination.hasMore) {
                return;
            }
            
            const container = this.scrollContainer || window;
            let scrollTop, scrollHeight, clientHeight;
            
            if (container === window) {
                scrollTop = window.pageYOffset || document.documentElement.scrollTop;
                scrollHeight = document.documentElement.scrollHeight;
                clientHeight = document.documentElement.clientHeight;
            } else {
                scrollTop = container.scrollTop;
                scrollHeight = container.scrollHeight;
                clientHeight = container.clientHeight;
            }
            
            const distanceToBottom = scrollHeight - (scrollTop + clientHeight);
            
         
            // 距离底部 100px 时开始加载
            if (scrollTop + clientHeight >= scrollHeight - 100) {
                this.loadMoreData();
            }
        },
        
        // 加载更多数据
        loadMoreData() {
            if (this.loadingMore) {
                return;
            }
            
            if (!this.pagination.hasMore) {
                return;
            }
            
            this.loadingMore = true;
            this.pagination.pageNo += 1;
            
            this.loadFeedBackData(true);
        },

        // 加载反馈数据（分页加载列表数据）
        loadFeedBackData(isLoadMore = false) {
            // 显示查询中的 loading 提示（只在非加载更多时显示）
            let loadingInstance = null;
            
            // 验证日期选择：如果选择了日期，必须同时选择开始和结束日期
            if ((this.startDate && !this.endDate) || (!this.startDate && this.endDate)) {
                ElMessage.warning('请同时选择开始日期和结束日期');
                this.loading = false;
                return;
            }
            
            // 如果两个日期都选择了，验证结束日期不能早于开始日期
            if (this.startDate && this.endDate) {
                const start = new Date(this.startDate);
                const end = new Date(this.endDate);
                if (end < start) {
                    ElMessage.warning('结束日期不能早于开始日期');
                    this.loading = false;
                    return;
                }
            }
            
            if (!isLoadMore) {
                this.loading = true;
                loadingInstance = ElLoading.service({
                    lock: true,
                    text: '查询中',
                    background: 'rgba(0, 0, 0, 0.7)'
                });
            }
            
            // 根据是否是首次加载决定 pageSize
            // 首次加载（pageNo === 1 且不是加载更多）使用 5 条，后续使用 3 条
            const pageSize = (!isLoadMore && this.pagination.pageNo === 1) ? 5 : 3;

            // 已受理：合并 status 2（处理中）与 3（待核验），不单独展示待核验 Tab
            if (this.currentStatus === 'accepted') {
                const common = this.buildMyPageCommonQuery(this.pagination.pageNo, pageSize);
                const url2 = `/app-api/problem/feedback/my-page?${common}&statusFilter=2`;
                const url3 = `/app-api/problem/feedback/my-page?${common}&statusFilter=3`;

                Promise.all([this.$http.get(url2), this.$http.get(url3)])
                    .then(([res2, res3]) => {
                        const list2 = res2.data?.list || [];
                        const list3 = res3.data?.list || [];
                        const total2 = res2.data?.total || 0;
                        const total3 = res3.data?.total || 0;
                        const batch = this.mergeFeedbackByCreateTime(list2, list3);

                        if (isLoadMore) {
                            this.allFeedBackList = this.mergeFeedbackByCreateTime(
                                this.allFeedBackList,
                                batch
                            );
                        } else {
                            this.allFeedBackList = batch;
                            this.loadStatisticsData();
                        }

                        const total = total2 + total3;
                        this.pagination.total = total;
                        const currentLoaded = this.allFeedBackList.length;
                        this.pagination.hasMore =
                            currentLoaded < total &&
                            (list2.length > 0 || list3.length > 0);
                        this.feedBackList = this.allFeedBackList;
                    })
                    .catch(() => {
                        if (!isLoadMore) {
                            this.allFeedBackList = [];
                            this.feedBackList = [];
                            this.resetStatusCounts();
                        }
                        this.pagination.hasMore = false;
                    })
                    .finally(() => {
                        this.loading = false;
                        this.loadingMore = false;
                        if (loadingInstance) {
                            loadingInstance.close();
                        }
                    });
                return;
            }

            // 手动构建查询字符串（非「已受理」）
            let queryParams = [];
            queryParams.push(`pageNo=${this.pagination.pageNo}`);
            queryParams.push(`pageSize=${pageSize}`);

            if (this.startDate && this.endDate) {
                queryParams.push(`createTime=${this.startDate},${this.endDate}`);
            }

            if (this.currentStatus && this.currentStatus !== 'all') {
                const statusTab = this.statusTabs.find(tab => tab.value === this.currentStatus);
                if (statusTab && statusTab.statusValue !== null) {
                    queryParams.push(`statusFilter=${statusTab.statusValue}`);
                }
            }

            const url = `/app-api/problem/feedback/my-page?${queryParams.join('&')}`;

            this.$http.get(url).then(res => {
                if (res.data) {
                    const newList = res.data.list || [];
                    const total = res.data.total || 0;

                    if (isLoadMore) {
                        this.allFeedBackList = [...this.allFeedBackList, ...newList];
                    } else {
                        this.allFeedBackList = newList;
                        this.loadStatisticsData();
                    }

                    this.pagination.total = total;
                    const currentLoaded = this.allFeedBackList.length;
                    this.pagination.hasMore = currentLoaded < total;
                    this.feedBackList = this.allFeedBackList;
                } else {
                    if (!isLoadMore) {
                        this.allFeedBackList = [];
                        this.feedBackList = [];
                        this.resetStatusCounts();
                    }
                    this.pagination.hasMore = false;
                }
            }).catch(error => {
                if (!isLoadMore) {
                    this.allFeedBackList = [];
                    this.feedBackList = [];
                    this.resetStatusCounts();
                }
                this.pagination.hasMore = false;
            }).finally(() => {
                this.loading = false;
                this.loadingMore = false;
                if (loadingInstance) {
                    loadingInstance.close();
                }
            });
        },

        // 列表请求公共参数（分页 + 可选日期）
        buildMyPageCommonQuery(pageNo, pageSize) {
            const queryParams = [];
            queryParams.push(`pageNo=${pageNo}`);
            queryParams.push(`pageSize=${pageSize}`);
            if (this.startDate && this.endDate) {
                queryParams.push(`createTime=${this.startDate},${this.endDate}`);
            }
            return queryParams.join('&');
        },

        // 合并两段列表：按 createTime 倒序，按 id 去重（后者覆盖前者）
        mergeFeedbackByCreateTime(a, b) {
            const map = new Map();
            [...(a || []), ...(b || [])].forEach(item => {
                if (item && item.id != null) {
                    map.set(item.id, item);
                }
            });
            return [...map.values()].sort((x, y) => {
                const tx = x.createTime || 0;
                const ty = y.createTime || 0;
                return ty - tx;
            });
        },

        // 前端过滤反馈列表
        filterFeedBackList() {
            let filteredList = [...this.allFeedBackList];

            if (this.currentStatus && this.currentStatus !== 'all') {
                const statusTab = this.statusTabs.find(tab => tab.value === this.currentStatus);
                if (statusTab && statusTab.statusValue !== null) {
                    if (statusTab.value === 'accepted') {
                        filteredList = filteredList.filter(
                            item => item.status === 2 || item.status === 3
                        );
                    } else {
                        filteredList = filteredList.filter(
                            item => item.status === statusTab.statusValue
                        );
                    }
                }
            }

            this.feedBackList = filteredList;
        },

        // 加载统计数据（循环调用接口获取每个状态的统计数据）
        loadStatisticsData() {
            const tabsToQuery = this.statusTabs;

            const promises = tabsToQuery.map(tab => {
                const base = this.buildMyPageCommonQuery(1, 10);

                if (tab.value === 'accepted') {
                    const url2 = `/app-api/problem/feedback/my-page?${base}&statusFilter=2`;
                    const url3 = `/app-api/problem/feedback/my-page?${base}&statusFilter=3`;
                    return Promise.all([this.$http.get(url2), this.$http.get(url3)])
                        .then(([r2, r3]) => ({
                            tab,
                            total: (r2.data?.total || 0) + (r3.data?.total || 0)
                        }))
                        .catch(() => ({ tab, total: 0 }));
                }

                let suffix = base;
                if (tab.statusValue !== null && tab.statusValue !== undefined) {
                    suffix = `${base}&statusFilter=${tab.statusValue}`;
                }
                const url = `/app-api/problem/feedback/my-page?${suffix}`;
                return this.$http.get(url)
                    .then(res => ({ tab, total: res.data?.total || 0 }))
                    .catch(() => ({ tab, total: 0 }));
            });

            Promise.all(promises).then(results => {
                results.forEach(({ tab, total }) => {
                    const statusTab = this.statusTabs.find(t => t.value === tab.value);
                    if (statusTab) {
                        statusTab.count = total;
                    }
                });
            });
        },

        // 更新状态按钮的数字
        updateStatusCounts(counts) {
            this.statusTabs.forEach(tab => {
                if (tab.value === 'all') {
                    tab.count = counts.all || 0;
                } else if (tab.value === 'pending') {
                    tab.count = counts.pending || 0;
                } else if (tab.value === 'accepted') {
                    tab.count = counts.accepted || 0;
                } else if (tab.value === 'processed') {
                    tab.count = counts.processed || 0;
                } else if (tab.value === 'rejected') {
                    tab.count = counts.rejected || 0;
                }
            });
        },

        // 重置状态统计数字
        resetStatusCounts() {
            this.statusTabs.forEach(tab => {
                tab.count = 0;
            });
        },

        // 格式化时间戳
        formatTime(timestamp) {
            if (!timestamp) return '--';
            return this.$tool.dateFormat(timestamp, 'yyyy-MM-dd hh:mm:ss');
        },

        // 格式化时间戳（简短格式：2025/12/9 12:56）
        formatTimeShort(timestamp) {
            if (!timestamp) return '--';
            return this.$tool.dateFormat(timestamp, 'yyyy/MM/dd hh:mm');
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
                window.open(this.resolveFileUrl(fileUrl), '_blank');
            } else {
                // 预览视频
                window.open(this.resolveFileUrl(fileUrl), '_blank');
            }
        },

        // 前往详情页面
        // 跳转到反馈页面
        goToFeedBack() {
            this.$router.push({ name: 'UserFeedBack' });
        },

        goToDetail(item) {
            // 使用 query 参数传递数据
            this.$router.push({
                name: 'FeedBackDetail',
                query: {
                    data: JSON.stringify(item)
                }
            });
        },

        // 根据状态值获取颜色
        getStatusColor(status) {
            // status: 0-待审核(待受理), 1-已驳回, 2-处理中(已受理), 3-待核验, 4-已办结(已办理)
            // 列表中待核验（3）与已受理（2）同一套样式
            const key = status === 3 ? 2 : status;
            const colorMap = {
                0: '#FF5100',  // 待受理
                1: '#999999',  // 已驳回
                2: '#349DFF',  // 已受理
                4: '#3CB298'   // 已办理
            };
            return colorMap[key] || '#666666';
        },

        // 根据状态值获取背景颜色
        getStatusBackgroundColor(status) {
            const key = status === 3 ? 2 : status;
            const colorMap = {
                0: '#FFE7DC', // 待受理
                1: '#E4E4E4', // 已驳回
                2: '#DAEDFF', // 处理中 / 待核验（列表归入已受理）
                4: '#DDF1ED'  // 已办结
            };
            return colorMap[key] || '#666666';
        },

        // 获取状态标签文本（将"处理中"转换为"已受理"）
        getStatusLabelText(item) {
            if (!item) {
                return '';
            }
            if (item.status === 3) {
                return '已受理';
            }
            if (!item.statusLabel) {
                return '';
            }
            if (item.status === 2 && item.statusLabel === '处理中') {
                return '已受理';
            }
            return item.statusLabel;
        },

        // 判断项目是否超时
        isItemOverdue(item) {
            // 只判断处理中（status === 2）和待核验（status === 3）阶段
            if (!item || (item.status !== 2 && item.status !== 3)) {
                return false;
            }

            // 优先使用 detailData.plannedCompletionTime（如果列表数据中有）
            if (item.plannedCompletionTime) {
                const plannedTime = item.plannedCompletionTime;
                const currentTime = Date.now();
                return currentTime > plannedTime;
            }

            // 如果没有 plannedCompletionTime，返回 false
            return false;
        },

        // 根据状态值获取激活状态的背景色
        getActiveBackgroundColor(statusValue) {
            const activeMap = {
                'all': '#daedff',         // 全部
                'pending': '#ffe7dc',     // 待受理
                'accepted': '#daedff',    // 已受理
                'processed': '#ddf1ed',   // 已办理
                'rejected': '#daedff'     // 已驳回
            };
            return activeMap[statusValue] || '#daedff';
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
        
        // 获取标题文案
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
            
            // 组合标题：位置名称发现问题类型问题（无空格）
            let title = '';
            if (locationText && item.feedbackTypeLabel) {
                title = `${locationText}发现${item.feedbackTypeLabel}问题`;
            } else if (locationText) {
                title = locationText;
            } else if (item.feedbackTypeLabel) {
                title = `发现${item.feedbackTypeLabel}问题`;
            }
            
            return title || '创建的问题';
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
        }
    }
}
</script>

<style lang="scss" scoped>
.feedBack-list-container {
    position: relative;
    width: 100%;
    height: 100vh; // 使用视口高度
    overflow: hidden;
    background-color: #f5f5f5;
    display: flex;
    flex-direction: column;

    .content-layer {
        width: 100%;
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        min-height: 0; // 重要：允许 flex 子元素缩小

        .paddingContent {
            flex: 1;
            overflow-y: auto;
            -webkit-overflow-scrolling: touch; // iOS 平滑滚动
            padding: 0;
            padding-bottom: 140px; // 为底部菜单栏留出空间
            display: flex;
            flex-direction: column;
            align-items: center;
            min-height: 0; // 重要：允许 flex 子元素缩小

            // 筛选区域
            .filter-section {
                width: 100%;
                background-color: #ffffff;
                padding: 24px;
                box-sizing: border-box;

                .date-picker-wrapper {
                    display: flex;
                    align-items: center;
                    gap: 16px;
                    width: 100%;

                    .date-picker {
                        flex: 1;
                    }

                    .date-separator {
                        font-size: 28px;
                        color: #666666;
                        flex-shrink: 0;
                    }
                }
            }

            // 状态筛选区域
            .status-filter-section {
                width: 100%;
                margin-top: 16px;
                display: flex;
                align-items: center;
                justify-content: flex-start;
                gap: 0;
                box-sizing: border-box;
                flex-wrap: wrap;
                background-color: #ffffff;

                .status-tab {
                    flex: 1;
                    min-width: 0;
                    height: 104px;
                    opacity: 1;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    position: relative;
                    cursor: pointer;
                    border-bottom: 4px solid transparent;
                    box-sizing: border-box;
                    transition: border-bottom-color 0.3s ease;
                    background-color: #ffffff;

                    .status-label {
                        font-size: 24px;
                        color: #3d3d3d;
                        font-weight: 400;
                        margin-bottom: 8px;
                        line-height: 1;
                    }

                    .status-count {
                        font-size: 32px;
                        font-weight: 500;
                        line-height: 1;
                    }

                    .divider {
                        position: absolute;
                        right: 0;
                        top: 50%;
                        transform: translateY(-50%);
                        width: 1px;
                        height: 60px;
                        background-color: #f0f0f0;
                    }

                    &.active {
                        border-bottom-color: #349DFF;
                    }

                    &:active {
                        opacity: 0.7;
                    }
                }
            }

            // 反馈列表
            .feedBackList {
                width: 702px;
                opacity: 1;
                background: transparent;
                padding: 0;
                margin-top: 16px;
                display: flex;
                flex-direction: column;
                gap: 16px;

                .feedBackItem {
                    width: 100%;
                    min-height: 205px;
                    background-color: #ffffff;
                    padding: 24px;
                    box-sizing: border-box;
                    cursor: pointer;
                    transition: all 0.3s ease;
                    position: relative; // 为超时标记提供定位基准
                    display: flex;
                    flex-direction: column;
                    overflow: visible;

                    &:active {
                        opacity: 0.8;
                    }

                    // 超时标记
                    .overtime-mark {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 85px;
                        height: 28px;
                        z-index: 5;
                        pointer-events: none; // 防止图片阻挡点击
                    }

                    .itemTitle {
                        color: #3d3d3d;
                        font-size: 32px;
                        line-height: 32px;
                        padding-bottom: 16px;
                        margin-bottom: 16px;
                        display: flex;
                        align-items: center;
                        justify-content: flex-start;
                        gap: 16px;
                        flex-shrink: 0;
                        position: relative;

                        .feedback-type-badge {
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
                        }

                        .type-badge {
                            width: 56px;
                            height: 32px;
                            opacity: 1;
                            background: #369eff;
                            border-radius: 4px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            color: #ffffff;
                            font-size: 24px;
                            font-weight: 500;
                            flex-shrink: 0;

                            &.reservoir-badge {
                                background: #3CB298;
                            }
                        }

                        .title-text {
                            flex: 1;
                            font-size: 32px;
                            font-weight: 500;
                            color: #3d3d3d;
                            word-break: break-all;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            display: -webkit-box;
                            -webkit-line-clamp: 1;
                            line-clamp: 1;
                            -webkit-box-orient: vertical;
                            padding-right: 100px; // 为状态标签留出空间
                        }

                        .status-label {
                            position: absolute;
                            top: -24px;
                            right: -24px;
                            width: 100px;
                            height: 100px;
                            z-index: 2;
                            overflow: hidden;
                            clip-path: polygon(0 0, 100% 0, 100% 100%);
                            
                            .status-text {
                                position: absolute;
                                top: 18px;
                                right: 0px;
                                font-size: 24px;
                                font-weight: 500;
                                color: #ffffff;
                                white-space: nowrap;
                                transform: rotate(45deg);
                                transform-origin: center;
                            }
                        }
                    }

                    .feedBackBox {
                        display: flex;
                        flex-direction: column;
                        gap: 16px;
                        flex: 1;
                        min-height: 0;
                        overflow: visible;

                        .file-time-row {
                            display: flex;
                            align-items: center;
                            gap: 16px;
                            flex-shrink: 0;
                            padding-bottom: 24px;
                            border-bottom: 1px solid rgba(238, 238, 238, 0.5);
                            margin-bottom: 8px;

                            .file-count-wrapper {
                                width: 69px;
                                height: 36px;
                                background-image: url('@/assets/img/riverM-appendix.png');
                                background-size: cover;
                                background-position: center;
                                background-repeat: no-repeat;
                                border-radius: 2px;
                                display: flex;
                                align-items: center;
                                justify-content: right;
                                flex-shrink: 0;
                                position: relative;
                                padding: 0px 10px;

                                .file-count {
                                    font-size: 28px;
                                    color: #3d3d3d;
                                }
                            }

                            .time-text {
                                padding: 1px;
                                font-size: 28px;
                                color: #3d3d3d;
                                background: #f8f8f8;
                                flex-shrink: 0;
                            }
                        }

                        .location-row {
                            display: flex;
                            align-items: center;
                            gap: 8px;
                            flex-shrink: 0;

                            .location-icon {
                                width: 25px;
                                height: 25px;
                                flex-shrink: 0;
                            }

                            .location-text {
                                font-size: 28px;
                                color: #3d3d3d;
                                flex: 1;
                                overflow: hidden;
                                text-overflow: ellipsis;
                                white-space: nowrap;
                            }
                        }
                    }
                }

                // 加载更多提示
                .loading-more {
                    width: 100%;
                    padding: 40px 24px;
                    text-align: center;
                    box-sizing: border-box;

                    .loading-text {
                        font-size: 28px;
                        color: #999999;
                        font-weight: 400;
                    }
                }
                
                // 空状态提示
                .empty-state {
                    width: 100%;
                    max-width: 100%;
                    background-color: #ffffff;
                    padding: 60px 24px;
                    text-align: center;
                    box-sizing: border-box;
                    overflow: visible;
                    margin-top: 16px;

                    .empty-text {
                        font-size: 28px;
                        color: #999999;
                        font-weight: 400;
                        display: inline-block;
                        width: auto;
                        white-space: nowrap;
                        word-break: keep-all;
                    }
                }
            }
        }
    }

    // 悬浮反馈按钮
    .floating-feedback-btn {
        position: fixed;
        right: 24px;
        bottom: 160px; // 避免被底部菜单栏遮挡
        width: 100px;
        height: 100px;
        background: url('@/assets/img/gotoFeedBack.png') no-repeat center center;
        background-size: 100% 100%;
        cursor: pointer;
        z-index: 999;
    }
}

// 覆盖 element-plus 日期选择器样式，适配移动端
:deep(.el-date-editor) {
    width: 100% !important;
    
    .el-input__wrapper {
        padding: 12px 16px;
        font-size: 24px;
        background-color: #f8f8f8 !important;
    }
    
    .el-input__inner {
        font-size: 24px;
        height: auto;
    }
}

:deep(.el-date-editor--daterange) {
    .el-range-separator {
        font-size: 24px;
        padding: 0 8px;
    }
}
</style>

