import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Création d'une image
 * @author Yanis Laurençont
 */
public class Image {

    private static final int CODE_ERREUR_MAX_VAL = 1;
    private int width;
    private int height;

    // pixels[y][x][0=R,1=G,2=B]
    private int[][][] pixels;

    /**
     * @return la largeur de l'image
     */
    public int getWidth() { 
        return width; 
    }

    /**
     * @return la hauteur de l'image
     */
    public int getHeight() { 
        return height; 
    }

    /**
     * Constructeur : initialise une image vide.
     * @param width largeur de l'image en pixels
     * @param height hauteur de l'image en pixels
     */
    public Image(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new int[height][width][3];
    }

    /**
     * Définit la couleur d'un pixel à la position (x, y)
     * @param x position horizontale du pixel
     * @param y position verticale du pixel
     * @param r composante rouge (0 à 255)
     * @param g composante verte (0 à 255)
     * @param b composante bleue (0 à 255)
     */
    public void setPixel(int x, int y, int r, int g, int b) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            pixels[y][x][0] = r;
            pixels[y][x][1] = g;
            pixels[y][x][2] = b;
        }
    }

    /**
     * @param x position horizontale du pixel
     * @param y position verticale du pixel
     * @return l'intensité du rouge à la position donnée
     */
    public int getRed(int x, int y) {
        return pixels[y][x][0];
    }

    /**
     * @param x position horizontale du pixel
     * @param y position verticale du pixel
     * @return l'intensité du vert à la position donnée
     */
    public int getGreen(int x, int y) {
        return pixels[y][x][1];
    }

    /**
     * @param x position horizontale du pixel
     * @param y position verticale du pixel
     * @return l'intensité du bleu à la position donnée
     */
    public int getBlue(int x, int y) {
        return pixels[y][x][2];
    }

    /**
     * Lit un fichier au format texte PPM (P3)
     * @param filename nom du fichier à lire
     * @return une Image créée à partir de l'image lue
     * @throws IOException en cas d'erreur de lecture du fichier
     */
    public static Image read_txt(String filename) throws IOException {
        Scanner scanner = new Scanner(new File(filename));

        // Lecture du mot magique
        String magic = scanner.next();
        if (!magic.equals("P3")) {
            scanner.close();
            throw new IllegalArgumentException("Le fichier n'est pas au format P3");
        }

        // Lecture de la taille et de la valeur max
        int width = scanner.nextInt();
        int height = scanner.nextInt();
        int maxVal = scanner.nextInt();

        if (maxVal > 255) {
            System.err.println("Erreur : maxVal dépasse 255");
            scanner.close();
            System.exit(CODE_ERREUR_MAX_VAL);
        }

        Image img = new Image(width, height);

        // Lecture des pixels en texte
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = scanner.nextInt();
                int g = scanner.nextInt();
                int b = scanner.nextInt();
                img.setPixel(x, y, r, g, b);
            }
        }

        scanner.close();
        return img;
    }

    /**
     * Lit un fichier au format binaire PPM (P6)
     * @param filename nom du fichier à lire
     * @return une Image créée à partir de l'image lue 
     * @throws IOException en cas d'erreur de lecture du fichier
     */
    public static Image read_bin(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        
        String header = "";
        int lineCount = 0;
        
        /* 
         * Le header P6 contient 3 lignes d'informations texte
         * donc on le vérifie caractère par caractère 
         */
        while (lineCount < 3) {
            char c = (char) fis.read();
            header += c;
            if (c == '\n') {
                lineCount++;
            }
        }
        
        // On découpe le header avec les espaces ou sauts de ligne
        String[] tokens = header.trim().split("\\s+");
        String magic = tokens[0]; // P6

        // On vérifie que le magic est correct
        if (!magic.equals("P6")) {
            fis.close();
            throw new IllegalArgumentException("Le fichier n'est pas au format P6");
        }

        int width = Integer.parseInt(tokens[1]);
        int height = Integer.parseInt(tokens[2]);
        int maxVal = Integer.parseInt(tokens[3]); // 255 en théorie

        if (maxVal > 255) {
            System.err.println("Erreur : maxVal dépasse 255");
            fis.close();
            System.exit(CODE_ERREUR_MAX_VAL);
        }

        Image img = new Image(width, height);

        // Puis on itère la lecture des lignes sur le fichier
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = fis.read();
                int g = fis.read();
                int b = fis.read();
                img.setPixel(x, y, r, g, b);
            }
        }

        fis.close();
        return img;
    }

    /**
     * Sauvegarde l'image au format texte PPM (P3)
     * @param filename chemin ou nom du fichier de destination
     * @throws IOException en cas d'erreur d'écriture dans le fichier
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

    /**
     * Sauvegarde l'image au format binaire PPM (P6)
     * @param filename chemin ou nom du fichier de destination
     * @throws IOException en cas d'erreur d'écriture dans le fichier
     */
    /*
     * Cette méthode est nommée write_bin dans le sujet du TP1.
     * Or, save_txt existe déjà. Pour respecter les conventions de nommage, on la nommera save_bin.
     */
    public void save_bin(String filename) throws IOException {
        FileOutputStream writer = new FileOutputStream(filename);

        // L'en-tête reste une chaîne de caractères, donc il faut le convertir en bytes avec getBytes()
        String header = "P6\n" + width + " " + height + "\n255\n";
        writer.write(header.getBytes());

        // Ensuite, on écrit les pixels un par un, directement en binaire
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // On cast chaque valeur (int) en (byte)
                writer.write((byte) pixels[y][x][0]); // rouge
                writer.write((byte) pixels[y][x][1]); // vert
                writer.write((byte) pixels[y][x][2]); // bleu
            }
        }

        writer.close();
    }
}
