package nsu.kardash.crackhash.manager.controller;

import nsu.kardash.crackhash.manager.domain.RequestStatus;
import nsu.kardash.crackhash.manager.dto.CrackHashRequest;
import nsu.kardash.crackhash.manager.dto.CrackHashResponse;
import nsu.kardash.crackhash.manager.dto.HashStatusResponse;
import nsu.kardash.crackhash.manager.service.HashCrackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hash")
public class HashCrackController {

    private final HashCrackService hashCrackService;

    public HashCrackController(HashCrackService hashCrackService) {
        this.hashCrackService = hashCrackService;
    }

    @PostMapping("/crack")
    public ResponseEntity<CrackHashResponse> crack(@RequestBody CrackHashRequest request) {
        String requestId = hashCrackService.submitCrackRequest(request.getHash(), request.getMaxLength());
        return ResponseEntity.ok(new CrackHashResponse(requestId));
    }

    @GetMapping(value = "/status", params = "requestId")
    public ResponseEntity<HashStatusResponse> status(@RequestParam("requestId") String requestId) {
        RequestStatus status = hashCrackService.getStatus(requestId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        List<String> data = null;
        if (status == RequestStatus.READY) {
            data = hashCrackService.getResult(requestId);
        }
        String errorReason = status == RequestStatus.ERROR ? hashCrackService.getErrorReason(requestId) : null;
        return ResponseEntity.ok(new HashStatusResponse(status.name(), data, errorReason));
    }

    /** Снимок словаря результатов: ключ «md5:maxLength», значение — список слов */
    @GetMapping("/dictionary")
    public Map<String, List<String>> dictionary() {
        return hashCrackService.getDictionarySnapshot();
    }
}
