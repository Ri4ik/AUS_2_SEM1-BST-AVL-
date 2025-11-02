package com.mycompany.bst_du.gui.view.table;

import com.mycompany.bst_du.domain.Patient;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public final class PatientTableModel extends AbstractTableModel {
    private final String[] cols = {"PatientId","FirstName","LastName","BirthDate"};
    private final List<Patient> data = new ArrayList<>();

    public void setData(List<Patient> list){
        data.clear();
        if (list != null) data.addAll(list);
        fireTableDataChanged();
    }

    @Override public int getRowCount(){ return data.size(); }
    @Override public int getColumnCount(){ return cols.length; }
    @Override public String getColumnName(int c){ return cols[c]; }

    @Override public Object getValueAt(int r, int c){
        Patient p = data.get(r);
        return switch (c){
            case 0 -> p.patientId;
            case 1 -> p.firstName;
            case 2 -> p.lastName;
            case 3 -> p.birthDate.toString();
            default -> "";
        };
    }
}
