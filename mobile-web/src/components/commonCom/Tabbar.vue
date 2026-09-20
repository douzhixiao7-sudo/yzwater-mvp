<!-- Tabbar 组件 -->
<template>
    <div class="tabbar-container">
        <!-- Tabbar 标签栏（镇/村单页 plain 时由父级抽屉标题承接，此处隐藏） -->
        <div
            v-if="showTabbarRow"
            class="tabbar"
            :class="{ 'tabbar--plain': plain }"
            :style="tabbarStyle"
        >
            <div 
                v-for="(tab, index) in tabs" 
                :key="index"
                class="tab-item" 
                :class="{ active: activeTab === tab.key }"
                @click="switchTab(tab.key, index)"
            >
                {{ tab.label }}
            </div>
        </div>
        
        <!-- 标签内容区域 -->
        <div class="tab-content">
            <transition name="tab-fade" mode="out-in">
                <component 
                    :is="currentComponent" 
                    :key="activeTab"
                    class="tab-panel"
                />
            </transition>
        </div>
    </div>
</template>

<script>
import { markRaw } from 'vue';
import tabbarDefault from '@/assets/img/tabbar-default.png';
import tabbarActive from '@/assets/img/tabbar-active.png';
import tabbar1 from '@/assets/img/tabbar1.png';
import tabbar2 from '@/assets/img/tabbar2.png';
import tabbar3 from '@/assets/img/tabbar3.png';

export default {
    name: 'Tabbar',
    props: {
        // 标签数组，格式: [{ label: '标签名', key: '标签key' }]
        tabs: {
            type: Array,
            required: true,
            default: () => []
        },
        // 组件映射对象，格式: { '标签key': 组件 }
        components: {
            type: Object,
            required: true,
            default: () => ({})
        },
        // 默认激活的标签 key
        defaultActiveKey: {
            type: String,
            default: ''
        },
        /** 为 true 时不使用 tab 装饰背景图（纯色+底边线） */
        plain: {
            type: Boolean,
            default: false
        }
    },
    data() {
        // 初始化时找到默认激活标签的索引
        const defaultIndex = this.tabs.findIndex(tab => tab.key === (this.defaultActiveKey || (this.tabs.length > 0 ? this.tabs[0].key : '')));
        return {
            activeTab: this.defaultActiveKey || (this.tabs.length > 0 ? this.tabs[0].key : ''),
            currentIndex: defaultIndex >= 0 ? defaultIndex : 0 // 当前激活的标签索引
        }
    },
    computed: {
        /** 镇/村仅一页：不展示标签条，由外层抽屉标题展示 */
        showTabbarRow() {
            if (this.plain && this.tabs.length === 1) return false;
            return true;
        },
        // 根据标签数量和当前索引来动态切换背景图片（非 plain 时使用）
        decorativeTabbarStyle() {
            let bgImage;
            
            // 如果是2个标签，使用原有的逻辑
            if (this.tabs.length === 2) {
                // 根据 currentIndex 决定使用哪个背景图
                // index 0 使用 default, index 1 使用 active
                bgImage = this.currentIndex === 0 ? tabbarDefault : tabbarActive;
            } 
            // 如果是3个标签，使用 tabbar1, tabbar2, tabbar3
            else if (this.tabs.length === 3) {
                const bgImages = [tabbar1, tabbar2, tabbar3];
                bgImage = bgImages[this.currentIndex] || bgImages[0];
            }
            // 其他情况使用默认背景
            else {
                bgImage = tabbarDefault;
            }
            
            return {
                backgroundImage: `url(${bgImage})`,
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'center center',
                backgroundSize: '100% 100%'
            };
        },
        tabbarStyle() {
            if (this.plain) {
                return {
                    backgroundImage: 'none',
                    backgroundColor: '#ffffff',
                    borderBottom: '1px solid #eeeeee',
                    backgroundRepeat: 'no-repeat',
                    backgroundPosition: 'center center',
                    backgroundSize: '100% 100%'
                };
            }
            return this.decorativeTabbarStyle;
        },
        // 当前激活的组件，使用 markRaw 处理组件，避免响应式化警告
        currentComponent() {
            const component = this.components[this.activeTab];
            // 如果组件存在且不是 markRaw 标记的，则标记它
            return component ? (component.__v_isReactive ? markRaw(component) : component) : null;
        }
    },
    methods: {
        // 切换标签
        switchTab(tabKey, index) {
            // 更新当前索引，用于切换背景图
            this.currentIndex = index;
            this.activeTab = tabKey;
            // 触发切换事件，让父组件知道
            this.$emit('tab-change', tabKey, index);
        }
    },
    watch: {
        // 监听 tabs 变化，更新 currentIndex
        tabs: {
            handler(newTabs) {
                if (newTabs && newTabs.length > 0) {
                    const defaultIndex = newTabs.findIndex(tab => tab.key === this.activeTab);
                    if (defaultIndex >= 0) {
                        this.currentIndex = defaultIndex;
                    }
                }
            },
            immediate: true
        },
        // 监听 defaultActiveKey 变化，更新 activeTab 和 currentIndex
        defaultActiveKey: {
            handler(newKey) {
                if (newKey) {
                    this.activeTab = newKey;
                    const index = this.tabs.findIndex(tab => tab.key === newKey);
                    if (index >= 0) {
                        this.currentIndex = index;
                    }
                }
            },
            immediate: true
        }
    }
}
</script>

<style lang="scss" scoped>
.tabbar-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

// Tabbar 标签栏
.tabbar {
    display: flex;
    padding: 20px 40px;
    border-radius: 40px 40px 0 0;
    // 统一过渡时间，与文字过渡同步
    transition: background-image 0.35s cubic-bezier(0.4, 0, 0.2, 1);
    // 开启硬件加速
    will-change: background-image;

    &.tabbar--plain {
        transition: border-color 0.2s ease;
        will-change: auto;
    }
    
    .tab-item {
        flex: 1;
        text-align: center;
        padding: 8px 0;
        font-size: 28px;
        color: #3D3D3D;
        position: relative;
        cursor: pointer;
        font-weight: 500;
        // 使用 transform 代替 font-size 变化，避免重排，提升性能
        transform: scale(1);
        // 添加所有需要过渡的属性，使用更平滑的缓动函数
        // 统一使用相同的过渡时间和缓动函数，确保同步
        transition: color 0.35s cubic-bezier(0.4, 0, 0.2, 1),
                    font-weight 0.35s cubic-bezier(0.4, 0, 0.2, 1),
                    transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
        // 开启硬件加速，优化性能
        will-change: transform, color, font-weight;
        // 防止文字选中时的闪烁
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        
        &.active {
            font-weight: 700;
            // 使用 scale 代替 font-size，16/14 ≈ 1.143，避免重排
            transform: scale(1.143);
            color: #3D3D3D;
        }
    }
}

// 标签内容区域
.tab-content {
    flex: 1;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
    padding: 24px;
    position: relative;
    
    .tab-panel {
        width: 100%;
        height: auto;
        min-height: 100%;
    }
}

// Tab 内容切换动画
.tab-fade-enter-active {
    transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1), transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-fade-leave-active {
    transition: opacity 0.25s cubic-bezier(0.4, 0, 0.2, 1), transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-fade-enter-from {
    opacity: 0;
    transform: translateX(20px);
}

.tab-fade-leave-to {
    opacity: 0;
    transform: translateX(-20px);
}
</style>

