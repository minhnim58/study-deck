import { apiClient } from './client';
import type {
  CreateDeckRequest,
  CreateFolderRequest,
  DeckResponse,
  DeckSummaryResponse,
  FolderResponse,
  UpdateDeckRequest,
  UpdateFolderRequest,
  ViewerCardResponse,
  CreateFlashcardRequest,
  UpdateFlashcardRequest,
  FlashcardResponse,
} from './types';

export async function listFolders() {
  const response = await apiClient.get<FolderResponse[]>('/folders');
  return response.data;
}

export async function createFolder(request: CreateFolderRequest) {
  const response = await apiClient.post<FolderResponse>('/folders', request);
  return response.data;
}

export async function getFolder(folderId: number) {
  const response = await apiClient.get<FolderResponse>(`/folders/${folderId}`);
  return response.data;
}

export async function updateFlashcard(flashcardId: number, request: UpdateFlashcardRequest) {
  const { data } = await apiClient.patch<FlashcardResponse>(`/api/v1/flashcards/${flashcardId}`, request);
  return data;
}

export async function importFlashcards(deckId: number, requests: CreateFlashcardRequest[]) {
  const { data } = await apiClient.post<FlashcardResponse[]>(`/decks/${deckId}/flashcards/bulk`, requests);
  return data;
}

export async function updateFolder(folderId: number, request: UpdateFolderRequest) {
  const response = await apiClient.patch<FolderResponse>(`/folders/${folderId}`, request);
  return response.data;
}

export async function deleteFolder(folderId: number) {
  await apiClient.delete(`/folders/${folderId}`);
}

export async function listDecks() {
  const response = await apiClient.get<DeckResponse[]>('/decks');
  return response.data;
}

export async function createDeck(request: CreateDeckRequest) {
  const response = await apiClient.post<DeckResponse>('/decks', request);
  return response.data;
}

export async function getDeck(deckId: number) {
  const response = await apiClient.get<DeckResponse>(`/decks/${deckId}`);
  return response.data;
}

export async function updateDeck(deckId: number, request: UpdateDeckRequest) {
  const response = await apiClient.patch<DeckResponse>(`/decks/${deckId}`, request);
  return response.data;
}

export async function deleteDeck(deckId: number) {
  await apiClient.delete(`/decks/${deckId}`);
}

export async function addDeckToFolder(folderId: number, deckId: number) {
  const response = await apiClient.post<DeckResponse>(`/folders/${folderId}/decks/${deckId}`);
  return response.data;
}

export async function removeDeckFromFolder(folderId: number, deckId: number) {
  const response = await apiClient.delete<DeckResponse>(`/folders/${folderId}/decks/${deckId}`);
  return response.data;
}

export async function getDeckSummary(deckId: number) {
  const response = await apiClient.get<DeckSummaryResponse>(`/decks/${deckId}/summary`);
  return response.data;
}

export async function listViewerCards(deckId: number, options?: { sort?: string; mode?: string }) {
  const response = await apiClient.get<ViewerCardResponse[]>(`/decks/${deckId}/viewer-cards`, {
    params: options,
  });
  return response.data;
}
