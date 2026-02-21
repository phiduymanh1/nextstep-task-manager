import '@/components/layout/boardCardLayout/BoardCard.css';

interface Props {
  title: string;
  image: string;
}

export default function BoardCard({ title, image }: Props) {
  return (
    <div className="board-card" style={{ backgroundImage: `url(${image})` }}>
      <div className="board-overlay" />
      <div className="board-title">{title}</div>
    </div>
  );
}
