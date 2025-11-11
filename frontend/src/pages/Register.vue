<script setup lang="ts">
import { useRouter } from 'vue-router';
import AuthForm from '@/components/AuthForm.vue';

const router = useRouter();

const handleAuthSuccess = () => {
  router.push('/login');
};

const handleSwitchToLogin = () => {
  router.push('/login');
};
</script>

<template>
  <div class="register-container">
    <div class="register-form">
      <h2 class="register-title">注册 AI Travel Planner</h2>
      <AuthForm 
        mode="register" 
        @switch-mode="handleSwitchToLogin"
        @auth-success="handleAuthSuccess"
      />
    </div>
  </div>
</template>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-form {
  width: 400px;
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.register-form-inner {
  width: 100%;
}

/* 样式已在AuthForm组件中定义 */
</style>