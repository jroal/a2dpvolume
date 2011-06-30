/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\eclipse\\moto\\A2DP Volume\\src\\android\\bluetooth\\IBluetoothA2dp.aidl
 */
package android.bluetooth;
/**
 * System private API for Bluetooth A2DP service
 *
 * {@hide}
 */
public interface IBluetoothA2dp extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements android.bluetooth.IBluetoothA2dp
{
private static final java.lang.String DESCRIPTOR = "android.bluetooth.IBluetoothA2dp";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an android.bluetooth.IBluetoothA2dp interface,
 * generating a proxy if needed.
 */
public static android.bluetooth.IBluetoothA2dp asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof android.bluetooth.IBluetoothA2dp))) {
return ((android.bluetooth.IBluetoothA2dp)iin);
}
return new android.bluetooth.IBluetoothA2dp.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_connectSink:
{
data.enforceInterface(DESCRIPTOR);
android.bluetooth.BluetoothDevice _arg0;
if ((0!=data.readInt())) {
_arg0 = android.bluetooth.BluetoothDevice.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _result = this.connectSink(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_disconnectSink:
{
data.enforceInterface(DESCRIPTOR);
android.bluetooth.BluetoothDevice _arg0;
if ((0!=data.readInt())) {
_arg0 = android.bluetooth.BluetoothDevice.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _result = this.disconnectSink(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_suspendSink:
{
data.enforceInterface(DESCRIPTOR);
android.bluetooth.BluetoothDevice _arg0;
if ((0!=data.readInt())) {
_arg0 = android.bluetooth.BluetoothDevice.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _result = this.suspendSink(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_resumeSink:
{
data.enforceInterface(DESCRIPTOR);
android.bluetooth.BluetoothDevice _arg0;
if ((0!=data.readInt())) {
_arg0 = android.bluetooth.BluetoothDevice.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _result = this.resumeSink(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getConnectedSinks:
{
data.enforceInterface(DESCRIPTOR);
android.bluetooth.BluetoothDevice[] _result = this.getConnectedSinks();
reply.writeNoException();
reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
return true;
}
case TRANSACTION_getNonDisconnectedSinks:
{
data.enforceInterface(DESCRIPTOR);
android.bluetooth.BluetoothDevice[] _result = this.getNonDisconnectedSinks();
reply.writeNoException();
reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
return true;
}
case TRANSACTION_getSinkState:
{
data.enforceInterface(DESCRIPTOR);
android.bluetooth.BluetoothDevice _arg0;
if ((0!=data.readInt())) {
_arg0 = android.bluetooth.BluetoothDevice.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
int _result = this.getSinkState(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setSinkPriority:
{
data.enforceInterface(DESCRIPTOR);
android.bluetooth.BluetoothDevice _arg0;
if ((0!=data.readInt())) {
_arg0 = android.bluetooth.BluetoothDevice.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
int _arg1;
_arg1 = data.readInt();
boolean _result = this.setSinkPriority(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getSinkPriority:
{
data.enforceInterface(DESCRIPTOR);
android.bluetooth.BluetoothDevice _arg0;
if ((0!=data.readInt())) {
_arg0 = android.bluetooth.BluetoothDevice.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
int _result = this.getSinkPriority(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements android.bluetooth.IBluetoothA2dp
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public boolean connectSink(android.bluetooth.BluetoothDevice device) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((device!=null)) {
_data.writeInt(1);
device.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_connectSink, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean disconnectSink(android.bluetooth.BluetoothDevice device) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((device!=null)) {
_data.writeInt(1);
device.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_disconnectSink, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean suspendSink(android.bluetooth.BluetoothDevice device) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((device!=null)) {
_data.writeInt(1);
device.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_suspendSink, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean resumeSink(android.bluetooth.BluetoothDevice device) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((device!=null)) {
_data.writeInt(1);
device.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_resumeSink, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public android.bluetooth.BluetoothDevice[] getConnectedSinks() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.bluetooth.BluetoothDevice[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getConnectedSinks, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArray(android.bluetooth.BluetoothDevice.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// change to Set<> once AIDL supports

public android.bluetooth.BluetoothDevice[] getNonDisconnectedSinks() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.bluetooth.BluetoothDevice[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getNonDisconnectedSinks, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArray(android.bluetooth.BluetoothDevice.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// change to Set<> once AIDL supports

public int getSinkState(android.bluetooth.BluetoothDevice device) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((device!=null)) {
_data.writeInt(1);
device.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_getSinkState, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean setSinkPriority(android.bluetooth.BluetoothDevice device, int priority) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((device!=null)) {
_data.writeInt(1);
device.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeInt(priority);
mRemote.transact(Stub.TRANSACTION_setSinkPriority, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int getSinkPriority(android.bluetooth.BluetoothDevice device) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((device!=null)) {
_data.writeInt(1);
device.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_getSinkPriority, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_connectSink = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_disconnectSink = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_suspendSink = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_resumeSink = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getConnectedSinks = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getNonDisconnectedSinks = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getSinkState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_setSinkPriority = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getSinkPriority = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
}
public boolean connectSink(android.bluetooth.BluetoothDevice device) throws android.os.RemoteException;
public boolean disconnectSink(android.bluetooth.BluetoothDevice device) throws android.os.RemoteException;
public boolean suspendSink(android.bluetooth.BluetoothDevice device) throws android.os.RemoteException;
public boolean resumeSink(android.bluetooth.BluetoothDevice device) throws android.os.RemoteException;
public android.bluetooth.BluetoothDevice[] getConnectedSinks() throws android.os.RemoteException;
// change to Set<> once AIDL supports

public android.bluetooth.BluetoothDevice[] getNonDisconnectedSinks() throws android.os.RemoteException;
// change to Set<> once AIDL supports

public int getSinkState(android.bluetooth.BluetoothDevice device) throws android.os.RemoteException;
public boolean setSinkPriority(android.bluetooth.BluetoothDevice device, int priority) throws android.os.RemoteException;
public int getSinkPriority(android.bluetooth.BluetoothDevice device) throws android.os.RemoteException;
}
