package org.telegram.ui.Wallet.gifttheme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.R;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Wallet.WalletCardView;

/**
 * "Gift theme" page (the fitting room): the real wallet card on top, the user's gifts in a grid below.
 * Tapping a gift applies its colours and model to the card.
 *
 * In the product this would be a regular BaseFragment opened from the wallet menu ("Gift theme") and the grid would be
 * filled from the gifts the user owns; here it is an Activity with a static list ({@link GiftTheme#GIFTS}).
 */
public class GiftThemePickerActivity extends Activity {

    private FrameLayout cardHolder;
    private WalletCardView card;
    private GiftTile[] tiles;
    private boolean transitioning;
    private boolean walletNeedsRefresh;

    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }

    private int dpi(float v) { return Math.round(dp(v)); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setOnApplyWindowInsetsListener((v, ins) -> {
            v.setPadding(0, ins.getSystemWindowInsetTop(), 0, 0);
            return ins;
        });

        // ---- header
        FrameLayout bar = new FrameLayout(this);
        ImageView back = new ImageView(this);
        back.setImageResource(R.drawable.ic_ab_back);
        back.setColorFilter(0xFF222222);
        back.setScaleType(ImageView.ScaleType.CENTER);
        back.setOnClickListener(v -> finish());
        bar.addView(back, new FrameLayout.LayoutParams(dpi(56), dpi(56), Gravity.START | Gravity.CENTER_VERTICAL));
        TextView title = new TextView(this);
        title.setText("Gift theme");
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(0xFF111111);
        FrameLayout.LayoutParams tl = new FrameLayout.LayoutParams(-2, -2, Gravity.START | Gravity.CENTER_VERTICAL);
        tl.leftMargin = dpi(64);
        bar.addView(title, tl);
        root.addView(bar, new LinearLayout.LayoutParams(-1, dpi(56)));

        // ---- scrolling content
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFFF0F0F5);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dpi(12), 0, dpi(24));
        scroll.addView(content, new FrameLayout.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        cardHolder = new FrameLayout(this);
        cardHolder.setClipChildren(false);
        content.addView(cardHolder, new LinearLayout.LayoutParams(-1, -2));
        showCard(false);

        TextView hint = new TextView(this);
        hint.setText("Choose a gift to try it on your wallet card");
        hint.setTextSize(14);
        hint.setTextColor(0xFF7A7A80);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hl = new LinearLayout.LayoutParams(-1, -2);
        hl.topMargin = dpi(14);
        hl.bottomMargin = dpi(10);
        content.addView(hint, hl);

        TextView section = new TextView(this);
        section.setText("YOUR GIFTS");
        section.setTextSize(13);
        section.setTypeface(Typeface.DEFAULT_BOLD);
        section.setTextColor(0xFF7A7A80);
        section.setPadding(dpi(20), dpi(6), dpi(20), dpi(8));
        content.addView(section, new LinearLayout.LayoutParams(-1, -2));

        // ---- gifts grid, 3 per row: "Default" first, then the gifts
        GiftTheme[] all = new GiftTheme[GiftTheme.GIFTS.length + 1];
        all[0] = GiftTheme.DEFAULT;
        System.arraycopy(GiftTheme.GIFTS, 0, all, 1, GiftTheme.GIFTS.length);
        tiles = new GiftTile[all.length];
        LinearLayout row = null;
        for (int i = 0; i < all.length; i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setPadding(dpi(8), 0, dpi(8), dpi(10));
                content.addView(row, new LinearLayout.LayoutParams(-1, -2));
            }
            final GiftTile tile = new GiftTile(this, all[i]);
            tiles[i] = tile;
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            cell.setPadding(dpi(4), 0, dpi(4), 0);
            cell.addView(tile, new LinearLayout.LayoutParams(-1, -2));

            // caption in the style of Telegram's gift list: bold title + grey number
            TextView name = new TextView(this);
            name.setText(all[i].title);
            name.setTextSize(13);
            name.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            name.setTextColor(0xFF000000);
            name.setSingleLine(true);
            LinearLayout.LayoutParams nl = new LinearLayout.LayoutParams(-2, -2);
            nl.topMargin = dpi(7);
            cell.addView(name, nl);
            if (all[i].number != null) {
                TextView number = new TextView(this);
                number.setText(all[i].number);
                number.setTextSize(12);
                number.setTextColor(0xFF8A8A8F);
                cell.addView(number, new LinearLayout.LayoutParams(-2, -2));
            }
            row.addView(cell, new LinearLayout.LayoutParams(0, -2, 1f));
            tile.setOnClickListener(v -> onTile(tile));
        }
        int rem = all.length % 3;
        if (rem != 0 && row != null) {
            for (int i = rem; i < 3; i++) row.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        }
        refreshSelection();

        setContentView(root);
        root.requestApplyInsets();
    }

    private void onTile(GiftTile t) {
        if (t.theme == GiftTheme.current() || transitioning) return;
        GiftTheme.setCurrent(this, t.theme);
        refreshSelection();                          // instant feedback first, heavy work on the next frame
        walletNeedsRefresh = true;
        getWindow().getDecorView().post(() -> showCard(true));
    }

    private void refreshSelection() {
        for (GiftTile t : tiles) t.setSelectedTile(t.theme == GiftTheme.current());
    }

    /** The card of the wallet page below is rebuilt only after the preview card is shown (never two 3D cards at once). */
    private void flushWalletRefresh() {
        if (!walletNeedsRefresh) return;
        walletNeedsRefresh = false;
        WalletCardView.rebuildOnWalletPage();        // see README: WalletFragment.onResume
    }

    @Override
    protected void onPause() {
        super.onPause();
        flushWalletRefresh();
    }

    @Override
    public void finish() {
        flushWalletRefresh();
        super.finish();
    }

    /**
     * Builds the new card hidden under the old one and cross-fades only when its content (shader, texts, model)
     * is really on screen, so everything changes at the same moment.
     */
    private void showCard(boolean animate) {
        final WalletCardView old = card;
        final WalletCardView fresh = WalletCardView.createForBalance(this);   // same view as on the wallet page (3D gem)
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -2);
        lp.leftMargin = dpi(8);
        lp.rightMargin = dpi(8);
        card = fresh;
        CardGiftEmblem.sync(fresh.getFrontFace());
        if (!animate || old == null) {
            cardHolder.addView(fresh, lp);
            if (old != null) cardHolder.removeView(old);
            return;
        }
        transitioning = true;
        fresh.setAlpha(0f);
        cardHolder.addView(fresh, 0, lp);            // below the old card
        final boolean[] revealed = {false};
        final Runnable reveal = () -> {
            if (revealed[0]) return;
            revealed[0] = true;
            ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
            a.setDuration(240);
            a.addUpdateListener(v -> {
                float f = (float) v.getAnimatedValue();
                fresh.setAlpha(f);
                old.setAlpha(1f - f);
            });
            a.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator an) {
                    cardHolder.removeView(old);
                    transitioning = false;
                    cardHolder.postDelayed(GiftThemePickerActivity.this::flushWalletRefresh, 250);
                }
            });
            a.start();
        };
        fresh.setOnFrontContentPresented(() -> cardHolder.postDelayed(reveal, 120));
        cardHolder.postDelayed(reveal, 1800);        // safety net if the 3D frame never arrives
    }

    // =====================================================================

    /** A gift tile: radial backdrop in the gift colours with the looping gift model in the centre. */
    private static final class GiftTile extends FrameLayout {
        final GiftTheme theme;
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint badge = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path clip = new Path();
        private final RectF rect = new RectF();
        private boolean selected;
        private int builtFor;

        GiftTile(Context c, GiftTheme theme) {
            super(c);
            this.theme = theme;
            setWillNotDraw(false);
            ring.setStyle(Paint.Style.STROKE);
            ring.setColor(Color.WHITE);
            badge.setColor(Color.WHITE);
            tick.setStyle(Paint.Style.STROKE);
            tick.setStrokeCap(Paint.Cap.ROUND);
            tick.setColor(luma(theme.backdropEdge) < 0.25f ? 0xFF3A3B3C : theme.backdropEdge);   // readable on dark tiles
        }

        private static float luma(int c) {
            return (0.2126f * Color.red(c) + 0.7152f * Color.green(c) + 0.0722f * Color.blue(c)) / 255f;
        }

        private float dp(float v) { return v * getResources().getDisplayMetrics().density; }

        void setSelectedTile(boolean s) {
            selected = s;
            invalidate();
        }

        @Override
        protected void onMeasure(int w, int h) {
            int width = MeasureSpec.getSize(w);
            if (builtFor != width && width > 0) {
                builtFor = width;
                buildContent(width);
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(Math.round(width * 1.12f), MeasureSpec.EXACTLY));
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            fill.setShader(new RadialGradient(w / 2f, h * 0.46f, Math.max(w, h) * 0.78f,
                    theme.backdropCenter, theme.backdropEdge, Shader.TileMode.CLAMP));
            rect.set(0, 0, w, h);
            clip.reset();
            clip.addRoundRect(rect, dp(14), dp(14), Path.Direction.CW);
        }

        private void buildContent(int w) {
            removeAllViews();
            float wd = w / getResources().getDisplayMetrics().density;   // tile width in dp
            if (theme.model != 0) {
                RLottieImageView model = new RLottieImageView(getContext());
                int size = Math.round(wd * 0.62f);
                model.setAnimation(theme.model, size, size);
                model.setAutoRepeat(true);
                model.playAnimation();
                LayoutParams lp = new LayoutParams(Math.round(dp(size)), Math.round(dp(size)), Gravity.CENTER);
                lp.topMargin = Math.round(-dp(4));
                addView(model, lp);
            } else {
                ImageView gem = new ImageView(getContext());
                gem.setImageResource(R.drawable.wallet_gram_large);
                int size = Math.round(dp(wd * 0.34f));
                addView(gem, new LayoutParams(size, size, Gravity.CENTER));
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.save();
            canvas.clipPath(clip);
            canvas.drawRect(rect, fill);
            canvas.restore();
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            canvas.save();
            canvas.clipPath(clip);
            super.dispatchDraw(canvas);
            canvas.restore();
            if (selected) {
                ring.setStrokeWidth(dp(3));
                RectF r = new RectF(rect);
                r.inset(dp(2.5f), dp(2.5f));
                canvas.drawRoundRect(r, dp(12), dp(12), ring);
                float cx = rect.right - dp(18), cy = rect.bottom - dp(18);
                canvas.drawCircle(cx, cy, dp(11), badge);
                tick.setStrokeWidth(dp(2.4f));
                canvas.drawLine(cx - dp(5), cy, cx - dp(1.5f), cy + dp(4), tick);
                canvas.drawLine(cx - dp(1.5f), cy + dp(4), cx + dp(5.5f), cy - dp(4), tick);
            }
        }
    }
}
