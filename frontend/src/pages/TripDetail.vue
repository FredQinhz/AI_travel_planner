<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useTripsStore } from '@/stores/trips';
import { ElButton, ElCard, ElSelect, ElOption, ElEmpty } from 'element-plus';
import { ArrowLeft, MapLocation } from '@element-plus/icons-vue';
import TripInfo from '@/components/trip/TripInfo.vue';
import DailyPlans from '@/components/trip/DailyPlans.vue';
import ExpenseManagement from '@/components/trip/ExpenseManagement.vue';

const route = useRoute();
const router = useRouter();
const tripsStore = useTripsStore();

const loading = ref(true);

// 从路由参数中获取行程ID
const tripId = computed(() => route.params.id as string);

// 当前选中的视图：'plans' | 'expenses' | 'map'
const currentView = ref<'plans' | 'expenses' | 'map'>('plans');

// 视图选项
const viewOptions = [
  { label: '每日行程计划', value: 'plans' },
  { label: '行程支出', value: 'expenses' },
  { label: '地图展示', value: 'map' }
];

// 加载行程详情
const loadTripDetail = async () => {
  if (!tripId.value) return;
  
  loading.value = true;
  try {
    const trip = await tripsStore.fetchTrip(tripId.value);
    if (!trip) {
      // 如果行程不存在，返回仪表盘
      router.push('/dashboard');
    }
  } catch (error) {
    console.error('加载行程详情失败:', error);
    router.push('/dashboard');
  } finally {
    loading.value = false;
  }
};

// 返回仪表盘
const goBack = () => {
  router.push('/dashboard');
};

// 跳转到地图页面
const goToMap = () => {
  if (currentTrip.value) {
    // 传递行程信息到地图页面，并保存当前路径用于返回
    router.push({
      path: '/map',
      query: {
        tripId: currentTrip.value.id,
        tripTitle: currentTrip.value.title,
        returnPath: route.fullPath // 保存当前完整路径用于返回
      }
    });
  }
};

// 计算属性，用于获取当前行程
const currentTrip = computed(() => tripsStore.currentTrip);

// 组件挂载时加载行程详情
onMounted(() => {
  loadTripDetail();
});
</script>

<template>
  <div class="trip-detail-page">
    <!-- 返回按钮 - 固定在左上角 -->
    <el-button 
      class="back-button" 
      type="primary" 
      @click="goBack" 
      :icon="ArrowLeft"
      circle
    >
    </el-button>

    <!-- 主要内容区域 -->
    <main class="main-content" v-loading="loading">
      <div v-if="currentTrip" class="content-container">
        <!-- 页面标题 - 随内容滚动 -->
        <h1 class="page-title">{{ currentTrip.title }}</h1>
        
        <!-- 上半部分：行程详情 -->
        <div class="trip-info-section">
          <TripInfo :trip="currentTrip" />
        </div>

        <!-- 下半部分：视图选择器和内容区域 -->
        <div class="view-section">
          <el-card shadow="hover" class="view-selector-card">
            <div class="view-selector">
              <span class="selector-label">查看内容：</span>
              <el-select 
                v-model="currentView" 
                placeholder="请选择要查看的内容"
                style="width: 200px"
              >
                <el-option
                  v-for="option in viewOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </div>
          </el-card>

          <!-- 内容区域 -->
          <div class="view-content">
            <!-- 每日旅行计划 -->
            <div v-if="currentView === 'plans'" class="view-panel">
              <DailyPlans :day-plans="currentTrip.dayPlans || []" />
            </div>

            <!-- 行程支出 -->
            <div v-if="currentView === 'expenses'" class="view-panel">
              <ExpenseManagement :trip-id="tripId" />
            </div>

            <!-- 地图展示 -->
            <div v-if="currentView === 'map'" class="view-panel">
              <el-card shadow="hover" class="map-placeholder-card">
                <div class="map-placeholder">
                  <el-icon :size="64" color="#909399">
                    <MapLocation />
                  </el-icon>
                  <h3>地图展示</h3>
                  <p>点击下方按钮跳转到地图页面查看行程路线</p>
                  <el-button 
                    type="primary" 
                    size="large" 
                    @click="goToMap"
                    :icon="MapLocation"
                  >
                    查看地图
                  </el-button>
                </div>
              </el-card>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载状态或空状态 -->
      <div v-else-if="!loading" class="empty-state">
        <el-empty description="行程不存在或加载失败" />
        <el-button type="primary" @click="goBack" style="margin-top: 20px">
          返回我的行程
        </el-button>
      </div>
    </main>
  </div>
