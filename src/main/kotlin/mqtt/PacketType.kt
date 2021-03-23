package mqtt

/**
 * Created by Mihael Valentin Berčič
 * on 21/03/2021 at 20:12
 * using IntelliJ IDEA
 */
enum class PacketType(val value: Int, val variableHeaderRequirement: Requirement, val payloadRequirement: Requirement, val identifierField: Requirement) {
    Reserved(0, Requirement.None, Requirement.None, Requirement.None),
    Connect(1, Requirement.Required, Requirement.Required,Requirement.None),
    ConnectAcknowledgment(2, Requirement.None, Requirement.None, Requirement.None),
    Publish(3, Requirement.Required, Requirement.Optional, Requirement.Optional),
    PublishAcknowledgment(4, Requirement.Required, Requirement.None, Requirement.Required),
    PublishReceived(5, Requirement.Required, Requirement.None, Requirement.Required),
    PublishRelease(6, Requirement.Required, Requirement.None, Requirement.Required),
    PublishComplete(7, Requirement.Required, Requirement.None, Requirement.Required),
    Subscribe(8, Requirement.Required, Requirement.Required, Requirement.Required),
    SubscribeAcknowledgment(9, Requirement.Required, Requirement.Required, Requirement.Required),
    Unsubscribe(10, Requirement.Required, Requirement.Required, Requirement.Required),
    UnsubscribeAcknowledgment(11, Requirement.Required, Requirement.Required, Requirement.Required),
    PingRequest(12, Requirement.None, Requirement.None, Requirement.None),
    PingResponse(13, Requirement.None, Requirement.None, Requirement.None),
    Disconnect(14, Requirement.None, Requirement.None, Requirement.None),
    Authentication(15, Requirement.None, Requirement.None, Requirement.None);
}


enum class Requirement {
    Required,
    Optional,
    None
}