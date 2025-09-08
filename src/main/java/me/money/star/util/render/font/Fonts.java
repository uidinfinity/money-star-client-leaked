package me.money.star.util.render.font;



import me.money.star.MoneyStar;

import java.io.FileInputStream;
import java.io.IOException;

public class Fonts
{
    //
    public static final VanillaTextRenderer VANILLA = new VanillaTextRenderer();

    public static final String DEFAULT_FONT_FILE_PATH = "assets/money/star/font/verdana.ttf";
    public static String FONT_FILE_PATH = DEFAULT_FONT_FILE_PATH;

    public static AWTFontRenderer CLIENT;
    public static AWTFontRenderer CLIENT_UNSCALED;
    //
    public static float FONT_SIZE = 9.0f;

    private static boolean initialized;

    public static void init()
    {
        if (initialized)
        {
            return;
        }

        loadFonts();
        MoneyStar.info("Loaded fonts!");
        initialized = true;
    }

    public static void loadFonts()
    {
        try
        {
            CLIENT = new AWTFontRenderer(FONT_FILE_PATH.startsWith("assets") ?
                    MoneyStar.getResource(FONT_FILE_PATH) : new FileInputStream(FONT_FILE_PATH), FONT_SIZE);
            CLIENT_UNSCALED = new AWTFontRenderer(FONT_FILE_PATH.startsWith("assets") ?
                    MoneyStar.getResource(FONT_FILE_PATH) : new FileInputStream(FONT_FILE_PATH), 9.0f);
        }
        catch (IOException e)
        {

        }
    }

    public static void closeFonts()
    {
        CLIENT.close();
        CLIENT_UNSCALED.close();
    }

    public static void setSize(float size)
    {
        FONT_SIZE = size;


    }

    public static boolean isInitialized()
    {
        return initialized;
    }
}
