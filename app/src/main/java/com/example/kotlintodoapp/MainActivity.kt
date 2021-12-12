package com.example.kotlintodoapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlintodoapp.data.Todo
import com.example.kotlintodoapp.repo.TodoRepo
import com.example.kotlintodoapp.ui.theme.KotlinToDoAppTheme
import com.example.kotlintodoapp.viewmodel.TodosViewModel
import kotlinx.coroutines.flow.asStateFlow
import java.lang.IllegalStateException
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val openDialog by remember { mutableStateOf(false) }
            val navController = rememberNavController()
            val currentRoute = navController.currentBackStackEntryFlow.collectAsState(
                initial = navController.currentBackStackEntry)
            KotlinToDoAppTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(
                     floatingActionButton = {
                         ExtendedFloatingActionButton(
                             onClick = {
                                 when (currentRoute.value?.destination?.route) {
                                     "home" -> { navController.navigate("add_todo") }
                                     "add_todo" -> { navController.navigate("home") }
                                 }
                             },
                             icon = { Icon(Icons.Rounded.Add, contentDescription = "Add") },
                             text = {
                                 when (currentRoute.value?.destination?.route) {
                                     "home" -> Text(text = "Add Todo")
                                     "add_todo" -> Text(text = "View Todos")
                                 }
                             }
                         )
                     }
                ) {
                    NavHost(navController = navController, startDestination = "home" ) {
                        composable("home") {
                            TodoList()
                        }
                        composable("add_todo") {
                            AddTodo()
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SimpleDateFormat")
fun getCurrentDate():String{
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    return sdf.format(Date())
}

@Composable
fun AddTodo() {
    val firestore = FirebaseFirestore.getInstance()
    var title by rememberSaveable {  mutableStateOf("") }
    var content by rememberSaveable {  mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    Scaffold(
//        topBar = {
//            TopAppBar(
//                modifier = Modifier.padding(10.dp),
//                title = { Text(text = "Add new Todo") }
//            )
//        },
        scaffoldState = scaffoldState,
        content = {
            Column(modifier = Modifier.padding(15.dp)) {
                Text(
                    text = "Add new Todo",
                    style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(12.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text(text = "Some title...") },
                    label = { Text(text = "Title") }
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text(text = "Some content about your todo...") },
                    label = { Text(text = "Content") }
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val collection = firestore.collection("todos")
                        val date = getCurrentDate()
                        val todo = Todo(title, content, date, false, false)
                        collection.add(todo)
                            .addOnSuccessListener { documentReference ->
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar("Todo added Ref: ${documentReference.id}")
                                }
                            }
                            .addOnFailureListener { e ->
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar("Failed to add todo")
                                }
                            }
//                              sendTodoDetails(title, content)
                    },
                    content = {
                        Row() {
                            Icon(Icons.Rounded.Save, contentDescription = "Save")
                            Text(text = "Save todo")
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun TodoList(
    todosViewModel: TodosViewModel = viewModel(
        factory = TodoViewModelFactory(TodoRepo())
    )
) {
    when (val todosList = todosViewModel.todoStateFlow.asStateFlow().collectAsState().value) {
        is OnError -> {
            Text(text = "Something went wrong!")
        }

        is OnSuccess -> {
            val listOfTodos = todosList.querySnapshot?.toObjects(Todo::class.java)
//            val gson = Gson()
//            val listOfTodos = gson.fromJson(
//                "[{'todoTitle': 'Kotlin', 'todoContent': 'Create a todo app using Jetpack Compose', 'time': 'December 5, 2021 at 7:45:10 PM UTC+5:30', 'isCompleted': false, 'remaind': false}]",
//                Array<Todo>::class.java
//            ).asList()
            listOfTodos?.let {
                Column {
                    Text(
                        text = "Todo App",
                        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(16.dp)
                    )

                    LazyColumn(modifier = Modifier.fillMaxHeight()) {
                        items(listOfTodos) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                TodoDetails(it)
                            }
                        }
                    }
//                    FloatingActionButton(onClick = { /*TODO*/ }) {
//                        Icon(Icons.Rounded.Add, contentDescription = "Add Todo")
//                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TodoDetails(todo: Todo) {
    var showTodoOptions by remember {
        mutableStateOf(false)
    }
    val completedCheckedState = remember { mutableStateOf(todo.isCompleted) }
    val remainderCheckedState = remember { mutableStateOf(todo.remaind) }

    Column(modifier = Modifier.clickable {
        showTodoOptions = showTodoOptions.not()
    }) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = todo.todoTitle,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
            )
            Text(
                text = todo.time,
                style = TextStyle(fontSize = 14.sp, color = Color.LightGray)
            )
        }

        AnimatedVisibility(visible = showTodoOptions) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = todo.todoContent,
                    style = TextStyle(fontSize = 16.sp, color = Color.Gray),
                    modifier = Modifier.padding(10.dp),
                )
                Row(modifier = Modifier.padding(vertical = 20.dp)) {
                    Checkbox(modifier = Modifier.padding(horizontal = 20.dp), checked = completedCheckedState.value, onCheckedChange = { completedCheckedState.value = it})
                    Icon(Icons.Rounded.Check, contentDescription = "Mark")
                    Text(modifier = Modifier.padding(start = 20.dp), text = "Mark as Completed", style = TextStyle(fontSize = 16.sp, color = Color.LightGray))
                }
                Row(modifier = Modifier.padding(vertical = 20.dp)) {
                    Checkbox(modifier = Modifier.padding(horizontal = 20.dp), checked = remainderCheckedState.value, onCheckedChange = { remainderCheckedState.value = it})
                    Icon(Icons.Rounded.Notifications, contentDescription = "Remainder")
                    Text(modifier = Modifier.padding(start = 20.dp), text = "Remaind Me", style = TextStyle(fontSize = 16.sp, color = Color.LightGray))
                }
            }
        }
    }
}

class TodoViewModelFactory(private val todoRepo: TodoRepo)
    : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(TodosViewModel::class.java)) {
            return TodosViewModel(todoRepo) as T
        }
        throw IllegalStateException()
    }
}