<template>
  <div class="admin-my-page">
    <!-- <PageHeader title="我的" @close="handleClose" /> -->
    <div class="content-placeholder">
      <h2 class="placeholder-title">我的</h2>
    </div>
  </div>
</template>

<script>
import PageHeader from '@/components/commonCom/PageHeader.vue'

export default {
  name: 'AdminMyPage',
  components: {
    PageHeader
  },
  mounted() {
    // document.title = '我的';
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
.admin-my-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.content-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;

  .placeholder-title {
    font-size: 48px;
    font-weight: 500;
    color: #333;
  }
}
</style>

