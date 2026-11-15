package com.mcb.vpn
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
class MCBVpnService : VpnService() {
    companion object { var isRunning = false; var listener: ((Boolean)->Unit)? = null }
    private var tun: ParcelFileDescriptor? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getStringExtra("ACTION") == "STOP") { stopVpn(); return START_NOT_STICKY }
        if (intent?.getStringExtra("ACTION") == "START") { startVpn() }
        return START_STICKY
    }
    private fun startVpn() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel("mcb_vpn", "MCB VPN", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
            val n = Notification.Builder(this, "mcb_vpn").setContentTitle("MCB VPN Connected").setContentText("Your DNS is private and faster").setSmallIcon(android.R.drawable.ic_lock_lock).build()
            startForeground(1, n)
        }
        val b = Builder()
        b.setSession("MCB VPN")
        b.addAddress("10.0.0.2", 32)
        b.addDnsServer("1.1.1.1"); b.addDnsServer("1.0.0.1")
        b.addRoute("0.0.0.0", 0)
        b.setBlocking(true)
        try { tun = b.establish() } catch (e: Exception) { stopVpn(); return }
        isRunning = true; listener?.invoke(true)
        Thread { try { while (isRunning) { Thread.sleep(1000) } } catch (e: Exception) {} }.start()
    }
    private fun stopVpn() { isRunning = false; listener?.invoke(false); try { tun?.close() } catch (e: Exception) {}; tun = null; stopForeground(true); stopSelf() }
    override fun onDestroy() { stopVpn(); super.onDestroy() }
    override fun onRevoke() { stopVpn(); super.onRevoke() }
}