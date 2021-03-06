package me.shouheng.omnilist.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.afollestad.materialdialogs.color.ColorChooserDialog;

import org.polaric.colorful.Colorful;

import java.util.LinkedList;
import java.util.List;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.base.CommonActivity;
import me.shouheng.omnilist.databinding.ActivitySettingsBinding;
import me.shouheng.omnilist.fragment.setting.AppInfoFragment;
import me.shouheng.omnilist.fragment.setting.PrimaryPickerFragment;
import me.shouheng.omnilist.fragment.setting.SettingsAssignment;
import me.shouheng.omnilist.fragment.setting.SettingsBackup;
import me.shouheng.omnilist.fragment.setting.SettingsDashboard;
import me.shouheng.omnilist.fragment.setting.SettingsFragment;
import me.shouheng.omnilist.fragment.setting.SettingsNotification;
import me.shouheng.omnilist.fragment.setting.SettingsPreferences;
import me.shouheng.omnilist.fragment.setting.SettingsSecurity;
import me.shouheng.omnilist.listener.OnFragmentDestroyListener;
import me.shouheng.omnilist.listener.OnSettingsChangedListener;
import me.shouheng.omnilist.listener.OnThemeSelectedListener;
import me.shouheng.omnilist.listener.SettingChangeType;
import me.shouheng.omnilist.manager.FragmentHelper;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.preferences.ColorPreferences;
import me.shouheng.omnilist.utils.preferences.LockPreferences;

public class SettingsActivity extends CommonActivity<ActivitySettingsBinding> implements
        SettingsFragment.OnPreferenceClickListener,
        OnThemeSelectedListener,
        OnFragmentDestroyListener,
        OnSettingsChangedListener {

    public final static String KEY_CONTENT_CHANGE_TYPES = "key_content_change_types";
    public final static String ACTION_NAV_TO_BACKUP_FRAGMENT = "action_navigate_to_backup_settings_fragment";

    private final static int REQUEST_CODE_PASSWORD = 0x0201;

    private String keyForColor;

    private boolean isDashboardSettingsChanged = false;

    private List<Integer> changedTypes = new LinkedList<>();

    public static void start(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void start(Activity activity, String action, int requestCode) {
        Intent intent = new Intent(activity, SettingsActivity.class);
        intent.setAction(action);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_settings;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        configToolbar();

        configFragment();
    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.text_settings);
        }
        if (!isDarkTheme()) {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay);
        }
    }

    private void configFragment() {
        String action = getIntent().getAction();
        if (TextUtils.isEmpty(action)) {
            FragmentHelper.replace(this, new SettingsFragment(), R.id.fragment_container);
        } else if (ACTION_NAV_TO_BACKUP_FRAGMENT.equals(action)){
            FragmentHelper.replace(this, new SettingsBackup(), R.id.fragment_container);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
        if (getString(R.string.key_accent_color).equals(keyForColor)) {
            setupTheme(selectedColor);
            if (isSettingsFragment()) {
                ((SettingsFragment) getCurrentFragment()).notifyAccentColorChanged(selectedColor);
            }
        }
    }

    private void setupTheme(@ColorInt int i) {
        String colorName = ColorUtils.getColorName(i);
        ColorPreferences.getInstance().setAccentColor(Colorful.AccentColor.getByColorName(colorName));
        ColorUtils.forceUpdateThemeStatus();
        updateTheme();
        ToastUtils.makeToast(R.string.set_successfully);
    }

    private boolean isSettingsFragment() {
        return getCurrentFragment() instanceof SettingsFragment;
    }

    private android.app.Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    public void onPreferenceClick(String key) {
        keyForColor = key;
        if (getString(R.string.key_preferences).equals(key)) {
            replaceWithCallback(SettingsPreferences.newInstance());
        }  else if (getString(R.string.key_assignment).equals(key)) {
            replaceWithCallback(SettingsAssignment.newInstance());
        } else if (getString(R.string.key_notification).equals(key)) {
            replaceWithCallback(SettingsNotification.newInstance());
        } else if (getString(R.string.key_data_backup).equals(key)) {
            replaceWithCallback(SettingsBackup.newInstance());
        } else if (getString(R.string.key_data_security).equals(key)) {
            if (LockPreferences.getInstance().isPasswordRequired()
                    && !TextUtils.isEmpty(LockPreferences.getInstance().getPassword())) {
                LockActivity.requirePassword(this, REQUEST_CODE_PASSWORD);
            } else {
                replaceWithCallback(SettingsSecurity.newInstance());
            }
        } else if (getString(R.string.key_primary_color).equals(key)) {
            replaceWithCallback(PrimaryPickerFragment.newInstance());
        } else if (getString(R.string.key_accent_color).equals(key)) {
            showAccentColorPicker();
        } else if (getString(R.string.key_dashboard).equals(key)) {
            replaceWithCallback(SettingsDashboard.newInstance());
        } else if (getString(R.string.key_about).equals(key)) {
            replaceWithCallback(AppInfoFragment.newInstance());
        }
    }

    private void replaceWithCallback(android.app.Fragment fragment) {
        FragmentHelper.replaceWithCallback(this, fragment, R.id.fragment_container);
    }

    private void replaceWithCallback(Fragment fragment) {
        FragmentHelper.replaceWithCallback(this, fragment, R.id.fragment_container);
    }

    @Override
    public void onThemeSelected(Colorful.ThemeColor themeColor) {
        getBinding().bar.toolbar.setBackgroundColor(primaryColor());
        if (isSettingsFragment())
            ((SettingsFragment) getCurrentFragment())
                    .notifyPrimaryColorChanged(getResources().getColor(themeColor.getColorRes()));
    }

    private void showAccentColorPicker() {
        new ColorChooserDialog.Builder(this, R.string.select_accent_color)
                .allowUserColorInput(false)
                .preselect(ColorUtils.accentColor())
                .allowUserColorInputAlpha(false)
                .titleSub(R.string.select_accent_color)
                .accentMode(true)
                .backButton(R.string.text_back)
                .doneButton(R.string.done_label)
                .cancelButton(R.string.text_cancel)
                .show();
    }

    @Override
    public void onFragmentDestroy() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.text_settings);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PASSWORD:
                if (resultCode == Activity.RESULT_OK) {
                    FragmentHelper.replaceWithCallback(this, new SettingsSecurity(), R.id.fragment_container);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSettingChanged(SettingChangeType changedType) {
        isDashboardSettingsChanged = true;
        changedTypes.add(changedType.id);
    }

    @Override
    public void onBackPressed() {
        if (isDashboardSettingsChanged) {
            Intent intent = new Intent();

            int len = changedTypes.size();
            int[] types = new int[len];
            for (int i=0;i<len;i++) {
                types[i] = changedTypes.get(i);
            }

            intent.putExtra(KEY_CONTENT_CHANGE_TYPES, types);
            setResult(Activity.RESULT_OK, intent);
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
