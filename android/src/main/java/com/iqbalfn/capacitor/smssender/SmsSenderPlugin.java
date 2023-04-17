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

    private static final String SMS_SENT = "SMS_SENT";
    private static final String SMS_DELIVERED = "SMS_DELIVERED";

    @Override
    public void load() {
        BroadcastReceiver sendBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                JSObject ret = new JSObject();
                ret.put("id", intent.getExtras().getInt("id"));
                ret.put("res_status", getResultCode());

                if (getResultCode() == Activity.RESULT_OK) {
                    ret.put("status", "SENT");
                } else {
                    ret.put("status", "FAILED");
                }

                notifyListeners("smsSenderStatusUpdated", ret);
            }
        };

        BroadcastReceiver delvBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                JSObject ret = new JSObject();
                ret.put("id", intent.getExtras().getInt("id"));
                ret.put("res_status", getResultCode());

                if (getResultCode() == Activity.RESULT_OK) {
                    ret.put("status", "DELIVERED");
                } else {
                    ret.put("status", "FAILED");
                }

                notifyListeners("smsSenderStatusUpdated", ret);
            }
        };

        getContext().registerReceiver(sendBR, new IntentFilter(SMS_SENT));
        getContext().registerReceiver(delvBR, new IntentFilter(SMS_DELIVERED));
    }

    @PluginMethod
    public void send(PluginCall call) {
        if (!hasRequiredPermissions()) {
            call.reject("Requested permission is not granted");
            return;
        }

        int sim = call.getInt("sim", 0);
        int id = call.getInt("id");
        String text = call.getString("text");
        String phone = call.getString("phone");

        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        Intent iSMSSent = new Intent(SMS_SENT);
        iSMSSent.putExtra("id", id);
        PendingIntent sentPI = PendingIntent.getBroadcast(getContext(), 0, iSMSSent, flags);

        Intent iSMSDelivered = new Intent(SMS_DELIVERED);
        iSMSDelivered.putExtra("id", id);
        PendingIntent delvPI = PendingIntent.getBroadcast(getContext(), 0, iSMSDelivered, flags);

        SmsManager manager;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            manager = SmsManager.getSmsManagerForSubscriptionId(sim);
        } else {
            manager = getContext()
                    .getSystemService(SmsManager.class)
                    .createForSubscriptionId(sim);
        }

        manager.sendTextMessage(phone, null, text, sentPI, delvPI);

        JSObject ret = new JSObject();
        ret.put("id", id);
        ret.put("status", "PENDING");
        call.resolve(ret);
    }
}
