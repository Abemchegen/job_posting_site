package sample.project.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sample.project.Model.Company;

public interface CompanyRepo extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    // @Query("Select m from Company m where m.phoneNumber = :phonenumber")

    Optional<Company> findByPhoneNumber(@Param("phonenumber") String phonenumber);

}
