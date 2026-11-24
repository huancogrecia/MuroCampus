package com.tecsup.murocampus

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.murocampus.data.PostRepositoryRemote
import com.tecsup.murocampus.data.remote.RetrofitClient
import com.tecsup.murocampus.ui.PantallaLista
import com.tecsup.murocampus.ui.PantallaDetalle
import com.tecsup.murocampus.ui.PantallaCrear
import com.tecsup.murocampus.ui.Cargando
import com.tecsup.murocampus.ui.ErrorConReintento
import com.tecsup.murocampus.ui.theme.MuroCampus_HuancoTheme
import com.tecsup.murocampus.viewmodel.PostsViewModel
import com.tecsup.murocampus.viewmodel.PostsViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ViewModelProvider(
            this,
            PostsViewModelFactory(PostRepositoryRemote(RetrofitClient.api))
        )[PostsViewModel::class.java]

        setContent {
            MuroCampus_HuancoTheme {
                val detalle by viewModel.detalle.collectAsStateWithLifecycle()
                val idSeleccionado by viewModel.idSeleccionado.collectAsStateWithLifecycle()
                val mostrarFormulario by viewModel.mostrarFormulario.collectAsStateWithLifecycle()

                BackHandler(enabled = idSeleccionado != null || mostrarFormulario) {
                    if (idSeleccionado != null) viewModel.cerrarDetalle()
                    else viewModel.cerrarFormulario()
                }

                if (mostrarFormulario) {
                    PantallaCrear(vm = viewModel, onAtras = viewModel::cerrarFormulario)
                } else if (idSeleccionado == null) {
                    PantallaLista(
                        vm = viewModel,
                        onAbrirDetalle = { id ->
                            viewModel.abrir(id)
                        },
                        onCrear = viewModel::abrirFormulario
                    )
                } else {
                    when (val estado = detalle) {
                        null, com.tecsup.murocampus.viewmodel.UiState.Cargando -> Cargando()
                        is com.tecsup.murocampus.viewmodel.UiState.Error -> ErrorConReintento(
                            mensaje = estado.mensaje,
                            onReintentar = { viewModel.abrir(idSeleccionado!!) }
                        )
                        is com.tecsup.murocampus.viewmodel.UiState.Exito -> PantallaDetalle(
                            detalle = estado.datos,
                            onAtras = viewModel::cerrarDetalle
                        )
                    }
                }
            }
        }
    }
}
