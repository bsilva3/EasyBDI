package main_app.query_ui;

public enum LogicOperation {
    AND, OR, NOT;

    public static LogicOperation[] getOpList(){
        return new LogicOperation[]{LogicOperation.AND, LogicOperation.OR, LogicOperation.NOT};
    }
}
