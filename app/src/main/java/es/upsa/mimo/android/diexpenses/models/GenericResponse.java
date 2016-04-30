package es.upsa.mimo.android.diexpenses.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Created by Diego on 2/4/16.
 */
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class GenericResponse {

    private int code;
    private String message;
}
