package br.com.cursohub.learning.application.exception;

public class LearningCourseUnavailableException extends RuntimeException {
    public LearningCourseUnavailableException() { super("Curso não encontrado entre suas matrículas disponíveis"); }
}
