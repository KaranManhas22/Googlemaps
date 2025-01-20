package com.karan.googlemaps

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class Maps {
    companion object {
        fun getLocationName(context: Context, coordinate: LatLng): String {
            var address = ""
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                geocoder.getFromLocation(coordinate.latitude, coordinate.longitude, 1)!!
                    .firstOrNull()?.apply {
                        for (index in 0 until maxAddressLineIndex) {
                            address += getAddressLine(index).appendIfNotBlank(",")
                        }
                        address += featureName.appendIfNotBlank(",")
                        address += locality.appendIfNotBlank(",")
                        address += adminArea.appendIfNotBlank(",")
                        address += countryName.appendIfNotBlank(",")
                        address += postalCode.appendIfNotBlank(",")
                    }
                address = address.trim().removeSuffix(",")
                if (address.isNotBlank()) {
                    return address
                } else return "Address not found"

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return address
        }
    }
}