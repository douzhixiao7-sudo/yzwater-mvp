<template>
  <div class="home-welcome">
    <ContentWrap>
      <div class="home-welcome__card">
        <div class="home-welcome__grid" aria-hidden="true"></div>
        <div class="home-welcome__scan" aria-hidden="true"></div>

        <div class="home-welcome__accent-line" aria-hidden="true"></div>

        <header class="home-welcome__header">
          <div class="home-welcome__brand">
            <h1 class="home-welcome__title">欢迎使用</h1>
            <p class="home-welcome__subtitle">{{ appTitle }}</p>
          </div>

          <div class="home-welcome__hud" aria-live="polite">
            <div class="home-welcome__hud-corner home-welcome__hud-corner--tl"></div>
            <div class="home-welcome__hud-corner home-welcome__hud-corner--tr"></div>
            <div class="home-welcome__hud-corner home-welcome__hud-corner--bl"></div>
            <div class="home-welcome__hud-corner home-welcome__hud-corner--br"></div>
            <span class="home-welcome__hud-label">当前时间</span>
            <p class="home-welcome__hud-date">{{ datePart }}</p>
            <p class="home-welcome__hud-clock">{{ clockPart }}</p>
          </div>
        </header>

        <div class="home-welcome__divider" aria-hidden="true"></div>

        <p class="home-welcome__hint">
          <span class="home-welcome__hint-dot"></span>
          您可通过左侧菜单进入各业务功能模块。
        </p>
      </div>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

defineOptions({ name: 'HomeWelcome' })

const appTitle = import.meta.env.VITE_APP_TITLE || '仪征防汛抗旱平台'

const WEEK_LABELS = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

const datePart = ref('')
const clockPart = ref('')

function tick(): void {
  const d = new Date()
  const y = d.getFullYear()
  const m = d.getMonth() + 1
  const day = d.getDate()
  const w = WEEK_LABELS[d.getDay()]
  const p2 = (n: number) => String(n).padStart(2, '0')
  datePart.value = `${y}年${m}月${day}日 · ${w}`
  clockPart.value = `${p2(d.getHours())}:${p2(d.getMinutes())}:${p2(d.getSeconds())}`
}

let timer: ReturnType<typeof setInterval> | undefined

onMounted(() => {
  tick()
  timer = setInterval(tick, 1000)
})

onBeforeUnmount(() => {
  if (timer != null) {
    clearInterval(timer)
    timer = undefined
  }
})
</script>

<style scoped lang="scss">
.tech-font {
  font-family:
    ui-monospace,
    'SF Mono',
    'Cascadia Code',
    Menlo,
    Monaco,
    Consolas,
    'Liberation Mono',
    monospace;
}

