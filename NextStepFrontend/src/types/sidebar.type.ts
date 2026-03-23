import type { Workspace } from "./workspace.type";

export type SidebarProps = {
  onSelectWorkspace: (ws: Workspace) => void;
  onChangeTab: (tab: string) => void;
  activeTab: string;
};
