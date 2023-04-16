import type { PluginListenerHandle } from '@capacitor/core';
import type { PermissionState } from '@capacitor/core';

export interface PermissionStatus {
    /**
     * SEND_SMS Permission
     * @since 1.0.0
     */
    send_sms: PermissionState,

    /**
     * READ_PHONE_STATE Permission
     * @since 1.0.0
     */
    read_phone_state: PermissionState
}

export interface SmsSenderOptions {
    /**
     * SMS Message id
     * @since 1.0.0
     */
    id: number,

    /**
     * Device SIM index
     * @since 1.0.0
     */
    sim: number,

    /**
     * Target phone number
     * @since 1.0.0
     */
    phone: string,

    /**
     * SMS Message
     * @since 1.0.0
     */
    text: string
}

export interface SmsSenderResult {
    /**
     * Message SMS id
     * @since 1.0.0
     */
    id: number,

    /**
     * Delivery status, possible value are 'FAILED', 'SENT', or 'DELIVERED'
     * @since 1.0.0
     */
    status: string
}

export interface SmsSenderPlugin {
    /**
     * Send a text sms to a number from a specified sim index
     * @since 1.0.0
     */
    send(opts: SmsSenderOptions): Promise<SmsSenderResult>;

    /**
     * Check status of permissions
     * @since 1.0.0
     */
    checkPermissions(): Promise<PermissionStatus>;

    /**
     * Request the required permissions
     * @sincei 1.0.0
     */
    requestPermissions(): Promise<PermissionStatus>;

    /**
     * Listen for sms status update
     * @since 1.0.0
     */
    addListener(
        eventName: 'smsSenderDelivered',
        listenerFunc: (result: SmsSenderResult) => void
    ): Promise<PluginListenerHandle> & PluginListenerHandle;

    /**
     * Remove all registered listeners
     * @since 1.0.0
     */
    removeAllListeners(): Promise<void>;
}
