package com.mycompany.bst_du.gui.view.table;

import com.mycompany.bst_du.dto.PatientScore;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public final class PatientScoreTableModel extends AbstractTableModel {
    private final String[] cols = {"PatientId","FirstName","LastName","Score","District"};
    private final List<PatientScore> data = new ArrayList<>();

    public void setData(List<PatientScore> list){
        data.clear();
        if (list != null) data.addAll(list);
        fireTableDataChanged();
    }

    @Override public int getRowCount(){ return data.size(); }
    @Override public int getColumnCount(){ return cols.length; }
    @Override public String getColumnName(int c){ return cols[c]; }

    @Override public Object getValueAt(int r, int c){
        PatientScore ps = data.get(r);
        return switch (c){
            case 0 -> ps.patient.patientId;
            case 1 -> ps.patient.firstName;
            case 2 -> ps.patient.lastName;
            case 3 -> ps.score;
            case 4 -> ps.district; // может быть 0, если не задан в DTO
            default -> "";
        };
    }
}
