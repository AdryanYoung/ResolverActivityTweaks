package org.fifi.android.resolveractivitytweaks;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class RATSettings extends PreferenceActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (onIsMultiPane()) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        if (!onIsMultiPane()) {
            getFragmentManager().beginTransaction().replace(android.R.id.content,
                    new GeneralPreferenceFragment()).commit();
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private class ReflectInDescriptionPrefChangeListener implements Preference.OnPreferenceChangeListener {
        private int mOnDescrResId;
        private int mOffDescrResId;

        public ReflectInDescriptionPrefChangeListener(int onDescrResId, int offDescrResId) {
            mOnDescrResId = onDescrResId;
            mOffDescrResId = offDescrResId;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            setDescriptionString(preference, value);
            return true;
        }
        public void setDescriptionString(Preference preference, Object value) {
            if ((Boolean) value) {
                preference.setSummary(mOnDescrResId);
            } else {
                preference.setSummary(mOffDescrResId);
            }
        }
    }

    private class ToggleHideOnceAlwaysListener extends ReflectInDescriptionPrefChangeListener {
        private Preference mDependentPreference;

        public ToggleHideOnceAlwaysListener(int onDescrResId, int offDescrResId, Preference dependentPreference) {
            super(onDescrResId, offDescrResId);
            mDependentPreference = dependentPreference;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            setDependentPreference((Boolean)value);
            return super.onPreferenceChange(preference, value);
        }

        public void setDependentPreference(boolean value) {
            mDependentPreference.setEnabled(value);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(Const.PREFERENCES_NAME);
            //noinspection deprecation
            prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.pref_general);

            Preference ratEnabledPref = findPreference(Const.PREF_RAT_ENABLE);
            Preference hideOnceAlwaysPref = findPreference(Const.PREF_RAT_HIDE_ONCE_ALWAYS);
            ToggleHideOnceAlwaysListener thoal = new ToggleHideOnceAlwaysListener(R.string.rat_enable_description_on, R.string.rat_enable_description_off, hideOnceAlwaysPref);
            ratEnabledPref.setOnPreferenceChangeListener(thoal);
            boolean enableVal = PreferenceManager.getDefaultSharedPreferences(ratEnabledPref.getContext()).getBoolean(Const.PREF_RAT_ENABLE, Const.PREF_RAT_ENABLE_DEFAULT);
            thoal.setDescriptionString(ratEnabledPref, enableVal);
            thoal.setDependentPreference(enableVal);

            ReflectInDescriptionPrefChangeListener hideOnceAlwaysChangeListener = new ReflectInDescriptionPrefChangeListener(R.string.rat_hideAlwaysOnce_description_on, R.string.rat_hideAlwaysOnce_description_off);
            hideOnceAlwaysPref.setOnPreferenceChangeListener(hideOnceAlwaysChangeListener);
            hideOnceAlwaysChangeListener.setDescriptionString(hideOnceAlwaysPref, PreferenceManager.getDefaultSharedPreferences(ratEnabledPref.getContext()).getBoolean(Const.PREF_RAT_HIDE_ONCE_ALWAYS, Const.PREF_RAT_HIDE_ONCE_ALWAYS_DEFAULT));

            Preference showInLauncherPref = findPreference(Const.PREF_RAT_SHOW_LAUNCHER_ICON);
            ReflectInDescriptionPrefChangeListener showInLauncherPrefChangeListener = new ReflectInDescriptionPrefChangeListener(R.string.rat_showLauncher_description_on, R.string.rat_showLauncher_description_off) {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    PackageManager pm = getPackageManager();
                    int val;
                    if ((Boolean)value) {
                        val = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                    } else {
                        val = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                    }
                    pm.setComponentEnabledSetting(new ComponentName(RATSettings.this, Const.PACKAGE_NAME+".RATSettings-Alias"), val, PackageManager.DONT_KILL_APP);
                    return super.onPreferenceChange(preference, value);
                }
            };
            showInLauncherPref.setOnPreferenceChangeListener(showInLauncherPrefChangeListener);
            showInLauncherPrefChangeListener.setDescriptionString(showInLauncherPref, showInLauncherPref.getSharedPreferences().getBoolean(Const.PREF_RAT_SHOW_LAUNCHER_ICON, Const.PREF_RAT_SHOW_LAUNCHER_ICON_DEFAULT));
        }
    }
}
