<!-- 全局底部导航栏组件 - 支持前进/后退 -->
<template>
  <transition name="fade">
    <div v-if="isVisible" class="navigation-bar">
      <button 
        class="nav-btn nav-btn-back" 
        :class="{ disabled: !canGoBack }"
        :disabled="!canGoBack"
        @click="goBack"
        aria-label="后退"
      >
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      
      <button 
        class="nav-btn nav-btn-forward" 
        :class="{ disabled: !canGoForward }"
        :disabled="!canGoForward"
        @click="goForward"
        aria-label="前进"
      >
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </transition>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import tool from '@/utils/tools'

export default {
  name: 'NavigationBar',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // 使用浏览器历史记录的长度来判断是否可以前进/后退
    // 注意：由于浏览器安全限制，我们无法直接访问历史记录
    // 所以使用自定义的历史记录栈
    const historyStack = ref([])
    const currentIndex = ref(-1)
    
    // 需要隐藏导航栏的路由名称（登录相关和用户基础信息页面）
    const hiddenRouteNames = ['Login', 'ListLogin', 'AdminLogin', 'UserBaseInfo']
    
    // 是否显示导航栏
    const isVisible = computed(() => {
      // 只在安卓设备上显示
      if (!tool.isAndroid()) {
        return false
      }
      
      // 检查路由名称是否在隐藏列表中
      if (route.name && hiddenRouteNames.includes(route.name)) {
        return false
      }
      
      // 检查路由 meta 中是否标记需要显示返回按钮
      if (route.meta && route.meta.showBackButton === true) {
        return true
      }
      
      // 默认不显示（只有明确标记 showBackButton: true 的页面才显示）
      return false
    })
    
    // 计算是否可以后退
    const canGoBack = computed(() => {
      // 如果历史栈中有记录，使用历史栈判断
      if (currentIndex.value > 0) {
        return true
      }
      // 如果历史栈为空或只有一条记录，检查浏览器历史记录
      // 注意：无法直接访问浏览器历史记录长度，但可以通过尝试返回来判断
      // 这里我们假设如果历史栈为空，可能还有浏览器历史记录
      return historyStack.value.length > 0
    })
    
    // 计算是否可以前进
    const canGoForward = computed(() => {
      return currentIndex.value < historyStack.value.length - 1
    })
    
    // 添加到历史记录栈
    const addToHistory = (route) => {
      // 如果当前不在栈的末尾，删除后面的记录（用户进行了新的导航）
      if (currentIndex.value < historyStack.value.length - 1) {
        historyStack.value = historyStack.value.slice(0, currentIndex.value + 1)
      }
      
      // 避免重复添加相同的路由
      const lastRoute = historyStack.value[historyStack.value.length - 1]
      if (!lastRoute || lastRoute.fullPath !== route.fullPath) {
        historyStack.value.push({
          path: route.path,
          name: route.name,
          fullPath: route.fullPath
        })
        currentIndex.value = historyStack.value.length - 1
      } else {
        // 如果是相同的路由，确保 currentIndex 正确
        currentIndex.value = historyStack.value.length - 1
      }
    }
    
    // 后退
    const goBack = () => {
      // 如果历史栈中有记录且不是第一个，使用历史栈
      if (currentIndex.value > 0) {
        currentIndex.value--
        const targetRoute = historyStack.value[currentIndex.value]
        if (targetRoute) {
          router.push(targetRoute.fullPath).catch(() => {})
        }
      } else {
        // 如果历史栈为空或只有一条记录，使用浏览器历史记录
        router.go(-1)
      }
    }
    
    // 前进
    const goForward = () => {
      if (canGoForward.value) {
        currentIndex.value++
        const targetRoute = historyStack.value[currentIndex.value]
        if (targetRoute) {
          router.push(targetRoute.fullPath).catch(() => {})
        }
      } else {
        // 如果自定义历史记录已到末尾，使用浏览器历史记录
        router.go(1)
      }
    }
    
    // 监听路由变化
    watch(() => route.fullPath, () => {
      // 记录所有路由，包括隐藏的路由（如登录页），这样才能正确返回
      addToHistory(route)
    }, { immediate: true })
    
    onMounted(() => {
      // 初始化：添加当前路由（包括隐藏的路由）
      addToHistory(route)
    })
    
    return {
      isVisible,
      canGoBack,
      canGoForward,
      goBack,
      goForward
    }
  }
}
</script>

<style lang="scss" scoped>
.navigation-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 60px;
  padding: 14px 30px;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  z-index: 1000;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  
  // 安全区域适配（iPhone X 等有底部安全区域的设备）
  padding-bottom: calc(14px + env(safe-area-inset-bottom));
}

.nav-btn {
  width: auto;
  height: auto;
  border: none;
  background: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 8px;
  transition: opacity 0.3s ease;
  
  svg {
    width: 20px;
    height: 20px;
    color: #3D3D3D;
    stroke: #3D3D3D;
  }
  
  &:active {
    opacity: 0.6;
  }
  
  &.disabled {
    opacity: 0.3;
    cursor: not-allowed;
    pointer-events: none;
  }
  
  &:not(.disabled):hover {
    opacity: 0.7;
  }
}

// 移动端优化
@media (max-width: 768px) {
  .navigation-bar {
    padding: 12px 24px;
    padding-bottom: calc(12px + env(safe-area-inset-bottom));
    gap: 50px;
  }
  
  .nav-btn {
    svg {
      width: 18px;
      height: 18px;
    }
  }
}

// 小屏幕设备
@media (max-width: 375px) {
  .navigation-bar {
    padding: 10px 20px;
    padding-bottom: calc(10px + env(safe-area-inset-bottom));
    gap: 40px;
  }
  
  .nav-btn {
    svg {
      width: 16px;
      height: 16px;
    }
  }
}

// 淡入淡出动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>

