package sample.project.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.CreateJobRequest;
import sample.project.DTO.request.SubCatagoriesRequest;
import sample.project.DTO.request.UpdateSubCatagoryRequest;
import sample.project.DTO.request.UpdateSubcat;
import sample.project.DTO.request.Subcat;
import sample.project.DTO.request.UpdateJobRequest;
import sample.project.DTO.request.UpdateSubCatagoriesRequest;
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
        List<Subcatagory> subcatagories = new ArrayList<Subcatagory>();

        if (request.subcatagories() != null) {
            for (Subcat sub : request.subcatagories()) {
                Subcatagory subcat = new Subcatagory();
                subcat.setName(sub.name());
                subcat.setDescription(sub.description());
                subcat.setJob(job);
                subcatagories.add(subcat);
            }

        }
        job.setSubcatagory(subcatagories);
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

        for (Subcat newSubcat : request.subcatagories()) {
            boolean exists = false;
            for (Subcatagory existing : subcatagories) {
                if (existing.getName().equalsIgnoreCase(newSubcat.name())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                Subcatagory sub = new Subcatagory();
                sub.setName(newSubcat.name());
                sub.setDescription(newSubcat.description());
                sub.setJob(job);
                subcatagories.add(sub);
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

        for (Subcat subcat : request.subcatagories()) {
            Subcatagory sub = null;
            for (Subcatagory existing : subcatagories) {
                if (existing.getName().equalsIgnoreCase(subcat.name())) {
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
    public Job updateSubCatagories(UpdateSubCatagoriesRequest request) {

        Optional<Job> optionalJob = jobRepo.findByName(request.jobName());
        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job", "name");
        }

        Job job = optionalJob.get();
        List<Subcatagory> subcatagories = job.getSubcatagory();

        for (UpdateSubcat newSubcat : request.subcatagories()) {
            Subcatagory sub = null;
            for (Subcatagory existing : subcatagories) {
                if (existing.getName().equalsIgnoreCase(newSubcat.existingName())) {
                    sub = existing;
                    break;
                }
            }
            if (sub != null) {
                if (!sub.getName().equals(newSubcat.updatedName())) {
                    boolean nameExists = subcatagories.stream()
                            .anyMatch(s -> s.getName().equalsIgnoreCase(newSubcat.updatedName()));
                    if (nameExists) {
                        throw new ObjectAlreadyExists("Subcatagory", "name");
                    }
                }

                sub.setName(newSubcat.updatedName());
                sub.setDescription(newSubcat.description());
            }
        }
        job.setSubcatagory(subcatagories);
        return job;

    }

    @Transactional
    public Job updateSubCatagory(UpdateSubCatagoryRequest request) {
        Optional<Job> optionalJob = jobRepo.findByName(request.jobName());
        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job", "name");
        }

        Job job = optionalJob.get();
        List<Subcatagory> subcatagories = job.getSubcatagory();
        UpdateSubcat newSubcat = request.subcatagory();

        for (Subcatagory existing : subcatagories) {
            if (existing.getName().equalsIgnoreCase(newSubcat.existingName())) {

                if (!existing.getName().equals(newSubcat.updatedName())) {
                    boolean nameExists = subcatagories.stream()
                            .anyMatch(s -> s.getName().equalsIgnoreCase(newSubcat.updatedName()));
                    if (nameExists) {
                        throw new ObjectAlreadyExists("Subcatagory", "name");
                    }
                    existing.setName(newSubcat.updatedName());
                }
                existing.setDescription(newSubcat.description());
                break;
            }
        }

        job.setSubcatagory(subcatagories);
        return job;
    }

    @Transactional
    public Job updateJobDetails(UpdateJobRequest request) {
        Optional<Job> optionalJob = jobRepo.findByName(request.existingJobname());
        if (!optionalJob.isPresent() && request.existingJobname().equals(request.updatedJobName())) {
            throw new ObjectNotFound("Job", "name");
        }

        if (request.updatedJobName() != null && !request.existingJobname().equals(request.updatedJobName())) {
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

    public List<Job> getAllJob(String search) {

        List<Job> jobs = jobRepo.findAll();
        if (search != null) {
            System.out.println(search);
            jobs.removeIf(job -> !(job.getName().toLowerCase().contains(search.toLowerCase()) ||
                    job.getDescription().toLowerCase().contains(search.toLowerCase())));

        }
        return jobs;
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
