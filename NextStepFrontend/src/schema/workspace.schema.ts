import { z } from 'zod';

export const workspaceSchema = z.object({
  name: z
    .string()
    .min(4, 'Name phải >= 4 ký tự')
    .max(20, 'Name <= 20 ký tự')
    .nonempty('Name không được để trống'),

  description: z.string().max(255, 'Description <= 255 ký tự').optional(),

  visibility: z.enum(['PUBLIC', 'PRIVATE', 'WORKSPACE'] as const, {
    message: 'Visibility là bắt buộc',
  }),
});

export type workspaceSchema = z.infer<typeof workspaceSchema>;
