import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // --- 1. Initial Immutable Data Setup ---
        Car jdm1 = new Car(1500, 35.00, "Internal combustion", "Nissan", 4, 4);
        Car luxury1 = new Car(2100, 80.50, "Electric", "Mercedes-Benz", 4, 4);
        AirVehicle heli = new AirVehicle(3000, 200.00, "Turboshaft", "Bell", 6, 12.5);
        WaterVehicle boat = new WaterVehicle(5000, 150.00, "Diesel", "Yamaha", 10, false);

        Person testPerson = new Person(LocalDate.of(2007, 4, 24), "Aryan");
        Person testPerson2 = new Person(LocalDate.of(2000, 1, 16), "Max");

        // Start State (Immutable)
        RentalState state = new RentalState(
            List.of(testPerson, testPerson2),
            List.of(testPerson), // Aryan is on the deny list
            List.of(jdm1, luxury1, heli, boat),
            List.of()
        );

        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Functional Vehicle Rental App ===");

        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1 - Show Available Vehicles (Today)");
            System.out.println("2 - Rent a Vehicle (Simulation)");
            System.out.println("3 - Show Recursive Revenue Calculation");
            System.out.println("4 - Exit");
            System.out.print("Select: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> {
                    System.out.println("--- Available Vehicles ---");
                    var available = RentalLogic.getAvailableVehicles(state, LocalDate.now());
                    available.forEach(v -> {
                        // Pattern matching output
                        System.out.println(RentalLogic.getVehicleDescription(v) + " | $" + v.pricePerHour() + "/h");
                    });
                }
                case "2" -> {
                    // Try to rent luxury1 to Max (TestPerson2)
                    Contract contract = new Contract(
                        LocalDate.now(), 
                        LocalDate.now().plusDays(3), 
                        "Basic Insurance", 
                        testPerson2, 
                        luxury1
                    );
                    
                    Result<RentalState> result = RentalLogic.createContract(state, contract);
                    
                    // Functional Error Handling (No Exceptions!)
                    if (result instanceof Result.Success<RentalState> success) {
                        state = success.value(); // Update state locally inside loop
                        System.out.println("✅ Renting successful! Updated Immutable State.");
                    } else if (result instanceof Result.Failure<RentalState> failure) {
                        System.out.println("❌ Renting failed: " + failure.reason());
                    }
                }
                case "3" -> {
                    double totalValue = RentalLogic.calculateTotalContractValueRecursive(state.contracts());
                    System.out.println("💰 Total Value from ALL contracts: $" + totalValue + " (calculated via Recursion)");
                }
                case "4" -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }
}
