package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.AddCvRequest;
import sample.project.DTO.request.UpdateCvRequest;
import sample.project.DTO.response.AgentResponse;
import sample.project.DTO.response.ServiceResponse;
import sample.project.Service.AgentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/users/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/uploadCv/{userid}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<?> addCv(@RequestBody AddCvRequest req, @PathVariable long userid,
            @AuthenticationPrincipal Jwt jwt) {

        ServiceResponse<AgentResponse> response = agentService.addCv(req, userid);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response.getMessage());
        }

        return ResponseEntity.ok().body(response.getData());

    }

    @PostMapping("/updateCv/{userid}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<?> updateCv(@RequestBody UpdateCvRequest req, @PathVariable long userid) {

        ServiceResponse<AgentResponse> response = agentService.updateCv(req, userid);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response.getMessage());
        }
        return ResponseEntity.ok().body(response.getData());

    }

    @DeleteMapping("/deleteCv/{deleteid}/user/{userid}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<?> deleteCv(@PathVariable long deleteid, @RequestBody String deletename,
            @PathVariable long userid) {

        ServiceResponse<AgentResponse> response = agentService.deleteCv(deletename, deleteid, userid);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response.getMessage());
        }
        return ResponseEntity.ok().body(response.getData());

    }

}
