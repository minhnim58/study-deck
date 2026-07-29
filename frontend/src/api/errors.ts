import { AxiosError } from 'axios';

export type ApiFieldError = {
  field: string;
  message: string;
};

export type ApiError = {
  status?: number;
  message: string;
  path?: string;
  errors?: ApiFieldError[];
};

type ApiErrorBody = Partial<ApiError> & {
  error?: string;
  detail?: string;
  fieldErrors?: ApiFieldError[];
};

function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'message' in error &&
    typeof (error as { message?: unknown }).message === 'string'
  );
}

export function normalizeApiError(error: unknown): ApiError {
  if (isApiError(error)) {
    return error;
  }

  if (error instanceof AxiosError) {
    const data = error.response?.data as ApiErrorBody | undefined;
    const fieldErrors = [...(data?.errors ?? []), ...(data?.fieldErrors ?? [])];

    let rawMessage = data?.message ?? data?.detail ?? data?.error ?? error.message ?? 'Request failed';
    
    // Map weird technical messages to friendly ones
    if (rawMessage.includes('Duplicate entry') && rawMessage.includes('email')) {
      rawMessage = 'This email is already registered. Please sign in or use a different email.';
    } else if (rawMessage.includes('Bad credentials') || rawMessage.includes('UserDetailsService')) {
      rawMessage = 'Incorrect email or password.';
    } else if (rawMessage.includes('User not found')) {
      rawMessage = 'No account found with this email.';
    } else if (rawMessage.includes('Validation failed')) {
      rawMessage = 'Please check your input properties.';
    }

    return {
      status: error.response?.status,
      message: rawMessage,
      path: data?.path,
      errors: fieldErrors.length > 0 ? fieldErrors : undefined,
    };
  }

  if (error instanceof Error) {
    return { message: error.message };
  }

  return { message: 'Unexpected error' };
}
