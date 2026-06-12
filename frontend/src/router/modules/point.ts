import type { RouteRecordRaw } from 'vue-router'

const pointRoutes: RouteRecordRaw[] = [
  {
    path: '/points-mall',
    name: 'PointsMall',
    component: () => import('@/views/PointsMall.vue'),
    meta: { title: '积分商城', requiresAuth: true }
  },
  {
    path: '/points-logs',
    name: 'PointsLogs',
    component: () => import('@/views/PointsLogs.vue'),
    meta: { title: '积分明细', requiresAuth: true }
  }
]

export default pointRoutes
