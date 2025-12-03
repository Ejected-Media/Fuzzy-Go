package com.ejectedmedia.fuzzygo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ejectedmedia.fuzzygo.ui.screens.ProjectBoardScreen
import com.ejectedmedia.fuzzygo.ui.screens.ProjectListScreen
import com.ejectedmedia.fuzzygo.ui.viewmodel.BoardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "project_list") {
                
                // SCREEN 1: Project Picker
                composable("project_list") {
                    ProjectListScreen(
                        onProjectClick = { projectId ->
                            // Navigate to board with the ID
                            navController.navigate("board/$projectId")
                        }
                    )
                }

                // SCREEN 2: The Board
                composable(
                    route = "board/{projectId}",
                    arguments = listOf(navArgument("projectId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
                    
                    // specific View Model instance for this screen
                    val boardViewModel: BoardViewModel = viewModel()
                    
                    // Trigger the load when we enter this screen
                    LaunchedEffect(projectId) {
                        boardViewModel.loadProject(projectId)
                    }

                    ProjectBoardScreen(viewModel = boardViewModel)
                }
            }
        }
    }
}
