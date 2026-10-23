package br.com.cursohub.catalog.application.exception;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException() { super("Curso não encontrado"); }
}
