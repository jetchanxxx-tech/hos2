<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi } from '@/api'

const user = ref<any>({})
const saving = ref(false)
const msg = ref('')

onMounted(async () => {
  try {
    const res: any = await userApi.me()
    user.value = res.data || {}
  } catch (e) { console.error(e) }
})

async function handleSave() {
  saving.value = true
  msg.value = ''
  try {
    await userApi.updateProfile({
      nameMasked: user.value.nameMasked,
      birthDate: user.value.birthDate || null,
      gender: user.value.gender,
      avatarUrl: user.value.avatarUrl || null,
    })
    msg.value = '保存成功'
  } catch (e: any) { msg.value = e?.response?.data?.message || '保存失败' }
  finally { saving.value = false }
}
</script>

<template>
  <div class="page">
    <h1 class="title">个人资料</h1>
    <div v-if="msg" :class="msg.includes('成功') ? 'msg-ok' : 'msg-err'">{{ msg }}</div>
    <div class="form">
      <label>姓名</label>
      <input v-model="user.nameMasked" class="input" placeholder="姓名（支持脱敏）" />
      <label>性别</label>
      <select v-model="user.gender" class="select">
        <option :value="0">女</option>
        <option :value="1">男</option>
      </select>
      <label>出生日期</label>
      <input type="date" v-model="user.birthDate" class="input" />
      <label>头像 URL</label>
      <input v-model="user.avatarUrl" class="input" placeholder="https://..." />
      <label>角色</label>
      <input :value="user.role" class="input" disabled />
      <label>注册时间</label>
      <input :value="user.createdAt" class="input" disabled />
      <button class="btn btn-primary" @click="handleSave" :disabled="saving">
        {{ saving ? '保存中...' : '保存' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 480px; margin: 0 auto; padding: var(--space-10) var(--space-8); }
.title { font-family: var(--font-display); font-size: var(--text-2xl); margin-bottom: var(--space-6); }
.form { display: flex; flex-direction: column; gap: var(--space-3); }
.input, .select { padding: var(--space-2) var(--space-3); border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--text-sm); }
.input:disabled { background: var(--surface-alt); color: var(--fg-muted); }
.msg-ok { background: var(--success-bg); color: var(--success); padding: var(--space-2) var(--space-3); border-radius: var(--radius-sm); }
.msg-err { background: var(--danger-bg); color: var(--danger); padding: var(--space-2) var(--space-3); border-radius: var(--radius-sm); }
.btn { padding: var(--space-3); border-radius: var(--radius-sm); font-size: var(--text-sm); font-weight: 500; cursor: pointer; border: none; }
.btn-primary { background: var(--accent); color: #fff; }
.btn-primary:disabled { opacity: 0.6; }
</style>
