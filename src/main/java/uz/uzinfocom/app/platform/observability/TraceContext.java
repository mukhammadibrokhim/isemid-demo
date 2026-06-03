package uz.uzinfocom.app.platform.observability;

public final class TraceContext {

    private TraceContext() {
    }

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    public static final String REQUEST_ATTRIBUTE =
            TraceContext.class.getName() + ".TRACE_ID";

    /**
     * Do not use "traceId" as custom MDC key if Micrometer/OpenTelemetry is enabled.
     */
    public static final String MDC_KEY = "traceId";
}