/* ========================= com/mycompany/bst_du/PersonNode.java ========================= */
package com.mycompany.bst_du;

public class PersonNode extends EntityNode<PersonNode> {
    private final String firstName, lastName;
    private final int birthYear;

    public PersonNode(String firstName, String lastName, int birthYear) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthYear = birthYear;
    }

    @Override
    public int compareTo(PersonNode p) {
        int c1 = Integer.compare(this.birthYear, p.birthYear) ;
        if (c1 != 0) return c1;
        int c2 = this.firstName.compareToIgnoreCase(p.firstName);
        if (c2 != 0) return c2;
        return this.lastName.compareToIgnoreCase(p.lastName);
    }

    @Override
    public String pretty() {
        return "Person{" + lastName + ", " + firstName + " | " + birthYear + "}";
    }
}
