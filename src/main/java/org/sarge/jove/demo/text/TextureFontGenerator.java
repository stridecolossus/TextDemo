package org.sarge.jove.demo.text;

import static org.sarge.lib.util.Check.*;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.model.*;

import picocli.CommandLine;
import picocli.CommandLine.*;

/**
 * The <i>texture font generator</i> is an offline utility used to generate a texture-based glyph font.
 * @author Sarge
 */
public class TextureFontGenerator {
	private int size = 512;
	private int tiles = 16;
	private char start = 0;
	private Color back = new Color(0, 0, 0, 0);
	private Color text = Color.WHITE;
	private boolean alias = true;

	/**
	 * Sets the size of the texture (default is 512 x 512).
	 * @param size Texture size (pixels)
	 */
	public TextureFontGenerator size(int size) {
		this.size = oneOrMore(size);
		return this;
	}

	/**
	 * Sets the starting character of the font.
	 * @param start Starting character
	 */
	public TextureFontGenerator start(char start) {
		if(start < 0) throw new IllegalArgumentException("Starting character must be zero-or-more");
		this.start = start;
		return this;
	}

	/**
	 * Sets the number of tiles for the texture font image, i.e. the number of rows and columns.
	 * @param tiles Number of tiles
	 */
	public TextureFontGenerator tiles(int tiles) {
		this.tiles = oneOrMore(tiles);
		return this;
	}

	/**
	 * Sets the background colour of the texture font image.
	 * Note that it is assumed (but not enforced) that the background colour is translucent.
	 * @param back Background colour
	 */
	public TextureFontGenerator background(Color back) {
		this.back = notNull(back);
		return this;
	}

	/**
	 * Sets the foreground text colour of the texture font image.
	 * @param text Text colour
	 */
	public TextureFontGenerator text(Color text) {
		this.text = notNull(text);
		return this;
	}

	/**
	 * Sets whether glyphs are anti-aliased.
	 * @param alias Whether to anti-alias
	 */
	public TextureFontGenerator alias(boolean alias) {
		this.alias = alias;
		return this;
	}

	/**
	 * Texture font instance.
	 */
	public class Instance {
		private final Font font;
		private FontMetrics metrics;
		private int pairs;

		/**
		 * Constructor.
		 * @param font Font to generate
		 */
		public Instance(Font font) {
			this.font = notNull(font);
		}

    	/**
    	 * Generates the texture image for the given font.
    	 * @return Texture font image
    	 */
    	public BufferedImage image() {
    		// Create texture font image
    		final var image = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);

    		// Init anti-aliasing
    		final var g = (Graphics2D) image.getGraphics();
    		if(alias) {
    			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    		}

    		// Clear translucent background
    		g.setBackground(back);
    		g.clearRect(0, 0, size, size);

    		// Set text colour
    		g.setFont(font);
    		g.setColor(text);

    		// Note font metrics for metadata generation
    		metrics = g.getFontMetrics();

    		// Render character glyphs
    		final int w = size / tiles;
    		final int offset = metrics.getAscent();
    		char ch = start;
    		for(int r = 0; r < tiles; ++r) {
    			final int y = r * w + offset;
    			for(int c = 0; c < tiles; ++c) {
    				final int x = c * w;
    				g.drawChars(new char[]{ch}, 0, 1, x, y);
    				++ch;
    			}
    		}

    		/*
    		TODO - option
    		g.setColor(Color.CYAN);
    		for(int n = 0; n < tiles; ++n) {
    			final int pos = n * w;
    			g.drawLine(0, pos, size, pos);
    			g.drawLine(pos, 0, pos, size);
    		}
    		*/

    		return image;
    	}

    	/**
    	 * Generates the glyph metadata for the given texture font.
    	 * @return Glyph metadata
    	 */
    	public GlyphFont metadata() {
    		// Retrieve font metrics
    		final List<Glyph> glyphs = IntStream
    				.range(start, end())
    				.mapToObj(this::glyph)
    				.toList();

    		// Create glyph font
    		return new GlyphFont(start, glyphs, tiles);
    	}

    	/**
    	 * Determines the end character based on the number of tiles.
    	 */
    	private int end() {
    		return start + tiles * tiles;
    	}

