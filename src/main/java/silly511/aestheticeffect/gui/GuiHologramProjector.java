package silly511.aestheticeffect.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import silly511.aestheticeffect.AestheticEffect;
import silly511.aestheticeffect.helpers.StringHelper;
import silly511.aestheticeffect.packets.PacketSetProjectorCode;
import silly511.aestheticeffect.tile.TileHologramProjector;

public class GuiHologramProjector extends GuiScreen {
	
	private static final ResourceLocation texture = new ResourceLocation(AestheticEffect.modid, "textures/gui/hologram_projector_gui.png");
	private static final FileFilter codeFileFilter = new FileNameExtensionFilter("GL Code", "glcode");
	private static final int textAreaSizeX = 236;
	private static final int textAreaSizeY = 170;
	
	private List<String> text;
	private int cursorX;
	private int cursorY;
	private int scrollY;
	private int cursorBlinkTimer;
	
	private BlockPos pos;
	
	private boolean shouldSave = true;
	private GuiButton importButton;
	private GuiButton exportButton;
	
	public GuiHologramProjector(int x, int y, int z, World world) {
		this.pos = new BlockPos(x, y, z);
		this.text = new ArrayList<String>(((TileHologramProjector) world.getTileEntity(pos)).getCode());
		this.cursorX = text.get(0).length();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int x = this.width / 2 - 120;
		int y = this.height / 2 - 100;
		
		this.drawDefaultBackground();
		
		this.mc.renderEngine.bindTexture(texture);
		this.drawTexturedModalRect(x - 8, y - 8, 0, 0, 248, 208);
		
		for (int i = 0; i < 15; i++) {
			if (i + scrollY > text.size() - 1) break;
			
			int renderY = y + (fontRenderer.FONT_HEIGHT + 2) * i;
			String line = text.get(scrollY + i);
			String trimedLine = line.trim();
			
			if (trimedLine.startsWith("//"))
				line = TextFormatting.DARK_GRAY + line;
			else if (trimedLine.matches("==[^=]*=="))
				line = TextFormatting.BOLD.toString() + TextFormatting.GRAY + line;
			else {
				line = StringHelper.replaceIngoreQuotes("(?<!#)\\b(\\d+(\\.\\d+)?)\\b(?!([^:]*:))", TextFormatting.DARK_AQUA + "%s" + TextFormatting.RESET, line);
				line = line.replaceAll("\"[^\"]*\"(?!([^:]*:))", TextFormatting.DARK_GREEN + "$0" + TextFormatting.RESET);
				line = line.replaceAll("^\\s*\\w+", TextFormatting.GOLD + "$0" + TextFormatting.RESET);
				
				Matcher m = Pattern.compile("#([0-9A-Fa-f]{6})\\b").matcher(line);
				
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 3);
				
				if (m.find()) do {
					int offset = this.fontRenderer.getStringWidth(line.substring(0, m.start()));
					int width = this.fontRenderer.getStringWidth(m.group());
					int color = Integer.parseInt(m.group(1), 16);
					
					drawRect(x + offset, renderY - 1, x + offset + width, renderY + fontRenderer.FONT_HEIGHT, color | 0xFF000000);
					
					this.fontRenderer.drawString(m.group(), x + offset, renderY, color >= 0x888888 ? 0x0 : 0xFFFFFF);
				} while (m.find());
				
				GlStateManager.popMatrix();
			}
			
			this.fontRenderer.drawString(line, x, renderY, 0xFFFFFF);
		}
		
		if (this.cursorBlinkTimer / 6 % 2 == 0 && this.cursorY < this.scrollY + 15 && this.cursorY > this.scrollY - 1) {
			int lineWidth = this.fontRenderer.getStringWidth(text.get(cursorY).substring(0, cursorX)) + 1;
			int startY = y + (fontRenderer.FONT_HEIGHT + 2) * (cursorY - scrollY) - 1;
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 5);
			
			this.drawVerticalLine(x + lineWidth - 2, startY - 1, startY + fontRenderer.FONT_HEIGHT + 1, 0xFFFFFFFF);
			
