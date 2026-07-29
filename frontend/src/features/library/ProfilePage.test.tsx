import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { writeAuthSnapshot } from '../../auth/authStore';
import { ProfilePage } from './ProfilePage';

describe('ProfilePage', () => {
  it('renders the user profile with gamification summary', async () => {
    writeAuthSnapshot({
      accessToken: 'token',
      user: { id: 1, email: 'student@example.com', displayName: 'Ada', points: 250, level: 4, streakCount: 8 },
    });

    server.use(
      http.get('/api/v1/users/me/gamification', () =>
        HttpResponse.json({
          userId: 1,
          points: 250,
          level: 4,
          streakCount: 8,
          lastActiveAt: '2026-07-30T08:00:00Z',
          nextLevelProgress: 62,
          nextLevelRequiredPoints: 400,
        }),
      ),
      http.get('/api/v1/users/me/daily-missions', () =>
        HttpResponse.json([
          {
            key: 'learn_session_completed',
            title: 'Complete a learn session',
            description: 'Finish one learning session',
            target: 1,
            progress: 1,
            rewardPoints: 40,
            status: 'COMPLETED',
            claimedAt: null,
          },
        ]),
      ),
    );

    renderWithProviders(<ProfilePage />);

    expect(await screen.findByText('Ada')).toBeInTheDocument();
    expect(screen.getByText('student@example.com')).toBeInTheDocument();
    expect(screen.getByText('Points')).toBeInTheDocument();
    expect(screen.getByText('Level')).toBeInTheDocument();
    expect(screen.getByText('Streak')).toBeInTheDocument();
    expect(await screen.findByText('Complete a learn session')).toBeInTheDocument();
  });
});
