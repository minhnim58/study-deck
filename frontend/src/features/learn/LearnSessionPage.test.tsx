import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { Route, Routes } from 'react-router-dom';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { LearnSessionPage } from './LearnSessionPage';

describe('LearnSessionPage', () => {
  it('shows a reward summary after completing a learn session', async () => {
    server.use(
      http.get('/api/v1/learn-sessions/7', () =>
        HttpResponse.json({
          id: 7,
          status: 'ACTIVE',
          totalItems: 1,
          correctCount: 0,
          wrongCount: 0,
          items: [
            {
              id: 11,
              flashcardId: 101,
              questionType: 'WRITTEN',
              promptSide: 'TERM',
              prompt: 'What is 2 + 2?',
              answer: '4',
              attempts: 1,
            },
          ],
        }),
      ),
      http.post('/api/v1/learn-sessions/7/answers', () =>
        HttpResponse.json({
          id: 7,
          status: 'ACTIVE',
          totalItems: 1,
          correctCount: 1,
          wrongCount: 0,
          items: [],
        }),
      ),
      http.post('/api/v1/learn-sessions/7/complete', () =>
        HttpResponse.json({
          id: 7,
          status: 'COMPLETED',
          totalItems: 1,
          correctCount: 1,
          wrongCount: 0,
          items: [],
        }),
      ),
    );

    renderWithProviders(
      <Routes>
        <Route path="/learn-sessions/:sessionId" element={<LearnSessionPage />} />
      </Routes>,
      { initialEntries: ['/learn-sessions/7'] },
    );

    await screen.findByText('What is 2 + 2?');
    await userEvent.type(screen.getByLabelText('Your answer'), '4');
    await userEvent.click(screen.getByRole('button', { name: 'Check answer' }));
    await userEvent.click(screen.getByRole('button', { name: 'Finish session' }));

    expect(await screen.findByText('Reward unlocked')).toBeInTheDocument();
    expect(screen.getByText(/40 points/i)).toBeInTheDocument();
  });
});
