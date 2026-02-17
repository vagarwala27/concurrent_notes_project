package com.notes.app.service;

import com.notes.app.data.Note;
import com.notes.app.data.NoteRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
  private static final String NOTE_SUMMARY_EVENT_QUEUE = "note_summary_event_queue";

  private final NoteRepository noteRepository;
  private final StringRedisTemplate redis;

  public NoteService(NoteRepository noteRepository, StringRedisTemplate redis) {
    this.noteRepository = noteRepository;
    this.redis = redis;
  }

  public List<Note> getAllNotes() {
    return noteRepository.findAllByOrderByCreatedAtDesc();
  }

  public Optional<Note> getNoteById(Long noteId) {
    return noteRepository.findById(noteId);
  }

  public Note createNote(String content, String color) {
    // validate content and color
    Note note = noteRepository.save(new Note(content, color));

    String event = note.getId() + "::" + content;
    redis.opsForList().rightPush(NOTE_SUMMARY_EVENT_QUEUE, event);

    System.out.println("NoteService.java: Queued summary job for note: " + note.getId());

    return note;
  }
}
