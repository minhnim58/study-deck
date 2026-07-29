import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { Route, Routes } from 'react-router-dom';
import { server } from '../../test/server';
import { renderWithProviders } from '../../test/render';
import { PracticeSessionPage } from './PracticeSessionPage';

describe('PracticeSessionPage', () => {
  it('shows a reward summary after submitting a practice test', async () => {
    server.use(
      http.get('/api/v1/practice-tests/1', () =>
        HttpResponse.json({
          id: 1,
          status: 'ACTIVE',
          questionCount: 1,
          answeredCount: 0,
          scorePercent: 0,
          questions: [
            {
              id: 10,
              flashcardId: 99,
              questionType: 'WRITTEN',
              promptSide: 'TERM',
              prompt: 'What is the capital of France?',
              submittedAnswer: null,
              correct: null,
            },
          ],
        }),
      ),
      http.post('/api/v1/practice-tests/1/submit', () =>
        HttpResponse.json({
          id: 1,
          status: 'SUBMITTED',
          questionCount: 1,
          answeredCount: 1,
          scorePercent: 100,
          questions: [
            {
              id: 10,
              flashcardId: 99,
              questionType: 'WRITTEN',
              promptSide: 'TERM',
              prompt: 'What is the capital of France?',
              submittedAnswer: 'Paris',
              correct: true,
            },
          ],
        }),
      ),
    );

    renderWithProviders(
      <Routes>
        <Route path="/practice-tests/:practiceTestId" element={<PracticeSessionPage />} />
      </Routes>,
      { initialEntries: ['/practice-tests/1'] },
    );

    await screen.findByText('What is the capital of France?');
    await userEvent.click(screen.getByRole('button', { name: 'Submit test' }));

    expect(await screen.findByText('Reward unlocked')).toBeInTheDocument();
    expect(screen.getByText(/50 points/i)).toBeInTheDocument();
  });
});
