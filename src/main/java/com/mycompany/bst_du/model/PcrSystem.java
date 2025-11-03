/*
 * SK: PCR systém — indexy na vlastných AVL/BST, CSV I/O bez knižníc.
 */
package com.mycompany.bst_du.model;

import com.mycompany.bst_du.AVL;
import com.mycompany.bst_du.BST;
import com.mycompany.bst_du.StringNode;

import com.mycompany.bst_du.domain.Patient;
import com.mycompany.bst_du.domain.PcrTest;

import com.mycompany.bst_du.dto.DistrictCount;
import com.mycompany.bst_du.dto.PatientScore;
import com.mycompany.bst_du.dto.RegionCount;
import com.mycompany.bst_du.dto.TestWithPatient;

import com.mycompany.bst_du.index.PatientByIdNode;
import com.mycompany.bst_du.index.TestByCodeNode;
import com.mycompany.bst_du.index.TestByDateNode;
import com.mycompany.bst_du.index.TestByDistrictDateNode;
import com.mycompany.bst_du.index.TestByPatientTimeNode;
import com.mycompany.bst_du.index.TestByRegionDateNode;
import com.mycompany.bst_du.index.TestByWorkstationDateNode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * SK: Centrálna doménová logika: drží všetky indexy (AVL) a implementuje 21 operácií.
 */
public final class PcrSystem {

    // === SK: Indexy (AVL) ===
    private final AVL<PatientByIdNode>           idxPatients          = new AVL<>();
    private final AVL<TestByCodeNode>            idxByCode            = new AVL<>();
    private final AVL<TestByPatientTimeNode>     idxByPatientTime     = new AVL<>();
    private final AVL<TestByDistrictDateNode>    idxByDistrictDate    = new AVL<>();
    private final AVL<TestByRegionDateNode>      idxByRegionDate      = new AVL<>();
    private final AVL<TestByDateNode>            idxByDate            = new AVL<>();
    private final AVL<TestByWorkstationDateNode> idxByWorkstationDate = new AVL<>();

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE    = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter INSTANT = DateTimeFormatter.ISO_INSTANT;

    // === SK: Pomocné prepočty dátumov (YYYYMMDD) ===
    private static int ymd(LocalDate d) {
        return d.getYear() * 10000 + d.getMonthValue() * 100 + d.getDayOfMonth();
    }
    private static int ymd(Instant t) {
        LocalDate ld = LocalDateTime.ofInstant(t, ZONE).toLocalDate();
        return ymd(ld);
    }

    // === SK: Štatistika / servis ===
    public int countPatients() { return idxPatients.size(); }
    public int countTests()    { return idxByCode.size(); }

    public void clearAll() {
        idxPatients.clear();
        idxByCode.clear();
        idxByPatientTime.clear();
        idxByDistrictDate.clear();
        idxByRegionDate.clear();
        idxByDate.clear();
        idxByWorkstationDate.clear();
    }

    // === SK: Základné CRUD via indexy ===
    public boolean addPatient(Patient p) {
        if (p == null) return false;
        if (idxPatients.find(new PatientByIdNode(p.patientId, null)) != null) return false;
        return idxPatients.insert(new PatientByIdNode(p.patientId, p));
    }

    public boolean removePatient(String patientId) {
        if (patientId == null || patientId.isEmpty()) return false;
        PatientByIdNode cur = idxPatients.find(new PatientByIdNode(patientId, null));
        if (cur == null) return false;

        // SK: Zmaž všetky testy pacienta zo všetkých indexov
        TestByPatientTimeNode lo = new TestByPatientTimeNode(patientId, Long.MIN_VALUE, Long.MIN_VALUE, null);
        TestByPatientTimeNode hi = new TestByPatientTimeNode(patientId, Long.MAX_VALUE, Long.MAX_VALUE, null);
        List<TestByPatientTimeNode> nodes = idxByPatientTime.range(lo, true, hi, true);
        for (TestByPatientTimeNode n : nodes) removeTestByRef(n.ref);

        return idxPatients.delete(cur);
    }

