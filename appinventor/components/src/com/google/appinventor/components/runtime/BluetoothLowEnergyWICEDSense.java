////-*- mode: java; c-basic-offset: 2; -*-
////
////TODO: Figure out what this should say now.
////Copyright 2014 - David Garrett - Broadcom Corporation
////http://www.apache.org/licenses/LICENSE-2.0
////
////
//
//package com.google.appinventor.components.runtime;
//
//import android.app.Activity;
//import android.content.Context;
//import android.os.Handler;
//import android.util.Log;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothAdapter.LeScanCallback;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothProfile;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//
//import com.google.appinventor.components.annotations.DesignerComponent;
//import com.google.appinventor.components.annotations.DesignerProperty;
//import com.google.appinventor.components.annotations.PropertyCategory;
//import com.google.appinventor.components.annotations.SimpleEvent;
//import com.google.appinventor.components.annotations.SimpleFunction;
//import com.google.appinventor.components.annotations.SimpleObject;
//import com.google.appinventor.components.annotations.SimpleProperty;
//import com.google.appinventor.components.annotations.UsesPermissions;
//import com.google.appinventor.components.common.ComponentCategory;
//import com.google.appinventor.components.common.PropertyTypeConstants;
//import com.google.appinventor.components.common.YaVersion;
//import com.google.appinventor.components.runtime.util.SdkLevel;
//import com.google.appinventor.components.runtime.util.ErrorMessages;
//import com.google.appinventor.components.runtime.EventDispatcher;
//
//import java.util.List;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.UUID;
//
//
///**
//* The WICEDSense component connects to the BLTE device
//*
//* @author  David Garrett (not the violinist)
//*/
//
//@DesignerComponent(version = YaVersion.BTLE_WICEDSENSE_COMPONENT_VERSION,
////category = ComponentCategory.CONNECTIVITY,
//category = ComponentCategory.SENSORS,
//description = "The WICEDSense component is still experimental",
//nonVisible = true,
//iconName = "images/wicedSenseIcon.png")
//@SimpleObject
//@UsesPermissions(permissionNames = 
//            "android.permission.BLUETOOTH, " + 
//            "android.permission.BLUETOOTH_ADMIN")
//public final class BluetoothLowEnergyWICEDSense extends BluetoothLowEnergyBaseClass {
//
//public BluetoothLowEnergyWICEDSense(ComponentContainer container) {
//	super(container);
//	// TODO Auto-generated constructor stub
//}
//
//}





//-*- mode: java; c-basic-offset: 2; -*-
//
//Copyright 2014 - David Garrett - Broadcom Corporation
//http://www.apache.org/licenses/LICENSE-2.0
//
//

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;


import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.EventDispatcher;


import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

/**
 * The WICEDSense component connects to the BLTE device
 *
 * @author  David Garrett (not the violinist)
 */
@DesignerComponent(version = YaVersion.BTLE_WICEDSENSE_COMPONENT_VERSION,
	category = ComponentCategory.SENSORS,
	description = "The WICEDSense component is still experimental",
	nonVisible = true,
	iconName = "images/wicedSenseIcon.png")
@SimpleObject
@UsesPermissions(permissionNames = 
"android.permission.BLUETOOTH, " + 
		"android.permission.BLUETOOTH_ADMIN")
