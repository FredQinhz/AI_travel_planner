<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
      <div class="sidebar-header">
        <h2 class="app-logo">
          <el-icon><Location /></el-icon>
          <span v-if="!isSidebarCollapsed">AI Travel</span>
        </h2>
      </div>
      
      <nav class="sidebar-nav">
        <ul class="nav-menu">
          <li v-for="item in menuItems" :key="item.path" class="nav-item">
            <router-link 
              :to="item.path" 
              class="nav-link"
              :class="{ active: $route.path === item.path }"
            >
              <el-icon class="nav-icon">
                <component :is="item.icon" />
              </el-icon>
              <span v-if="!isSidebarCollapsed" class="nav-text">{{ item.name }}</span>
            </router-link>
          </li>
        </ul>
      </nav>
      
      <div class="sidebar-footer">
        <el-button 
          type="text" 
          class="sidebar-toggle" 
          @click="toggleSidebar"
          :title="isSidebarCollapsed ? '展开侧边栏' : '收起侧边栏'"
        >
          <el-icon>
            <component :is="isSidebarCollapsed ? 'ArrowRight' : 'ArrowLeft'" />
          </el-icon>
        </el-button>
      </div>
    </aside>
    
    <!-- 主内容区 -->
    <main class="main-content" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
      <!-- 顶部导航栏 -->
      <header class="top-header">
        <div class="header-left">
          <el-button 
            type="text" 
            class="menu-toggle" 
            @click="toggleSidebar"
            :title="isSidebarCollapsed ? '展开侧边栏' : '收起侧边栏'"
          >
            <el-icon><Menu /></el-icon>
          </el-button>
          <h1 class="page-title">{{ currentPageTitle }}</h1>
        </div>
        
        <div class="header-right">
          <span class="user-info">欢迎，{{ userEmail }}</span>
          <el-button type="danger" @click="handleLogout" size="small">退出登录</el-button>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <div class="content-area">
        <slot></slot>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { 
  Location, 
  Menu, 
  ArrowRight, 
  ArrowLeft,
  Compass,
  Calendar,
  MapLocation
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式状态
const isSidebarCollapsed = ref(false)

// 计算属性
const userEmail = computed(() => authStore.email || '用户')
// 页面标题映射
const getPageTitle = (route: any) => {
  const titles: Record<string, string> = {
    '/dashboard': '我的行程',
    '/trips/create': '创建行程',
    '/trips': '行程详情',
    '/map': '地图'
  }
  return titles[route.path] || 'AI Travel Planner'
}

const currentPageTitle = computed(() => getPageTitle(route))

// 菜单项配置
const menuItems = [
  { path: '/dashboard', name: '我的行程', icon: Calendar },
  { path: '/trips/create', name: '创建行程', icon: Compass },
  { path: '/map', name: '地图', icon: MapLocation }
]

// 方法
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
  localStorage.setItem('sidebarCollapsed', isSidebarCollapsed.value.toString())
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

// 初始化
onMounted(() => {
  // 从localStorage加载用户偏好
  const savedSidebarState = localStorage.getItem('sidebarCollapsed')
  
  if (savedSidebarState === 'true') {
    isSidebarCollapsed.value = true
  }
})
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background-color: var(--el-bg-color);
  transition: all 0.3s ease;
}

/* 侧边栏样式 */
.sidebar {
  width: 240px;
  background-color: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 1000;
}

.sidebar-collapsed {
  width: 64px;
}

.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid var(--el-border-color);
}

.app-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin: 0;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 0;
}

.nav-menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin: 4px 0;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  text-decoration: none;
  color: var(--el-text-color-regular);
  border-radius: 6px;
  margin: 0 8px;
  transition: all 0.3s ease;
}

.nav-link:hover {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.nav-link.active {
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  font-weight: 500;
}

.nav-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.nav-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--el-border-color);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.theme-toggle,
.sidebar-toggle {
  width: 100%;
  justify-content: flex-start;
  padding: 8px 12px;
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  margin-left: 240px;
  transition: margin-left 0.3s ease;
  display: flex;
  flex-direction: column;
}

.main-content.sidebar-collapsed {
  margin-left: 64px;
}

.top-header {
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color);
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 999;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.menu-toggle {
  display: none;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.content-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
  }
  
  .sidebar.sidebar-collapsed {
    transform: translateX(0);
    width: 240px;
  }
  
  .main-content {
    margin-left: 0;
  }
  
  .menu-toggle {
    display: block;
  }
  
  .content-area {
    padding: 16px;
  }
}

/* 深色主题变量 */
.app-layout.dark-theme {
  --el-bg-color: #1a1a1a;
  --el-text-color-primary: #e5e5e5;
  --el-text-color-regular: #b3b3b3;
  --el-border-color: #404040;
}
</style>