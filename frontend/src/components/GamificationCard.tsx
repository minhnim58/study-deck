import { Card, Group, Loader, Stack, Text } from '@mantine/core';
import type { ReactNode } from 'react';

type GamificationCardProps = {
  title: string;
  value: ReactNode;
  description?: ReactNode;
  isLoading?: boolean;
  loadingLabel?: string;
};

export function GamificationCard({ title, value, description, isLoading = false, loadingLabel = 'Loading' }: GamificationCardProps) {
  return (
    <Card withBorder radius="sm" p="md">
      <Stack gap={4}>
        <Text fw={600}>{title}</Text>
        <Text fw={700} size="xl">
          {isLoading ? <Loader aria-label={loadingLabel} size="sm" /> : value}
        </Text>
        {description ? <Text c="dimmed" size="sm">{description}</Text> : null}
      </Stack>
    </Card>
  );
}
