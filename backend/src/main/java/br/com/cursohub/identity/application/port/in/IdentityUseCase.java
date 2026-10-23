package br.com.cursohub.identity.application.port.in;

import br.com.cursohub.identity.application.model.AuthenticatedSession;
import br.com.cursohub.identity.application.model.LoginCommand;
import br.com.cursohub.identity.application.model.RegisterStudentCommand;
import br.com.cursohub.identity.domain.UserAccount;
import java.util.UUID;

public interface IdentityUseCase {
    AuthenticatedSession registerStudent(RegisterStudentCommand command);
    AuthenticatedSession login(LoginCommand command);
    UserAccount findCurrentUser(UUID userId);
}
