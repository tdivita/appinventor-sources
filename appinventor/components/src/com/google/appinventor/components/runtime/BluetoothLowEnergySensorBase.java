//-*- mode: java; c-basic-offset: 2; -*-
//
//TODO: Figure out what this should say now.
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
import com.google.appinventor.components.runtime.WICEDSense.DeviceScanRecord;
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

@SimpleObject
//@UsesPermissions(permissionNames = 
//              "android.permission.BLUETOOTH, " + 
//              "android.permission.BLUETOOTH_ADMIN")

public abstract class BluetoothLowEnergySensorBase extends AndroidNonvisibleComponent 
implements Component, OnStopListener, OnResumeListener, OnPauseListener, Deleteable {

	  protected static String LOG_TAG = "BTLEDevice";
	  protected final Activity activity;

	  // if constructor finds enabled BTLE device, this is set
	  protected boolean isEnabled = false;
	  
	  // Start with no scan
	  protected boolean scanning = false;

	  // Holds the link to the Bluetooth Adapter
	  protected BluetoothAdapter bluetoothAdapter;

	  // holds error message
	  protected boolean mLogEnabled = true;
	  protected String mLogMessage = "";

	  // holds the BT device
	  protected int deviceRssi = -130;
	  protected BluetoothDevice mDevice;
	  
	  // Holds list of devices
	  protected ArrayList<DeviceScanRecord> mScannedDevices;
	  
	  // holds sensors data
	  protected boolean mSensorsEnabled = false;
	  
	  // Gatt client pointer
	  protected BluetoothGatt mBluetoothGatt = null;

	  // Service founds
	  protected List<BluetoothGattService> mGattServices;

	  // holds current connection state
	  protected int mConnectionState = STATE_DISCONNECTED;

	  // TODO: Look at what happens if they have more than battery and sensor service
	  // Holds specific WICED services
	  protected boolean validDevice = false;
	  protected BluetoothGattService mSensorService = null;
	  protected BluetoothGattCharacteristic mSensorNotification = null;
	  protected BluetoothGattService mBatteryService = null;
	  protected BluetoothGattCharacteristic mBatteryCharacteristic = null;
	  
	  // Initialize in any subclass, and override the onCharacteristicChanged method.
	  protected BLESensorBaseBluetoothGattCallback mGattCallback;

	  // Holds Battery level
	  protected int mBatteryLevel = -1;

	  protected boolean mRunInBackground = true;
	  
	  // Defines BTLE States
	  protected static final int STATE_DISCONNECTED = 0;
	  protected static final int STATE_NEED_SERVICES = 1;
	  protected static final int STATE_CONNECTED = 2;

	  /** Descriptor used to enable/disable notifications/indications */
	  protected static final UUID CLIENT_CONFIG_UUID = UUID
	          .fromString("00002902-0000-1000-8000-00805f9b34fb");
	  // The service UUIDs must be defined in the constructor of any subclass.
	  protected static UUID SENSOR_SERVICE_UUID;
	  protected static UUID SENSOR_NOTIFICATION_UUID;
	  protected static final UUID BATTERY_SERVICE_UUID = UUID
	          .fromString("0000180F-0000-1000-8000-00805f9b34fb");
	  protected static final UUID BATTERY_LEVEL_UUID = UUID
	          .fromString("00002a19-0000-1000-8000-00805f9b34fb");


	  /**
	   * Creates a new WICEDSense component.
	   *
	   * @param container the enclosing component
	   */
	  public BluetoothLowEnergySensorBase (ComponentContainer container) {
	    super(container.$form());
	    activity = container.$context();

	    // names the function
	    String functionName = "BTLEDevice";

	    // setup new list of devices
	    mScannedDevices = new ArrayList<DeviceScanRecord>();

	    // initialize GATT services
	    mGattServices = new ArrayList<BluetoothGattService>();

	    /* Setup the Bluetooth adapter */
	    if (SdkLevel.getLevel() < SdkLevel.LEVEL_JELLYBEAN_MR2) { 
	      bluetoothAdapter = null;
	      /** issues message to reader */
	      form.dispatchErrorOccurredEvent(this, functionName,
	          ErrorMessages.ERROR_BLUETOOTH_LE_NOT_SUPPORTED);
	    } else { 
	      bluetoothAdapter = newBluetoothAdapter(activity);
	    }

	    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) { 
	      isEnabled = false;
	      LogMessage("No Valid BTLE Device on platform", "e");

	      /** issues message to reader */
	      form.dispatchErrorOccurredEvent(this, "BTLEDevice",
	          ErrorMessages.ERROR_BLUETOOTH_NOT_ENABLED);

	    } else { 
	      isEnabled = true;
	      LogMessage("Found the BTLE Device on platform", "i");
	    }

	    // register with the forms to that OnResume and OnNewIntent
	    // messages get sent to this component
	    form.registerForOnResume(this);
	    form.registerForOnStop(this);
	    //form.registerForOnNewIntent(this);
	    form.registerForOnPause(this);

	  }
	  /** Get Device name */
	  protected String GetDeviceNameAndAddress(BluetoothDevice nextDevice) { 
	    String mName;
	    if (nextDevice != null) { 
	      mName = nextDevice.getName() + ":" + nextDevice.toString();
	    } else { 
	      mName = "Null device"; 
	    }
	    return mName;
	  }

	  /** Log Messages */
	  protected void LogMessage(String message, String level) { 
	    if (mLogEnabled) { 
	      mLogMessage = message;
	      String errorLevel = "e";
	      String warningLevel = "w";
	  
	      // push to appropriate logging
	      if (level.equals(errorLevel)) {
	        Log.e(LOG_TAG, message);
	      } else if (level.equals(warningLevel)) {
	        Log.w(LOG_TAG, message);
	      } else { 
	        Log.i(LOG_TAG, message);
	      }
	    }
	  }


	  /** Log Messages */
	  protected void CleanupBTLEState() { 

	    mConnectionState = STATE_DISCONNECTED;

	    // null out services
	    mSensorService = null;
	    mSensorNotification = null;
	    mBatteryService = null;
	    mBatteryCharacteristic = null;
	    mGattServices.clear();
	    validDevice = false;
	    mScannedDevices.clear();

	    LogMessage("Issuing a cleanup of the BTLE state", "i");
	  }

	  /** ----------------------------------------------------------------------
	   *  BTLE Code Section
	   *  ----------------------------------------------------------------------
	   */

	  /* Create Device list from scan */
	  public class DeviceScanRecord implements Comparable<DeviceScanRecord> { 
	    private BluetoothDevice device = null;
	    private int rssi = 0;
	    private byte[] scanRecord = null;
	      
	    public DeviceScanRecord() { 
	      device = null;
	      rssi = 0;
	      scanRecord = null;
	    }
	    
	    // set the container to scan results
	    public void setRecord(final BluetoothDevice deviceVal, int rssiVal, byte[] scanRecordVal) {
	      this.device = deviceVal;
	      this.rssi = rssiVal;

	      this.scanRecord = new byte[scanRecordVal.length];
	      for (int loop1 = 0; loop1 < scanRecordVal.length; loop1++) { 
	        this.scanRecord[loop1] = scanRecordVal[loop1];
	      }
	    }

	    // get the RSSI
	    public int getRssi() { 
	      return rssi;
	    }
	    // get the device handle
	    public BluetoothDevice getDevice() { 
	      return device;
	    }
	    // returns the scan record data
	    public String getScanRecord() { 
	      return bytesToHex(scanRecord);
	    }
	   
	    public int compareTo(DeviceScanRecord compareScan) { 
	      return compareScan.getRssi() - this.rssi;
	    }
	  }

	  // Set the sensor state
	  public void setSensorState() { 

	    // Fire off characteristic 
	    if (validDevice) { 

	      // Write the characteristic
	      if (mSensorNotification == null) { 
	        LogMessage("Trying to set sensors notification with null pointer", "e");
	      } else { 
	        BluetoothGattDescriptor mSensorNotificationClientConfig;

	        // Update descriptor client config
	        mSensorNotificationClientConfig = mSensorNotification.getDescriptor(CLIENT_CONFIG_UUID);
	        if (mSensorNotificationClientConfig == null) {
	          LogMessage("Cannot find sensor client descriptor, this device is not supported", "e");
	          return;
	        }

	        // set values in the descriptor
	        if (mSensorsEnabled) { 
	          mSensorNotificationClientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	        } else { 
	          mSensorNotificationClientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
	        }

	        // write the gatt descriptor
	        mBluetoothGatt.writeDescriptor(mSensorNotificationClientConfig); 
	        if (mSensorsEnabled) { 
	          LogMessage("Turning on Sensor notifications", "i");
	        } else { 
	          LogMessage("Turning off Sensor notifications", "i");
	        }
	      }
	    }
	  }



	  /** create adaptor */
	  public static BluetoothAdapter newBluetoothAdapter(Context context) {
	    final BluetoothManager bluetoothManager = 
	      (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
	    return bluetoothManager.getAdapter();  
	  }

	  /** Device scan callback. */
	  private LeScanCallback mLeScanCallback = new LeScanCallback() {
	    @Override
	    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

	      // Add new device
	      DeviceScanRecord newDevice = new DeviceScanRecord();
	      boolean foundNewDevice = true;
	  
	      // get the device record
	      newDevice.setRecord(device, rssi, scanRecord);

	      // make sure to ignore null devices
	      if (device != null) { 
	  
	        // Search through found devices and find matching one
	        for (int loop1 = 0; loop1 < mScannedDevices.size(); loop1++) {
	          DeviceScanRecord prevDevice;
	  
	          // see if we already know about this device
	          prevDevice = mScannedDevices.get(loop1); 
	          if (device.equals(prevDevice.getDevice())) { 
	            foundNewDevice = false;
	          }
	        }
	        if (foundNewDevice) {
	          mScannedDevices.add(newDevice);
	          LogMessage("Adding a BTLE device " + GetDeviceNameAndAddress(device) + " with rssi = " + rssi + " dBm to scan list", "i");
	        }
	      }

	    }
	  };

	  /** Get Sensor Message in HEX */
	  final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	  protected static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	      int v = bytes[j] & 0xFF;
	      hexChars[j * 2] = hexArray[v >>> 4];
	      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	  }
	  
	  /*
	   * Class for use by all BLE Sensor components. Each subclass of the SensorBase class will need
	   * to instantiate one of these in its constructor, and override the onCharacteristicChanged
	   * method to do the specific sensor reading required for their device.
	   */
	  protected class BLESensorBaseBluetoothGattCallback extends BluetoothGattCallback{
		  /** Various callback methods defined by the BLE API. */
		  @Override
	        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
	          LogMessage("onConnectionStateChange callback with status = " + status, "i");

	          //String intentAction;
	          if (newState == BluetoothProfile.STATE_CONNECTED) {
	            mConnectionState = STATE_NEED_SERVICES;

	            // Trigger device discovery 
	            LogMessage("Connected to BLTE device, starting service discovery", "i");
	            boolean success = mBluetoothGatt.discoverServices();
	            if (!success) { 
	              LogMessage("Cannot start service discovery for some reason", "e");
	            }
	          // Finalizing the disconnect profile
	          } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
	             mConnectionState = STATE_DISCONNECTED;

	             // close out connection
//	             mBluetoothGatt.close();

	             // null out services
	             mSensorService = null;
	             mSensorNotification = null;
	             mBatteryService = null;
	             mBatteryCharacteristic = null;
	             mGattServices.clear();

	             LogMessage("Disconnected from BLTE device", "i");
	          }
	        }
		  
		  @Override
	        // New services discovered
	        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
	          LogMessage("onReadRemoteRssi callback with status = " + status, "i");

	          deviceRssi = rssi;
	          LogMessage("Updating RSSI from remove device = " + rssi + " dBm", "i");

	          // update RSSI
	          //RSSIUpdated();
	        }
		  
		  @Override
	        // New services discovered
	        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

	          LogMessage("onServicesDiscovered callback with status = " + status, "i");
	  
	          if (status == BluetoothGatt.GATT_SUCCESS) {
	            // record services 
	            mGattServices = gatt.getServices();
	            validDevice = true;

	            // update connection state
	            if (mConnectionState == STATE_NEED_SERVICES) { 
	              mConnectionState = STATE_CONNECTED;
	            }
	  
	            // log message
	            LogMessage("Found " + mGattServices.size() + " Device services", "i");

	            // Match to sensor services
	            BluetoothGattService mService;
	            for (int loop1 = 0; loop1 < mGattServices.size(); loop1++) {
	              mService = mGattServices.get(loop1);
	              // get battery service
	              if (BATTERY_SERVICE_UUID.equals(mService.getUuid())) { 
	                mBatteryService = mService;
	                mBatteryCharacteristic = mBatteryService.getCharacteristic(BATTERY_LEVEL_UUID);
	              } 
	              // get the sensor service
	              if (SENSOR_SERVICE_UUID.equals(mService.getUuid())) { 
	                mSensorService = mService;
	                mSensorNotification = mSensorService.getCharacteristic(SENSOR_NOTIFICATION_UUID);
	              } 
	            }

	            // Check for VALID WICED service
	            validDevice = true;
	            if (mBatteryService == null) { validDevice = false; }
	            if (mBatteryCharacteristic == null) { validDevice = false; }
	            if (mSensorService == null) { validDevice = false; }
	            if (mSensorNotification == null) { validDevice = false; }

	            // Warnings if not valid
	            if (validDevice) { 
	              LogMessage("Found services on valid sensor device", "i");
	            } else { 
	              LogMessage("Connected device is not the right type of device", "e");
	            }

	            // Set the sensor state directly
	            setSensorState();

	            // Triggers callback for connected device
	            //Connected();
	          } else {
	            LogMessage("onServicesDiscovered received but failed", "e");
	          }
	        }
		  
		  @Override
	        // Result of a characteristic read operation
	        public void onCharacteristicRead(BluetoothGatt gatt, 
	                                         BluetoothGattCharacteristic characteristic, 
	                                         int status) {
	          if (status == BluetoothGatt.GATT_SUCCESS) {
	            if (BATTERY_LEVEL_UUID.equals(characteristic.getUuid())) {
	              try {
	                mBatteryLevel = characteristic.getIntValue(
	                               BluetoothGattCharacteristic.FORMAT_UINT8, 0);
	                LogMessage("Read battery level " + mBatteryLevel + "%", "i");

	                // trigger event
	                //BatteryLevelUpdated();
	              } catch (Exception e) {
	                LogMessage("Unable to read battery level.", "e");
	                return;
	              }
	            }
	          } else {
	            LogMessage("Failure in reading Gatt Characteristics", "e");
	          }
	        }

	        @Override
	        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

	          LogMessage("onDescriptorWrite with status = " + status, "i");
	          if (mSensorNotification == null) {
	            LogMessage("onDescriptorWrite: mSensorNotification == null", "e");
	            return;
	          }

	          // set the enable value
	          boolean success = mBluetoothGatt.setCharacteristicNotification(mSensorNotification, mSensorsEnabled);
	          if (success) {
	            LogMessage("Was able to write sensor notification characteristics", "i");
	          } else { 
	            LogMessage("Failed to write sensor notification characteristic", "e");
	           }
	        }
	  }


