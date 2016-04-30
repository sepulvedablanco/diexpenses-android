package es.upsa.mimo.android.diexpenses.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.Builder;

/**
 * Created by Diego on 2/4/16.
 */
@Accessors(chain = true)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class NewUser extends Credential {

    private String name;

    @Builder
    public NewUser(String name, String user, String pass){
        super(user, pass);
        this.name = name;
    }

}
