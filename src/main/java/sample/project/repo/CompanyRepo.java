package sample.project.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sample.project.Model.Company;

public interface CompanyRepo extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    Optional<Company> findByPhonenumber(String phonenumber);

}
