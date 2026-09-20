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
import tiandituMixin from '@/mixins/tiandituMixin'

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
            geoJsonLayer: null, // GeoJSON 数据图层
            center: [39.9042, 116.4074], // 默认北京天安门
            zoom: 13
        }
    },

    computed:{

     
    },
    async mounted() {
        // 获取对应坐标信息
        this.initMap()
        const locationInfo = await this.getLocationInfo()

        if (locationInfo) {
            // 使用 flyTo 方法实现平滑的飞行动画效果
            this.map.flyTo([locationInfo.latitude, locationInfo.longitude], this.zoom, {
                duration: 1, // 动画持续时间（秒）
                easeLinearity: 0.25 // 缓动线性度，值越小动画越平滑
            })
        }
    },
    beforeUnmount() {
        this.destroyMap()
    },
    methods: {
        // 初始化地图
        initMap() {
            if (this.$refs.mapContainer) {
                // 初始化地图，禁用版权信息控件，启用 Canvas 渲染（polycolor 需要）
                this.map = L.map(this.$refs.mapContainer, {
                    attributionControl: false,
                    preferCanvas: true // polycolor 插件需要 Canvas 渲染模式
                }).setView(this.center, this.zoom)

                // 创建并添加矢量底图
                this.baseLayer = this.createTiandituLayer(L, {
                    layer: 'vec',
                    attribution: '',
                    maxZoom: 18,
                    tileSize: 256
                })

                if (this.baseLayer) {
                    this.baseLayer.addTo(this.map)
                }

                // 加载 GeoJSON 数据
                this.loadGeoJsonData()
            }
        },

        // 加载 GeoJSON 数据
        async loadGeoJsonData() {
         
        },

        // 获取对应坐标信息
        async getLocationInfo() {
       
        },
        
        // 销毁地图
        destroyMap() {
            // 移除 GeoJSON 图层
            if (this.geoJsonLayer) {
                this.map.removeLayer(this.geoJsonLayer)
                this.geoJsonLayer = null
            }
            
            if (this.map) {
                this.map.remove()
                this.map = null
            }
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
    
    // 只对底图瓦片应用滤镜，不影响其他图层（如面数据、标记等）
    // 底图瓦片在 .leaflet-tile-pane 中，其他图层在 .leaflet-overlay-pane 等中
    :deep(.leaflet-tile-pane img.leaflet-tile) {
        filter: grayscale(100%) brightness(1.15) contrast(0.9);
        -webkit-filter: grayscale(100%) brightness(1.15) contrast(0.9);
    }
    
    // 确保底图瓦片容器中的所有图片都应用滤镜
    :deep(.leaflet-tile-pane .leaflet-tile-container img) {
        filter: grayscale(100%) brightness(1.15) contrast(0.9);
        -webkit-filter: grayscale(100%) brightness(1.15) contrast(0.9);
    }
    
    // 明确排除其他图层，确保不受滤镜影响
    :deep(.leaflet-overlay-pane),
    :deep(.leaflet-marker-pane),
    :deep(.leaflet-shadow-pane),
    :deep(.leaflet-popup-pane),
    :deep(.leaflet-tooltip-pane) {
        filter: none !important;
        -webkit-filter: none !important;
    }
}
</style>

