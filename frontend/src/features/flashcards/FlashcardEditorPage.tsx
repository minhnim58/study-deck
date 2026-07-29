import { Alert, Badge, Button, Card, Group, Loader, SimpleGrid, Stack, Text } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { IconAlertCircle, IconArrowLeft, IconEdit, IconPlus, IconStar, IconStarFilled, IconTrash } from '@tabler/icons-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getDeck } from '../../api/deckApi';
import { createFlashcard, deleteFlashcard, listFlashcards, setFlashcardStarred, updateFlashcard } from '../../api/flashcardApi';
import type { CreateFlashcardRequest, FlashcardResponse, UpdateFlashcardRequest } from '../../api/types';
import { EmptyState } from '../../components/EmptyState';
import { PageHeader } from '../../components/PageHeader';
import { FlashcardFormModal } from './FlashcardFormModal';
import { ImportCardsModal } from './ImportCardsModal';
import { importFlashcards } from '../../api/deckApi';

export function FlashcardEditorPage() {
  const { deckId } = useParams();
  const queryClient = useQueryClient();
  const parsedDeckId = Number(deckId);
  const enabled = Number.isFinite(parsedDeckId);
  const deck = useQuery({ queryKey: ['deck', parsedDeckId], queryFn: () => getDeck(parsedDeckId), enabled });
  const flashcards = useQuery({ queryKey: ['flashcards', parsedDeckId], queryFn: () => listFlashcards(parsedDeckId), enabled });
  const [modalOpen, setModalOpen] = useState(false);
  const [importModalOpen, setImportModalOpen] = useState(false);
  const [editingCard, setEditingCard] = useState<FlashcardResponse | null>(null);

  function invalidateFlashcards() {
    queryClient.invalidateQueries({ queryKey: ['flashcards', parsedDeckId] });
    queryClient.invalidateQueries({ queryKey: ['deck-summary', parsedDeckId] });
  }

  const createMutation = useMutation({
    mutationFn: (request: CreateFlashcardRequest) => createFlashcard(parsedDeckId, request),
    onSuccess: () => {
      notifications.show({ color: 'green', message: 'Flashcard created' });
      setModalOpen(false);
      invalidateFlashcards();
    },
  });

  const importMutation = useMutation({
    mutationFn: (requests: CreateFlashcardRequest[]) => importFlashcards(parsedDeckId, requests),
    onSuccess: (data) => {
      notifications.show({ color: 'green', message: `Imported ${data.length} flashcards` });
      setImportModalOpen(false);
      invalidateFlashcards();
    },
    onError: (error) => {
      notifications.show({ color: 'red', message: `Could not import flashcards. Check format and try again.` });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ flashcardId, request }: { flashcardId: number; request: UpdateFlashcardRequest }) =>
      updateFlashcard(flashcardId, request),
    onSuccess: () => {
      notifications.show({ color: 'green', message: 'Flashcard updated' });
      setEditingCard(null);
      invalidateFlashcards();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteFlashcard,
    onSuccess: () => {
      notifications.show({ color: 'green', message: 'Flashcard deleted' });
      invalidateFlashcards();
    },
  });

  const starMutation = useMutation({
    mutationFn: ({ flashcardId, starred }: { flashcardId: number; starred: boolean }) => setFlashcardStarred(flashcardId, starred),
    onSuccess: () => {
      invalidateFlashcards();
    },
  });

  function handleDelete(card: FlashcardResponse) {
    if (deleteMutation.isPending) {
      return;
    }

    if (window.confirm(`Delete flashcard "${card.term}"?`)) {
      deleteMutation.mutate(card.id);
    }
  }

  const isLoading = deck.isLoading || flashcards.isLoading;
  const isError = deck.isError || flashcards.isError || !enabled;

  return (
    <Stack gap="xl">
      <Button component={Link} to={`/decks/${parsedDeckId}`} variant="subtle" leftSection={<IconArrowLeft size={16} />} w="fit-content">
        Back to deck
      </Button>

      <PageHeader
        title={deck.data ? `${deck.data.title} cards` : 'Flashcards'}
        description="Create and maintain the cards in this deck."
        actions={
          <Group gap="sm">
            <Button
              variant="light"
              onClick={() => {
                setImportModalOpen(true);
              }}
            >
              Import 
            </Button>
            <Button
              leftSection={<IconPlus size={16} />}
              onClick={() => {
                setEditingCard(null);
                setModalOpen(true);
              }}
            >
              New card
            </Button>
          </Group>
        }
      />

      {isError ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not load flashcards">
          Check that the deck exists, then try again.
        </Alert>
      ) : null}

      {isLoading ? <Loader aria-label="Loading flashcards" /> : null}

      {!isLoading && flashcards.data?.length === 0 ? (
        <EmptyState
          title="No flashcards yet"
          description="Add the first term and definition for this deck."
          action={<Button onClick={() => setModalOpen(true)}>New card</Button>}
        />
      ) : null}

      {flashcards.data && flashcards.data.length > 0 ? (
        <SimpleGrid cols={{ base: 1, md: 2 }}>
          {flashcards.data.map((card) => (
            <Card key={card.id} withBorder radius="sm">
              <Stack gap="sm">
                <Group justify="space-between" align="flex-start">
                  <Stack gap={4}>
                    <Group gap="xs">
                      <Text fw={700}>{card.term}</Text>
                      {card.starred ? <Badge variant="light">Starred</Badge> : null}
                    </Group>
                    <Text c="dimmed" size="sm">
                      {card.definition}
                    </Text>
                  </Stack>
                  <Group gap={4}>
                    <Button
                      aria-label={card.starred ? 'Unstar card' : 'Star card'}
                      variant="subtle"
                      px="xs"
                      disabled={starMutation.isPending}
                      onClick={() => starMutation.mutate({ flashcardId: card.id, starred: !card.starred })}
                    >
                      {card.starred ? <IconStarFilled size={18} /> : <IconStar size={18} />}
                    </Button>
                    <Button
                      aria-label="Edit card"
                      variant="subtle"
                      px="xs"
                      disabled={updateMutation.isPending}
                      onClick={() => {
                        setEditingCard(card);
                      }}
                    >
                      <IconEdit size={18} />
                    </Button>
                    <Button
                      aria-label="Delete card"
                      variant="subtle"
                      color="red"
                      px="xs"
                      disabled={deleteMutation.isPending}
                      onClick={() => handleDelete(card)}
                    >
                      <IconTrash size={18} />
                    </Button>
                  </Group>
                </Group>
                {card.termImageUrl ? (
                  <Text size="xs" c="dimmed">
                    Term image: {card.termImageUrl}
                  </Text>
                ) : null}
                {card.definitionImageUrl ? (
                  <Text size="xs" c="dimmed">
                    Definition image: {card.definitionImageUrl}
                  </Text>
                ) : null}
              </Stack>
            </Card>
          ))}
        </SimpleGrid>
      ) : null}

      <FlashcardFormModal
        opened={modalOpen || Boolean(editingCard)}
        onClose={() => {
          setModalOpen(false);
          setEditingCard(null);
        }}
        flashcard={editingCard}
        loading={createMutation.isPending || updateMutation.isPending}
        onSubmit={(values) => {
          if (editingCard) {
            updateMutation.mutate({ flashcardId: editingCard.id, request: values });
          } else {
            createMutation.mutate(values);
          }
        }}
      />
      <ImportCardsModal
        opened={importModalOpen}
        onClose={() => setImportModalOpen(false)}
        loading={importMutation.isPending}
        onSubmit={(cards) => importMutation.mutate(cards)}
      />
    </Stack>
  );
}
