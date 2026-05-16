<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { chatApi } from '@/api'
import StatusPill from '@/components/StatusPill.vue'

const sessions = ref<any[]>([])
const messages = ref<any[]>([])
const activeSession = ref<number | null>(null)
const inputText = ref('')
const loading = ref(false)
const sending = ref(false)

onMounted(async () => {
  try {
    const res: any = await chatApi.mySessions()
    sessions.value = res.data?.records || res.data || []
  } catch (e) { console.error(e) }
})

async function selectSession(id: number) {
  activeSession.value = id
  try {
    const res: any = await chatApi.getMessages(id)
    messages.value = res.data || []
    await nextTick()
    scrollToBottom()
  } catch (e) { console.error(e) }
}

async function send() {
  if (!inputText.value.trim() || !activeSession.value) return
  sending.value = true
  try {
    await chatApi.sendMessage(activeSession.value, {
      senderType: 'BUTLER', msgType: 'TEXT', senderName: '管家', content: inputText.value,
    })
    const res: any = await chatApi.getMessages(activeSession.value)
    messages.value = res.data || []
    inputText.value = ''
    await nextTick()
    scrollToBottom()
  } catch (e) { console.error(e) }
  finally { sending.value = false }
}

function scrollToBottom() {
  const el = document.querySelector('.msg-list')
  if (el) el.scrollTop = el.scrollHeight
}

const intentLabels: Record<string, string> = { MEDICAL: '医疗咨询', BENEFIT: '权益问题', COMPLAINT: '投诉', APPOINTMENT: '预约', GENERAL: '通用' }
const sentimentLabels: Record<string, string> = { POSITIVE: '😊 积极', NEUTRAL: '😐 中性', ANXIOUS: '😰 焦虑', ANGRY: '😡 愤怒' }

async function startNewSession() {
  try {
    const res: any = await chatApi.startSession('MINIPROGRAM')
    const s = res.data
    sessions.value.unshift(s)
    if (s.id) selectSession(s.id)
  } catch(e) { console.error(e) }
}
</script>

<template>
  <div class="page">
    <div class="section-header">
      <div class="section-label">惠福灵犀</div>
      <h1 class="section-title">智能客服工作台</h1>
      <p class="section-desc">AI+HCRM，智能问答+意图识别+紧急预警。</p>
    </div>

    <div class="chat-layout">
      <div class="session-list">
        <div class="session-list-header">
          <h3>会话列表</h3>
          <button class="btn-new-session" @click="startNewSession">+ 新建</button>
        </div>
        <div v-for="s in sessions" :key="s.id" class="session-item"
          :class="{ active: activeSession === s.id, urgent: s.escalationLevel === 'URGENT' }" @click="selectSession(s.id)">
          <div class="session-name">
            {{ s.sessionNo?.substring(s.sessionNo.length-6) || '#'+s.id }}
            <span class="chan-tag">{{ s.channel }}</span>
          </div>
          <div class="session-meta">
            <span v-if="s.intentType" class="tag">{{ intentLabels[s.intentType] || s.intentType }}</span>
            <span v-if="s.sentimentLabel" class="tag sentiment">{{ sentimentLabels[s.sentimentLabel] || s.sentimentLabel }}</span>
            <StatusPill :status="s.escalationLevel === 'URGENT' ? 'blocked' : s.status === 'ACTIVE' ? 'active' : 'completed'">
              {{ s.escalationLevel === 'URGENT' ? '⚠' : s.status === 'ACTIVE' ? '●' : '✓' }}
            </StatusPill>
          </div>
        </div>
        <div v-if="sessions.length === 0" class="empty">暂无会话，点击"+ 新建"创建</div>
      </div>

      <div class="chat-main">
        <div v-if="!activeSession" class="chat-placeholder">选择左侧会话开始沟通</div>
        <template v-else>
          <div class="msg-list">
            <div v-for="msg in messages" :key="msg.id" class="msg" :class="msg.senderType.toLowerCase()">
              <div class="msg-sender">{{ msg.senderName || msg.senderType }}</div>
              <div class="msg-content">{{ msg.content }}</div>
              <div v-if="msg.triggerAlert" class="msg-alert">⚠ 触发预警: {{ msg.alertKeyword }}</div>
            </div>
          </div>
          <div class="msg-input">
            <input v-model="inputText" @keyup.enter="send" placeholder="输入消息..." />
            <button @click="send" :disabled="sending">{{ sending ? '发送中' : '发送' }}</button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 1280px; margin: 0 auto; padding: var(--space-10) var(--space-8); }
