<template>
  <div class="admin-page">
    <div class="page-content">
      <!-- 根据当前 tab 切换显示不同的组件 -->
      <component :is="currentComponent" />
    </div>
    
    <!-- 底部菜单 -->
    <BottomMenu 
      :show-home="true" 
      :show-feed-back="true" 
      :show-mine="true"
      home-route-name="AdminPage"
      feed-back-route-name="AdminPage"
      mine-route-name="AdminPage"
      @tab-change="handleTabChange"
    />
  </div>
</template>

<script>
import { markRaw } from 'vue'
import BottomMenu from '@/components/commonCom/BottomMenu.vue'
import HomePage from './homePage.vue'
import AdminFeedBack from './adminFeedBack.vue'
import AdminMyPage from './myPage.vue'

export default {
  name: 'AdminPage',
  components: {
    BottomMenu,
    HomePage,
    AdminFeedBack,
    AdminMyPage
  },
  data() {
    return {
      currentTab: 'home' // 当前激活的 tab: home, feedback, mine
    }
  },
  computed: {
    // 根据当前 tab 返回对应的组件
    currentComponent() {
      const componentMap = {
        home: markRaw(HomePage),
        feedback: markRaw(AdminFeedBack),
        mine: markRaw(AdminMyPage)
      }
      return componentMap[this.currentTab] || componentMap.home
    }
  },
  watch: {
    // 监听路由查询参数变化
    '$route.query.tab'(newTab) {
      if (newTab && ['home', 'feedback', 'mine'].includes(newTab)) {
        this.currentTab = newTab
      }
    }
  },
  mounted() {
    // 从路由查询参数获取当前 tab
    const tab = this.$route.query.tab
    if (tab && ['home', 'feedback', 'mine'].includes(tab)) {
      this.currentTab = tab
    } else {
      // 默认显示首页
      this.currentTab = 'home'
      // 更新路由查询参数
      this.$router.replace({ query: { tab: 'home' } })
    }
    // 页面加载后确保滚动到顶部
    this.scrollToTop()
  },
  methods: {
    // 处理 tab 切换
    handleTabChange(tab) {
      this.currentTab = tab
      // 更新路由查询参数，但不触发页面跳转
      this.$router.replace({ query: { tab } })
    },
    // 滚动到顶部
    scrollToTop() {
      window.scrollTo(0, 0)
      document.documentElement.scrollTop = 0
      document.body.scrollTop = 0
    }
  }
}
</script>

<style lang="scss" scoped>
.admin-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  position: relative;
  overflow: hidden;
}

.page-content {
  flex: 1;
  width: 100%;
  height: 100%;
  min-height: 0; // 确保 flex 子元素可以正确收缩
  overflow: hidden;
}
</style>

