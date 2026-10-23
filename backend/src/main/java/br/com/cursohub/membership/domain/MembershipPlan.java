package br.com.cursohub.membership.domain;

import java.util.List;
import java.util.Objects;

/** Catálogo público de planos comerciais, sem ativação automática nem processamento de pagamento. */
public record MembershipPlan(
        String code,String title,int durationMonths,int priceCents,
        Integer courseLimit,int discountPercent,List<String> benefits) {
    public MembershipPlan {
        Objects.requireNonNull(code);Objects.requireNonNull(title);
        benefits = List.copyOf(Objects.requireNonNull(benefits));
        if(!List.of(1,6,12).contains(durationMonths)||priceCents<=0||
           discountPercent<0||discountPercent>30||(courseLimit!=null&&courseLimit<1))
            throw new IllegalArgumentException("Configuração inválida do plano");
    }
}
