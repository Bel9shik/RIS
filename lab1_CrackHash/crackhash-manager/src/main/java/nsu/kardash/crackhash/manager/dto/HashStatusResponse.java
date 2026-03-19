package nsu.kardash.crackhash.manager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HashStatusResponse {

    private final String status;
    private final List<String> data;
    /** Заполняется при status=ERROR: почему запрос не выполнен */
    private final String errorReason;

    public HashStatusResponse(String status, List<String> data) {
        this(status, data, null);
    }

    public HashStatusResponse(String status, List<String> data, String errorReason) {
        this.status = status;
        this.data = data;
        this.errorReason = errorReason;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("data")
    public List<String> getData() {
        return data;
    }

    @JsonProperty("errorReason")
    public String getErrorReason() {
        return errorReason;
    }
}
