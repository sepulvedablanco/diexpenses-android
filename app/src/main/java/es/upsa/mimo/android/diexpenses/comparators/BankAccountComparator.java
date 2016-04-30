package es.upsa.mimo.android.diexpenses.comparators;

import java.util.Comparator;

import es.upsa.mimo.android.diexpenses.models.BankAccount;

/**
 * Created by Diego on 19/4/16.
 */
public class BankAccountComparator implements Comparator<BankAccount> {

    @Override
    public int compare(BankAccount lhs, BankAccount rhs) {
        return lhs.getDescription().compareTo(rhs.getDescription());
    }
}
