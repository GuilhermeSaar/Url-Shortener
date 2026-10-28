<script setup>
import { useRoute, useRouter } from 'vue-router'
import { BarChart3, Link2, LogIn, LogOut, Plus } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

async function signOut() {
  await auth.signOut()
  router.replace({ name: 'home' })
}
</script>

<template>
  <header class="app-header">
    <RouterLink :to="{ name: 'home' }" class="widget-brand" aria-label="GS. — Página inicial">
      <span class="widget-brand-icon"><Link2 :size="19" :stroke-width="2" /></span>
      <span>GS<span class="brand-dot">.</span></span>
    </RouterLink>
    <nav class="workspace-account" aria-label="Navegação principal">
      <RouterLink v-if="route.name !== 'home'" :to="{ name: 'home' }" class="account-button"><Plus :size="14" /> Encurtar link</RouterLink>
      <RouterLink v-if="route.name === 'home'" :to="{ name: 'stats' }" class="account-button"><BarChart3 :size="14" /> Métricas</RouterLink>
      <template v-if="auth.isAuthenticated">
        <span class="workspace-label"><span class="is-authenticated" /> Sessão ativa</span>
        <button type="button" class="account-button" @click="signOut"><LogOut :size="14" /> Sair</button>
      </template>
      <RouterLink v-else-if="route.name !== 'login'" :to="{ name: 'login' }" class="account-button"><LogIn :size="14" /> Entrar</RouterLink>
    </nav>
  </header>
</template>