    public boolean addTest(PcrTest t) {
        if (t == null) return false;
        if (idxPatients.find(new PatientByIdNode(t.patientId, null)) == null) return false;
        if (idxByCode.find(new TestByCodeNode(t.testCode, null)) != null) return false;

        int y = ymd(t.timestamp);
        boolean ok = true;
        ok &= idxByCode.insert(new TestByCodeNode(t.testCode, t));
        ok &= idxByPatientTime.insert(new TestByPatientTimeNode(t.patientId, t.timestamp.toEpochMilli(), t.testCode, t));
        ok &= idxByDistrictDate.insert(new TestByDistrictDateNode(t.district, y, t.testCode, t));
        ok &= idxByRegionDate.insert(new TestByRegionDateNode(t.region, y, t.testCode, t));
        ok &= idxByDate.insert(new TestByDateNode(y, t.testCode, t));
        ok &= idxByWorkstationDate.insert(new TestByWorkstationDateNode(t.workstationId, y, t.testCode, t));
        if (!ok) { // SK: jednoduchý rollback pri zlyhaní
            idxByCode.delete(new TestByCodeNode(t.testCode, null));
            idxByPatientTime.delete(new TestByPatientTimeNode(t.patientId, t.timestamp.toEpochMilli(), t.testCode, null));
            idxByDistrictDate.delete(new TestByDistrictDateNode(t.district, y, t.testCode, null));
            idxByRegionDate.delete(new TestByRegionDateNode(t.region, y, t.testCode, null));
            idxByDate.delete(new TestByDateNode(y, t.testCode, null));
            idxByWorkstationDate.delete(new TestByWorkstationDateNode(t.workstationId, y, t.testCode, null));
            return false;
        }
        return true;
    }

    public boolean removeTest(long testCode) {
        TestByCodeNode n = idxByCode.find(new TestByCodeNode(testCode, null));
        if (n == null) return false;
        return removeTestByRef(n.ref);
    }

    private boolean removeTestByRef(PcrTest t) {
        if (t == null) return false;
        int y = ymd(t.timestamp);
        boolean ok = true;
        ok &= idxByCode.delete(new TestByCodeNode(t.testCode, null));
        ok &= idxByPatientTime.delete(new TestByPatientTimeNode(t.patientId, t.timestamp.toEpochMilli(), t.testCode, null));
        ok &= idxByDistrictDate.delete(new TestByDistrictDateNode(t.district, y, t.testCode, null));
        ok &= idxByRegionDate.delete(new TestByRegionDateNode(t.region, y, t.testCode, null));
        ok &= idxByDate.delete(new TestByDateNode(y, t.testCode, null));
        ok &= idxByWorkstationDate.delete(new TestByWorkstationDateNode(t.workstationId, y, t.testCode, null));
        return ok;
    }

    // === SK: 21 operácií ===

    public boolean op1_insertTest(PcrTest t) { return addTest(t); }

    public Optional<TestWithPatient> op2_findTestOfPatient(long testCode, String patientId) {
        TestByCodeNode tn = idxByCode.find(new TestByCodeNode(testCode, null));
        if (tn == null) return Optional.empty();
        PcrTest t = tn.ref;
        if (!t.patientId.equals(patientId)) return Optional.empty();
        PatientByIdNode pn = idxPatients.find(new PatientByIdNode(patientId, null));
        if (pn == null) return Optional.empty();
        return Optional.of(new TestWithPatient(t, pn.ref));
    }

    public List<PcrTest> op3_allTestsOfPatientChrono(String patientId) {
        TestByPatientTimeNode lo = new TestByPatientTimeNode(patientId, Long.MIN_VALUE, Long.MIN_VALUE, null);
        TestByPatientTimeNode hi = new TestByPatientTimeNode(patientId, Long.MAX_VALUE, Long.MAX_VALUE, null);
        List<TestByPatientTimeNode> nodes = idxByPatientTime.range(lo, true, hi, true);
        ArrayList<PcrTest> out = new ArrayList<>(nodes.size());
        for (TestByPatientTimeNode n : nodes) out.add(n.ref);
        return out;
    }

