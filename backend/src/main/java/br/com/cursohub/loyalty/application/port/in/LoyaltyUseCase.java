package br.com.cursohub.loyalty.application.port.in;

import java.util.UUID;
public interface LoyaltyUseCase {
    /** Pontos acumulados exclusivamente com aulas obrigatórias concluídas. Sem descontos aplicados. */
    record Summary(String tier,int completedCourses,int points,int nextTierAt,
                   String notice) {}
    Summary summary(UUID studentId);
}
