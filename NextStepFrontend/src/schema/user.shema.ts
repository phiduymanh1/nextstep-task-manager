import { z } from 'zod';

const PHONE_REGEX = /^0[35789]\d{8}$/;

export const userUpdateSchema = z.object({
  fullName: z
    .string()
    .max(100, 'Full name must be at most 100 characters')
    .nullable()
    .optional(),

  phone: z
    .string()
    .regex(PHONE_REGEX, 'Số điện thoại không đúng định dạng') // ValidateMessageConst.PHONE_VALID
    .nullable()
    .optional()
    .or(z.literal('')),
});

export type UserUpdatePayload = z.infer<typeof userUpdateSchema>;
