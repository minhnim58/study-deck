import { apiClient } from './client';
import type {
  DailyMissionResponse,
  DailyMissionStatusResponse,
  UserGamification,
} from './types';

export async function getGamification(): Promise<UserGamification> {
  const response = await apiClient.get<UserGamification>('/users/me/gamification');
  return response.data;
}

export async function getDailyMissions(): Promise<DailyMissionResponse[]> {
  const response = await apiClient.get<DailyMissionResponse[]>('/users/me/daily-missions');
  return response.data;
}

export async function claimDailyMission(missionKey: string): Promise<DailyMissionStatusResponse> {
  const response = await apiClient.post<DailyMissionStatusResponse>(
    `/users/me/daily-missions/${missionKey}/claim`,
  );
  return response.data;
}
