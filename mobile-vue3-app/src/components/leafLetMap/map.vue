<template>
    <div class="map-page">
        <div class="map-wrapper">
            <div id="map-container" ref="mapContainer"></div>
        </div>
    </div>
</template>

<script>
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import leafletPolycolor from 'leaflet-polycolor'
import { parse as parseWkt } from 'wellknown'
import tiandituMixin from '@/mixins/tiandituMixin'
import publicNoticeBoard from '@/assets/img/publicNoticeBoard.png';
import reservoirBoard from '@/assets/img/reservoirBorad.png';
import singleIcon1 from '@/assets/img/singleIcon1.png';
import singleIcon2 from '@/assets/img/singleIcon2.png';

// 确保 global 对象存在（浏览器环境兼容）
if (typeof global === 'undefined') {
    window.global = window
}

// 初始化 leaflet-polycolor 插件
leafletPolycolor(L)

export default {
    name: 'Map',
    mixins: [tiandituMixin],
    data() {
        return {
            map: null,
            baseLayer: null, // 底图图层
            wmsLayer: null, // WMS 图层
            geoJsonLayer: null, // GeoJSON 数据图层
            riverLayers: [], // 存储河道面图层
            reservoirLayers: [], // 存储水库面图层
            center: [39.9042, 116.4074], // 默认北京天安门
            zoom: 13,
            mapData: null,

        }
    },

    computed: {
        //获取Qrcode
        getQrcode() {
            return this.$store.state.qrcode
        }


    },
    async mounted() {
        // 获取对应坐标信息
        await this.initMap();

        this.mapData = await this.getBaseData();

        this.handleData();



        //     if (locationInfo) {
        //         // 使用 flyTo 方法实现平滑的飞行动画效果
        //         this.map.flyTo([locationInfo.latitude, locationInfo.longitude], this.zoom, {
        //             duration: 1, // 动画持续时间（秒）
        //             easeLinearity: 0.25 // 缓动线性度，值越小动画越平滑
        //         })
        //     }
    },
    beforeUnmount() {
        this.destroyMap()
    },
    methods: {
        // 初始化地图
        async initMap() {
            if (this.$refs.mapContainer) {
                // 初始化地图，禁用版权信息控件和缩放控件，启用 Canvas 渲染
                this.map = L.map(this.$refs.mapContainer, {
                    attributionControl: false,
                    zoomControl: false, // 禁用放大缩小按钮
                    preferCanvas: true, // polycolor 插件需要 Canvas 渲染模式
                    tap: false, // 禁用 Leaflet 的 tap 处理器，防止移动端双击/抖动
                    bounceAtZoomLimits: false // 到达缩放极限时不反弹，增加平稳感
                }).setView(this.center, this.zoom)

                // 使用自定义(瓦片)服务
                const baseUrl = window.getConfig('api.baseUrl') || 'https://yzriver.sy-digit.com'
                const tileServicePath = window.getConfig('geoServe.tileService') || '/static/map/{z}/{x}/{y}.png'
                const tileServiceUrl = `${baseUrl}${tileServicePath}`
                this.baseLayer = L.tileLayer(tileServiceUrl, {
                    attribution: '',
                    maxZoom: 18,
                    tileSize: 256,
                    zoomOffset: 0
                })
                this.baseLayer.addTo(this.map)

                // 添加 WMS (河道)图层
                const wmsConfig = window.getConfig('geoServe.wmsService') || {
                    path: '/geoserver/wms',
                    layers: 'yzriver:yz_chief_river_pure2',
                    format: 'image/png',
                    transparent: true,
                    version: '1.1.0',
                    attribution: ''
                }
                const wmsServiceUrl = `${baseUrl}${wmsConfig.path}`
                this.wmsLayer = L.tileLayer.wms(wmsServiceUrl, {
                    layers: wmsConfig.layers,
                    format: wmsConfig.format,
                    transparent: wmsConfig.transparent,
                    version: wmsConfig.version,
                    attribution: wmsConfig.attribution
                })
                this.wmsLayer.addTo(this.map)

                // 确保地图正确显示
                this.$nextTick(() => {
                    if (this.map) {
                        this.map.invalidateSize()
                    }
                })
            }
        },

        //获取基础数据
        async getBaseData() {
            const response = await this.$http.get(`/app-api/signboard/info/by-qr/${this.getQrcode}`);
            return response.data;
        },

        //处理数据源 - 根据 waterReservoirId 判断类型
        handleData() {
            if (!this.mapData) return;

            // 根据 waterReservoirId 字段判断类型
            const { waterReservoirId } = this.mapData;

            if (waterReservoirId) {
                // 有 waterReservoirId，走水库逻辑
                this.handleReservoirData();
            } else {
                // 没有 waterReservoirId，走河道逻辑
                this.handleRiverData();
            }
        },

        // 处理河道数据
        handleRiverData() {
            // 1. 获取公示牌数据
            const { longitude, latitude, riverChannelName, signboardName, rivers } = this.mapData;

            // 2. 加载河道面数据
            if (rivers && rivers.length > 0) {
                this.loadRiverChannels(rivers);
            }

            // 3. 加载公示牌并触发初始飞行
            if (longitude && latitude) {
                // 第四个参数设为 true，强制执行初始 flyTo 动画
                this.addPublicBoard([latitude, longitude], riverChannelName, signboardName, true);
            }
        },

        // 处理水库数据
        handleReservoirData() {
            // 1. 获取公示牌数据
            const { longitude, latitude, reservoirName, signboardName, waterReservoirs } = this.mapData;

            // 2. 加载水库面数据 - 使用 waterReservoirs 数组
            if (waterReservoirs && waterReservoirs.length > 0) {
                this.loadReservoirChannels(waterReservoirs);
            }

            // 3. 加载公示牌并触发初始飞行
            if (longitude && latitude) {
                // 第四个参数设为 true，强制执行初始 flyTo 动画
                // 第五个参数设为 'reservoir'，使用水库图标
                // 水库使用 reservoirName 作为名称
                this.addPublicBoard([latitude, longitude], reservoirName, signboardName, true, 'reservoir');
            }
        },

        /**
         * 加载河道面数据 (WKT 格式)
         * @param {Array} rivers - 接口返回的 rivers 数组
         */
        loadRiverChannels(rivers) {
            // 清除旧的河道图层
            this.riverLayers.forEach(layer => this.map.removeLayer(layer));
            this.riverLayers = [];

            rivers.forEach(river => {
                const channel = river.channel;
                if (channel && channel.geomWkt) {
                    try {
                        // 1. 处理 WKT 字符串，去除 SRID 前缀 (例如 "SRID=4490;")
                        let wkt = channel.geomWkt;
                        if (wkt.includes(';')) {
                            wkt = wkt.split(';')[1];
                        }

                        // 2. 将 WKT 转换为 GeoJSON
                        const geojson = parseWkt(wkt);

                        if (geojson) {
                            // 3. 创建 Leaflet 图层并添加到地图
                            const layer = L.geoJSON(geojson, {
                                style: {
                                    color: '#349DFF',      // 边界颜色
                                    weight: 2,
                                    fillColor: '#349DFF',  // 填充颜色
                                    fillOpacity: 0.6       // 填充透明度
                                }
                            }).addTo(this.map);

                            this.riverLayers.push(layer);
                        }
                    } catch (error) {
                        console.error('解析河道 WKT 数据失败:', error, channel.geomWkt);
                    }
                }
            });

            // 如果有河道数据，自动缩放以包含所有河道
            if (this.riverLayers.length > 0) {
                const group = new L.FeatureGroup(this.riverLayers);
                // 使用 animate: false 防止与后续的动作冲突
                this.map.fitBounds(group.getBounds(), { padding: [50, 50], animate: false });
            }
        },

        /**
         * 加载水库面数据 (WKT 格式)
         * 使用 waterReservoirs 数组第一个对象的 geomWkt 数据
         * @param {Array} waterReservoirs - 接口返回的 waterReservoirs 数组
         */
        loadReservoirChannels(waterReservoirs) {
            // 清除旧的水库图层
            this.reservoirLayers.forEach(layer => this.map.removeLayer(layer));
            this.reservoirLayers = [];

            // 只使用数组的第一个对象
            if (waterReservoirs.length > 0) {
                const firstReservoir = waterReservoirs[0];
                
                // 获取第一个对象的 geomWkt 数据
                if (firstReservoir && firstReservoir.geomWkt) {
                    try {
                        // 1. 处理 WKT 字符串，去除 SRID 前缀 (例如 "SRID=4490;")
                        let wkt = firstReservoir.geomWkt;
                        if (wkt.includes(';')) {
                            wkt = wkt.split(';')[1];
                        }

                        // 2. 将 WKT 转换为 GeoJSON
                        const geojson = parseWkt(wkt);

                        if (geojson) {
                            // 3. 创建 Leaflet 图层并添加到地图
                            // 水库使用不同的颜色区分（例如使用绿色系）
                            const layer = L.geoJSON(geojson, {
                                style: {
                                    color: '#52C41A',      // 边界颜色（绿色）
                                    weight: 2,
                                    fillColor: '#52C41A',  // 填充颜色（绿色）
                                    fillOpacity: 0.6       // 填充透明度
                                }
                            }).addTo(this.map);

                            this.reservoirLayers.push(layer);

                            // 自动缩放以包含水库范围
                            const group = new L.FeatureGroup(this.reservoirLayers);
                            // 使用 animate: false 防止与后续的动作冲突
                            this.map.fitBounds(group.getBounds(), { padding: [50, 50], animate: false });
                        }
                    } catch (error) {
                        console.error('解析水库 WKT 数据失败:', error, firstReservoir.geomWkt);
                    }
                }
            }
        },

        /**
         * 封装：添加自定义公示牌标记
         * @param {Array} latlng - [lat, lng] 坐标
         * @param {String} riverName - 河道/水库名称
         * @param {String} boardName - 公示牌原始名称
         * @param {Boolean} shouldFly - 是否在初始化时飞向该位置
         * @param {String} iconType - 图标类型：'river' 河道 或 'reservoir' 水库，默认为河道
         */
        addPublicBoard(latlng, riverName, boardName, shouldFly = false, iconType = 'river') {
            // 去除名称中的"公示牌"字样
            let signboardName = boardName || ''
            if (signboardName) {
                signboardName = signboardName.replace(/公示牌/g, '')
            }

            // 根据 referenceType 选择底部图标
            const bottomIcon = iconType === 'reservoir' ? singleIcon2 : singleIcon1

            // 1. 创建自定义 HTML 图标 (L.divIcon) - 上下结构，宽度自适应
            const customDivIcon = L.divIcon({
                className: 'custom-signboard-container',
                html: `
                    <div class="signboard-icon-wrapper">
                        <div class="signboard-top-block">
                            <div class="top-content-wrapper">
                                <div class="top-name">${signboardName}</div>
                            </div>
                        </div>
                        <div class="signboard-bottom-block">
                            <img src="${bottomIcon}" alt="图标" class="signboard-icon" />
                        </div>
                    </div>
                `,
                iconSize: [100, 110], // 宽度自适应，高度为顶部+底部(30+80=110)
                iconAnchor: [50, 55], // 锚点在底部中心
            });

            // 2. 添加标记到地图
            const marker = L.marker(latlng, { icon: customDivIcon }).addTo(this.map);

            // 如果是水库，添加 tooltip 显示 signboardName
            // if (iconType === 'reservoir' && boardName) {
            //     marker.bindTooltip(boardName, {
            //         permanent: false,
            //         direction: 'top',
            //         offset: [0, -10],
            //         className: 'reservoir-signboard-tooltip'
            //     });
            // }

            // 3. 监听点击事件
            marker.on('click', (e) => {
                // 阻止事件冒泡，防止触发地图的点击事件
                if (e.originalEvent) {
                    e.originalEvent.stopPropagation();
                    e.originalEvent.preventDefault();
                }

                // 停止当前地图正在进行的任何动画，防止冲突抖动
                this.map.stop();

                const currentZoom = this.map.getZoom();
                const currentCenter = this.map.getCenter();
                const targetLatLng = L.latLng(latlng);
                const distance = currentCenter.distanceTo(targetLatLng);

                // 如果已经在目标位置附近且缩放一致，则直接打开抽屉，不再触发移动
                if (distance < 5 && currentZoom === 16) {
                    this.$emit('open-drawer');
                    return;
                }

                // 如果距离非常近且缩放级别已经较高，使用 panTo 代替 flyTo 以减少“俯冲”带来的抖动感
                if (distance < 200 && currentZoom >= 14) {
                    this.map.panTo(latlng, {
                        animate: true,
                        duration: 0.5
                    });
                } else {
                    // 距离较远时使用 flyTo
                    this.map.flyTo(latlng, 16, {
                        duration: 1,
                        easeLinearity: 0.5 // 提高线性度，使动画末尾更稳定
                    });
                }

                // 监听地图移动结束事件（一次性监听）
                this.map.once('moveend', () => {
                    this.$emit('open-drawer');
                });
            });

            // 4. 初始化时强制执行一次飞向该位置的动画
            if (shouldFly) {
                this.map.flyTo(latlng, 16, { // 飞向目标点位，缩放级别设为 16
                    duration: 2, // 增加时长以放缓速度
                    easeLinearity: 0.25
                });
                // 监听地图移动结束事件，在动画结束后自动打开抽屉
                this.map.once('moveend', () => {
                    this.$emit('open-drawer');
                });
            }
        },

        //生成一个专门加载点的方法
        addPoint(latlng, color) {
            L.circleMarker(latlng, {
                radius: 8,
                fillColor: '#ffffff',
                fillOpacity: 1,
                color: '#ffffff',
                weight: 0,
                pane: 'markerPane'
            }).addTo(this.map);
        },

        // 添加带同心圆的标记
        addCircleMarker(latlng, color) {
            // 外层白色圆圈
            L.circleMarker(latlng, {
                radius: 8,
                fillColor: '#ffffff',
                fillOpacity: 1,
                color: '#ffffff',
                weight: 0,
                pane: 'markerPane'
            }).addTo(this.map);

            // 内层同心圆（颜色与线段一致）
            L.circleMarker(latlng, {
                radius: 4,
                fillColor: color,
                fillOpacity: 1,
                stroke: false,
                pane: 'markerPane'
            }).addTo(this.map);
        },

        // 销毁地图
        destroyMap() {
            // 移除河道图层
            if (this.riverLayers && this.riverLayers.length > 0) {
                this.riverLayers.forEach(layer => this.map.removeLayer(layer));
                this.riverLayers = [];
            }

            // 移除水库图层
            if (this.reservoirLayers && this.reservoirLayers.length > 0) {
                this.reservoirLayers.forEach(layer => this.map.removeLayer(layer));
                this.reservoirLayers = [];
            }

            // 移除 GeoJSON 图层
            if (this.geoJsonLayer) {
                this.map.removeLayer(this.geoJsonLayer)
                this.geoJsonLayer = null
            }

            // 移除 WMS 图层
            if (this.wmsLayer && this.map) {
                this.map.removeLayer(this.wmsLayer)
                this.wmsLayer = null
            }

            if (this.map) {
                this.map.remove()
                this.map = null
            }
            this.baseLayer = null
        }

    }

}
</script>

