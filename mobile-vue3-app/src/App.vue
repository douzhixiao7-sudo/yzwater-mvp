<template>
  <el-config-provider :locale="zhCn">
    <div id="app">
      <router-view />
      <!-- 全局底部导航栏（仅在安卓设备上显示，且路由 meta.showBackButton 为 true） -->
      <!-- <NavigationBar /> -->
    </div>
  </el-config-provider>
</template>

<script>
// import NavigationBar from '@/components/commonCom/NavigationBar.vue'
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

export default {
  name: 'App',
  components: {
    // NavigationBar,
    ElConfigProvider
  },
  data() {
    return {
      zhCn
    }
  },
  mounted() {
    // 页面加载后确保滚动到顶部
    window.scrollTo(0, 0)
    // 兼容移动端
    document.documentElement.scrollTop = 0
    document.body.scrollTop = 0
    
    // 动态设置页面标题
    this.updatePageTitle()
    
    // 监听 localStorage 变化，以便在 roleName 改变时更新标题
    window.addEventListener('storage', this.handleStorageChange)
    // 监听自定义事件，用于同窗口内的 localStorage 变化
    window.addEventListener('roleNameChanged', this.updatePageTitle)
  },
  beforeUnmount() {
    // 清理事件监听器
    window.removeEventListener('storage', this.handleStorageChange)
    window.removeEventListener('roleNameChanged', this.updatePageTitle)
  },
  methods: {
    // 更新页面标题
    updatePageTitle() {
      return ;
      // 获取当前路由路径
      const currentPath = this.$route ? this.$route.path : window.location.pathname
      
      // 优先判断路径：如果路径包含指定关键词，则显示"河道问题反馈"
      const userPaths = ['/login', '/user', '/user_feedBack']
      const isUserPath = userPaths.some(keyword => currentPath.includes(keyword))
      
      if (isUserPath) {
        document.title = '河道问题反馈'
        return
      }

      // 包含user_baseInfo路径
      if (currentPath.includes('/user_baseInfo')) {
        document.title = '仪征河湖管理平台'
        return
      }
      
      // 如果路径不匹配，再检查 roleName
      const roleName = localStorage.getItem('roleName')
      if (roleName === '游客') {
        document.title = '河道问题反馈'
      } else {
        document.title = '仪征河湖管理平台'
      }
    },
    // 处理 localStorage 变化事件
    handleStorageChange(event) {
      if (event.key === 'roleName') {
        this.updatePageTitle()
      }
    }
  },
  watch: {
    // 监听路由变化，确保标题正确
    '$route'() {
      this.updatePageTitle()
    }
  }
}
</script>

<style lang="scss">

</style>

