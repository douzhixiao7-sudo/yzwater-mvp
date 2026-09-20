<template>
  <div :class="prefixCls" class="login-page">
    <div class="login-container">
      <Transition appear enter-active-class="animate__animated animate__fadeInDown">
        <div class="login-panel">
          <LoginForm />
        </div>
      </Transition>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { useDesign } from '@/hooks/web/useDesign'

import { LoginForm } from './components'

defineOptions({ name: 'Login' })

const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('login')
</script>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-login;

.#{$prefix-cls} {
  height: 100vh;
  width: 100%;
  background: url('/login.png') center / cover no-repeat;

  .login-container {
    height: 100%;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
  }

  .login-panel {
    width: 520px;
    max-width: 100%;
    padding: 28px 28px 18px;
    border-radius: 10px;
    background: rgba(8, 25, 55, 0.68);
    border: 1px solid rgba(255, 255, 255, 0.28);
    box-shadow: 0 14px 38px rgba(0, 0, 0, 0.38);
    backdrop-filter: blur(6px);
    position: relative;
    z-index: 0;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: 10px;
      pointer-events: none;
      z-index: 0;
      background: radial-gradient(900px 260px at 85% 15%, rgba(0, 165, 255, 0.35), transparent 60%),
        radial-gradient(900px 260px at 20% 95%, rgba(0, 165, 255, 0.25), transparent 65%);
    }

    :deep(.login-form) {
      position: relative;
      z-index: 1;
    }

    :deep(.el-form-item__label) {
      color: rgba(255, 255, 255, 0.85);
    }

    :deep(.el-input__wrapper) {
      background: rgba(8, 25, 55, 0.68);
      box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.22);
    }

    :deep(.el-input__inner) {
      color: rgba(255, 255, 255, 0.92);
    }

    :deep(.el-input__inner::placeholder) {
      color: rgba(255, 255, 255, 0.55);
    }

    :deep(.el-input__prefix) {
      color: rgba(255, 255, 255, 0.7);
    }
  }
}
</style>

<style lang="scss">
.dark .login-form {
  .el-divider__text {
    background-color: var(--login-bg-color);
  }

  .el-card {
    background-color: var(--login-bg-color);
  }
}
</style>
