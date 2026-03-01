"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { NoteCard } from "@/components/molecules/note-card";
import { EditPanel } from "@/components/organisms/edit-panel/edit-panel";
import { Client } from "@stomp/stompjs";
import { Note } from "@/types/note";

interface NotesListProps {
  selectedNoteId?: string;
  pendingNoteId?: string;
}

interface NotesQueryResponse {
  data?: {
    notes?: Array<{
      id: string;
      content: string;
      color?: string;
      summary?: string;
    }>;
  };
}

interface NoteViewModel extends Note {
  summary?: string;
  isLoading?: boolean;
  error?: string;
  lastProcessedTimestamp?: number;
  lastEventTimestamp?: number;
}

interface SummaryEventPayload {
  noteId?: string;
  status?: "PROCESSING" | "COMPLETED" | "FAILED" | "NOTES_REFRESH";
  content?: string;
  summary?: string;
  timestamp?: number;
}

export function NotesList({ selectedNoteId, pendingNoteId }: NotesListProps) {
  const [notes, setNotes] = useState<NoteViewModel[]>([]);
  const [isInitialLoading, setIsInitialLoading] = useState(!pendingNoteId);
  const processingNoteIdsRef = useRef<Set<string>>(new Set());

  const loadNotes = useCallback(async () => {
    try {
      const response = await fetch("http://localhost:8000/graphql", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Accept": "application/json",
        },
        body: JSON.stringify({
          query: `
            query {
              notes {
                id
                content
                color
                summary
              }
            }
          `,
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error("GraphQL Error:", errorText);
        throw new Error("Failed to fetch notes");
      }

      const payload: NotesQueryResponse = await response.json();
      const fetchedNotes = payload.data?.notes ?? [];

      setNotes((currentNotes) => {
        const byId = new Map(currentNotes.map((note) => [note.id, note]));

        return fetchedNotes.map((note) => {
          const current = byId.get(note.id);

          return {
            id: note.id,
            content: note.content,
            color: note.color,
            summary: note.summary ?? current?.summary,
            isLoading:
              (current?.isLoading ?? false) ||
              processingNoteIdsRef.current.has(note.id) ||
              (!note.summary && note.id === pendingNoteId),
            error: current?.error,
            lastProcessedTimestamp: current?.lastProcessedTimestamp ?? 0,
            lastEventTimestamp: current?.lastEventTimestamp ?? 0,
          };
        });
      });
    } finally {
      setIsInitialLoading(false);
    }
  }, [pendingNoteId]);

  useEffect(() => {
    queueMicrotask(() => {
      loadNotes().catch((error) => {
        console.error(error);
      });
    });
  }, [loadNotes]);

  useEffect(() => {
    const client = new Client({
      brokerURL: "ws://localhost:8000/ws",
      onConnect: () => {
        client.subscribe("/topic/note-summaries", (message) => {
          console.log(message.body);
          let payload: SummaryEventPayload;
          try {
            payload = JSON.parse(message.body) as SummaryEventPayload;
          } catch (error) {
            console.error(error);
            return;
          }

          if (!payload.status) {
            return;
          }

          if (payload.status === "NOTES_REFRESH") {
            loadNotes().catch((error) => {
              console.error(error);
            });
            return;
          }

          if (!payload.noteId) {
            return;
          }

          setNotes((currentNotes) => {
            if (!currentNotes.some((note) => note.id === payload.noteId)) {
              if (payload.status === "PROCESSING") {
                processingNoteIdsRef.current.add(payload.noteId);
              }
              queueMicrotask(() => {
                loadNotes().catch((error) => {
                  console.error(error);
                });
              });
              return currentNotes;
            }

            return currentNotes.map((note) => {
              if (note.id !== payload.noteId) {
                return note;
              }

              const incomingTimestamp =
                typeof payload.timestamp === "number"
                  ? payload.timestamp
                  : 0;
              const currentEventTimestamp = note.lastEventTimestamp ?? 0;

              if (incomingTimestamp < currentEventTimestamp) {
                return note;
              }

              if (payload.status === "PROCESSING") {
                processingNoteIdsRef.current.add(payload.noteId);
                return {
                  ...note,
                  isLoading: true,
                  error: undefined,
                  lastEventTimestamp: incomingTimestamp,
                };
              }

              if (payload.status === "COMPLETED") {
                processingNoteIdsRef.current.delete(payload.noteId);
                const currentTimestamp = note.lastProcessedTimestamp ?? 0;

                if (incomingTimestamp < currentTimestamp) {
                  return note;
                }

                return {
                  ...note,
                  content: payload.content ?? note.content,
                  summary: payload.summary ?? note.summary,
                  lastProcessedTimestamp: incomingTimestamp,
                  lastEventTimestamp: incomingTimestamp,
                  isLoading: false,
                  error: undefined,
                };
              }

              if (payload.status === "FAILED") {
                processingNoteIdsRef.current.delete(payload.noteId);
                return {
                  ...note,
                  content: payload.content ?? note.content,
                  lastProcessedTimestamp: incomingTimestamp,
                  lastEventTimestamp: incomingTimestamp,
                  isLoading: false,
                  error: "Summary could not be generated",
                };
              }

              return note;
            });
          });
        });
      },
    });

    client.activate();
    return () => {
      client.deactivate();
    };
  }, [loadNotes]);

  const selectedNote = selectedNoteId
    ? notes.find((n) => n.id === selectedNoteId) ?? null
    : null;

  if (isInitialLoading && !pendingNoteId) {
    return (
      <p className="text-center py-12 text-gray-600">
        Loading notes...
      </p>
    );
  }

  if (notes.length === 0 && !pendingNoteId) {
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
              <div className="space-y-2">
                <NoteCard
                  content={note.content}
                  color={note.color}
                  date={note.date}
                />
                {note.isLoading && (
                  <p className="text-xs text-gray-700 px-1">Processing...</p>
                )}
                {!note.isLoading && note.summary && (
                  <p className="text-xs text-gray-700 px-1">
                    Summary: {note.summary}
                  </p>
                )}
                {note.error && (
                  <p className="text-xs text-red-600 px-1">{note.error}</p>
                )}
              </div>
            </Link>
          </li>
        ))}
      </ul>

      {selectedNote && (
        <EditPanel
          initialNote={selectedNote}
          onNoteUpdated={(updatedNote) => {
            setNotes((currentNotes) =>
              currentNotes.map((note) =>
                note.id === updatedNote.id
                  ? {
                      ...note,
                      content: updatedNote.content,
                      color: updatedNote.color,
                      isLoading: note.content !== updatedNote.content,
                      error: undefined,
                    }
                  : note,
              ),
            );
          }}
          onNoteDeleted={(deletedNoteId) => {
            setNotes((currentNotes) =>
              currentNotes.filter((note) => note.id !== deletedNoteId),
            );
          }}
        />
      )}
    </div>
  );
}
