package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.AddCvRequest;
import sample.project.DTO.request.UpdateCvRequest;
import sample.project.DTO.response.AgentResponse;
import sample.project.Model.User;
import sample.project.Service.AgentService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/users/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/uploadCv")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<AgentResponse> addCv(@RequestBody AddCvRequest req,
            @AuthenticationPrincipal User currentUser) {

        AgentResponse response = agentService.addCv(req, currentUser.getId());

        return ResponseEntity.ok().body(response);

    }

    @PostMapping("/updateCv")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<AgentResponse> updateCv(@RequestBody UpdateCvRequest req,
            @AuthenticationPrincipal User currentUser) {
        AgentResponse response = agentService.updateCv(req, currentUser.getId());

        return ResponseEntity.ok().body(response);

    }

    @DeleteMapping("/deleteCv/{deleteid}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<AgentResponse> deleteCv(@PathVariable long deleteid, @RequestBody String deletename,
            @AuthenticationPrincipal User currentUser) {
        AgentResponse response = agentService.deleteCv(deletename, deleteid, currentUser.getId());

        return ResponseEntity.ok().body(response);

    }

    // @GetMapping("/getJobApplications")
    // @PreAuthorize("hasRole('AGENT')")
    // public ResponseEntity<List<JobApplication>>
    // getMyJobApplications(@AuthenticationPrincipal User currentUser) {

    // List<JobApplication> response =
    // agentService.getMyJobApplications(currentUser.getId());
    // return ResponseEntity.ok().body(response);
    // }

}
