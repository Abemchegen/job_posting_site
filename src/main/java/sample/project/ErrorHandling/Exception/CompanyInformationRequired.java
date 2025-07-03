package sample.project.ErrorHandling.Exception;

public class CompanyInformationRequired extends RuntimeException {
    public CompanyInformationRequired() {
        super("Required field for company is not filled.");
    }
}
