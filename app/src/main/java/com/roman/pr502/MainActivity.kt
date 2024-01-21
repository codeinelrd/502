/**
 * Paquete que contiene la implementación de la aplicación para gestionar tareas.
 */
package com.roman.pr502

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Clase principal que representa la actividad principal de la aplicación.
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.DarkGray
            ) {
                TaskManager()
            }
        }
    }
}

/**
 * Función composable que define la interfaz de usuario para la gestión de tareas.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TaskManager() {
    // Estado para el seguimiento de tareas y gestión de diálogos
    var tasks by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var isDialogVisible by remember { mutableStateOf(false) }
    var selectedTaskIndex by remember { mutableStateOf(-1) }
    var isSelectionDialogVisible by remember { mutableStateOf(false) }

    // Diseño de la interfaz de usuario usando Compose
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botones para crear y editar tareas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    isDialogVisible = true
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text("Create Task", color = Color.White)
            }

            Button(
                onClick = {
                    isSelectionDialogVisible = true
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                Text("Edit Task", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diálogo de gestión de tareas
        if (isDialogVisible) {
            ManageTasksDialog(
                tasks = tasks,
                onAddTask = { name, description ->
                    tasks[name] = description
                    isDialogVisible = false
                },
                onEditTask = { name, description ->
                    if (selectedTaskIndex != -1) {
                        tasks[name] = description
                        selectedTaskIndex = -1
                    }
                    isDialogVisible = false
                },
                onDeleteTask = { name ->
                    tasks.remove(name)
                    isDialogVisible = false
                },
                onCancel = {
                    isDialogVisible = false
                    selectedTaskIndex = -1
                },
                selectedIndex = selectedTaskIndex
            )
        }

        // Diálogo de selección de tarea
        if (isSelectionDialogVisible) {
            TaskSelectionDialog(
                tasks = tasks,
                onSelectTask = { index ->
                    selectedTaskIndex = index
                    isSelectionDialogVisible = false
                    isDialogVisible = true
                },
                onCancel = {
                    isSelectionDialogVisible = false
                }
            )
        }
    }
}

/**
 * Diálogo para seleccionar una tarea.
 */
@Composable
fun TaskSelectionDialog(
    tasks: MutableMap<String, String>,
    onSelectTask: (index: Int) -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Edit Task", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de tareas para seleccionar
            TaskList(
                tasks = tasks,
                onTaskClick = { index ->
                    onSelectTask(index)
                },
                onDeleteTask = {},
                selectedIndex = -1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de cancelar
            Button(
                onClick = onCancel,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondary)
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    }
}

/**
 * Diálogo para gestionar tareas (crear, editar, eliminar).
 */
@Composable
fun ManageTasksDialog(
    tasks: MutableMap<String, String>,
    onAddTask: (name: String, description: String) -> Unit,
    onEditTask: (name: String, description: String) -> Unit,
    onDeleteTask: (name: String) -> Unit,
    onCancel: () -> Unit,
    selectedIndex: Int
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Create Task", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de tareas para editar/eliminar
            TaskList(
                tasks = tasks,
                onTaskClick = { index ->
                    onEditTask(tasks.keys.elementAt(index), tasks.values.elementAt(index))
                },
                onDeleteTask = onDeleteTask,
                selectedIndex = selectedIndex
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Formulario para agregar/editar tareas
            TaskForm(
                onAddTask = onAddTask,
                onCancel = onCancel,
                initialTask = if (selectedIndex != -1) {
                    val selectedTask = tasks.keys.elementAt(selectedIndex)
                    Pair(selectedTask, tasks[selectedTask] ?: "")
                } else {
                    null
                },
                onEditTask = onEditTask
            )
        }
    }
}

/**
 * Lista de tareas con opciones para editar/eliminar.
 */
@Composable
fun TaskList(
    tasks: MutableMap<String, String>,
    onTaskClick: (index: Int) -> Unit,
    onDeleteTask: (name: String) -> Unit,
    selectedIndex: Int
) {
    Column {
        // Iterar sobre las tareas
        tasks.keys.forEachIndexed { index, taskName ->
            val isSelected = index == selectedIndex

            // Elemento de lista con opción para editar/eliminar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        onTaskClick(index)
                    }
                    .background(Color.DarkGray),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(taskName, modifier = Modifier.weight(1f), color = Color.White)
                IconButton(
                    onClick = {
                        onDeleteTask(taskName)
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = Color.White)
                }
            }
            Divider()
        }
    }
}

/**
 * Formulario para agregar o editar una tarea.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskForm(
    onAddTask: (name: String, description: String) -> Unit,
    onCancel: () -> Unit,
    initialTask: Pair<String, String>? = null,
    onEditTask: (name: String, description: String) -> Unit
) {
    // Estado para el nombre y descripción de la tarea
    var taskName by remember { mutableStateOf(initialTask?.first ?: "") }
    var taskDescription by remember { mutableStateOf(initialTask?.second ?: "") }

    // Diseño del formulario
    Column {
        OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textStyle = LocalTextStyle.current.copy(color = Color.White)
        )

        OutlinedTextField(
            value = taskDescription,
            onValueChange = { taskDescription = it },
            label = { Text("Task Description", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textStyle = LocalTextStyle.current.copy(color = Color.White)
        )

        // Botones para agregar/editar y cancelar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    if (initialTask != null) {
                        onEditTask(taskName, taskDescription)
                    } else {
                        onAddTask(taskName, taskDescription)
                    }
                    taskName = ""
                    taskDescription = ""
                },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text(if (initialTask != null) "Edit" else "Add", color = Color.White)
            }

            Button(
                onClick = onCancel,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondary)
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    }
}

/**
 * Vista previa de la interfaz de usuario.
 */
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TaskManager()
}
