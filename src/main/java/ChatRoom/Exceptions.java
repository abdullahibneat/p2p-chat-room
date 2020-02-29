package ChatRoom;

/**
 * Exception for trying to use a port that is already in use.
 */
class PortNotAvailbleException extends Exception {
    public PortNotAvailbleException(String errorMessage) { super(errorMessage); }
}

/**
 * Exception for trying to connect to a non-existing member.
 */
class UnknownMemberException extends Exception {
    public UnknownMemberException(String errorMessage) { super(errorMessage); }
}

/**
 * Exception for an invalid username.
 */
class InvalidUsernameException extends Exception {
    public InvalidUsernameException(String errorMessage) { super(errorMessage); }
}