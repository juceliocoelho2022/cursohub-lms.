package br.com.cursohub.membership.application;

import br.com.cursohub.membership.application.port.in.ListMembershipPlansUseCase;
import br.com.cursohub.membership.application.port.out.MembershipPlanReaderPort;
import br.com.cursohub.membership.domain.MembershipPlan;
import java.util.List;
import java.util.Objects;

public final class MembershipPlanService implements ListMembershipPlansUseCase {
    private final MembershipPlanReaderPort reader;
    public MembershipPlanService(MembershipPlanReaderPort reader){this.reader=Objects.requireNonNull(reader);}
    @Override public List<MembershipPlan> listAvailablePlans(){return reader.listAvailable();}
}
