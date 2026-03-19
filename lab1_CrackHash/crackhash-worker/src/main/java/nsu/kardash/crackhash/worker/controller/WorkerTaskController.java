package nsu.kardash.crackhash.worker.controller;

import nsu.kardash.crackhash.schema.CrackTaskRequest;
import nsu.kardash.crackhash.worker.service.WorkerAsyncCrackService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/api/worker/hash/crack")
public class WorkerTaskController {

    private final WorkerAsyncCrackService workerAsyncCrackService;

    public WorkerTaskController(WorkerAsyncCrackService workerAsyncCrackService) {
        this.workerAsyncCrackService = workerAsyncCrackService;
    }

    @PostMapping(value = "/task", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void handleTask(@RequestBody CrackTaskRequest request) {
        workerAsyncCrackService.processTaskAsync(request);
    }
}
