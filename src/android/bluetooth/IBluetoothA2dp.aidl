/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * System private API for Bluetooth A2DP service
 *
 * {@hide}
 */
interface IBluetoothA2dp {
    boolean connectSink(in BluetoothDevice device); // Pre API 11 only
    boolean disconnectSink(in BluetoothDevice device); // Pre API 11 only
    boolean connect(in BluetoothDevice device); // API 11 and up only
    boolean disconnect(in BluetoothDevice device); // API 11 and up only
    boolean suspendSink(in BluetoothDevice device); // all
    boolean resumeSink(in BluetoothDevice device); // all
    BluetoothDevice[] getConnectedSinks();  // change to Set<> once AIDL supports, pre API 11 only
    BluetoothDevice[] getNonDisconnectedSinks();  // change to Set<> once AIDL supports, 
    int getSinkState(in BluetoothDevice device); // pre API 11 only
    int getConnectionState(in BluetoothDevice device); // API 11 and up
    boolean setSinkPriority(in BluetoothDevice device, int priority); // Pre API 11 only
    boolean setPriority(in BluetoothDevice device, int priority); // API 11 and up only
    int getPriority(in BluetoothDevice device); // API 11 and up only
    int getSinkPriority(in BluetoothDevice device); // Pre API 11 only
    boolean isA2dpPlaying(in BluetoothDevice device); // API 11 and up only
}