</template>

<style scoped>
.trip-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 0;
  margin: -24px; /* 抵消 AppLayout content-area 的 padding */
  position: relative;
  display: flex;
  flex-direction: column;
  /* 让页面占据整个视口高度，因为 top-header 已被隐藏 */
  height: 100vh;
  overflow: hidden; /* 禁止页面本身滚动 */
}

/* 返回按钮 - 固定在左上角 */
.back-button {
  position: fixed;
  top: 16px;
  left: calc(var(--sidebar-width, 240px) + 16px); /* 考虑侧边栏宽度 */
  z-index: 200;
  width: 48px;
  height: 48px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.back-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* 页面标题 - 随内容滚动 */
.page-title {
  margin: 0 0 32px 0;
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  text-align: center;
  padding: 0 60px; /* 左右留出空间，避免与返回按钮重叠 */
  line-height: 1.4;
}

/* 主要内容区域 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  flex: 1; /* 占据剩余空间 */
  position: relative;
  z-index: 1;
  /* 添加顶部 padding，避免内容被返回按钮遮挡 */
  padding-top: 80px; /* 为返回按钮留出空间 */
  /* 让 main-content 自己滚动 */
  overflow-y: auto;
  overflow-x: hidden;
  /* 占据剩余高度 */
  height: 100vh;
  box-sizing: border-box;
}

.content-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 行程详情区域 */
.trip-info-section {
  width: 100%;
}

/* 视图选择区域 */
.view-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.view-selector-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.view-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.selector-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}

/* 内容区域 */
.view-content {
  min-height: 400px;
}

.view-panel {
  width: 100%;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 地图占位符卡片 */
.map-placeholder-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  min-height: 400px;
}

.map-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  min-height: 400px;
}

.map-placeholder h3 {
  margin: 20px 0 12px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.map-placeholder p {
  margin: 0 0 30px;
  color: #606266;
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content {
    padding: 20px;
  }
  
  .content-container {
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .trip-detail-page {
    height: 100vh; /* 移动端也是全屏高度 */
  }
  
  .back-button {
    top: 12px;
    left: 16px; /* 移动端侧边栏隐藏，按钮在左上角 */
    width: 40px;
    height: 40px;
  }
  
  .page-title {
    font-size: 24px;
    margin-bottom: 24px;
    padding: 0 50px; /* 移动端减少左右 padding */
  }
  
  .main-content {
    padding: 16px;
    padding-top: 70px; /* 移动端为返回按钮留出空间 */
  }
  
  .view-selector {
    flex-direction: column;
    align-items: stretch;
  }
  
  .selector-label {
    margin-bottom: 8px;
  }
  
  .view-selector :deep(.el-select) {
    width: 100% !important;
  }
}

@media (max-width: 480px) {
  .trip-detail-page {
    height: 100vh; /* 小屏幕也是全屏高度 */
  }
  
  .back-button {
    top: 10px;
    left: 12px;
    width: 36px;
    height: 36px;
  }
  
  .page-title {
    font-size: 20px;
    margin-bottom: 20px;
    padding: 0 40px; /* 小屏幕减少左右 padding */
  }
  
  .main-content {
    padding: 12px;
    padding-top: 60px; /* 小屏幕为返回按钮留出空间 */
  }
  
  .content-container {
    gap: 16px;
  }
}
</style>
