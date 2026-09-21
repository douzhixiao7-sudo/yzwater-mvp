/**
 * 外部配置文件 - 打包后可以修改
 * 此文件包含应用程序的所有配置信息，包括API地址、认证信息、模型配置等
 * 修改此文件后无需重新打包，可直接在服务器上更新配置
 */
window.APP_CONFIG = {


  api: {
    // 本地联调：前端与后端运行在同一设备，手机访问时使用该设备的主机名
    
    baseUrl: new URL(window.location.protocol + '//' + window.location.hostname + ':48082').origin,
    wsBaseUrl: (window.location.protocol === 'https:' ? 'wss:' : 'ws:') + '//' + window.location.hostname + ':48082',
    
    // 请求超时时间（毫秒）
    timeout: 20000,

    // 请求失败重试次数
    retryTimes: 3,

    // 请求重试间隔时间（毫秒）
    retryDelay: 1000
  },
  // 天地图token
  tdMapToken:[
    '22a4277abffa39525703a9c1521729c2',
    '5793aca922ec3d4c0da703da937a0247'
  ],
  // 高德逆地理编码key
  amapKey: '29bcf1ba579a6d1d0783e4b467171447',

  // 地理服务配置（路径部分，域名使用 api.baseUrl）
  geoServe: {
    tileService: '/static/map/{z}/{x}/{y}.png',
    wmsService: {
      baseUrl: 'http://101.227.41.85:9090',

      baseUrl: 'https://yzriver.sy-digit.com',


      path: '/geoserver/wms',
      layers: 'yzriver:yz_main_river_grandient_1.14',
      // layers: 'yzriver:yz_chief_river_pure2',
      format: 'image/png',
      transparent: true,
      version: '1.1.0',
      attribution: ''
    }
  }


};

/**
 * 配置获取方法
 * 通过点号分隔的路径字符串获取配置值
 * 
 * @param {string} key - 配置路径，如 'api.baseUrl' 或 'modelConfig.sjn.bimId'
 * @returns {*} 配置值，如果路径不存在则返回 undefined
 * 
 * @example
 * // 获取API基础地址
 * const baseUrl = window.getConfig('api.baseUrl');
 * 
 * // 获取BIM模型ID
 * const bimId = window.getConfig('modelConfig.sjn.bimId');
 */
window.getConfig = function (key) {
  // 参数验证
  if (!key || typeof key !== 'string') {
    console.warn('getConfig: 无效的key参数');
    return undefined;
  }

  // 根据点号分割路径，转换为数组
  const keys = key.split('.');

  // 从根配置对象开始查找
  let value = window.APP_CONFIG;

  // 逐层遍历配置对象
  for (const k of keys) {
    // 检查当前值是否为对象且包含指定属性
    if (value && typeof value === 'object' && k in value) {
      value = value[k];
    } else {
      // 路径不存在，返回 undefined
      return undefined;
    }
  }

  return value;
};

/**
 * 配置设置方法
 * 通过点号分隔的路径字符串设置配置值
 * 
 * @param {string} key - 配置路径，如 'api.timeout'
 * @param {*} value - 要设置的配置值
 * 
 * @example
 * // 设置API超时时间
 * window.setConfig('api.timeout', 15000);
 * 
 * // 设置新的配置项
 * window.setConfig('newConfig.item', 'value');
 */
window.setConfig = function (key, value) {
  // 参数验证
  if (!key || typeof key !== 'string') {
    console.warn('setConfig: 无效的key参数');
    return;
  }

  // 根据点号分割路径
  const keys = key.split('.');
  let obj = window.APP_CONFIG;

  // 逐层创建对象结构（如果不存在）
  for (let i = 0; i < keys.length - 1; i++) {
    const k = keys[i];
    if (!(k in obj) || typeof obj[k] !== 'object') {
      obj[k] = {};
    }
    obj = obj[k];
  }

  // 设置最终的值
  obj[keys[keys.length - 1]] = value;
};


