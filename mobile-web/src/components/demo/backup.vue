
<!-- 用户端反馈页面 -->
<template>
    <div class="user-feedBack-container">
        <!-- 地图背景 -->
        <div class="map-background">
            <Map />
        </div>

        <div class="content-layer">

            <!-- 页面标题组件 -->
            <!-- <PageHeader title="仪征河湖管理平台" @close="handleClose" /> -->

            <button class="test-btn" @click="openDrawer">打开抽屉</button>
        </div>

        <!-- 遮罩层 -->
        <transition name="fade">
            <div v-if="drawerVisible" class="drawer-mask" @click="closeDrawer"></div>
        </transition>

        <!-- 抽屉容器 -->
        <transition name="slide-up">
            <div v-if="drawerVisible" class="drawer-container">
                <div class="drawer-header">
                
                </div>
                <div class="drawer-content">

                    21312312
                </div>
             

            </div>
        </transition>



    </div>
</template>

<script>
import Map from '@/components/leafLetMap/map.vue';
import PageHeader from '@/components/commonCom/PageHeader.vue';
export default {
    name: 'UserFeedBack',
    components: {
        Map, PageHeader
    },
    data() {
        return {
            drawerVisible: false // 控制抽屉显示/隐藏
        }
    },
    mounted() {
        document.title = '仪征河湖管理平台';
        // 页面加载后确保滚动到顶部
        this.scrollToTop()
    },
    methods: {
        // 滚动到顶部
        scrollToTop() {
            window.scrollTo(0, 0)
            document.documentElement.scrollTop = 0
            document.body.scrollTop = 0
        },
        handleClose() {
            this.$router.go(-1);
        },
        // 打开抽屉
        openDrawer() {
            this.drawerVisible = true;
        },
        // 关闭抽屉
        closeDrawer() {
            this.drawerVisible = false;
        },
    }
}
</script>

<style lang="scss" scoped>
.user-feedBack-container {
    position: relative;
    width: 100%;
    min-height: 100vh;
    overflow: hidden;

    // 地图背景层
    .map-background {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100vh;
        z-index: 1;
    }

    // 页面内容层
    .content-layer {
        position: relative;
        z-index: 2;
        width: 100%;
        min-height: 100vh;
        pointer-events: none;

        .test-btn {
            padding: 24px 48px;
            background-color: #007aff;
            color: #fff;
            border-radius: 16px;
            font-size: 32px;
            pointer-events: auto;
        }


    }

    // 遮罩层样式
    .drawer-mask {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, 0.2);
        z-index: 998;
    }

    // 抽屉容器样式
    .drawer-container {
        position: fixed;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: #fff;
        border-radius: 40px 40px 0 0;
        z-index: 999;
        max-height: 80vh;
        display: flex;
        flex-direction: column;
        box-shadow: 0 -8px 40px rgba(0, 0, 0, 0.1);

    }

}


// 抽屉头部
.drawer-header {
    padding: 0px 40px;

    text-align: center;
    position: relative;

    .drawer-handle {
        width: 80px;
        height: 4px;
        background-color: #ddd;
        border-radius: 4px;
        margin: 0 auto 24px;

    }

}

// 抽屉内容区域
.drawer-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

// 遮罩层淡入淡出动画
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

// 抽屉从下到上滑入动画
.slide-up-enter-active,
.slide-up-leave-active {
    transition: transform 0.3s ease-out;
}

.slide-up-enter-from,
.slide-up-leave-to {
    transform: translateY(100%);
}
</style>