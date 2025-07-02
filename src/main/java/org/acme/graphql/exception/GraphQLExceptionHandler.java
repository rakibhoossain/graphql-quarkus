package org.acme.graphql.exception;

import io.smallrye.graphql.api.ErrorExtensionProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.acme.service.exception.BusinessException;
import org.acme.service.exception.EntityNotFoundException;



/**
 * Global GraphQL exception handler that maps Java exceptions to GraphQL errors
 */
@ApplicationScoped
public class GraphQLExceptionHandler implements ErrorExtensionProvider {

    @Override
    public String getKey() {
        return "exception";
    }

    @Override
    public JsonValue mapValueFrom(Throwable throwable) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        if (throwable instanceof BusinessException) {
            builder.add("classification", "BUSINESS_ERROR");
            builder.add("code", "BUSINESS_RULE_VIOLATION");
            builder.add("message", throwable.getMessage());

        } else if (throwable instanceof EntityNotFoundException) {
            builder.add("classification", "NOT_FOUND");
            builder.add("code", "ENTITY_NOT_FOUND");
            builder.add("message", throwable.getMessage());

        } else if (throwable instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) throwable;
            builder.add("classification", "VALIDATION_ERROR");
            builder.add("code", "CONSTRAINT_VIOLATION");
            builder.add("message", "Validation failed");

            // Add violations as a JSON array
            var violationsBuilder = Json.createArrayBuilder();
            for (ConstraintViolation<?> violation : cve.getConstraintViolations()) {
                violationsBuilder.add(Json.createObjectBuilder()
                    .add("field", violation.getPropertyPath().toString())
                    .add("message", violation.getMessage())
                    .add("invalidValue", String.valueOf(violation.getInvalidValue())));
            }
            builder.add("violations", violationsBuilder);

        } else if (throwable instanceof IllegalArgumentException) {
            builder.add("classification", "INVALID_INPUT");
            builder.add("code", "INVALID_ARGUMENT");
            builder.add("message", throwable.getMessage());

        } else {
            // Generic error for unexpected exceptions
            builder.add("classification", "INTERNAL_ERROR");
            builder.add("code", "UNEXPECTED_ERROR");
            builder.add("message", "An unexpected error occurred");
        }

        return builder.build();
    }


}
