<template>
  <div class="common-map-container">
    <div id="common-map" ref="mapContainer"></div>
  </div>
</template>

<script>
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import tiandituMixin from '@/mixins/tiandituMixin'
import publicNoticeBoard from '@/assets/img/publicNoticeBoard.png'
import reservoirBoard from '@/assets/img/reservoirBorad.png'
import singleIcon1 from '@/assets/img/singleIcon1.png'
import singleIcon2 from '@/assets/img/singleIcon2.png'
import singleIcon3 from '@/assets/img/position3.png'

// 确保 global 对象存在（浏览器环境兼容）
if (typeof global === 'undefined') {
    window.global = window
}

export default {
  name: 'CommonMap',
  mixins: [tiandituMixin],
  props: {
    detailData: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      map: null,
      baseLayer: null,
      annotationLayer: null, // 矢量标注图层
      marker: null,
      center: [32.43, 119.18], // 仪征市大致坐标
      zoom: 14 // 默认缩放级别
    }
  },
  mounted() {
    this.initMap()
  },
  beforeUnmount() {
    this.destroyMap()
  },
  watch: {
    detailData: {
      handler() {
        if (this.map && this.detailData) {
          this.updateMarker()
        }
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    // 初始化地图
    initMap() {
      if (this.$refs.mapContainer) {
        // 初始化地图，禁用所有交互
        this.map = L.map(this.$refs.mapContainer, {
          attributionControl: false,
          zoomControl: false,
          scrollWheelZoom: false,
          dragging: false,
          touchZoom: false,
          doubleClickZoom: false,
          boxZoom: false,
          keyboard: false
        })

        // 使用天地图矢量底图
        this.baseLayer = this.createTiandituLayer(L, {
          layer: 'vec',
          attribution: '© 国家基础地理信息中心',
          maxZoom: 18,
          tileSize: 256
        })
        
        if (this.baseLayer) {
          this.baseLayer.addTo(this.map)
        } else {
          console.error('天地图矢量底图图层创建失败')
        }

        // 创建天地图矢量标注图层
        this.annotationLayer = this.createTiandituLayer(L, {
          layer: 'cva',
          attribution: '© 国家基础地理信息中心',
          maxZoom: 18,
          tileSize: 256
        })
        
        if (this.annotationLayer) {
          this.annotationLayer.addTo(this.map)
        } else {
          console.error('天地图矢量标注图层创建失败')
        }

        // 设置初始视图
        this.map.setView(this.center, this.zoom)

        // 确保地图正确显示
        this.$nextTick(() => {
          if (this.map) {
            this.map.invalidateSize()
            // 禁用所有交互事件
            this.map.dragging.disable()
            this.map.touchZoom.disable()
            this.map.doubleClickZoom.disable()
            this.map.scrollWheelZoom.disable()
            this.map.boxZoom.disable()
            this.map.keyboard.disable()
            // 初始化标记
            this.updateMarker()
          }
        })
      }
    },

    // 更新标记
    updateMarker() {
      if (!this.map || !this.detailData) {
        return
      }

      // 清除旧标记
      if (this.marker) {
        this.map.removeLayer(this.marker)
        this.marker = null
      }

      // 获取经纬度（支持多种字段名）
      const latitude = this.detailData.problemLatitude || this.detailData.latitude || this.detailData.lat
      const longitude = this.detailData.problemLongitude || this.detailData.longitude || this.detailData.lon || this.detailData.lng

      if (!latitude || !longitude) {
        console.warn('问题位置缺少经纬度信息', {
          problemLatitude: this.detailData.problemLatitude,
          problemLongitude: this.detailData.problemLongitude,
          latitude: this.detailData.latitude,
          longitude: this.detailData.longitude,
          lat: this.detailData.lat,
          lon: this.detailData.lon,
          lng: this.detailData.lng
        })
        return
      }

      const latlng = [parseFloat(latitude), parseFloat(longitude)]

      // 根据 referenceType 选择图标
      const referenceType = this.detailData.referenceType || 'river'
      const bottomIcon = referenceType === 'reservoir' ? singleIcon3 : singleIcon3

      // 只获取问题定位的位置信息
      const specificLocation = this.detailData.issueSpecificLocation || this.detailData.specificLocation || '问题位置'

      // 创建自定义 HTML 图标，只显示问题定位位置
      const customDivIcon = L.divIcon({
        className: 'custom-signboard-container',
        html: `
          <div class="signboard-icon-wrapper">
            <div class="signboard-top-block">
              <div class="top-content-wrapper">
                <div class="top-location">${specificLocation}</div>
              </div>
            </div>
            <div class="signboard-bottom-block">
              <img src="${bottomIcon}" alt="图标" class="signboard-icon" />
            </div>
          </div>
        `,
        // iconSize: [10, 60],
        iconAnchor: [50, 40],
      })

      // 创建标记
      this.marker = L.marker(latlng, { icon: customDivIcon })
      this.marker.addTo(this.map)

      // 定位到标记位置
      this.map.setView(latlng, 14)
    },

    // 销毁地图
    destroyMap() {
      if (this.marker) {
        this.map.removeLayer(this.marker)
        this.marker = null
      }
      if (this.annotationLayer) {
        this.map.removeLayer(this.annotationLayer)
        this.annotationLayer = null
      }
      if (this.baseLayer) {
        this.map.removeLayer(this.baseLayer)
        this.baseLayer = null
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
.common-map-container {
  width: 100%;
  height: 300px;
  min-height: 300px; // 确保最小高度
  max-height: 50vh; // 最大高度不超过视口高度的50%
  position: relative;
  overflow: hidden;

  #common-map {
    width: 100%;
    height: 100%;
    
    // 禁用地图交互，但允许标记显示
    :deep(.leaflet-container) {
      cursor: default !important;
    }
    
    :deep(.leaflet-interactive) {
      pointer-events: none !important;
    }
    
    // 标记容器允许显示
    :deep(.leaflet-marker-pane) {
      pointer-events: auto;
    }
  }
}

// 自定义公示牌图标样式（参考 adminMap.vue）
:deep(.custom-signboard-container) {
  background: none !important;
  border: none !important;

  .signboard-icon-wrapper {
    display: flex;
    flex-direction: column;
    width: fit-content;
    min-width: 150px;
    border-radius: 4px;
    overflow: visible;

    .signboard-top-block {
      width: fit-content;
      min-width: 150px;
      border: 1px solid #A5CFF8;
      background: #ffffff80;
      margin-bottom: 10px;
      display: flex;
      flex-direction: column;
      padding: 6px 10px;
      box-sizing: border-box;
      position: relative;

      .top-content-wrapper {
        display: flex;
        flex-direction: column;
        width: 100%;
      }

      .top-location {
        text-align: center;
        font-size: 24px;
        color: #666666;
        font-weight: 400;
        line-height: 1.2;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .signboard-bottom-block {
      width: 100%;
      height: 80px;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;

      .signboard-icon {
        width: 60px;
        height: 150px;
        object-fit: contain;
      }
    }
  }
}
</style>
