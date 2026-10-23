package br.com.cursohub.loyalty.adapters.out;

import br.com.cursohub.loyalty.application.port.in.LoyaltyUseCase;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LoyaltyPostgresAdapter implements LoyaltyUseCase {
    private final JdbcTemplate jdbc;
    public LoyaltyPostgresAdapter(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override @Transactional(readOnly=true)
    public Summary summary(UUID studentId) {
        Long completed=jdbc.queryForObject("""
          SELECT COUNT(*) FROM enrollments e
          WHERE e.student_id=? AND e.status IN ('ACTIVE','COMPLETED')
            AND EXISTS(
              SELECT 1 FROM lessons l JOIN course_modules m ON m.id=l.module_id
              WHERE m.course_id=e.course_id AND (l.is_required=TRUE OR l.kind='VIDEO'))
            AND NOT EXISTS(
              SELECT 1 FROM lessons l JOIN course_modules m ON m.id=l.module_id
              WHERE m.course_id=e.course_id AND (l.is_required=TRUE OR l.kind='VIDEO')
                AND NOT EXISTS(
                  SELECT 1 FROM lesson_progress p
                  WHERE p.enrollment_id=e.id AND p.lesson_id=l.id))
          """,Long.class,studentId);
        int total=Math.toIntExact(completed==null?0:completed);
        String tier=total>=5?"OURO":total>=2?"PRATA":"BRONZE";
        int next=total>=5?5:total>=2?5:2;
        return new Summary(tier,total,Math.multiplyExact(total,100),next,
            "Pontos e níveis reconhecem cursos concluídos. Descontos e resgates ainda não estão disponíveis.");
    }
}
