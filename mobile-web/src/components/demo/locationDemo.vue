<template>
  <div class="location-demo">
    <div class="demo-header">
      <h1>定位功能测试 Demo</h1>
      <div class="browser-info">
        <span class="browser-tag" :class="{ 'wechat': isWeChat }">
          {{ isWeChat ? '微信浏览器' : '普通浏览器' }}
        </span>
      </div>
    </div>

    <div class="demo-content">
      <!-- 按钮区域 -->
      <div class="button-section">
        <button 
          class="demo-btn primary" 
          @click="getLocation"
          :disabled="gettingLocation"
        >
          {{ gettingLocation ? '定位中...' : '获取当前位置' }}
        </button>

        <button 
          class="demo-btn success" 
          @click="openAmap"
          :disabled="!currentLocation"
        >
          跳转到高德地图
        </button>
      </div>

      <!-- 位置信息显示 -->
      <div class="location-info" v-if="currentLocation">
        <h3>当前位置信息</h3>
        <div class="info-item">
          <span class="label">经度：</span>
          <span class="value">{{ currentLocation.longitude }}</span>
        </div>
        <div class="info-item">
          <span class="label">纬度：</span>
          <span class="value">{{ currentLocation.latitude }}</span>
        </div>
        <div class="info-item" v-if="currentLocation.accuracy">
          <span class="label">精度：</span>
          <span class="value">{{ currentLocation.accuracy }} 米</span>
        </div>
        <div class="info-item" v-if="currentLocation.method">
          <span class="label">定位方式：</span>
          <span class="value">{{ currentLocation.method }}</span>
        </div>
      </div>

      <!-- 日志显示区域 -->
      <div class="log-section">
        <div class="log-header">
          <h3>操作日志</h3>
          <button class="clear-btn" @click="clearLogs">清空日志</button>
        </div>
        <div class="log-content" ref="logContainer">
          <div 
            v-for="(log, index) in logs" 
            :key="index" 
            class="log-item"
            :class="log.type"
          >
            <span class="log-time">{{ log.time }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
          <div v-if="logs.length === 0" class="log-empty">暂无日志</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { isWeChatBrowser } from '@/utils/wechatLocation'
import http from '@/utils/request'

export default {
  name: 'LocationDemo',
  setup() {
    const isWeChat = ref(false)
    const gettingLocation = ref(false)
    const currentLocation = ref(null)
    const logs = ref([])
    const logContainer = ref(null)

    // 添加日志
    const addLog = (message, type = 'info') => {
      const time = new Date().toLocaleTimeString()
      logs.value.push({
        time,
        message,
        type
      })
      // 滚动到底部
      nextTick(() => {
        if (logContainer.value) {
          logContainer.value.scrollTop = logContainer.value.scrollHeight
        }
      })
      // 同时显示 ElMessage
      if (type === 'error') {
        ElMessage.error(message)
      } else if (type === 'success') {
        ElMessage.success(message)
      } else if (type === 'warning') {
        ElMessage.warning(message)
      } else {
        ElMessage.info(message)
      }
    }

    // 清空日志
    const clearLogs = () => {
      logs.value = []
      ElMessage.info('日志已清空')
    }

    // 初始化微信 SDK
    const initWeChatSDK = async () => {
      try {
        // 检查微信 SDK 是否已加载
        if (typeof window.wx === 'undefined') {
          throw new Error('微信 JS-SDK 未加载，请检查 script 标签')
        }

        const url = window.location.href.split('#')[0]
        const response = await http.get('/admin-api/wechat/sign', { url })
        
        if (response.code !== 0) {
          throw new Error(response.msg || '获取签名失败')
        }

        const { appId, timestamp, nonceStr, signature } = response.data

        window.wx.config({
          debug: false,
          appId: appId,
          timestamp: timestamp,
          nonceStr: nonceStr,
          signature: signature,
          jsApiList: ['getLocation']
        })

        return new Promise((resolve, reject) => {
          window.wx.ready(() => {
            addLog('微信 JS-SDK 初始化成功', 'success')
            resolve()
          })

          window.wx.error((res) => {
            reject(new Error('微信 JS-SDK 配置失败: ' + res.errMsg))
          })
        })
      } catch (error) {
        addLog(`微信 SDK 初始化失败: ${error.message}`, 'error')
        throw error
      }
    }

    // 获取微信定位
    const getWeChatLocationPosition = async () => {
      try {
        addLog('开始使用微信 SDK 获取位置...', 'info')
        
        // 检查微信 SDK 是否已加载
        if (typeof window.wx === 'undefined') {
          throw new Error('微信 JS-SDK 未加载')
        }
        
        // 确保 SDK 已初始化
        await initWeChatSDK()

        return new Promise((resolve, reject) => {
          window.wx.getLocation({
            type: 'gcj02',
            success: (res) => {
              const location = {
                longitude: res.longitude,
                latitude: res.latitude,
                accuracy: res.accuracy || null,
                method: '微信 SDK 定位'
              }
              
              currentLocation.value = location
              addLog(`定位成功！经度: ${res.longitude}, 纬度: ${res.latitude}`, 'success')
              if (res.accuracy) {
                addLog(`定位精度: ${res.accuracy} 米`, 'info')
              }
              
              resolve(location)
            },
            fail: (err) => {
              reject(new Error(err.errMsg || '获取位置失败'))
            }
          })
        })
      } catch (error) {
        addLog(`微信定位失败: ${error.message || '未知错误'}`, 'error')
        throw error
      }
    }

    // 等待高德地图 API 加载
    const waitForAmapAPI = (timeout = 10000) => {
      return new Promise((resolve, reject) => {
        if (typeof window.AMap !== 'undefined') {
          resolve()
          return
        }

        addLog('等待高德地图 API 加载...', 'info')
        const startTime = Date.now()
        const checkInterval = setInterval(() => {
          if (typeof window.AMap !== 'undefined') {
            clearInterval(checkInterval)
            addLog('高德地图 API 加载完成', 'success')
            resolve()
          } else if (Date.now() - startTime > timeout) {
            clearInterval(checkInterval)
            reject(new Error('高德地图 API 加载超时，请检查网络连接或配置'))
          }
        }, 100)
      })
    }

    // 获取高德地图定位
    const getAmapLocation = async () => {
      // 先等待 API 加载
      await waitForAmapAPI()
      
      return new Promise((resolve, reject) => {
        if (typeof window.AMap === 'undefined') {
          reject(new Error('高德地图 API 未加载'))
          return
        }

        addLog('开始使用高德地图定位...', 'info')

        window.AMap.plugin(['AMap.Geolocation'], () => {
          const options = {
            showButton: false,
            showMarker: false,
            showCircle: false,
            enableHighAccuracy: true,
            timeout: 15000,
            maximumAge: 0,
            convert: true,
            extensions: 'all'
          }

          const geolocation = new window.AMap.Geolocation(options)

          geolocation.getCurrentPosition((status, result) => {
            if (status === 'complete') {
              const position = result.position
              currentLocation.value = {
                longitude: position.lng,
                latitude: position.lat,
                accuracy: result.accuracy || null,
                method: '高德地图定位'
              }
              
              addLog(`定位成功！经度: ${position.lng}, 纬度: ${position.lat}`, 'success')
              if (result.accuracy) {
                addLog(`定位精度: ${result.accuracy} 米`, 'info')
              }
              
              resolve(currentLocation.value)
            } else {
              let errorMsg = '定位失败'
              if (result && result.message) {
                errorMsg = result.message
              }
              addLog(`高德地图定位失败: ${errorMsg}`, 'error')
              reject(new Error(errorMsg))
            }
          })
        })
      })
    }

    // 获取位置（根据浏览器类型选择）
    const getLocation = async () => {
      gettingLocation.value = true
      currentLocation.value = null

      try {
        if (isWeChat.value) {
          // 微信浏览器：使用微信 SDK
          addLog('检测到微信浏览器，使用微信 SDK 定位', 'info')
          await getWeChatLocationPosition()
        } else {
          // 普通浏览器：使用高德地图定位
          addLog('检测到普通浏览器，使用高德地图定位', 'info')
          
          // 先检查浏览器定位权限
          if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
              async () => {
                // 权限已授予，使用高德地图定位
                try {
                  await getAmapLocation()
                } catch (error) {
                  addLog(`定位失败: ${error.message}`, 'error')
                } finally {
                  gettingLocation.value = false
                }
              },
              (error) => {
                gettingLocation.value = false
                let errorMsg = '定位权限被拒绝'
                
                switch (error.code) {
                  case error.PERMISSION_DENIED:
                    errorMsg = '定位权限被拒绝，请在浏览器设置中允许位置权限'
                    break
                  case error.POSITION_UNAVAILABLE:
                    errorMsg = '位置信息不可用，请检查GPS是否开启'
                    break
                  case error.TIMEOUT:
                    errorMsg = '定位超时，请稍后重试'
                    break
                }
                
                addLog(errorMsg, 'error')
              },
              {
                enableHighAccuracy: true,
                timeout: 5000,
                maximumAge: 0
              }
            )
          } else {
            // 浏览器不支持定位，直接尝试高德地图定位
            try {
              await getAmapLocation()
            } catch (error) {
              addLog(`定位失败: ${error.message}`, 'error')
            } finally {
              gettingLocation.value = false
            }
          }
        }
      } catch (error) {
        gettingLocation.value = false
        addLog(`定位失败: ${error.message}`, 'error')
      }
    }

    // 跳转到高德地图
    const openAmap = () => {
      if (!currentLocation.value) {
        addLog('请先获取当前位置', 'warning')
        return
      }

      const { longitude, latitude } = currentLocation.value
      
      // 高德地图 H5 导航页面
      // 可以设置目的地（这里使用扬州坐标作为示例）
      const destinationLng = 119.412420 // 扬州经度
      const destinationLat = 32.393159  // 扬州纬度
      
      // 高德地图网页版 URL
      const amapUrl = `https://uri.amap.com/navigation?from=${longitude},${latitude}&to=${destinationLng},${destinationLat}&mode=car`
      
      addLog(`正在打开高德地图网页版...`, 'info')
      addLog(`起点: ${latitude}, ${longitude}`, 'info')
      addLog(`终点: ${destinationLat}, ${destinationLng}`, 'info')
      
      // 在新窗口打开
      window.open(amapUrl, '_blank')
      
      addLog('已打开高德地图网页版', 'success')
    }

    // 初始化
    onMounted(async () => {
      isWeChat.value = isWeChatBrowser()
      addLog(`页面加载完成，浏览器类型: ${isWeChat.value ? '微信浏览器' : '普通浏览器'}`, 'info')
      
      // 检查高德地图 API 是否加载（非微信浏览器）
      if (!isWeChat.value) {
        // 等待一下，让 API 有时间加载
        setTimeout(() => {
          if (typeof window.AMap === 'undefined') {
            addLog('提示: 高德地图 API 正在加载中，请稍候...', 'info')
            // 尝试等待加载
            waitForAmapAPI(5000).then(() => {
              addLog('高德地图 API 已就绪', 'success')
            }).catch(() => {
              addLog('警告: 高德地图 API 加载失败，请检查网络或配置', 'warning')
            })
          } else {
            addLog('高德地图 API 已就绪', 'success')
          }
        }, 500)
      }
    })

    return {
      isWeChat,
      gettingLocation,
      currentLocation,
      logs,
      logContainer,
      getLocation,
      openAmap,
      clearLogs
    }
  }
}
</script>

