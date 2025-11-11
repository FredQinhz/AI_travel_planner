<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'

const route = useRoute()

// 判断是否需要显示布局（登录/注册页面和行程详情页面不需要）
const showLayout = computed(() => {
  const noLayoutRoutes = ['/login', '/register']
  
  // 行程详情页面不使用布局（精确匹配 /trips/UUID 格式）
  const tripDetailPattern = /^\/trips\/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/
  if (tripDetailPattern.test(route.path)) {
    return false
  }
  
  return !noLayoutRoutes.includes(route.path)
})
</script>

<template>
  <div class="app-container">
    <AppLayout v-if="showLayout">
      <router-view />
    </AppLayout>
    <router-view v-else />
  </div>
</template>

<style>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.app-container {
  min-height: 100vh;
  width: 100%;
}
</style>
