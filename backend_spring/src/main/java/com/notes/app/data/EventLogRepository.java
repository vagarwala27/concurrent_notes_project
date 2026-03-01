package com.notes.app.data;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {
  List<EventLog> findAllByNoteIdOrderByCreatedAtAsc(Long noteId);

  Optional<EventLog> findTopByNoteIdAndStatusOrderByCreatedAtDesc(Long noteId, EventLog.Status status);
}
