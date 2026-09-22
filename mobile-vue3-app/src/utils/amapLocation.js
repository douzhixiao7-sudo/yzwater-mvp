/**
 * 高德地图定位工具
 * 封装高德API获取当前位置的方法
 */

/** 定位失败时的默认坐标（仪征附近，WGS84：纬度, 经度） */
export const DEFAULT_LOCATION = {
  latitude: 32.278043559397815,
  longitude: 119.17581272883548
}

async function buildLocationResult(wgs84Lng, wgs84Lat, accuracy = null, isDefault = false) {
  const [gcjLng, gcjLat] = transformWGS84ToGCJ02(wgs84Lng, wgs84Lat)
  const address = await getAddressFromCoordinates(gcjLng, gcjLat)
  return {
    longitude: wgs84Lng,
    latitude: wgs84Lat,
    gcj02Longitude: gcjLng,
    gcj02Latitude: gcjLat,
    address,
    accuracy,
    timestamp: new Date().toISOString(),
    isDefault
  }
}

/**
 * 获取当前位置（使用浏览器原生定位 + 高德逆地理编码）
 * @param {{ useFallback?: boolean }} [options] useFallback 默认 true：失败时回退默认坐标
 * @returns {Promise<Object>} 返回位置信息 { longitude, latitude, address, accuracy, timestamp, isDefault }
 */
export async function getCurrentLocationByAmap(options = {}) {
  const { useFallback = true } = options
  try {
    // 检查浏览器是否支持定位
    if (!navigator.geolocation) {
      throw new Error('您的浏览器不支持定位功能')
    }

    // 获取当前位置（WGS84坐标系）
    const position = await new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        (pos) => resolve(pos),
        (err) => {
          let errorMsg = '获取位置失败'
          if (err.code === 1) {
            errorMsg = '用户拒绝了定位请求'
          } else if (err.code === 2) {
            errorMsg = '位置信息不可用'
          } else if (err.code === 3) {
            errorMsg = '获取位置超时'
          }
          reject(new Error(errorMsg))
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 0
        }
      )
    })

    return await buildLocationResult(
      position.coords.longitude,
      position.coords.latitude,
      position.coords.accuracy || null,
      false
    )
  } catch (error) {
    if (useFallback) {
      console.warn('定位失败，使用默认坐标:', error?.message || error)
      return await buildLocationResult(
        DEFAULT_LOCATION.longitude,
        DEFAULT_LOCATION.latitude,
        null,
        true
      )
    }
    throw error
  }
}

/**
 * 根据经纬度获取地址信息（使用高德逆地理编码API）
 * @param {number} longitude - 经度（GCJ-02坐标系）
 * @param {number} latitude - 纬度（GCJ-02坐标系）
 * @returns {Promise<string>} 返回地址字符串
 */
export async function getAddressFromCoordinates(longitude, latitude) {
  try {
    // 从配置文件获取高德地图Key
    const amapKey = window.getConfig('amapKey')

    if (!amapKey) {
      // 如果没有配置Key，返回经纬度
      return `${longitude.toFixed(6)}, ${latitude.toFixed(6)}`
    }

    // location格式：经度,纬度，经纬度小数位数不超过6位
    const location = `${longitude.toFixed(6)},${latitude.toFixed(6)}`
    const url = `https://restapi.amap.com/v3/geocode/regeo?key=${amapKey}&location=${location}&output=json&radius=1000&extensions=all`

    const response = await fetch(url)
    const data = await response.json()

    // 解析高德地图返回的地址信息
    if (data && data.status === '1' && data.regeocode && data.regeocode.formatted_address) {
      // 去除省市信息，只保留详细地址
      const address = removeProvinceCity(data.regeocode.formatted_address)
      return address || data.regeocode.formatted_address
    }

    // 如果API返回格式不符合预期，返回经纬度
    return `${longitude.toFixed(6)}, ${latitude.toFixed(6)}`
  } catch (error) {
    // 出错时返回经纬度
    return `${longitude.toFixed(6)}, ${latitude.toFixed(6)}`
  }
}

/**
 * 去除地址中的省市信息，只保留区县及以下信息
 * @param {string} address - 原始地址
 * @returns {string} 处理后的地址
 */
export function removeProvinceCity(address) {
  if (!address || typeof address !== 'string') {
    return address
  }

  let processedAddress = address.trim()
  let previousLength = processedAddress.length
  let maxIterations = 10
  let iterations = 0

  while (iterations < maxIterations) {
    const beforeReplace = processedAddress

    // 去除省名+省
    processedAddress = processedAddress.replace(/^[^省]+省/, '')
    // 去除市名+市
    processedAddress = processedAddress.replace(/^[^市]+市/, '')
    // 去除开头可能的空格、标点
    processedAddress = processedAddress.replace(/^[\s,，、]+/, '').trim()

    if (processedAddress === beforeReplace || processedAddress.length === previousLength) {
      break
    }

    previousLength = processedAddress.length
    iterations++
  }

  return processedAddress || address
}

/**
 * WGS84 转 GCJ-02 坐标转换函数
 * 浏览器返回 WGS84 坐标，高德地图使用 GCJ-02 坐标系
 * @param {number} lng - 经度（WGS84）
 * @param {number} lat - 纬度（WGS84）
 * @returns {Array<number>} [经度, 纬度]（GCJ-02）
 */
export function transformWGS84ToGCJ02(lng, lat) {
  const a = 6378245.0
  const ee = 0.00669342162296594323

  let dLat = transformLat(lng - 105.0, lat - 35.0)
  let dLng = transformLng(lng - 105.0, lat - 35.0)
  const radLat = lat / 180.0 * Math.PI
  let magic = Math.sin(radLat)
  magic = 1 - ee * magic * magic
  const sqrtMagic = Math.sqrt(magic)
  dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI)
  dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI)
  const mgLat = lat + dLat
  const mgLng = lng + dLng
  return [mgLng, mgLat]
}

/**
 * 纬度转换辅助函数
 * @param {number} lng - 经度偏移
 * @param {number} lat - 纬度偏移
 * @returns {number} 纬度偏移量
 */
function transformLat(lng, lat) {
  let ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng))
  ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0
  ret += (20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0
  ret += (160.0 * Math.sin(lat / 12.0 * Math.PI) + 320 * Math.sin(lat * Math.PI / 30.0)) * 2.0 / 3.0
  return ret
}

/**
 * 经度转换辅助函数
 * @param {number} lng - 经度偏移
 * @param {number} lat - 纬度偏移
 * @returns {number} 经度偏移量
 */
function transformLng(lng, lat) {
  let ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng))
  ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0
  ret += (20.0 * Math.sin(lng * Math.PI) + 40.0 * Math.sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0
  ret += (150.0 * Math.sin(lng / 12.0 * Math.PI) + 300.0 * Math.sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0
  return ret
}
