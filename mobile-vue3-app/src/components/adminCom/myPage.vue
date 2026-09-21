<template>
    <div class="my-page-container">
        <div class="content-layer">
            <!-- <PageHeader title="我的" @close="handleClose" /> -->
            <div class="paddingContent">
                <div class="content-container">
                    <!-- Logo -->
                    <div class="logo-section">
                        <img src="@/assets/img/loginLogo.png" alt="Logo" class="app-logo" />
                    </div>

                    <!-- 应用标题 -->
                    <div class="app-title">仪征河长制</div>

                    <!-- 版本号 -->
                    <div class="app-version">版本号V1.2</div>

                    <!-- 退出登录按钮 -->
                    <button class="logout-btn" @click="handleLogout">退出登录</button>
                </div>
            </div>
        </div>

        <!-- 底部菜单栏 -->
        <BottomMenu 
            v-if="currentRoute === 'FeedBackList' || currentRoute === 'MyPage'"
            :show-home="false"
            :show-feed-back="true"
            :show-mine="true"
        />
    </div>
</template>

<script>
import PageHeader from '@/components/commonCom/PageHeader.vue';
import BottomMenu from '@/components/commonCom/BottomMenu.vue';

export default {
    name: 'MyPage',
    components: {
        PageHeader,
        BottomMenu
    },
    data() {
        return {
            myPageList: []
        };
    },
    computed: {
        // 当前路由名称
        currentRoute() {
            return this.$route.name;
        }
    },
    mounted() {
        // document.title = '我的';
    },
    methods: {
        // 关闭页面
        handleClose() {
            this.$router.go(-1);
        },
        // 退出登录
        handleLogout() {
            // 清除token
            localStorage.removeItem('X-Access-Token');
            // 跳转到管理员登录页面
            this.$router.push({ name: 'AdminLogin' });
        }
    }
};
</script>

<style lang="scss" scoped>
.my-page-container {
    position: relative;
    width: 100%;
    height: 100vh;
    background-color: #f5f5f5;

    .content-layer {
        width: 100%;
        height: 98.5vh;
        display: flex;
        flex-direction: column;
        padding-bottom: 100px; // 为底部菜单栏留出空间
        box-sizing: border-box;

        .paddingContent {
            flex: 1;
            padding: 0;
            display: flex;
            flex-direction: column;
            align-items: center;
            padding-top: 16px;
            overflow-y: auto;

            .content-container {
                width: 702px;
                height: 100%;
                opacity: 1;
                background: #ffffff;
                padding: 24px;
                box-sizing: border-box;
                display: flex;
                flex-direction: column;
                align-items: center;

                .logo-section {
                    margin-top: 220px;
                    margin-bottom: 32px;

                    .app-logo {
                        width: 160px;
                        height: 160px;
                        object-fit: contain;
                    }
                }

                .app-title {
                    font-size: 36px;
                    font-weight: 500;
                    color: #3d3d3d;
                    margin-bottom: 16px;
                    text-align: center;
                }

                .app-version {
                    font-size: 28px;
                    color: #999999;
                    margin-bottom: 192px;
                    text-align: center;
                }

                .logout-btn {
                    width: 338px;
                    height: 96px;
                    border-radius: 6px;
                    opacity: 1;
                    border: 1px solid #349dff;
                    background: #ffffff;
                    font-size: 32px;
                    color:#349DFF;
                    cursor: pointer;
                    transition: all 0.3s ease;

                    &:active {
                        opacity: 0.7;
                    }
                }
            }
        }
    }
}
</style>
