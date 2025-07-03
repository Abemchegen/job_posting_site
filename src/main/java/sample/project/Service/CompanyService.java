package sample.project.Service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.CompanyUpdateRequest;
import sample.project.DTO.response.CompanyUpdateResponse;
import sample.project.ErrorHandling.Exception.ObjectAlreadyExists;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
import sample.project.Model.Company;
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
    public CompanyUpdateResponse changeCompanyDetails(CompanyUpdateRequest req) {
        Optional<Company> optionalCompanyName = companyRepo.findByName(req.name());

        if (optionalCompanyName.isPresent()) {
            throw new ObjectAlreadyExists("Company", "name");
        }
        Optional<Company> optionalCompanyPhonenum = companyRepo.findByPhonenumber(req.phonenumber());

        if (optionalCompanyPhonenum.isPresent()) {
            throw new ObjectAlreadyExists("Company", "phonenumber");
        }

        Optional<Company> optionalCompany = companyRepo.findById(req.companyID());
        if (!optionalCompany.isPresent()) {
            throw new ObjectNotFound("Company", "id");
        }
        Company company = optionalCompany.get();
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

}