//
//
//	  /* ----------------------------------------------------------------------
//	   * ----------------------------------------------------------------------
//	   * GUI Interface Code Section
//	   * ----------------------------------------------------------------------
//	   * ----------------------------------------------------------------------
//	   */
//
//	  /*  ----------------------------------------------------------------------
//	   *   Events 
//	   *   ----------------------------------------------------------------------
//	   */
//
//	  /**
//	   * Callback for an Error Event
//	   */
//	//  @SimpleEvent(description = "Event when there is an Error.")
//	//  public void Error() { 
////	    LogMessage("Firing the Error()", "e");
////	    EventDispatcher.dispatchEvent(this, "Error");
//	//  }
//
	  /**
	   * Callback for Found Device Event
	   */
	  @SimpleEvent(description = "Event when an LE Device is found in scan.")
	  public void FoundDevice() { 
	    LogMessage("Firing the FoundDevice() event", "i");
	    EventDispatcher.dispatchEvent(this, "FoundDevice");
	  }
//
//	  /**
//	   * Callback for RSSI data
//	   */
//	//  @SimpleEvent(description = "RSSI Read Event.")
//	//  public void RSSIUpdated() { 
////	    boolean success;
////	    LogMessage("Firing the RSSIUpdated() event", "i");
////	    success = EventDispatcher.dispatchEvent(this, "RSSIUpdated");
////	    if (!success) { 
////	      LogMessage("Failed to dispatch RSSIUpdated() event", "e");
////	    } else { 
////	      LogMessage("Success in dispatching RSSIUpdated() event", "i");
////	    }
//	//  }
//	 
//	  /**
//	   * Callback events for device connection
//	   */
//	//  @SimpleEvent(description = "BTLE Connection Event.")
//	//  public void Connected() { 
////	    EventDispatcher.dispatchEvent(this, "Connected");
//	//  }
//
//
//	  /**
//	   * Callback events for Sensor Update
//	   */
//	//  @SimpleEvent(description = "Sensor data updated.")
//	//  public void SensorsUpdated() { 
////	    EventDispatcher.dispatchEvent(this, "SensorsUpdated");
//	//  }
//
//	  /**
//	   * Callback events for battery levels
//	   */
//	//  @SimpleEvent(description = "Received Battery Level.")
//	//  public void BatteryLevelUpdated() { 
////	    EventDispatcher.dispatchEvent(this, "BatteryLevelUpdated");
//	//  }
//
	  /**  ----------------------------------------------------------------------
	   *   Function calls
	   *   ----------------------------------------------------------------------
	   */

	  /**
	   * Allows the user to check battery level
	   */
	  @SimpleFunction(description = "Reads WICED Sense kit battery level.")
	  public void ReadBatteryLevel() { 
	    String functionName = "ReadBatteryLevel";
	    if (mConnectionState == STATE_CONNECTED) { 
	      if (validDevice) { 
	        if (mBatteryCharacteristic == null) { 
	          LogMessage("Reading null battery characteristic", "e");
	        } else { 
	          boolean success = mBluetoothGatt.readCharacteristic(mBatteryCharacteristic);
	          if (success) { 
	            LogMessage("Reading battery characteristic", "i");
	          } else { 
	            LogMessage("Reading battery characteristic failed", "e");
	          }
	        }
	      } else { 
	        LogMessage("Trying to reading battery without a device", "e");
	      }
	    } else { 
	      LogMessage("Trying to reading battery before connected", "e");
	    }
	  }

	  /**
	   * Allows the user to start the scan
	   */
	  @SimpleFunction(description = "Starts BTLE scanning")
	  public void startLeScan() { 
	    String functionName = "startLeScan";
	    
	    // If not scanning, clear list rescan
	    if (!scanning) { 
	      mScannedDevices.clear();
	      scanning = true;

	      // Force the LE scan
	      try { 
	        bluetoothAdapter.startLeScan(mLeScanCallback);
	        LogMessage("Starting LE scan", "i");
	      } catch (Exception e) { 
	        LogMessage("Failed to start LE scan", "e");
	        scanning = false;
	      }
	    }
	  }

	  /**
	   * Allows the user to Stop the scan
	   */
	  @SimpleFunction(description = "Stops BTLE scanning")
	  public void stopLeScan() { 
	    String functionName = "stopLeScan";
	    if (scanning) { 
	      try { 
	        bluetoothAdapter.stopLeScan(mLeScanCallback);
	        scanning = false;
	        LogMessage("Stopping LE scan with " + mScannedDevices.size() + " devices", "i");

	        // fire off event
	        if (mScannedDevices.size() > 0) { 
	    
	          // Sort the list of devices by RSSI
	          Collections.sort(mScannedDevices);

	          // Fire off the event
	          FoundDevice();
	        }
	      } catch (Exception e) { 
	        LogMessage("Failed to stop LE scan", "e");
	      }
	    }
	  }
