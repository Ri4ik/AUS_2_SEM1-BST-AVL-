package com.mycompany.bst_du;

public final class StringNode extends EntityNode<StringNode> {
    private final String value;

    public StringNode(String value) {
        if (value == null) throw new IllegalArgumentException("value null");
        this.value = value;
    }

    public String getValue() { return value; }

    @Override
    public int compareTo(StringNode other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String pretty() {
        return "Str{\"" + value + "\"}";
    }
}
