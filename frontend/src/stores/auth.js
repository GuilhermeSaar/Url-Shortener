import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { clearAuthToken, getAuthToken, login, registerUser, setAuthToken } from '../services/api.js'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getAuthToken())
  const isAuthenticated = computed(() => Boolean(token.value))

  async function signIn(email, password) {
    const receivedToken = await login(email, password)
    token.value = receivedToken
    setAuthToken(receivedToken)
  }

  async function signUp(email, password) {
    await registerUser(email, password)
  }

  function signOut() {
    token.value = null
    clearAuthToken()
  }

  return { isAuthenticated, signIn, signOut, signUp }
})
