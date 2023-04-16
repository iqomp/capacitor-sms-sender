package com.iqbalfn.capacitor.smssender;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.telephony.SmsManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

@CapacitorPlugin(
        name = "SmsSender",
        permissions = {
                @Permission(
                        alias = "send_sms",
                        strings = {Manifest.permission.SEND_SMS}
                ),
                @Permission(
                        alias = "read_phone_state",
                        strings = {Manifest.permission.READ_PHONE_STATE}
                )
        }
)
public class SmsSenderPlugin extends Plugin {
    @PluginMethod
    public void send(PluginCall call) {
        if (!hasRequiredPermissions()) {
            call.reject("Requested permission not granted");
            return;
        }

        int sim = call.getInt("sim", 0);
        int id = call.getInt("id");
        String text = call.getString("text");
        String phone = call.getString("phone");

        JSObject ret = new JSObject();
        ret.put("id", id);

        String SMS_SENT = "SMS_SENT";
        String SMS_DELIVERED = "SMS_DELIVERED";

        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        PendingIntent sentPI = PendingIntent.getBroadcast(getContext(), 0, new Intent(SMS_SENT), flags);
        PendingIntent delvPI = PendingIntent.getBroadcast(getContext(), 0, new Intent(SMS_DELIVERED), flags);

        BroadcastReceiver sendBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        ret.put("status", "SENT");
                        break;
                    default:
                        ret.put("status", "FAILED");
                }

                call.resolve(ret);
            }
        };

        BroadcastReceiver delvBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                JSObject ret = new JSObject();

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        ret.put("status", "DELIVERED");
                        break;
                    default:
                        ret.put("status", "FAILED");
                }

                ret.put("id", id);
                notifyListeners("smsSenderDelivered", ret);
            }
        };

        getContext().registerReceiver(sendBR, new IntentFilter(SMS_SENT));
        getContext().registerReceiver(delvBR, new IntentFilter(SMS_DELIVERED));

        SmsManager manager;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            manager = SmsManager.getDefault();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            manager = SmsManager.getSmsManagerForSubscriptionId(sim);
        } else {
            manager = getContext().getSystemService(SmsManager.class)
                    .createForSubscriptionId(sim);
        }

        manager.sendTextMessage(phone, null, text, sentPI, delvPI);
    }
}
