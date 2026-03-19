package nsu.kardash.crackhash.manager.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Потокобезопасное состояние одного запроса на взлом хэша.
 */
public class RequestState {

    private volatile RequestStatus status;
    private final int partCount;
    private final Instant createdAt;
    private final long timeoutSeconds;
    private final String hash;
    private final int maxLength;
    /** С момента перехода в IN_PROGRESS (для таймаута) */
    private volatile Instant processingStartedAt;
    /**
     * Собранные слова от воркеров (partNumber -> слова).
     */
    private final ConcurrentHashMap<Integer, List<String>> resultsByPart = new ConcurrentHashMap<>();
    private final AtomicInteger receivedParts = new AtomicInteger(0);
    /**
     * Если не null — результат взят из словаря (без воркеров), сразу READY.
     */
    private final List<String> dictionaryWords;

    public RequestState(int partCount, long timeoutSeconds, String hash, int maxLength, RequestStatus initialStatus) {
        this(partCount, timeoutSeconds, hash, maxLength, initialStatus, null);
    }

    public RequestState(int partCount, long timeoutSeconds, String hash, int maxLength,
                        RequestStatus initialStatus, List<String> dictionaryWords) {
        this.partCount = partCount;
        this.createdAt = Instant.now();
        this.timeoutSeconds = timeoutSeconds;
        this.hash = hash == null ? "" : hash.trim().toLowerCase();
        this.maxLength = maxLength;
        this.status = initialStatus;
        this.dictionaryWords = dictionaryWords != null ? List.copyOf(dictionaryWords) : null;
    }

    public String getHash() {
        return hash;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void markProcessingStarted() {
        this.processingStartedAt = Instant.now();
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public int getPartCount() {
        return partCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    /**
     * Добавить результат от воркера. Возвращает true, если все части получены.
     */
    public boolean addPartResult(int partNumber, List<String> words) {
        List<String> copy = words != null ? new ArrayList<>(words) : new ArrayList<>();
        List<String> previous = resultsByPart.put(partNumber, copy);
        if (previous != null) {
            // Повторный PATCH по той же части — не увеличиваем счётчик (иначе ложный «все части получены»).
            return false;
        }
        return receivedParts.incrementAndGet() >= partCount;
    }

    /**
     * Все собранные слова в одном списке (порядок по partNumber).
     */
    public List<String> getAllWords() {
        if (dictionaryWords != null) {
            return Collections.unmodifiableList(new ArrayList<>(dictionaryWords));
        }
        List<String> all = new ArrayList<>();
        for (int i = 0; i < partCount; i++) {
            List<String> part = resultsByPart.get(i);
            if (part != null) {
                all.addAll(part);
            }
        }
        return Collections.unmodifiableList(all);
    }

    public boolean isTimedOut() {
        if (processingStartedAt == null) {
            return false;
        }
        return Instant.now().isAfter(processingStartedAt.plusSeconds(timeoutSeconds));
    }
}