    public List<PcrTest> op4_positiveByDistrictInPeriod(int district, LocalDate from, LocalDate toExclusive) {
        return filterPositive(rangeDistrict(district, from, toExclusive));
    }

    public List<PcrTest> op5_allByDistrictInPeriod(int district, LocalDate from, LocalDate toExclusive) {
        return rangeDistrict(district, from, toExclusive);
    }

    public List<PcrTest> op6_positiveByRegionInPeriod(int region, LocalDate from, LocalDate toExclusive) {
        return filterPositive(rangeRegion(region, from, toExclusive));
    }

    public List<PcrTest> op7_allByRegionInPeriod(int region, LocalDate from, LocalDate toExclusive) {
        return rangeRegion(region, from, toExclusive);
    }

    public List<PcrTest> op8_positiveInPeriod(LocalDate from, LocalDate toExclusive) {
        return filterPositive(rangeGlobal(from, toExclusive));
    }

    public List<PcrTest> op9_allInPeriod(LocalDate from, LocalDate toExclusive) {
        return rangeGlobal(from, toExclusive);
    }

    public List<Patient> op10_sickByDistrictAtDate(int district, LocalDate atDate, int daysWindow) {
        return new ArrayList<>(computeSickSet(rangeDistrictWindow(district, atDate, daysWindow)));
    }

    public List<PatientScore> op11_sickByDistrictAtDateSortedByValue(int district, LocalDate atDate, int daysWindow) {
        return sortScoresDesc(computeSickScoresWithDistrict(rangeDistrictWindow(district, atDate, daysWindow), district));
    }

    public List<Patient> op12_sickByRegionAtDate(int region, LocalDate atDate, int daysWindow) {
        return new ArrayList<>(computeSickSet(rangeRegionWindow(region, atDate, daysWindow)));
    }

    public List<Patient> op13_sickEverywhereAtDate(LocalDate atDate, int daysWindow) {
        return new ArrayList<>(computeSickSet(rangeGlobalWindow(atDate, daysWindow)));
    }

    public List<PatientScore> op14_oneSickPerDistrictMaxValue(LocalDate atDate, int daysWindow) {
        List<PcrTest> window = rangeGlobalWindow(atDate, daysWindow);
        class Best { int district; String pid; double val; }
        ArrayList<Best> acc = new ArrayList<>();
        for (PcrTest t : window) if (t.positive) {
            Best b = null;
            for (Best x : acc) if (x.district == t.district) { b = x; break; }
            if (b == null) { b = new Best(); b.district = t.district; b.pid = t.patientId; b.val = t.value; acc.add(b); }
            else if (t.value > b.val) { b.pid = t.patientId; b.val = t.value; }
        }
        ArrayList<PatientScore> out = new ArrayList<>(acc.size());
        for (Best b : acc) {
            Patient p = getPatient(b.pid);
            if (p != null) out.add(new PatientScore(p, b.val, b.district));
        }
        out.sort(Comparator.comparingInt(ps -> ps.district));
        return out;
    }

    public List<DistrictCount> op15_districtsBySickCount(LocalDate atDate, int daysWindow) {
        List<PcrTest> window = rangeGlobalWindow(atDate, daysWindow);
        class DC { int district; AVL<StringNode> pids = new AVL<>(); }
        ArrayList<DC> acc = new ArrayList<>();
        for (PcrTest t : window) if (t.positive) {
            DC dc = null;
            for (DC x : acc) if (x.district == t.district) { dc = x; break; }
            if (dc == null) { dc = new DC(); dc.district = t.district; acc.add(dc); }
            dc.pids.insert(new StringNode(t.patientId));
        }
        ArrayList<DistrictCount> out = new ArrayList<>(acc.size());
        for (DC dc : acc) out.add(new DistrictCount(dc.district, dc.pids.size()));
        out.sort((a, b) -> Integer.compare(b.count, a.count));
        return out;
    }

