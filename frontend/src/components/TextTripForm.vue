<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { useTripsStore } from '@/stores/trips';
import type { TripRequest } from '@/stores/trips';

const emit = defineEmits<{
  createSuccess: [tripId: string]
}>();

const tripsStore = useTripsStore();
const router = useRouter();

const form = ref({
  title: '',
  destination: '',
  startDate: '',
  endDate: '',
  budgetTotal: 0,
  companionCount: 0,
  preferences: [] as string[],
  request: ''
});

const preferencesOptions = [
  { label: '美食', value: '美食' },
  { label: '购物', value: '购物' },
  { label: '文化', value: '文化' },
  { label: '自然风景', value: '自然风景' },
  { label: '历史古迹', value: '历史古迹' },
  { label: '主题乐园', value: '主题乐园' },
  { label: '艺术展览', value: '艺术展览' },
  { label: '特种兵行程', value: '特种兵行程' },
  { label: '轻松行程', value: '轻松行程' },
  { label: '亲子', value: '亲子' },
  { label: '情侣', value: '情侣' }
];

const rules = {
  title: [
    { required: true, message: '请输入行程标题', trigger: 'blur' },
    { max: 50, message: '标题不能超过50个字符', trigger: 'blur' }
  ],
  destination: [
    { required: true, message: '请输入目的地', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' },
    {
      validator: (_: any, value: string, callback: any) => {
        if (value && form.value.startDate && new Date(value) < new Date(form.value.startDate)) {
          callback(new Error('结束日期不能早于开始日期'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  budgetTotal: [
    { required: true, message: '请输入总预算', trigger: 'blur' },
    { type: 'number', min: 0, message: '预算不能为负数', trigger: 'blur' }
  ],
  companionCount: [
    { type: 'number', min: 0, message: '同行人数不能为负数', trigger: 'blur' }
  ]
};

const loading = ref(false);
const formRef = ref();
const showLoadingOverlay = ref(false);

const handleCreate = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    loading.value = true;
    showLoadingOverlay.value = true;
    
    const tripRequest: TripRequest = {
      ...form.value
    };
    
    const trip = await tripsStore.createTrip(tripRequest);
    
    // 短暂延迟后关闭遮罩并显示成功消息，然后跳转到dashboard
    setTimeout(() => {
      showLoadingOverlay.value = false;
      if (trip) {
        ElMessage.success('旅行计划已生成');
        // 自动跳转到dashboard页面并刷新
        router.push('/dashboard').then(() => {
          window.location.reload();
        });
      } else {
        ElMessage.error(tripsStore.error || '行程创建失败');
      }
    }, 30000);
  } catch (error) {
    showLoadingOverlay.value = false;
    // 表单验证失败
  } finally {
    loading.value = false;
  }
};

// 清空表单
const clearForm = () => {
  form.value = {
    title: '',
    destination: '',
    startDate: '',
    endDate: '',
    budgetTotal: 0,
    companionCount: 0,
    preferences: [] as string[],
    request: ''
  };
  ElMessage.success('表单已清空');
};
</script>

<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-width="120px"
    class="trip-create-form"
  >
    <el-form-item label="行程标题" prop="title">
      <el-input v-model="form.title" placeholder="请输入行程标题"></el-input>
    </el-form-item>
    <el-form-item label="目的地" prop="destination">
      <el-input v-model="form.destination" placeholder="请输入目的地城市"></el-input>
    </el-form-item>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="总预算" prop="budgetTotal">
          <el-input-number
            v-model="form.budgetTotal"
            :min="0"
            placeholder="请输入预算金额"
            style="width: 100%"
          ></el-input-number>
        </el-form-item> 
      </el-col>
      <el-col :span="12">
        <el-form-item label="同行人数" prop="companionCount">
          <el-input-number
            v-model="form.companionCount"
            :min="0"
            placeholder="请输入同行人数"
            style="width: 100%"
          ></el-input-number>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="旅行偏好">
      <el-select
        v-model="form.preferences"
        multiple
        placeholder="请选择旅行偏好"
        collapse-tags
      >
        <el-option
          v-for="option in preferencesOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        ></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="额外信息">
      <el-input
        v-model="form.request"
        type="textarea"
        :rows="3"
        placeholder="请输入您的特别要求或其他信息"
      ></el-input>
    </el-form-item>
    <el-form-item>
      <el-button style="margin-right: 10px" @click="clearForm">清空</el-button>
      <el-button type="primary" @click="handleCreate" :loading="loading">创建行程</el-button>
    </el-form-item>
  </el-form>
  
  <!-- 全屏灰色遮罩 -->
  <div 
    v-if="showLoadingOverlay" 
    class="loading-overlay"
  >
    <div class="loading-content">
      <el-spinner size="large" />
      <p class="loading-text">正在生成旅行计划...</p>
    </div>
  </div>
</template>

<style scoped>
.trip-create-form {
  padding: 20px 0;
}

/* 按钮居中 - 强制覆盖Element Plus默认样式 */
.trip-create-form :deep(.el-form-item:last-child) {
  text-align: center !important;
  display: flex !important;
  justify-content: center !important;
}

.trip-create-form :deep(.el-form-item:last-child .el-form-item__content) {
  display: flex !important;
  justify-content: center !important;
  gap: 10px !important;
  margin-left: 0 !important;
  padding-left: 0 !important;
}

/* 全屏灰色遮罩样式 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-content {
  text-align: center;
  color: white;
}

.loading-text {
  margin-top: 16px;
  font-size: 16px;
}
</style>