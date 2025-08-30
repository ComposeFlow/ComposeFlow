package io.composeflow.ui.asset

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapBoth
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.cloud.storage.BlobInfoWrapper
import io.composeflow.cloud.storage.GoogleCloudStorageWrapper
import io.composeflow.datastore.LocalAssetSaver
import io.composeflow.di.ServiceLocator
import io.composeflow.model.project.Project
import io.composeflow.model.project.asset.RemoveResult
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

class AssetEditorViewModel(
    private val firebaseIdToken: FirebaseIdToken,
    private val project: Project,
    private val ioDispatcher: CoroutineDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.Default
        },
    private val storageWrapper: GoogleCloudStorageWrapper,
    private val projectRepository: ProjectRepository = ProjectRepository(firebaseIdToken),
    private val localAssetSaver: LocalAssetSaver = LocalAssetSaver(),
) : ViewModel() {
    private val _uploadResult: MutableStateFlow<UploadResult> =
        MutableStateFlow(UploadResult.NotStarted)
    val uploadResult: StateFlow<UploadResult> = _uploadResult

    private val _removeResult: MutableStateFlow<RemoveResult> =
        MutableStateFlow(RemoveResult.NotStarted)
    val removeResult: StateFlow<RemoveResult> = _removeResult

    private val isCloudStorageAvailable: Boolean
        get() = firebaseIdToken is FirebaseIdToken.SignedInToken

    fun onDeleteImageAsset(blobInfoWrapper: BlobInfoWrapper) {
        viewModelScope.launch {
            _removeResult.value = RemoveResult.Removing(blobInfoWrapper)

            if (isCloudStorageAvailable) {
                // For signed-in users: delete from cloud storage
                val result = storageWrapper.deleteFile(fullPath = blobInfoWrapper.blobId.name)
                result.mapBoth(
                    success = {
                        _removeResult.value = RemoveResult.Success(blobInfoWrapper)
                        project.assetHolder.images.remove(blobInfoWrapper)
                        localAssetSaver.deleteAsset(
                            userId = firebaseIdToken.user_id,
                            projectId = project.id,
                            blobInfoWrapper = blobInfoWrapper,
                        )
                        saveProject()
                    },
                    failure = {
                        Logger.e("Failed to delete image asset from cloud storage", it)
                        _removeResult.value = RemoveResult.Failure(it.message, it.cause)
                    },
                )
            } else {
                // For anonymous users: delete only locally
                try {
                    localAssetSaver.deleteAsset(
                        userId = firebaseIdToken.user_id,
                        projectId = project.id,
                        blobInfoWrapper = blobInfoWrapper,
                    )
                    project.assetHolder.images.remove(blobInfoWrapper)
                    saveProject()
                    _removeResult.value = RemoveResult.Success(blobInfoWrapper)
                    Logger.i("Deleted image asset locally for anonymous user")
                } catch (e: Exception) {
                    Logger.e("Failed to delete image asset locally", e)
                    _removeResult.value = RemoveResult.Failure(e.message, e.cause)
                }
            }
        }
    }

    fun onUploadImageFile(file: PlatformFile) {
        viewModelScope.launch(ioDispatcher) {
            _uploadResult.value = UploadResult.Uploading

            if (isCloudStorageAvailable) {
                // For signed-in users: upload to cloud storage
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
                            project.assetHolder.images.add(blobInfo)
                            localAssetSaver.saveAsset(
                                firebaseIdToken.user_id,
                                project.id,
                                blobInfo,
                            )
                            saveProject()
                        }
                    },
                    failure = {
                        Logger.e("Failed to upload image asset to cloud storage", it)
                        _uploadResult.value = UploadResult.Failure(it.message, it.cause)
                    },
                )
            } else {
                // For anonymous users: save only locally
                try {
                    val blobInfo =
                        localAssetSaver.saveAssetLocally(
                            userId = firebaseIdToken.user_id,
                            projectId = project.id,
                            file = file,
                        )
                    project.assetHolder.images.add(blobInfo)
                    saveProject()
                    _uploadResult.value = UploadResult.Success(blobInfo)
                    Logger.i("Saved image asset locally for anonymous user")
                } catch (e: Exception) {
                    Logger.e("Failed to save image asset locally", e)
                    _uploadResult.value = UploadResult.Failure(e.message, e.cause)
                }
            }
        }
    }

    fun onUploadIconFile(file: PlatformFile) {
        viewModelScope.launch(ioDispatcher) {
            _uploadResult.value = UploadResult.Uploading

            if (isCloudStorageAvailable) {
                // For signed-in users: upload to cloud storage
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
                            saveProject()
                        }
                    },
                    failure = {
                        Logger.e("Failed to upload icon asset to cloud storage", it)
                        _uploadResult.value = UploadResult.Failure(it.message, it.cause)
                    },
                )
            } else {
                // For anonymous users: save only locally
                try {
                    val blobInfo =
                        localAssetSaver.saveAssetLocally(
                            userId = firebaseIdToken.user_id,
                            projectId = project.id,
                            file = file,
                        )
                    project.assetHolder.icons.add(blobInfo)
                    saveProject()
                    _uploadResult.value = UploadResult.Success(blobInfo)
                    Logger.i("Saved icon asset locally for anonymous user")
                } catch (e: Exception) {
                    Logger.e("Failed to save icon asset locally", e)
                    _uploadResult.value = UploadResult.Failure(e.message, e.cause)
                }
            }
        }
    }

    fun onDeleteIconAsset(blobInfoWrapper: BlobInfoWrapper) {
        viewModelScope.launch {
            _removeResult.value = RemoveResult.Removing(blobInfoWrapper)

            if (isCloudStorageAvailable) {
                // For signed-in users: delete from cloud storage
                val result = storageWrapper.deleteFile(fullPath = blobInfoWrapper.blobId.name)
                result.mapBoth(
                    success = {
                        _removeResult.value = RemoveResult.Success(blobInfoWrapper)
                        project.assetHolder.icons.remove(blobInfoWrapper)
                        localAssetSaver.deleteAsset(
                            userId = firebaseIdToken.user_id,
                            projectId = project.id,
                            blobInfoWrapper = blobInfoWrapper,
                        )
                        saveProject()
                    },
                    failure = {
                        Logger.e("Failed to delete icon asset from cloud storage", it)
                        _removeResult.value = RemoveResult.Failure(it.message, it.cause)
                    },
                )
            } else {
                // For anonymous users: delete only locally
                try {
                    localAssetSaver.deleteAsset(
                        userId = firebaseIdToken.user_id,
                        projectId = project.id,
                        blobInfoWrapper = blobInfoWrapper,
                    )
                    project.assetHolder.icons.remove(blobInfoWrapper)
                    saveProject()
                    _removeResult.value = RemoveResult.Success(blobInfoWrapper)
                    Logger.i("Deleted icon asset locally for anonymous user")
                } catch (e: Exception) {
                    Logger.e("Failed to delete icon asset locally", e)
                    _removeResult.value = RemoveResult.Failure(e.message, e.cause)
                }
            }
        }
    }

    fun onResetUploadResult() {
        _uploadResult.value = UploadResult.NotStarted
    }

    fun onResetRemoveResult() {
        _removeResult.value = RemoveResult.NotStarted
    }

    private fun saveProject() {
        viewModelScope.launch {
            projectRepository.updateProject(project)
        }
    }
}
