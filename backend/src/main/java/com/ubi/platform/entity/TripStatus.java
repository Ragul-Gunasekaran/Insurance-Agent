package com.ubi.platform.entity;

/**
 * Lifecycle status of an individual vehicle journey trip session.
 */
public enum TripStatus {
    /** Trip is currently active and awaiting subsequent movement or inactivity timeout. */
    IN_PROGRESS,

    /** Trip has concluded after vehicle movement stopped and inactivity threshold was exceeded. */
    COMPLETED
}
