package es.upsa.mimo.android.diexpenses.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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
public class MovementPage {

    @SerializedName(value = "lstMovements", alternate = {"Page", "Pagina"})
    private List<Movement> lstMovements;
    @SerializedName(value = "totalMovements", alternate = {"TotalMovements", "MovimientosTotales"})
    private Integer totalMovements;

}
