package ar.rulosoft.mimanganu.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.navegadores.Navigator;

public class Util {
    public static int n = 0;
    private static NotificationCompat.Builder searchingForUpdatesNotificationBuilder;
    private static NotificationCompat.Builder notificationWithProgressbarBuilder;
    private static NotificationManager notificationManager;

    private Util() {
    }

    private static class LazyHolder {
        private static final Util utilInstance = new Util();
    }

    public static Util getInstance() {
        return LazyHolder.utilInstance;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public ArrayList<String> dirList(String directory) {
        ArrayList<String> list = new ArrayList<>();
        if (directory.length() != 1) {
            list.add("..");
        }
        File dir = new File(directory);
        if (dir.listFiles() != null) {
            for (File child : dir.listFiles()) {
                if (child.isDirectory()) {
                    list.add(child.getName());
                }
            }
        }
        return list;
    }

    public ArrayList<String> imageList(String directory) {
        ArrayList<String> list = new ArrayList<>();
        File dir = new File(directory);
        if (dir.listFiles() != null) {
            for (File child : dir.listFiles()) {
                if (!child.isDirectory()) {
                    if (child.getName().matches(".+?\\.(jpg|bmp|png|jpeg|gif)+"))
                        list.add(child.getName());
                }
            }
        }
        return list;
    }

    public String getLastStringInPath(String path) {
        path = path.substring(0, path.length() - 1);
        int idx = path.lastIndexOf("/");
        return path.substring(idx + 1);
    }

    public String getLastStringInPathDontRemoveLastChar(String path) {
        path = path.substring(0, path.length());
        int idx = path.lastIndexOf("/");
        return path.substring(idx + 1);
    }

    public void showFastSnackBar(String message, View view, Context context) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            if (MainActivity.colors != null)
                snackbar.getView().setBackgroundColor(MainActivity.colors[0]);
            snackbar.show();
        } else {
            if (context != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showSlowSnackBar(String message, View view, Context context) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
            if (MainActivity.colors != null)
                snackbar.getView().setBackgroundColor(MainActivity.colors[0]);
            snackbar.show();
        } else {
            if (context != null) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null) {
            if (fileOrDirectory.isDirectory() && fileOrDirectory.listFiles() != null) {
                if (fileOrDirectory.listFiles().length > 0) {
                    for (File child : fileOrDirectory.listFiles()) {
                        deleteRecursive(child);
                    }
                }
            }
            fileOrDirectory.delete();
        }
    }

    public void deleteEmptyDirectoriesRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null) {
            if (fileOrDirectory.isDirectory() && fileOrDirectory.listFiles() != null) {
                if (fileOrDirectory.listFiles().length > 0) {
                    for (File child : fileOrDirectory.listFiles()) {
                        deleteEmptyDirectoriesRecursive(child);
                    }
                }
            }
            if (fileOrDirectory.isDirectory() && fileOrDirectory.listFiles() != null) {
                if (fileOrDirectory.listFiles().length == 0) {
                    fileOrDirectory.delete();
                    Util.getInstance().changeNotificationWithProgressbar(0, 0, 69, fileOrDirectory.getAbsolutePath(), true);
                }
            }
        }
    }

    public void restartApp(Context context) {
        context.startActivity(context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        System.exit(0);
    }

    /**
     * Calculate size of folder.
     *
     * @param folder your directory to check
     * @return totalSize
     */
    public long dirSize(final File folder) {
        if (folder == null || !folder.exists())
            return 0;
        if (!folder.isDirectory())
            return folder.length();
        final List<File> dirs = new LinkedList<>();
        dirs.add(folder);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists())
                continue;
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0)
                continue;
            for (final File child : listFiles) {
                result += child.length();
                if (child.isDirectory())
                    dirs.add(child);
            }
        }
        return result;
    }

    public void toast(final Context context, final String toast) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (context != null)
                    Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
                else
                    Log.e("Util", "Failed to deliver toast! Context was null. Message was: " + toast);
            }
        });
    }

    public void toast(final Context context, final String toast, final int length) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (context != null)
                    Toast.makeText(context, toast, length).show();
                else
                    Log.e("Util", "Failed to deliver toast! Context was null. Message was: " + toast);
            }
        });
    }

    private int getCorrectIcon() {
        int icon;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon = R.drawable.ic_launcher_white;
        } else {
            icon = R.drawable.ic_launcher;
        }
        return icon;
    }

    public void createNotification(Context context, boolean isPermanent, int id, Intent intent, String contentTitle, String contentText) {
        Notification notification;
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent deleteIntent = new Intent(context, NotificationDeleteIntentReceiver.class);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis() + 1, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setContentTitle(contentTitle);
        notificationBuilder.setContentText(contentText);
        notificationBuilder.setSmallIcon(getCorrectIcon());
        notificationBuilder.setContentIntent(contentPendingIntent);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setDeleteIntent(deletePendingIntent);
        if (MainActivity.pm != null) {
            if (MainActivity.pm.getBoolean("update_sound", false))
                notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        }
        ++n;
        //notificationBuilder.setNumber(n); // don't delete this I need this for debugging ~ xtj9182
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(contentTitle));
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        }
        notificationManager = (NotificationManager) context.getSystemService(MainActivity.NOTIFICATION_SERVICE);

        notification = notificationBuilder.build();
        if (isPermanent) {
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notification.flags = Notification.FLAG_NO_CLEAR;
        } else {
            notification.flags = Notification.FLAG_AUTO_CANCEL;
        }
        if (MainActivity.pm != null) {
            if (MainActivity.pm.getBoolean("update_vibrate", false))
                notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        notificationManager.notify(id, notification);
    }

    public void createSearchingForUpdatesNotification(Context context, int id) {
        Intent cancelIntent = new Intent(context, CancelIntentReceiver.class);
        cancelIntent.putExtra("manga_id", -1);
        cancelIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.putExtra("manga_id", -2);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis() + 1, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        searchingForUpdatesNotificationBuilder = new NotificationCompat.Builder(context);
        searchingForUpdatesNotificationBuilder.setOngoing(true);
        searchingForUpdatesNotificationBuilder.setContentTitle(context.getResources().getString(R.string.searching_for_updates));
        searchingForUpdatesNotificationBuilder.setContentText("");
        searchingForUpdatesNotificationBuilder.setSmallIcon(R.drawable.ic_action_av_reload);
        searchingForUpdatesNotificationBuilder.setContentIntent(contentPendingIntent);
        searchingForUpdatesNotificationBuilder.setAutoCancel(true);
        searchingForUpdatesNotificationBuilder.addAction(R.drawable.ic_action_x_light, context.getResources().getString(R.string.cancel), cancelPendingIntent);
        searchingForUpdatesNotificationBuilder.setGroup("searchingForUpdates");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            searchingForUpdatesNotificationBuilder.setPriority(Notification.PRIORITY_HIGH);
            searchingForUpdatesNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(context.getResources().getString(R.string.searching_for_updates)));
            searchingForUpdatesNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(""));
        }
        searchingForUpdatesNotificationBuilder.setProgress(100, 0, true);

        notificationManager = (NotificationManager) context.getSystemService(MainActivity.NOTIFICATION_SERVICE);
        Notification notification = searchingForUpdatesNotificationBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(id, notification);
    }

    public void changeSearchingForUpdatesNotification(Context context, int max, int progress, int id, String contentTitle, String contentText, boolean ongoing) {
        searchingForUpdatesNotificationBuilder.setContentTitle(contentTitle);
        searchingForUpdatesNotificationBuilder.setContentText(contentText);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            searchingForUpdatesNotificationBuilder.setPriority(Notification.PRIORITY_HIGH);
            searchingForUpdatesNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(contentTitle));
            searchingForUpdatesNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        }
        if (ongoing) {
            searchingForUpdatesNotificationBuilder.setOngoing(true);
            if (progress == max) {
                searchingForUpdatesNotificationBuilder.setProgress(max, progress, true);
                searchingForUpdatesNotificationBuilder.setContentText(context.getResources().getString(R.string.finishing_update));
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    searchingForUpdatesNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getResources().getString(R.string.finishing_update)));
                }
            } else {
                searchingForUpdatesNotificationBuilder.setProgress(max, progress, false);
            }
        } else {
            searchingForUpdatesNotificationBuilder.setOngoing(false);
            searchingForUpdatesNotificationBuilder.setProgress(max, progress, false);
        }
        notificationManager.notify(id, searchingForUpdatesNotificationBuilder.build());
    }

    public void createNotificationWithProgressbar(Context context, int id, String contentTitle, String contentText) {
        Intent defaultIntent = new Intent(context, MainActivity.class);
        defaultIntent.putExtra("manga_id", -2);
        defaultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent defaultPendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), defaultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationWithProgressbarBuilder = new NotificationCompat.Builder(context);
        notificationWithProgressbarBuilder.setOngoing(true);
        notificationWithProgressbarBuilder.setContentTitle(contentTitle);
        notificationWithProgressbarBuilder.setContentText(contentText);
        notificationWithProgressbarBuilder.setSmallIcon(getCorrectIcon());
        notificationWithProgressbarBuilder.setContentIntent(defaultPendingIntent);
        notificationWithProgressbarBuilder.setAutoCancel(true);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationWithProgressbarBuilder.setPriority(Notification.PRIORITY_HIGH);
            notificationWithProgressbarBuilder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(contentTitle));
            notificationWithProgressbarBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        }
        notificationWithProgressbarBuilder.setProgress(100, 0, true);

        notificationManager = (NotificationManager) context.getSystemService(MainActivity.NOTIFICATION_SERVICE);
        Notification notification = notificationWithProgressbarBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(id, notification);
    }

    public void changeNotificationWithProgressbar(int max, int progress, int id, String contentTitle, String contentText, boolean ongoing) {
        notificationWithProgressbarBuilder.setContentTitle(contentTitle);
        notificationWithProgressbarBuilder.setContentText(contentText);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationWithProgressbarBuilder.setPriority(Notification.PRIORITY_HIGH);
            notificationWithProgressbarBuilder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(contentTitle));
            notificationWithProgressbarBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        }
        if (ongoing) {
            notificationWithProgressbarBuilder.setOngoing(true);
            if (progress == max) {
                notificationWithProgressbarBuilder.setProgress(max, progress, true);
            } else {
                notificationWithProgressbarBuilder.setProgress(max, progress, false);
            }
        } else {
            notificationWithProgressbarBuilder.setOngoing(false);
            notificationWithProgressbarBuilder.setProgress(max, progress, false);
        }
        notificationManager.notify(id, notificationWithProgressbarBuilder.build());
    }

    // same as above but without contentTitle
    public void changeNotificationWithProgressbar(int max, int progress, int id, String contentText, boolean ongoing) {
        notificationWithProgressbarBuilder.setContentText(contentText);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationWithProgressbarBuilder.setPriority(Notification.PRIORITY_HIGH);
            notificationWithProgressbarBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
        }
        if (ongoing) {
            notificationWithProgressbarBuilder.setOngoing(true);
            if (progress == max) {
                notificationWithProgressbarBuilder.setProgress(max, progress, true);
            } else {
                notificationWithProgressbarBuilder.setProgress(max, progress, false);
            }
        } else {
            notificationWithProgressbarBuilder.setOngoing(false);
            notificationWithProgressbarBuilder.setProgress(max, progress, false);
        }
        notificationManager.notify(id, notificationWithProgressbarBuilder.build());
    }

    public void cancelNotification(int id) {
        try {
            if (notificationManager != null)
                notificationManager.cancel(id);
        } catch (Exception e) {
            Log.e("Util", "Exception", e);
        }
    }

    public Spanned fromHtml(String source) {
        // https://stackoverflow.com/questions/37904739/html-fromhtml-deprecated-in-android-n
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    public int getGridColumnSizeFromWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float dpWidth = displayMetrics.widthPixels / activity.getResources().getDisplayMetrics().density;
        int columnSize = (int) (dpWidth / 150);
        if (columnSize < 2)
            columnSize = 2;
        else if (columnSize > 6)
            columnSize = 6;
        return columnSize;
    }

    public String toCamelCase(String input) {
        StringBuilder camelCase = new StringBuilder();
        boolean nextCamelCase = true;
        for (char c : input.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '\'') { //Character.isSpaceChar(c)
                nextCamelCase = true;
            } else if (nextCamelCase) {
                c = Character.toTitleCase(c);
                nextCamelCase = false;
            }
            camelCase.append(c);
        }
        return camelCase.toString();
    }

    public String getFirstMatchDefault(String patron, String source, String mDefault) throws Exception {
        Pattern p = Pattern.compile(patron);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        } else {
            return mDefault;
        }
    }

    public void checkAppUpdates(Context context) {
        new CheckForAppUpdates(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class CheckForAppUpdates extends AsyncTask<Void, Integer, Void> {
        private String error = "";
        private Context context;

        CheckForAppUpdates(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
                Navigator.navigator.flushParameter();
                String source = Navigator.navigator.get("https://api.github.com/repos/raulhaag/MiMangaNu/releases/latest");
                int onlineVersionMinor = Integer.parseInt(getFirstMatchDefault("\"tag_name\": \"\\d+\\.(\\d+)\"", source, ""));
                int onlineVersionMajor = Integer.parseInt(getFirstMatchDefault("\"tag_name\": \"(\\d+)\\.\\d+\"", source, ""));
                //String body = getFirstMatchDefault("\"body\": \"(.+?)\"", source, "").replaceAll("\\\\r\\\\n","").trim().replaceAll("  "," ");
                String downloadurl = getFirstMatchDefault("\"browser_download_url\": \"(.+?)\"", source, "");
                String currentVersionTmp = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                int currentVersionMinor = Integer.parseInt(getFirstMatchDefault("\\d+\\.(\\d+)", currentVersionTmp, ""));
                int currentVersionMajor = Integer.parseInt(getFirstMatchDefault("(\\d+)\\.\\d+", currentVersionTmp, ""));
                if (currentVersionMinor != onlineVersionMinor || currentVersionMajor != onlineVersionMajor) {
                    Util.getInstance().createNotification(context, false, (int) System.currentTimeMillis(), new Intent(Intent.ACTION_VIEW, Uri.parse(downloadurl)), context.getString(R.string.app_update), context.getString(R.string.app_name) + " v" + onlineVersionMajor + "." + onlineVersionMinor + " " + context.getString(R.string.is_available));
                    pm.edit().putBoolean("on_latest_app_version", false).apply();
                } else {
                    pm.edit().putBoolean("on_latest_app_version", true).apply();
                    Log.i("Util", "App is up to date. No update necessary");
                }
            } catch (Exception e) {
                Log.e("Util", "checkAppUpdates Exception");
                e.printStackTrace();
                error = Log.getStackTraceString(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!error.isEmpty())
                Log.e("Util", error);
            super.onPostExecute(result);
        }
    }

}
