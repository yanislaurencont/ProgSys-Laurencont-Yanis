<<<<<<< HEAD
public class Utils {

    public static int writeInt(byte[] memory, int offset, int value) {
        // TODO: Écrire les 4 octets de 'value' dans 'memory'
        // à partir de 'offset', en big-endian.
        binValue = (byte) value;
        
        return 4;
    }

    public static int readInt(byte[] memory, int offset) {
        // TODO: Reconstituer le int sur 4 octets.
        return 0;
    }

    public static int writeShort(byte[] memory, int offset, short value) {
        // TODO: Écrire les 2 octets de 'value'.
        return 2;
    }

    public static short readShort(byte[] memory, int offset) {
        // TODO: Lire le short sur 2 octets.
        return 0;
    }
}
=======
/**
 * Sérialisation de différents types                                                         19/09/2026    
 * Utils.java
 * @author Yanis Laurençont
 */
public class Utils {

    /** 
     * Ecrit un entier sur 4 octets dans la mémoire
     * @param memoire tableau représentant la mémoire
     * @param offset position à laquelle écrire la valeur
     * @param valeur entier à écrire 
     * @return nombre d'octets écrits 
     */
    public static int writeInt(byte[] memoire, int offset, int valeur) {
        memoire[offset]     = (byte) (valeur >>> 24);
        memoire[offset + 1] = (byte) (valeur >>> 16);
        memoire[offset + 2] = (byte) (valeur >>> 8);
        memoire[offset + 3] = (byte) valeur;
        return 4;
    }

    /**
     * Lit un entier de 4 octets dans la mémoire
     * @param memoire tableau représentant la mémoire
     * @param offset position à partir de laquelle lire la valeur
     * @return entier lu dans la mémoire
     */
    public static int readInt(byte[] memoire, int offset) {
        return ((memoire[offset] & 0xFF) << 24)
             | ((memoire[offset + 1] & 0xFF) << 16)
             | ((memoire[offset + 2] & 0xFF) << 8)
             | (memoire[offset + 3] & 0xFF);
    }

    /**
     * Ecrit un short sur 2 octets dans la mémoire
     * @param memoire tableau représentant la mémoire
     * @param offset position à laquelle écrire la valeur
     * @param valeur short à écrire
     * @return nombre d'octets écrits
     */
    public static int writeShort(byte[] memoire, int offset, short valeur) {
        memoire[offset]     = (byte) (valeur >>> 8);
        memoire[offset + 1] = (byte) valeur;
        return 2;
    }

    /** 
     * Lit un short de 2 octets dans la mémoire
     * @param memoire tableau représentant la mémoire
     * @param offset position à partir de laquelle lire la valeur
     * @return short lu dans la mémoire
     */
    public static short readShort(byte[] memoire, int offset) {
        return (short) (((memoire[offset] & 0xFF) << 8) | (memoire[offset + 1] & 0xFF));
    }

    /**
     * Ecrit un long sur 8 octets dans la mémoire
     * @param memoire tableau représentant la mémoire
     * @param offset position à laquelle écrire la valeur
     * @param valeur long à écrire
     * @return nombre d'octets écrits
     */
    public static int writeLong(byte[] memoire, int offset, long valeur) {
        for (int indexOctet = 0; indexOctet < 8; indexOctet++) {
            memoire[offset + indexOctet] = (byte) (valeur >>> (56 - 8 * indexOctet));
        }
        return 8;
    }

    /**
     * Lit un long de 8 octets dans la mémoire
     * @param memoire tableau représentant la mémoire
     * @param offset position à partir de laquelle lire la valeur
     * @return long lu dans la mémoire
     */
    public static long readLong(byte[] memoire, int offset) {
        long resultat = 0;
        for (int indexOctet = 0; indexOctet < 8; indexOctet++) {
            resultat = (resultat << 8) | (memoire[offset + indexOctet] & 0xFFL);
        }
        return resultat;
    }

    /**
     * Ecrit une chaîne de caractères dans une zone de taille fixe
     * sans depasser la longueur maximale
     * @param memoire tableau représentant la mémoire
     * @param offset position à laquelle écrire la chaîne
     * @param chaine chaîne de caractères à écrire
     * @param longueurMaximale taille maximale de la zone réservée
     * @return taille de la zone réservée
     */
    public static int writeString(byte[] memoire, int offset, String chaine, int longueurMaximale) {
        byte[] octetsChaine = chaine.getBytes();
        int longueurUtile = Math.min(octetsChaine.length, longueurMaximale);

        System.arraycopy(octetsChaine, 0, memoire, offset, longueurUtile);

        // Nettoyage de la zone restante
        for (int indexOctet = longueurUtile; indexOctet < longueurMaximale; indexOctet++) {
            memoire[offset + indexOctet] = 0;
        }
        return longueurMaximale;
    }

    /** 
     * Lit une chaîne de caractères dans une zone de taille fixe
     * Lit jusqu'au premier octet nul rencontré
     * ou jusqu'à la longueur maximale
     * @param memoire tableau représentant la mémoire
     * @param offset position à partir de laquelle lire la chaîne
     * @param longueurMaximale taille maximale de la zone à lire
     * @return chaîne de caractères lue 
     */
    public static String readString(byte[] memoire, int offset, int longueurMaximale) {
        int longueurChaine = 0;
        while (longueurChaine < longueurMaximale && memoire[offset + longueurChaine] != 0) {
            longueurChaine++;
        }
        return new String(memoire, offset, longueurChaine);
    }
}
>>>>>>> a33955d0666d4381306eb45fb548d52b19ff0050
