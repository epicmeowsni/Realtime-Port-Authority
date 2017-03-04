package rectangledbmi.com.pittsburghrealtimetracker.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import rectangledbmi.com.pittsburghrealtimetracker.BuildConfig;
import rectangledbmi.com.pittsburghrealtimetracker.R;
import rectangledbmi.com.pittsburghrealtimetracker.model.PatApiService;
import rectangledbmi.com.pittsburghrealtimetracker.model.PatApiServiceImpl;
import rectangledbmi.com.pittsburghrealtimetracker.selection.NotificationMessage;
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route;
import rectangledbmi.com.pittsburghrealtimetracker.ui.about.AboutActivity;
import rectangledbmi.com.pittsburghrealtimetracker.ui.selection.NavigationDrawerFragment;
import rectangledbmi.com.pittsburghrealtimetracker.ui.selection.SelectionFragment;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * This is the main activity of the Realtime Tracker...
 */
public class MainActivity extends AppCompatActivity implements
        SelectionFragment.BusSelectionInteraction {

    private static final String LINES_LAST_UPDATED = "lines_last_updated";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Fragment that contains data from navigation drawer
     */
    private SelectionFragment selectionFragment;

    /**
     * Handles retrieving Port Authority data from the internet
     * @since 78
     */
    private PatApiService patApiService;

    /**
     * Reference to the main activity's Layout.
     *
     * @since 55
     */
    private DrawerLayout mainLayout;

    /**
     * subject to show toast messages.
     * @since 70
     */
    private PublishSubject<NotificationMessage> toastSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        restoreActionBar();
        buildPATAPI();
        setContentView(R.layout.activity_select_transit);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                fragmentManager.findFragmentById(R.id.navigation_drawer);

        // Update pattern cache
        // note, when we move to RxJava 2 this Observable can be replaced with a Maybe
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final long lastUpdated = sp.getLong(LINES_LAST_UPDATED, -1);
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            patApiService.getPatternDataManager()
                    .updatePatternCache(lastUpdated,mNavigationDrawerFragment.getSelectedRoutes())
                    .subscribe(
                        t -> {
                            Timber.d("Writing new lastUpdated %d, replacing %d", t, lastUpdated);
                            Editor editor = sp.edit();
                            editor.putLong(LINES_LAST_UPDATED, t);
                            editor.commit();
                        },
                     Timber::e);;
        }


        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mainLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        selectionFragment = (SelectionFragment) fragmentManager.findFragmentById(R.id.map);

        // create a publish subject for displaying toasts
        toastSubject = PublishSubject.create();
        toastSubject.asObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(toastMessageObserver());

        enableHttpResponseCache();
    }

    /**
     * In addition to destroying the {@link MainActivity}, it will also complete the toast subject.
     * @since 70
     */
    @Override
    protected void onDestroy() {
        toastSubject.onCompleted();
        super.onDestroy();
    }

    /**
     * Builds the PAT API Client from a Rest Adapter
     *
     * @since 46
     */
    private void buildPATAPI() {
        patApiService = new PatApiServiceImpl(
                getString(R.string.api_url),
                BuildConfig.PAT_API_KEY,
                getDatadirectory());
    }

    public PatApiService getPatApiService() {
        return patApiService;
    }

    private void enableHttpResponseCache() {
        try {
            long httpCacheSize = 10485760; // 10 MiB
            File fetch = getExternalCacheDir();
            if (fetch == null) {
                fetch = getCacheDir();
            }
            File httpCacheDir = new File(fetch, "http");
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File.class, long.class)
                    .invoke(null, httpCacheDir, httpCacheSize);
        } catch (Exception httpResponseCacheNotAvailable) {
            Timber.e("HTTP response cache is unavailable.");
        }
    }

    public void restoreActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar);
            } catch (Throwable e) {
                Snackbar.make(mainLayout,
                        "Material Design bugged out on your device. Please report this to the " +
                                "Play Store Email if this pops up.", Snackbar.LENGTH_SHORT).show();
//                Toast.makeText(this, "Material Design bugged out on your device. Please report this to the Play Store Email if this pops up.", Toast.LENGTH_SHORT).show();
            }
        }
        try {
            ActionBar t = getSupportActionBar();
            if (t != null) t.setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Snackbar.make(mainLayout,
                    "Material Design bugged out on your device. Please report this to the " +
                            "Play Store Email if this pops up.", Snackbar.LENGTH_SHORT).show();
        }

    }

    //dunno...
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.select_transit, menu);
//            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles action bar item clicks. The action bar will automatically handle clicks on the
     * home/up button so long as you specify a parent activity in the AndroidManifest.xml.
     *
     * @param item the menu item clicked
     * @return true if the option is found?
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("Running MainActivity's onOptionsItemSelected");
        // TODO: Perhaps we should be using onClick events in the XML like onClickAppDetails()
        if (mNavigationDrawerFragment != null &&
                mNavigationDrawerFragment.getActionBarDrawerToggle() != null &&
                mNavigationDrawerFragment.getActionBarDrawerToggle().onOptionsItemSelected(item)) {
            Timber.d("Hamburger menu selected - will either close or open drawer");
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.action_select_buses && mNavigationDrawerFragment != null) {
            Timber.d("Select Buses in Menu Dropdown clicked - opens the drawer");
            mNavigationDrawerFragment.openDrawer();
            return true;
        } else if (id == R.id.action_about) {
            Timber.d("About Button Clicked. Will open the about page");
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_clear) {
            clearSelection();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void clearSelection() {
        mNavigationDrawerFragment.clearSelection();
        selectionFragment.clearSelection();
    }

    /**
     * <p>Clears out route cache.</p>
     * @since 79
     */
    private void clearCache() {
        File lineInfo = new File(getFilesDir(), "/lineinfo");
        Timber.d("cleared files: %s", lineInfo.getAbsolutePath());
        if (lineInfo.exists()) {
            File[] files = lineInfo.listFiles();
            if (files != null) {
                for (File file : files)
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
            }
        }
        showToast("Cleared route cache. Restart app to take effect.", Toast.LENGTH_SHORT);

    }


    /**
     * Show a toast message.
     * @since 47
     * @param message the message to show as a toast
     * @param duration the duration that the message shows
     */
    @Override
    public void showToast(String message, int duration) {
        if (toastSubject == null) {
            return;
        }
        toastSubject.onNext(NotificationMessage.create(message, duration));
    }

    /**
     * Workaround to show a toast from {@link #toastMessageObserver()}
     * @param message the message to show
     * @param duration the duration of the message
     */
    private void showToastInternal(String message, int duration) {
        Toast.makeText(this, message, duration).show();
    }

    /**
     * {@link Observer} to show a toast using Notification Messages.
     *
     * @since 70
     */
    private Observer<NotificationMessage> toastMessageObserver() {
        return new Observer<NotificationMessage>() {
            @Override
            public void onCompleted() {
                Timber.i("Completed toast observer");
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Toast Observable encountered an error");
            }

            @Override
            public void onNext(NotificationMessage notificationMessage) {
                if (notificationMessage == null) {
                    Timber.d("No notification message was sent");
                } else {
                    Timber.d("printing toast message: %s", notificationMessage.getMessage());
                    // android linter goes crazy if passing a normal int into
                    // Toast.makeText in the line below...... (Why) must use
                    // showToastInternal() to generate a toast as a workaround.
                    showToastInternal(
                            notificationMessage.getMessage(),
                            notificationMessage.getLength());
                }

            }
        };
    }


    public void makeSnackbar(@NonNull String string,
                             int showLength,
                             @NonNull String action,
                             @NonNull View.OnClickListener listener) {
        if (string.length() == 0) return;
        Snackbar.make(mainLayout, string, showLength)
                .setAction(action, listener)
                .show();
    }


    public void onBackPressed() {
        if (!mNavigationDrawerFragment.closeDrawer())
            super.onBackPressed();
    }

    @Override
    public void showOkDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    @NonNull
    public Route getSelectedRoute(String routeNumber) {
        return mNavigationDrawerFragment.getSelectedRoute(routeNumber);
    }


    @Override
    @NonNull
    public Set<String> getSelectedRoutes() {
        return mNavigationDrawerFragment.getSelectedRoutes();
    }

    @Override
    public void openPermissionsPage() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public File getDatadirectory() {
        return getFilesDir();
    }

    @Override
    public Observable<Set<String>> getSelectedRoutesObservable() {
        return mNavigationDrawerFragment.getSelectedRoutesObservable();
    }

    @Override
    public Observable<Route> getToggledRouteObservable() {
        return mNavigationDrawerFragment.getToggledRouteObservable();
    }




    /**
     * Click Event for the
     * {@link rectangledbmi.com.pittsburghrealtimetracker.R.menu#select_transit}'s Detour
     * Information.
     * <p>
     * Maybe we should be moving item item events in {@link #onOptionsItemSelected(MenuItem)} to
     * an onClick like this method.
     *
     * @param item the application details item
     */
    public void onClickDetourInfo(MenuItem item) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.detour_url)));
        startActivity(browserIntent);
    }

    /**
     * Click Event for the
     * {@link rectangledbmi.com.pittsburghrealtimetracker.R.menu#select_transit}'s Application
     * Details.
     * <p>
     * Maybe we should be moving item item events in {@link #onOptionsItemSelected(MenuItem)} to an
     * onClick like this method.
     *
     * @param item the application details item
     */
    public void onClickAppDetails(MenuItem item) {
        openPermissionsPage();
    }

    /**
     * Click event for the
     * {@link rectangledbmi.com.pittsburghrealtimetracker.R.menu#select_transit} to clear cache.
     * @param menuItem the menu item.
     */
    public void onClearCache(MenuItem menuItem) {
        clearCache();
    }

    public void restoreSelection() {
        if (mNavigationDrawerFragment == null) {
            return;
        }
        mNavigationDrawerFragment.reselectRoutes();
    }

}
