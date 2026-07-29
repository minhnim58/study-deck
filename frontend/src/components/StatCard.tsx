import { Paper, Stack, Text, ThemeIcon, Title } from '@mantine/core';
import type { ReactNode } from 'react';

type StatCardProps = {
  label: string;
  value: ReactNode;
  icon?: ReactNode;
  color?: string;
};

export function StatCard({ label, value, icon, color = 'blue' }: StatCardProps) {
  return (
    <Paper withBorder p="md" radius="sm">
      <Stack gap={4}>
        {icon ? (
          <ThemeIcon variant="light" size="lg" color={color} radius="md">
            {icon}
          </ThemeIcon>
        ) : null}
        <Text c="dimmed" size="sm">
          {label}
        </Text>
        <Title order={2}>{value}</Title>
      </Stack>
    </Paper>
  );
}
