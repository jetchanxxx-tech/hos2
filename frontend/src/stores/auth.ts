import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<any>(null)
  const token = ref(localStorage.getItem('accessToken') || '')

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => user.value?.role)
  const userName = computed(() => user.value?.name)

  async function login(phone: string, password: string) {
    const res: any = await authApi.login({ phone, password })
    token.value = res.data.accessToken
    user.value = res.data.user
    localStorage.setItem('accessToken', res.data.accessToken)
    return res.data
  }

  async function register(phone: string, password: string, name: string, inviteCode?: string) {
    const res: any = await authApi.register({ phone, password, name, inviteCode })
    token.value = res.data.accessToken
    user.value = res.data.user
    localStorage.setItem('accessToken', res.data.accessToken)
    return res.data
  }

  async function fetchMe() {
    try {
      const res: any = await authApi.me ? undefined : undefined
    } catch { /* ignore */ }
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('accessToken')
  }

  return { user, token, isLoggedIn, role, userName, login, register, fetchMe, logout }
})
