package de.subcentral.core.util;

import java.util.Objects;

import de.subcentral.core.util.Service.Status.Code;

public interface Service {
    public static class Status {
        public enum Code {
            AVAILABLE, LIMITED, NOT_AVAILABLE
        }

        private final Code code;
        private final long responseTime;

        public static Status of(Code code) {
            return new Status(code, -1L);
        }

        public static Status of(Code code, long responseTime) {
            return new Status(code, responseTime);
        }

        private Status(Code code, long responseTime) {
            this.code = Objects.requireNonNull(code, "code");
            this.responseTime = responseTime;
        }

        public Code getCode() {
            return code;
        }

        public long getResponseTime() {
            return responseTime;
        }
    }

    public String getName();

    public default Status checkStatus() {
        return Status.of(Code.AVAILABLE);
    }
}
