<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi } from '@/api'
import StatusPill from '@/components/StatusPill.vue'

const family = ref<any>(null)
const members = ref<any[]>([])

onMounted(async () => {
  try {
    const famRes: any = await userApi.myFamily()
    family.value = famRes.data
    if (family.value?.id) {
      const memRes: any = await userApi.getFamilyMembers(family.value.id)
      members.value = memRes.data || []
    }
  } catch (e) { console.error(e) }
})

const relationshipMap: Record<string, string> = {
  SELF: '本人', SPOUSE: '配偶', CHILD: '子女', PARENT: '父母', OTHER: '其他',
}
const shareMap: Record<string, string> = { ALL: '全部共享', REPORT_ONLY: '仅报告', BASIC_ONLY: '仅基础信息', NONE: '不共享' }

async function handleCreateFamily() {
  await userApi.createFamily('我的家庭')
  location.reload()
}
</script>

<template>
  <div class="page">
    <div class="section-header">
      <div class="section-label">惠福同心圆</div>
      <h1 class="section-title">家庭健康账户</h1>
      <p class="section-desc">以"家庭ID"为核心的同心圆管理模型。</p>
    </div>

    <div v-if="family" class="family-card">
      <div class="family-header">
        <div class="family-icon">👨‍👩‍👧</div>
        <div>
          <h2>{{ family.familyName }}</h2>
          <div class="family-meta">
            <span>邀请码：<code>{{ family.inviteCode }}</code></span>
            <span>{{ family.memberCount }} 位成员</span>
          </div>
        </div>
      </div>

      <div class="member-grid">
        <div v-for="m in members" :key="m.id" class="member-card">
          <div class="member-avatar">{{ m.relationship?.charAt(0) || '?' }}</div>
          <div class="member-info">
            <div class="member-relation">{{ relationshipMap[m.relationship] || m.relationship }}</div>
            <div class="member-share">共享范围：{{ shareMap[m.shareScope] || m.shareScope }}</div>
            <StatusPill :status="m.isEmergencyContact ? 'blocked' : 'active'">
              {{ m.isEmergencyContact ? '紧急联系人' : '普通成员' }}
            </StatusPill>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-card">
      <p>暂未创建家庭</p>
      <button class="btn btn-primary" @click="handleCreateFamily">
        创建家庭
      </button>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 800px; margin: 0 auto; padding: var(--space-10) var(--space-8); }
.section-header { margin-bottom: var(--space-8); }
.section-label { font-family: var(--font-mono); font-size: var(--text-xs); text-transform: uppercase; letter-spacing: 0.08em; color: var(--fg-muted); margin-bottom: var(--space-2); }
.section-title { font-family: var(--font-display); font-size: var(--text-3xl); font-weight: 700; letter-spacing: -0.02em; }
.section-desc { font-size: var(--text-md); color: var(--fg-secondary); margin-top: var(--space-2); }
.family-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-xl); padding: var(--space-8); }
.family-header { display: flex; align-items: center; gap: var(--space-4); margin-bottom: var(--space-8); }
.family-icon { font-size: 2.5rem; }
.family-header h2 { font-family: var(--font-display); font-size: var(--text-xl); font-weight: 600; }
.family-meta { display: flex; gap: var(--space-4); font-size: var(--text-xs); color: var(--fg-muted); margin-top: var(--space-1); }
.family-meta code { font-family: var(--font-mono); background: var(--accent-bg); padding: 1px 6px; border-radius: 4px; }
.member-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: var(--space-4); }
.member-card { display: flex; gap: var(--space-4); padding: var(--space-4); background: var(--surface-alt); border-radius: var(--radius-md); align-items: center; }
.member-avatar { width: 48px; height: 48px; border-radius: 50%; background: var(--accent); color: #fff; display: flex; align-items: center; justify-content: center; font-size: var(--text-lg); font-weight: 600; }
.member-relation { font-weight: 600; font-size: var(--text-sm); }
.member-share { font-size: var(--text-xs); color: var(--fg-muted); }
.empty-card { text-align: center; padding: var(--space-12); color: var(--fg-muted); }
.btn { padding: var(--space-2) var(--space-5); border-radius: var(--radius-sm); font-size: var(--text-sm); font-weight: 500; cursor: pointer; border: none; }
.btn-primary { background: var(--accent); color: #fff; margin-top: var(--space-4); }
</style>
