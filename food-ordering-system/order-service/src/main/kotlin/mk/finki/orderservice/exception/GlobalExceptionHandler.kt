package mk.finki.orderservice.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException::class)
    fun handleNotFound(ex: OrderNotFoundException): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex.message ?: "Order not found"))

    /**
     * Fired when e.g. PATCH /orders/{id}/status?status=NOT_A_REAL_STATUS is sent —
     * Spring can't convert the query param to the OrderStatus enum. Without this
     * handler it bubbles up as an unhandled 500 instead of a clean 400.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<Any> {
        val message = "Invalid value '${ex.value}' for parameter '${ex.name}'" +
            (ex.requiredType?.let { if (it.isEnum) ": expected one of ${it.enumConstants.joinToString()}" else "" } ?: "")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(message))
    }

    @ExceptionHandler(InvalidOrderException::class)
    fun handleInvalidOrder(ex: InvalidOrderException): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            mapOf(
                "timestamp" to Instant.now().toString(),
                "error" to ex.message,
                "unavailableItems" to ex.unavailableItems
            )
        )

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(body(ex.message ?: "Conflict"))

    private fun body(message: String): Map<String, Any> =
        mapOf("timestamp" to Instant.now().toString(), "error" to message)
}
