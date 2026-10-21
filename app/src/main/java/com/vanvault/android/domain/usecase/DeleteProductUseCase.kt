package com.vanvault.android.domain.usecase

import com.vanvault.android.domain.model.Product
import com.vanvault.android.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * DeleteProductUseCase.
 *
 * Elimina un producto por id. Si el producto existe, devuelve un [DeleteOutcome.Deleted]
 * para que la UI ofrezca Undo vía Snackbar (Nielsen #5: "Help users undo errors").
 *
 * Pragmatic tip: ortogonalidad — la UI no necesita saber cómo se restaura.
 */
class DeleteProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    /** Resultado que la UI consume para mostrar el Snackbar Undo. */
    sealed interface DeleteOutcome {
        data class Deleted(val product: Product) : DeleteOutcome
        data class NotFound(val message: String = "Producto no encontrado") : DeleteOutcome
        data class Failure(val message: String) : DeleteOutcome
    }

    /**
     * @param id Firebase key del producto.
     * @return [DeleteOutcome.Deleted] con el producto eliminado, o error descriptivo.
     */
    suspend operator fun invoke(id: String): DeleteOutcome {
        val product = repository.findById(id)
            ?: return DeleteOutcome.NotFound()
        val deleteResult = repository.delete(id)
        return if (deleteResult.isSuccess) {
            DeleteOutcome.Deleted(product)
        } else {
            DeleteOutcome.Failure(deleteResult.exceptionOrNull()?.message ?: "No se pudo eliminar")
        }
    }

    /** Restaura un producto eliminado (usado por la acción "Deshacer"). */
    suspend fun restore(product: Product): kotlin.Result<Unit> = repository.save(product)
}