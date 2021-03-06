package co.gounplugged.unpluggeddroid.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.gounplugged.unpluggeddroid.exceptions.InvalidPhoneNumberException;

/**
 * Created by Marvin Arnold on 27/04/15.
 */
public class PhoneNumberParser {
    // From: https://stackoverflow.com/questions/2113908/what-regular-expression-will-match-valid-international-phone-numbers

    public static String PHONE_NUMBER_REGEX = "(\\+(9[976]\\d|8[987530]\\d|6[987]\\d|5[90]\\d|42\\d|3[875]\\d|\n" +
            "2[98654321]\\d|9[8543210]|8[6421]|6[6543210]|5[87654321]|\n" +
            "4[987654310]|3[9643210]|2[70]|7|1))(\\d{1,14})";

    public static String parsePhoneNumber(String fullNumber) throws InvalidPhoneNumberException {
        return parseGroup(fullNumber, 3);
    }

    public static String parseCountryCode(String fullNumber) throws InvalidPhoneNumberException{
        return parseGroup(fullNumber, 1);
    }

    private static String parseGroup(String fullNumber, int group) throws  InvalidPhoneNumberException {
        fullNumber = sanitizePhoneNumber(fullNumber);
        if(isValidFullPhoneNumber(fullNumber)) return splitOnCountryCode(fullNumber, group);
        throw new InvalidPhoneNumberException("malformed number");
    }

    private static String splitOnCountryCode(String sanitizedFullNumber, int group) {
        Matcher m = Pattern.compile(PHONE_NUMBER_REGEX).matcher(sanitizedFullNumber);
        m.matches();
        return m.group(group);
    }

    public static String sanitizePhoneNumber(String number)  {
        number = number.replaceAll("\\(", "");
        number = number.replaceAll("\\)", "");
        number = number.replaceAll("-", "");
        number = number.replaceAll(" ", "");

        return number;
    }

    public static boolean isValidFullPhoneNumber(String number) {
        return number.matches("^" + PHONE_NUMBER_REGEX + "$");
    }

    /**
     * Try to fix number by trying the in the following order:
     * 1) Sanitizing
     * 2) Adding + if that would make number begin with expected country code
     * 3) Adding the expected country code
     * 4) Adding +
     * @param number
     * @param expectedCountryCode
     * @return
     * @throws InvalidPhoneNumberException
     */
    public static String makeValid(String number, String expectedCountryCode) throws  InvalidPhoneNumberException {
        number = sanitizePhoneNumber(number);
        if(isValidFullPhoneNumber(number)) return number;

        String newNumber = "+" + number;
        if(numberMatchesCountryCode(newNumber, expectedCountryCode) &&
                isValidFullPhoneNumber(newNumber)) return  newNumber;

        newNumber =  expectedCountryCode + number;
        if(isValidFullPhoneNumber(newNumber)) return newNumber;

        newNumber = "+" + number;
        if(isValidFullPhoneNumber(newNumber)) return newNumber;
        throw new InvalidPhoneNumberException("Malformed phone number");
    }

    public static boolean numberMatchesCountryCode(String number, String countryCode) {
        return number.matches("^\\" + countryCode + "\\d*$"); // country code contains '+' which messes w/ country code
    }
}
