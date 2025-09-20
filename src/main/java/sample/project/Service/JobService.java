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
import sample.project.DTO.response.ServiceResponse;
import sample.project.DTO.request.Subcat;
import sample.project.DTO.request.UpdateJobRequest;
import sample.project.DTO.request.UpdateSubCatagoriesRequest;
import sample.project.Model.Job;
import sample.project.Model.Subcatagory;
import sample.project.Repo.JobRepo;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepo jobRepo;

    public ServiceResponse<Job> addJob(CreateJobRequest request) {
        Optional<Job> optionalJob = jobRepo.findByName(request.name());
        if (optionalJob.isPresent()) {
            return new ServiceResponse<Job>(false, "Job not found", null);
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
        Job j = jobRepo.save(job);
        return new ServiceResponse<Job>(true, "", j);

    }

    @Transactional
    public ServiceResponse<Job> addSubCatagories(SubCatagoriesRequest request) {

        Optional<Job> optionalJob = jobRepo.findByName(request.jobName());
        if (!optionalJob.isPresent()) {
            return new ServiceResponse<Job>(false, "Job not found", null);
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
        return new ServiceResponse<Job>(true, "", job);

    }

    @Transactional
    public ServiceResponse<Job> removeSubCatagories(SubCatagoriesRequest request) {

        Optional<Job> optionalJob = jobRepo.findByName(request.jobName());
        if (!optionalJob.isPresent()) {
            return new ServiceResponse<Job>(false, "Job not found", null);
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
        return new ServiceResponse<Job>(true, "", job);

    }

    @Transactional
    public ServiceResponse<Job> updateSubCatagories(UpdateSubCatagoriesRequest request) {

        Optional<Job> optionalJob = jobRepo.findByName(request.jobName());
        if (!optionalJob.isPresent()) {
            return new ServiceResponse<Job>(false, "Job not found", null);
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
                        return new ServiceResponse<Job>(false, "Subcatagory with this name already exists", null);
                    }
                }

                sub.setName(newSubcat.updatedName());
                sub.setDescription(newSubcat.description());
            }
        }
        job.setSubcatagory(subcatagories);
        return new ServiceResponse<Job>(true, "", job);

    }

    @Transactional
    public ServiceResponse<Job> updateSubCatagory(UpdateSubCatagoryRequest request) {
        Optional<Job> optionalJob = jobRepo.findByName(request.jobName());
        if (!optionalJob.isPresent()) {
            return new ServiceResponse<Job>(false, "Job not found", null);
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
                        return new ServiceResponse<Job>(false, "Subcatagory with this name already exists", null);
                    }
                    existing.setName(newSubcat.updatedName());
                }
                existing.setDescription(newSubcat.description());
                break;
            }
        }
        job.setSubcatagory(subcatagories);
        return new ServiceResponse<Job>(true, "", job);
    }

    @Transactional
    public ServiceResponse<Job> updateJobDetails(UpdateJobRequest request) {
        Optional<Job> optionalJob = jobRepo.findByName(request.existingJobname());
        if (!optionalJob.isPresent() && request.existingJobname().equals(request.updatedJobName())) {
            return new ServiceResponse<Job>(false, "Job not found", null);
        }

        if (request.updatedJobName() != null && !request.existingJobname().equals(request.updatedJobName())) {
            Optional<Job> optionalUpdatedJob = jobRepo.findByName(request.updatedJobName());
            if (optionalUpdatedJob.isPresent()) {
                return new ServiceResponse<Job>(false, "Job with this name already exists", null);
            }
        }

        Job job = optionalJob.get();

        if (request.updatedJobName() != null) {
            job.setName(request.updatedJobName());
        }
        if (request.description() != null) {
            job.setDescription(request.description());
        }

        return new ServiceResponse<Job>(true, null, job);

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

    public ServiceResponse<Job> getJobById(Long id) {
        Optional<Job> optionalJob = jobRepo.findById(id);

        if (!optionalJob.isPresent()) {
            return new ServiceResponse<Job>(false, "Job not found", null);
        }

        return new ServiceResponse<Job>(true, null, optionalJob.get());
    }

    public Optional<Job> getJob(String jobName) {
        return jobRepo.findByName(jobName);
    }

    public void deleteJobById(Long id) {
        jobRepo.deleteById(id);
        return;
    }

}
