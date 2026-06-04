package com.example.androidbtl.data

import java.text.Normalizer

object VietQrConfig {
    const val MBBANK_BIN = "970422"


    const val BANK_ACCOUNT_NUMBER = "0823468986"
    const val BANK_ACCOUNT_NAME = "NGUYEN NGOC HIEP"

    const val BANK_DISPLAY_NAME = "MB Bank"
}

object VietQrGenerator {
    private const val ACQUIRER_ID = "A000000727"
    private const val SERVICE_TRANSFER = "QRIBFTTA"
    private const val MERCHANT_CATEGORY = "5812"
    private const val CURRENCY_VND = "704"
    private const val COUNTRY_VN = "VN"

    /**
     * Sinh payload VietQR theo dạng TLV và gắn CRC16 ở cuối để app ngân hàng quét được.
     */
    fun build(
        bankBin: String,
        accountNo: String,
        accountName: String,
        amount: Long,
        description: String
    ): String {
        val beneficiary = tlv("00", bankBin) + tlv("01", accountNo)
        val merchantAccountInfo = tlv("00", ACQUIRER_ID) +
            tlv("01", beneficiary) +
            tlv("02", SERVICE_TRANSFER)

        val additionalData = tlv("08", sanitize(description))

        val name = sanitize(accountName).take(25)

        // Payload VietQR là chuỗi TLV: mỗi field gồm id + length + value.
        // Thứ tự field ở đây bám theo cấu trúc EMV QR cho chuyển khoản ngân hàng.
        val sb = StringBuilder().apply {
            append(tlv("00", "01"))
            append(tlv("01", "12"))
            append(tlv("38", merchantAccountInfo))
            append(tlv("52", MERCHANT_CATEGORY))
            append(tlv("53", CURRENCY_VND))
            append(tlv("54", amount.toString()))
            append(tlv("58", COUNTRY_VN))
            if (name.isNotBlank()) append(tlv("59", name))
            append(tlv("62", additionalData))
        }

        val withoutCrc = sb.toString() + "6304"
        val crc = crc16Ccitt(withoutCrc)
        return withoutCrc + crc
    }

    private fun tlv(id: String, value: String): String {
        val length = "%02d".format(value.length)
        return id + length + value
    }

    private fun sanitize(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return normalized
            .replace(Regex("\\p{M}+"), "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .uppercase()
    }

    /**
     * CRC16-CCITT là checksum bắt buộc của payload VietQR/EMV QR.
     */
    private fun crc16Ccitt(input: String): String {
        var crc = 0xFFFF
        for (b in input.toByteArray(Charsets.ISO_8859_1)) {
            crc = crc xor ((b.toInt() and 0xFF) shl 8)
            repeat(8) {
                crc = if ((crc and 0x8000) != 0) {
                    ((crc shl 1) xor 0x1021) and 0xFFFF
                } else {
                    (crc shl 1) and 0xFFFF
                }
            }
        }
        return "%04X".format(crc and 0xFFFF)
    }
}
