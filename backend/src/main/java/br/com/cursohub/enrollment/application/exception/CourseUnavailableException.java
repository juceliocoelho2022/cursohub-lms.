package br.com.cursohub.enrollment.application.exception;

public class CourseUnavailableException extends RuntimeException {
    public CourseUnavailableException() { super("Curso não publicado ou indisponível para matrícula"); }
}
