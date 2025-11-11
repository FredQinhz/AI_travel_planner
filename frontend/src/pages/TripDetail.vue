<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useTripsStore } from '@/stores/trips'; 
import { ElCard, ElTabs, ElTabPane, ElDescriptions, ElDescriptionsItem, ElTag, ElButton } from 'element-plus';

const route = useRoute();
const router = useRouter();
const tripsStore = useTripsStore();
const loading = ref(true);

// 从路由参数中获取行程ID
const tripId = computed(() => route.params.id as string);
const activeDay = ref('1'); // 默认选中第一天

// 加载行程详情
const loadTripDetail = async () => {
  if (!tripId.value) return;
  
  loading.value = true;
  const trip = await tripsStore.fetchTrip(tripId.value);
  if (!trip) {
    // 如果行程不存在，返回仪表盘
    router.push('/dashboard');
  }
  loading.value = false;
};

// 返回仪表盘
const goBack = () => {
  router.push('/dashboard');
};

// 组件挂载时加载行程详情
onMounted(() => {
  loadTripDetail();
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

// 导入computed
import { computed } from 'vue';
</script>

<template>
  <div class="trip-detail-container">
    <header class="detail-header">
      <div class="header-left">
        <el-button @click="goBack" icon="ArrowLeft" style="margin-right: 16px">返回</el-button>
        <h1 class="trip-title" v-if="currentTrip">{{ currentTrip.title }}</h1>
      </div>
    </header>
    <main class="detail-main" v-if="currentTrip">
      <el-card shadow="hover" class="trip-info-card">
        <el-descriptions :column="1" border label-position="top">
          <el-descriptions-item label="目的地">{{ currentTrip.destination }}</el-descriptions-item>
          <el-descriptions-item label="旅行日期">{{ currentTrip.startDate }} 至 {{ currentTrip.endDate }}（共{{ tripDays }}天）</el-descriptions-item>
          <el-descriptions-item label="总预算">¥{{ currentTrip.budgetTotal }}</el-descriptions-item>
          <el-descriptions-item label="同行人数">{{ currentTrip.companionCount }}</el-descriptions-item>
          <el-descriptions-item label="旅行偏好">
            <el-tag v-for="(pref, index) in currentTrip.preferences" :key="index" type="info" style="margin-right: 8px; margin-bottom: 8px">{{ pref }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ new Date(currentTrip.createdAt).toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ new Date(currentTrip.updatedAt).toLocaleString() }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
      <
      <el-card shadow="hover" class="day-plans-card" style="margin-top: 24px">
        <div slot="header" class="card-header">
          <h3 class="card-title">每日行程安排</h3>
        </div>
        <
        <el-tabs v-model="activeDay" type="border-card" class="day-tabs">
          <el-tab-pane
            v-for="dayPlan in currentTrip.dayPlans"
            :key="dayPlan.day"
            :label="`第${dayPlan.day}天`"
          >
            <div class="day-content">
              <div v-if="dayPlan.locations.length === 0" class="empty-locations">
                暂无安排
              </div>
              <el-timeline v-else>
                <el-timeline-item
                  v-for="(location, index) in dayPlan.locations"
                  :key="index"
                  :timestamp="`第${index + 1}站`"
                >
                  <el-card shadow="hover" class="location-card">
                    <div class="location-header">
                      <h4 class="location-name">{{ location.name }}</h4>
                      <el-tag :type="getLocationTagType(location.type)">{{ getLocationTypeName(location.type) }}</el-tag>
                    </div>
                    <p class="location-description">{{ location.description }}</p>
                    <div class="location-coords">
                      经纬度：{{ location.lat }}, {{ location.lng }}
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </main>
  </div>
</template>

<style scoped>
.trip-detail-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.detail-header {
  background-color: #fff;
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.trip-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.detail-main {
  padding: 24px;
}

.trip-info-card,
.day-plans-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.day-tabs {
  margin-top: 20px;
}

.day-content {
  padding: 20px 0;
}

.empty-locations {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.location-card {
  margin-bottom: 16px;
}

.location-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.location-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.location-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

.location-coords {
  color: #909399;
  font-size: 14px;
}
</style>