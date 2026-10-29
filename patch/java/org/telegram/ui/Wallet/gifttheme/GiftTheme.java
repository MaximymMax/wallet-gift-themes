package org.telegram.ui.Wallet.gifttheme;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.R;

/**
 * A "gift theme" for the Gram Wallet: the backdrop colours and the animated model of a gift are applied to the
 * wallet card (and, derived from them, to the buttons, sheets and transfer bubbles around it).
 *
 * The backdrop colours and the model of a real gift come from the gift's attributes
 * (TL_starGiftAttributeBackdrop / TL_starGiftAttributeModel), so in the product this class would be built from a gift
 * the user owns. Here the table is static for the concept.
 */
public final class GiftTheme {

    /** Everything that is coloured after the backdrop of the gift. */
    public static final class Palette {
        public int accent;                 // wallet buttons, transfer icons
        public int circleTop, circleBottom;// gradient of the round transfer icons in the history
        public int bg1, bg2;               // 2D fallback card background (canvas renderer)
        public int gramText, usdText;      // balance texts printed on the card
        public int nameShadow;             // shadow of the card holder text
        public int sheetTop, sheetBottom;  // gradient of the "receive" sheet
        public int sheetText, ringText;    // texts on that sheet
        public int btnBg, btnPressed;      // light buttons on that sheet
        public int engrave;                // colour of the engraved address on the card

        /** Palette computed from the two backdrop colours (hi = backdrop centre, lo = backdrop edge). */
        public static Palette derive(int hi, int lo, boolean dark) {
            Palette p = new Palette();
            p.accent = mix(lo, hi, 0.45f);
            p.circleTop = hi;
            p.circleBottom = lo;
            p.bg1 = lo;
            p.bg2 = hi;
            p.gramText = dark ? 0xFFF2F4F5 : mix(hi, 0xFFFFFFFF, 0.85f);
            p.usdText = dark ? 0xFFBFC5C7 : mix(hi, 0xFFFFFFFF, 0.6f);
            p.nameShadow = mix(lo, 0xFF000000, 0.45f);
            p.sheetTop = lo;
            p.sheetBottom = hi;
            p.sheetText = mix(lo, 0xFF000000, 0.1f);
            p.btnBg = mix(hi, 0xFFFFFFFF, 0.92f);
            p.btnPressed = mix(hi, 0xFFFFFFFF, 0.8f);
            p.ringText = dark ? 0xFFC9CED0 : mix(lo, 0xFF000000, 0.35f);
            p.engrave = mix(lo, 0xFF000000, 0.15f);
            return p;
        }
    }

    public final String id;              // stable key, also used for the shader file suffix
    public final String title;           // "Lush Bouquet"
    public final String number;          // "#46123" (null for the default look)
    public final int backdropCenter;     // backdrop gradient 0 %
    public final int backdropEdge;       // backdrop gradient 100 %
    public final int model;              // R.raw.* Lottie of the gift model (0 = none)
    public final int gemRes;             // centre gem of the QR / receive sheet
    public final int gramSmallRes;       // small gram icon next to amounts in the history
    public final int shaderIndex;        // wallet_card_fragment_t<N>.glsl (0 = original shader)
    public final Palette palette;

    private GiftTheme(String id, String title, String number, int center, int edge, int model,
                      int gemRes, int gramSmallRes, int shaderIndex, Palette palette) {
        this.id = id; this.title = title; this.number = number;
        this.backdropCenter = center; this.backdropEdge = edge;
        this.model = model; this.gemRes = gemRes; this.gramSmallRes = gramSmallRes;
        this.shaderIndex = shaderIndex; this.palette = palette;
    }

    private static GiftTheme derived(String id, String title, String number, int center, int edge, int model,
                                     int shaderIndex, boolean dark, int gem, int gramSmall) {
        return new GiftTheme(id, title, number, center, edge, model, gem, gramSmall, shaderIndex,
                Palette.derive(center, edge, dark));
    }

    /** Hand tuned palette of the original blue wallet. */
    private static Palette blue() {
        Palette p = new Palette();
        p.accent = 0xFF229AF0; p.circleTop = 0xFF3AB0F8; p.circleBottom = 0xFF229AF0;
        p.bg1 = 0xFF088FEE; p.bg2 = 0xFF16ABF9;
        p.gramText = 0xFF6DDCFF; p.usdText = 0xFF87EFFF; p.nameShadow = 0xFF0285D4;
        p.sheetTop = 0xFF088FEE; p.sheetBottom = 0xFF45B8F7; p.sheetText = 0xFF129EEA;
        p.btnBg = 0xFFF0F9FE; p.btnPressed = 0xFFE2E8FC; p.ringText = 0xFF0285CE; p.engrave = 0xFF0071CC;
        return p;
    }

