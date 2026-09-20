import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/components/userFeedBackCom/login.vue'
import ListLogin from '@/components/userFeedBackCom/listLogin.vue'
import AdminLogin from '@/components/adminCom/login.vue'
import AdminPage from '@/components/adminCom/adminPage.vue'
import Map from '@/components/leafLetMap/map.vue'
import UserBaseInfo from '@/components/userBaseInfoCom/user_baseInfo.vue'
import UserFeedBack from '@/components/userFeedBackCom/user_feedBack.vue'
import FeedBackSuccess from '@/components/userFeedBackCom/feedBackSuccess.vue'
import FeedBackList from '@/components/userFeedBackCom/feedBackList.vue'
import FeedBackDetail from '@/components/userFeedBackCom/feedBackDetail.vue'
import ShortUrlDetail from '@/components/userFeedBackCom/shortUrlDetail.vue'
import FixFeedBack from '@/components/adminCom/fixFeedBack.vue'
import FeedBackMap from '@/components/userFeedBackCom/map.vue'
import MyPage from '@/components/userFeedBackCom/myPage.vue'
import Test from '@/components/demo/test.vue'
import LocationDemo from '@/components/demo/locationDemo.vue'
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/listLogin',
    name: 'ListLogin',
    component: ListLogin
  },
  {
    path: '/adminLogin',
    name: 'AdminLogin',
    component: AdminLogin
  },
  {
    path: '/adminPage',
    name: 'AdminPage',
    component: AdminPage,
    meta: { showBackButton: true }
  },
  {
    path: '/map',
    name: 'Map',
    component: Map,
    meta: { showBackButton: true }
  },
  {
    path: '/user_baseInfo',
    name: 'UserBaseInfo',
    component: UserBaseInfo
  },
  {
    path: '/user_feedBack',
    name: 'UserFeedBack',
    component: UserFeedBack,
    meta: { showBackButton: true }
  },
  {
    path: '/user_feedBack/success',
    name: 'FeedBackSuccess',
    component: FeedBackSuccess,
    meta: { showBackButton: true }
  },
  {
    path: '/user_feedBack/feedBackList',
    name: 'FeedBackList',
    component: FeedBackList,
    meta: { showBackButton: true }
  },
  {
    path: '/user_feedBack/detail',
    name: 'FeedBackDetail',
    component: FeedBackDetail,
    meta: { showBackButton: true }
  },
  {
    path: '/shortUrlDetail',
    name: 'ShortUrlDetail',
    component: ShortUrlDetail,
    meta: { showBackButton: true }
  },
  {
    path: '/admin/fixFeedBack',
    name: 'FixFeedBack',
    component: FixFeedBack,
    meta: { showBackButton: true }
  },
  {
    path: '/user_feedBack/map',
    name: 'FeedBackMap',
    component: FeedBackMap,
    meta: { showBackButton: true }
  },
  {
    path: '/myPage',
    name: 'MyPage',
    component: MyPage,
    meta: { showBackButton: true }
  },
  {
    path: '/test',
    name: 'Test',
    component: Test,
    meta: { showBackButton: true }
  },
  {
    path: '/locationDemo',
    name: 'LocationDemo',
    component: LocationDemo,
    meta: { showBackButton: true }
  }
]

const router = createRouter({
  history: createWebHistory('/h5/'),
  routes,
  // 路由切换时滚动到顶部
  scrollBehavior(to, from, savedPosition) {
    // 如果有保存的位置（浏览器前进/后退），则使用保存的位置
    if (savedPosition) {
      return savedPosition
    } else {
      // 否则滚动到顶部
      return { top: 0, left: 0, behavior: 'smooth' }
    }
  }
})

export default router

