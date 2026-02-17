import { Sidebar } from "@/components/organisms/sidebar";
import { NotesList } from "@/components/organisms/notes-list";
import { Header } from "@/components/organisms/header";

interface PageProps {
  searchParams: Promise<{ noteId?: string }>;
}

export default async function Home({ searchParams }: PageProps) {
  const { noteId } = await searchParams;

  return (
    <>
      <Sidebar />

      <main className="flex-1 p-8 overflow-y-auto bg-gray-50">
        <Header title="Notes" />

        <NotesList selectedNoteId={noteId} />
      </main>
    </>
  );
}
