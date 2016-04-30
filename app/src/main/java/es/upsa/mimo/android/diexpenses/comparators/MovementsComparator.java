package es.upsa.mimo.android.diexpenses.comparators;

import java.util.Comparator;

import es.upsa.mimo.android.diexpenses.models.Movement;

/**
 * Created by Diego on 20/4/16.
 */
public class MovementsComparator implements Comparator<Movement> {

    @Override
    public int compare(Movement lhs, Movement rhs) {
        return rhs.getTransactionDate().compareTo(lhs.getTransactionDate());
    }
}
