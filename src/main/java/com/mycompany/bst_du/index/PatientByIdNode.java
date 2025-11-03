/*
 * SK: Uzol indexu pacienta podľa jeho ID.
 *     Porovnanie pre BST/AVL prebieha lexikograficky podľa poľa `id`.
 */
package com.mycompany.bst_du.index;

import com.mycompany.bst_du.EntityNode;
import com.mycompany.bst_du.domain.Patient;

public final class PatientByIdNode extends EntityNode<PatientByIdNode> {
    /** SK: Primárny kľúč pacienta (patientId). */
    public final String id;
    /** SK: Odkaz na doménový objekt pacienta. */
    public final Patient ref;

    public PatientByIdNode(String id, Patient ref){ this.id=id; this.ref=ref; }

    @Override public int compareTo(PatientByIdNode o){ return this.id.compareTo(o.id); }

    /** SK: Krátke ľudské zobrazenie uzla pre logy/testy. */
    @Override public String pretty(){ return "PID{" + id + "}"; }
}
