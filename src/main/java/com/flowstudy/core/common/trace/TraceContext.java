package com.flowstudy.core.common.trace;

import org.slf4j.MDC;

public final class TraceContext {

    public static final String TRACE_ID = "traceId";

    private TraceContext() {
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }
}
