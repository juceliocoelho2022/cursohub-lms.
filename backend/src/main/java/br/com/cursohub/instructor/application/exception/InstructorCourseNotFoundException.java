package br.com.cursohub.instructor.application.exception;

public class InstructorCourseNotFoundException extends RuntimeException {
    public InstructorCourseNotFoundException() {
        super("Curso ou módulo não encontrado para este instrutor");
    }
}
