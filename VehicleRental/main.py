import sys
from models import Vehicle, RentalState, Success, Failure
from logic import (
    handle_command, 
    get_available_vehicles, 
    get_rented_vehicles,
    calculate_potential_revenue_recursive
)

def display_menu(state: RentalState):
    print("\n" + "="*45)
    print("🚗 VEHICLE RENTAL SYSTEM (FP EDITION) 🚗")
    print("="*45)
    print(f"Bisheriger realisierter Umsatz: {state.total_revenue} CHF")
    print("-" * 45)
    print("1. Freie Fahrzeuge anzeigen")
    print("2. Gemietete Fahrzeuge anzeigen")
    print("3. Fahrzeug mieten")
    print("4. Fahrzeug zurückgeben")
    print("5. Potenzieller Gesamt-Tagesumsatz (Rekursion)")
    print("6. Beenden")
    print("-" * 45)

def main():
    # Initiale Daten (Immutable) vorbereiten
    initial_vehicles = (
        Vehicle(1, "BMW", "3er", 120.0, False),
        Vehicle(2, "Audi", "A4", 110.0, False),
        Vehicle(3, "VW", "Golf", 80.0, False),
        Vehicle(4, "Tesla", "Model 3", 150.0, False)
    )
    
    current_state = RentalState(initial_vehicles, 0.0)
    
    # Zentrale Ein-/Ausgabe Schleife
    # Modul Anforderung: "Stellen Sie sicher, dass Sie Ausgaben an einer zentralen Stelle implementieren"
    while True:
        display_menu(current_state)
        choice = input("Bitte wählen Sie eine Option (1-6): ").strip()
        
        if choice == "1":
            print("\n--- Freie Fahrzeuge ---")
            available = get_available_vehicles(current_state)
            if not available:
                print("Keine Fahrzeuge frei.")
            for v in available:
                print(f"ID: {v.id} | {v.brand} {v.model} | {v.price_per_day} CHF/Tag")
                
        elif choice == "2":
            print("\n--- Gemietete Fahrzeuge ---")
            rented = get_rented_vehicles(current_state)
            if not rented:
                print("Momentan sind keine Fahrzeuge gemietet.")
            for v in rented:
                print(f"ID: {v.id} | {v.brand} {v.model}")
                
        elif choice == "3":
            try:
                v_id = int(input("Welches Fahrzeug-ID möchten Sie mieten? "))
                # Aufruf unserer reinen Funktion (ohne globale Daten zu verändern!)
                result = handle_command(("RENT", v_id), current_state)
                
                # Handling unseres Result-Monads (Success / Failure Pattern)
                if isinstance(result, Success):
                    current_state = result.value
                    print("\n✅ Fahrzeug erfolgreich gemietet!")
                elif isinstance(result, Failure):
                    print(f"\n❌ Fehler: {result.reason}")
            except ValueError:
                print("\n❌ Ungültige Eingabe.")
                
        elif choice == "4":
            try:
                v_id = int(input("Welches Fahrzeug-ID möchten Sie zurückgeben? "))
                result = handle_command(("RETURN", v_id), current_state)
                
                if isinstance(result, Success):
                    current_state = result.value
                    print("\n✅ Fahrzeug erfolgreich zurückgegeben!")
                elif isinstance(result, Failure):
                    print(f"\n❌ Fehler: {result.reason}")
            except ValueError:
                print("\n❌ Ungültige Eingabe.")
                
        elif choice == "5":
            # Test der Rekursionsfunktion
            pot_revenue = calculate_potential_revenue_recursive(current_state.vehicles)
            print(f"\n💰 Wenn alle Autos vermietet wären, läge der theoretische")
            print(f"Umsatz bei: {pot_revenue} CHF (berechnet mittels REKURSION)")
            
        elif choice == "6":
            print("Programm wird beendet. Auf Wiedersehen!")
            sys.exit(0)
        else:
            print("❌ Ungültige Option.")

if __name__ == "__main__":
    main()
