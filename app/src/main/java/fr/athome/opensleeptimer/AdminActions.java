package fr.athome.opensleeptimer;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;

public class AdminActions {
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    private DevicePolicyManager dpm;
    private ComponentName adminName;
    private Activity mainActivity;

    public AdminActions(DevicePolicyManager dpm, ComponentName adminName, Activity mainActivity) {
        this.dpm = dpm;
        this.adminName = adminName;
        this.mainActivity = mainActivity;
    }

    public void checkAdminRights() {
        if (!dpm.isAdminActive(adminName)) {
            // try to become active
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "I need admin rights");
            mainActivity.startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
        }
    }

    public void lockNow() {
        this.dpm.lockNow();
    }
}
