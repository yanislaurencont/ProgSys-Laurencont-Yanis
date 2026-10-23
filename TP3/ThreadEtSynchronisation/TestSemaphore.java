/**
 * Test du producteur-consommateur : scénarios équilibré et déséquilibré.
 * @author Yanis Laurençont
 */
public class TestSemaphore {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Scénario équilibré : 2 producteurs, 3 consommateurs ===");
        lancer(2, 15, 3, 10);

        System.out.println("\n=== Scénario déséquilibré : 5 producteurs, 2 consommateurs ===");
        lancer(5, 4, 2, 10);
    }

    // Lance les threads, attend leur fin puis affiche le bilan
    private static void lancer(int nbProducteurs, int produitsParProducteur,
                               int nbConsommateurs, int consommationsParConsommateur)
            throws InterruptedException {
        int debutProduit = ProducteurConsommateur.getTotalProduit();
        int debutConsomme = ProducteurConsommateur.getTotalConsomme();

        Thread[] threads = new Thread[nbProducteurs + nbConsommateurs];
        for (int i = 0; i < nbProducteurs; i++) {
            threads[i] = new Thread(new ProducteurConsommateur.Producteur(i, produitsParProducteur));
        }
        for (int i = 0; i < nbConsommateurs; i++) {
            threads[nbProducteurs + i] = new Thread(
                    new ProducteurConsommateur.Consommateur(i, consommationsParConsommateur));
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        int produits = ProducteurConsommateur.getTotalProduit() - debutProduit;
        int consommes = ProducteurConsommateur.getTotalConsomme() - debutConsomme;
        System.out.println("Produits : " + produits + " | Consommés : " + consommes);
        System.out.println(produits == consommes ? "Cohérent" : "Incohérent !");
    }
}
