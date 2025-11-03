/*
 * SK: Uzol indexu pre PCR testy zoradené podľa ID pacienta a času testu.
 */
package com.mycompany.bst_du.index;

import com.mycompany.bst_du.EntityNode;
import com.mycompany.bst_du.domain.PcrTest;

public final class TestByPatientTimeNode extends EntityNode<TestByPatientTimeNode> {
    public final String patientId;
    public final long ts;
    public final long code;
    public final PcrTest ref;

    public TestByPatientTimeNode(String pid, long ts, long code, PcrTest ref) {
        this.patientId = pid;
        this.ts = ts;
        this.code = code;
        this.ref = ref;
    }

    @Override
    public int compareTo(TestByPatientTimeNode o) {
        int c = this.patientId.compareTo(o.patientId);
        if (c != 0) return c;
        c = Long.compare(this.ts, o.ts);
        if (c != 0) return c;
        return Long.compare(this.code, o.code);
    }

    @Override
    public String pretty() {
        return "PTime{" + patientId + "," + ts + "," + code + "}";
    }
}
