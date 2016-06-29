package com.example.raghvendrat.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

/*** Created by raghvendrat on 26-06-2016.*/

public class BluetoothActivity extends Activity {
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          String action = intent.getAction();
          //Bluetooth device found
          if(BluetoothDevice.ACTION_FOUND.equals(action)){
              // Get bluetooth device object from intent
              BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
              // Add name/address to show in array adapter in List View
              adapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
          }
        }
    };

    private BluetoothAdapter bluetoothAdapter;
    private ToggleButton toggleButton;
    private ListView listView;
    private ArrayAdapter adapter;
    private static final int ENABLE_BT_REQ_CODE = 1;
    private static final int DISCOVERABLE_BT_REQ_CODE = 2;
    private static final int DISCOVERABLE_DURATION = 300;

   protected void onCreate(Bundle savedInstanceState){
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_bluetooth);

     toggleButton = (ToggleButton)findViewById(R.id.toggleButton);
     listView = (ListView)findViewById(R.id.listView);
     adapter = new ArrayAdapter(this , android.R.layout.simple_list_item_1);
     listView.setAdapter(adapter);
     bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
   }

   public void onToggleClicked(View view){
     adapter.clear();
     toggleButton = (ToggleButton) view;

     if( bluetoothAdapter == null){  // Device doesnt support blth
           Toast.makeText(getApplicationContext() , "Device doesn't support bluetooth",
                   Toast.LENGTH_SHORT).show();
           toggleButton.setChecked(false);
     }
     else{
         if( toggleButton.isChecked()){ // Turn on Blth
              if(!bluetoothAdapter.isEnabled()){
                  //Dialog asking user permission to enable blth
                  Intent enableBlthIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                  startActivityForResult(enableBlthIntent, ENABLE_BT_REQ_CODE);
              }
              else{
                  Toast.makeText(getApplicationContext(), "Your device has been enabled." + "\n" + "Scanning for remote bluetooth devices..." , Toast.LENGTH_SHORT).show();
                  // discover remote Blth devices
                  discover_Devices();
                  //make local devices discoverable by other devices
                 makeDiscoverable();
              }
         }
         else{   // turn off bluetooth
             bluetoothAdapter.disable();
             adapter.clear();
             Toast.makeText(getApplicationContext() , "Your device is now disabled.",
                     Toast.LENGTH_SHORT).show();
         }
     }
   }

   public void onActivityResult(int req_Code , int result_Code ,Intent data){
        if( req_Code == ENABLE_BT_REQ_CODE){
           // Bluetooth successfully enabled
               if(result_Code == Activity.RESULT_OK){
                  Toast.makeText(getApplicationContext() , "Bluetooth is now enabled." +
                  "\n" + "Scanning for remote bluetooth devices.." , Toast.LENGTH_SHORT).show();

                   // make local device discoverable by other devices
                   makeDiscoverable();
                   // discover renote bluetooth devices
                   discover_Devices();
               }
               else { // failed to enable bluetooth
               Toast.makeText(getApplicationContext(),"Bluetooth is not enabled.",
                       Toast.LENGTH_SHORT).show();
               toggleButton.setChecked(false);
               }
        }
        else if(req_Code == DISCOVERABLE_BT_REQ_CODE) {
                if (result_Code == DISCOVERABLE_BT_REQ_CODE) {
                    Toast.makeText(getApplicationContext(), "Your device is now discoverable by other devices for " +
                            DISCOVERABLE_DURATION + "seconds", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Fail to enable discoverability on your device.", Toast.LENGTH_SHORT).show();
                }
        }
   }
   private void discover_Devices(){
   // scan for remote blth devices
   if(bluetoothAdapter.startDiscovery()){
    Toast.makeText(getApplicationContext() ,"Discoevring bluetooth devices.. ",
           Toast.LENGTH_SHORT).show();
    }
   else{
    Toast.makeText(getApplicationContext() , "Discovery failed to start." ,
           Toast.LENGTH_SHORT).show();
    }
   }
   private void makeDiscoverable(){
     // make local device discoverable
     Intent discoverableIntent  = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
     discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION ,DISCOVERABLE_DURATION );
     startActivityForResult(discoverableIntent, DISCOVERABLE_BT_REQ_CODE);
   }
   protected void onResume(){
      super.onResume();
      // register the broadcast receiver for ACTION_FOUND
       IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
       this.registerReceiver(broadcastReceiver , filter);
   }
   protected void onPause(){
      super.onPause();
       this.unregisterReceiver(broadcastReceiver);
   }
}
