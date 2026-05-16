<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { pkgApi, pkgOrderApi } from '@/api'

const packages = ref<any[]>([])
const orders = ref<any[]>([])
const tab = ref<'buy'|'orders'>('buy')
const msg = ref('')
const loading = ref(true)

onMounted(async () => {
  try {
    const [p, o] = await Promise.all([
      pkgApi.list(1, 50),
      pkgOrderApi.myOrders(1, 20),
    ])
    packages.value = (p as any).data?.records || []
    orders.value = (o as any).data?.records || []
  } catch(e) { console.error(e) }
  finally { loading.value = false }
})

async function handleBuy(pkgId: number, name: string) {
  if (!confirm(`确定购买 ${name} 吗？`)) return
  try {
    msg.value = ''
    await pkgOrderApi.create(pkgId)
    msg.value = `成功购买 ${name}`
    const o: any = await pkgOrderApi.myOrders(1, 20)
    orders.value = o.data?.records || []
  } catch(e: any) { msg.value = e?.response?.data?.message || '购买失败' }
}
</script>

<template>
  <div class="page">
    <h1>服务包中心</h1>
    <div v-if="msg" :class="msg.includes('成功')?'msg-ok':'msg-err'">{{ msg }}</div>

    <div class="tabs">
      <button :class="{active:tab==='buy'}" @click="tab='buy'">服务包选购</button>
      <button :class="{active:tab==='orders'}" @click="tab='orders'">我的订单</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="tab==='buy'" class="pkg-grid">
      <div v-for="p in packages" :key="p.id" class="pkg-card">
        <div class="pkg-name">{{ p.name }}</div>
        <div class="pkg-subtitle">{{ p.subtitle }}</div>
        <div class="pkg-price">¥{{ p.discountPrice || p.price }}</div>
        <div v-if="p.price !== p.discountPrice" class="pkg-original">原价 ¥{{ p.price }}</div>
        <div class="pkg-duration">有效期 {{ p.durationDays }} 天</div>
        <div class="pkg-type">{{ p.type }}</div>
        <button class="btn-buy" @click="handleBuy(p.id, p.name)">立即购买</button>
      </div>
      <div v-if="packages.length===0" class="empty">暂无在售服务包</div>
    </div>

    <div v-else class="table-wrap">
      <table><thead><tr><th>订单号</th><th>服务包ID</th><th>金额</th><th>状态</th><th>时间</th></tr></thead><tbody>
        <tr v-for="o in orders" :key="o.id">
          <td>{{ o.orderNo }}</td><td>{{ o.packageId }}</td><td>¥{{ o.finalAmount||o.amount }}</td>
          <td>{{ o.status }}</td><td>{{ o.createdAt?.substring(0,16) }}</td>
        </tr>
        <tr v-if="orders.length===0"><td colspan="5" class="empty">暂无订单</td></tr>
      </tbody></table>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 900px; margin: 0 auto; padding: var(--space-8); }
h1 { font-family: var(--font-display); font-size: var(--text-2xl); margin-bottom: var(--space-6); }
.tabs { display: flex; gap:0; margin-bottom: var(--space-6); border-bottom:2px solid var(--border); }
.tabs button { padding: var(--space-2) var(--space-5); border:none; background:none; cursor:pointer; font-size: var(--text-sm); color: var(--fg-muted); }
.tabs button.active { color: var(--accent); border-bottom:2px solid var(--accent); margin-bottom:-2px; font-weight:600; }
.msg-ok { background:var(--success-bg);color:var(--success);padding:var(--space-2) var(--space-3);border-radius:var(--radius-sm);margin-bottom:var(--space-4); }
.msg-err { background:var(--danger-bg);color:var(--danger);padding:var(--space-2) var(--space-3);border-radius:var(--radius-sm);margin-bottom:var(--space-4); }
.loading,.empty { text-align:center; color:var(--fg-muted); padding:var(--space-8); }
.pkg-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: var(--space-4); }
.pkg-card { background:var(--surface); border:1px solid var(--border); border-radius:var(--radius-lg); padding:var(--space-6); }
.pkg-name { font-weight:600; font-size:var(--text-lg); }
.pkg-subtitle { font-size:var(--text-sm); color:var(--fg-secondary); margin-top:var(--space-1); }
.pkg-price { font-size:var(--text-2xl); font-weight:700; color:var(--accent); margin-top:var(--space-4); }
.pkg-original { font-size:var(--text-xs); color:var(--fg-muted); text-decoration:line-through; }
.pkg-duration { font-size:var(--text-xs); color:var(--fg-muted); margin-top:var(--space-2); }
.pkg-type { display:inline-block; padding:2px 8px; background:var(--accent-bg); color:var(--accent); border-radius:4px; font-size:var(--text-xs); margin-top:var(--space-2); }
.btn-buy { width:100%; margin-top:var(--space-4); padding:var(--space-3); background:var(--accent); color:#fff; border:none; border-radius:var(--radius-sm); font-size:var(--text-base); font-weight:600; cursor:pointer; }
.btn-buy:hover { opacity:0.9; }
.table-wrap { overflow-x:auto; }
.table-wrap table { width:100%; border-collapse:collapse; font-size:var(--text-sm); }
.table-wrap th { text-align:left; padding:var(--space-2) var(--space-3); border-bottom:2px solid var(--border); color:var(--fg-muted); }
.table-wrap td { padding:var(--space-2) var(--space-3); border-bottom:1px solid var(--border); }
</style>
