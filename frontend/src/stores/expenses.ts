import { defineStore } from 'pinia';
import api from '@/api/axios';

export interface ExpenseResponse {
  id: string;
  userId: string;
  tripId: string;
  comment: string;
  amount: number;
  currency: string;
  category: string;
  expenseDate: string;
}

export interface BudgetResponse {
  totalBudget: number;
  spent: number;
  remaining: number;
  overspend: boolean;
}

export interface ExpenseRequest {
  amount: number;
  currency: string;
  comment: string;
  category: string;
  expenseDate: string;
}

export const useExpensesStore = defineStore('expenses', {
  state: () => ({
    expenses: [] as ExpenseResponse[],
    budget: null as BudgetResponse | null,
    isLoading: false,
    error: ''
  }),

  actions: {
    // 获取行程的所有消费记录
    async fetchExpenses(tripId: string) {
      this.isLoading = true;
      this.error = '';
      try {
        const response = await api.get<ExpenseResponse[]>(`/expenses/${tripId}`);
        this.expenses = response.data;
        return this.expenses;
      } catch (error: any) {
        this.error = error.response?.data || '获取消费记录失败';
        return [];
      } finally {
        this.isLoading = false;
      }
    },

    // 获取预算状态
    async fetchBudget(tripId: string) {
      this.isLoading = true;
      this.error = '';
      try {
        const response = await api.get<BudgetResponse>(`/budget/${tripId}`);
        this.budget = response.data;
        return this.budget;
      } catch (error: any) {
        this.error = error.response?.data || '获取预算状态失败';
        return null;
      } finally {
        this.isLoading = false;
      }
    },

    // 添加单个消费记录
    async addExpense(tripId: string, expense: ExpenseRequest) {
      this.isLoading = true;
      this.error = '';
      try {
        const response = await api.post<ExpenseResponse>(`/expenses/${tripId}`, expense);
        this.expenses.push(response.data);
        // 重新获取预算状态
        await this.fetchBudget(tripId);
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data || '添加消费记录失败';
        return null;
      } finally {
        this.isLoading = false;
      }
    },

    // 更新消费记录
    async updateExpense(tripId: string, expenseId: string, expense: Partial<ExpenseRequest>) {
      this.isLoading = true;
      this.error = '';
      try {
        const response = await api.put<ExpenseResponse>(`/expenses/${tripId}/expense/${expenseId}`, expense);
        const index = this.expenses.findIndex(e => e.id === expenseId);
        if (index !== -1) {
          this.expenses[index] = response.data;
        }
        // 重新获取预算状态
        await this.fetchBudget(tripId);
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data || '更新消费记录失败';
        return null;
      } finally {
        this.isLoading = false;
      }
    },

    // 删除消费记录
    async deleteExpense(tripId: string, expenseId: string) {
      this.isLoading = true;
      this.error = '';
      try {
        await api.delete(`/expenses/${tripId}/expense/${expenseId}`);
        this.expenses = this.expenses.filter(e => e.id !== expenseId);
        // 重新获取预算状态
        await this.fetchBudget(tripId);
        return true;
      } catch (error: any) {
        this.error = error.response?.data || '删除消费记录失败';
        return false;
      } finally {
        this.isLoading = false;
      }
    },

    // 批量添加消费记录
    async addExpensesBatch(tripId: string, expenses: ExpenseRequest[]) {
      this.isLoading = true;
      this.error = '';
      try {
        const response = await api.post<ExpenseResponse[]>(`/expenses/batch/${tripId}`, expenses);
        this.expenses.push(...response.data);
        // 重新获取预算状态
        await this.fetchBudget(tripId);
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data || '批量添加消费记录失败';
        return [];
      } finally {
        this.isLoading = false;
      }
    },

    // 清除当前数据
    clearData() {
      this.expenses = [];
      this.budget = null;
      this.error = '';
    }
  }
});

