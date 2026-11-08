package ru.beggar.berlit.ae2.iface;

import appeng.tile.misc.TileInterface;
import ru.beggar.berlit.ae2.InterfacePatternExpander;

/** ME Interface with 27 pattern slots. */
public class TileBigInterface27 extends TileInterface {

    public TileBigInterface27() {
        super();
        InterfacePatternExpander.expand(this, 27);
    }
}
