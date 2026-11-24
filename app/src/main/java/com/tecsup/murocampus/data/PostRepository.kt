package com.tecsup.murocampus.data

import com.tecsup.murocampus.data.remote.PostApi
import com.tecsup.murocampus.model.Comentario
import com.tecsup.murocampus.model.NuevoPost
import com.tecsup.murocampus.model.Post
import kotlinx.coroutines.CancellationException

interface PostRepository {
    suspend fun listar(): Result<List<Post>>

    suspend fun detalle(id: Int): Result<Post>

    suspend fun comentarios(id: Int): Result<List<Comentario>>

    suspend fun porUsuario(userId: Int): Result<List<Post>>

    suspend fun crear(nuevo: NuevoPost): Result<Post>
}

class PostRepositoryRemote(
    private val api: PostApi
) : PostRepository {
    override suspend fun listar(): Result<List<Post>> = try {
        Result.success(api.listar())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun detalle(id: Int): Result<Post> = try {
        Result.success(api.detalle(id))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun comentarios(id: Int): Result<List<Comentario>> = try {
        Result.success(api.comentarios(id))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun porUsuario(userId: Int): Result<List<Post>> = try {
        Result.success(api.porUsuario(userId))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun crear(nuevo: NuevoPost): Result<Post> = try {
        Result.success(api.crear(nuevo))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
