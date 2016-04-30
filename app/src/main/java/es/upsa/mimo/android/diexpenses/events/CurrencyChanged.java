package es.upsa.mimo.android.diexpenses.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Diego on 29/4/16.
 */
@Getter
@AllArgsConstructor(suppressConstructorProperties = true)
public class CurrencyChanged {

    private String currency;
}
