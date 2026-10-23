import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/**
 * Producteurs et consommateurs autour d'un buffer borné, protégé par 3 sémaphores.
 * @author Yanis Laurençont
 */
public class ProducteurConsommateur {
    private static final int TAILLE_BUFFER = 5;
    private static final Queue<Integer> buffer = new LinkedList<>();

    private static final Semaphore placesLibres = new Semaphore(TAILLE_BUFFER);
    private static final Semaphore elementsDisponibles = new Semaphore(0);
    private static final Semaphore mutexBuffer = new Semaphore(1);

    private static int totalProduit = 0;
    private static int totalConsomme = 0;

    static int genererProduit() {
        return (int) (Math.random() * 100);
    }

    public static int getTotalProduit() {
        return totalProduit;
    }

    public static int getTotalConsomme() {
        return totalConsomme;
    }

    static class Producteur implements Runnable {
        private final int id;
        private final int nombreProduits;

        public Producteur(int id, int nombreProduits) {
            this.id = id;
            this.nombreProduits = nombreProduits;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < nombreProduits; i++) {
                    int produit = genererProduit();

                    placesLibres.acquire();     // attend une place libre
                    mutexBuffer.acquire();      // accès exclusif au buffer
                    buffer.add(produit);
                    totalProduit++;
                    System.out.println("Producteur " + id + " ajoute " + produit
                            + " -> buffer " + buffer);
                    mutexBuffer.release();      // libère le buffer
                    elementsDisponibles.release(); // un élément de plus

                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                System.err.println("Producteur " + id + " interrompu");
            }
        }
    }

    static class Consommateur implements Runnable {
        private final int id;
        private final int nombreConsommations;

        public Consommateur(int id, int nombreConsommations) {
            this.id = id;
            this.nombreConsommations = nombreConsommations;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < nombreConsommations; i++) {
                    elementsDisponibles.acquire(); // attend un élément
                    mutexBuffer.acquire();         // accès exclusif au buffer
                    Integer produit = buffer.poll();
                    totalConsomme++;
                    System.out.println("Consommateur " + id + " retire " + produit
                            + " -> buffer " + buffer);
                    mutexBuffer.release();         // libère le buffer
                    placesLibres.release();        // une place de plus

                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                System.err.println("Consommateur " + id + " interrompu");
            }
        }
    }
}
