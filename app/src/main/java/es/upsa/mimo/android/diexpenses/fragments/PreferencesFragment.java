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
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.dialogs.ExplainPermissionDialogFragment;
import es.upsa.mimo.android.diexpenses.dialogs.ItemsDialogFragment;
import es.upsa.mimo.android.diexpenses.events.CurrencyChanged;
import es.upsa.mimo.android.diexpenses.events.ProfileImageChanged;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;

/**
 * Created by Diego on 24/4/16.
 */
public class PreferencesFragment extends PreferenceFragmentCompat implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener,
        ExplainPermissionDialogFragment.ExplainPermissionDialogListener,
        DialogInterface.OnClickListener {

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
            openAddPhoto();
        }

        return true;
    }

    private void showSelectImage() {
        Intent intent = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.preferences_select_picture)), Constants.RequestCodes.PICK_PROFILE_IMAGE);
    }

    private void showCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, Constants.RequestCodes.OPEN_CAMERA);
    }

    private void checkPermissionAndSelectOrTakePicture(String permission) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "Permission is granted");
            selectOrTakePicture(permission);
            return;
        }

        if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission is granted");
            selectOrTakePicture(permission);
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
            showDialog(permission);
        } else {
            Log.d(TAG, "Permission is revoked");
            int requestCode = getRequestCode(permission);
            requestPermissions(new String[]{permission}, requestCode);
        }
    }

    private void selectOrTakePicture(String permission) {
        switch (permission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                showSelectImage();
                break;
            case Manifest.permission.CAMERA:
                showCamera();
                break;
            default:
                break;
        }
    }

    private @StringRes int getRequestPermissionMessage(String permission) {
        switch (permission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return R.string.preferences_external_storage_permission_message;
            case Manifest.permission.CAMERA:
                return R.string.preferences_camera_permission_message;
            default:
                return -1;
        }
    }

    private int getRequestCode(String permission) {
        switch (permission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return Constants.RequestCodes.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE;
            case Manifest.permission.CAMERA:
                return Constants.RequestCodes.REQUEST_CAMERA_PERMISSION_CODE;
            default:
                return -1;
        }
    }

    private void showDialog(final String permission) {
        final String tag = "PermissionDialogFragment";
        Diexpenses.checkDialog(getFragmentManager(), tag);
        DialogFragment permissionDialog = ExplainPermissionDialogFragment.newInstance(
                PreferencesFragment.this,
                R.string.preferences_permission_title,
                getRequestPermissionMessage(permission),
                permission);
        permissionDialog.show(getFragmentManager(), tag);
    }

    private void openAddPhoto() {

        String[] addPhoto = new String[]{getString(R.string.preferences_take_photo), getString(R.string.preferences_photo_from_gallery)};

        final String tag = "ItemsDialogFragment";
        Diexpenses.checkDialog(getFragmentManager(), tag);
        DialogFragment dialogFragment = ItemsDialogFragment.newInstance(
                R.string.preference_profile_image_origin, addPhoto, PreferencesFragment.this);
        dialogFragment.show(getFragmentManager(), tag);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.RequestCodes.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Write external storage permission: " + permissions[0] + " was " + grantResults[0]);
                showSelectImage();
            } else {
                Log.e(TAG, "Write external storage permission not granted.");
                Snackbar.make(getView(), R.string.preferences_external_storage_permission_alert, Snackbar.LENGTH_LONG).show();
            }
        else if (requestCode == Constants.RequestCodes.REQUEST_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission: " + permissions[0] + " was " + grantResults[0]);
                showCamera();
            } else {
                Log.e(TAG, "Camera permission not granted.");
                Snackbar.make(getView(), R.string.preferences_camera_permission_alert, Snackbar.LENGTH_LONG).show();
            }
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
                saveImage(imageProfile);
            } catch (Exception e) {
                Log.e(TAG, "Error while opening profile image", e);
            }
        } else if (requestCode == Constants.RequestCodes.OPEN_CAMERA) {
            Bitmap imageProfile = (Bitmap) data.getExtras().get("data");
            saveImage(imageProfile);
        }
    }

    private void saveImage(Bitmap bmImageProfile) {
        try {
            String diexpensesFolder = Diexpenses.getAppFolder(getActivity());

            File file = new File(diexpensesFolder, Constants.Preferences.PROFILE_IMAGE_NAME);
            OutputStream fOut = new FileOutputStream(file);

            bmImageProfile.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            EventBus.getDefault().post(new ProfileImageChanged(bmImageProfile));
        } catch (Exception e) {
            Log.e(TAG, "Error while processing profile image", e);
        }

    }

    @Override
    public void onOK(String permission) {
        requestPermissions(new String[]{permission}, getRequestCode(permission));
    }

    @Override
    public void onKO() {
        // do nothing
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(which == 0) {
            checkPermissionAndSelectOrTakePicture(Manifest.permission.CAMERA);
        } else if (which == 1) {
            checkPermissionAndSelectOrTakePicture(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }
}