public final class BluetoothLowEnergyWICEDSense extends BluetoothLowEnergySensorBase
implements Component, OnStopListener, OnResumeListener, OnPauseListener, Deleteable {

	//private static final String LOG_TAG = "WICEDSense";
	//private final Activity activity;
	//
	////if constructor finds enabled BTLE device, this is set
	//private boolean isEnabled = false;
	//
	////Start with no scan
	//private boolean scanning = false;
	//
	////Holds the link to the Bluetooth Adapter
	//private BluetoothAdapter bluetoothAdapter;
	//
	////holds error message
	//private boolean mLogEnabled = true;
	//private String mLogMessage = "";
	//
	////holds the BT device
	//private int deviceRssi = -130;
	//private BluetoothDevice mDevice;
	//
	////Holds list of devices
	//private ArrayList<DeviceScanRecord> mScannedDevices;
	//
	////holds sensors data
	//private boolean mSensorsEnabled = false;
	//
	////Gatt client pointer
	//private BluetoothGatt mBluetoothGatt = null;
	//
	////Service founds
	//private List<BluetoothGattService> mGattServices;
	//
	////holds current connection state
	//private int mConnectionState = STATE_DISCONNECTED;
	//
	////Holds specific WICED services
	//private boolean validWICEDDevice = false;
	//private BluetoothGattService mSensorService = null;
	//private BluetoothGattCharacteristic mSensorNotification = null;
	//private BluetoothGattService mBatteryService = null;
	//private BluetoothGattCharacteristic mBatteryCharacteristic = null;
	//
	////Holds Battery level
	//private int mBatteryLevel = -1;
	
	//Holds time stamp data
	private long startTime = 0;
	private long currentTime = 0;
	private long tempCurrentTime = 0;
	
	////Holds the sensor data
	//private float mXAccel = 0;
	//private float mYAccel = 0;
	//private float mZAccel = 0;
	//private float mXGyro = 0;
	//private float mYGyro = 0;
	//private float mZGyro = 0;
	//private float mXMagnetometer = 0;
	//private float mYMagnetometer = 0;
	//private float mZMagnetometer = 0;
	//private float mHumidity = 0;
	//private float mPressure = 0;
	//private float mTemperature = 0;
	//
	////set default temperature setting
	//private boolean mUseFahrenheit = true;
	//private boolean mRunInBackground = true;
	//
	////Defines BTLE States
	//private static final int STATE_DISCONNECTED = 0;
	//private static final int STATE_NEED_SERVICES = 1;
	//private static final int STATE_CONNECTED = 2;
	//
	///** Descriptor used to enable/disable notifications/indications */
	//private static final UUID CLIENT_CONFIG_UUID = UUID
	//  .fromString("00002902-0000-1000-8000-00805f9b34fb");
	//private static final UUID BATTERY_SERVICE_UUID = UUID
	//  .fromString("0000180F-0000-1000-8000-00805f9b34fb");
	//private static final UUID BATTERY_LEVEL_UUID = UUID
	//  .fromString("00002a19-0000-1000-8000-00805f9b34fb");


	/**
	 * Creates a new WICEDSense component.
	 *
	 * @param container the enclosing component
	 */
	public BluetoothLowEnergyWICEDSense (ComponentContainer container) {
		super(container.$form());
		LOG_TAG = "WICEDSense";
		SENSOR_SERVICE_UUID = UUID.fromString("739298B6-87B6-4984-A5DC-BDC18B068985");
		SENSOR_NOTIFICATION_UUID = UUID.fromString("33EF9113-3B55-413E-B553-FEA1EAADA459");

		// record the constructor time
		startTime  = System.nanoTime();
		currentTime  = startTime;
		tempCurrentTime  = startTime;
	}

	// TODO: Make this set the base variable properly.
	/** Various callback methods defined by the BLE API. */
	private final BLESensorBaseBluetoothGattCallback mGattCallback = new BLESensorBaseBluetoothGattCallback() {
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			if (SENSOR_NOTIFICATION_UUID.equals(characteristic.getUuid())) {
				byte[] value = characteristic.getValue();
				int bitMask = value[0];
				int index = 1;

				// Update timestamp
				currentTime = System.nanoTime();

				if ((bitMask & 0x1)>0) { 
					mXAccel = (value[index+1] << 8) + (value[  index] & 0xFF);
					mYAccel = (value[index+3] << 8) + (value[index+2] & 0xFF);
					mZAccel = (value[index+5] << 8) + (value[index+4] & 0xFF);
					index = index + 6;
				}
				if ((bitMask & 0x2)>0) { 
					mXGyro = (value[index+1] << 8) + (value[  index] & 0xFF);
					mYGyro = (value[index+3] << 8) + (value[index+2] & 0xFF);
					mZGyro = (value[index+5] << 8) + (value[index+4] & 0xFF);
					mXGyro = mXGyro / (float)100.0;
					mYGyro = mYGyro / (float)100.0;
					mZGyro = mZGyro / (float)100.0;
					index = index + 6;
				}
				if ((bitMask & 0x4)>0) { 
					mHumidity =  ((value[index+1] & 0xFF) << 8) + (value[index] & 0xFF);
					mHumidity = mHumidity / (float)10.0;
					index = index + 2;
				}
				if ((bitMask & 0x8)>0) { 
					mXMagnetometer = (value[index+1] << 8) + (value[  index] & 0xFF);
					mYMagnetometer = (value[index+3] << 8) + (value[index+2] & 0xFF);
					mZMagnetometer = (value[index+5] << 8) + (value[index+4] & 0xFF);
					index = index + 6;
				}
				if ((bitMask & 0x10)>0) { 
					mPressure =  ((value[index+1] & 0xFF) << 8) + (value[index] & 0xFF);
					mPressure = mPressure / (float)10.0;
					index = index + 2;
				}
				if ((bitMask & 0x20)>0) { 
					mTemperature =  ((value[index+1] & 0xFF) << 8) + (value[index] & 0xFF);
					mTemperature = mTemperature / (float)10.0;
					index = index + 2;
					tempCurrentTime = System.nanoTime();
				}

				LogMessage("Reading back sensor data with type " + bitMask + " packet", "i");
				//SensorsUpdated();
			}
		}
	};


	/**  ----------------------------------------------------------------------
	 *   Properties of the Device
	 *   ----------------------------------------------------------------------
	 */


	/**
	 * Resets the internal counter
	 */
	@SimpleFunction(description = "Resets the internal timer")
	public void ResetTimestamp() {
		startTime = System.nanoTime();
		currentTime = startTime;
		tempCurrentTime = startTime;
	}

	/**
	 * Returns the time since reset in milliseconds
	 *
	 */
	@SimpleProperty(description = "Returns timestamp of sensor data in milliseconds since reset", 
			category = PropertyCategory.BEHAVIOR,
			userVisible = true)
	public int Timestamp() {
		long timeDiff;
		int timeMilliseconds;

		// compute nanoseconds since start time
		timeDiff = currentTime - startTime;

		// Convert to milliseconds
		timeDiff = timeDiff / 1000000;

		// convert to int
		timeMilliseconds = (int)timeDiff;

		return timeMilliseconds;
	}

	/**
	 * Returns the time since reset in milliseconds
	 *
	 */
	@SimpleProperty(description = "Returns timestamp of just temperature, humidity and pressure sensor data in milliseconds since reset", 
			category = PropertyCategory.BEHAVIOR,
			userVisible = true)
	public int TemperatureTimestamp() {
		long timeDiff;
		int timeMilliseconds;

		// compute nanoseconds since start time
		timeDiff = tempCurrentTime - startTime;

		// Convert to milliseconds
		timeDiff = timeDiff / 1000000;

		// convert to int
		timeMilliseconds = (int)timeDiff;

		return timeMilliseconds;
	}

	/**
	 * Sets the temperature setting for Fahrenheit or Celsius
	 *
	 */
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
			defaultValue = "True")
	@SimpleProperty(description = "Sets temperature data in Fahrenheit, not Celius", 
	category = PropertyCategory.BEHAVIOR,
	userVisible = true)
	public void UseFahrenheit(boolean enableFlag) {
		mUseFahrenheit = enableFlag;
	}

	/**
	 * Returns whether the temperature setting is Fahrenheit or Celsius
	 *
	 */
	@SimpleProperty(description = "Returns true if temperature format is in Fahrenheit", 
			category = PropertyCategory.BEHAVIOR,
			userVisible = true)
	public boolean UseFahrenheit() {
		return mUseFahrenheit;
	}

	/**
	 * Return the X Accelerometer sensor data
	 */
	@SimpleProperty(description = "Get X Accelerometer data", category = PropertyCategory.BEHAVIOR, userVisible = true)
	public float XAccel() {
		return mXAccel;
	}

	/**
	 * Return the Y Accelerometer sensor data
	 */
	@SimpleProperty(description = "Get Y Accelerometer data", category = PropertyCategory.BEHAVIOR, userVisible = true)
	public float YAccel() {
		return mYAccel;
	}

	/**
	 * Return the Z Accelerometer sensor data
	 */
	@SimpleProperty(description = "Get Z Accelerometer data", category = PropertyCategory.BEHAVIOR, userVisible = true)
	public float ZAccel() {
		return mZAccel;
	}

	/**
	 * Return the X Gyro sensor data
	 */
	@SimpleProperty(description = "Get X Gyro data", category = PropertyCategory.BEHAVIOR, userVisible = true)
	public float XGyro() {
		return mXGyro;
	}

	/**
	 * Return the Y Gyro sensor data
	 */
	@SimpleProperty(description = "Get Y Gyro data", category = PropertyCategory.BEHAVIOR, userVisible = true)
	public float YGyro() {
		return mYGyro;
	}

	/**
	 * Return the Z Gyro sensor data
	 */
	@SimpleProperty(description = "Get Z Gyro data", category = PropertyCategory.BEHAVIOR, userVisible = true)
	public float ZGyro() {
		return mZGyro;
	}


	/**
	 * Return the X Magnetometer sensor data
	 */
	@SimpleProperty(description = "Get X Magnetometer data", category = PropertyCategory.BEHAVIOR, userVisible = true)
	public float XMagnetometer() {
		return mXMagnetometer;
	}

	/**
	 * Return the Y Magnetometer sensor data
	 */
	@SimpleProperty(description = "Get Y Magnetometer data", category = PropertyCategory.BEHAVIOR, userVisible = true)
	public float YMagnetometer() {
		return mYMagnetometer;
	}

	/**
	 * Return the Z Magnetometer sensor data
	 */
	@SimpleProperty(description = "Get Z Magnetometer data", category = PropertyCategory.BEHAVIOR, userVisible = true)
	public float ZMagnetometer() {
		return mZMagnetometer;
	}

	/**
	 * Return the Compass heading
	 */
	@SimpleProperty(description = "Get the compass heading in degrees assuming device is flat", category = PropertyCategory.BEHAVIOR, userVisible = true)
	public float Heading() {
		double mag = Math.sqrt(mXMagnetometer*mXMagnetometer + mYMagnetometer*mYMagnetometer);
		double heading;

		LogMessage("Calculating heading from X+Y magnetometer data (" + 
				mXMagnetometer + "," + mYMagnetometer + "), mag = " + mag, "i");

		if (mag > 0.0) { 
			// convert x,y to radians to degrees
			double nX = mXMagnetometer/mag;
			double nY = mYMagnetometer/mag;
			heading = Math.atan2(nY, nX) * 57.295779578 + 180.0;
		} else { 
			heading = 0.0;
		}

		LogMessage("Heading = " + heading, "i");
		return (float)heading;
	}

	/**
	 * Return the Humidity sensor data
	 */
	@SimpleProperty(description = "Get Humidity data in %", 
			category = PropertyCategory.BEHAVIOR,
			userVisible = true)
	public float Humidity() {
		return mHumidity;
	}

	/**
	 * Return the Pressure sensor data
	 */
	@SimpleProperty(description = "Get Pressure data in millibar", 
			category = PropertyCategory.BEHAVIOR, 
			userVisible = true)
	public float Pressure() {
		return mPressure;
	}

	/**
	 * Return the Temperature sensor data
	 */
	@SimpleProperty(description = "Get Temperature data in Fahrenheit or Celsius", 
			category = PropertyCategory.BEHAVIOR, 
			userVisible = true)
	public float Temperature() {
		float tempConvert;

		// get temperature in celsius
		tempConvert = mTemperature;

		// Convert to Fahrenheit if selected
		if (mUseFahrenheit) { 
			tempConvert = tempConvert* (float)(9.0/5.0) + (float)32.0; 
		}

		return tempConvert;
	}
}

