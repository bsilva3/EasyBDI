package helper_classes.elements;

import java.util.Objects;

public class DatatypePair {

    private String datatype1;
    private String datatype2;

    public DatatypePair(String datatype1, String datatype2) {
        this.datatype1 = datatype1;
        this.datatype2 = datatype2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatatypePair that = (DatatypePair) o;
        return datatype1.equals(that.datatype1) &&
                datatype2.equals(that.datatype2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datatype1, datatype2);
    }
}
