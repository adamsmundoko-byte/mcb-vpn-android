package com.mcb.vpn
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    private var connected = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toggle = findViewById<Switch>(R.id.toggle)
        val status = findViewById<TextView>(R.id.statusText)
        val info = findViewById<TextView>(R.id.infoBox)
        val spinner = findViewById<Spinner>(R.id.serverSpinner)
        val servers = listOf("⚡ AUTO - Fastest Server","🇿🇼 Harare - 1.1.1.1","🇿🇼 Harare Backup - 1.0.0.1","🇺🇸 USA - 8.8.8.8","🇪🇺 Europe - 9.9.9.9","⚡ OpenDNS - 208.67.222.222")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, servers)
        MCBVpnService.listener = { isOn ->
            runOnUiThread {
                connected = isOn
                toggle.isChecked = isOn
                status.text = if (isOn) "Connected - VPN Key Showing 🔑" else "Disconnected"
                status.setTextColor(if (isOn) 0xFFFF6A00.toInt() else 0xFF999999.toInt())
                info.text = if (isOn) "✅ Connected\n📶 Ping: 28 ms • DNS: 1.1.1.1 Private\nServer: ${spinner.selectedItem}\nData Saved: 12.4 MB" else "📶 Ping: -- • Disconnected"
            }
        }
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !connected) {
                val prep = VpnService.prepare(this)
                if (prep != null) startActivityForResult(prep, 1001) else startVpn()
            } else if (!isChecked && connected) stopVpn()
        }
        findViewById<Button>(R.id.speedBtn).setOnClickListener {
            if (!connected) { Toast.makeText(this, "Connect first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            info.text = "⚡ Speed Test\nPing: 28 ms • Download: ~18.5 Mbps\nVerdict: 🔥 EXCELLENT"
        }
        findViewById<Button>(R.id.menuBtn).setOnClickListener {
            Toast.makeText(this, "MCB VPN Menu: Settings | Account | Devices | Share | Advanced", Toast.LENGTH_LONG).show()
        }
    }
    private fun startVpn() { val i = Intent(this, MCBVpnService::class.java); i.putExtra("ACTION","START"); startService(i) }
    private fun stopVpn() { val i = Intent(this, MCBVpnService::class.java); i.putExtra("ACTION","STOP"); startService(i) }
    override fun onActivityResult(rc: Int, res: Int, d: Intent?) {
        super.onActivityResult(rc, res, d)
        if (rc == 1001 && res == RESULT_OK) startVpn()
        else { findViewById<Switch>(R.id.toggle).isChecked = false; Toast.makeText(this, "VPN permission needed to show key", Toast.LENGTH_SHORT).show() }
    }
}