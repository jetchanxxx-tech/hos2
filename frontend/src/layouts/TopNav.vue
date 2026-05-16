<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const navLinks = computed(() => {
  const links = [
    { path: '/dashboard', label: '惠福星盘' },
    { path: '/timeline', label: '惠福时光轴' },
    { path: '/lingxi/chat', label: '惠福灵犀' },
    { path: '/concentric', label: '惠福同心圆' },
    { path: '/packages', label: '服务包' },
  ]
  if (['HOSPITAL_ADMIN', 'OPS_ADMIN', 'SUPER_ADMIN'].includes(auth.role || '')) {
    links.push({ path: '/system/users', label: '系统管理' })
  }
  return links
})

function isActive(path: string) {
  return route.path.startsWith(path)
}
</script>

<template>
  <nav class="topnav">
    <div class="topnav-brand" @click="router.push('/')">
      <div class="logo-icon">⭐</div>
      <span>惠福星链</span>
    </div>
    <div class="topnav-links">
      <router-link v-for="link in navLinks" :key="link.path" :to="link.path"
        :class="{ active: isActive(link.path) }">
        {{ link.label }}
      </router-link>
      <router-link v-if="auth.isLoggedIn" to="/profile" class="profile-link" :class="{ active: isActive('/profile') }" title="个人资料">
        👤
      </router-link>
      <button v-if="auth.isLoggedIn" class="btn btn-ghost btn-sm" @click="auth.logout(); router.push('/login')">
        退出
      </button>
    </div>
  </nav>
</template>

<style scoped>
.topnav {
  position: sticky; top: 0; z-index: 100;
  background: oklch(100% 0 0 / 0.82);
  backdrop-filter: blur(16px) saturate(1.4);
  -webkit-backdrop-filter: blur(16px) saturate(1.4);
  border-bottom: 1px solid var(--border);
  padding: 0 var(--space-8); height: 52px;
  display: flex; align-items: center; justify-content: space-between;
}
.topnav-brand {
  display: flex; align-items: center; gap: var(--space-3);
  font-family: var(--font-display); font-weight: 680;
  font-size: var(--text-md); color: var(--fg);
  letter-spacing: -0.01em; cursor: pointer;
}
.logo-icon {
  width: 28px; height: 28px; border-radius: var(--radius-sm);
  background: var(--accent); color: var(--fg-inverse);
  display: flex; align-items: center; justify-content: center; font-size: 15px;
}
.topnav-links {
  display: flex; gap: var(--space-1); align-items: center;
}
.topnav-links a {
  text-decoration: none; color: var(--fg-secondary);
  font-size: var(--text-sm); padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-sm);
  transition: all var(--duration-fast) var(--ease-out); font-weight: 500;
}
.topnav-links a:hover, .topnav-links a.active {
  color: var(--fg); background: var(--surface-alt);
}
.profile-link { text-decoration: none; font-size: var(--text-lg); padding: var(--space-1) var(--space-2); }
.btn { cursor: pointer; }
.btn-ghost { background: transparent; color: var(--fg-secondary); border: 1px solid var(--border); border-radius: var(--radius-sm); }
.btn-sm { padding: var(--space-1) var(--space-3); font-size: var(--text-xs); }
@media (max-width: 768px) { .topnav { padding: 0 var(--space-4); } }
@media (max-width: 480px) { .topnav-links { display: none; } }
</style>
