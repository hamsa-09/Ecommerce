package com.example.vertexspace_server.dto;

public class SuccessResponse<T> {
    private boolean success = true;
    private T data;

    public SuccessResponse(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
