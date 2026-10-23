package br.com.cursohub.enrollment.application.exception;

/** Não permitir matrícula em curso pago sem checkout e confirmação de pagamento. */
public class PaidCourseRequiresCheckoutException extends RuntimeException {
    public PaidCourseRequiresCheckoutException(){
        super("Curso pago: checkout ainda não disponível. Nenhuma cobrança foi realizada.");
    }
}
