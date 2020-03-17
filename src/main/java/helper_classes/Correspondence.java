package helper_classes;

public class Correspondence {

    private ColumnData globalCol;
    private ColumnData localCol;
    private String conversion;

    public Correspondence(ColumnData globalCol, ColumnData localCol, String conversion) {
        this.globalCol = globalCol;
        this.localCol = localCol;
        this.conversion = conversion;
    }

    public ColumnData getGlobalCol() {
        return globalCol;
    }

    public void setGlobalCol(ColumnData globalCol) {
        this.globalCol = globalCol;
    }

    public ColumnData getLocalCol() {
        return localCol;
    }

    public void setLocalCol(ColumnData localCol) {
        this.localCol = localCol;
    }

    public String getConversion() {
        return conversion;
    }

    public void setConversion(String conversion) {
        this.conversion = conversion;
    }
}
