<template>
  <div class="home-page">
    <!-- <PageHeader title="首页" @close="handleClose" /> -->
    <!-- 地图组件容器 -->
    <div class="map-container">
      <AdminMap />
    </div>
  </div>
</template>

<script>
import PageHeader from '@/components/commonCom/PageHeader.vue'
import AdminMap from './adminMap.vue'

export default {
  name: 'AdminHomePage',
  components: {
    PageHeader,
    AdminMap
  },
  mounted() {
    // document.title = '首页';
  },
  methods: {
    handleClose() {
      // 可以在这里处理关闭逻辑
      this.$router.go(-1)
    }
  }
}
</script>

<style lang="scss" scoped>
.home-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  overflow: hidden;
}

.map-container {
  flex: 1;
  width: 100%;
  height: 100%;
  min-height: 0; // 确保 flex 子元素可以正确收缩
  overflow: hidden;
}
</style>

