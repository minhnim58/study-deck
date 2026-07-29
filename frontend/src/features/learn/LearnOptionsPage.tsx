import { Alert, Button, Checkbox, Group, NumberInput, Paper, Stack, Title } from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconAlertCircle, IconArrowLeft, IconPlayerPlay } from '@tabler/icons-react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getDeck } from '../../api/deckApi';
import { createLearnSession } from '../../api/learnApi';
import type { CreateLearnSessionRequest } from '../../api/types';
import { ModeShell } from '../../components/ModeShell';

export function LearnOptionsPage() {
  const { deckId } = useParams();
  const parsedDeckId = Number(deckId);
  const navigate = useNavigate();
  const form = useForm<CreateLearnSessionRequest>({
    initialValues: {
      lengthOfRounds: 10,
      flashcards: false,
      multipleChoice: true,
      written: false,
      trueFalse: false,
      starredOnly: false,
      shuffleTerms: true,
    },
    validate: {
      lengthOfRounds: (value) => (value < 1 ? 'Choose at least one item' : null),
    },
  });
  const deckQuery = useQuery({
    queryKey: ['deck', parsedDeckId],
    queryFn: () => getDeck(parsedDeckId),
    enabled: Number.isFinite(parsedDeckId),
  });

  useEffect(() => {
    if (deckQuery.data?.totalCards) {
      form.setFieldValue('lengthOfRounds', deckQuery.data.totalCards);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [deckQuery.data?.totalCards]);

  const startSession = useMutation({
    mutationFn: (values: CreateLearnSessionRequest) => createLearnSession(parsedDeckId, values),
    onSuccess: (session) => navigate(`/learn-sessions/${session.id}`),
  });
  const disabled = startSession.isPending || !Number.isFinite(parsedDeckId);

  return (
    <ModeShell title="Learn options" description="Build a guided round from this deck.">
      <Button component={Link} to={`/decks/${parsedDeckId}`} variant="subtle" leftSection={<IconArrowLeft size={16} />} w="fit-content">
        Back to deck
      </Button>

      {startSession.isError || !Number.isFinite(parsedDeckId) ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not start learning">
          Check that the deck has cards, then try again.
        </Alert>
      ) : null}

      <Paper withBorder radius="sm" p="lg">
        <form onSubmit={form.onSubmit((values) => startSession.mutate(values))}>
          <Stack gap="lg">
            <NumberInput
              label="Round length"
              min={1}
              max={deckQuery.data?.totalCards ?? 100}
              allowDecimal={false}
              {...form.getInputProps('lengthOfRounds')}
            />

            <Stack gap="xs">
              <Title order={3}>Question types</Title>
              <Checkbox label="Multiple choice" {...form.getInputProps('multipleChoice', { type: 'checkbox' })} />
              <Checkbox label="Written" {...form.getInputProps('written', { type: 'checkbox' })} />
              <Checkbox label="True/false" {...form.getInputProps('trueFalse', { type: 'checkbox' })} />
            </Stack>

            <Stack gap="xs">
              <Title order={3}>Card selection</Title>
              <Checkbox label="Starred only" {...form.getInputProps('starredOnly', { type: 'checkbox' })} />
              <Checkbox label="Shuffle terms" {...form.getInputProps('shuffleTerms', { type: 'checkbox' })} />
            </Stack>

            <Group justify="flex-end">
              <Button type="submit" leftSection={<IconPlayerPlay size={16} />} loading={startSession.isPending} disabled={disabled}>
                Start learning
              </Button>
            </Group>
          </Stack>
        </form>
      </Paper>
    </ModeShell>
  );
}
