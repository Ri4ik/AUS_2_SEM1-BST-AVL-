package com.mycompany.bst_du.gui.model;

import com.mycompany.bst_du.model.PcrSystem;
import com.mycompany.bst_du.domain.Patient;
import com.mycompany.bst_du.domain.PcrTest;
import com.mycompany.bst_du.dto.*;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class GuiModel {
    private final PcrSystem sys = new PcrSystem();

    // --- Patients ---
    public boolean insertPatient(Patient p) { return sys.op19_insertPatient(p); }
    public boolean deletePatientWithTests(String patientId) { return sys.op21_deletePatientWithTests(patientId); }
    public int countPatients() { return sys.countPatients(); }

    // --- Tests ---
    public boolean insertTest(PcrTest t) { return sys.op1_insertTest(t); }
    public Optional<PcrTest> findTestByCode(long code) { return sys.op18_findTestByCode(code); }
    public boolean deleteTestByCode(long code) { return sys.op20_deleteTestByCode(code); }
    public int countTests() { return sys.countTests(); }

    // --- Queries ---
    public Optional<TestWithPatient> findTestOfPatient(long code, String pid) { return sys.op2_findTestOfPatient(code, pid); }
    public List<PcrTest> allTestsOfPatientChrono(String pid) { return sys.op3_allTestsOfPatientChrono(pid); }

    public List<PcrTest> positiveByDistrictInPeriod(int district, LocalDate from, LocalDate toEx) { return sys.op4_positiveByDistrictInPeriod(district, from, toEx); }
    public List<PcrTest> allByDistrictInPeriod(int district, LocalDate from, LocalDate toEx) { return sys.op5_allByDistrictInPeriod(district, from, toEx); }

    public List<PcrTest> positiveByRegionInPeriod(int region, LocalDate from, LocalDate toEx) { return sys.op6_positiveByRegionInPeriod(region, from, toEx); }
    public List<PcrTest> allByRegionInPeriod(int region, LocalDate from, LocalDate toEx) { return sys.op7_allByRegionInPeriod(region, from, toEx); }

    public List<PcrTest> positiveInPeriod(LocalDate from, LocalDate toEx) { return sys.op8_positiveInPeriod(from, toEx); }
    public List<PcrTest> allInPeriod(LocalDate from, LocalDate toEx) { return sys.op9_allInPeriod(from, toEx); }

    public List<Patient> sickByDistrictAtDate(int district, LocalDate at, int days) { return sys.op10_sickByDistrictAtDate(district, at, days); }
    public List<PatientScore> sickByDistrictAtDateSortedByValue(int district, LocalDate at, int days) { return sys.op11_sickByDistrictAtDateSortedByValue(district, at, days); }
    public List<Patient> sickByRegionAtDate(int region, LocalDate at, int days) { return sys.op12_sickByRegionAtDate(region, at, days); }
    public List<Patient> sickEverywhereAtDate(LocalDate at, int days) { return sys.op13_sickEverywhereAtDate(at, days); }
    public List<PatientScore> oneSickPerDistrictMaxValue(LocalDate at, int days) { return sys.op14_oneSickPerDistrictMaxValue(at, days); }
    public List<DistrictCount> districtsBySickCount(LocalDate at, int days) { return sys.op15_districtsBySickCount(at, days); }
    public List<RegionCount> regionsBySickCount(LocalDate at, int days) { return sys.op16_regionsBySickCount(at, days); }

    public List<PcrTest> allByWorkstationInPeriod(long ws, LocalDate from, LocalDate toEx) { return sys.op17_allByWorkstationInPeriod(ws, from, toEx); }

    // --- CSV ---
    public void exportToCsv(Path dir) throws Exception { sys.exportToCsv(dir); }
    public void importFromCsv(Path dir) throws Exception { sys.importFromCsv(dir); }

    // --- Utils to build domain objects ---
    public static Patient mkPatient(String id, String fn, String ln, LocalDate birth){ return new Patient(id, fn, ln, birth); }
    public static PcrTest mkTest(long code, String pid, Instant ts, long ws, int dist, int reg, boolean pos, double val, String note){
        return new PcrTest(code, pid, ts, ws, dist, reg, pos, val, note);
    }
    
     public void clearAll() { sys.clearAll(); }
}
