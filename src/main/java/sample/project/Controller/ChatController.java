package sample.project.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.ChatRequest;
import sample.project.DTO.response.ServiceResponse;
import sample.project.DTO.response.UserResponse;
import sample.project.Model.ChatMessage;
import sample.project.Model.User;
import sample.project.Service.ChatService;
import sample.project.Service.UserService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")

@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;

    @MessageMapping("/private-message")
    public ChatMessage addUser(@Payload ChatRequest chatMessage) {
        ChatMessage message = chatService.addUser(chatMessage);
        return message;
    }

    @GetMapping("/contacts")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY')")
    public ResponseEntity<?> getChatContacts(@AuthenticationPrincipal Jwt jwt) {

        Optional<User> opuser = userService.getUserByEmail(jwt.getClaim("email"));
        if (!opuser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        ServiceResponse<List<UserResponse>> response = chatService.getChatContacts(opuser.get().getId());

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response.getMessage());
        }
        return ResponseEntity.ok().body(response.getData());
    }

    @GetMapping("/history/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY')")
    public ResponseEntity<?> getChatHistory(@AuthenticationPrincipal Jwt jwt,
            @PathVariable long userid) {

        Optional<User> opuser = userService.getUserByEmail(jwt.getClaim("email"));
        if (!opuser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        ServiceResponse<List<ChatMessage>> response = chatService.getChatHistory(opuser.get().getId(), userid);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response.getMessage());
        }
        return ResponseEntity.ok().body(response.getData());
    }

    @DeleteMapping("/delete/{chatId}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY')")
    public ResponseEntity<String> putMethodName(@PathVariable long chatId) {
        chatService.deleteChat(chatId);
        return ResponseEntity.ok().body("Chat deleted");
    }

    @GetMapping("/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY')")
    public ResponseEntity<?> getUsertoChat(@PathVariable long userid,
            @AuthenticationPrincipal Jwt jwt) {
        ServiceResponse<UserResponse> response = chatService.getUsertoChat(userid);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response.getMessage());
        }
        return ResponseEntity.ok().body(response.getData());
    }

}
