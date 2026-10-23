package br.com.cursohub.instructor.application.model;

import br.com.cursohub.learning.domain.LessonKind;

public record CreateLessonCommand(String title, LessonKind kind, String contentUrl,
                                  String bodyText, int durationSeconds, boolean required) {}