    public List<RegionCount> op16_regionsBySickCount(LocalDate atDate, int daysWindow) {
        List<PcrTest> window = rangeGlobalWindow(atDate, daysWindow);
        class RC { int region; AVL<StringNode> pids = new AVL<>(); }
        ArrayList<RC> acc = new ArrayList<>();
        for (PcrTest t : window) if (t.positive) {
            RC rc = null;
            for (RC x : acc) if (x.region == t.region) { rc = x; break; }
            if (rc == null) { rc = new RC(); rc.region = t.region; acc.add(rc); }
            rc.pids.insert(new StringNode(t.patientId));
        }
        ArrayList<RegionCount> out = new ArrayList<>(acc.size());
        for (RC rc : acc) out.add(new RegionCount(rc.region, rc.pids.size()));
        out.sort((a, b) -> Integer.compare(b.count, a.count));
        return out;
    }

    public List<PcrTest> op17_allByWorkstationInPeriod(long workstationId, LocalDate from, LocalDate toExclusive) {
        int y1 = ymd(from), y2 = ymd(toExclusive);
        TestByWorkstationDateNode lo = new TestByWorkstationDateNode(workstationId, y1, Long.MIN_VALUE, null);
        TestByWorkstationDateNode hi = new TestByWorkstationDateNode(workstationId, y2, Long.MIN_VALUE, null);
        List<TestByWorkstationDateNode> nodes = idxByWorkstationDate.range(lo, true, hi, false);
        ArrayList<PcrTest> out = new ArrayList<>(nodes.size());
        for (TestByWorkstationDateNode n : nodes) out.add(n.ref);
        return out;
    }

    public Optional<PcrTest> op18_findTestByCode(long testCode) {
        TestByCodeNode n = idxByCode.find(new TestByCodeNode(testCode, null));
        return (n == null) ? Optional.empty() : Optional.of(n.ref);
    }

    public boolean op19_insertPatient(Patient p) { return addPatient(p); }

    public boolean op20_deleteTestByCode(long testCode) { return removeTest(testCode); }

    public boolean op21_deletePatientWithTests(String patientId) { return removePatient(patientId); }

    // === SK: CSV (bez knižníc), využíva naše in-order/range ===

    public void exportToCsv(Path dir) throws IOException {
        if (dir == null) throw new IllegalArgumentException("dir required");
        Files.createDirectories(dir);

        Path pFile = dir.resolve("patients.csv");
        try (BufferedWriter w = Files.newBufferedWriter(pFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            List<PatientByIdNode> nodes = idxPatients.inOrder();
            for (PatientByIdNode n : nodes) {
                Patient p = n.ref;
                w.write(BST.csvEsc(p.patientId)); w.write(';');
                w.write(BST.csvEsc(p.firstName)); w.write(';');
                w.write(BST.csvEsc(p.lastName));  w.write(';');
                w.write(DATE.format(p.birthDate));
                w.write('\n');
            }
        }

        Path tFile = dir.resolve("tests.csv");
        try (BufferedWriter w = Files.newBufferedWriter(tFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            List<TestByDateNode> nodes = idxByDate.inOrder();
            for (TestByDateNode n : nodes) {
                PcrTest t = n.ref;
                w.write(Long.toString(t.testCode));       w.write(';');
                w.write(BST.csvEsc(t.patientId));         w.write(';');
                w.write(INSTANT.format(t.timestamp));     w.write(';');
                w.write(Long.toString(t.workstationId));  w.write(';');
                w.write(Integer.toString(t.district));    w.write(';');
                w.write(Integer.toString(t.region));      w.write(';');
                w.write(t.positive ? "1" : "0");          w.write(';');
                w.write(Double.toString(t.value));        w.write(';');
                w.write(BST.csvEsc(t.note));
                w.write('\n');
            }
        }
    }

    public void importFromCsv(Path dir) throws IOException {
        if (dir == null) throw new IllegalArgumentException("dir required");
        clearAll();

        Path pFile = dir.resolve("patients.csv");
        if (Files.exists(pFile)) {
            try (BufferedReader r = Files.newBufferedReader(pFile, StandardCharsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.isEmpty()) continue;
                    String[] a = BST.csvSplit(line);
                    if (a.length < 4) continue;
                    Patient p = new Patient(
                            BST.csvUnesc(a[0]),
                            BST.csvUnesc(a[1]),
                            BST.csvUnesc(a[2]),
                            LocalDate.parse(a[3], DATE)
                    );
                    addPatient(p);
                }
            }
        }

        Path tFile = dir.resolve("tests.csv");
        if (Files.exists(tFile)) {
            try (BufferedReader r = Files.newBufferedReader(tFile, StandardCharsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.isEmpty()) continue;
                    String[] a = BST.csvSplit(line);
                    if (a.length < 9) continue;
                    long   code  = Long.parseLong(a[0]);
                    String pid   = BST.csvUnesc(a[1]);
                    Instant ts   = Instant.parse(a[2]);
                    long   ws    = Long.parseLong(a[3]);
                    int    dist  = Integer.parseInt(a[4]);
                    int    reg   = Integer.parseInt(a[5]);
                    boolean pos  = "1".equals(a[6]) || "true".equalsIgnoreCase(a[6]);
                    double val   = Double.parseDouble(a[7]);
                    String note  = BST.csvUnesc(a[8]);
                    addTest(new PcrTest(code, pid, ts, ws, dist, reg, pos, val, note));
                }
            }
        }
    }

