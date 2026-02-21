import { z } from 'zod';

export const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email')
    .max(100, 'Email must be at most 100 characters')
    .trim(),

  password: z
    .string()
    .min(6, 'Password must be at least 6 characters')
    .max(100, 'Password must be at most 100 characters')
    .trim(),
});

export type LoginFormValues = z.infer<typeof loginSchema>;
