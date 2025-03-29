package dev.korryr.digitalid.ui.features.dataModel

import com.google.gson.annotations.SerializedName

data class Student(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("department") val department: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("qr_code_data") val qrCodeData: String
)

// Sealed class for handling API responses
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}