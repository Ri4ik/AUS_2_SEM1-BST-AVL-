package com.mycompany.bst_du.gui.controller;

import com.mycompany.bst_du.gui.model.GuiModel;
import com.mycompany.bst_du.domain.Patient;
import com.mycompany.bst_du.domain.PcrTest;
import com.mycompany.bst_du.dto.*;
import com.mycompany.bst_du.domain.Patient;
import com.mycompany.bst_du.domain.PcrTest;
import com.mycompany.bst_du.dto.PatientScore;
import com.mycompany.bst_du.dto.DistrictCount;
import com.mycompany.bst_du.dto.RegionCount;
import java.time.LocalDate;
import java.util.List;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class GuiController {
    private final GuiModel model;

    public GuiController(GuiModel model) { this.model = model; }
     
    // --- Patients ---
    public String insertPatient(String id, String fn, String ln, String birthIso) {
        try {
            if (id == null || id.isEmpty()) return "Patient ID required";
            LocalDate birth = LocalDate.parse(birthIso);
            Patient p = GuiModel.mkPatient(id, fn, ln, birth);
            boolean ok = model.insertPatient(p);
            return ok ? "Patient inserted: " + id : "Patient already exists: " + id;
        } catch (Exception e) {
            return "Error insertPatient: " + e.getMessage();
        }
    }

    public String deletePatient(String id) {
        try {
            boolean ok = model.deletePatientWithTests(id);
            return ok ? "Patient deleted with tests: " + id : "Patient not found: " + id;
        } catch (Exception e) {
            return "Error deletePatient: " + e.getMessage();
        }
    }

    public String counts() {
        return "Patients=" + model.countPatients() + ", Tests=" + model.countTests();
    }

    // --- Tests ---
    public String insertTest(String codeStr, String pid, String instantIso, String wsStr,
                             String distStr, String regStr, String posStr, String valStr, String note) {
        try {
            long code = Long.parseLong(codeStr);
            long ws = Long.parseLong(wsStr);
            int dist = Integer.parseInt(distStr);
            int reg = Integer.parseInt(regStr);
            boolean pos = "1".equals(posStr) || "true".equalsIgnoreCase(posStr);
            double val = Double.parseDouble(valStr);
            Instant ts = Instant.parse(instantIso);
            PcrTest t = GuiModel.mkTest(code, pid, ts, ws, dist, reg, pos, val, note);
            boolean ok = model.insertTest(t);
            return ok ? "Test inserted: " + code : "Test code already exists or patient missing";
        } catch (Exception e) {
            return "Error insertTest: " + e.getMessage();
        }
    }

    public String findTestByCode(String codeStr) {
        try {
            long code = Long.parseLong(codeStr);
            Optional<PcrTest> r = model.findTestByCode(code);
            return r.map(this::fmtTest).orElse("Not found: " + code);
        } catch (Exception e) {
            return "Error findTestByCode: " + e.getMessage();
        }
    }

    public String deleteTestByCode(String codeStr) {
        try {
            long code = Long.parseLong(codeStr);
            boolean ok = model.deleteTestByCode(code);
            return ok ? "Deleted test: " + code : "Test not found: " + code;
        } catch (Exception e) {
            return "Error deleteTestByCode: " + e.getMessage();
        }
    }

    // --- Queries ---
    public String testsOfPatientChrono(String pid) {
        try {
            List<PcrTest> list = model.allTestsOfPatientChrono(pid);
            return fmtTests(list);
        } catch (Exception e) {
            return "Error testsOfPatientChrono: " + e.getMessage();
        }
    }

    public String districtInPeriod(String districtStr, String fromIso, String toIso, boolean onlyPos) {
        try {
            int d = Integer.parseInt(districtStr);
            LocalDate from = LocalDate.parse(fromIso);
            LocalDate toEx = LocalDate.parse(toIso);
            List<PcrTest> list = onlyPos ? model.positiveByDistrictInPeriod(d, from, toEx)
                                         : model.allByDistrictInPeriod(d, from, toEx);
            return fmtTests(list);
        } catch (Exception e) {
            return "Error districtInPeriod: " + e.getMessage();
        }
    }

    public String regionInPeriod(String regionStr, String fromIso, String toIso, boolean onlyPos) {
        try {
            int r = Integer.parseInt(regionStr);
            LocalDate from = LocalDate.parse(fromIso);
            LocalDate toEx = LocalDate.parse(toIso);
            List<PcrTest> list = onlyPos ? model.positiveByRegionInPeriod(r, from, toEx)
                                         : model.allByRegionInPeriod(r, from, toEx);
            return fmtTests(list);
        } catch (Exception e) {
            return "Error regionInPeriod: " + e.getMessage();
        }
    }

    public String globalInPeriod(String fromIso, String toIso, boolean onlyPos) {
        try {
            LocalDate from = LocalDate.parse(fromIso);
            LocalDate toEx = LocalDate.parse(toIso);
            List<PcrTest> list = onlyPos ? model.positiveInPeriod(from, toEx)
                                         : model.allInPeriod(from, toEx);
            return fmtTests(list);
        } catch (Exception e) {
            return "Error globalInPeriod: " + e.getMessage();
        }
    }

    public String workstationInPeriod(String wsStr, String fromIso, String toIso) {
        try {
            long ws = Long.parseLong(wsStr);
            LocalDate from = LocalDate.parse(fromIso);
            LocalDate toEx = LocalDate.parse(toIso);
            List<PcrTest> list = model.allByWorkstationInPeriod(ws, from, toEx);
            return fmtTests(list);
        } catch (Exception e) {
            return "Error workstationInPeriod: " + e.getMessage();
        }
    }

    // --- CSV ---
    public String exportCsv(String dirPath) {
        try {
            model.exportToCsv(Path.of(dirPath));
            return "Export OK: " + dirPath;
        } catch (Exception e) {
            return "Error exportCsv: " + e.getMessage();
        }
    }

    public String importCsv(String dirPath) {
        try {
            model.importFromCsv(Path.of(dirPath));
            return "Import OK: " + dirPath;
        } catch (Exception e) {
            return "Error importCsv: " + e.getMessage();
        }
    }

    // --- Formatting helpers ---
    private String fmtTests(List<PcrTest> arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("Count=").append(arr.size()).append('\n');
        for (PcrTest t : arr) sb.append(fmtTest(t)).append('\n');
        return sb.toString();
    }
    private String fmtTest(PcrTest t) {
        return "code=" + t.testCode + ", pid=" + t.patientId + ", ts=" + t.timestamp
               + ", ws=" + t.workstationId + ", dist=" + t.district + ", reg=" + t.region
               + ", pos=" + t.positive + ", val=" + t.value + ", note=" + t.note;
    } 
    
    // ===== Sickness (ops 10–16) — имена ровно как ждёт MainFrame =====
    public List<Patient> sickByDistrictAtDate(int district, LocalDate atDate, int daysWindow) {
        return model.sickByDistrictAtDate(district, atDate, daysWindow);
    }

    public List<PatientScore> sickByDistrictAtDateSortedByValue(int district, LocalDate atDate, int daysWindow) {
        return model.sickByDistrictAtDateSortedByValue(district, atDate, daysWindow);
    }

    public List<Patient> sickByRegionAtDate(int region, LocalDate atDate, int daysWindow) {
        return model.sickByRegionAtDate(region, atDate, daysWindow);
    }

    public List<Patient> sickEverywhereAtDate(LocalDate atDate, int daysWindow) {
        return model.sickEverywhereAtDate(atDate, daysWindow);
    }

    public List<PatientScore> oneSickPerDistrictMaxValue(LocalDate atDate, int daysWindow) {
        return model.oneSickPerDistrictMaxValue(atDate, daysWindow);
    }

    public List<DistrictCount> districtsBySickCount(LocalDate atDate, int daysWindow) {
        return model.districtsBySickCount(atDate, daysWindow);
    }

    public List<RegionCount> regionsBySickCount(LocalDate atDate, int daysWindow) {
        return model.regionsBySickCount(atDate, daysWindow);
    }

    // ===== Queries для таблиц (без префикса opXX_ в модели) =====
    public List<PcrTest> modelAllTestsOfPatientChrono(String patientId) {
        return model.allTestsOfPatientChrono(patientId);
    }

    public List<PcrTest> modelAllByDistrictInPeriod(int district, LocalDate from, LocalDate toExclusive) {
        return model.allByDistrictInPeriod(district, from, toExclusive);
    }

    public List<PcrTest> modelPositiveByDistrictInPeriod(int district, LocalDate from, LocalDate toExclusive) {
        return model.positiveByDistrictInPeriod(district, from, toExclusive);
    }

    public List<PcrTest> modelAllByRegionInPeriod(int region, LocalDate from, LocalDate toExclusive) {
        return model.allByRegionInPeriod(region, from, toExclusive);
    }

    public List<PcrTest> modelPositiveByRegionInPeriod(int region, LocalDate from, LocalDate toExclusive) {
        return model.positiveByRegionInPeriod(region, from, toExclusive);
    }

    public List<PcrTest> modelAllInPeriod(LocalDate from, LocalDate toExclusive) {
        return model.allInPeriod(from, toExclusive);
    }

    public List<PcrTest> modelPositiveInPeriod(LocalDate from, LocalDate toExclusive) {
        return model.positiveInPeriod(from, toExclusive);
    }

    public List<PcrTest> modelAllByWorkstationInPeriod(long workstationId, LocalDate from, LocalDate toExclusive) {
        return model.allByWorkstationInPeriod(workstationId, from, toExclusive);
    }
     // ===== op-2 Find test of patient =====
    public Optional<TestWithPatient> findTestOfPatient(long code, String patientId) {
        return model.findTestOfPatient(code, patientId);
    }

}
