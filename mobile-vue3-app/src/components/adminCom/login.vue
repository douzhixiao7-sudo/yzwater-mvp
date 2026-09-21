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
        <!-- 账号输入 -->
        <div class="input-group">
          <div class="input-icon phone-icon">
            <!-- 账号图标，使用手机图标样式 -->
          </div>
          <input v-model="form.username" type="text" class="input-field" placeholder="请输入账号" />
        </div>

        <!-- 密码输入 -->
        <div class="input-group">
          <div class="input-icon password-icon">
            <img src="@/assets/img/passwordIcon.png" alt="密码" />
          </div>
          <input 
            v-model="form.password" 
            type="password" 
            class="input-field" 
            placeholder="请输入密码"
          />
        </div>
      </div>

      <!-- 登录按钮 -->
      <div class="login-btn-wrapper">
        <button class="login-btn" :disabled="loading" @click="handleLogin">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import http from '@/utils/request'

export default {
  name: 'AdminLogin',
  data() {
    return {
      form: {
        username: '',
        password: '',
        captchaVerification: '' // 验证码（如果需要）
      },
      loading: false // 登录中状态
    }
  },
  methods: {
    // 返回
    handleBack() {
      this.$router.go(-1)
    },
    // 菜单按钮
    handleMenu() {
      // 菜单功能待实现
    },
    // 表单验证
    validateForm() {
      if (!this.form.username || !this.form.username.trim()) {
        ElMessage.warning('账号不能为空')
        return false
      }
      if (!this.form.password || !this.form.password.trim()) {
        ElMessage.warning('密码不能为空')
        return false
      }
      return true
    },
    // 登录
    async handleLogin() {
      // 表单验证
      if (!this.validateForm()) {
        return
      }

      // 防止重复提交
      if (this.loading) {
        return
      }

      this.loading = true

      try {
        // 构建请求参数
        const loginData = {
          username: this.form.username.trim(),
          password: this.form.password.trim()
        }

        // 如果验证码开启，需要传递验证码（暂时留空，如果接口需要会返回错误提示）
        if (this.form.captchaVerification) {
          loginData.captchaVerification = this.form.captchaVerification
        }

        // 调用登录接口
        const response = await http.post('/admin-api/system/auth/login', loginData)

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
            console.log('角色信息完整数据:', JSON.stringify(profileResponse, null, 2))
            
            // 打印各个可能的字段
            if (profileResponse) {
              console.log('profileResponse.code:', profileResponse.code)
              console.log('profileResponse.data:', profileResponse.data)
              if (profileResponse.data) {
                console.log('角色信息 data 字段详情:')
                Object.keys(profileResponse.data).forEach(key => {
                  console.log(`  ${key}:`, profileResponse.data[key])
                })
                
                // 存储角色名称
                if (profileResponse.data.roles && profileResponse.data.roles.length > 0) {
                  const roleName = profileResponse.data.roles[0].name
                  console.log('存储角色名称:', roleName)
                  // 同时保存到 Vuex 和 localStorage
                  this.$store.commit('setRoleName', roleName)
                  localStorage.setItem('roleName', roleName)
                }
              }
            }
          } catch (profileError) {
            console.error('获取角色信息失败:', profileError)
            console.error('错误详情:', profileError.response?.data || profileError.message)
          }
          
          // 延迟一下再跳转，让用户看到成功提示
          setTimeout(() => {
            this.loading = false
            // 跳转到管理后台首页
            this.$router.push({ name: 'AdminPage' })
          }, 300)
        } else {
          // 登录失败
          this.loading = false
          const errorMsg = response.msg || response.message || '登录失败，请重试'
          ElMessage.error(errorMsg)
        }
      } catch (error) {
        // 请求失败，不允许跳转
        this.loading = false
        let errorMsg = '登录失败，请稍后重试'
        if (error.response && error.response.data) {
          errorMsg = error.response.data.msg || error.response.data.message || errorMsg
        } else if (error.message) {
          errorMsg = error.message
        }
        
        ElMessage.error(errorMsg)
        // 不执行跳转操作
        return
      }
    },
    
    // 滚动到顶部
    scrollToTop() {
      window.scrollTo(0, 0)
      document.documentElement.scrollTop = 0
      document.body.scrollTop = 0
    }
  },
  mounted() {
    // 页面加载后确保滚动到顶部
    this.scrollToTop();

    //开发环境默认填充密码和账号
    if (process.env.NODE_ENV === 'development') {
      this.form.username = 'admin';
      this.form.password = 'Sydigit@ct2025';
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
    margin-bottom: 112px;
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
      display: flex;
      align-items: center;
      justify-content: center;

      &.phone-icon {
        // 账号图标样式，参考原login.vue使用背景图片
        background-image: url('@/assets/img/phoneIcon.png');
        background-size: 100% 100%;
        background-repeat: no-repeat;
        background-position: center;
      }

      &.password-icon {
        // 密码图标
        img {
          width: 36px;
          height: 36px;
          object-fit: contain;
        }
      }
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
    }
  }
}

// 登录按钮
.login-btn-wrapper {
  margin-top: 80px;
  padding: 0 24px;
}

.login-btn {
  width: 100%;
  height: 96px;
  background: #349DFF;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 36px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    opacity: 0.8;
    transform: scale(0.98);
  }

  &:disabled {
    background: #eaeaea;
    color: #999;
    cursor: not-allowed;
  }
}
</style>

