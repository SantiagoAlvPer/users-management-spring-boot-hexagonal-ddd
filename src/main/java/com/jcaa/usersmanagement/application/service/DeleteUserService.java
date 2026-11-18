package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.DeleteUserUseCase;
import com.jcaa.usersmanagement.application.port.out.DeleteUserPort;
import com.jcaa.usersmanagement.application.port.out.FileStoragePort;
import com.jcaa.usersmanagement.application.port.out.GetUserByIdPort;
import com.jcaa.usersmanagement.application.port.out.PublishUserEventPort;
import com.jcaa.usersmanagement.application.service.dto.command.DeleteUserCommand;
import com.jcaa.usersmanagement.application.service.mapper.UserApplicationMapper;
import com.jcaa.usersmanagement.domain.event.UserDeletedDomainEvent;
import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeleteUserService implements DeleteUserUseCase {

  private final DeleteUserPort deleteUserPort;
  private final GetUserByIdPort getUserByIdPort;
  private final FileStoragePort fileStoragePort;
  private final PublishUserEventPort publishUserEventPort;
  private final Validator validator;

  @Override
  public void execute(final DeleteUserCommand command) {
    validateCommand(command);

    final UserId userId = UserApplicationMapper.fromDeleteCommandToUserId(command);
    ensureUserExists(userId);
    fileStoragePort.deleteByPrefix("usuarios/" + userId.value() + "/");
    deleteUserPort.delete(userId);
    publishUserEventPort.publish(new UserDeletedDomainEvent(userId));
  }

  private void validateCommand(final DeleteUserCommand command) {
    final Set<ConstraintViolation<DeleteUserCommand>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  private void ensureUserExists(final UserId userId) {
    getUserByIdPort
        .getById(userId)
        .orElseThrow(() -> UserNotFoundException.becauseIdWasNotFound(userId.value()));
  }
}
