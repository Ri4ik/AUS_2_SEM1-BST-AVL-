package com.mycompany.bst_du.gui;

import com.mycompany.bst_du.gui.controller.GuiController;
import com.mycompany.bst_du.domain.PcrTest;

import java.time.LocalDate;
import java.util.List;

public final class UtilQueries {
    private UtilQueries(){}

    public static List<PcrTest> testsOfPatient(GuiController ctl, String pid){
        try { return ctl.modelAllTestsOfPatientChrono(pid); }
        catch (Throwable t){ return java.util.List.of(); }
    }

    public static List<PcrTest> district(GuiController ctl, String dist, String from, String toEx, boolean pos){
        try {
            int d = Integer.parseInt(dist);
            LocalDate f = LocalDate.parse(from);
            LocalDate t = LocalDate.parse(toEx);
            return pos ? ctl.modelPositiveByDistrictInPeriod(d,f,t) : ctl.modelAllByDistrictInPeriod(d,f,t);
        } catch (Throwable e){ return java.util.List.of(); }
    }

    public static List<PcrTest> region(GuiController ctl, String reg, String from, String toEx, boolean pos){
        try {
            int r = Integer.parseInt(reg);
            LocalDate f = LocalDate.parse(from);
            LocalDate t = LocalDate.parse(toEx);
            return pos ? ctl.modelPositiveByRegionInPeriod(r,f,t) : ctl.modelAllByRegionInPeriod(r,f,t);
        } catch (Throwable e){ return java.util.List.of(); }
    }

    public static List<PcrTest> global(GuiController ctl, String from, String toEx, boolean pos){
        try {
            LocalDate f = LocalDate.parse(from);
            LocalDate t = LocalDate.parse(toEx);
            return pos ? ctl.modelPositiveInPeriod(f,t) : ctl.modelAllInPeriod(f,t);
        } catch (Throwable e){ return java.util.List.of(); }
    }

    public static List<PcrTest> workstation(GuiController ctl, String ws, String from, String toEx){
        try {
            long w = Long.parseLong(ws);
            LocalDate f = LocalDate.parse(from);
            LocalDate t = LocalDate.parse(toEx);
            return ctl.modelAllByWorkstationInPeriod(w,f,t);
        } catch (Throwable e){ return java.util.List.of(); }
    }
}
