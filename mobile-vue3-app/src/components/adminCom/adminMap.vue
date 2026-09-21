<template>
  <div class="map-page">
    <div class="map-wrapper">
      <div id="map-container" ref="mapContainer"></div>
      <!-- 行政级别选择器 -->
      <!-- <div class="level-selector">
        <div v-for="(item, index) in levelOptions" :key="index" class="level-item"
          :class="{ active: selectedLevel === item.value }" @click="selectLevel(item.value)">
          <img :src="selectedLevel === item.value ? aimIconActive : aimIcon" alt="级别图标" class="level-icon" />
          <span class="level-text">{{ item.label }}</span>
        </div>
      </div> -->
      <!-- 河道按钮 -->
      <div class="river-button pos2" @click="toggleRiverLayer">
        <img :src="riverChannelIcon" alt="河道" class="river-icon" />
        <span class="river-text">河道</span>
      </div>
      <!-- 待办按钮 -->
      <div class="todo-button" @click="handleTodo">
        <div class="todo-icon-wrapper">
          <img :src="noticeIcon" alt="待办" class="todo-icon" />
        </div>
        <span class="todo-text">待办</span>
      </div>


      <!-- 基础信息按钮 -->
      <!-- <div class="info-button river-button" @click="handleBaseInfo">
        <img :src="layerIcon" alt="基础信息" class="river-icon" />
        <span class="river-text">基础</span>
      </div> -->
      
      <!-- 河道等级按钮 -->
      <div class="river-level-button river-button" @click="handleRiverLevel">
        <img :src="layerIcon" alt="河道等级" class="river-icon" />
        <span class="river-text">等级</span>
      </div>
      
      <!-- 遮罩层 -->
      <transition name="fade">
        <div v-if="drawerVisible || todoDrawerVisible || baseInfoDrawerVisible || riverLevelDrawerVisible" class="drawer-mask"
          @click="closeAllDrawers"></div>
      </transition>
      
      <!-- 河道信息抽屉（从右侧滑出） -->
      <transition name="slide-right">
        <div v-if="drawerVisible" class="drawer-container drawer-right">
          <RiverCourseInfo @river-name-click="handleRiverNameClick" @close-drawer="drawerVisible = false" />
        </div>
      </transition>
      
      <!-- 待办信息抽屉（从右侧滑出） -->
      <transition name="slide-right">
        <div v-if="todoDrawerVisible" class="drawer-container drawer-right">
          <TodoInfo @locate-feedback="handleLocateFeedback" />
        </div>
      </transition>
      
      <!-- 基础信息抽屉（从下到上滑出） -->
      <transition name="slide-up">
        <div v-if="baseInfoDrawerVisible" class="drawer-container drawer-bottom">
          <div class="drawer-header">
            <!-- <div class="drawer-handle"></div> -->
          </div>
          <div class="drawer-content">
            <!-- Tabbar 组件 -->
            <Tabbar :tabs="tabs" :components="tabComponents" :default-active-key="'base'"
              @tab-change="handleTabChange" />
          </div>
        </div>
      </transition>

      <!-- 河道等级选择抽屉（从右侧滑出） -->
      <transition name="slide-right">
        <div v-if="riverLevelDrawerVisible" class="drawer-container drawer-right river-level-drawer">
          <div class="river-level-content">
            <div class="river-level-header">
              <div class="river-level-title">河道等级</div>
            </div>
            <div class="river-level-grid">
              <div 
                v-for="(level, index) in riverLevelOptions" 
                :key="index"
                class="river-level-item"
                :class="{ active: selectedRiverLevel === level.value }"
                @click="selectRiverLevel(level.value)"
              >
                {{ level.label }}
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import 'leaflet.markercluster'
import 'leaflet.markercluster/dist/MarkerCluster.css'
import 'leaflet.markercluster/dist/MarkerCluster.Default.css'
import { parse as parseWkt } from 'wellknown'
import tiandituMixin from '@/mixins/tiandituMixin'
import aimIcon from '@/assets/img/aimIcon.png'
import aimIconActive from '@/assets/img/aimIconAcitive.png'
import layerIcon from '@/assets/img/layerIcon.png'
import riverChannelIcon from '@/assets/img/riverChannelIcon.png'
import noticeIcon from '@/assets/img/noticeIcon.png'
import publicNoticeBoard from '@/assets/img/publicNoticeBoard.png'
import reservoirBoard from '@/assets/img/reservoirBorad.png'
import clusterImg from '@/assets/img/clusterImg.png'
import singleIcon1 from '@/assets/img/singleIcon1.png'
import singleIcon2 from '@/assets/img/singleIcon2.png'
import feedbackType1 from '@/assets/img/feedbackType1.png'
import feedbackType2 from '@/assets/img/feedbackType2.png'
import feedbackType3 from '@/assets/img/feedbackType3.png'
import { markRaw } from 'vue'
import { ElMessage } from 'element-plus'
import RiverCourseInfo from '@/components/adminCom/baseInfoCom/riverCourseInfo.vue'
import TodoInfo from '@/components/adminCom/baseInfoCom/todoInfo.vue'
import Tabbar from '@/components/commonCom/Tabbar.vue'
import BaseInfoContent from '@/components/userBaseInfoCom/BaseInfoContent.vue'
import RiverInfoContent from '@/components/userBaseInfoCom/RiverInfoContent.vue'
import FacilityInfoContent from '@/components/userBaseInfoCom/FacilityInfoContent.vue'

// 确保 global 对象存在（浏览器环境兼容）
if (typeof global === 'undefined') {
    window.global = window
}

