import { Anchor, Button, Container, Paper, PasswordInput, Stack, Text, TextInput, Title } from '@mantine/core';
import { isEmail, isNotEmpty, useForm } from '@mantine/form';
import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { login } from '../../api/authApi';
import type { ApiError } from '../../api/errors';
import { normalizeApiError } from '../../api/errors';
import { useAuth } from '../../auth/AuthProvider';
import { FormErrorAlert } from '../../components/FormErrorAlert';

export function LoginPage() {
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(false);
  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = '/';

  const form = useForm({
    mode: 'controlled',
    initialValues: {
      email: '',
      password: '',
    },
    validate: {
      email: isEmail('Enter a valid email'),
      password: isNotEmpty('Password is required'),
    },
  });

  return (
    <Container size={420} py={80}>
      <Paper withBorder p="xl" radius="sm">
        <form
          onSubmit={form.onSubmit(async (values) => {
            setError(null);
            setLoading(true);
            try {
              const response = await login(values);
              auth.setSession(response);
              navigate(from, { replace: true });
            } catch (requestError) {
              setError(normalizeApiError(requestError));
            } finally {
              setLoading(false);
            }
          })}
        >
          <Stack>
            <Stack gap={4}>
              <Title order={1}>Sign in</Title>
              <Text c="dimmed" size="sm">
                Access your study workspace.
              </Text>
            </Stack>
            <FormErrorAlert error={error} />
            <TextInput label="Email" autoComplete="email" {...form.getInputProps('email')} />
            <PasswordInput label="Password" autoComplete="current-password" {...form.getInputProps('password')} />
            <Button type="submit" loading={loading}>
              Sign in
            </Button>
            <Text size="sm" ta="center">
              New to Study Deck?{' '}
              <Anchor component={Link} to="/register">
                Create an account
              </Anchor>
            </Text>
          </Stack>
        </form>
      </Paper>
    </Container>
  );
}
