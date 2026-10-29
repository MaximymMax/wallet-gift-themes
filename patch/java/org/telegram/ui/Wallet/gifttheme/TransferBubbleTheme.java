package org.telegram.ui.Wallet.gifttheme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.HashMap;

/**
 * Chat service message "You sent Mom 100 Grams": its card is drawn on the blue gradient bitmap
 * R.drawable.wallet_action_card_gradient. This class returns that gradient recoloured to a gift theme:
 *  - a transfer you sent uses YOUR current theme,
 *  - a transfer you received uses the theme of the SENDER (his current gift theme).
 */
public final class TransferBubbleTheme {
    private static final HashMap<String, Drawable> CACHE = new HashMap<>();

    private TransferBubbleTheme() { }

    /** Recoloured copy of the gradient for the given theme, or the original for the default look. */
    public static Drawable gradient(Context c, Drawable original, GiftTheme theme) {
        if (theme == null || theme.isDefault()) return original;
        Drawable cached = CACHE.get(theme.id);
        if (cached == null) {
            cached = new BitmapDrawable(c.getResources(), recolor(((BitmapDrawable) original).getBitmap(), theme));
            CACHE.put(theme.id, cached);
        }
        return cached.getConstantState().newDrawable().mutate();
    }

    /** Maps the luminance of the source gradient onto backdropEdge (dark) .. backdropCenter (light). */
    private static Bitmap recolor(Bitmap src, GiftTheme theme) {
        int w = src.getWidth(), h = src.getHeight();
        int[] px = new int[w * h];
        src.getPixels(px, 0, w, 0, 0, w, h);
        float lo = 1f, hi = 0f;
        float[] lum = new float[px.length];
        for (int i = 0; i < px.length; i++) {
            int c = px[i];
            float l = (0.3f * ((c >> 16) & 255) + 0.59f * ((c >> 8) & 255) + 0.11f * (c & 255)) / 255f;
            lum[i] = l;
            if ((c >>> 24) > 8) { lo = Math.min(lo, l); hi = Math.max(hi, l); }
        }
        for (int i = 0; i < px.length; i++) {
            float f = hi > lo ? Math.max(0f, Math.min(1f, (lum[i] - lo) / (hi - lo))) : 0.5f;
            int rgb = GiftTheme.mix(theme.backdropEdge, theme.backdropCenter, f) & 0xFFFFFF;
            px[i] = (px[i] & 0xFF000000) | rgb;
        }
        return Bitmap.createBitmap(px, w, h, Bitmap.Config.ARGB_8888);
    }

    /** Text colours of the bubble that were blue: light tint of the theme backdrop (name, "Grams"), engraving, ribbon. */
    public static int text(GiftTheme t, int originalColor) {
        if (t == null || t.isDefault()) return originalColor;
        switch (originalColor) {
            case 0xFF5DE3FF: return GiftTheme.mix(t.backdropCenter, 0xFFFFFFFF, 0.55f);   // "Grams", recipient name
            case 0xFF0145D0: return GiftTheme.mix(t.backdropCenter, 0xFFFFFFFF, 0.25f);   // engraved address
            case 0xFF66E6FF: return t.backdropCenter;                                      // outline
            case 0xFF4BB9FE: return GiftTheme.mix(t.backdropCenter, 0xFFFFFFFF, 0.1f);    // "received" ribbon
            default: return originalColor;
        }
    }
}