.home-welcome {
  position: relative;
  isolation: isolate;

  :deep(.content-wrap.el-card),
  :deep(.el-card.content-wrap),
  :deep(.el-card) {
    overflow: visible;
    background: transparent !important;
    box-shadow: none !important;
    border: none !important;
  }

  :deep(.el-card__body) {
    padding: 0 !important;
    background: transparent !important;
  }

  &__card {
    position: relative;
    overflow: hidden;
    border-radius: 14px;
    padding: 28px 26px 24px;
    background: linear-gradient(155deg, #112338 0%, #153352 38%, #1a4870 100%);
    border: 1px solid rgba(56, 189, 248, 0.42);
    box-shadow:
      0 0 0 1px rgba(165, 243, 252, 0.12) inset,
      0 0 56px rgba(34, 211, 238, 0.18),
      0 20px 44px rgba(0, 0, 0, 0.22);
  }

  &__grid {
    position: absolute;
    inset: 0;
    opacity: 0.58;
    background-image:
      linear-gradient(rgba(103, 232, 249, 0.1) 1px, transparent 1px),
      linear-gradient(90deg, rgba(103, 232, 249, 0.1) 1px, transparent 1px);
    background-size: 32px 32px;
    mask-image: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.9) 18%, transparent 92%);
    pointer-events: none;
  }

  &__scan {
    position: absolute;
    inset: -40% 0;
    pointer-events: none;
    background: linear-gradient(
      180deg,
      transparent 0%,
      rgba(103, 232, 249, 0.1) 48%,
      transparent 96%
    );
    animation: home-welcome-scan 5.5s ease-in-out infinite;
  }

  &__accent-line {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(
      90deg,
      transparent,
      rgba(103, 232, 249, 0.45) 12%,
      rgba(125, 211, 252, 1) 50%,
      rgba(147, 197, 253, 0.75) 86%,
      transparent
    );
    opacity: 1;
  }

  &__header {
    position: relative;
    z-index: 1;
    display: flex;
    flex-wrap: wrap;
    align-items: flex-start;
    justify-content: space-between;
    gap: 22px 28px;
  }

  &__brand {
    flex: 1;
    min-width: 220px;
  }

  &__title {
    margin: 0 0 10px;
    font-size: 28px;
    font-weight: 700;
    letter-spacing: 0.06em;
    color: #ffffff;
    text-shadow:
      0 0 24px rgba(103, 232, 249, 0.45),
      0 2px 0 rgba(0, 0, 0, 0.2);
  }

  &__subtitle {
    margin: 0;
    font-size: 14px;
    line-height: 1.65;
    color: rgba(207, 232, 252, 0.95);
    max-width: 420px;
  }

  /* HUD 时间卡片 */
  &__hud {
    @extend .tech-font;
    position: relative;
    min-width: 240px;
    padding: 16px 18px 14px;
    border-radius: 10px;
    background: linear-gradient(160deg, rgba(30, 64, 102, 0.72) 0%, rgba(17, 46, 78, 0.88) 100%);
    border: 1px solid rgba(103, 232, 249, 0.45);
    box-shadow:
      0 0 32px rgba(34, 211, 238, 0.2),
      0 0 0 1px rgba(255, 255, 255, 0.08) inset;
    flex-shrink: 0;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: inherit;
      background: radial-gradient(circle at 80% -20%, rgba(125, 211, 252, 0.28), transparent 55%);
      pointer-events: none;
    }
  }

  &__hud-label {
    position: relative;
    display: block;
    font-size: 10px;
    font-weight: 700;
    letter-spacing: 0.26em;
    color: rgba(165, 243, 252, 0.95);
    margin-bottom: 8px;
  }

  &__hud-date {
    position: relative;
    margin: 0 0 4px;
    font-size: 13px;
    font-weight: 500;
    color: rgba(224, 242, 254, 0.95);
    letter-spacing: 0.04em;
  }

  &__hud-clock {
    position: relative;
    margin: 0;
    font-size: 28px;
    font-weight: 600;
    line-height: 1.15;
    color: #7af7e6;
    text-shadow:
      0 0 22px rgba(45, 212, 191, 0.65),
      0 0 48px rgba(56, 189, 248, 0.35);
    letter-spacing: 0.08em;
    font-variant-numeric: tabular-nums;
  }

  /* HUD 四角线框 */
  &__hud-corner {
    position: absolute;
    width: 10px;
    height: 10px;
    border-color: rgba(103, 232, 249, 0.9);
    border-style: solid;
    pointer-events: none;

    &--tl {
      top: 6px;
      left: 6px;
      border-width: 2px 0 0 2px;
    }
    &--tr {
      top: 6px;
      right: 6px;
      border-width: 2px 2px 0 0;
    }
    &--bl {
      bottom: 6px;
      left: 6px;
      border-width: 0 0 2px 2px;
    }
    &--br {
      bottom: 6px;
      right: 6px;
      border-width: 0 2px 2px 0;
    }
  }

  &__divider {
    height: 1px;
    margin: 22px 0 16px;
    background: linear-gradient(
      90deg,
      transparent,
      rgba(103, 232, 249, 0.35) 20%,
      rgba(186, 230, 253, 0.55) 50%,
      rgba(103, 232, 249, 0.35) 80%,
      transparent
    );
    position: relative;
    z-index: 1;
  }

  &__hint {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 0;
    font-size: 13px;
    line-height: 1.7;
    color: rgba(205, 230, 248, 0.95);
  }

  &__hint-dot {
    flex-shrink: 0;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #67e8f9;
    box-shadow:
      0 0 10px rgba(103, 232, 249, 0.95),
      0 0 20px rgba(56, 189, 248, 0.55);
    animation: home-welcome-pulse 1.8s ease-in-out infinite;
  }
}

@keyframes home-welcome-scan {
  0% {
    opacity: 0.42;
    transform: translateY(-12%);
  }
  45% {
    opacity: 0.72;
    transform: translateY(18%);
  }
  100% {
    opacity: 0.42;
    transform: translateY(52%);
  }
}

@keyframes home-welcome-pulse {
  0%,
  100% {
    opacity: 1;
    box-shadow:
      0 0 8px rgba(103, 232, 249, 0.65),
      0 0 16px rgba(56, 189, 248, 0.35);
  }
  50% {
    opacity: 0.7;
    box-shadow:
      0 0 16px rgba(103, 232, 249, 0.95),
      0 0 28px rgba(56, 189, 248, 0.5);
  }
}
</style>
