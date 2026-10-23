public class Calcule extends Thread {
    private final int valeur;
    private final int multiplicateur;
    private final int index;
    private long sommeTotal;

    public Calcule(int valeur, int multiplicateur, int index) {
        this.valeur = valeur;       
        this.multiplicateur = multiplicateur;
        this.index = index;        
    }

    @Override
    public void run() {
        // Implémente le calcul intensif.
        // Même logique que dans la boucle interne de CalculSequentiel.
        long resultat = 0;
        for (int indice = 0; indice < multiplicateur; indice++) {
            resultat += valeur * valeur + valeur;
        }

        sommeTotal = resultat;
        System.out.println("Thread " + index + " : traitement de " + valeur + " terminé : " + resultat);
    }

    long getResultat() {
        return sommeTotal;
    }
}
