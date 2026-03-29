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
    visibility: '' as 'PUBLIC' | 'PRIVATE' | 'WORKSPACE' | '',
  });

  const [error, setError] = useState('');

  const handleCreate = async () => {
    try {
      setError('');

      const parsed = workspaceSchema.parse(form);

      await createWorkspace(parsed);

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

        {/* NAME */}
        <input
          placeholder="Name"
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />

        {/* DESCRIPTION */}
        <textarea
          placeholder="Description"
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />

        {/* 🔥 VISIBILITY (NEW UI) */}
        <div className="visibility-group">
          <button
            className={form.visibility === 'PUBLIC' ? 'active' : ''}
            onClick={() => setForm({ ...form, visibility: 'PUBLIC' })}
            type="button"
          >
            🌍 Public
          </button>

          <button
            className={form.visibility === 'WORKSPACE' ? 'active' : ''}
            onClick={() => setForm({ ...form, visibility: 'WORKSPACE' })}
            type="button"
          >
            👥 Workspace
          </button>

          <button
            className={form.visibility === 'PRIVATE' ? 'active' : ''}
            onClick={() => setForm({ ...form, visibility: 'PRIVATE' })}
            type="button"
          >
            🔒 Private
          </button>
        </div>

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
