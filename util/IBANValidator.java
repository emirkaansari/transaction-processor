package com.playtech.assignment.util;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IBANValidator {

    public static boolean isValidIBAN(String accountNumber, String country) {
        accountNumber = accountNumber.replaceAll("\\s+", "").toUpperCase();

        if (!isValidFormat(accountNumber)) {
            return false;
        }

        String ibanCountryCode = accountNumber.substring(0, 2);
        if (!ibanCountryCode.equals(country.toUpperCase())) {
            return false;
        }

        
        String rearrangedIban = accountNumber.substring(4) + accountNumber.substring(0, 4);

        
        StringBuilder numericIban = new StringBuilder();
        for (int i = 0; i < rearrangedIban.length(); i++) {
            numericIban.append(Character.digit(rearrangedIban.charAt(i), 36));
        }

        
        BigInteger ibanNumber = new BigInteger(numericIban.toString());
        return ibanNumber.mod(BigInteger.valueOf(97)).intValue() == 1;
    }

    private static boolean isValidFormat(String accountNumber) {
        String ibanPattern = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{2,}$";
        Pattern pattern = Pattern.compile(ibanPattern);
        Matcher matcher = pattern.matcher(accountNumber);
        return matcher.matches();
    }
}