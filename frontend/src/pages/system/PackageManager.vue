<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { pkgApi } from '@/api'
import StatusPill from '@/components/StatusPill.vue'
import DataTable from '@/components/DataTable.vue'

const packages = ref<any[]>([])
const loading = ref(true)

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: '服务包名称' },
  { key: 'type', label: '类型' },
  { key: 'price', label: '价格' },
  { key: 'discountPrice', label: '折后价' },
  { key: 'durationDays', label: '有效期(天)' },
  { key: 'status', label: '状态' },
]

const typeLabels: Record<string, string> = { VIP: 'VIP', VVIP: 'VVIP', STANDARD: '标准', EXPERIENCE: '体验' }

onMounted(async () => {
  loading.value = true
  try {
    const res: any = await pkgApi.list(1, 20)
    packages.value = res.data?.records || res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})
</script>

<template>
  <div class="page">
    <div class="section-header">
      <div class="section-label">系统管理</div>
      <h1 class="section-title">服务包配置</h1>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else class="card">
      <DataTable :columns="columns" :rows="packages" row-key="id">
        <template #cell-type="{ value }">
          <StatusPill :status="value === 'VVIP' ? 'blocked' : value === 'VIP' ? 'progress' : 'active'">
            {{ typeLabels[value as string] || value }}
          </StatusPill>
        </template>
        <template #cell-price="{ value }">
          <span style="font-family:var(--font-mono)">¥{{ (value as number)?.toLocaleString() }}</span>
        </template>
        <template #cell-discountPrice="{ value }">
          <span v-if="value" style="font-family:var(--font-mono);color:var(--accent);font-weight:600">
            ¥{{ (value as number)?.toLocaleString() }}
          </span>
          <span v-else>-</span>
        </template>
        <template #cell-status="{ value }">
          <StatusPill :status="value === 'ON_SHELF' ? 'completed' : value === 'DRAFT' ? 'pending' : 'todo'">
            {{ value === 'ON_SHELF' ? '已上架' : value === 'DRAFT' ? '草稿' : '已下架' }}
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
