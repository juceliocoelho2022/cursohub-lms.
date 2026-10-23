package br.com.cursohub.catalog.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Entidade de catálogo sem framework. Dinheiro em centavos inteiros de BRL. */
public record Course(UUID id,String slug,String title,String description,
                     String category,CourseLevel level,CourseStatus status,
                     UUID instructorId,int estimatedMinutes,String thumbnailUrl,
                     Instant publishedAt,int priceCents) {
    public Course(UUID id,String slug,String title,String description,
                  String category,CourseLevel level,CourseStatus status,
                  UUID instructorId,int estimatedMinutes,String thumbnailUrl,Instant publishedAt) {
        this(id,slug,title,description,category,level,status,instructorId,estimatedMinutes,thumbnailUrl,publishedAt,0);
    }
    public Course {
        Objects.requireNonNull(id);Objects.requireNonNull(slug);Objects.requireNonNull(title);
        Objects.requireNonNull(level);Objects.requireNonNull(status);
        if(estimatedMinutes<0) throw new IllegalArgumentException("Duração inválida");
        if(priceCents<0||priceCents>2000000) throw new IllegalArgumentException("Preço inválido");
        if(status==CourseStatus.PUBLICADO&&publishedAt==null)
            throw new IllegalArgumentException("Curso publicado exige data de publicação");
    }
    public boolean isPublished(){return status==CourseStatus.PUBLICADO;}
}
