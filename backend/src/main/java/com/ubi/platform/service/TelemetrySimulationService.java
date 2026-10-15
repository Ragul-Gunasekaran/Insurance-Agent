package com.ubi.platform.service;

import com.ubi.platform.dto.TelematicsPointRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service to generate realistic, physics-consistent synthetic driving routes
 * and IoT telematics streams for development, demonstration, and automated testing.
 */
@Service
public class TelemetrySimulationService {

    private static final Logger log = LoggerFactory.getLogger(TelemetrySimulationService.class);

    // Reference starting hub: Tech corridor (Bangalore, 12.9716, 77.5946)
    private static final double BASE_LATITUDE = 12.9716;
    private static final double BASE_LONGITUDE = 77.5946;

    /**
     * Generates a realistic sequence of telemetry samples matching a requested behavioral profile.
     */
    public List<TelematicsPointRequest> generateSimulationRoute(Long vehicleId, String profile, int count) {
        int safeCount = Math.min(Math.max(5, count), 100);
        String safeProfile = (profile == null) ? "SAFE_COMMUTE" : profile.toUpperCase().trim();

        log.info("Generating {} synthetic telemetry points for vehicle {} with profile {}", safeCount, vehicleId, safeProfile);

        List<TelematicsPointRequest> points = new ArrayList<>();
        Instant baseTime = Instant.now().minus(safeCount * 5L, ChronoUnit.SECONDS);

        double curLat = BASE_LATITUDE;
        double curLon = BASE_LONGITUDE;
        double curHeading = 45.0; // Heading North-East
        double fuelLevel = 84.5;
        Random rnd = new Random(vehicleId * 31 + safeCount);

        for (int i = 0; i < safeCount; i++) {
            Instant pointTime = baseTime.plus(i * 5L, ChronoUnit.SECONDS);
            // Advance coordinate incrementally (~30-50m per ping)
            double deltaLat = (rnd.nextDouble() * 0.0003) + 0.0002;
            double deltaLon = (rnd.nextDouble() * 0.0003) + 0.0002;
            curLat += deltaLat;
            curLon += deltaLon;
            curHeading = (curHeading + (rnd.nextDouble() * 10.0 - 5.0) + 360) % 360;
            fuelLevel = Math.max(10.0, fuelLevel - 0.02);

            double speed;
            double accel;
            int rpm;

            switch (safeProfile) {
                case "AGGRESSIVE_DRIVING":
                    // Frequent harsh braking (accel < -0.35G) and rapid bursts (accel > +0.30G)
                    if (i % 4 == 1) {
                        // Rapid acceleration burst
                        speed = 65.0 + rnd.nextDouble() * 25.0;
                        accel = 0.35 + rnd.nextDouble() * 0.18; // +0.35G to +0.53G
                        rpm = 4200 + rnd.nextInt(1200);
                    } else if (i % 4 == 3) {
                        // Harsh braking event
                        speed = 18.0 + rnd.nextDouble() * 10.0;
                        accel = -0.42 - rnd.nextDouble() * 0.22; // -0.42G to -0.64G (Harsh brake!)
                        rpm = 1500 + rnd.nextInt(500);
                    } else {
                        speed = 45.0 + rnd.nextDouble() * 20.0;
                        accel = (rnd.nextDouble() * 0.2) - 0.1;
                        rpm = 2500 + rnd.nextInt(800);
                    }
                    break;

                case "SPEEDING_HIGHWAY":
                    // High-speed expressway run (120 - 155 km/h)
                    speed = 125.0 + rnd.nextDouble() * 28.0;
                    accel = (rnd.nextDouble() * 0.14) - 0.07;
                    rpm = 3400 + rnd.nextInt(900);
                    break;

                case "ANOMALOUS_SENSOR":
                    // Commute with deliberate sensor dropouts / extreme spikes
                    if (i == 2) {
                        // Impossible speed spike (glitch)
                        speed = 340.0;
                        accel = 0.10;
                        rpm = 3000;
                    } else if (i == 6) {
                        // GPS coordinate out of valid bounds
                        speed = 50.0;
                        accel = 0.05;
                        rpm = 2200;
                        curLat = 135.0; // Invalid latitude
                    } else if (i == 10) {
                        // Acceleration spike exceeding physical road limits
                        speed = 40.0;
                        accel = -2.60; // -2.6G sensor dropout
                        rpm = 2000;
                    } else {
                        speed = 35.0 + (rnd.nextDouble() * 25.0);
                        accel = (rnd.nextDouble() * 0.20) - 0.10;
                        rpm = 2000 + rnd.nextInt(600);
                    }
                    break;

                case "SAFE_COMMUTE":
                default:
                    // Smooth, exemplary driving: speeds 30-65 km/h, gentle braking and accel
                    double progress = (double) i / safeCount;
                    if (progress < 0.2) {
                        speed = 15.0 + progress * 150.0;
                        accel = 0.12 + (rnd.nextDouble() * 0.06);
                    } else if (progress > 0.8) {
                        speed = Math.max(0.0, 50.0 - (progress - 0.8) * 200.0);
                        accel = -0.12 - (rnd.nextDouble() * 0.06);
                    } else {
                        speed = 48.0 + (rnd.nextDouble() * 12.0 - 6.0);
                        accel = (rnd.nextDouble() * 0.10) - 0.05;
                    }
                    rpm = 1600 + (int) (speed * 20);
                    break;
            }

            // Rounding for clean precision
            speed = Math.round(speed * 10.0) / 10.0;
            accel = Math.round(accel * 100.0) / 100.0;
            double roundLat = Math.round(curLat * 100000.0) / 100000.0;
            double roundLon = Math.round(curLon * 100000.0) / 100000.0;
            double roundHeading = Math.round(curHeading * 10.0) / 10.0;
            double roundFuel = Math.round(fuelLevel * 10.0) / 10.0;

            points.add(new TelematicsPointRequest(
                    vehicleId,
                    pointTime,
                    roundLat,
                    roundLon,
                    speed,
                    accel,
                    roundHeading,
                    rpm,
                    roundFuel
            ));
        }

        return points;
    }
}
