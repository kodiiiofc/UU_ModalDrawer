package com.kodiiiofc.urbanuniversity.jetpackcompose.modaldrawer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kodiiiofc.urbanuniversity.jetpackcompose.modaldrawer.ui.theme.ModalDrawerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val selectedNote = remember {
                mutableStateOf<NoteItem?>(null)
            }


            val notesList = remember {
                mutableStateListOf<NoteItem>(
                    NoteItem("Заметка №1", "Тут содержимое тестовой заметки"),
                    NoteItem("Заметка №2", "Вторая текстовая заметка"),
                )
            }

            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "MainView"
            ) {
                composable("MainView") {
                    MainView(navController, notesList, selectedNote)
                }
                composable("CreatingNoteView") {
                    CreatingNoteView(navController, notesList)
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    navController: NavController,
    notesList: SnapshotStateList<NoteItem>,
    selectedNote: MutableState<NoteItem?>
) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val showDeleteDialog = remember {
        mutableStateOf(false)
    }
    val deletingItemIndex = remember { mutableStateOf<NoteItem?>(null) }

    DeleteItemConfirmAlertDialog(showDeleteDialog, notesList, selectedNote, deletingItemIndex)

    ModalDrawerTheme {

        ModalNavigationDrawer(
            drawerContent = {
                NavMenu(notesList, selectedNote, drawerState, {
                    showDeleteDialog.value = true
                    deletingItemIndex.value = it
                })
            },
            drawerState = drawerState,
            content = {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(text = selectedNote.value?.title ?: "Заметки")
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    content = {
                                        Icon(Icons.Filled.Menu, "Список заметок")
                                    }
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        deletingItemIndex.value = selectedNote.value
                                        showDeleteDialog.value = true
                                    }) {
                                    Icon(Icons.Default.Delete, "Удалить заметку")
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                navController.navigate("CreatingNoteView")
                            }
                        ) {
                            Row(
                                Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, "Добавить заметку")
                                Text("Новая заметка")
                            }
                        }

                    },

                    ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = selectedNote.value?.content ?: ""
                        )
                    }
                }
            })


    }
}

@Composable
fun NavMenu(
    noteList: SnapshotStateList<NoteItem>,
    selectedItem: MutableState<NoteItem?>,
    drawerState: DrawerState,
    onItemDeleteIconClick: (NoteItem) -> Unit,
) {
    val scope = rememberCoroutineScope()

    ModalDrawerSheet(Modifier.requiredWidth(320.dp)) {
        noteList.forEachIndexed { index, note ->
            NavigationDrawerItem(
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(note.title)
                        IconButton(onClick =
                        {
                            onItemDeleteIconClick(note)
                        }) {
                            Icon(Icons.Default.Delete, "Удалить заметку")
                        }
                    }
                },
                selected = note == selectedItem.value,
                onClick = {
                    selectedItem.value = noteList[index]
                    scope.launch { drawerState.close() }
                }
            )
        }
    }
}

@Composable
fun DeleteItemConfirmAlertDialog(
    showDialog: MutableState<Boolean>,
    noteList: SnapshotStateList<NoteItem>,
    selectedItem: MutableState<NoteItem?>,
    deletingItem: MutableState<NoteItem?>
) {
    if (showDialog.value && deletingItem.value != null) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog.value = false
                        selectedItem.value = null
                        noteList.remove(deletingItem.value!!)
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog.value = false }
                ) {
                    Text("Отмена")
                }
            },
            title = { Text("Удаление элемента") },
            icon = { Icon(Icons.Default.Delete, "Удаление элемента") },
            text = { Text("Вы собираетесь удалить заметку \"${deletingItem.value!!.title}\". Удалить?") },

            )
    }
}

data class NoteItem(
    val title: String,
    val content: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatingNoteView(navController: NavController, noteList: SnapshotStateList<NoteItem>) {
    ModalDrawerTheme {

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        var noteTitle by remember {
            mutableStateOf("")
        }

        var noteContent by remember {
            mutableStateOf("")
        }

        val noteHasContent by
        remember {
            derivedStateOf {
                noteTitle.isNotBlank() && noteContent.isNotBlank()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Создание заметки") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, "Назад")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (noteHasContent) {
                                noteList.add(NoteItem(noteTitle, noteContent))
                                navController.popBackStack()
                            } else scope.launch {
                                snackbarHostState.showSnackbar("Поля заметки не должны быть пустыми")
                            }
                        }) {
                            Icon(Icons.Default.Add, "Создать заметку")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        )
        { innerPadding ->
            Column(
                Modifier
                    .padding(innerPadding)
                    .padding(8.dp)
            ) {
                TextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    placeholder = { Text("Заголовок") }
                )

                TextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    singleLine = false,
                    placeholder = { Text("Текст заметки") }
                )

            }
        }
    }

}