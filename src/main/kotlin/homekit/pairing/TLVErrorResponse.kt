package homekit.pairing

import homekit.communication.Response
import homekit.tlv.TLVError
import homekit.tlv.TLVItem
import homekit.tlv.TLVPacket
import homekit.tlv.TLVValue

class TLVErrorResponse(state: Byte, error: TLVError) : Response(TLVPacket(TLVItem(TLVValue.State, state), TLVItem(TLVValue.Error, error.code)).toByteArray())