/** 独立交付版运行时配置。第三方 Key 由使用方自行申请并填写。 */
window.APP_CONFIG = {
  api: {
    baseUrl: 'http://127.0.0.1:48082',
    timeout: 20000,
    retryTimes: 3,
    retryDelay: 1000
  },
  tdMapToken: [],
  amapKey: '',
  geoServe: {
    tileService: '/static/map/{z}/{x}/{y}.png',
    wmsService: {
      baseUrl: '',
      path: '/geoserver/wms',
      layers: '',
      format: 'image/png',
      transparent: true,
      version: '1.1.0',
      attribution: ''
    }
  }
}

window.getConfig = function (key) {
  if (!key || typeof key !== 'string') return undefined
  return key.split('.').reduce((value, part) =>
    value && typeof value === 'object' && part in value ? value[part] : undefined,
  window.APP_CONFIG)
}

window.setConfig = function (key, value) {
  if (!key || typeof key !== 'string') return
  const parts = key.split('.')
  let target = window.APP_CONFIG
  for (let index = 0; index < parts.length - 1; index += 1) {
    if (!target[parts[index]] || typeof target[parts[index]] !== 'object') target[parts[index]] = {}
    target = target[parts[index]]
  }
  target[parts[parts.length - 1]] = value
}
