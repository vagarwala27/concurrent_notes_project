package com.notes.app.service;

import com.notes.app.data.EventLog;
import com.notes.app.data.EventLogRepository;
import com.notes.app.data.Note;
import com.notes.app.data.NoteRepository;
import com.notes.app.grpc.NoteSummaryEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NoteService {
  private static final String NOTE_CREATED_QUEUE = "note_created";
  private static final AtomicLong LAST_EVENT_TIMESTAMP = new AtomicLong(0L);

  private final NoteRepository noteRepository;
  private final RedisTemplate<String, byte[]> redisTemplate;
  private final EventLogRepository eventLogRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public NoteService(NoteRepository noteRepository, RedisTemplate<String, byte[]> redisTemplate,
      EventLogRepository eventLogRepository, SimpMessagingTemplate messagingTemplate) {
    this.noteRepository = noteRepository;
    this.redisTemplate = redisTemplate;
    this.eventLogRepository = eventLogRepository;
    this.messagingTemplate = messagingTemplate;
  }

  public List<Note> getAllNotes() {
    return noteRepository.findAllByOrderByCreatedAtDesc();
  }

  public Optional<Note> getNoteById(Long noteId) {
    return noteRepository.findById(noteId);
  }

  public Note createNote(String content, String color) {
    Note note = noteRepository.save(new Note(content, color));
    long eventTimestamp = nextEventTimestamp();

    messagingTemplate.convertAndSend("/topic/note-summaries", Map.of(
        "status", "NOTES_REFRESH",
        "timestamp", eventTimestamp));

    EventLog eventLog = new EventLog();
    eventLog.setNoteId(note.getId());
    eventLog.setStatus(EventLog.Status.QUEUED);
    EventLog savedEventLog = eventLogRepository.save(eventLog);

    NoteSummaryEvent event = NoteSummaryEvent.newBuilder()
        .setEventId(savedEventLog.getId().toString())
        .setNoteId(note.getId().toString())
        .setContent(note.getContent())
        .setTimestamp(eventTimestamp)
        .build();

    redisTemplate.opsForList().leftPush(NOTE_CREATED_QUEUE, event.toByteArray());

    System.out.println("NoteService.java: Queued summary job for note: " + note.getId());

    return note;
  }

  public Optional<Note> updateNote(Long noteId, String content, String color) {
    return noteRepository.findById(noteId).map(note -> {
      note.setContent(content);
      note.setColor(color);
      Note updatedNote = noteRepository.save(note);
      long eventTimestamp = nextEventTimestamp();

      messagingTemplate.convertAndSend("/topic/note-summaries", Map.of(
          "status", "NOTES_REFRESH",
          "timestamp", eventTimestamp));

      EventLog eventLog = new EventLog();
      eventLog.setNoteId(updatedNote.getId());
      eventLog.setStatus(EventLog.Status.QUEUED);
      EventLog savedEventLog = eventLogRepository.save(eventLog);

      NoteSummaryEvent event = NoteSummaryEvent.newBuilder()
          .setEventId(savedEventLog.getId().toString())
          .setNoteId(updatedNote.getId().toString())
          .setContent(updatedNote.getContent())
          .setTimestamp(eventTimestamp)
          .build();

      redisTemplate.opsForList().leftPush(NOTE_CREATED_QUEUE, event.toByteArray());

      return updatedNote;
    });
  }

  public boolean deleteNote(Long noteId) {
    if (!noteRepository.existsById(noteId)) {
      return false;
    }
    noteRepository.deleteById(noteId);
    messagingTemplate.convertAndSend("/topic/note-summaries", Map.of(
        "status", "NOTES_REFRESH",
        "timestamp", nextEventTimestamp()));
    return true;
  }

  private long nextEventTimestamp() {
    long now = System.currentTimeMillis();
    return LAST_EVENT_TIMESTAMP.updateAndGet(previous -> now > previous ? now : previous + 1);
  }
}
