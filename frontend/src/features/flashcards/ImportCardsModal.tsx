import { Button, Modal, Stack, Textarea, Text, Group, Select } from '@mantine/core';
import { useForm } from '@mantine/form';
import type { CreateFlashcardRequest } from '../../api/types';
import { useState } from 'react';

type ImportCardsModalProps = {
  opened: boolean;
  onClose: () => void;
  loading: boolean;
  onSubmit: (cards: CreateFlashcardRequest[]) => void;
};

export function ImportCardsModal({ opened, onClose, loading, onSubmit }: ImportCardsModalProps) {
  const [separator, setSeparator] = useState('tab');

  const form = useForm({
    initialValues: {
      text: '',
    },
    validate: {
      text: (value) => (value.trim().length === 0 ? 'Data is required to import cards' : null),
    },
  });

  const handleSubmit = (values: typeof form.values) => {
    const sepChar = separator === 'tab' ? '\t' : separator === 'comma' ? ',' : '-';
    const lines = values.text.split('\n');
    const cards: CreateFlashcardRequest[] = [];
    
    for (const line of lines) {
      if (line.trim().length === 0) continue;
      const parts = line.split(sepChar);
      if (parts.length >= 2) {
        cards.push({
          term: parts[0].trim(),
          definition: parts.slice(1).join(sepChar).trim(),
          termImageUrl: null,
          definitionImageUrl: null,
        });
      } else {
        cards.push({
          term: parts[0].trim(),
          definition: '',
          termImageUrl: null,
          definitionImageUrl: null,
        });
      }
    }

    if (cards.length > 0) {
      onSubmit(cards);
      form.reset();
    } else {
      form.setFieldError('text', 'No valid cards found to import.');
    }
  };

  return (
    <Modal opened={opened} onClose={onClose} title="Import multiple cards" size="lg">
      <form onSubmit={form.onSubmit(handleSubmit)}>
        <Stack gap="md">
          <Text size="sm" c="dimmed">
            Copy and paste your data here. Default is one card per line.
          </Text>
          <Select
            label="Between term and definition"
            value={separator}
            onChange={(val) => setSeparator(val ?? 'tab')}
            data={[
              { value: 'tab', label: 'Tab' },
              { value: 'comma', label: 'Comma' },
              { value: 'dash', label: 'Dash (-)' },
            ]}
          />
          <Textarea
            label="Card data"
            placeholder="Term 1&#9;Definition 1&#10;Term 2&#9;Definition 2"
            minRows={10}
            {...form.getInputProps('text')}
          />
          <Group justify="flex-end" mt="md">
            <Button variant="default" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" loading={loading}>
              Import cards
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
}
