package memorybook.com.memorybook.entity;

// Lesestatus eines Buches mit Anzeigename und Bootstrap-Farbe
public enum ReadingStatus {

    GEPLANT("Will ich lesen", "secondary"),
    LESE_ICH("Lese ich gerade", "primary"),
    GELESEN("Gelesen", "success"),
    ABGEBROCHEN("Abgebrochen", "danger");

    private final String label;
    private final String color;

    ReadingStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}
