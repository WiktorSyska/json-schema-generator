
# JSON Schema Generator

Aplikacja desktopowa napisana w JavaFX, umożliwiająca automatyczne generowanie schematów JSON Schema (specyfikacja draft 2020-12) na podstawie przykładowych dokumentów JSON, a następnie wykorzystanie tych schematów do walidacji innych dokumentów.
Pełna dokumentacja projektu (część teoretyczna, architektura, diagramy UML, instrukcja użytkowania, testy) dostępna jest w pliku [`docs/Dokumentacja.pdf`](docs/Dokumentacja.pdf).

## Funkcjonalności

- Wczytywanie plików JSON (przycisk lub przeciągnięcie pliku na okno aplikacji)
- Generowanie schematu na podstawie pojedynczego dokumentu
- Generowanie schematu na podstawie wielu dokumentów (tablicy JSON) z automatycznym wykrywaniem pól wymaganych (`required`)
- Edycja schematu wybór pól `required` za pomocą checkboxów, z uwzględnieniem zagnieżdżonych obiektów i elementów tablic
- Walidacja dokumentu JSON względem aktualnego schematu, wraz z raportem błędów (ścieżka w dokumencie + numer linii)
- Zapis wygenerowanego schematu do pliku
- Zarządzanie lokalnym repozytorium schematów (zapis, wczytanie, usunięcie)
- Podgląd struktury JSON oraz schematu w formie kolorowanego drzewa

## Technologie

| Technologia | Rola w projekcie |
|---|---|
| Java 21 | główny język implementacji |
| JavaFX 21 | interfejs graficzny użytkownika |
| FXML | deklaratywny opis układu okna |
| JUnit 5 | framework testów jednostkowych |

### Struktura pakietów (`com.example.jsonschemagenerator`)

- **loader** - wczytywanie plików JSON z dysku (`JsonFileLoader`, `JsonLoadException`)
- **json** - własny model obiektowy JSON oparty na wzorcu Kompozyt (`JsonValue`, `JsonObject`, `JsonArray`, `JsonString`, `JsonNumber`, `JsonBoolean`, `JsonNull`) oraz własny `ObjectMapper` do serializacji
- **parser** - ręcznie napisany parser JSON metodą rekurencyjnego zejścia (`JsonParser`, `JsonParserException`)
- **generator** - generowanie schematu JSON Schema (`SchemaGenerator`), w tym podpakiet `dateValidator` wykrywający formaty dat (ISO 8601)
- **validator** - walidacja dokumentów JSON względem schematu (`JsonSchemaValidator`)
- **database** - trwałe przechowywanie schematów na dysku (`SchemaRepository`)
- **views / views.controllers** - kontrolery JavaFX oraz pliki FXML

## Instrukcja użytkowania

1. **Wczytaj plik JSON** - przyciskiem „Wczytaj JSON…” lub przez przeciągnięcie pliku na obszar drzewa.
2. **Wygeneruj schemat** - „Generuj schemat” dla pojedynczego dokumentu lub „Generuj dla wielu” dla pliku zawierającego tablicę dokumentów.
3. **Edytuj schemat** (opcjonalnie) - przyciskiem „Edytuj schemat” otwierane jest okno z listą checkboxów odpowiadającą strukturze schematu, umożliwiające oznaczenie pól jako `required`.
4. **Waliduj dokument** - przyciskiem „Waliduj plik…” wskazujesz dokument JSON do sprawdzenia względem aktualnego schematu.
5. **Zapisz schemat** - przyciskiem „Zapisz schemat…” do pliku, lub poprzez „Schematy…” do lokalnego repozytorium.

## Uruchomienie

### Z linii komend (Maven)
 
```bash
mvn clean install
mvn javafx:run
```
 
### W IntelliJ IDEA
 
1. Otwórz projekt: **File → Open…** i wskaż katalog z plikiem `pom.xml`. IntelliJ automatycznie rozpozna projekt jako Mavenowy i zaimportuje zależności.
2. Odnajdź klasę startową `Launcher` (lub `HelloApplication`) w drzewie projektu.
3. Kliknij prawym przyciskiem na klasę i wybierz **Run 'Launcher.main()'**, albo kliknij zielony przycisk ▶ przy metodzie `main`.