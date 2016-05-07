package es.upsa.mimo.android.diexpenses.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
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
public class PreferencesFragment extends PreferenceFragmentCompat implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String TAG = PreferencesFragment.class.getSimpleName();

    public static PreferencesFragment newInstance() {
        PreferencesFragment preferencesFragment = new PreferencesFragment();
        return preferencesFragment;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
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
        if (getString(R.string.preferences_profile_image_key).equals(preference.getKey())) {
            checkPermissionAndShowSelectImage();
        }

        return true;
    }

    private void showSelectImage() {
        Intent intent = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.preferences_select_picture)), Constants.RequestCodes.PICK_PROFILE_IMAGE);
    }

    private void checkPermissionAndShowSelectImage() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "Permission is granted");
            showSelectImage();
            return;
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission is granted");
            showSelectImage();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showDialog();
        } else {
            Log.d(TAG, "Permission is revoked");
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.Permissions.REQUEST_WRITE_EXTERNAL_STORAGE_CODE);
        }
    }

    private void showDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.preferences_permission_title))
                .setMessage(getString(R.string.preferences_permission_message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.Permissions.REQUEST_WRITE_EXTERNAL_STORAGE_CODE);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.Permissions.REQUEST_WRITE_EXTERNAL_STORAGE_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission: " + permissions[0] + " was " + grantResults[0]);
            showSelectImage();
        } else {
            Log.e(TAG, "Permission not granted.");
            Snackbar.make(getView(), R.string.preferences_permission_alert, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == Constants.RequestCodes.PICK_PROFILE_IMAGE) {
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