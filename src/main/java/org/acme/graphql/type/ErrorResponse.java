package org.acme.graphql.type;

import org.eclipse.microprofile.graphql.Type;

import java.util.List;

/**
 * GraphQL type for error responses
 */
@Type("ErrorResponse")
public class ErrorResponse {
    
    public String code;
    public String message;
    public String classification;
    public List<ValidationError> validationErrors;
    public Object details;

    public ErrorResponse() {}

    public ErrorResponse(String code, String message, String classification) {
        this.code = code;
        this.message = message;
        this.classification = classification;
    }

    public ErrorResponse(String code, String message, String classification, List<ValidationError> validationErrors) {
        this.code = code;
        this.message = message;
        this.classification = classification;
        this.validationErrors = validationErrors;
    }

    @Type("ValidationError")
    public static class ValidationError {
        public String field;
        public String message;
        public String invalidValue;

        public ValidationError() {}

        public ValidationError(String field, String message, String invalidValue) {
            this.field = field;
            this.message = message;
            this.invalidValue = invalidValue;
        }
    }
}
