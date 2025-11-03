package com.mycompany.bst_du.gui.view.table;

import com.mycompany.bst_du.dto.DistrictCount;
import com.mycompany.bst_du.dto.RegionCount;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** SK: Tabuľkový model pre prehľady "počet podľa okresu/regiónu". */
public final class CountTableModel extends AbstractTableModel {
    private String[] cols = {"Key","Count"};
    private final List<Object> data = new ArrayList<>();
    private boolean isDistrict = true; // SK: true=okresy, false=regióny

    /** SK: Prepne na režim okresov a nastaví dáta. */
    public void setDistrictData(List<DistrictCount> list){
        data.clear();
        isDistrict = true;
        if (list != null) data.addAll(list);
        cols = new String[]{"District","Count"}; // SK: hlavičky pre okresy
        fireTableStructureChanged();
    }

    /** SK: Prepne na režim regiónov a nastaví dáta. */
    public void setRegionData(List<RegionCount> list){
        data.clear();
        isDistrict = false;
        if (list != null) data.addAll(list);
        cols = new String[]{"Region","Count"}; // SK: hlavičky pre regióny
        fireTableStructureChanged();
    }

    @Override public int getRowCount(){ return data.size(); }
    @Override public int getColumnCount(){ return cols.length; }
    @Override public String getColumnName(int c){ return cols[c]; }

    /** SK: Hodnota podľa aktuálneho režimu a stĺpca (0=kľúč, 1=počet). */
    @Override public Object getValueAt(int r, int c){
        Object o = data.get(r);
        if (isDistrict && o instanceof DistrictCount dc){
            return c==0 ? dc.district : dc.count;
        } else if (!isDistrict && o instanceof RegionCount rc){
            return c==0 ? rc.region : rc.count;
        }
        return "";
    }
}
