package homekit.pairing

import homekit.communication.HttpResponse
import homekit.communication.Response
import homekit.tlv.TLVError
import homekit.tlv.TLVItem
import homekit.tlv.TLVPacket
import homekit.tlv.Tag

class TLVErrorResponse(state: Byte, error: TLVError) : HttpResponse(contentType = "application/pairing+tlv8", data = TLVPacket(TLVItem(Tag.State, state), TLVItem(Tag.Error, error.code)).asByteArray)