    // === SK: Pomocné rozsahy/filtre/vyhľadávania ===

    private List<PcrTest> rangeDistrict(int district, LocalDate from, LocalDate toExclusive) {
        int y1 = ymd(from), y2 = ymd(toExclusive);
        TestByDistrictDateNode lo = new TestByDistrictDateNode(district, y1, Long.MIN_VALUE, null);
        TestByDistrictDateNode hi = new TestByDistrictDateNode(district, y2, Long.MIN_VALUE, null);
        List<TestByDistrictDateNode> nodes = idxByDistrictDate.range(lo, true, hi, false);
        ArrayList<PcrTest> out = new ArrayList<>(nodes.size());
        for (TestByDistrictDateNode n : nodes) out.add(n.ref);
        return out;
    }

    private List<PcrTest> rangeRegion(int region, LocalDate from, LocalDate toExclusive) {
        int y1 = ymd(from), y2 = ymd(toExclusive);
        TestByRegionDateNode lo = new TestByRegionDateNode(region, y1, Long.MIN_VALUE, null);
        TestByRegionDateNode hi = new TestByRegionDateNode(region, y2, Long.MIN_VALUE, null);
        List<TestByRegionDateNode> nodes = idxByRegionDate.range(lo, true, hi, false);
        ArrayList<PcrTest> out = new ArrayList<>(nodes.size());
        for (TestByRegionDateNode n : nodes) out.add(n.ref);
        return out;
    }

    private List<PcrTest> rangeGlobal(LocalDate from, LocalDate toExclusive) {
        int y1 = ymd(from), y2 = ymd(toExclusive);
        TestByDateNode lo = new TestByDateNode(y1, Long.MIN_VALUE, null);
        TestByDateNode hi = new TestByDateNode(y2, Long.MIN_VALUE, null);
        List<TestByDateNode> nodes = idxByDate.range(lo, true, hi, false);
        ArrayList<PcrTest> out = new ArrayList<>(nodes.size());
        for (TestByDateNode n : nodes) out.add(n.ref);
        return out;
    }

    private List<PcrTest> rangeDistrictWindow(int district, LocalDate atDate, int daysWindow) {
        LocalDate from = atDate.minusDays(Math.max(0, daysWindow - 1));
        LocalDate toEx = atDate.plusDays(1);
        return rangeDistrict(district, from, toEx);
    }

    private List<PcrTest> rangeRegionWindow(int region, LocalDate atDate, int daysWindow) {
        LocalDate from = atDate.minusDays(Math.max(0, daysWindow - 1));
        LocalDate toEx = atDate.plusDays(1);
        return rangeRegion(region, from, toEx);
    }

