package edu.feutech.redu

import android.app.Application
import edu.feutech.redu.data.ReduDatabase
import edu.feutech.redu.vlm.ModelDownloadManager

class ReduApp : Application() {
    val database: ReduDatabase by lazy { ReduDatabase.create(this) }
    val modelDownloadManager: ModelDownloadManager by lazy { ModelDownloadManager(this) }
}
