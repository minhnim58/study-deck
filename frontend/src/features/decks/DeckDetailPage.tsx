import { Alert, Badge, Button, Card, Group, Loader, SimpleGrid, Stack, Text, Title } from '@mantine/core';
import {
  IconAlertCircle,
  IconArrowLeft,
  IconBrain,
  IconCards,
  IconClipboardList,
  IconEdit,
  IconRepeat,
  IconSortAscending,
  IconSparkles,
} from '@tabler/icons-react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { useParams } from 'react-router-dom';
import { getDeck, getDeckSummary } from '../../api/deckApi';
import { listFlashcards } from '../../api/flashcardApi';
import { EmptyState } from '../../components/EmptyState';
import { PageHeader } from '../../components/PageHeader';
import { useAuth } from '../../auth/AuthProvider';

export function DeckDetailPage() {
  const { user } = useAuth();
  const { deckId } = useParams();
  const parsedDeckId = Number(deckId);
  const enabled = Number.isFinite(parsedDeckId);
  const deck = useQuery({ queryKey: ['deck', parsedDeckId], queryFn: () => getDeck(parsedDeckId), enabled });
  const summary = useQuery({ queryKey: ['deck-summary', parsedDeckId], queryFn: () => getDeckSummary(parsedDeckId), enabled });
  const flashcards = useQuery({ queryKey: ['flashcards', parsedDeckId], queryFn: () => listFlashcards(parsedDeckId), enabled });
  const isLoading = deck.isLoading || summary.isLoading || flashcards.isLoading;
  const isError = deck.isError || summary.isError || flashcards.isError || !enabled;
  const modes = [
    { title: 'Learn', description: 'Study with guided prompts.', href: `/decks/${parsedDeckId}/learn/options`, icon: IconBrain },
    { title: 'Practice Test', description: 'Generate a quiz from this deck.', href: `/decks/${parsedDeckId}/practice/options`, icon: IconClipboardList },
    { title: 'Match', description: 'Pair terms with definitions.', href: `/decks/${parsedDeckId}/matching/options`, icon: IconSparkles },
    { title: 'Flashcards', description: 'Flip through cards one at a time.', href: `/decks/${parsedDeckId}/flashcards/viewer`, icon: IconCards },
    { title: 'Card Sorting', description: 'Separate known from unknown cards.', href: `/decks/${parsedDeckId}/sorting`, icon: IconSortAscending },
    { title: 'Spaced Repetition', description: 'Review due cards on schedule.', href: `/decks/${parsedDeckId}/srs`, icon: IconRepeat },
  ];

  return (
    <Stack gap="xl">
      <Button component={Link} to="/library" variant="subtle" leftSection={<IconArrowLeft size={16} />} w="fit-content">
        Back to library
      </Button>

      {isError ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not load deck">
          Check that the deck exists, then try again.
        </Alert>
      ) : null}

      {isLoading ? <Loader aria-label="Loading deck" /> : null}

      {deck.data ? (
        <>
          <PageHeader
            title={deck.data.title}
            description={deck.data.description ?? 'No description'}
          />

          <Group gap="sm">
            {deck.data.visibility === 'PUBLIC' && deck.data.creatorDisplayName ? (
              <Badge variant="dot" color="blue">Created by {deck.data.creatorDisplayName}</Badge>
            ) : null}
            <Badge variant="light">{summary.data?.totalCards ?? flashcards.data?.length ?? 0} total cards</Badge>
            <Badge variant="light">{flashcards.data?.length ?? 0} loaded cards</Badge>
            <Badge variant="light">{summary.data?.starredCards ?? 0} starred</Badge>
            <Badge variant="light">{summary.data?.dueSrsCards ?? 0} due for SRS</Badge>
            <Badge variant="light">{summary.data?.newCards ?? 0} new</Badge>
            <Badge variant="light">{summary.data?.learningCards ?? 0} learning</Badge>
            <Badge variant="light">{summary.data?.reviewCards ?? 0} review</Badge>
          </Group>

          <Stack gap="md">
            <Title order={2}>Study modes</Title>
            <SimpleGrid cols={{ base: 1, sm: 2, lg: 3 }} aria-label="Study modes">
              {modes.map((mode) => {
                const Icon = mode.icon;
                return (
                  <Card key={mode.title} component={Link} to={mode.href} withBorder radius="sm">
                    <Stack gap="sm">
                      <Group gap="sm">
                        <Icon size={20} />
                        <Text fw={700}>{mode.title}</Text>
                      </Group>
                      <Text c="dimmed" size="sm">
                        {mode.description}
                      </Text>
                    </Stack>
                  </Card>
                );
              })}
            </SimpleGrid>
          </Stack>

          {flashcards.data?.length === 0 ? (
            <EmptyState
              title="No cards yet"
              description="Create flashcards before starting a study mode."
              action={
                deck.data?.creatorId === user?.id ? (
                  <Button component={Link} to={`/decks/${parsedDeckId}/flashcards/edit`} leftSection={<IconEdit size={16} />}>
                    Manage cards
                  </Button>
                ) : null
              }
            />
          ) : null}

          {flashcards.data && flashcards.data.length > 0 ? (
            <Stack gap="md">
              <Group justify="space-between">
                <Title order={2}>Cards</Title>
                {deck.data?.creatorId === user?.id ? (
                  <Button component={Link} to={`/decks/${parsedDeckId}/flashcards/edit`} variant="light" leftSection={<IconEdit size={16} />}>
                    Manage cards
                  </Button>
                ) : null}
              </Group>
              <SimpleGrid cols={{ base: 1, md: 2 }}>
                {flashcards.data.map((card, index) => (
                  <Card key={card.id} withBorder radius="sm">
                    <Stack gap="sm">
                      <Group justify="space-between" align="flex-start">
                        <Badge variant="light">#{index + 1}</Badge>
                        {card.starred ? <Badge color="yellow">Starred</Badge> : null}
                      </Group>
                      <Group align="stretch" gap="lg" wrap="nowrap">
                        <Stack gap={4} flex={1}>
                          <Text size="xs" c="dimmed" fw={700} tt="uppercase">
                            Term
                          </Text>
                          <Text fw={700}>{card.term}</Text>
                        </Stack>
                        <Stack gap={4} flex={1}>
                          <Text size="xs" c="dimmed" fw={700} tt="uppercase">
                            Definition
                          </Text>
                          <Text>{card.definition}</Text>
                        </Stack>
                      </Group>
                    </Stack>
                  </Card>
                ))}
              </SimpleGrid>
            </Stack>
          ) : null}
        </>
      ) : null}
    </Stack>
  );
}
