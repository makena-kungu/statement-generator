package org.example.data;

import java.util.Set;

public enum Classification {
    REVENUE("Revenue"), COGS("Cost of Goods Sold"), EXPENSES("Expenses"), INCOMES("Incomes");

    public static final int TYPE1 = 1;
    public static final int TYPE2 = 2;

    private final String title;

    Classification(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public int getType() {
        return isType1() ? TYPE1 : TYPE2;
    }

    public Set<Classification> type1() {
        return Set.of(REVENUE, COGS);
    }

    public Set<Classification> type2() {
        return Set.of(EXPENSES, INCOMES);
    }

    private boolean isType1() {
        return isInClass(this, type1());
    }

    private boolean isType2() {
        return isInClass(this, type2());
    }

    private boolean isInClass(Classification klass, Set<Classification> classes) {
        return classes.contains(klass);
    }
}
