public class Main {

    public static void main(String[] args){

        final String URL = "jdbc:presto://127.0.0.1:8080";
        PrestoConnector connector = new PrestoConnector();
        connector.setConnection(URL);
        //connector.makeQuery("select * from mongodb.products.products");
        connector.makeQuery("describe mongodb.products.products");
    }
}
