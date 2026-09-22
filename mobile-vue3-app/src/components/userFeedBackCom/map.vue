<template>
    <div class="feedBack-map-container" :class="{ 'has-bottom-nav': hasBottomNav }">
        <div class="content-layer">
            <!-- <PageHeader title="选择位置" @close="handleClose" /> -->
            <div class="map-wrapper">
                <div id="map-container" ref="mapContainer"></div>
                <!-- 位置信息显示 -->
                <div class="location-info" :class="{ 'has-bottom-nav': hasBottomNav, 'list-expanded': isListExpanded }" v-if="locationInfo">
                    <div class="location-item">
                        <span class="location-label">坐标:</span>
                        <span class="location-value">{{ locationInfo.coordinates }}</span>
                    </div>
                    <div class="location-item">
                        <span class="location-label">位置:</span>
                        <span class="location-value">{{ locationInfo.address || '正在获取...' }}</span>
                    </div>
                </div>
                
                <!-- 点位信息列表 -->
                <div class="location-list-container" :class="{ 'has-bottom-nav': hasBottomNav, 'expanded': isListExpanded }">
                    <div class="location-list-header">
                        <div class="search-box">
                            <svg class="search-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2"/>
                                <path d="m20 20-4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                            </svg>
                            <input 
                                type="text" 
                                class="search-input" 
                                placeholder="请搜索河湖/水库名称"
                                v-model="searchKeyword"
                                @input="handleSearchInput"
                                @focus="handleSearchFocus"
                                @click="handleSearchClick"
                            />
                        </div>
                        <button class="toggle-btn" @click="toggleList">
                            <svg v-if="isListExpanded" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M18 15L12 9L6 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </button>
                    </div>
                    <div class="location-list" v-show="isListExpanded">
                        <div v-if="isLoadingLocations" class="loading-tip">正在加载点位信息...</div>
                        <div v-else-if="filteredLocationList.length === 0" class="empty-tip">当前位置没有河道/水库</div>
                        <div 
                            v-else
                            v-for="(item, index) in filteredLocationList" 
                            :key="index"
                            class="location-list-item"
                            :class="{ 'active': selectedIndex === getOriginalIndex(item) }"
                            @click="handleLocationSelect(getOriginalIndex(item), item)">
                            <img src="@/assets/img/position2.png" alt="位置" class="location-icon" />
                            <span class="location-name">{{ item.name }}</span>
                            <span v-if="index === 0" class="nearest-badge">距离最近</span>
                            <span v-if="item.distanceM !== undefined && item.distanceM !== null" class="location-distance">{{ Math.floor(item.distanceM) }}米</span>
                            <span v-else class="location-distance">0米</span>
                            <div class="check-icon-wrapper">
                                <img v-if="selectedIndex === getOriginalIndex(item)" src="@/assets/img/selectOn.png" alt="选中" class="check-icon" />
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- 底部确认按钮区域 - 绝对定位覆盖在地图上 -->
                <div class="bottom-action-bar" :class="{ 'has-bottom-nav': hasBottomNav }">
                    <button class="confirm-btn" :class="{ disabled: !canConfirm }" :disabled="!canConfirm" @click="handleConfirm">确认位置</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import PageHeader from '@/components/commonCom/PageHeader.vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import tiandituMixin from '@/mixins/tiandituMixin';
import resetPositionIcon from '@/assets/img/resetPositionIcon.png';
import tool from '@/utils/tools';
import { parse as parseWkt } from 'wellknown';
import { getCurrentLocationByAmap } from '@/utils/amapLocation';

// 确保 global 对象存在（浏览器环境兼容）
if (typeof global === 'undefined') {
    window.global = window;
}

// 特权账号：允许拖拽地图，并使用更大缓冲区查询半径
const PRIVILEGED_MOBILES = ['13270321160', '13083502622'];
const PRIVILEGED_BUFFER_RADIUS_M = 10000;
const DEFAULT_BUFFER_RADIUS_M = 10000;

