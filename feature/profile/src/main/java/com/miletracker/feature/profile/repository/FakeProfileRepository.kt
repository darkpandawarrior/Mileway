package com.miletracker.feature.profile.repository

import com.miletracker.feature.profile.model.ProfileHeader
import com.miletracker.stub.DemoMockData

/**
 * Offline implementation that maps the mocked [DemoMockData] profile into a [ProfileHeader].
 */
class FakeProfileRepository : ProfileRepository {

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
