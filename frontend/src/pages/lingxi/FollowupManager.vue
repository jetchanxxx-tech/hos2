<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { followupApi } from '@/api'
import StatusPill from '@/components/StatusPill.vue'
import DataTable from '@/components/DataTable.vue'

const tasks = ref<any[]>([])
const stats = ref<any>({})

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'userId', label: '用户ID' },
  { key: 'taskType', label: '类型' },
  { key: 'scheduledDate', label: '计划日期' },
  { key: 'priority', label: '优先级' },
  { key: 'status', label: '状态' },
  { key: 'followupMethod', label: '方式' },
]

const typeLabels: Record<string, string> = {
  GESTATIONAL: '孕期随访', POST_DISCHARGE: '出院随访', MONTHLY_AGE: '月龄随访',
  VACCINE: '疫苗提醒', ABNORMAL_LAB: '异常指标', CHRONIC_DISEASE: '慢病管理',
}

onMounted(async () => {
  try {
    const [t, s] = await Promise.all([followupApi.myTasks(), followupApi.stats()])
    tasks.value = (t as any).data?.records || (t as any).data || []
    stats.value = (s as any).data || s || {}
  } catch (e) { console.error(e) }
})
</script>

<template>
  <div class="page">
    <div class="section-header">
      <div class="section-label">惠福灵犀 · 随访管理</div>
      <h1 class="section-title">智能随访任务看板</h1>
    </div>

    <div class="stats-bar">
      <div v-for="(count, status) in stats" :key="status" class="stat-chip">
        <span class="stat-label">{{ status }}</span>
        <span class="stat-value">{{ count }}</span>
      </div>
    </div>

    <div class="card" style="margin-top:var(--space-6)">
      <DataTable :columns="columns" :rows="tasks" row-key="id">
        <template #cell-taskType="{ value }">
          {{ typeLabels[value as string] || value }}
        </template>
        <template #cell-priority="{ value }">
          <StatusPill :status="value === 'HIGH' || value === 'URGENT' ? 'blocked' : value === 'NORMAL' ? 'active' : 'todo'">
            {{ value }}
          </StatusPill>
        </template>
        <template #cell-status="{ value }">
          <StatusPill :status="value === 'COMPLETED' ? 'completed' : value === 'PENDING' ? 'pending' : 'progress'">
            {{ value }}
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
.stats-bar { display: flex; gap: var(--space-4); flex-wrap: wrap; }
.stat-chip { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: var(--space-3) var(--space-5); display: flex; align-items: center; gap: var(--space-3); }
.stat-label { font-size: var(--text-xs); color: var(--fg-muted); text-transform: uppercase; }
.stat-value { font-family: var(--font-mono); font-size: var(--text-lg); font-weight: 600; color: var(--fg); }
.card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: var(--space-6); }
</style>
