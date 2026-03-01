"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { NoteCard } from "@/components/molecules/note-card";
import { EditPanel } from "@/components/organisms/edit-panel/edit-panel";
import { Client } from "@stomp/stompjs";
import { Note } from "@/types/note";

interface NotesListProps {
  selectedNoteId?: string;
}

interface NotesQueryResponse {
  data?: {
    notes?: Array<{
      id: string;
      content: string;
      color?: string;
      updatedAt?: string;
    }>;
  };
}

export function NotesList({ selectedNoteId }: NotesListProps) {
  const [notes, setNotes] = useState<Note[]>([]);

  useEffect(() => {
    const loadNotes = async () => {
      const response = await fetch("/graphql", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          query: `
            query {
              notes {
                id
                content
                color
                updatedAt
              }
            }
          `,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to fetch notes");
      }

      const payload: NotesQueryResponse = await response.json();
      const mappedNotes: Note[] =
        payload.data?.notes?.map((note) => ({
          id: note.id,
          content: note.content,
          color: note.color,
          date: note.updatedAt,
        })) ?? [];
      setNotes(mappedNotes);
    };

    loadNotes().catch((error) => {
      console.error(error);
    });
  }, []);

  useEffect(() => {
    const client = new Client({
      brokerURL: "ws://localhost:8000/ws",
      onConnect: () => {
        client.subscribe("/topic/note-summaries", (message) => {
          console.log(message.body);
        });
      },
    });

    client.activate();
    return () => {
      client.deactivate();
    };
  }, []);

  const selectedNote = selectedNoteId
    ? notes.find((n) => n.id === selectedNoteId) ?? null
    : null;

  if (notes.length === 0) {
    return (
      <p className="text-center py-12 text-gray-600">
        No notes yet. Add your first note!
      </p>
    );
  }

  return (
    <div className="flex gap-6">
      <ul className="grid gap-4 grid-cols-1 md:grid-cols-2 lg:grid-cols-3 auto-rows-max list-none flex-1">
        {notes.map((note) => (
          <li key={note.id}>
            <Link href={`/?noteId=${note.id}`} scroll={false}>
              <NoteCard
                content={note.content}
                color={note.color}
                date={note.date}
              />
            </Link>
          </li>
        ))}
      </ul>

      {selectedNote && (
        <EditPanel initialNote={selectedNote} />
      )}
    </div>
  );
}
