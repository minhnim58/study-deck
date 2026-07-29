import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../test/server';
import { claimDailyMission, getDailyMissions, getGamification } from './gamificationApi';

describe('gamificationApi', () => {
  it('fetches the gamification summary and daily mission state', async () => {
    server.use(
      http.get('/api/v1/users/me/gamification', () =>
        HttpResponse.json({
          userId: 1,
          points: 120,
          level: 3,
          streakCount: 7,
          lastActiveAt: '2026-07-30T08:00:00Z',
          nextLevelProgress: 60,
          nextLevelRequiredPoints: 200,
        }),
      ),
      http.get('/api/v1/users/me/daily-missions', () =>
        HttpResponse.json([
          {
            key: 'practice_test_completed',
            title: 'Complete a practice test',
            description: 'Finish one practice test session',
            target: 1,
            progress: 1,
            rewardPoints: 50,
            status: 'COMPLETED',
            claimedAt: null,
          },
        ]),
      ),
      http.post('/api/v1/users/me/daily-missions/practice_test_completed/claim', () =>
        HttpResponse.json({
          missionKey: 'practice_test_completed',
          status: 'CLAIMED',
          progress: 1,
          target: 1,
          claimedAt: '2026-07-30T08:15:00Z',
        }),
      ),
    );

    const summary = await getGamification();
    const missions = await getDailyMissions();
    const claim = await claimDailyMission('practice_test_completed');

    expect(summary.points).toBe(120);
    expect(summary.level).toBe(3);
    expect(missions[0].status).toBe('COMPLETED');
    expect(claim.status).toBe('CLAIMED');
  });
});
