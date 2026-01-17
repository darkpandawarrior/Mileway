package com.miletracker

import com.miletracker.core.data.model.display.OdometerCaptureResult
import com.miletracker.core.data.model.display.OdometerPurpose
import com.miletracker.core.data.model.network.ExpenseSubmissionResponse
import com.miletracker.core.data.model.network.PolicyViolation
import com.miletracker.core.data.model.network.ViolationSeverity
import com.miletracker.core.data.model.network.SubmissionStatus
import com.miletracker.core.data.model.network.SubmitMilesRequestK
import com.miletracker.core.network.api.MileTrackerNetworkApi
import com.miletracker.feature.tracking.manager.TrackingConfigManager
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import com.miletracker.feature.tracking.repository.TripAttachmentRepository
import com.miletracker.feature.tracking.viewmodel.MileageSubmissionViewModel
import com.miletracker.feature.tracking.viewmodel.SubmissionSheet
import com.miletracker.feature.tracking.viewmodel.SubmissionUiState
import com.miletracker.stub.DemoConfigManager
import com.miletracker.stub.FakeTrackingNetworkApi
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [MileageSubmissionViewModel] covering the submission state machine,
 * form intent reducers, and odometer capture flow.
 *
 * Uses the same fake-at-boundary strategy as [TrackMilesViewModelTest]:
 * - [FakeTrackingNetworkApi] and [DemoConfigManager] are the real offline stubs the app ships.
 * - [SavedTrackRepository] is backed by a fresh [FakeSavedTrackDao] (from TrackMilesViewModelTest).
 * - [TripAttachmentRepository] is a mockk(relaxed) — persisting attachments is fire-and-forget
 *   from the test's perspective; we verify via [pendingReceipts]/[pendingOdoStart] flows.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MileageSubmissionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao = FakeSavedTrackDao()
    private val trackRepo = SavedTrackRepository(dao)
    private val attachmentRepo: TripAttachmentRepository = mockk(relaxed = true)
    private val configManager = TrackingConfigManager(DemoConfigManager())

    private fun viewModel(api: MileTrackerNetworkApi = FakeTrackingNetworkApi()) =
        MileageSubmissionViewModel(
            api = api,
            trackRepository = trackRepo,
            attachmentRepository = attachmentRepo,
            configManager = configManager,
        )

    // ── Initial form state ────────────────────────────────────────────────────

    @Test
    fun `init loads offices and entities from config`() = runTest {
        val vm = viewModel()
        val form = vm.form.value
        assertTrue(form.offices.isNotEmpty(), "Expected offices from DemoConfigManager")
        assertTrue(form.entities.isNotEmpty(), "Expected entities from DemoConfigManager")
    }

    @Test
    fun `init populates two required form fields`() = runTest {
        val vm = viewModel()
        assertEquals(2, vm.form.value.fields.size)
        assertTrue(vm.form.value.fields.all { it.required })
    }

    @Test
    fun `canSubmit is false when required fields are empty`() = runTest {
        val vm = viewModel()
        // No form values filled → remainingRequirements not empty
        assertTrue(!vm.form.value.canSubmit)
    }

    @Test
    fun `canSubmit is true when all required fields and office and entity are filled`() = runTest {
        val vm = viewModel()
        vm.setFormValue("purpose", "Client visit")
        vm.setFormValue("gender", "Male")
        // Office and entity are required per DemoConfigManager
        val office = vm.form.value.offices.first()
        val entity = vm.form.value.entities.first()
        vm.selectOffice(office.code)
        vm.selectEntity(entity.name)
        assertTrue(vm.form.value.canSubmit)
    }

    // ── Form intent reducers ──────────────────────────────────────────────────

    @Test
    fun `setFormValue stores value by field id`() = runTest {
        val vm = viewModel()
        vm.setFormValue("purpose", "Audit visit")
        assertEquals("Audit visit", vm.form.value.values["purpose"])
    }

    @Test
    fun `toggleDraft updates saveAsDraft in form`() = runTest {
        val vm = viewModel()
        vm.toggleDraft(true)
        assertTrue(vm.form.value.saveAsDraft)
        vm.toggleDraft(false)
        assertTrue(!vm.form.value.saveAsDraft)
    }

    @Test
    fun `selectOffice stores matching office and closes picker`() = runTest {
        val vm = viewModel()
        val offices = vm.form.value.offices
        val target = offices.first()
        vm.openOfficePicker()
        assertEquals(SubmissionSheet.OFFICE_PICKER, vm.form.value.sheet)

        vm.selectOffice(target.code)
        assertEquals(target.code, vm.form.value.selectedOffice?.code)
        assertEquals(SubmissionSheet.NONE, vm.form.value.sheet)
    }

    @Test
    fun `selectEntity stores matching entity and closes picker`() = runTest {
        val vm = viewModel()
        val entities = vm.form.value.entities
        val target = entities.first()
        vm.openEntityPicker()
        assertEquals(SubmissionSheet.ENTITY_PICKER, vm.form.value.sheet)

        vm.selectEntity(target.name)
        assertEquals(target.name, vm.form.value.selectedEntity?.name)
        assertEquals(SubmissionSheet.NONE, vm.form.value.sheet)
    }

    @Test
    fun `dismissSheet resets active sheet to NONE`() = runTest {
        val vm = viewModel()
        vm.openSubmitConfirm()
        assertEquals(SubmissionSheet.SUBMIT_CONFIRM, vm.form.value.sheet)
        vm.dismissSheet()
        assertEquals(SubmissionSheet.NONE, vm.form.value.sheet)
    }

    // ── Odometer capture ──────────────────────────────────────────────────────

    @Test
    fun `captureOdometerStart stores reading and image uri`() = runTest {
        val vm = viewModel()
        val result = OdometerCaptureResult(
            purpose = OdometerPurpose.START,
            imageUri = "content://start.jpg",
            reading = 45_000,
            isManual = false,
            captureTimeMs = 1_000L
        )
        vm.captureOdometerStart(result)

        val form = vm.form.value
        assertEquals(45_000, form.simulatedStartOdo)
        assertEquals("content://start.jpg", form.odometerStartImageUri)
        assertEquals(1_000L, form.odometerStartCaptureMs)
        assertTrue(!form.isManualStartOdo)
    }

    @Test
    fun `captureOdometerEnd computes odometerKm from start reading`() = runTest {
        val vm = viewModel()
        vm.captureOdometerStart(
            OdometerCaptureResult(OdometerPurpose.START, "uri://start", reading = 50_000, isManual = false, captureTimeMs = 0L)
        )
        vm.captureOdometerEnd(
            OdometerCaptureResult(OdometerPurpose.END, "uri://end", reading = 50_020, isManual = false, captureTimeMs = 1_000L)
        )

        val form = vm.form.value
        assertEquals(50_020, form.simulatedEndOdo)
        assertEquals(20.0, form.smartDistanceOdometerKm, 1e-9)
    }

    // ── Submission state machine ───────────────────────────────────────────────

    @Test
    fun `submit success path emits Success state`() = runTest {
        val vm = viewModel()
        vm.setFormValue("purpose", "Field visit")
        vm.setFormValue("gender", "Female")
        vm.selectOffice(vm.form.value.offices.first().code)
        vm.selectEntity(vm.form.value.entities.first().name)

        vm.submit("route-001", distanceKm = 3.0, vehicleKey = "fourWheelerPetrol", startTime = 0L, endTime = 1L)
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is SubmissionUiState.Success)
        assertNotNull((state as SubmissionUiState.Success).response.transId)
    }

    @Test
    fun `submit policy violation path parks response and shows violation sheet`() = runTest {
        val violatingApi = object : MileTrackerNetworkApi by FakeTrackingNetworkApi() {
            override suspend fun submitMiles(request: SubmitMilesRequestK) =
                ExpenseSubmissionResponse(
                    transId = null,
                    submissionStatus = SubmissionStatus.POLICY_VIOLATION,
                    reimbursableAmount = 0.0,
                    violations = listOf(PolicyViolation(id = "DISTANCE_LIMIT", title = "Distance Limit", message = "Exceeds 500 km cap", severity = ViolationSeverity.VIOLATION))
                )
        }

        val vm = viewModel(api = violatingApi)
        vm.submit("route-002", distanceKm = 600.0, vehicleKey = "fourWheelerPetrol", startTime = 0L, endTime = 1L)
        advanceUntilIdle()

        assertEquals(SubmissionSheet.POLICY_VIOLATION, vm.form.value.sheet)
        assertTrue(vm.form.value.violations.isNotEmpty())
        assertEquals(SubmissionUiState.Idle, vm.state.value)
    }

    @Test
    fun `resolvePolicyAndFinalize transitions to Success after violation acknowledgement`() = runTest {
        val violatingApi = object : MileTrackerNetworkApi by FakeTrackingNetworkApi() {
            override suspend fun submitMiles(request: SubmitMilesRequestK) =
                ExpenseSubmissionResponse(
                    transId = "DEMO-VIOLATION",
                    submissionStatus = SubmissionStatus.POLICY_VIOLATION,
                    reimbursableAmount = 100.0,
                    violations = listOf(PolicyViolation(id = "DISTANCE_LIMIT", title = "Distance Limit", message = "Exceeds cap", severity = ViolationSeverity.VIOLATION))
                )
        }

        val vm = viewModel(api = violatingApi)
        vm.submit("route-003", 600.0, "fourWheelerPetrol", 0L, 1L)
        advanceUntilIdle()

        // Acknowledge violation
        vm.setAskAuthorities(true)
        vm.setViolationNote("Approved by manager")
        vm.resolvePolicyAndFinalize()
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is SubmissionUiState.Success)
    }

    @Test
    fun `addReceipt and removeReceipt update pendingReceipts`() = runTest {
        val vm = viewModel()
        vm.addReceipt("uri://receipt1")
        vm.addReceipt("uri://receipt2")
        assertEquals(2, vm.pendingReceipts.value.size)

        vm.removeReceipt("uri://receipt1")
        assertEquals(1, vm.pendingReceipts.value.size)
        assertEquals("uri://receipt2", vm.pendingReceipts.value.first())
    }

    @Test
    fun `reset clears pending attachments and state`() = runTest {
        val vm = viewModel()
        vm.addReceipt("uri://r1")
        vm.setOdometerStart("uri://odo", "45000")
        vm.submit("route-004", 10.0, "fourWheelerPetrol", 0L, 1L)
        advanceUntilIdle()

        vm.reset()
        assertEquals(SubmissionUiState.Idle, vm.state.value)
        assertTrue(vm.pendingReceipts.value.isEmpty())
        assertNull(vm.pendingOdoStart.value)
    }
}
