package helper_classes;

public enum MappingType {
    Simple(0), Vertical(1), Horizontal(2);

    private int number;

    MappingType(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}


