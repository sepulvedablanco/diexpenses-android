package es.upsa.mimo.android.diexpenses.models;

import lombok.experimental.Accessors;
import lombok.experimental.Builder;

/**
 * Created by Diego on 14/4/16.
 */
@Accessors(chain = true)
public class Subkind extends KindBase {

    public Subkind(String description) {
        this(null, description);
    }

    @Builder
    public Subkind(Long id, String description){
        this.setId(id);
        this.setDescription(description);
    }

}
