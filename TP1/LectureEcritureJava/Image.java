import java.io.FileWriter;
import java.io.IOException;

/**
 * Création d'une image
 * @author Yanis Laurençont
 */
public class Image {
    private int width;
    private int height;
    // pixels[y][x][0=R,1=G,2=B]
    private int[][][] pixels;

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    /**
     * Constructeur : initialise une image vide.
     */
    public Image(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new int[height][width][3];
    }

    /**
     * Définit la couleur d'un pixel à la position (x, y)
     */
    public void setPixel(int x, int y, int r, int g, int b) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            pixels[y][x][0] = r;
            pixels[y][x][1] = g;
            pixels[y][x][2] = b;
        }
    }

    /**
     * @return pixels[y][x][0] l'intensité du rouge à la position donnée
     */
    public int getRed(int x, int y) {
        return pixels[y][x][0];
    }

    /**
     * @return pixels[y][x][0] l'intensité du vert à la position donnée
     */
    public int getGreen(int x, int y) {
        return pixels[y][x][1];
    }

    /**
     * @return pixels[y][x][0] l'intensité du bleu à la position donnée
     */
    public int getBlue(int x, int y) {
        return pixels[y][x][2];
    }

    /**
     * Sauvegarde l'image au format texte PPM (P3)
     */
    public void save_txt(String filename) throws IOException {
        FileWriter writer = new FileWriter(filename);

        writer.write("P3\n");
        writer.write(width + " " + height + "\n");
        writer.write("255\n");

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.write(pixels[y][x][0] + " " + pixels[y][x][1] + " " + pixels[y][x][2]);
                if (x < width - 1) {
                    writer.write("   ");
                }
            }
            writer.write("\n");
        }

        writer.close();
    }
}
