public record Car(
    int weight, 
    double pricePerHour, 
    String motorType, 
    String vehicleBrand, 
    int seatNumber, 
    int wheelCount
) implements Vehicle {}
