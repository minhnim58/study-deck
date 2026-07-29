import { Button, Group, Modal, Select, Stack, TextInput, Textarea } from '@mantine/core';
import { isNotEmpty, useForm } from '@mantine/form';
import { useEffect } from 'react';
import type { CreateFolderRequest, FolderResponse } from '../../api/types';

type FolderFormValues = {
  name: string;
  description: string;
  visibility: "PUBLIC" | "PRIVATE";
};

type FolderFormModalProps = {
  opened: boolean;
  onClose: () => void;
  onSubmit: (values: CreateFolderRequest) => void;
  folder?: FolderResponse | null;
  loading?: boolean;
};

function toRequest(values: FolderFormValues): CreateFolderRequest {
  return {
    name: values.name.trim(),
    description: values.description.trim() || null,
    visibility: values.visibility,
  };
}

export function FolderFormModal({ opened, onClose, onSubmit, folder, loading = false }: FolderFormModalProps) {
  const form = useForm<FolderFormValues>({
    initialValues: {
      name: '',
      description: '',
      visibility: 'PRIVATE',
    },
    validate: {
      name: isNotEmpty('Folder name is required'),
    },
  });

  useEffect(() => {
    if (!opened) {
      return;
    }

    form.setValues({
      name: folder?.name ?? '',
      description: folder?.description ?? '',
      visibility: folder?.visibility ?? 'PRIVATE',
    });
    form.clearErrors();
    form.resetDirty();
  }, [folder, opened]);

  return (
    <Modal opened={opened} onClose={onClose} title={folder ? 'Edit folder' : 'Create folder'} centered>
      <form onSubmit={form.onSubmit((values) => onSubmit(toRequest(values)))}>
        <Stack>
          <TextInput label="Name" placeholder="English" withAsterisk {...form.getInputProps('name')} />
          <Textarea label="Description" placeholder="Vocabulary decks" minRows={3} {...form.getInputProps('description')} />
          <Select
            label="Visibility"
            placeholder="Select visibility"
            data={[
              { value: 'PRIVATE', label: 'Private - Only you can view' },
              { value: 'PUBLIC', label: 'Public - Anyone can view' },
            ]}
            {...form.getInputProps('visibility')}
          />
          <Group justify="flex-end">
            <Button variant="subtle" onClick={onClose} type="button">
              Cancel
            </Button>
            <Button type="submit" loading={loading}>
              {folder ? 'Save folder' : 'Create folder'}
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
}
