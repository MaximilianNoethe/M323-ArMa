import java.util.List;

public record RentalState(
    List<Person> customerList,
    List<Person> denyList,
    List<Vehicle> vehicles,
    List<Contract> contracts
) {
    // Compact constructor to ensure deep immutability
    public RentalState {
        customerList = List.copyOf(customerList);
        denyList = List.copyOf(denyList);
        vehicles = List.copyOf(vehicles);
        contracts = List.copyOf(contracts);
    }
}
