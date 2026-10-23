public class TestCompteur {
    private CompteurDangereux compteur = new CompteurDangereux();

    for (int indice=0; indice <= 50; indice++) {
        compteur.run();
    }
}