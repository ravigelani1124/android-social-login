package com.example.login_demo

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Utility {
    companion object{
        //    TODO: To generate keyhash for facebook for api version 28 or higher

        fun getFaceBookHashKey(context: Context) {
            try {
                val info: PackageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES)

                for (signature in getApplicationSignature(context)) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val hashKey = String(Base64.encode(md.digest(), Base64.DEFAULT))
                    Log.e("Hash Key:= ", hashKey)
                }
            } catch (e: NoSuchAlgorithmException) {
                Log.e("Facebook hash", "printHashKey()", e)
            } catch (e: java.lang.Exception) {
                Log.e("Facebook hash", "printHashKey()", e)
            }

        }
        fun getApplicationSignature(context: Context ): List<String> {
            val signatureList: List<String>
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // New signature
                    val sig = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
                    signatureList = if (sig.hasMultipleSigners()) {
                        // Send all with apkContentsSigners
                        sig.apkContentsSigners.map {
                            val digest = MessageDigest.getInstance("SHA")
                            digest.update(it.toByteArray())
                            bytesToHex(digest.digest())
                        }
                    } else {
                        // Send one with signingCertificateHistory
                        sig.signingCertificateHistory.map {
                            val digest = MessageDigest.getInstance("SHA")
                            digest.update(it.toByteArray())
                            bytesToHex(digest.digest())
                        }
                    }
                } else {
                    val sig = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
                    signatureList = sig.map {
                        val digest = MessageDigest.getInstance("SHA")
                        digest.update(it.toByteArray())
                        bytesToHex(digest.digest())
                    }
                }

                return signatureList
            } catch (e: Exception) {
                // Handle error
            }
            return emptyList()
        }

        fun bytesToHex(bytes: ByteArray): String {
            val hexArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
            val hexChars = CharArray(bytes.size * 2)
            var v: Int
            for (j in bytes.indices) {
                v = bytes[j].toInt() and 0xFF
                hexChars[j * 2] = hexArray[v.ushr(4)]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
            }
            return String(hexChars)
        }
    }

}