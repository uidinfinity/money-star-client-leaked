package me.money.star.util.math;


import me.money.star.util.traits.Util;

public class HexRandom implements Util
{
    private static final String HEX_CHARS = "0123456789abcdef";

    public static String generateRandomHex(int length)
    {
        StringBuilder hexString = new StringBuilder(length);
        for (int i = 0; i < length; i++)
        {
            int index = RANDOM.nextInt(HEX_CHARS.length());
            hexString.append(HEX_CHARS.charAt(index));
        }
        return hexString.toString();
    }
}
