<template>
  <div class="map-page">
    <div class="page-header">
      <h1>地图预览</h1>
      <p>地图功能正在开发中，后续将替换为自由地图预览</p>
    </div>
    
    <div class="map-placeholder">
      <div class="placeholder-content">
        <el-icon size="64" color="#909399">
          <MapLocation />
        </el-icon>
        <h3>地图预览</h3>
        <p>这里将显示用户行程在地图上的分布</p>
        <p class="placeholder-hint">功能正在开发中...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { MapLocation } from '@element-plus/icons-vue'
</script>

<style scoped>
.map-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.page-header p {
  font-size: 16px;
  color: #606266;
}

.map-placeholder {
  background: #f5f7fa;
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-content {
  text-align: center;
  padding: 40px;
}

.placeholder-content h3 {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
  margin: 16px 0 8px;
}

.placeholder-content p {
  color: #606266;
  margin-bottom: 8px;
}

.placeholder-hint {
  font-size: 14px;
  color: #909399;
  font-style: italic;
}

@media (max-width: 768px) {
  .map-page {
    padding: 16px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .map-placeholder {
    min-height: 300px;
  }
}
</style>