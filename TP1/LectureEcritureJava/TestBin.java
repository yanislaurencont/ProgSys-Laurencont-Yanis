/**
 * Test de la sauvegarde binaire (P6) de la classe Image.
 * Reproduit la même image que FirstPPM.java (3x2 pixels),
 * mais via Image.save_bin() au lieu d'écrire le fichier à la main.
 */
public class TestBin {
    public static void main(String[] args) {
        // Création d'une image 3 pixels de large, 2 pixels de haut
        Image img = new Image(3, 2);

        // Première ligne : rouge, vert, bleu
        img.setPixel(0, 0, 255, 0, 0);
        img.setPixel(1, 0, 0, 255, 0);
        img.setPixel(2, 0, 0, 0, 255);

        // Deuxième ligne : jaune, blanc, noir
        img.setPixel(0, 1, 255, 255, 0);
        img.setPixel(1, 1, 255, 255, 255);
        img.setPixel(2, 1, 0, 0, 0);

        try {
            img.save_bin("TestBin.ppm");
            System.out.println("Image binaire (P6) créée avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'image binaire : " + e.getMessage());
        }
    }
}
