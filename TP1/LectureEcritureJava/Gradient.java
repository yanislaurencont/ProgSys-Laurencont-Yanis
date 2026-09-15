/**
 * Création d'un dégradé de couleurs
 * @author Yanis Laurençont
 */
public class Gradient {
    public static void main(String[] args) {
        int largeur = 200;
        int hauteur = 100;
        Image img = new Image(largeur, hauteur);

        // Génération du dégradé de bleu
        // Le bleu varie de 0 (à gauche) à 255 (à droite) selon la colonne x.
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                int bleu = (x * 255) / (largeur - 1);
                img.setPixel(x, y, 0, 0, bleu);
            }
        }

        try {
            img.save_txt("gradient.ppm");
            System.out.println("Dégradé créé avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du dégradé : " + e.getMessage());
        }
    }
}