.section-header { margin-bottom: var(--space-8); }
.section-label { font-family: var(--font-mono); font-size: var(--text-xs); text-transform: uppercase; letter-spacing: 0.08em; color: var(--fg-muted); margin-bottom: var(--space-2); }
.section-title { font-family: var(--font-display); font-size: var(--text-3xl); font-weight: 700; letter-spacing: -0.02em; }
.section-desc { font-size: var(--text-md); color: var(--fg-secondary); margin-top: var(--space-2); }
.chat-layout { display: grid; grid-template-columns: 280px 1fr; gap: var(--space-6); height: calc(100vh - 200px); min-height: 500px; }
.session-list { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: var(--space-4); overflow-y: auto; }
.session-list { display: flex; flex-direction: column; }
.session-list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-3); }
.session-list-header h3 { font-size: var(--text-sm); font-weight: 600; color: var(--fg); }
.btn-new-session { padding: 2px 10px; background: var(--accent); color: #fff; border: none; border-radius: 100px; font-size: var(--text-xs); cursor: pointer; }
.session-item { padding: var(--space-3); border-radius: var(--radius-sm); cursor: pointer; border-bottom: 1px solid var(--border-light); }
.session-item:hover, .session-item.active { background: var(--accent-bg); }
.session-item.urgent { background: var(--danger-bg); border-left: 3px solid var(--danger); }
.session-name { font-weight: 500; font-size: var(--text-sm); display: flex; align-items: center; gap: var(--space-2); }
.chan-tag { font-size: 9px; background: var(--surface-alt); color: var(--fg-muted); padding: 1px 4px; border-radius: 3px; }
.session-meta { margin-top: var(--space-1); display: flex; gap: 4px; flex-wrap: wrap; align-items: center; }
.tag { font-size: 10px; padding: 1px 6px; border-radius: 4px; background: var(--info-bg); color: var(--info); }
.tag.sentiment { background: var(--accent-bg); color: var(--accent); }
.chat-main { display: flex; flex-direction: column; background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-lg); overflow: hidden; }
.chat-placeholder { display: flex; align-items: center; justify-content: center; height: 100%; color: var(--fg-muted); }
.msg-list { flex: 1; overflow-y: auto; padding: var(--space-4); display: flex; flex-direction: column; gap: var(--space-3); }
.msg { max-width: 70%; padding: var(--space-2) var(--space-4); border-radius: var(--radius-md); }
.msg.user { align-self: flex-end; background: var(--accent-bg); }
.msg.ai { align-self: flex-start; background: var(--surface-alt); }
.msg.butler { align-self: flex-start; background: var(--coral-bg); }
.msg.system { align-self: center; font-size: var(--text-xs); color: var(--fg-muted); }
.msg-sender { font-size: var(--text-xs); color: var(--fg-muted); font-weight: 500; }
.msg-content { font-size: var(--text-sm); margin-top: 2px; }
.msg-alert { margin-top: var(--space-1); font-size: var(--text-xs); color: var(--danger); font-weight: 600; }
.msg-input { padding: var(--space-3); border-top: 1px solid var(--border); display: flex; gap: var(--space-2); }
.msg-input input { flex: 1; padding: var(--space-2) var(--space-3); border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--text-sm); }
.msg-input button { padding: var(--space-2) var(--space-5); background: var(--accent); color: #fff; border: none; border-radius: var(--radius-sm); cursor: pointer; font-weight: 500; }
.msg-input button:hover { background: var(--accent-hover); }
.empty { text-align: center; color: var(--fg-muted); padding: var(--space-8); }
</style>
