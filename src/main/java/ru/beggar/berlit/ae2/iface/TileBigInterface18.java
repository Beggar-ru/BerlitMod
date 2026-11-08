package ru.beggar.berlit.ae2.iface;

import appeng.tile.misc.TileInterface;
import ru.beggar.berlit.ae2.InterfacePatternExpander;

/** ME Interface with 18 pattern slots. */
public class TileBigInterface18 extends TileInterface {

    public TileBigInterface18() {
        super();
        InterfacePatternExpander.expand(this, 18);
    }
}
