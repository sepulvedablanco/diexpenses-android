package es.upsa.mimo.android.diexpenses.events;

import android.graphics.Bitmap;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Diego on 27/4/16.
 */
@Getter
@AllArgsConstructor(suppressConstructorProperties = true)
public class ProfileImageChanged {

    private Bitmap bitmapProfileImage;
}
