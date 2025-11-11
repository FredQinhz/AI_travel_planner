<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useTripsStore } from '@/stores/trips';
import { ElMessage } from 'element-plus';
import type { TripRequest } from '@/stores/trips';

const router = useRouter();
const tripsStore = useTripsStore();

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
  { label: '温泉', value: '温泉' },
  { label: '艺术展览', value: '艺术展览' }
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

const handleCreate = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    loading.value = true;
    
    const tripRequest: TripRequest = {
      ...form.value
    };
    
    const trip = await tripsStore.createTrip(tripRequest);
    
    if (trip) {
      ElMessage.success('行程创建成功');
      router.push(`/trips/${trip.id}`);
    } else {
      ElMessage.error(tripsStore.error || '行程创建失败');
    }
  } catch (error) {
    // 表单验证失败
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  router.push('/dashboard');
};
</script>

<template>
  <div class="trip-create-container">
    <header class="page-header">
      <h2 class="page-title">创建新行程</h2>
    </header>
    <
    <main class="page-main">
      <el-card shadow="hover" class="create-form-card">
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
          <el-form-item label="特别要求">
            <el-input
              v-model="form.request"
              type="textarea"
              :rows="3"
              placeholder="请输入您的特别要求或其他信息"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button @click="goBack" style="margin-right: 10px">取消</el-button>
            <el-button type="primary" @click="handleCreate" :loading="loading">创建行程</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </main>
  </div>
</template>

<style scoped>
.trip-create-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  background-color: #fff;
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.page-main {
  padding: 24px;
}

.create-form-card {
  border-radius: 8px;
}

.trip-create-form {
  padding: 20px 0;
}
</style>