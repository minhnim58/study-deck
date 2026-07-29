import { Alert, Button, Card, Group, Loader, SimpleGrid, Stack, Text, Title } from '@mantine/core';
import { IconAlertCircle, IconCards, IconFlame, IconFolder, IconPlus, IconStar, IconTargetArrow } from '@tabler/icons-react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { listDecks, listFolders } from '../../api/deckApi';
import { getDailyMissions, getGamification } from '../../api/gamificationApi';
import { DailyMissionList } from '../../components/DailyMissionList';
import { EmptyState } from '../../components/EmptyState';
import { GamificationCard } from '../../components/GamificationCard';
import { PageHeader } from '../../components/PageHeader';
import { StatCard } from '../../components/StatCard';

export function HomePage() {
  const folders = useQuery({ queryKey: ['folders'], queryFn: listFolders });
  const decks = useQuery({ queryKey: ['decks'], queryFn: listDecks });
  const gamification = useQuery({ queryKey: ['gamification'], queryFn: getGamification });
  const missions = useQuery({ queryKey: ['daily-missions'], queryFn: getDailyMissions });
  const isLoading = folders.isLoading || decks.isLoading || gamification.isLoading || missions.isLoading;
  const isError = folders.isError || decks.isError || gamification.isError || missions.isError;
  const recentDecks = [...(decks.data ?? [])]
    .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
    .slice(0, 4);

  return (
    <Stack gap="xl">
      <PageHeader
        title="Home"
        description="Pick up where you left off."
        actions={
          <Button component={Link} to="/library" leftSection={<IconPlus size={16} />}>
            Create
          </Button>
        }
      />

      {isError ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not load dashboard">
          Check that the backend is running, then try again.
        </Alert>
      ) : null}

      <SimpleGrid cols={{ base: 1, sm: 2, lg: 5 }}>
        <StatCard
          label="Decks"
          value={isLoading ? <Loader aria-label="Loading dashboard" size="sm" /> : (decks.data?.length ?? 0)}
          icon={<IconCards size={20} />}
        />
        <StatCard
          label="Folders"
          value={isLoading ? <Loader aria-label="Loading dashboard" size="sm" /> : (folders.data?.length ?? 0)}
          icon={<IconFolder size={20} />}
        />
        <StatCard
          label="Points"
          value={isLoading ? <Loader aria-label="Loading dashboard" size="sm" /> : (gamification.data?.points ?? 0)}
          icon={<IconStar size={20} />}
        />
        <StatCard
          label="Level"
          value={isLoading ? <Loader aria-label="Loading dashboard" size="sm" /> : (gamification.data?.level ?? 1)}
          icon={<IconTargetArrow size={20} />}
        />
        <StatCard
          label="Streak"
          value={isLoading ? <Loader aria-label="Loading dashboard" size="sm" /> : `${gamification.data?.streakCount ?? 0} days`}
          icon={<IconFlame size={20} />}
        />
      </SimpleGrid>

      <GamificationCard
        title="Next level progress"
        value={isLoading ? <Loader aria-label="Loading dashboard" size="sm" /> : `${gamification.data?.nextLevelProgress ?? 0}%`}
        description={isLoading ? 'Loading progress...' : `${gamification.data?.nextLevelProgress ?? 0}% to the next level`}
        isLoading={isLoading}
        loadingLabel="Loading dashboard"
      />

      <Stack gap="md">
        <Title order={2}>Daily missions</Title>
        {missions.isLoading ? <Loader aria-label="Loading missions" /> : null}
        {!missions.isLoading && (missions.data?.length ?? 0) > 0 ? (
          <DailyMissionList missions={missions.data ?? []} />
        ) : null}
        {!missions.isLoading && (missions.data?.length ?? 0) === 0 ? (
          <Text c="dimmed">No missions available today.</Text>
        ) : null}
      </Stack>

      <Group gap="sm">
        <Button component={Link} to="/library" variant="light">
          Browse library
        </Button>
        <Button component={Link} to="/srs" variant="subtle">
          Spaced repetition
        </Button>
      </Group>

      <Stack gap="md">
        <Title order={2}>Recent decks</Title>
        {isLoading ? <Loader aria-label="Loading recent decks" /> : null}
        {!isLoading && recentDecks.length === 0 ? (
          <EmptyState
            title="No decks yet"
            description="Create your first deck from the library."
            action={
              <Button component={Link} to="/library">
                Open library
              </Button>
            }
          />
        ) : null}
        <SimpleGrid cols={{ base: 1, sm: 2, lg: 4 }}>
          {recentDecks.map((deck) => (
            <Card key={deck.id} withBorder radius="sm">
              <Stack gap="sm">
                <Text fw={700}>{deck.title}</Text>
                <Text c="dimmed" size="sm" lineClamp={2}>
                  {deck.description ?? 'No description'}
                </Text>
                <Button component={Link} to={`/decks/${deck.id}`} variant="light">
                  Open
                </Button>
              </Stack>
            </Card>
          ))}
        </SimpleGrid>
      </Stack>
    </Stack>
  );
}
