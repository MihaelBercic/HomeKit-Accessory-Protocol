package homekit.pairing

import homekit.communication.Response
import homekit.tlv.TLVError
import homekit.tlv.TLVItem
import homekit.tlv.TLVPacket
import homekit.tlv.Tag

class TLVErrorResponse(state: Byte, error: TLVError) : Response(TLVPacket(TLVItem(Tag.State, state), TLVItem(Tag.Error, error.code)).asByteArray)