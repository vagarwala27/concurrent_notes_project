package com.notes.app.worker;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import net.devh.boot.grpc.client.inject.GrpcClient;

import com.google.protobuf.InvalidProtocolBufferException;
import com.notes.app.data.EventLog;
import com.notes.app.data.EventLogRepository;
import com.notes.app.grpc.NoteSummaryEvent;
import com.notes.app.grpc.NoteSummaryRequest;
import com.notes.app.grpc.NoteSummaryResponse;
import com.notes.app.grpc.NoteSummaryServiceGrpc;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
public class NoteSummaryWorker {
  private static final String NOTE_CREATED_QUEUE = "note_created";

  private final RedisTemplate<String, byte[]> redisTemplate;
  private final SimpMessagingTemplate messagingTemplate;
  private final EventLogRepository eventLogRepository;

  @GrpcClient("note-summary-service")
  private NoteSummaryServiceGrpc.NoteSummaryServiceBlockingStub mockSummaryService;

  public NoteSummaryWorker(RedisTemplate<String, byte[]> redisTemplate, SimpMessagingTemplate messagingTemplate,
      EventLogRepository eventLogRepository) {
    this.redisTemplate = redisTemplate;
    this.messagingTemplate = messagingTemplate;
    this.eventLogRepository = eventLogRepository;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void startWorker() {
    new Thread(() -> {
      System.out
          .println("NoteSummaryWorker.java: Summary Worker started. Listening for note_created events");
      while (true) {
        try {
          byte[] eventBytes = redisTemplate.opsForList().leftPop(NOTE_CREATED_QUEUE, Duration.ofSeconds(30));

          if (eventBytes != null) {
            NoteSummaryEvent event = NoteSummaryEvent.parseFrom(eventBytes);
            System.out.println("NoteSummaryWorker.java: Received eventId: " + event.getEventId());
            generateSummary(event);
          }
        } catch (InvalidProtocolBufferException e) {
          System.err.println("Error parsing NoteSummaryEvent payload: " + e.getMessage());
        } catch (Exception e) {
          if (e.getMessage() != null && e.getMessage().contains("timed out")) {
            continue;
          }
          System.err.println("Error processing event: " + e.getMessage());
          e.printStackTrace();
        }
      }
    }).start();
  }

  private void generateSummary(NoteSummaryEvent event) {
    String noteId = event.getNoteId();
    String content = event.getContent();
    Optional<EventLog> eventLog = Optional.empty();

    try {
      Long eventLogId = Long.parseLong(event.getEventId());
      eventLog = eventLogRepository.findById(eventLogId);
      if (eventLog.isPresent()) {
        EventLog log = eventLog.get();
        log.setStatus(EventLog.Status.PROCESSING);
        eventLogRepository.save(log);
      }
    } catch (NumberFormatException e) {
      System.err.println("Invalid eventId in NoteSummaryEvent: " + event.getEventId());
    }

    try {
      NoteSummaryResponse response = mockSummaryService.getNoteSummary(
          NoteSummaryRequest.newBuilder().setContent(content).setNoteId(noteId).build());

      if (eventLog.isPresent()) {
        EventLog log = eventLog.get();
        log.setStatus(EventLog.Status.COMPLETED);
        log.setSummary(response.getSummary());
        eventLogRepository.save(log);
      }

      Map<String, Object> payload = Map.of(
          "noteId", noteId,
          "status", "COMPLETED",
          "summary", response.getSummary(),
          "timestamp", System.currentTimeMillis());
      messagingTemplate.convertAndSend("/topic/note-summaries", payload);
    } catch (Exception e) {
      if (eventLog.isPresent()) {
        EventLog log = eventLog.get();
        log.setStatus(EventLog.Status.FAILED);
        eventLogRepository.save(log);
      }

      Map<String, Object> payload = Map.of(
          "noteId", noteId,
          "status", "FAILED");
      messagingTemplate.convertAndSend("/topic/note-summaries", payload);
    }

    /**
     * Paste into localhost:8000 dev console to test WebSocket connection:
     * const script = document.createElement('script');
     * script.src =
     * 'https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/bundles/stomp.umd.min.js';
     * script.onload = () => {
     * const client = new StompJs.Client({
     * brokerURL: 'ws://localhost:8000/ws',
     * debug: (str) => console.log(str),
     * onConnect: () => {
     * console.log('Connected!');
     * client.subscribe('/topic/note-summaries',
     * (msg) => {
     * console.log('Received:', msg.body);
     * });
     * }
     * });
     * client.activate();
     * window.stompClient = client;
     * };
     * document.head.appendChild(script)
     */
  }
}
