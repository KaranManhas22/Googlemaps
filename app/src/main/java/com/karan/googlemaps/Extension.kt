package com.karan.googlemaps

fun String?.appendIfNotBlank(s:String) = if (this!=null && isNotBlank()) "$this$s" else ""
