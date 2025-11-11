<script setup lang="ts">
import { ref, onMounted, computed, nextTick, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useTripsStore } from '@/stores/trips'; 
import { ElCard, ElTabs, ElTabPane, ElDescriptions, ElDescriptionsItem, ElTag, ElButton, ElLoading } from 'element-plus';
import MapService, { type Location } from '@/services/MapService';

const route = useRoute();
const router = useRouter();
const tripsStore = useTripsStore();
const loading = ref(true);
const mapLoading = ref(false);

// 从路由参数中获取行程ID
const tripId = computed(() => route.params.id as string);
const activeDay = ref('1'); // 默认选中第一天

// 创建地图服务实例
const mapService = new MapService();

// 加载行程详情
const loadTripDetail = async () => {
  if (!tripId.value) return;
  
  loading.value = true;
  const trip = await tripsStore.fetchTrip(tripId.value);
  if (!trip) {
    // 如果行程不存在，返回仪表盘
    router.push('/dashboard');
  } else {
    // 行程加载完成后初始化地图
    await nextTick();
    initMap();
  }
  loading.value = false;
};

// 初始化地图
const initMap = async () => {
  if (!currentTrip.value || !currentTrip.value.dayPlans || currentTrip.value.dayPlans.length === 0) return;
  
  mapLoading.value = true;
  
  try {
    // 初始化地图
    await mapService.initMap('map-container', { 
      lng: 116.397428, 
      lat: 39.90923 
    }, 12);
    
    // 获取当前选中天的行程安排
    const currentDayPlan = currentTrip.value.dayPlans.find(plan => plan.day.toString() === activeDay.value);
    if (currentDayPlan && currentDayPlan.locations.length > 0) {
      // 在地图上标记所有位置
      currentDayPlan.locations.forEach((location, index) => {
        mapService.addMarker({
          name: location.name,
          lat: location.lat,
          lng: location.lng,
          type: location.type,
          description: location.description
        });
      });
      
      // 如果有多于一个位置，绘制路线
      if (currentDayPlan.locations.length > 1) {
        const route = {
          start: currentDayPlan.locations[0],
          end: currentDayPlan.locations[currentDayPlan.locations.length - 1],
          waypoints: currentDayPlan.locations.slice(1, -1)
        };
        mapService.drawRoute(route);
      }
    }
  } catch (error) {
    console.error('地图初始化失败:', error);
  } finally {
    mapLoading.value = false;
  }
};

// 返回仪表盘
const goBack = () => {
  router.push('/dashboard');
};

// 切换天数时重新加载地图
const handleDayChange = () => {
  // 清除之前的地图标记和路线
  mapService.clearMarkers();
  mapService.clearRoutes();
  
  // 重新初始化地图
  initMap();
};

// 组件挂载时加载行程详情
onMounted(() => {
  loadTripDetail();
});

// 组件卸载时销毁地图
onUnmounted(() => {
  mapService.destroy();
});

// 计算属性，用于获取当前行程
const currentTrip = computed(() => tripsStore.currentTrip);

// 计算行程天数
const tripDays = computed(() => {
  if (!currentTrip.value) return 0;
  const start = new Date(currentTrip.value.startDate);
  const end = new Date(currentTrip.value.endDate);
  return Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1;
});

// 获取位置类型对应的标签类型
const getLocationTagType = (type: string) => {
  const typeMap: Record<string, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    'attraction': 'primary',
    'restaurant': 'success',
    'cultural': 'warning',
    'shopping': 'info',
    'hotel': 'danger'
  };
  return typeMap[type] || 'info'; // 使用info作为默认类型
};

// 获取位置类型的中文名称
const getLocationTypeName = (type: string) => {
  const typeMap: Record<string, string> = {
    'attraction': '景点',
    'restaurant': '餐厅',
    'cultural': '文化场所',
    'shopping': '购物',
    'hotel': '酒店'
  };
  return typeMap[type] || type;
};
</script>

