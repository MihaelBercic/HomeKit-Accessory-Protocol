package homekit.pairing

import homekit.communication.Response
import homekit.tlv.structure.TLVError
import homekit.tlv.structure.TLVItem
import homekit.tlv.structure.TLVPacket
import homekit.tlv.structure.TLVValue

class TLVErrorResponse(state: Byte, error: TLVError) : Response(TLVPacket(TLVItem(TLVValue.State, state), TLVItem(TLVValue.Error, error.code)).toByteArray())