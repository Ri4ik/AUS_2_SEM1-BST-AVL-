/* ========================= com/mycompany/bst_du/IntNode.java ========================= */
package com.mycompany.bst_du;

public final class IntNode extends EntityNode<IntNode> {
    private final int value;

    public IntNode(int value) { this.value = value; }

    public int getValue() { return value; }

    @Override
    public int compareTo(IntNode other) {
        // SK: prirodzené porovnanie podľa hodnoty
        return Integer.compare(this.value, other.value);
    }

    @Override
    public String pretty() {
        return "Int{" + value + "}";
    }
}
