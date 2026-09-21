<!-- 反馈提交成功中转页面 -->
<template>
    <div class="feedBack-success-container">
        <div class="content-layer">
            <!-- 第一部分：反馈成功标题和二维码 -->
            <div class="section-1">
                <!-- 反馈成功标题 -->
                <div class="success-title">
                    <img src="@/assets/img/uploadSuccessIcon.png" alt="">
                    <span>反馈成功</span>
                </div>
                
                <!-- 二维码容器 -->
                <div class="qrcode-box">
                    <img :src="saveQrcodeImg" class="qrcode-image" :class="{ 'qrcode-glow-active': isQrcodeGlowing }" alt="二维码" />
                </div>
                
                <!-- 说明文字 -->
                <div class="qrcode-label">长按保存二维码，以便查看反馈</div>
            </div>

            <!-- 第二部分 -->
            <div class="section-2">
                <div class="section-2-content" v-if="feedBackList.length > 0" v-for="item in feedBackList" :key="item.id">
                    <!-- 标题部分 -->
                    <div class="section-2-title">
                        <div class="feedback-type-badge" v-if="item.feedbackTypeLabel"
                            :style="{ background: getFeedbackTypeColor(item.feedbackTypeLabel) }">
                            {{ item.feedbackTypeLabel }}
                        </div>
                        <div class="title-text">
                            {{ formatTitleText(item) }}
                        </div>
                    </div>
                    <!-- 内容部分 -->
                    <div class="section-2-content-area">
                        {{ item.feedbackContent || '--' }}
                    </div>
                </div>
                <div class="section-2-content" v-else>
                    <!-- 数据加载中或暂无数据时的占位 -->
                    <div class="section-2-title">
                        <div class="feedback-type-badge">
                            其他
                        </div>
                        <div class="title-text">
                           --
                        </div>
                    </div>
                    <div class="section-2-content-area">
                        --
                    </div>
                </div>
            </div>

            <!-- 第三部分：反馈文件 -->
            <div class="section-3">
                <div class="section-3-content" v-if="feedBackList.length > 0" v-for="item in feedBackList" :key="item.id">
                    <!-- 标题 -->
                    <div class="section-3-title">
                        <span>反馈文件</span>
                    </div>
                    <!-- 内容部分：图片和视频 -->
                    <div class="section-3-content-area">
                        <div v-if="item.uploadedFiles && item.uploadedFiles.length > 0" class="file-list">
                            <div 
                                class="file-item" 
                                v-for="(fileUrl, index) in item.uploadedFiles" 
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
                <div class="section-3-content" v-else>
                    <div class="section-3-title">
                        <span>反馈文件</span>
                    </div>
                    <div class="section-3-content-area">
                        <div class="no-files">暂无文件</div>
                    </div>
                </div>
            </div>

            <!-- 第四部分：问题点位 -->
            <div class="section-4">
                <div class="section-4-content" v-if="feedBackList.length > 0" v-for="item in feedBackList" :key="item.id">
                    <!-- 标题 -->
                    <div class="section-4-title">
                        <span>问题点位</span>
                    </div>
                    <!-- 地图容器 -->
                    <div class="section-4-map-wrapper">
                        <div id="problem-location-map" ref="problemLocationMap" class="problem-location-map"></div>
                    </div>
                </div>
                <div class="section-4-content" v-else>
                    <div class="section-4-title">
                        <span>问题点位</span>
                    </div>
                    <div class="section-4-map-wrapper">
                        <div class="no-map-data">暂无位置信息</div>
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

        <!-- 悬浮反馈按钮 -->
        <div class="floating-feedback-btn" @click="goToFeedBack">
            <!-- <span class="btn-text"></span> -->
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
    name: 'FeedBackSuccess',
    components: {
        PageHeader
    },
    mixins: [tiandituMixin],
    mounted() {
        // document.title = '仪征河湖管理平台';
        this.getFeedBackList();
    },
    beforeUnmount() {
        this.destroyMap();
    },
    computed: {
        // 判断是否为生产环境
        isProduction() {
            // 使用 NODE_ENV 判断环境
            return process.env.NODE_ENV === 'production';
        },
        // 获取 base URL（处理 Vite 的 base 配置）
        baseUrl() {
            return import.meta.env.BASE_URL || '/';
        },
        // 二维码图片路径（展示和保存都使用这个，确保二维码可扫描）
        saveQrcodeImg() {
            // 生产环境使用 dev-save.png，开发环境使用 prod-save.png
            const env = this.isProduction ? 'prod' : 'dev';
            // 使用 baseUrl 拼接路径，确保在开发和生产环境都能正确加载
            const path = `qrCodeImg/${env}-save.png`;
            return `${this.baseUrl}${path}`;
        }
    },
    data() {
        return {
            feedBackList: [],
            hasShownConfirm: false, // 标记是否已显示过确认提示
            isQrcodeGlowing: false, // 控制二维码泛光动画
            problemMap: null, // 问题点位地图实例
            problemMarker: null, // 问题点位标记
            tiandituVecLayer: null, // 天地图矢量底图图层
            tiandituCvaLayer: null // 天地图矢量标注图层
        };
    },
    methods: {
        // 关闭页面
        handleClose() {
            this.$router.push({ name: 'UserFeedBack' });
        },

        // 前往反馈列表（二次确认）
        goToFeedBackList() {
            if (!this.hasShownConfirm) {
                // 第一次点击，显示提示信息并触发二维码闪烁
                ElMessage.warning('建议您保存二维码,方便后续查看');
                this.hasShownConfirm = true;
                // 触发二维码闪烁动画
                this.triggerQrcodeGlow();
            } else {
                // 第二次点击，执行跳转
                this.$router.push({ name: 'FeedBackList' });
            }
        },

        // 前往反馈页面
        goToFeedBack() {
            this.$router.push({ name: 'UserFeedBack' });
        },

        // 触发二维码闪烁动画
        triggerQrcodeGlow() {
            this.isQrcodeGlowing = true;
            // 动画持续约2.5秒（闪烁3次），然后移除类名
            setTimeout(() => {
                this.isQrcodeGlowing = false;
            }, 2500);
        },

        //查询全部反馈列表
        getFeedBackList() {
            const params = {
                pageNo: 1,
                pageSize: 1,
            };
            this.$http.get('/app-api/problem/feedback/my-page', { params }).then(res => {
                console.log('反馈列表数据:', res.data);
                console.log('第一条数据:', res.data.list[0]);
                if (res.data && res.data.list && res.data.list.length > 0) {
                    // 第一条
                    this.feedBackList = [res.data.list[0]];
                    console.log('feedBackContent:', this.feedBackList[0].feedbackContent);
                    // 数据加载后初始化地图
                    this.$nextTick(() => {
                        this.initProblemMap();
                    });
                }
            }).catch(error => {
                console.error('获取反馈列表失败:', error);
            });
        },

        // 格式化时间戳
        formatTime(timestamp) {
            return this.$tool.dateFormat(timestamp, 'yyyy-MM-dd hh:mm:ss');
        },

        // 格式化时间轴时间
        formatTimelineTime(timestamp) {
            if (!timestamp || isNaN(timestamp)) return '--';
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

        // 获取设施类型标签（用于色块显示）
        getFacilityTypeLabel(referenceType) {
            const typeMap = {
                'river': '河道',
                'river_section': '河道',
                'reservoir': '水库'
            };
            return typeMap[referenceType] || '河道';
        },

        // 初始化问题点位地图
        initProblemMap() {
            // 获取 ref，v-for 中 ref 返回数组
            const mapContainer = Array.isArray(this.$refs.problemLocationMap) 
                ? this.$refs.problemLocationMap[0] 
                : this.$refs.problemLocationMap;

            if (!mapContainer) {
                console.warn('地图容器未找到');
                return;
            }

            if (this.feedBackList.length === 0) {
                console.warn('反馈列表为空，无法初始化地图');
                return;
            }

            const item = this.feedBackList[0];
            const longitude = item.problemLongitude;
            const latitude = item.problemLatitude;
            const issueSpecificLocation = item.issueSpecificLocation || '问题位置';

            if (!longitude || !latitude) {
                console.warn('问题位置缺少经纬度信息', { longitude, latitude });
                return;
            }

            console.log('初始化问题点位地图', { longitude, latitude, issueSpecificLocation });

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
            // 如果坐标看起来是 GCJ-02（中国境内），需要转换为 WGS84
            // 先尝试转换，如果坐标已经是 WGS84，转换后的偏差会很小
            const [wgsLng, wgsLat] = this.transformGCJ02ToWGS84(
                parseFloat(longitude),
                parseFloat(latitude)
            );
            
            console.log('坐标转换:', {
                原始: { lng: longitude, lat: latitude },
                转换后: { lng: wgsLng, lat: wgsLat }
            });
            
            // 初始化地图 - Leaflet 使用 [纬度, 经度] 顺序
            // 将中心点稍微往上移动（增加纬度），使镜头稍微上移
            const offsetLat = 0.002; // 大约向上移动 200 米
            const center = [wgsLat + offsetLat, wgsLng];
            
            console.log('开始创建地图实例，容器尺寸:', {
                width: mapContainer.offsetWidth,
                height: mapContainer.offsetHeight,
                clientWidth: mapContainer.clientWidth,
                clientHeight: mapContainer.clientHeight
            });

            // 创建地图实例（只读模式，不允许用户操作）
            this.problemMap = L.map(mapContainer, {
                attributionControl: false,
                zoomControl: false,
                preferCanvas: true,
                tap: false,
                bounceAtZoomLimits: false,
                // 禁用所有交互功能，地图仅用于展示
                dragging: false,           // 禁用拖拽
                touchZoom: false,          // 禁用触摸缩放
                doubleClickZoom: false,    // 禁用双击缩放
                scrollWheelZoom: false,    // 禁用滚轮缩放
                boxZoom: false,            // 禁用框选缩放
                keyboard: false            // 禁用键盘操作
            });

            console.log('地图实例创建完成');

            // 设置地图视图和中心点
            this.problemMap.setView(center, 14);
            console.log('地图视图已设置，中心点:', center, '缩放级别: 14');

            // 使用天地图矢量地图服务（参考 map.vue）
            this.initTiandituVectorMap();

            // 确保地图正确显示 - 延迟调用以确保容器已完全渲染
            this.$nextTick(() => {
                setTimeout(() => {
                    if (this.problemMap) {
                        console.log('调用 invalidateSize，当前容器尺寸:', {
                            width: mapContainer.offsetWidth,
                            height: mapContainer.offsetHeight
                        });
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

        // 初始化天地图矢量地图服务（参考 map.vue）
        initTiandituVectorMap() {
            if (!this.problemMap) {
                console.error('地图实例不存在，无法初始化天地图');
                return;
            }

            // 检查天地图 token
            const token = this.getRandomTiandituToken();
            console.log('天地图 token:', token ? '已配置' : '未配置');

            // 创建天地图矢量底图图层
            this.tiandituVecLayer = this.createTiandituLayer(L, {
                layer: 'vec',
                attribution: '© 国家基础地理信息中心',
                maxZoom: 18,
                tileSize: 256
            });
            
            if (this.tiandituVecLayer) {
                // 监听瓦片加载事件
                this.tiandituVecLayer.on('tileload', () => {
                    console.log('天地图底图瓦片加载成功');
                });
                this.tiandituVecLayer.on('tileerror', (error, tile) => {
                    console.error('天地图底图瓦片加载失败:', error, tile);
                });
                
                this.tiandituVecLayer.addTo(this.problemMap);
                console.log('天地图矢量底图图层已添加');
            } else {
                console.error('天地图矢量底图图层创建失败');
            }

            // 创建天地图矢量标注图层
            this.tiandituCvaLayer = this.createTiandituLayer(L, {
                layer: 'cva',
                attribution: '© 国家基础地理信息中心',
                maxZoom: 18,
                tileSize: 256
            });
            
            if (this.tiandituCvaLayer) {
                // 监听瓦片加载事件
                this.tiandituCvaLayer.on('tileload', () => {
                    console.log('天地图标注瓦片加载成功');
                });
                this.tiandituCvaLayer.on('tileerror', (error, tile) => {
                    console.error('天地图标注瓦片加载失败:', error, tile);
                });
                
                this.tiandituCvaLayer.addTo(this.problemMap);
                console.log('天地图矢量标注图层已添加');
            } else {
                console.error('天地图矢量标注图层创建失败');
            }
        },

        // 添加问题点位标记
        addProblemMarker(latlng, issueSpecificLocation) {
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

        // GCJ-02 转 WGS84 坐标转换函数（反向转换）
        // 接口返回的坐标可能是 GCJ-02，天地图使用 WGS84 坐标系
        transformGCJ02ToWGS84(lng, lat) {
            // 使用迭代方法进行反向转换
            let dLat = 0;
            let dLng = 0;
            let outLat = lat;
            let outLng = lng;
            
            // 迭代计算，直到误差足够小
            for (let i = 0; i < 10; i++) {
                const [gcjLng, gcjLat] = this.transformWGS84ToGCJ02(outLng, outLat);
                dLat = lat - gcjLat;
                dLng = lng - gcjLng;
                outLat += dLat;
                outLng += dLng;
                
                // 如果误差足够小，退出循环
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
        },

        // 格式化标题文本：位置名称发现feedbackType问题
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

    }
}
</script>

<style lang="scss" scoped>
.feedBack-success-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    overflow-y: auto;
    overflow-x: hidden;
    -webkit-overflow-scrolling: touch;
    background-color: #f5f5f5;

    .content-layer {
        display: flex;
        flex-direction: column;
        width: 100%;
        padding: 24px 24px 180px 24px; // 底部增加 padding，避免内容被固定按钮遮挡
        box-sizing: border-box;
        min-height: 100%;

        // 第一部分：反馈成功标题和二维码
        .section-1 {
            min-height: 32px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: flex-start;
            margin-bottom: 16px;
            background: #ffffff;
            padding: 24px;
            // border-radius: 8px;
            box-sizing: border-box;

            .success-title {
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 5px;
                margin-bottom: 24px;

                img {
                    width: 30px;
                    height: 30px;
                }

                span {
                    color: #3d3d3d;
                    font-size: 36px;
                    font-weight: 500;
                    line-height: 32px;
                }
            }

            .qrcode-box {
                width: 410px;
                height: 250px;
                background-color: #C5E2FF;
                background-image: url('@/assets/img/qrcodeBox.png');
                background-size: 100% 100%;
                background-repeat: no-repeat;
                background-position: center;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-bottom: 24px;
                border-radius: 8px;
                position: relative;

                .qrcode-image {
                    margin-top: 25px;
                    width: 190px;
                    height: 190px;
                    object-fit: contain;
                    -webkit-touch-callout: default;
                    -webkit-user-select: none;
                    user-select: none;
                    pointer-events: auto;
                    filter: none;
                    transition: filter 0.3s ease;

                    &.qrcode-glow-active {
                        animation: qrcode-glow-blink 2.5s ease-in-out;
                    }
                }
            }

            .qrcode-label {
                font-size: 24px;
                color: #999999;
                text-align: center;
            }
        }

        // 第二部分
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
                display: flex;
                flex-direction: column;

                // 标题部分
                .section-2-title {
                    height: 81px;
                    display: flex;
                    align-items: center;
                    gap: 4px 12px 12px 4px;
                                    border-bottom: 1px solid #E6E6E6;


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
                        font-size: 28px;
                        font-weight: 500;
                        line-height: 1.4;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        display: -webkit-box;
                        -webkit-line-clamp: 2;
                        line-clamp: 2;
                        -webkit-box-orient: vertical;
                    }
                }

                // 内容部分
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
                    word-break: break-all;
                    box-sizing: border-box;
                }
            }
        }

        // 第三部分：反馈文件
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

                // 标题
                .section-3-title {
                    height: 81px;
                    display: flex;
                    align-items: center;
                    border-bottom: 1px solid #E6E6E6;

                    span {
                        color: #3d3d3d;
                        font-size: 28px;
                        font-weight: 500;
                        line-height: 1.4;
                    }
                }

                // 内容部分
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

        // 第四部分：问题点位
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

                // 标题部分
                .section-4-title {
                    height: 81px;
                    display: flex;
                    align-items: center;
                    border-bottom: 1px solid #E6E6E6;

                    span {
                        color: #3d3d3d;
                        font-size: 28px;
                        font-weight: 500;
                    }
                }

                // 地图容器
                .section-4-map-wrapper {
                    flex: 1;
                    margin-top: 16px;
                    height: 188px;
                    min-height: 188px;
                    border-radius: 8px;
                    overflow: hidden;
                    position: relative;
                    background: #f0f0f0; // 临时背景色，用于调试

                    .problem-location-map {
                        width: 100%;
                        height: 100%;
                        min-height: 188px;
                        display: block;
                    }

                    .no-map-data {
                        width: 100%;
                        height: 100%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: #999999;
                        font-size: 28px;
                        background: #f8f8f8;
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

    }

    // 底部操作栏 - 固定定位，悬浮在页面底部
    .bottom-action-bar {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        width: 100%;
        padding: 24px 24px 40px 24px;
        box-sizing: border-box;
        display: flex;
        justify-content: center;
        align-items: center;
        background: #ffffff;
        z-index: 1000;
        box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);

        .go-to-list-btn {
            width: 100%;
            max-width: 700px;
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

// 二维码橙黄色边框闪烁动画（点击按钮时触发，闪烁3次后停止，范围小、速度慢、更柔和）
@keyframes qrcode-glow-blink {
    0%, 100% {
        filter: none;
    }
    10% {
        filter: drop-shadow(0 0 3px rgba(255, 152, 0, 0.5)) 
                drop-shadow(0 0 5px rgba(255, 152, 0, 0.4))
                drop-shadow(0 0 7px rgba(255, 152, 0, 0.3));
    }
    20% {
        filter: none;
    }
    30% {
        filter: drop-shadow(0 0 4px rgba(255, 152, 0, 0.6)) 
                drop-shadow(0 0 6px rgba(255, 152, 0, 0.5))
                drop-shadow(0 0 8px rgba(255, 152, 0, 0.4));
    }
    40% {
        filter: none;
    }
    50% {
        filter: drop-shadow(0 0 3px rgba(255, 152, 0, 0.5)) 
                drop-shadow(0 0 5px rgba(255, 152, 0, 0.4))
                drop-shadow(0 0 7px rgba(255, 152, 0, 0.3));
    }
    60% {
        filter: none;
    }
    70% {
        filter: drop-shadow(0 0 4px rgba(255, 152, 0, 0.6)) 
                drop-shadow(0 0 6px rgba(255, 152, 0, 0.5))
                drop-shadow(0 0 8px rgba(255, 152, 0, 0.4));
    }
    80% {
        filter: none;
    }
    90% {
        filter: drop-shadow(0 0 3px rgba(255, 152, 0, 0.5)) 
                drop-shadow(0 0 5px rgba(255, 152, 0, 0.4))
                drop-shadow(0 0 7px rgba(255, 152, 0, 0.3));
    }
}
</style>
