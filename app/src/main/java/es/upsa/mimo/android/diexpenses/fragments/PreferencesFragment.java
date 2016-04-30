package es.upsa.mimo.android.diexpenses.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.MediaStore;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.events.CurrencyChanged;
import es.upsa.mimo.android.diexpenses.events.ProfileImageChanged;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;

/**
 * Created by Diego on 24/4/16.
 */
public class PreferencesFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String TAG = PreferencesFragment.class.getSimpleName();

    public static PreferencesFragment newInstance() {
        PreferencesFragment preferencesFragment = new PreferencesFragment();
        return preferencesFragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference currencyPreference = findPreference(getString(R.string.preferences_currency_key));
        currencyPreference.setOnPreferenceChangeListener(this);

        Preference profileImagePreference = findPreference(getString(R.string.preferences_profile_image_key));
        profileImagePreference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (getString(R.string.preferences_currency_key).equals(preference.getKey())) {
            String[] currencies = getResources().getStringArray(R.array.currencies);
            int index = Integer.parseInt(newValue.toString());
            String currency = currencies[index];
            EventBus.getDefault().post(new CurrencyChanged(currency));
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(getString(R.string.preferences_profile_image_key).equals(preference.getKey())) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            int PICK_IMAGE = 1;
            startActivityForResult(Intent.createChooser(intent, getString(R.string.preferences_select_picture)), PICK_IMAGE);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                InputStream imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
                Bitmap imageProfile = BitmapFactory.decodeStream(imageStream);
                String diexpensesFolder = Diexpenses.getAppFolder(getActivity());

                File file = new File(diexpensesFolder, Constants.Preferences.PROFILE_IMAGE_NAME);
                OutputStream fOut = new FileOutputStream(file);

                imageProfile.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();

                MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

                EventBus.getDefault().post(new ProfileImageChanged(imageProfile));

            } catch (Exception e) {
                Log.e(TAG, "Error while processing profile image", e);
            }
        }
    }
}
