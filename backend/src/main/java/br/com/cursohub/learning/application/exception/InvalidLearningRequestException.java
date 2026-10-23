package br.com.cursohub.learning.application.exception;

public class InvalidLearningRequestException extends RuntimeException {
    public InvalidLearningRequestException() { super("Identificador do curso inválido"); }
}
