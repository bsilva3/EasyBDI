package helper_classes.ui_utils;

import java.util.Objects;

/**
 * An element in a list in the query ui
 */
public class ListElementWrapper {
    private String name;
    private Object obj;
    private ListElementType type;

    public ListElementWrapper(String name, Object obj, ListElementType type) {
        this.name = name;
        this.obj = obj;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getObj() {
        return this.obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public ListElementType getType() {
        return this.type;
    }

    public void setType(ListElementType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListElementWrapper)) return false;
        ListElementWrapper that = (ListElementWrapper) o;
        return this.getName().equals(that.getName()) &&
                Objects.equals(this.getObj(), that.getObj()) &&
                this.getType() == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getName(), this.getObj(), this.getType());
    }
}
