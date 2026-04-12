import { z } from 'zod';

export const boardSchema = z.object({
  name: z
    .string()
    .min(1, 'Name không được để trống') // @NotBlank
    .min(3, 'Name phải >= 3 ký tự') // @Size(min = 3)
    .max(100, 'Name <= 100 ký tự'), // @Size(max = 100)

  description: z.string().optional(),

  backgroundColor: z
    .string()
    .regex(/^#([A-Fa-f0-9]{6})$/, 'Invalid color format') // @Pattern
    .optional(),

  backgroundImageUrl: z
    .string()
    .max(500, 'Background image <= 500 ký tự') // @Size(max = 500)
    .optional(),

  visibility: z.enum(['PUBLIC', 'PRIVATE', 'WORKSPACE'] as const, {
    message: 'Visibility là bắt buộc',
  }),
});

export type BoardSchema = z.infer<typeof boardSchema>;

export const boardUpdateSchema = z.object({
  name: z
    .string()
    .min(3, 'Name phải >= 3 ký tự')
    .max(100, 'Name <= 100 ký tự')
    .optional(),

  description: z.string().optional(),

  backgroundColor: z
    .string()
    .regex(/^#([A-Fa-f0-9]{6})$/, 'Invalid color format')
    .optional(),

  backgroundImageUrl: z
    .string()
    .max(500, 'Background image <= 500 ký tự')
    .optional(),

  visibility: z.enum(['PUBLIC', 'PRIVATE', 'WORKSPACE'] as const).optional(),
});

export type BoardUpdateSchema = z.infer<typeof boardUpdateSchema>;