package nsu.kardash.crackhash.schema;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

@XmlType(name = "WordsType", namespace = "http://nsu.kardash/crackhash/task-response", propOrder = {"words"})
@XmlAccessorType(XmlAccessType.FIELD)
public class WordsType {

    @XmlElement(name = "word", namespace = "http://nsu.kardash/crackhash/task-response")
    private List<String> words = new ArrayList<>();

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words != null ? words : new ArrayList<>();
    }
}
