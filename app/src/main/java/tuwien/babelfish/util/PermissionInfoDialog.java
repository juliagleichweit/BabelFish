/**
 * BabelFish
 * Copyright (C) 2019  Julia Gleichweit
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tuwien.babelfish.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import tuwien.babelfish.R;

/**
 * Utility class to show permission info dialog. Redirects user to settings page of the application
 * if user feedback is positive. Otherwise cancels permission request.
 */
public class PermissionInfoDialog {

    /**
     * Inform user of needed permission and  give shortcut to settings menu
     */
    public static void show(Activity activity, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.title_missing_permission);
        builder.setMessage(msg);
        builder.setCancelable(true);

        // on ok go to settings page
        builder.setPositiveButton(R.string.settings, (dialogInterface, which) -> {
            try {
                dialogInterface.dismiss();

                Intent settingsIntent = new Intent();

                // we want to display the application's settings page
                settingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                settingsIntent.addCategory(Intent.CATEGORY_DEFAULT);
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //identify our app
                settingsIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                //start our intent
                activity.startActivity(settingsIntent);
            } catch (Exception e) {
                Context context = activity.getApplicationContext();
                Toast.makeText(context, activity.getResources().getString(R.string.generic_error), Toast.LENGTH_SHORT).show();
            }
        });
        // automatically creates and immediately shows the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
