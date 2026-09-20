/**
 * 高德地图 API 动态加载工具
 * 从配置文件中读取 key 并动态加载高德地图 API
 */

/**
 * 加载高德地图 API
 * @returns {Promise<void>}
 */
export function loadAmapAPI() {
  return new Promise((resolve, reject) => {
    // 如果已经加载，直接返回
    if (typeof window.AMap !== 'undefined') {
      resolve()
      return
    }

    // 获取高德地图 Key
    const getAmapKey = () => {
      if (window.getConfig && typeof window.getConfig === 'function') {
        return window.getConfig('amapKey')
      }
      return null
    }

    // 等待配置加载
    const waitForConfig = (timeout = 5000) => {
      return new Promise((resolve, reject) => {
        if (window.APP_CONFIG && window.getConfig) {
          resolve()
          return
        }

        const startTime = Date.now()
        const checkInterval = setInterval(() => {
          if (window.APP_CONFIG && window.getConfig) {
            clearInterval(checkInterval)
            resolve()
          } else if (Date.now() - startTime > timeout) {
            clearInterval(checkInterval)
            reject(new Error('配置加载超时'))
          }
        }, 50)
      })
    }

    // 加载脚本
    const loadScript = (amapKey) => {
      const script = document.createElement('script')
      script.type = 'text/javascript'
      script.src = `https://webapi.amap.com/maps?v=2.0&key=${amapKey}`
      script.async = true
      
      script.onload = () => {
        resolve()
      }
      
      script.onerror = () => {
        reject(new Error('高德地图 API 加载失败'))
      }
      
      document.head.appendChild(script)
    }

    // 执行加载流程
    waitForConfig()
      .then(() => {
        const amapKey = getAmapKey()
        if (amapKey) {
          loadScript(amapKey)
        } else {
          reject(new Error('高德地图 Key 未配置'))
        }
      })
      .catch((error) => {
        reject(error)
      })
  })
}

/**
 * 自动加载高德地图 API（在页面加载时调用）
 */
export function autoLoadAmapAPI() {
  // 如果已经加载，直接返回
  if (typeof window.AMap !== 'undefined') {
    return
  }

  // 延迟加载，确保配置已加载
  setTimeout(() => {
    loadAmapAPI().catch((error) => {
      console.warn('高德地图 API 自动加载失败:', error.message)
    })
  }, 100)
}
