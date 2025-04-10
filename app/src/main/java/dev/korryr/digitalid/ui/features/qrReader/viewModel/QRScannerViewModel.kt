package dev.korryr.digitalid.ui.features.qrReader.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.korryr.digitalid.ui.features.dataModel.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.korryr.digitalid.ui.features.dataModel.Student
import dev.korryr.digitalid.ui.features.qrReader.domain.useCase.GetStudentDetailsUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val getStudentDetailsUseCase: GetStudentDetailsUseCase
) : ViewModel() {

    private val _studentState = mutableStateOf<Result<Student>>(Result.Loading)
    val studentState: State<Result<Student>> = _studentState

    /**
     * Processes the raw QR code string.
     * The method now supports:
     * 1. A JSON string containing the keys "name", "id", and "image"
     * 2. A direct image URL if the scanned data starts with "http"
     * 3. A pipe-delimited string "studentId|qr_code_data" (legacy format)
     */
    fun scanQRCode(qrRaw: String) {
        viewModelScope.launch {
            when {
                // New JSON-based QR code
                qrRaw.trim().startsWith("{") -> {
                    try {
                        val jsonObj = JSONObject(qrRaw)
                        val studentId = jsonObj.optString("id", "N/A")
                        val studentName = jsonObj.optString("name", "N/A")
                        val studentImage = jsonObj.optString("image", "")

                        // Create student object with available values.
                        val student = Student(
                            studentId = studentId,
                            fullName = studentName,
                            email = "N/A",        // Use defaults or parse additional fields if available.
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
                                _studentState.value = Result.Error(e.localizedMessage ?: "Unknown error")
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
}
