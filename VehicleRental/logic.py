from typing import Callable, Tuple, Any, Dict
from models import RentalState, Vehicle, Result, Success, Failure
from functools import reduce

# --- 1. Pure Functions ---
# Modul Anforderung: "Wir verwenden Pure Functions"
# Diese Funktionen haben keine Side-Effects (verändern keine globalen Variablen).
# Sie erhalten Input und geben determiniert einen Output (oder neuen State) zurück.

def get_available_vehicles(state: RentalState) -> Tuple[Vehicle, ...]:
    # --- 2. Higher-Order-Functions (HOF) & Pipelines ---
    # Modul Anforderungen: "HOF", "Pipelines", "Map/Filter"
    # filter() nimmt eine Lambda-Funktion als Argument.
    return tuple(filter(lambda v: not v.is_rented, state.vehicles))

def get_rented_vehicles(state: RentalState) -> Tuple[Vehicle, ...]:
    return tuple(filter(lambda v: v.is_rented, state.vehicles))

def get_all_vehicle_names(state: RentalState) -> Tuple[str, ...]:
    # Map wendet eine Transformation auf alle Elemente an
    return tuple(map(lambda v: f"{v.brand} {v.model}", state.vehicles))

# --- 3. Rekursion ---
# Modul Anforderung: "Wir wenden Rekursion an"
def calculate_potential_revenue_recursive(vehicles: Tuple[Vehicle, ...]) -> float:
    """Berechnet den theoretischen Tages-Gesamtwert aller Autos durch REKURSION statt Loops."""
    if not vehicles:
        return 0.0
    # Ruft sich selbst mit dem Rest der Liste wieder auf! (Head / Tail Pattern)
    return vehicles[0].price_per_day + calculate_potential_revenue_recursive(vehicles[1:])

# Alternative mit reduce (entspricht foldLeft in z.B. Scala)
# Modul Anforderung: "Einsatz kennengelernter Features (u. a. foldLeft)"
def calculate_potential_revenue_foldLeft(vehicles: Tuple[Vehicle, ...]) -> float:
    return reduce(lambda acc, v: acc + v.price_per_day, vehicles, 0.0)

# --- Core Business Logic ---
# Da unsere Daten immutable sind, müssen wir Kopien erzeugen, anstatt Eigenschaften zu überschreiben.

def rent_vehicle(state: RentalState, vehicle_id: int) -> Result[RentalState]:
    found = tuple(filter(lambda v: v.id == vehicle_id, state.vehicles))
    
    if not found:
        return Failure(f"Fahrzeug mit ID {vehicle_id} nicht gefunden.")
        
    vehicle = found[0]
    if vehicle.is_rented:
        return Failure(f"{vehicle.brand} {vehicle.model} ist bereits gemietet.")
        
    # Map-Pipeline zur immutablen Erstellung der neuen Liste
    updated_vehicles = tuple(
        map(lambda v: Vehicle(v.id, v.brand, v.model, v.price_per_day, True) if v.id == vehicle_id else v, 
            state.vehicles)
    )
    
    # Neuen State zurückgeben (Umsatz steigt!)
    new_state = RentalState(updated_vehicles, state.total_revenue + vehicle.price_per_day)
    return Success(new_state)

def return_vehicle(state: RentalState, vehicle_id: int) -> Result[RentalState]:
    found = tuple(filter(lambda v: v.id == vehicle_id, state.vehicles))
    
    if not found:
        return Failure(f"Fahrzeug mit ID {vehicle_id} nicht gefunden.")
        
    vehicle = found[0]
    if not vehicle.is_rented:
        return Failure(f"{vehicle.brand} {vehicle.model} ist gar nicht gemietet.")
        
    updated_vehicles = tuple(
        map(lambda v: Vehicle(v.id, v.brand, v.model, v.price_per_day, False) if v.id == vehicle_id else v, 
            state.vehicles)
    )
    
    new_state = RentalState(updated_vehicles, state.total_revenue)
    return Success(new_state)

# --- 4. Pattern Matching Simulation ---
# Modul Anforderung: "Wir arbeiten mit PatternMatching"
# Python 3.10 hat `match/case`. Auf niedrigeren Versionen repräsentiert 
# Functional-Dictionary-Dispatch das exakt gleiche FP-Konzept.

def handle_command(command: Tuple[str, Any], state: RentalState) -> Result[RentalState]:
    action, payload = command
    
    # Dispatch-Table als Pattern Matcher Ersatz
    pattern_matcher: Dict[str, Callable[[RentalState, Any], Result[RentalState]]] = {
        "RENT": rent_vehicle,
        "RETURN": return_vehicle
    }
    
    matched_function = pattern_matcher.get(action)
    if matched_function:
        return matched_function(state, payload)
    
    return Failure(f"Unbekanntes Kommando (Pattern nicht gefunden): {action}")
