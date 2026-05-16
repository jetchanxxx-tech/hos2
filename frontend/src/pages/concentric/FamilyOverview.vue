<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi } from '@/api'

const family = ref<any>(null)
const members = ref<any[]>([])
const loading = ref(true)
const showInviteModal = ref(false)
const showAddMemberModal = ref(false)
const showJoinModal = ref(false)
const joinInviteCode = ref('')
const joinError = ref('')
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const selectedRelationship = ref('OTHER')
const selectedShareScope = ref('BASIC_ONLY')
const errorMsg = ref('')

onMounted(async () => {
  await loadFamily()
})

async function loadFamily() {
  loading.value = true
  try {
    const famRes: any = await userApi.myFamily()
    family.value = famRes.data
    if (family.value?.id) {
      const memRes: any = await userApi.getFamilyMembers(family.value.id)
      members.value = memRes.data || []
    }
  } catch (e) { errorMsg.value = '加载失败' }
  finally { loading.value = false }
}

async function handleCreateFamily() {
  try {
    errorMsg.value = ''
    const res: any = await userApi.createFamily('我的家庭')
    family.value = res.data
    await loadFamily()
    location.reload()
  } catch (e: any) { errorMsg.value = e?.message || '创建失败' }
}

async function handleRemoveMember(memberUserId: number, name: string) {
  if (!confirm(`确定移除 ${name} 吗？`)) return
  try {
    await userApi.removeFamilyMember(family.value.id, memberUserId)
    await loadFamily()
  } catch (e: any) { errorMsg.value = e?.message || '移除失败' }
}

async function handleSearchUser() {
  if (!searchKeyword.value.trim()) return
  try {
    const res: any = await userApi.searchUsers(searchKeyword.value)
    searchResults.value = res.data?.content || []
  } catch (e: any) { errorMsg.value = e?.message || '搜索失败' }
}

async function handleAddMember(userId: number) {
  try {
    errorMsg.value = ''
    await userApi.addFamilyMember(family.value.id, userId, selectedRelationship.value, selectedShareScope.value)
    showAddMemberModal.value = false
    searchResults.value = []
    searchKeyword.value = ''
    await loadFamily()
  } catch (e: any) { errorMsg.value = e?.message || '添加失败' }
}

async function handleUpdateShareScope(memberUserId: number, shareScope: string) {
  try {
    await userApi.updateShareScope(family.value.id, memberUserId, shareScope)
    await loadFamily()
  } catch (e: any) { errorMsg.value = e?.message || '更新失败' }
}

async function handleToggleEmergency(memberUserId: number) {
  try {
    await userApi.toggleEmergency(family.value.id, memberUserId)
    await loadFamily()
  } catch (e: any) { errorMsg.value = e?.message || '操作失败' }
}

async function handleJoinFamily() {
  joinError.value = ''
  try {
    await userApi.joinFamily(joinInviteCode.value.trim())
    showJoinModal.value = false
    joinInviteCode.value = ''
    await loadFamily()
  } catch (e: any) { joinError.value = e?.response?.data?.message || '加入失败，请检查邀请码' }
}

async function handleDissolveFamily() {
  if (!confirm('确定解散家庭吗？此操作不可恢复。')) return
  try {
    await userApi.dissolveFamily(family.value.id)
    family.value = null
    members.value = []
  } catch (e: any) { errorMsg.value = e?.message || '解散失败' }
}

function copyInviteCode() {
  if (family.value?.inviteCode) {
    navigator.clipboard?.writeText(family.value.inviteCode)
    alert('邀请码已复制: ' + family.value.inviteCode)
  }
}

const relationshipMap: Record<string, string> = {
  SELF: '本人', SPOUSE: '配偶', CHILD: '子女', PARENT: '父母', OTHER: '其他',
}
const shareScopeMap: Record<string, string> = {
  ALL: '全部共享', REPORT_ONLY: '仅报告', BASIC_ONLY: '仅基础信息', NONE: '不共享',
}
</script>

