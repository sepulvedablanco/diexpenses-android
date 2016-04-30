package es.upsa.mimo.android.diexpenses.models;

import android.os.Parcelable;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Created by Diego on 19/4/16.
 */
@Accessors(chain = true)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
@hrisey.Parcelable
public class BankAccount implements Parcelable {

    private Long id;
    private String iban;
    private String entity;
    private String office;
    private String controlDigit;
    private String accountNumber;
    private BigDecimal balance;
    private String description;
    private String completeBankAccount;
}
