<template>
  <a-layout class="main-layout">
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :width="240"
      :collapsed-width="64"
      class="sidebar"
      theme="light"
    >
      <div class="sidebar-logo" @click="goHome">
        <div class="logo-icon" v-if="!collapsed">
          <ApartmentOutlined style="font-size: 24px" />
        </div>
        <div class="logo-icon" v-else>
          <ApartmentOutlined style="font-size: 20px" />
        </div>
        <span v-if="!collapsed" class="logo-text">员工管理系统</span>
      </div>

      <a-menu
        mode="inline"
        :selected-keys="selectedKeys"
        :open-keys="openKeys"
        @openChange="handleOpenChange"
        @click="handleMenuClick"
        class="sidebar-menu"
      >
        <a-sub-menu v-for="group in filteredMenus" :key="group.key">
          <template #icon>
            <component :is="group.icon" />
          </template>
          <template #title>{{ group.label }}</template>
          <a-menu-item v-for="item in group.children" :key="item.key">
            <span>{{ item.label }}</span>
          </a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-layout-sider>

    <a-layout class="content-layout">
      <a-layout-header class="layout-header">
        <div class="header-left">
          <span
            class="collapse-btn"
            @click="toggleCollapsed"
          >
            <MenuUnfoldOutlined v-if="collapsed" />
            <MenuFoldOutlined v-else />
          </span>
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item v-for="item in breadcrumbItems" :key="item">
              {{ item }}
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="header-right">
          <a-dropdown>
            <div class="user-info">
              <a-avatar :size="32" :src="getAvatarUrl(userStore.avatar)">
                {{ userStore.name ? userStore.name.charAt(0) : 'U' }}
              </a-avatar>
              <span class="username">{{ userStore.name || userStore.username }}</span>
              <DownOutlined style="font-size: 12px; color: #999" />
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item v-if="userStore.isEmployee()" key="changePassword" @click="handleChangePassword">
                  <KeyOutlined /> 修改密码
                </a-menu-item>
                <a-menu-divider v-if="userStore.isEmployee()" />
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content class="layout-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Modal, message } from 'ant-design-vue'
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  ApartmentOutlined,
  DownOutlined,
  KeyOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMenuByRole, findMenuGroupByKey, findMenuKeyByPath } from '@/config/menuConfig'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const collapsed = ref(false)
const STORAGE_KEY_OPEN_KEYS = 'menu_open_keys'
const STORAGE_KEY_COLLAPSED = 'menu_collapsed'

const filteredMenus = computed(() => {
  return getMenuByRole(userStore.role)
})

const selectedKeys = ref([])
const openKeys = ref([])

const loadPersistedState = () => {
  try {
    const savedOpenKeys = localStorage.getItem(STORAGE_KEY_OPEN_KEYS)
    if (savedOpenKeys) {
      openKeys.value = JSON.parse(savedOpenKeys)
    }

    const savedCollapsed = localStorage.getItem(STORAGE_KEY_COLLAPSED)
    if (savedCollapsed !== null) {
      collapsed.value = JSON.parse(savedCollapsed)
    }
  } catch (e) {
    // ignore parse errors
  }
}

const persistOpenKeys = () => {
  localStorage.setItem(STORAGE_KEY_OPEN_KEYS, JSON.stringify(openKeys.value))
}

const persistCollapsed = () => {
  localStorage.setItem(STORAGE_KEY_COLLAPSED, JSON.stringify(collapsed.value))
}

const updateSelectedKeys = () => {
  const key = findMenuKeyByPath(route.path)
  if (key) {
    selectedKeys.value = [key]

    const groupKey = findMenuGroupByKey(key)
    if (groupKey && !openKeys.value.includes(groupKey)) {
      openKeys.value = [...openKeys.value, groupKey]
      persistOpenKeys()
    }
  }
}

const breadcrumbItems = computed(() => {
  const items = []
  for (const group of filteredMenus.value) {
    for (const child of group.children) {
      if (child.path === route.path) {
        items.push(group.label)
        items.push(child.label)
        return items
      }
    }
  }
  return items
})

watch(() => route.path, () => {
  updateSelectedKeys()
}, { immediate: true })

watch(collapsed, () => {
  persistCollapsed()
})

const handleOpenChange = (keys) => {
  openKeys.value = keys
  persistOpenKeys()
}

const toggleCollapsed = () => {
  collapsed.value = !collapsed.value
}

const handleMenuClick = ({ key }) => {
  for (const group of filteredMenus.value) {
    for (const child of group.children) {
      if (child.key === key) {
        router.push(child.path)
        return
      }
    }
  }
}

const goHome = () => {
  if (userStore.isAdmin()) {
    router.push('/employee')
  } else {
    router.push('/profile')
  }
}

const handleChangePassword = () => {
  router.push('/profile')
}

const handleLogout = () => {
  Modal.confirm({
    title: '确认退出',
    content: '确定要退出登录吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      userStore.logout()
      message.success('已退出登录')
      router.push('/login')
    }
  })
}

const getAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}

onMounted(() => {
  loadPersistedState()
  updateSelectedKeys()
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.sidebar {
  background: #fff !important;
  border-right: 1px solid #f0f0f0;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: all 0.3s;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-menu {
  border-right: none !important;
  padding: 8px 0;
}

.sidebar-menu :deep(.ant-menu-submenu-title) {
  height: 44px;
  line-height: 44px;
  margin: 2px 0;
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}

.sidebar-menu :deep(.ant-menu-submenu-title:hover) {
  color: #667eea;
  background: #f5f7ff;
}

.sidebar-menu :deep(.ant-menu-item) {
  height: 40px;
  line-height: 40px;
  margin: 1px 0;
  font-size: 14px;
  color: #6b7280;
  padding-left: 52px !important;
}

.sidebar-menu :deep(.ant-menu-item:hover) {
  color: #667eea;
  background: #f5f7ff;
}

.sidebar-menu :deep(.ant-menu-item-selected) {
  color: #667eea !important;
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.05) 100%) !important;
  font-weight: 500;
}

.sidebar-menu :deep(.ant-menu-item-selected::after) {
  content: '';
  position: absolute;
  right: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 3px 0 0 3px;
}

.sidebar-menu :deep(.ant-menu-submenu-arrow) {
  color: #999;
}

.sidebar-menu :deep(.ant-menu-submenu-open > .ant-menu-submenu-title .ant-menu-submenu-arrow) {
  color: #667eea;
}

.content-layout {
  background: #f0f2f5;
}

.layout-header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  line-height: 56px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 18px;
  cursor: pointer;
  color: #6b7280;
  transition: color 0.3s;
  display: flex;
  align-items: center;
  padding: 4px;
}

.collapse-btn:hover {
  color: #667eea;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f7ff;
}

.username {
  color: #374151;
  font-size: 14px;
  font-weight: 500;
}

.layout-content {
  margin: 16px;
  min-height: calc(100vh - 56px - 32px);
}

:deep(.ant-layout-sider-children) {
  display: flex;
  flex-direction: column;
}

:deep(.ant-layout-sider-collapsed .logo-text) {
  display: none;
}

:deep(.ant-layout-sider-collapsed .sidebar-logo) {
  padding: 0;
  justify-content: center;
}
</style>
