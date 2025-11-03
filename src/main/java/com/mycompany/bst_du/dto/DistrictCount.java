package com.mycompany.bst_du.dto;

/**
 * SK: Jednoduché DTO (data transfer object) reprezentujúce počet testov v danom okrese.
 * Používa sa v štatistických výstupoch a prehľadoch.
 */
public final class DistrictCount {
    public final int district;
    public final int count;

    public DistrictCount(int d, int c){
        this.district = d;
        this.count = c;
    }
}
