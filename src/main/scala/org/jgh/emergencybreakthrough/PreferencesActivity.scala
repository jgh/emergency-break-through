package org.jgh.emergencybreakthrough

import android.os.Bundle
import android.preference.{Preference, PreferenceActivity}
import android.preference.Preference.{OnPreferenceClickListener, OnPreferenceChangeListener}
import android.content.Intent
import android.app.AlertDialog

class PreferencesActivity extends PreferenceActivity {

   override  def onCreate(savedInstanceState:Bundle ) {
     super.onCreate(savedInstanceState);
     addPreferencesFromResource(R.xml.preferences);

     //todo:validate  not  empty  string
     val emergencyPrefix = new Preferences(this).getEmergencyPrefix()
     val prefix = findPreference("prefix_preference")
     val  ctx = this
     prefix.setOnPreferenceChangeListener(
       new OnPreferenceChangeListener() {
         def onPreferenceChange(pref:Preference , newValue:Any):Boolean = {
            newValue  match  {
              case  ""  => {
                  val builder = new AlertDialog.Builder(ctx);
                  builder.setTitle("Invalid Input");
                  builder.setMessage("The prefix can not be empty.");
                  builder.setPositiveButton(android.R.string.ok, null);
                  builder.show();
                  false;
              }
              case  _  => true;
            }
         }
     })

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
     val whitelist = findPreference("whitelist_preference")
     val blacklist = findPreference("blacklist_preference")
     def enableListPreferences(newValue: AnyRef) {
       whitelist.setEnabled(newValue.equals("whitelist"))
       blacklist.setEnabled(newValue.equals("blacklist"))
     }
     val mode = findPreference("sender_mode_preference")
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