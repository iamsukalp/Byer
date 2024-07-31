package com.byer.byerretailer.Models;
import java.util.ArrayList;

public class Units {

    private static ArrayList<String> units;

    public static ArrayList<String> getUnits() {
        units = new ArrayList<>();

        units.add("piece");
        units.add("kg");
        units.add("gram");
        units.add("ml");
        units.add("liter");
        units.add("mm");
        units.add("ft");
        units.add("meter");
        units.add("sq. ft.");
        units.add("sq. mtr");
        units.add("km");
        units.add("set");
        units.add("hour");
        units.add("day");
        units.add("bunch");
        units.add("bundle");
        units.add("month");
        units.add("year");
        units.add("service");
        units.add("work");
        units.add("packet");
        units.add("box");
        units.add("pound");
        units.add("dozen");
        units.add("gunta");
        units.add("pair");
        units.add("minute");
        units.add("quintal");
        units.add("ton");
        units.add("capsule");
        units.add("tablet");
        units.add("plate");
        units.add("inch");

        return units;
    }
}
