import './InputField.css';

type Props = {
  label: string;
  type?: string;
  error?: string;
} & React.InputHTMLAttributes<HTMLInputElement>;

export default function InputField({
  label,
  type = 'text',
  error,
  ...props
}: Props) {
  return (
    <div className="input-group">
      <label>{label}</label>
      <input
        type={type}
        className={error ? 'input error' : 'input'}
        {...props}
      />
      {error && <p className="error-text">{error}</p>}
    </div>
  );
}
