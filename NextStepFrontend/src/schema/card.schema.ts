import z from "zod";

export const ListUpdateSchema = z.object({
  name: z
    .string()
    .min(1, 'Tên danh sách phải từ 1-100 ký tự')
    .max(100, 'Tên danh sách phải từ 1-100 ký tự'),
});

export const ListPositionSchema = z.object({
  beforeId: z.number().nullable(),
  afterId: z.number().nullable(),
});

export const CardCreateSchema = z.object({
  title: z
    .string()
    .min(1, 'Tiêu đề thẻ không được để trống')
    .max(255, 'Tiêu đề tối đa 255 ký tự'),
  description: z.string().max(5000, 'Mô tả tối đa 5000 ký tự').optional(),
  afterId: z.number().nullable().optional(),
  beforeId: z.number().nullable().optional(),
});

export const CardPositionSchema = z.object({
  listId: z.number().refine((val) => val !== undefined, {
    message: 'listId là bắt buộc',
  }),
  afterId: z.number().nullable().optional(),
  beforeId: z.number().nullable().optional(),
});
