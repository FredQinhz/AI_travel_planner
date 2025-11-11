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
            <component :is="sidebarToggleIcon" />
          </el-icon>
        </el-button>
      </div>
    </aside>
    
    <!-- 主内容区 -->
    <main 
      class="main-content" 
      :class="{ 'sidebar-collapsed': isSidebarCollapsed }"
      :style="{ '--sidebar-width': isSidebarCollapsed ? '64px' : '240px' }"
    >
      <!-- 顶部导航栏 - 在行程详情页面隐藏 -->
      <header v-if="!isTripDetailPage" class="top-header">
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
          <div class="user-info-wrapper">
            <el-icon class="user-icon"><User /></el-icon>
            <span class="user-info">欢迎，{{ userEmail }}</span>
          </div>
          <el-button 
            type="danger" 
            @click="handleLogout" 
            size="small"
            :icon="SwitchButton"
          >
            退出登录
          </el-button>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <div class="content-area" :class="{ 'no-top-header': isTripDetailPage, 'map-page': isMapPage }">
        <slot></slot>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { 
  Location, 
  Menu, 
  ArrowRight, 
  ArrowLeft,
  Compass,
  Calendar,
  MapLocation,
  User,
  SwitchButton
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

// 判断是否是行程详情页面
const isTripDetailPage = computed(() => {
  return route.path.startsWith('/trips/') && route.params.id
})

// 判断是否是地图页面
const isMapPage = computed(() => {
  return route.path === '/map'
})

// 菜单项配置
const menuItems = [
  { path: '/dashboard', name: '我的行程', icon: Calendar },
  { path: '/trips/create', name: '创建行程', icon: Compass },
  { path: '/map', name: '地图', icon: MapLocation }
]

// 侧边栏切换图标
const sidebarToggleIcon = computed(() => {
  return isSidebarCollapsed.value ? ArrowRight : ArrowLeft
})

// 方法
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
  localStorage.setItem('sidebarCollapsed', isSidebarCollapsed.value.toString())
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

// 更新 CSS 变量
const updateSidebarWidth = () => {
  document.documentElement.style.setProperty(
    '--sidebar-width',
    isSidebarCollapsed.value ? '64px' : '240px'
  )
}

// 监听侧边栏状态变化
watch(isSidebarCollapsed, () => {
  updateSidebarWidth()
})

// 初始化
onMounted(() => {
  // 从localStorage加载用户偏好
  const savedSidebarState = localStorage.getItem('sidebarCollapsed')
  
  if (savedSidebarState === 'true') {
    isSidebarCollapsed.value = true
  }
  
  // 初始化 CSS 变量
  updateSidebarWidth()
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
  background: linear-gradient(180deg, #f0f7ff 0%, #e6f2ff 100%); /* 浅蓝色渐变背景 */
  border-right: 1px solid #d0e7ff; /* 浅蓝色边框 */
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 1000;
  box-shadow: 2px 0 8px rgba(64, 158, 255, 0.1); /* 添加浅蓝色阴影 */
}

.sidebar-collapsed {
  width: 64px;
}

.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid #d0e7ff; /* 浅蓝色边框 */
  background: rgba(255, 255, 255, 0.5); /* 半透明白色背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
}

.app-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #409eff; /* 主题蓝色 */
  margin: 0;
}

.app-logo .el-icon {
  font-size: 24px;
  color: #409eff;
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
  color: #606266; /* 深灰色文字 */
  border-radius: 8px;
  margin: 0 8px;
  transition: all 0.3s ease;
  position: relative;
}

.nav-link:hover {
  background: rgba(64, 158, 255, 0.1); /* 浅蓝色背景 */
  color: #409eff;
  transform: translateX(4px); /* 轻微右移效果 */
}

.nav-link.active {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); /* 蓝色渐变背景 */
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3); /* 蓝色阴影 */
}

.nav-link.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: #ffffff;
  border-radius: 0 3px 3px 0;
}

.nav-icon {
  font-size: 20px;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.nav-link:hover .nav-icon {
  transform: scale(1.1); /* 悬停时图标放大 */
}

.nav-link.active .nav-icon {
  color: #ffffff;
}

.nav-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #d0e7ff; /* 浅蓝色边框 */
  background: rgba(255, 255, 255, 0.3); /* 半透明白色背景 */
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.theme-toggle,
.sidebar-toggle {
  width: 100%;
  justify-content: flex-start;
  padding: 8px 12px;
  color: #606266;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.sidebar-toggle:hover {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
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
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f2ff 100%); /* 浅蓝色渐变背景 */
  border-bottom: 1px solid #d0e7ff; /* 浅蓝色边框 */
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 999;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.08); /* 浅蓝色阴影 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
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
  color: #303133; /* 深灰色 */
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.6); /* 半透明白色背景 */
  border-radius: 20px;
  transition: all 0.3s ease;
}

.user-info-wrapper:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateY(-1px);
}

.user-icon {
  font-size: 18px;
  color: #409eff;
}

.user-info {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.content-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* 当隐藏 top-header 时，content-area 可以占据整个高度 */
.content-area.no-top-header {
  padding-top: 0;
  padding-bottom: 0;
  overflow-y: visible; /* 让子页面自己控制滚动 */
}

/* 地图页面样式 - 移除所有 padding，让地图占据整个空间 */
.content-area.map-page {
  padding: 0;
  overflow: hidden; /* 让地图页面自己控制滚动 */
  height: calc(100vh - 64px); /* 减去顶部栏高度 */
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