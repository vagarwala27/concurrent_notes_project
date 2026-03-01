import { EditPanel } from "@/components/organisms/edit-panel/edit-panel";
import { Note } from "@/types/note";

const GRAPHQL_URL = "http://localhost:8000/graphql";

async function fetchNoteById(id: string): Promise<Note | null> {
  const response = await fetch(GRAPHQL_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Accept": "application/json",
    },
    body: JSON.stringify({
      query: `
        query NoteById($noteId: ID!) {
          note(noteId: $noteId) {
            id
            content
            color
          }
        }
      `,
      variables: {
        noteId: id,
      },
    }),
    cache: "no-store",
  });
  if (!response.ok) {
    return null;
  }
  const payload = await response.json();
  const note = payload?.data?.note;
  if (!note) {
    return null;
  }
  return {
    id: note.id,
    content: note.content,
    color: note.color,
  };
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
