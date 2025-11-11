<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage, type FormRules } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import type { LoginRequest, RegisterRequest } from '@/stores/auth';

interface Props {
  mode: 'login' | 'register';
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'login'
});

const emit = defineEmits<{
  'switch-mode': [mode: 'login' | 'register'];
  'auth-success': [];
}>();

const authStore = useAuthStore();
const loading = ref(false);
const formRef = ref();
const showPassword = ref(false);
const showConfirmPassword = ref(false);

const form = ref({
  email: '',
  password: '',
  confirmPassword: ''
});

const submitText = computed(() => {
  return props.mode === 'login' ? '登录' : '注册';
});

const switchModeText = computed(() => {
  return props.mode === 'login' ? '立即注册' : '立即登录';
});

const switchModeLabel = computed(() => {
  return props.mode === 'login' ? '还没有账号？' : '已有账号？';
});

const switchModeTo = computed(() => {
  return props.mode === 'login' ? 'register' : 'login';
});

const rules = computed<FormRules>(() => {
  const baseRules: FormRules = {
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email' as const, message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ]
  };

  if (props.mode === 'register') {
    return {
      ...baseRules,
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
  }

  return baseRules;
});

const handleSwitchMode = () => {
  emit('switch-mode', switchModeTo.value);
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    loading.value = true;
    
    let success = false;
    
    if (props.mode === 'login') {
      const loginData: LoginRequest = {
        email: form.value.email,
        password: form.value.password
      };
      success = await authStore.login(loginData);
    } else {
      const registerData: RegisterRequest = {
        email: form.value.email,
        password: form.value.password
      };
      success = await authStore.register(registerData);
    }
    
    if (success) {
      ElMessage.success(props.mode === 'login' ? '登录成功' : '注册成功，请登录');
      emit('auth-success');
    } else {
      ElMessage.error(authStore.error || (props.mode === 'login' ? '登录失败，请检查邮箱和密码' : '注册失败，请稍后重试'));
    }
  } catch (error) {
    // 表单验证失败
  } finally {
    loading.value = false;
  }
};

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

const toggleConfirmPasswordVisibility = () => {
  showConfirmPassword.value = !showConfirmPassword.value;
};
</script>

<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    label-width="100%"
    class="auth-form"
  >
    <el-form-item label="邮箱地址" prop="email"
      class="form-item-custom"
    >
      <el-input 
        v-model="form.email" 
        type="email" 
        placeholder="请输入您的邮箱"
        prefix-icon="el-icon-user"
        class="form-input-custom"
        size="large"
      ></el-input>
    </el-form-item>
    <el-form-item label="密码" prop="password"
      class="form-item-custom"
    >
      <el-input 
        v-model="form.password" 
        :type="showPassword ? 'text' : 'password'" 
        placeholder="请输入您的密码"
        prefix-icon="el-icon-lock"
        class="form-input-custom"
        size="large"
      >
        <template #append>
          <el-button 
            @click="togglePasswordVisibility" 
            type="text" 
            class="toggle-password-btn"
          >
            {{ showPassword ? '隐藏' : '显示' }}
          </el-button>
        </template>
      </el-input>
    </el-form-item>
    <el-form-item 
      v-if="mode === 'register'" 
      label="确认密码" 
      prop="confirmPassword"
      class="form-item-custom"
    >
      <el-input 
        v-model="form.confirmPassword" 
        :type="showConfirmPassword ? 'text' : 'password'" 
        placeholder="请再次输入您的密码"
        prefix-icon="el-icon-lock"
        class="form-input-custom"
        size="large"
      >
        <template #append>
          <el-button 
            @click="toggleConfirmPasswordVisibility" 
            type="text" 
            class="toggle-password-btn"
          >
            {{ showConfirmPassword ? '隐藏' : '显示' }}
          </el-button>
        </template>
      </el-input>
    </el-form-item>
    <el-form-item class="submit-button-container">
      <el-button 
        type="primary" 
        @click="handleSubmit" 
        :loading="loading" 
        class="submit-button"
        size="large"
      >
        {{ submitText }}
      </el-button>
    </el-form-item>
    <div class="switch-link-container">
      <span class="switch-link-label">{{ switchModeLabel }}</span>
      <a 
        href="javascript:void(0)" 
        @click="handleSwitchMode"
        class="switch-link-button"
      >{{ switchModeText }}</a>
    </div>
  </el-form>
</template>

<style scoped>
.auth-form {
  width: 100%;
}

.form-item-custom {
  margin-bottom: 1.5rem;
}

.form-item-custom .el-form-item__label {
  font-size: 0.9rem;
  font-weight: 500;
  color: #4a5568;
  margin-bottom: 0.5rem;
  padding: 0;
}

.form-input-custom {
  width: 100%;
  border-radius: 8px !important;
  border: 1px solid #e2e8f0 !important;
  transition: all 0.3s ease;
}

.form-input-custom:focus-within {
  border-color: #667eea !important;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1) !important;
}

.form-input-custom .el-input__inner {
  border: none !important;
  border-radius: 8px !important;
  padding: 0.75rem 1rem !important;
  font-size: 1rem;
  transition: all 0.3s ease;
  background-color: #f9fafb;
}

.form-input-custom .el-input__inner:focus {
  background-color: #ffffff;
  box-shadow: none !important;
}

.form-input-custom .el-input__prefix {
  color: #a0aec0;
  font-size: 1.1rem;
}

.toggle-password-btn {
  color: #718096;
  font-size: 0.875rem;
  padding: 0 0.75rem;
  transition: color 0.3s ease;
}

.toggle-password-btn:hover {
  color: #667eea;
}

.submit-button-container {
  margin-top: 2rem;
  margin-bottom: 1.5rem;
}

.submit-button {
  width: 100%;
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
  height: auto;
  line-height: 1.5;
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

.submit-button:active:not(:disabled) {
  transform: translateY(0);
}

.switch-link-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.switch-link-label {
  color: #718096;
}

.switch-link-button {
  color: #667eea;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s ease;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.switch-link-button:hover {
  background-color: rgba(102, 126, 234, 0.1);
  text-decoration: none;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .form-item-custom {
    margin-bottom: 1.25rem;
  }
  
  .submit-button-container {
    margin-top: 1.5rem;
  }
}
</style>