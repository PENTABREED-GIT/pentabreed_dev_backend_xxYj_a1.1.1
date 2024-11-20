package com.penta.template.common.exception;

/**
 * 중복 유저 아이디 예외
 */
public class DuplicateUserIdException extends RuntimeException {

    public DuplicateUserIdException() {
        super("이미 존재하는 회원입니다");
    }

    public DuplicateUserIdException(String message) {
        super(message);
    }

    public DuplicateUserIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateUserIdException(Throwable cause) {
        super(cause);
    }

    public DuplicateUserIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
