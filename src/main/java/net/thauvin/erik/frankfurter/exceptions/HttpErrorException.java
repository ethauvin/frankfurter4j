/*
 * HttpErrorException.java
 *
 * Copyright 2025 Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of this project nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.thauvin.erik.frankfurter.exceptions;

import java.io.IOException;
import java.io.Serial;
import java.net.URI;

/**
 * Exception representing HTTP errors with status codes.
 */
public class HttpErrorException extends IOException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int statusCode;
    private final URI uri;

    /**
     * Creates an HttpErrorException with status code, message, and cause.
     *
     * @param statusCode the HTTP status code
     * @param message    the error message
     * @param cause      the underlying cause of the exception
     */
    public HttpErrorException(int statusCode, String message, URI uri, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.uri = uri;
    }

    /**
     * Creates an HttpErrorException with status code and message.
     *
     * @param statusCode the HTTP status code
     * @param message    the error message
     * @param uri        the URI
     */
    public HttpErrorException(int statusCode, String message, URI uri) {
        this(statusCode, message, uri, null);
    }

    /**
     * Gets the HTTP status code.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the URI.
     *
     * @return the URI
     */
    public URI getUri() {
        return uri;
    }
}
