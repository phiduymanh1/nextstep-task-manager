import './Login.css';
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

export default function Login() {
  const navigate = useNavigate();

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
    } catch (err: unknown) {
      if (err instanceof Error) {
        toast.error(err.message);
      } else {
        toast.error('Một lỗi không xác định đã xảy ra');
      }
    }
  };
  return (
    <AuthLayout>
      <div className="login-card">
        <h2 className="login-heading">Đăng nhập</h2>

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
