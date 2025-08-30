package io.composeflow.ui.settings.appassets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapBoth
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.cloud.storage.GoogleCloudStorageWrapper
import io.composeflow.datastore.LocalAssetSaver
import io.composeflow.di.ServiceLocator
import io.composeflow.model.project.Project
import io.composeflow.model.project.asset.UploadResult
import io.composeflow.repository.ProjectRepository
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class AppAssetsViewModel(
    project: Project,
    val firebaseIdToken: FirebaseIdToken,
    private val ioDispatcher: CoroutineDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.Default
        },
    private val storageWrapper: GoogleCloudStorageWrapper = GoogleCloudStorageWrapper(),
    private val projectRepository: ProjectRepository = ProjectRepository(firebaseIdToken),
    private val localAssetSaver: LocalAssetSaver = LocalAssetSaver(),
) : ViewModel() {
    var project by mutableStateOf(project)
        private set

    private val _uploadResult: MutableStateFlow<UploadResult> =
        MutableStateFlow(UploadResult.NotStarted)
    val uploadResult: StateFlow<UploadResult> = _uploadResult

    fun onChangeAndroidSplashScreenImageIcon(blobInfoWrapper: BlobInfoWrapper?) {
        project.appAssetHolder.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
            blobInfoWrapper
        saveProject()
    }

    fun onChangeAndroidSplashBackgroundColor(color: Color?) {
        project.appAssetHolder.splashScreenInfoHolder.androidSplashScreenBackgroundColor.value =
            color
        saveProject()
    }

    fun onUploadAndroidSplashScreenImageIcon(file: PlatformFile) {
        viewModelScope.launch(ioDispatcher) {
            _uploadResult.value = UploadResult.Uploading
            val result =
                storageWrapper.uploadAsset(
                    userId = firebaseIdToken.user_id,
                    projectId = project.id,
                    file,
                )
            result.mapBoth(
                success = {
                    result.get()?.let { blobInfo ->
                        _uploadResult.value = UploadResult.Success(blobInfo)
                        project.assetHolder.icons.add(blobInfo)
                        localAssetSaver.saveAsset(
                            firebaseIdToken.user_id,
                            project.id,
                            blobInfo,
                        )
                        project.appAssetHolder.splashScreenInfoHolder.androidSplashScreenImageBlobInfo.value =
                            blobInfo
                        saveProject()
                    }
                },
                failure = {
                    _uploadResult.value = UploadResult.Failure(it.message, it.cause)
                },
            )
        }
    }

    fun onChangeIosSplashBackgroundColor(color: Color?) {
        project.appAssetHolder.splashScreenInfoHolder.iOSSplashScreenBackgroundColor.value =
            color
        saveProject()
    }

    fun onChangeIosSplashScreenImageIcon(blobInfoWrapper: BlobInfoWrapper?) {
        project.appAssetHolder.splashScreenInfoHolder.iOSSplashScreenImageBlobInfo.value =
            blobInfoWrapper
        saveProject()
    }

    fun onUploadIosSplashScreenImageIcon(file: PlatformFile) {
        viewModelScope.launch(ioDispatcher) {
            _uploadResult.value = UploadResult.Uploading
            val result =
                storageWrapper.uploadAsset(
                    userId = firebaseIdToken.user_id,
                    projectId = project.id,
                    file,
                )
            result.mapBoth(
                success = {
                    result.get()?.let { blobInfo ->
                        _uploadResult.value = UploadResult.Success(blobInfo)
                        // Add to images list for iOS
                        project.assetHolder.images.add(blobInfo)
                        localAssetSaver.saveAsset(
                            firebaseIdToken.user_id,
                            project.id,
                            blobInfo,
                        )
                        project.appAssetHolder.splashScreenInfoHolder.iOSSplashScreenImageBlobInfo.value =
                            blobInfo
                        saveProject()
                    }
                },
                failure = {
                    _uploadResult.value = UploadResult.Failure(it.message, it.cause)
                },
            )
        }
    }

    private fun saveProject() {
        viewModelScope.launch {
            projectRepository.updateProject(project)
        }
    }
}
