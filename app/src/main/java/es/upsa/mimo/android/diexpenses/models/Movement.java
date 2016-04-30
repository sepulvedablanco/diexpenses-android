package es.upsa.mimo.android.diexpenses.models;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Created by Diego on 20/4/16.
 */
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class Movement {

    private Long id;
    private Boolean expense;
    private String concept;
    private Date transactionDate;
    private BigDecimal amount;
    @SerializedName("financialMovementType")
    private Kind kind;
    @SerializedName("financialMovementSubtype")
    private Subkind subkind;
    private BankAccount bankAccount;

}