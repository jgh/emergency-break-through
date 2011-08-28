package org.jgh.emergencybreakthrough

import android.telephony.SmsMessage
import android.os.Bundle
import scala.collection.JavaConverters._
import collection.mutable.Set
import android.util.{DisplayMetrics, Log}
import android.content.{DialogInterface, Intent, Context, BroadcastReceiver}
import android.media.MediaPlayer
import android.app.{Dialog, AlertDialog}
import android.app.AlertDialog.Builder
import java.lang.Math

class SmsReceiver extends  BroadcastReceiver {

  var mp:MediaPlayer = null;

  def extractMessageFromIntent(smsReceivedIntent: Intent):SmsMessage = {
    val bundle: Bundle = smsReceivedIntent.getExtras
    val pdus = bundle.get("pdus").asInstanceOf[Array[AnyRef]]
    SmsMessage.createFromPdu(pdus(0).asInstanceOf[Array[Byte]])
  }

  def openActivity(context: Context, messageBody: String, number:String) {
    val intent = new Intent(context, classOf[EmergencySmsReceivedActivity])
    intent.putExtra(Intent.EXTRA_TEXT, messageBody)
    intent.putExtra(Intent.EXTRA_PHONE_NUMBER, number)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
  }

  def onReceive(context: Context, smsReceivedIntent: Intent) {
    val message = extractMessageFromIntent(smsReceivedIntent)
    val messageBody = message.getDisplayMessageBody
    val number = message.getOriginatingAddress

    Log.i("SmsReceiver", "SMS Received: " + messageBody)

    val emergencyPrefix = "emergency:"
    val messagePrefix = messageBody.substring(0, Math.min(messageBody.length(),  emergencyPrefix.length() ))
    if (messagePrefix.equalsIgnoreCase(emergencyPrefix)) {
      openActivity(context, messageBody, number)
    }
  }
}