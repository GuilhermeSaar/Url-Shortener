<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { KeyRound, LoaderCircle, LogIn, UserPlus } from 'lucide-vue-next'
import { errorMessage } from '../services/api.js'
import { useAuthStore } from '../stores/auth.js'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const emailInput = ref(null)
const mode = ref('login')
const email = ref('')
const password = ref('')
const error = ref('')
const notice = ref('')
const loading = ref(false)

function isLogin() {
  return mode.value === 'login'
}

function switchMode() {
  mode.value = isLogin() ? 'register' : 'login'
  error.value = ''
  notice.value = ''
}

function authenticationError(cause) {
  if (cause?.response?.status === 401) return 'E-mail ou senha inválidos.'
  return errorMessage(cause)
}

async function submit() {
  if (loading.value) return
  error.value = ''
  notice.value = ''
  loading.value = true
  try {
    if (isLogin()) {
      await auth.signIn(email.value.trim(), password.value)
      password.value = ''
      await router.replace({ name: 'stats', query: route.query })
      return
    }
    await auth.signUp(email.value.trim(), password.value)
    mode.value = 'login'
    password.value = ''
    notice.value = 'Cadastro concluído. Entre para consultar as estatísticas.'
    await nextTick()
    emailInput.value?.focus()
  } catch (cause) {
    error.value = authenticationError(cause)
  } finally {
    loading.value = false
  }
}

onMounted(() => emailInput.value?.focus())
</script>

<template>
  <div class="workspace workspace-auth">
    <section class="auth-card" aria-labelledby="auth-title">
    <form class="auth-form" :aria-busy="loading" @submit.prevent="submit">
      <div class="auth-heading">
        <div><span class="auth-eyebrow"><KeyRound :size="13" /> SUAS MÉTRICAS</span><h1 id="auth-title">{{ isLogin() ? 'Que bom ver você' : 'Vamos começar' }}<span class="brand-dot">.</span></h1></div>
      </div>

      <p class="auth-description">{{ isLogin() ? 'Entre para acompanhar os cliques dos seus links.' : 'Crie uma conta para acessar as métricas dos links.' }}</p>
      <label class="auth-field" for="auth-email">E-mail<input id="auth-email" ref="emailInput" v-model="email" type="email" autocomplete="email" inputmode="email" required :readonly="loading" /></label>
      <label class="auth-field" for="auth-password">Senha<input id="auth-password" v-model="password" type="password" :autocomplete="isLogin() ? 'current-password' : 'new-password'" :minlength="isLogin() ? undefined : 8" required :readonly="loading" /></label>
      <p v-if="error" class="auth-error" role="alert">{{ error }}</p>
      <p v-else-if="notice" class="auth-notice" role="status">{{ notice }}</p>

      <button type="submit" class="auth-submit" :disabled="loading"><LoaderCircle v-if="loading" :size="16" class="animate-spin" /><LogIn v-else-if="isLogin()" :size="16" /><UserPlus v-else :size="16" />{{ isLogin() ? 'Entrar' : 'Criar conta' }}</button>
      <p class="auth-switch">{{ isLogin() ? 'Ainda não tem uma conta?' : 'Já possui uma conta?' }} <button type="button" :disabled="loading" @click="switchMode">{{ isLogin() ? 'Cadastre-se' : 'Entrar' }}</button></p>
    </form>
    </section>
  </div>
</template>
