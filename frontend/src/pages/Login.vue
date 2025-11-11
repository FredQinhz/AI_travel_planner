<script setup lang="ts">
import { useRouter } from 'vue-router';
import AuthForm from '@/components/AuthForm.vue';

const router = useRouter();

const handleAuthSuccess = () => {
  router.push('/dashboard');
};

const handleSwitchToRegister = () => {
  router.push('/register');
};
</script>

<template>
  <div class="login-container">
    <!-- 左侧品牌展示区 -->
    <div class="login-brand-section">
      <div class="brand-content">
        <h1 class="brand-title">AI Travel Planner</h1>
        <p class="brand-subtitle">智能规划，畅享旅程</p>
        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-icon">✈️</div>
            <span>AI 驱动的行程规划</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🗺️</div>
            <span>智能路线推荐</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">💰</div>
            <span>预算管理与优化</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">📅</div>
            <span>个性化日程安排</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 右侧登录表单区 -->
    <div class="login-form-section">
      <div class="login-form-wrapper">
        <div class="form-header">
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-subtitle">登录您的账号，开始智能旅行规划</p>
        </div>
        <AuthForm 
          mode="login" 
          @switch-mode="handleSwitchToRegister"
          @auth-success="handleAuthSuccess"
        />
        <div class="form-footer">
          <p>© 2025 AI Travel Planner. 保留所有权利。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 确保根元素没有默认边距和内边距 */
:global(body),
:global(html) {
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}

.login-container {
  height: 100vh;
  display: flex;
  width: 100%;
  overflow: hidden;
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

/* 防止容器宽度超过视口 */
:global(#app) {
  width: 100%;
  overflow-x: hidden;
}

/* 左侧品牌展示区 */
.login-brand-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  padding: 2rem;
  position: relative;
  overflow: hidden;
}

.login-brand-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.15) 0%, transparent 30%),
    radial-gradient(circle at 70% 60%, rgba(255, 255, 255, 0.1) 0%, transparent 40%),
    radial-gradient(circle at 40% 80%, rgba(255, 255, 255, 0.08) 0%, transparent 35%);
  pointer-events: none;
}

.brand-content {
  max-width: 400px;
  z-index: 1;
}

.brand-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 1rem;
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 1.25rem;
  margin-bottom: 3rem;
  opacity: 0.9;
  font-weight: 300;
}

.brand-features {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 1rem;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease, background-color 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.15);
}

.feature-icon {
  font-size: 2rem;
  margin-bottom: 0.75rem;
}

.feature-item span {
  font-size: 0.9rem;
  line-height: 1.4;
}

/* 右侧登录表单区 */
.login-form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafbfc;
  padding: 2rem;
}

.login-form-wrapper {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 16px;
  padding: 2.5rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s ease;
}

.login-form-wrapper:hover {
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.08);
}

.form-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.login-title {
  font-size: 1.75rem;
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 0.5rem;
}

.login-subtitle {
  font-size: 1rem;
  color: #718096;
  margin: 0;
}

.form-footer {
  text-align: center;
  margin-top: 2rem;
  color: #a0aec0;
  font-size: 0.875rem;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .brand-title {
    font-size: 2.5rem;
  }
  
  .brand-features {
    gap: 1rem;
  }
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    height: auto;
    min-height: 100vh;
  }
  
  .login-brand-section {
    padding: 2rem 1.5rem;
    min-height: 50vh;
  }
  
  .login-form-section {
    padding: 2rem 1.5rem;
    flex: none;
  }
  
  .brand-features {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>