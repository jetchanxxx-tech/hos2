<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { recordApi } from '@/api'

const records = ref<any[]>([])
const labTrend = ref<any[]>([])
const loading = ref(true)
const filterType = ref('')
const expandedId = ref<number | null>(null)
const showTrend = ref(false)
const trendCode = ref('')
const userId = ref<number>(0)

const recordTypes = ['PREGNANCY_PREP','CHECKUP','DELIVERY','POSTPARTUM','CHILD_CHECKUP','REHABILITATION','SELF_RECORD','VACCINATION']
const typeLabels: Record<string, string> = {
  PREGNANCY_PREP:'备孕', CHECKUP:'产检', REPORT:'报告', FOLLOWUP:'随访', IMMUNIZATION:'疫苗',
  SURGERY:'手术', DELIVERY:'分娩', SELF_RECORD:'自测', MILESTONE:'里程碑',
  POSTPARTUM:'产后', CHILD_CHECKUP:'儿保', REHABILITATION:'康复', VACCINATION:'接种',
}

const timelineRecords = computed(() => records.value)

async function load() {
  loading.value = true
  try {
    const res: any = await recordApi.timeline(userId.value || 0, 1, 100, filterType.value)
    records.value = (res.data?.records || res.data || [])
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function loadTrend(code: string) {
  trendCode.value = code
  try {
    const res: any = await recordApi.trend(code)
    labTrend.value = res.data || []
  } catch (e) { labTrend.value = [] }
  showTrend.value = true
}

function toggleExpand(id: number) {
  expandedId.value = expandedId.value === id ? null : id
  if (expandedId.value === id) {
    const rec = records.value.find((r: any) => r.id === id)
    if (rec?.detailJson) {
      try { rec._detail = JSON.parse(rec.detailJson) } catch(e) { rec._detail = rec.detailJson }
    }
  }
}

onMounted(async () => {
  try {
    const me: any = await (await import('@/api')).userApi.me()
    userId.value = (me.data?.id) || 200
  } catch(e) {}
  await load()
})
</script>

<template>
  <div class="page">
    <div class="section-header">
      <div class="section-label">惠福时光轴</div>
      <h1 class="section-title">全周期健康时间轴</h1>
      <p class="section-desc">从备孕到育儿，关键节点自动标注，异常指标标红提醒。</p>
    </div>

    <div class="filters">
      <button v-for="t in recordTypes" :key="t" class="filter-chip" :class="{ active: filterType === t }"
        @click="filterType = filterType === t ? '' : t; load()">
        {{ typeLabels[t] || t }}
      </button>
      <button class="filter-chip mild" @click="showTrend = !showTrend; showTrend && trendCode && loadTrend(trendCode)">
        📈 指标趋势
      </button>
    </div>

    <!-- 指标趋势图 -->
    <div v-if="showTrend" class="trend-panel">
      <h3>指标趋势：{{ trendCode || '选择指标' }}</h3>
      <div class="trend-select">
        <button v-for="c in ['OGTT_1H','FPG','HGB','WBC']" :key="c" class="filter-chip" :class="{active:trendCode===c}"
          @click="loadTrend(c)">{{ c }}</button>
      </div>
      <div class="trend-bars">
        <div v-for="(pt, i) in labTrend" :key="i" class="trend-bar-wrap">
          <div class="bar-date">{{ pt.reportDate }}</div>
          <div class="bar-track"><div class="bar-fill" :style="{ width: pt.valuePercent || 50, background: pt.isAbnormal ? 'var(--danger)' : 'var(--accent)' }"></div></div>
          <div class="bar-val" :class="{danger: pt.isAbnormal}">{{ pt.resultValue }}{{ pt.unit }}</div>
        </div>
        <div v-if="labTrend.length === 0" class="hint">选择指标代码查看趋势</div>
      </div>
      <button class="btn-close" @click="showTrend = false">✕</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else class="timeline">
      <div v-for="rec in timelineRecords" :key="rec.id" class="timeline-item" :class="{ abnormal: rec.abnormalFlag > 0, expanded: expandedId === rec.id }">
        <div class="timeline-dot" :class="{ abnormal: rec.abnormalFlag > 0 }"></div>
        <div class="timeline-content" @click="toggleExpand(rec.id)">
          <div class="timeline-date">{{ rec.eventDate }}
            <span v-if="rec.gestationalWeek" class="gest-week-tag">{{ rec.gestationalWeek }}</span>
          </div>
          <div class="timeline-title">{{ rec.eventTitle }}
            <span v-if="rec.abnormalFlag > 0" class="abnormal-badge">⚠ 异常</span>
          </div>
          <div v-if="rec.eventSummary" class="timeline-summary">{{ rec.eventSummary }}</div>
          <div class="timeline-meta">
            <span class="source-tag">{{ typeLabels[rec.recordType] || rec.recordType }}</span>
            <span class="source-tag src">{{ rec.source }}</span>
            <span v-if="rec.hospitalDept">{{ rec.hospitalDept }}</span>
            <span v-if="rec.attendingDoctor">{{ rec.attendingDoctor }}</span>
          </div>

          <!-- 展开详情 -->
          <div v-if="expandedId === rec.id && rec._detail" class="record-detail">
            <table class="detail-table">
              <tr v-for="(v,k) in rec._detail" :key="k"><th>{{ k }}</th><td>{{ v }}</td></tr>
            </table>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!loading && records.length === 0" class="empty">
      <p>暂无记录</p>
      <p class="hint">请在服务器导入演示数据：mysql < database/migrations/V3__demo_data.sql</p>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 800px; margin: 0 auto; padding: var(--space-10) var(--space-8); }
.section-header { margin-bottom: var(--space-8); }
.section-label { font-family: var(--font-mono); font-size: var(--text-xs); text-transform: uppercase; letter-spacing: 0.08em; color: var(--fg-muted); margin-bottom: var(--space-2); }
.section-title { font-family: var(--font-display); font-size: var(--text-3xl); font-weight: 700; letter-spacing: -0.02em; }
.section-desc { font-size: var(--text-md); color: var(--fg-secondary); margin-top: var(--space-2); }
.filters { display: flex; gap: var(--space-2); flex-wrap: wrap; margin-bottom: var(--space-6); }
.filter-chip { padding: var(--space-1) var(--space-3); border-radius: 100px; border: 1px solid var(--border); background: var(--surface); font-size: var(--text-xs); cursor: pointer; color: var(--fg-secondary); }
.filter-chip.active, .filter-chip:hover { background: var(--accent-bg); border-color: var(--accent-light); color: var(--accent); }
.filter-chip.mild { opacity: 0.7; }
.loading, .empty { text-align: center; color: var(--fg-muted); padding: var(--space-12); }
.hint { font-size: var(--text-xs); margin-top: var(--space-2); }

/* Timeline */
.timeline { position: relative; padding-left: var(--space-8); }
.timeline::before { content: ''; position: absolute; left: 7px; top: 0; bottom: 0; width: 2px; background: var(--border); }
.timeline-item { position: relative; margin-bottom: var(--space-5); }
.timeline-dot { position: absolute; left: calc(-1 * var(--space-8) + 3px); top: 6px; width: 10px; height: 10px; border-radius: 50%; background: var(--accent); border: 2px solid var(--surface); z-index: 1; }
.timeline-dot.abnormal { background: var(--danger); }
.timeline-content { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: var(--space-4); cursor: pointer; transition: border-color 0.2s; }
.timeline-content:hover { border-color: var(--accent-light); }
.timeline-item.abnormal .timeline-content { border-left: 3px solid var(--danger); }
.timeline-item.expanded .timeline-content { border-color: var(--accent); }
.timeline-date { font-family: var(--font-mono); font-size: var(--text-xs); color: var(--fg-muted); display: flex; align-items: center; gap: var(--space-2); }
.gest-week-tag { background: var(--accent-bg); color: var(--accent); font-size: 10px; padding: 1px 6px; border-radius: 4px; font-family: var(--font-mono); }
.timeline-title { font-weight: 600; font-size: var(--text-base); margin-top: var(--space-1); display: flex; align-items: center; gap: var(--space-2); flex-wrap: wrap; }
.abnormal-badge { font-size: var(--text-xs); color: var(--danger); font-weight: 600; }
.timeline-summary { font-size: var(--text-sm); color: var(--fg-secondary); margin-top: var(--space-1); }
.timeline-meta { display: flex; gap: var(--space-2); margin-top: var(--space-2); font-size: var(--text-xs); color: var(--fg-muted); flex-wrap: wrap; }
.source-tag { background: var(--accent-bg); color: var(--accent); padding: 1px 6px; border-radius: 4px; }
.source-tag.src { background: var(--surface-alt); color: var(--fg-muted); }

/* Record detail */
.record-detail { margin-top: var(--space-3); padding-top: var(--space-3); border-top: 1px dashed var(--border); }
.detail-table { width: 100%; border-collapse: collapse; font-size: var(--text-xs); }
.detail-table th { text-align: left; padding: 2px 8px; color: var(--fg-muted); width: 120px; vertical-align: top; }
.detail-table td { padding: 2px 8px; color: var(--fg-secondary); }

/* Trend panel */
.trend-panel { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: var(--space-6); margin-bottom: var(--space-6); position: relative; }
.trend-panel h3 { font-size: var(--text-sm); margin-bottom: var(--space-3); }
.trend-select { display: flex; gap: var(--space-2); margin-bottom: var(--space-4); }
.trend-bars { display: flex; flex-direction: column; gap: var(--space-3); }
.trend-bar-wrap { display: flex; align-items: center; gap: var(--space-3); }
.bar-date { font-size: var(--text-xs); color: var(--fg-muted); min-width: 70px; }
.bar-track { flex: 1; height: 20px; background: var(--surface-alt); border-radius: 10px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 10px; min-width: 4px; transition: width 0.3s; }
.bar-val { font-size: var(--text-xs); font-weight: 600; min-width: 60px; }
.bar-val.danger { color: var(--danger); }
.btn-close { position: absolute; top: 8px; right: 12px; background: none; border: none; cursor: pointer; font-size: var(--text-lg); color: var(--fg-muted); }
</style>
