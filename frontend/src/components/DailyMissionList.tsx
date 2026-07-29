import { Badge, Button, Card, Group, Progress, Stack, Text, ThemeIcon } from '@mantine/core';
import { IconCheck, IconTargetArrow } from '@tabler/icons-react';
import type { DailyMissionResponse } from '../api/types';

type DailyMissionListProps = {
  missions: DailyMissionResponse[];
  onClaim?: (missionKey: string) => void;
};

export function DailyMissionList({ missions, onClaim }: DailyMissionListProps) {
  return (
    <Stack gap="sm">
      {missions.map((mission) => {
        const progressPercent = Math.min(100, Math.round((mission.progress / mission.target) * 100));
        const canClaim = mission.status === 'COMPLETED' && mission.progress >= mission.target;

        return (
          <Card key={mission.key} withBorder radius="sm" p="md">
            <Group justify="space-between" align="flex-start">
              <Stack gap={4} style={{ flex: 1 }}>
                <Group gap="xs">
                  <ThemeIcon size="sm" variant="light" color={canClaim ? 'green' : 'blue'}>
                    {canClaim ? <IconCheck size={14} /> : <IconTargetArrow size={14} />}
                  </ThemeIcon>
                  <Text fw={600}>{mission.title}</Text>
                  <Badge color={mission.status === 'CLAIMED' ? 'gray' : canClaim ? 'green' : 'blue'}>
                    {mission.status === 'CLAIMED'
                      ? 'Claimed'
                      : mission.status === 'COMPLETED'
                        ? 'Completed'
                        : 'In progress'}
                  </Badge>
                </Group>
                <Text c="dimmed" size="sm">
                  {mission.description}
                </Text>
                <Progress value={progressPercent} size="sm" mt={4} />
                <Text size="xs" c="dimmed">
                  {mission.progress}/{mission.target} · Reward: {mission.rewardPoints} pts
                </Text>
              </Stack>
              {canClaim ? (
                <Button size="compact-sm" variant="light" onClick={() => onClaim?.(mission.key)}>
                  Claim
                </Button>
              ) : null}
            </Group>
          </Card>
        );
      })}
    </Stack>
  );
}
