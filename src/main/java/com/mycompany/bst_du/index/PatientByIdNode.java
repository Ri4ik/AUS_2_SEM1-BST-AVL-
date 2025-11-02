/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bst_du.index;

import com.mycompany.bst_du.EntityNode;
import com.mycompany.bst_du.domain.Patient;

public final class PatientByIdNode extends EntityNode<PatientByIdNode> {
    public final String id;
    public final Patient ref;
    public PatientByIdNode(String id, Patient ref){ this.id=id; this.ref=ref; }
    @Override public int compareTo(PatientByIdNode o){ return this.id.compareTo(o.id); }
    @Override public String pretty(){ return "PID{" + id + "}"; }
}