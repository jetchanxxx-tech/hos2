<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const phone = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(phone.value, password.value)
    router.push('/dashboard')
  } catch (e: any) {
    error.value = e?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="login-icon">⭐</div>
        <h1>惠福星链</h1>
        <p>全病程健康协同平台</p>
      </div>
      <form @submit.prevent="handleLogin">
        <div class="field">
          <label>手机号</label>
          <input v-model="phone" type="text" placeholder="请输入手机号" />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" class="btn-login" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <p class="hint">演示账号：13800001111 / 任意密码</p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: var(--bg);
}
.login-card {
  background: var(--surface); border: 1px solid var(--border);
  border-radius: var(--radius-xl); padding: var(--space-10);
  width: 400px; max-width: 90vw;
  box-shadow: var(--shadow-md);
}
.login-header { text-align: center; margin-bottom: var(--space-8); }
.login-icon { font-size: 2.5rem; margin-bottom: var(--space-3); }
.login-header h1 { font-family: var(--font-display); font-size: var(--text-2xl); font-weight: 700; color: var(--fg); }
.login-header p { font-size: var(--text-sm); color: var(--fg-muted); margin-top: var(--space-1); }
.field { margin-bottom: var(--space-4); }
.field label { display: block; font-size: var(--text-xs); font-weight: 500; color: var(--fg-secondary); margin-bottom: var(--space-1); text-transform: uppercase; letter-spacing: 0.05em; }
.field input { width: 100%; padding: var(--space-3) var(--space-4); border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--text-base); background: var(--surface); color: var(--fg); }
.field input:focus { outline: none; border-color: var(--accent); box-shadow: 0 0 0 3px var(--accent-bg); }
.error { color: var(--danger); font-size: var(--text-xs); margin-bottom: var(--space-3); }
.btn-login { width: 100%; padding: var(--space-3); background: var(--accent); color: var(--fg-inverse); border: none; border-radius: var(--radius-sm); font-size: var(--text-base); font-weight: 600; cursor: pointer; transition: background var(--duration-fast); }
.btn-login:hover { background: var(--accent-hover); }
.btn-login:disabled { opacity: 0.6; cursor: not-allowed; }
.hint { text-align: center; font-size: var(--text-xs); color: var(--fg-muted); margin-top: var(--space-4); }
</style>
