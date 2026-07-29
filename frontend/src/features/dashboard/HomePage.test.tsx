import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { HomePage } from './HomePage';

describe('HomePage', () => {
  it('renders dashboard counts and recent decks', async () => {
    server.use(
      http.get('/api/v1/folders', () =>
        HttpResponse.json([{ id: 1, name: 'English', description: null, position: 0, createdAt: '', updatedAt: '' }]),
      ),
      http.get('/api/v1/decks', () =>
        HttpResponse.json([
          {
            id: 2,
            folderId: 1,
            title: 'English Vocabulary',
            description: 'Intermediate words',
            visibility: 'PRIVATE',
            createdAt: '2026-07-01T00:00:00Z',
            updatedAt: '2026-07-14T00:00:00Z',
          },
        ]),
      ),
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
    );

    renderWithProviders(<HomePage />);

    expect(await screen.findByText('English Vocabulary')).toBeInTheDocument();
    expect(screen.getByText('Decks')).toBeInTheDocument();
    expect(screen.getByText('Folders')).toBeInTheDocument();
    expect(screen.getByText('Points')).toBeInTheDocument();
    expect(screen.getByText('Level')).toBeInTheDocument();
    expect(screen.getByText('Streak')).toBeInTheDocument();
    expect(screen.getByText('Complete a practice test')).toBeInTheDocument();
  });

  it('shows loading while dashboard data is pending', () => {
    server.use(
      http.get('/api/v1/folders', () => new Promise(() => undefined)),
      http.get('/api/v1/decks', () => new Promise(() => undefined)),
    );

    renderWithProviders(<HomePage />);

    expect(screen.getAllByLabelText('Loading dashboard').length).toBeGreaterThan(0);
    expect(screen.getByLabelText('Loading recent decks')).toBeInTheDocument();
  });

  it('shows an error state when dashboard data fails', async () => {
    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([], { status: 500 })),
      http.get('/api/v1/decks', () => HttpResponse.json([], { status: 500 })),
    );

    renderWithProviders(<HomePage />);

    expect(await screen.findByText('Could not load dashboard')).toBeInTheDocument();
  });

  it('shows an empty state when there are no recent decks', async () => {
    server.use(
      http.get('/api/v1/folders', () => HttpResponse.json([])),
      http.get('/api/v1/decks', () => HttpResponse.json([])),
    );

    renderWithProviders(<HomePage />);

    expect(await screen.findByText('No decks yet')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Open library' })).toHaveAttribute('href', '/library');
  });
});
