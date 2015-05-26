package co.gounplugged.unpluggeddroid.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import co.gounplugged.unpluggeddroid.db.DatabaseAccess;
import co.gounplugged.unpluggeddroid.exceptions.InvalidPhoneNumberException;
import co.gounplugged.unpluggeddroid.exceptions.NotFoundInDatabaseException;
import co.gounplugged.unpluggeddroid.models.Contact;
import co.gounplugged.unpluggeddroid.models.Profile;

public class ContactUtil extends DbUtil {
    private final static String TAG = "ContactUtil";

    public static List<Contact> getCachedContacts(Context context) {
        DatabaseAccess<Contact> databaseAccess  = new DatabaseAccess<>(context, Contact.class);
        return databaseAccess.getAll();
    }

    public static List<Contact> loadContacts(Context context) {
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        DatabaseAccess<Contact> contactAccess = new DatabaseAccess<>(context, Contact.class);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                String thumbnail = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Contact c = null;
                        try {
                            c = new Contact(name, phoneNo, lookupKey);
                            Log.d(TAG, "Adding Name: " + name + ", Phone No: " + phoneNo + ", Thumbnail: " + thumbnail);
                            contacts.add(c);
                            contactAccess.create(c);
                        } catch (InvalidPhoneNumberException e) {
                            try {
                                String userCountryCode  = PhoneNumberParser.parseCountryCode(Profile.getPhoneNumber());
                                String correctedPhoneNumber = PhoneNumberParser.makeValid(phoneNo, userCountryCode);
                                c = ContactUtil.create(context, name, correctedPhoneNumber, lookupKey);
                                Log.d(TAG, "Modified Name: " + name + ", Phone No: " + correctedPhoneNumber + ", Thumbnail: " + thumbnail);
                                contacts.add(c);
                            } catch (InvalidPhoneNumberException e1) {
                                Log.d(TAG, "Skipping Name: " + name + ", Phone No: " + phoneNo + ", Thumbnail: " + thumbnail);
                            }
                        }
                    }
                    pCur.close();
                }
            }
        }

        return contacts;
    }

    public static Contact getContact(Context context, String phoneNumber) throws NotFoundInDatabaseException {
        try {
            phoneNumber = PhoneNumberParser.parsePhoneNumber(phoneNumber);
        } catch (InvalidPhoneNumberException e) {
            e.printStackTrace();
        }
        DatabaseAccess<Contact> contactAccess = new DatabaseAccess<>(context, Contact.class);
        Contact contact = contactAccess.getFirstString("mPhoneNumber", phoneNumber);
        if (contact == null) throw new NotFoundInDatabaseException("Could not find a contact with that phone number");
        return contact;
    }

    public static Contact create(Context context, String name, String fullPhoneNumber, String lookupKey) throws InvalidPhoneNumberException {
        DatabaseAccess<Contact> contactAccess = new DatabaseAccess<>(context, Contact.class);
        Contact c = new Contact(name, fullPhoneNumber, lookupKey);
        contactAccess.create(c);
        return c;
    }

    public static Contact create(Context context, String name, String fullPhoneNumber) throws InvalidPhoneNumberException {
        return create(context, name, fullPhoneNumber, false);
    }

    public static Contact create(Context context, String name, String fullPhoneNumber, boolean usesSecondLine) throws InvalidPhoneNumberException {
        DatabaseAccess<Contact> contactAccess = new DatabaseAccess<>(context, Contact.class);
        Contact c = new Contact(name, fullPhoneNumber, usesSecondLine);
        contactAccess.create(c);
        return c;
    }

    public static Contact firstOrCreate(Context context, String name, String phoneNumber) throws InvalidPhoneNumberException {
        try {
            return getContact(context, phoneNumber);
        } catch (NotFoundInDatabaseException e) {
            return create(context, name, phoneNumber);
        }
    }

    public static int refresh(Context context, Contact contact) {
        return refresh(context, Contact.class, contact);
    }

    public static int update(Context context, Contact contact) {
        return update(context, Contact.class, contact);
    }

    public static List<Contact> getAll(Context context) {
        return getAll(context, Contact.class);
    }

    public static void deleteAll(Context context) {
        deleteAll(context, Contact.class);
    }
}
