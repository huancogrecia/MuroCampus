package com.tecsup.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.murocampus.data.PostRepository
import com.tecsup.murocampus.data.mensajeDe
import com.tecsup.murocampus.model.Comentario
import com.tecsup.murocampus.model.Post
import com.tecsup.murocampus.model.NuevoPost
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

    private val _busquedaQuery = MutableStateFlow("")
    val busquedaQuery = _busquedaQuery.asStateFlow()

    private var postsObtenidos: List<Post> = emptyList()

    private var cargaJob: Job? = null

    private val _detalle = MutableStateFlow<UiState<DetalleUi>?>(null)
    val detalle = _detalle.asStateFlow()

    private var detalleJob: Job? = null

    private val _idSeleccionado = MutableStateFlow<Int?>(null)
    val idSeleccionado = _idSeleccionado.asStateFlow()

    private val _mostrarFormulario = MutableStateFlow(false)
    val mostrarFormulario = _mostrarFormulario.asStateFlow()

    private val _publicacionCreada = MutableStateFlow<UiState<Post>?>(null)
    val publicacionCreada = _publicacionCreada.asStateFlow()

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
                    postsObtenidos = posts
                    actualizarListaFiltrada()
                }
                .onFailure { error ->
                    _lista.value = UiState.Error(mensajeDe(error))
                }
        }
    }

    private fun actualizarListaFiltrada() {
        val query = _busquedaQuery.value.trim()
        val filtrados = if (query.isEmpty()) {
            postsObtenidos
        } else {
            postsObtenidos.filter { it.title.contains(query, ignoreCase = true) }
        }
        _lista.value = UiState.Exito(filtrados)
    }

    fun buscar(query: String) {
        _busquedaQuery.value = query
        if (_lista.value is UiState.Exito) {
            actualizarListaFiltrada()
        }
    }

    fun filtrar(userId: Int?) {
        _usuarioFiltro.value = userId
        cargar()
    }

    fun abrir(id: Int) {
        detalleJob?.cancel()
        _idSeleccionado.value = id
        detalleJob = viewModelScope.launch {
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
        detalleJob?.cancel()
        detalleJob = null
        _idSeleccionado.value = null
    }

    fun abrirFormulario() {
        _publicacionCreada.value = null
        _mostrarFormulario.value = true
    }

    fun cerrarFormulario() {
        _mostrarFormulario.value = false
    }

    fun publicar(titulo: String, texto: String) {
        viewModelScope.launch {
            _publicacionCreada.value = UiState.Cargando
            repo.crear(NuevoPost(userId = 1, title = titulo, body = texto))
                .onSuccess { post ->
                    _publicacionCreada.value = UiState.Exito(post)
                }
                .onFailure { error ->
                    _publicacionCreada.value = UiState.Error(mensajeDe(error))
                }
        }
    }
}

data class DetalleUi(val post: Post, val comentarios: List<Comentario>)
