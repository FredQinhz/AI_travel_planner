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
</script>

<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-width="80px"
    class="auth-form"
  >
    <el-form-item label="邮箱" prop="email">
      <el-input v-model="form.email" type="email" placeholder="请输入邮箱"></el-input>
    </el-form-item>
    <el-form-item label="密码" prop="password">
      <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
    </el-form-item>
    <el-form-item v-if="mode === 'register'" label="确认密码" prop="confirmPassword">
      <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="handleSubmit" :loading="loading" style="width: 100%">
        {{ submitText }}
      </el-button>
    </el-form-item>
    <div class="switch-link">
      {{ switchModeLabel }} <a href="javascript:void(0)" @click="handleSwitchMode">{{ switchModeText }}</a>
    </div>
  </el-form>
</template>

<style scoped>
.auth-form {
  width: 100%;
}

.switch-link {
  text-align: center;
  margin-top: 20px;
  color: #606266;
}

.switch-link a {
  color: #409eff;
  text-decoration: none;
}

.switch-link a:hover {
  text-decoration: underline;
}
</style>