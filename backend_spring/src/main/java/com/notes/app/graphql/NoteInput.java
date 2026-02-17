package com.notes.app.graphql;

public class NoteInput {
  private String content;
  private String color;

  public NoteInput() {
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
}