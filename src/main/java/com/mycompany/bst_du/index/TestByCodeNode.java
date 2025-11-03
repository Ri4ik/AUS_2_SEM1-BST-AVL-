/*
 * SK: Uzol indexu pre PCR test podľa jeho unikátneho kódu.
 *     Porovnanie pre BST/AVL sa vykonáva číselne podľa poľa `code`.
 */
package com.mycompany.bst_du.index;

import com.mycompany.bst_du.EntityNode;
import com.mycompany.bst_du.domain.PcrTest;

public final class TestByCodeNode extends EntityNode<TestByCodeNode> {
    /** SK: Jedinečný kód testu (primárny kľúč). */
    public final long code;

    /** SK: Odkaz na objekt PCR testu v doméne. */
    public final PcrTest ref;

    public TestByCodeNode(long code, PcrTest ref){ 
        this.code = code; 
        this.ref = ref; 
    }

    @Override 
    public int compareTo(TestByCodeNode o){ 
        return Long.compare(this.code, o.code); 
    }

    /** SK: Krátke textové zobrazenie uzla pre ladenie alebo logovanie. */
    @Override 
    public String pretty(){ 
        return "T{" + code + "}"; 
    }
}
