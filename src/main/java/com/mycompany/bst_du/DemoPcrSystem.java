package com.mycompany.bst_du;

import com.mycompany.bst_du.model.PcrSystem;
import com.mycompany.bst_du.domain.Patient;
import com.mycompany.bst_du.domain.PcrTest;
import com.mycompany.bst_du.dto.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

public final class DemoPcrSystem {

    private static int passed = 0, failed = 0;

    public static void main(String[] args) throws Exception {
        PcrSystem sys = new PcrSystem();

        // ---------- 0) Seed: пациенты ----------
        assertTrue(sys.op19_insertPatient(new Patient("P001", "Ada",    "Lovelace", LocalDate.of(1815,12,10))), "insert P001");
        assertTrue(sys.op19_insertPatient(new Patient("P002", "Alan",   "Turing",   LocalDate.of(1912,6,23))),  "insert P002");
        assertTrue(sys.op19_insertPatient(new Patient("P003", "Grace",  "Hopper",   LocalDate.of(1906,12,9))),  "insert P003");
        assertEquals(3, sys.countPatients(), "patients count=3");

        // ---------- 1) Seed: тесты ----------
        addTest(sys, 1001, "P001", "2024-01-10T08:00:00Z",  10L,  1,  11, true,  25.5, "note A");
        addTest(sys, 1002, "P001", "2024-01-12T09:00:00Z",  10L,  1,  11, false, 12.3, "note B");
        addTest(sys, 1003, "P002", "2024-01-11T10:00:00Z",  20L,  2,  22, true,  33.3, "note C");
        addTest(sys, 1004, "P002", "2024-01-15T11:00:00Z",  21L,  2,  22, false,  7.1, "note D");
        addTest(sys, 1005, "P003", "2024-01-13T12:00:00Z",  20L,  3,  33, true,  41.7, "note E");
        addTest(sys, 1006, "P003", "2024-01-20T13:00:00Z",  10L,  1,  11, true,  10.0, "note F");

        assertEquals(6, sys.countTests(), "tests count=6");

        // ---------- 2) op2: find test of patient ----------
        Optional<TestWithPatient> t2 = sys.op2_findTestOfPatient(1003, "P002");
        assertTrue(t2.isPresent(), "op2 find 1003/P002");
        assertEquals("P002", t2.get().patient.patientId, "op2 patientId");

        // ---------- 3) op3: all tests of patient (chrono) ----------
        List<PcrTest> p1tests = sys.op3_allTestsOfPatientChrono("P001");
        assertEquals(2, p1tests.size(), "op3 P001 size=2");
        assertTrue(p1tests.get(0).timestamp.isBefore(p1tests.get(1).timestamp), "op3 chrono order");

        // ---------- 4/5: district period ----------
        LocalDate from = LocalDate.of(2024,1,10);
        LocalDate toEx = LocalDate.of(2024,1,21);
        List<PcrTest> d1all = sys.op5_allByDistrictInPeriod(1, from, toEx);
        List<PcrTest> d1pos = sys.op4_positiveByDistrictInPeriod(1, from, toEx);
        assertEquals(3, d1all.size(), "district 1 all=3");
        assertEquals(2, d1pos.size(), "district 1 positive=2");

        // ---------- 6/7: region period ----------
        List<PcrTest> r22all = sys.op7_allByRegionInPeriod(22, from, toEx);
        List<PcrTest> r22pos = sys.op6_positiveByRegionInPeriod(22, from, toEx);
        assertEquals(2, r22all.size(), "region 22 all=2");
        assertEquals(1, r22pos.size(), "region 22 positive=1");

        // ---------- 8/9: global period ----------
        List<PcrTest> gall = sys.op9_allInPeriod(from, toEx);
        List<PcrTest> gpos = sys.op8_positiveInPeriod(from, toEx);
        assertEquals(6, gall.size(), "global all=6");
        assertEquals(4, gpos.size(), "global positive=4");

        // ---------- 10–16: sickness window ----------
        LocalDate at = LocalDate.of(2024,1,15);
        int X = 7;

        // В ОКРУГЕ 1 к 15.01.2024 болен ТОЛЬКО P001 (окно [2024-01-09 .. 2024-01-16))
        List<Patient> sickD1 = sys.op10_sickByDistrictAtDate(1, at, X);
        assertEqSet(idsAVL(sickD1), setAVL("P001"), "op10 sick district1 {P001}");

        List<PatientScore> sickD1Sorted = sys.op11_sickByDistrictAtDateSortedByValue(1, at, X);
        assertEquals(1, sickD1Sorted.size(), "op11 size=1");
        // На всякий случай — проверка убывания только если элементов >= 2
        if (!sickD1Sorted.isEmpty()) {
            for (int i = 1; i < sickD1Sorted.size(); i++) {
                assertTrue(sickD1Sorted.get(i-1).score >= sickD1Sorted.get(i).score, "op11 sorted desc");
            }
        }

        List<Patient> sickR22 = sys.op12_sickByRegionAtDate(22, at, X);
        assertEqSet(idsAVL(sickR22), setAVL("P002"), "op12 sick region22 {P002}");

        List<Patient> sickAll = sys.op13_sickEverywhereAtDate(at, X);
        assertEqSet(idsAVL(sickAll), setAVL("P001","P002","P003"), "op13 sick global {P001,P002,P003}");

        List<PatientScore> onePerDistrict = sys.op14_oneSickPerDistrictMaxValue(at, X);
        assertTrue(onePerDistrict.size() >= 3, "op14 >=3 districts present");

        List<DistrictCount> byDistrict = sys.op15_districtsBySickCount(at, X);
        assertTrue(!byDistrict.isEmpty(), "op15 non-empty");

        List<RegionCount> byRegion = sys.op16_regionsBySickCount(at, X);
        assertTrue(!byRegion.isEmpty(), "op16 non-empty");

        // ---------- 17: workstation in period ----------
        List<PcrTest> ws20 = sys.op17_allByWorkstationInPeriod(20L, from, toEx);
        assertEquals(2, ws20.size(), "op17 ws=20 count=2");

        // ---------- 18/20: find & delete test by code ----------
        assertTrue(sys.op18_findTestByCode(1002).isPresent(), "op18 find 1002");
        assertTrue(sys.op20_deleteTestByCode(1002), "op20 delete 1002");
        assertTrue(sys.op18_findTestByCode(1002).isEmpty(), "op18 not found 1002 after delete");
        assertEquals(5, sys.countTests(), "tests count after delete=5");

        // ---------- 21: delete patient with tests ----------
        assertTrue(sys.op21_deletePatientWithTests("P002"), "op21 delete P002 with tests");
        assertEquals(2, sys.countPatients(), "patients after op21=2");
        assertTrue(sys.op18_findTestByCode(1003).isEmpty(), "1003 gone");
        assertTrue(sys.op18_findTestByCode(1004).isEmpty(), "1004 gone");

        // ---------- CSV export / clear / import ----------
        Path outDir = Paths.get("pcr_demo_out");
        sys.exportToCsv(outDir);

        int pBefore = sys.countPatients();
        int tBefore = sys.countTests();

        sys.clearAll();
        assertEquals(0, sys.countPatients(), "after clear patients=0");
        assertEquals(0, sys.countTests(), "after clear tests=0");

        sys.importFromCsv(outDir);
        assertEquals(pBefore, sys.countPatients(), "after import patients restored");
        assertEquals(tBefore, sys.countTests(), "after import tests restored");

        System.out.println("======================================");
        System.out.printf("PcrSystem smoke tests: passed=%d, failed=%d%n", passed, failed);
        System.out.println("======================================");
        if (failed > 0) System.exit(1);
    }

