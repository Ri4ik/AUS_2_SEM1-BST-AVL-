package com.mycompany.bst_du.gui.view.table;

import com.mycompany.bst_du.dto.PatientScore;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Model pre tabuľku s výsledkami pacientov a ich skóre. */
public final class PatientScoreTableModel extends AbstractTableModel {
    private static final String[] COLS = {"Patient ID", "First Name", "Last Name", "Score", "District"};
    private final List<PatientScore> data = new ArrayList<>();

    public void setData(List<PatientScore> list) {
        data.clear();
        if (list != null) data.addAll(list);
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return COLS.length; }
    @Override public String getColumnName(int c) { return COLS[c]; }

    @Override
    public Object getValueAt(int r, int c) {
        PatientScore ps = data.get(r);
        return switch (c) {
            case 0 -> ps.patient.patientId;
            case 1 -> ps.patient.firstName;
            case 2 -> ps.patient.lastName;
            case 3 -> ps.score;      // hodnotiaca metrika
            case 4 -> ps.district;   // -1 ak sa nevzťahuje
            default -> "";
        };
    }
}
