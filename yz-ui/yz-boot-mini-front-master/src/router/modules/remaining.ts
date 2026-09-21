import { Layout } from '@/utils/routerHelper'

const { t } = useI18n()
/**
 * redirect: noredirect        当设置 noredirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'          设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * meta : {
 hidden: true              当设置 true 的时候该路由不会再侧边栏出现 如404，login等页面(默认 false)

 alwaysShow: true          当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式，
 只有一个时，会将那个子路由当做根路由显示在侧边栏，
 若你想不管路由下面的 children 声明的个数都显示你的根路由，
 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，
 一直显示根路由(默认 false)

 title: 'title'            设置该路由在侧边栏和面包屑中展示的名字

 icon: 'svg-name'          设置该路由的图标

 noCache: true             如果设置为true，则不会被 <keep-alive> 缓存(默认 false)

 breadcrumb: false         如果设置为false，则不会在breadcrumb面包屑中显示(默认 true)

 affix: true               如果设置为true，则会一直固定在tag项中(默认 false)

 noTagsView: true          如果设置为true，则不会出现在tag中(默认 false)

 activeMenu: '/dashboard'  显示高亮的路由路径

 followAuth: '/dashboard'  跟随哪个路由进行权限过滤

 canTo: true               设置为true即使hidden为true，也依然可以进行路由跳转(默认 false)
 }
 **/

/**
 * 仅本地 `vite` 开发注入坑塘入口。
 * 生产构建（import.meta.env.DEV=false）不会带上这段，侧栏仍走后台菜单，避免和线上重复。
 */
const localOnlyRouters: AppRouteRecordRaw[] = import.meta.env.DEV
  ? [
      {
        path: '/infra-facility',
        component: Layout,
        name: 'LocalInfraFacility',
        meta: { title: '基础设施', icon: 'ep:office-building', alwaysShow: true },
        children: [
          {
            path: 'pond',
            component: () => import('@/views/yz/pond/index.vue'),
            name: 'LocalWaterPondManage',
            meta: { title: '河塘管理', icon: 'ep:place' }
          }
        ]
      }
    ]
  : []

