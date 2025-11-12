<script setup lang="ts">
import { ref } from 'vue';
import { ElCard, ElTabs, ElTabPane, ElTag, ElEmpty, ElButton } from 'element-plus';
import { Refresh } from '@element-plus/icons-vue';
import type { DayPlanDTO, LocationDTO } from '@/stores/trips';

interface Props {
  dayPlans: DayPlanDTO[];
  refreshing?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  refreshing: false
});

const emit = defineEmits<{
  refresh: [];
}>();

const activeDay = ref('1');

// 获取位置类型对应的标签类型
const getLocationTagType = (type: string) => {
  const typeMap: Record<string, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    'attraction': 'primary',
    'restaurant': 'success',
    'cultural': 'warning',
    'shopping': 'info',
    'hotel': 'danger'
  };
  return typeMap[type] || 'info';
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
  <el-card shadow="hover" class="daily-plans-card">
    <template #header>
      <div class="card-header">
        <h2 class="card-title">每日行程安排</h2>
        <el-button 
          class="refresh-button" 
          type="primary" 
          :icon="Refresh"
          :loading="refreshing"
          @click="emit('refresh')"
          circle
          size="small"
          :title="'刷新行程计划'"
        >
        </el-button>
      </div>
    </template>
    
    <div v-if="dayPlans && dayPlans.length > 0">
      <el-tabs v-model="activeDay" type="border-card" class="day-tabs">
        <el-tab-pane 
          v-for="dayPlan in dayPlans" 
          :key="dayPlan.day" 
          :label="`第 ${dayPlan.day} 天`" 
          :name="dayPlan.day.toString()"
        >
          <div v-if="dayPlan.locations && dayPlan.locations.length > 0" class="day-plan-content">
            <div 
              v-for="(location, index) in dayPlan.locations" 
              :key="index"
              class="location-item"
            >
              <div class="location-header">
                <div class="location-order">
                  <span class="order-number">{{ index + 1 }}</span>
                </div>
                <div class="location-info">
                  <h4 class="location-name">{{ location.name }}</h4>
                  <el-tag :type="getLocationTagType(location.type)" size="small" class="location-tag">
                    {{ getLocationTypeName(location.type) }}
                  </el-tag>
                </div>
              </div>
              <p v-if="location.description" class="location-description">
                {{ location.description }}
              </p>
              <div class="location-meta">
                <span class="coordinates">
                  坐标：{{ location.lat.toFixed(6) }}, {{ location.lng.toFixed(6) }}
                </span>
              </div>
            </div>
          </div>
          <div v-else class="no-plan">
            <el-empty description="暂无行程安排" :image-size="80" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <div v-else class="no-plans">
      <el-empty description="暂无行程计划" :image-size="100" />
    </div>
  </el-card>
</template>

<style scoped>
.daily-plans-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.refresh-button {
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.refresh-button:hover {
  transform: rotate(180deg);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.day-tabs {
  margin-top: 0;
}

.day-plan-content {
  padding: 16px 0;
  max-height: 600px;
  overflow-y: auto;
}

.location-item {
  padding: 20px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  border-left: 4px solid #409eff;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.location-item:hover {
  background: linear-gradient(135deg, #eef2ff 0%, #f8f9fa 100%);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.location-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 12px;
}

.location-order {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.order-number {
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.location-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.location-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.location-tag {
  margin-left: auto;
}

.location-description {
  margin: 12px 0;
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
  padding-left: 48px;
}

.location-meta {
  padding-left: 48px;
  font-size: 12px;
  color: #909399;
}

.coordinates {
  font-family: 'Courier New', monospace;
}

.no-plan,
.no-plans {
  padding: 60px 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .location-header {
    flex-direction: column;
    gap: 8px;
  }
  
  .location-description,
  .location-meta {
    padding-left: 0;
  }
  
  .location-info {
    width: 100%;
  }
  
  .location-tag {
    margin-left: 0;
  }
}
</style>

