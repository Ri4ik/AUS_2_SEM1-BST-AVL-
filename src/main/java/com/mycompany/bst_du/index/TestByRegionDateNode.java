/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bst_du.index;

import com.mycompany.bst_du.EntityNode;
import com.mycompany.bst_du.domain.PcrTest;

public final class TestByRegionDateNode extends EntityNode<TestByRegionDateNode> {
    public final int region;
    public final int ymd;
    public final long code;
    public final PcrTest ref;
    public TestByRegionDateNode(int region, int ymd, long code, PcrTest ref){
        this.region=region; this.ymd=ymd; this.code=code; this.ref=ref;
    }
    @Override public int compareTo(TestByRegionDateNode o){
        int c = Integer.compare(this.region, o.region);
        if (c!=0) return c;
        c = Integer.compare(this.ymd, o.ymd);
        if (c!=0) return c;
        return Long.compare(this.code, o.code);
    }
    @Override public String pretty(){ return "RDate{" + region + "," + ymd + "," + code + "}"; }
}