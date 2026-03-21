import java.time.LocalDate;
import java.time.Period;

public record Person(LocalDate birthDate, String name) {
    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