    // ---------- helpers ----------
    private static void addTest(PcrSystem sys, long code, String pid, String isoTs,
                                long ws, int dist, int reg, boolean pos, double val, String note) {
        boolean ok = sys.op1_insertTest(new PcrTest(code, pid, Instant.parse(isoTs), ws, dist, reg, pos, val, note));
        assertTrue(ok, "insert test " + code);
    }

    private static void assertTrue(boolean cond, String name){
        if (cond) { System.out.println("[PASS] " + name); passed++; }
        else { System.out.println("[FAIL] " + name); failed++; }
    }

    private static void assertEquals(int exp, int act, String name){
        assertTrue(exp == act, name + " (exp=" + exp + ", act=" + act + ")");
    }

    private static void assertEquals(String exp, String act, String name){
        assertTrue(java.util.Objects.equals(exp, act),
                name + " (exp=\"" + exp + "\", act=\"" + act + "\")");
    }

    // ---------- AVL<StringNode> ----------
    private static AVL<StringNode> setAVL(String... a){
        AVL<StringNode> s = new AVL<>();
        for (String x : a) s.insert(new StringNode(x));
        return s;
    }

    private static AVL<StringNode> idsAVL(List<Patient> ps){
        AVL<StringNode> s = new AVL<>();
        for (Patient p : ps) s.insert(new StringNode(p.patientId));
        return s;
    }

    private static void assertEqSet(AVL<StringNode> a, AVL<StringNode> b, String name){
        List<StringNode> la = a.inOrder();
        List<StringNode> lb = b.inOrder();
        boolean eq = la.size() == lb.size();
        if (eq) {
            for (int i = 0; i < la.size(); i++) {
                if (!la.get(i).getValue().equals(lb.get(i).getValue())) { eq = false; break; }
            }
        }
        assertTrue(eq, name + " (got=" + la + ", exp=" + lb + ")");
    }
}
