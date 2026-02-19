package searchengine.exception;

import org.springframework.http.HttpStatus;

public class SiteNotIndexedException extends ApiException{
        public SiteNotIndexedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
