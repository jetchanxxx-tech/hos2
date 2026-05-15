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

const intentLabels: Record<string, string> = { MEDICAL: '医疗咨询', BENEFIT: '权益问题', COMPLAINT: '投诉', GENERAL: '通用' }
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
        <h3>会话列表</h3>
        <div v-for="s in sessions" :key="s.id" class="session-item"
          :class="{ active: activeSession === s.id }" @click="selectSession(s.id)">
          <div class="session-name">用户 {{ s.userId }}</div>
          <div class="session-meta">
            <StatusPill :status="s.escalationLevel === 'URGENT' ? 'blocked' : s.status === 'ACTIVE' ? 'active' : 'completed'">
              {{ s.escalationLevel === 'URGENT' ? '⚠ 紧急' : intentLabels[s.intentType] || s.status }}
            </StatusPill>
          </div>
        </div>
        <div v-if="sessions.length === 0" class="empty">暂无会话</div>
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
.session-list h3 { font-size: var(--text-sm); font-weight: 600; margin-bottom: var(--space-3); color: var(--fg); }
.session-item { padding: var(--space-3); border-radius: var(--radius-sm); cursor: pointer; border-bottom: 1px solid var(--border-light); }
.session-item:hover, .session-item.active { background: var(--accent-bg); }
.session-name { font-weight: 500; font-size: var(--text-sm); }
.session-meta { margin-top: var(--space-1); }
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
