package com.mycompany.bst_du.gui.controller;

import com.mycompany.bst_du.gui.model.GuiModel;
import com.mycompany.bst_du.domain.Patient;
import com.mycompany.bst_du.domain.PcrTest;
import com.mycompany.bst_du.dto.*;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GuiController {
    private final GuiModel model;

    public GuiController(GuiModel model) { this.model = model; }

    // ========================= Utilities: strict parsing & guards =========================
    private static void requireNonEmpty(String s, String field) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(field + " is required");
    }
    private static int parseIntStrict(String s, String field) {
        requireNonEmpty(s, field);
        return Integer.parseInt(s.trim());
    }
    private static long parseLongStrict(String s, String field) {
        requireNonEmpty(s, field);
        return Long.parseLong(s.trim());
    }
    private static double parseDoubleStrict(String s, String field) {
        requireNonEmpty(s, field);
        return Double.parseDouble(s.trim());
    }
    private static LocalDate parseDateStrict(String s, String field) {
        requireNonEmpty(s, field);
        return LocalDate.parse(s.trim());
    }
    private static Instant parseInstantStrict(String s, String field) {
        requireNonEmpty(s, field);
        return Instant.parse(s.trim());
    }
    private static boolean parseBool10(String s) {
        if (s == null) return false;
        String v = s.trim();
        return "1".equals(v) || "true".equalsIgnoreCase(v);
    }

    // ========================= Patients =========================
    public String insertPatient(String id, String fn, String ln, String birthIso) {
        try {
            requireNonEmpty(id, "Patient ID");
            LocalDate birth = parseDateStrict(birthIso, "Birth (YYYY-MM-DD)");
            Patient p = GuiModel.mkPatient(id.trim(), fn == null ? "" : fn.trim(), ln == null ? "" : ln.trim(), birth);
            boolean ok = model.insertPatient(p);
            return ok ? "Patient inserted: " + id : "Patient already exists: " + id;
        } catch (Exception e) {
            return "Error insertPatient: " + e.getMessage();
        }
    }

    public String deletePatient(String id) {
        try {
            requireNonEmpty(id, "Patient ID");
            boolean ok = model.deletePatientWithTests(id.trim());
            return ok ? "Patient deleted with tests: " + id : "Patient not found: " + id;
        } catch (Exception e) {
            return "Error deletePatient: " + e.getMessage();
        }
    }

    public String counts() {
        return "Patients=" + model.countPatients() + ", Tests=" + model.countTests();
    }

    // ========================= Tests =========================
    public String insertTest(String codeStr, String pid, String instantIso, String wsStr,
                             String distStr, String regStr, String posStr, String valStr, String note) {
        try {
            long code = parseLongStrict(codeStr, "Code");
            requireNonEmpty(pid, "Patient ID");
            Instant ts = parseInstantStrict(instantIso, "Timestamp (ISO Instant)");
            long ws = parseLongStrict(wsStr, "Workstation ID");
            int dist = parseIntStrict(distStr, "District");
            int reg = parseIntStrict(regStr, "Region");
            boolean pos = parseBool10(posStr);
            double val = parseDoubleStrict(valStr, "Value");
            PcrTest t = GuiModel.mkTest(code, pid.trim(), ts, ws, dist, reg, pos, val, note == null ? "" : note);
            boolean ok = model.insertTest(t);
            return ok ? "Test inserted: " + code : "Test code already exists or patient missing";
        } catch (Exception e) {
            return "Error insertTest: " + e.getMessage();
        }
    }

    public String findTestByCode(String codeStr) {
        try {
            long code = parseLongStrict(codeStr, "Code");
            Optional<PcrTest> r = model.findTestByCode(code);
            return r.map(this::fmtTest).orElse("Not found: " + code);
        } catch (Exception e) {
            return "Error findTestByCode: " + e.getMessage();
        }
    }

    /** Для таблицы Tests: вернуть 0/1 строку по коду. */
    public List<PcrTest> findTestByCodeAsList(String codeStr) {
        try {
            long code = parseLongStrict(codeStr, "Code");
            Optional<PcrTest> r = model.findTestByCode(code);
            if (r.isPresent()) {
                ArrayList<PcrTest> out = new ArrayList<>(1);
                out.add(r.get());
                return out;
            }
            return java.util.List.of();
        } catch (Exception e) {
            return java.util.List.of();
        }
    }

    public String deleteTestByCode(String codeStr) {
        try {
            long code = parseLongStrict(codeStr, "Code");
            boolean ok = model.deleteTestByCode(code);
            return ok ? "Deleted test: " + code : "Test not found: " + code;
        } catch (Exception e) {
            return "Error deleteTestByCode: " + e.getMessage();
        }
    }

    // ========================= Queries (string/pretty) =========================
    public String testsOfPatientChrono(String pid) {
        try {
            requireNonEmpty(pid, "Patient ID");
            List<PcrTest> list = model.allTestsOfPatientChrono(pid.trim());
            return fmtTests(list);
        } catch (Exception e) {
            return "Error testsOfPatientChrono: " + e.getMessage();
        }
    }

    public String districtInPeriod(String districtStr, String fromIso, String toIso, boolean onlyPos) {
        try {
            int d = parseIntStrict(districtStr, "District");
            LocalDate from = parseDateStrict(fromIso, "From (YYYY-MM-DD)");
            LocalDate toEx = parseDateStrict(toIso, "ToEx (YYYY-MM-DD)");
            List<PcrTest> list = onlyPos ? model.positiveByDistrictInPeriod(d, from, toEx)
                                         : model.allByDistrictInPeriod(d, from, toEx);
            return fmtTests(list);
        } catch (Exception e) {
            return "Error districtInPeriod: " + e.getMessage();
        }
    }

    public String regionInPeriod(String regionStr, String fromIso, String toIso, boolean onlyPos) {
        try {
            int r = parseIntStrict(regionStr, "Region");
            LocalDate from = parseDateStrict(fromIso, "From (YYYY-MM-DD)");
            LocalDate toEx = parseDateStrict(toIso, "ToEx (YYYY-MM-DD)");
            List<PcrTest> list = onlyPos ? model.positiveByRegionInPeriod(r, from, toEx)
                                         : model.allByRegionInPeriod(r, from, toEx);
            return fmtTests(list);
        } catch (Exception e) {
            return "Error regionInPeriod: " + e.getMessage();
        }
    }

    public String globalInPeriod(String fromIso, String toIso, boolean onlyPos) {
        try {
            LocalDate from = parseDateStrict(fromIso, "From (YYYY-MM-DD)");
            LocalDate toEx = parseDateStrict(toIso, "ToEx (YYYY-MM-DD)");
            List<PcrTest> list = onlyPos ? model.positiveInPeriod(from, toEx)
                                         : model.allInPeriod(from, toEx);
            return fmtTests(list);
        } catch (Exception e) {
            return "Error globalInPeriod: " + e.getMessage();
        }
    }

    public String workstationInPeriod(String wsStr, String fromIso, String toIso) {
        try {
            long ws = parseLongStrict(wsStr, "Workstation ID");
            LocalDate from = parseDateStrict(fromIso, "From (YYYY-MM-DD)");
            LocalDate toEx = parseDateStrict(toIso, "ToEx (YYYY-MM-DD)");
            List<PcrTest> list = model.allByWorkstationInPeriod(ws, from, toEx);
            return fmtTests(list);
        } catch (Exception e) {
            return "Error workstationInPeriod: " + e.getMessage();
        }
    }

    // ========================= op2 Find test of patient =========================
    public String findTestOfPatient(String codeStr, String pid) {
        try {
            long code = parseLongStrict(codeStr, "Code");
            requireNonEmpty(pid, "Patient ID");
            Optional<TestWithPatient> r = model.findTestOfPatient(code, pid.trim());
            if (r.isEmpty()) return "Not found: code=" + code + ", pid=" + pid;
            TestWithPatient twp = r.get();
            return fmtTest(twp.test) + " | patient=" + twp.patient.patientId + " "
                    + twp.patient.firstName + " " + twp.patient.lastName + " birth=" + twp.patient.birthDate;
        } catch (Exception e) {
            return "Error findTestOfPatient: " + e.getMessage();
        }
    }
    /** Для таблицы Tests: 0/1 строка по оп2. */
    public List<PcrTest> findTestOfPatientAsList(String codeStr, String pid) {
        try {
            long code = parseLongStrict(codeStr, "Code");
            requireNonEmpty(pid, "Patient ID");
            Optional<TestWithPatient> r = model.findTestOfPatient(code, pid.trim());
            if (r.isPresent()) return java.util.List.of(r.get().test);
            return java.util.List.of();
        } catch (Exception e) {
            return java.util.List.of();
        }
    }

    // ========================= CSV & Service =========================
    public String exportCsv(String dirPath) {
        try {
            requireNonEmpty(dirPath, "Directory");
            model.exportToCsv(Path.of(dirPath.trim()));
            return "Export OK: " + dirPath;
        } catch (Exception e) {
            return "Error exportCsv: " + e.getMessage();
        }
    }

    public String importCsv(String dirPath) {
        try {
            requireNonEmpty(dirPath, "Directory");
            model.importFromCsv(Path.of(dirPath.trim()));
            return "Import OK: " + dirPath;
        } catch (Exception e) {
            return "Error importCsv: " + e.getMessage();
        }
    }

    public String clearAll() {
        try {
            model.clearAll();
            return "Cleared ALL data.";
        } catch (Exception e) {
            return "Error clearAll: " + e.getMessage();
        }
    }

    // ========================= Formatting helpers =========================
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

    // ===== Queries for tables =====
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
}
