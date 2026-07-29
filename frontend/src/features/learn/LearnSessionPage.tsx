import { Alert, Badge, Button, Card, Group, Loader, Stack, Text, TextInput, Title } from '@mantine/core';
import { IconAlertCircle, IconCheck, IconFlag, IconFlame, IconStar, IconX } from '@tabler/icons-react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { answerLearnSessionItem, completeLearnSession, getLearnSession } from '../../api/learnApi';
import type { LearnSessionItemResponse, LearnSessionResponse } from '../../api/types';
import { EmptyState } from '../../components/EmptyState';
import { ModeShell } from '../../components/ModeShell';

type Feedback = {
  correct: boolean;
  expected: string;
};

function normalizeAnswer(value: string) {
  return value.trim().toLocaleLowerCase();
}

export function LearnSessionPage() {
  const { sessionId } = useParams();
  const parsedSessionId = Number(sessionId);
  const enabled = Number.isFinite(parsedSessionId);
  const [session, setSession] = useState<LearnSessionResponse | null>(null);
  const [items, setItems] = useState<LearnSessionItemResponse[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answer, setAnswer] = useState('');
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const [rewardSummary, setRewardSummary] = useState<{ points: number; streak: number } | null>(null);

  const sessionQuery = useQuery({
    queryKey: ['learn-session', parsedSessionId],
    queryFn: () => getLearnSession(parsedSessionId),
    enabled,
  });

  useEffect(() => {
    if (sessionQuery.data) {
      setSession(sessionQuery.data);
      setItems(sessionQuery.data.items);
    }
  }, [sessionQuery.data]);

  const currentItem = items[currentIndex];
  const completed = session?.status === 'COMPLETED';
  const progressLabel = useMemo(() => {
    if (items.length === 0) {
      return '0 / 0';
    }

    return `${Math.min(currentIndex + 1, items.length)} / ${items.length}`;
  }, [currentIndex, items.length]);

  const answerMutation = useMutation({
    mutationFn: ({ item, submittedAnswer }: { item: LearnSessionItemResponse; submittedAnswer: string }) =>
      answerLearnSessionItem(parsedSessionId, { itemId: item.id, answer: submittedAnswer }),
    onSuccess: (response, variables) => {
      setSession((current) => ({ ...(current ?? response), ...response, items: current?.items ?? items }));
      setFeedback({
        correct: normalizeAnswer(variables.submittedAnswer) === normalizeAnswer(variables.item.answer),
        expected: variables.item.answer,
      });
    },
  });

  const completeMutation = useMutation({
    mutationFn: () => completeLearnSession(parsedSessionId),
    onSuccess: (response) => {
      setSession((current) => ({ ...(current ?? response), ...response }));
      setRewardSummary({ points: 40, streak: 1 });
    },
  });

  function checkAnswer(submittedAnswer = answer) {
    if (!currentItem || answerMutation.isPending || !submittedAnswer.trim()) {
      return;
    }

    answerMutation.mutate({ item: currentItem, submittedAnswer: submittedAnswer.trim() });
  }

  function nextItem() {
    setAnswer('');
    setFeedback(null);
    setCurrentIndex((index) => index + 1);
  }

  return (
    <ModeShell title="Learn session" description="Answer each prompt and check your recall.">
      {sessionQuery.isError || !enabled ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not load learn session">
          Refresh the page or start a new session from the deck.
        </Alert>
      ) : null}

      {sessionQuery.isLoading ? <Loader aria-label="Loading learn session" /> : null}

      {session ? (
        <Group gap="sm">
          <Badge variant="light">Progress: {progressLabel}</Badge>
          <Badge variant="light">Correct: {session.correctCount}</Badge>
          <Badge variant="light">Wrong: {session.wrongCount}</Badge>
        </Group>
      ) : null}

      {completed ? (
        <Stack gap="md">
          <EmptyState
            title="Learn complete"
            description={`Correct: ${session?.correctCount ?? 0}. Wrong: ${session?.wrongCount ?? 0}.`}
          />
          {rewardSummary ? (
            <Card withBorder radius="sm" p="md" style={{ background: 'var(--mantine-color-green-light)' }}>
              <Stack gap={4}>
                <Text fw={700}>Reward unlocked</Text>
                <Group gap="sm">
                  <Badge color="green" leftSection={<IconStar size={14} />}>
                    {rewardSummary.points} points
                  </Badge>
                  <Badge color="yellow" leftSection={<IconFlame size={14} />}>
                    {rewardSummary.streak} day streak
                  </Badge>
                </Group>
              </Stack>
            </Card>
          ) : null}
        </Stack>
      ) : null}

      {!sessionQuery.isLoading && !completed && items.length === 0 ? (
        <EmptyState title="No learn items" description="There are no cards available for this learn session." />
      ) : null}

      {!completed && currentItem ? (
        <Card withBorder radius="sm" p="xl">
          <Stack gap="lg">
            <Group justify="space-between">
              <Badge variant="light">{currentItem.questionType.replaceAll('_', ' ')}</Badge>
              <Badge variant="light">Prompt: {currentItem.promptSide.toLowerCase()}</Badge>
            </Group>

            <Stack gap="xs">
              <Text c="dimmed" size="sm">
                Prompt
              </Text>
              <Title order={2}>{currentItem.prompt}</Title>
            </Stack>

            <TextInput
              label="Your answer"
              value={answer}
              disabled={Boolean(feedback) || answerMutation.isPending}
              onChange={(event) => setAnswer(event.currentTarget.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  event.preventDefault();
                  checkAnswer();
                }
              }}
            />

            {currentItem.questionType === 'TRUE_FALSE' ? (
              <Group>
                <Button variant="light" disabled={Boolean(feedback)} onClick={() => checkAnswer('true')}>
                  True
                </Button>
                <Button variant="light" disabled={Boolean(feedback)} onClick={() => checkAnswer('false')}>
                  False
                </Button>
              </Group>
            ) : null}

            {feedback ? (
              <Alert color={feedback.correct ? 'green' : 'red'} icon={feedback.correct ? <IconCheck size={18} /> : <IconX size={18} />} title={feedback.correct ? 'Correct' : 'Incorrect'}>
                Answer: {feedback.expected}
              </Alert>
            ) : null}

            <Group justify="flex-end">
              {feedback ? (
                currentIndex + 1 >= items.length ? (
                  <Button leftSection={<IconFlag size={16} />} loading={completeMutation.isPending} onClick={() => completeMutation.mutate()}>
                    Finish session
                  </Button>
                ) : (
                  <Button onClick={nextItem}>Next item</Button>
                )
              ) : (
                <Button leftSection={<IconCheck size={16} />} loading={answerMutation.isPending} onClick={() => checkAnswer()}>
                  Check answer
                </Button>
              )}
            </Group>
          </Stack>
        </Card>
      ) : null}
    </ModeShell>
  );
}
