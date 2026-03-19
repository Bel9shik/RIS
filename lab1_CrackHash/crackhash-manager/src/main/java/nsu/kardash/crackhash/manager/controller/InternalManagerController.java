package nsu.kardash.crackhash.manager.controller;

import nsu.kardash.crackhash.manager.service.HashCrackService;
import nsu.kardash.crackhash.schema.CrackTaskResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/api/manager/hash/crack")
public class InternalManagerController {

    private final HashCrackService hashCrackService;

    public InternalManagerController(HashCrackService hashCrackService) {
        this.hashCrackService = hashCrackService;
    }

    @PatchMapping(value = "/request", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Void> onWorkerResponse(@RequestBody CrackTaskResponse response) {
        hashCrackService.onWorkerResponse(response);
        return ResponseEntity.noContent().build();
    }
}
