package nsu.kardash.crackhash.worker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Дефолтный RestTemplate (HttpURLConnection) не поддерживает PATCH — нужен JDK HttpClient.
 */
@Configuration
public class WorkerConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new JdkClientHttpRequestFactory());
    }
}
