import java.time.LocalDate;

public record Contract(
    LocalDate startDate, 
    LocalDate endDate, 
    String contractModalities, 
    Person person, 
    Vehicle vehicle,
    boolean documentRequestSent,
    boolean documentsSubmitted
) {}
