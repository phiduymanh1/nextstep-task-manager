import './Button.css';

type Props = {
  loading?: boolean;
} & React.ButtonHTMLAttributes<HTMLButtonElement>;

export default function Button({ children, loading, ...props }: Props) {
  return (
    <button className="btn-primary" {...props}>
      {loading ? 'Đang xử lý...' : children}
    </button>
  );
}
