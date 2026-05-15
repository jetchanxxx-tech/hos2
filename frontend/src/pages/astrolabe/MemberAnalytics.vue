<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'
import KpiCard from '@/components/KpiCard.vue'

const stats = ref<any>({})

onMounted(async () => {
  try {
    const res: any = await adminApi.stats()
    stats.value = res.data || res || {}
  } catch (e) { console.error(e) }
})
</script>

<template>
  <div class="page">
    <div class="section-header">
      <div class="section-label">惠福星盘</div>
      <h1 class="section-title">会员分析</h1>
    </div>

    <div class="kpi-row">
      <KpiCard label="总用户数" :value="stats.totalUsers || 0" />
      <KpiCard label="活跃居民" :value="stats.activeResidents || 0" />
      <KpiCard label="管家数量" :value="stats.butlers || 0" />
    </div>

    <div class="card" style="margin-top:var(--space-6)">
      <h3>会员增长趋势</h3>
      <p class="placeholder">图表区域（接入 ECharts 后展示）</p>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: var(--space-10) var(--space-8); }
.section-header { margin-bottom: var(--space-6); }
.section-label { font-family: var(--font-mono); font-size: var(--text-xs); text-transform: uppercase; letter-spacing: 0.08em; color: var(--fg-muted); margin-bottom: var(--space-2); }
.section-title { font-family: var(--font-display); font-size: var(--text-2xl); font-weight: 700; }
.kpi-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: var(--space-4); }
.card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: var(--space-6); }
.card h3 { font-size: var(--text-sm); font-weight: 600; }
.placeholder { font-size: var(--text-sm); color: var(--fg-muted); margin-top: var(--space-4); text-align: center; padding: var(--space-10); }
</style>
