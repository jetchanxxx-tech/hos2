<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { dashboardApi } from '@/api'
import KpiCard from '@/components/KpiCard.vue'
import DataTable from '@/components/DataTable.vue'

const kpis = ref<any>({})
const salesRanking = ref<any[]>([])
const activities = ref<any[]>([])
const leaderboard = ref<any>({})

const salesColumns = [
  { key: 'packageName', label: '服务包' },
  { key: 'price', label: '单价', align: 'right' as const },
  { key: 'count', label: '销量', align: 'right' as const },
  { key: 'revenue', label: '营收', align: 'right' as const },
  { key: 'status', label: '状态' },
]

onMounted(async () => {
  try {
    const [k, s, a, l] = await Promise.all([
      dashboardApi.kpiSummary(),
      dashboardApi.salesRanking(),
      dashboardApi.activityFeed(),
      dashboardApi.butlerLeaderboard(),
    ])
    kpis.value = (k as any).data || k
    salesRanking.value = (s as any).data || s || []
    activities.value = (a as any).data || a || []
    leaderboard.value = (l as any).data || l || {}
  } catch (e) { console.error(e) }
})
</script>

<template>
  <div class="page">
    <div class="dash-header">
      <div class="dash-emoji">📊</div>
      <div>
        <h1 class="dash-title">惠福星盘 · 运营驾驶舱</h1>
        <div class="dash-meta">
          <span>数据刷新：刚刚</span>
          <span class="live-pill"><span class="dot"></span> 实时 · 已同步</span>
        </div>
      </div>
    </div>

    <div class="section-label">核心指标</div>
    <div class="kpi-row">
      <KpiCard label="活跃会员" :value="kpis.activeMembers || 0" :delta="'12% vs 上月'" delta-type="up" />
      <KpiCard label="本月营收" :value="`¥${(kpis.monthlyRevenue || 0).toLocaleString()}`" :delta="'8.5% vs 上月'" delta-type="up" />
      <KpiCard label="随访完成率" :value="`${kpis.followupRate || 0}%`" :delta="'2.1% vs 上月'" delta-type="up" />
      <KpiCard label="满意度评分" :value="kpis.satisfactionScore || '4.82'" :delta="'0.15 vs 上月'" delta-type="up" />
    </div>

    <div class="two-col">
      <div class="card">
        <div class="card-title">📦 服务包销售排行</div>
        <DataTable :columns="salesColumns" :rows="salesRanking" row-key="packageId">
          <template #cell-status="{ row }">
            <span class="pill pill-progress">热销中</span>
          </template>
        </DataTable>
      </div>

      <div style="display:flex;flex-direction:column;gap:var(--space-6);">
        <div class="card">
          <div class="card-title">🕐 最近动态</div>
          <div class="activity-list">
            <div v-for="(act, i) in activities" :key="i" class="activity-row">
              <div class="activity-avatar" :style="{ background: i === 4 ? 'var(--danger)' : i === 2 ? 'var(--success)' : 'var(--accent)' }">
                {{ (act.actor || '?').charAt(0) }}
              </div>
              <div style="flex:1;">
                <strong>{{ act.actor }}</strong> {{ act.action }} — {{ act.target }}
              </div>
              <div class="activity-time">{{ act.time }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card" style="margin-top:var(--space-6)">
      <div class="card-title">🏆 医护积分榜</div>
      <div class="butler-grid">
        <div v-for="b in leaderboard.topButlers || []" :key="b.rank" class="butler-item">
          <span class="butler-rank">#{{ b.rank }}</span>
          <span>{{ b.name }}</span>
          <span class="butler-pts">{{ b.points }} 分</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 1280px; margin: 0 auto; padding: var(--space-10) var(--space-8); }
.dash-header { display: flex; align-items: flex-start; gap: var(--space-4); margin-bottom: var(--space-8); }
.dash-emoji { font-size: 2rem; }
.dash-title { font-family: var(--font-display); font-size: var(--text-3xl); font-weight: 700; letter-spacing: -0.02em; }
.dash-meta { font-size: var(--text-xs); color: var(--fg-muted); margin-top: var(--space-1); display: flex; gap: var(--space-4); align-items: center; }
.live-pill { color: var(--success); background: var(--success-bg); padding: 2px 10px; border-radius: 100px; font-weight: 500; display: inline-flex; align-items: center; gap: 4px; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: var(--success); animation: pulse 2s infinite; }
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.4; } }
.section-label { font-family: var(--font-mono); font-size: var(--text-xs); text-transform: uppercase; letter-spacing: 0.08em; color: var(--fg-muted); margin-bottom: var(--space-4); }
.kpi-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: var(--space-4); margin-bottom: var(--space-8); }
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-6); }
.card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: var(--space-6); }
.card-title { font-weight: 600; font-size: var(--text-sm); margin-bottom: var(--space-4); }
.pill { display: inline-flex; padding: 2px 10px; border-radius: 100px; font-size: var(--text-xs); font-weight: 500; }
.pill-progress { background: var(--info-bg); color: var(--info); }
.activity-list { display: flex; flex-direction: column; gap: var(--space-3); }
.activity-row { display: flex; align-items: flex-start; gap: var(--space-3); padding: var(--space-2) 0; border-bottom: 1px solid var(--border-light); font-size: var(--text-sm); }
.activity-row:last-child { border-bottom: none; }
.activity-avatar { width: 24px; height: 24px; border-radius: 50%; flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-size: 10px; font-weight: 600; color: #fff; }
.activity-time { font-size: var(--text-xs); color: var(--fg-muted); white-space: nowrap; }
.butler-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: var(--space-3); }
.butler-item { padding: var(--space-3) var(--space-4); background: var(--surface-alt); border-radius: var(--radius-sm); display: flex; align-items: center; gap: var(--space-3); }
.butler-rank { font-family: var(--font-mono); font-weight: 600; color: var(--accent); }
.butler-pts { margin-left: auto; font-family: var(--font-mono); font-size: var(--text-sm); color: var(--fg-muted); }
@media (max-width: 768px) { .two-col { grid-template-columns: 1fr; } .page { padding: var(--space-4); } }
</style>
