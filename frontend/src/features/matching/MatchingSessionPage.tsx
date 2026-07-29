import { Alert, Badge, Button, Card, Group, Loader, SimpleGrid, Stack, Text, Title } from '@mantine/core';
import { IconAlertCircle, IconFlag, IconFlame, IconStar } from '@tabler/icons-react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { completeMatchingSession, getMatchingSession, matchMatchingItem } from '../../api/matchingApi';
import type { MatchingSessionItemResponse, MatchingSessionResponse } from '../../api/types';
import { EmptyState } from '../../components/EmptyState';
import { ModeShell } from '../../components/ModeShell';

type Selection = {
  kind: 'term' | 'definition';
  itemId: number;
};

export function MatchingSessionPage() {
  const { sessionId } = useParams();
  const parsedSessionId = Number(sessionId);
  const enabled = Number.isFinite(parsedSessionId);
  const [session, setSession] = useState<MatchingSessionResponse | null>(null);
  const [selection, setSelection] = useState<Selection | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [rewardSummary, setRewardSummary] = useState<{ points: number; streak: number } | null>(null);

  const sessionQuery = useQuery({
    queryKey: ['matching-session', parsedSessionId],
    queryFn: () => getMatchingSession(parsedSessionId),
    enabled,
  });

  useEffect(() => {
    if (sessionQuery.data) {
      setSession(sessionQuery.data);
    }
  }, [sessionQuery.data]);

  const items = session?.items ?? [];
  const unmatchedItems = useMemo(() => items.filter((item) => !item.matched), [items]);
  const definitions = useMemo(() => [...unmatchedItems].reverse(), [unmatchedItems]);
  const completed = session?.status === 'COMPLETED';

  const matchMutation = useMutation({
    mutationFn: (item: MatchingSessionItemResponse) => matchMatchingItem(parsedSessionId, { itemId: item.id }),
    onSuccess: (response) => {
      setSession(response);
      setSelection(null);
      setMessage('Matched');
      if (response.status === 'COMPLETED') {
        setRewardSummary({ points: 10 + response.matchedCount * 2, streak: 1 });
      }
    },
  });

  const completeMutation = useMutation({
    mutationFn: () => completeMatchingSession(parsedSessionId),
    onSuccess: (response) => {
      setSession(response);
      setRewardSummary({ points: 10 + response.matchedCount * 2, streak: 1 });
    },
  });

  function selectTile(nextSelection: Selection) {
    if (matchMutation.isPending) {
      return;
    }

    if (!selection || selection.kind === nextSelection.kind) {
      setSelection(nextSelection);
      setMessage(null);
      return;
    }

    const matchedItem = unmatchedItems.find((item) => item.id === nextSelection.itemId && item.id === selection.itemId);

    if (matchedItem) {
      matchMutation.mutate(matchedItem);
      return;
    }

    setMessage('Not a match');
    setSelection(null);
  }

  function isSelected(kind: Selection['kind'], itemId: number) {
    return selection?.kind === kind && selection.itemId === itemId;
  }

  function selectedTileStyles(selected: boolean) {
    return {
      cursor: matchMutation.isPending ? 'not-allowed' : 'pointer',
      borderColor: selected ? 'var(--mantine-color-blue-6)' : undefined,
      backgroundColor: selected ? 'var(--mantine-color-blue-light)' : undefined,
      boxShadow: selected ? 'inset 0 0 0 2px var(--mantine-color-blue-6)' : undefined,
    };
  }

  return (
    <ModeShell title="Matching session" description="Select one term and one definition to make a pair.">
      {sessionQuery.isError || !enabled ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not load matching session">
          Refresh the page or start a new session from the deck.
        </Alert>
      ) : null}

      {sessionQuery.isLoading ? <Loader aria-label="Loading matching session" /> : null}

      {session ? (
        <Group gap="sm">
          <Badge variant="light">Matched: {session.matchedCount} / {session.cardCount}</Badge>
          <Badge variant="light">{Math.round(session.durationMs / 1000)}s</Badge>
        </Group>
      ) : null}

      {message ? (
        <Alert color={message === 'Matched' ? 'green' : 'red'} title={message}>
          Keep pairing the remaining cards.
        </Alert>
      ) : null}

      {completed ? (
        <Stack gap="md">
          <EmptyState title="Matching complete" description={`Matched ${session?.matchedCount ?? 0} cards.`} />
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
        <EmptyState title="No matching cards" description="There are no cards available for this matching session." />
      ) : null}

      {!completed && items.length > 0 ? (
        <Stack gap="lg">
          {unmatchedItems.length === 0 ? (
            <EmptyState
              title="All pairs matched"
              description="Complete the session to save the result."
              action={
                <Button leftSection={<IconFlag size={16} />} loading={completeMutation.isPending} onClick={() => completeMutation.mutate()}>
                  Complete session
                </Button>
              }
            />
          ) : (
            <SimpleGrid cols={{ base: 1, md: 2 }}>
              <Stack gap="sm">
                <Title order={3}>Terms</Title>
                {unmatchedItems.map((item) => (
                  <Card
                    key={item.id}
                    component="button"
                    withBorder
                    radius="sm"
                    p="md"
                    aria-label={`Term: ${item.term}`}
                    aria-pressed={isSelected('term', item.id)}
                    data-selected={isSelected('term', item.id) || undefined}
                    disabled={matchMutation.isPending}
                    style={selectedTileStyles(isSelected('term', item.id))}
                    onClick={() => selectTile({ kind: 'term', itemId: item.id })}
                  >
                    <Text fw={700}>{item.term}</Text>
                  </Card>
                ))}
              </Stack>
              <Stack gap="sm">
                <Title order={3}>Definitions</Title>
                {definitions.map((item) => (
                  <Card
                    key={item.id}
                    component="button"
                    withBorder
                    radius="sm"
                    p="md"
                    aria-label={`Definition: ${item.definition}`}
                    aria-pressed={isSelected('definition', item.id)}
                    data-selected={isSelected('definition', item.id) || undefined}
                    disabled={matchMutation.isPending}
                    style={selectedTileStyles(isSelected('definition', item.id))}
                    onClick={() => selectTile({ kind: 'definition', itemId: item.id })}
                  >
                    <Text>{item.definition}</Text>
                  </Card>
                ))}
              </Stack>
            </SimpleGrid>
          )}
        </Stack>
      ) : null}
    </ModeShell>
  );
}
