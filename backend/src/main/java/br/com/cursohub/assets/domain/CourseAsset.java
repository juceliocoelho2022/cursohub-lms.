package br.com.cursohub.assets.domain;

import java.util.UUID;

/** Metadata only; no bytes are serialized into course listings. */
public record CourseAsset(UUID id, UUID courseId, UUID lessonId, String filename,
                          String mediaType, long sizeBytes) {}
