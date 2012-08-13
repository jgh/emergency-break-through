package org.jgh.emergencybreakthrough

import android.content.Context
import android.preference.PreferenceManager

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy
 * Date: 16/09/11
 * Time: 8:47 PM
 * To change this template use File | Settings | File Templates.
 */

class Preferences(context:Context) {

  def getBlacklist(): Array[String] = {
    ContactPreference.parseStoredValue(sharedPreferences.getString("blacklist_preference", null))
  }

  def getWhitelist(): Array[String] = {
    ContactPreference.parseStoredValue(sharedPreferences.getString("whitelist_preference", null))
  }

  def activeForContact(contactId: Option[String]):Boolean = {
    val contactsOnly = sharedPreferences.getBoolean("contacts_preference", true)
    if (!contactId.isDefined && contactsOnly) return  false

    val mode = sharedPreferences.getString("sender_mode_preference", "anybody")
    mode match {
      case "blacklist" => !contactId.exists(getBlacklist().contains(_))
      case "whitelist" => contactId.exists(getWhitelist().contains(_))
      case _ =>  true;
    }
  }

  lazy val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
  val defaultPrefix: String = "emergency:"
  val prefixKey: String = "prefix_preference"

  def getEmergencyPrefix(): String = {
    sharedPreferences.getString(prefixKey, defaultPrefix)
  }

  def enabled():Boolean = {
    sharedPreferences.getBoolean("enabled_preference", true)
  }
}
