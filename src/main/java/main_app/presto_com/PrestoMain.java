package main_app.presto_com;

public class PrestoMain {
    public static void main (String[] agrs){
        /*PrestoMediator p = new PrestoMediator();
        p.createConnection();
        System.out.println(p.makeQuery("show create view system.pvFactsView"));*/
        for (int i = 1; i <= (48*4); i++){
            System.out.print("("+i+"),");
        }
    }
}
