package org.telegram.ui.Wallet.gifttheme;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Small static helpers called from the places listed in README.md ("integration points").
 * They only read the current {@link GiftTheme}.
 */
public final class GiftThemeHooks {
    private GiftThemeHooks() { }

    /** Colour of the gram texts printed on the card (balance), replaces the hard-coded blue. */
    public static int cardGramText() { return GiftTheme.current().isDefault() ? 0xFF6DDCFF : GiftTheme.current().palette.gramText; }

    /** Colour of the USD line under the balance on the card. */
    public static int cardUsdText() { return GiftTheme.current().isDefault() ? 0xFF87EFFF : GiftTheme.current().palette.usdText; }

    /** Accent of the wallet buttons ("Add funds", "Send", "OK" of the transaction sheet). */
    public static int accent() { return GiftTheme.current().palette.accent; }

    /** Small gram icon (history amounts, transaction details), 16 dp. */
    public static Drawable smallGram(Context c, int sizePx) {
        Drawable d = c.getResources().getDrawable(GiftTheme.current().gramSmallRes).mutate();
        d.setBounds(0, 0, sizePx, sizePx);
        return d;
    }

    /** Round transfer icons use a blue gradient: recolour only the blue ones. */
    public static int circleTop(int original) { return recolour(original, GiftTheme.current().palette.circleTop); }

    public static int circleBottom(int original) { return recolour(original, GiftTheme.current().palette.circleBottom); }

    private static int recolour(int original, int themed) {
        if (GiftTheme.current().isDefault()) return original;
        boolean blue = (original & 0xFF) > ((original >> 16) & 0xFF) + 30;
        return blue ? themed : original;
    }
}
