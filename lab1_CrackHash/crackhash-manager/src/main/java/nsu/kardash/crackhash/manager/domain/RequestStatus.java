package nsu.kardash.crackhash.manager.domain;

public enum RequestStatus {
    /** В очереди, ожидает предыдущих запросов */
    QUEUED,
    IN_PROGRESS,
    READY,
    ERROR
}
