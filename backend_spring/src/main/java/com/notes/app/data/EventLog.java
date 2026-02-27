package com.notes.app.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "EVENT_LOGS")
@EntityListeners(AuditingEntityListener.class)
public class EventLog {

  public enum Status {
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private Long id;

  @Column(name = "NOTE_ID", nullable = false)
  private Long noteId;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  private Status status;

  @Column(name = "SUMMARY")
  private String summary;

  @CreatedDate
  @Column(name = "CREATED_AT", nullable = false, updatable = false)
  private Instant createdAt;

  public EventLog() {
  }

  public EventLog(Long noteId, Status status, String summary) {
    this.noteId = noteId;
    this.status = status;
    this.summary = summary;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getNoteId() {
    return noteId;
  }

  public void setNoteId(Long noteId) {
    this.noteId = noteId;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
