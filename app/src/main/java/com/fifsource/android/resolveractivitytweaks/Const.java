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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Const {
    static class XposedModuleManagerPackageName {
        final public String packageName;
        final public String appName;
        XposedModuleManagerPackageName(String packageName_, String appName_) {
            packageName = packageName_;
            appName = appName_;
        }
    }

    static final List<XposedModuleManagerPackageName> XPOSED_INSTALLER_PACKAGE_NAMES = Collections.unmodifiableList(new ArrayList<XposedModuleManagerPackageName>() {
        {
            //noinspection SpellCheckingInspection
            add(new XposedModuleManagerPackageName("com.solohsu.android.edxp.manager", "EdXposed Manager"));
            //noinspection SpellCheckingInspection
            add(new XposedModuleManagerPackageName("org.meowcat.edxposed.manager", "EdXposed Installer"));
            //noinspection SpellCheckingInspection
            add(new XposedModuleManagerPackageName("de.robv.android.xposed.installer", "Xposed Installer"));
        }
    });

    static final String XPOSED_INSTALLER_OPEN_SECTION = ".OPEN_SECTION";

    static final String  PREFERENCES_NAME = "prefs";
    static final String  REMOTE_PREFERENCE_AUTHORITY = BuildConfig.APPLICATION_ID + ".preferences";
    static final String  PREF_RAT_COPYRIGHT = "rat_copyright";
    static final String  PREF_RAT_CATEGORY_NO_XPOSED = "rat_category_no_xposed";
    static final String  PREF_RAT_NO_XPOSED_UNINSTALL = "rat_no_xposed_uninstall";
    static final String  PREF_RAT_CATEGORY_XPOSED_INACTIVE = "rat_category_xposed_inactive";
    static final String  PREF_RAT_XPOSED_INACTIVE_ACTIVATE = "rat_xposed_inactive_activate";
    static final String  PREF_RAT_CATEGORY_XPOSED_MISMATCH = "rat_category_xposed_mismatch";
    static final String  PREF_RAT_CATEGORY_XPOSED = "rat_category_xposed";
    static final String  PREF_RAT_ENABLE = "rat_enable";
    static final boolean PREF_RAT_ENABLE_DEFAULT = false;
    static final String  PREF_RAT_HIDE_ONCE_ALWAYS = "rat_hideOnceAlways";
    static final boolean PREF_RAT_HIDE_ONCE_ALWAYS_DEFAULT = false;
    static final String  PREF_RAT_CATEGORY_LAUNCHER = "rat_category_launcher";
    static final String  PREF_RAT_SHOW_LAUNCHER_ICON = "rat_showLauncherIcon";
    static final boolean PREF_RAT_SHOW_LAUNCHER_ICON_DEFAULT = true;
    static final String  PREF_RAT_BUILD_CODE = "rat_build_code";
    static final String  PREF_RAT_GIT_REVISION = "rat_git_revision";
    static final String  PREF_RAT_LICENSE = "rat_license";
}
