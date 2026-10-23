package br.com.cursohub.enrollment.application.exception;

public class EnrollmentConflictException extends RuntimeException {
    public EnrollmentConflictException() {
        super("A matrícula deste aluno está cancelada. Solicite reativação à administração.");
    }
}
