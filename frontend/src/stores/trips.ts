import { defineStore } from 'pinia';
import api from '@/api/axios';

// 定义数据类型
export interface TripRequest {
  title: string;
  destination: string;
  startDate: string;
  endDate: string;
  budgetTotal: number;
  companionCount?: number;
  preferences?: string[];
  request?: string;
}

export interface DayPlanDTO {
  day: number;
  locations: LocationDTO[];
}

export interface LocationDTO {
  name: string;
  lng: number;
  lat: number;
  description: string;
  type: string;
}

export interface TripResponse {
  id: string;
  userId: string;
  title: string;
  destination: string;
  startDate: string;
  endDate: string;
  budgetTotal: number;
  companionCount: number;
  preferences: string[];
  dayPlans: DayPlanDTO[];
  createdAt: string;
  updatedAt: string;
}

export const useTripsStore = defineStore('trips', {
  state: () => ({
    trips: [] as TripResponse[],
    currentTrip: null as TripResponse | null,
    isLoading: false,
    error: '',
    lastPlanId: ''
  }),

  actions: {
    async fetchTrips() {
      this.isLoading = true;
      this.error = '';
      try {
        const response = await api.get<TripResponse[]>('/trips');
        this.trips = response.data;
        return this.trips;
      } catch (error: any) {
        this.error = error.response?.data || '获取行程列表失败';
        return [];
      } finally {
        this.isLoading = false;
      }
    },

    async fetchTrip(id: string) {
      this.isLoading = true;
      this.error = '';
      try {
        const response = await api.get<TripResponse>(`/trips/${id}`);
        this.currentTrip = response.data;
        this.lastPlanId = id;
        return this.currentTrip;
      } catch (error: any) {
        this.error = error.response?.data || '获取行程详情失败';
        return null;
      } finally {
        this.isLoading = false;
      }
    },

    async createTrip(tripRequest: TripRequest) {
      this.isLoading = true;
      this.error = '';
      try {
        const response = await api.post<TripResponse>('/trips', tripRequest);
        this.trips.push(response.data);
        this.currentTrip = response.data;
        this.lastPlanId = response.data.id;
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data || '创建行程失败';
        return null;
      } finally {
        this.isLoading = false;
      }
    },

    async updateTrip(id: string, tripRequest: TripRequest) {
      this.isLoading = true;
      this.error = '';
      try {
        const response = await api.put<TripResponse>(`/trips/${id}`, tripRequest);
        
        // 更新本地状态
        const index = this.trips.findIndex(trip => trip.id === id);
        if (index !== -1) {
          this.trips[index] = response.data;
        }
        
        if (this.currentTrip?.id === id) {
          this.currentTrip = response.data;
        }
        
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data || '更新行程失败';
        return null;
      } finally {
        this.isLoading = false;
      }
    },

    async fetchLocations(tripId: string) {
      this.isLoading = true;
      this.error = '';
      try {
        const response = await api.get(`/locations/${tripId}`);
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data || '获取地点信息失败';
        return [];
      } finally {
        this.isLoading = false;
      }
    },

    clearCurrentTrip() {
      this.currentTrip = null;
    }
  }
});