export default {
    name: 'FeedBackMap',
    mixins: [tiandituMixin],
    components: {
        PageHeader
    },
    props: {
        // 从父组件传入的默认经纬度
        defaultLat: {
            type: Number,
            default: null
        },
        defaultLng: {
            type: Number,
            default: null
        },
        // 从父组件传入的默认地址
        defaultAddress: {
            type: String,
            default: null
        }
    },
    data() {
        return {
            map: null,
            baseLayer: null, // 底图图层
            annotationLayer: null, // 标注图层
            tiandituVecLayer: null, // 天地图矢量底图图层
            tiandituCvaLayer: null, // 天地图矢量标注图层
            marker: null, // 标记点
            center: [32.27, 119.18], // 仪征市大致坐标
            zoom: 13,
            locationInfo: null, // 位置信息 { coordinates, address }
            // 点位信息列表（从接口获取）
            locationList: [],
            selectedIndex: -1, // 当前选中的索引（-1表示未选中）
            selectedLocationItem: null, // 用户从列表中选择的点位信息（独立于标记点位置）
            searchKeyword: '', // 搜索关键词
            isListExpanded: true, // 列表是否展开
            isLoadingLocations: false // 是否正在加载点位列表
        };
    },
    computed: {
        // 判断是否有底部返回按钮（安卓设备且路由 meta.showBackButton 为 true）
        hasBottomNav() {
            // 只在安卓设备上显示
            const isAndroid = tool.isAndroid();
            const hasMeta = this.$route && this.$route.meta && this.$route.meta.showBackButton === true;
            
            // 调试信息
            console.log('hasBottomNav 计算:', {
                isAndroid,
                routeName: this.$route?.name,
                routeMeta: this.$route?.meta,
                hasMeta,
                result: isAndroid && hasMeta
            });
            
            return isAndroid && hasMeta;
        },
        // 过滤后的点位列表
        filteredLocationList() {
            if (!this.searchKeyword || !this.searchKeyword.trim()) {
                return this.locationList;
            }
            const keyword = this.searchKeyword.trim().toLowerCase();
            return this.locationList.filter(item => {
                return item.name && item.name.toLowerCase().includes(keyword);
            });
        },
        // 获取默认位置信息（优先使用 props，其次使用路由参数）
        defaultLocation() {
            // 优先使用 props
            if (this.defaultLat !== null && this.defaultLng !== null && !isNaN(this.defaultLat) && !isNaN(this.defaultLng)) {
                return {
                    lat: this.defaultLat,
                    lng: this.defaultLng,
                    address: this.defaultAddress || null
                };
            }
            
            // 如果没有 props，尝试从路由参数获取（兼容作为路由组件使用）
            if (this.$route && this.$route.query) {
                const lat = parseFloat(this.$route.query.lat);
                const lng = parseFloat(this.$route.query.lng);
                const address = this.$route.query.address;
                
                if (!isNaN(lat) && !isNaN(lng)) {
                    return {
                        lat: lat,
                        lng: lng,
                        address: address || null
                    };
                }
            }
            
            // 如果路由参数也没有，尝试从 Vuex 读取
            const relocatedLocationFromStore = this.$store.getters.getRelocatedLocation;
            if (relocatedLocationFromStore && relocatedLocationFromStore.latitude && relocatedLocationFromStore.longitude) {
                return {
                    lat: relocatedLocationFromStore.latitude,
                    lng: relocatedLocationFromStore.longitude,
                    address: relocatedLocationFromStore.specificLocation || null
                };
            }
            
            return null;
        },
        // 是否可确认（需有附近河道/水库）
        canConfirm() {
            return this.locationList.length > 0 && !!this.selectedLocationItem;
        },
        // 指定登录手机号允许拖拽标记调整位置
        allowMapDrag() {
            return this.isPrivilegedMobile();
        }
    },
    async mounted() {
        // document.title = '选择位置';
        
        // 调试：检查 hasBottomNav 和 DOM 元素
        this.$nextTick(() => {
            console.log('mounted - hasBottomNav:', this.hasBottomNav);
            const locationInfoEl = document.querySelector('.location-info');
            const bottomActionBarEl = document.querySelector('.bottom-action-bar');
            console.log('location-info classList:', locationInfoEl?.classList.toString());
            console.log('bottom-action-bar classList:', bottomActionBarEl?.classList.toString());
        });
        
        // 判断是否是首次进入（检查是否有保存的位置信息）
        const relocatedLocationFromStore = this.$store.getters.getRelocatedLocation;
        const hasSavedLocation = relocatedLocationFromStore && 
                                 relocatedLocationFromStore.latitude && 
                                 relocatedLocationFromStore.longitude;
        
        if (hasSavedLocation) {
            // 再次进入：使用保存的位置信息
            console.log('再次进入地图页面，使用保存的位置信息');
            const savedLocation = relocatedLocationFromStore;
            this.center = [savedLocation.latitude, savedLocation.longitude];
            
            // 如果有地址信息，直接设置位置信息（处理省市信息）
            if (savedLocation.specificLocation) {
                const processedAddress = this.removeProvinceCity(savedLocation.specificLocation);
                this.locationInfo = {
                    coordinates: `${savedLocation.latitude.toFixed(6)}, ${savedLocation.longitude.toFixed(6)}`,
                    address: processedAddress
                };
            }
            
            // 初始化地图（会恢复标记点和列表）
            this.initMap();
        } else {
            // 首次进入：使用高德 API 获取位置
            console.log('首次进入地图页面，使用高德 API 获取位置');
            try {
                this.showMessage('正在获取位置...', 'info');
                
                // 使用高德 API 获取位置（返回 WGS84 坐标）
                const locationData = await getCurrentLocationByAmap();
                console.log('高德 API 获取的位置:', locationData);
                
                // 高德 API 返回的是 WGS84 坐标系，天地图也使用 WGS84，可以直接使用
                const wgsLat = locationData.latitude;
                const wgsLng = locationData.longitude;
                
                // 使用 WGS84 坐标
                this.center = [wgsLat, wgsLng];
                
                // 初始化地图
                this.initMap();
                
                // 使用高德 API 返回的地址信息
                if (locationData.address) {
                    const processedAddress = this.removeProvinceCity(locationData.address);
                    this.locationInfo = {
                        coordinates: `${wgsLat.toFixed(6)}, ${wgsLng.toFixed(6)}`,
                        address: processedAddress
                    };
                } else {
                    // 如果没有地址，调用逆地理编码
                    await this.updateLocationInfo(wgsLat, wgsLng);
                }
                
                this.showMessage(
                    locationData.isDefault ? '定位失败，已使用默认位置' : '位置获取成功',
                    locationData.isDefault ? 'warning' : 'success'
                );
            } catch (error) {
                console.error('获取位置失败:', error);
                this.showMessage('定位失败,请稍后再试', 'error');
                setTimeout(() => {
                    this.$router.go(-1);
                }, 1500);
            }
        }
    },
    beforeUnmount() {
        this.destroyMap();
    },
    methods: {
        getLoginMobile() {
            return (localStorage.getItem('userMobile') || '').trim();
        },
        isPrivilegedMobile() {
            return PRIVILEGED_MOBILES.includes(this.getLoginMobile());
        },
        getBufferQueryRadiusM() {
            return this.isPrivilegedMobile() ? PRIVILEGED_BUFFER_RADIUS_M : DEFAULT_BUFFER_RADIUS_M;
        },
        // 初始化地图
        initMap() {
            if (this.$refs.mapContainer) {
                // 初始化地图，禁用版权信息控件
                this.map = L.map(this.$refs.mapContainer, {
                    attributionControl: false,
                    zoomControl: true,
                    scrollWheelZoom: true
                }).setView(this.center, this.zoom);

                // 使用天地图矢量地图服务
                this.initTiandituVectorMap();

                // 默认仅自动定位；指定账号可拖拽标记
                // 初始化时添加标记（使用自动定位中心点）
                const initialPosition = this.center;
                this.addMarker(initialPosition);
                
                // 地图向上偏移，避免标记点被底部容器遮挡，并往北移动一点
                this.$nextTick(() => {
                    this.map.panBy([0, 150], { animate: false });
                });
                
                // 获取点位列表（使用中心点的经纬度）
                this.fetchLocationList(initialPosition[0], initialPosition[1]);
            }
        },
        
        
        // 恢复之前选中的列表项
        restoreSelectedLocation() {
            const relocatedLocationFromStore = this.$store.getters.getRelocatedLocation;
            if (!relocatedLocationFromStore) {
                return;
            }
            
            // 尝试根据保存的河道/水库信息恢复选中项
            let targetItem = null;
            
            if (relocatedLocationFromStore.riverChannelName) {
                // 查找河道
                targetItem = this.locationList.find(item => 
                    item.type === 'river' && item.name === relocatedLocationFromStore.riverChannelName
                );
            }
            
            if (!targetItem && relocatedLocationFromStore.riverSectionName) {
                // 查找河段
                targetItem = this.locationList.find(item => 
                    item.type === 'riverSection' && item.name === relocatedLocationFromStore.riverSectionName
                );
            }
            
            if (!targetItem && relocatedLocationFromStore.waterReservoirs && relocatedLocationFromStore.waterReservoirs.length > 0) {
                // 查找水库
                const reservoirName = relocatedLocationFromStore.waterReservoirs[0].reservoirName;
                targetItem = this.locationList.find(item => 
                    item.type === 'reservoir' && item.name === reservoirName
                );
            }
            
            if (targetItem) {
                const index = this.locationList.indexOf(targetItem);
                if (index !== -1) {
                    this.selectedIndex = index;
                    this.selectedLocationItem = targetItem;
                    console.log('恢复选中的列表项:', targetItem);
                }
            }
        },

        // 初始化天地图矢量地图服务
        initTiandituVectorMap() {
            // 创建天地图矢量底图图层
            this.tiandituVecLayer = this.createTiandituLayer(L, {
                layer: 'vec',
                attribution: '© 国家基础地理信息中心',
                maxZoom: 18,
                tileSize: 256
            });
            
            if (this.tiandituVecLayer) {
                this.tiandituVecLayer.addTo(this.map);
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
                this.tiandituCvaLayer.addTo(this.map);
                console.log('天地图矢量标注图层已添加');
            } else {
                console.error('天地图矢量标注图层创建失败');
            }
        },

        // 添加标记
        addMarker(position) {
            // 移除旧的标记
            if (this.marker) {
                this.map.removeLayer(this.marker);
            }

            // 创建新标记 - 使用自定义图标
            // 原图尺寸 100x100，缩小为 50x50
            const iconSize = [50, 50];
            // 锚点设置：针尖应该在图标底部中心
            // iconAnchor 定义图标上哪个点对应标记的地理位置
            // [x, y] 其中 x 是水平偏移（从左边缘），y 是垂直偏移（从上边缘）
            // 对于定位针图标，针尖通常在底部中心，所以锚点应该是 [width/2, height]
            // 确保锚点精确，让针尖始终指向准确的地理位置，不受缩放级别影响
            const iconAnchorX = iconSize[0] / 2; // 水平居中：25
            const iconAnchorY = iconSize[1]; // 垂直到底部：50（针尖位置）
            
            const icon = L.icon({
                iconUrl: resetPositionIcon,
                iconSize: iconSize, // 图标显示尺寸 50x50
                iconAnchor: [iconAnchorX, iconAnchorY], // 锚点精确设置在底部中心：针尖位置
                popupAnchor: [0, -iconAnchorY], // 弹出框位置调整：在图标上方
                // 确保图标在不同缩放级别下保持精确
                className: 'precise-marker-icon'
            });

            // 指定登录手机号允许拖拽，其余用户仅自动定位
            this.marker = L.marker(position, { 
                icon,
                draggable: this.allowMapDrag,
                riseOnHover: false,
                zIndexOffset: 1000
            }).addTo(this.map);

            if (this.allowMapDrag) {
                this.marker.on('dragend', (e) => {
                    const latlng = e.target.getLatLng();
                    const lat = latlng.lat;
                    const lng = latlng.lng;
                    this.selectedIndex = -1;
                    this.selectedLocationItem = null;
                    this.updateLocationInfo(lat, lng);
                    this.fetchLocationList(lat, lng, false);
                });
            }
            
            // 初始化时更新位置信息
            // 如果已经有默认地址信息，直接使用，否则调用逆地理编码
            if (this.defaultLocation && this.defaultLocation.address) {
                // 已经有地址信息，确保坐标信息正确（处理省市信息）
                const processedAddress = this.removeProvinceCity(this.defaultLocation.address);
                if (!this.locationInfo) {
                    this.locationInfo = {
                        coordinates: `${position[0].toFixed(6)}, ${position[1].toFixed(6)}`,
                        address: processedAddress
                    };
                } else {
                    // 更新坐标信息和地址信息（确保地址已处理）
                    this.locationInfo.coordinates = `${position[0].toFixed(6)}, ${position[1].toFixed(6)}`;
                    this.locationInfo.address = processedAddress;
                }
            } else {
                // 没有地址信息，调用逆地理编码获取
                this.updateLocationInfo(position[0], position[1]);
            }
        },

        // 更新位置信息
        async updateLocationInfo(lat, lng) {
            console.log('updateLocationInfo 被调用:', lat, lng);
            
            // 更新坐标显示
            this.locationInfo = {
                coordinates: `${lat.toFixed(6)}, ${lng.toFixed(6)}`,
                address: '正在获取...'
            };

            // 通过坐标获取地点名称（逆地理编码）
            try {
                const originalAddress = await this.getAddressByCoordinate(lat, lng);
                console.log('获取到的原始地址:', originalAddress);
                
                if (this.locationInfo) {
                    // 立即处理地址，去除省市信息
                    const processedAddress = originalAddress && originalAddress !== '未知位置' && originalAddress !== '正在获取...'
                        ? this.removeProvinceCity(originalAddress)
                        : (originalAddress || '未知位置');
                    
                    console.log('处理后的地址:', processedAddress);
                    this.locationInfo.address = processedAddress;
                }
            } catch (error) {
                console.error('获取地点名称失败:', error);
                if (this.locationInfo) {
                    this.locationInfo.address = '获取位置失败';
                }
            }
        },

        // GCJ-02 转 WGS84 坐标转换函数（反向转换）
        // 微信 SDK 返回 GCJ-02 坐标，天地图使用 WGS84 坐标系
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
        // 高德地图使用 GCJ-02 坐标系，需要将 WGS84 坐标转换为 GCJ-02
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

        // 通过坐标获取地点名称（逆地理编码）- 使用高德地图API
        async getAddressByCoordinate(lat, lng) {
            try {
                // 从配置文件获取高德地图Key
                const amapKey = window.getConfig('amapKey');
                console.log('高德Key:', amapKey);
                
                if (!amapKey) {
                    console.warn('高德Key未配置，使用默认地址');
                    return `仪征市 (${lat.toFixed(6)}, ${lng.toFixed(6)})`;
                }
                
                // 天地图使用 WGS84 坐标系，高德 API 需要 GCJ-02 坐标系
                // 需要将 WGS84 坐标转换为 GCJ-02 坐标
                const [gcjLng, gcjLat] = this.transformWGS84ToGCJ02(lng, lat);
                console.log(`坐标转换: WGS84(${lng.toFixed(6)}, ${lat.toFixed(6)}) -> GCJ-02(${gcjLng.toFixed(6)}, ${gcjLat.toFixed(6)})`);
                
                // location格式：经度,纬度，经纬度小数位数不超过6位
                const location = `${gcjLng.toFixed(6)},${gcjLat.toFixed(6)}`;
                const url = `https://restapi.amap.com/v3/geocode/regeo?key=${amapKey}&location=${location}&output=json`;
                
                console.log('调用高德API:', url);
                
                const response = await fetch(url);
                const data = await response.json();
                
                console.log('高德API返回:', data);
                
                // 解析高德地图返回的地址信息
                if (data && data.status === '1' && data.regeocode && data.regeocode.formatted_address) {
                    return data.regeocode.formatted_address;
                }
                
                // 如果API返回格式不符合预期，返回默认信息
                console.warn('高德API返回格式异常:', data);
                return `仪征市 (${lat.toFixed(6)}, ${lng.toFixed(6)})`;
            } catch (error) {
                console.error('逆地理编码失败:', error);
                // 如果API调用失败，返回坐标信息
                return `仪征市 (${lat.toFixed(6)}, ${lng.toFixed(6)})`;
            }
        },

        // 销毁地图
        destroyMap() {
            if (this.marker) {
                this.map.removeLayer(this.marker);
                this.marker = null;
            }
            if (this.tiandituCvaLayer) {
                this.map.removeLayer(this.tiandituCvaLayer);
                this.tiandituCvaLayer = null;
            }
            if (this.tiandituVecLayer) {
                this.map.removeLayer(this.tiandituVecLayer);
                this.tiandituVecLayer = null;
            }
            if (this.annotationLayer) {
                this.map.removeLayer(this.annotationLayer);
                this.annotationLayer = null;
            }
            if (this.baseLayer) {
                this.map.removeLayer(this.baseLayer);
                this.baseLayer = null;
            }
            if (this.map) {
                this.map.remove();
                this.map = null;
            }
        },

        // 关闭页面
        handleClose() {
            // 使用路由返回到 UserFeedBack 页面
            this.$router.push({ name: 'UserFeedBack' });
        },
        
        // 去除地址中的省市信息，只保留区县及以下信息
        removeProvinceCity(address) {
            if (!address || typeof address !== 'string') {
                return address;
            }
            
            // 使用正则表达式去除省市信息
            // 匹配模式：省名+省、市名+市（例如：江苏省、扬州市、仪征市）
            // 去除这些前缀，保留后面的内容
            let processedAddress = address.trim();
            
            // 循环去除所有省和市信息，直到没有匹配为止
            let previousLength = processedAddress.length;
            let maxIterations = 10; // 防止无限循环
            let iterations = 0;
            
            while (iterations < maxIterations) {
                const beforeReplace = processedAddress;
                
                // 去除省名+省（例如：江苏省、浙江省、安徽省等）
                // 匹配：从开头开始，一个或多个非"省"字符，然后跟着"省"字
                processedAddress = processedAddress.replace(/^[^省]+省/, '');
                
                // 去除市名+市（例如：扬州市、南京市、仪征市等）
                // 匹配：从开头开始，一个或多个非"市"字符，然后跟着"市"字
                processedAddress = processedAddress.replace(/^[^市]+市/, '');
                
                // 去除开头可能的空格、标点或多余的分隔符
                processedAddress = processedAddress.replace(/^[\s,，、]+/, '').trim();
                
                // 如果处理后没有变化，说明已经没有省市信息了，退出循环
                if (processedAddress === beforeReplace || processedAddress.length === previousLength) {
                    break;
                }
                
                previousLength = processedAddress.length;
                iterations++;
            }
            
            // 如果处理后为空，返回原地址
            return processedAddress || address;
        },
        
        // 处理搜索输入
        handleSearchInput(event) {
            // 搜索功能已通过 computed filteredLocationList 实现
            // 当 searchKeyword 变化时，filteredLocationList 会自动更新
        },
        
        // 获取原始索引（用于在过滤后的列表中保持正确的选中状态）
        getOriginalIndex(item) {
            return this.locationList.findIndex(loc => loc === item);
        },
        
        // 处理搜索框点击
        handleSearchClick(event) {
            // 如果列表收起，点击输入框时展开列表
            if (!this.isListExpanded) {
                this.isListExpanded = true;
            }
            event.stopPropagation();
        },
        
        // 处理搜索框聚焦
        handleSearchFocus(event) {
            console.log('搜索框聚焦');
            // 如果列表收起，聚焦时也展开列表
            if (!this.isListExpanded) {
                this.isListExpanded = true;
            }
            event.stopPropagation();
        },
        
        // 切换列表展开/收起
        toggleList() {
            this.isListExpanded = !this.isListExpanded;
        },
        
        // 从 geomWkt 中提取坐标点
        extractCoordinatesFromWkt(geomWkt) {
            if (!geomWkt) return null;
            
            try {
                // 处理 WKT 字符串，去除 SRID 前缀
                let wkt = geomWkt;
                if (wkt.includes(';')) {
                    wkt = wkt.split(';')[1];
                }
                
                // 将 WKT 转换为 GeoJSON
                const geojson = parseWkt(wkt);
                
                if (!geojson || !geojson.coordinates) {
                    return null;
                }
                
                // 根据几何类型提取坐标
                let coords = null;
                if (geojson.type === 'Point') {
                    // 点：直接使用坐标 [lng, lat]
                    coords = geojson.coordinates;
                } else if (geojson.type === 'LineString') {
                    // 线：使用第一个点 [lng, lat]
                    coords = geojson.coordinates[0];
                } else if (geojson.type === 'MultiLineString') {
                    // 多线：使用第一条线的第一个点
                    coords = geojson.coordinates[0][0];
                } else if (geojson.type === 'Polygon') {
                    // 面：使用外环的第一个点 [lng, lat]
                    coords = geojson.coordinates[0][0];
                } else if (geojson.type === 'MultiPolygon') {
                    // 多面：使用第一个面的外环第一个点
                    coords = geojson.coordinates[0][0][0];
                }
                
                if (coords && Array.isArray(coords) && coords.length >= 2) {
                    // GeoJSON 格式是 [lng, lat]，需要转换为 [lat, lng]
                    return {
                        lng: coords[0],
                        lat: coords[1]
                    };
                }
            } catch (error) {
                console.error('解析 geomWkt 失败:', error, geomWkt);
            }
            
            return null;
        },
        
        // 获取点位列表
        async fetchLocationList(latitude, longitude, shouldRestoreSelection = true) {
            if (this.isLoadingLocations) return;
            
            this.isLoadingLocations = true;
            try {
                const response = await this.$http.post('/app-api/problem/feedback/buffer-query', {
                    longitude: longitude,
                    latitude: latitude,
                    radiusM: this.getBufferQueryRadiusM()
                });
                
                if (response && response.data) {
                    const { rivers = [], riverSections = [], reservoirs = [] } = response.data;
                    
                    // 合并所有点位数据
                    const allLocations = [];
                    
                    // 处理河道数据
                    rivers.forEach(river => {
                        if (river.riverName) {
                            // 从 geomWkt 中提取坐标，如果没有则使用中心点
                            const coords = this.extractCoordinatesFromWkt(river.geomWkt);
                            allLocations.push({
                                name: river.riverName,
                                lat: coords ? coords.lat : latitude,
                                lng: coords ? coords.lng : longitude,
                                type: 'river',
                                id: river.id,
                                distanceM: river.distanceM || 0,
                                geomWkt: river.geomWkt,
                                geomType: river.geomType
                            });
                        }
                    });
                    
                    // 处理河段数据
                    riverSections.forEach(section => {
                        if (section.displayName || section.sectionName) {
                            // 从 geomWkt 中提取坐标，如果没有则使用中心点
                            const coords = this.extractCoordinatesFromWkt(section.geomWkt);
                            allLocations.push({
                                name: section.displayName || section.sectionName,
                                lat: coords ? coords.lat : latitude,
                                lng: coords ? coords.lng : longitude,
                                type: 'riverSection',
                                id: section.id,
                                riverChannelId: section.riverChannelId,
                                distanceM: section.distanceM || 0,
                                geomWkt: section.geomWkt,
                                geomType: section.geomType
                            });
                        }
                    });
                    
                    // 处理水库数据
                    reservoirs.forEach(reservoir => {
                        if (reservoir.reservoirName) {
                            // 从 geomWkt 中提取坐标，如果没有则使用中心点
                            const coords = this.extractCoordinatesFromWkt(reservoir.geomWkt);
                            allLocations.push({
                                name: reservoir.reservoirName,
                                lat: coords ? coords.lat : latitude,
                                lng: coords ? coords.lng : longitude,
                                type: 'reservoir',
                                id: reservoir.id,
                                distanceM: reservoir.distanceM || 0,
                                geomWkt: reservoir.geomWkt,
                                geomType: reservoir.geomType
                            });
                        }
                    });
                    
                    // 按距离排序（距离近的在前）
                    allLocations.sort((a, b) => (a.distanceM || 0) - (b.distanceM || 0));
                    
                    this.locationList = allLocations;
                    
                    // 只有在需要恢复选中状态时才执行（初始化时恢复）
                    if (shouldRestoreSelection) {
                        // 尝试恢复之前选中的列表项
                        this.restoreSelectedLocation();
                        
                        // 如果没有恢复选中项，且有数据，默认选中第一个
                        if (this.locationList.length > 0 && this.selectedIndex === -1) {
                            this.selectedIndex = 0;
                            this.selectedLocationItem = this.locationList[0];
                        }
                    }

                    if (allLocations.length === 0) {
                        this.selectedIndex = -1;
                        this.selectedLocationItem = null;
                        this.showMessage('当前位置没有河道/水库', 'error');
                    }
                }
            } catch (error) {
                console.error('获取点位列表失败:', error);
                // 失败时使用空列表
                this.locationList = [];
                this.selectedIndex = -1;
                this.selectedLocationItem = null;
                this.showMessage('当前位置没有河道/水库', 'error');
            } finally {
                this.isLoadingLocations = false;
            }
        },
        
        // 选择点位（只记录选中的点位信息，不移动标记点）
        handleLocationSelect(index, item) {
            // 获取原始索引
            const originalIndex = this.getOriginalIndex(item);
            this.selectedIndex = originalIndex;
            // 保存选中的点位信息（独立于标记点位置，仅用于收集值）
            this.selectedLocationItem = item;
            // 点击列表项仅选择河道/水库，不移动标记点
        },
        
        // 确认位置信息
        handleConfirm() {
            if (!this.locationInfo || !this.marker) {
                this.showMessage('请先选择位置', 'error');
                return;
            }
            
            // 如果没有选中列表项，但列表有数据，自动选中第一个
            if (!this.selectedLocationItem && this.locationList.length > 0) {
                this.selectedIndex = 0;
                this.selectedLocationItem = this.locationList[0];
            }
            
            // 如果列表为空或仍然没有选中项，给出提示
            if (!this.selectedLocationItem) {
                this.showMessage('当前位置没有河道/水库', 'error');
                return;
            }
            
            // 获取当前标记的坐标
            const position = this.marker.getLatLng();
            const lat = position.lat;
            const lng = position.lng;
            
            // locationInfo.address 已经是处理后的地址（在获取时已处理）
            const processedAddress = this.locationInfo.address || '未知位置';
            
            // 收集位置信息（标记点的经纬度）
            const locationData = {
                latitude: lat,
                longitude: lng,
                coordinates: `${lat.toFixed(6)}, ${lng.toFixed(6)}`,
                address: processedAddress, // 返回处理后的地址（用于提交和显示）
                // 用户从列表中选择的点位信息（独立值）
                selectedLocation: {
                    id: this.selectedLocationItem.id,
                    name: this.selectedLocationItem.name,
                    type: this.selectedLocationItem.type,
                    lat: this.selectedLocationItem.lat,
                    lng: this.selectedLocationItem.lng,
                    distance: this.selectedLocationItem.distanceM,
                    riverChannelId: this.selectedLocationItem.riverChannelId
                }
            };
            
            // 构建河道/水库信息结构
            let locationInfo = {
                longitude: lng,
                latitude: lat,
                specificLocation: processedAddress !== '未知位置' ? processedAddress : locationData.coordinates
            };
            
            // 根据选中的点位信息，自动填充河道/水库名称和关联信息
            const selected = this.selectedLocationItem;
            
            // 根据数据来源（rivers/riverSections/reservoirs）自动确定 referenceType
            if (selected.type === 'river') {
                // 河道 - 来自 rivers 数据
                locationInfo.riverChannelName = selected.name;
                locationInfo.referenceId = selected.id;
                locationInfo.referenceType = 'river';
            } else if (selected.type === 'riverSection') {
                // 河段 - 来自 riverSections 数据
                locationInfo.riverSectionName = selected.name;
                locationInfo.referenceId = selected.id;
                locationInfo.referenceType = 'river_section';
                // 如果有河道ID，尝试从列表中查找对应的河道名称
                if (selected.riverChannelId) {
                    // 从列表中查找对应的河道（通过 riverChannelId 匹配）
                    const river = this.locationList.find(item => 
                        item.type === 'river' && item.id === selected.riverChannelId
                    );
                    if (river) {
                        locationInfo.riverChannelName = river.name;
                    }
                }
            } else if (selected.type === 'reservoir') {
                // 水库 - 来自 reservoirs 数据
                locationInfo.waterReservoirs = [{
                    reservoirName: selected.name
                }];
                locationInfo.referenceId = selected.id;
                locationInfo.referenceType = 'reservoir';
            } else {
                // 未知类型，给出错误提示
                console.error('未知的点位类型:', selected.type);
                this.showMessage('点位类型不正确，请重新选择', 'error');
                return;
            }
            
            console.log('保存到 Vuex 的位置信息:', locationInfo);
            console.log('选中的点位信息:', this.selectedLocationItem);
            console.log('referenceId:', locationInfo.referenceId);
            console.log('referenceType:', locationInfo.referenceType);
            
            // 保存到 Vuex
            this.$store.dispatch('updateRelocatedLocation', locationInfo);
            
            // 使用路由返回，而不是 emit 事件
            this.$router.push({ name: 'UserFeedBack' });
        },
        
        // 显示消息提示
        showMessage(message, type = 'info') {
            const messageEl = document.createElement('div');
            messageEl.className = `simple-message simple-message-${type}`;
            messageEl.textContent = message;
            document.body.appendChild(messageEl);

            setTimeout(() => {
                messageEl.classList.add('show');
            }, 10);

            setTimeout(() => {
                messageEl.classList.remove('show');
                setTimeout(() => {
                    if (document.body.contains(messageEl)) {
                        document.body.removeChild(messageEl);
                    }
                }, 300);
            }, 2000);
        }
    }
};
</script>

