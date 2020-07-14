package com.yfbx.loader

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import kotlin.concurrent.thread

/**
 * Author: Edward
 * Date: 2020-07-11
 * Description:系统下载器
 */
object FileLoader {

    fun download(context: Context, url: String, fileName: String, progress: (Long) -> Unit) {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            //通知栏标题
            setTitle(fileName)
            //保存位置
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            //通知栏是否可见
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }
        val id = manager.enqueue(request)

        val handler = Handler(Looper.getMainLooper())
        //查询下载进度
        thread {
            while (true) {
                val cursor = manager.query(DownloadManager.Query().setFilterById(id))
                cursor.moveToFirst()
                val loaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val percent = loaded * 100 / total
                cursor.close()
                handler.post { progress.invoke(percent) }
                if (loaded == total) {
                    break
                }
            }
        }
    }
}