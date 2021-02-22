package homekit.communication.structure

/**
 * Created by Mihael Valentin Berčič
 * on 14/02/2021 at 13:10
 * using IntelliJ IDEA
 */
enum class StatusCodes(val value: Int) {
    Success(0),
    InsufficientPrivileges(-70401),
    UnableToPerform(-70402),
    Busy(-70403),
    ReadOnly(-70404),
    WriteOnly(-70405),
    NotificationNotSupported(-70406),
    OutOfResources(-70407),
    TimedOut(-70408),
    ResourceDoesNotExist(-70409),
    InvalidWrite(-70410),
    InsufficientAuthorization(-70411)
}