export interface AuditRecord {
  createdAt: string;
  updatedAt: string;
}

export type UserRole = 'ADMIN' | 'USER';

export interface User {
  username: string;
  email: string;
  fullName: string;
  avatarUrl?: string;
  phone?: string;
  isActive: boolean;
  role: UserRole;
  audit: AuditRecord;
}
