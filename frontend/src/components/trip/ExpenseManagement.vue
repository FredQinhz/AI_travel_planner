<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElCard, ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElInputNumber, ElSelect, ElDatePicker, ElMessage, ElMessageBox, ElTag, ElStatistic, ElRow, ElCol, ElEmpty, ElProgress } from 'element-plus';
import { Plus, Edit, Delete, Money, TrendCharts } from '@element-plus/icons-vue';
import { useExpensesStore, type ExpenseRequest } from '@/stores/expenses';
// 使用原生 Date API 格式化日期

interface Props {
  tripId: string;
}

const props = defineProps<Props>();

const expensesStore = useExpensesStore();

// 对话框状态
const showAddDialog = ref(false);
const showEditDialog = ref(false);
const showBudgetDialog = ref(false);
const currentExpense = ref<ExpenseRequest | null>(null);
const editingExpenseId = ref<string | null>(null);

// 表单引用
const addFormRef = ref();
const editFormRef = ref();

// 表单数据
const expenseForm = ref<ExpenseRequest>({
  amount: 0,
  currency: 'CNY',
  comment: '',
  category: '',
  expenseDate: new Date().toISOString().split('T')[0]
});

// 表单验证规则
const formRules = {
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ],
  currency: [
    { required: true, message: '请选择货币', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入消费说明', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择消费类别', trigger: 'change' }
  ],
  expenseDate: [
    { required: true, message: '请选择消费日期', trigger: 'change' }
  ]
};

// 消费类别选项
const categoryOptions = [
  { label: '餐饮', value: '餐饮' },
  { label: '住宿', value: '住宿' },
  { label: '交通', value: '交通' },
  { label: '景点', value: '景点' },
  { label: '购物', value: '购物' },
  { label: '娱乐', value: '娱乐' },
  { label: '其他', value: '其他' }
];

// 货币选项
const currencyOptions = [
  { label: '人民币 (CNY)', value: 'CNY' },
  { label: '美元 (USD)', value: 'USD' },
  { label: '欧元 (EUR)', value: 'EUR' },
  { label: '日元 (JPY)', value: 'JPY' }
];

// 加载数据
const loadData = async () => {
  await Promise.all([
    expensesStore.fetchExpenses(props.tripId),
    expensesStore.fetchBudget(props.tripId)
  ]);
};

// 打开添加对话框
const openAddDialog = () => {
  expenseForm.value = {
    amount: 0,
    currency: 'CNY',
    comment: '',
    category: '',
    expenseDate: new Date().toISOString().split('T')[0]
  };
  showAddDialog.value = true;
};

// 打开编辑对话框
const openEditDialog = (expense: any) => {
  editingExpenseId.value = expense.id;
  expenseForm.value = {
    amount: expense.amount,
    currency: expense.currency,
    comment: expense.comment,
    category: expense.category,
    expenseDate: expense.expenseDate
  };
  showEditDialog.value = true;
};

// 提交添加
const handleAdd = async () => {
  if (!addFormRef.value) return;
  
  try {
    await addFormRef.value.validate();
    const result = await expensesStore.addExpense(props.tripId, expenseForm.value);
    if (result) {
      ElMessage.success('添加消费记录成功');
      showAddDialog.value = false;
      await loadData();
    } else {
      ElMessage.error(expensesStore.error || '添加失败');
    }
  } catch (error) {
    // 表单验证失败
  }
};

// 提交编辑
const handleEdit = async () => {
  if (!editFormRef.value || !editingExpenseId.value) return;
  
  try {
    await editFormRef.value.validate();
    const result = await expensesStore.updateExpense(
      props.tripId, 
      editingExpenseId.value, 
      expenseForm.value
    );
    if (result) {
      ElMessage.success('更新消费记录成功');
      showEditDialog.value = false;
      editingExpenseId.value = null;
      await loadData();
    } else {
      ElMessage.error(expensesStore.error || '更新失败');
    }
  } catch (error) {
    // 表单验证失败
  }
};

// 删除消费记录
const handleDelete = async (expense: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除消费记录「${expense.comment}」吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    const success = await expensesStore.deleteExpense(props.tripId, expense.id);
    if (success) {
      ElMessage.success('删除成功');
      await loadData();
    } else {
      ElMessage.error(expensesStore.error || '删除失败');
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除操作失败');
    }
  }
};

// 格式化日期
const formatDate = (dateString: string) => {
  try {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  } catch {
    return dateString;
  }
};

