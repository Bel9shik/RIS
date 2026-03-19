package nsu.kardash.crackhash.worker.service;

import nsu.kardash.crackhash.schema.CrackTaskRequest;
import nsu.kardash.crackhash.schema.CrackTaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Перебор и callback менеджеру в фоне — HTTP POST /task сразу возвращает ответ,
 * иначе менеджер блокируется на RestTemplate и может не принять PATCH от воркера (deadlock).
 */
@Service
public class WorkerAsyncCrackService {

    private static final Logger log = LoggerFactory.getLogger(WorkerAsyncCrackService.class);

    private final HashCrackTaskService hashCrackTaskService;
    private final ManagerCallbackService managerCallbackService;

    public WorkerAsyncCrackService(HashCrackTaskService hashCrackTaskService,
                                   ManagerCallbackService managerCallbackService) {
        this.hashCrackTaskService = hashCrackTaskService;
        this.managerCallbackService = managerCallbackService;
    }

    @Async("taskExecutor")
    public void processTaskAsync(CrackTaskRequest request) {
        try {
            List<String> words = hashCrackTaskService.crack(
                    request.getHash(),
                    request.getMaxLength(),
                    request.getAlphabet(),
                    request.getPartNumber(),
                    request.getPartCount()
            );
            CrackTaskResponse response = new CrackTaskResponse(
                    request.getRequestId(),
                    request.getPartNumber(),
                    words
            );
            managerCallbackService.sendResponse(response);
        } catch (Exception e) {
            log.error("Task failed for requestId={}", request.getRequestId(), e);
        }
    }
}