<style scoped lang="scss">
.location-demo {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  box-sizing: border-box;

  .demo-header {
    text-align: center;
    color: white;
    margin-bottom: 30px;

    h1 {
      font-size: 24px;
      margin: 0 0 10px 0;
      font-weight: 600;
    }

    .browser-info {
      .browser-tag {
        display: inline-block;
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 12px;
        background: rgba(255, 255, 255, 0.2);
        color: white;
        
        &.wechat {
          background: rgba(9, 187, 7, 0.3);
        }
      }
    }
  }

  .demo-content {
    background: white;
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  }

  .button-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 24px;

    .demo-btn {
      padding: 14px 20px;
      border: none;
      border-radius: 8px;
      font-size: 16px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.3s;
      
      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        
        &:not(:disabled):active {
          transform: scale(0.98);
        }
      }

      &.success {
        background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
        color: white;
        
        &:not(:disabled):active {
          transform: scale(0.98);
        }
      }
    }
  }

  .location-info {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 24px;

    h3 {
      margin: 0 0 12px 0;
      font-size: 16px;
      color: #333;
    }

    .info-item {
      display: flex;
      margin-bottom: 8px;
      font-size: 14px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        color: #666;
        min-width: 80px;
      }

      .value {
        color: #333;
        font-weight: 500;
      }
    }
  }

  .log-section {
    .log-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      h3 {
        margin: 0;
        font-size: 16px;
        color: #333;
      }

      .clear-btn {
        padding: 6px 12px;
        border: 1px solid #ddd;
        border-radius: 4px;
        background: white;
        color: #666;
        font-size: 12px;
        cursor: pointer;
        transition: all 0.3s;

        &:active {
          background: #f5f7fa;
        }
      }
    }

    .log-content {
      background: #1e1e1e;
      border-radius: 8px;
      padding: 12px;
      max-height: 300px;
      overflow-y: auto;
      font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
      font-size: 12px;

      .log-item {
        display: flex;
        margin-bottom: 6px;
        line-height: 1.5;

        &:last-child {
          margin-bottom: 0;
        }

        .log-time {
          color: #888;
          margin-right: 8px;
          min-width: 80px;
        }

        .log-message {
          flex: 1;
          word-break: break-all;
        }

        &.info .log-message {
          color: #61dafb;
        }

        &.success .log-message {
          color: #4caf50;
        }

        &.warning .log-message {
          color: #ff9800;
        }

        &.error .log-message {
          color: #f44336;
        }
      }

      .log-empty {
        color: #888;
        text-align: center;
        padding: 20px;
      }
    }
  }
}
</style>