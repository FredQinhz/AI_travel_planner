<script setup lang="ts">
import { computed } from 'vue';
import { ElCard, ElTag, ElDescriptions, ElDescriptionsItem } from 'element-plus';
import type { TripResponse } from '@/stores/trips';

interface Props {
  trip: TripResponse;
}

const props = defineProps<Props>();

// 计算行程天数
const tripDays = computed(() => {
  if (!props.trip) return 0;
  const start = new Date(props.trip.startDate);
  const end = new Date(props.trip.endDate);
  return Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1;
});

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};
</script>

<template>
  <el-card shadow="hover" class="trip-info-card">
    <template #header>
      <div class="card-header">
        <h2 class="card-title">行程信息</h2>
      </div>
    </template>
    
    <el-descriptions :column="2" border>
      <el-descriptions-item label="行程标题">
        <span class="info-value">{{ trip.title }}</span>
      </el-descriptions-item>
      
      <el-descriptions-item label="目的地">
        <span class="info-value">{{ trip.destination }}</span>
      </el-descriptions-item>
      
      <el-descriptions-item label="旅行日期">
        <span class="info-value">
          {{ formatDate(trip.startDate) }} 至 {{ formatDate(trip.endDate) }}
        </span>
        <el-tag type="info" size="small" style="margin-left: 8px">
          共 {{ tripDays }} 天
        </el-tag>
      </el-descriptions-item>
      
      <el-descriptions-item label="总预算">
        <span class="info-value budget">¥{{ trip.budgetTotal.toLocaleString() }}</span>
      </el-descriptions-item>
      
      <el-descriptions-item label="同行人数">
        <span class="info-value">{{ trip.companionCount || 1 }} 人</span>
      </el-descriptions-item>
      
      <el-descriptions-item label="旅行偏好">
        <div class="preferences">
          <el-tag 
            v-for="(pref, index) in trip.preferences" 
            :key="index" 
            type="primary" 
            size="small" 
            style="margin-right: 8px; margin-bottom: 4px"
          >
            {{ pref }}
          </el-tag>
          <span v-if="!trip.preferences || trip.preferences.length === 0" class="no-prefs">
            暂无偏好设置
          </span>
        </div>
      </el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<style scoped>
.trip-info-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.info-value {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.info-value.budget {
  color: #409eff;
  font-size: 16px;
  font-weight: 600;
}

.preferences {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.no-prefs {
  color: #909399;
  font-size: 12px;
  font-style: italic;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #303133;
  width: 120px;
}

:deep(.el-descriptions__content) {
  color: #606266;
}
</style>

