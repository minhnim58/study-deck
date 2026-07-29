import { Alert, Badge, Button, Card, Group, Loader, Stack, Text, Title } from '@mantine/core';
import { IconAlertCircle, IconEye, IconStar } from '@tabler/icons-react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getSrsDueCards, reviewFlashcard } from '../../api/srsApi';
import type { SrsDueCardResponse, SrsRating } from '../../api/types';
import { EmptyState } from '../../components/EmptyState';
import { ModeShell } from '../../components/ModeShell';

const ratingLabels: { rating: SrsRating; label: string; color: string }[] = [
  { rating: 'AGAIN', label: 'Again', color: 'red' },
  { rating: 'HARD', label: 'Hard', color: 'orange' },
  { rating: 'GOOD', label: 'Good', color: 'green' },
  { rating: 'EASY', label: 'Easy', color: 'blue' },
];

export function SrsReviewPage() {
  const { deckId } = useParams();
  const parsedDeckId = Number(deckId);
  const enabled = Number.isFinite(parsedDeckId);
  const [cards, setCards] = useState<SrsDueCardResponse[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answerVisible, setAnswerVisible] = useState(false);
  const [reviewedCount, setReviewedCount] = useState(0);
  const cardStartedAtRef = useRef(Date.now());

  const dueCards = useQuery({
    queryKey: ['srs-due', parsedDeckId],
    queryFn: () => getSrsDueCards(parsedDeckId),
    enabled,
  });

  useEffect(() => {
    if (dueCards.data) {
      setCards(dueCards.data);
      setCurrentIndex(0);
      setAnswerVisible(false);
      setReviewedCount(0);
    }
  }, [dueCards.data]);

  const currentCard = cards[currentIndex];
  const reviewComplete = dueCards.isSuccess && cards.length > 0 && currentIndex >= cards.length;

  useEffect(() => {
    if (currentCard) {
      cardStartedAtRef.current = Date.now();
    }
  }, [currentCard?.flashcardId]);

  const reviewMutation = useMutation({
    mutationFn: (rating: SrsRating) => {
      if (!currentCard) {
        throw new Error('No due card selected');
      }

      const durationMs = Math.max(0, Date.now() - cardStartedAtRef.current);

      return reviewFlashcard(currentCard.flashcardId, { rating, durationMs });
    },
    onSuccess: () => {
      cardStartedAtRef.current = Date.now();
      setReviewedCount((count) => count + 1);
      setCurrentIndex((index) => index + 1);
      setAnswerVisible(false);
    },
  });

  return (
    <ModeShell title="SRS review" description="Recall the answer, reveal it, then rate your memory.">
      {dueCards.isError || !enabled ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not load due cards">
          Refresh the page or try again later.
        </Alert>
      ) : null}

      {dueCards.isLoading ? <Loader aria-label="Loading due cards" /> : null}

      {dueCards.isSuccess && cards.length === 0 ? (
        <EmptyState title="No cards due" description="This deck has no cards scheduled for review right now." />
      ) : null}

      {reviewComplete ? (
        <Stack gap="md">
          <EmptyState title="Review complete" description={`Reviewed ${reviewedCount} cards.`} />
          {reviewedCount > 0 ? (
            <Card withBorder radius="sm" p="md" style={{ background: 'var(--mantine-color-green-light)' }}>
              <Stack gap={4}>
                <Text fw={700}>Reward unlocked</Text>
                <Group gap="sm">
                  <Badge color="green" leftSection={<IconStar size={14} />}>
                    {reviewedCount * 2} points
                  </Badge>
                </Group>
              </Stack>
            </Card>
          ) : null}
        </Stack>
      ) : null}

      {currentCard && currentIndex < cards.length ? (
        <Card withBorder radius="sm" p="xl">
          <Stack gap="lg">
            <Group justify="space-between">
              <Badge variant="light">
                Card {currentIndex + 1} of {cards.length}
              </Badge>
              <Badge variant="light">{currentCard.state.toLowerCase()}</Badge>
            </Group>

            <Stack gap="xs">
              <Text c="dimmed" size="sm">
                Term
              </Text>
              <Title order={2}>{currentCard.term}</Title>
            </Stack>

            {answerVisible ? (
              <Stack gap="xs">
                <Text c="dimmed" size="sm">
                  Definition
                </Text>
                <Text size="lg">{currentCard.definition}</Text>
              </Stack>
            ) : null}

            {!answerVisible ? (
              <Button leftSection={<IconEye size={16} />} onClick={() => setAnswerVisible(true)}>
                Show answer
              </Button>
            ) : (
              <Group grow>
                {ratingLabels.map((rating) => (
                  <Button
                    key={rating.rating}
                    color={rating.color}
                    variant="light"
                    loading={reviewMutation.isPending}
                    disabled={reviewMutation.isPending}
                    onClick={() => reviewMutation.mutate(rating.rating)}
                  >
                    {rating.label}
                  </Button>
                ))}
              </Group>
            )}
          </Stack>
        </Card>
      ) : null}
    </ModeShell>
  );
}
