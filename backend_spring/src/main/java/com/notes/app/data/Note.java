package com.notes.app.data;

import jakarta.persistence.*;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// This is our Model (MVC)
@Entity
@Table(name = "NOTES")
@EntityListeners(AuditingEntityListener.class)
public class Note {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private Long id;

  @Column(name = "CONTENT")
  private String content;

  @Column(name = "COLOR")
  private String color;

  @CreatedDate
  @Column(name = "CREATED_AT")
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "UPDATED_AT")
  private Instant updatedAt;

  public Note() {
  }

  public Note(String content, String color) {
    this.content = content;
    this.color = color;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
