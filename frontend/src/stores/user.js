import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')
  const employeeId = ref(localStorage.getItem('employeeId') || null)
  const name = ref(localStorage.getItem('name') || '')
  const avatar = ref(localStorage.getItem('avatar') || '')
  const isActive = ref(localStorage.getItem('isActive') || null)
  const isLoggedIn = ref(!!token.value)

  const login = (loginToken, loginUsername, loginRole, empId, empName, empAvatar, active) => {
    token.value = loginToken
    username.value = loginUsername
    role.value = loginRole
    employeeId.value = empId
    name.value = empName
    avatar.value = empAvatar
    isActive.value = active
    isLoggedIn.value = true
    
    localStorage.setItem('token', loginToken)
    localStorage.setItem('username', loginUsername)
    localStorage.setItem('role', loginRole)
    if (empId) localStorage.setItem('employeeId', empId)
    if (empName) localStorage.setItem('name', empName)
    if (empAvatar) localStorage.setItem('avatar', empAvatar)
    if (active !== null) localStorage.setItem('isActive', active)
  }

  const updateAvatar = (newAvatar) => {
    avatar.value = newAvatar
    localStorage.setItem('avatar', newAvatar)
  }

  const logout = () => {
    token.value = ''
    username.value = ''
    role.value = ''
    employeeId.value = null
    name.value = ''
    avatar.value = ''
    isActive.value = null
    isLoggedIn.value = false
    
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    localStorage.removeItem('employeeId')
    localStorage.removeItem('name')
    localStorage.removeItem('avatar')
    localStorage.removeItem('isActive')
  }

  const getToken = () => {
    return token.value
  }

  const isAdmin = () => {
    return role.value === 'admin'
  }

  const isEmployee = () => {
    return role.value === 'employee'
  }

  return {
    token,
    isLoggedIn,
    username,
    role,
    employeeId,
    name,
    avatar,
    isActive,
    login,
    updateAvatar,
    logout,
    getToken,
    isAdmin,
    isEmployee
  }
})
