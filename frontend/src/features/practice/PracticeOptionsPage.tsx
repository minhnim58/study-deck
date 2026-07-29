import { Alert, Button, Checkbox, Group, NumberInput, Paper, Stack, Title } from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconAlertCircle, IconArrowLeft, IconPlayerPlay } from '@tabler/icons-react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getDeck } from '../../api/deckApi';
import { createPracticeTest } from '../../api/practiceApi';
import type { CreatePracticeTestRequest } from '../../api/types';
import { ModeShell } from '../../components/ModeShell';

export function PracticeOptionsPage() {
  const { deckId } = useParams();
  const parsedDeckId = Number(deckId);
  const navigate = useNavigate();
  const form = useForm<CreatePracticeTestRequest>({
    initialValues: {
      questionCount: 10,
      multipleChoice: true,
      written: true,
      trueFalse: false,
      starredOnly: false,
      answerWithTerm: true,
      answerWithDefinition: true,
    },
    validate: {
      questionCount: (value) => (value < 1 ? 'Choose at least one question' : null),
    },
  });
  const deckQuery = useQuery({
    queryKey: ['deck', parsedDeckId],
    queryFn: () => getDeck(parsedDeckId),
    enabled: Number.isFinite(parsedDeckId),
  });

  useEffect(() => {
    if (deckQuery.data?.totalCards) {
      form.setFieldValue('questionCount', deckQuery.data.totalCards);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [deckQuery.data?.totalCards]);

  const startTest = useMutation({
    mutationFn: (values: CreatePracticeTestRequest) => createPracticeTest(parsedDeckId, values),
    onSuccess: (test) => navigate(`/practice-tests/${test.id}`),
  });
  const disabled = startTest.isPending || !Number.isFinite(parsedDeckId);

  return (
    <ModeShell title="Practice options" description="Generate a test from this deck.">
      <Button component={Link} to={`/decks/${parsedDeckId}`} variant="subtle" leftSection={<IconArrowLeft size={16} />} w="fit-content">
        Back to deck
      </Button>

      {startTest.isError || !Number.isFinite(parsedDeckId) ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not start test">
          Check that the deck has cards, then try again.
        </Alert>
      ) : null}

      <Paper withBorder radius="sm" p="lg">
        <form onSubmit={form.onSubmit((values) => startTest.mutate(values))}>
          <Stack gap="lg">
            <NumberInput
              label="Question count"
              min={1}
              max={deckQuery.data?.totalCards ?? 100}
              allowDecimal={false}
              {...form.getInputProps('questionCount')}
            />

            <Stack gap="xs">
              <Title order={3}>Question types</Title>
              <Checkbox label="Multiple choice" {...form.getInputProps('multipleChoice', { type: 'checkbox' })} />
              <Checkbox label="Written" {...form.getInputProps('written', { type: 'checkbox' })} />
              <Checkbox label="True/false" {...form.getInputProps('trueFalse', { type: 'checkbox' })} />
            </Stack>

            <Stack gap="xs">
              <Title order={3}>Answers</Title>
              <Checkbox label="Answer with term" {...form.getInputProps('answerWithTerm', { type: 'checkbox' })} />
              <Checkbox label="Answer with definition" {...form.getInputProps('answerWithDefinition', { type: 'checkbox' })} />
              <Checkbox label="Starred only" {...form.getInputProps('starredOnly', { type: 'checkbox' })} />
            </Stack>

            <Group justify="flex-end">
              <Button type="submit" leftSection={<IconPlayerPlay size={16} />} loading={startTest.isPending} disabled={disabled}>
                Start test
              </Button>
            </Group>
          </Stack>
        </form>
      </Paper>
    </ModeShell>
  );
}
