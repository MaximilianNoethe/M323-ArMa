from dataclasses import dataclass
from typing import Tuple, Generic, TypeVar, Union

T = TypeVar('T')

# --- Funktionale Datentypen: Result Type ---
# In der funktionalen Programmierung vermeiden wir geworfene Exceptions (Seiteneffekte).
# Stattdessen geben wir explizit Erfolg (Success) oder Fehler (Failure) zurück.

@dataclass(frozen=True)
class Success(Generic[T]):
    value: T

@dataclass(frozen=True)
class Failure:
    reason: str

Result = Union[Success[T], Failure]

# --- Domain Data Models (Immutable) ---
# Modul Anforderung: "Die Daten sind immutable gehalten"
# Wir nutzen @dataclass(frozen=True), damit Eigenschaften nach der Erstellung
# nicht mehr verändert werden können.

@dataclass(frozen=True)
class Vehicle:
    id: int
    brand: str
    model: str
    price_per_day: float
    is_rented: bool

@dataclass(frozen=True)
class RentalState:
    vehicles: Tuple[Vehicle, ...]  # Tuple ist immutable (im Gegensatz zu list)
    total_revenue: float
