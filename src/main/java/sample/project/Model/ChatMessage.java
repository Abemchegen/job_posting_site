package sample.project.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String senderUsername;
    private long senderID;
    private long receiverID;
    private String receiverUsername;
    private String message;
    // private MessageStatus status;
    private LocalDateTime date = LocalDateTime.now();

}
