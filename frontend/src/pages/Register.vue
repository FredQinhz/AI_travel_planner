<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElMessage } from 'element-plus';

const router = useRouter();
const authStore = useAuthStore();

const form = ref({
  email: '',
  password: '',
  confirmPassword: ''
});

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_: any, value: string, callback: any) => {
        if (value !== form.value.password) {
          callback(new Error('两次输入密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

const loading = ref(false);
const formRef = ref();

const handleRegister = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    loading.value = true;
    
    const success = await authStore.register({
      email: form.value.email,
      password: form.value.password
    });
    
    if (success) {
      ElMessage.success('注册成功，请登录');
      router.push('/login');
    } else {
      ElMessage.error(authStore.error || '注册失败，请稍后重试');
    }
  } catch (error) {
    // 表单验证失败
  } finally {
    loading.value = false;
  }
};

const goToLogin = () => {
  router.push('/login');
};
</script>

<template>
  <div class="register-container">
    <div class="register-form">
      <h2 class="register-title">注册 AI Travel Planner</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        class="register-form-inner"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" type="email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
        <div class="login-link">
          已有账号？<a href="javascript:void(0)" @click="goToLogin">立即登录</a>
        </div>
      </el-form>
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

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #606266;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>