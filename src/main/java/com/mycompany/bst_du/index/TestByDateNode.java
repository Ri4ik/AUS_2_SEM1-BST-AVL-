/*
 * SK: Uzol indexu pre PCR testy zoradené podľa dátumu vykonania.
 *     Primárne porovnáva dátum (ymd = YYYYMMDD), sekundárne podľa kódu testu.
 */
package com.mycompany.bst_du.index;

import com.mycompany.bst_du.EntityNode;
import com.mycompany.bst_du.domain.PcrTest;

public final class TestByDateNode extends EntityNode<TestByDateNode> {
    /** SK: Dátum testu vo formáte YYYYMMDD, uložený ako celé číslo. */
    public final int ymd;

    /** SK: Jedinečný kód testu (sekundárny kľúč pri rovnakom dátume). */
    public final long code;

    /** SK: Odkaz na objekt PCR testu v doméne. */
    public final PcrTest ref;

    public TestByDateNode(int ymd, long code, PcrTest ref){
        this.ymd = ymd;
        this.code = code;
        this.ref = ref;
    }

    @Override 
    public int compareTo(TestByDateNode o){
        int c = Integer.compare(this.ymd, o.ymd);
        if (c != 0) return c;
        return Long.compare(this.code, o.code);
    }

    /** SK: Krátke textové zobrazenie uzla (napr. pre logy alebo testovanie). */
    @Override 
    public String pretty(){ 
        return "GDate{" + ymd + "," + code + "}"; 
    }
}
