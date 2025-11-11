<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';
import TextTripForm from '@/components/TextTripForm.vue';
import VoiceTripForm from '@/components/VoiceTripForm.vue';

const router = useRouter();

// 当前选中的创建方式：'text' 或 'voice'
const createMode = ref('text');

// 切换创建方式
const switchCreateMode = (mode: 'text' | 'voice') => {
  createMode.value = mode;
};

// 返回上一页
const goBack = () => {
  router.push('/dashboard');
};

// 处理行程创建成功
const handleCreateSuccess = (tripId: string) => {
  router.push(`/trips/${tripId}`);
};
</script>

<template>
  <div class="trip-create-container">
    <header class="page-header">
      <div class="header-content">
        <el-button type="text" @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2 class="page-title">创建新的行程</h2>
      </div>
    </header>
    
    <main class="page-main">
      <el-card shadow="hover" class="create-form-card">
        <!-- 创建方式切换 -->
        <div class="create-mode-switch">
          <el-button-group size="large" class="mode-toggle-buttons">
            <el-button 
              class="mode-button" 
              :class="{ active: createMode === 'text' }"
              @click="switchCreateMode('text')"
            >
              文本创建
            </el-button>
            <el-button 
              class="mode-button" 
              :class="{ active: createMode === 'voice' }"
              @click="switchCreateMode('voice')"
            >
              语音创建
            </el-button>
          </el-button-group>
        </div>
        
        <!-- 根据选择显示对应的表单组件 -->
        <TextTripForm v-if="createMode === 'text'" @create-success="handleCreateSuccess" />
        <VoiceTripForm v-else @create-success="handleCreateSuccess" />
      </el-card>
    </main>
  </div>
</template>

<style scoped>
.trip-create-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.page-header {
  background-color: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 100;
  /* 添加顶部边距，避免被AppLayout的顶部导航栏遮挡 */
  margin-top: 64px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  position: relative;
}

.back-button {
  position: absolute;
  left: 0;
  font-size: 18px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.page-main {
  padding: 24px;
  flex: 1;
  /* 确保内容区域也有足够的上边距 */
  margin-top: 0;
}

.create-form-card {
  border-radius: 12px;
  overflow: hidden;
  /* 确保卡片不会被遮挡 */
  margin-top: 0;
}

.create-mode-switch {
  position: relative;
  padding: 24px 0 24px 0;
  margin: 0;
  border-bottom: 1px solid #ebeef5;
  background-color: #fff;
  z-index: 1;
}

.mode-toggle-buttons {
  display: flex;
  width: 300px;
  margin: 0 auto;
  position: relative;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
}

.mode-button {
  flex: 1;
  background-color: #fff;
  color: #606266;
  border: none;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

.mode-button.active {
  background-color: #409eff;
  color: #fff;
}

.mode-button:hover:not(.active) {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    padding: 12px 16px;
    margin-top: 56px; /* 移动端顶部导航栏可能更小 */
  }
  
  .page-main {
    padding: 16px;
  }
  
  .mode-toggle-buttons {
    width: 100%;
  }
  
  .create-mode-switch {
    margin-bottom: 16px;
  }
}
</style>