package ru.beggar.berlit.ae2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.gui.GuiKit;
import ru.beggar.berlit.gui.GuiTheme;
import ru.beggar.berlit.gui.ThemedButton;
import ru.beggar.berlit.machine.MachineType;
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketDigitalFilterRequest;
import ru.beggar.berlit.network.PacketDigitalFilterToggle;

/**
 * Client-side recipe manager opened from a digital machine. It shows the machine's exported
 * patterns and lets the player disable duplicated or unwanted ones globally for that machine type.
 */
public class GuiDigitalFilter extends GuiScreen {

    private static final int WIDTH = 244;
    private static final int HEIGHT = 224;
    private static final int PER_PAGE = 6;
    private static final int ROW_HEIGHT = 22;
    private static final int ROW_START = 44;

    private final TileEntityDigitalMachine tile;
    private final MachineType machineType;
    private final List<DigitalRecipeFilter.Entry> allEntries;
    private List<DigitalRecipeFilter.Entry> entries = new ArrayList<DigitalRecipeFilter.Entry>();
    private GuiTextField search;

    private int page;

    public GuiDigitalFilter(TileEntityDigitalMachine tile) {
        this.tile = tile;
        this.machineType = tile.getMachineType();
        this.allEntries = DigitalRecipeFilter.getEntries(machineType, tile.getMode());
        this.entries = new ArrayList<DigitalRecipeFilter.Entry>(allEntries);
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();

        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        search = new GuiTextField(fontRendererObj, x + 8, y + 22, WIDTH - 16, 14);
        search.setMaxStringLength(64);
        search.setTextColor(GuiTheme.TEXT);
        search.setDisabledTextColour(GuiTheme.TEXT_DISABLED);

        buttonList.add(
            new ThemedButton(
                0,
                x + 8,
                y + HEIGHT - 22,
                50,
                16,
                StatCollector.translateToLocal("berlit.gui.digital.filter.prev")));
        buttonList.add(
            new ThemedButton(
                1,
                x + WIDTH - 116,
                y + HEIGHT - 22,
                50,
                16,
                StatCollector.translateToLocal("berlit.gui.digital.filter.next")));
        buttonList.add(
            new ThemedButton(
                2,
                x + WIDTH - 58,
                y + HEIGHT - 22,
                50,
                16,
                StatCollector.translateToLocal("berlit.gui.digital.filter.close")));

        ModNetwork.INSTANCE.sendToServer(new PacketDigitalFilterRequest(tile.xCoord, tile.yCoord, tile.zCoord));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        int pages = pages();
        if (button.id == 0 && page > 0) page--;
        if (button.id == 1 && page + 1 < pages) page++;
        if (button.id == 2) mc.displayGuiScreen(null);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (search != null) search.updateCursorCounter();

        int pages = pages();
        for (Object object : buttonList) {
            GuiButton button = (GuiButton) object;
            if (button.id == 0) button.enabled = page > 0;
            if (button.id == 1) button.enabled = page + 1 < pages;
        }
    }

    /** Called by the filter sync packet after the server answers. */
    public void onFilterSync() {
        applySearch();
    }

    private void applySearch() {
        String query = search == null ? ""
            : search.getText()
                .trim()
                .toLowerCase(Locale.ENGLISH);

        entries = new ArrayList<DigitalRecipeFilter.Entry>();
        for (DigitalRecipeFilter.Entry entry : allEntries) {
            if (query.isEmpty() || matches(entry, query)) {
                entries.add(entry);
            }
        }

        if (page >= pages()) page = Math.max(0, pages() - 1);
    }

    private static boolean matches(DigitalRecipeFilter.Entry entry, String query) {
        if (entry.getInput()
            .getDisplayName()
            .toLowerCase(Locale.ENGLISH)
            .contains(query)) return true;

        for (ItemStack output : entry.getOutputs()) {
            if (output.getDisplayName()
                .toLowerCase(Locale.ENGLISH)
                .contains(query)) return true;
        }
        return false;
    }

    private int pages() {
        return Math.max(1, (entries.size() + PER_PAGE - 1) / PER_PAGE);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        drawRect(x, y, x + WIDTH, y + HEIGHT, GuiTheme.PANEL_BOTTOM);
        drawRect(x, y, x + WIDTH, y + 1, GuiTheme.FRAME_LIGHT);
        drawRect(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, GuiTheme.FRAME_LIGHT);
        drawRect(x, y, x + 1, y + HEIGHT, GuiTheme.FRAME_LIGHT);
        drawRect(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, GuiTheme.FRAME_LIGHT);

        String title = StatCollector.translateToLocalFormatted(
            "berlit.gui.digital.filter.title",
            StatCollector.translateToLocal(machineType.getDigitalTitleKey()));
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(title, WIDTH - 100), x + 8, y + 6, GuiTheme.TITLE);

