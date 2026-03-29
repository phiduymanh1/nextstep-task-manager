export interface ResponseMetaData {
  success: boolean;
  code: string;
  message: string;
  timestamp: string;
  errors: string[];
}

export interface ApiResponse<T = unknown> {
  metaData: ResponseMetaData;
  data: T;
}

export interface PageResponse<T = unknown> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