<template>
  <div class="trip-detail-page">
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="header-content">
        <el-button class="back-button" type="primary" @click="goBack">
          <i class="el-icon-arrow-left"></i>
          返回仪表盘
        </el-button>
        <h1 class="page-title">{{ currentTrip?.title || '行程详情' }}</h1>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="layout-container" v-if="currentTrip">
        <!-- 左侧内容区域 (1/3) -->
        <div class="left-panel">
          <!-- 行程基本信息卡片 -->
          <el-card class="trip-info-card" shadow="hover" v-loading="loading">
            <template #header>
              <div class="card-header">
                <h2 class="card-title">
                  <i class="el-icon-document"></i>
                  行程信息
                </h2>
              </div>
            </template>
            
            <div class="trip-info-content">
              <div class="info-item">
                <span class="info-label">行程标题:</span>
                <span class="info-value">{{ currentTrip.title }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">目的地:</span>
                <span class="info-value">{{ currentTrip.destination }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">旅行日期:</span>
                <span class="info-value">{{ currentTrip.startDate }} 至 {{ currentTrip.endDate }}（共{{ tripDays }}天）</span>
              </div>
              <div class="info-item">
                <span class="info-label">总预算:</span>
                <span class="info-value">¥{{ currentTrip.budgetTotal }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">同行人数:</span>
                <span class="info-value">{{ currentTrip.companionCount }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">旅行偏好:</span>
                <span class="info-value">
                  <el-tag v-for="(pref, index) in currentTrip.preferences" :key="index" type="info" size="small" style="margin-right: 8px; margin-bottom: 8px">{{ pref }}</el-tag>
                </span>
              </div>
            </div>
          </el-card>

          <!-- 每日行程安排卡片 -->
          <el-card class="day-plans-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <h2 class="card-title">
                  <i class="el-icon-date"></i>
                  每日行程安排
                </h2>
              </div>
            </template>
            
            <el-tabs v-model="activeDay" type="border-card" @tab-change="handleDayChange">
              <el-tab-pane 
                v-for="dayPlan in currentTrip.dayPlans" 
                :key="dayPlan.day" 
                :label="`第${dayPlan.day}天`" 
                :name="dayPlan.day.toString()"
              >
                <div v-if="dayPlan.locations.length > 0" class="day-plan-content">
                  <div 
                    v-for="(location, index) in dayPlan.locations" 
                    :key="index"
                    class="location-item"
                  >
                    <div class="location-header">
                      <h4 class="location-name">{{ location.name }}</h4>
                      <el-tag :type="getLocationTagType(location.type)" size="small">
                        {{ getLocationTypeName(location.type) }}
                      </el-tag>
                    </div>
                    <p class="location-description">{{ location.description }}</p>
                    <div class="location-meta">
                      <span class="coordinates">经纬度：{{ location.lat }}, {{ location.lng }}</span>
                    </div>
                  </div>
                </div>
                <div v-else class="no-plan">
                  <i class="el-icon-info"></i>
                  暂无安排
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </div>

        <!-- 右侧地图区域 (2/3) -->
        <div class="right-panel">
          <el-card class="map-card" shadow="hover">
            <template #header>
              <div class="map-header">
                <h2 class="card-title">
                  <i class="el-icon-map-location"></i>
                  行程地图
                </h2>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="initMap"
                  :loading="mapLoading"
                  class="refresh-button"
                >
                  <i class="el-icon-refresh"></i>
                  刷新地图
                </el-button>
              </div>
            </template>
            
            <div class="map-container-wrapper">
              <div id="map-container" class="map-container">
                <div v-if="mapLoading" class="map-loading">
                  <el-loading />
                  <span>地图加载中...</span>
                </div>
                <div v-else-if="!currentTrip.dayPlans || currentTrip.dayPlans.length === 0" class="map-empty">
                  <p>暂无行程安排</p>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div v-else-if="loading" class="loading-container">
        <el-loading text="加载中..." />
      </div>
    </main>
  </div>
</template>

<style scoped>
.trip-detail-page {
  height: 100vh;
  width: 100vw;
  margin: 0;
  padding: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Helvetica Neue', Arial, sans-serif;
  overflow-x: hidden;
  overflow-y: auto;
}

/* 页面头部样式 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100vw;
  margin: 0;
  position: relative;
  left: 0;
  right: 0;
}

.header-content {
  width: 100vw;
  margin: 0;
  padding: 0 15px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-sizing: border-box;
}

.page-title {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

/* 主要内容区域 */
.main-content {
  width: 100vw;
  height: calc(100vh - 70px);
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.layout-container {
  display: flex;
  gap: 0;
  height: 100%;
  width: 100vw;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

/* 左侧面板 (1/3) */
.left-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-right: 1px solid #e0e0e0;
  overflow-y: auto;
  height: 100%;
  box-sizing: border-box;
}

/* 右侧面板 (2/3) */
.right-panel {
  flex: 2;
  min-width: 0;
  padding: 15px;
  background: #ffffff;
  overflow-y: auto;
  height: 100%;
  box-sizing: border-box;
}

/* 卡片通用样式 */
.trip-info-card,
.day-plans-card,
.map-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  min-height: auto;
  height: auto;
  display: flex;
  flex-direction: column;
}

.trip-info-card :deep(.el-card__body),
.day-plans-card :deep(.el-card__body),
.map-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.day-plans-card :deep(.el-tabs) {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.day-plans-card :deep(.el-tabs__content) {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.day-plans-card :deep(.el-tab-pane) {
  height: 100%;
  min-height: 0;
}
.card-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title i {
  font-size: 1.1em;
}

/* 卡片头部样式 */
.trip-info-card :deep(.el-card__header),
.day-plans-card :deep(.el-card__header),
.map-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px 16px 0 0;
  padding: 20px 24px;
  border-bottom: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title i {
  font-size: 1.2em;
}

/* 行程信息内容 */
.trip-info-content {
  padding: 16px 20px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.info-label {
  font-weight: 600;
  color: #333;
  min-width: 90px;
  margin-right: 12px;
  font-size: 0.95rem;
  line-height: 1.4;
}

.info-value {
  color: #666;
  flex: 1;
  line-height: 1.4;
  font-size: 0.95rem;
}

.description-item {
  flex-direction: column;
  align-items: flex-start;
}

.description-item .info-value {
  margin-top: 8px;
  line-height: 1.6;
}

/* 地图头部样式 */
.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.refresh-button {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  transition: all 0.3s ease;
}

.refresh-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

/* 地图容器 */
.map-container-wrapper {
  position: relative;
  height: calc(100vh - 160px);
  border-radius: 12px;
  overflow: hidden;
  background: #f8f9fa;
}

.map-container {
  width: 100%;
  height: 100%;
}

.map-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  z-index: 10;
  color: #666;
  font-size: 1.1rem;
}

/* 每日行程安排 */
.day-plan-content {
  padding: 16px 0;
}

.location-item {
  padding: 20px;
  margin-bottom: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  border-left: 4px solid #667eea;
  transition: all 0.3s ease;
}

.location-item:hover {
  background: #eef2ff;
  transform: translateX(4px);
}

.location-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.location-name {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
}

.location-description {
  margin: 8px 0 12px 0;
  color: #666;
  line-height: 1.6;
  font-size: 0.95rem;
}

.location-meta {
  font-size: 0.9rem;
  color: #999;
}

.coordinates {
  font-family: 'Courier New', monospace;
}

.no-plan {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-style: italic;
  font-size: 1.1rem;
}

.no-plan i {
  font-size: 2rem;
  margin-bottom: 16px;
  display: block;
  color: #ccc;
}

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  font-size: 1.2rem;
  color: #666;
}

/* 响应式设计 */
/* 大屏幕优化 (1920px+) */
@media (min-width: 1920px) {
  .left-panel {
    padding: 30px;
  }
  
  .right-panel {
    padding: 30px;
  }
  
  .map-container-wrapper {
    height: calc(100vh - 200px);
  }
}

/* 中等屏幕 (1024px - 1919px) */
@media (max-width: 1919px) and (min-width: 1024px) {
  .left-panel {
    padding: 20px;
  }
  
  .right-panel {
    padding: 20px;
  }
  
  .map-container-wrapper {
    height: calc(100vh - 160px);
  }
}

/* 平板设备 (768px - 1023px) */
@media (max-width: 1023px) and (min-width: 768px) {
  .layout-container {
    flex-direction: column;
    min-height: auto;
  }
  
  .left-panel {
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
    padding: 20px;
  }
  
  .right-panel {
    padding: 20px;
  }
  
  .map-container-wrapper {
    height: 500px;
  }
}

/* 手机设备 (768px以下) */
@media (max-width: 767px) {
  .layout-container {
    flex-direction: column;
    min-height: auto;
  }
  
  .left-panel {
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
    padding: 15px;
  }
  
  .right-panel {
    padding: 15px;
  }
  
  .map-container-wrapper {
    height: 400px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .page-title {
    font-size: 1.6rem;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .info-label {
    margin-bottom: 8px;
    min-width: auto;
    font-size: 0.9rem;
  }
  
  .info-value {
    font-size: 0.9rem;
  }
}

/* 小屏手机 (480px以下) */
@media (max-width: 480px) {
  .left-panel {
    padding: 12px;
  }
  
  .right-panel {
    padding: 12px;
  }
  
  .map-container-wrapper {
    height: 350px;
  }
  
  .header-content {
    padding: 0 12px;
  }
  
  .page-header {
    padding: 15px 0;
  }
}
</style>