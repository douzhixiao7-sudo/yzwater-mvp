<!-- 用户端查看基础信息页面 -->
<template>
    <div class="user-base-info-container">
        <!-- 地图背景 -->
        <div class="map-background">
            <Map @open-drawer="openDrawer" />
        </div>

        <!-- 页面内容层 -->
        <div class="content-layer">
            <!-- 页面标题组件 -->
            <!-- <PageHeader title="仪征河湖管理平台" @close="handleClose" /> -->
        </div>

        <!-- 去反馈：可关闭；隐藏后右侧细条可再次展开 -->
        <div
            v-if="feedbackFabVisible"
            class="floating-feedback-wrap"
            :style="feedbackFabPositionStyle"
            @pointerdown="onFeedbackFabPointerDown"
        >
            <div
                class="floating-feedback-btn"
                role="button"
                aria-label="问题反馈"
                @click.stop="onFeedbackFabActivate"
            ></div>
            <button
                type="button"
                class="floating-feedback-close"
                aria-label="隐藏入口"
                @pointerdown.stop
                @click.stop="hideFeedbackFab"
            >
                ×
            </button>
        </div>
        <button
            v-else
            type="button"
            class="floating-feedback-restore"
            aria-label="显示去反馈"
            :style="feedbackFabPositionStyle"
            @pointerdown="onFeedbackFabPointerDown"
            @click="onFeedbackFabRestoreClick"
        >
            反馈
        </button>

        <!-- 遮罩层 -->
        <transition name="fade">
            <div v-if="drawerVisible" class="drawer-mask" @click="closeDrawer"></div>
        </transition>

        <!-- 抽屉容器 -->
        <transition name="slide-up">
            <div
                v-if="drawerVisible"
                class="drawer-container"
                :class="{ 'drawer-container--river-only': isTownOrVillageSignboard }"
            >
                <div class="drawer-header" :class="{ 'drawer-header--river-only': isTownOrVillageSignboard }">
                    <template v-if="isTownOrVillageSignboard">
                        <div class="drawer-handle" aria-hidden="true"></div>
                        <div class="drawer-title-row">
                            <div class="drawer-title-accent" aria-hidden="true"></div>
                            <div class="drawer-title-block">
                                <div class="drawer-title">河长信息</div>
                            </div>
                        </div>
                    </template>
                </div>
                <div
                    class="drawer-content"
                    :class="{ 'drawer-content--river-only': isTownOrVillageSignboard }"
                >
                    <!-- Tabbar：镇/村 plain，无标签背景图；单页时标签栏隐藏，标题见 drawer-header -->
                    <Tabbar
                        :tabs="displayTabs"
                        :components="displayTabComponents"
                        :default-active-key="defaultTabKey"
                        :plain="isTownOrVillageSignboard"
                        @tab-change="handleTabChange"
                    />
                </div>
            </div>
        </transition>
    </div>
</template>

<script>
import { markRaw } from 'vue';
import Tabbar from '@/components/commonCom/Tabbar.vue';
import BaseInfoContent from '@/components/userBaseInfoCom/BaseInfoContent.vue';
import RiverInfoContent from '@/components/userBaseInfoCom/RiverInfoContent.vue';
import Map from '@/components/leafLetMap/map.vue';
import PageHeader from '@/components/commonCom/PageHeader.vue';

const FEEDBACK_FAB_HIDDEN_KEY = 'user_base_info_feedback_fab_hidden';
/** 悬浮「问题反馈」距视口底部的像素（另加 safe-area-inset-bottom） */
const FEEDBACK_FAB_BOTTOM_PX_KEY = 'user_base_info_feedback_fab_bottom_px';
const FEEDBACK_FAB_DEFAULT_BOTTOM_PX = 120;
const FEEDBACK_FAB_HEIGHT_FULL = 100;
const FEEDBACK_FAB_HEIGHT_SLIM = 80;

/**
 * 镇/村级公示牌：扫描后仅展示「河长信息」。
 * 优先认后端级别字段；否则用河道 riverLevelLabel / riverLevel（如 6j 镇级、8 村级等）。
 */
function isTownOrVillageSignboardPayload(data) {
    if (!data || typeof data !== 'object') return false;

    const textFields = [
        data.signboardLevelLabel,
        data.signboardLevelName,
        data.levelLabel,
        data.boardTypeLabel,
        data.signboardTypeLabel
    ];
    for (let i = 0; i < textFields.length; i++) {
        const t = textFields[i];
        if (t != null && /镇|村/.test(String(t))) return true;
    }

    const code = data.signboardLevel ?? data.boardLevel ?? data.level ?? data.signboardType;
    if (code != null && code !== '') {
        const c = String(code).trim();
        if (c === '6' || c === '8' || c === 6 || c === 8) return true;
    }

    const ch = data.rivers && data.rivers[0] ? data.rivers[0].channel : null;
    if (ch) {
        const rl = ch.riverLevelLabel;
        if (rl != null && /镇|村/.test(String(rl))) return true;
        const rv = ch.riverLevel;
        if (rv != null) {
            const m = String(rv).match(/(\d+)/);
            if (m) {
                const n = parseInt(m[1], 10);
                if (n === 6 || n === 8) return true;
            }
        }
    }

    return false;
}

