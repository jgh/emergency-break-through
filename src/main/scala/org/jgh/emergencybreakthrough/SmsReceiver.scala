package org.jgh.emergencybreakthrough

import android.telephony.SmsMessage
import android.os.Bundle
import android.util.Log
import android.content.{Intent, Context, BroadcastReceiver}
import android.media.MediaPlayer
import java.lang.Math
import android.preference.PreferenceManager
import org.jgh.emergencybreakthrough.PreferencesActivity
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup
import android.provider.BaseColumns
import android.provider.Contacts.PeopleColumns

class SmsReceiver extends  BroadcastReceiver {

  def extractMessageFromIntent(smsReceivedIntent: Intent):SmsMessage = {
    val bundle: Bundle = smsReceivedIntent.getExtras
    val pdus = bundle.get("pdus").asInstanceOf[Array[AnyRef]]
    SmsMessage.createFromPdu(pdus(0).asInstanceOf[Array[Byte]])
  }

  def openActivity(context: Context, messageBody: String, number:String, name: String) {
    val intent = new Intent(context, classOf[EmergencySmsReceivedActivity])
    intent.putExtra(Intent.EXTRA_TEXT, messageBody)
    intent.putExtra(Intent.EXTRA_PHONE_NUMBER, number)
    intent.putExtra("display_name", name)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
  }

  def onReceive(context: Context, smsReceivedIntent: Intent) {
    val preferences = new Preferences(context)
    if(!preferences.enabled) {
      return
    }

    val message = extractMessageFromIntent(smsReceivedIntent)
    val messageBody = message.getDisplayMessageBody

    val emergencyPrefix = preferences.getEmergencyPrefix()
    val messagePrefix = messageBody.substring(0, Math.min(messageBody.length(),  emergencyPrefix.length() ))
    if (!messagePrefix.equalsIgnoreCase(emergencyPrefix)) {
      return
    }

    val number = message.getOriginatingAddress
    if (preferences.activeForContact(lookupContact(context, number, BaseColumns._ID))) {
      val displayName = lookupContact(context, number, PeopleColumns.DISPLAY_NAME)
      openActivity(context, messageBody, number, displayName.getOrElse(number))
    }
  }

  def lookupContact(context:Context, phoneNumber: String, column: String):Option[String] = {
    val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
    val array = List(column)
    val cursor = context.getContentResolver.query(uri, array.toArray, null, null, null)

    try {
      if (cursor.moveToFirst()) {
        // Get the field values
        val name = cursor.getString(cursor.getColumnIndex(column))
        Some(name)
      } else {
        None
      }
    }
    finally {
      cursor.close()
    }
  }
}