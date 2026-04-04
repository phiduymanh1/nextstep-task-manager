import { useState } from 'react';
import { createBoardLabel } from '@/services/label.service';

interface Label {
  id: string;
  name: string;
  color: string;
}

interface Props {
  boardLabels: Label[];
  selectedIds: string[];
  onToggle: (id: string) => void;
  onClose: () => void;
  onAddLabel: (label: Label) => void;
  boardSlug: string;
}

export default function LabelModal({
  boardLabels,
  selectedIds,
  onToggle,
  onClose,
  onAddLabel,
  boardSlug,
}: Props) {
  const [creating, setCreating] = useState(false);
  const [name, setName] = useState('');
  const [color, setColor] = useState('#579dff');

  const handleCreate = async () => {
    if (!name.trim()) return;

    try {
      const newLabel = await createBoardLabel(boardSlug, {
        name: name.trim(),
        color,
      });

      // update state cha (chuẩn React)
      onAddLabel(newLabel);

      setName('');
      setCreating(false);
    } catch (err) {
      console.error('Create label error:', err);
    }
  };

  return (
    <div className="cdm-inner-modal">
      <div className="cdm-inner-modal-header">
        <span>Nhãn</span>
        <button onClick={onClose}>✕</button>
      </div>

      <div className="cdm-inner-modal-body">
        {boardLabels.length === 0 && <p>Chưa có nhãn</p>}

        {boardLabels.map((l) => {
          const checked = selectedIds.includes(l.id);

          return (
            <div key={l.id} onClick={() => onToggle(l.id)}>
              <span style={{ background: l.color }}>{l.name}</span>

              <input
                type="checkbox"
                checked={checked}
                onChange={() => onToggle(l.id)}
                onClick={(e) => e.stopPropagation()}
              />
            </div>
          );
        })}

        {/* CREATE */}
        {!creating ? (
          <button onClick={() => setCreating(true)}>+ Tạo nhãn</button>
        ) : (
          <div>
            <input
              placeholder="Tên nhãn"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />

            <input
              type="color"
              value={color}
              onChange={(e) => setColor(e.target.value)}
            />

            <button onClick={handleCreate}>Tạo</button>
            <button onClick={() => setCreating(false)}>Hủy</button>
          </div>
        )}
      </div>
    </div>
  );
}
