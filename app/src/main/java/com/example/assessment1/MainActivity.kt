package com.example.assessment1

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.assessment1.model.Recipe
import com.example.assessment1.viewmodel.RecipeViewModel
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                SimpleRecipeApp()
            }
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC5),
            tertiary = Color(0xFF3700B3),
            background = Color.White,
            surface = Color.White
        ),
        content = content
    )
}

@Composable
fun SimpleRecipeApp() {
    val navController = rememberNavController()
    val viewModel: RecipeViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf("home", "categories", "share", "profile")) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") { SplashScreen(navController) }
            composable("home") { HomeScreen(navController) }
            composable("categories") { CategoryListScreen(navController, viewModel) }
            composable(
                "category/{category}",
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: "Pasta"
                CategoryDetailScreen(category, navController, viewModel)
            }
            composable(
                "recipeDetail/{recipeId}",
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                RecipeDetailScreen(recipeId = recipeId, viewModel = viewModel, navController = navController)
            }
            composable("share") { ShareScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(navController: NavHostController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Recipe Tracker",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { navController.navigate("categories") }) {
                Text("Start Tracking")
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        "home" to "Home",
        "categories" to "Categories",
        "share" to "Share",
        "profile" to "Profile"
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { (route, label) ->
            NavigationBarItem(
                icon = {
                    when (route) {
                        "home" -> Icon(Icons.Default.Home, contentDescription = label)
                        "categories" -> Icon(Icons.AutoMirrored.Filled.List, contentDescription = label)
                        "share" -> Icon(Icons.Default.Share, contentDescription = label)
                        "profile" -> Icon(Icons.Default.Person, contentDescription = label)
                        else -> Icon(Icons.Default.Home, contentDescription = label)
                    }
                },
                label = { Text(label) },
                selected = currentRoute == route,
                onClick = { navController.navigate(route) }
            )
        }
    }
}

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val viewModel: RecipeViewModel = viewModel()
    val lastViewed by viewModel.lastViewedRecipe.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopAppBar(title = { Text("Home") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Recently Viewed",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (lastViewed != null) {
                val (recipe, timestamp) = lastViewed!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("recipeDetail/${recipe.id}")
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(recipe.title, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Category: ${recipe.category}", color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Last viewed: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(timestamp))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }
            } else {
                Text("No recipes viewed yet.")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(navController: NavHostController, viewModel: RecipeViewModel) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Recipe Categories") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(viewModel.categories) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { navController.navigate("category/$category") }
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    category: String,
    navController: NavHostController,
    viewModel: RecipeViewModel
) {
    val recipes by viewModel.recipes.collectAsState()
    val filteredRecipes = viewModel.getRecipesByCategory(category)
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Recipe")
            }
        },
        topBar = {
            Column {
                TopAppBar(title = { Text(category) })
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("All", "Contains Gluten", "Gluten Free").forEach { type ->
                        FilterChip(
                            selected = viewModel.filter.value == type,
                            onClick = { viewModel.setFilter(type) },
                            label = { Text(type) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val finalList = when (viewModel.filter.value) {
            "Contains Gluten" -> filteredRecipes.filter { it.hasGluten }
            "Gluten Free" -> filteredRecipes.filter { !it.hasGluten }
            else -> filteredRecipes
        }

        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(finalList) { recipe ->
                RecipeCard(recipe = recipe, navController = navController)
            }
        }

        if (showDialog) {
            AddRecipeDialog(
                category = category,
                onDismiss = { showDialog = false },
                onConfirm = { newRecipe ->
                    viewModel.addRecipe(newRecipe)
                }
            )
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("recipeDetail/${recipe.id}")
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${recipe.title} ${if (recipe.hasGluten) "(Contains Gluten)" else "(Gluten Free)"}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Category: ${recipe.category}", color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ingredients: ${recipe.ingredients.joinToString()}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Steps: ${recipe.steps.joinToString("\n")}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    viewModel: RecipeViewModel,
    navController: NavHostController
) {
    val recipe by remember(recipeId) {
        derivedStateOf { viewModel.getRecipeById(recipeId) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title ?: "Recipe Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        recipe?.let {
            viewModel.setLastViewedRecipe(it)
            LazyColumn(modifier = Modifier.padding(padding)) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Category: ${it.category}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Ingredients:",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        it.ingredients.forEachIndexed { index, ingredient ->
                            Text(
                                text = "${index + 1}. $ingredient",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Steps:",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        it.steps.forEachIndexed { index, step ->
                            Text(
                                text = "${index + 1}. $step",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (it.hasGluten) "Contains Gluten" else "Gluten Free",
                            color = if (it.hasGluten) Color.Red else Color.Green,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Recipe not found")
            }
        }
    }
}

@Composable
fun AddRecipeDialog(
    category: String,
    onDismiss: () -> Unit,
    onConfirm: (Recipe) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }
    var hasGluten by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recipe to $category") },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Recipe Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text("Ingredients (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = steps,
                    onValueChange = { steps = it },
                    label = { Text("Steps (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasGluten,
                        onCheckedChange = { hasGluten = it }
                    )
                    Text("Contains Gluten")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val newRecipe = Recipe(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    imageUrl = imageUrl,
                    ingredients = ingredients.split(","),
                    steps = steps.split(","),
                    hasGluten = hasGluten,
                    category = category
                )
                onConfirm(newRecipe)
                onDismiss()
            }) {
                Text("Confirm Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Share") }) }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            Text("Share Feature Coming Soon", modifier = Modifier.align(Alignment.Center))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            Text("Profile Page", modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    AppTheme {
        HomeScreen(rememberNavController())
    }
}