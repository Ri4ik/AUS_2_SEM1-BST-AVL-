/* ========================= com/mycompany/bst_du/BookNode.java ========================= */
package com.mycompany.bst_du;

public class BookNode extends EntityNode<BookNode> {
    private final String title, author;
    private final int year;

    public BookNode(String title, String author, int year) {
        this.title = title;
        this.author = author;
        this.year = year;
    }

    @Override
    public int compareTo(BookNode b) {
        int c1 = Integer.compare(this.year, b.year);
        if (c1 != 0) return c1;
        int c2 = this.author.compareToIgnoreCase(b.author);
        if (c2 != 0) return c2;
        return this.title.compareToIgnoreCase(b.title);
    }

    @Override
    public String pretty() {
        return "Book{" + title + " | " + author + " | " + year + "}";
    }
}

