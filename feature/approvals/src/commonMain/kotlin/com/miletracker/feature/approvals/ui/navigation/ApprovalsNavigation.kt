package com.miletracker.feature.approvals.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.miletracker.feature.approvals.ui.screens.ApprovalDetailsScreen
import com.miletracker.feature.approvals.ui.screens.ApprovalsScreen
import androidx.savedstate.read

object ApprovalsRoutes {
    const val HOME = "approvals_home"
    const val DETAIL = "approval_detail/{id}"

    fun detail(id: String) = "approval_detail/$id"
}

fun NavGraphBuilder.approvalsGraph(navController: NavHostController) {
    composable(ApprovalsRoutes.HOME) {
        ApprovalsScreen(
            onOpenDetail = { id -> navController.navigate(ApprovalsRoutes.detail(id)) }
        )
    }

    composable(
        route = ApprovalsRoutes.DETAIL,
        arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) { backStack ->
        val id = backStack.arguments?.read { getStringOrNull("id") } ?: return@composable
        ApprovalDetailsScreen(
            approvalId = id,
            onBack = { navController.popBackStack() }
        )
    }
}
