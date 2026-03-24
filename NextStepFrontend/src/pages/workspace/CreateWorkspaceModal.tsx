import { useState } from 'react';
import { workspaceSchema } from '@/schema/workspace.schema';
import { ZodError } from 'zod';
import { createPortal } from 'react-dom';
import '@/assets/styles/CreateWorkspace.css';
import { createWorkspace } from '@/services/workspace.service';

type Props = {
  open: boolean;
  onClose: () => void;
};

export default function CreateWorkspaceModal({ open, onClose }: Props) {
  const [form, setForm] = useState({
    name: '',
    description: '',
    visibility: '' as 'PUBLIC' | 'PRIVATE' | '',
  });

  const [error, setError] = useState('');

  const handleCreate = async () => {
    try {
      setError('');

      const parsed = workspaceSchema.parse(form);

      await createWorkspace(parsed);

      // reset form
      setForm({
        name: '',
        description: '',
        visibility: '',
      });

      onClose();
    } catch (err) {
      if (err instanceof ZodError) {
        setError(err.issues[0].message);
      } else {
        setError('Something went wrong');
      }
    }
  };

  if (!open) return null;

  return createPortal(
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>Create Workspace</h3>

        <input
          placeholder="Name"
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />

        <textarea
          placeholder="Description"
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />

        <select
          value={form.visibility}
          onChange={(e) =>
            setForm({
              ...form,
              visibility: e.target.value as 'PUBLIC' | 'PRIVATE',
            })
          }
        >
          <option value="" disabled>
            Select visibility
          </option>
          <option value="PUBLIC">Public</option>
          <option value="PRIVATE">Private</option>
        </select>

        {error && <p className="error">{error}</p>}

        <div className="actions">
          <button onClick={handleCreate}>Create</button>
          <button onClick={onClose}>Cancel</button>
        </div>
      </div>
    </div>,
    document.body
  );
}
