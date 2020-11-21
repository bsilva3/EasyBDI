package main_app.presto_com;

import helper_classes.utils_other.Utils;

import java.io.File;

public class PrestoMain {
    public static void main (String[] agrs){
        /*PrestoMediator p = new PrestoMediator();
        p.createConnection();
        System.out.println(p.makeQuery("show create view system.pvFactsView"));*/
        for (int i = 1; i <= ((366*48)*4); i++){
            System.out.print("("+i+"),");
        }

        /*String s = "file:///home/bruno/2012_2013_strategy.csv";
        System.out.println(s.contains(File.separator) && s.contains("."));
        System.out.println(Utils.getFileNameNoExtension("2012_2013_strategy (view)"));*/
    }
}
