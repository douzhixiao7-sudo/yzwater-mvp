/**
 * 微信 SDK 位置服务工具
 * 用于测试和封装微信 JS-SDK 的位置相关功能
 */

import http from '@/utils/request'
import { ElMessage } from 'element-plus'

/**
 * 检查是否在微信浏览器中
 */
export function isWeChatBrowser() {
  return /micromessenger/i.test(navigator.userAgent)
}

/**
 * 检查微信 JS-SDK 是否已加载
 */
export function isWxSDKLoaded() {
  return typeof window.wx !== 'undefined'
}

/**
 * 初始化微信 JS-SDK
 * @param {string} url - 当前页面 URL（不包含 hash）
 * @returns {Promise<Object>} 返回包含 appId, timestamp, nonceStr, signature 的配置对象
 */
export async function initWeChatSDK(url) {
  try {
    // 检查是否在微信浏览器中
    if (!isWeChatBrowser()) {
      throw new Error('请在微信浏览器中打开')
    }

    // 检查 SDK 是否已加载
    if (!isWxSDKLoaded()) {
      throw new Error('微信 JS-SDK 未加载，请检查 script 标签')
    }

    // 调用后端接口获取签名
    const response = await http.get('/admin-api/wechat/sign', { url: url })

    if (response.code !== 0) {
      throw new Error(response.msg || '获取签名失败')
    }

    const { appId, timestamp, nonceStr, signature } = response.data

    // 初始化微信 JS-SDK
    window.wx.config({
      debug: false,
      appId: appId,
      timestamp: timestamp,
      nonceStr: nonceStr,
      signature: signature,
      jsApiList: ['getLocation']
    })

    // 等待 SDK 就绪
    await new Promise((resolve, reject) => {
      window.wx.ready(() => {
        console.log('微信 JS-SDK 初始化成功')
        resolve()
      })

      window.wx.error((res) => {
        console.error('微信 JS-SDK 初始化失败:', res)
        reject(new Error('微信 JS-SDK 配置失败: ' + res.errMsg))
      })
    })

    return { appId, timestamp, nonceStr, signature }
  } catch (error) {
    console.error('初始化微信 SDK 失败:', error)
    throw error
  }
}

/**
 * 获取当前位置（使用微信 SDK）
 * @param {string} type - 坐标系类型，'wgs84' 或 'gcj02'，默认 'gcj02'
 * @returns {Promise<Object>} 返回位置信息 { longitude, latitude, speed, accuracy }
 */
export async function getWeChatLocation(type = 'gcj02') {
  try {
    // 检查 SDK 是否已加载
    if (!isWxSDKLoaded()) {
      throw new Error('微信 JS-SDK 未加载')
    }

    // 获取当前页面 URL（不包含 hash）
    const url = window.location.href.split('#')[0]

    // 先初始化 SDK
    await initWeChatSDK(url)

    // 获取位置
    return new Promise((resolve, reject) => {
      window.wx.getLocation({
        type: type, // 'wgs84' 或 'gcj02'
        success: (res) => {
          const location = {
            longitude: res.longitude,
            latitude: res.latitude,
            speed: res.speed || null,
            accuracy: res.accuracy || null
          }
          resolve(location)
        },
        fail: (err) => {
          reject(new Error(err.errMsg || '获取位置失败'))
        }
      })
    })
  } catch (error) {
    console.error('获取位置失败:', error)
    throw error
  }
}

/**
 * 打开高德地图网页版
 * @param {number} longitude - 经度
 * @param {number} latitude - 纬度
 * @param {string} mode - 导航模式，默认 'car'（驾车）
 */
export function openAmapH5(longitude, latitude, mode = 'car') {
  if (!longitude || !latitude) {
    ElMessage.warning('经纬度不能为空')
    return
  }

  // 高德地图 H5 导航页面
  // 格式：https://uri.amap.com/navigation?from=起点经度,起点纬度&mode=car
  const h5Url = `https://uri.amap.com/navigation?from=${longitude},${latitude}&mode=${mode}`

  console.log('打开高德地图网页版:', h5Url)
  console.log('当前位置坐标:', latitude, longitude)

  // 在新窗口打开高德地图
  window.open(h5Url, '_blank')
}

/**
 * 测试函数：获取位置并打开高德地图
 * 用于测试微信 SDK 位置功能
 */
export async function testWeChatLocationAndOpenAmap() {
  try {
    ElMessage.info('正在获取位置...')

    // 获取位置
    const location = await getWeChatLocation('gcj02')

    console.log('========== 位置信息 ==========')
    console.log('经度:', location.longitude)
    console.log('纬度:', location.latitude)
    console.log('速度:', location.speed || '不可用')
    console.log('精度:', location.accuracy || '不可用')
    console.log('============================')

    ElMessage.success('位置获取成功')

    // 打开高德地图
    openAmapH5(location.longitude, location.latitude)

    return location
  } catch (error) {
    console.error('测试失败:', error)
    ElMessage.error(error.message || '测试失败')
    throw error
  }
}
