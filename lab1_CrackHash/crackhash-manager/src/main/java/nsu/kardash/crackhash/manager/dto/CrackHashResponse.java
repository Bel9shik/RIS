package nsu.kardash.crackhash.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CrackHashResponse {

    private final String requestId;

    public CrackHashResponse(String requestId) {
        this.requestId = requestId;
    }

    @JsonProperty("requestId")
    public String getRequestId() {
        return requestId;
    }
}
