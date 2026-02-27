package com.notes.app.service;

import com.notes.app.data.EventLog;
import com.notes.app.data.EventLogRepository;
import com.notes.app.data.Note;
import com.notes.app.data.NoteRepository;
import com.notes.app.grpc.NoteSummaryEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
  private static final String NOTE_CREATED_QUEUE = "note_created";

  private final NoteRepository noteRepository;
  private final RedisTemplate<String, byte[]> redisTemplate;
  private final EventLogRepository eventLogRepository;

  public NoteService(NoteRepository noteRepository, RedisTemplate<String, byte[]> redisTemplate,
      EventLogRepository eventLogRepository) {
    this.noteRepository = noteRepository;
    this.redisTemplate = redisTemplate;
    this.eventLogRepository = eventLogRepository;
  }

  public List<Note> getAllNotes() {
    return noteRepository.findAllByOrderByCreatedAtDesc();
  }

  public Optional<Note> getNoteById(Long noteId) {
    return noteRepository.findById(noteId);
  }

  public Note createNote(String content, String color) {
    Note note = noteRepository.save(new Note(content, color));

    EventLog eventLog = new EventLog();
    eventLog.setNoteId(note.getId());
    eventLog.setStatus(EventLog.Status.QUEUED);
    EventLog savedEventLog = eventLogRepository.save(eventLog);

    NoteSummaryEvent event = NoteSummaryEvent.newBuilder()
        .setEventId(savedEventLog.getId().toString())
        .setNoteId(note.getId().toString())
        .setContent(note.getContent())
        .setTimestamp(System.currentTimeMillis())
        .build();

    redisTemplate.opsForList().leftPush(NOTE_CREATED_QUEUE, event.toByteArray());

    System.out.println("NoteService.java: Queued summary job for note: " + note.getId());

    return note;
  }
}
