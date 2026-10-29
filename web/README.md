# Wallet card with gift stickers – web concept

Open `index.html` in a browser (Chrome / Edge / Safari, desktop or phone). It needs internet for two things only:
the `lottie-web` script (cdnjs) and the Roboto fonts (Google Fonts). Everything else is in this folder:

| File | What it is |
|---|---|
| `index.html` | page structure (top bar, card, buttons, gifts grid, bin, popup menu) |
| `style.css` | layout and look (all card sizes are fractions of the card width, so it scales) |
| `app.js` | the card (WebGL shader + CSS 3D tilt) and the sticker editor |
| `gifts-data.js` | the 8 gifts: title, number, backdrop colours and the Lottie model |

## What it does (ported from the Wallet in Telegram Beta-2, class `org.telegram.ui.Wallet.t7` and `lc0.dispatchTouchEvent`)

**Gifts list**
* tap = opens the gift page (a toast in this demo); hold 500 ms (or right click) = menu **Apply colour / Remove colour** and **Add to card / Remove from card**;
  "Apply colour" is offered only for gifts in `BG_GIFTS` (in the client the server decides: `walletCardBgGifts`; here `gem_signet` is left out as an example).
* drag a gift **from the middle of its tile** (at once, or after holding 170 ms) onto the card. A gift that is already on the card cannot be dragged again from the list.
  While you drag, the card gets a dashed outline and the hint "Drag this collectible here" (the very first time: "...on a card to decorate it").

**Dropping**
* A dropped gift stays **exactly where you let go**, at the default size (23.1 % of the card width). There is no collision check and no snapping:
  it may lie over the balance, the USD amount, the name and the engraved words (they get a darker shadow the more they are covered).
  Only the **QR chip can not be covered**: the sticker layer has a hole in the shape of the chip.
* The finger must be on the card. Dropped anywhere else the gift flies back to its tile.
* The **automatic placement** (scoring of every spot, keeps clear of texts / chip / edge / other stickers, picks the size) is used only by the menu
  action "Add to card" and when you **fling** a gift at the card (faster than 700 px/s).
* Max **10** stickers, the 11th sends the oldest one back to the list. Every gift can be on the card once. Stickers are the static first frame of the model.

**Stickers on the card**
* tap = select: the sticker gets a dashed ring with two blue handles. Tapping anywhere else deselects.
* drag a handle = **scale and rotate** (size 32 dp ... 1.21 x, snaps to 0 / 90 / 180 / 270 degrees with a click). Hold the sticker 450 ms and move the same finger around it for the same thing.
* drag the sticker = move it. Drop it on the card = stays there. Drag it off the card: the **Remove** pill appears; on the pill (or anywhere outside the card) it leaves the card.
* the latest touched sticker is on top.

**Colour**
* "Apply colour" needs the gift on the card (it is added automatically if not). The card takes the gift's backdrop colours, spreading as a circle that starts at the sticker,
  and the gift's symbol is tiled over the card as a faint pattern. The colour belongs to the sticker: when the sticker leaves the card the colour goes too.
* Everything (stickers, colour) is remembered in `localStorage`.

## How it relates to the real client

* In the client the saved card is `TL_wallet.card { stickers[], bg_nft_item }`; the stickers are gifts (NFTs) with
  `MediaAreaCoordinates` (x, y, width, height, rotation) in percent of the card, saved with `setCardStickers`, and the
  colour comes from the gift's backdrop. Here the same data is kept as `{id, x, y, sc, r, z, t}` in `localStorage`.
* The card look is a reconstruction (a hand written shader tuned against screenshots of the real card), not the
  original multi-pass shaders.

## Notes

* The card is not pixel-identical to the app: the original renders a real 3D model with several shader passes.
  The shader is in `app.js` (`FS`), the cone / ring / grain / glint constants are the ones to tune.
* To run it without internet, download `lottie.min.js` and the two fonts next to the files and change the
  `<script>` / `<link>` tags in `index.html`.
