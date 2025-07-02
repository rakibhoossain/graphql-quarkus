package org.acme.graphql.exception;

import io.smallrye.graphql.api.ErrorCode;

/**
 * GraphQL-specific business exception with error codes
 */
@ErrorCode("BUSINESS_ERROR")
public class GraphQLBusinessException extends RuntimeException {
    
    private final String errorCode;
    private final Object details;

    public GraphQLBusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_RULE_VIOLATION";
        this.details = null;
    }

    public GraphQLBusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public GraphQLBusinessException(String message, String errorCode, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object getDetails() {
        return details;
    }
}
