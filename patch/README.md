# Gift themes for Gram Wallet

**Idea.** Let users dress their Gram Wallet in a gift they own. Pick a gift in a "fitting room" and the wallet card takes the
gift's backdrop colours and shows its animated model; the buttons, the receive sheet, the transaction history and the
transfer messages in chats follow the same colours. Everything the feature needs already exists on the gift
(backdrop gradient + model), no new assets are required.

This folder contains the implementation as a patch for the Android client (tested on the current Telegram Beta with the
Wallet). A demo video (screen recording of a working client with this code) is attached separately.

---

## What is in the patch

| File | What it does |
|---|---|
| `java/.../gifttheme/GiftTheme.java` | The theme model: backdrop colours, gift model (Lottie), palette derived from the backdrop, shader index, current theme + persistence. In the product it is built from a gift's `TL_starGiftAttributeBackdrop` / `...Model`. |
| `java/.../gifttheme/GiftThemePickerActivity.java` | The "fitting room": the live card on top, the gifts grid below; tapping a gift applies it. Caption = title + number, like the gift list. |
| `java/.../gifttheme/CardGiftEmblem.java` | The looping gift model in the top-left corner of the card. |
| `java/.../gifttheme/GiftThemeHooks.java` | Tiny helpers read at the integration points (accent colour, small gram icon, recolouring of the blue transfer-icon gradient). |
| `java/.../gifttheme/TransferBubbleTheme.java` | Recolours the card inside the chat message "You sent X Grams" (sent = my theme, received = sender's theme). |
| `assets/shaders/wallet_card_fragment_t*.glsl`, `wallet_card_diamond_fragment_t*.glsl` | One pair of fragment shaders per theme (same shaders as the original, only the card colour, the environment tint and the gem tint differ). |
| `tools/gen_themes.py`, `tools/themes.json`, `tools/base_*.glsl` | Generates the shader pairs from two backdrop colours (see "Shaders"). |
| `res/raw/gift_*.json` | Lottie of the gift models used by the demo (in the product they come from the gift itself). |
| `res/drawable/wallet_gem_t*.xml`, `wallet_gram_small_t*.xml` | Recoloured copies of the QR gem and of the small gram icon (gold / red / grey). |

Not part of the patch: everything that only exists to make the demo video (fake contacts and chats, fake transfers,
avatars, wallet history, ...).

---

## Integration points

Components that need a small change, described by their role.

### 1. Card colours: shader selection
* **Shader loader of the 3D card.** Before loading a shader asset path, map it:
  `path = GiftTheme.current().shaderPath(path)`; for the default theme the path is unchanged.
* The shaders are copies of `wallet_card_fragment.glsl` / `wallet_card_diamond_fragment.glsl` with a few constants changed
  (see "Shaders").

### 2. Card texts and 2D fallback
* **`WalletCardView`** (the card, a `FrameLayout`): in the constructor use `palette.nameShadow` for the holder text
  shadow and `palette.usdText` for the USD line; where the balance is set use `palette.gramText`.
  Call `CardGiftEmblem.sync(getFrontFace())` after the front face is built (emblem in the top-left corner).
* **Canvas card background** (2D renderer used when 3D is unavailable): replace the hard-coded gradient
  `0xFF088FEE .. 0xFF16ABF9` with `palette.bg1 .. palette.bg2`.
* **Card face of the 2D renderer**: the engraved address colour (`0xFF0071CC`) becomes `palette.engrave`.
* **`setOnFrontContentPresented(Runnable)`** (already exists on the card) is used by the picker to cross-fade the card
  exactly when the new 3D frame is ready.

### 3. Wallet page (`WalletFragment`)
* Balance on the card and in the collapsed toolbar: `gramText` / `usdText`.
* `onResume()`: if the theme changed while the picker was open, rebuild the card (toggle 2D/3D once so the shader is
  reloaded, `CardGiftEmblem.sync(...)`, restyle the buttons). The picker calls this through
  `WalletCardView.rebuildOnWalletPage()` (a thin wrapper to add).
* "Add funds" / "Send" buttons (**ButtonWithLoading**): `setColor(GiftThemeHooks.accent())` after creation and in
  `onResume`.
* Menu: add an item "Gift theme" that opens `GiftThemePickerActivity` (or a fragment version of it).
* Transaction details sheet: the `OK` button -> `accent()`.
* Gram amounts in the history and the "how it works" sheet: small gram icon -> `GiftThemeHooks.smallGram(...)`,
  sheet colours -> `palette.sheetBottom / sheetText`.

### 4. Transaction history and details
* **Transaction row**: constructor and the method that binds a transaction: small gram icon -> `smallGram(...)`.
* **Round transfer icon** (shader factory that returns the gradient of the up/down arrow circles): use
  `GiftThemeHooks.circleTop(c)` / `circleBottom(c)` for the two colours.
* **Transaction details binder**: the "Grams" word and the amount accent -> `accent()`; small gram -> `smallGram(...)`.

### 5. Receive / QR sheet
* **Receive sheet content** (copy address / show my QR): gem -> `theme.gemRes`, title colour -> `palette.sheetText`,
  button colours -> `palette.btnBg / btnPressed`.
* **Sheet background gradient drawable**: `palette.sheetTop .. palette.sheetBottom`.
* **Address-on-a-ring view**: `palette.ringText`.

### 6. Transfers in chats
* **Gram transfer message bubble** (draws the service message of `TL_messageActionGramTransfer`):
  * when it binds a message, replace its gradient bitmap with
    `TransferBubbleTheme.gradient(ctx, original, themeOf(message))`, where `themeOf` is the current user's theme for
    outgoing messages and the sender's theme for incoming ones;
  * wherever it uses the hard-coded blue text colours (`0xFF5DE3FF`, `0xFF0145D0`, `0xFF66E6FF`, `0xFF4BB9FE`) route them
    through `TransferBubbleTheme.text(theme, color)`.

### 7. Recipient card in the "Send Grams" windows (optional part of the concept)
* **Money tab of the attachment sheet** and **Send Grams fragment**: the address chip on top is replaced by the
  recipient's wallet card, built exactly like on the wallet page (`WalletCardView`) with the **recipient's**
  gift theme (its shader is chosen when the card is created, so the theme is switched only while the card is
  created). Tapping the card opens the same "<Name>'s wallet" explanation dialog as the chip.

---

## Notes on the code

* The Java files use only the public API of the client (`AndroidUtilities`, `R`, `RLottieImageView`) plus three small
  adapter methods to add to the card class `WalletCardView`:
  `static WalletCardView createForBalance(Context)` (the same card as on the wallet page, with the 3D gem next to the
  balance), `static void rebuildOnWalletPage()` (asks the open wallet fragment to rebuild its card) and the existing
  `getFrontFace()` / `setOnFrontContentPresented(Runnable)`.
* The files here were cleaned up from the code of the working demo and were not compiled separately.

---

## Shaders

`wallet_card_fragment.glsl` computes the card from two gradient colours, a "graze" tint and five environment colours.
`tools/gen_themes.py` writes one pair per theme from `tools/themes.json`:

```
bright / dark  : the two backdrop colours of the gift
gain           : per-channel gain that makes the rendered card match the backdrop on screen
graze, env, hi : tints of the metallic reflection and the gem
```

`gain` was calibrated by measuring rendered pixels of the card on a device until the average colour matched the gift
backdrop within a few units. For the real feature the shader constants can simply be uniforms set from
`GiftTheme.palette` (no per-theme files needed) - the generated files are the quick way to get the same look.

---

## Behaviour details

* Only one 3D card is created at a time while switching gifts: the preview card is built hidden, revealed when its
  first 3D frame is presented, and the wallet page card is rebuilt only after that (otherwise switching stutters).
* The gift model loops all the time; the card itself stays still.
* The recipient's card uses the **recipient's** gift theme, the transfer bubble of a sent transfer uses **mine**.
* Default theme = the original blue look (no shader or palette changes).

## Suggested next steps for the real implementation

1. Build `GiftTheme` from an owned gift (`TL_starGiftAttributeBackdrop`: `center_color`, `edge_color`, `pattern_color`,
   `text_color`; model document) and save the chosen gift id in the user's settings (server side, so it follows the
   account).
2. Turn the shader constants into uniforms.
3. Show other people's themes only when they have chosen one (public wallet appearance setting).
