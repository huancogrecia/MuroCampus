package com.tecsup.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.murocampus.viewmodel.DetalleUi
import com.tecsup.murocampus.viewmodel.PostsViewModel
import com.tecsup.murocampus.viewmodel.UiState

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalle(
    vm: PostsViewModel,
    detalle: DetalleUi,
    onAtras: () -> Unit
) {
    val eliminacionState by vm.eliminacionState.collectAsStateWithLifecycle()
    val eliminando = eliminacionState is UiState.Cargando
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("MuroCampus") },
                navigationIcon = {
                    TextButton(onClick = {
                        vm.limpiarEstadoEliminacion()
                        onAtras()
                    }) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = detalle.post.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(detalle.post.body, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = { mostrarConfirmacion = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                enabled = !eliminando
            ) {
                if (eliminando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                    Text(" Eliminando...")
                } else {
                    Text("Eliminar publicación")
                }
            }

            when (val estado = eliminacionState) {
                is UiState.Error -> {
                    Text(
                        text = estado.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> Unit
            }

            if (mostrarConfirmacion) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmacion = false },
                    title = { Text("Confirmar eliminación") },
                    text = { Text("¿Estás seguro de que deseas eliminar esta publicación?\n\n(Nota: JSONPlaceholder simula la eliminación y solo se reflejará localmente).") },
                    confirmButton = {
                        Button(
                            onClick = {
                                mostrarConfirmacion = false
                                vm.eliminar(detalle.post.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarConfirmacion = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Text(
                text = "Comentarios (${detalle.comentarios.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            detalle.comentarios.forEach { comentario ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = comentario.email,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(comentario.body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
