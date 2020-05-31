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

    public static MappingType getMapping(int n) {
        switch (n){
            case 0:
                return MappingType.Simple;
            case 1:
                return MappingType.Vertical;
            case 2:
                return MappingType.Horizontal;
        }
        return null;
    }
}


