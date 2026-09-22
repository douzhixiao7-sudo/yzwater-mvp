import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import '@/assets/style/index.scss'
// 引入 Element Plus 样式
import 'element-plus/dist/index.css'
//引入工具
import tool from "@/utils/tools.js";
import http from "@/utils/request.js";
// 引入 Ant Design Vue
// import Antd from 'ant-design-vue';
// import 'ant-design-vue/dist/reset.css';
// 自动加载高德地图 API
import { autoLoadAmapAPI } from '@/utils/loadAmap'

const app = createApp(App);

// 挂载工具函数到Vue实例
app.config.globalProperties.$tool = tool;
app.config.globalProperties.resolveFileUrl = tool.resolveFileUrl;
// 挂载http函数到Vue实例
app.config.globalProperties.$http = http;

// Options API 组件可直接用 resolveFileUrl()
app.mixin({
  methods: {
    resolveFileUrl(url) {
      return tool.resolveFileUrl(url)
    }
  }
})
app.use(router)
app.use(store)
// app.use(Antd)

// 自动加载高德地图 API
autoLoadAmapAPI()

app.mount('#app')

