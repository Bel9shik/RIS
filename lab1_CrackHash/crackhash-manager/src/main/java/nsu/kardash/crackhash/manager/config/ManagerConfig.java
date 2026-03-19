package nsu.kardash.crackhash.manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class ManagerConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /** Короткие таймауты для GET /health воркеров */
    @Bean(name = "workerHealthRestTemplate")
    public RestTemplate workerHealthRestTemplate(
            @Value("${crackhash.worker.health-connect-timeout-ms:2000}") int connectMs,
            @Value("${crackhash.worker.health-read-timeout-ms:2000}") int readMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectMs));
        factory.setReadTimeout(Duration.ofMillis(readMs));
        return new RestTemplate(factory);
    }

    @Bean
    public List<String> workerUrls(
            @Value("${crackhash.worker.urls:http://crackhash-worker:8080}") String urls) {
        return Arrays.stream(urls.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Bean
    public long requestTimeoutSeconds(
            @Value("${crackhash.request.timeout-seconds:300}") long timeout) {
        return timeout;
    }
}
