/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bst_du.index;

import com.mycompany.bst_du.EntityNode;
import com.mycompany.bst_du.domain.PcrTest;

public final class TestByCodeNode extends EntityNode<TestByCodeNode> {
    public final long code;
    public final PcrTest ref;
    public TestByCodeNode(long code, PcrTest ref){ this.code=code; this.ref=ref; }
    @Override public int compareTo(TestByCodeNode o){ return Long.compare(this.code, o.code); }
    @Override public String pretty(){ return "T{" + code + "}"; }
}