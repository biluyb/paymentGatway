package com.safaricom.mpesa.exception;

/**
 * ResourceNotFoundException
 *
 * <p>Thrown when an entity or resource (such as an Account or Transaction)
 * cannot be located by its primary or unique identifier in the database.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified error message.
     *
     * @param message human-readable message describing the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
