<template>
  <div class="login-container">

    <!-- 头部 -->
    <!-- <div class="login-header">

      <div class="close-btn" @click="handleBack">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round" />
        </svg>
      </div>
      <div class="menu-btn" @click="handleMenu">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="5" r="1.5" fill="currentColor" />
          <circle cx="12" cy="12" r="1.5" fill="currentColor" />
          <circle cx="12" cy="19" r="1.5" fill="currentColor" />
        </svg>
      </div>
    </div> -->

    <!-- 主要内容区域 -->
    <div class="login-content">
      <!-- 中央图标和标题 -->
      <div class="icon-section">
        <div class="icon-wrapper">
          <div class="icon-circle">
          </div>
        </div>
        <h2 class="app-title">仪征河长制</h2>
      </div>

      <!-- 表单区域 -->
      <div class="form-section">
        <!-- 手机号输入 -->
        <div class="input-group">
          <div class="input-icon phone-icon">
            <!-- 手机图标占位（后续替换为图片） -->
          </div>
          <input v-model="form.phone" type="tel" class="input-field" placeholder="请输入手机号" maxlength="11" />
        </div>

        <!-- 验证码输入 -->
        <div class="input-group">
          <div class="input-icon shield-icon">
            <!-- 盾牌图标占位（后续替换为图片） -->
          </div>
          <input v-model="form.code" type="text" class="input-field code-input" placeholder="请输入验证码" maxlength="6"
            @input="handleCodeInput" :disabled="codeVerifying" />
          <div v-if="codeVerifying" class="code-loading">
            <div class="loading-spinner"></div>
          </div>
          <button class="get-code-btn" :disabled="countdown > 0 || !form.phone" @click="handleGetCode">
            {{ countdown > 0 ? `${countdown}秒` : '获取验证码' }}
          </button>
        </div>

        <!-- 协议同意 - 放在登录按钮上方 -->
        <label class="agreement-section" :class="{ 'disabled': agreementDisabled }">
          <div class="agreement-checkbox">
            <input v-model="form.agreed" type="checkbox" class="checkbox-input" :disabled="agreementDisabled" />
            <span class="checkbox-custom"></span>
          </div>
          <span class="agreement-text">
            阅读并同意
            <a href="#" class="agreement-link" @click.stop.prevent="handleAgreement('user')">《用户协议》</a>
            <span>和</span>
            <a href="#" class="agreement-link" @click.stop.prevent="handleAgreement('privacy')">《隐私政策》</a>
          </span>
        </label>

        <!-- 登录按钮 -->
        <button class="login-btn"
          :disabled="codeVerifying || !form.phone || !form.code || form.code.length !== 6 || !form.agreed"
          @click="handleLogin">
          <div v-if="codeVerifying" class="login-loading">
            <div class="loading-spinner"></div>
          </div>
          <span v-else>登录</span>
        </button>
      </div>
    </div>

    <!-- 人机验证弹窗 -->
    <div v-if="showVerify" class="verify-modal" @click.self="closeVerify">
      <div class="verify-content" @click.stop>
        <div class="verify-header">
          <h3>安全验证</h3>
          <div class="verify-close" @click="closeVerify">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                stroke-linejoin="round" />
            </svg>
          </div>
        </div>
        <div class="verify-body">
          <div class="verify-tip">请完成安全验证</div>
          <div class="verify-slider-wrapper">
            <div class="verify-slider-bg">
              <div class="verify-slider-text" :class="{ 'text-white': verifyProgress > 0 }">{{ verifyText }}</div>
              <div class="verify-slider-track" :style="{ width: verifyProgress + '%' }"></div>
            </div>
            <div class="verify-slider-btn" :style="{ left: verifyProgress + '%' }" @touchstart="handleTouchStart"
              @touchmove="handleTouchMove" @touchend="handleTouchEnd" @mousedown="handleMouseDown">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                  stroke-linejoin="round" />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import http from '@/utils/request'

