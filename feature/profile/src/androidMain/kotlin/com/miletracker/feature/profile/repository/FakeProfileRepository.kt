package com.miletracker.feature.profile.repository

import com.miletracker.core.network.model.DemoAccount
import com.miletracker.core.network.model.EmployeeProfile
import com.miletracker.core.network.model.ProfileCompletion
import com.miletracker.core.network.model.UserSession
import com.miletracker.feature.profile.model.ProfileHeader
import com.miletracker.stub.DemoMockData
import com.miletracker.stub.ProfileMockData

/**
 * Offline implementation that maps the mocked [DemoMockData] profile into a [ProfileHeader]
 * and serves the rich profile surfaces straight from [ProfileMockData].
 */
class FakeProfileRepository : ProfileRepository {

    override fun richProfile(): EmployeeProfile = ProfileMockData.primaryProfile()

    override fun completion(): ProfileCompletion = ProfileMockData.completion()

    override fun sessions(): List<UserSession> = ProfileMockData.sessions()

    override fun accounts(): List<DemoAccount> = ProfileMockData.accounts()

    override fun header(): ProfileHeader {
        val profile = DemoMockData.userConfig().profile
        return ProfileHeader(
            name = profile?.name.orEmpty(),
            email = profile?.email.orEmpty(),
            code = profile?.code.orEmpty(),
            tenant = profile?.tenant.orEmpty(),
            initials = initialsFrom(profile?.name.orEmpty()),
        )
    }

    private fun initialsFrom(name: String): String =
        name.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }
}
