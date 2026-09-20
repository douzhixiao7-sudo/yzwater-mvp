/**
 * 天地图 Mixin
 * 提供天地图 token 获取和相关工具方法
 */
export default {
  methods: {
    /**
     * 获取随机天地图 token
     * @returns {string} 随机选择的 token，如果未配置则返回空字符串
     */
    getRandomTiandituToken() {
      const tokens = window.getConfig('tdMapToken') || []
      if (tokens.length === 0) {
        console.warn('天地图 token 未配置')
        return ''
      }
      const randomIndex = Math.floor(Math.random() * tokens.length)
      return tokens[randomIndex]
    },

    /**
     * 生成天地图瓦片服务 URL
     * @param {string} layer - 图层类型，如 'img'（影像）、'vec'（矢量）、'ter'（地形）、'cia'（影像标注）、'cva'（矢量标注）、'cta'（地形标注）等
     * @param {string} style - 样式，默认为 'default'
     * @param {string} tileMatrixSet - 瓦片矩阵集，默认为 'w'（Web Mercator）
     * @returns {string} 天地图瓦片服务 URL
     */
    getTiandituTileUrl(layer = 'img', style = 'default', tileMatrixSet = 'w') {
      const token = this.getRandomTiandituToken()
      if (!token) {
        console.error('无法获取天地图 token，请检查配置')
        return ''
      }
      
      // 根据图层类型选择对应的服务类型
      // 底图类型：img（影像）、vec（矢量）、ter（地形）
      // 标注类型：cia（影像标注）、cva（矢量标注）、cta（地形标注）
      const serviceTypeMap = {
        'img': 'img_w',   // 影像底图
        'vec': 'vec_w',   // 矢量底图
        'ter': 'ter_w',   // 地形底图
        'cia': 'cia_w',   // 影像标注
        'cva': 'cva_w',   // 矢量标注
        'cta': 'cta_w'    // 地形标注
      }
      
      const serviceType = serviceTypeMap[layer] || 'img_w'
      
      return `https://t0.tianditu.gov.cn/${serviceType}/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=${layer}&STYLE=${style}&TILEMATRIXSET=${tileMatrixSet}&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=${token}`
    },

    /**
     * 创建天地图底图图层
     * @param {Object} L - Leaflet 对象（需要从组件中传入）
     * @param {Object} options - 图层配置选项
     * @param {string} options.layer - 图层类型，默认为 'img'
     * @param {string} options.attribution - 版权信息，默认为 '© 国家基础地理信息中心'
     * @param {number} options.maxZoom - 最大缩放级别，默认为 18
     * @param {number} options.tileSize - 瓦片大小，默认为 256
     * @returns {L.TileLayer|null} Leaflet 瓦片图层对象，如果创建失败则返回 null
     */
    createTiandituLayer(L, options = {}) {
      if (!L) {
        console.error('Leaflet 对象未传入')
        return null
      }

      const {
        layer = 'img',
        attribution = '© 国家基础地理信息中心',
        maxZoom = 18,
        tileSize = 256
      } = options

      const url = this.getTiandituTileUrl(layer)
      if (!url) {
        return null
      }

      return L.tileLayer(url, {
        attribution,
        maxZoom,
        tileSize,
        zoomOffset: 0
      })
    }
  }
}

