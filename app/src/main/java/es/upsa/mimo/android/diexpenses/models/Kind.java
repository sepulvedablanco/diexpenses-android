package es.upsa.mimo.android.diexpenses.models;

import lombok.experimental.Accessors;
import lombok.experimental.Builder;

/**
 * Created by Diego on 14/4/16.
 */
@Accessors(chain = true)
public class Kind extends KindBase {

    public Kind(String description) {
        this(null, description);
    }

    @Builder
    public Kind(Long id, String description){
        this.setId(id);
        this.setDescription(description);
    }
}
