/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bst_du.index;

import com.mycompany.bst_du.EntityNode;
import com.mycompany.bst_du.domain.PcrTest;

public final class TestByWorkstationDateNode extends EntityNode<TestByWorkstationDateNode> {
    public final long workstationId;
    public final int  ymd;
    public final long code;
    public final PcrTest ref;
    public TestByWorkstationDateNode(long ws, int ymd, long code, PcrTest ref){
        this.workstationId=ws; this.ymd=ymd; this.code=code; this.ref=ref;
    }
    @Override public int compareTo(TestByWorkstationDateNode o){
        int c = Long.compare(this.workstationId, o.workstationId);
        if (c!=0) return c;
        c = Integer.compare(this.ymd, o.ymd);
        if (c!=0) return c;
        return Long.compare(this.code, o.code);
    }
    @Override public String pretty(){ return "WSDate{" + workstationId + "," + ymd + "," + code + "}"; }
}