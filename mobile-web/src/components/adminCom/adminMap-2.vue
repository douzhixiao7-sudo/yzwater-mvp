<template>
  <div class="map-page">
    <div class="map-wrapper">
      <div id="map-container" ref="mapContainer"></div>
      <!-- 行政级别选择器 -->
      <div class="level-selector">
        <div v-for="(item, index) in levelOptions" :key="index" class="level-item"
          :class="{ active: selectedLevel === item.value }" @click="selectLevel(item.value)">
          <img :src="selectedLevel === item.value ? aimIconActive : aimIcon" alt="级别图标" class="level-icon" />
          <span class="level-text">{{ item.label }}</span>
        </div>
      </div>
      <!-- 河道按钮 -->
      <div class="river-button" @click="toggleRiverLayer">
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
      <div class="info-button river-button" @click="handleBaseInfo">
        <img :src="layerIcon" alt="基础信息" class="river-icon" />
        <span class="river-text">基础</span>
      </div>

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
          <RiverCourseInfo />
        </div>
      </transition>

      <!-- 待办信息抽屉（从右侧滑出） -->
      <transition name="slide-right">
        <div v-if="todoDrawerVisible" class="drawer-container drawer-right">
          <TodoInfo />
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
        <div v-if="riverLevelDrawerVisible" class="drawer-container drawer-right">
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
import { markRaw } from 'vue'
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
      center: [32.27, 119.18], // 仪征市大致坐标
      zoom: 13,
      aimIcon,
      aimIconActive,
      layerIcon,
      riverChannelIcon,
      noticeIcon,
      selectedLevel: 'township', // 默认选中乡级
      drawerVisible: false, // 控制河道抽屉显示/隐藏
      todoDrawerVisible: false, // 控制待办抽屉显示/隐藏
      baseInfoDrawerVisible: false, // 控制基础信息抽屉显示/隐藏
      riverLevelDrawerVisible: false, // 控制河道等级抽屉显示/隐藏
      selectedRiverLevel: 'all', // 选中的河道等级，默认"全部"
      // 河道等级选项
      riverLevelOptions: [
        { label: '全部', value: 'all' },
        { label: '1级河道', value: '1' },
        { label: '2级河道', value: '2' },
        { label: '3级河道', value: '3' },
        { label: '4级河道', value: '4' },
        { label: '5级河道', value: '5' },
        { label: '6级河道', value: '6' },
        { label: '7级河道', value: '7' },
        { label: '8级河道', value: '8' },
        { label: '9级河道', value: '9' }
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
        { label: '关联设施', key: 'facility' }
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
      // 设施图层列表
      facilityLayers: [],
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
            backgroundColor: '#349DFF',
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

        // 使用高德地图矢量底图
        const amapKey = window.getConfig('amapKey')
        if (amapKey) {
          // 高德地图矢量底图
          this.baseLayer = L.tileLayer('https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
            subdomains: ['1', '2', '3', '4'],
            attribution: '© 高德地图',
            maxZoom: 18,
            tileSize: 256,
            zoomOffset: 0
          })
          this.baseLayer.addTo(this.map)

          // 高德地图矢量标注图层（注记）
          const annotationLayer = L.tileLayer('https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=6&x={x}&y={y}&z={z}', {
            subdomains: ['1', '2', '3', '4'],
            attribution: '© 高德地图',
            maxZoom: 18,
            tileSize: 256,
            zoomOffset: 0
          })
          annotationLayer.addTo(this.map)
        } else {
          console.warn('高德Key未配置，使用天地图作为备用')
          // 备用方案：使用天地图
          this.baseLayer = this.createTiandituLayer(L, {
            layer: 'vec',
            attribution: '',
            maxZoom: 18,
            tileSize: 256
          })
          if (this.baseLayer) {
            this.baseLayer.addTo(this.map)
          }
        }

        // 确保地图正确显示
        this.$nextTick(() => {
          if (this.map) {
            this.map.invalidateSize()
          }
        })

        // 创建聚合组
        this.createMarkerClusterGroup()

        // 加载公示牌位置数据
        this.loadSignboardLocations()

        // 加载设施几何数据
        this.loadFacilityGeom()
      }
    },

    // 销毁地图
    destroyMap() {
      // 清除所有公示牌标记
      this.clearSignboardMarkers()

      // 清除所有设施图层
      this.clearFacilityLayers()

      // 移除聚合组
      if (this.markerClusterGroup && this.map) {
        this.map.removeLayer(this.markerClusterGroup)
        this.markerClusterGroup = null
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
      console.log(level)
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
    selectRiverLevel(level) {
      this.selectedRiverLevel = level
      console.log('选中的河道等级:', level)
      // 这里可以添加根据等级筛选数据的逻辑
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
      console.log('标签切换:', tabKey, index)
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

        console.log('公示牌位置响应:', response)

        if (response && response.code === 0 && response.data && response.data.list) {
          const signboardList = response.data.list

          // 清除之前的标记
          this.clearSignboardMarkers()

          // 遍历数据，生成地图点位
          signboardList.forEach(item => {
            if (item.latitude && item.longitude) {
              const latlng = [item.latitude, item.longitude]
              const iconType = item.referenceType === 'reservoir' ? 'reservoir' : 'river'

              // 根据 referenceType 判断是河道还是水库
              // 这里需要根据实际数据结构获取 riverName，暂时使用 name 或空字符串
              const riverName = '' // 可以根据实际需求从其他接口获取

              // 添加公示牌点位，传递完整的 item 数据
              this.addPublicBoard(latlng, riverName, item.name || '', false, iconType, item)
            }
          })
        } else {
          console.error('获取公示牌位置失败:', response)
        }
      } catch (error) {
        console.error('加载公示牌位置失败:', error)
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

      // 1. 创建自定义 HTML 图标 (L.divIcon)
      const customDivIcon = L.divIcon({
        className: 'custom-signboard-container',
        html: `
          <div class="signboard-icon-wrapper">
            <img src="${iconImage}" class="bg-icon" />
            <div class="riverName-text">${riverName || ''}</div>
            <div class="signboard-text">${displayName}</div>
          </div>
        `,
        iconSize: [120, 120],
        iconAnchor: [60, 110],
      })

      // 2. 创建标记（不直接添加到地图，而是添加到聚合组）
      const marker = L.marker(latlng, { icon: customDivIcon })

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
      if (iconType === 'reservoir' && boardName) {
        marker.bindTooltip(boardName, {
          permanent: false,
          direction: 'top',
          offset: [0, -10],
          className: 'reservoir-signboard-tooltip'
        })
      }

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
          console.log('设置 qrcode 到 Vuex:', signboardData.qrCode)
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

    // WGS84/CGCS2000 转 GCJ-02 坐标转换函数
    // 高德地图使用 GCJ-02 坐标系，需要将 WGS84/CGCS2000 坐标转换为 GCJ-02
    transformWGS84ToGCJ02(lng, lat) {
      const a = 6378245.0
      const ee = 0.00669342162296594323

      let dLat = this.transformLat(lng - 105.0, lat - 35.0)
      let dLng = this.transformLng(lng - 105.0, lat - 35.0)
      const radLat = lat / 180.0 * Math.PI
      let magic = Math.sin(radLat)
      magic = 1 - ee * magic * magic
      const sqrtMagic = Math.sqrt(magic)
      dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI)
      dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI)
      const mgLat = lat + dLat
      const mgLng = lng + dLng
      return [mgLng, mgLat]
    },

    transformLat(lng, lat) {
      let ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng))
      ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0
      ret += (20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0
      ret += (160.0 * Math.sin(lat / 12.0 * Math.PI) + 320 * Math.sin(lat * Math.PI / 30.0)) * 2.0 / 3.0
      return ret
    },

    transformLng(lng, lat) {
      let ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng))
      ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0
      ret += (20.0 * Math.sin(lng * Math.PI) + 40.0 * Math.sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0
      ret += (150.0 * Math.sin(lng / 12.0 * Math.PI) + 300.0 * Math.sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0
      return ret
    },

    // 转换 GeoJSON 坐标数组（递归处理所有坐标点）
    transformGeoJSONCoordinates(coordinates) {
      if (Array.isArray(coordinates)) {
        if (typeof coordinates[0] === 'number') {
          // 这是一个坐标点 [lng, lat]
          const [lng, lat] = coordinates
          return this.transformWGS84ToGCJ02(lng, lat)
        } else {
          // 这是一个坐标数组，递归处理
          return coordinates.map(coord => this.transformGeoJSONCoordinates(coord))
        }
      }
      return coordinates
    },

    // 加载设施几何数据
    async loadFacilityGeom() {
      try {
        const response = await this.$http.post('/admin-api/screen/statistics/facility-geom', {
          value: ['river']
        })

        console.log('设施几何数据响应:', response)

        if (response && response.code === 0 && response.data && Array.isArray(response.data)) {
          const facilityList = response.data

          // 清除之前的设施图层
          this.clearFacilityLayers()

          // 遍历设施数据，解析并绘制几何图形
          facilityList.forEach(facility => {
            if (facility.geomWkt) {
              try {
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

                console.log('WKT SRID:', srid, 'WKT内容:', wkt.substring(0, 50))

                // 2. 将 WKT 转换为 GeoJSON
                const geojson = parseWkt(wkt)

                if (geojson) {
                  // 3. 数据源是WGS84 (4326)，高德地图使用GCJ-02坐标系
                  // 统一转换为GCJ-02坐标系，确保数据与地图坐标系一致
                  if (geojson.coordinates) {
                    geojson.coordinates = this.transformGeoJSONCoordinates(geojson.coordinates)
                    console.log('坐标已从WGS84转换为GCJ-02')
                  }

                  // 4. 根据几何类型设置不同的样式
                  const isLineString = geojson.type === 'LineString' || geojson.type === 'MultiLineString'

                  // 线数据使用绿色，面数据使用橙色
                  const style = isLineString ? {
                    color: '#32D6A3',      // 线数据颜色（绿色）
                    weight: 4,              // 线宽度（更粗）
                    fillColor: '#32D6A3',   // 填充颜色
                    fillOpacity: 0.6        // 填充透明度
                  } : {
                    color: '#FF5100',       // 面数据边界颜色（橙色）
                    weight: 2,
                    fillColor: '#FF5100',    // 填充颜色
                    fillOpacity: 0.4        // 填充透明度
                  }

                  // 5. 创建 Leaflet 图层并添加到地图
                  const layer = L.geoJSON(geojson, {
                    style: style
                  }).addTo(this.map)

                  // 存储图层引用
                  this.facilityLayers.push(layer)

                  console.log('设施几何图形已绘制:', facility.deviceName || facility.facilityType)
                }
              } catch (error) {
                console.error('解析设施 WKT 数据失败:', error, facility.geomWkt)
              }
            }
          })

          // 如果有设施数据，自动缩放以包含所有设施（可选）
          if (this.facilityLayers.length > 0) {
            const group = new L.FeatureGroup(this.facilityLayers)
            // 使用 animate: false 防止与后续的动作冲突
            this.map.fitBounds(group.getBounds(), { padding: [50, 50], animate: false })
          }
        } else {
          console.error('获取设施几何数据失败:', response)
        }
      } catch (error) {
        console.error('加载设施几何数据失败:', error)
      }
    },

    // 清除所有设施图层
    clearFacilityLayers() {
      if (this.map) {
        this.facilityLayers.forEach(layer => {
          if (layer) {
            this.map.removeLayer(layer)
          }
        })
      }
      this.facilityLayers = []
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
    background-color: #349DFF;

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
  top: 100px; // 避开头部标题
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
    top: 100px; // 避开头部标题
    right: 0;
    bottom: 100px; // 避开底部菜单
    width: 610px;
    box-shadow: -4px 0 20px rgba(0, 0, 0, 0.15);
  }

  // 从下到上滑出的抽屉
  &.drawer-bottom {
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: 40px 40px 0 0;
    max-height: 80vh;
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
  grid-template-columns: repeat(2, 1fr);
  gap: 0 24px;
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
    position: relative;
    width: 100%;
    height: 100%;
    color: #3D3D3D;
    font-size: 24px;

    .bg-icon {
      width: 100%;
      height: 100%;
      display: block;
    }

    .riverName-text,
    .signboard-text {
      position: absolute;
      left: 50%; // 水平居中起点
      transform: translateX(-50%); // 水平居中偏移
      text-align: center;
    }

    .riverName-text {
      top: 22px;
      font-weight: 600;
    }

    .signboard-text {
      top: 58px;
      font-weight: 500;
      font-size: 24px;
      pointer-events: none; // 防止文字阻挡点击事件
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
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
