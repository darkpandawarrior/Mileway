package com.miletracker.feature.profile.repository

import com.miletracker.core.network.model.DemoAccount
import com.miletracker.core.network.model.EmployeeProfile
import com.miletracker.core.network.model.ProfileCompletion
import com.miletracker.core.network.model.UserSession
import com.miletracker.feature.profile.model.ProfileHeader
import com.miletracker.stub.ProfileMockData

interface ProfileRepository {
    fun header(): ProfileHeader

    /** Full employee profile for the profile detail screen. */
    fun richProfile(): EmployeeProfile = ProfileMockData.primaryProfile()

    /** Profile-completion checklist (headline percent + per-category counts). */
    fun completion(): ProfileCompletion = ProfileMockData.completion()

    /** Device sessions this account is signed in on. */
    fun sessions(): List<UserSession> = ProfileMockData.sessions()

    /** Accounts the user can switch between. */
    fun accounts(): List<DemoAccount> = ProfileMockData.accounts()
}
