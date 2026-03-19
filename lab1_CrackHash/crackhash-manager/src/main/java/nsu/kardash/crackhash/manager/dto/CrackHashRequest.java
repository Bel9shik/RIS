package nsu.kardash.crackhash.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CrackHashRequest {

    private final String hash;
    private final int maxLength;

    @JsonCreator
    public CrackHashRequest(
            @JsonProperty("hash") String hash,
            @JsonProperty("maxLength") int maxLength) {
        this.hash = hash;
        this.maxLength = maxLength;
    }

    public String getHash() {
        return hash;
    }

    public int getMaxLength() {
        return maxLength;
    }
}