const remainingRouter: AppRouteRecordRaw[] = [
  {
    path: '/redirect',
    component: Layout,
    name: 'Redirect',
    children: [
      {
        path: '/redirect/:path(.*)',
        name: 'Redirect',
        component: () => import('@/views/Redirect/Redirect.vue'),
        meta: {}
      }
    ],
    meta: {
      hidden: true,
      noTagsView: true
    }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/index', // 默认进入欢迎页（首页）
    name: 'Home',
    meta: {
      title: '首页',
      icon: 'ep:home-filled',
      alwaysShow: true
    },
    children: [
      {
        path: 'index',
        component: () => import('@/views/Home/Welcome.vue'),
        name: 'Index',
        meta: {
          title: '首页',
          icon: 'ep:home-filled',
          noCache: false,
          affix: true
        }
      },
      {
        path: 'one-map',
        component: () => import('@/views/Home/Index.vue'),
        name: 'OneMap',
        meta: {
          title: '一张图',
          icon: 'ep:map-location',
          hidden: true,
          noCache: false
        }
      }
    ]
  },
  ...localOnlyRouters,
 /* {
    path: '/gis',
    component: Layout,
    name: 'GisTools',
    meta: { title: 'GIS 管理', icon: 'ep:map-location' },
    children: [
      {
        path: 'buffer-query',
        component: () => import('@/views/yz/buffer/index.vue'),
        name: 'GisBufferQuery',
        meta: { title: '缓冲区管理', icon: 'ep:map-location' }
      }
    ]
  },*/
/*  {
    path: '/water-facility',
    component: Layout,
    name: 'GisWaterFacility',
    meta: { title: '水利设施', icon: 'ep:location' },
    children: [
      {
        path: '',
        component: () => import('@/views/waterFacility/index.vue'),
        name: 'GisWaterFacilityList',
        meta: { title: '水利设施管理', icon: 'ep:location' }
      }
    ]
  },*/
  {
    path: '/user',
    component: Layout,
    name: 'UserInfo',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'profile',
        component: () => import('@/views/Profile/Index.vue'),
        name: 'Profile',
        meta: {
          canTo: true,
          hidden: true,
          noTagsView: false,
          icon: 'ep:user',
          title: t('common.profile')
        }
      },
      {
        path: 'notify-message',
        component: () => import('@/views/system/notify/my/index.vue'),
        name: 'MyNotifyMessage',
        meta: {
          canTo: true,
          hidden: true,
          noTagsView: false,
          icon: 'ep:message',
          title: '我的站内信'
        }
      }
    ]
  },
  {
    path: '/dict',
    component: Layout,
    name: 'dict',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'type/data/:dictType',
        component: () => import('@/views/system/dict/data/index.vue'),
        name: 'SystemDictData',
        meta: {
          title: '字典数据',
          noCache: true,
          hidden: true,
          canTo: true,
          icon: '',
          activeMenu: '/system/dict'
        }
      }
    ]
  },

  {
    path: '/codegen',
    component: Layout,
    name: 'CodegenEdit',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'edit',
        component: () => import('@/views/infra/codegen/EditTable.vue'),
        name: 'InfraCodegenEditTable',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          icon: 'ep:edit',
          title: '修改生成配置',
          activeMenu: 'infra/codegen/index'
        }
      }
    ]
  },
  {
    path: '/job',
    component: Layout,
    name: 'JobL',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'job-log',
        component: () => import('@/views/infra/job/logger/index.vue'),
        name: 'InfraJobLog',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          icon: 'ep:edit',
          title: '调度日志',
          activeMenu: 'infra/job/index'
        }
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/Login/Login.vue'),
    name: 'Login',
    meta: {
      hidden: true,
      title: t('router.login'),
      noTagsView: true
    }
  },
  {
    path: '/sso',
    component: () => import('@/views/Login/Login.vue'),
    name: 'SSOLogin',
    meta: {
      hidden: true,
      title: t('router.login'),
      noTagsView: true
    }
  },
  {
    path: '/external-auto-login',
    component: () => import('@/views/Login/ExternalAutoLogin.vue'),
    name: 'ExternalAutoLogin',
    meta: {
      hidden: true,
      title: t('router.login'),
      noTagsView: true
    }
  },
  {
    path: '/social-login',
    component: () => import('@/views/Login/SocialLogin.vue'),
    name: 'SocialLogin',
    meta: {
      hidden: true,
      title: t('router.socialLogin'),
      noTagsView: true
    }
  },
  {
    path: '/403',
    component: () => import('@/views/Error/403.vue'),
    name: 'NoAccess',
    meta: {
      hidden: true,
      title: '403',
      noTagsView: true
    }
  },
  {
    path: '/404',
    component: () => import('@/views/Error/404.vue'),
    name: 'NoFound',
    meta: {
      hidden: true,
      title: '404',
      noTagsView: true
    }
  },
  {
    path: '/500',
    component: () => import('@/views/Error/500.vue'),
    name: 'Error',
    meta: {
      hidden: true,
      title: '500',
      noTagsView: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/Error/404.vue'),
    name: '',
    meta: {
      title: '404',
      hidden: true,
      breadcrumb: false
    }
  },
  {
    path: '/iot',
    component: Layout,
    name: 'IOT',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'product/product/detail/:id',
        name: 'IoTProductDetail',
        meta: {
          title: '产品详情',
          noCache: true,
          hidden: true,
          activeMenu: '/iot/device/product'
        },
        component: () => import('@/views/iot/product/product/detail/index.vue')
      },
      {
        path: 'device/detail/:id',
        name: 'IoTDeviceDetail',
        meta: {
          title: '设备详情',
          noCache: true,
          hidden: true,
          activeMenu: '/iot/device/device'
        },
        component: () => import('@/views/iot/device/device/detail/index.vue')
      },
      {
        path: 'ota/operation/firmware/detail/:id',
        name: 'IoTOtaFirmwareDetail',
        meta: {
          title: '固件详情',
          noCache: true,
          hidden: true,
          activeMenu: '/iot/operation/ota/firmware'
        },
        component: () => import('@/views/iot/ota/firmware/detail/index.vue')
      },
      {
        path: 'realtime-data/mapping',
        name: 'IoTRealtimeDataMapping',
        meta: {
          title: '点位配置',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/iot/realtime-data/source'
        },
        component: () => import('@/views/iot/realtime-data/mapping/index.vue')
      }
    ]
  },
]

export default remainingRouter
