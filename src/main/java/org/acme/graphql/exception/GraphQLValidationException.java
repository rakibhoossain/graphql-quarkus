package org.acme.graphql.exception;

import io.smallrye.graphql.api.ErrorCode;

import java.util.List;
import java.util.Map;

/**
 * GraphQL-specific validation exception
 */
@ErrorCode("VALIDATION_ERROR")
public class GraphQLValidationException extends RuntimeException {
    
    private final List<ValidationError> validationErrors;

    public GraphQLValidationException(String message, List<ValidationError> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public static class ValidationError {
        private final String field;
        private final String message;
        private final Object invalidValue;

        public ValidationError(String field, String message, Object invalidValue) {
            this.field = field;
            this.message = message;
            this.invalidValue = invalidValue;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        public Object getInvalidValue() {
            return invalidValue;
        }
    }
}
