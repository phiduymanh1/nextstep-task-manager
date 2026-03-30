import { useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
  closestCorners,
  type DragStartEvent,
  type DragOverEvent,
  type DragEndEvent,
} from '@dnd-kit/core';
import {
  SortableContext,
  useSortable,
  verticalListSortingStrategy,
  arrayMove,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

import '@/assets/styles/BoardDetail.css';

type Task = { id: string; title: string };
type Column = { id: string; title: string; tasks: Task[] };

// ===== SortableTask =====
function SortableTask({ task }: { task: Task }) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: task.id, data: { type: 'task', task } });

  const style: React.CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.4 : 1, // bản gốc mờ, DragOverlay hiện clone
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className="task-card"
      {...attributes}
      {...listeners}
    >
      {task.title}
    </div>
  );
}

// ===== TaskOverlay (clone theo con trỏ) =====
function TaskOverlay({ task }: { task: Task }) {
  return <div className="task-card is-dragging">{task.title}</div>;
}

// ===== BoardDetail =====
export default function BoardDetail() {
  const { boardSlug } = useParams();

  const [columns, setColumns] = useState<Column[]>([
    {
      id: '1',
      title: 'Welcome',
      tasks: [
        { id: '1', title: 'Done' },
        { id: '2', title: 'Doing' },
        { id: '3', title: 'Pending' },
      ],
    },
    {
      id: '2',
      title: 'Nhiệm vụ',
      tasks: [
        { id: '4', title: 'Test phía admin' },
        { id: '5', title: 'Thêm bộ lọc myorder' },
      ],
    },
    {
      id: '3',
      title: 'Đang làm',
      tasks: [{ id: '6', title: 'Xây dựng API CRUD + bảo hành' }],
    },
    { id: '4', title: 'Review', tasks: [] },
    { id: '5', title: 'Bug', tasks: [] },
  ]);

  const [activeTask, setActiveTask] = useState<Task | null>(null);

  // PointerSensor với distance nhỏ để tránh click nhầm
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { distance: 5 },
    })
  );

  // Tìm column chứa taskId
  const findColumn = (taskId: string) =>
    columns.find((col) => col.tasks.some((t) => t.id === taskId));

  const handleDragStart = (event: DragStartEvent) => {
    const task = event.active.data.current?.task as Task;
    setActiveTask(task ?? null);
  };

  const handleDragOver = (event: DragOverEvent) => {
    const { active, over } = event;
    if (!over) return;

    const activeId = String(active.id);
    const overId = String(over.id);
    if (activeId === overId) return;

    const activeCol = findColumn(activeId);
    // over có thể là task hoặc column
    const overCol = findColumn(overId) ?? columns.find((c) => c.id === overId);

    if (!activeCol || !overCol || activeCol.id === overCol.id) return;

    // Di chuyển task sang column khác ngay khi hover
    setColumns((prev) => {
      const activeTask = activeCol.tasks.find((t) => t.id === activeId)!;
      const overTaskIndex = overCol.tasks.findIndex((t) => t.id === overId);
      const insertIndex =
        overTaskIndex >= 0 ? overTaskIndex : overCol.tasks.length;

      return prev.map((col) => {
        if (col.id === activeCol.id) {
          return { ...col, tasks: col.tasks.filter((t) => t.id !== activeId) };
        }
        if (col.id === overCol.id) {
          const newTasks = [...col.tasks];
          newTasks.splice(insertIndex, 0, activeTask);
          return { ...col, tasks: newTasks };
        }
        return col;
      });
    });
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveTask(null);
    if (!over) return;

    const activeId = String(active.id);
    const overId = String(over.id);
    if (activeId === overId) return;

    // Sắp xếp lại trong cùng column
    setColumns((prev) =>
      prev.map((col) => {
        const oldIndex = col.tasks.findIndex((t) => t.id === activeId);
        const newIndex = col.tasks.findIndex((t) => t.id === overId);
        if (oldIndex !== -1 && newIndex !== -1) {
          return { ...col, tasks: arrayMove(col.tasks, oldIndex, newIndex) };
        }
        return col;
      })
    );
  };

  return (
    <div className="board-container">
      <div className="board-header">
        <h2>{boardSlug}</h2>
      </div>

      <div className="board-scroll-area">
        <DndContext
          sensors={sensors}
          collisionDetection={closestCorners}
          onDragStart={handleDragStart}
          onDragOver={handleDragOver}
          onDragEnd={handleDragEnd}
        >
          <div className="board-columns">
            {columns.map((col) => (
              <div key={col.id} className="board-column">
                <div className="column-title">{col.title}</div>

                <SortableContext
                  items={col.tasks.map((t) => t.id)}
                  strategy={verticalListSortingStrategy}
                >
                  <div className="task-list">
                    {col.tasks.map((task) => (
                      <SortableTask key={task.id} task={task} />
                    ))}
                  </div>
                </SortableContext>

                <button className="add-task">+ Thêm thẻ</button>
              </div>
            ))}
          </div>

          {/* DragOverlay: render clone đúng vị trí con trỏ, không bị lệch */}
          <DragOverlay>
            {activeTask ? <TaskOverlay task={activeTask} /> : null}
          </DragOverlay>
        </DndContext>
      </div>
    </div>
  );
}
