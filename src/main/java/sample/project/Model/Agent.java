package sample.project.Model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Agent {
    @Id
    private Long id;
    @OneToOne
    @MapsId
    private User user;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Cv cv;
    @OneToMany(mappedBy = "agent")
    private List<JobApplication> JobApplications;
}