export default {
  name: 'ListLogin',
  data() {
    return {
      form: {
        phone: '',
        code: '',
        agreed: false
      },
      countdown: 0,
      countdownTimer: null,
      // 接口返回的位置信息
      locationInfo: null,
      // 人机验证相关
      showVerify: false,
      verifyProgress: 0,
      verifyText: '向右滑动完成验证',
      isDragging: false,
      startX: 0,
      sliderWidth: 0,
      // 协议checkbox是否禁用
      agreementDisabled: false,
      // 验证码验证中状态
      codeVerifying: false
    }
  },
  computed: {



    // 获取QrCode
    getQrcode() {
      return this.$route.query.qrcode
    }
  },
  mounted() {
    // 页面加载后确保滚动到顶部
    this.scrollToTop();

    // 记录登录来源页面
    localStorage.setItem('loginSource', 'ListLogin');

    // 将请求参数中的 qrcode 存储到 Vuex
    if (this.getQrcode) {
      this.$store.dispatch('updateQrcode', this.getQrcode)
    }

  },
  beforeUnmount() {
    // 清理定时器
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
      this.countdownTimer = null
    }
  },
  methods: {
    // 滚动到顶部
    scrollToTop() {
      window.scrollTo(0, 0)
      document.documentElement.scrollTop = 0
      document.body.scrollTop = 0
    },

    // 关闭按钮
    handleBack() {
      this.$router.back()
    },
    // 菜单按钮
    handleMenu() {
      // TODO: 实现菜单功能
    },
    // 协议链接
    handleAgreement(type) {
      // TODO: 跳转到对应的协议页面
    },
    // 获取验证码
    async handleGetCode() {
      if (!this.form.phone) {
        ElMessage.warning('请输入手机号')
        return
      }

      if (this.form.phone.length !== 11) {
        ElMessage.warning('请输入正确的手机号')
        return
      }

      // 检查是否勾选用户协议
      if (!this.form.agreed) {
        ElMessage.warning('请先阅读并同意用户协议和隐私政策')
        return
      }

      // 显示人机验证弹窗
      this.showVerify = true
      this.resetVerify()
    },
    // 发送验证码（验证通过后调用）
    async sendCode() {
      try {
        // 调用获取验证码接口
        await http.post('/admin-api/system/auth/send-sms-login-code', {
          mobile: this.form.phone
        })

        ElMessage.success('验证码已发送')

        // 开始倒计时
        this.countdown = 60
        this.countdownTimer = setInterval(() => {
          this.countdown--
          if (this.countdown <= 0) {
            clearInterval(this.countdownTimer)
            this.countdownTimer = null
          }
        }, 1000)
      } catch (error) {
        const errorMsg = error.response?.data?.msg || error.message || '获取验证码失败，请稍后重试'
        ElMessage.error(errorMsg)
      }
    },
    // 验证码输入处理
    handleCodeInput() {
      // 只允许输入数字
      this.form.code = this.form.code.replace(/\D/g, '')
    },
    // 登录按钮点击处理
    handleLogin() {
      this.verifyCode()
    },
    // 验证验证码
    async verifyCode() {
      // 先检查前置条件
      if (!this.form.phone) {
        ElMessage.warning('请输入手机号')
        this.form.code = ''
        return
      }

      if (this.form.phone.length !== 11) {
        ElMessage.warning('请输入正确的手机号')
        this.form.code = ''
        return
      }

      if (!this.form.agreed) {
        ElMessage.warning('请先阅读并同意用户协议和隐私政策')
        this.form.code = ''
        return
      }

      if (!this.form.code || this.form.code.length !== 6) {
        ElMessage.warning('请输入6位验证码')
        return
      }

      // 显示 loading
      this.codeVerifying = true

      try {
        // 调用登录接口
        const response = await http.post('/admin-api/system/auth/sms-login', {
          mobile: this.form.phone,
          code: this.form.code
        })

        // 处理响应
        if (response.code === 200 || response.code === 0) {
          // 登录成功
          ElMessage.success('登录成功')

          // 保存 token（如果接口返回了 token）
          if (response.data && response.data.token) {
            localStorage.setItem('X-Access-Token', response.data.token)
          } else if (response.data && response.data.accessToken) {
            localStorage.setItem('X-Access-Token', response.data.accessToken)
          }

          // 获取角色信息
          try {
            const profileResponse = await http.get('/admin-api/system/user/profile/get')
            console.log('角色信息接口返回:', profileResponse)
            
            // 存储角色名称和手机号码
            if (profileResponse && profileResponse.data) {
              // 存储角色名称
              if (profileResponse.data.roles && profileResponse.data.roles.length > 0) {
                const roleName = profileResponse.data.roles[0].name
                console.log('存储角色名称:', roleName)
                // 同时保存到 Vuex 和 localStorage
                this.$store.commit('setRoleName', roleName)
                localStorage.setItem('roleName', roleName)
              }
              
              // 存储手机号码
              if (profileResponse.data.mobile) {
                const mobile = profileResponse.data.mobile
                console.log('存储手机号码:', mobile)
                localStorage.setItem('userMobile', mobile)
              }
              
              // 存储昵称（优先使用 nickname，如果没有则使用 username）
              const nickname = profileResponse.data.nickname || profileResponse.data.username || '';
              if (nickname) {
                console.log('存储昵称:', nickname)
                localStorage.setItem('userNickname', nickname)
              } else {
                console.warn('警告：nickname 和 username 都为空，无法保存用户昵称')
              }
            }
          } catch (profileError) {
            console.error('获取角色信息失败:', profileError)
            console.error('错误详情:', profileError.response?.data || profileError.message)
          }

          // 延迟一下再跳转，让用户看到成功提示
          setTimeout(() => {
            this.codeVerifying = false
            // 跳转到反馈列表页面
            this.$router.push({ name: 'FeedBackList' })
          }, 300)
        } else {
          // 登录失败
          this.codeVerifying = false
          const errorMsg = response.msg || response.message || '登录失败，请重试'
          ElMessage.error(errorMsg)
          this.form.code = ''
        }
      } catch (error) {
        // 请求失败，不允许跳转
        this.codeVerifying = false
        let errorMsg = '登录失败，请稍后重试'
        if (error.response && error.response.data) {
          errorMsg = error.response.data.msg || error.response.data.message || errorMsg
        } else if (error.message) {
          errorMsg = error.message
        }

        ElMessage.error(errorMsg)
        this.form.code = ''
        // 不执行跳转操作
        return
      }
    },
    // 关闭验证弹窗
    closeVerify() {
      this.showVerify = false
      this.resetVerify()
    },
    // 重置验证状态
    resetVerify() {
      this.verifyProgress = 0
      this.verifyText = '向右滑动完成验证'
      this.isDragging = false
    },
    // 触摸开始
    handleTouchStart(e) {
      this.isDragging = true
      this.startX = e.touches[0].clientX
      const slider = e.currentTarget.parentElement
      this.sliderWidth = slider.offsetWidth - e.currentTarget.offsetWidth
    },
    // 触摸移动
    handleTouchMove(e) {
      if (!this.isDragging) return
      e.preventDefault()
      const currentX = e.touches[0].clientX
      const diff = currentX - this.startX
      this.updateProgress(diff)
    },
    // 触摸结束
    handleTouchEnd() {
      if (!this.isDragging) return
      this.isDragging = false
      this.checkVerify()
    },
    // 鼠标按下
    handleMouseDown(e) {
      this.isDragging = true
      this.startX = e.clientX
      const slider = e.currentTarget.parentElement
      this.sliderWidth = slider.offsetWidth - e.currentTarget.offsetWidth

      const handleMouseMove = (e) => {
        if (!this.isDragging) return
        const diff = e.clientX - this.startX
        this.updateProgress(diff)
      }

      const handleMouseUp = () => {
        if (!this.isDragging) return
        this.isDragging = false
        this.checkVerify()
        document.removeEventListener('mousemove', handleMouseMove)
        document.removeEventListener('mouseup', handleMouseUp)
      }

      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)
    },
    // 更新进度
    updateProgress(diff) {
      let progress = (diff / this.sliderWidth) * 100
      progress = Math.max(0, Math.min(100, progress))
      this.verifyProgress = progress

      if (progress < 100) {
        this.verifyText = '向右滑动完成验证'
      }
    },
    // 检查验证结果
    checkVerify() {
      // 验证通过阈值（可以设置容差，比如95%以上算通过）
      if (this.verifyProgress >= 95) {
        this.verifyText = '验证成功'
        // 验证成功后禁用协议checkbox
        this.agreementDisabled = true
        setTimeout(() => {
          this.closeVerify()
          this.sendCode()
        }, 300)
      } else {
        // 验证失败，重置
        this.verifyText = '验证失败，请重试'
        setTimeout(() => {
          this.resetVerify()
        }, 1000)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  min-height: 100vh;
  background-color: #fff;
  position: relative;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}

// 头部样式
.login-header {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 32px;
  background: #fff;

  .close-btn,
  .menu-btn {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #000;
    cursor: pointer;

    svg {
      width: 40px;
      height: 40px;
    }

    &:active {
      opacity: 0.6;
    }
  }
}

// 主要内容区域
.login-content {
  padding: 40px 48px 0;
  flex: 1;
}

// 图标区域
.icon-section {
  position: relative;
  top: 10px;
  text-align: center;
  margin: 80px 0px;

  .icon-wrapper {
    display: inline-block;
    margin-bottom: 32px;

    .icon-circle {
      width: 160px;
      height: 160px;
      border-radius: 50%;
      background: url('@/assets/img/loginLogo.png') no-repeat center center;
      background-size: 100% 100%;


    }
  }

  .app-title {
    font-size: 44px;
    font-weight: 500;
    color: #333;
    margin-bottom: 60px;
  }
}

// 表单区域
.form-section {
  .input-group {
    position: relative;
    margin-bottom: 36px;
    display: flex;
    align-items: center;
    background: #f5f5f5;
    border-radius: 24px;
    padding: 0 24px;
    height: 104px;
    min-width: 0;
    overflow: hidden;

    .input-icon {
      width: 36px;
      height: 36px;
      flex-shrink: 0;
      margin-right: 24px;
      border-radius: 8px;
      // 这里后续替换为实际的图标图片
      background-image: url('@/assets/img/phoneIcon.png');
      background-size: 100% 100%;
      background-repeat: no-repeat;
      background-position: center;
    }

    .shield-icon {

      background-image: url('@/assets/img/verificationCodeIcon.png');
      background-size: 100% 100%;
      background-repeat: no-repeat;
      background-position: center;
    }

    .input-field {
      flex: 1;
      min-width: 0;
      height: 100%;
      padding: 0;
      background: transparent;
      border: none;
      font-size: 32px;
      color: #333;
      outline: none;

      &::placeholder {
        color: #999;
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }

    .code-input {
      flex: 1;
      min-width: 0;
      max-width: 100%;
    }

    .code-loading {
      position: absolute;
      right: 160px;
      top: 50%;
      transform: translateY(-50%);
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 2;
    }

    .loading-spinner {
      width: 32px;
      height: 32px;
      border: 2px solid #e0e0e0;
      border-top-color: #56A9FF;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to {
        transform: rotate(360deg);
      }
    }

    .get-code-btn {
      height: auto;
      padding: 0 8px;
      background: transparent;
      color: #56A9FF;
      border: none;
      font-size: 24px;
      white-space: nowrap;
      flex-shrink: 0;
      cursor: pointer;
      transition: all 0.3s;
      margin-left: 16px;
      max-width: 160px;
      overflow: hidden;
      text-overflow: ellipsis;

      &:disabled {
        color: #ccc;
        cursor: not-allowed;
      }

      &:not(:disabled):active {
        opacity: 0.7;
      }
    }
  }

  // 协议同意区域 - 放在登录按钮上方
  .agreement-section {
    display: flex;
    align-items: center;
    margin-top: 16px;
    margin-bottom: 32px;
    cursor: pointer;
    user-select: none;

    &.disabled {
      cursor: not-allowed;
      opacity: 0.6;
      pointer-events: none;
    }

    .agreement-checkbox {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      flex-shrink: 0;
      // 增大热区：增加padding，让点击区域更大
      padding: 8px;
      margin: -8px 8px -8px -8px;

      .checkbox-input {
      display: none;

      &:checked+.checkbox-custom {
        background: #56A9FF;
        border-color: #56A9FF;

        &::after {
          opacity: 1;
        }
      }

      &:disabled+.checkbox-custom {
        cursor: not-allowed;
        opacity: 0.6;
      }
    }

    .checkbox-custom {
      width: 24px;
      height: 24px;
      border: 1.5px solid #ccc;
      border-radius: 4px;
      background: #fff;
      position: relative;
      transition: all 0.3s;

      &::after {
        content: '';
        position: absolute;
        top: 1px;
        left: 6px;
        width: 6px;
        height: 12px;
        border: solid #fff;
        border-width: 0 1.5px 1.5px 0;
        transform: rotate(45deg);
        opacity: 0;
        transition: opacity 0.3s;
      }
    }
  }

  .agreement-text {
    flex: 1;
    font-size: 24px;
    color: #666;
    line-height: 1.5;

    .agreement-link {
      color: #56A9FF;
      text-decoration: none;

      &:active {
        opacity: 0.7;
      }
    }
  }
  }

  // 登录按钮
  .login-btn {
    width: 100%;
    height: 104px;
    background: linear-gradient(90deg, #56A9FF 0%, #4590e6 100%);
    border: none;
    border-radius: 8px;
    color: #fff;
    font-size: 36px;
    font-weight: 500;
    cursor: pointer;
    margin-top: 68px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
    position: relative;

    &:disabled {
      background: #ccc;
      cursor: not-allowed;
      opacity: 0.6;
    }

    &:not(:disabled):active {
      opacity: 0.8;
      transform: scale(0.98);
    }

    .login-loading {
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .loading-spinner {
      width: 32px;
      height: 32px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: #fff;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
  }
}

// 人机验证弹窗
.verify-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 40px;
}

.verify-content {
  width: 100%;
  max-width: 640px;
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
}

.verify-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32px 40px;
  border-bottom: 1px solid #f0f0f0;

  h3 {
    margin: 0;
    font-size: 36px;
    font-weight: 500;
    color: #333;
  }

  .verify-close {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    cursor: pointer;
    transition: color 0.3s;

    &:active {
      color: #333;
    }

    svg {
      width: 36px;
      height: 36px;
    }
  }
}

.verify-body {
  padding: 48px 40px;

  .verify-tip {
    text-align: center;
    font-size: 28px;
    color: #666;
    margin-bottom: 40px;
  }
}

.verify-slider-wrapper {
  position: relative;
  width: 100%;
  height: 80px;
  background: transparent;
  border-radius: 40px;
  overflow: visible;
}

.verify-slider-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  border-radius: 40px;
  overflow: hidden;
}

.verify-slider-text {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #666;
  z-index: 2;
  user-select: none;
  transition: color 0.3s;

  &.text-white {
    color: #fff;
  }
}

.verify-slider-track {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(90deg, #56A9FF 0%, #4590e6 100%);
  border-radius: 40px;
  transition: width 0.1s;
  z-index: 1;
}

.verify-slider-btn {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  z-index: 10;
  transition: left 0.1s;
  user-select: none;

  &:active {
    cursor: grabbing;
  }

  svg {
    width: 40px;
    height: 40px;
    color: #56A9FF;
  }
}
</style>
