package sample.project.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.CreateJobRequest;
import sample.project.DTO.request.SubCatagoriesRequest;
import sample.project.DTO.request.SubCatagoryRequest;
import sample.project.DTO.request.UpdateJobRequest;
import sample.project.ErrorHandling.Exception.ObjectAlreadyExists;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
import sample.project.Model.Job;
import sample.project.Model.Subcatagory;
import sample.project.Repo.JobRepo;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepo jobRepo;

    public Job addJob(CreateJobRequest request) {
        Optional<Job> optionalJob = jobRepo.findByName(request.name());
        if (optionalJob.isPresent()) {
            throw new ObjectAlreadyExists("Job", "name");
        }

        Job job = new Job();
        job.setName(request.name());
        job.setDescription(request.description());

        if (request.subcatagories() != null) {
            for (Subcatagory sub : request.subcatagories()) {
                sub.setJob(job);
            }
            job.setSubcatagory(request.subcatagories());
        } else {
            job.setSubcatagory(new ArrayList<>());
        }

        return jobRepo.save(job);

    }

    @Transactional
    public Job addSubCatagories(SubCatagoriesRequest request) {

        Optional<Job> optionalJob = jobRepo.findByName(request.jobName());
        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job", "name");
        }

        Job job = optionalJob.get();
        List<Subcatagory> subcatagories = job.getSubcatagory();

        for (Subcatagory newSubcat : request.subcatagories()) {
            boolean exists = false;
            for (Subcatagory existing : subcatagories) {
                if (existing.getName().equalsIgnoreCase(newSubcat.getName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                newSubcat.setJob(job);
                subcatagories.add(newSubcat);
            }
        }
        return job;

    }

    @Transactional
    public Job removeSubCatagories(SubCatagoriesRequest request) {

        Optional<Job> optionalJob = jobRepo.findByName(request.jobName());
        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job", "name");
        }

        Job job = optionalJob.get();
        List<Subcatagory> subcatagories = job.getSubcatagory();

        for (Subcatagory subcat : request.subcatagories()) {
            Subcatagory sub = null;
            for (Subcatagory existing : subcatagories) {
                if (existing.getName().equalsIgnoreCase(subcat.getName())) {
                    sub = existing;
                    break;
                }
            }
            if (sub != null) {
                subcatagories.remove(sub);
            }
        }
        job.setSubcatagory(subcatagories);
        return job;

    }

    @Transactional
    public Job updateSubCatagories(SubCatagoriesRequest request) {

        Optional<Job> optionalJob = jobRepo.findByName(request.jobName());
        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job", "name");
        }

        Job job = optionalJob.get();
        List<Subcatagory> subcatagories = job.getSubcatagory();

        for (Subcatagory newSubcat : request.subcatagories()) {
            Subcatagory sub = null;
            for (Subcatagory existing : subcatagories) {
                if (existing.getName().equalsIgnoreCase(newSubcat.getName())) {
                    sub = existing;
                    break;
                }
            }
            if (sub != null) {
                sub.setDescription(newSubcat.getDescription());
            }
        }
        job.setSubcatagory(subcatagories);
        return job;

    }

    @Transactional
    public Job updateSubCatagory(SubCatagoryRequest request) {
        Optional<Job> optionalJob = jobRepo.findByName(request.jobName());
        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job", "name");
        }

        Job job = optionalJob.get();
        List<Subcatagory> subcatagories = job.getSubcatagory();
        Subcatagory newSubcat = request.subcatagory();

        for (Subcatagory existing : subcatagories) {
            if (existing.getName().equalsIgnoreCase(newSubcat.getName())) {
                existing.setDescription(newSubcat.getDescription());
                break;
            }
        }

        job.setSubcatagory(subcatagories);
        return job;
    }

    @Transactional
    public Job updateJobDetails(UpdateJobRequest request) {
        Optional<Job> optionalJob = jobRepo.findByName(request.existingJobname());
        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job", "name");
        }

        if (request.updatedJobName() != null) {
            Optional<Job> optionalUpdatedJob = jobRepo.findByName(request.updatedJobName());
            if (optionalUpdatedJob.isPresent()) {
                throw new ObjectAlreadyExists("Job", "name");
            }

        }

        Job job = optionalJob.get();

        if (request.updatedJobName() != null) {
            job.setName(request.updatedJobName());
        }
        if (request.description() != null) {
            job.setDescription(request.description());
        }

        return job;

    }

    public List<Job> getAllJob() {
        return jobRepo.findAll();
    }

    public Job getJobById(Long id) {
        Optional<Job> optionalJob = jobRepo.findById(id);

        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job", "id");
        }

        return optionalJob.get();
    }

    public Optional<Job> getJob(String jobName) {
        return jobRepo.findByName(jobName);
    }

    public void deleteJobById(Long id) {
        jobRepo.deleteById(id);
        return;
    }

}
