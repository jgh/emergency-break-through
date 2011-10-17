package org.jgh.emergencybreakthrough

import android.preference.ListPreference
import android.app.AlertDialog
import android.database.Cursor
import android.util.{Log, AttributeSet}
import android.content.{DialogInterface, Context}
import android.provider.Contacts.PeopleColumns
import android.net.Uri
import android.provider.{ContactsContract, BaseColumns}
import org.jgh.emergencybreakthrough.ContactPreference

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy
 * Date: 15/09/11
 * Time: 7:58 AM
 * To change this template use File | Settings | File Templates.
 */
object ContactPreference {

  def parseStoredValue(value : String): Array[String] = {
    if ("" == value || value == null) Array()
    else value.split(SEPARATOR)
  }

  def joinValues(selectedValues: Array[CharSequence]): String = {
    selectedValues.mkString(ContactPreference.SEPARATOR)
  }
  private val SEPARATOR: String = "|"
}
class ContactPreference(context:Context, attrs:AttributeSet) extends ListPreference(context, attrs) {

  protected override def onPrepareDialogBuilder(builder: AlertDialog.Builder): Unit = {
    val contactsCursor = lookupContacts()
    val entries = extractColumn(contactsCursor, PeopleColumns.DISPLAY_NAME).toArray
    setEntries(entries)
    setEntryValues(extractColumn(lookupContacts(), BaseColumns._ID).toArray)
    mClickedDialogEntryIndices =  restoreCheckedEntries()
    builder.setMultiChoiceItems(entries, mClickedDialogEntryIndices, new DialogInterface.OnMultiChoiceClickListener {
      def onClick(dialog: DialogInterface, which: Int, value : Boolean) {
        mClickedDialogEntryIndices(which) = value
      }
    })
  }

  def getStoredValues: Array[String] = {
    ContactPreference.parseStoredValue(getValue)
  }

  private def restoreCheckedEntries(): Array[Boolean] = {
    val entryValues: Array[CharSequence] = getEntryValues
    val values: Array[String] = getStoredValues

    if (!values.isEmpty ) {
      entryValues.map(values.contains(_))
    } else {
      Array.fill(entryValues.length) {false}
    }
  }

  protected override def onDialogClosed(positiveResult: Boolean) {
    val entryValues: Array[CharSequence] = getEntryValues
    if (positiveResult && entryValues != null) {
      val joinedValues = ContactPreference.joinValues(
        entryValues.zip(mClickedDialogEntryIndices).filter(_._2).map(_._1))
      if (callChangeListener(joinedValues)) {
        setValue(joinedValues)
      }
    }
  }

  private var mClickedDialogEntryIndices: Array[Boolean] = null
  def lookupContacts():Cursor = {
    val uri:Uri  = ContactsContract.Contacts.CONTENT_URI;
    val columns = List(PeopleColumns.DISPLAY_NAME,
      BaseColumns._ID);
    getContext.getContentResolver.query(uri, columns.toArray, null, null, null)
  }

  def extractColumn(cursor:Cursor, column:String):List[CharSequence] = {
    val columnIndex = cursor.getColumnIndex(column)
    def readRow(current:List[String]):List[String] = {
        val name: String = cursor.getString(columnIndex)
        Log.i("EmergencyBreakthrough", "Column: " + column + " = " + name)
      val result = name::current
      if (cursor.moveToNext()) {
        readRow( result)
      } else {
       result
      }
    }

    if (cursor.moveToFirst()) {
      readRow(List()).reverse
    } else {
      List()
    }
  }

}