<style lang="scss" scoped>
.map-page {
    width: 100%;
    height: 100vh;
    display: flex;
    flex-direction: column;
}

.map-wrapper {
    flex: 1;
    position: relative;
    width: 100%;
    height: 100%;
    min-height: 800px;
}

#map-container {
    width: 100%;
    height: 100%;
    min-height: 800px;

    // 自定义公示牌图标样式（与 adminMap.vue 保持一致）
    :deep(.custom-signboard-container) {
        background: none !important;
        border: none !important;

        .signboard-icon-wrapper {
            display: flex;
            flex-direction: column;
            width: fit-content; // 宽度根据内容撑开
            min-width: 200px; // 缩小最小宽度
            border-radius: 4px;
            overflow: visible; // 允许内容超出容器显示

            .signboard-top-block {
                width: fit-content; // 宽度根据内容自适应
                min-width: 200px; // 缩小最小宽度
                border: 1px solid #A5CFF8;
                background: #ffffff80;
                margin-bottom: 10px;
                display: flex;
                flex-direction: column;
                padding: 8px 12px;
                box-sizing: border-box;
                position: relative;

                .top-content-wrapper {
                    display: flex;
                    flex-direction: column;
                    width: 100%;
                    gap: 4px;
                }

                .top-name {
                    text-align: center;
                    font-size: 24px;
                    color: #3D3D3D;
                    font-weight: 500;
                    line-height: 1.2;
                    white-space: nowrap; // 不换行
                    overflow: hidden;
                    text-overflow: ellipsis;
                }
            }

            .signboard-bottom-block {
                width: 100%;
                height: 80px; // 底部容器高度
                display: flex;
                align-items: center;
                justify-content: center;
                position: relative;

                .signboard-icon {
                    width: 100px;
                    height: 150px;
                    object-fit: contain;
                }
            }
        }

        // 水库公示牌 tooltip 样式
        :deep(.reservoir-signboard-tooltip) {
            background-color: rgba(0, 0, 0, 0.8);
            color: #fff;
            border: none;
            border-radius: 4px;
            padding: 8px 12px;
            font-size: 24px;
            white-space: nowrap;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
        }
    }
}
</style>
