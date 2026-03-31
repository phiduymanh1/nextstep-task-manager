import { z } from 'zod';

export const listCreateSchema = z.object({
  name: z
    .string()
    .min(1, 'Name không được để trống') // @NotBlank
    .max(100, 'Name phải từ 1-100 ký tự'), // @Size(max = 100)

  beforeId: z.number().int().nullable().optional(),
  afterId: z.number().int().nullable().optional(),
});

export type ListCreateSchema = z.infer<typeof listCreateSchema>;
