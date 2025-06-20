package com.miletracker.feature.profile.repository

import com.miletracker.feature.profile.model.ProfileHeader

interface ProfileRepository {
    fun header(): ProfileHeader
}