        String pageText = StatCollector.translateToLocalFormatted("berlit.gui.digital.filter.page", page + 1, pages());
        fontRendererObj
            .drawString(pageText, x + WIDTH - 8 - fontRendererObj.getStringWidth(pageText), y + 6, GuiTheme.TEXT);

        search.drawTextBox();
        if (search.getText()
            .isEmpty() && !search.isFocused()) {
            fontRendererObj.drawString(
                StatCollector.translateToLocal("berlit.gui.digital.filter.search"),
                x + 12,
                y + 26,
                GuiTheme.TEXT_DISABLED);
        }

        if (entries.isEmpty()) {
            fontRendererObj.drawString(
                StatCollector.translateToLocal("berlit.gui.digital.filter.empty"),
                x + 12,
                y + 52,
                GuiTheme.TEXT_DISABLED);
        } else {
            drawRows(x, y, mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawRows(int x, int y, int mouseX, int mouseY) {
        int start = page * PER_PAGE;
        int end = Math.min(entries.size(), start + PER_PAGE);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(GuiKit.KIT);

        for (int i = start; i < end; i++) {
            int row = i - start;
            int rowY = y + ROW_START + row * ROW_HEIGHT;
            DigitalRecipeFilter.Entry entry = entries.get(i);

            GuiKit.CELL.drawSprite(zLevel, x + 8, rowY, 18, 18);
            GuiKit.CELL.drawSprite(zLevel, x + 48, rowY, 18, 18);

            RenderHelper.enableGUIStandardItemLighting();
            renderStack(entry.getInput(), x + 9, rowY);
            ItemStack output = entry.getOutputs()
                .isEmpty() ? null
                    : entry.getOutputs()
                        .get(0);
            if (output != null) renderStack(output, x + 49, rowY);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);

            fontRendererObj.drawString("->", x + 31, rowY + 4, GuiTheme.TEXT_DISABLED);
            fontRendererObj.drawString(
                StatCollector.translateToLocalFormatted("berlit.gui.digital.filter.energy", entry.getEnergy()),
                x + 72,
                rowY + 4,
                GuiTheme.TEXT);

            boolean enabled = !DigitalRecipeFilter.getDisabled(tile.getWorldObj(), machineType)
                .contains(entry.getKey());
            int tx = x + WIDTH - 52;
            drawRect(tx, rowY, tx + 44, rowY + 16, enabled ? 0xFF2E6B4F : 0xFF6B2E33);
            drawRect(tx, rowY, tx + 44, rowY + 1, enabled ? 0xFF6FE9C0 : 0xFFFF9A9A);
            fontRendererObj.drawString(
                StatCollector
                    .translateToLocal(enabled ? "berlit.gui.digital.filter.on" : "berlit.gui.digital.filter.off"),
                tx + 11,
                rowY + 4,
                enabled ? 0xFFE8F1F5 : 0xFFFFD0D0);

            if (inside(mouseX, mouseY, x + 8, rowY, 58, 18)) {
                List<String> lines = new ArrayList<String>();
                lines.add(
                    entry.getInput()
                        .getDisplayName());
                if (output != null) lines.add(
                    StatCollector.translateToLocal("berlit.gui.digital.filter.output") + " " + output.getDisplayName());
                drawHoveringText(lines, mouseX, mouseY, fontRendererObj);
            } else if (inside(mouseX, mouseY, tx, rowY, 44, 16)) {
                drawHoveringText(
                    Arrays.asList(StatCollector.translateToLocal("berlit.gui.digital.filter.toggle")),
                    mouseX,
                    mouseY,
                    fontRendererObj);
            }
        }
    }

    private void renderStack(ItemStack stack, int x, int y) {
        RenderItem.getInstance()
            .renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), stack, x, y);
        RenderItem.getInstance()
            .renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), stack, x, y);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        if (inside(mouseX, mouseY, search.xPosition, search.yPosition, search.width, search.height)) {
            search.mouseClicked(mouseX, mouseY, button);
            search.setFocused(true);
            return;
        }
        search.setFocused(false);

        if (button != 0) {
            super.mouseClicked(mouseX, mouseY, button);
            return;
        }

        int start = page * PER_PAGE;
        int end = Math.min(entries.size(), start + PER_PAGE);

        for (int i = start; i < end; i++) {
            int rowY = y + ROW_START + (i - start) * ROW_HEIGHT;
            if (inside(mouseX, mouseY, x + WIDTH - 52, rowY, 44, 16)) {
                DigitalRecipeFilter.Entry entry = entries.get(i);
                boolean enabled = !DigitalRecipeFilter.getDisabled(tile.getWorldObj(), machineType)
                    .contains(entry.getKey());
                DigitalRecipeFilter.setEnabled(tile.getWorldObj(), machineType, entry.getKey(), !enabled);
                ModNetwork.INSTANCE.sendToServer(
                    new PacketDigitalFilterToggle(tile.xCoord, tile.yCoord, tile.zCoord, entry.getKey(), !enabled));
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (search != null && search.isFocused() && search.textboxKeyTyped(typedChar, keyCode)) {
            applySearch();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
