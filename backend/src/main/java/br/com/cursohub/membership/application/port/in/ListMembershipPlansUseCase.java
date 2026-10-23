package br.com.cursohub.membership.application.port.in;

import br.com.cursohub.membership.domain.MembershipPlan;
import java.util.List;

public interface ListMembershipPlansUseCase {
    List<MembershipPlan> listAvailablePlans();
}
