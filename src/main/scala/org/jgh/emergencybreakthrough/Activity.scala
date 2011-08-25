package org.jgh.emergencybreakthrough

import _root_.android.os.Bundle
import _root_.android.widget.TextView
import android.media.{AudioManager, MediaPlayer}
import android.content.res.Resources
import android.app.{AlertDialog, Activity}
import android.app.AlertDialog.Builder
import android.content.{DialogInterface, Context, Intent}
import android.net.Uri
import android.provider.ContactsContract.{Contacts, PhoneLookup}
import android.provider.ContactsContract
import android.database.Cursor
import android.util.Log
import android.telephony.PhoneNumberUtils
import android.content.DialogInterface.{OnClickListener, OnCancelListener}
import android.provider.Contacts.PeopleColumns

class MainActivity extends Activity {
  def setRingerModeToNormal {
    val audioManager = getSystemService(Context.AUDIO_SERVICE).asInstanceOf[AudioManager];
    val ringerMode = audioManager.getRingerMode
    if (ringerMode != AudioManager.RINGER_MODE_NORMAL) {
      audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL)
    }
  }

  def lookupContactName(phoneNumber: String):String = {
    val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
    val array = List(PeopleColumns.DISPLAY_NAME)
    val cursor = managedQuery(uri, array.toArray, null, null, null)

    if (cursor.moveToFirst()) {
        // Get the field values
        val name = cursor.getString(cursor.getColumnIndex(PeopleColumns.DISPLAY_NAME))
        Log.w("EmergencyBreakThrough", "Contact:" + name)
        name
    } else {
      Log.w("EmergencyBreakThrough", "No contact found for:" + phoneNumber)
      phoneNumber
    }
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    val phoneNumber = getIntent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)

    setContentView(new TextView(this) {
      setText(getIntent.getStringExtra(Intent.EXTRA_TEXT) + " [" + phoneNumber + "].")
    })

    setRingerModeToNormal

    val mp = MediaPlayer.create(this, R.raw.police);
    mp.start();


    val name = lookupContactName(phoneNumber)


    val dialog = new Builder(this)
    dialog.setTitle(R.string.alert_title)
    dialog.setMessage("You have received an emergency text from: " + name)
    dialog.setNeutralButton("Stop Siren",new OnClickListener {
      def onClick(p1: DialogInterface, p2: Int) {
        mp.stop();
        mp.release();
        Log.w("EmergencyBreakThrough", "Dialog closed.")

      }
    })
    dialog.show()
  }
}