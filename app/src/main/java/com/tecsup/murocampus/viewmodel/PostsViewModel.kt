package com.tecsup.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.murocampus.data.PostRepository
import com.tecsup.murocampus.model.Comentario
import com.tecsup.murocampus.model.Post
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class PostsViewModel(
    private val repo: PostRepository
) : ViewModel() {
    private val _lista = MutableStateFlow<UiState<List<Post>>>(UiState.Cargando)
    val lista = _lista.asStateFlow()

    private val _usuarioFiltro = MutableStateFlow<Int?>(null)
    val usuarioFiltro = _usuarioFiltro.asStateFlow()

    private var cargaJob: Job? = null

    private val _detalle = MutableStateFlow<UiState<DetalleUi>?>(null)
    val detalle = _detalle.asStateFlow()

    private val _idSeleccionado = MutableStateFlow<Int?>(null)
    val idSeleccionado = _idSeleccionado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        cargaJob?.cancel()
        val userId = _usuarioFiltro.value
        cargaJob = viewModelScope.launch {
            _lista.value = UiState.Cargando
            val resultado = if (userId == null) repo.listar() else repo.porUsuario(userId)
            resultado
                .onSuccess { posts ->
                    _lista.value = UiState.Exito(posts)
                }
                .onFailure { error ->
                    _lista.value = UiState.Error(mensajeDe(error))
                }
        }
    }

    fun filtrar(userId: Int?) {
        _usuarioFiltro.value = userId
        cargar()
    }

    fun abrir(id: Int) {
        _idSeleccionado.value = id
        viewModelScope.launch {
            _detalle.value = UiState.Cargando
            val (post, comentarios) = coroutineScope {
                val postDeferred = async { repo.detalle(id) }
                val comentariosDeferred = async { repo.comentarios(id) }
                postDeferred.await() to comentariosDeferred.await()
            }

            post.fold(
                onSuccess = { publicacion ->
                    comentarios.fold(
                        onSuccess = { listaComentarios ->
                            _detalle.value = UiState.Exito(
                                DetalleUi(publicacion, listaComentarios)
                            )
                        },
                        onFailure = { _detalle.value = UiState.Error(mensajeDe(it)) }
                    )
                },
                onFailure = { _detalle.value = UiState.Error(mensajeDe(it)) }
            )
        }
    }

    fun cerrarDetalle() {
        _idSeleccionado.value = null
    }
}

data class DetalleUi(val post: Post, val comentarios: List<Comentario>)
