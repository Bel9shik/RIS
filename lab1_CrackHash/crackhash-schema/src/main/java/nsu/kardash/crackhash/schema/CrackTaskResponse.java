package nsu.kardash.crackhash.schema;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "CrackTaskResponse", namespace = "http://nsu.kardash/crackhash/task-response")
@XmlType(name = "CrackTaskResponseType", namespace = "http://nsu.kardash/crackhash/task-response", propOrder = {
    "requestId", "partNumber", "words"
})
@XmlAccessorType(XmlAccessType.FIELD)
public class CrackTaskResponse {

    @XmlElement(required = true, namespace = "http://nsu.kardash/crackhash/task-response")
    private String requestId;

    private int partNumber;

    @XmlElement(required = true, namespace = "http://nsu.kardash/crackhash/task-response")
    private WordsType words = new WordsType();

    public CrackTaskResponse() {
    }

    public CrackTaskResponse(String requestId, int partNumber, List<String> wordList) {
        this.requestId = requestId;
        this.partNumber = partNumber;
        this.words = new WordsType();
        if (wordList != null) {
            this.words.getWords().addAll(wordList);
        }
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public WordsType getWords() {
        return words;
    }

    public void setWords(WordsType words) {
        this.words = words != null ? words : new WordsType();
    }

    public List<String> getWordList() {
        return words != null ? words.getWords() : new ArrayList<>();
    }
}
