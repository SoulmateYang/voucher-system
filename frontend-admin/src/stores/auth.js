import { reactive, computed } from 'vue'

const state = reactive({
  token: localStorage.getItem('admin_token') || '',
  user: JSON.parse(localStorage.getItem('admin_user') || 'null'),
})

export function useAuthStore() {
  const isAuthenticated = computed(() => !!state.token)

  function setToken(token) {
    state.token = token
    localStorage.setItem('admin_token', token)
  }

  function setUser(user) {
    state.user = user
    localStorage.setItem('admin_user', JSON.stringify(user))
  }

  function logout() {
    state.token = ''
    state.user = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  return {
    state,
    isAuthenticated,
    setToken,
    setUser,
    logout,
  }
}
