package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.*;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /** overlay used to display bus route legend text on a layer above the map */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /** overlays used to plot bus routes */
    private List<Polyline> busRouteOverlays;

    /**
     * Constructor
     * @param context   the application context
     * @param mapView   the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();
    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */
    public void plotRoutes(int zoomLevel) {
        busRouteOverlays.clear();
        busRouteLegendOverlay.clear();
        updateVisibleArea();
        Stop s = StopManager.getInstance().getSelected();
        if (StopManager.getInstance().getSelected() != null) {
            for (Route r : s.getRoutes()) {
                busRouteLegendOverlay.add(r.getNumber());
                for (RoutePattern rp : r.getPatterns()) {
                    List<GeoPoint> logp = new ArrayList<>();
                    LatLon point1 = rp.getPath().get(0);
                    LatLon point2 = rp.getPath().get(1);
                    for (int i = 0; i < rp.getPath().size() - 1; i++) {
                        if (Geometry.rectangleIntersectsLine(northWest, southEast, point1, point2)) {
                            point1 = rp.getPath().get(i);
                            point2 = rp.getPath().get(i++);
                            logp.add(Geometry.gpFromLL(point1));
                            logp.add(Geometry.gpFromLL(point2));
                            Polyline p = new Polyline(context);
                            p.setPoints(logp);
                            p.setWidth(getLineWidth(zoomLevel));
                            p.setColor(busRouteLegendOverlay.getColor(r.getNumber()));
                            busRouteOverlays.add(p);
                        } else {
                            point1 = rp.getPath().get(i);
                            point2 = rp.getPath().get(i++);
                        }
                    }
                }
            }
        }
        //TODO: complete the implementation of this method (Task 7)
    }

    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     * @param zoomLevel   the zoom level of the map
     * @return            width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if(zoomLevel > 14)
            return 7.0f * BusesAreUs.dpiFactor();
        else if(zoomLevel > 10)
            return 5.0f * BusesAreUs.dpiFactor();
        else
            return 2.0f * BusesAreUs.dpiFactor();
    }
}
