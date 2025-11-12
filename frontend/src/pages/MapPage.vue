<template>
  <div class="map-page">
    <!-- 侧边栏 -->
    <aside class="map-sidebar">
      <!-- 返回按钮 - 在侧边栏内部左上角 -->
      <el-button 
        class="back-button" 
        type="primary" 
        @click="goBack" 
        :icon="ArrowLeft"
        size="small"
      >
        返回
      </el-button>
      
      <div class="sidebar-header">
        <h3>行程地图</h3>
      </div>

      <!-- 行程选择 -->
      <div class="sidebar-section">
        <div class="section-title">
          <el-icon><Calendar /></el-icon>
          <span>选择行程</span>
        </div>
        <el-select
          v-model="selectedTripId"
          placeholder="请选择行程"
          filterable
          clearable
          @change="handleTripChange"
          class="trip-selector"
        >
          <el-option
            v-for="trip in filteredTrips"
            :key="trip.id"
            :label="trip.title"
            :value="trip.id"
          >
            <div class="trip-option">
              <span class="trip-title">{{ trip.title }}</span>
              <span class="trip-destination">{{ trip.destination || '' }}</span>
            </div>
          </el-option>
        </el-select>
      </div>

      <!-- 天数选择 -->
      <div v-if="selectedTrip && selectedTrip.dayPlans && selectedTrip.dayPlans.length > 0" class="sidebar-section">
        <div class="section-title">
          <el-icon><Clock /></el-icon>
          <span>选择日期</span>
        </div>
        <el-radio-group v-model="selectedDay" @change="handleDayChange" class="day-selector">
          <el-radio
            v-for="dayPlan in selectedTrip.dayPlans"
            :key="dayPlan.day"
            :label="dayPlan.day"
            border
          >
            第{{ dayPlan.day }}天
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 地点列表 -->
      <div v-if="currentDayLocations.length > 0" class="sidebar-section locations-section">
        <div class="section-title">
          <el-icon><Location /></el-icon>
          <span>当日行程 ({{ currentDayLocations.length }})</span>
        </div>
        <div class="locations-list">
          <div
            v-for="(location, index) in currentDayLocations"
            :key="index"
            class="location-item"
            :class="{ active: selectedLocationIndex === index }"
            @click="handleLocationClick(index)"
          >
            <div class="location-order">{{ index + 1 }}</div>
            <div class="location-info">
              <div class="location-name">{{ location.name }}</div>
              <div class="location-type">{{ getLocationTypeName(location.type) }}</div>
            </div>
            <el-icon class="location-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 路线规划 -->
      <div v-if="currentDayLocations.length > 1" class="sidebar-section route-section">
        <div class="section-title">
          <el-icon><MapLocation /></el-icon>
          <span>路线规划</span>
        </div>
        <!-- 路线类型选择器已隐藏，默认使用驾车模式 -->
        <!--
        <el-radio-group v-model="routeType" class="route-type-selector">
          <el-radio-button label="driving">
            <el-icon><Location /></el-icon>
            <span>驾车</span>
          </el-radio-button>
          <el-radio-button label="transit">
            <el-icon><Location /></el-icon>
            <span>公交</span>
          </el-radio-button>
          <el-radio-button label="walking">
            <el-icon><Location /></el-icon>
            <span>步行</span>
          </el-radio-button>
        </el-radio-group>
        -->
        <div class="route-buttons-container">
          <el-button
            type="primary"
            :icon="MapLocation"
            @click="planRoute"
            :loading="routePlanning"
            class="plan-route-button"
          >
            规划当日路线
          </el-button>
          <el-button
            type="default"
            :icon="Delete"
            @click="clearRoute"
            class="clear-route-button"
          >
            清除路线
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!selectedTrip" class="empty-state">
        <el-empty description="请选择一个行程" :image-size="100" />
      </div>
    </aside>

    <!-- 地图主体 -->
    <main class="map-container">
      <div id="amap-container" class="amap-container"></div>
      <div v-if="mapLoading" class="map-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>地图加载中...</span>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useTripsStore } from '@/stores/trips';
