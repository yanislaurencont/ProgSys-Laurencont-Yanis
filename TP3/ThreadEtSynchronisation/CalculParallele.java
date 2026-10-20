public class CalculParallele {

    private static final int[] DONNEES = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private static final int MULTIPLICATEUR = 1000;
    private Calcule[] threads = new Calcule[DONNEES.length];
    private int cores = Runtime.getRuntime().availableProcessors();
    private int nbThreads = 2 * cores;


    public static void main(String[] args) {
        System.out.println();
    }
}
