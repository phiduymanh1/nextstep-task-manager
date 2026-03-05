import '@/assets/styles/Login.css';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

import { loginSchema, type LoginFormValues } from '@/schema/auth.schema';
import { login } from '@/services/auth.service';
import { saveAccessToken } from '@/utils/token';
import { useNavigate } from 'react-router-dom';
import AuthLayout from '@/components/layout/authLayout/AuthLayout';
import InputField from '@/components/ui/input/InputField';
import Button from '@/components/ui/button/Button';
import toast from 'react-hot-toast';
import axios from 'axios';
import { useState } from 'react';
import type { ApiResponse } from '@/types/api.type';

export default function Login() {
  const navigate = useNavigate();

  const [formErrors, setFormErrors] = useState<string[]>([]);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (values: LoginFormValues) => {
    try {
      const auth = await login(values);
      saveAccessToken(auth.accessToken);
      navigate('/dashboard');
    } catch (error: unknown) {
      if (axios.isAxiosError<ApiResponse>(error)) {
        const status = error.response?.status;
        const meta = error.response?.data?.metaData;

        const errors =
          meta?.errors && meta.errors.length > 0
            ? meta.errors
            : [meta?.message || 'Có lỗi xảy ra'];

        if (status === 400) {
          setFormErrors(errors);
          return;
        }
      } else {
        toast.error('Unexpected error');
      }
    }
  };
  return (
    <AuthLayout>
      <div className="login-card">
        <h2 className="login-heading">Đăng nhập</h2>
        {formErrors.length > 0 && (
          <div className="error-box">
            {formErrors.map((error, index) => (
              <div key={index} className="error-item">
                {error}
              </div>
            ))}
          </div>
        )}
        <br />

        <form onSubmit={handleSubmit(onSubmit)}>
          <InputField
            label="Email"
            {...register('email')}
            error={errors.email?.message}
          />

          <InputField
            label="Mật khẩu"
            type="password"
            {...register('password')}
            error={errors.password?.message}
          />

          <Button type="submit" loading={isSubmitting}>
            Đăng nhập
          </Button>
        </form>
      </div>
    </AuthLayout>
  );
}
