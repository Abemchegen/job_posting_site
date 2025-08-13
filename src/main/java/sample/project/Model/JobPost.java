package sample.project.Model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JsonBackReference("jobpost-company")
    private Company company;
    @jakarta.persistence.Column(length = 10000)
    private String description;
    private String jobName;
    @ManyToOne
    @JsonBackReference("subcatagory-jobpost")
    private Subcatagory subcatagory;
    private int peopleNeeded;
    private long salary;
    private LocalDate date;
    @JsonManagedReference("jobpost-jobapplication")
    @OneToMany(mappedBy = "jobPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobApplication> JobApplications;

}

// filter by experiance endpoints