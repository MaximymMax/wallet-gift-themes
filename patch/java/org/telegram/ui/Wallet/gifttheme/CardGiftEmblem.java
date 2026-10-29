package org.telegram.ui.Wallet.gifttheme;

import android.content.Context;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.RLottieImageView;

/**
 * The animated gift model in the top-left corner of the wallet card (front face).
 * Call {@link #sync} whenever the card is built or the theme changes.
 */
public final class CardGiftEmblem {
    private static final String TAG = "gift_emblem";

    private CardGiftEmblem() { }

    /** Removes the old emblem and adds the one of the current theme (none for the default look). */
    public static void sync(FrameLayout frontFace) {
        if (frontFace == null) return;
        for (int i = frontFace.getChildCount() - 1; i >= 0; i--) {
            if (TAG.equals(frontFace.getChildAt(i).getTag())) frontFace.removeViewAt(i);
        }
        GiftTheme theme = GiftTheme.current();
        if (theme.model == 0) return;
        Context context = frontFace.getContext();
        RLottieImageView emblem = new RLottieImageView(context);
        emblem.setTag(TAG);
        emblem.setAnimation(theme.model, 32, 32);   // 32 dp
        emblem.setAutoRepeat(true);                 // loops forever
        emblem.playAnimation();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(AndroidUtilities.dp(28), AndroidUtilities.dp(28));
        lp.gravity = android.view.Gravity.TOP | android.view.Gravity.START;
        lp.leftMargin = AndroidUtilities.dp(20);
        lp.topMargin = AndroidUtilities.dp(18);
        frontFace.addView(emblem, lp);
    }
}
