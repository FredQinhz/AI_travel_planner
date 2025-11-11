<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useTripsStore } from '@/stores/trips';
import { ElTable, ElTableColumn, ElButton, ElCard, ElEmpty } from 'element-plus';
import { 
  Calendar, 
  Money, 
  Clock, 
  SuccessFilled, 
  Plus, 
  Location, 
  View 
} from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();
const tripsStore = useTripsStore();
const loading = ref(true);

// 计算属性
const totalBudget = computed(() => {
  return tripsStore.trips.reduce((sum, trip) => sum + (trip.budgetTotal || 0), 0);
});

const upcomingTripsCount = computed(() => {
  const today = new Date();
  return tripsStore.trips.filter(trip => new Date(trip.startDate) > today).length;
});

const completedTripsCount = computed(() => {
  const today = new Date();
  return tripsStore.trips.filter(trip => new Date(trip.endDate) < today).length;
});

const recentTrips = computed(() => {
  return [...tripsStore.trips]
    .sort((a, b) => new Date(b.createdAt || b.startDate).getTime() - new Date(a.createdAt || a.startDate).getTime())
    .slice(0, 6);
});

// 加载行程列表
const loadTrips = async () => {
  loading.value = true;
  try {
    await tripsStore.fetchTrips();
  } catch (error) {
    console.error('获取行程列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 创建新行程
const createNewTrip = () => {
  router.push('/trips/create');
};

// 查看行程详情
const viewTrip = (tripId: string) => {
  router.push(`/trips/${tripId}`);
};



const getTripStatusType = (trip: any) => {
  const today = new Date();
  const startDate = new Date(trip.startDate);
  const endDate = new Date(trip.endDate);
  
  if (endDate < today) return 'success'; // 已完成
  if (startDate > today) return 'warning'; // 即将出行
  return 'primary'; // 进行中
};

const getTripStatusText = (trip: any) => {
  const today = new Date();
  const startDate = new Date(trip.startDate);
  const endDate = new Date(trip.endDate);
  
  if (endDate < today) return '已完成';
  if (startDate > today) return '即将出行';
  return '进行中';
};

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN');
};

// 退出登录
const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};

// 组件挂载时加载行程
onMounted(() => {
  loadTrips();
});
</script>

<template>
  <div class="dashboard-page">
    <!-- 统计概览卡片 -->
    <div class="stats-overview">
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total-trips">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ tripsStore.trips.length }}</div>
                <div class="stat-label">总行程数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total-budget">
                <el-icon><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥{{ totalBudget }}</div>
                <div class="stat-label">总预算</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon upcoming-trips">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ upcomingTripsCount }}</div>
                <div class="stat-label">即将出行</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon completed-trips">
                <el-icon><SuccessFilled /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ completedTripsCount }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>



    <!-- 所有行程列表 -->
    <div class="all-trips">
      <el-card shadow="hover" class="trips-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">所有行程</span>
            <el-button type="primary" @click="createNewTrip">
              <el-icon><Plus /></el-icon>
              创建新行程
            </el-button>
          </div>
        </template>
        
        <el-table 
          :data="tripsStore.trips" 
          v-loading="loading"
          max-height="400"
          style="width: 100%"
          :default-sort="{ prop: 'createdAt', order: 'descending' }"
        >
          <el-table-column prop="title" label="行程标题" min-width="200">
            <template #default="{ row }">
              <div class="trip-title-cell">
                <span class="title-text">{{ row.title }}</span>
                <el-tag 
                  :type="getTripStatusType(row)"
                  size="small"
                  style="margin-left: 8px"
                >
                  {{ getTripStatusText(row) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="destination" label="目的地" min-width="120" />
          <el-table-column prop="startDate" label="开始日期" min-width="120">
            <template #default="{ row }">
              {{ formatDate(row.startDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="endDate" label="结束日期" min-width="120">
            <template #default="{ row }">
              {{ formatDate(row.endDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="budgetTotal" label="预算" min-width="100">
            <template #default="{ row }">
              ¥{{ row.budgetTotal }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="100" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewTrip(row.id)"
                :icon="View"
              >
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="!loading && tripsStore.trips.length === 0" class="empty-state">
          <el-empty description="还没有创建任何行程" />
          <el-button type="primary" @click="createNewTrip" style="margin-top: 20px">
            创建第一个行程
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 最近行程网格 -->
    <div class="recent-trips">
      <el-card shadow="hover" class="trips-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">最近行程</span>
          </div>
        </template>
        
        <div v-loading="loading" class="trips-grid">
          <div 
            v-for="trip in recentTrips" 
            :key="trip.id" 
            class="trip-card"
            @click="viewTrip(trip.id)"
          >
            <el-card shadow="hover" class="trip-item">
              <div class="trip-header">
                <h3 class="trip-title">{{ trip.title }}</h3>
                <el-tag 
                  :type="getTripStatusType(trip)"
                  size="small"
                >
                  {{ getTripStatusText(trip) }}
                </el-tag>
              </div>
              
              <div class="trip-info">
                <div class="trip-destination">
                  <el-icon><Location /></el-icon>
                  <span>{{ trip.destination }}</span>
                </div>
                <div class="trip-dates">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(trip.startDate) }} - {{ formatDate(trip.endDate) }}</span>
                </div>
                <div class="trip-budget">
                  <el-icon><Money /></el-icon>
                  <span>¥{{ trip.budgetTotal }}</span>
                </div>
              </div>
              
              <div class="trip-actions">
                <el-button type="primary" size="small" @click.stop="viewTrip(trip.id)">
                  查看详情
                </el-button>
              </div>
            </el-card>
          </div>
        </div>
        
        <div v-if="!loading && tripsStore.trips.length === 0" class="empty-state">
          <el-empty description="还没有创建任何行程" />
          <el-button type="primary" @click="createNewTrip" style="margin-top: 20px">
            创建第一个行程
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  /* 添加顶部边距，避免被AppLayout的顶部导航栏遮挡 */
  margin-top: 64px;
}

/* 统计概览样式 */
.stats-overview {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stat-icon.total-trips {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.total-budget {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.upcoming-trips {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.completed-trips {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 所有行程列表样式 */
.all-trips {
  margin-bottom: 24px;
}

.trip-title-cell {
  display: flex;
  align-items: center;
}

.title-text {
  font-weight: 500;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* 最近行程样式 */
.recent-trips {
  margin-bottom: 24px;
}

.trips-card {
  border-radius: 12px;
  border: none;
}

.trips-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-top: 8px;
}

.trip-card {
  cursor: pointer;
  transition: transform 0.2s ease;
}

.trip-card:hover {
  transform: translateY(-2px);
}

.trip-item {
  border-radius: 8px;
  border: none;
  height: 100%;
}

.trip-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.trip-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  flex: 1;
  margin-right: 8px;
}

.trip-info {
  margin-bottom: 16px;
}

.trip-destination,
.trip-dates,
.trip-budget {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.trip-destination .el-icon,
.trip-dates .el-icon,
.trip-budget .el-icon {
  margin-right: 8px;
  color: #909399;
  font-size: 16px;
}

.trip-actions {
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-page {
    padding: 16px;
    margin-top: 56px; /* 移动端顶部导航栏可能更小 */
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
  
  .trips-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-content {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 8px;
  }
}

@media (max-width: 480px) {
  .dashboard-page {
    padding: 12px;
  }
  
  .trip-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .trip-title {
    margin-right: 0;
    margin-bottom: 8px;
  }
}
</style>