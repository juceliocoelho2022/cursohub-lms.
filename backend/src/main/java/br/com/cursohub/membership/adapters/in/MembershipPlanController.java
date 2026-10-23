package br.com.cursohub.membership.adapters.in;

import br.com.cursohub.membership.application.port.in.ListMembershipPlansUseCase;
import br.com.cursohub.membership.domain.MembershipPlan;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MembershipPlanController {
    private final ListMembershipPlansUseCase useCase;
    public MembershipPlanController(ListMembershipPlansUseCase useCase){this.useCase=useCase;}
    @GetMapping("/api/v1/membership/plans")
    public List<MembershipPlan> list(){return useCase.listAvailablePlans();}
}
