package com.tecsup.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.murocampus.model.Post
import com.tecsup.murocampus.viewmodel.PostsViewModel
import com.tecsup.murocampus.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrear(vm: PostsViewModel, onAtras: () -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    val resultado by vm.publicacionCreada.collectAsStateWithLifecycle()
    val publicando = resultado is UiState.Cargando

    LaunchedEffect(resultado) {
        if (resultado is UiState.Exito<*>) {
            titulo = ""
            contenido = ""
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("MuroCampus") },
                navigationIcon = {
                    TextButton(onClick = onAtras) {
                        Text("← Atrás", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título") },
                singleLine = true,
                enabled = !publicando
            )
            OutlinedTextField(
                value = contenido,
                onValueChange = { contenido = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contenido") },
                minLines = 4,
                enabled = !publicando
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = { vm.publicar(titulo.trim(), contenido.trim()) },
                    enabled = titulo.isNotBlank() && contenido.isNotBlank() && !publicando
                ) {
                    if (publicando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(" Publicando…")
                    } else {
                        Text("Publicar")
                    }
                }
            }

            when (val estado = resultado) {
                is UiState.Exito -> MensajePublicado(estado.datos)
                is UiState.Error -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { vm.publicar(titulo.trim(), contenido.trim()) }) {
                        Text("Reintentar")
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun MensajePublicado(post: Post) {
    Text(
        text = "Publicado con id ${post.id}",
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleMedium
    )
}
