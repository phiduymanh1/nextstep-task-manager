import { useEffect, useState } from 'react';
import type { Visibility } from '@/types/workspace.type';
import '@/assets/styles/CreateWorkspace.css';
import { workspaceSchema } from '@/schema/workspace.schema';
import { ZodError } from 'zod';
import toast from 'react-hot-toast';

type Props = {
  open: boolean;
  onClose: () => void;
  defaultData: {
    name: string;
    visibility: Visibility;
  };
  onSubmit: (data: {
    name: string;
    description?: string;
    visibility: Visibility;
  }) => Promise<void>;
};

export default function UpdateWorkspaceModal({
  open,
  onClose,
  defaultData,
  onSubmit,
}: Props) {
  const [form, setForm] = useState({
    name: '',
    description: '',
    visibility: 'PRIVATE' as Visibility,
  });

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // set data khi mở modal
  useEffect(() => {
    if (open) {
      setForm({
        name: defaultData.name,
        description: '',
        visibility: defaultData.visibility,
      });
      setError('');
    }
  }, [defaultData, open]);

  if (!open) return null;

  const handleUpdate = async () => {
    try {
      setError('');
      setLoading(true);

      // validate giống create
      const parsed = workspaceSchema.parse(form);

      await onSubmit({
        name: parsed.name,
        visibility: parsed.visibility,
        ...(parsed.description && { description: parsed.description }),
      });

      onClose();
    } catch (err) {
      if (err instanceof ZodError) {
        setError(err.issues[0].message); // giống create
      } else {
        toast.error('Update failed');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>Update Workspace</h3>

        {/* NAME */}
        <input
          placeholder="Name"
          value={form.name}
          onChange={(e) => {
            setForm({ ...form, name: e.target.value });
            setError('');
          }}
        />

        {/* DESCRIPTION */}
        <textarea
          placeholder="Description"
          value={form.description}
          onChange={(e) => {
            setForm({
              ...form,
              description: e.target.value,
            });
            setError('');
          }}
        />

        {/* VISIBILITY */}
        <div className="visibility-group">
          <button
            type="button"
            className={`visibility-btn ${
              form.visibility === 'PUBLIC' ? 'active' : ''
            }`}
            onClick={() => {
              setForm({ ...form, visibility: 'PUBLIC' });
              setError('');
            }}
          >
            🌍 Public
          </button>

          <button
            type="button"
            className={`visibility-btn ${
              form.visibility === 'WORKSPACE' ? 'active' : ''
            }`}
            onClick={() => {
              setForm({
                ...form,
                visibility: 'WORKSPACE',
              });
              setError('');
            }}
          >
            👥 Workspace
          </button>

          <button
            type="button"
            className={`visibility-btn ${
              form.visibility === 'PRIVATE' ? 'active' : ''
            }`}
            onClick={() => {
              setForm({
                ...form,
                visibility: 'PRIVATE',
              });
              setError('');
            }}
          >
            🔒 Private
          </button>
        </div>

        {/* ERROR */}
        {error && <p className="error">{error}</p>}

        {/* ACTIONS */}
        <div className="actions">
          <button className="primary" onClick={handleUpdate} disabled={loading}>
            {loading ? 'Đang lưu...' : 'Update'}
          </button>

          <button className="secondary" onClick={onClose} disabled={loading}>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}
