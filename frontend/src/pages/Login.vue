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
    <div class="login-form">
      <h2 class="login-title">登录 AI Travel Planner</h2>
      <AuthForm 
        mode="login" 
        @switch-mode="handleSwitchToRegister"
        @auth-success="handleAuthSuccess"
      />
    </div>
  </div>
</template>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-form {
  width: 400px;
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.login-form-inner {
  width: 100%;
}

/* 样式已在AuthForm组件中定义 */
</style>