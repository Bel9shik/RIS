package nsu.kardash.crackhash.schema;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "CrackTaskRequest", namespace = "http://nsu.kardash/crackhash/task-request")
@XmlType(name = "CrackTaskRequestType", namespace = "http://nsu.kardash/crackhash/task-request", propOrder = {
    "requestId", "hash", "maxLength", "alphabet", "partNumber", "partCount"
})
@XmlAccessorType(XmlAccessType.FIELD)
public class CrackTaskRequest {

    @XmlElement(required = true)
    private String requestId;

    @XmlElement(required = true)
    private String hash;

    private int maxLength;

    @XmlElement(required = true)
    private String alphabet;

    private int partNumber;

    private int partCount;

    public CrackTaskRequest() {
    }

    public CrackTaskRequest(String requestId, String hash, int maxLength, String alphabet, int partNumber, int partCount) {
        this.requestId = requestId;
        this.hash = hash;
        this.maxLength = maxLength;
        this.alphabet = alphabet;
        this.partNumber = partNumber;
        this.partCount = partCount;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public int getPartCount() {
        return partCount;
    }

    public void setPartCount(int partCount) {
        this.partCount = partCount;
    }
}
