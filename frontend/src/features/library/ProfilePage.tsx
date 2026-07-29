import { Card, Group, Loader, SimpleGrid, Stack, Text, Title } from '@mantine/core';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../auth/AuthProvider';
import { getDailyMissions, getGamification } from '../../api/gamificationApi';
import { DailyMissionList } from '../../components/DailyMissionList';
import { PageHeader } from '../../components/PageHeader';
import { StatCard } from '../../components/StatCard';
import { IconFlame, IconStar, IconTargetArrow } from '@tabler/icons-react';

export function ProfilePage() {
  const { user } = useAuth();
  const gamification = useQuery({ queryKey: ['gamification'], queryFn: getGamification });
  const missions = useQuery({ queryKey: ['daily-missions'], queryFn: getDailyMissions });
  const isLoading = gamification.isLoading || missions.isLoading;

  return (
    <Stack gap="lg">
      <PageHeader title="Profile" description="Your signed-in study account." />
      <Card withBorder radius="sm">
        <Stack gap={4}>
          <Text fw={700}>{user?.displayName || 'Study Deck user'}</Text>
          <Text c="dimmed">{user?.email}</Text>
        </Stack>
      </Card>

      <SimpleGrid cols={{ base: 1, sm: 3 }}>
        <StatCard
          label="Points"
          value={isLoading ? <Loader aria-label="Loading profile stats" size="sm" /> : (gamification.data?.points ?? 0)}
          icon={<IconStar size={20} />}
        />
        <StatCard
          label="Level"
          value={isLoading ? <Loader aria-label="Loading profile stats" size="sm" /> : (gamification.data?.level ?? 1)}
          icon={<IconTargetArrow size={20} />}
        />
        <StatCard
          label="Streak"
          value={isLoading ? <Loader aria-label="Loading profile stats" size="sm" /> : `${gamification.data?.streakCount ?? 0} days`}
          icon={<IconFlame size={20} />}
        />
      </SimpleGrid>

      <Card withBorder radius="sm" p="md">
        <Stack gap={8}>
          <Group justify="space-between" align="center">
            <Text fw={600}>Next level progress</Text>
            <Text fw={700}>
              {isLoading ? <Loader aria-label="Loading profile stats" size="sm" /> : `${gamification.data?.nextLevelProgress ?? 0}%`}
            </Text>
          </Group>
          <Text c="dimmed" size="sm">
            {isLoading
              ? 'Loading progress...'
              : `${gamification.data?.nextLevelProgress ?? 0}% to the next level`}
          </Text>
        </Stack>
      </Card>

      <Stack gap="md">
        <Title order={2}>Daily missions</Title>
        {missions.isLoading ? <Loader aria-label="Loading missions" /> : null}
        {!missions.isLoading && (missions.data?.length ?? 0) > 0 ? (
          <DailyMissionList missions={missions.data ?? []} />
        ) : null}
        {!missions.isLoading && (missions.data?.length ?? 0) === 0 ? <Text c="dimmed">No missions available today.</Text> : null}
      </Stack>
    </Stack>
  );
}