export default {
  name: 'AdminMap',
  mixins: [tiandituMixin],
  components: {
    RiverCourseInfo,
    TodoInfo,
    Tabbar,
    BaseInfoContent,
    RiverInfoContent,
    FacilityInfoContent
  },
  data() {
    return {
      map: null,
      baseLayer: null, // 底图图层
      wmsLayer: null, // WMS 图层
      center: [32.43, 119.18], // 仪征市大致坐标（稍微往北移动）
      zoom: 10, // 默认缩放级别为8级
      aimIcon,
      aimIconActive,
      layerIcon,
      riverChannelIcon,
      noticeIcon,
      singleIcon1,
      singleIcon2,
      feedbackType1,
      feedbackType2,
      feedbackType3,
      selectedLevel: 'township', // 默认选中乡级
      drawerVisible: false, // 控制河道抽屉显示/隐藏
      todoDrawerVisible: false, // 控制待办抽屉显示/隐藏
      baseInfoDrawerVisible: false, // 控制基础信息抽屉显示/隐藏
      riverLevelDrawerVisible: false, // 控制河道等级抽屉显示/隐藏
      selectedRiverLevel: null, // 选中的河道等级，默认不选中
      // 缓存已查询的详情数据，避免重复查询
      signboardDetailCache: new Map(), // 使用 Map 存储已查询的 referenceId+referenceType 组合
      // 河道等级选项
      riverLevelOptions: [
        { label: '全部', value: 'all' },
        { label: '省级', value: '1' },
        { label: '市级', value: '2' },
        { label: '县级', value: '5' },
        { label: '镇级', value: '6' },
        { label: '乡级', value: '7' },
        // { label: '6级河道', value: '6' },
        // { label: '7级河道', value: '7' },
        // { label: '8级河道', value: '8' },
        // { label: '9级河道', value: '9' }
      ],
      levelOptions: [
        { label: '省级', value: 'province' },
        { label: '市级', value: 'city' },
        { label: '县级', value: 'county' },
        { label: '乡级', value: 'township' }
      ],
      // Tabbar 标签配置
      tabs: [
        { label: '基础信息', key: 'base' },
        { label: '河长信息', key: 'river' },
        // { label: '关联设施', key: 'facility' }
      ],
      // 标签对应的组件映射，使用 markRaw 防止组件被响应式化
      tabComponents: {
        'base': markRaw(BaseInfoContent),
        'river': markRaw(RiverInfoContent),
        'facility': markRaw(FacilityInfoContent)
      },
      // 公示牌标记列表
      signboardMarkers: [],
      // 聚合组
      markerClusterGroup: null,
      // 当前选中的公示牌信息
      currentSignboardInfo: null,
      // 反馈定位标记
      feedbackMarker: null,
      // 设施图层列表（包含图层和 riverLevel 信息）
      facilityLayers: [],
      // 标记是否正在手动调整缩放级别（避免自动更新图层可见性干扰）
      isManualZooming: false,
      // 聚合样式配置（可自定义）
      clusterConfig: {
        // 聚合半径（像素）
        maxClusterRadius: 80,
        // 在最大缩放级别时展开
        spiderfyOnMaxZoom: true,
        // 鼠标悬停时不显示覆盖范围
        showCoverageOnHover: false,
        // 点击聚合时缩放到边界
        zoomToBoundsOnClick: true,
        // 在缩放级别 16 时禁用聚合
        disableClusteringAtZoom: 16,
        // 聚合图标样式配置
        iconStyles: {
          small: {
            // TODO: 待业务提供颜色配置
            // backgroundColor: '#349DFF',
            backgroundColor: '#cccccc',  // 临时默认色
            borderColor: '#ffffff',
            borderWidth: 3,
            width: 60,
            height: 60,
            fontSize: 20,
            fontWeight: 600,
            color: '#ffffff',
            maxCount: 50,
            imageUrl: clusterImg // 使用 clusterImg 图片
          },
          medium: {
            backgroundColor: '#FFA500',
            borderColor: '#ffffff',
            borderWidth: 3,
            width: 75,
            height: 75,
            fontSize: 24,
            fontWeight: 600,
            color: '#ffffff',
            maxCount: 100,
            imageUrl: clusterImg // 使用 clusterImg 图片
          },
          large: {
            backgroundColor: '#FF5100',
            borderColor: '#ffffff',
            borderWidth: 3,
            width: 90,
            height: 90,
            fontSize: 28,
            fontWeight: 600,
            color: '#ffffff',
            maxCount: Infinity,
            imageUrl: clusterImg // 使用 clusterImg 图片
          }
        }
      }
    }
  },
  mounted() {
    this.initMap()
  },
  beforeUnmount() {
    this.destroyMap()
  },
  methods: {
    // 初始化地图
    initMap() {
      if (this.$refs.mapContainer) {
        // 初始化地图，禁用版权信息控件和缩放控件
        this.map = L.map(this.$refs.mapContainer, {
          attributionControl: false,
          zoomControl: false,
          scrollWheelZoom: true
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
        //使用wms的baseUrl
        const wmsBaseUrl = window.getConfig('geoServe.wmsService.baseUrl') || 'http://101.227.41.85:9090/'
        const wmsConfig = window.getConfig('geoServe.wmsService') || {
          path: '/geoserver/wms',
          layers: 'yzriver:yz_main_river_grandient_1.14',
          format: 'image/png',
          transparent: true,
          version: '1.1.0',
          attribution: ''
        }
        const wmsServiceUrl = `${wmsBaseUrl}${wmsConfig.path}`
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

        // 监听地图缩放事件，控制7级河道的显示/隐藏
        this.map.on('zoomend', () => {
          this.updateRiverLevel7Visibility()
        })

        // 创建聚合组
        this.createMarkerClusterGroup()

        // 加载公示牌位置数据
        this.loadSignboardLocations()

        // 加载水库几何数据
        this.loadReservoirGeom()

        // 默认不加载设施几何数据，等待用户选择河道等级后再加载
        // this.loadFacilityGeom()
      }
    },
    
    // 销毁地图
    destroyMap() {
      // 清除所有公示牌标记
      this.clearSignboardMarkers()

      // 清除所有设施图层
      this.clearFacilityLayers()

      // 移除 WMS 图层
      if (this.wmsLayer && this.map) {
        this.map.removeLayer(this.wmsLayer)
        this.wmsLayer = null
      }

      // 移除聚合组
      if (this.markerClusterGroup && this.map) {
        this.map.removeLayer(this.markerClusterGroup)
        this.markerClusterGroup = null
      }
      
      // 清除反馈标记
      if (this.feedbackMarker) {
        this.map.removeLayer(this.feedbackMarker)
        this.feedbackMarker = null
      }

      if (this.map) {
        this.map.remove()
        this.map = null
      }
      this.baseLayer = null
    },

    // 创建标记聚合组
    createMarkerClusterGroup() {
      if (!this.map) return

      const config = this.clusterConfig

      this.markerClusterGroup = new L.MarkerClusterGroup({
        maxClusterRadius: config.maxClusterRadius,
        spiderfyOnMaxZoom: config.spiderfyOnMaxZoom,
        showCoverageOnHover: config.showCoverageOnHover,
        zoomToBoundsOnClick: config.zoomToBoundsOnClick,
        disableClusteringAtZoom: config.disableClusteringAtZoom,
        iconCreateFunction: (cluster) => {
          const count = cluster.getChildCount()
          const iconStyles = config.iconStyles

          // 根据数量确定样式大小
          let style
          if (count > iconStyles.medium.maxCount) {
            style = iconStyles.large
          } else if (count > iconStyles.small.maxCount) {
            style = iconStyles.medium
          } else {
            style = iconStyles.small
          }

          // 创建自定义 HTML
          const imageHtml = style.imageUrl
            ? `<img src="${style.imageUrl}" class="cluster-icon-image" alt="聚合图标" />`
            : ''

          const html = `
            <div class="marker-cluster marker-cluster-custom" 
                 style="
                   background-color: ${style.backgroundColor};
                   width: ${style.width}px;
                   height: ${style.height}px;
                   font-size: ${style.fontSize}px;
                   font-weight: ${style.fontWeight};
                   color: ${style.color};
                 ">
              ${imageHtml}
              <span class="cluster-count">${count}</span>
            </div>
          `

          return L.divIcon({
            html: html,
            className: 'marker-cluster-container',
            iconSize: L.point(style.width, style.height),
            iconAnchor: L.point(style.width / 2, style.height / 2)
          })
        }
      })

      // 将聚合组添加到地图
      this.markerClusterGroup.addTo(this.map)
    },
    // 选择行政级别
    selectLevel(level) {
      this.selectedLevel = level
      // 这里可以添加根据级别切换地图数据的逻辑
    },
    // 切换河道图层
    toggleRiverLayer() {
      // 关闭待办抽屉（如果打开）
      this.todoDrawerVisible = false
      // 打开抽屉显示河道信息
      this.drawerVisible = true
    },
    // 处理待办
    handleTodo() {
      // 关闭河道抽屉（如果打开）
      this.drawerVisible = false
      // 打开待办抽屉
      this.todoDrawerVisible = true
    },
    
    // 处理反馈定位
    handleLocateFeedback(locationData) {
      const { latitude, longitude, feedbackTypeLabel, feedbackItem } = locationData
      
      if (!latitude || !longitude) {
        ElMessage.warning('缺少位置信息')
        return
      }
      
      // 关闭待办抽屉
      this.todoDrawerVisible = false
      
      // 清除之前的反馈标记
      if (this.feedbackMarker) {
        if (this.map) {
          this.map.removeLayer(this.feedbackMarker)
        }
        this.feedbackMarker = null
      }
      
      // 根据反馈类型选择图标
      let iconUrl = this.feedbackType3 // 默认：其他
      if (feedbackTypeLabel === '漂浮物') {
        iconUrl = this.feedbackType1
      } else if (feedbackTypeLabel === '设施损坏' || feedbackTypeLabel === '设备损坏') {
        iconUrl = this.feedbackType2
      }
      
      // 创建图标（使用 divIcon 以便添加自定义类名和阴影）
      const feedbackIcon = L.divIcon({
        className: 'feedback-marker-icon',
        html: `<img src="${iconUrl}" class="feedback-marker-img" />`,
        iconSize: [64, 64],
        iconAnchor: [32, 32],
        popupAnchor: [0, -32]
      })
      
      // 创建标记
      const latlng = [parseFloat(latitude), parseFloat(longitude)]
      this.feedbackMarker = L.marker(latlng, { icon: feedbackIcon })
      
      // 如果有关联的反馈项数据，存储到标记上，并添加点击事件
      if (feedbackItem) {
        this.feedbackMarker._feedbackItem = feedbackItem
        
        // 添加点击事件，跳转到详情页
        this.feedbackMarker.on('click', (e) => {
          // 阻止事件冒泡
          if (e.originalEvent) {
            e.originalEvent.stopPropagation()
            e.originalEvent.preventDefault()
          }
          
          // 跳转到反馈处理页面
          this.$router.push({
            name: 'FixFeedBack',
            query: {
              data: JSON.stringify(feedbackItem)
            }
          })
        })
      }
      
      this.feedbackMarker.addTo(this.map)
      
      // 定位到标记位置（放大层级到 18）
      this.map.flyTo(latlng, 15, {
        duration: 1,
        easeLinearity: 0.5
      })
    },
    // 处理基础信息
    handleBaseInfo() {
      // 关闭其他抽屉（如果打开）
      this.drawerVisible = false
      this.todoDrawerVisible = false
      this.riverLevelDrawerVisible = false
      // 打开基础信息抽屉
      this.baseInfoDrawerVisible = true
    },
    // 处理河道等级
    handleRiverLevel() {
      // 关闭其他抽屉（如果打开）
      this.drawerVisible = false
      this.todoDrawerVisible = false
      this.baseInfoDrawerVisible = false
      // 打开河道等级抽屉
      this.riverLevelDrawerVisible = true
    },
    // 选择河道等级
    async selectRiverLevel(level) {
      this.selectedRiverLevel = level
      
      // 如果选择的是"全部"，不传递 riverLevel 参数
      // 否则将等级转换为 '1j', '2j' 等格式
      const riverLevel = level === 'all' ? null : `${level}j`
      
      // 重新加载设施几何数据，传递河道级别参数
      const loadedCount = await this.loadFacilityGeom(['river'], riverLevel)
      
      // 根据加载结果显示提示信息
      if (loadedCount === 0) {
        ElMessage.warning('缺少该河道级别的数据')
      } else {
        ElMessage.success('加载成功')
      }
      
      // 关闭河道等级抽屉
      this.riverLevelDrawerVisible = false
      
      // 按选择的等级移动到对应视角
      if (this.map) {
        let targetLat, targetLng, targetZoom
        if (level === '6') {
          // 镇级：定位到指定坐标
          targetLat = 32.252938260852005
          targetLng = 119.31764172
          targetZoom = 12
        } else {
          targetLat = 32.267778
          targetLng = 119.1725
          targetZoom = 12
        }

        // 获取当前地图状态
        const currentCenter = this.map.getCenter()
        const currentZoom = this.map.getZoom()
        
        // 计算距离（米）
        const distance = currentCenter.distanceTo([targetLat, targetLng])
        const zoomDiff = Math.abs(currentZoom - targetZoom)
        
        // 如果位置和缩放级别都正确（距离小于100米且缩放级别相同），则不执行移动
        if (distance >= 100 || zoomDiff !== 0) {
          // 执行视角移动
          this.map.flyTo([targetLat, targetLng], targetZoom, {
            duration: 1.0,
            easeLinearity: 0.25
          })
        }
      }
    },

    // 根据选择的河道等级调整地图缩放级别并定位
    flyToRiverLevel(selectedLevel) {
      if (!this.map) return

      if (selectedLevel === 'all') {
        // 选择"全部"时，定位到所有可见河道
        return
      }

      // 将选中的等级字符串转换为数字
      const selectedLevelNum = parseInt(selectedLevel)
      
      // 根据河道等级计算对应的地图缩放级别
      // 1级河道对应 zoom 8，2级对应 zoom 9，以此类推
      // 公式：zoom = riverLevel + 7
      const targetZoom = selectedLevelNum + 7

      // 收集该等级的所有图层
      const matchedLayerInfos = []
      this.facilityLayers.forEach(layerInfo => {
        if (layerInfo && layerInfo.layer && layerInfo.riverLevel === selectedLevelNum) {
          matchedLayerInfos.push(layerInfo)
        }
      })

      // 临时禁用自动更新图层可见性，避免缩放后又被重置
      this.isManualZooming = true
      
      // 先调整地图缩放级别到目标层级（使用动画效果）
      if (this.map.getZoom() !== targetZoom) {
        this.map.flyTo(this.map.getCenter(), targetZoom, {
          duration: 1.0, // 动画时长1秒
          easeLinearity: 0.25
        })
      }
      
      // 等待缩放完成后再处理图层
      this.map.once('zoomend', () => {
        
        // 重新启用自动更新图层可见性
        this.isManualZooming = false
        
        setTimeout(() => {
          if (matchedLayerInfos.length > 0) {
            // 如果找到了该等级的图层，强制显示，然后定位
            const layersToShow = []
            
            matchedLayerInfos.forEach(layerInfo => {
              const layer = layerInfo.layer
              
              // 强制显示该等级的图层（如果未在地图上）
              if (!this.map.hasLayer(layer)) {
                layer.addTo(this.map)
              }
              
              layersToShow.push(layer)
            })

            // 处理其他等级的图层显示/隐藏（已移除层级控制，不再根据缩放级别隐藏图层）
            // this.facilityLayers.forEach(layerInfo => {
            //   if (layerInfo && layerInfo.layer && layerInfo.riverLevel !== selectedLevelNum) {
            //     const currentZoom = this.map.getZoom()
            //     const shouldShow = this.shouldShowLayer(currentZoom, layerInfo.riverLevel)
            //     const isOnMap = this.map.hasLayer(layerInfo.layer)
            //     
            //     if (!shouldShow && isOnMap) {
            //       this.map.removeLayer(layerInfo.layer)
            //     }
            //   }
            // })

            // 定位到这些图层的位置（但不改变缩放级别）
            if (layersToShow.length > 0) {
              this.flyToLayers(layersToShow, targetZoom) // 传递目标缩放级别，防止被改变
            }
          } else {
            // 如果没有找到该等级的图层，只调整缩放级别
          }
        }, 200) // 增加延迟，确保缩放动画完成
      })
    },

    // 处理河道名称点击事件
    async handleRiverNameClick(data) {
      if (!data || !this.map) {
        return
      }

      // 关闭抽屉
      this.drawerVisible = false

      const { longitude, latitude, riverLevel, riverName, id } = data

      // 检查坐标是否有效
      if (!longitude || !latitude) {
        ElMessage.warning(`${riverName || '河道'} 缺少坐标信息`)
        return
      }

      try {
        // 1. 先定位到坐标位置
        const targetLat = latitude
        const targetLng = longitude
        const targetZoom = 16 // 使用与公示牌点击相同的缩放级别

        // 停止当前地图正在进行的任何动画
        this.map.stop()

        // 定位到目标位置
        this.map.flyTo([targetLat, targetLng], targetZoom, {
          duration: 1.0,
          easeLinearity: 0.25
        })

        // 2. 根据 riverLevel 加载对应的 geom 数据
        if (riverLevel) {
          // 提取河道级别数字（如 "1j" -> "1"）
          const levelMatch = riverLevel.toString().match(/(\d+)/)
          const level = levelMatch ? levelMatch[1] : null

          if (level) {
            // 加载对应级别的 geom 数据
            await this.loadFacilityGeom(['river'], `${level}j`)
          }
        }

        // 3. 查找该位置对应的公示牌并打开抽屉
        // 等待地图移动完成后查找公示牌
        this.map.once('moveend', () => {
          this.findAndOpenSignboardDrawer(targetLat, targetLng, id)
        })
      } catch (error) {
        console.error('定位失败:', error)
        ElMessage.error('定位失败，请稍后重试')
      }
    },

    // 查找并打开对应的公示牌抽屉
    findAndOpenSignboardDrawer(latitude, longitude, riverId = null) {
      if (!this.signboardMarkers || this.signboardMarkers.length === 0) {
        return
      }

      // 距离阈值（米），在阈值内认为匹配
      const thresholdDistance = 100 // 100米

      let matchedMarker = null
      let minDistance = Infinity

      // 遍历所有公示牌标记，找到最近的一个
      this.signboardMarkers.forEach(marker => {
        if (!marker || !marker.getLatLng) {
          return
        }

        const markerLatLng = marker.getLatLng()
        const distance = markerLatLng.distanceTo([latitude, longitude])

        // 如果提供了河道 ID，优先匹配 referenceId
        if (riverId && marker._signboardData) {
          const signboardData = marker._signboardData
          if (signboardData.referenceType === 'river' && 
              signboardData.referenceId === riverId) {
            // 找到精确匹配的公示牌
            matchedMarker = marker
            minDistance = 0
            return
          }
        }

        // 如果没有精确匹配，找距离最近的
        if (distance < thresholdDistance && distance < minDistance) {
          minDistance = distance
          matchedMarker = marker
        }
      })

      // 如果找到匹配的公示牌，打开抽屉
      if (matchedMarker && matchedMarker._signboardData) {
        const signboardData = matchedMarker._signboardData
        
        // 设置 qrCode 到 Vuex
        if (signboardData.qrCode) {
          this.$store.commit('setQrcode', signboardData.qrCode)
        }
        
        // 打开抽屉
        this.openSignboardDrawer()
      }
    },

    // 将地图视图调整到指定图层的位置
    flyToLayers(layers, preserveZoom = null) {
      if (!this.map) {
        return
      }

      if (!layers || layers.length === 0) {
        return
      }

      try {
        // 创建一个 FeatureGroup 包含所有匹配的图层
        const group = new L.FeatureGroup(layers)
        
        // 获取所有图层的边界范围
        const bounds = group.getBounds()
        
        if (bounds && bounds.isValid && bounds.isValid()) {
          const center = bounds.getCenter()
          
          if (preserveZoom !== null) {
            // 如果指定了要保持的缩放级别，只移动中心点，不改变缩放级别
            this.isManualZooming = true // 临时禁用自动更新
            this.map.flyTo(center, preserveZoom, {
              duration: 1.5,
              easeLinearity: 0.25
            })
            // 移动完成后重新启用自动更新
            this.map.once('moveend', () => {
              setTimeout(() => {
                this.isManualZooming = false
              }, 100)
            })
          } else {
            // 使用 flyToBounds 动画效果定位到这些图层
            // 添加一些内边距，确保图层不会紧贴地图边缘
            this.map.flyToBounds(bounds, {
              padding: [100, 100], // 上下左右各100px的内边距
              duration: 1.5, // 动画时长1.5秒
              easeLinearity: 0.25
            })
          }
        } else {
          // 如果 flyToBounds 失败，尝试使用 fitBounds
          try {
            this.map.fitBounds(bounds, {
              padding: [100, 100],
              animate: true
            })
          } catch (fitError) {
            // fitBounds 失败，静默处理
          }
        }
      } catch (error) {
        // 尝试备用方案：获取第一个图层的中心点
        try {
          if (layers[0] && layers[0].getBounds) {
            const firstBounds = layers[0].getBounds()
            if (firstBounds && firstBounds.isValid && firstBounds.isValid()) {
              const center = firstBounds.getCenter()
              this.map.flyTo(center, this.map.getZoom(), {
                duration: 1.5
              })
            }
          }
        } catch (fallbackError) {
          // 备用定位方案失败，静默处理
        }
      }
    },

    // 关闭所有抽屉
    closeAllDrawers() {
      this.drawerVisible = false
      this.todoDrawerVisible = false
      this.baseInfoDrawerVisible = false
      this.riverLevelDrawerVisible = false
    },
    // 处理标签切换事件
    handleTabChange(tabKey, index) {
      // 可以在这里处理标签切换后的逻辑
    },

    // 打开公示牌信息抽屉
    openSignboardDrawer() {
      // 关闭其他抽屉
      this.drawerVisible = false
      this.todoDrawerVisible = false
      // 打开基础信息抽屉
      this.baseInfoDrawerVisible = true
    },

    // 加载公示牌位置数据
    async loadSignboardLocations() {
      try {
        const response = await this.$http.get('/admin-api/screen/statistics/signboard-locations')

        if (response && response.code === 0 && response.data && response.data.list) {
          const signboardList = response.data.list

          // 清除之前的标记
          this.clearSignboardMarkers()

          // 收集需要查询详情的点位
          const detailQueryList = []

          // 先遍历数据，生成地图点位（不阻塞）
          signboardList.forEach(item => {
            if (item.latitude && item.longitude) {
              const latlng = [item.latitude, item.longitude]
              const iconType = item.referenceType === 'reservoir' ? 'reservoir' : 'river'

              // 根据 referenceType 判断是河道还是水库
              // 这里需要根据实际数据结构获取 riverName，暂时使用 name 或空字符串
              const riverName = '' // 可以根据实际需求从其他接口获取

              // 添加公示牌点位，传递完整的 item 数据
              this.addPublicBoard(latlng, riverName, item.name || '', false, iconType, item)

              // 阶段一：收集需要查询详情的点位（不立即查询，避免阻塞）
              if (item.unfinishedProblemCount && item.unfinishedProblemCount > 0) {
                if (item.referenceId && item.referenceType) {
                  detailQueryList.push({
                    referenceType: item.referenceType,
                    referenceId: item.referenceId
                  })
                }
              }
            }
          })

          // 延迟批量处理详情查询，避免阻塞页面渲染
          if (detailQueryList.length > 0) {
            // 使用 setTimeout 延迟执行，让页面先渲染
            setTimeout(() => {
              this.batchLoadSignboardDetails(detailQueryList)
            }, 500)
          }
        }
      } catch (error) {
        // 加载失败，静默处理
        console.error('加载公示牌位置失败:', error)
      }
    },

    // 批量加载公示牌详情（控制并发数量，避免性能问题）
    async batchLoadSignboardDetails(detailQueryList) {
      // 控制并发数量，每次最多处理 3 个请求
      const concurrency = 3
      
      for (let i = 0; i < detailQueryList.length; i += concurrency) {
        const batch = detailQueryList.slice(i, i + concurrency)
        
        // 并发处理当前批次
        const promises = batch.map(item => this.loadSignboardDetail(item.referenceType, item.referenceId))
        
        // 等待当前批次完成
        await Promise.all(promises)
        
        // 批次之间稍作延迟，避免请求过于密集
        if (i + concurrency < detailQueryList.length) {
          await new Promise(resolve => setTimeout(resolve, 100))
        }
      }
    },

    // 加载单个公示牌详情
    async loadSignboardDetail(referenceType, referenceId) {
      // 生成缓存 key：referenceType_referenceId
      const cacheKey = `${referenceType}_${referenceId}`
      
      // 检查缓存，如果已经查询过，直接返回
      if (this.signboardDetailCache.has(cacheKey)) {
        const cachedData = this.signboardDetailCache.get(cacheKey)
        
        return cachedData
      }

      // 缓存中没有，调用接口查询
      try {
        // 构建查询参数
        const queryParams = []
        queryParams.push(`referenceType=${referenceType}`)
        queryParams.push(`referenceId=${referenceId}`)
        
        // 拼接完整的 URL
        const url = `/admin-api/screen/statistics/signboard-reference-detail?${queryParams.join('&')}`
        
        // 调用详情接口
        const detailResponse = await this.$http.get(url)

        // 查询到了就存入缓存
        if (detailResponse && detailResponse.data) {
          // 存入缓存，避免重复查询
          this.signboardDetailCache.set(cacheKey, detailResponse.data)
          
          return detailResponse.data
        }
      } catch (detailError) {
        console.error('获取公示牌详情失败:', detailError)
      }
    },

    // 添加公示牌点位（参考 leafLetMap/map.vue 的 addPublicBoard 方法）
    addPublicBoard(latlng, riverName, boardName, shouldFly = false, iconType = 'river', signboardData = null) {
      // 如果是水库，signboardName 不做处理（不需要截取）
      // 如果是河道，去除名称中的"公示牌"字样
      const displayName = iconType === 'reservoir'
        ? (boardName || '')
        : (boardName ? boardName.replace('公示牌', '') : '')

      // 根据类型选择不同的图标
      const iconImage = iconType === 'reservoir' ? reservoirBoard : publicNoticeBoard
      
      // 根据 referenceType 选择底部图标
      // 如果 signboardData 存在，使用其 referenceType，否则使用 iconType
      const referenceType = signboardData?.referenceType || iconType
      const bottomIcon = referenceType === 'reservoir' ? singleIcon2 : singleIcon1
      
      // 获取点位名称和位置信息
      // 去除名称中的"公示牌"字样
      let pointName = signboardData?.name || boardName || ''
      if (pointName) {
        pointName = pointName.replace(/公示牌/g, '')
      }
      const specificLocation = signboardData?.specificLocation || ''
      // const unfinishedProblemCount = signboardData?.unfinishedProblemCount
      const unfinishedProblemCount = 0

      // 1. 创建自定义 HTML 图标 (L.divIcon) - 上下结构，宽度自适应
      const customDivIcon = L.divIcon({
        className: 'custom-signboard-container',
        html: `
          <div class="signboard-icon-wrapper">
            <div class="signboard-top-block">
              ${unfinishedProblemCount !== undefined && unfinishedProblemCount !== null && unfinishedProblemCount > 0 ? `<div class="top-corner-block">${unfinishedProblemCount}</div>` : ''}
              <div class="top-content-wrapper">
                <div class="top-name">${pointName}</div>
                <div class="top-location">${specificLocation}</div>
              </div>
            </div>
            <div class="signboard-bottom-block">
              <img src="${bottomIcon}" alt="图标" class="signboard-icon" />
            </div>
          </div>
        `,
        iconSize: [100, 110], // 宽度自适应，高度为顶部+底部(30+80=110)
        iconAnchor: [50, 55], // 锚点在底部中心
      })

      // 2. 创建标记（不直接添加到地图，而是添加到聚合组）
      const marker = L.marker(latlng, { icon: customDivIcon })

      // 将 signboardData 存储在标记上，方便后续查找
      if (signboardData) {
        marker._signboardData = signboardData
      }

      // 保存标记引用，方便后续清除
      this.signboardMarkers.push(marker)

      // 将标记添加到聚合组
      if (this.markerClusterGroup) {
        this.markerClusterGroup.addLayer(marker)
      } else {
        // 如果聚合组不存在，直接添加到地图（备用方案）
        marker.addTo(this.map)
      }

      // 如果是水库，添加 tooltip 显示 signboardName
      // if (iconType === 'reservoir' && boardName) {
      //   marker.bindTooltip(boardName, {
      //     permanent: false,
      //     direction: 'top',
      //     offset: [0, -10],
      //     className: 'reservoir-signboard-tooltip'
      //   })
      // }

      // 3. 监听点击事件
      marker.on('click', (e) => {
        // 阻止事件冒泡，防止触发地图的点击事件
        if (e.originalEvent) {
          e.originalEvent.stopPropagation()
          e.originalEvent.preventDefault()
        }

        // 停止当前地图正在进行的任何动画，防止冲突抖动
        this.map.stop()

        // 将公示牌的 qrCode 存储到 Vuex，这样 BaseInfoContent 组件就能获取到正确的数据
        if (signboardData && signboardData.qrCode) {
          // 将公示牌 qrCode 存储到 Vuex
          this.$store.commit('setQrcode', signboardData.qrCode)
        }

        const currentZoom = this.map.getZoom()
        const currentCenter = this.map.getCenter()
        const targetLatLng = L.latLng(latlng)
        const distance = currentCenter.distanceTo(targetLatLng)

        // 如果已经在目标位置附近且缩放一致，则直接打开抽屉，不再触发移动
        if (distance < 5 && currentZoom === 16) {
          this.openSignboardDrawer()
          return
        }

        // 如果距离非常近且缩放级别已经较高，使用 panTo 代替 flyTo 以减少"俯冲"带来的抖动感
        if (distance < 200 && currentZoom >= 14) {
          this.map.panTo(latlng, {
            animate: true,
            duration: 0.5
          })
        } else {
          // 距离较远时使用 flyTo
          this.map.flyTo(latlng, 16, {
            duration: 1,
            easeLinearity: 0.5 // 提高线性度，使动画末尾更稳定
          })
        }

        // 监听地图移动结束事件（一次性监听）
        this.map.once('moveend', () => {
          this.openSignboardDrawer()
        })
      })

      // 4. 初始化时强制执行一次飞向该位置的动画
      if (shouldFly) {
        this.map.flyTo(latlng, 16, { // 飞向目标点位，缩放级别设为 16
          duration: 2, // 增加时长以放缓速度
          easeLinearity: 0.25
        })
        // 监听地图移动结束事件，在动画结束后自动打开抽屉
        this.map.once('moveend', () => {
          // 可以在这里添加打开抽屉的逻辑
        })
      }
    },

    // 清除所有公示牌标记
    clearSignboardMarkers() {
      // 从聚合组中移除所有标记
      if (this.markerClusterGroup) {
        this.signboardMarkers.forEach(marker => {
          if (marker) {
            this.markerClusterGroup.removeLayer(marker)
          }
        })
      } else {
        // 如果聚合组不存在，直接从地图移除
        this.signboardMarkers.forEach(marker => {
          if (marker && this.map) {
            this.map.removeLayer(marker)
          }
        })
      }
      this.signboardMarkers = []
    },


    // 加载设施几何数据
    async loadFacilityGeom(value = ['river'], riverLevel = null) {
      // 检查地图是否已初始化
      if (!this.map) {
        return 0
      }

      try {
        // 构建请求参数
        const params = {
          value: value // 水利设施的类型
        }
        
        // 如果传递了 riverLevel 参数，则添加到请求中
        if (riverLevel !== null && riverLevel !== undefined) {
          params.riverLevel = riverLevel
        }
        
        const response = await this.$http.post('/admin-api/screen/statistics/facility-geom', params)

        if (response && response.code === 0 && response.data && Array.isArray(response.data)) {
          const facilityList = response.data
          
          // 记录实际加载的图层数量
          let loadedCount = 0

          // 只清除河道图层，保留水库图层
          this.clearRiverLayers()

          // 再次检查地图是否还存在（防止在异步操作过程中地图被销毁）
          if (!this.map) {
            return 0
          }

          // 遍历设施数据，解析并绘制几何图形
          facilityList.forEach(facility => {
            if (facility.geomWkt) {
              try {
                // 再次检查地图是否存在（防止在循环过程中地图被销毁）
                if (!this.map) {
                  return
                }

                // 1. 处理 WKT 字符串，提取 SRID 和 WKT 内容
                let wkt = facility.geomWkt
                let srid = null
                if (wkt.includes(';')) {
                  const parts = wkt.split(';')
                  const sridMatch = parts[0].match(/SRID=(\d+)/i)
                  if (sridMatch) {
                    srid = parseInt(sridMatch[1])
                  }
                  wkt = parts[1]
                }

                // 如果没有SRID，默认认为是WGS84 (4326)
                if (srid === null) {
                  srid = 4326
                }

                // 2. 将 WKT 转换为 GeoJSON
                const geojson = parseWkt(wkt)

                if (geojson) {
                  // 3. 数据源和地图都使用WGS84坐标系，无需转换

                  // 4. 判断几何类型（线数据还是面数据）
                  const isLineString = geojson.type === 'LineString' || geojson.type === 'MultiLineString'
                  const isPolygon = geojson.type === 'Polygon' || geojson.type === 'MultiPolygon'
                  
                  // 设置样式，颜色为亮绿色（稍柔和）
                  const style = {
                    color: '#32D69F',  // 边框颜色和线颜色（亮绿色，稍柔和）
                    weight: 2,
                    fillColor: '#32D69F',  // 填充颜色（亮绿色，稍柔和，面数据使用）
                    fillOpacity: isLineString ? 0 : 1,  // 线数据不填充，面数据纯色填充（完全不透明）
                    opacity: 1
                  }

                  // 5. 提取 riverLevel 数值（例如 "5级" -> 5）
                  // 如果 riverLevel 为 null，设置为 null，表示需要在层级 13 以后才显示
                  let riverLevel = null
                  if (facility.riverLevel !== null && facility.riverLevel !== undefined) {
                    const levelMatch = facility.riverLevel.toString().match(/(\d+)/)
                    if (levelMatch) {
                      riverLevel = parseInt(levelMatch[1])
                    } else {
                      // 如果无法解析，默认视为最不重要的（9级）
                      riverLevel = 9
                    }
                  }

                  // 6. 创建 Leaflet 图层
                  const layer = L.geoJSON(geojson, {
                    style: style
                  })

                  // 7. 将图层信息和 riverLevel 存储在一起，同时保存原始样式
                  const layerInfo = {
                    layer: layer,
                    riverLevel: riverLevel,
                    facilityName: facility.deviceName || facility.facilityType || '未知',
                    originalStyle: style, // 保存原始样式，用于恢复
                    isLineString: isLineString // 保存几何类型
                  }

                  // 8. 添加到地图，7级河道在缩放级别小于12时隐藏（再次检查地图是否存在）
                  if (this.map) {
                    const currentZoom = this.map.getZoom()
                    // 如果是7级河道，且缩放级别小于12，则不添加到地图
                    if (riverLevel === 7 && currentZoom < 12) {
                      // 不添加到地图，但存储图层信息
                      this.facilityLayers.push(layerInfo)
                      // 不增加计数，因为未显示
                    } else {
                      // 直接添加到地图
                      layer.addTo(this.map)
                      // 存储图层信息
                      this.facilityLayers.push(layerInfo)
                      loadedCount++
                    }
                  }
                }
              } catch (error) {
                // 解析失败，静默处理
              }
            }
          })

          // 返回加载的图层数量
          return loadedCount
        } else {
          // 如果没有数据或响应异常，返回 0
          return 0
        }
      } catch (error) {
        // 加载失败，返回 0
        console.error('加载设施几何数据失败:', error)
        return 0
      }
      
      return 0
    },

    // 加载水库几何数据
    async loadReservoirGeom() {
      // 检查地图是否已初始化
      if (!this.map) {
        return
      }

      try {
        // 构建请求参数
        const params = {
          value: ['reservoir'] // 水库类型
        }
        
        const response = await this.$http.post('/admin-api/screen/statistics/facility-geom', params)

        if (response && response.code === 0 && response.data && Array.isArray(response.data)) {
          const facilityList = response.data

          // 再次检查地图是否还存在（防止在异步操作过程中地图被销毁）
          if (!this.map) {
            return
          }

          // 遍历水库数据，解析并绘制几何图形
          facilityList.forEach(facility => {
            if (facility.geomWkt) {
              try {
                // 再次检查地图是否存在（防止在循环过程中地图被销毁）
                if (!this.map) {
                  return
                }

                // 1. 处理 WKT 字符串，提取 SRID 和 WKT 内容
                let wkt = facility.geomWkt
                let srid = null
                if (wkt.includes(';')) {
                  const parts = wkt.split(';')
                  const sridMatch = parts[0].match(/SRID=(\d+)/i)
                  if (sridMatch) {
                    srid = parseInt(sridMatch[1])
                  }
                  wkt = parts[1]
                }

                // 如果没有SRID，默认认为是WGS84 (4326)
                if (srid === null) {
                  srid = 4326
                }

                // 2. 将 WKT 转换为 GeoJSON
                const geojson = parseWkt(wkt)

                if (geojson) {
                  // 3. 数据源和地图都使用WGS84坐标系，无需转换

                  // 4. 判断几何类型（线数据还是面数据）
                  const isLineString = geojson.type === 'LineString' || geojson.type === 'MultiLineString'
                  
                  // 设置样式，颜色为 #0882f4（纯色）
                  const style = {
                    color: '#0882f4',  // 边框颜色和线颜色（蓝色）
                    weight: 2,
                    fillColor: '#0882f4',  // 填充颜色（蓝色，面数据使用）
                    fillOpacity: isLineString ? 0 : 1,  // 线数据不填充，面数据纯色填充（完全不透明）
                    opacity: 1
                  }

                  // 5. 创建 Leaflet 图层
                  const layer = L.geoJSON(geojson, {
                    style: style
                  })

                  // 6. 将图层信息存储到设施图层列表
                  const layerInfo = {
                    layer: layer,
                    riverLevel: null, // 水库没有河道级别
                    facilityName: facility.deviceName || facility.facilityType || '未知',
                    originalStyle: style, // 保存原始样式
                    isLineString: isLineString, // 保存几何类型
                    isReservoir: true // 标记为水库
                  }

                  // 7. 根据缩放级别决定是否添加到地图（再次检查地图是否存在）
                  if (this.map) {
                    const currentZoom = this.map.getZoom()
                    // 水库在缩放级别 >= 13 时才显示，避免过于密集
                    if (currentZoom >= 13) {
                      layer.addTo(this.map)
                    }
                    // 存储图层信息到设施图层列表（无论是否显示都存储）
                    this.facilityLayers.push(layerInfo)
                  }
                }
              } catch (error) {
                // 解析失败，静默处理
                console.error('解析水库 WKT 数据失败:', error)
              }
            }
          })
        }
      } catch (error) {
        // 加载失败，静默处理
        console.error('加载水库几何数据失败:', error)
      }
    },

    // 清除所有设施图层（包括河道和水库）
    clearFacilityLayers() {
      if (this.map) {
        this.facilityLayers.forEach(layerInfo => {
          if (layerInfo && layerInfo.layer) {
            this.map.removeLayer(layerInfo.layer)
          }
        })
      }
      this.facilityLayers = []
    },

    // 只清除河道图层，保留水库图层
    clearRiverLayers() {
      if (this.map) {
        const currentZoom = this.map.getZoom()
        // 先移除河道图层
        this.facilityLayers.forEach(layerInfo => {
          if (layerInfo && layerInfo.layer && !layerInfo.isReservoir) {
            this.map.removeLayer(layerInfo.layer)
          }
        })
        // 然后从数组中移除河道图层信息，保留水库图层信息
        this.facilityLayers = this.facilityLayers.filter(layerInfo => {
          // 保留水库图层
          if (layerInfo.isReservoir) {
            // 如果当前缩放级别 >= 13，确保水库图层在地图上显示
            if (currentZoom >= 13 && !this.map.hasLayer(layerInfo.layer)) {
              layerInfo.layer.addTo(this.map)
            }
            return true
          }
          return false
        })
      }
    },


    // 根据缩放级别和 riverLevel 判断是否应该显示图层
    // 规则：从8级开始，地图8级展示1级河道，9级展示1-2级河道，以此类推
    // 即：zoom 8 显示 1级，zoom 9 显示 1-2级，zoom 10 显示 1-3级...
    // 如果 riverLevel 为 null，则在层级 13 以后才显示
    shouldShowLayer(currentZoom, riverLevel) {
      
      // 如果缩放级别小于8，不显示任何河道
      if (currentZoom < 8) {
        return false
      }
      
      // 如果 riverLevel 为 null，需要在层级 13 以后才显示
      if (riverLevel === null || riverLevel === undefined) {
        return currentZoom >= 13
      }
      
      // 从8级开始，每增加一级，多显示一级河道
      // zoom 8: 显示 1级河道 (riverLevel <= 1)
      // zoom 9: 显示 1-2级河道 (riverLevel <= 2)
      // zoom 10: 显示 1-3级河道 (riverLevel <= 3)
      // ...
      // zoom 16: 显示 1-9级河道 (riverLevel <= 9)
      // zoom >= 17: 显示所有级别
      const maxLevel = currentZoom - 7 // 8级对应1级，9级对应2级，以此类推
      
      if (currentZoom >= 17) {
        // zoom >= 17，显示所有级别
        return true
      } else {
        // 显示 riverLevel <= maxLevel 的河道
        return riverLevel <= maxLevel
      }
    },

    // 根据当前缩放级别更新7级河道和水库的显示/隐藏状态
    updateRiverLevel7Visibility() {
      if (!this.map) return

      const currentZoom = this.map.getZoom()

      this.facilityLayers.forEach(layerInfo => {
        if (layerInfo && layerInfo.layer) {
          // 处理7级河道
          if (layerInfo.riverLevel === 7) {
            const isOnMap = this.map.hasLayer(layerInfo.layer)
            // 如果是7级河道，缩放级别小于12时隐藏，大于等于12时显示
            const shouldShow = currentZoom >= 12

            if (shouldShow && !isOnMap) {
              // 应该显示但未在地图上，添加到地图
              layerInfo.layer.addTo(this.map)
            } else if (!shouldShow && isOnMap) {
              // 不应该显示但在地图上，从地图移除
              this.map.removeLayer(layerInfo.layer)
            }
          }
          
          // 处理水库
          if (layerInfo.isReservoir) {
            const isOnMap = this.map.hasLayer(layerInfo.layer)
            // 水库在缩放级别 >= 13 时才显示，避免过于密集
            const shouldShow = currentZoom >= 13

            if (shouldShow && !isOnMap) {
              // 应该显示但未在地图上，添加到地图
              layerInfo.layer.addTo(this.map)
            } else if (!shouldShow && isOnMap) {
              // 不应该显示但在地图上，从地图移除
              this.map.removeLayer(layerInfo.layer)
            }
          }
        }
      })
    },

    // 根据当前缩放级别更新所有设施图层的显示/隐藏状态
    updateFacilityLayersVisibility() {
      if (!this.map) return

      const currentZoom = this.map.getZoom()

      this.facilityLayers.forEach(layerInfo => {
        if (layerInfo && layerInfo.layer) {
          const shouldShow = this.shouldShowLayer(currentZoom, layerInfo.riverLevel)
          const isOnMap = this.map.hasLayer(layerInfo.layer)

          if (shouldShow && !isOnMap) {
            // 应该显示但未在地图上，添加到地图
            layerInfo.layer.addTo(this.map)
          } else if (!shouldShow && isOnMap) {
            // 不应该显示但在地图上，从地图移除
            this.map.removeLayer(layerInfo.layer)
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.map-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.map-wrapper {
  flex: 1;
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 0; // 确保 flex 子元素可以正确收缩
  overflow: hidden; // 防止滚动条
}

#map-container {
  width: 100%;
  height: 100%;
  // 纯净版地图，不添加任何滤镜效果
}

// 行政级别选择器
.level-selector {
  position: absolute;
  top: 28px;
  left: 24px;
  width: 60px;
  height: 328px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.level-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.3s ease;
  background-color: transparent;

  .level-icon {
    width: 30px;
    height: 30px;
    object-fit: contain;
    margin-bottom: 4px;
  }

  .level-text {
    font-size: 20px;
    color: #3D3D3D;
    font-weight: 400;
    transition: color 0.3s ease;
  }

  &.active {
    // TODO: 待业务提供颜色配置
    // background-color: #349DFF;
    background-color: #cccccc;  // 临时默认色

    .level-text {
      color: #ffffff;
    }
  }

  &:active {
    opacity: 0.8;
  }
}

// 河道按钮
.river-button {
  position: absolute;
  top: 28px;
  right: 24px;
    top: 25px;
  
  width: 60px;
  height: 70px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  border-radius: 4px;
  cursor: pointer;
  transition: opacity 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .river-icon {
    width: 30px;
    height: 30px;
    object-fit: contain;
    margin-bottom: 4px;
  }

  .river-text {
    font-size: 20px;
    color: #3D3D3D;
    font-weight: 400;
    line-height: 1;
  }

  &:active {
    opacity: 0.8;
  }
}

.pos2{
  top:210px;
}

// 基础信息按钮
.info-button {
  position: absolute;
  top: 208px;
  right: 24px;
  width: 60px;
  height: 70px;
  z-index: 1000;
}

// 待办按钮
.todo-button {
  position: absolute;
  top: 118px;
  right: 24px;
  width: 60px;
  height: 70px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  transition: opacity 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background-color: #FF5100;


  .todo-icon-wrapper {
    
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 4px;
  }

  .todo-icon {
    width: 30px;
    height: 30px;
    object-fit: contain;
    animation: shake 1.5s ease-in-out infinite;
  }

  .todo-text {
    font-size: 20px;
    color: white;
    font-weight: 400;
    line-height: 1;
  }

  &:active {
    opacity: 0.8;
  }
}

// 震动动画
@keyframes shake {

  0%,
  100% {
    transform: translateX(0);
  }

  5%,
  15%,
  25%,
  35%,
  45% {
    transform: translateX(-1px);
  }

  10%,
  20%,
  30%,
  40% {
    transform: translateX(1px);
  }

  50%,
  100% {
    transform: translateX(0);
  }
}

// 遮罩层样式
.drawer-mask {
  position: fixed;
  top: 0; // 从顶部开始
  left: 0;
  right: 0;
  bottom: 100px; // 避开底部菜单
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 1001; // 高于按钮的 z-index: 1000
}

// 抽屉容器样式
.drawer-container {
  position: fixed;
  background-color: #ffffff;
  z-index: 1002; // 高于遮罩层和按钮的 z-index
  display: flex;
  flex-direction: column;
  overflow: hidden;

  // 从右侧滑出的抽屉
  &.drawer-right {
    top: 0; // 从顶部开始
    right: 0;
    bottom: 100px; // 避开底部菜单
    width: 610px;
    box-shadow: -4px 0 20px rgba(0, 0, 0, 0.15);
    
    // 河道等级抽屉特殊宽度
    &.river-level-drawer {
      width: 360px;
    }
  }

  // 从下到上滑出的抽屉
  &.drawer-bottom {
    left: 0;
    right: 0;
    bottom: 100px; // 避开底部菜单
    border-radius: 40px 40px 0 0; // 恢复圆角
    max-height: 80vh; // 不撑满页面，最大高度为视口的80%
    box-shadow: 0 -8px 40px rgba(0, 0, 0, 0.1);
  }
}

// 抽屉头部
.drawer-header {
  text-align: center;
  position: relative;

  .drawer-handle {
    width: 80px;
    height: 4px;
    background-color: #ddd;
    border-radius: 4px;
    margin: 0 auto 16px;
  }

  .drawer-title {
    font-size: 36px;
    font-weight: 600;
    color: #3D3D3D;
  }
}

// 抽屉内容区域
.drawer-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 河道等级选择器内容
.river-level-content {
  padding: 40px 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f8f8f8;
}

.river-level-header {
  margin-bottom: 32px;
}

.river-level-title {
  font-size: 36px;
  font-weight: 600;
  color: #3D3D3D;
}

.river-level-grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 0;
  align-content: start;
}

.river-level-item {
  height: 60px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  font-size: 28px;
  color: #3D3D3D;
  font-weight: 400;
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    opacity: 0.7;
  }

  &.active {
    background-color: #349DFF;
    color: #ffffff;
    border-color: #349DFF;
    font-weight: 500;
  }
}

// 遮罩层淡入淡出动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 抽屉从右侧滑入动画
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.3s ease-out;
}

.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
}

// 抽屉从下到上滑入动画
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s ease-out;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
}


// 自定义公示牌图标样式
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
      position: relative; // 为右上角色块定位

      .top-corner-block {
        position: absolute;
        top: -20px;
        right: -20px;
        width: 58px;
        height: 36px;
        border-radius: 42px;
        opacity: 1;
        background: #ff5100;
        text-align: center;
        color: white;
        font-weight: 600;
      }

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
        white-space: nowrap; // 不换行，至少展示10个字
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .top-location {
        text-align: center;
        font-size: 24px;
        color: #666666;
        font-weight: 400;
        line-height: 1.2;
        white-space: nowrap; // 不换行，至少展示10个字
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .signboard-bottom-block {
      width: 100%;
      height: 80px; // 底部容器高度（减小）
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

// 标记聚合样式
:deep(.marker-cluster-container) {
  background: none !important;
  border: none !important;
}

// 反馈标记图标样式
:deep(.feedback-marker-icon) {
  background: none !important;
  border: none !important;
  
  .feedback-marker-img {
    width: 64px;
    height: 64px;
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
  }
}

:deep(.marker-cluster-custom) {
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  transition: transform 0.2s ease;
  overflow: hidden;

  &:hover {
    transform: scale(1.1);
  }

  .cluster-icon-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 50%;
    position: absolute;
    top: 0;
    left: 0;
    z-index: 1;
  }

  .cluster-count {
    position: absolute;
    z-index: 2;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
    top: 46%; // 使用百分比定位，向下移动数字
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>
