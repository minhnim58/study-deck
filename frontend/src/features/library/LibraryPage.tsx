import {
  Alert,
  Badge,
  Button,
  Card,
  Group,
  Loader,
  Select,
  SimpleGrid,
  Stack,
  Text,
  TextInput,
  Title,
} from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { IconAlertCircle, IconCards, IconFolder, IconPlus, IconSearch } from '@tabler/icons-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  createDeck,
  createFolder,
  deleteDeck,
  deleteFolder,
  listDecks,
  listFolders,
  updateDeck,
  updateFolder,
} from '../../api/deckApi';
import type { CreateDeckRequest, CreateFolderRequest, DeckResponse, FolderResponse, UpdateDeckRequest } from '../../api/types';
import { EmptyState } from '../../components/EmptyState';
import { EntityMenu } from '../../components/EntityMenu';
import { PageHeader } from '../../components/PageHeader';
import { DeckFormModal } from './DeckFormModal';
import { FolderFormModal } from './FolderFormModal';

export function LibraryPage() {
  const queryClient = useQueryClient();
  const location = useLocation();
  const folders = useQuery({ queryKey: ['folders'], queryFn: listFolders });
  const decks = useQuery({ queryKey: ['decks'], queryFn: listDecks });
  const [folderModalOpen, setFolderModalOpen] = useState(false);
  const [deckModalOpen, setDeckModalOpen] = useState(false);
  const [editingFolder, setEditingFolder] = useState<FolderResponse | null>(null);
  const [editingDeck, setEditingDeck] = useState<DeckResponse | null>(null);
  const [search, setSearch] = useState('');
  const [folderFilter, setFolderFilter] = useState<string | null>(null);

  const createFolderMutation = useMutation({
    mutationFn: createFolder,
    onSuccess: () => {
      notifications.show({ color: 'green', message: 'Folder created' });
      setFolderModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['folders'] });
    },
  });

  const updateFolderMutation = useMutation({
    mutationFn: ({ folderId, request }: { folderId: number; request: CreateFolderRequest }) => updateFolder(folderId, request),
    onSuccess: () => {
      notifications.show({ color: 'green', message: 'Folder updated' });
      setEditingFolder(null);
      queryClient.invalidateQueries({ queryKey: ['folders'] });
    },
  });

  const deleteFolderMutation = useMutation({
    mutationFn: deleteFolder,
    onSuccess: () => {
      notifications.show({ color: 'green', message: 'Folder deleted' });
      queryClient.invalidateQueries({ queryKey: ['folders'] });
      queryClient.invalidateQueries({ queryKey: ['decks'] });
    },
  });

  const createDeckMutation = useMutation({
    mutationFn: createDeck,
    onSuccess: () => {
      notifications.show({ color: 'green', message: 'Deck created' });
      setDeckModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['decks'] });
    },
  });

  const updateDeckMutation = useMutation({
    mutationFn: ({ deckId, request }: { deckId: number; request: UpdateDeckRequest }) => updateDeck(deckId, request),
    onSuccess: () => {
      notifications.show({ color: 'green', message: 'Deck updated' });
      setEditingDeck(null);
      queryClient.invalidateQueries({ queryKey: ['decks'] });
    },
  });

  const deleteDeckMutation = useMutation({
    mutationFn: deleteDeck,
    onSuccess: () => {
      notifications.show({ color: 'green', message: 'Deck deleted' });
      queryClient.invalidateQueries({ queryKey: ['decks'] });
    },
  });

  const routeFilter =
    location.pathname === '/folders'
      ? 'folders'
      : location.pathname === '/decks'
        ? 'decks'
        : location.pathname === '/starred'
          ? 'starred'
          : 'all';
  const pageCopy = {
    all: {
      title: 'Library',
      description: 'Organize folders, decks, and study material.',
      empty: 'Create a folder or deck, or adjust the current filters.',
    },
    folders: {
      title: 'Folders',
      description: 'Organize decks into study folders.',
      empty: 'No folders found. Create a folder to group your decks.',
    },
    decks: {
      title: 'Decks',
      description: 'Browse and manage all study decks.',
      empty: 'No decks found. Create a deck to start adding cards.',
    },
    starred: {
      title: 'Starred',
      description: 'Review decks that contain starred cards.',
      empty: 'No starred deck activity found yet. Star cards inside a deck to build this view.',
    },
  }[routeFilter];
  const normalizedSearch = search.trim().toLowerCase();
  const filteredFolders = useMemo(
    () =>
      (folders.data ?? []).filter((folder) => {
        if (routeFilter === 'decks' || routeFilter === 'starred') {
          return false;
        }

        return [folder.name, folder.description ?? ''].some((value) => value.toLowerCase().includes(normalizedSearch));
      }),
    [folders.data, normalizedSearch, routeFilter],
  );
  const filteredDecks = useMemo(
    () =>
      (decks.data ?? []).filter((deck) => {
        if (routeFilter === 'folders') {
          return false;
        }
        if (routeFilter === 'starred') {
          return false;
        }

        const matchesSearch = [deck.title, deck.description ?? ''].some((value) => value.toLowerCase().includes(normalizedSearch));
        const matchesFolder = !folderFilter || String(deck.folderId ?? 'none') === folderFilter;
        return matchesSearch && matchesFolder;
      }),
    [decks.data, folderFilter, normalizedSearch, routeFilter],
  );

  const isLoading = folders.isLoading || decks.isLoading;
  const isError = folders.isError || decks.isError;
  const isEmpty = !isLoading && filteredFolders.length === 0 && filteredDecks.length === 0;
  const folderOptions = [
    { value: 'none', label: 'No folder' },
    ...(folders.data ?? []).map((folder) => ({ value: String(folder.id), label: folder.name })),
  ];

  function handleDeleteFolder(folder: FolderResponse) {
    if (window.confirm(`Delete folder "${folder.name}"? Decks in this folder will be moved out of it.`)) {
      deleteFolderMutation.mutate(folder.id);
    }
  }

  function handleDeleteDeck(deck: DeckResponse) {
    if (window.confirm(`Delete deck "${deck.title}"?`)) {
      deleteDeckMutation.mutate(deck.id);
    }
  }

  return (
    <Stack gap="xl">
      <PageHeader
        title={pageCopy.title}
        description={pageCopy.description}
        actions={
          <>
            <Button
              variant="light"
              leftSection={<IconFolder size={16} />}
              onClick={() => {
                setEditingFolder(null);
                setFolderModalOpen(true);
              }}
            >
              New folder
            </Button>
            <Button
              leftSection={<IconPlus size={16} />}
              onClick={() => {
                setEditingDeck(null);
                setDeckModalOpen(true);
              }}
            >
              New deck
            </Button>
          </>
        }
      />

      <Group align="flex-end">
        <TextInput
          label="Search"
          placeholder="Search folders and decks"
          leftSection={<IconSearch size={16} />}
          value={search}
          onChange={(event) => setSearch(event.currentTarget.value)}
          flex={1}
        />
        <Select
          label="Folder"
          placeholder="All folders"
          clearable
          data={folderOptions}
          value={folderFilter}
          onChange={setFolderFilter}
          w={{ base: '100%', sm: 220 }}
        />
      </Group>

      {isError ? (
        <Alert color="red" icon={<IconAlertCircle size={18} />} title="Could not load library">
          Check that the backend is running, then try again.
        </Alert>
      ) : null}

      {isLoading ? <Loader aria-label="Loading library" /> : null}

      {isEmpty ? (
        <EmptyState
          title="No library items found"
          description={pageCopy.empty}
          action={
            <Button onClick={() => setDeckModalOpen(true)} leftSection={<IconPlus size={16} />}>
              New deck
            </Button>
          }
        />
      ) : null}

      {filteredFolders.length > 0 ? (
        <Stack gap="md">
          <Title order={2}>Folders</Title>
          <SimpleGrid cols={{ base: 1, sm: 2, lg: 3 }}>
            {filteredFolders.map((folder) => (
              <Card key={folder.id} withBorder radius="sm">
                <Stack gap="sm">
                  <Group justify="space-between" align="flex-start">
                    <Group gap="sm">
                      <IconFolder size={20} />
                      <Text fw={700}>{folder.name}</Text>
                    </Group>
                    <EntityMenu onEdit={() => setEditingFolder(folder)} onDelete={() => handleDeleteFolder(folder)} />
                  </Group>
                  {folder.description ? (
                    <Text c="dimmed" size="sm" lineClamp={2}>
                      {folder.description}
                    </Text>
                  ) : null}
                  <Badge variant="light">{(decks.data ?? []).filter((deck) => deck.folderId === folder.id).length} decks</Badge>
                </Stack>
              </Card>
            ))}
          </SimpleGrid>
        </Stack>
      ) : null}

      {filteredDecks.length > 0 ? (
        <Stack gap="md">
          <Title order={2}>Decks</Title>
          <SimpleGrid cols={{ base: 1, sm: 2, lg: 3 }}>
            {filteredDecks.map((deck) => (
              <Card key={deck.id} withBorder radius="sm">
                <Stack gap="sm">
                  <Group justify="space-between" align="flex-start">
                    <Group gap="sm">
                      <IconCards size={20} />
                      <Text fw={700}>{deck.title}</Text>
                    </Group>
                    <EntityMenu onEdit={() => setEditingDeck(deck)} onDelete={() => handleDeleteDeck(deck)} />
                  </Group>
                  {deck.description ? (
                    <Text c="dimmed" size="sm" lineClamp={2}>
                      {deck.description}
                    </Text>
                  ) : null}
                  <Group justify="space-between">
                    <Group gap="xs">
                      <Badge variant="light">{deck.visibility}</Badge>
                      <Badge variant="outline" color="gray">
                        {deck.totalCards} cards
                      </Badge>
                    </Group>
                    <Button component={Link} to={`/decks/${deck.id}`} variant="light" size="xs">
                      Open
                    </Button>
                  </Group>
                </Stack>
              </Card>
            ))}
          </SimpleGrid>
        </Stack>
      ) : null}

      <FolderFormModal
        opened={folderModalOpen || Boolean(editingFolder)}
        onClose={() => {
          setFolderModalOpen(false);
          setEditingFolder(null);
        }}
        folder={editingFolder}
        loading={createFolderMutation.isPending || updateFolderMutation.isPending}
        onSubmit={(values) => {
          if (editingFolder) {
            updateFolderMutation.mutate({ folderId: editingFolder.id, request: values });
          } else {
            createFolderMutation.mutate(values);
          }
        }}
      />
      <DeckFormModal
        opened={deckModalOpen || Boolean(editingDeck)}
        onClose={() => {
          setDeckModalOpen(false);
          setEditingDeck(null);
        }}
        deck={editingDeck}
        folders={folders.data ?? []}
        loading={createDeckMutation.isPending || updateDeckMutation.isPending}
        onSubmit={(values) => {
          if (editingDeck) {
            updateDeckMutation.mutate({ deckId: editingDeck.id, request: values as UpdateDeckRequest });
          } else {
            createDeckMutation.mutate(values as CreateDeckRequest);
          }
        }}
      />
    </Stack>
  );
}
