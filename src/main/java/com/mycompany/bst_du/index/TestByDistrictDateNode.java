/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bst_du.index;

import com.mycompany.bst_du.EntityNode;
import com.mycompany.bst_du.domain.PcrTest;

public final class TestByDistrictDateNode extends EntityNode<TestByDistrictDateNode> {
    public final int district;
    public final int ymd;
    public final long code;
    public final PcrTest ref;
    public TestByDistrictDateNode(int district, int ymd, long code, PcrTest ref){
        this.district=district; this.ymd=ymd; this.code=code; this.ref=ref;
    }
    @Override public int compareTo(TestByDistrictDateNode o){
        int c = Integer.compare(this.district, o.district);
        if (c!=0) return c;
        c = Integer.compare(this.ymd, o.ymd);
        if (c!=0) return c;
        return Long.compare(this.code, o.code);
    }
    @Override public String pretty(){ return "DDate{" + district + "," + ymd + "," + code + "}"; }
}