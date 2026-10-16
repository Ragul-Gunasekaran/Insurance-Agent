package com.ubi.platform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Service calculating geospatial distance between consecutive GPS telemetry coordinates
 * using the great-circle Haversine formula.
 *
 * NOTE: Raw smartphone/IoT GPS distance is an approximation based on discrete point sampling
 * and subject to multipath interference and sensor noise. Map-matching against road graphs
 * can be added in future iterations for millimeter precision.
 */
@Component
public class GeoDistanceCalculator {

    private static final Logger log = LoggerFactory.getLogger(GeoDistanceCalculator.class);

    /** Mean radius of the Earth in meters according to WGS-84 ellipsoid standard. */
    public static final double EARTH_RADIUS_METERS = 6371000.0;

    /** Maximum plausible speed in meters per second for automotive travel (~220 km/h = 61.1 m/s). */
    public static final double MAX_PLAUSIBLE_SPEED_MPS = 61.11;

    /**
     * Calculates the great-circle distance between two GPS coordinates in meters.
     * Returns 0.0 if any coordinate is missing, out-of-bounds, or identical.
     */
    public double calculateDistanceMeters(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 0.0;
        }

        // Validate coordinate bounds
        if (lat1 < -90.0 || lat1 > 90.0 || lat2 < -90.0 || lat2 > 90.0 ||
            lon1 < -180.0 || lon1 > 180.0 || lon2 < -180.0 || lon2 > 180.0) {
            log.warn("Invalid coordinate bounds in distance calculation: [{}, {}] to [{}, {}]", lat1, lon1, lat2, lon2);
            return 0.0;
        }

        // Short-circuit identical coordinates
        if (Double.compare(lat1, lat2) == 0 && Double.compare(lon1, lon2) == 0) {
            return 0.0;
        }

        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double sinHalfPhi = Math.sin(deltaPhi / 2.0);
        double sinHalfLambda = Math.sin(deltaLambda / 2.0);

        double a = (sinHalfPhi * sinHalfPhi) +
                   Math.cos(phi1) * Math.cos(phi2) * (sinHalfLambda * sinHalfLambda);

        // Clamp 'a' to [0, 1] to prevent potential NaN from precision rounding
        a = Math.min(1.0, Math.max(0.0, a));

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Calculates distance in kilometers.
     */
    public double calculateDistanceKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        return calculateDistanceMeters(lat1, lon1, lat2, lon2) / 1000.0;
    }

    /**
     * Evaluates whether a calculated GPS distance increment is physically plausible
     * for automotive travel over a given elapsed time window.
     * Prevents GPS multipath drift spikes from artificially inflating policyholder mileage.
     */
    public boolean isPlausibleJump(double distanceMeters, long deltaSeconds) {
        if (distanceMeters < 0.0) {
            return false;
        }
        if (deltaSeconds <= 0) {
            // Instantaneous displacement: anything over 25 meters without time delta is considered sensor drift
            return distanceMeters <= 25.0;
        }

        double impliedSpeedMps = distanceMeters / deltaSeconds;
        return impliedSpeedMps <= MAX_PLAUSIBLE_SPEED_MPS;
    }
}
