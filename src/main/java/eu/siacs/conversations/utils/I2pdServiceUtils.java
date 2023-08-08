package eu.siacs.conversations.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;
import eu.siacs.conversations.R;
import me.drakeet.support.toast.ToastCompat;

import java.util.List;

public class I2pdServiceUtils {

    private static final String URI_I2PD = "org.purplei2p.i2pd";
    private static final String URI_I2P = "net.i2p.android.router";
    private static final Uri I2PD_FDROID_URI = Uri.parse("market://details?id=" + URI_I2PD);
    private static final String ACTIVITY_START_I2PD = "org.purplei2p.i2pd.I2PDPermsAskerActivity";
    private static final String ACTION_START_I2P = "net.i2p.android.router.START_I2P";
    public static final Intent INSTALL_INTENT = new Intent(Intent.ACTION_VIEW, I2PD_FDROID_URI);
    public static final Intent LAUNCH_I2P_INTENT = new Intent(ACTION_START_I2P);

    public static boolean isI2pdInstalled(final Context context) {
        try {
            context.getPackageManager().getPackageInfo(URI_I2PD, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (final PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isI2pInstalled(final Context context) {
        try {
            context.getPackageManager().getPackageInfo(URI_I2P, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (final PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void downloadI2pd(Activity activity, int requestCode) {
        try {
            activity.startActivityForResult(INSTALL_INTENT, requestCode);
        } catch (final ActivityNotFoundException e) {
            ToastCompat.makeText(
                            activity, R.string.no_market_app_installed, ToastCompat.LENGTH_SHORT)
                    .show();
        }
    }

    public static void startI2pd(final Activity activity, final int requestCode) {
        Intent launchI2pdIntent = getLaunchIntentI2pd();
        try {
            activity.startActivityForResult(launchI2pdIntent, requestCode);
        } catch (final ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.install_i2p_, Toast.LENGTH_LONG).show();
        }
    }

    public static Intent getLaunchIntentI2pd() {
        Intent launchI2pdIntent = new Intent();
        launchI2pdIntent.setComponent(new ComponentName(URI_I2PD, ACTIVITY_START_I2PD));
        return launchI2pdIntent;
    }

    public static void startI2p(final Activity activity, final int requestCode) {
        try {
            activity.startActivityForResult(LAUNCH_I2P_INTENT, requestCode);
        } catch (final ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.install_i2p_, Toast.LENGTH_LONG).show();
        }
    }
}
