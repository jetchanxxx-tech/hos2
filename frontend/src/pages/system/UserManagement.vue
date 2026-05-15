<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'
import StatusPill from '@/components/StatusPill.vue'
import DataTable from '@/components/DataTable.vue'

const users = ref<any[]>([])
const loading = ref(true)

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'nameMasked', label: '姓名' },
  { key: 'role', label: '角色' },
  { key: 'status', label: '状态' },
  { key: 'dataAuthConsent', label: '数据授权' },
  { key: 'createdAt', label: '注册时间' },
]

const roleLabels: Record<string, string> = {
  RESIDENT: '星球居民', BUTLER_MEDICAL: '医疗管家', BUTLER_SERVICE: '服务管家',
  HOSPITAL_ADMIN: '医院管理者', OPS_ADMIN: '运营管理', SUPER_ADMIN: '超级管理员',
}

onMounted(async () => {
  loading.value = true
  try {
    const res: any = await adminApi.listUsers(1, 100)
    users.value = res.data?.records || res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})
</script>

<template>
  <div class="page">
    <div class="section-header">
      <div class="section-label">系统管理</div>
      <h1 class="section-title">用户管理</h1>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else class="card">
      <DataTable :columns="columns" :rows="users" row-key="id">
        <template #cell-role="{ value }">
          <StatusPill :status="value === 'SUPER_ADMIN' || value === 'HOSPITAL_ADMIN' ? 'progress' : 'active'">
            {{ roleLabels[value as string] || value }}
          </StatusPill>
        </template>
        <template #cell-status="{ value }">
          <StatusPill :status="value === 'ACTIVE' ? 'completed' : value === 'SUSPENDED' ? 'blocked' : 'todo'">
            {{ value === 'ACTIVE' ? '正常' : value === 'SUSPENDED' ? '已停用' : '未激活' }}
          </StatusPill>
        </template>
        <template #cell-dataAuthConsent="{ value }">
          <StatusPill :status="value === 1 ? 'completed' : 'blocked'">
            {{ value === 1 ? '已授权' : '未授权' }}
          </StatusPill>
        </template>
      </DataTable>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: var(--space-10) var(--space-8); }
.section-header { margin-bottom: var(--space-6); }
.section-label { font-family: var(--font-mono); font-size: var(--text-xs); text-transform: uppercase; letter-spacing: 0.08em; color: var(--fg-muted); margin-bottom: var(--space-2); }
.section-title { font-family: var(--font-display); font-size: var(--text-2xl); font-weight: 700; }
.card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: var(--space-6); }
.loading { text-align: center; color: var(--fg-muted); padding: var(--space-12); }
</style>
