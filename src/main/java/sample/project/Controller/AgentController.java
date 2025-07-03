package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.AddCvRequest;
import sample.project.DTO.response.UserResponse;
import sample.project.Model.User;
import sample.project.Service.AgentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("users/agent")
@RequiredArgsConstructor
public class AgentController {

    private AgentService agentService;

    @PostMapping("/uploadCv/{id}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<UserResponse> addCv(@RequestBody AddCvRequest req, @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        if (!currentUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        UserResponse response = agentService.addCv(req, id);

        return ResponseEntity.ok().body(response);

    }

    @PostMapping("/updateCv/{id}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<UserResponse> updateCv(@RequestBody AddCvRequest req, @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        if (!currentUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        UserResponse response = agentService.updateCv(req, id);

        return ResponseEntity.ok().body(response);

    }

}
