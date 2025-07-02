package org.acme.graphql.type;

import org.eclipse.microprofile.graphql.Type;

/**
 * Generic result wrapper for GraphQL operations
 */
@Type("OperationResult")
public class OperationResult<T> {
    
    public boolean success;
    public T data;
    public ErrorResponse error;
    public String message;

    public OperationResult() {}

    public static <T> OperationResult<T> success(T data) {
        OperationResult<T> result = new OperationResult<>();
        result.success = true;
        result.data = data;
        result.message = "Operation completed successfully";
        return result;
    }

    public static <T> OperationResult<T> success(T data, String message) {
        OperationResult<T> result = new OperationResult<>();
        result.success = true;
        result.data = data;
        result.message = message;
        return result;
    }

    public static <T> OperationResult<T> error(ErrorResponse error) {
        OperationResult<T> result = new OperationResult<>();
        result.success = false;
        result.error = error;
        result.message = error.message;
        return result;
    }

    public static <T> OperationResult<T> error(String code, String message, String classification) {
        OperationResult<T> result = new OperationResult<>();
        result.success = false;
        result.error = new ErrorResponse(code, message, classification);
        result.message = message;
        return result;
    }
}
