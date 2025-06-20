package com.miletracker.core.data.model.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Coordinates ──────────────────────────────────────────────────────────────

@Serializable
data class CoordsV2(
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lng") val lng: Double? = null,
    @SerialName("name") val name: String? = null
)

@Serializable
data class CoordsPayload(
    @SerialName("coords") val coords: List<CoordsV2>? = null
)

// ── Vehicles ──────────────────────────────────────────────────────────────────

@Serializable
data class ApprovedVehicle(
    @SerialName("key") val vehicleKey: String? = null,
    @SerialName("value") val vehicleName: String? = null,
    @SerialName("pricing") val vehiclePricing: Double? = null
)

@Serializable
data class PolicyApprovedVehiclesResponse(
    @SerialName("items") val vehicles: List<ApprovedVehicle> = emptyList()
)

@Serializable
data class ApprovedVehiclePricingResponse(
    @SerialName("data") val data: Map<String, Double> = emptyMap()
)

// ── Submission response ───────────────────────────────────────────────────────

@Serializable
data class ExpenseSubmissionResponse(
    @SerialName("status") val status: Int = 0,
    @SerialName("policyViolationsList") val policyViolations: List<PolicyViolationItem>? = emptyList(),
    @SerialName("reimbursableAmount") val reimbursableAmount: Double? = null,
    @SerialName("amount") val amount: Double? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("voucher") val voucher: VoucherInfo? = null,
    @SerialName("distance") val distance: Double = 0.0,
    @SerialName("message") val message: String? = null,
    @SerialName("error") val error: String? = null,
    @SerialName("transId") val transId: String? = null
)

@Serializable
data class PolicyViolationItem(@SerialName("error") val error: String? = null)

@Serializable
data class VoucherInfo(
    @SerialName("transId") val transId: String? = null,
    @SerialName("id") val id: Long? = null,
    @SerialName("purpose") val purpose: String? = null,
    @SerialName("title") val title: String? = null
)

// ── Track status ──────────────────────────────────────────────────────────────

@Serializable
data class TrackMileageStatusResponse(
    @SerialName("statusCode") val statusCode: Int = 0,
    @SerialName("description") val description: String = ""
) {
    fun isActive(): Boolean = statusCode == 200
    fun isCancelledDueToConfig(): Boolean = statusCode == 505
    fun isDeactivatedByUser(): Boolean = statusCode == 504
}

// ── Log Miles services ────────────────────────────────────────────────────────

@Serializable
data class LogMilesServicesResponse(
    @SerialName("services") val services: List<LogMilesServiceDto>? = null
)

@Serializable
data class LogMilesServiceDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("glCode") val glCode: String? = null
)

// ── Log Miles routes ──────────────────────────────────────────────────────────

@Serializable
data class LogMilesRoutesResponse(
    @SerialName("location") val locations: List<LogMilesRouteGroup> = emptyList()
)

@Serializable
data class LogMilesRouteGroup(
    @SerialName("coords") val coords: List<CoordsV2> = emptyList(),
    @SerialName("count") val count: Int = 0
)

// ── Distance ──────────────────────────────────────────────────────────────────

@Serializable
data class DistanceRequestV2(@SerialName("coords") val coords: List<CoordsV2>)

@Serializable
data class DistanceResponseV2(
    @SerialName("distance") val distance: Double = 0.0,
    @SerialName("unit") val unit: String = "km"
)

// ── User config ───────────────────────────────────────────────────────────────

@Serializable
data class UserConfigResponseV2(
    @SerialName("currency") val currency: String = "INR",
    @SerialName("miles") val miles: Boolean = false,
    @SerialName("logMiles") val logMiles: Boolean = false,
    @SerialName("mileage") val mileage: MileageConfig? = null,
    @SerialName("isOdometerMandatory") val isOdometerMandatory: Boolean = false,
    @SerialName("geoCheckIn") val geoCheckIn: Boolean = false,
    @SerialName("autoDiscardTrackMileage") val autoDiscardTrackMileage: Boolean = false,
    @SerialName("calculateDistanceOnBackend") val calculateDistanceOnBackend: Boolean = false,
    @SerialName("trackMilesV2") val trackMilesV2: Boolean = true,
    @SerialName("draftTrackMiles") val draftTrackMiles: Boolean = true,
    @SerialName("multiServiceLogMiles") val multiServiceLogMiles: Boolean = false,
    @SerialName("profile") val profile: UserProfile? = null
)

@Serializable
data class MileageConfig(
    @SerialName("time") val time: Int = 10,
    @SerialName("distance") val distance: Double = 0.0
)

@Serializable
data class UserProfile(
    @SerialName("code") val code: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("tenant") val tenant: String = "",
    @SerialName("currency") val currency: String = "INR"
)

// ── Domain models ─────────────────────────────────────────────────────────────

data class LogMilesService(
    val id: Long,
    val name: String,
    val glCode: String
) {
    fun getDisplayString(): String = "$name ($glCode)"
}
