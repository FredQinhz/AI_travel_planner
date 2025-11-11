<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useTripsStore } from '@/stores/trips';
import { ElTable, ElTableColumn, ElButton, ElCard, ElEmpty } from 'element-plus';

const router = useRouter();
const authStore = useAuthStore();
const tripsStore = useTripsStore();
const loading = ref(true);

// 加载行程列表
const loadTrips = async () => {
  loading.value = true;
  await tripsStore.fetchTrips();
  loading.value = false;
};

// 创建新行程
const createNewTrip = () => {
  router.push('/trips/create');
};

// 查看行程详情
const viewTrip = (tripId: string) => {
  router.push(`/trips/${tripId}`);
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
  <div class="dashboard-container">
    <header class="dashboard-header">
      <div class="header-left">
        <h1 class="app-title">AI Travel Planner</h1>
      </div>
      <div class="header-right">
        <span class="user-info">欢迎，{{ authStore.email }}</span>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </header>
    <main class="dashboard-main">
      <div class="dashboard-actions">
        <h2 class="page-title">我的旅行计划</h2>
        <el-button type="primary" @click="createNewTrip">创建新行程</el-button>
      </div>
      
      <el-card shadow="hover" class="trips-card">
        <el-table
          v-loading="loading"
          :data="tripsStore.trips"
          style="width: 100%"
          empty-text="暂无行程"
        >
          <el-table-column prop="title" label="行程标题" min-width="200"></el-table-column>
          <el-table-column prop="destination" label="目的地" min-width="120"></el-table-column>
          <el-table-column prop="startDate" label="开始日期" min-width="120"></el-table-column>
          <el-table-column prop="endDate" label="结束日期" min-width="120"></el-table-column>
          <el-table-column prop="budgetTotal" label="总预算" min-width="100">
            <template #default="{ row }">¥{{ row.budgetTotal }}</template>
          </el-table-column>
          <el-table-column prop="companionCount" label="同行人数" min-width="80"></el-table-column>
          <el-table-column label="操作" min-width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" @click="viewTrip(row.id)" size="small">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="!loading && tripsStore.trips.length === 0" class="empty-state">
          <el-empty description="还没有创建任何行程" />
          <el-button type="primary" @click="createNewTrip" style="margin-top: 20px">创建第一个行程</el-button>
        </div>
      </el-card>
    </main>
  </div>
</template>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.dashboard-header {
  background-color: #fff;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.app-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  color: #606266;
}

.dashboard-main {
  padding: 24px;
}

.dashboard-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.trips-card {
  border-radius: 8px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}
</style>