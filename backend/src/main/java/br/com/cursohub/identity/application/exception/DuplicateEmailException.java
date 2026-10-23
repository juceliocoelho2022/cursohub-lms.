package br.com.cursohub.identity.application.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() { super("Este e-mail já está cadastrado"); }
}
