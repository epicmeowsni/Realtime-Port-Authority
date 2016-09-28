package rectangledbmi.com.pittsburghrealtimetracker;

/**
 * Fragment that houses the selection.
 *
 * Created by epicstar on 3/15/16.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;

import java.util.Set;

import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.PATAPI;
import rectangledbmi.com.pittsburghrealtimetracker.selection.RouteSelection;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;
import rx.Observable;

public abstract class SelectionFragment extends Fragment implements NavigationDrawerFragment.BusListCallbacks, ClearSelection {

    /**
     * Interface that interacts with the list of buses in {@link NavigationDrawerFragment}
     * @author Jeremy Jao
     */
    public interface BusSelectionInteraction {

        /**
         * @param routeNumber the route number
         * @return the selected route from the {@link NavigationDrawerFragment}
         */
        Route getSelectedRoute(String routeNumber);

        /**
         * @return the selected routes from the {@link NavigationDrawerFragment}
         */
        Set<String> getSelectedRoutes();

        /**
         * @return The PAT API instantiated in {@link SelectTransit#onCreate(Bundle)}
         */
        PATAPI getPatApiClient();

        /**
         * @return the Rx Observable that the {@link NavigationDrawerFragment} emits list clicks on.
         */
        Observable<RouteSelection> getSelectionSubject();

        void showOkDialog(String message, DialogInterface.OnClickListener okListener);

        void makeSnackbar(@NonNull String string, @Snackbar.Duration int showLength, @Nullable String action, @Nullable View.OnClickListener listener);

        /**
         * Opens the permissions page
         */
        void openPermissionsPage();
    }
}
