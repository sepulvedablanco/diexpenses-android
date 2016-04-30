package es.upsa.mimo.android.diexpenses.comparators;

import java.util.Comparator;

import es.upsa.mimo.android.diexpenses.models.KindBase;

/**
 * Created by Diego on 17/4/16.
 */
public class KindBaseComparator implements Comparator<KindBase> {

    @Override
    public int compare(KindBase lhs, KindBase rhs) {
        return lhs.getDescription().compareTo(rhs.getDescription());
    }
}
