package com.flowstudy.core.common.result;

import com.flowstudy.core.common.trace.TraceContext;

public record Result<T>(int code, String message, T data, String traceId, long timestamp) {

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data, TraceContext.getTraceId(), System.currentTimeMillis());
    }

    public static Result<Void> failure(int code, String message) {
        return new Result<>(code, message, null, TraceContext.getTraceId(), System.currentTimeMillis());
    }
}
