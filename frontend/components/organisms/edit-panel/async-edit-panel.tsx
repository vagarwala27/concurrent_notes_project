import { EditPanel } from "@/components/organisms/edit-panel/edit-panel";
import { Note } from "@/types/note";

const API_URL = process.env.API_URL || "http://127.0.0.1:8000";

async function fetchNoteById(id: string): Promise<Note | null> {
  // To simulate DB latency, uncomment the following line:
  await new Promise((resolve) => setTimeout(resolve, 1000));

  const response = await fetch(`${API_URL}/api/notes/${id}`, {
    cache: "no-store",
  });
  if (!response.ok) {
    return null;
  }
  return response.json();
}

interface AsyncEditPanelProps {
  noteId: string;
}

export async function AsyncEditPanel({ noteId }: AsyncEditPanelProps) {
  const note = await fetchNoteById(noteId);

  if (!note) {
    return (
      <aside className="w-96 sticky top-0 h-fit">
        <div className="bg-white p-6 rounded-lg shadow-sm">
          <p className="text-gray-500">Note not found</p>
        </div>
      </aside>
    );
  }

  return <EditPanel initialNote={note} />;
}
