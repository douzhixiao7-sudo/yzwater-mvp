<template>
  <div class="external-auto-login">正在自动登录...</div>
</template>

<script lang="ts" setup>
import type { TokenType } from '@/api/login/types'
import * as authUtil from '@/utils/auth'
import { deleteUserCache } from '@/hooks/web/useCache'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'

defineOptions({ name: 'ExternalAutoLogin' })

const route = useRoute()
const router = useRouter()

const normalizeRedirect = (value?: string): string => {
  if (!value) {
    return '/'
  }
  let redirect = value
  try {
    redirect = decodeURIComponent(redirect)
  } catch (error) {}
  return redirect.startsWith('/') ? redirect : '/'
}

/**
 * 换新 Token 后必须清掉本地用户/菜单缓存并重置 Pinia，
 * 否则 permission.ts 里 getIsSetUser 仍为 true，不会再次请求 get-permission-info，界面仍显示上一用户。
 */
const resetSessionAfterTokenSwitch = () => {
  deleteUserCache()
  useUserStore().resetState()
  usePermissionStore().$patch({
    routers: [],
    addRouters: [],
    menuTabRouters: []
  })
}

const handleExternalAutoLogin = async () => {
  const accessToken =
    typeof route.query.accessToken === 'string' ? route.query.accessToken : undefined
  const refreshToken =
    typeof route.query.refreshToken === 'string' ? route.query.refreshToken : undefined
  const redirect = normalizeRedirect(
    typeof route.query.redirect === 'string' ? route.query.redirect : undefined
  )

  if (!accessToken || !refreshToken) {
    authUtil.removeToken()
    await router.replace(`/login?redirect=${encodeURIComponent(redirect)}`)
    return
  }

  authUtil.setToken({ accessToken, refreshToken } as TokenType)
  resetSessionAfterTokenSwitch()
  await router.replace(redirect)
}

onMounted(() => {
  handleExternalAutoLogin()
})
</script>

<style lang="scss" scoped>
.external-auto-login {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  font-size: 14px;
  color: var(--el-text-color-regular);
}
</style>
