public sealed interface Vehicle permits Car, AirVehicle, WaterVehicle {
    String vehicleBrand();
    double pricePerHour();
}
