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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private var isUsbDriveConnected = false
    private var connectedDevice: UsbDevice? = null

    private var manager: UsbManager? = null

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            //call method to set up device communication
                        }
                    } else {
                        Log.d("MainActivity", "permission denied for device $device")
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
            checkIfUsbStorageIsConnected()
            if (isUsbDriveConnected)
                Toast.makeText(this, "UsbDrive is connected", Toast.LENGTH_SHORT).show()

            isFileExists()
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

                manager?.requestPermission(device, PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0))
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
}