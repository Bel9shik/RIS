package nsu.kardash.crackhash.worker.service;

import nsu.kardash.crackhash.schema.CrackTaskResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * Отправка ответа воркера менеджеру в формате XML (PATCH).
 */
@Service
public class ManagerCallbackService {

    private final RestTemplate restTemplate;
    private final String managerUrl;

    public ManagerCallbackService(RestTemplate restTemplate,
                                  @Value("${crackhash.manager.url:http://crackhash-manager:8080}") String managerUrl) {
        this.restTemplate = restTemplate;
        this.managerUrl = managerUrl;
    }

    public void sendResponse(CrackTaskResponse response) {
        String url = managerUrl + "/internal/api/manager/hash/crack/request";
        try {
            String xml = marshalToXml(response);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(xml, headers), String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send response to manager", e);
        }
    }

    private static String marshalToXml(CrackTaskResponse response) throws Exception {
        JAXBContext context = JAXBContext.newInstance(CrackTaskResponse.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        StringWriter writer = new StringWriter();
        marshaller.marshal(response, writer);
        return writer.toString();
    }
}
