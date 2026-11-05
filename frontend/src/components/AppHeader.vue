<script setup>
import { useRoute, useRouter } from 'vue-router'
import { Link2, LogIn, LogOut } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

async function signOut() {
  try {
    await auth.signOut()
  } finally {
    router.replace({ name: 'home' })
  }
}
</script>

<template>
  <header class="app-header">
    <RouterLink :to="{ name: 'home' }" class="widget-brand" aria-label="GS. — Página inicial">
      <span class="widget-brand-icon"><Link2 :size="19" :stroke-width="2" /></span>
      <span>GS<span class="brand-dot">.</span></span>
    </RouterLink>
    <nav class="workspace-account" aria-label="Navegação principal">
      <RouterLink v-if="auth.isAuthenticated || route.name === 'home'" :to="{ name: 'links' }" class="account-button" :class="{ 'account-button-active': route.name === 'links' }"><Link2 :size="14" /> Meus links</RouterLink>
      <template v-if="auth.isAuthenticated">
        <span class="workspace-label"><span class="is-authenticated" /> Sessão ativa</span>
        <button type="button" class="account-button" @click="signOut"><LogOut :size="14" /> Sair</button>
      </template>
      <RouterLink v-else-if="route.name !== 'login'" :to="{ name: 'login' }" class="account-button"><LogIn :size="14" /> Entrar</RouterLink>
    </nav>
  </header>
</template>
