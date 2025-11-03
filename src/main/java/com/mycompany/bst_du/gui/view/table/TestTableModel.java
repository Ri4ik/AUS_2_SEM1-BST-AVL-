package com.mycompany.bst_du.gui.view.table;

import com.mycompany.bst_du.domain.PcrTest;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Model tabuľky PCR testov. */
public final class TestTableModel extends AbstractTableModel {
    private static final String[] COLS = {
        "Code", "Patient ID", "Timestamp", "Workstation", "District", "Region", "Positive", "Value", "Note"
    };
    private final List<PcrTest> data = new ArrayList<>();

    public void setData(List<PcrTest> list) {
        data.clear();
        if (list != null) data.addAll(list);
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return COLS.length; }
    @Override public String getColumnName(int c) { return COLS[c]; }

    @Override
    public Object getValueAt(int r, int c) {
        PcrTest t = data.get(r);
        return switch (c) {
            case 0 -> t.testCode;
            case 1 -> t.patientId;
            case 2 -> t.timestamp;
            case 3 -> t.workstationId;
            case 4 -> t.district;
            case 5 -> t.region;
            case 6 -> t.positive;
            case 7 -> t.value;
            case 8 -> t.note;
            default -> "";
        };
    }
}
