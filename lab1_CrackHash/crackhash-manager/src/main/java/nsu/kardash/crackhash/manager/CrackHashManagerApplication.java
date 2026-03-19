package nsu.kardash.crackhash.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "nsu.kardash.crackhash.manager")
@EnableScheduling
public class CrackHashManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrackHashManagerApplication.class, args);
    }
}
