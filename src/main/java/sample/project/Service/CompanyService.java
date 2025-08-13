package sample.project.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.CompanyUpdateRequest;
import sample.project.DTO.response.CompanyUpdateResponse;
import sample.project.DTO.response.JobpostResponse;
import sample.project.ErrorHandling.Exception.ObjectAlreadyExists;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
import sample.project.Model.Company;
import sample.project.Model.JobPost;
import sample.project.Repo.CompanyRepo;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepo companyRepo;

    @Transactional
    public Company findOrCreateCompany(String companyName, String companyPhonenumber) {

        Optional<Company> optionalCompany = companyRepo.findByName(companyName);
        Company company;
        if (!optionalCompany.isPresent()) {
            company = new Company();
            company.setName(companyName);
            company.setPhoneNumber(companyPhonenumber);
            company = companyRepo.save(company);
        } else {
            company = optionalCompany.get();
        }
        return company;
    }

    @Transactional
    public CompanyUpdateResponse changeCompanyDetails(CompanyUpdateRequest req, long companyID) {
        Optional<Company> optionalCompany = companyRepo.findById(companyID);
        if (!optionalCompany.isPresent()) {
            throw new ObjectNotFound("Company", "id");
        }
        Company company = optionalCompany.get();
        Optional<Company> optionalCompanyName = companyRepo.findByName(req.name());

        if (optionalCompanyName.isPresent() && !optionalCompanyName.get().getName().equals(company.getName())) {
            throw new ObjectAlreadyExists("Company", "name");
        }
        Optional<Company> optionalCompanyPhonenum = companyRepo.findByPhoneNumber(req.phonenumber());

        if (optionalCompanyPhonenum.isPresent()
                && !optionalCompanyPhonenum.get().getPhoneNumber().equals(company.getPhoneNumber())) {
            throw new ObjectAlreadyExists("Company", "phonenumber");
        }

        if (req.name() != null) {
            company.setName(req.name());

        }

        if (req.phonenumber() != null) {
            company.setPhoneNumber(req.phonenumber());
        }

        return new CompanyUpdateResponse(company.getId(), company.getName(), company.getPhoneNumber());

    }

    public Optional<Company> getCompany(String companyName) {
        return companyRepo.findByName(companyName);
    }

    public Optional<Company> getCompany(long companyID) {
        return companyRepo.findById(companyID);
    }

    public List<JobpostResponse> getAllJobPostsFromACompany(long companyID) {
        Optional<Company> company = companyRepo.findById(companyID);

        if (!company.isPresent()) {
            throw new ObjectNotFound("Company", "id");
        }

        List<JobPost> post = company.get().getJobPosts();
        List<JobpostResponse> responses = new ArrayList<JobpostResponse>();
        for (JobPost p : post) {
            JobpostResponse response = new JobpostResponse(p.getId(), p.getDescription(), p.getCompany().getName(),
                    p.getCompany().getPhoneNumber(),
                    p.getJobName(), p.getSubcatagory().getName(), p.getSubcatagory().getDescription(),
                    p.getPeopleNeeded(), p.getSalary(), p.getDate());

            responses.add(response);
        }

        return responses;
    }

}
