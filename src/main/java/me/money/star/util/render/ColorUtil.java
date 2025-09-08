package me.money.star.util.render;


import me.money.star.client.modules.client.Colors;

import java.awt.*;

public class ColorUtil {
    public static Color hslToColor(float f, float f2, float f3, float f4) {
        if (f2 < 0.0f || f2 > 100.0f) {
            throw new IllegalArgumentException("Color parameter outside of expected range - Saturation");
        }
        if (f3 < 0.0f || f3 > 100.0f) {
            throw new IllegalArgumentException("Color parameter outside of expected range - Lightness");
        }
        if (f4 < 0.0f || f4 > 1.0f) {
            throw new IllegalArgumentException("Color parameter outside of expected range - Alpha");
        }
        f %= 360.0f;
        float f5 = 0.0f;
        f5 = (double) f3 < 0.5 ? f3 * (1.0f + f2) : (f3 /= 100.0f) + (f2 /= 100.0f) - f2 * f3;
        f2 = 2.0f * f3 - f5;
        f3 = Math.max(0.0f, colorCalc(f2, f5, (f /= 360.0f) + 0.33333334f));
        float f6 = Math.max(0.0f, colorCalc(f2, f5, f));
        f2 = Math.max(0.0f, colorCalc(f2, f5, f - 0.33333334f));
        f3 = Math.min(f3, 1.0f);
        f6 = Math.min(f6, 1.0f);
        f2 = Math.min(f2, 1.0f);
        return new Color(f3, f6, f2, f4);
    }

    private static float colorCalc(float f, float f2, float f3) {
        if (f3 < 0.0f) {
            f3 += 1.0f;
        }
        if (f3 > 1.0f) {
            f3 -= 1.0f;
        }
        if (6.0f * f3 < 1.0f) {
            float f4 = f;
            return f4 + (f2 - f4) * 6.0f * f3;
        }
        if (2.0f * f3 < 1.0f) {
            return f2;
        }
        if (3.0f * f3 < 2.0f) {
            float f5 = f;
            return f5 + (f2 - f5) * 6.0f * (0.6666667f - f3);
        }
        return f;
    }
    public static int toARGB(int r, int g, int b, int a) {
        return new Color(r, g, b, a).getRGB();
    }

    public static int toRGBA(int r, int g, int b) {
        return ColorUtil.toRGBA(r, g, b, 255);
    }

    public static int toRGBA(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + b + (a << 24);
    }

    public static int toRGBA(float r, float g, float b, float a) {
        return  ColorUtil.toRGBA((int) (r * 255.0f), (int) (g * 255.0f), (int) (b * 255.0f), (int) (a * 255.0f));
    }

    public static Color rainbow(int delay) {
        double rainbowState = Math.ceil((double) (System.currentTimeMillis() + (long) delay) / 20.0);
        return Color.getHSBColor((float) ((rainbowState %= 360.0) / 360.0), Colors.getInstance().rainbowSaturation.getValue().floatValue() / 255.0f, Colors.getInstance().rainbowBrightness.getValue().floatValue() / 255.0f);
    }
    public static Color rainbowNew(int delay) {
        double rainbowState = Math.ceil((double) (System.currentTimeMillis() + (long) delay) / 20.0);
        return Color.getHSBColor((float) ((rainbowState %= 360.0) / 360.0), 75, Colors.getInstance().rainbowBrightness.getValue().floatValue() / 255.0f);
    }

    public static int toRGBA(float[] colors) {
        if (colors.length != 4) {
            throw new IllegalArgumentException("colors[] must have a length of 4!");
        }
        return  ColorUtil.toRGBA(colors[0], colors[1], colors[2], colors[3]);
    }

    public static int toRGBA(double[] colors) {
        if (colors.length != 4) {
            throw new IllegalArgumentException("colors[] must have a length of 4!");
        }
        return  ColorUtil.toRGBA((float) colors[0], (float) colors[1], (float) colors[2], (float) colors[3]);
    }

    public static int toRGBA(Color color) {
        return  ColorUtil.toRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
}
