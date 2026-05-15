<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
  delta?: string
  deltaType?: 'up' | 'down' | 'neutral'
}>()
</script>

<template>
  <div class="kpi-card">
    <div class="kpi-label">{{ label }}</div>
    <div class="kpi-value">{{ value }}</div>
    <div v-if="delta" class="kpi-delta" :class="deltaType || 'neutral'">
      {{ deltaType === 'up' ? '↑' : deltaType === 'down' ? '↓' : '' }} {{ delta }}
    </div>
  </div>
</template>

<style scoped>
.kpi-card {
  background: var(--surface); border: 1px solid var(--border);
  border-radius: var(--radius-lg); padding: var(--space-5) var(--space-6);
  transition: box-shadow var(--duration-fast) var(--ease-out);
}
.kpi-card:hover { box-shadow: var(--shadow-sm); }
.kpi-label { font-size: var(--text-xs); color: var(--fg-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: var(--space-1); }
.kpi-value { font-family: var(--font-display); font-size: var(--text-2xl); font-weight: 680; font-variant-numeric: tabular-nums; letter-spacing: -0.015em; color: var(--fg); line-height: 1.2; }
.kpi-delta { font-size: var(--text-xs); margin-top: var(--space-1); display: flex; align-items: center; gap: var(--space-1); }
.kpi-delta.up { color: var(--success); }
.kpi-delta.down { color: var(--danger); }
.kpi-delta.neutral { color: var(--fg-muted); }
</style>
