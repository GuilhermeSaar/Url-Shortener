import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getSession, login, logout, registerUser } from '../services/api.js'

export const useAuthStore = defineStore('auth', () => {
  const authenticated = ref(false)
  const sessionChecked = ref(false)
  const isAuthenticated = computed(() => authenticated.value)

  async function signIn(email, password) {
    await login(email, password)
    authenticated.value = true
    sessionChecked.value = true
  }

  async function signUp(email, password) {
    await registerUser(email, password)
  }

  async function restoreSession() {
    if (sessionChecked.value) return isAuthenticated.value

    try {
      await getSession()
      authenticated.value = true
    } catch (error) {
      if (error?.response?.status !== 401) throw error
      authenticated.value = false
    } finally {
      sessionChecked.value = true
    }

    return isAuthenticated.value
  }

  async function signOut() {
    try {
      await logout()
    } finally {
      authenticated.value = false
      sessionChecked.value = true
    }
  }

  return { isAuthenticated, restoreSession, signIn, signOut, signUp }
})
