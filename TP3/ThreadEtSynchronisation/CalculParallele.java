/**
 * Version parallèle de CalculSequentiel : un thread par élément.
 * @author Yanis Laurençont
 */
public class CalculParallele {
    private static final int[] DONNEES = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private static final int MULTIPLICATEUR = 1000;

    public static void main(String[] args) {
        System.out.println("=== Calcul Parallèle ===");

        long debut = System.nanoTime();
        long sommeTotal = 0;

        // Création des threads (un par donnée)
        Calcule[] threads = new Calcule[DONNEES.length];
        for (int i = 0; i < DONNEES.length; i++) {
            threads[i] = new Calcule(DONNEES[i], MULTIPLICATEUR, i);
        }

        // Démarrage : start() crée le fil d'exécution (run() lancerait dans le thread courant)
        for (Calcule t : threads) {
            t.start();
        }

        // Attente de la fin de tous les threads
        for (Calcule t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Attente interrompue : " + e.getMessage());
                return;
            }
        }

        // Agrégation dans le thread principal : pas de concurrence sur la somme
        for (Calcule t : threads) {
            sommeTotal += t.getResultat();
        }

        long duree = (System.nanoTime() - debut) / 1_000_000;
        System.out.println("Résultat total : " + sommeTotal);
        System.out.println("Durée : " + duree + " ms");
    }
}
