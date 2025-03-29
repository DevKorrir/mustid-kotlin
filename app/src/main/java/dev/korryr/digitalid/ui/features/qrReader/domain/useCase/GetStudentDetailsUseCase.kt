package dev.korryr.digitalid.ui.features.qrReader.domain.useCase

import dev.korryr.digitalid.ui.features.dataModel.Student
import dev.korryr.digitalid.ui.features.qrReader.repo.StudentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import dev.korryr.digitalid.ui.features.dataModel.Result

class GetStudentDetailsUseCase @Inject constructor(
    private val repository: StudentRepository
) {
    operator fun invoke(studentId: String, qrCodeData: String): Flow<Result<Student>> {
        return repository.getStudentDetails(studentId, qrCodeData)
    }
}