//
//	  /**  ----------------------------------------------------------------------
//	   *   Properties of the Device
//	   *   ----------------------------------------------------------------------
//	   */
//
	  /** Gets Battery Level */
	  @SimpleProperty(description = "Returns the battery level.", 
	                  category = PropertyCategory.BEHAVIOR,
	                  userVisible = true)
	  public int BatteryLevel() {
	    return mBatteryLevel;
	  }

	  /** Checks we have found services on the device */
	  @SimpleProperty(description = "Queries if Device Services have been discoverd", 
	                  category = PropertyCategory.BEHAVIOR,
	                  userVisible = true)
	  public boolean FoundServices() {
	    if (mConnectionState == STATE_CONNECTED) { 
	      return true;
	    } else { 
	      return false;
	    }
	  }

	  /** Makes sure GATT profile is connected */
	  @SimpleProperty(description = "Queries Connected state", 
	                  category = PropertyCategory.BEHAVIOR,
	                  userVisible = true)
	  public boolean IsConnected() {
	    if (mConnectionState == STATE_CONNECTED) {
	      return true;
	    } else { 
	      return false;
	    }
	  }

	  /** Returns the RSSI measurement from devices
	   *
	   *  Instantly returns the rssi on WICEDsene class variable
	   *  but calls a Gatt callback that will update with the new 
	   *  values on the callback (later in time)
	   *
	   *  Should consider callback "EVENT" to get accurate value
	   */
	  @SimpleProperty(description = "Queries RSSI", 
	                  category = PropertyCategory.BEHAVIOR,
	                  userVisible = true)
	  public int RSSI() {
	    return deviceRssi;
	  }