<template>
  <div class="page">
    <div class="section-header">
      <div class="section-label">惠福同心圆</div>
      <h1 class="section-title">家庭健康账户</h1>
      <p class="section-desc">以"家庭ID"为核心的同心圆管理模型。</p>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="family" class="family-card">
      <!-- 家庭头部 -->
      <div class="family-header">
        <div class="family-icon">👨‍👩‍👧</div>
        <div>
          <h2>{{ family.familyName }}</h2>
          <div class="family-meta">
            <span>邀请码：<code>{{ family.inviteCode }}</code></span>
            <button class="btn-link" @click="copyInviteCode">📋 复制</button>
            <span>{{ members.length }} 位成员</span>
          </div>
        </div>
        <div class="family-actions">
          <button class="btn btn-outline" @click="showAddMemberModal = true">+ 添加成员</button>
          <button class="btn btn-outline" @click="showInviteModal = true">📤 邀请成员</button>
          <button class="btn btn-danger-text" @click="handleDissolveFamily" v-if="members.length === 1">解散家庭</button>
          <button class="btn btn-danger-text" @click="handleDissolveFamily" v-else disabled title="请先移除所有成员">解散家庭</button>
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMsg" class="error-banner">{{ errorMsg }}</div>

      <!-- 成员网格 -->
      <div class="member-grid">
        <div v-for="m in members" :key="m.userId" class="member-card">
          <div class="member-avatar">{{ m.relationship?.charAt(0) || '?' }}</div>
          <div class="member-info">
            <div class="member-relation">{{ relationshipMap[m.relationship] || m.relationship }}</div>
            <div class="member-share">
              共享范围：
              <select :value="m.shareScope" @change="handleUpdateShareScope(m.userId, ($event.target as HTMLSelectElement).value)" class="select-inline">
                <option value="ALL">全部共享</option>
                <option value="REPORT_ONLY">仅报告</option>
                <option value="BASIC_ONLY">仅基础</option>
                <option value="NONE">不共享</option>
              </select>
            </div>
            <div class="member-emergency">
              <label class="toggle-label">
                <input type="checkbox" :checked="m.isEmergencyContact" @change="handleToggleEmergency(m.userId)" />
                紧急联系人
              </label>
            </div>
          </div>
          <div class="member-actions" v-if="m.relationship !== 'SELF'">
            <button class="btn-icon" @click="handleRemoveMember(m.userId, relationshipMap[m.relationship])" title="移除">✕</button>
          </div>
        </div>
      </div>

      <!-- 邀请弹窗 -->
      <div v-if="showInviteModal" class="modal-overlay" @click.self="showInviteModal = false">
        <div class="modal">
          <h3>邀请家庭成员</h3>
          <p>将此邀请码分享给家人</p>
          <div class="invite-code-display">{{ family.inviteCode }}</div>
          <p class="hint">家人注册时输入此邀请码，将自动加入家庭</p>
          <button class="btn btn-primary" @click="copyInviteCode">复制邀请码</button>
          <button class="btn btn-secondary" @click="showInviteModal = false">关闭</button>
        </div>
      </div>

      <!-- 添加成员弹窗 -->
      <div v-if="showAddMemberModal" class="modal-overlay" @click.self="showAddMemberModal = false">
        <div class="modal">
          <h3>添加家庭成员</h3>
          <div class="form-row">
            <input v-model="searchKeyword" placeholder="输入手机号搜索用户" class="input" @keyup.enter="handleSearchUser" />
            <button class="btn btn-primary" @click="handleSearchUser">搜索</button>
          </div>
          <div class="form-row">
            <label>关系：</label>
            <select v-model="selectedRelationship" class="select">
              <option value="SPOUSE">配偶</option>
              <option value="CHILD">子女</option>
              <option value="PARENT">父母</option>
              <option value="OTHER">其他</option>
            </select>
            <label>共享范围：</label>
            <select v-model="selectedShareScope" class="select">
              <option value="ALL">全部共享</option>
              <option value="REPORT_ONLY">仅报告</option>
              <option value="BASIC_ONLY">仅基础信息</option>
            </select>
          </div>
          <div v-if="searchResults.length" class="search-results">
            <div v-for="u in searchResults" :key="u.id" class="search-item" @click="handleAddMember(u.id)">
              <span>{{ u.nameMasked || ('用户' + u.id) }}</span>
              <button class="btn btn-sm">添加</button>
            </div>
          </div>
          <div v-else-if="searchKeyword && searchResults.length === 0" class="hint">未找到用户</div>
          <button class="btn btn-secondary" @click="showAddMemberModal = false; searchResults = []; searchKeyword = ''">关闭</button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-card">
      <p>暂未创建家庭</p>
      <div class="empty-buttons">
        <button class="btn btn-primary" @click="handleCreateFamily">创建家庭</button>
        <button class="btn btn-outline" @click="showJoinModal = true">加入已有家庭</button>
      </div>
    </div>

    <!-- 加入家庭弹窗 -->
    <div v-if="showJoinModal" class="modal-overlay" @click.self="showJoinModal = false; joinError = ''">
      <div class="modal">
        <h3>加入已有家庭</h3>
        <p class="hint">输入家庭邀请码（如 HF12345678）</p>
        <input v-model="joinInviteCode" placeholder="邀请码" class="input full" style="font-size:var(--text-lg);text-align:center;letter-spacing:0.1em" maxlength="10" @keyup.enter="handleJoinFamily" />
        <div v-if="joinError" class="error-banner">{{ joinError }}</div>
        <div class="modal-buttons">
          <button class="btn btn-primary" @click="handleJoinFamily">加入</button>
          <button class="btn btn-secondary" @click="showJoinModal = false; joinError = ''; joinInviteCode = ''">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 900px; margin: 0 auto; padding: var(--space-10) var(--space-8); }
