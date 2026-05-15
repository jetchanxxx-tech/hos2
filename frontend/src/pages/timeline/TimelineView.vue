<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { recordApi } from '@/api'
import StatusPill from '@/components/StatusPill.vue'

const records = ref<any[]>([])
const loading = ref(true)
const filterType = ref('')
const userId = ref(100) // Demo user

const recordTypes = ['CHECKUP', 'REPORT', 'FOLLOWUP', 'IMMUNIZATION', 'SURGERY', 'DELIVERY', 'SELF_RECORD', 'MILESTONE']
const typeLabels: Record<string, string> = {
  CHECKUP: '产检', REPORT: '报告', FOLLOWUP: '随访', IMMUNIZATION: '疫苗',
  SURGERY: '手术', DELIVERY: '分娩', SELF_RECORD: '自测', MILESTONE: '里程碑',
}

async function load() {
  loading.value = true
  try {
    const res: any = await recordApi.timeline(userId.value, 1, 50, filterType.value)
    records.value = (res.data?.records || res.data || [])
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(load)
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
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else class="timeline">
      <div v-for="rec in records" :key="rec.id" class="timeline-item" :class="{ abnormal: rec.abnormalFlag > 0 }">
        <div class="timeline-dot" :class="{ abnormal: rec.abnormalFlag > 0 }"></div>
        <div class="timeline-content">
          <div class="timeline-date">{{ rec.eventDate }}</div>
          <div class="timeline-title">
            {{ rec.eventTitle }}
            <span v-if="rec.gestationalWeek" class="gest-week">{{ rec.gestationalWeek }}</span>
            <StatusPill v-if="rec.abnormalFlag > 0" status="blocked">异常</StatusPill>
          </div>
          <div v-if="rec.eventSummary" class="timeline-summary">{{ rec.eventSummary }}</div>
          <div class="timeline-meta">
            <span class="source-tag">{{ rec.source }}</span>
            <span v-if="rec.hospitalDept">{{ rec.hospitalDept }}</span>
            <span v-if="rec.attendingDoctor">{{ rec.attendingDoctor }}</span>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!loading && records.length === 0" class="empty">暂无记录</div>
  </div>
</template>

<style scoped>
.page { max-width: 800px; margin: 0 auto; padding: var(--space-10) var(--space-8); }
.section-header { margin-bottom: var(--space-8); }
.section-label { font-family: var(--font-mono); font-size: var(--text-xs); text-transform: uppercase; letter-spacing: 0.08em; color: var(--fg-muted); margin-bottom: var(--space-2); }
.section-title { font-family: var(--font-display); font-size: var(--text-3xl); font-weight: 700; letter-spacing: -0.02em; color: var(--fg); }
.section-desc { font-size: var(--text-md); color: var(--fg-secondary); margin-top: var(--space-2); }
.filters { display: flex; gap: var(--space-2); flex-wrap: wrap; margin-bottom: var(--space-6); }
.filter-chip { padding: var(--space-1) var(--space-3); border-radius: 100px; border: 1px solid var(--border); background: var(--surface); font-size: var(--text-xs); cursor: pointer; color: var(--fg-secondary); }
.filter-chip.active, .filter-chip:hover { background: var(--accent-bg); border-color: var(--accent-light); color: var(--accent); }
.loading, .empty { text-align: center; color: var(--fg-muted); padding: var(--space-12); }
.timeline { position: relative; padding-left: var(--space-8); }
.timeline::before { content: ''; position: absolute; left: 7px; top: 0; bottom: 0; width: 2px; background: var(--border); }
.timeline-item { position: relative; margin-bottom: var(--space-6); }
.timeline-dot { position: absolute; left: calc(-1 * var(--space-8) + 3px); top: 6px; width: 10px; height: 10px; border-radius: 50%; background: var(--accent); border: 2px solid var(--surface); z-index: 1; }
.timeline-dot.abnormal { background: var(--danger); }
.timeline-content { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-md); padding: var(--space-4); }
.timeline-item.abnormal .timeline-content { border-left: 3px solid var(--danger); }
.timeline-date { font-family: var(--font-mono); font-size: var(--text-xs); color: var(--fg-muted); }
.timeline-title { font-weight: 600; font-size: var(--text-base); margin-top: var(--space-1); display: flex; align-items: center; gap: var(--space-2); flex-wrap: wrap; }
.gest-week { font-size: var(--text-xs); color: var(--accent); font-weight: 500; }
.timeline-summary { font-size: var(--text-sm); color: var(--fg-secondary); margin-top: var(--space-1); }
.timeline-meta { display: flex; gap: var(--space-2); margin-top: var(--space-2); font-size: var(--text-xs); color: var(--fg-muted); }
.source-tag { background: var(--accent-bg); color: var(--accent); padding: 1px 6px; border-radius: 4px; }
</style>
