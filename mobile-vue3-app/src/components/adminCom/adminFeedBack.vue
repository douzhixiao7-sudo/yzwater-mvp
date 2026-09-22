<!-- 公众反馈列表页面 -->
<template>
    <div class="public-feedback-list-container">
        <div class="content-layer">
            <!-- 页面标题组件 -->
            <!-- <PageHeader title="公众反馈" @close="handleClose" /> -->

            <div class="paddingContent">
                <!-- 日期范围筛选 -->
                <el-config-provider :locale="zhCn">
                    <div class="filter-section">
                        <div class="date-picker-wrapper">
                            <el-date-picker v-model="startDate" type="date" placeholder="开始日期" format="YYYY-MM-DD"
                                value-format="YYYY-MM-DD" @change="handleDateChange" class="date-picker" />
                            <span class="date-separator">-</span>
                            <el-date-picker v-model="endDate" type="date" placeholder="结束日期" format="YYYY-MM-DD"
                                value-format="YYYY-MM-DD" @change="handleDateChange" class="date-picker" />
                        </div>
                    </div>
                </el-config-provider>

                <!-- 状态筛选 -->
                <div class="status-filter-section">
                    <div class="status-tab" v-for="(item, index) in filteredStatusTabs" :key="item.value"
                        :class="{ 'active': currentStatus === item.value }" @click="handleStatusChange(item.value)">
                        <div class="status-label">{{ item.label }}</div>
                        <div class="status-count" :style="{ color: item.color }">{{ item.count }}</div>
                        <div class="divider" v-if="index < filteredStatusTabs.length - 1"></div>
                    </div>
                </div>

                <!-- 反馈列表 -->
                <div class="feedBackList" ref="feedBackListContainer">
                    <div class="feedBackItem" v-for="item in feedBackList" :key="item.id" @click="goToDetail(item)">
                        <!-- 超时标记 -->
                        <img v-if="isItemOverdue(item)" :src="isOverTimeImg" class="overtime-mark" alt="已超时">
                        <div class="itemTitle">
                            <span class="type-badge" v-if="item.feedbackTypeLabel"
                                :style="{ background: getFeedbackTypeColor(item.feedbackTypeLabel) }">{{
                                item.feedbackTypeLabel }}</span>
                            <span class="title-text">{{ getTitleText(item) }}</span>
                            <span class="status-label" v-if="item.statusLabel"
                                :style="{ background: getStatusBackgroundColor(item.status) }">
                                <span class="status-text" :style="{ color: getStatusColor(item.status) }">{{
                                    item.statusLabel }}</span>
                            </span>
                        </div>

                        <div class="feedBackBox">
                            <!-- 文件数量和时间 -->
                            <div class="feedBackItem-row file-time-row">
                                <div class="file-count-wrapper"
                                    v-if="item.uploadedFiles && item.uploadedFiles.length > 0">
                                    <span class="file-count">{{ item.uploadedFiles.length }}</span>
                                </div>
                                <span class="time-text">{{ formatTimeShort(item.createTime) }}</span>
                            </div>
                            <!-- 问题定位 -->
                            <div class="feedBackItem-row location-row">
                                <img src="@/assets/img/position2.png" alt="问题定位" class="location-icon">
                                <span class="location-text">{{ item.issueSpecificLocation || item.specificLocation ||
                                    '--' }}</span>
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
    </div>
</template>

<script>
import PageHeader from '@/components/commonCom/PageHeader.vue';
import { ElDatePicker, ElConfigProvider, ElMessage, ElLoading } from 'element-plus';
import zhCn from 'element-plus/dist/locale/zh-cn.js';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import isOverTimeImg from '@/assets/img/isOverTimeImg.png';

// 设置 Day.js 为中文
dayjs.locale('zh-cn');

