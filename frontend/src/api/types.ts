export type AuthUser = {
  id: number;
  email: string;
  displayName: string | null;
  points?: number;
  level?: number;
  streakCount?: number;
};

export type AuthResponse = {
  accessToken: string;
  tokenType: "Bearer";
  expiresInSeconds: number;
  user: AuthUser;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  email: string;
  password: string;
  displayName: string;
};

export type Visibility = "PRIVATE" | "PUBLIC";

export type UserGamification = {
  userId: number;
  points: number;
  level: number;
  streakCount: number;
  lastActiveAt: string | null;
  nextLevelProgress: number;
  nextLevelRequiredPoints: number;
};

export type DailyMissionResponse = {
  key: string;
  title: string;
  description: string;
  target: number;
  progress: number;
  rewardPoints: number;
  status: "IN_PROGRESS" | "COMPLETED" | "CLAIMED";
  claimedAt: string | null;
};

export type DailyMissionStatusResponse = {
  missionKey: string;
  status: "IN_PROGRESS" | "COMPLETED" | "CLAIMED";
  progress: number;
  target: number;
  claimedAt: string | null;
};

export type FolderResponse = {
  id: number;
  name: string;
  description: string | null;
  visibility: Visibility;
  creatorId: number | null;
  creatorDisplayName: string | null;
  position: number;
  createdAt: string;
  updatedAt: string;
};

export type CreateFolderRequest = {
  name: string;
  description: string | null;
  visibility: Visibility;
};

export type UpdateFolderRequest = CreateFolderRequest;

export type DeckResponse = {
  id: number;
  folderId: number | null;
  title: string;
  description: string | null;
  visibility: Visibility;
  creatorId: number | null;
  creatorDisplayName: string | null;
  createdAt: string;
  updatedAt: string;
  totalCards: number;
};

export type CreateDeckRequest = {
  folderId: number | null;
  title: string;
  description: string | null;
  visibility: Visibility;
};

export type UpdateDeckRequest = {
  title: string;
  description: string | null;
  visibility: Visibility;
};

export type DeckSummaryResponse = {
  deckId: number;
  totalCards: number;
  starredCards: number;
  dueSrsCards: number;
  newCards: number;
  learningCards: number;
  reviewCards: number;
  availableModes: string[];
};

export type FlashcardResponse = {
  id: number;
  deckId: number;
  term: string;
  definition: string;
  termImageUrl: string | null;
  definitionImageUrl: string | null;
  starred: boolean;
  position: number;
  createdAt: string;
  updatedAt: string;
};

export type CreateFlashcardRequest = {
  term: string;
  definition: string;
  termImageUrl: string | null;
  definitionImageUrl: string | null;
};

export type UpdateFlashcardRequest = CreateFlashcardRequest;

export type StarFlashcardRequest = {
  starred: boolean;
};

export type ViewerCardResponse = {
  id: number;
  term: string;
  definition: string;
  termImageUrl: string | null;
  definitionImageUrl: string | null;
  starred: boolean;
  position: number;
};

export type SrsRating = "AGAIN" | "HARD" | "GOOD" | "EASY";
export type SrsCardState = "NEW" | "LEARNING" | "REVIEW" | "RELEARNING";

export type SrsDueCardResponse = {
  flashcardId: number;
  term: string;
  definition: string;
  dueAt: string;
  state: SrsCardState;
};

export type SrsReviewRequest = {
  rating: SrsRating;
  durationMs: number;
};

export type SrsReviewResponse = {
  flashcardId: number;
  rating: SrsRating;
  state: SrsCardState;
  dueAt: string;
  reps: number;
  lapses: number;
};

export type SrsStatsResponse = {
  newCards: number;
  learningCards: number;
  reviewCards: number;
  dueCards: number;
};

export type SrsCardStateResponse = {
  flashcardId: number;
  state: SrsCardState;
  dueAt: string;
  stability: number;
  difficulty: number;
  reps: number;
  lapses: number;
};

export type LearnPromptSide = "TERM" | "DEFINITION";
export type LearnQuestionType =
  | "FLASHCARD"
  | "MULTIPLE_CHOICE"
  | "WRITTEN"
  | "TRUE_FALSE";

export type CreateLearnSessionRequest = {
  lengthOfRounds: number;
  flashcards: boolean;
  multipleChoice: boolean;
  written: boolean;
  trueFalse: boolean;
  starredOnly: boolean;
  shuffleTerms: boolean;
};

export type LearnSessionResponse = {
  id: number;
  status: "ACTIVE" | "COMPLETED";
  totalItems: number;
  correctCount: number;
  wrongCount: number;
  items: LearnSessionItemResponse[];
};

export type LearnSessionItemResponse = {
  id: number;
  flashcardId: number;
  questionType: LearnQuestionType;
  promptSide: LearnPromptSide;
  prompt: string;
  answer: string;
  options: string[];
  attempts: number;
};

export type LearnAnswerRequest = {
  itemId: number;
  answer: string;
};

export type CreatePracticeTestRequest = {
  questionCount: number;
  multipleChoice: boolean;
  written: boolean;
  trueFalse: boolean;
  starredOnly: boolean;
  answerWithTerm: boolean;
  answerWithDefinition: boolean;
};

export type PracticeTestResponse = {
  id: number;
  status: "ACTIVE" | "SUBMITTED";
  questionCount: number;
  answeredCount: number;
  scorePercent: number;
  questions: PracticeQuestionResponse[];
};

export type PracticeQuestionResponse = {
  id: number;
  flashcardId: number;
  questionType: LearnQuestionType;
  promptSide: LearnPromptSide;
  prompt: string;
  options: string[];
  submittedAnswer: string | null;
  correct: boolean | null;
  correctAnswer: string;
};

export type PracticeAnswerRequest = {
  questionId: number;
  answer: string;
};

export type CreateMatchingSessionRequest = {
  cardCount: number;
  starredOnly: boolean;
};

export type MatchingSessionResponse = {
  id: number;
  status: "ACTIVE" | "COMPLETED";
  cardCount: number;
  matchedCount: number;
  durationMs: number;
  items: MatchingSessionItemResponse[];
};

export type MatchingSessionItemResponse = {
  id: number;
  flashcardId: number;
  term: string;
  definition: string;
  matched: boolean;
};

export type MatchingAnswerRequest = {
  itemId: number;
};

export type CreateSortingSessionRequest = {
  starredOnly: boolean;
  shuffle: boolean;
};

export type SortingAnswer = "KNOW" | "DO_NOT_KNOW";

export type SortingSessionItemResponse = {
  id: number;
  flashcardId: number;
  term: string;
  definition: string;
  termImageUrl: string | null;
  definitionImageUrl: string | null;
  starred: boolean;
  position: number;
  answer: SortingAnswer | null;
};

export type SortingSessionResponse = {
  id: number;
  status: "ACTIVE" | "COMPLETED";
  knownCount: number;
  doNotKnowCount: number;
  items: SortingSessionItemResponse[];
};

export type AnswerSortingItemRequest = {
  itemId: number;
  answer: SortingAnswer;
};
