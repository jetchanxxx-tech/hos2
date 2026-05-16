<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi, dashboardApi, pkgApi, complaintApi } from '@/api'
import KpiCard from '@/components/KpiCard.vue'

const stats = ref<any>({ totalUsers: 0, activeResidents: 0, butlers: 0 })
const kpiSummary = ref<any>({})
const leaderboard = ref<any[]>([])
const auditLogs = ref<any[]>([])
const tab = ref<'users'|'packages'|'knowledge'|'audit'|'sessions'|'orders'|'complaints'>('users')
const users = ref<any[]>([])
const packages = ref<any[]>([])
const knowledge = ref<any[]>([])
const sessions = ref<any[]>([])
const orders = ref<any[]>([])
const complaints = ref<any[]>([])

onMounted(async () => {
  const [s, k, l, a, u, p, kn, ss, od, cp] = await Promise.all([
    adminApi.stats(),
    dashboardApi.kpiSummary(),
    dashboardApi.butlerLeaderboard(),
    adminApi.auditLogs(1, 20),
    adminApi.listUsers(1, 50),
    pkgApi.list(1, 50),
    adminApi.listKnowledge(1, 50),
    adminApi.listChatSessions(1, 50),
    adminApi.listOrders(1, 50),
    complaintApi.list('PENDING', 1, 50),
  ])
  stats.value = (s as any).data || {}
  kpiSummary.value = (k as any).data || {}
  leaderboard.value = ((l as any).data?.topButlers) || []
  auditLogs.value = ((a as any).data?.content) || []
  users.value = ((u as any).data?.content) || []
  packages.value = ((p as any).data?.content) || []
  knowledge.value = ((kn as any).data?.content) || []
  sessions.value = ((ss as any).data?.content) || []
  orders.value = ((od as any).data?.content) || []
  complaints.value = ((cp as any).data?.content) || []
})

async function handleRoleChange(userId: number, role: string) {
  await adminApi.updateUserRole(userId, role)
  const idx = users.value.findIndex((u: any) => u.id === userId)
  if (idx >= 0) users.value[idx].role = role
}

async function handleStatusToggle(userId: number, status: string) {
  await adminApi.updateUserStatus(userId, status)
  const idx = users.value.findIndex((u: any) => u.id === userId)
  if (idx >= 0) users.value[idx].status = status
}

const roleOptions = ['RESIDENT','BUTLER_MEDICAL','BUTLER_SERVICE','HOSPITAL_ADMIN','OPS_ADMIN','SUPER_ADMIN']
</script>

