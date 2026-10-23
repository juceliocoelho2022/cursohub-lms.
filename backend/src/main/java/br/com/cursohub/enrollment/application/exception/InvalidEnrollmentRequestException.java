package br.com.cursohub.enrollment.application.exception;

public class InvalidEnrollmentRequestException extends RuntimeException {
    public InvalidEnrollmentRequestException() { super("Identificador do curso inválido"); }
}
