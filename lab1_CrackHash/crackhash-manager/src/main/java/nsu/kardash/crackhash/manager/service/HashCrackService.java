package nsu.kardash.crackhash.manager.service;

import nsu.kardash.crackhash.manager.domain.RequestStatus;
import nsu.kardash.crackhash.manager.domain.RequestState;
import nsu.kardash.crackhash.schema.CrackTaskRequest;
import nsu.kardash.crackhash.schema.CrackTaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class HashCrackService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final Logger log = LoggerFactory.getLogger(HashCrackService.class);

    private final RestTemplate restTemplate;
    private final List<String> workerUrls;
    private final long requestTimeoutSeconds;

    private final Map<String, RequestState> requests = new ConcurrentHashMap<>();
    private final Map<String, String> errorReasons = new ConcurrentHashMap<>();

    private final Object queueLock = new Object();
    private volatile String currentActiveRequestId;
    private final Queue<String> pendingQueue = new ConcurrentLinkedQueue<>();

    /** Словарь: ключ hash:maxLength → найденные слова (после первого успешного взлома) */
    private final ConcurrentHashMap<String, List<String>> crackDictionary = new ConcurrentHashMap<>();

    private static String dictionaryKey(String hash, int maxLength) {
        String h = hash == null ? "" : hash.trim().toLowerCase();
        return h + ":" + maxLength;
    }

    /**
     * Слова для пары (hash, maxLength): из in-memory словаря или из любой уже READY задачи с тем же ключом.
     * Восстанавливает запись в словаре, если нашли только по READY-соседу (на случай гонок / пропуска put).
     */
    private List<String> resolveCachedWordsForHash(String hash, int maxLength) {
        String key = dictionaryKey(hash, maxLength);
        List<String> fromDict = crackDictionary.get(key);
        if (fromDict != null) {
            return new ArrayList<>(fromDict);
        }
        for (RequestState other : requests.values()) {
            if (other.getStatus() != RequestStatus.READY) {
                continue;
            }
            if (!dictionaryKey(other.getHash(), other.getMaxLength()).equals(key)) {
                continue;
            }
            List<String> words = new ArrayList<>(other.getAllWords());
            crackDictionary.putIfAbsent(key, List.copyOf(words));
            log.debug("Кэш для {} восстановлен из READY-задачи (словарь был пуст)", key);
            return words;
        }
        return null;
    }

    /**
     * Если хэш уже в словаре — сохраняет READY с результатом, воркеры не нужны.
     *
     * @return true если ответ взят из словаря
     */
    private boolean applyDictionaryHitIfPresent(String requestId, String hash, int maxLength) {
        List<String> cached = resolveCachedWordsForHash(hash, maxLength);
        if (cached == null) {
            return false;
        }
        RequestState hit = new RequestState(0, requestTimeoutSeconds, hash, maxLength,
                RequestStatus.READY, cached);
        requests.put(requestId, hit);
        log.info("Request {} — из словаря/готового соседа (key={}), расчёт на воркерах не запускаем",
                requestId, dictionaryKey(hash, maxLength));
        return true;
    }

    public HashCrackService(
            RestTemplate restTemplate,
            List<String> workerUrls,
            @Value("${crackhash.request.timeout-seconds:300}") long requestTimeoutSeconds) {
        this.restTemplate = restTemplate;
        this.workerUrls = workerUrls;
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    /**
     * Регистрирует запрос. Если сейчас никто не считается — сразу в работу, иначе в очередь (QUEUED).
     */
    public String submitCrackRequest(String hash, int maxLength) {
        String requestId = UUID.randomUUID().toString();
        int partCount = workerUrls.size();

        if (partCount == 0) {
            RequestState state = new RequestState(0, requestTimeoutSeconds, hash, maxLength, RequestStatus.ERROR);
            requests.put(requestId, state);
            errorReasons.put(requestId, "Нет воркеров: проверьте crackhash.worker.urls");
            return requestId;
        }

        if (applyDictionaryHitIfPresent(requestId, hash, maxLength)) {
            return requestId;
        }

        RequestState state = new RequestState(partCount, requestTimeoutSeconds, hash, maxLength, RequestStatus.QUEUED);
        requests.put(requestId, state);

        synchronized (queueLock) {
            if (currentActiveRequestId == null) {
                startRequestLocked(requestId, state);
            } else {
                pendingQueue.offer(requestId);
                log.debug("Request {} queued (active: {})", requestId, currentActiveRequestId);
            }
        }
        return requestId;
    }

    private void startRequestLocked(String requestId, RequestState state) {
        if (applyDictionaryHitIfPresent(requestId, state.getHash(), state.getMaxLength())) {
            startNextFromQueueLocked();
            return;
        }
        currentActiveRequestId = requestId;
        state.setStatus(RequestStatus.IN_PROGRESS);
        state.markProcessingStarted();
        if (!dispatchToWorkers(requestId, state)) {
            currentActiveRequestId = null;
            startNextFromQueueLocked();
        }
    }

    /** @return false если отправка на воркеры провалилась (уже выставлен ERROR) */
    private boolean dispatchToWorkers(String requestId, RequestState state) {
        int partCount = workerUrls.size();
        for (int partNumber = 0; partNumber < partCount; partNumber++) {
            CrackTaskRequest task = new CrackTaskRequest(
                    requestId,
                    state.getHash(),
                    state.getMaxLength(),
                    ALPHABET,
                    partNumber,
                    partCount
            );
            String workerUrl = workerUrls.get(partNumber);
            String url = workerUrl + "/internal/api/worker/hash/crack/task";
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                restTemplate.postForObject(url, new HttpEntity<>(task, headers), Void.class);
            } catch (Exception e) {
                String reason = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.warn("Failed to send task to worker {}: {}", workerUrl, reason, e);
                errorReasons.put(requestId, "Не удалось отправить задачу на " + workerUrl + " — " + reason);
                state.setStatus(RequestStatus.ERROR);
                return false;
            }
        }
        return true;
    }

    private void startNextFromQueueLocked() {
        while (!pendingQueue.isEmpty()) {
            String nextId = pendingQueue.poll();
            RequestState s = requests.get(nextId);
            if (s == null || s.getStatus() != RequestStatus.QUEUED) {
                continue;
            }
            if (applyDictionaryHitIfPresent(nextId, s.getHash(), s.getMaxLength())) {
                continue;
            }
            startRequestLocked(nextId, s);
            return;
        }
    }

    private void resolveQueuedTasksMatchingDictionaryLocked() {
        if (pendingQueue.isEmpty()) {
            return;
        }
        List<String> stillWaiting = new ArrayList<>();
        String id;
        while ((id = pendingQueue.poll()) != null) {
            RequestState st = requests.get(id);
            if (st == null || st.getStatus() != RequestStatus.QUEUED) {
                continue;
            }
            if (applyDictionaryHitIfPresent(id, st.getHash(), st.getMaxLength())) {
                continue;
            }
            stillWaiting.add(id);
        }
        for (String back : stillWaiting) {
            pendingQueue.offer(back);
        }
    }

    /**
     * Успех: словарь + очередь под одним lock — ожидающие с тем же хэшем сразу получают READY.
     */
    private void finishActiveSuccess(
            String finishedRequestId, String hash, int maxLength, List<String> words) {
        synchronized (queueLock) {
            if (!finishedRequestId.equals(currentActiveRequestId)) {
                return;
            }
            crackDictionary.put(dictionaryKey(hash, maxLength), List.copyOf(words));
            currentActiveRequestId = null;
            resolveQueuedTasksMatchingDictionaryLocked();
            startNextFromQueueLocked();
        }
    }

    /** Таймаут / ошибка: словарь не трогаем, только освобождаем слот и запускаем следующего из очереди. */
    private void releaseActiveAndStartNext(String finishedRequestId) {
        synchronized (queueLock) {
            if (!finishedRequestId.equals(currentActiveRequestId)) {
                return;
            }
            currentActiveRequestId = null;
            resolveQueuedTasksMatchingDictionaryLocked();
            startNextFromQueueLocked();
        }
    }

    public void onWorkerResponse(CrackTaskResponse response) {
        String requestId = response.getRequestId();
        RequestState state = requests.get(requestId);
        if (state == null || state.getStatus() != RequestStatus.IN_PROGRESS) {
            return;
        }
        if (!requestId.equals(currentActiveRequestId)) {
            return;
        }
        boolean allReceived = state.addPartResult(response.getPartNumber(), response.getWordList());
        if (allReceived) {
            state.setStatus(RequestStatus.READY);
            List<String> words = state.getAllWords();
            log.info("Request {} READY ({} слов), обновляем словарь и очередь", requestId, words.size());
            finishActiveSuccess(requestId, state.getHash(), state.getMaxLength(), words);
        }
    }

    public RequestStatus getStatus(String requestId) {
        synchronized (queueLock) {
            RequestState st = requests.get(requestId);
            if (st != null) {
                RequestStatus s = st.getStatus();
                if (s == RequestStatus.QUEUED || s == RequestStatus.IN_PROGRESS) {
                    if (applyDictionaryHitIfPresent(requestId, st.getHash(), st.getMaxLength())) {
                        log.info("Request {} — результат из словаря при опросе статуса (было {})",
                                requestId, s);
                        if (requestId.equals(currentActiveRequestId)) {
                            currentActiveRequestId = null;
                            resolveQueuedTasksMatchingDictionaryLocked();
                            startNextFromQueueLocked();
                        } else {
                            resolveQueuedTasksMatchingDictionaryLocked();
                        }
                    }
                }
            }
        }
        RequestState state = requests.get(requestId);
        if (state == null) {
            return null;
        }
        return state.getStatus();
    }

    public String getErrorReason(String requestId) {
        return errorReasons.get(requestId);
    }

    public List<String> getResult(String requestId) {
        RequestState state = requests.get(requestId);
        if (state == null || state.getStatus() != RequestStatus.READY) {
            return null;
        }
        return state.getAllWords();
    }

    /** Копия словаря hash:maxLength → слова (для отладки / API) */
    public Map<String, List<String>> getDictionarySnapshot() {
        return Map.copyOf(crackDictionary);
    }

    @Scheduled(fixedDelayString = "${crackhash.timeout-check-interval-ms:5000}")
    public void checkTimeouts() {
        String activeId;
        synchronized (queueLock) {
            activeId = currentActiveRequestId;
        }
        if (activeId == null) {
            return;
        }
        RequestState state = requests.get(activeId);
        if (state != null && state.getStatus() == RequestStatus.IN_PROGRESS && state.isTimedOut()) {
            errorReasons.put(activeId,
                    "Таймаут " + requestTimeoutSeconds + " с: воркер не ответил или задача слишком тяжёлая");
            state.setStatus(RequestStatus.ERROR);
            log.info("Request {} timed out", activeId);
            releaseActiveAndStartNext(activeId);
        }
    }
}
