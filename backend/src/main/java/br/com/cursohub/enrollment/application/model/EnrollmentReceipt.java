package br.com.cursohub.enrollment.application.model;

import java.util.UUID;

/** Dados retornados pela matrícula; created distingue 201 de 200 idempotente. */
public record EnrollmentReceipt(UUID enrollmentId, UUID courseId, String courseSlug,
                                String status, boolean created) {}