export default {
    components: {
        Tabbar,
        BaseInfoContent,
        RiverInfoContent,
        Map,
        PageHeader
    },
    computed: {
        // 获取路径中的  qrcode
        getQrcode() {
            return this.$route.query.qrcode
        },
        /** 镇/村级公示牌仅展示河长信息 */
        isTownOrVillageSignboard() {
            return isTownOrVillageSignboardPayload(this.baseInfoData);
        },
        displayTabs() {
            if (this.isTownOrVillageSignboard) {
                return [{ label: '河长信息', key: 'river' }];
            }
            return [
                { label: '基础信息', key: 'base' },
                { label: '河长信息', key: 'river' }
            ];
        },
        displayTabComponents() {
            if (this.isTownOrVillageSignboard) {
                return { river: markRaw(RiverInfoContent) };
            }
            return {
                base: markRaw(BaseInfoContent),
                river: markRaw(RiverInfoContent)
            };
        },
        defaultTabKey() {
            return this.isTownOrVillageSignboard ? 'river' : 'base';
        },
        /** 贴右边框；纵向位置由 feedbackFabBottomPx 控制 */
        feedbackFabPositionStyle() {
            return {
                right: '0',
                left: 'auto',
                bottom: `calc(${this.feedbackFabBottomPx}px + env(safe-area-inset-bottom, 0px))`,
                touchAction: 'none'
            };
        }
    },
    data() {
        return {
            drawerVisible: false, // 控制抽屉显示/隐藏
            baseInfoData:null,
            /** 是否显示完整去反馈悬浮按钮（false 时仅显示右侧「反馈」细条） */
            feedbackFabVisible: true,
            /** 距视口底部像素（不含 safe-area，与历史 calc(120px + …) 对齐） */
            feedbackFabBottomPx: FEEDBACK_FAB_DEFAULT_BOTTOM_PX,
            /** 刚完成纵向拖拽时，抑制一次点击进入反馈 */
            feedbackFabSuppressClick: false,
            /** @type {{ startX: number, startY: number, startBottom: number, dragging: boolean, pointerId?: number } | null} */
            _feedbackFabDrag: null
        }
    },
    created() {
        try {
            this.feedbackFabVisible = localStorage.getItem(FEEDBACK_FAB_HIDDEN_KEY) !== '1';
        } catch (_) {
            this.feedbackFabVisible = true;
        }
        try {
            const raw = localStorage.getItem(FEEDBACK_FAB_BOTTOM_PX_KEY);
            if (raw != null && raw !== '') {
                const n = parseInt(raw, 10);
                if (!Number.isNaN(n) && n >= 0 && n < 5000) {
                    this.feedbackFabBottomPx = n;
                }
            }
        } catch (_) {}
    },
    async mounted() {
        // document.title = '仪征河湖管理平台';
        // 页面加载后确保滚动到顶部
        this.scrollToTop()
        // 将 qrcode 存储到 Vuex 中
        if (this.getQrcode) {
            this.$store.commit('setQrcode', this.getQrcode)
        }

        this.baseInfoData = await this.getBaseInfo()
        this.clampFeedbackFabBottom();
        window.addEventListener('resize', this.clampFeedbackFabBottom);
        if (window.visualViewport) {
            window.visualViewport.addEventListener('resize', this.clampFeedbackFabBottom);
        }
    },
    beforeUnmount() {
        window.removeEventListener('resize', this.clampFeedbackFabBottom);
        if (window.visualViewport) {
            window.visualViewport.removeEventListener('resize', this.clampFeedbackFabBottom);
        }
        this.teardownFeedbackFabDragListeners();
    },
    methods: {
        //接口
        //根据qrcode获取基础信息
        async getBaseInfo() {
            const response = await this.$http.get(`/app-api/signboard/info/by-qr/${this.getQrcode}`, {
            });

            return response.data;
       
        },
   
        // 打开抽屉
        openDrawer() {
            this.drawerVisible = true;
        },
        // 关闭抽屉
        closeDrawer() {
            this.drawerVisible = false;
        },
        // 处理标签切换事件
        handleTabChange(tabKey, index) {
            console.log('标签切换:', tabKey, index);
            // 可以在这里处理标签切换后的逻辑
        },
        // 处理关闭按钮点击
        handleClose() {
            // 可以在这里处理关闭逻辑，比如返回上一页
            this.$router.go(-1);
        },
        // 去反馈：先进入登录页；登录成功后 login.vue 会跳转到 /user_feedBack（qrcode 仍在 Vuex）
        goToFeedBack() {
            this.$router.push({ name: 'Login' });
        },
        onFeedbackFabActivate() {
            if (this.feedbackFabSuppressClick) return;
            this.goToFeedBack();
        },
        onFeedbackFabRestoreClick() {
            if (this.feedbackFabSuppressClick) return;
            this.showFeedbackFab();
        },
        _feedbackFabHeight() {
            return this.feedbackFabVisible ? FEEDBACK_FAB_HEIGHT_FULL : FEEDBACK_FAB_HEIGHT_SLIM;
        },
        _feedbackFabBottomBounds() {
            const vh = window.visualViewport ? window.visualViewport.height : window.innerHeight;
            const h = this._feedbackFabHeight();
            const topReserve = 48;
            const bottomReserve = 12;
            const maxBottom = Math.max(bottomReserve, vh - topReserve - h);
            return { minBottom: bottomReserve, maxBottom };
        },
        clampFeedbackFabBottom() {
            const { minBottom, maxBottom } = this._feedbackFabBottomBounds();
            this.feedbackFabBottomPx = Math.min(
                maxBottom,
                Math.max(minBottom, this.feedbackFabBottomPx)
            );
        },
        persistFeedbackFabBottom() {
            try {
                localStorage.setItem(FEEDBACK_FAB_BOTTOM_PX_KEY, String(this.feedbackFabBottomPx));
            } catch (_) {}
        },
        teardownFeedbackFabDragListeners() {
            window.removeEventListener('pointermove', this.onFeedbackFabPointerMove, {
                passive: false
            });
            window.removeEventListener('pointerup', this.onFeedbackFabPointerUp);
            window.removeEventListener('pointercancel', this.onFeedbackFabPointerUp);
            this._feedbackFabDrag = null;
        },
        onFeedbackFabPointerDown(e) {
            if (e.pointerType === 'mouse' && e.button !== 0) return;
            this._feedbackFabDrag = {
                startX: e.clientX,
                startY: e.clientY,
                startBottom: this.feedbackFabBottomPx,
                dragging: false,
                pointerId: e.pointerId
            };
            window.addEventListener('pointermove', this.onFeedbackFabPointerMove, { passive: false });
            window.addEventListener('pointerup', this.onFeedbackFabPointerUp);
            window.addEventListener('pointercancel', this.onFeedbackFabPointerUp);
        },
        onFeedbackFabPointerMove(e) {
            const st = this._feedbackFabDrag;
            if (!st) return;
            const dx = e.clientX - st.startX;
            const dy = e.clientY - st.startY;
            if (!st.dragging) {
                if (Math.abs(dx) < 10 && Math.abs(dy) < 10) return;
                // 以纵向为主才进入拖拽；横向主导则不移动（不支持左右拖动）
                if (Math.abs(dx) >= Math.abs(dy)) return;
                st.dragging = true;
            }
            e.preventDefault();
            const next = st.startBottom - dy;
            const { minBottom, maxBottom } = this._feedbackFabBottomBounds();
            this.feedbackFabBottomPx = Math.min(maxBottom, Math.max(minBottom, next));
        },
        onFeedbackFabPointerUp() {
            const st = this._feedbackFabDrag;
            if (!st) return;
            const wasDrag = st.dragging;
            this.teardownFeedbackFabDragListeners();
            if (wasDrag) {
                this.persistFeedbackFabBottom();
                this.feedbackFabSuppressClick = true;
                setTimeout(() => {
                    this.feedbackFabSuppressClick = false;
                }, 280);
            }
        },
        hideFeedbackFab() {
            this.feedbackFabVisible = false;
            try {
                localStorage.setItem(FEEDBACK_FAB_HIDDEN_KEY, '1');
            } catch (_) {}
            this.$nextTick(() => this.clampFeedbackFabBottom());
        },
        showFeedbackFab() {
            this.feedbackFabVisible = true;
            try {
                localStorage.removeItem(FEEDBACK_FAB_HIDDEN_KEY);
            } catch (_) {}
            this.$nextTick(() => this.clampFeedbackFabBottom());
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
.user-base-info-container {
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
        width: 100vw;
        min-height: 100vh;
        padding: 0px;
        // 让空白区域不阻止地图交互
        pointer-events: none;
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

    &.drawer-container--river-only {
        border-radius: 28px 28px 0 0;
        box-shadow:
            0 -4px 24px rgba(26, 95, 180, 0.08),
            0 -12px 48px rgba(0, 0, 0, 0.08);
        overflow: hidden;
    }
}

// 抽屉头部
.drawer-header {
    padding: 0px 40px;

    text-align: center;
    position: relative;
    flex-shrink: 0;

    &.drawer-header--river-only {
        text-align: left;
        padding: 8px 24px 18px;
        background: linear-gradient(135deg, #e8f2ff 0%, #f5f9ff 42%, #ffffff 100%);
        border-bottom: none;
        box-shadow:
            inset 0 -1px 0 rgba(43, 140, 255, 0.14),
            0 4px 16px rgba(26, 61, 92, 0.05);

        .drawer-handle {
            margin: 0 auto 12px;
            width: 48px;
            height: 5px;
        }

        .drawer-title-row {
            display: flex;
            align-items: center;
            gap: 14px;
            width: 100%;
        }

        .drawer-title-accent {
            width: 7px;
            height: 46px;
            flex-shrink: 0;
            border-radius: 4px;
            background: linear-gradient(180deg, #1a7bff 0%, #4ec3ff 55%, #7adfff 100%);
            box-shadow:
                2px 0 10px rgba(26, 123, 255, 0.28),
                inset 0 1px 0 rgba(255, 255, 255, 0.45);
            align-self: center;
        }

        .drawer-title-block {
            flex: 1;
            min-width: 0;
            padding: 8px 0 4px;
        }

        .drawer-title {
            font-size: 38px;
            font-weight: 800;
            letter-spacing: 1px;
            color: #0f2d4a;
            line-height: 1.3;

            &::after {
                content: '';
                display: block;
                width: 112px;
                max-width: 55%;
                height: 4px;
                margin-top: 12px;
                border-radius: 2px;
                background: linear-gradient(90deg, #1a7bff 0%, #52c4ff 70%, rgba(122, 223, 255, 0.35) 100%);
            }
        }
    }

    .drawer-handle {
        width: 56px;
        height: 6px;
        background: rgba(0, 0, 0, 0.1);
        border-radius: 3px;
        margin: 0 auto 18px;
    }

    .drawer-title {
        font-size: 34px;
        font-weight: 700;
        color: #3d3d3d;
        letter-spacing: 1px;
    }

}

// 抽屉内容区域
.drawer-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    &.drawer-content--river-only {
        :deep(.tab-content) {
            padding: 14px 24px 24px;
            /* 自右下角的冷色晕（略加深） */
            background: radial-gradient(
                ellipse 125% 110% at 100% 100%,
                rgba(95, 150, 215, 0.62) 0%,
                rgba(165, 200, 235, 0.42) 26%,
                rgba(215, 232, 250, 0.78) 54%,
                rgba(241, 246, 252, 0.95) 82%,
                #ffffff 100%
            );
        }
    }
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

// 去反馈外层：右下角；高于抽屉
.floating-feedback-wrap {
    position: fixed;
    width: 100px;
    height: 100px;
    z-index: 1001;
    pointer-events: auto;
    user-select: none;
    -webkit-user-select: none;
}

// 悬浮「去反馈」主按钮
.floating-feedback-btn {
    width: 100%;
    height: 100%;
    background: url('@/assets/img/gotoFeedBack.png') no-repeat center center;
    background-size: 100% 100%;
    cursor: pointer;
    transition: transform 0.2s ease, opacity 0.2s ease;

    &:active {
        transform: scale(0.95);
        opacity: 0.9;
    }
}

// 关闭（隐藏大图入口，保留右侧细条）
.floating-feedback-close {
    position: absolute;
    top: -6px;
    right: -2px;
    width: 40px;
    height: 40px;
    padding: 0;
    border: none;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.45);
    color: #fff;
    font-size: 28px;
    line-height: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    -webkit-tap-highlight-color: transparent;

    &:active {
        background: rgba(0, 0, 0, 0.6);
    }
}

// 隐藏后主按钮后：右侧细条恢复
.floating-feedback-restore {
    position: fixed;
    z-index: 1001;
    margin: 0;
    padding: 12px 10px;
    min-width: 44px;
    min-height: 72px;
    border: none;
    border-radius: 10px 0 0 10px;
    background: rgba(52, 157, 255, 0.92);
    color: #fff;
    font-size: 26px;
    font-weight: 600;
    letter-spacing: 2px;
    writing-mode: vertical-rl;
    text-orientation: upright;
    cursor: pointer;
    box-shadow: -2px 2px 12px rgba(0, 0, 0, 0.12);
    -webkit-tap-highlight-color: transparent;
    user-select: none;
    -webkit-user-select: none;

    &:active {
        opacity: 0.88;
    }
}
</style>