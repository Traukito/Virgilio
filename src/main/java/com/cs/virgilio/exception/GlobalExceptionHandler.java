package com.cs.virgilio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CodigoNoEncontradoException.class)
    public ResponseEntity<String> handleCodigoNoEncontrado(CodigoNoEncontradoException ex) {
        // Devuelve un mensaje 404 cuando el código no es encontrado
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HawkServiceException.class)
    public ResponseEntity<String> handleHawkServiceException(HawkServiceException ex) {
        // Devuelve un error 500 cuando ocurre un error de servicio
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HawkAuthException.class)
    public ResponseEntity<String> handleHawkAuthException(HawkAuthException ex) {
        // Devuelve un error 401 cuando hay problemas de autenticación
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        // Para cualquier otro error
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CodigoSinHistorialException.class)
    //Para cuanndi no rmcuentra el código del Activo en la API
    public ResponseEntity<String> handleCodigoSinHistorial(CodigoSinHistorialException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

}
