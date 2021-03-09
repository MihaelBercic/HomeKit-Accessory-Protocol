package homekit.structure.data

class Pairing(val identifier: String, val publicKey: ByteArray, var isAdmin: Boolean)