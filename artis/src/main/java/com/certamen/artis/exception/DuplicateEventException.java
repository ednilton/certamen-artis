package com.certamen.artis.exception;

/**
 * Exceção lançada quando tentamos registrar um evento que já existe no banco.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
public class DuplicateEventException extends RuntimeException {
    public DuplicateEventException(String message) {
        super(message);
    }
}