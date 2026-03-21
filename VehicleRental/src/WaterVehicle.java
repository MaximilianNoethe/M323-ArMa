public record WaterVehicle(
    int weight, 
    double pricePerHour, 
    String motorType, 
    String vehicleBrand, 
    int seatNumber,
    boolean isSailboat
) implements Vehicle {}
