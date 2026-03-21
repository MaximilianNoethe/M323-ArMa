public record AirVehicle(
    int weight, 
    double pricePerHour, 
    String motorType, 
    String vehicleBrand, 
    int seatNumber,
    double wingspan
) implements Vehicle {}
