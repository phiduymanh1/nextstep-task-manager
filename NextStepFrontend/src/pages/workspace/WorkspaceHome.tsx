import { useParams } from 'react-router-dom';

export default function WorkspaceHome() {
  const { slug } = useParams();

  return (
    <>
      <div className="workspace-header">Workspace: {slug}</div>

      <div className="section-title">Các bảng của bạn</div>
    </>
  );
}
