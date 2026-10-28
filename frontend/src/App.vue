<script setup>
import AppHeader from './components/AppHeader.vue'
</script>

<template>
  <a href="#main" class="skip-link">Pular para o conteúdo</a>
  <div class="app-shell home-shell">
    <div class="widget-home">
      <AppHeader />
      <main id="main" class="home-main" tabindex="-1"><RouterView /></main>
    </div>
  </div>
</template>
