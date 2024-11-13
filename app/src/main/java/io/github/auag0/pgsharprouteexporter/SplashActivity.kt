package io.github.auag0.pgsharprouteexporter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    companion object {
        init {
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_MOUNT_MASTER)
                    .setTimeout(10)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        checkRootAccess()
    }

    private fun checkRootAccess() {
        fun checkRoot(): Boolean {
            if (Shell.getShell().isRoot) {
                return true
            } else {
                Shell.getShell().close()
            }
            return Shell.getShell().isRoot
        }

        if (checkRoot()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_required_root)
                .setMessage(R.string.dialog_message_required_root)
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    checkRootAccess()
                }
                .setNegativeButton(R.string.dialog_button_exit) { _, _ ->
                    finish()
                }
                .show()
        }
    }
}