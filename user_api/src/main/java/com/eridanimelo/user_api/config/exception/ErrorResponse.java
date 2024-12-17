package com.eridanimelo.user_api.config.exception;

public class ErrorResponse {
    private String error;
    private Object message;

    public ErrorResponse(String error, Object message) {
        this.error = error;
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }
}
