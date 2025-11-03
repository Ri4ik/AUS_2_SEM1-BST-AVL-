package com.mycompany.bst_du.gui.view;

import com.mycompany.bst_du.gui.controller.GuiController;
import com.mycompany.bst_du.domain.PcrTest;
import com.mycompany.bst_du.domain.Patient;
import com.mycompany.bst_du.dto.*;

import com.mycompany.bst_du.gui.view.table.TestTableModel;
import com.mycompany.bst_du.gui.view.table.PatientTableModel;
import com.mycompany.bst_du.gui.view.table.PatientScoreTableModel;
import com.mycompany.bst_du.gui.view.table.CountTableModel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.prefs.Preferences;

public final class MainFrame extends JFrame {
    private final GuiController ctl;

    private final JTextArea log = new JTextArea(12, 80);

    // Результаты в табах
    private final JTabbedPane resultTabs = new JTabbedPane();
    private final JTable testsTable = new JTable(new TestTableModel());
    private final JTable peopleTable = new JTable(new PatientTableModel()); // переключаемые модели

    public MainFrame(GuiController ctl) {
        super("PCR Demo System — MVC GUI");
        this.ctl = ctl;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Вкладки ввода
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Demo Data", buildDemoPanel());
        tabs.add("Patients", buildPatientsPanel());
        tabs.add("Tests", buildTestsPanel());
        tabs.add("Queries", buildQueriesPanel());
        tabs.add("Sickness (ops 10–16)", buildSicknessPanel()); 
        tabs.add("CSV / Service", buildCsvPanel());

        // Результаты
        resultTabs.add("Tests", new JScrollPane(testsTable));
        resultTabs.add("People/Counts", new JScrollPane(peopleTable));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, buildBottomPanel());
        split.setResizeWeight(0.6);

        add(split, BorderLayout.CENTER);

