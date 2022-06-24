package br.dev.emiliano.mobile.trackerlocalization

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties

data class LocationLogging(var Latitude: Double? = 0.0, var Longitude: Double? = 0.0) {
}