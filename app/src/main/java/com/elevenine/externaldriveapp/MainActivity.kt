package com.elevenine.externaldriveapp

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mjdev.libaums.UsbMassStorageDevice
import java.io.File


class MainActivity : AppCompatActivity() {

    private var isUsbDriveConnected = false
    private var connectedDevice: UsbMassStorageDevice? = null

    private var manager: UsbManager? = null
    private var testInfoString = ""

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        // before interacting with a device you need to call init()!
                        connectedDevice?.init()

                        // Only uses the first partition on the device
                        val currentFs = connectedDevice?.partitions?.get(0)?.fileSystem
                        testInfoString += currentFs?.rootDirectory?.absolutePath + "\n"
                        testInfoString += "Capacity: " + currentFs?.capacity + "\n"

                        Log.d(TAG, "Occupied Space: " + currentFs?.occupiedSpace)
                        Log.d(TAG, "Free Space: " + currentFs?.freeSpace)
                        Log.d(TAG, "Chunk size: " + currentFs?.chunkSize)

                        val root = currentFs?.rootDirectory
                        val usbDirectory = root?.search(findViewById<EditText>(R.id.et_search).text.toString())
                        if (usbDirectory == null) {
                            Toast.makeText(this@MainActivity, "Directory was not found!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, usbDirectory.absolutePath + " - " + usbDirectory.name, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d("MainActivity", "permission denied for device $connectedDevice")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get usbDevice when intent-filter for opening this app when a usb drive is connected
        // is called
        connectedDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)



        findViewById<Button>(R.id.btn_usb).setOnClickListener {
            /*checkIfUsbStorageIsConnected()
            if (isUsbDriveConnected)
                Toast.makeText(this, "UsbDrive is connected", Toast.LENGTH_SHORT).show()

            isFileExists()*/

            if (packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
                val devices =
                    UsbMassStorageDevice.getMassStorageDevices(this /* Context or Activity */)

                // just print data for testing
                devices.forEach { entry ->
                    Log.d("MainActivity", entry.usbDevice.deviceName)
                    testInfoString += entry.usbDevice.deviceName + " - " + entry.usbDevice.deviceClass + "\n"

                }
                testInfoString += "device info: " + "\n"

                for (device in devices) {

                    connectedDevice = device

                    manager = getSystemService(USB_SERVICE) as UsbManager
                    registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION))
                    val permissionIntent =
                        PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
                    manager?.requestPermission(device.usbDevice, permissionIntent)
                }

                findViewById<TextView>(R.id.tv_usb).text = testInfoString
            }
        }
    }

    private fun isFileExists() {
        val pixel3_api30_filepath = "/storage/emulated/0/DCIM/asaka_cheque_1614257946330.png"
        val file = File(pixel3_api30_filepath)

        if (file.exists())
            Log.d("MainActivity", "File exists")
        else Log.d("MainActivity", "File is ABSENT")
    }

    private fun checkIfUsbStorageIsConnected() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
            manager = getSystemService(USB_SERVICE) as UsbManager
            registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION))

            val deviceList: HashMap<String, UsbDevice> = manager?.deviceList!!
            deviceList.values.forEach { device ->
                if (device.deviceClass == UsbConstants.USB_CLASS_MASS_STORAGE)
                    isUsbDriveConnected = true

                manager?.requestPermission(
                    device, PendingIntent.getBroadcast(
                        this, 0, Intent(
                            ACTION_USB_PERMISSION
                        ), 0
                    )
                )
            }

            // just print data for testing
            var string = ""
            deviceList.entries.forEach { entry ->
                Log.d("MainActivity", entry.key)
                string += entry.key + " - " + entry.value.deviceClass + "\n"

            }
            findViewById<TextView>(R.id.tv_usb).text = string
        }
    }


/*    private fun isAccessGranted(): Boolean {
        return try {
            val packageManager = packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appOpsManager = getSystemService(APP_OPS_SERVICE) as AppOpsManager
            var mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid, applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }*/


    companion object {
        const val TAG = "MainActivity"
    }
}