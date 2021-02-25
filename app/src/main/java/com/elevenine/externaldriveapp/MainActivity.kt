package com.elevenine.externaldriveapp

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private var isUsbDriveConnected = false
    private var connectedDevice: UsbDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectedDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST))
            Log.d("MainActivity", "Usb host is ON")

        findViewById<Button>(R.id.btn_usb).setOnClickListener {
            val manager = getSystemService(Context.USB_SERVICE) as UsbManager

            val deviceList: HashMap<String, UsbDevice> = manager.deviceList
            deviceList.values.forEach { device ->
                if (device.deviceClass == UsbConstants.USB_CLASS_MASS_STORAGE)
                    isUsbDriveConnected = true
            }

            var string = ""
            deviceList.keys.forEach {
                Log.d("MainActivity", it)
                string += it + "\n"
            }

            if (isUsbDriveConnected)
                Toast.makeText(this, "UsbDrive is connected", Toast.LENGTH_SHORT).show()

            val pixel3_api30_filepath = "/storage/emulated/0/DCIM/asaka_cheque_1614257946330.png"
            val file = File(pixel3_api30_filepath)
            if (file.exists())
                Log.d("MainActivity", "File exists")
            else Log.d("MainActivity", "File is ABSENT")
        }
    }
}