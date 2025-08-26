package sample.project.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sample.project.Model.ChatMessage;

public interface ChatRepo extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT DISTINCT c.senderUsername FROM ChatMessage c WHERE c.receiverUsername = :username " +
            "UNION " +
            "SELECT DISTINCT c.receiverUsername FROM ChatMessage c WHERE c.senderUsername = :username")
    List<String> findChatUsernames(String username);

    List<ChatMessage> findBySenderUsernameAndReceiverUsernameOrReceiverUsernameAndSenderUsernameOrderByDateAsc(
            String sender, String receiver, String receiver2, String sender2);
}
