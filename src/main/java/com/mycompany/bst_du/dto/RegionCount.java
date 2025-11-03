package com.mycompany.bst_du.dto;

/**
 * SK: Jednoduché DTO (data transfer object) reprezentujúce počet testov v danom regióne.
 * Používa sa v prehľadoch a štatistikách systému.
 */
public final class RegionCount {
    public final int region;
    public final int count;

    public RegionCount(int r, int c){
        this.region = r;
        this.count = c;
    }
}