export default {
    name: 'PublicFeedBackList',
    components: {
        PageHeader,
        ElDatePicker,
        ElConfigProvider
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
                { label: '待受理', value: 'pending', count: 0, color: '#FF5100', statusValue: 0, background: '#ffe7dc' },
                { label: '处理中', value: 'accepted', count: 0, color: '#349DFF', statusValue: 2, background: '#daedff' },
                { label: '待核验', value: 'verifying', count: 0, color: '#FFA500', statusValue: 3, background: '#f6eada' },
                { label: '已办结', value: 'processed', count: 0, color: '#3CB298', statusValue: 4, background: '#ddf1ed' },
                { label: '已驳回', value: 'rejected', count: 0, color: '#3D3D3D', statusValue: 1, background: '#daedff' }
            ],
            // 反馈列表（显示的数据）
            feedBackList: [],
            // 原始全部数据（用于前端过滤）
            allFeedBackList: [],
            // 分页参数
            pagination: {
                pageNo: 1,
                pageSize: 3,  // 每次加载3条数据
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
        // 根据角色名称过滤状态标签
        filteredStatusTabs() {
            const roleName = this.$store.getters.getRoleName;
            console.log('当前角色名称:', roleName);

            // 如果是"普通角色"，过滤掉"处理中"按钮，只显示其他按钮
            if (roleName === '普通角色') {
                return this.statusTabs
                    .filter(tab => {
                        // 过滤掉"处理中"（accepted）按钮
                        return tab.value !== 'accepted';
                    })
                    .map(tab => {
                        // 普通角色中，pending 显示为"待处理"，背景色改为 #daedff，statusValue 改为 2（处理中），颜色改为蓝色
                        if (tab.value === 'pending') {
                            return { ...tab, label: '待处理', background: '#daedff', statusValue: 2, color: '#349DFF' };
                        }
                        return tab;
                    });
            }

            // 其他角色显示全部标签
            return this.statusTabs;
        }
    },
    mounted() {
        // document.title = '公众反馈';

        // 设置默认时间范围：2026-01-01 到当前日期
        this.startDate = '2026-01-01';
        this.endDate = dayjs().format('YYYY-MM-DD');

        // 恢复之前保存的 tabbar 状态
        const savedStatus = sessionStorage.getItem('adminFeedBack_currentStatus');
        if (savedStatus && this.statusTabs.some(tab => tab.value === savedStatus)) {
            this.currentStatus = savedStatus;
            console.log('[恢复状态] 恢复 tabbar 状态:', savedStatus);
        }

        // 在 nextTick 中初始化滚动容器，确保 DOM 已渲染
        this.$nextTick(() => {
            // 查找滚动容器（.paddingContent）
            this.scrollContainer = document.querySelector('.paddingContent') ||
                this.$el.querySelector('.paddingContent') ||
                this.$refs.feedBackListContainer?.parentElement;
            // 添加滚动监听
            this.addScrollListener();
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
            this.$emit('close');
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

            console.log('[日期查询] 重置分页参数:', {
                pageNo: this.pagination.pageNo,
                pageSize: this.pagination.pageSize,
                startDate: this.startDate,
                endDate: this.endDate
            });

            // 重新加载列表数据（从第1页开始，每次3条）
            this.loadFeedBackData();
        },

        // 状态变化处理
        handleStatusChange(status) {
            console.log('[状态切换] 切换到状态:', status);
            this.currentStatus = status;
            // 保存当前状态到 sessionStorage
            sessionStorage.setItem('adminFeedBack_currentStatus', status);
            // 状态切换时，重置分页并重新加载数据
            this.pagination.pageNo = 1;
            this.pagination.hasMore = true;
            this.pagination.total = 0;
            // 清空已加载的数据
            this.allFeedBackList = [];
            this.feedBackList = [];
            // 重新加载数据（从第1页开始，每次3条，传递对应的status参数）
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
            if (this.loadingMore || !this.pagination.hasMore) {
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

            // 距离底部 100px 时开始加载
            if (scrollTop + clientHeight >= scrollHeight - 100) {
                this.loadMoreData();
            }
        },

        // 加载更多数据
        loadMoreData() {
            if (this.loadingMore || !this.pagination.hasMore) {
                return;
            }

            this.loadingMore = true;
            this.pagination.pageNo += 1;

            this.loadFeedBackData(true);
        },

        // 加载统计数据（循环调用接口获取每个状态的统计数据）
        loadStatisticsData() {
            // 获取所有需要统计的状态标签（过滤掉 filteredStatusTabs 中不显示的）
            const tabsToQuery = this.filteredStatusTabs;

            // 循环调用接口获取每个状态的统计数据
            const promises = tabsToQuery.map(tab => {
                // 构建查询参数
                let queryParams = [];

                // 分页参数（随便写，只需要 total）
                queryParams.push(`pageNo=1`);
                queryParams.push(`pageSize=10`);

                // 日期参数（如果有）
                if (this.startDate && this.endDate) {
                    queryParams.push(`createTime=${this.startDate},${this.endDate}`);
                }

                // status 参数（如果有 statusValue）
                if (tab.statusValue !== null && tab.statusValue !== undefined) {
                    queryParams.push(`status=${tab.statusValue}`);
                }
                // 注意：对于 "全部"（statusValue 为 null），不传 status 参数

                // 其他可选参数（根据接口要求）
                //   queryParams.push(`facilityName=`);
                //   queryParams.push(`facilityType=`);
                //   queryParams.push(`feedbackType=`);

                // 拼接完整的 URL
                const url = `/admin-api/problem/feedback/page?${queryParams.join('&')}`;

                console.log(`[统计数据] 查询 ${tab.label} (status=${tab.statusValue}):`, url);

                // 返回 Promise，用于后续处理
                return this.$http.get(url)
                    .then(res => {
                        const total = res.data?.total || 0;
                        console.log(`[统计数据] ${tab.label} 总数:`, total);
                        return { tab, total };
                    })
                    .catch(error => {
                        console.error(`[统计数据] 获取 ${tab.label} 统计数据失败:`, error);
                        return { tab, total: 0 };
                    });
            });

            // 等待所有请求完成
            Promise.all(promises).then(results => {
                // 更新每个状态的 count
                results.forEach(({ tab, total }) => {
                    // 找到对应的 statusTabs 项并更新 count
                    const statusTab = this.statusTabs.find(t => t.value === tab.value);
                    if (statusTab) {
                        statusTab.count = total;
                    }
                });

                console.log('[统计数据] 所有状态统计完成:', this.statusTabs.map(t => ({ label: t.label, count: t.count })));
            });
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

            // 手动构建查询字符串
            let queryParams = [];

            // 根据是否为加载更多设置不同的 pageSize
            // 首次加载查询5条，后续滚动加载更多查询3条
            const pageSize = isLoadMore ? 3 : 5;

            // 必填参数
            queryParams.push(`pageNo=${this.pagination.pageNo}`);
            queryParams.push(`pageSize=${pageSize}`);

            console.log('[加载数据] 分页参数:', {
                pageNo: this.pagination.pageNo,
                pageSize: pageSize,
                isLoadMore,
                currentStatus: this.currentStatus,
                startDate: this.startDate,
                endDate: this.endDate
            });

            // 构建 createTime 参数（字符串格式）
            // 格式为: createTime=2024-10-11,2024-10-12 (开始日期和结束日期用逗号分隔)
            // 只有在两个日期都选择时才添加 createTime 参数
            if (this.startDate && this.endDate) {
                // 使用字符串格式，开始日期和结束日期用逗号分隔
                queryParams.push(`createTime=${this.startDate},${this.endDate}`);
            }

            // 根据当前选择的状态传递 status 参数
            if (this.currentStatus && this.currentStatus !== 'all') {
                const roleName = this.$store.getters.getRoleName;
                let statusValue = null;

                // 如果是普通角色且选择的是 pending（待处理），应该查询 status === 2（处理中）
                if (roleName === '普通角色' && this.currentStatus === 'pending') {
                    statusValue = 2;
                } else {
                    const statusTab = this.statusTabs.find(tab => tab.value === this.currentStatus);
                    if (statusTab && statusTab.statusValue !== null) {
                        statusValue = statusTab.statusValue;
                    }
                }

                // 如果有 statusValue，添加到查询参数
                if (statusValue !== null) {
                    queryParams.push(`status=${statusValue}`);
                }
            }
            // 注意：如果选择的是 "全部"（currentStatus === 'all'），不传 status 参数

            // 拼接完整的 URL
            const url = `/admin-api/problem/feedback/page?${queryParams.join('&')}`;

            console.log('[加载数据] 请求 URL:', url);
            console.log('[加载数据] 当前状态:', this.currentStatus, '传递的status参数:', queryParams.find(p => p.startsWith('status=')) || '无');

            this.$http.get(url).then(res => {
                console.log('反馈数据:', res.data);
                if (res.data) {
                    const newList = res.data.list || [];
                    const total = res.data.total || 0;

                    // 更新总数据量
                    this.pagination.total = total;

                    // 判断是否还有更多数据
                    const currentLoaded = isLoadMore ? this.allFeedBackList.length + newList.length : newList.length;
                    this.pagination.hasMore = currentLoaded < total;

                    if (isLoadMore) {
                        // 加载更多：追加数据
                        this.allFeedBackList = [...this.allFeedBackList, ...newList];
                    } else {
                        // 首次加载或重新加载：替换数据
                        this.allFeedBackList = newList;
                        // 只在首次加载或重新加载时调用统计接口（避免加载更多时重复请求）
                        this.loadStatisticsData();
                    }

                    // 由于接口已经根据 status 参数返回了对应状态的数据，不需要前端过滤
                    // 直接使用返回的数据
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
                console.error('获取反馈数据失败:', error);
                if (!isLoadMore) {
                    this.allFeedBackList = [];
                    this.feedBackList = [];
                    this.resetStatusCounts();
                }
                this.pagination.hasMore = false;
            }).finally(() => {
                this.loading = false;
                this.loadingMore = false;
                // 关闭 loading 提示
                if (loadingInstance) {
                    loadingInstance.close();
                }
            });
        },

        // 前端过滤反馈列表
        filterFeedBackList() {
            let filteredList = [...this.allFeedBackList];

            if (this.currentStatus && this.currentStatus !== 'all') {
                const roleName = this.$store.getters.getRoleName;

                // 如果是普通角色且选择的是 pending（待处理），应该过滤 status === 2（处理中）
                if (roleName === '普通角色' && this.currentStatus === 'pending') {
                    filteredList = filteredList.filter(item => item.status === 2);
                } else {
                    const statusTab = this.statusTabs.find(tab => tab.value === this.currentStatus);
                    if (statusTab && statusTab.statusValue !== null) {
                        filteredList = filteredList.filter(item => item.status === statusTab.statusValue);
                    }
                }
            }

            this.feedBackList = filteredList;
        },

        // 重置状态统计数字
        resetStatusCounts() {
            this.statusTabs.forEach(tab => {
                tab.count = 0;
            });
        },

        // 计算状态数量
        calculateStatusCounts(list) {
            const counts = {
                all: list.length,
                pending: 0,
                accepted: 0,
                verifying: 0,
                processed: 0,
                rejected: 0
            };

            list.forEach(item => {
                if (item.status === 0) {
                    counts.pending++;
                } else if (item.status === 1) {
                    counts.rejected++;
                } else if (item.status === 2) {
                    counts.accepted++;
                } else if (item.status === 3) {
                    counts.verifying++;
                } else if (item.status === 4) {
                    counts.processed++;
                }
            });

            this.updateStatusCounts(counts);
        },

        // 更新状态按钮的数字
        updateStatusCounts(counts) {
            const roleName = this.$store.getters.getRoleName;

            this.statusTabs.forEach(tab => {
                if (tab.value === 'all') {
                    tab.count = counts.all || 0;
                } else if (tab.value === 'pending') {
                    // 如果是普通角色，pending 显示为"待处理"，应该统计 status === 2（处理中）的数量
                    if (roleName === '普通角色') {
                        tab.count = counts.accepted || 0;
                    } else {
                        tab.count = counts.pending || 0;
                    }
                } else if (tab.value === 'accepted') {
                    tab.count = counts.accepted || 0;
                } else if (tab.value === 'verifying') {
                    tab.count = counts.verifying || 0;
                } else if (tab.value === 'processed') {
                    tab.count = counts.processed || 0;
                } else if (tab.value === 'rejected') {
                    tab.count = counts.rejected || 0;
                }
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

        // 判断列表项是否超时
        isItemOverdue(item) {
            // 只判断处理中（status === 2）和待核验（status === 3）阶段
            if (!item || (item.status !== 2 && item.status !== 3)) {
                return false;
            }

            // 直接使用 item.plannedCompletionTime
            if (!item.plannedCompletionTime) {
                return false;
            }

            // 判断是否超时：当前时间 > plannedCompletionTime
            const currentTime = new Date().getTime();
            const plannedTime = item.plannedCompletionTime;

            return currentTime > plannedTime;
        },

        // 预览文件
        previewFile(fileUrl) {
            if (this.isImage(fileUrl)) {
                window.open(this.resolveFileUrl(fileUrl), '_blank');
            } else {
                window.open(this.resolveFileUrl(fileUrl), '_blank');
            }
        },

        // 前往详情页面
        goToDetail(item) {
         

            // 保存当前 tabbar 状态到 sessionStorage
            sessionStorage.setItem('adminFeedBack_currentStatus', this.currentStatus);

            this.$router.push({
                name: 'FixFeedBack',
                query: {
                    data: JSON.stringify(item)
                }
            });
        },

        // 根据状态值获取颜色
        getStatusColor(status) {
            const colorMap = {
                0: '#FF5100',
                1: '#999999', // 已驳回
                2: '#349DFF',
                3: '#FFA500',
                4: '#3CB298'
            };
            return colorMap[status] || '#666666';
        },

        // 根据状态值获取背景颜色
        getStatusBackgroundColor(status) {
            const colorMap = {
                0: '#FFE7DC', // 待受理
                1: '#E4E4E4', // 已驳回
                2: '#DAEDFF', // 处理中
                3: '#F6EADA', // 待核验
                4: '#DDF1ED'  // 已办结
            };
            return colorMap[status] || '#666666';
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

        // 获取反馈类型色块颜色
        getFeedbackTypeColor(feedbackTypeLabel) {
            console.log('feedbackTypeLabel', feedbackTypeLabel);
            if (!feedbackTypeLabel) return '#2F88DB';

            const typeMap = {
                '漂浮物': '#369EFF',
                '设施损坏': '#3CB298',
                '设备损坏': '#3CB298'
            };

            return typeMap[feedbackTypeLabel] || '#2F88DB'; // 其他类型默认为 #FF2F88DB
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
        }
    }
}
</script>

<style lang="scss" scoped>
.public-feedback-list-container {
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
    background-color: #f5f5f5;
    display: flex;
    flex-direction: column;

    .content-layer {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        overflow: hidden;

        .paddingContent {
            flex: 1;
            overflow-y: auto;
            padding: 0;
            padding-bottom: 140px;
            display: flex;
            flex-direction: column;
            align-items: center;

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
                        position: relative;

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
                        white-space: nowrap;
                        word-break: keep-all;
                        display: inline-block;
                        width: auto;
                        max-width: 100%;
                    }
                }
            }
        }
    }
}

// 覆盖 element-plus 日期选择器样式
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
