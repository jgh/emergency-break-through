package org.jgh.emergencybreakthrough

import android.os.Bundle
import android.preference.{Preference, PreferenceActivity}
import android.preference.Preference.{OnPreferenceClickListener, OnPreferenceChangeListener}
import android.content.Intent

class PreferencesActivity extends PreferenceActivity {

   override  def onCreate(savedInstanceState:Bundle ) {
     super.onCreate(savedInstanceState);
     addPreferencesFromResource(R.xml.preferences);
      //todo: disable/enable lists based on mode

     val emergencyPrefix = new Preferences(this).getEmergencyPrefix()
     val sms = findPreference("sms_preference")
     sms.setOnPreferenceClickListener(new OnPreferenceClickListener {
       def onPreferenceClick(p1: Preference) = {
         val intent: Intent = new Intent(Intent.ACTION_SEND)
         intent.setType("text/plain")
         intent.putExtra(Intent.EXTRA_TEXT, getResources.getString(R.string.send_sms_template, emergencyPrefix ))
         startActivity(intent)
         true

       }
     })
     val mode = findPreference("sender_mode_preference")
     val bl = findPreference("whitelist_preference")
     val wl = findPreference("blacklist_preference")
     def enableListPreferences(newValue: AnyRef) {
       wl.setEnabled(newValue.equals("whitelist"))
       bl.setEnabled(newValue.equals("blacklist"))
     }
     mode.setOnPreferenceChangeListener(new OnPreferenceChangeListener {
       def onPreferenceChange(p: Preference, newValue: AnyRef) = {
         enableListPreferences(newValue)
         true
       }
     })

     val currentValue = mode.getSharedPreferences.getString("sender_mode_preference", "anybody")
     enableListPreferences(currentValue)
   }
}