package com.notes.app.graphql;

import com.notes.app.data.EventLog;
import com.notes.app.data.EventLogRepository;
import com.notes.app.data.Note;
import com.notes.app.service.NoteService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class NoteController {

    private final NoteService noteService;
    private final EventLogRepository eventLogRepository;

    public NoteController(NoteService noteService, EventLogRepository eventLogRepository) {
        this.noteService = noteService;
        this.eventLogRepository = eventLogRepository;
    }

    @QueryMapping
    public List<Note> notes() {
        return noteService.getAllNotes();
    }

    @QueryMapping
    public Note note(@Argument Long noteId) {
        return noteService.getNoteById(noteId).orElse(null);
    }

    @MutationMapping
    public Note createNote(@Argument NoteInput input) {
        return noteService.createNote(input.getContent(), input.getColor() != null ? input.getColor() : "#FCA5A5");
    }

    @MutationMapping
    public Note updateNote(@Argument String noteId, @Argument NoteInput input) {
        Long parsedNoteId = Long.parseLong(noteId);
        return noteService.updateNote(
                parsedNoteId,
                input.getContent(),
                input.getColor() != null ? input.getColor() : "#FCA5A5")
                .orElse(null);
    }

    @MutationMapping
    public boolean deleteNote(@Argument String noteId) {
        Long parsedNoteId = Long.parseLong(noteId);
        return noteService.deleteNote(parsedNoteId);
    }

    @SchemaMapping(typeName = "Note", field = "summary")
    public String summary(Note note) {
        return eventLogRepository
                .findTopByNoteIdAndStatusOrderByCreatedAtDesc(note.getId(), EventLog.Status.COMPLETED)
                .map(EventLog::getSummary)
                .orElse(null);
    }
}
