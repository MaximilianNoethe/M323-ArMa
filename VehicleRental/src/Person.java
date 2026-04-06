import java.time.LocalDate;
import java.time.Period;

public record Person(LocalDate birthDate, String name, String email) {
    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public int getAge(LocalDate checkDate) {
        return Period.between(birthDate, checkDate).getYears();
    }
}
