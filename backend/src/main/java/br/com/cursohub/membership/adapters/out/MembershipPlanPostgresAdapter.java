package br.com.cursohub.membership.adapters.out;

import br.com.cursohub.membership.application.port.out.MembershipPlanReaderPort;
import br.com.cursohub.membership.domain.MembershipPlan;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MembershipPlanPostgresAdapter implements MembershipPlanReaderPort {
    private final JdbcTemplate jdbc;
    public MembershipPlanPostgresAdapter(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override @Transactional(readOnly=true)
    public List<MembershipPlan> listAvailable() {
        return jdbc.query("""
            SELECT code,title,duration_months,price_cents,course_limit,discount_percent,benefits
            FROM membership_plans WHERE active=TRUE ORDER BY sort_order
            """,(rs,i)->{
                Integer limit=rs.getObject("course_limit",Integer.class);
                String[] perks=(String[])rs.getArray("benefits").getArray();
                return new MembershipPlan(
                    rs.getString("code"),rs.getString("title"),
                    rs.getInt("duration_months"),rs.getInt("price_cents"),
                    limit,rs.getInt("discount_percent"),Arrays.asList(perks));
            });
    }
}
