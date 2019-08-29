// Resolver Activity Tweaks - Xposed module to tweak the Android
// Resolver Activity
// Copyright (C) 2015-2019 Philippe Troin (F-i-f on Github)
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

package com.fifsource.android.resolveractivitytweaks;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
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

    private final String mBuildCodeFromXposed = null;
    private static final String LOG_TAG = "ResolverActivityTweaks";

    public RATSettings() {
        // Empty
    }

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
    private static class ReflectInDescriptionBooleanPrefChangeListener implements Preference.OnPreferenceChangeListener {
        private final int    mOnDescriptionResId;
        private final String mOnDescriptionString;
        private final int    mOffDescriptionResId;
        private final String mOffDescriptionString;

        ReflectInDescriptionBooleanPrefChangeListener(int onDescriptionResId, int offDescriptionResId) {
            mOnDescriptionResId = onDescriptionResId;
            mOnDescriptionString = null;
            mOffDescriptionResId = offDescriptionResId;
            mOffDescriptionString = null;
        }

        ReflectInDescriptionBooleanPrefChangeListener(String onDescriptionString, int offDescriptionResId) {
            mOnDescriptionResId = -1;
            mOnDescriptionString = onDescriptionString;
            mOffDescriptionResId = offDescriptionResId;
            mOffDescriptionString = null;
        }

        ReflectInDescriptionBooleanPrefChangeListener(int onDescriptionResId, String offDescriptionString) {
            mOnDescriptionResId = onDescriptionResId;
            mOnDescriptionString = null;
            mOffDescriptionResId = -1;
            mOffDescriptionString = offDescriptionString;
        }

        ReflectInDescriptionBooleanPrefChangeListener(String onDescriptionString, String offDescriptionString) {
            mOnDescriptionResId = -1;
            mOnDescriptionString = onDescriptionString;
            mOffDescriptionResId = -1;
            mOffDescriptionString = offDescriptionString;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            setDescriptionString(preference, value);
            return true;
        }
        void setDescriptionString(Preference preference, Object value) {
            if ((Boolean) value) {
                if (mOnDescriptionString != null) {
                    preference.setSummary(mOnDescriptionString);
                } else {
                    preference.setSummary(mOnDescriptionResId);
                }
            } else {
                if (mOffDescriptionString != null) {
                    preference.setSummary(mOffDescriptionString);
                } else {
                    preference.setSummary(mOffDescriptionResId);
                }
            }
        }
    }

    private static class ReflectInDescriptionListChangedListener implements Preference.OnPreferenceChangeListener {
        private final String[] mEntryValuesArray;
        private final String[] mEntryDescriptionArray;

        public ReflectInDescriptionListChangedListener(RATSettings activity, int entryValuesArrayId, int entryDescriptionArrayId) {
            Resources resources = activity.getResources();
            mEntryValuesArray = resources.getStringArray(entryValuesArrayId);
            mEntryDescriptionArray = resources.getStringArray(entryDescriptionArrayId);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            setDescriptionString(preference, value);
            return true;
        }

        void setDescriptionString(Preference preference, Object value) {
            String stringVal = (String) value;
            int sz = mEntryValuesArray.length;
            int idx;
            for (idx = 0; idx < sz; ++idx) {
                if (stringVal.equals(mEntryValuesArray[idx])) {
                    preference.setSummary(mEntryDescriptionArray[idx]);
                    return;
                }
            }
        }
    }

    private static class ToggleHideOnceAlwaysListener extends ReflectInDescriptionBooleanPrefChangeListener {
        private final Preference mDependentPreference;

        ToggleHideOnceAlwaysListener(int onDescriptionResId, int offDescriptionResId, Preference dependentPreference) {
            super(onDescriptionResId, offDescriptionResId);
            mDependentPreference = dependentPreference;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            setDependentPreference((Boolean)value);
            return super.onPreferenceChange(preference, value);
        }

        void setDependentPreference(boolean value) {
            mDependentPreference.setEnabled(value);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        public GeneralPreferenceFragment() {
            // Empty
        }

        private void openBrowserOnClick(Preference pref, final String url) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final RATSettings activity = (RATSettings) getActivity();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(activity, activity.getString(R.string.rat_error_browser_not_found), Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
            });
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Log.i(LOG_TAG, "ResolverActivityTweaks Preferences " + BuildConfig.VERSION_NAME + " (" + BuildConfig.RANDOM_BUILD_CODE + "), git " + BuildConfig.GIT_REVISION);

            final RATSettings activity = (RATSettings) getActivity();
            if (activity == null) {
                // Don't do squat if for whatever reason getActivity() fails.
                Log.e(LOG_TAG, "onCreate(): getActivity() returned null");
                return;
            }

            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(Const.PREFERENCES_NAME);
            addPreferencesFromResource(R.xml.pref_general);

            Preference ratCopyright = findPreference(Const.PREF_RAT_COPYRIGHT);
            ratCopyright.setTitle(ratCopyright.getTitle() + " " + BuildConfig.VERSION_NAME);
            openBrowserOnClick(ratCopyright, activity.getString(R.string.xda_support_thread_link));

            Preference ratBuildCode = findPreference(Const.PREF_RAT_BUILD_CODE);
            ratBuildCode.setSummary(BuildConfig.RANDOM_BUILD_CODE);

            Preference ratGitRevision = findPreference(Const.PREF_RAT_GIT_REVISION);
            ratGitRevision.setSummary(BuildConfig.GIT_REVISION+"\n"+ratGitRevision.getSummary());
            openBrowserOnClick(ratGitRevision, activity.getString(R.string.github_project_link));

            openBrowserOnClick(findPreference(Const.PREF_RAT_LICENSE), activity.getString(R.string.license_link));

            PackageInfo xposedInstPackageTry = null;
            String xposedInstNameTry = null;
            final PackageManager pm = activity.getPackageManager();
            if (pm == null) {
                Log.e(LOG_TAG, "onCreate(): getPackageManager() returned null");
            } else {
                for (Const.XposedModuleManagerPackageName managerInfo : Const.XPOSED_INSTALLER_PACKAGE_NAMES) {
                    try {
                        xposedInstPackageTry = pm.getPackageInfo(managerInfo.packageName, 0);
                        xposedInstNameTry = managerInfo.appName;
                        break;
                    } catch (PackageManager.NameNotFoundException e) {
                        // Nothing
                    }
                }
            }
            final PackageInfo xposedInstPackage = xposedInstPackageTry;
            final String xposedInstName = xposedInstNameTry;

            ArrayList<Preference> sectionsToRemove = new ArrayList<>();

            if (xposedInstPackage == null) {
                sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_XPOSED_INACTIVE));
                sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_XPOSED_MISMATCH));
                sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_XPOSED));
                sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_LAUNCHER));

                Preference uninstallPref = findPreference(Const.PREF_RAT_NO_XPOSED_UNINSTALL);
                uninstallPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Uri packageURI = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                        try {
                            startActivity(uninstallIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(activity, activity.getString(R.string.rat_error_pm_not_found), Toast.LENGTH_LONG).show();
                        }
                        return true;
                    }
                });
            } else //noinspection ConstantConditions
                if (activity.mBuildCodeFromXposed == null) {
                    sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_NO_XPOSED));
                    sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_XPOSED_MISMATCH));
                    sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_XPOSED));

                    Preference inactiveXposedSummaryPref = findPreference(Const.PREF_RAT_XPOSED_INACTIVE_SUMMARY);
                    inactiveXposedSummaryPref.setSummary(String.format(activity.getString(R.string.rat_xposed_inactive_summary), xposedInstName));

                    Preference activateModulePref = findPreference(Const.PREF_RAT_XPOSED_INACTIVE_ACTIVATE);
                    activateModulePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        boolean launchedOk = false;
                        try {
                            Intent intent = new Intent(xposedInstPackage.packageName + Const.XPOSED_INSTALLER_OPEN_SECTION);
                            intent.putExtra("section", "modules");
                            startActivity(intent);
                            launchedOk = true;
                        } catch (ActivityNotFoundException e) {
                            try {
                                Intent intent = pm.getLaunchIntentForPackage(xposedInstPackage.packageName);
                                if (intent != null) {
                                    startActivity(intent);
                                    launchedOk = true;
                                }
                            } catch (ActivityNotFoundException e2) {
                                // Empty
                            }
                        }
                        if ( ! launchedOk ) {
                            Toast.makeText(activity, String.format(activity.getString(R.string.rat_error_launching_xposed_installer),
                                                                   xposedInstName), Toast.LENGTH_LONG).show();
                        }
                        return true;
                    }
                });

            } else if (!activity.mBuildCodeFromXposed.equals(BuildConfig.RANDOM_BUILD_CODE)) {
                sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_NO_XPOSED));
                sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_XPOSED_INACTIVE));
                sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_XPOSED));
            } else {
                sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_NO_XPOSED));
                sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_XPOSED_INACTIVE));
                sectionsToRemove.add(findPreference(Const.PREF_RAT_CATEGORY_XPOSED_MISMATCH));
            }

            Preference ratEnabledPref = findPreference(Const.PREF_RAT_ENABLE);
            Preference hideOnceAlwaysPref = findPreference(Const.PREF_RAT_HIDE_ONCE_ALWAYS);

            SharedPreferences sharedPrefs = ratEnabledPref.getSharedPreferences();
            if (sharedPrefs == null) {
                Log.e(LOG_TAG, "onCreate(): ratEnabledPref.getSharedPreferences() is null");
            }

            ToggleHideOnceAlwaysListener toggleHideOnceAlwaysListener = new ToggleHideOnceAlwaysListener(R.string.rat_enable_description_on, R.string.rat_enable_description_off, hideOnceAlwaysPref);
            ratEnabledPref.setOnPreferenceChangeListener(toggleHideOnceAlwaysListener);
            boolean enableVal = Const.PREF_RAT_ENABLE_DEFAULT;
            if (sharedPrefs != null) {
                enableVal = sharedPrefs.getBoolean(Const.PREF_RAT_ENABLE, Const.PREF_RAT_ENABLE_DEFAULT);
            }
            toggleHideOnceAlwaysListener.setDescriptionString(ratEnabledPref, enableVal);
            toggleHideOnceAlwaysListener.setDependentPreference(enableVal);

            ReflectInDescriptionBooleanPrefChangeListener hideOnceAlwaysChangeListener = new ReflectInDescriptionBooleanPrefChangeListener(R.string.rat_hideAlwaysOnce_description_on, R.string.rat_hideAlwaysOnce_description_off);
            hideOnceAlwaysPref.setOnPreferenceChangeListener(hideOnceAlwaysChangeListener);
            boolean hideOnceAlwaysVal = Const.PREF_RAT_HIDE_ONCE_ALWAYS_DEFAULT;
            if (sharedPrefs != null) {
                hideOnceAlwaysVal = sharedPrefs.getBoolean(Const.PREF_RAT_HIDE_ONCE_ALWAYS, Const.PREF_RAT_HIDE_ONCE_ALWAYS_DEFAULT);
            }
            hideOnceAlwaysChangeListener.setDescriptionString(hideOnceAlwaysPref, hideOnceAlwaysVal);

            Preference showInLauncherPref = findPreference(Const.PREF_RAT_SHOW_LAUNCHER_ICON);
            ReflectInDescriptionBooleanPrefChangeListener showInLauncherPrefChangeListener = new ReflectInDescriptionBooleanPrefChangeListener(R.string.rat_showLauncher_description_on, R.string.rat_showLauncher_description_off) {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    RATSettings activity = (RATSettings)getActivity();
                    if (activity == null) {
                        Log.e(LOG_TAG, "onPreferenceChange(): getActivity() is null");
                    } else {
                        PackageManager pm = activity.getPackageManager();
                        if (pm == null) {
                            Log.e(LOG_TAG, "onPreferenceChange(): getPackageManager() is null");
                        } else {
                            int val;
                            if ((Boolean) value) {
                                val = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                            } else {
                                val = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                            }
                            pm.setComponentEnabledSetting(new ComponentName(activity, BuildConfig.APPLICATION_ID + ".RATSettingsAlias"), val, PackageManager.DONT_KILL_APP);
                        }
                    }
                    return super.onPreferenceChange(preference, value);
                }
            };
            showInLauncherPref.setOnPreferenceChangeListener(showInLauncherPrefChangeListener);
            boolean showInLauncherVal = Const.PREF_RAT_SHOW_LAUNCHER_ICON_DEFAULT;
            if (sharedPrefs != null) {
                showInLauncherVal = sharedPrefs.getBoolean(Const.PREF_RAT_SHOW_LAUNCHER_ICON, Const.PREF_RAT_SHOW_LAUNCHER_ICON_DEFAULT);
            }
            showInLauncherPrefChangeListener.setDescriptionString(showInLauncherPref, showInLauncherVal);

            PreferenceScreen screen = getPreferenceScreen();
            for (Preference aSectionsToRemove : sectionsToRemove) {
                screen.removePreference(aSectionsToRemove);
            }
        }
    }
}
