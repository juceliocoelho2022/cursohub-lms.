package br.com.cursohub.membership.application.port.out;

import br.com.cursohub.membership.domain.MembershipPlan;
import java.util.List;

public interface MembershipPlanReaderPort {
    List<MembershipPlan> listAvailable();
}