    private List<PcrTest> rangeGlobalWindow(LocalDate atDate, int daysWindow) {
        LocalDate from = atDate.minusDays(Math.max(0, daysWindow - 1));
        LocalDate toEx = atDate.plusDays(1);
        return rangeGlobal(from, toEx);
    }

    private static List<PcrTest> filterPositive(List<PcrTest> src) {
        ArrayList<PcrTest> out = new ArrayList<>(src.size());
        for (PcrTest t : src) if (t.positive) out.add(t);
        return out;
    }

    private Patient getPatient(String patientId) {
        PatientByIdNode n = idxPatients.find(new PatientByIdNode(patientId, null));
        return (n == null) ? null : n.ref;
    }

    private ArrayList<Patient> computeSickSet(List<PcrTest> window) {
        AVL<StringNode> uniq = new AVL<>();
        for (PcrTest t : window) if (t.positive) uniq.insert(new StringNode(t.patientId));
        ArrayList<Patient> out = new ArrayList<>();
        List<StringNode> ids = uniq.inOrder();
        for (StringNode n : ids) {
            Patient p = getPatient(n.getValue());
            if (p != null) out.add(p);
        }
        return out;
    }

    private ArrayList<PatientScore> computeSickScoresWithDistrict(List<PcrTest> window, int district) {
        AVL<StringNode> uniq = new AVL<>();
        for (PcrTest t : window) if (t.positive) uniq.insert(new StringNode(t.patientId));

        ArrayList<PatientScore> out = new ArrayList<>();
        for (StringNode id : uniq.inOrder()) {
            String pid = id.getValue();
            double maxVal = Double.NEGATIVE_INFINITY;
            for (PcrTest t : window) {
                if (t.positive && pid.equals(t.patientId)) {
                    if (t.value > maxVal) maxVal = t.value;
                }
            }
            if (maxVal > Double.NEGATIVE_INFINITY) {
                Patient p = getPatient(pid);
                if (p != null) out.add(new PatientScore(p, maxVal, district)); // SK: okres sa zachová
            }
        }
        return out;
    }

    private static List<PatientScore> sortScoresDesc(ArrayList<PatientScore> list) {
        list.sort((a, b) -> Double.compare(b.score, a.score));
        return list;
    }

    // === SK: Verejné čítacie helpery (pre GUI) ===
    public List<String> getAllPatientIds() {
        List<PatientByIdNode> nodes = idxPatients.inOrder();
        ArrayList<String> out = new ArrayList<>(nodes.size());
        for (PatientByIdNode n : nodes) out.add(n.ref.patientId);
        return out;
    }

    public Patient findPatientById(String patientId) {
        PatientByIdNode n = idxPatients.find(new PatientByIdNode(patientId, null));
        return (n == null) ? null : n.ref;
    }
    
    // === SK: Generovanie náhodného unikátneho kódu testu ===
    private static final long CODE_MIN = 100_000_000L;     // SK: 9-miestny rozsah (možno zmeniť)
    private static final long CODE_MAX = 999_999_999L;     // SK: vrátane

    private long randomCode() {
        return java.util.concurrent.ThreadLocalRandom.current().nextLong(CODE_MIN, CODE_MAX + 1);
    }

    public long genUniqueTestCode() {
        long code;
        int attempts = 0;
        do {
            code = randomCode();
            attempts++;
            if (attempts > 1_000_000)
                throw new IllegalStateException("Unable to generate unique test code (too many collisions)");
        } while (idxByCode.find(new TestByCodeNode(code, null)) != null);
        return code;
    }

    /** SK: Op1-random — vytvorí test s auto-generovaným unikátnym kódom a hneď ho vloží. */
    public PcrTest op1_insertTestRandom(String patientId, Instant timestamp,
                                        long workstationId, int district, int region,
                                        boolean positive, double value, String note) {
        long code = genUniqueTestCode();
        PcrTest t = new PcrTest(code, patientId, timestamp, workstationId, district, region, positive, value, note);
        boolean ok = op1_insertTest(t);
        if (!ok) throw new IllegalStateException("Insert failed (patient missing or duplicate code)");
        return t;
    }
}
