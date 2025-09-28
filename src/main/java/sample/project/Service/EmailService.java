package sample.project.Service;

import com.resend.Resend;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resendClient;
    private final String from;

    public EmailService(@Value("${RESEND_API_KEY}") String apiKey,
            @Value("${RESEND_FROM}") String from) {
        this.resendClient = new Resend(apiKey);
        this.from = from;
    }

    public void sendEmail(String to, String subject, String htmlContent) {
        SendEmailRequest request = SendEmailRequest.builder()
                .from(from)
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build();
        SendEmailResponse data = resendClient.emails().send(request);

    }
}
