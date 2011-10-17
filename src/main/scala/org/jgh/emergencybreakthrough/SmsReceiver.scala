package org.jgh.emergencybreakthrough

import android.telephony.SmsMessage
import android.os.Bundle
import java.lang.Math
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup
import android.provider.BaseColumns
import android.provider.Contacts.PeopleColumns
import compat.Platform
import android.app.{PendingIntent, Notification, NotificationManager}
import android.util.Log
import android.content.{Intent, Context, BroadcastReceiver}

class SmsReceiver extends  BroadcastReceiver {

  def extractMessageFromIntent(smsReceivedIntent: Intent):SmsMessage = {
    val bundle: Bundle = smsReceivedIntent.getExtras
    val pdus = bundle.get("pdus").asInstanceOf[Array[AnyRef]]
    SmsMessage.createFromPdu(pdus(0).asInstanceOf[Array[Byte]])
  }


  def openActivity(context: Context, messageBody: String, number:String, name: String, smsReceivedIntent:Intent) {
    val intent = new Intent(context, classOf[EmergencySmsReceivedActivity])
    intent.putExtra(Intent.EXTRA_TEXT, messageBody)
    intent.putExtra(Intent.EXTRA_PHONE_NUMBER, number)
    intent.putExtra("display_name", name)
//    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val contentIntent: PendingIntent = PendingIntent.getActivity(context, Platform.currentTime.toInt, intent, 0)

    val uri = smsReceivedIntent.getData
    Log.i("EmergencyBreakthough", "SMS URI: " + uri)
    var viewIntent: PendingIntent =  PendingIntent.getActivity(context, 0,
      new Intent(Intent.ACTION_VIEW, Uri.parse("sms:")), 0)

    val title = context.getString(R.string.alert_title)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager];
    //todo: replace icon.
    val notification = new Notification(R.drawable.ic_launcher, title, Platform.currentTime)
    notification.setLatestEventInfo(context, title , "Sender: " + name, viewIntent)
    notification.flags |= Notification.FLAG_AUTO_CANCEL
    notification.flags |= Notification.FLAG_INSISTENT
    notification.flags |= Notification.FLAG_SHOW_LIGHTS
    notification.fullScreenIntent = contentIntent

    notificationManager.notify(Platform.currentTime.asInstanceOf[Int], notification)

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
    val contactId = lookupContact(context, number, BaseColumns._ID)
    if (preferences.activeForContact(contactId)) {
      Log.i("EmergencyBreakthough", "Active for contact: " + contactId )
      val displayName = lookupContact(context, number, PeopleColumns.DISPLAY_NAME)
      openActivity(context, messageBody, number, displayName.getOrElse(number), smsReceivedIntent)
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