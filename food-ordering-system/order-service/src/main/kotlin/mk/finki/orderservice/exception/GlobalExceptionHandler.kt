package mk.finki.orderservice.exception

import jakarta.servlet.http.HttpServletRequest
import mk.finki.orderservice.application.MenuItemNotFoundException
import mk.finki.orderservice.application.MenuItemUnavailableException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException::class)
    fun handleNotFound(ex: OrderNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        error(HttpStatus.NOT_FOUND, ex, request)

    @ExceptionHandler(MenuItemNotFoundException::class)
    fun handleMenuItemNotFound(ex: MenuItemNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        error(HttpStatus.NOT_FOUND, ex, request)

    @ExceptionHandler(MenuItemUnavailableException::class)
    fun handleMenuItemUnavailable(ex: MenuItemUnavailableException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        error(HttpStatus.CONFLICT, ex, request)

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val message = "Invalid value '${ex.value}' for parameter '${ex.name}'" +
            (ex.requiredType?.let { if (it.isEnum) ": expected one of ${it.enumConstants.joinToString()}" else "" } ?: "")
        return error(HttpStatus.BAD_REQUEST, message, request)
    }

    @ExceptionHandler(InvalidOrderException::class)
    fun handleInvalidOrder(ex: InvalidOrderException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        error(HttpStatus.BAD_REQUEST, ex, request)

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        error(HttpStatus.CONFLICT, ex, request)

    private fun error(status: HttpStatus, ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        error(status, ex.message ?: status.reasonPhrase, request)

    private fun error(status: HttpStatus, message: String, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
                path = request.requestURI
            )
        )
}