<style lang="scss" scoped>
.feedBack-map-container {
    position: relative;
    width: 100%;
    height: 100vh;
    overflow: hidden;
    background-color: #f5f5f5;

    .content-layer {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;

        .map-wrapper {
            flex: 1;
            position: relative;
            width: 100%;
            height: 100%;

            #map-container {
                width: 100%;
                height: 100%;
                
                // 移除所有滤镜，确保地图和注记正常显示
                :deep(.leaflet-tile-pane img.leaflet-tile),
                :deep(.leaflet-tile-pane .leaflet-tile-container img),
                :deep(.leaflet-overlay-pane),
                :deep(.leaflet-marker-pane),
                :deep(.leaflet-shadow-pane),
                :deep(.leaflet-popup-pane),
                :deep(.leaflet-tooltip-pane) {
                    filter: none !important;
                    -webkit-filter: none !important;
                }
            }

            // 位置信息显示
            .location-info {
                position: absolute;
                left: 50%;
                transform: translateX(-50%);
                width: calc(100% - 48px);
                max-width: 702px;
                background-color: #ffffff;
                border-radius: 8px;
                padding: 20px 24px;
                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                z-index: 1001; // 确保在列表上方
                box-sizing: border-box;
                transition: bottom 0.3s ease;
                
                // 列表展开时，显示在列表上方
                &.list-expanded {
                    bottom: 650px; // 504px(列表高度) + 130px(列表底部位置) + 16px(间距)
                }
                
                // 列表收起时，显示在列表头部上方（避免遮挡展开按钮）
                &:not(.list-expanded) {
                    bottom: 270px; // 130px(列表底部位置) + 120px(列表头部高度) + 20px(间距)
                }
                
                // 如果有底部返回按钮，需要往上移动
                &.has-bottom-nav.list-expanded {
                    bottom: 720px !important; // 650px + 70px (NavigationBar 高度约 70px)
                }
                
                &.has-bottom-nav:not(.list-expanded) {
                    bottom: 340px !important; // 270px + 70px (NavigationBar 高度约 70px)
                }

                .location-item {
                    display: flex;
                    align-items: center;
                    margin-bottom: 12px;
                    font-size: 28px;

                    &:last-child {
                        margin-bottom: 0;
                    }

                    .location-label {
                        color: #666666;
                        font-weight: 400;
                        margin-right: 16px;
                        min-width: 60px;
                    }

                    .location-value {
                        color: #3d3d3d;
                        font-weight: 500;
                        flex: 1;
                        word-break: break-all;
                    }
                }
            }
            
            // 点位信息列表容器
            .location-list-container {
                position: absolute;
                bottom: 130px; // 在确认按钮上方
                left: 0;
                right: 0;
                width: 100%;
                background: #ffffff;
                z-index: 1000;
                display: flex;
                flex-direction: column;
                box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
                transition: height 0.3s ease;
                overflow: hidden;
                
                // 展开状态
                &.expanded {
                    height: 504px;
                }
                
                // 收起状态（只显示头部）
                &:not(.expanded) {
                    height: auto;
                    min-height: 120px; // 头部高度
                }
                
                // 如果有底部返回按钮，需要往上移动
                &.has-bottom-nav {
                    bottom: 190px !important; // 120px + 70px (NavigationBar 高度约 70px)
                }
                
                .location-list-header {
                    padding: 24px;
                    display: flex;
                    align-items: center;
                    gap: 16px;
                    // border-bottom: 1px solid #f0f0f0;
                    
                    .search-box {
                        flex: 1;
                        display: flex;
                        align-items: center;
                        padding: 16px 20px;
                        background: #f8f8f8;
                        border-radius: 8px;
                        position: relative;
                        cursor: text;
                        
                        .search-icon {
                            width: 28px;
                            height: 28px;
                            margin-right: 12px;
                            flex-shrink: 0;
                            color: #999999;
                            pointer-events: none;
                        }
                        
                        .search-input {
                            flex: 1;
                            border: none;
                            outline: none;
                            background: transparent;
                            font-size: 28px;
                            color: #3d3d3d;
                            min-width: 0;
                            width: 100%;
                            cursor: text;
                            
                            &::placeholder {
                                color: #999999;
                            }
                            
                            &:focus {
                                outline: none;
                            }
                        }
                    }
                    
                    .toggle-btn {
                        width: 48px;
                        height: 48px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        background: #f8f8f8;
                        border: none;
                        border-radius: 8px;
                        cursor: pointer;
                        flex-shrink: 0;
                        color: #666666;
                        transition: all 0.3s;
                        
                        svg {
                            width: 24px;
                            height: 24px;
                        }
                        
                        &:active {
                            background: #e8e8e8;
                            opacity: 0.8;
                        }
                    }
                }
                
                .location-list {
                    flex: 1;
                    overflow-y: auto;
                    -webkit-overflow-scrolling: touch;
                    padding: 0 24px;
                    
                    .loading-tip,
                    .empty-tip {
                        padding: 40px 24px;
                        text-align: center;
                        color: #999999;
                        font-size: 28px;
                    }
                    
                    .location-list-item {
                        height: 97px;
                        display: flex;
                        align-items: center;
                        padding: 0;
                        border-bottom: 1px solid #f0f0f0;
                        cursor: pointer;
                        transition: background-color 0.2s;
                        
                        &:active {
                            background-color: #f8f8f8;
                        }
                        
                        .nearest-badge {
                            display: inline-block;
                            padding: 4px 12px;
                            background: #349dff;
                            color: #ffffff;
                            font-size: 20px;
                            font-weight: 400;
                            border-radius: 4px;
                            margin-right: 12px;
                            flex-shrink: 0;
                            white-space: nowrap;
                        }
                        
                        .location-icon {
                            width: 32px;
                            height: 32px;
                            margin-right: 16px;
                            flex-shrink: 0;
                        }
                        
                        .location-name {
                            flex: 1;
                            font-size: 28px;
                            color: #3d3d3d;
                            font-weight: 400;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            white-space: nowrap;
                            margin-right: 16px;
                        }
                        
                        .location-distance {
                            font-size: 24px;
                            color: #999999;
                            font-weight: 400;
                            text-align: right;
                            min-width: 80px;
                            flex-shrink: 0;
                        }
                        
                        .check-icon-wrapper {
                            width: 32px;
                            height: 32px;
                            margin-left: 16px;
                            flex-shrink: 0;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        
                        .check-icon {
                            width: 32px;
                            height: 32px;
                        }
                    }
                }
            }
            
            // 底部确认按钮区域 - 绝对定位覆盖在地图上
            .bottom-action-bar {
                position: absolute;
                bottom: 0;
                left: 0;
                right: 0;
                width: 100%;
                background-color: #ffffff;
                padding: 24px;
                box-sizing: border-box;
                display: flex;
                justify-content: center;
                align-items: center;
                box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
                z-index: 1000;
                
                // 如果有底部返回按钮，需要往上移动
                &.has-bottom-nav {
                    bottom: 70px !important; // NavigationBar 高度约 70px（包括安全区域）
                }
                
                .confirm-btn {
                    width: 702px;
                    height: 96px;
                    border-radius: 6px;
                    background: #349dff;
                    border: none;
                    color: #ffffff;
                    font-size: 32px;
                    font-weight: 500;
                    cursor: pointer;
                    transition: opacity 0.3s;
                    
                    &:active {
                        opacity: 0.8;
                    }
                    
                    &:disabled {
                        background: #cccccc;
                        cursor: not-allowed;
                    }
                }
            }
        }
        
        // 确保标记图标在不同缩放级别下位置精确
        ::v-deep .precise-marker-icon {
            // 确保图标位置精确，不受缩放影响
            transform-origin: center bottom;
            // 防止图标在不同缩放级别下出现偏移
            image-rendering: -webkit-optimize-contrast;
            image-rendering: crisp-edges;
            // 确保锚点精确对应
            position: relative;
        }
        
        // 确保 Leaflet 默认的标记图标样式不影响精确度
        ::v-deep .leaflet-marker-icon {
            // 确保标记位置精确
            transform-origin: center bottom;
        }
    }
}
</style>

<style lang="scss">
// 全局消息提示样式
.simple-message {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%) scale(0.8);
    padding: 20px 32px;
    background-color: rgba(0, 0, 0, 0.8);
    color: #fff;
    border-radius: 8px;
    font-size: 28px;
    z-index: 9999;
    opacity: 0;
    transition: all 0.3s ease;
    pointer-events: none;
    max-width: 80%;
    text-align: center;
    
    &.show {
        opacity: 1;
        transform: translate(-50%, -50%) scale(1);
    }
    
    &.simple-message-success {
        background-color: rgba(76, 175, 80, 0.9);
    }
    
    &.simple-message-error {
        background-color: rgba(244, 67, 54, 0.9);
    }
    
    &.simple-message-info {
        background-color: rgba(33, 150, 243, 0.9);
    }
    
    &.simple-message-warning {
        background-color: rgba(255, 152, 0, 0.9);
    }
}
</style>
