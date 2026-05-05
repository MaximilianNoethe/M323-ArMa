import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RentalLogicTest {
    private static final Car TEST_CAR = new Car(1500, 10.00, "Benzin", "TestCar", 4, 4);
    private static final WaterVehicle TEST_BOAT = new WaterVehicle(3000, 20.00, "Diesel", "TestBoat", 6, false);
    private static final Person ADULT = new Person(LocalDate.of(2000, 1, 1), "Max", "max@example.com");

    @Test
    void validEmailIsAcceptedAndInvalidEmailsAreRejected() {
        assertTrue(RentalLogic.isValidEmail("max@example.com"));
        assertFalse(RentalLogic.isValidEmail("max.example.com"));
        assertFalse(RentalLogic.isValidEmail("max@"));
    }

    @Test
    void availableVehiclesReturnsSameResultAndDoesNotChangeState() {
        Contract existingContract = new Contract(
            LocalDate.of(2026, 3, 9),
            LocalDate.of(2026, 3, 12),
            "Test",
            ADULT,
            TEST_CAR,
            true,
            true
        );
        RentalState state = new RentalState(
            List.of(ADULT),
            List.of(),
            List.of(TEST_CAR, TEST_BOAT),
            List.of(existingContract)
        );

        List<Vehicle> firstResult = RentalLogic.getAvailableVehicles(state, LocalDate.of(2026, 3, 10));
        List<Vehicle> secondResult = RentalLogic.getAvailableVehicles(state, LocalDate.of(2026, 3, 10));

        assertEquals(List.of(TEST_BOAT), firstResult);
        assertEquals(firstResult, secondResult);
        assertEquals(1, state.contracts().size());
        assertEquals(2, state.vehicles().size());
    }

    @Test
    void createContractReturnsNewStateAndKeepsOriginalStateUnchanged() {
        RentalState originalState = new RentalState(
            List.of(),
            List.of(),
            List.of(TEST_CAR),
            List.of()
        );
        Contract newContract = new Contract(
            LocalDate.of(2026, 3, 14),
            LocalDate.of(2026, 3, 16),
            "Test",
            ADULT,
            TEST_CAR,
            true,
            true
        );

        Result<RentalState> result = RentalLogic.createContract(originalState, newContract);

        assertTrue(result instanceof Result.Success<RentalState>);

        Result.Success<RentalState> success = (Result.Success<RentalState>) result;
        RentalState newState = success.value();

        assertNotSame(originalState, newState);
        assertEquals(0, originalState.contracts().size());
        assertEquals(1, newState.contracts().size());
        assertEquals(0, originalState.customerList().size());
        assertEquals(1, newState.customerList().size());
    }

    @Test
    void collisionDetectionOnlyFindsOverlappingRentalsForSameVehicle() {
        Contract existingContract = new Contract(
            LocalDate.of(2026, 3, 9),
            LocalDate.of(2026, 3, 12),
            "Test",
            ADULT,
            TEST_CAR,
            true,
            true
        );
        List<Contract> contracts = List.of(existingContract);

        assertTrue(RentalLogic.checkCollision(
            contracts,
            TEST_CAR,
            LocalDate.of(2026, 3, 10),
            LocalDate.of(2026, 3, 11)
        ));
        assertFalse(RentalLogic.checkCollision(
            contracts,
            TEST_CAR,
            LocalDate.of(2026, 3, 13),
            LocalDate.of(2026, 3, 14)
        ));
        assertFalse(RentalLogic.checkCollision(
            contracts,
            TEST_BOAT,
            LocalDate.of(2026, 3, 10),
            LocalDate.of(2026, 3, 11)
        ));
    }

    @Test
    void recursiveRevenueCalculationAddsContractValues() {
        Contract firstContract = new Contract(
            LocalDate.of(2026, 3, 9),
            LocalDate.of(2026, 3, 11),
            "Test",
            ADULT,
            TEST_CAR,
            true,
            true
        );
        Contract secondContract = new Contract(
            LocalDate.of(2026, 3, 12),
            LocalDate.of(2026, 3, 13),
            "Test",
            ADULT,
            TEST_BOAT,
            true,
            true
        );

        double total = RentalLogic.calculateTotalContractValueRecursive(List.of(firstContract, secondContract));

        assertEquals(960.00, total);
    }

    @Test
    void vehicleTextFunctionsReturnReadableValues() {
        assertEquals("Auto: TestCar", RentalLogic.getVehicleDescription(TEST_CAR));
        assertTrue(RentalLogic.getVehicleDetails(TEST_BOAT).contains("Segelboot: nein"));
    }
}
