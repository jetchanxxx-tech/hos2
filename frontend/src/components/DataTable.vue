<script setup lang="ts" generic="T">
defineProps<{
  columns: { key: string; label: string; align?: 'left' | 'right' | 'center'; width?: string }[]
  rows: T[]
  rowKey?: string
}>()
</script>

<template>
  <div class="table-wrapper">
    <table class="db-table">
      <thead>
        <tr>
          <th v-for="col in columns" :key="col.key"
            :style="{ textAlign: col.align || 'left', width: col.width }">
            {{ col.label }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, idx) in rows" :key="(rowKey && (row as any)[rowKey]) || idx">
          <td v-for="col in columns" :key="col.key"
            :style="{ textAlign: col.align || 'left' }">
            <slot :name="`cell-${col.key}`" :row="row" :value="(row as any)[col.key]">
              {{ (row as any)[col.key] }}
            </slot>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.table-wrapper { overflow-x: auto; }
.db-table { width: 100%; border-collapse: collapse; font-size: var(--text-sm); }
.db-table thead th {
  font-family: var(--font-mono); font-size: var(--text-xs);
  text-transform: uppercase; letter-spacing: 0.06em;
  color: var(--fg-muted); font-weight: 500; text-align: left;
  padding: var(--space-2) var(--space-4); border-bottom: 1px solid var(--border); white-space: nowrap;
}
.db-table tbody td {
  padding: var(--space-3) var(--space-4); border-bottom: 1px solid var(--border-light);
  color: var(--fg); vertical-align: middle;
}
.db-table tbody tr:hover { background: var(--surface-alt); }
.db-table tbody tr:last-child td { border-bottom: none; }
</style>