import MapService, { type Location as MapLocationType, type RouteType } from '@/services/MapService';
import { ElMessage } from 'element-plus';
import {
  Calendar,
  Clock,
  Location,
  ArrowRight,
  ArrowLeft,
  MapLocation,
  Delete,
  Loading
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const tripsStore = useTripsStore();

// 状态
const selectedTripId = ref<string>('');
const selectedDay = ref<number>(1);
const selectedLocationIndex = ref<number>(0);
const routeType = ref<RouteType>('driving');
const routePlanning = ref(false);
const mapLoading = ref(false);

// MapService 实例
let mapService: MapService | null = null;
const locationMarkers = ref<any[]>([]);

// 计算属性
const trips = computed(() => tripsStore.trips || []);
const filteredTrips = computed(() => {
  return trips.value.filter(trip => trip && trip.id);
});

const selectedTrip = computed(() => {
  if (!selectedTripId.value) return null;
  return tripsStore.trips.find(trip => trip.id === selectedTripId.value) || null;
});

const currentDayLocations = computed(() => {
  if (!selectedTrip.value || !selectedTrip.value.dayPlans) return [];
  const dayPlan = selectedTrip.value.dayPlans.find(dp => dp.day === selectedDay.value);
  return dayPlan ? dayPlan.locations : [];
});

// 方法
const getLocationTypeName = (type: string): string => {
  const typeMap: Record<string, string> = {
    'attraction': '景点',
    'restaurant': '餐厅',
    'cultural': '文化场所',
    'shopping': '购物',
    'hotel': '酒店'
  };
  return typeMap[type] || type;
};

// 返回上一页
const goBack = () => {
  // 从 query 参数中获取返回路径，如果没有则返回 dashboard
  const returnPath = (route.query.returnPath as string) || '/dashboard';
  router.push(returnPath);
};

// 初始化地图
const initMap = async () => {
  // 如果已有地图服务，先销毁
  if (mapService) {
    try {
      mapService.destroy();
    } catch (error) {
      console.warn('清理旧地图时出错:', error);
    }
    mapService = null;
  }

  mapService = new MapService();
  mapLoading.value = true;
  
  try {
    // 等待 DOM 更新，确保容器已渲染
    await new Promise(resolve => setTimeout(resolve, 300));
    
    // 检查容器是否存在
    const container = document.getElementById('amap-container');
    if (!container) {
      throw new Error('地图容器不存在');
    }
    
    // 确保容器有正确的尺寸
    const containerWidth = container.offsetWidth || container.clientWidth;
    const containerHeight = container.offsetHeight || container.clientHeight;
    
    if (containerWidth === 0 || containerHeight === 0) {
      console.warn('地图容器尺寸为0，等待容器渲染...');
      // 再等待一段时间
      await new Promise(resolve => setTimeout(resolve, 500));
    }
    
    // 默认中心点（北京天安门）
    const defaultCenter = { lng: 116.397428, lat: 39.90923 };
    
    // 如果有选中的行程和地点，使用第一个地点的坐标
    if (currentDayLocations.value.length > 0) {
      const firstLocation = currentDayLocations.value[0];
      if (firstLocation && firstLocation.lng && firstLocation.lat) {
        await mapService.initMap('amap-container', {
          lng: firstLocation.lng,
          lat: firstLocation.lat
        }, 13);
        
        // 添加所有标记
        updateMarkers();
      } else {
        await mapService.initMap('amap-container', defaultCenter, 10);
      }
    } else {
      await mapService.initMap('amap-container', defaultCenter, 10);
    }
    
    // 地图初始化后，强制调整尺寸
    setTimeout(() => {
      if (mapService) {
        mapService.resize();
        // 再次调整，确保地图正确显示
        setTimeout(() => {
          if (mapService) {
            mapService.resize();
          }
        }, 300);
      }
    }, 500);
  } catch (error: any) {
    console.error('地图初始化失败:', error);
    // 只有在真正的错误时才显示错误消息
    const errorMessage = error?.message || '未知错误';
    // 如果是控件初始化错误，不显示错误消息（因为地图已经加载成功）
    if (errorMessage.includes('Zoom is not a constructor') || 
        errorMessage.includes('Scale is not a constructor') || 
        errorMessage.includes('ToolBar is not a constructor')) {
      console.warn('控件初始化失败，但地图已成功加载');
      // 不显示错误消息，因为地图本身已经加载成功
    } else if (errorMessage.includes('容器') || errorMessage.includes('不存在')) {
      // 不显示错误，因为可能是组件已卸载
      console.warn('地图容器未找到，可能组件已卸载');
    } else {
      ElMessage.error('地图加载失败：' + errorMessage);
    }
  } finally {
    mapLoading.value = false;
  }
};

// 更新标记
const updateMarkers = () => {
  if (!mapService) return;
  
  try {
    // 清除旧标记
    mapService.clearMarkers();
    locationMarkers.value = [];

    // 添加新标记
    currentDayLocations.value.forEach((location, index) => {
      const isHighlighted = index === selectedLocationIndex.value;
      const marker = mapService!.addMarker(
        {
          name: location.name,
          lat: location.lat,
          lng: location.lng,
          type: location.type,
          description: location.description
        },
        undefined,
        isHighlighted
      );
      if (marker) {
        locationMarkers.value.push(marker);
      }
    });

    // 如果有地点，调整地图视野
    if (currentDayLocations.value.length > 0) {
      const map = mapService.getMap();
      if (map && locationMarkers.value.length > 0) {
        map.setFitView(locationMarkers.value, false, [50, 50, 50, 50]);
      }
    }
  } catch (error) {
    console.warn('更新标记时出错:', error);
  }
};

// 处理行程变化
const handleTripChange = () => {
  if (selectedTrip.value && selectedTrip.value.dayPlans && selectedTrip.value.dayPlans.length > 0) {
    const firstDayPlan = selectedTrip.value.dayPlans[0];
    if (firstDayPlan && firstDayPlan.day) {
      selectedDay.value = firstDayPlan.day;
      selectedLocationIndex.value = 0;
      updateMarkers();
    } else {
      mapService?.clearMarkers();
      mapService?.clearRoutes();
    }
  } else {
    mapService?.clearMarkers();
    mapService?.clearRoutes();
  }
};

// 处理天数变化
const handleDayChange = () => {
  selectedLocationIndex.value = 0;
  updateMarkers();
  mapService?.clearRoutes();
};

// 处理地点点击
const handleLocationClick = (index: number) => {
  selectedLocationIndex.value = index;
  if (mapService && locationMarkers.value[index]) {
    mapService.highlightMarker(locationMarkers.value[index]);
  }
};

// 规划路线
const planRoute = async () => {
  if (!mapService || currentDayLocations.value.length < 2) {
    ElMessage.warning('至少需要2个地点才能规划路线');
    return;
  }

  routePlanning.value = true;
  try {
    const locations: MapLocationType[] = currentDayLocations.value.map(loc => ({
      name: loc.name,
      lat: loc.lat,
      lng: loc.lng,
      type: loc.type,
      description: loc.description
    }));

    await mapService.planRoute(locations, routeType.value);
    ElMessage.success('路线规划成功');
  } catch (error: any) {
    console.error('路线规划失败:', error);
    ElMessage.error('路线规划失败：' + (error.message || '未知错误'));
  } finally {
    routePlanning.value = false;
  }
};

// 清除路线
const clearRoute = () => {
  mapService?.clearRoutes();
  ElMessage.info('已清除路线');
};

// 生命周期
onMounted(async () => {
  // 加载行程列表
  await tripsStore.fetchTrips();

  // 如果路由中有 tripId，自动选择
  const tripIdFromRoute = route.query.tripId as string;
  if (tripIdFromRoute) {
    selectedTripId.value = tripIdFromRoute;
    // 加载行程详情
    await tripsStore.fetchTrip(tripIdFromRoute);
  }

  // 初始化地图
  await initMap();
});

onUnmounted(() => {
  // 清理地图服务
  if (mapService) {
    try {
      mapService.destroy();
    } catch (error) {
      console.warn('地图销毁时出错:', error);
    }
    mapService = null;
  }
  // 清理标记引用
  locationMarkers.value = [];
});

// 监听地点变化，更新标记
watch(currentDayLocations, () => {
  if (mapService) {
    updateMarkers();
  }
}, { deep: true });
</script>

<style scoped>
.map-page {
  display: flex;
  height: 100vh; /* 占据整个视口高度 */
  width: 100vw; /* 占据整个视口宽度 */
  background: #f5f7fa;
  overflow: hidden;
  position: relative;
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

/* 侧边栏样式 */
.map-sidebar {
  width: 360px;
  min-width: 360px; /* 防止被压缩 */
  max-width: 360px; /* 防止被拉伸 */
  background: #ffffff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  flex-shrink: 0; /* 防止侧边栏被压缩 */
  position: relative;
}

/* 返回按钮 - 在侧边栏内部左上角 */
.back-button {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 10;
  border-radius: 8px; /* 方形圆角 */
  padding: 8px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.back-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.sidebar-header {
  padding: 20px 20px 16px;
  padding-top: 70px; /* 为返回按钮留出空间 */
  border-bottom: 1px solid #e4e7ed;
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f2ff 100%);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.sidebar-section {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.section-title .el-icon {
  font-size: 16px;
  color: #409eff;
}

.trip-selector {
  width: 100%;
}

.trip-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.trip-title {
  font-size: 14px;
  color: #303133;
}

.trip-destination {
  font-size: 12px;
  color: #909399;
}

.day-selector {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.day-selector :deep(.el-radio) {
  margin: 0;
  width: 100%;
}

.day-selector :deep(.el-radio__label) {
  width: 100%;
  padding-left: 8px;
}

.locations-section {
  flex: 1;
  min-height: 200px;
}

.locations-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.location-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.location-item:hover {
  background: #ecf5ff;
  border-color: #b3d8ff;
  transform: translateX(4px);
}

.location-item.active {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-color: #409eff;
  color: #ffffff;
}

.location-item.active .location-name,
.location-item.active .location-type {
  color: #ffffff;
}

.location-order {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  color: #409eff;
  flex-shrink: 0;
}

.location-item.active .location-order {
  background: rgba(255, 255, 255, 0.3);
  color: #ffffff;
}

.location-info {
  flex: 1;
  min-width: 0;
}

.location-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.location-type {
  font-size: 12px;
  color: #909399;
}

.location-icon {
  font-size: 16px;
  color: #c0c4cc;
  flex-shrink: 0;
}

.location-item.active .location-icon {
  color: #ffffff;
}

.route-section {
  padding: 20px;
}

.route-type-selector {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
  width: 100%;
}

.route-type-selector :deep(.el-radio-button) {
  width: 100%;
}

.route-type-selector :deep(.el-radio-button__inner) {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.route-buttons-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.plan-route-button,
.clear-route-button {
  width: 100%;
  margin: 0;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

/* 地图容器样式 */
.map-container {
  flex: 1;
  position: relative;
  background: #e4e7ed;
  min-width: 0; /* 允许 flex 子元素缩小 */
  overflow: hidden;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}

.amap-container {
  width: 100% !important;
  height: 100% !important;
  min-width: 0;
  box-sizing: border-box;
}

.map-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.map-loading .el-icon {
  font-size: 24px;
  color: #409eff;
}

.map-loading span {
  font-size: 14px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .map-sidebar {
    width: 300px;
  }
}

@media (max-width: 768px) {
  .map-page {
    flex-direction: column;
    height: calc(100vh - 64px);
  }

  .map-sidebar {
    width: 100%;
    max-height: 40vh;
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
  }

  .map-container {
    flex: 1;
    min-height: 60vh;
  }
}
</style>
