package br.com.cursohub.assessment.application;

public class AssessmentNotFoundException extends RuntimeException {
    public AssessmentNotFoundException() { super("Avaliação ou certificado não encontrado para esta conta"); }
}
