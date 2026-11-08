package ru.beggar.berlit.ae2.iface;

import appeng.tile.misc.TileInterface;
import ru.beggar.berlit.ae2.InterfacePatternExpander;

/** ME Interface with 36 pattern slots. */
public class TileBigInterface36 extends TileInterface {

    public TileBigInterface36() {
        super();
        InterfacePatternExpander.expand(this, 36);
    }
}
