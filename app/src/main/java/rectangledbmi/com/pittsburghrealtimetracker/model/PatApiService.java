package rectangledbmi.com.pittsburghrealtimetracker.model;

import java.util.Collection;
import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.patterns.PatternDataManager;
import rectangledbmi.com.pittsburghrealtimetracker.predictions.response.Prd;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.response.Pt;
import rectangledbmi.com.pittsburghrealtimetracker.patterns.response.Ptr;
import rectangledbmi.com.pittsburghrealtimetracker.vehicles.response.VehicleResponse;
import rx.Observable;
import rx.Single;

/**
 * <p>Service interface to get data from the Port Authority Trueime API and process it to be useable.</p>
 * <p>Created by epicstar on 9/18/16.</p>
 * @author Jeremy Jao
 * @since 78
 */
public interface PatApiService {
    Observable<VehicleResponse> getVehicles(Collection<String> rts);

    Single<List<Prd>> getVehiclePredictions(int id);

    Single<List<Prd>> getStopPredictions(int id, Collection<String> rts);

    Observable<List<Ptr>> getPatterns(String rt);

    PatternDataManager getPatternDataManager();

    @SuppressWarnings("unused")
    Observable<List<Pt>> getStops(String rt);
}
