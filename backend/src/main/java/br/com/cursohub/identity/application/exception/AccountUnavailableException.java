package br.com.cursohub.identity.application.exception;

public class AccountUnavailableException extends RuntimeException {
    public AccountUnavailableException() { super("Conta não disponível"); }
}