        pack();
        restoreWindowBounds();
        setLocationRelativeTo(null);
    }

    // ====== Patients ======
    private JPanel buildPatientsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = gbc();

        JTextField tfId = new JTextField(10);
        JTextField tfFn = new JTextField(10);
        JTextField tfLn = new JTextField(10);
        JTextField tfBirth = new JTextField(10);

        JButton bIns = new JButton("Insert Patient");
        bIns.addActionListener(e -> {
            String msg = ctl.insertPatient(tfId.getText().trim(), tfFn.getText().trim(),
                    tfLn.getText().trim(), tfBirth.getText().trim());
            appendLog(msg);
            appendCounts();
        });

        JTextField tfDel = new JTextField(10);
        JButton bDel = new JButton("Delete Patient + Tests");
        bDel.addActionListener(e -> {
            String msg = ctl.deletePatient(tfDel.getText().trim());
            appendLog(msg);
            appendCounts();
        });

        int r=0;
        addRow(p,c,r++, "Patient ID:", tfId);
        addRow(p,c,r++, "First name:", tfFn);
        addRow(p,c,r++, "Last name:", tfLn);
        addRow(p,c,r++, "Birth (YYYY-MM-DD):", tfBirth);
        addBtn(p,c,r++, bIns);
        addRow(p,c,r++, "Delete Patient ID:", tfDel);
        addBtn(p,c,r++, bDel);

        return p;
    }

    // ====== Tests ======
    private JPanel buildTestsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = gbc();

        JTextField tCode = new JTextField(8);
        JTextField tPid  = new JTextField(8);
        JTextField tTs   = new JTextField(22);
        JTextField tWs   = new JTextField(8);
        JTextField tDist = new JTextField(6);
        JTextField tReg  = new JTextField(6);
        JTextField tPos  = new JTextField(4);
        JTextField tVal  = new JTextField(6);
        JTextField tNote = new JTextField(20);

        JButton bIns = new JButton("Insert Test");
        bIns.addActionListener(e -> {
            String msg = ctl.insertTest(tCode.getText().trim(), tPid.getText().trim(),
                    tTs.getText().trim(), tWs.getText().trim(), tDist.getText().trim(),
                    tReg.getText().trim(), tPos.getText().trim(), tVal.getText().trim(),
                    tNote.getText());
            appendLog(msg);
            appendCounts();
        });

        JTextField fCode = new JTextField(8);
        JButton bFind = new JButton("Find Test by Code → Tests table");
        bFind.addActionListener(e -> {
            List<PcrTest> list = ctl.findTestByCodeAsList(fCode.getText().trim());
            showTests(list);
            appendLog(ctl.findTestByCode(fCode.getText().trim()));
        });

        JTextField dCode = new JTextField(8);
        JButton bDel = new JButton("Delete Test by Code");
        bDel.addActionListener(e -> {
            String msg = ctl.deleteTestByCode(dCode.getText().trim());
            appendLog(msg);
            appendCounts();
        });

        int r=0;
        addRow(p,c,r++, "Code:", tCode);
        addRow(p,c,r++, "Patient ID:", tPid);
        addRow(p,c,r++, "Timestamp (ISO Instant):", tTs);
        addRow(p,c,r++, "Workstation ID:", tWs);
        addRow(p,c,r++, "District:", tDist);
        addRow(p,c,r++, "Region:", tReg);
        addRow(p,c,r++, "Positive (1/0/true/false):", tPos);
        addRow(p,c,r++, "Value:", tVal);
        addRow(p,c,r++, "Note:", tNote);
        addBtn(p,c,r++, bIns);
        addRow(p,c,r++, "Find code:", fCode); addBtn(p,c,r++, bFind);
        addRow(p,c,r++, "Delete code:", dCode); addBtn(p,c,r++, bDel);
        return p;
    }

    // ====== Queries ======
    private JPanel buildQueriesPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = gbc();

        // --- op3: tests of patient chrono ---
        JTextField pid = new JTextField(8);
        JButton bPid = new JButton("Tests of Patient (chrono) → Tests table");
        bPid.addActionListener(e -> {
            List<PcrTest> list = com.mycompany.bst_du.gui.UtilQueries.testsOfPatient(ctl, pid.getText().trim());
            showTests(list);
        });

        // --- common dates ---
        JTextField qFrom = new JTextField(10);
        JTextField qToEx = new JTextField(10);

        // --- district ---
        JTextField dist = new JTextField(6);
        JButton bDistAll = new JButton("District ALL → Tests table");
        bDistAll.addActionListener(e -> showTests(
                com.mycompany.bst_du.gui.UtilQueries.district(ctl, dist.getText().trim(), qFrom.getText().trim(), qToEx.getText().trim(), false)
        ));
        JButton bDistPos = new JButton("District POS → Tests table");
        bDistPos.addActionListener(e -> showTests(
                com.mycompany.bst_du.gui.UtilQueries.district(ctl, dist.getText().trim(), qFrom.getText().trim(), qToEx.getText().trim(), true)
        ));

        // --- region ---
        JTextField reg = new JTextField(6);
        JButton bRegAll = new JButton("Region ALL → Tests table");
        bRegAll.addActionListener(e -> showTests(
                com.mycompany.bst_du.gui.UtilQueries.region(ctl, reg.getText().trim(), qFrom.getText().trim(), qToEx.getText().trim(), false)
        ));
        JButton bRegPos = new JButton("Region POS → Tests table");
        bRegPos.addActionListener(e -> showTests(
                com.mycompany.bst_du.gui.UtilQueries.region(ctl, reg.getText().trim(), qFrom.getText().trim(), qToEx.getText().trim(), true)
        ));

        // --- global ---
        JButton bGAll = new JButton("Global ALL → Tests table");
        bGAll.addActionListener(e -> showTests(
                com.mycompany.bst_du.gui.UtilQueries.global(ctl, qFrom.getText().trim(), qToEx.getText().trim(), false)
        ));
        JButton bGPos = new JButton("Global POS → Tests table");
        bGPos.addActionListener(e -> showTests(
                com.mycompany.bst_du.gui.UtilQueries.global(ctl, qFrom.getText().trim(), qToEx.getText().trim(), true)
        ));

        // --- workstation ---
        JTextField ws = new JTextField(8);
        JButton bWs = new JButton("Workstation ALL → Tests table");
        bWs.addActionListener(e -> showTests(
                com.mycompany.bst_du.gui.UtilQueries.workstation(ctl, ws.getText().trim(), qFrom.getText().trim(), qToEx.getText().trim())
        ));

        // --- op2: Find test of patient (code+pid) ---
        JTextField f2Code = new JTextField(8);
        JTextField f2Pid  = new JTextField(8);
        JButton bFindOp2 = new JButton("Find test of patient (op2) → Tests table");
        bFindOp2.addActionListener(e -> {
            List<PcrTest> list = ctl.findTestOfPatientAsList(f2Code.getText().trim(), f2Pid.getText().trim());
            showTests(list);
            appendLog(ctl.findTestOfPatient(f2Code.getText().trim(), f2Pid.getText().trim()));
        });

        int r=0;
        addRow(p,c,r++, "Patient ID (op3):", pid); addBtnInline(p,c,r-1, bPid);
        addRow(p,c,r++, "From (YYYY-MM-DD):", qFrom);
        addRow(p,c,r++, "ToEx (YYYY-MM-DD):", qToEx);
        addRow(p,c,r++, "District:", dist);
        addBtnInline(p,c,r-1, bDistAll); addBtnInline(p,c,r-1, bDistPos);
        addRow(p,c,r++, "Region:", reg);
        addBtnInline(p,c,r-1, bRegAll); addBtnInline(p,c,r-1, bRegPos);
        addBtn(p,c,r++, bGAll); addBtn(p,c,r++, bGPos);
        addRow(p,c,r++, "Workstation ID:", ws); addBtn(p,c,r++, bWs);

        addRow(p,c,r++, "op2 Code:", f2Code);
        addRow(p,c,r++, "op2 Patient ID:", f2Pid);
        addBtn(p,c,r++, bFindOp2);

        return p;
    }

    // ====== Sickness (ops 10–16) ======
    private JPanel buildSicknessPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = gbc();

        JTextField at = new JTextField(10);
        JTextField x  = new JTextField(5);
        JTextField dist = new JTextField(6);
        JTextField reg  = new JTextField(6);

        JButton b10 = new JButton("10) Sick by District → People");
        b10.addActionListener(e -> {
            try {
                List<Patient> list = ctl.sickByDistrictAtDate(Integer.parseInt(dist.getText().trim()), LocalDate.parse(at.getText().trim()), Integer.parseInt(x.getText().trim()));
                showPatients(list);
            } catch (Exception ex){ appendLog("Error op10: " + ex.getMessage()); }
        });

        JButton b11 = new JButton("11) Sick by District (sorted by value) → People");
        b11.addActionListener(e -> {
            try {
                List<PatientScore> list = ctl.sickByDistrictAtDateSortedByValue(Integer.parseInt(dist.getText().trim()), LocalDate.parse(at.getText().trim()), Integer.parseInt(x.getText().trim()));
                showScores(list);
            } catch (Exception ex){ appendLog("Error op11: " + ex.getMessage()); }
        });

        JButton b12 = new JButton("12) Sick by Region → People");
        b12.addActionListener(e -> {
            try {
                List<Patient> list = ctl.sickByRegionAtDate(Integer.parseInt(reg.getText().trim()), LocalDate.parse(at.getText().trim()), Integer.parseInt(x.getText().trim()));
                showPatients(list);
            } catch (Exception ex){ appendLog("Error op12: " + ex.getMessage()); }
        });

        JButton b13 = new JButton("13) Sick Everywhere → People");
        b13.addActionListener(e -> {
            try {
                List<Patient> list = ctl.sickEverywhereAtDate(LocalDate.parse(at.getText().trim()), Integer.parseInt(x.getText().trim()));
                showPatients(list);
            } catch (Exception ex){ appendLog("Error op13: " + ex.getMessage()); }
        });

        JButton b14 = new JButton("14) One Sick per District (max value) → People");
        b14.addActionListener(e -> {
            try {
                List<PatientScore> list = ctl.oneSickPerDistrictMaxValue(LocalDate.parse(at.getText().trim()), Integer.parseInt(x.getText().trim()));
                showScores(list);
            } catch (Exception ex){ appendLog("Error op14: " + ex.getMessage()); }
        });

        JButton b15 = new JButton("15) Districts by Sick Count → People");
        b15.addActionListener(e -> {
            try {
                List<DistrictCount> list = ctl.districtsBySickCount(LocalDate.parse(at.getText().trim()), Integer.parseInt(x.getText().trim()));
                showDistrictCounts(list);
            } catch (Exception ex){ appendLog("Error op15: " + ex.getMessage()); }
        });

        JButton b16 = new JButton("16) Regions by Sick Count → People");
        b16.addActionListener(e -> {
            try {
                List<RegionCount> list = ctl.regionsBySickCount(LocalDate.parse(at.getText().trim()), Integer.parseInt(x.getText().trim()));
                showRegionCounts(list);
            } catch (Exception ex){ appendLog("Error op16: " + ex.getMessage()); }
        });

        int r=0;
        addRow(p,c,r++, "At (YYYY-MM-DD):", at);
        addRow(p,c,r++, "Window X (days):", x);
        addRow(p,c,r++, "District:", dist);
        addRow(p,c,r++, "Region:", reg);
        addBtn(p,c,r++, b10);
        addBtn(p,c,r++, b11);
        addBtn(p,c,r++, b12);
        addBtn(p,c,r++, b13);
        addBtn(p,c,r++, b14);
        addBtn(p,c,r++, b15);
        addBtn(p,c,r++, b16);

        return p;
    }

    // ====== CSV / Service ======
    private JPanel buildCsvPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = gbc();

        JTextField dir = new JTextField(20);
        JButton bExp = new JButton("Export CSV");
        bExp.addActionListener(e -> appendLog(ctl.exportCsv(dir.getText().trim())));

        JButton bImp = new JButton("Import CSV");
        bImp.addActionListener(e -> {
            appendLog(ctl.importCsv(dir.getText().trim()));
            appendCounts();
        });

        JButton bClear = new JButton("Clear ALL");
        bClear.addActionListener(e -> {
            appendLog(ctl.clearAll());
            ((TestTableModel) testsTable.getModel()).setData(java.util.List.of());
            PatientTableModel m = new PatientTableModel();
            m.setData(java.util.List.of());
            peopleTable.setModel(m);
            appendCounts();
        });

        int r=0;
        addRow(p,c,r++, "Directory:", dir);
        addBtn(p,c,r++, bExp);
        addBtn(p,c,r++, bImp);
        addBtn(p,c,r++, bClear);

        return p;
    }

    // ====== Bottom (results + log + counts) ======
    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new BorderLayout());
        log.setEditable(false);
        JScrollPane spLog = new JScrollPane(log);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultTabs, spLog);
        split.setResizeWeight(0.7);

        JButton bCounts = new JButton("Refresh counts");
        bCounts.addActionListener(e -> appendCounts());

        p.add(split, BorderLayout.CENTER);
        p.add(bCounts, BorderLayout.EAST);
        return p;
    }

    // ====== Show helpers ======
    private void showTests(List<PcrTest> list){
        ((TestTableModel) testsTable.getModel()).setData(list);
        resultTabs.setSelectedIndex(0);
        appendLog("Displayed tests: " + list.size());
    }
    private void showPatients(List<Patient> list){
        PatientTableModel m = new PatientTableModel();
        m.setData(list);
        peopleTable.setModel(m);
        resultTabs.setSelectedIndex(1);
        appendLog("Displayed patients: " + list.size());
    }
    private void showScores(List<PatientScore> list){
        PatientScoreTableModel m = new PatientScoreTableModel();
        m.setData(list);
        peopleTable.setModel(m);
        resultTabs.setSelectedIndex(1);
        appendLog("Displayed scores: " + list.size());
    }
    private void showDistrictCounts(List<DistrictCount> list){
        CountTableModel m = new CountTableModel();
        m.setDistrictData(list);
        peopleTable.setModel(m);
        resultTabs.setSelectedIndex(1);
        appendLog("Displayed district counts: " + list.size());
    }
    private void showRegionCounts(List<RegionCount> list){
        CountTableModel m = new CountTableModel();
        m.setRegionData(list);
        peopleTable.setModel(m);
        resultTabs.setSelectedIndex(1);
        appendLog("Displayed region counts: " + list.size());
    }

    // ====== UI helpers ======
    private static GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0; c.gridy=0; c.weightx=1.0; c.weighty=0; c.anchor=GridBagConstraints.WEST;
        c.insets = new Insets(4,6,4,6);
        return c;
    }
    private static void addRow(JPanel p, GridBagConstraints c, int r, String label, JComponent field) {
        GridBagConstraints l = (GridBagConstraints)c.clone();
        l.gridx = 0; l.gridy = r; l.fill = GridBagConstraints.NONE; l.weightx = 0;
        p.add(new JLabel(label), l);
        GridBagConstraints f = (GridBagConstraints)c.clone();
        f.gridx = 1; f.gridy = r; f.fill = GridBagConstraints.HORIZONTAL; f.weightx = 1.0;
        p.add(field, f);
    }
    private static void addBtn(JPanel p, GridBagConstraints c, int r, JButton b) {
        GridBagConstraints f = (GridBagConstraints)c.clone();
        f.gridx = 0; f.gridy = r; f.gridwidth = 2; f.fill = GridBagConstraints.NONE; f.weightx = 0;
        p.add(b, f);
    }
    private static void addBtnInline(JPanel p, GridBagConstraints c, int r, JButton b) {
        GridBagConstraints f = (GridBagConstraints)c.clone();
        f.gridx = 2; f.gridy = r; f.gridwidth = 1; f.fill = GridBagConstraints.NONE; f.weightx = 0;
        p.add(b, f);
    }

    private void appendLog(String line) {
        log.append(line);
        log.append("\n");
        log.setCaretPosition(log.getDocument().getLength());
    }
    private void appendCounts() {
        appendLog("COUNTS: " + ctl.counts());
    }

    // ====== Persist window bounds ======
    private void restoreWindowBounds(){
        Preferences p = Preferences.userNodeForPackage(MainFrame.class);
        int x = p.getInt("win.x", -1);
        int y = p.getInt("win.y", -1);
        int w = p.getInt("win.w", 1100);
        int h = p.getInt("win.h", 800);
        setSize(w, h);
        if (x >= 0 && y >= 0) setLocation(x, y);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                Point loc = getLocationOnScreen();
                Dimension sz = getSize();
                p.putInt("win.x", loc.x);
                p.putInt("win.y", loc.y);
                p.putInt("win.w", sz.width);
                p.putInt("win.h", sz.height);
            }
        });
    }
    // ====== Demo Data ======
    private JPanel buildDemoPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = gbc();

        JTextField tfPat = new JTextField("10", 6);
        JTextField tfTst = new JTextField("20", 6);

        JButton bGenPat = new JButton("Generate Patients");
        bGenPat.addActionListener(e -> {
            try {
                int n = Integer.parseInt(tfPat.getText().trim());
                appendLog(ctl.generatePatients(n));
                appendCounts();
            } catch (Exception ex) {
                appendLog("Error generate patients: " + ex.getMessage());
            }
        });

        JButton bGenTst = new JButton("Generate Tests");
        bGenTst.addActionListener(e -> {
            try {
                int n = Integer.parseInt(tfTst.getText().trim());
                appendLog(ctl.generateTests(n));
                appendCounts();
            } catch (Exception ex) {
                appendLog("Error generate tests: " + ex.getMessage());
            }
        });

        JButton bClear = new JButton("Clear All Data");
        bClear.addActionListener(e -> {
            try {
                ctl.clearAll();
                appendLog("All data cleared.");
                appendCounts();
            } catch (Exception ex) {
                appendLog("Error clearAll: " + ex.getMessage());
            }
        });

        int r=0;
        addRow(p,c,r++, "Patients to generate:", tfPat);
        addBtn(p,c,r++, bGenPat);
        addRow(p,c,r++, "Tests to generate:", tfTst);
        addBtn(p,c,r++, bGenTst);
        addBtn(p,c,r++, bClear);

        return p;
    }
}
