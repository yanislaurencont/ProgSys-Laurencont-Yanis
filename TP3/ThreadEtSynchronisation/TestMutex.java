/**
 * Test de CompteurSecurise : le résultat doit toujours être correct.
 * @author Yanis Laurençont
 */
public class TestMutex {
    private static final int NB_TESTS = 50;
    private static final int NB_THREADS = 9;
    private static final int NB_INCREMENTS = 1111111;

    public static void main(String[] args) throws InterruptedException {
        int attendu = NB_THREADS * NB_INCREMENTS;
        int correct = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        long debut = System.nanoTime();

        for (int test = 0; test < NB_TESTS; test++) {
            CompteurSecurise.resetCompteur();

            Thread[] threads = new Thread[NB_THREADS];
            for (int i = 0; i < NB_THREADS; i++) {
                threads[i] = new Thread(new CompteurSecurise.Incrementeur("T" + i, NB_INCREMENTS));
                threads[i].start();
            }
            for (Thread t : threads) {
                t.join();
            }

            int resultat = CompteurSecurise.getCompteur();
            if (resultat == attendu) {
                correct++;
            }
            min = Math.min(min, resultat);
            max = Math.max(max, resultat);
            System.out.println("Test " + (test + 1) + " : " + resultat + " (attendu : " + attendu + ")");
        }

        long duree = (System.nanoTime() - debut) / 1_000_000;

        System.out.println("Statistiques (version synchronisée)");
        System.out.println("Résultats corrects : " + correct + "/" + NB_TESTS);
        System.out.println("Taux de réussite : " + (100.0 * correct / NB_TESTS) + " %");
        System.out.println("Minimum : " + min + " | Maximum : " + max);
        System.out.println("Durée totale : " + duree + " ms");
    }
}
