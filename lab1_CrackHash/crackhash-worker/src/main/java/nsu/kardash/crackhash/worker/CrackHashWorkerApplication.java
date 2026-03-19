package nsu.kardash.crackhash.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "nsu.kardash.crackhash.worker")
public class CrackHashWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrackHashWorkerApplication.class, args);
    }
}