.section-header { margin-bottom: var(--space-8); }
.section-label { font-family: var(--font-mono); font-size: var(--text-xs); text-transform: uppercase; letter-spacing: 0.08em; color: var(--fg-muted); margin-bottom: var(--space-2); }
.section-title { font-family: var(--font-display); font-size: var(--text-3xl); font-weight: 700; letter-spacing: -0.02em; }
.section-desc { font-size: var(--text-md); color: var(--fg-secondary); margin-top: var(--space-2); }
.loading { text-align: center; padding: var(--space-12); color: var(--fg-muted); }
.error-banner { background: var(--danger-bg); color: var(--danger); padding: var(--space-3) var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-4); }

.family-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-xl); padding: var(--space-8); }
.family-header { display: flex; align-items: center; gap: var(--space-4); margin-bottom: var(--space-6); flex-wrap: wrap; }
.family-icon { font-size: 2.5rem; }
.family-header h2 { font-family: var(--font-display); font-size: var(--text-xl); font-weight: 600; }
.family-meta { display: flex; gap: var(--space-4); font-size: var(--text-xs); color: var(--fg-muted); margin-top: var(--space-1); align-items: center; }
.family-meta code { font-family: var(--font-mono); background: var(--accent-bg); padding: 1px 6px; border-radius: 4px; }
.family-actions { margin-left: auto; display: flex; gap: var(--space-2); flex-wrap: wrap; }

.member-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: var(--space-4); }
.member-card { display: flex; gap: var(--space-4); padding: var(--space-4); background: var(--surface-alt); border-radius: var(--radius-md); align-items: center; }
.member-avatar { width: 48px; height: 48px; border-radius: 50%; background: var(--accent); color: #fff; display: flex; align-items: center; justify-content: center; font-size: var(--text-lg); font-weight: 600; flex-shrink: 0; }
.member-info { flex: 1; min-width: 0; }
.member-relation { font-weight: 600; font-size: var(--text-sm); }
.member-share { font-size: var(--text-xs); color: var(--fg-muted); margin-top: var(--space-1); }
.member-emergency { margin-top: var(--space-1); }
.toggle-label { font-size: var(--text-xs); display: flex; align-items: center; gap: var(--space-1); cursor: pointer; }
.member-actions { flex-shrink: 0; }
.btn-icon { background: none; border: none; color: var(--fg-muted); cursor: pointer; font-size: var(--text-lg); padding: var(--space-1); }
.btn-icon:hover { color: var(--danger); }
.select-inline { font-size: var(--text-xs); padding: 2px 4px; border: 1px solid var(--border); border-radius: 4px; background: var(--surface); }

.empty-card { text-align: center; padding: var(--space-12); color: var(--fg-muted); }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: var(--surface); padding: var(--space-8); border-radius: var(--radius-lg); max-width: 480px; width: 100%; max-height: 80vh; overflow-y: auto; }
.modal h3 { font-family: var(--font-display); margin-bottom: var(--space-4); }
.invite-code-display { font-family: var(--font-mono); font-size: var(--text-2xl); text-align: center; padding: var(--space-4); background: var(--accent-bg); border-radius: var(--radius-md); margin: var(--space-4) 0; letter-spacing: 0.1em; }
.hint { font-size: var(--text-xs); color: var(--fg-muted); text-align: center; margin: var(--space-2) 0; }
.form-row { display: flex; gap: var(--space-2); align-items: center; margin-bottom: var(--space-3); flex-wrap: wrap; }
.input { flex: 1; padding: var(--space-2) var(--space-3); border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--text-sm); }
.select { padding: var(--space-2); border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--text-sm); }
.search-results { max-height: 200px; overflow-y: auto; margin: var(--space-3) 0; }
.search-item { display: flex; justify-content: space-between; align-items: center; padding: var(--space-2) var(--space-3); border-bottom: 1px solid var(--border); cursor: pointer; }
.search-item:hover { background: var(--surface-alt); }

/* Buttons */
.btn { padding: var(--space-2) var(--space-5); border-radius: var(--radius-sm); font-size: var(--text-sm); font-weight: 500; cursor: pointer; border: none; }
.btn-primary { background: var(--accent); color: #fff; }
.btn-secondary { background: var(--surface-alt); border: 1px solid var(--border); margin-top: var(--space-4); }
.btn-outline { background: transparent; border: 1px solid var(--accent); color: var(--accent); }
.btn-danger-text { background: transparent; border: none; color: var(--danger); cursor: pointer; font-size: var(--text-xs); }
.btn-danger-text:disabled { color: var(--fg-muted); cursor: not-allowed; }
.btn-link { background: none; border: none; color: var(--accent); cursor: pointer; font-size: var(--text-xs); padding: 0; }
.btn-sm { padding: var(--space-1) var(--space-3); font-size: var(--text-xs); }
.empty-buttons { display: flex; gap: var(--space-3); justify-content: center; margin-top: var(--space-4); }
.modal-buttons { display: flex; gap: var(--space-3); justify-content: flex-end; margin-top: var(--space-4); }
.input.full { width: 100%; box-sizing: border-box; }

</style>
