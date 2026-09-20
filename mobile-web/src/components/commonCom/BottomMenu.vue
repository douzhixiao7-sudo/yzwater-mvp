<template>
    <div class="bottom-menu" :class="{ 'has-bottom-nav': hasBottomNav }">
        <!-- 首页按钮（可选） -->
        <div 
            v-if="showHome" 
            class="menu-item" 
            :class="{ active: isHomeActive }" 
            @click="goToHome"
        >
            <img :src="isHomeActive ? homeActiveIcon : homeIcon" alt="首页" />
            <span class="menu-label" :class="{ active: isHomeActive }">首页</span>
        </div>
        
        <!-- 反馈按钮（可选） -->
        <div 
            v-if="showFeedBack" 
            class="menu-item" 
            :class="{ active: isFeedBackActive }" 
            @click="goToFeedBack"
        >
            <img :src="isFeedBackActive ? feedBackActiveIcon : feedBackIcon" alt="反馈" />
            <span class="menu-label" :class="{ active: isFeedBackActive }">公众反馈</span>
        </div>
        
        <!-- 我的按钮（可选） -->
        <div 
            v-if="showMine" 
            class="menu-item" 
            :class="{ active: isMineActive }" 
            @click="goToMine"
        >
            <img :src="isMineActive ? mineActiveIcon : mineIcon" alt="我的" />
            <span class="menu-label" :class="{ active: isMineActive }">我的</span>
        </div>
    </div>
</template>

<script>
import feedBackIcon from '@/assets/img/shortUrlFeedBack.png';
import feedBackActiveIcon from '@/assets/img/shortUrlFeedBack-active.png';
import mineIcon from '@/assets/img/shortUrlMine.png';
import mineActiveIcon from '@/assets/img/shortUrlMine-active.png';
import homeIcon from '@/assets/img/homePageIcon.png.png';
import homeActiveIcon from '@/assets/img/homePageActive.png';
import tool from '@/utils/tools';

export default {
    name: 'BottomMenu',
    props: {
        // 是否显示首页按钮
        showHome: {
            type: Boolean,
            default: false
        },
        // 是否显示反馈按钮
        showFeedBack: {
            type: Boolean,
            default: false
        },
        // 是否显示我的按钮
        showMine: {
            type: Boolean,
            default: false
        },
        // 首页路由名称
        homeRouteName: {
            type: String,
            default: 'AdminPage'
        },
        // 反馈路由名称
        feedBackRouteName: {
            type: String,
            default: 'FeedBackList'
        },
        // 我的路由名称
        mineRouteName: {
            type: String,
            default: 'MyPage'
        }
    },
    data() {
        return {
            feedBackIcon,
            feedBackActiveIcon,
            mineIcon,
            mineActiveIcon,
            homeIcon,
            homeActiveIcon
        };
    },
    computed: {
        // 判断是否有底部返回按钮（安卓设备且路由 meta.showBackButton 为 true）
        hasBottomNav() {
            return false;
            // 只在安卓设备上显示
            const isAndroid = tool.isAndroid();
            const hasMeta = this.$route && this.$route.meta && this.$route.meta.showBackButton === true;
            return isAndroid && hasMeta;
        },
        // 当前路由名称
        currentRoute() {
            return this.$route.name;
        },
        // 当前查询参数中的 tab
        currentTab() {
            return this.$route.query.tab || 'home';
        },
        // 首页菜单是否激活
        isHomeActive() {
            if (!this.showHome) return false;
            // 如果所有菜单都指向同一个路由，则通过查询参数判断
            if (this.homeRouteName === this.feedBackRouteName && 
                this.homeRouteName === this.mineRouteName) {
                return this.currentRoute === this.homeRouteName && this.currentTab === 'home';
            }
            return this.currentRoute === this.homeRouteName;
        },
        // 反馈菜单是否激活
        isFeedBackActive() {
            if (!this.showFeedBack) return false;
            // 如果所有菜单都指向同一个路由，则通过查询参数判断
            if (this.homeRouteName === this.feedBackRouteName && 
                this.homeRouteName === this.mineRouteName) {
                return this.currentRoute === this.feedBackRouteName && this.currentTab === 'feedback';
            }
            return this.currentRoute === this.feedBackRouteName;
        },
        // 我的菜单是否激活
        isMineActive() {
            if (!this.showMine) return false;
            // 如果所有菜单都指向同一个路由，则通过查询参数判断
            if (this.homeRouteName === this.feedBackRouteName && 
                this.homeRouteName === this.mineRouteName) {
                return this.currentRoute === this.mineRouteName && this.currentTab === 'mine';
            }
            return this.currentRoute === this.mineRouteName;
        }
    },
    methods: {
        // 前往首页
        goToHome() {
            // 如果所有菜单都指向同一个路由，则通过查询参数切换
            if (this.homeRouteName === this.feedBackRouteName && 
                this.homeRouteName === this.mineRouteName) {
                if (this.currentTab !== 'home') {
                    this.$router.replace({ query: { tab: 'home' } });
                    this.$emit('tab-change', 'home');
                }
            } else {
                if (this.$route.name !== this.homeRouteName) {
                    this.$router.push({ name: this.homeRouteName });
                }
            }
        },
        // 前往反馈列表（主页面）
        goToFeedBack() {
            // 如果所有菜单都指向同一个路由，则通过查询参数切换
            if (this.homeRouteName === this.feedBackRouteName && 
                this.homeRouteName === this.mineRouteName) {
                if (this.currentTab !== 'feedback') {
                    this.$router.replace({ query: { tab: 'feedback' } });
                    this.$emit('tab-change', 'feedback');
                }
            } else {
                if (this.$route.name !== this.feedBackRouteName) {
                    this.$router.push({ name: this.feedBackRouteName });
                }
            }
        },
        // 前往我的页面
        goToMine() {
            // 如果所有菜单都指向同一个路由，则通过查询参数切换
            if (this.homeRouteName === this.feedBackRouteName && 
                this.homeRouteName === this.mineRouteName) {
                if (this.currentTab !== 'mine') {
                    this.$router.replace({ query: { tab: 'mine' } });
                    this.$emit('tab-change', 'mine');
                }
            } else {
                if (this.$route.name !== this.mineRouteName) {
                    this.$router.push({ name: this.mineRouteName });
                }
            }
        }
    }
};
</script>

<style lang="scss" scoped>
// 底部菜单栏样式
.bottom-menu {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    height: 100px;
    background-color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: space-around;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
    z-index: 1000;
    box-sizing: border-box;
    padding: 0 24px;
    
    // 如果有底部返回按钮（安卓设备），需要往上移动
    // NavigationBar 高度：padding-top(14px) + 按钮高度(约28px) + padding-bottom(14px + 安全区域)
    // 总高度约：56px + 安全区域，但为了保险起见，使用更大的值
    &.has-bottom-nav {
        // 计算 NavigationBar 的实际高度：14px + 28px + 14px = 56px，加上安全区域
        bottom: 95px !important;
    }

    .menu-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
        cursor: pointer;
        transition: opacity 0.3s ease;

        img {
            width: 38px;
            height: 38px;
            object-fit: contain;
            margin-bottom: 4px;
        }

        .menu-label {
            font-size: 24px;
            color: #3d3d3d;
            font-weight: 500;
            line-height: 1;
            transition: color 0.3s ease;

            &.active {
                color: #349DFF;
            }
        }

        &:active {
            opacity: 0.7;
        }
    }
}
</style>

