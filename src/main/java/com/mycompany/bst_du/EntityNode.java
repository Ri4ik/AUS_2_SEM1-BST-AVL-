/* ========================= com/mycompany/bst_du/EntityNode.java ========================= */
package com.mycompany.bst_du;
 
public abstract class EntityNode<T extends EntityNode<T>> implements Comparable<T> {
    @Override
    public abstract int compareTo(T other);
    public abstract String pretty();
}