//
//	  /**
//	   * Returns text log
//	   */
//	//  @SimpleProperty(description = "Queries current log message", 
////	                  category = PropertyCategory.BEHAVIOR,
////	                  userVisible = true)
//	//  public String Text() {
////	    return mLogMessage;
//	//  }
//
	  /**
	   * Allows the user to Read remote RSSI
	   */
	  @SimpleFunction(description = "Forces read of remote RSSI")
	  public void ReadRSSI() { 
	    String functionName = "ReadRSSI";
	    if (mConnectionState == STATE_CONNECTED) { 
	      mBluetoothGatt.readRemoteRssi();
	    } else { 
	      LogMessage("Trying to read RSSI without a connected device", "e");
	    }
	  }

	  /**
	   * Allows the user to disconnect
	   */
	  @SimpleFunction(description = "Disconnects GATT connection")
	  public void Disconnect() { 
	    String functionName = "Disconnect";

	    if (mConnectionState == STATE_CONNECTED || mConnectionState == STATE_NEED_SERVICES) {
	      mBluetoothGatt.disconnect();
	      LogMessage("Disconnecting from device", "i");
	    } else { 
	      LogMessage("Trying to disconnect without a connected device", "e");
	    }
	  }

	  /**
	   * Allows to Connect to closest Device 
	   */
	  @SimpleFunction(description = "Connects to the WICED sense kit with the strongest RSSI")
	  public void ConnectClosest() { 
	    String functionName = "ConnectClosest";
	    DeviceScanRecord nextScannedDevice;
	    String testname;
	    boolean foundDevice = false;
	    int maxRssi = -160;

	    // check connected state
	    if (mConnectionState == STATE_DISCONNECTED) { 
	  
	      // log message
	      LogMessage("Testing " + mScannedDevices.size() + " device(s) for closest scanned BTLE device", "i");
	  
	      // Search through strings and find matching one
	      for (int loop1 = 0; loop1 < mScannedDevices.size(); loop1++) {
	  
	        // get the next device
	        nextScannedDevice = mScannedDevices.get(loop1); 
	        LogMessage("Testing device " + GetDeviceNameAndAddress(nextScannedDevice.getDevice()) + ", rssi = " + nextScannedDevice.getRssi() + " dBm", "i");
	  
	        // update maximum value
	        if (nextScannedDevice.getRssi() > maxRssi) { 
	          maxRssi = nextScannedDevice.getRssi();
	          // setup device name
	          mDevice = nextScannedDevice.getDevice();
	          //tempDevice = nextScannedDevice.getDevice();
	          testname = GetDeviceNameAndAddress(mDevice);
	          LogMessage("Found closest device " + testname + ", rssi = " + maxRssi + " dBm", "i");
	          foundDevice = true;
	        }
	      }
	  
	      // Found the best device to connect
	      if (foundDevice) {
	        mBluetoothGatt = mDevice.connectGatt(activity, false, mGattCallback);
	        LogMessage("Connecting device " + GetDeviceNameAndAddress(mDevice), "i");
	      } else { 
	        LogMessage("No device found to connect", "e");
	      }
	    } else { 
	      LogMessage("Trying to connect with an already connected device", "e");
	    }
	  }

	  /**
	   * Allows the Connect to Device
	   */
	  @SimpleFunction(description = "Connects to the named WICED Sense kit")
	  public void Connect(String name) { 
	    String functionName = "Connect";
	    DeviceScanRecord nextScanRecord;
	    BluetoothDevice tempDevice;
	    String testname;
	    boolean foundDevice = false;

	    if (mConnectionState == STATE_DISCONNECTED) { 

	      // Search through strings and find matching one
	      for (int loop1 = 0; loop1 < mScannedDevices.size(); loop1++) {
	        // recover next device in list
	        tempDevice = mScannedDevices.get(loop1).getDevice();
	        testname = GetDeviceNameAndAddress(tempDevice);
	    
	        // check if this is the device
	        if (testname.equals(name)) { 
	          mDevice = tempDevice;
	          foundDevice = true;
	        }
	      }
	  
	      // Fire off the callback
	      if (foundDevice) { 
	        mBluetoothGatt = mDevice.connectGatt(activity, false, mGattCallback);
	        LogMessage("Connecting device " + GetDeviceNameAndAddress(mDevice), "i");
	      } else { 
	        LogMessage("No device found to connect", "e");
	      }
	    } else { 
	      LogMessage("Trying to connect with an already connected device", "e");
	    }
	  }

	  /**
	   * Sets whether BLE will run in the background.
	   *
	   */
	  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
	                    defaultValue = "True")
	  @SimpleProperty(description = "Keeps BTLE running in background", 
	                  category = PropertyCategory.BEHAVIOR,
	                  userVisible = true)
	  public void RunInBackground(boolean enableFlag) {
	    mRunInBackground = enableFlag;
	  }

	  /**
	   * Returns if Sensors are enabled
	   *
	   */
	  @SimpleProperty(description = "Returns true if Sensors are enabled", 
	                  category = PropertyCategory.BEHAVIOR,
	                  userVisible = true)
	  public boolean SensorsEnabled() {
	    return mSensorsEnabled;
	  }

	  /**
	   * Turns on sensors
	   *
	   */
	  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
	      defaultValue = "False")
	  @SimpleProperty(description = "Sets the sensor enabled flag", 
	                  category = PropertyCategory.BEHAVIOR,
	                  userVisible = true)
	  public void SensorsEnabled(boolean enableFlag) {

	    mSensorsEnabled = enableFlag;
	    if (enableFlag) { 
	      LogMessage("Setting SensorsEnabled to true", "i");
	    } else {
	      LogMessage("Setting SensorsEnabled to false", "i");
	    }

	    // Transfer to device if it's connected
	    setSensorState();

	  }

	  /**
	   * Returns the scanning status
	   *
	   * @return scanning if still scanning
	   */
	  @SimpleProperty(description = "Checks if BTLE device is scanning", 
	                  category = PropertyCategory.BEHAVIOR,
	                  userVisible = true)
	  public boolean Scanning() {
	    return scanning;
	  }

	  /**
	   * Sets is enabled
	   *
	   * @return Checks if the BTLE is enabled
	   */
	  @SimpleProperty(description = "Checks if BTLE is available and enabled", 
	                  category = PropertyCategory.BEHAVIOR,
	                  userVisible = true)
	  public boolean Enabled() {
	    return isEnabled;
	  }

	  /**
	   * Returns a list of the Gatt services
	   */
	  @SimpleProperty(description = "Lists the BLTE GATT Services", category = PropertyCategory.BEHAVIOR)
	  public List<String> DeviceServices() { 
	    List<String> listOfServices = new ArrayList<String>();
	    int numServices;
	    BluetoothGattService mService;

	    // number of services discovered
	    numServices = mGattServices.size();

	    // bail out if nothing found
	    if (numServices == 0) { 
	      listOfServices.add("No Services Found");
	      LogMessage("Did not find any Services", "i");
	    } else { 
	      LogMessage("Found " + numServices + " services", "i");
	      for (int loop1 = 0; loop1 < numServices; loop1++) {
	        mService = mGattServices.get(loop1);
	        if (mService != null) { 
	          listOfServices.add(mService.getUuid().toString());
	        }
	      }
	    }
	  
	    return listOfServices;
	  }

	  /**
	   * Allows to access of scan records found in the Scan
	   */
	  @SimpleProperty(description = "Lists the scan record of all BLTE devices find in scan", category = PropertyCategory.BEHAVIOR)
	  public List<String> ScanRecords() { 
	    List<String> listOfScanRecords = new ArrayList<String>();
	    BluetoothDevice nextDevice;

	    if (mScannedDevices.size() == 0) {
	      LogMessage("Did not find any devices in scan", "i");
	    } else { 
	      for (int loop1 = 0; loop1 < mScannedDevices.size(); loop1++) {
	        nextDevice = mScannedDevices.get(loop1).getDevice();
	        if (nextDevice != null) { 
	          listOfScanRecords.add(mScannedDevices.get(loop1).getScanRecord());
	          LogMessage("Adding scan record to list: " + mScannedDevices.get(loop1).getScanRecord(), "i");
	        }
	      }
	    }

	    return listOfScanRecords;
	  }

	  /**
	   * Allows to access of RSSI found in scan
	   */
	  @SimpleProperty(description = "Lists the RSSI of all BLTE devices find in scan", category = PropertyCategory.BEHAVIOR)
	  public List<Integer> ScanRSSI() { 
	    List<Integer> listOfRSSI = new ArrayList<Integer>();
	    BluetoothDevice nextDevice;

	    if (mScannedDevices.size() == 0) {
	      LogMessage("Did not find any devices in scan", "i");
	    } else { 
	      for (int loop1 = 0; loop1 < mScannedDevices.size(); loop1++) {
	        nextDevice = mScannedDevices.get(loop1).getDevice();
	        if (nextDevice != null) { 
	          listOfRSSI.add(mScannedDevices.get(loop1).getRssi());
	          LogMessage("Adding scan RSSI to list: " + mScannedDevices.get(loop1).getRssi(), "i");
	        }
	      }
	    }

	    return listOfRSSI;
	  }

	  /**
	   * Allows to access a list of Devices found in the Scan
	   */
	  @SimpleProperty(description = "Lists the BLTE devices", category = PropertyCategory.BEHAVIOR)
	  public List<String> AddressesAndNames() { 
	    List<String> listOfBTLEDevices = new ArrayList<String>();
	    String deviceName;
	    BluetoothDevice nextDevice;
	    int foundCount = 0;

	    if (mScannedDevices.size() == 0) {
	      listOfBTLEDevices.add("No devices found");
	      LogMessage("Did not find any devices to connect", "i");
	    } else { 
	      LogMessage("Finding names in " + mScannedDevices.size() + " devices", "i");
	      for (int loop1 = 0; loop1 < mScannedDevices.size(); loop1++) {
	        nextDevice = mScannedDevices.get(loop1).getDevice();
	        if (nextDevice != null) { 
	          //deviceName = GetDeviceNameAndAddress(nextDevice) + mScannedDevices.get(loop1).getScanRecord();
	          deviceName = GetDeviceNameAndAddress(nextDevice);
	          listOfBTLEDevices.add(deviceName);
	          foundCount++;
	        }
	      }
	      if (foundCount == 0) {
	        listOfBTLEDevices.add("All devices are null");
	      }
	    }

	    return listOfBTLEDevices;
	  }

	  /**  URGENT -- missing all the onResume() onPause() onStop() methods to cleanup
	   *   the connections during Life-cycle of app
	   *
	   *
	   */

	  // 
	  public void onResume() {
	    LogMessage("Resuming the WICED Sense component", "i");
	  }

	  public void onPause() {
	    LogMessage("Calling onPause()", "i");
	    //Log.d(TAG, "OnPause method started.");
	    //if (nfcAdapter != null) {
	    //  GingerbreadUtil.disableNFCAdapter(activity, nfcAdapter);
	    //}
	    //nfcAdapter.disableForegroundDispatch(activity);
	  }

	  @Override
	  public void onDelete() {
	    LogMessage("Deleting the WICED Sense component", "i");
	    if (mBluetoothGatt != null) { 
	      mBluetoothGatt.close();
	    }
	  }

	  @Override
	  public void onStop() {
	    LogMessage("Calling onStop()", "i");

	    // Force a disconnect on Stop
	    if (mRunInBackground) { 
	      LogMessage("Continuing to run in the background", "i");
	    } else { 
	      LogMessage("Auto-disconnecting device from onStop()", "i");
	      Disconnect();
	    }

	  }

	}