<template>
  <div class="page">
    <h1>系统管理</h1>

    <!-- KPI Row -->
    <div class="kpi-row">
      <KpiCard label="总用户" :value="stats.totalUsers" />
      <KpiCard label="活跃居民" :value="stats.activeResidents" />
      <KpiCard label="管家" :value="stats.butlers" />
      <KpiCard label="活跃会员" :value="kpiSummary.activeMembers" />
      <KpiCard label="本月收入" :value="'¥' + (kpiSummary.monthlyRevenue || 0)" />
      <KpiCard label="随访完成率" :value="(kpiSummary.followupRate || 0) + '%'" />
    </div>

    <!-- Tabs -->
    <div class="tabs">
      <button :class="{active: tab==='users'}" @click="tab='users'">用户管理</button>
      <button :class="{active: tab==='packages'}" @click="tab='packages'">服务包</button>
      <button :class="{active: tab==='knowledge'}" @click="tab='knowledge'">知识库</button>
      <button :class="{active: tab==='audit'}" @click="tab='audit'">审计日志</button>
      <button :class="{active: tab==='sessions'}" @click="tab='sessions'">客服会话</button>
      <button :class="{active: tab==='orders'}" @click="tab='orders'">服务订单</button>
      <button :class="{active: tab==='complaints'}" @click="tab='complaints'">投诉管理</button>
    </div>

    <!-- Users -->
    <div v-if="tab==='users'" class="table-wrap">
      <table><thead><tr><th>ID</th><th>姓名</th><th>角色</th><th>状态</th><th>数据授权</th></tr></thead><tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.id }}</td><td>{{ u.nameMasked }}</td>
          <td><select :value="u.role" @change="handleRoleChange(u.id, ($event.target as HTMLSelectElement).value)"><option v-for="r in roleOptions" :key="r" :value="r">{{ r }}</option></select></td>
          <td><button :class="u.status === 'ACTIVE' ? 'btn-green' : 'btn-red'" @click="handleStatusToggle(u.id, u.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE')">{{ u.status }}</button></td>
          <td>{{ u.dataAuthConsent ? '已授权' : '未授权' }}</td>
        </tr>
      </tbody></table>
    </div>

    <!-- Packages -->
    <div v-if="tab==='packages'" class="table-wrap">
      <table><thead><tr><th>ID</th><th>名称</th><th>类型</th><th>价格</th><th>状态</th></tr></thead><tbody>
        <tr v-for="p in packages" :key="p.id"><td>{{ p.id }}</td><td>{{ p.name }}</td><td>{{ p.type }}</td><td>¥{{ p.price }}</td><td>{{ p.status }}</td></tr>
      </tbody></table>
    </div>

    <!-- Knowledge -->
    <div v-if="tab==='knowledge'" class="table-wrap">
      <table><thead><tr><th>ID</th><th>问题</th><th>分类</th><th>状态</th><th>浏览量</th></tr></thead><tbody>
        <tr v-for="k in knowledge" :key="k.id"><td>{{ k.id }}</td><td>{{ k.question }}</td><td>{{ k.category }}</td><td>{{ k.status }}</td><td>{{ k.viewCount }}</td></tr>
      </tbody></table>
    </div>

    <!-- Audit Logs -->
    <div v-if="tab==='audit'" class="table-wrap">
      <table><thead><tr><th>时间</th><th>用户ID</th><th>操作</th><th>资源</th><th>结果</th></tr></thead><tbody>
        <tr v-for="l in auditLogs" :key="l.id"><td>{{ l.createdAt }}</td><td>{{ l.userId }}</td><td>{{ l.action }}</td><td>{{ l.resourceType }}/{{ l.resourceId }}</td><td>{{ l.result }}</td></tr>
      </tbody></table>
    </div>

    <!-- Chat Sessions -->
    <div v-if="tab==='sessions'" class="table-wrap">
      <table><thead><tr><th>ID</th><th>会话号</th><th>用户ID</th><th>渠道</th><th>意图</th><th>状态</th><th>升级</th><th>时间</th></tr></thead><tbody>
        <tr v-for="s in sessions" :key="s.id">
          <td>{{ s.id }}</td><td>{{ s.sessionNo }}</td><td>{{ s.userId }}</td><td>{{ s.channel }}</td>
          <td>{{ s.intentType || '-' }}</td><td>{{ s.status }}</td><td>{{ s.escalationLevel }}</td>
          <td>{{ s.createdAt?.substring(0,16) }}</td>
        </tr>
      </tbody></table>
    </div>

    <!-- Orders -->
    <div v-if="tab==='orders'" class="table-wrap">
      <table><thead><tr><th>ID</th><th>订单号</th><th>用户ID</th><th>服务包ID</th><th>金额</th><th>支付方式</th><th>状态</th><th>时间</th></tr></thead><tbody>
        <tr v-for="o in orders" :key="o.id">
          <td>{{ o.id }}</td><td>{{ o.orderNo }}</td><td>{{ o.userId }}</td><td>{{ o.packageId }}</td>
          <td>¥{{ o.finalAmount || o.amount }}</td><td>{{ o.paymentMethod }}</td>
          <td>{{ o.status }}</td><td>{{ o.createdAt?.substring(0,16) }}</td>
        </tr>
      </tbody></table>
    </div>

    <!-- Complaints -->
    <div v-if="tab==='complaints'" class="table-wrap">
      <table><thead><tr><th>ID</th><th>用户ID</th><th>分类</th><th>内容</th><th>状态</th><th>优先级</th><th>负责人</th></tr></thead><tbody>
        <tr v-for="c in complaints" :key="c.id">
          <td>{{ c.id }}</td><td>{{ c.userId }}</td><td>{{ c.category }}</td>
          <td>{{ (c.content||'').substring(0,40) }}</td>
          <td>{{ c.status }}</td><td>{{ c.priority }}</td><td>{{ c.assignedTo||'-' }}</td>
        </tr>
        <tr v-if="complaints.length===0"><td colspan="7" class="empty-hint">暂无投诉</td></tr>
      </tbody></table>
    </div>

    <!-- Butler Leaderboard -->
    <div class="section" style="margin-top: var(--space-8)">
      <h2>医护积分榜</h2>
      <div class="table-wrap"><table><thead><tr><th>排名</th><th>管家</th><th>随访完成数</th><th>积分</th></tr></thead><tbody>
        <tr v-for="b in leaderboard" :key="b.rank"><td>#{{ b.rank }}</td><td>{{ b.name }}</td><td>{{ b.followupCount }}</td><td>{{ b.points }}</td></tr>
      </tbody></table></div>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: var(--space-8); }
h1 { font-family: var(--font-display); font-size: var(--text-2xl); margin-bottom: var(--space-6); }
h2 { font-family: var(--font-display); font-size: var(--text-lg); margin-bottom: var(--space-4); }
.kpi-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: var(--space-4); margin-bottom: var(--space-6); }
.tabs { display: flex; gap: 0; margin-bottom: var(--space-6); border-bottom: 2px solid var(--border); }
.tabs button { padding: var(--space-2) var(--space-5); border: none; background: none; cursor: pointer; font-size: var(--text-sm); color: var(--fg-muted); }
.tabs button.active { color: var(--accent); border-bottom: 2px solid var(--accent); margin-bottom: -2px; font-weight: 600; }
.tabs button:hover { color: var(--fg); }
select { padding: 2px 4px; border: 1px solid var(--border); border-radius: 4px; font-size: var(--text-xs); }
.btn-green { background: var(--success-bg); color: var(--success); border: none; padding: 2px 8px; border-radius: 4px; cursor: pointer; font-size: var(--text-xs); }
.btn-red { background: var(--danger-bg); color: var(--danger); border: none; padding: 2px 8px; border-radius: 4px; cursor: pointer; font-size: var(--text-xs); }
.section { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: var(--space-6); }
.table-wrap { overflow-x: auto; }
.table-wrap table { width: 100%; border-collapse: collapse; font-size: var(--text-sm); }
.table-wrap th { text-align: left; padding: var(--space-2) var(--space-3); border-bottom: 2px solid var(--border); color: var(--fg-muted); font-weight: 600; white-space: nowrap; }
.table-wrap td { padding: var(--space-2) var(--space-3); border-bottom: 1px solid var(--border); }
.empty-hint { text-align: center; color: var(--fg-muted); padding: var(--space-4); }
</style>
