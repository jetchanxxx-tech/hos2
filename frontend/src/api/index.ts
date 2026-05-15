import axios from 'axios'
import router from '@/router'

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res.data,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('accessToken')
      router.push('/login')
    }
    return Promise.reject(err.response?.data || err)
  },
)

export default api

// ---- Auth ----
export const authApi = {
  login: (data: { phone: string; password: string }) => api.post('/auth/login', data),
  register: (data: { phone: string; password: string; name: string; inviteCode?: string }) =>
    api.post('/auth/register', data),
  wxLogin: (code: string) => api.post('/auth/wx-login', { code }),
  logout: () => api.post('/auth/logout'),
  refreshToken: (rt: string) => api.post('/auth/refresh-token', rt),
}

// ---- Users & Families ----
export const userApi = {
  me: () => api.get('/users/me'),
  list: (page = 1, size = 20) => api.get('/users', { params: { page, size } }),
  get: (id: number) => api.get(`/users/${id}`),
  myFamily: () => api.get('/families/me'),
  getFamily: (id: number) => api.get(`/families/${id}`),
  createFamily: (familyName: string) => api.post('/families', null, { params: { familyName } }),
  getFamilyMembers: (id: number) => api.get(`/families/${id}/members`),
  addFamilyMember: (id: number, userId: number, relationship: string, shareScope: string) =>
    api.post(`/families/${id}/members`, null, { params: { userId, relationship, shareScope } }),
  removeFamilyMember: (familyId: number, memberUserId: number) =>
    api.delete(`/families/${familyId}/members/${memberUserId}`),
  updateShareScope: (familyId: number, memberUserId: number, shareScope: string) =>
    api.put(`/families/${familyId}/members/${memberUserId}/share-scope`, null, { params: { shareScope } }),
  toggleEmergency: (familyId: number, memberUserId: number) =>
    api.put(`/families/${familyId}/members/${memberUserId}/emergency`),
  dissolveFamily: (id: number) => api.delete(`/families/${id}`),
  searchUsers: (keyword: string) => api.get('/users', { params: { keyword, size: 10 } }),
}

// ---- Health Records ----
export const recordApi = {
  timeline: (userId: number, page = 1, size = 20, recordType?: string) =>
    api.get('/records', { params: { userId, page, size, recordType } }),
  getRecord: (id: number) => api.get(`/records/${id}`),
  abnormal: (userId: number) => api.get('/records/abnormal', { params: { userId } }),
  getReports: (id: number) => api.get(`/records/${id}/reports`),
  trend: (code: string) => api.get(`/records/trends/${code}`),
}

// ---- Service Packages ----
export const pkgApi = {
  list: (page = 1, size = 20) => api.get('/packages', { params: { page, size } }),
  detail: (id: number) => api.get(`/packages/${id}`),
  order: (id: number) => api.post(`/packages/${id}/order`),
  myOrders: (page = 1, size = 20) => api.get('/packages/orders', { params: { page, size } }),
}

// ---- Dashboard ----
export const dashboardApi = {
  kpiSummary: () => api.get('/dashboard/kpi-summary'),
  salesRanking: () => api.get('/dashboard/sales-ranking'),
  activityFeed: () => api.get('/dashboard/activity-feed'),
  butlerLeaderboard: () => api.get('/dashboard/butler-leaderboard'),
  metricHistory: (name: string, from?: string, to?: string) =>
    api.get(`/dashboard/metrics/${name}/history`, { params: { from, to } }),
}

// ---- Chat ----
export const chatApi = {
  startSession: (channel = 'MINIPROGRAM') => api.post('/chat/sessions', null, { params: { channel } }),
  mySessions: (page = 1, size = 20) => api.get('/chat/sessions', { params: { page, size } }),
  getMessages: (sessionId: number) => api.get(`/chat/sessions/${sessionId}/messages`),
  sendMessage: (sessionId: number, data: any) => api.post(`/chat/sessions/${sessionId}/messages`, data),
  searchKnowledge: (keyword: string, page = 1, size = 10) =>
    api.get('/chat/knowledge', { params: { keyword, page, size } }),
}

// ---- Followup ----
export const followupApi = {
  butlerTasks: (status?: string, page = 1, size = 20) => api.get('/followups/butler', { params: { status, page, size } }),
  myTasks: (page = 1, size = 20) => api.get('/followups/my', { params: { page, size } }),
  stats: () => api.get('/followups/stats'),
}

// ---- Admin ----
export const adminApi = {
  listUsers: (page = 1, size = 20, role?: string, keyword?: string) =>
    api.get('/admin/users', { params: { page, size, role, keyword } }),
  updateUserStatus: (id: number, status: string) => api.put(`/admin/users/${id}/status`, null, { params: { status } }),
  updateUserRole: (id: number, role: string) => api.put(`/admin/users/${id}/role`, null, { params: { role } }),
  auditLogs: (page = 1, size = 50, userId?: number) =>
    api.get('/admin/audit-logs', { params: { page, size, userId } }),
  stats: () => api.get('/admin/stats'),
  listKnowledge: (page = 1, size = 20) => api.get('/admin/knowledge', { params: { page, size } }),
  createKnowledge: (data: any) => api.post('/admin/knowledge', data),
  updateKnowledge: (id: number, data: any) => api.put(`/admin/knowledge/${id}`, data),
}
