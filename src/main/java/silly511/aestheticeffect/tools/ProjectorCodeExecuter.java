package silly511.aestheticeffect.tools;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Queues;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import silly511.aestheticeffect.helpers.StringHelper;

public final class ProjectorCodeExecuter {
	
	/**
	 * Executes a list of GL commands.
	 * 
	 * @param code A list of commands to execute.
	 * 
	 * @return A hash table of any errors in the code. The first element is the line number and the second is the error message.
	 */
	public static Map<Integer, String> execute(List<String> code) {
		RenderState state = new RenderState();
		Map<Integer, String> errors = new HashMap<Integer, String>();
		
		GlStateManager.pushMatrix();
		
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		
		for (int lineNumber = 0; lineNumber < code.size(); lineNumber++) {
			String line = code.get(lineNumber).trim();
			
			if (line.isEmpty() || line.startsWith("//")) continue;
			
			try {
				handleCodeLine(line, state);
			} catch (InvalidSyntaxException ex) {
				errors.put(lineNumber, ex.error);
			}
		}
		
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		
		GlStateManager.popMatrix();
		
		return errors;
	}
	
	private static void handleCodeLine(String line, RenderState state) throws InvalidSyntaxException {
		if (line.matches("==\\w+==")) {
			if (state.isDrawing()) throw new InvalidSyntaxException("alreadyDrawing");
			String mode = line.substring(2, line.length() - 2);
			
			switch (mode) {
				case "line": state.drawMode = GL11.GL_LINES; break;
				case "line_strip": state.drawMode = GL11.GL_LINE_STRIP; break;
				case "line_loop": state.drawMode = GL11.GL_LINE_LOOP; break;
				
				case "triangle": state.drawMode = GL11.GL_TRIANGLES; break;
				case "triangle_strip": state.drawMode = GL11.GL_TRIANGLE_STRIP; break;
				case "triangle_fan": state.drawMode = GL11.GL_TRIANGLE_FAN; break;
				
				case "quad": state.drawMode = GL11.GL_QUADS; break;
				case "quad_strip": state.drawMode = GL11.GL_QUAD_STRIP; break;
				
				case "polygon": state.drawMode = GL11.GL_POLYGON; break;
				
				default: throw new InvalidSyntaxException("invalidDrawMode", mode);
			}
		} else if (line.equals("====")) {
			if (!state.isDrawing()) throw new InvalidSyntaxException("notDrawing");
			
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			
			buffer.setTranslation(state.translation.xCoord, state.translation.yCoord, state.translation.zCoord);
			buffer.begin(state.drawMode, DefaultVertexFormats.POSITION_COLOR);
				
			for (Vertex v : state.vertexs) {
				if (v.color == null) v.color = state.defaultColor;
				buffer.pos(v.pos.xCoord, v.pos.yCoord, v.pos.zCoord).color(v.color.getRed(), v.color.getGreen(), v.color.getBlue(), (int) (state.alpha * 255)).endVertex();
			}
			
			tessellator.draw();
			buffer.setTranslation(0, 0, 0);
			
			state.vertexs.clear();
			state.drawMode = -1;
		} else if (line.matches("\\w+(\\s*:.*)?")) {
			String[] splitLine = line.split(":", 2);
			String[] args = splitLine.length == 2 ? StringHelper.splitIngoreQuotes(',', splitLine[1]) : new String[0];
			
			switch (splitLine[0]) {
			
			//Methods with arguments
			
			case "translate": checkArgsSize(args, 3);
				state.translation = state.translation.addVector(formatDouble(args[0]), formatDouble(args[1]), formatDouble(args[2]));
				break;
			case "color": checkArgsSize(args, 1);
				state.defaultColor = formatColor(args[0]);
				if (args.length >= 2) state.alpha = MathHelper.clamp(formatDouble(args[1]), 0, 1);
				break;
			case "rotate": checkArgsSize(args, 4);
				double angle = formatDouble(args[0]);
				boolean x = formatBoolean(args[1]), y = formatBoolean(args[2]), z = formatBoolean(args[3]);
				
				GlStateManager.rotate((float) angle, x ? 1f : 0f, x ? 1f : 0f, x ? 1f : 0f);
				break;
			case "lineWidth": checkArgsSize(args, 1);
				GlStateManager.glLineWidth((float) formatDouble(args[0]));
				break;
			
			//Methods without arguments
			
			case "resetTranslate":
				state.translation = Vec3d.ZERO;
				break;
			
			//Drawing
		
			case "addVertex": checkArgsSize(args, 3); if (!state.isDrawing()) throw new InvalidSyntaxException("notDrawing");
				Vec3d pos = new Vec3d(formatDouble(args[0]), formatDouble(args[1]), formatDouble(args[2]));
				
				if (args.length >= 4)
					state.vertexs.add(new Vertex(pos, formatColor(args[3])));
				else if (args.length >= 3)
					state.vertexs.add(new Vertex(pos, null));
				break;
		
			//Method doesn't exist
			
			default:
				throw new InvalidSyntaxException("noSuchMethod", splitLine[0]);
			}
		} else {
			throw new InvalidSyntaxException("unknownSyntax");
		}
	}
	
	private static void checkArgsSize(String[] args, int argsCount) throws InvalidSyntaxException {
		if (args.length < argsCount) throw new InvalidSyntaxException("invalidArgs");
	}
	
	private static boolean formatBoolean(String input) throws InvalidSyntaxException {
		input = input.trim().toLowerCase();
		
		if (input.equals("true"))
			return true;
		else if (input.equals("false"))
			return false;
		else
			throw new InvalidSyntaxException("notABoolean", input);
	}
	
	private static double formatDouble(String input) throws InvalidSyntaxException {
		try {
			return Double.valueOf(input);
		} catch (NumberFormatException ex) {
			throw new InvalidSyntaxException("notANumber", input);
		}
	}
	
	private static Color formatColor(String input) throws InvalidSyntaxException {
		input = input.trim();
		
		if (!input.matches("#[0-9A-Fa-f]{6}")) throw new InvalidSyntaxException("notAColor", input);
		
		return new Color(Integer.valueOf(input.substring(1), 16));
	}
	
	public static class RenderState {
		public Vec3d translation = new Vec3d(0, 0, 0);
		public Color defaultColor = Color.white;
		public double alpha = 0.5;
		public int drawMode = -1;
		
		public Queue<Vertex> vertexs = Queues.newArrayDeque();
		
		public boolean isDrawing() {
			return drawMode >= 0;
		}
	}
	
	public static class Vertex {
		public Vec3d pos;
		public Color color;
		
		public Vertex(Vec3d pos, Color color) {
			this.pos = pos;
			this.color = color;
		}
	}
	
	public static class InvalidSyntaxException extends Exception {
		public String error;
		
		public InvalidSyntaxException(String errorLocal, Object... localReplacements) {
			this.error = I18n.format("hologramProjector.error." + errorLocal, localReplacements);
		}
	}

}
