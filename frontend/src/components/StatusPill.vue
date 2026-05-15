<script setup lang="ts">
defineProps<{ status: string }>()
</script>

<template>
  <span class="pill" :class="`pill-${status}`">
    <slot />
  </span>
</template>

<style scoped>
.pill {
  display: inline-flex; align-items: center; padding: 2px 10px;
  border-radius: 100px; font-size: var(--text-xs); font-weight: 500; line-height: 1.6;
}
.pill-completed, .pill-done { background: var(--success-bg); color: var(--success); }
.pill-progress, .pill-active { background: var(--info-bg); color: var(--info); }
.pill-pending, .pill-review { background: var(--warning-bg); color: var(--warning); }
.pill-blocked, .pill-suspended { background: var(--danger-bg); color: var(--danger); }
.pill-todo, .pill-neutral { background: var(--surface-alt); color: var(--fg-muted); }
</style>
