package com.example.geocal.services

import android.location.Location
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

class SupabaseService {
    /*
        Per ottenere le credenziali di Supabase:
        1. Vai su https://supabase.com
        2. Accedi al tuo account o creane uno nuovo
        3. Crea un nuovo progetto
        4. Nella dashboard del progetto, vai su "Project Settings" -> "API"
        5. Troverai:
           - Project URL (supabaseUrl)
           - anon/public key (supabaseKey)
        6. Sostituisci i valori qui sotto con le tue credenziali
    */
    private val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://ifthywkwrmrumewdgtlp.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlmdGh5d2t3cm1ydW1ld2RndGxwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDM1NzE4MTUsImV4cCI6MjA1OTE0NzgxNX0.CxPQmUYJ9E9YjFS5gIOGdYPzpgSrT-aEfZ_ZvFZzxI0"
    ) {
        install(Postgrest)
    }

    suspend fun saveLocationData(
        eventType: String,
        timestamp: String,
        location: Location
    ) {
        val locationData = mapOf(
            "event_type" to eventType,
            "timestamp" to timestamp,
            "latitude" to location.latitude,
            "longitude" to location.longitude
        )

        client.postgrest["location_data"].insert(locationData)
    }

    suspend fun getLocationHistory(): List<Map<String, Any>> {
        return client.postgrest["location_data"]
            .select()
            .order("timestamp", ascending = false)
            .limit(100)
            .decodeList()
    }
} 