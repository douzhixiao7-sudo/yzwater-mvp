
import axios from 'axios'

import router from '@/router';

import { ElMessage } from 'element-plus'

import JSONbig from 'json-bigint';

// 配置为大整数直接存为字符串
const JSONbigString = JSONbig({ storeAsString: true });

// 创建axios实例
const request = axios.create({

  baseURL: window.getConfig('api.baseUrl') || '',

  timeout: window.getConfig('api.timeout') || 10000

});

// 请求拦截器
request.interceptors.request.use(
  (config) => {

    // 从localStorage获取token
    let token = localStorage.getItem("X-Access-Token");

    config.headers["X-Client-Id"] = window.getConfig('loginAuth.clientId');

    config.headers["X-Access-Token"] = token || '';

    config.headers["Authorization"] = token || '';

    config.headers["X-Auth-Type"] = 'SSO';

    return config;
  },
  (error) => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 如果为401 说明需要登录
    if (response.data.code == 401) {
      // 获取 /h5 以后所有的字符串（包含查询参数）
      const currentPath = router.currentRoute.value.fullPath

      // const roleName = localStorage.getItem('roleName')

      // 1. (用户端)基础信息页面 或 反馈页面 /user_baseInfo，只提示不跳转
      if (currentPath.includes('user_')) {
        ElMessage.warning('您的访问超时，请重新登陆');
        if (currentPath.includes('feedBackList') || currentPath.includes('detail')) {
          router.push('/listLogin')
        } else {
          router.push('/login')
        }
        return response.data
      }

      // 2. (用户端)短链接 --> 如果路径包含 /shortUrlDetail
      if (currentPath.includes('short')) {
        router.push('/listLogin')
        return response.data
      }

      // 管理员端 
      if (currentPath.includes('admin')) {

        ElMessage.warning('您的访问超时,请重新登陆');

        router.push('/adminLogin')


        return response.data
      }

      // }
    }
    return response.data;
  },
  (error) => {

    if (error.code === "ERR_NETWORK") {
      ElMessage.error('网络连接失败');
      return;
    }

    return Promise.reject(error);
  }
);


/**
 * GET请求
 * @param {string} url - 请求地址
 * @param {object} params - 查询参数
 * @returns {Promise} 请求结果
 */
function get(url, params = {}) {
  return request.get(url, { params });
}

/**
 * POST请求
 * @param {string} url - 请求地址
 * @param {object} data - 请求数据
 * @returns {Promise} 请求结果
 */
function post(url, data = {}) {
  return request.post(url, data);
}

/**
 * PUT请求
 * @param {string} url - 请求地址
 * @param {object} data - 请求数据
 * @returns {Promise} 请求结果
 */
function put(url, data = {}) {
  return request.put(url, data);
}

/**
 * DELETE请求
 * @param {string} url - 请求地址
 * @returns {Promise} 请求结果
 */
function del(url) {
  return request.delete(url);
}

// 导出http对象，包含所有请求方法
const http = {
  get,
  post,
  put,
  delete: del,

  // 也可以导出axios实例供高级用法
  axios: request
};

// ==================== WebSocket 封装 ====================
let websock = null;

/**
 * 创建WebSocket连接
 * @param {string} url - WebSocket连接路径
 * @param {Function} onMessage - 消息回调函数
 */
function createWebSocket(url, onMessage) {
  if (typeof WebSocket === "undefined") {
    console.error("您的浏览器不支持WebSocket");
    return false;
  }

  try {

    // const tokenName = localStorage.getItem("tokenName") || "X-Access-Token";

    // const token = localStorage.getItem(tokenName);
    let token = localStorage.getItem("X-Access-Token");

    if (!token) {
      console.error("未找到认证token");
      return false;
    }

    // 优先使用专门的WebSocket配置，否则使用API baseUrl
    const wsBaseUrl = window.getConfig?.('api.wsBaseUrl') || window.getConfig?.('api.baseUrl') || 'ws://localhost:8080';
    const wsUrl = `${wsBaseUrl}${url}?token=${token}`;

    websock = new WebSocket(wsUrl);

    websock.onmessage = function (e) {
      try {
        // 用 JSONbigString 解析，避免大整数丢精度
        const jsonData = JSONbigString.parse(e.data);

        // 如果 data 本身还是字符串包了一层 JSON，再解一层
        if (jsonData.data && typeof jsonData.data === "string") {
          jsonData.data = JSONbigString.parse(jsonData.data);
        }

        if (onMessage) onMessage(jsonData);
      } catch (error) {
        console.error("消息解析失败:", error);
        if (onMessage) onMessage(e.data);
      }
    };

    websock.onopen = function () {
      console.log("WebSocket连接成功");
    };

    websock.onerror = function () {
      console.error("WebSocket连接错误");
    };

    websock.onclose = function (e) {
      console.log("WebSocket连接已关闭");
    };

    return true;
  } catch (error) {
    console.error("WebSocket初始化失败:", error);
    return false;
  }
}

/**
 * 发送WebSocket消息
 * @param {Object} data - 需要发送的数据
 */
function sendWebSocket(data) {
  setTimeout(() => {
    if (websock && websock.readyState === websock.OPEN) {
      websock.send(JSON.stringify(data));
    }
  }, 500);
}

function closeWebSocket() {
  if (websock) {
    console.log('关闭WebSocket连接，当前状态:', websock.readyState);
    websock.close();
    websock = null;
    console.log('WebSocket连接已关闭并清空');
  } else {
    console.log('WebSocket连接不存在，无需关闭');
  }
}

// WebSocket工具对象
const websocket = {
  create: createWebSocket,
  send: sendWebSocket,
  close: closeWebSocket
};

// 默认导出http对象，同时导出websocket
export default http;
export { websocket };
