package dev.korryr.digitalid.ui.features.qrReader.viewModel

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.korryr.digitalid.BuildConfig
import dev.korryr.digitalid.ui.features.dataModel.Result
import dev.korryr.digitalid.ui.features.dataModel.Student
import dev.korryr.digitalid.ui.features.qrReader.domain.useCase.GetStudentDetailsUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val getStudentDetailsUseCase: GetStudentDetailsUseCase
) : ViewModel() {

    private val _studentState = mutableStateOf<Result<Student>>(Result.Loading)
    val studentState: State<Result<Student>> = _studentState

    /**
     * Processes the raw QR code string.
     * In this implementation, we now also support decrypting QR codes whose content
     * is encrypted using CryptoJS AES in passphrase mode.
     *
     * Expected cases:
     * 1. Encrypted text from CryptoJS (detected by "U2FsdGVk" prefix)
     * 2. A JSON string (plain text)
     * 3. A direct image URL (starting with "http")
     * 4. Legacy format ("studentId|qr_code_data")
     */
    fun scanQRCode(qrRaw: String) {
        viewModelScope.launch {
            _studentState.value = Result.Loading
            when {

                // New branch: if QR starts with "U2FsdGVk", assume it's CryptoJS encrypted
                qrRaw.trim().startsWith("U2FsdGVk") -> {
                    val secretKey =
                        BuildConfig.SECRET_KEY.trim() // Replace this with your secure key (ideally from secure config)
                    try {
                        val decryptedString = decryptCryptoJS(qrRaw, secretKey)
                        if (decryptedString == null) {
                            Log.e("QRScannerViewModel", "Failed to decrypt QR code data.")
                            _studentState.value = Result.Error("Decryption error")
                        } else {
                            // Assume the decrypted string is JSON
                            val jsonObj = JSONObject(decryptedString)

                            // ⏰ Step 1: Extract expiration timestamp
                            val expiresAt = jsonObj.optLong("expiresAt", 0)
                            val currentTime = System.currentTimeMillis()

// ⛔ Step 2: Check if the QR has expired
                            if (currentTime > expiresAt) {
                                Log.e(
                                    "QRScannerViewModel",
                                    "QR Code expired. Current time: $currentTime, Expires at: $expiresAt"
                                )
                                _studentState.value = Result.Error("QR Code has expired.")
                                return@launch
                            }

                            val studentId = jsonObj.optString("id", "N/A")
                            val studentName = jsonObj.optString("name", "N/A")
                            val studentImage = jsonObj.optString("image", "")
                            val studentEmail = jsonObj.optString("email", "N/A")

                            val student = Student(
                                studentId = studentId,
                                fullName = studentName,
                                email = studentEmail,
                                department = "N/A",
                                imageUrl = studentImage,
                                qrCodeData = ""
                            )
                            _studentState.value = Result.Success(student)
                            Log.d("QRScannerViewModel", "Decrypted QR code data: $decryptedString")
                        }
                    } catch (e: Exception) {
                        Log.e("QRScannerViewModel", "Error during decryption", e)
                        _studentState.value =
                            Result.Error("Decryption error: ${e.localizedMessage}")
                    }
                }
                // Case 2: Plain JSON-based QR code
                qrRaw.trim().startsWith("{") -> {
                    try {
                        val jsonObj = JSONObject(qrRaw)
                        val studentId = jsonObj.optString("id", "N/A")
                        val studentName = jsonObj.optString("name", "N/A")
                        val studentImage = jsonObj.optString("image", "")
                        val studentEmail = jsonObj.optString("email", "N/A")

                        // Create student object with available values.
                        val student = Student(
                            studentId = studentId,
                            fullName = studentName,
                            email = studentEmail,       // Use defaults or parse additional fields if available.
                            department = "N/A",
                            imageUrl = studentImage,
                            qrCodeData = ""
                        )
                        _studentState.value = Result.Success(student)
                        Log.d("QRScannerViewModel", "Scanned JSON data: $qrRaw")
                    } catch (e: Exception) {
                        Log.e("QRScannerViewModel", "Error parsing JSON from QR code", e)
                        _studentState.value = Result.Error("Invalid JSON QR Code")
                    }
                }

                // Case 3: Direct image URL.
                // If the scanned data is a direct image URL.
                qrRaw.startsWith("http", ignoreCase = true) -> {
                    val student = Student(
                        studentId = "N/A", // default or placeholder value
                        fullName = "N/A",
                        email = "N/A",
                        department = "N/A",
                        imageUrl = qrRaw,
                        qrCodeData = ""
                    )
                    _studentState.value = Result.Success(student)
                    Log.d("QRScannerViewModel", "Scanned image URL: $qrRaw")
                }
                // Legacy support for "studentId|qr_code_data" format.
                else -> {
                    val parts = qrRaw.split("|")
                    if (parts.size >= 2) {
                        val studentId = parts[0]
                        val qrCodeData = parts[1]
                        Log.d("QRScannerViewModel", "Fetching student details for ID: $studentId")
                        getStudentDetailsUseCase(studentId, qrCodeData)
                            .onEach { result ->
                                Log.d("QRScannerViewModel", "Received result: $result")
                                _studentState.value = result
                            }
                            .catch { e ->
                                Log.e("QRScannerViewModel", "Error fetching student details", e)
                                _studentState.value =
                                    Result.Error(e.localizedMessage ?: "Unknown error")
                            }
                            .launchIn(this)
                    } else {
                        _studentState.value = Result.Error("Invalid QR Code format")
                        Log.e("QRScannerViewModel", "Invalid QR code format: $qrRaw")
                    }
                }
            }
        }
    }

    fun resetScan() {
        _studentState.value = Result.Loading
    }

    // ---------------------------------------------------------
    // Helper: Decrypt a CryptoJS AES passphrase-encrypted string.
    // This function replicates CryptoJS’s default encryption format.
    // ---------------------------------------------------------
    private fun decryptCryptoJS(encrypted: String, passphrase: String): String? {
        return try {
            // Decode Base64 with NO_WRAP to avoid newline issues.
            val encryptedBytes = Base64.decode(encrypted, Base64.NO_WRAP)

            // Debug: Log the total length of the byte array.
            Log.d("DEBUG", "Encrypted bytes length: ${encryptedBytes.size}")

            // "Salted__" header should be the first 8 bytes.
            val saltedHeader = "Salted__".toByteArray(Charsets.UTF_8)
            val header = encryptedBytes.copyOfRange(0, 8)
            Log.d("DEBUG", "Header: ${header.joinToString(separator = " ")}")

            if (!header.contentEquals(saltedHeader)) {
                throw IllegalArgumentException("Invalid salt header")
            }

            // Extract salt (bytes 8 to 16)
            val salt = encryptedBytes.copyOfRange(8, 16)
            Log.d("DEBUG", "Salt: ${salt.joinToString(separator = " ")}")

            // Derive key and IV using EVP_KDF (MD5 based). Expecting a 32-byte key and 16-byte IV.
            val keyIv = evpKDF(passphrase, salt, 32, 16)
            val key = keyIv.first
            val iv = keyIv.second
            Log.d("DEBUG", "Derived Key (hex): ${key.joinToString("") { "%02x".format(it) }}")
            Log.d("DEBUG", "Derived IV (hex): ${iv.joinToString("") { "%02x".format(it) }}")

            // The ciphertext is everything after the first 16 bytes.
            val ciphertext = encryptedBytes.copyOfRange(16, encryptedBytes.size)
            Log.d("DEBUG", "Ciphertext length: ${ciphertext.size}")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKey = SecretKeySpec(key, "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            val decryptedBytes = cipher.doFinal(ciphertext)

            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("decryptCryptoJS", "Decryption error", e)
            null
        }
    }

    // ---------------------------------------------------------
    // Helper: EVP_KDF implementation mimicking CryptoJS
    // ---------------------------------------------------------
    private fun evpKDF(
        pass: String,
        salt: ByteArray,
        keySize: Int,
        ivSize: Int,
        iterations: Int = 1
    ): Pair<ByteArray, ByteArray> {
        val passwordBytes = pass.toByteArray(Charsets.UTF_8)
        val totalSize = keySize + ivSize
        var derived = ByteArray(0)
        var block: ByteArray? = null
        while (derived.size < totalSize) {
            val md5 = MessageDigest.getInstance("MD5")
            if (block != null) {
                md5.update(block)
            }
            md5.update(passwordBytes)
            md5.update(salt)
            block = md5.digest()
            // Additional iterations, if needed
            for (i in 1 until iterations) {
                md5.reset()
                block = md5.digest(block)
            }
            if (block != null) {
                derived += block
            }
        }
        val key = derived.copyOfRange(0, keySize)
        val iv = derived.copyOfRange(keySize, totalSize)
        return Pair(key, iv)
    }
}

