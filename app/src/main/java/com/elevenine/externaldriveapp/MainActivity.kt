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
import com.github.mjdev.libaums.fs.UsbFile
import com.github.mjdev.libaums.fs.UsbFileStreamFactory
import java.io.BufferedInputStream


class MainActivity : AppCompatActivity() {

    /**
     * stores currently connected mass storage usb device, so that a [usbReceiver] can perform
     * its action to his usb device.
     */
    private var connectedDevice: UsbMassStorageDevice? = null

    private var manager: UsbManager? = null
    private var testInfoString = ""

    /**
     * The broadcast receiver that receives the permission for operating with the usb device.
     */
    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {

                    // If the permission was granted, then perform actions with the usb device
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        // before interacting with a device you need to init it so that we can
                        // access its file system
                        connectedDevice?.init()

                        // you can check if the connected device is the one needed, getting its
                        // name, class, subclass etc.
                        connectedDevice?.usbDevice?.deviceClass
                        connectedDevice?.usbDevice?.deviceSubclass
                        connectedDevice?.usbDevice?.productId
                        connectedDevice?.usbDevice?.productName
                        connectedDevice?.usbDevice?.deviceName
                        connectedDevice?.usbDevice?.deviceProtocol


                        // get the file system of the connected usb device. Only uses the first
                        // partition on the device.
                        val currentFs = connectedDevice?.partitions?.get(0)?.fileSystem

                        // some testing strings
                        testInfoString += currentFs?.rootDirectory?.absolutePath + "\n"
                        testInfoString += "Capacity: " + currentFs?.capacity + "\n"
                        Log.d(TAG, "Occupied Space: " + currentFs?.occupiedSpace)
                        Log.d(TAG, "Free Space: " + currentFs?.freeSpace)
                        Log.d(TAG, "Chunk size: " + currentFs?.chunkSize)


                        // get the root directory of the file system of the currently connected
                        // usb drive.
                        val root = currentFs?.rootDirectory

                        // get the directory or file from the path which was entered by a user in
                        // the editText field. If no directory or file was found, then it will have
                        // null.
                        val usbFileOrDirectory: UsbFile? =
                            root?.search(findViewById<EditText>(R.id.et_search).text.toString())


                        if (usbFileOrDirectory == null) {
                            Toast.makeText(
                                this@MainActivity,
                                "Directory was not found!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                usbFileOrDirectory.absolutePath + " - " + usbFileOrDirectory.name,
                                Toast.LENGTH_SHORT
                            ).show()

                            // if the path entered by a user indicates a file, then get its inputStream.
                            val inputStream: BufferedInputStream
                            if (!usbFileOrDirectory.isDirectory) {
                                inputStream = UsbFileStreamFactory.createBufferedInputStream(
                                    usbFileOrDirectory,
                                    currentFs
                                )
                                Toast.makeText(
                                    this@MainActivity,
                                    "inputStream was successfully created",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else { }

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

        // when a button is pressed:
        findViewById<Button>(R.id.btn_usb).setOnClickListener {

            if (packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
                val devices = UsbMassStorageDevice.getMassStorageDevices(this)

                // just print data for testing
                devices.forEach { entry ->
                    Log.d("MainActivity", entry.usbDevice.deviceName)
                    testInfoString += entry.usbDevice.deviceName + " - " + entry.usbDevice.deviceClass + "\n"

                }
                testInfoString += "device info: " + "\n"

                // traverse through all connected devices or you can just take 1 device (i.e. 0th)
                for (device in devices) {

                    connectedDevice = device

                    // init the UsbManager to handle usb otg connect/disconnect
                    manager = getSystemService(USB_SERVICE) as UsbManager

                    // register a broadcast receiver for getting permission and performing an action
                    // right after the permission is granted
                    registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION))

                    // build and request a usb permission
                    val permissionIntent =
                        PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
                    manager?.requestPermission(device.usbDevice, permissionIntent)
                }

                // just print the data on the screen for testing purposes
                findViewById<TextView>(R.id.tv_usb).text = testInfoString
            }
        }
    }


    /*
    *
    *
    * a method to find if a usb is connected without an external library, not used
    *
    *
    *
    * */
    private var isUsbDriveConnected = false
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


    companion object {
        const val TAG = "MainActivity"
        const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }
}