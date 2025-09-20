package sample.project.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.ChatRequest;
import sample.project.DTO.response.AgentResponse;
import sample.project.DTO.response.CompanyResponse;
import sample.project.DTO.response.ServiceResponse;
import sample.project.DTO.response.UserResponse;
import sample.project.ErrorHandling.Exception.AccessDenied;
import sample.project.Model.Agent;
import sample.project.Model.ChatMessage;
import sample.project.Model.Company;
import sample.project.Model.User;
import sample.project.Repo.ChatRepo;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate template;
    private final ChatRepo chatRepo;
    private final UserService userService;

    @Transactional
    public ChatMessage addUser(ChatRequest req) {
        ChatMessage message = new ChatMessage();
        message.setMessage(req.message());
        message.setReceiverUsername(req.recieverName());
        message.setSenderUsername(req.senderName());
        message.setReceiverID(req.receiverID());
        message.setSenderID(req.senderID());
        chatRepo.save(message);
        template.convertAndSendToUser(message.getReceiverUsername(), "/private",
                message);
        template.convertAndSendToUser(message.getSenderUsername(), "/private", message);

        return message;
    }

    public ServiceResponse<List<UserResponse>> getChatContacts(long id) {
        ServiceResponse<UserResponse> res = userService.getUser(id);
        if (!res.isSuccess()) {
            return new ServiceResponse<List<UserResponse>>(false, "User not found", null);
        }

        List<String> usernames = chatRepo.findChatUsernames(res.data.getEmail());
        usernames.remove(res.data.getEmail());
        List<UserResponse> response = new ArrayList<>();

        for (String username : usernames) {

            Optional<User> optionalUser = userService.getUserByEmail(username);
            if (optionalUser.isPresent()) {

                User user = optionalUser.get();
                if (user.getCompany() != null) {
                    Company company = user.getCompany();

                    response.add(CompanyResponse.builder()
                            .id(user.getId())
                            .birthdate(user.getBirthdate())
                            .email(user.getEmail())
                            .companyId(company.getId())
                            .companyName(company.getName())
                            .companyPhonenumber(company.getPhoneNumber())
                            .pfp(user.getPfpUrl())
                            .phonenumber(user.getPhonenumber())
                            .name(user.getName())
                            .role(user.getRole())
                            .build());
                }

                else if (user.getAgent() != null) {
                    Agent agent = user.getAgent();

                    response.add(AgentResponse.builder()
                            .id(agent.getId())
                            .birthdate(user.getBirthdate())
                            .email(user.getEmail())
                            .phonenumber(user.getPhonenumber())
                            .name(user.getName())
                            .pfp(user.getPfpUrl())
                            .role(user.getRole())
                            .cv(agent.getCv())
                            .build());

                } else if (user.getRole().toString().equals("ADMIN")) {
                    response.add(UserResponse.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .phonenumber(user.getPhonenumber())
                            .birthdate(user.getBirthdate())
                            .pfp(user.getPfpUrl())
                            .role(user.getRole())
                            .build());
                }

            }
        }

        return new ServiceResponse<List<UserResponse>>(true, null, response);
    }

    public ServiceResponse<List<ChatMessage>> getChatHistory(long id, long otherid) {
        ServiceResponse<UserResponse> res = userService.getUser(id);

        if (!res.isSuccess()) {
            return new ServiceResponse<>(false, "User not found", null);
        }
        ServiceResponse<UserResponse> otherres = userService.getUser(otherid);

        List<ChatMessage> response = chatRepo
                .findBySenderUsernameAndReceiverUsernameOrReceiverUsernameAndSenderUsernameOrderByDateAsc(
                        res.data.getEmail(), otherres.data.getEmail(), res.data.getEmail(), otherres.data.getEmail());

        return new ServiceResponse<>(true, "", response);
    }

    public void deleteChat(long chatId) {
        chatRepo.deleteById(chatId);
    }

    public ServiceResponse<UserResponse> getUsertoChat(long userid) {
        return userService.getUser(userid);
    }
}
