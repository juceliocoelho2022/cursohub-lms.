package br.com.cursohub.learning.application.exception;

public class LessonUnavailableException extends RuntimeException {
    public LessonUnavailableException() { super("Aula indisponível para esta matrícula"); }
}
