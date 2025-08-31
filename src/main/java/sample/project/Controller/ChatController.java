package sample.project.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.ChatRequest;
import sample.project.DTO.response.UserResponse;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
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
    public ResponseEntity<List<UserResponse>> getChatContacts(@AuthenticationPrincipal Jwt jwt) {

        Optional<User> opuser = userService.getUserByEmail(jwt.getClaim("email"));
        if (!opuser.isPresent()) {
            throw new ObjectNotFound("User", "email");
        }
        List<UserResponse> response = chatService.getChatContacts(opuser.get().getId());
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/history/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY')")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@AuthenticationPrincipal Jwt jwt,
            @PathVariable long userid) {

        Optional<User> opuser = userService.getUserByEmail(jwt.getClaim("email"));
        if (!opuser.isPresent()) {
            throw new ObjectNotFound("User", "email");
        }
        List<ChatMessage> response = chatService.getChatHistory(opuser.get().getId(), userid);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/delete/{chatId}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY')")
    public ResponseEntity<String> putMethodName(@PathVariable long chatId) {
        chatService.deleteChat(chatId);
        return ResponseEntity.ok().body("Chat deleted");

    }

    @GetMapping("/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY')")
    public ResponseEntity<UserResponse> getUsertoChat(@PathVariable long userid,
            @AuthenticationPrincipal Jwt jwt) {
        UserResponse response = chatService.getUsertoChat(userid);
        return ResponseEntity.ok().body(response);
    }

}