    public static final GiftTheme DEFAULT = new GiftTheme("default", "Default", null,
            0xFF3AB0F8, 0xFF1488E1, 0, R.drawable.mini_gram_72, R.drawable.wallet_gram_small, 0, blue());

    /** Gifts offered in the fitting room (after DEFAULT). Backdrop colours are the gift's gradient 0 % -> 100 %. */
    public static final GiftTheme[] GIFTS = {
            derived("lush_bouquet", "Lush Bouquet", "#46123", 0xFFDB9867, 0xFFC76F4F, R.raw.gift_lush_bouquet, 1, false,
                    R.drawable.wallet_gem_t1, R.drawable.wallet_gram_small_t1),
            derived("bow_tie", "Bow Tie", "#7438", 0xFFF05F4F, 0xFFC43949, R.raw.gift_bow_tie, 2, false,
                    R.drawable.wallet_gem_t2, R.drawable.wallet_gram_small_t2),
            derived("resistance_dog_3745", "Resistance Dog", "#3745", 0xFFD09932, 0xFFB77431, R.raw.gift_pharaoh, 6, false,
                    R.drawable.wallet_gem_t1, R.drawable.wallet_gram_small_t1),
            derived("resistance_dog_6483", "Resistance Dog", "#6483", 0xFF45B8ED, 0xFF3886D9, R.raw.gift_crystal, 7, false,
                    R.drawable.mini_gram_72, R.drawable.wallet_gram_small),
            derived("nail_bracelent", "Nail Bracelent", "#378", 0xFFA980F3, 0xFF5B62D8, R.raw.gift_xray, 8, false,
                    R.drawable.mini_gram_72, R.drawable.wallet_gram_small),
            derived("happy_brownie", "Happy Brownie", "#3900", 0xFF664D45, 0xFF47362E, R.raw.gift_cat_scat, 9, true,
                    R.drawable.wallet_gem_t3, R.drawable.wallet_gram_small_t3),
            derived("scared_cat", "Scared Cat", "#736", 0xFF6FC793, 0xFF3B9C84, R.raw.gift_esmeralda, 10, false,
                    R.drawable.mini_gram_72, R.drawable.wallet_gram_small),
            derived("gem_signet", "Gem Signet", "#937", 0xFF4D5254, 0xFF313638, R.raw.gift_blood_opal, 11, true,
                    R.drawable.wallet_gem_t3, R.drawable.wallet_gram_small_t3),
    };

    // ------------------------------------------------------------------ current theme (persisted)

    private static final String PREFS = "wallet_gift_theme";
    private static volatile GiftTheme current = DEFAULT;

    public static GiftTheme current() { return current; }

    public static void load(Context c) {
        try {
            SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            current = byId(p.getString("id", DEFAULT.id));
        } catch (Throwable ignore) {
            current = DEFAULT;
        }
    }

    public static void setCurrent(Context c, GiftTheme t) {
        current = t;
        try {
            c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("id", t.id).apply();
        } catch (Throwable ignore) { }
    }

    public static GiftTheme byId(String id) {
        for (GiftTheme t : GIFTS) if (t.id.equals(id)) return t;
        return DEFAULT;
    }

    public boolean isDefault() { return this == DEFAULT; }

    /** Shader asset of the card for this theme: wallet_card_fragment.glsl -> wallet_card_fragment_t3.glsl */
    public String shaderPath(String path) {
        if (shaderIndex == 0 || path == null || !path.endsWith(".glsl")) return path;
        return path.substring(0, path.length() - 5) + "_t" + shaderIndex + ".glsl";
    }

    // ------------------------------------------------------------------ helpers

    public static int mix(int a, int b, float f) {
        int r = (int) (((a >> 16) & 255) * (1 - f) + ((b >> 16) & 255) * f);
        int g = (int) (((a >> 8) & 255) * (1 - f) + ((b >> 8) & 255) * f);
        int bl = (int) ((a & 255) * (1 - f) + (b & 255) * f);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }
}
