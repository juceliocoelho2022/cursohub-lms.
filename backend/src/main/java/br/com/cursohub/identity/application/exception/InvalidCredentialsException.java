package br.com.cursohub.identity.application.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() { super("E-mail ou senha inválidos"); }
}
