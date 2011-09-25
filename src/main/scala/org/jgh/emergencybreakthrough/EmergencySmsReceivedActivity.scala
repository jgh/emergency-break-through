package org.jgh.emergencybreakthrough

import _root_.android.os.Bundle
import android.media.{AudioManager, MediaPlayer}
import android.app.Activity
import android.app.AlertDialog.Builder
import android.net.Uri
import android.util.Log
import android.content.{Intent, DialogInterface, Context}
import android.content.DialogInterface.{OnDismissListener, OnClickListener}

class EmergencySmsReceivedActivity extends Activity {

  def setRingerModeToNormal {
    val audioManager = getSystemService(Context.AUDIO_SERVICE).asInstanceOf[AudioManager];
    val ringerMode = audioManager.getRingerMode
    if (ringerMode != AudioManager.RINGER_MODE_NORMAL) {
      audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL)
    }
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    val phoneNumber = getIntent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
    val name =  getIntent.getStringExtra("display_name")

    setRingerModeToNormal

    val mp = MediaPlayer.create(this, R.raw.police);
    mp.start();

    val builder = new Builder(this)
    builder.setTitle(R.string.alert_title)
    builder.setMessage("Sender: " + name)
    builder.setPositiveButton(R.string.call, new OnClickListener {
      def onClick(p1: DialogInterface, p2: Int) {
        Log.w("EmergencyBreakThrough", "Dialog: Call clicked")
        val uri = Uri.parse("tel:" + phoneNumber);
        val intent: Intent = new Intent(Intent.ACTION_DIAL, uri)
        startActivity(intent)
      }
    })

    builder.setNeutralButton(R.string.stop_siren, new OnClickListener {
      def onClick(p1: DialogInterface, p2: Int) {
        Log.w("EmergencyBreakThrough", "Dialog closed: Stop siren.")
      }
    })

    val dialog = builder.create()
    dialog.setOnDismissListener(new OnDismissListener {
      def onDismiss(p1: DialogInterface) {
        mp.stop();
        mp.release();
        finish()
        Log.w("EmergencyBreakThrough", "Dialog dismissed.")
      }
    })
    dialog.show()
  }
}