import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class RentalLogic {
    private static final int MINIMUM_AGE = 12;

    // --- 1. Pure Function for creating a contract ---
    // Returns a NEW RentalState (Success) or an Error message (Failure)
    public static Result<RentalState> createContract(RentalState state, Contract contract) {
        Person person = contract.person();
        
        if (state.denyList().contains(person)) {
            return new Result.Failure<>("Person is on the deny list.");
        }
        
        if (person.getAge() < MINIMUM_AGE) {
            return new Result.Failure<>("Person is too young (under " + MINIMUM_AGE + ").");
        }
        
        if (checkCollision(state.contracts(), contract.vehicle(), contract.startDate(), contract.endDate())) {
            return new Result.Failure<>("Vehicle is already rented during this time.");
        }

        // Create new immutable list of contracts
        List<Contract> newContracts = Stream.concat(state.contracts().stream(), Stream.of(contract)).toList();
        
        // Return a brand new state (Immutability maintained!)
        RentalState newState = new RentalState(
            state.customerList(),
            state.denyList(),
            state.vehicles(),
            newContracts
        );
        
        return new Result.Success<>(newState);
    }

    // --- 2. Higher-Order Functions, Filter/Map Pipelines ---
    private static boolean checkCollision(List<Contract> existingContracts, Vehicle vehicle, LocalDate start, LocalDate end) {
        return existingContracts.stream()
            // HOF / Pipeline: filter out contracts for other vehicles
            .filter(c -> c.vehicle().equals(vehicle))
            // Pattern Match (Java 21+) and logic combination
            .anyMatch(c -> !(start.isAfter(c.endDate()) || end.isBefore(c.startDate())));
    }

    public static List<Vehicle> getAvailableVehicles(RentalState state, LocalDate checkDate) {
        // Pipeline with map and filter!
        List<Vehicle> rentedVehiclesOnDate = state.contracts().stream()
            .filter(c -> !checkDate.isBefore(c.startDate()) && !checkDate.isAfter(c.endDate()))
            .map(Contract::vehicle)
            .toList();

        return state.vehicles().stream()
            .filter(v -> !rentedVehiclesOnDate.contains(v))
            .toList();
    }

    // --- 3. Rekursion (Recursion) ---
    // Calculate total value of all contracts recursively
    public static double calculateTotalContractValueRecursive(List<Contract> contracts) {
        if (contracts.isEmpty()) {
            return 0.0;
        }
        Contract first = contracts.get(0);
        List<Contract> rest = contracts.subList(1, contracts.size());
        
        // Value = pricePerHour * hours (approx 24h per day). Simplified: 1 day = 24h.
        long days = java.time.temporal.ChronoUnit.DAYS.between(first.startDate(), first.endDate());
        if (days == 0) days = 1; // Minimum 1 day rent
        
        double contractValue = first.vehicle().pricePerHour() * 24 * days;
        
        return contractValue + calculateTotalContractValueRecursive(rest);
    }
    
    // --- 4. Pattern Matching (Switch Expression in Java 21) ---
    public static String getVehicleDescription(Vehicle vehicle) {
        return switch (vehicle) {
            case Car c -> "Car (" + c.vehicleBrand() + ") with " + c.wheelCount() + " wheels.";
            case AirVehicle a -> "Air Vehicle (" + a.vehicleBrand() + ") with " + a.wingspan() + "m wingspan.";
            case WaterVehicle w -> "Water Vehicle (" + w.vehicleBrand() + ") - Sailboat: " + w.isSailboat();
        };
    }
}
