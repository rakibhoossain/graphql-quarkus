package org.acme.graphql.exception;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.acme.service.exception.BusinessException;
import org.acme.service.exception.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to map service exceptions to GraphQL exceptions
 */
@ApplicationScoped
public class ExceptionMapper {

    /**
     * Maps service layer exceptions to appropriate GraphQL exceptions
     */
    public RuntimeException mapToGraphQLException(Throwable throwable) {
        if (throwable instanceof BusinessException) {
            return mapBusinessException((BusinessException) throwable);
        } else if (throwable instanceof EntityNotFoundException) {
            return mapEntityNotFoundException((EntityNotFoundException) throwable);
        } else if (throwable instanceof ConstraintViolationException) {
            return mapConstraintViolationException((ConstraintViolationException) throwable);
        } else if (throwable instanceof IllegalArgumentException) {
            return new GraphQLBusinessException(throwable.getMessage(), "INVALID_ARGUMENT");
        } else {
            // For unexpected exceptions, wrap them but don't expose internal details
            return new GraphQLBusinessException("An unexpected error occurred", "INTERNAL_ERROR");
        }
    }

    private GraphQLBusinessException mapBusinessException(BusinessException ex) {
        String errorCode = determineBusinessErrorCode(ex.getMessage());
        return new GraphQLBusinessException(ex.getMessage(), errorCode);
    }

    private GraphQLBusinessException mapEntityNotFoundException(EntityNotFoundException ex) {
        return new GraphQLBusinessException(ex.getMessage(), "ENTITY_NOT_FOUND");
    }

    private GraphQLValidationException mapConstraintViolationException(ConstraintViolationException ex) {
        List<GraphQLValidationException.ValidationError> validationErrors = ex.getConstraintViolations()
            .stream()
            .map(this::mapConstraintViolation)
            .collect(Collectors.toList());

        return new GraphQLValidationException("Validation failed", validationErrors);
    }

    private GraphQLValidationException.ValidationError mapConstraintViolation(ConstraintViolation<?> violation) {
        return new GraphQLValidationException.ValidationError(
            violation.getPropertyPath().toString(),
            violation.getMessage(),
            violation.getInvalidValue()
        );
    }

    private String determineBusinessErrorCode(String message) {
        if (message.contains("already exists")) {
            return "DUPLICATE_ENTITY";
        } else if (message.contains("not found")) {
            return "ENTITY_NOT_FOUND";
        } else if (message.contains("insufficient stock") || message.contains("stock")) {
            return "INSUFFICIENT_STOCK";
        } else if (message.contains("cannot delete") || message.contains("cannot move")) {
            return "OPERATION_NOT_ALLOWED";
        } else {
            return "BUSINESS_RULE_VIOLATION";
        }
    }
}
