package es.upsa.mimo.android.diexpenses.models;

import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Created by Diego on 27/3/16.
 */
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
@hrisey.Parcelable
public class User implements Parcelable {

    private Long id;
    private String authToken;
    private String user;
    private String name;
}
