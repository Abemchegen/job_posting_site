package sample.project.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class EmailService {

    private final WebClient webClient;
    private String from;

    public EmailService(@Value("${RESEND_API_KEY}") String apiKey,
            @Value("${RESEND_FROM}") String from) {
        this.from = from;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public void sendEmail(String to, String subject, String htmlContent) {
        Map<String, Object> body = Map.of(
                "from", from,
                "to", new String[] { to },
                "subject", subject,
                "html", htmlContent);

        webClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> System.out.println("Resend response: " + response))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                })
                .block();
    }
}
