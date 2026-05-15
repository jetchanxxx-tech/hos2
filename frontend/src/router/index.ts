import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/pages/LoginPage.vue') },
    { path: '/', redirect: '/dashboard' },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/pages/astrolabe/OperationalDashboard.vue'),
      meta: { title: '惠福星盘 · 运营驾驶舱' },
    },
    {
      path: '/dashboard/members',
      name: 'MemberAnalytics',
      component: () => import('@/pages/astrolabe/MemberAnalytics.vue'),
      meta: { title: '会员分析' },
    },
    {
      path: '/timeline',
      name: 'Timeline',
      component: () => import('@/pages/timeline/TimelineView.vue'),
      meta: { title: '惠福时光轴' },
    },
    {
      path: '/lingxi/chat',
      name: 'ChatDashboard',
      component: () => import('@/pages/lingxi/ChatDashboard.vue'),
      meta: { title: '惠福灵犀 · 智能客服' },
    },
    {
      path: '/lingxi/followup',
      name: 'FollowupManager',
      component: () => import('@/pages/lingxi/FollowupManager.vue'),
      meta: { title: '随访管理' },
    },
    {
      path: '/concentric',
      name: 'FamilyOverview',
      component: () => import('@/pages/concentric/FamilyOverview.vue'),
      meta: { title: '惠福同心圆 · 家庭账户' },
    },
    {
      path: '/system/users',
      name: 'UserManagement',
      component: () => import('@/pages/system/UserManagement.vue'),
      meta: { title: '用户管理' },
    },
    {
      path: '/system/packages',
      name: 'PackageManager',
      component: () => import('@/pages/system/PackageManager.vue'),
      meta: { title: '服务包管理' },
    },
  ],
})

router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || '惠福星链'
  const token = localStorage.getItem('accessToken')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
