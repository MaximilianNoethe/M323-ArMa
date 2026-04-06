import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RentalLogic {
    private static final int MINIMUM_AGE = 18;

    public static Result<RentalState> createContract(RentalState state, Contract contract) {
        Person person = contract.person();

        if (person.name().isBlank()) {
            return new Result.Failure<>("Bitte gib deinen Namen ein.");
        }

        if (!isValidEmail(person.email())) {
            return new Result.Failure<>("Bitte gib eine gültige E-Mail-Adresse ein.");
        }

        if (isDenied(state.denyList(), person)) {
            return new Result.Failure<>("Diese Person darf aktuell kein Vehicle mieten.");
        }

        if (person.getAge(LocalDate.now()) < MINIMUM_AGE) {
            return new Result.Failure<>("Du musst mindestens " + MINIMUM_AGE + " Jahre alt sein.");
        }

        if (contract.endDate().isBefore(contract.startDate())) {
            return new Result.Failure<>("Das Enddatum darf nicht vor dem Startdatum liegen.");
        }

        if (!contract.documentRequestSent()) {
            return new Result.Failure<>("Bitte simuliere zuerst die Unterlagen-E-Mail.");
        }

        if (!contract.documentsSubmitted()) {
            return new Result.Failure<>("Bitte bestätige, dass Führerschein und ID mitgeschickt wurden.");
        }

        if (checkCollision(state.contracts(), contract.vehicle(), contract.startDate(), contract.endDate())) {
            return new Result.Failure<>("Das Vehicle ist in diesem Zeitraum bereits vermietet.");
        }

        List<Person> newCustomers = state.customerList().contains(person)
            ? state.customerList()
            : Stream.concat(state.customerList().stream(), Stream.of(person)).toList();

        List<Contract> newContracts = Stream.concat(state.contracts().stream(), Stream.of(contract)).toList();

        return new Result.Success<>(new RentalState(
            newCustomers,
            state.denyList(),
            state.vehicles(),
            newContracts
        ));
    }

    public static boolean checkCollision(List<Contract> existingContracts, Vehicle vehicle, LocalDate start, LocalDate end) {
        return existingContracts.stream()
            .filter(c -> c.vehicle().equals(vehicle))
            .anyMatch(c -> datesOverlap(start, end, c.startDate(), c.endDate()));
    }

    public static boolean isDenied(List<Person> denyList, Person person) {
        return denyList.stream()
            .anyMatch(denied -> denied.name().equalsIgnoreCase(person.name())
                && denied.birthDate().equals(person.birthDate()));
    }

    public static boolean isAvailableOn(RentalState state, Vehicle vehicle, LocalDate date) {
        return state.contracts().stream()
            .filter(c -> c.vehicle().equals(vehicle))
            .noneMatch(c -> isDateInContract(date, c));
    }

    public static List<Vehicle> getAvailableVehicles(RentalState state, LocalDate checkDate) {
        List<Vehicle> rentedVehiclesOnDate = state.contracts().stream()
            .filter(c -> isDateInContract(checkDate, c))
            .map(Contract::vehicle)
            .toList();

        return state.vehicles().stream()
            .filter(v -> !rentedVehiclesOnDate.contains(v))
            .toList();
    }

    public static Optional<LocalDate> getNextAvailableDate(RentalState state, Vehicle vehicle, LocalDate checkDate) {
        return state.contracts().stream()
            .filter(c -> c.vehicle().equals(vehicle))
            .filter(c -> !c.endDate().isBefore(checkDate))
            .map(c -> c.endDate().plusDays(1))
            .max(LocalDate::compareTo);
    }

    public static String getAvailabilityText(RentalState state, Vehicle vehicle, LocalDate checkDate) {
        Optional<Contract> activeContract = state.contracts().stream()
            .filter(c -> c.vehicle().equals(vehicle))
            .filter(c -> isDateInContract(checkDate, c))
            .findFirst();

        if (activeContract.isPresent()) {
            LocalDate availableDate = activeContract.get().endDate().plusDays(1);
            return "Vermietet bis " + activeContract.get().endDate() + ", wieder verfügbar am " + availableDate;
        }

        return "Heute verfügbar";
    }

    public static String createDocumentRequestEmail(Person person, Vehicle vehicle) {
        return "To: " + person.email() + "\n"
            + "Subject: Unterlagen für Vehicle Rental\n\n"
            + "Hallo " + person.name() + ",\n"
            + "bitte antworte mit Führerschein, Ausweisdokument und Mietbestätigung für "
            + getVehicleDescription(vehicle) + "\n"
            + "Dies ist eine Simulation; es wird keine echte E-Mail versendet.";
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    public static double calculateTotalContractValueRecursive(List<Contract> contracts) {
        if (contracts.isEmpty()) {
            return 0.0;
        }

        Contract first = contracts.get(0);
        List<Contract> rest = contracts.subList(1, contracts.size());

        return calculateContractValue(first) + calculateTotalContractValueRecursive(rest);
    }

    public static String getVehicleDescription(Vehicle vehicle) {
        return switch (vehicle) {
            case Car c -> "Auto: " + c.vehicleBrand();
            case AirVehicle a -> "Luftfahrzeug: " + a.vehicleBrand();
            case WaterVehicle w -> "Wasserfahrzeug: " + w.vehicleBrand();
        };
    }

    public static String getVehicleDetails(Vehicle vehicle) {
        return switch (vehicle) {
            case Car c -> "Motor: " + c.motorType()
                + " | Sitze: " + c.seatNumber()
                + " | Räder: " + c.wheelCount()
                + " | Gewicht: " + c.weight() + " kg";
            case AirVehicle a -> "Motor: " + a.motorType()
                + " | Sitze: " + a.seatNumber()
                + " | Spannweite: " + a.wingspan() + " m"
                + " | Gewicht: " + a.weight() + " kg";
            case WaterVehicle w -> "Motor: " + w.motorType()
                + " | Plätze: " + w.seatNumber()
                + " | Segelboot: " + yesNo(w.isSailboat())
                + " | Gewicht: " + w.weight() + " kg";
        };
    }

    private static boolean datesOverlap(LocalDate firstStart, LocalDate firstEnd, LocalDate secondStart, LocalDate secondEnd) {
        return !firstStart.isAfter(secondEnd) && !firstEnd.isBefore(secondStart);
    }

    private static boolean isDateInContract(LocalDate date, Contract contract) {
        return !date.isBefore(contract.startDate()) && !date.isAfter(contract.endDate());
    }

    private static double calculateContractValue(Contract contract) {
        long days = ChronoUnit.DAYS.between(contract.startDate(), contract.endDate());
        long chargedDays = Math.max(1, days);

        return contract.vehicle().pricePerHour() * 24 * chargedDays;
    }

    private static String yesNo(boolean value) {
        return value ? "ja" : "nein";
    }
}
