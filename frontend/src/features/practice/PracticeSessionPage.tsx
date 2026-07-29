import { Alert, Badge, Button, Card, Group, Loader, Stack, Text, TextInput, Title } from '@mantine/core';
import { IconAlertCircle, IconCheck, IconClipboardCheck, IconFlame, IconStar } from '@tabler/icons-react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import { answerPracticeQuestion, getPracticeTest, submitPracticeTest } from '../../api/practiceApi';
import type { PracticeTestResponse } from '../../api/types';
import { EmptyState } from '../../components/EmptyState';
import { ModeShell } from '../../components/ModeShell';

type SaveAnswerVariables = {
  questionId: number;
  answer: string;
  advance: boolean;
};

export function PracticeSessionPage() {
  const { practiceTestId } = useParams();
  const parsedPracticeTestId = Number(practiceTestId);
  const enabled = Number.isFinite(parsedPracticeTestId);
  const [test, setTest] = useState<PracticeTestResponse | null>(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answer, setAnswer] = useState('');
  const [rewardSummary, setRewardSummary] = useState<{ points: number; streak: number } | null>(null);
  const actionInFlight = useRef(false);

  const testQuery = useQuery({
    queryKey: ['practice-test', parsedPracticeTestId],
    queryFn: () => getPracticeTest(parsedPracticeTestId),
    enabled,
  });

  useEffect(() => {
    if (testQuery.data) {
      setTest(testQuery.data);
      setAnswer(testQuery.data.questions[0]?.submittedAnswer ?? '');
    }
  }, [testQuery.data]);

  const questions = test?.questions ?? [];
  const currentQuestion = questions[currentIndex];
  const submitted = test?.status === 'SUBMITTED';
  const progress = useMemo(() => `${test?.answeredCount ?? 0} / ${test?.questionCount ?? questions.length}`, [questions.length, test]);

  const answerMutation = useMutation({
    mutationFn: ({ questionId, answer: nextAnswer }: SaveAnswerVariables) =>
      answerPracticeQuestion(parsedPracticeTestId, { questionId, answer: nextAnswer }),
    onSuccess: (response, variables) => {
      setTest(response);
      const savedIndex = response.questions.findIndex((question) => question.id === variables.questionId);
      const baseIndex = savedIndex >= 0 ? savedIndex : 0;
      const nextIndex = variables.advance ? Math.min(baseIndex + 1, Math.max(response.questions.length - 1, 0)) : baseIndex;
      setCurrentIndex(nextIndex);
      setAnswer(response.questions[nextIndex]?.submittedAnswer ?? '');
    },
  });

  const submitMutation = useMutation({
    mutationFn: () => submitPracticeTest(parsedPracticeTestId),
    onSuccess: (response) => {
      setTest(response);
      setRewardSummary({ points: response.scorePercent >= 50 ? 50 : 25, streak: 1 });
    },
  });

  const controlsDisabled = answerMutation.isPending || submitMutation.isPending || actionInFlight.current;

  async function saveCurrentAnswer(advance: boolean) {
    if (actionInFlight.current || !currentQuestion || submitted) {
      return;
    }

    const trimmedAnswer = answer.trim();
    if (!trimmedAnswer) {
      return;
    }

    actionInFlight.current = true;
    try {
      await answerMutation.mutateAsync({ questionId: currentQuestion.id, answer: trimmedAnswer, advance });
    } finally {
      actionInFlight.current = false;
    }
  }

  async function submitTest() {
    if (actionInFlight.current || submitted) {
      return;
    }

    actionInFlight.current = true;
    try {
      const trimmedAnswer = answer.trim();
      const savedAnswer = currentQuestion?.submittedAnswer?.trim() ?? '';

      if (currentQuestion && trimmedAnswer && trimmedAnswer !== savedAnswer) {
        await answerMutation.mutateAsync({ questionId: currentQuestion.id, answer: trimmedAnswer, advance: false });
      }

      await submitMutation.mutateAsync();
    } finally {
      actionInFlight.current = false;
    }
  }

  function selectQuestion(index: number) {
    if (actionInFlight.current || controlsDisabled) {
      return;
    }

    setCurrentIndex(index);
    setAnswer(questions[index]?.submittedAnswer ?? '');
  }

  return (
    <ModeShell title="Practice test" description="Answer each question, then submit for a score.">
      {testQuery.isError || !enabled ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not load practice test">
          Refresh the page or start a new test from the deck.
        </Alert>
      ) : null}

      {testQuery.isLoading ? <Loader aria-label="Loading practice test" /> : null}

      {test ? (
        <Group gap="sm">
          <Badge variant="light">Answered: {progress}</Badge>
          <Badge variant="light">Status: {test.status.toLowerCase()}</Badge>
        </Group>
      ) : null}

      {!testQuery.isLoading && questions.length === 0 ? (
        <EmptyState title="No practice questions" description="There are no questions available for this practice test." />
      ) : null}

      {submitted ? (
        <Stack gap="md">
          <Title order={2}>Score: {Math.round(test?.scorePercent ?? 0)}%</Title>
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
          {questions.map((question, index) => (
            <Card key={question.id} withBorder radius="sm">
              <Stack gap="xs">
                <Group justify="space-between">
                  <Text fw={700}>Question {index + 1}</Text>
                  <Badge color={question.correct ? 'green' : 'red'}>{question.correct ? 'Correct' : 'Incorrect'}</Badge>
                </Group>
                <Text>{question.prompt}</Text>
                <Text c="dimmed" size="sm">
                  Your answer: {question.submittedAnswer || 'No answer'}
                </Text>
              </Stack>
            </Card>
          ))}
        </Stack>
      ) : null}

      {!submitted && currentQuestion ? (
        <Card withBorder radius="sm" p="xl">
          <Stack gap="lg">
            <Group justify="space-between">
              <Badge variant="light">{currentQuestion.questionType.replaceAll('_', ' ')}</Badge>
              <Badge variant="light">Question {currentIndex + 1} of {questions.length}</Badge>
            </Group>

            <Stack gap="xs">
              <Text c="dimmed" size="sm">
                Prompt
              </Text>
              <Title order={2}>{currentQuestion.prompt}</Title>
            </Stack>

            <TextInput
              label={`Answer for question ${currentIndex + 1}`}
              value={answer}
              disabled={controlsDisabled}
              onChange={(event) => setAnswer(event.currentTarget.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  event.preventDefault();
                  void saveCurrentAnswer(true);
                }
              }}
            />

            <Group justify="space-between">
              <Group gap="xs">
                {questions.map((question, index) => (
                  <Button
                    key={question.id}
                    size="xs"
                    variant={index === currentIndex ? 'filled' : 'light'}
                    disabled={controlsDisabled}
                    onClick={() => selectQuestion(index)}
                  >
                    {index + 1}
                  </Button>
                ))}
              </Group>
              <Group>
                <Button
                  variant="light"
                  leftSection={<IconCheck size={16} />}
                  loading={answerMutation.isPending}
                  disabled={controlsDisabled || !answer.trim()}
                  onClick={() => void saveCurrentAnswer(true)}
                >
                  Save answer
                </Button>
                <Button
                  leftSection={<IconClipboardCheck size={16} />}
                  loading={submitMutation.isPending}
                  disabled={controlsDisabled}
                  onClick={() => void submitTest()}
                >
                  Submit test
                </Button>
              </Group>
            </Group>
          </Stack>
        </Card>
      ) : null}
    </ModeShell>
  );
}
