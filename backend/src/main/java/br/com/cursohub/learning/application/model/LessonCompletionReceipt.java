package br.com.cursohub.learning.application.model;

import java.util.UUID;

/** created=false indica conclusão já registrada anteriormente. */
public record LessonCompletionReceipt(UUID lessonId, boolean newlyCompleted) {}