// 格式化金额
const formatAmount = (amount: number, currency: string) => {
  return `${currency} ${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
};

// 打开预算对话框
const openBudgetDialog = async () => {
  await expensesStore.fetchBudget(props.tripId);
  showBudgetDialog.value = true;
};

// 计算总支出
const totalExpenses = computed(() => {
  return expensesStore.expenses.reduce((sum, expense) => sum + Number(expense.amount), 0);
});

// 组件挂载时加载数据
onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="expense-management">
    <!-- 预算状态卡片 -->
    <el-card shadow="hover" class="budget-card" v-if="expensesStore.budget">
      <template #header>
        <div class="card-header">
          <h3 class="card-title">
            <el-icon><Money /></el-icon>
            预算状态
          </h3>
          <el-button type="primary" size="small" @click="openBudgetDialog">
            <el-icon><TrendCharts /></el-icon>
            查看详情
          </el-button>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <el-statistic title="总预算" :value="expensesStore.budget.totalBudget">
            <template #prefix>¥</template>
          </el-statistic>
        </el-col>
        <el-col :span="8">
          <el-statistic title="已花费" :value="expensesStore.budget.spent">
            <template #prefix>¥</template>
            <template #suffix>
              <el-tag :type="expensesStore.budget.overspend ? 'danger' : 'success'" size="small" style="margin-left: 8px">
                {{ expensesStore.budget.overspend ? '超支' : '正常' }}
              </el-tag>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="8">
          <el-statistic title="剩余预算" :value="expensesStore.budget.remaining">
            <template #prefix>¥</template>
          </el-statistic>
        </el-col>
      </el-row>
    </el-card>

    <!-- 消费记录列表 -->
    <el-card shadow="hover" class="expenses-card">
      <template #header>
        <div class="card-header">
          <h3 class="card-title">消费记录</h3>
          <el-button type="primary" @click="openAddDialog" :icon="Plus">
            添加消费
          </el-button>
        </div>
      </template>
      
      <el-table 
        :data="expensesStore.expenses" 
        v-loading="expensesStore.isLoading"
        style="width: 100%"
        :default-sort="{ prop: 'expenseDate', order: 'descending' }"
      >
        <el-table-column prop="expenseDate" label="日期" width="120" sortable>
          <template #default="{ row }">
            {{ formatDate(row.expenseDate) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="category" label="类别" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="comment" label="说明" min-width="200" />
        
        <el-table-column prop="amount" label="金额" width="150" sortable>
          <template #default="{ row }">
            <span class="amount-text">{{ formatAmount(row.amount, row.currency) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                :icon="Edit"
                @click="openEditDialog(row)"
                class="action-button"
              >
                编辑
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                :icon="Delete"
                @click="handleDelete(row)"
                class="action-button"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="!expensesStore.isLoading && expensesStore.expenses.length === 0" class="empty-state">
        <el-empty description="暂无消费记录" />
      </div>
    </el-card>

    <!-- 添加消费对话框 -->
    <el-dialog
      v-model="showAddDialog"
      title="添加消费记录"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="addFormRef"
        :model="expenseForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="expenseForm.amount"
            :min="0.01"
            :precision="2"
            :step="10"
            style="width: 100%"
            placeholder="请输入金额"
          />
        </el-form-item>
        
        <el-form-item label="货币" prop="currency">
          <el-select v-model="expenseForm.currency" style="width: 100%" placeholder="请选择货币">
            <el-option
              v-for="option in currencyOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="类别" prop="category">
          <el-select v-model="expenseForm.category" style="width: 100%" placeholder="请选择消费类别">
            <el-option
              v-for="option in categoryOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="说明" prop="comment">
          <el-input
            v-model="expenseForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入消费说明"
          />
        </el-form-item>
        
        <el-form-item label="日期" prop="expenseDate">
          <el-date-picker
            v-model="expenseForm.expenseDate"
            type="date"
            style="width: 100%"
            placeholder="选择消费日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd" :loading="expensesStore.isLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑消费对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑消费记录"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editFormRef"
        :model="expenseForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="expenseForm.amount"
            :min="0.01"
            :precision="2"
            :step="10"
            style="width: 100%"
            placeholder="请输入金额"
          />
        </el-form-item>
        
        <el-form-item label="货币" prop="currency">
          <el-select v-model="expenseForm.currency" style="width: 100%" placeholder="请选择货币">
            <el-option
              v-for="option in currencyOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="类别" prop="category">
          <el-select v-model="expenseForm.category" style="width: 100%" placeholder="请选择消费类别">
            <el-option
              v-for="option in categoryOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="说明" prop="comment">
          <el-input
            v-model="expenseForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入消费说明"
          />
        </el-form-item>
        
        <el-form-item label="日期" prop="expenseDate">
          <el-date-picker
            v-model="expenseForm.expenseDate"
            type="date"
            style="width: 100%"
            placeholder="选择消费日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEdit" :loading="expensesStore.isLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 预算详情对话框 -->
    <el-dialog
      v-model="showBudgetDialog"
      title="预算详情"
      width="500px"
    >
      <div v-if="expensesStore.budget" class="budget-detail">
        <el-statistic title="总预算" :value="expensesStore.budget.totalBudget">
          <template #prefix>¥</template>
        </el-statistic>
        
        <el-statistic title="已花费" :value="expensesStore.budget.spent" style="margin-top: 20px">
          <template #prefix>¥</template>
        </el-statistic>
        
        <el-statistic title="剩余预算" :value="expensesStore.budget.remaining" style="margin-top: 20px">
          <template #prefix>¥</template>
        </el-statistic>
        
        <div class="budget-status" style="margin-top: 20px">
          <el-tag 
            :type="expensesStore.budget.overspend ? 'danger' : 'success'" 
            size="large"
          >
            {{ expensesStore.budget.overspend ? '⚠️ 已超支' : '✓ 预算正常' }}
          </el-tag>
        </div>
        
        <div class="budget-progress" style="margin-top: 20px">
          <el-progress
            :percentage="Math.min(100, (expensesStore.budget.spent / expensesStore.budget.totalBudget) * 100)"
            :color="expensesStore.budget.overspend ? '#f56c6c' : '#67c23a'"
            :stroke-width="20"
          />
        </div>
      </div>
      
      <template #footer>
        <el-button type="primary" @click="showBudgetDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.expense-management {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.budget-card,
.expenses-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.amount-text {
  font-weight: 600;
  color: #409eff;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.budget-detail {
  padding: 20px 0;
}

.budget-status {
  text-align: center;
}

.budget-progress {
    margin-top: 20px;
  }

  .action-buttons {
    display: flex;
    gap: 8px;
    justify-content: center;
  }

  .action-button {
    flex-shrink: 0;
  }

  :deep(.el-statistic__head) {
    font-size: 14px;
    color: #909399;
    margin-bottom: 8px;
  }

  :deep(.el-statistic__number) {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }
</style>