			GlStateManager.popMatrix();
		}
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		if (importButton.isMouseOver()) this.drawHoveringText(Arrays.asList(I18n.format("gui.aestheticeffect.import.tooltip").split("\n")), mouseX, mouseY);
		if (exportButton.isMouseOver()) this.drawHoveringText(Arrays.asList(I18n.format("gui.aestheticeffect.export.tooltip").split("\n")), mouseX, mouseY);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			this.mc.displayGuiScreen(null);
		} else if (keyCode == Keyboard.KEY_UP) {
			this.cursorY--;
			if (this.scrollY > 0 && this.cursorY < this.scrollY) this.scrollY--;
		} else if (keyCode == Keyboard.KEY_DOWN) {
			this.cursorY++;
			if (this.cursorY > this.scrollY + 14) this.scrollY++;
		} else if (keyCode == Keyboard.KEY_LEFT) {
			this.cursorX--;
		} else if (keyCode == Keyboard.KEY_RIGHT) {
			this.cursorX++;
		} else if (keyCode == Keyboard.KEY_RETURN) {
			String line = text.get(cursorY);
			
			text.set(cursorY, line.substring(0, cursorX));
			text.add(++cursorY, line.substring(cursorX));
			
			this.cursorX = 0;
			if (this.cursorY == this.scrollY + 15) this.scrollY++;
		} else if (keyCode == Keyboard.KEY_BACK) {
			if (this.cursorX > 0)
				text.set(cursorY, new StringBuilder(text.get(cursorY)).deleteCharAt(--cursorX).toString());
			else if (this.cursorY > 0) {
				if (text.get(cursorY).isEmpty()) {
					text.remove(cursorY);
					
					this.cursorX = text.get(--cursorY).length();
				} else if (text.get(cursorY - 1).isEmpty())
					text.remove(--cursorY);
				
				if (this.cursorY > 15) this.scrollY--;
			}
			
		} else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
			String line = new StringBuilder(text.get(cursorY)).insert(cursorX, typedChar).toString();
			
			if (this.fontRenderer.getStringWidth(line) > textAreaSizeX) return;
			
			this.cursorX++;
			text.set(cursorY, line);
		}
		
		this.cursorY = MathHelper.clamp(this.cursorY, 0, this.text.size() - 1);
		this.cursorX = MathHelper.clamp(this.cursorX, 0, this.text.get(cursorY).length());
		if (this.cursorY > this.scrollY + 15) this.scrollY = this.text.size() - 15;
	}

	@Override
	public void handleMouseInput() throws IOException {
		int scrollDir = Mouse.getEventDWheel();
		
		if (scrollDir != 0) {
			if (this.scrollY > 0 && scrollDir >= 1)
				this.scrollY--;
			else if (this.scrollY < text.size() - 15 && scrollDir <= -1)
				this.scrollY++;
		}
		
		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		int x = this.width / 2 - 120;
		int y = this.height / 2 - 100;
		
		if (mouseX > x && mouseX < x + textAreaSizeX && mouseY > y && mouseY < y + textAreaSizeY) {
			int relatetiveX = mouseX - x;
			int relatetiveY = mouseY - y;
			
			this.cursorY = MathHelper.clamp(this.scrollY + (int) (relatetiveY / (this.fontRenderer.FONT_HEIGHT + 2)), 0, text.size() - 1);
			this.cursorX = this.fontRenderer.trimStringToWidth(this.text.get(cursorY), relatetiveX).length();
		}
		
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			AestheticEffect.network.sendToServer(new PacketSetProjectorCode(this.text, pos));
			
			this.mc.displayGuiScreen(null);
		} else if (button.id == 1)
			this.mc.displayGuiScreen(null);
		else if (button.id == 2) {
			JFileChooser fc = new JFileChooser();
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(codeFileFilter);
			
			if (JOptionPane.showConfirmDialog(null,
					I18n.format("gui.aestheticeffect.confirmImport"),
					I18n.format("gui.aestheticeffect.confirmImport.title"),
					JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
				
			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				this.text.clear();
				
				try (BufferedReader reader = Files.newBufferedReader(fc.getSelectedFile().toPath())) {
					while (true) {
						String line = reader.readLine();
						if (line == null) break;
						this.text.add(this.fontRenderer.trimStringToWidth(ChatAllowedCharacters.filterAllowedCharacters(line), textAreaSizeX));
					}
				} catch (IOException ex) {
					AestheticEffect.logger.error("Unable to read file {}", fc.getSelectedFile(), ex);
				}
				
				if (this.text.isEmpty()) this.text.add("");
				this.scrollY = 0;
				this.cursorY = 0;
				this.cursorX = text.get(0).length();
			}
		} else if (button.id == 3) {
			JFileChooser fc = new JFileChooser();
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(codeFileFilter);
			
			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (!codeFileFilter.accept(file)) file = new File(file + ".glcode");
				
				if (file.exists() ? JOptionPane.showConfirmDialog(null,
						I18n.format("gui.aestheticeffect.confirmReplaceFile"),
						I18n.format("gui.aestheticeffect.confirmReplaceFile.title"),
						JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION : true)
					
				try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
					for (String line : this.text) {
						writer.write(line);
						writer.newLine();
					}
				} catch (IOException ex) {
					AestheticEffect.logger.error("Unable to write to file {}", fc.getSelectedFile(), ex);
				}
			}
		}
	}

	@Override
	public void updateScreen() {
		this.cursorBlinkTimer++;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		
		this.addButton(new GuiButton(0, this.width / 2 + 65, this.height / 2 + 74, 50, 20, I18n.format("gui.done")));
		this.addButton(new GuiButton(1, this.width / 2 + 13, this.height / 2 + 74, 50, 20, I18n.format("gui.cancel")));
		importButton = this.addButton(new GuiButton(2, this.width / 2 - 123, this.height / 2 + 74, 50, 20, I18n.format("gui.aestheticeffect.import")));
		exportButton = this.addButton(new GuiButton(3, this.width / 2 - 71, this.height / 2 + 74, 50, 20, I18n.format("gui.aestheticeffect.export")));
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}