		/**
		 * Generates the glyph for the given character.
		 */
		Glyph glyph(int ch) {
			final int advance = metrics.charWidth(ch);
			final float scaled = advance / (float) size;
			final Map<Integer, Float> pairs = kerning(ch, advance);
			return new Glyph(ch, scaled, pairs);
		}

		/**
		 * Determines the kerning pairs for the given character.
		 * @param ch 			Character
		 * @param advance		Character advance
		 * @return Kerning pairs
		 */
		private Map<Integer, Float> kerning(int ch, int advance) {
			// Ignore non-text characters
			if(!isValid(ch)) {
				return Map.of();
			}

			// Init character pair
			final StringBuilder str = new StringBuilder();
			str.append((char) ch);
			str.append(StringUtils.SPACE);

			// Compare characters to determine kerning pairs
			final Map<Integer, Float> kerning = new HashMap<>();
			final int end = end();
			for(char next = start; next < end; ++next) {
//				// Skip same character
//				if(ch == next) {
//					continue;
//				}

				// Skip non-text characters
				if(!isValid(next)) {
					continue;
				}

				// Determine the total width of the character pair
				str.setCharAt(1, next);
				final int total = metrics.stringWidth(str.toString());

				// Kerning pairs will have a different total width than the individual characters
				final int w = metrics.charWidth(next);
				if(total != advance + w) {
					final int k = total - w;
					kerning.put((int) next, k / (float) size);
					++pairs;
				}
			}

			return kerning;
		}

		/**
		 * @return Whether this given character is eligible for kerning
		 */
		private static boolean isValid(int ch) {
			return Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch);
		}
	}

	/**
	 * Command line runner.
	 */
	@Command(name="texturefont", mixinStandardHelpOptions=false, description="Generates the image grid and associated metadata for a texture font")
	private static class Runner implements Callable<Integer> {
		/**
		 * Font styles.
		 * @see Font#PLAIN
		 */
		private enum Style {
			PLAIN,
			BOLD,
			ITALIC
		}

		@Parameters(index="0", description="Name of the font to be generated")
		private String name;

		@Parameters(index="1", description="Point size")
		private int point;

		@Option(names="--style", description="Font style: ${COMPLETION-CANDIDATES}")
		private Style style = Style.PLAIN;

		@Option(names={"-k", "--kerning"}, description="Whether to enable kerning (default is true)")
		private boolean kerning = true;

		@Option(names={"-aa", "--anti-alias"}, description="Whether to enable anti-aliased glyphs (default is true)")
		private boolean alias = true;

		@Option(names="--size", description="Texture size in pixels (default is 512)")
		private int size = 512;

		@Option(names="--tiles", description="Number of tiles (default is 16)")
		private int tiles = 16;

		@Option(names="--start", description="Starting character (default is the space character)")
		private int start = ' ';

		@Option(names={"-f", "--filename"}, description="Output filename (defaults to font name and point size)")
		private String filename;

		@SuppressWarnings("resource")
		@Override
		public Integer call() throws Exception {
			// Build font
			final Font font = font();
			System.out.println("Font: " + font);

			// Init generator
			// TODO - background/text colour, optional translucent?
			final var generator = new TextureFontGenerator()
					.size(size)
					.tiles(tiles)
					.alias(alias)
					.start((char) start);

			// Generate texture font
			System.out.println("Generating texture font");
			final Instance instance = generator.new Instance(font);
			final BufferedImage image = instance.image();
			final GlyphFont metadata = instance.metadata();

			// Init filename
			if(filename == null) {
				filename = font.getFontName() + font.getSize();
			}

			// Output image
			final String ext = "png";
			System.out.println("Writing texture font");
			ImageIO.write(image, ext, new File(filename + "." + ext));

			// Output metadata
			System.out.println("Writing metadata");
			GlyphFont.Loader.write(metadata, new FileWriter(filename + ".yaml"));

			// Output stats
			if(kerning) {
				System.out.println("Kerning pairs: " + instance.pairs);
			}

			return 0;
		}

		/**
		 * Constructs the AWT font.
		 */
		private Font font() {
			final Font font = new Font(name, style.ordinal(), point);
			if(kerning) {
				return font.deriveFont(Map.of(TextAttribute.KERNING, TextAttribute.KERNING_ON));
			}
			else {
				return font;
			}
		}
	}

	public static void main(String[] args) {
		final var cmd = new CommandLine(new Runner());
		final int exit = cmd.execute(args);
		System.exit(exit);
	}
}
