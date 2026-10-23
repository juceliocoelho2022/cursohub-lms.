package br.com.cursohub.membership.config;

import br.com.cursohub.membership.application.MembershipPlanService;
import br.com.cursohub.membership.application.port.in.ListMembershipPlansUseCase;
import br.com.cursohub.membership.application.port.out.MembershipPlanReaderPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MembershipPlanConfiguration {
    @Bean ListMembershipPlansUseCase listMembershipPlansUseCase(MembershipPlanReaderPort reader){
        return new MembershipPlanService(reader);
    }
}
