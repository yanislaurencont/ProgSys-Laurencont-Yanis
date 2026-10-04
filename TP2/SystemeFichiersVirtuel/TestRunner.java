import java.nio.file.*;
import java.util.*;

/**
 * Point d'entrée de la validation du système de fichiers virtuel            04/10/2026
 * TestRunner.java
 * @author Yanis Laurençont
 *
 * Exécution : java -ea TestRunner [fichier_externe]
 */
public class TestRunner {

    private static final Random ALEATOIRE = new Random(42);

    /**
     * Génère un tableau de données déterministe
     * @param taille nombre d'octets à générer
     * @return tableau de taille octets
     */
    private static byte[] genererDonnees(int taille) {
        byte[] donnees = new byte[taille];
        for (int indexOctet = 0; indexOctet < taille; indexOctet++) {
            donnees[indexOctet] = (byte) (indexOctet * 31 + 7);
        }
        return donnees;
    }

    /**
     * Étape 2 : sérialisation des int et des short
     */
    public static void testStep2() {
        byte[] memoire = new byte[16];

        assert Utils.writeInt(memoire, 4, 0xF0A1B2E3) == 4;
        assert (memoire[4] & 0xFF) == 0xF0 && (memoire[5] & 0xFF) == 0xA1
            && (memoire[6] & 0xFF) == 0xB2 && (memoire[7] & 0xFF) == 0xE3 : "big-endian";
        assert Utils.readInt(memoire, 4) == 0xF0A1B2E3;

        // Bit de poids fort à 1 (complément à deux)
        Utils.writeInt(memoire, 0, 0x80000000);
        assert (memoire[0] & 0xFF) == 0x80 && memoire[1] == 0 && memoire[2] == 0 && memoire[3] == 0;
        assert Utils.readInt(memoire, 0) == 0x80000000;

        // Valeurs limites
        int[] limites = {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, 0x000000FF, 0x0000FF00};
        for (int valeur : limites) {
            Utils.writeInt(memoire, 3, valeur);
            assert Utils.readInt(memoire, 3) == valeur;
        }

        // Valeurs aléatoires, avec vérification directe des octets
        for (int essai = 0; essai < 10000; essai++) {
            int valeur = ALEATOIRE.nextInt();
            Utils.writeInt(memoire, 5, valeur);
            assert Utils.readInt(memoire, 5) == valeur;
            assert memoire[5] == (byte) (valeur >>> 24) && memoire[8] == (byte) valeur;
        }

        assert Utils.writeShort(memoire, 0, (short) 0xF0A1) == 2;
        assert (memoire[0] & 0xFF) == 0xF0 && (memoire[1] & 0xFF) == 0xA1;
        assert Utils.readShort(memoire, 0) == (short) 0xF0A1;

        // Toutes les valeurs de short
        for (int valeur = Short.MIN_VALUE; valeur <= Short.MAX_VALUE; valeur++) {
            Utils.writeShort(memoire, 2, (short) valeur);
            assert Utils.readShort(memoire, 2) == (short) valeur;
        }
        System.out.println("[OK] Étape 2 : int / short");
    }

    /**
     * Étape 3 : sérialisation des long et des chaînes
     */
    public static void testStep3() {
        byte[] memoire = new byte[64];

        long temps = 0x0123456789ABCDEFL;
        assert Utils.writeLong(memoire, 8, temps) == 8;
        for (int indexOctet = 0; indexOctet < 8; indexOctet++) {
            assert (memoire[8 + indexOctet] & 0xFF) == (0x01 + 0x22 * indexOctet) : "octet " + indexOctet;
        }
        assert Utils.readLong(memoire, 8) == temps;

        long[] limites = {0L, -1L, Long.MIN_VALUE, Long.MAX_VALUE, 0x80000000L, System.currentTimeMillis()};
        for (long valeur : limites) {
            Utils.writeLong(memoire, 1, valeur);
            assert Utils.readLong(memoire, 1) == valeur;
        }
        for (int essai = 0; essai < 10000; essai++) {
            long valeur = ALEATOIRE.nextLong();
            Utils.writeLong(memoire, 3, valeur);
            assert Utils.readLong(memoire, 3) == valeur;
        }

        // Chaîne dans une zone de 16 octets
        assert Utils.writeString(memoire, 0, "MYFS", 16) == 16;
        assert memoire[0] == 0x4D && memoire[1] == 0x59 && memoire[2] == 0x46 && memoire[3] == 0x53;
        for (int indexOctet = 4; indexOctet < 16; indexOctet++) {
            assert memoire[indexOctet] == 0 : "remplissage par des zéros";
        }
        assert Utils.readString(memoire, 0, 16).equals("MYFS");

        // Troncature sans débordement sur l'octet suivant
        Arrays.fill(memoire, (byte) 0x55);
        Utils.writeString(memoire, 0, "ABCDEFGHIJ", 4);
        assert Utils.readString(memoire, 0, 4).equals("ABCD");
        assert memoire[4] == 0x55 : "débordement";

        // Nettoyage des anciens octets
        Utils.writeString(memoire, 0, "LONGUECHAINE", 16);
        Utils.writeString(memoire, 0, "ab", 16);
        assert Utils.readString(memoire, 0, 16).equals("ab");
        assert memoire[2] == 0 && memoire[11] == 0;

        // Chaîne vide et chaîne remplissant toute la zone
        Utils.writeString(memoire, 0, "", 8);
        assert Utils.readString(memoire, 0, 8).equals("");
        Utils.writeString(memoire, 0, "12345678", 8);
        assert Utils.readString(memoire, 0, 8).equals("12345678");
        System.out.println("[OK] Étape 3 : long / String");
    }

    /**
     * Étape 4 : architecture mémoire et superbloc
     */
    public static void testStep4() {
        assert MemoryManager.TOTAL_MEMORY == 1048576;
        assert MemoryManager.NUM_BLOCKS == 2048;
        assert MemoryManager.INODE_TABLE_OFFSET == 1024;
        assert MemoryManager.DATA_OFFSET == 129 * 512;
        assert MemoryManager.MAX_INODES == 508;

        MemoryManager gestionnaire = new MemoryManager();
        byte[] memoire = gestionnaire.getFilesystemMemory();
        assert memoire.length == MemoryManager.TOTAL_MEMORY;
        assert Utils.readString(memoire, 0, 16).equals("MYFS1.0");
        assert Utils.readInt(memoire, 16) == 512;
        assert Utils.readInt(memoire, 20) == 1048576;
        assert Utils.readInt(memoire, 24) == 2048;
        assert Utils.readInt(memoire, 28) == 508;

        // Bitmap : blocs 0 à 128 réservés, le reste libre
        for (int indexOctet = 0; indexOctet < 16; indexOctet++) {
            assert (memoire[MemoryManager.BITMAP_OFFSET + indexOctet] & 0xFF) == 0xFF;
        }
        assert (memoire[MemoryManager.BITMAP_OFFSET + 16] & 0xFF) == 0x01;
        assert memoire[MemoryManager.BITMAP_OFFSET + 17] == 0;
        System.out.println("[OK] Étape 4 : architecture mémoire");
    }

    /**
     * Étape 5 : gestion du bitmap
     */
    public static void testStep5() {
        MemoryManager gestionnaire = new MemoryManager();
        byte[] memoire = gestionnaire.getFilesystemMemory();

        for (int numeroBloc = 0; numeroBloc <= 128; numeroBloc++) {
            assert gestionnaire.isBlockUsed(numeroBloc) == 1 : "bloc système " + numeroBloc;
        }
        assert gestionnaire.isBlockUsed(129) == 0;
        assert gestionnaire.isBlockUsed(-1) == -1 && gestionnaire.isBlockUsed(2048) == -1;
        assert !gestionnaire.setBlockUsed(-1, true) && !gestionnaire.setBlockUsed(2048, true);

        // Bloc 129 : octet 16, bit 1, masque 0x02
        assert gestionnaire.setBlockUsed(129, true);
        assert gestionnaire.isBlockUsed(129) == 1;
        assert (memoire[MemoryManager.BITMAP_OFFSET + 16] & 0xFF) == 0x03;
        assert gestionnaire.setBlockUsed(129, false);
        assert gestionnaire.isBlockUsed(129) == 0;
        assert (memoire[MemoryManager.BITMAP_OFFSET + 16] & 0xFF) == 0x01;

        // Bit 7 d'un octet (test du signe du byte)
        gestionnaire.setBlockUsed(143, true);
        assert gestionnaire.isBlockUsed(143) == 1;
        assert gestionnaire.isBlockUsed(142) == 0 && gestionnaire.isBlockUsed(144) == 0;
        gestionnaire.setBlockUsed(143, false);

        // Allocation : jamais de bloc système
        assert gestionnaire.allocateBlock() == 129;
        assert gestionnaire.allocateBlock() == 130;
        assert gestionnaire.isBlockUsed(129) == 1 && gestionnaire.isBlockUsed(130) == 1;
        gestionnaire.setBlockUsed(129, false);
        assert gestionnaire.allocateBlock() == 129 : "réutilisation du premier bloc libre";

        // Épuisement de la mémoire
        MemoryManager gestionnairePlein = new MemoryManager();
        int nombreAlloues = 0;
        int numeroBloc;
        while ((numeroBloc = gestionnairePlein.allocateBlock()) != -1) {
            assert numeroBloc >= 129;
            nombreAlloues++;
        }
        assert nombreAlloues == 1919 : "1919 blocs de données";
        assert gestionnairePlein.countFreeBlocks() == 0;
        System.out.println("[OK] Étape 5 : bitmap");
    }

    /**
     * Étape 6 : layout des inodes
     */
    public static void testStep6() {
        MemoryManager gestionnaire = new MemoryManager();
        assert new Inode(gestionnaire, 0).getInodeOffset() == 1024;
        assert new Inode(gestionnaire, 1).getInodeOffset() == 1152;
        assert new Inode(gestionnaire, 4).getInodeOffset() == 1536;
        assert new Inode(gestionnaire, 507).getInodeOffset() + 128 == MemoryManager.DATA_OFFSET;

        // Lecture de champs écrits directement dans la mémoire
        byte[] memoire = gestionnaire.getFilesystemMemory();
        int offset = new Inode(gestionnaire, 3).getInodeOffset();
        Utils.writeInt(memoire, offset + 4, 1);
        Utils.writeInt(memoire, offset + 8, 777);
        Utils.writeInt(memoire, offset + 28, 129);
        Utils.writeInt(memoire, offset + 28 + 36, 138);

        Inode inode = new Inode(gestionnaire, 3);
        assert inode.getFileType() == 1 && inode.getFileSize() == 777;
        int[] pointeurs = inode.getDirectPointers();
        assert pointeurs.length == 10 && pointeurs[0] == 129 && pointeurs[9] == 138 && pointeurs[1] == 0;
        System.out.println("[OK] Étape 6 : layout des inodes");
    }

    /**
     * Étape 7 : sérialisation d'un inode
     */
    public static void testStep7() {
        MemoryManager gestionnaire = new MemoryManager();
        byte[] memoire = gestionnaire.getFilesystemMemory();
        Inode inode = new Inode(gestionnaire, 5);

        int[] pointeurs = {129, 130, 131, 132, 133, 134, 135, 136, 137, 138};
        long creation = 1700000000000L;
        long modification = 1700000005000L;
        inode.writeToMemory(1, 4096, creation, modification, pointeurs, 200, (short) 0x1A4, 2);

        // Vérification directe des octets
        int offset = inode.getInodeOffset();
        assert Utils.readInt(memoire, offset) == 5;
        assert Utils.readInt(memoire, offset + 4) == 1;
        assert Utils.readInt(memoire, offset + 8) == 4096;
        assert Utils.readLong(memoire, offset + 12) == creation;
        assert Utils.readLong(memoire, offset + 20) == modification;
        assert Utils.readInt(memoire, offset + 28) == 129 && Utils.readInt(memoire, offset + 64) == 138;
        assert Utils.readInt(memoire, offset + 68) == 200;
        assert Utils.readShort(memoire, offset + 72) == 0x1A4;
        assert Utils.readInt(memoire, offset + 74) == 2;
        assert memoire[offset + 78] == 0 && memoire[offset + 127] == 0 : "octets réservés";

        // Lecture par les getters
        assert inode.getStoredNumber() == 5 && inode.getFileType() == 1 && inode.getFileSize() == 4096;
        assert inode.getCreationTime() == creation && inode.getModificationTime() == modification;
        assert Arrays.equals(inode.getDirectPointers(), pointeurs);
        assert inode.getIndirectPointer() == 200 && inode.getPermissions() == 0x1A4 && inode.getLinkCount() == 2;

        // Les inodes voisins ne sont pas modifiés
        for (int indexOctet = 0; indexOctet < 128; indexOctet++) {
            assert memoire[new Inode(gestionnaire, 4).getInodeOffset() + indexOctet] == 0;
            assert memoire[new Inode(gestionnaire, 6).getInodeOffset() + indexOctet] == 0;
        }

        // Réversibilité sur des valeurs aléatoires
        for (int essai = 0; essai < 5000; essai++) {
            int numero = ALEATOIRE.nextInt(MemoryManager.MAX_INODES);
            Inode inodeAleatoire = new Inode(gestionnaire, numero);
            int type = ALEATOIRE.nextInt();
            int taille = ALEATOIRE.nextInt();
            int indirect = ALEATOIRE.nextInt();
            int liens = ALEATOIRE.nextInt();
            long creationAleatoire = ALEATOIRE.nextLong();
            long modificationAleatoire = ALEATOIRE.nextLong();
            short permissions = (short) ALEATOIRE.nextInt();
            int[] directs = new int[10];
            for (int indexPointeur = 0; indexPointeur < 10; indexPointeur++) {
                directs[indexPointeur] = ALEATOIRE.nextInt();
            }

            inodeAleatoire.writeToMemory(type, taille, creationAleatoire, modificationAleatoire,
                                         directs, indirect, permissions, liens);

            assert inodeAleatoire.getStoredNumber() == numero;
            assert inodeAleatoire.getFileType() == type && inodeAleatoire.getFileSize() == taille;
            assert inodeAleatoire.getCreationTime() == creationAleatoire;
            assert inodeAleatoire.getModificationTime() == modificationAleatoire;
            assert Arrays.equals(inodeAleatoire.getDirectPointers(), directs);
            assert inodeAleatoire.getIndirectPointer() == indirect;
            assert inodeAleatoire.getPermissions() == permissions && inodeAleatoire.getLinkCount() == liens;
        }
        System.out.println("[OK] Étape 7 : sérialisation des inodes");
    }

    /**
     * Étape 8 : création de fichiers et allocation des inodes
     */
    public static void testStep8() {
        VirtualFileSystem vfs = new VirtualFileSystem();
        MemoryManager gestionnaire = vfs.getMemoryManager();

        int inodeA = vfs.createFileInode("/", "a.txt");
        int inodeB = vfs.createFileInode("/", "b.txt");
        assert inodeA == 0 && inodeB == 1 : "inodes alloués dans l'ordre";

        Inode inode = new Inode(gestionnaire, inodeA);
        assert inode.getFileType() == VirtualFileSystem.TYPE_FILE;
        assert inode.getFileSize() == 0;
        assert inode.getStoredNumber() == 0;
        assert inode.getLinkCount() == 1;
        assert inode.getCreationTime() > 0 && inode.getCreationTime() == inode.getModificationTime();
        for (int pointeur : inode.getDirectPointers()) {
            assert pointeur == 0;
        }
        assert vfs.readFile(inodeA).length == 0;
        assert gestionnaire.countFreeBlocks() == 1919 : "aucun bloc de données alloué";

        // Remplissage complet de la table des inodes
        VirtualFileSystem vfsPlein = new VirtualFileSystem();
        for (int indexFichier = 0; indexFichier < MemoryManager.MAX_INODES; indexFichier++) {
            assert vfsPlein.createFile("/", "f" + indexFichier);
        }
        assert !vfsPlein.createFile("/", "trop") : "table d'inodes pleine";
        System.out.println("[OK] Étape 8 : création de fichiers");
    }

    /**
     * Étape 9 : écriture et lecture de fichiers
     */
    public static void testStep9() {
        VirtualFileSystem vfs = new VirtualFileSystem();
        MemoryManager gestionnaire = vfs.getMemoryManager();
        byte[] memoire = gestionnaire.getFilesystemMemory();

        // Fichier de exactement un bloc
        int inodeUnBloc = vfs.createFileInode("/", "un_bloc");
        byte[] donnees512 = genererDonnees(512);
        assert vfs.writeFile(inodeUnBloc, donnees512);

        Inode inodeA = new Inode(gestionnaire, inodeUnBloc);
        assert inodeA.getFileSize() == 512;
        int[] pointeursA = inodeA.getDirectPointers();
        assert pointeursA[0] == 129 && pointeursA[1] == 0 : "un seul pointeur direct";
        assert gestionnaire.isBlockUsed(129) == 1;
        for (int indexOctet = 0; indexOctet < 512; indexOctet++) {
            assert memoire[129 * 512 + indexOctet] == donnees512[indexOctet] : "octet physique " + indexOctet;
        }
        assert Arrays.equals(vfs.readFile(inodeUnBloc), donnees512);

        // Fichier de 513 octets : deux blocs
        int inodeDeuxBlocs = vfs.createFileInode("/", "deux_blocs");
        byte[] donnees513 = genererDonnees(513);
        assert vfs.writeFile(inodeDeuxBlocs, donnees513);

        Inode inodeB = new Inode(gestionnaire, inodeDeuxBlocs);
        int[] pointeursB = inodeB.getDirectPointers();
        assert inodeB.getFileSize() == 513;
        assert pointeursB[0] == 130 && pointeursB[1] == 131 && pointeursB[2] == 0;
        for (int indexOctet = 0; indexOctet < 512; indexOctet++) {
            assert memoire[pointeursB[0] * 512 + indexOctet] == donnees513[indexOctet];
        }
        assert memoire[pointeursB[1] * 512] == donnees513[512] : "dernier octet dans le 2e bloc";
        for (int indexOctet = 1; indexOctet < 512; indexOctet++) {
            assert memoire[pointeursB[1] * 512 + indexOctet] == 0 : "reste du bloc à zéro";
        }
        assert Arrays.equals(vfs.readFile(inodeDeuxBlocs), donnees513);

        // Réécriture plus courte : les anciens blocs sont libérés
        assert vfs.writeFile(inodeDeuxBlocs, genererDonnees(10));
        assert Arrays.equals(vfs.readFile(inodeDeuxBlocs), genererDonnees(10));
        assert gestionnaire.countFreeBlocks() == 1919 - 2 : "pas de fuite de blocs";

        // Fichier vide
        int inodeVide = vfs.createFileInode("/", "vide");
        assert vfs.writeFile(inodeVide, new byte[0]);
        assert vfs.readFile(inodeVide).length == 0;

        // Dix blocs acceptés, dix blocs + 1 octet refusés
        int inodeDixBlocs = vfs.createFileInode("/", "dix_blocs");
        assert vfs.writeFile(inodeDixBlocs, genererDonnees(10 * 512));
        assert Arrays.equals(vfs.readFile(inodeDixBlocs), genererDonnees(10 * 512));

        int blocsLibres = gestionnaire.countFreeBlocks();
        int inodeTropGros = vfs.createFileInode("/", "trop_gros");
        assert !vfs.writeFile(inodeTropGros, genererDonnees(10 * 512 + 1));
        assert gestionnaire.countFreeBlocks() == blocsLibres : "aucun bloc alloué lors d'un refus";
        assert new Inode(gestionnaire, inodeTropGros).getFileSize() == 0;

        // Inodes invalides ou non utilisés
        assert !vfs.writeFile(400, genererDonnees(1));
        assert !vfs.writeFile(-1, genererDonnees(1));
        System.out.println("[OK] Étape 9 : écriture / lecture");
    }

    /**
     * Étape 10 : suppression et fragmentation
     */
    public static void testStep10() {
        VirtualFileSystem vfs = new VirtualFileSystem();
        MemoryManager gestionnaire = vfs.getMemoryManager();

        int inodeA = vfs.createFileInode("/", "a");
        int inodeB = vfs.createFileInode("/", "b");
        int inodeC = vfs.createFileInode("/", "c");
        vfs.writeFile(inodeA, genererDonnees(512));   // bloc 129
        vfs.writeFile(inodeB, genererDonnees(512));   // bloc 130
        vfs.writeFile(inodeC, genererDonnees(512));   // bloc 131
        assert gestionnaire.countFreeRuns() == 1;

        assert vfs.deleteFile(inodeB);
        assert gestionnaire.isBlockUsed(130) == 0 : "bloc libéré";
        assert new Inode(gestionnaire, inodeB).getFileType() == VirtualFileSystem.TYPE_FREE : "inode libéré";
        assert vfs.readFile(inodeB).length == 0;
        assert !vfs.deleteFile(inodeB) : "double suppression";
        assert gestionnaire.countFreeRuns() == 2 : "un trou : fragmentation externe";

        // Un fichier de 2 blocs utilise le trou puis la fin : blocs non contigus
        int inodeD = vfs.createFileInode("/", "d");
        assert inodeD == inodeB : "inode réutilisé";
        byte[] donneesD = genererDonnees(1024);
        assert vfs.writeFile(inodeD, donneesD);
        int[] pointeursD = new Inode(gestionnaire, inodeD).getDirectPointers();
        assert pointeursD[0] == 130 && pointeursD[1] == 132 : "blocs dispersés";
        assert Arrays.equals(vfs.readFile(inodeD), donneesD) : "lecture malgré la fragmentation";

        // Suppression de tout : retour à l'état initial
        vfs.deleteFile(inodeA);
        vfs.deleteFile(inodeC);
        vfs.deleteFile(inodeD);
        assert gestionnaire.countFreeBlocks() == 1919 && gestionnaire.countFreeRuns() == 1;

        // Cycles de création et suppression : pas de fuite
        for (int cycle = 0; cycle < 3000; cycle++) {
            int numeroInode = vfs.createFileInode("/", "tmp");
            assert numeroInode != -1 && vfs.writeFile(numeroInode, genererDonnees(2000));
            assert vfs.deleteFile(numeroInode);
        }
        assert gestionnaire.countFreeBlocks() == 1919;

        // Même nombre de blocs libres, fragmentation différente
        VirtualFileSystem vfsContigu = new VirtualFileSystem();
        VirtualFileSystem vfsAlterne = new VirtualFileSystem();
        int[] inodesContigu = new int[6];
        int[] inodesAlterne = new int[6];
        for (int indexFichier = 0; indexFichier < 6; indexFichier++) {
            inodesContigu[indexFichier] = vfsContigu.createFileInode("/", "x");
            vfsContigu.writeFile(inodesContigu[indexFichier], genererDonnees(512));
            inodesAlterne[indexFichier] = vfsAlterne.createFileInode("/", "y");
            vfsAlterne.writeFile(inodesAlterne[indexFichier], genererDonnees(512));
        }
        for (int indexFichier = 0; indexFichier < 3; indexFichier++) {
            vfsContigu.deleteFile(inodesContigu[indexFichier]);       // trou contigu : LLLOOO + fin libre
        }
        for (int indexFichier = 0; indexFichier < 6; indexFichier += 2) {
            vfsAlterne.deleteFile(inodesAlterne[indexFichier]);       // trous alternés : LOLOLO + fin libre
        }
        assert vfsContigu.getMemoryManager().countFreeBlocks() == vfsAlterne.getMemoryManager().countFreeBlocks();
        assert vfsContigu.getMemoryManager().countFreeRuns() == 2;
        assert vfsAlterne.getMemoryManager().countFreeRuns() == 4;
        System.out.println("[OK] Étape 10 : suppression et fragmentation");
    }

    /**
     * Étape 11 : test avec un fichier externe au VFS
     * Seuls les 10 premiers blocs (5120 octets) sont testés si le fichier est plus gros
     * @param chemin chemin du fichier externe
     */
    public static void testExternalFile(String chemin) {
        try {
            byte[] original = Files.readAllBytes(Paths.get(chemin));
            int tailleMaximale = Inode.DIRECT_POINTERS * MemoryManager.BLOCK_SIZE;
            if (original.length > tailleMaximale) {
                System.out.println("[INFO] Fichier de " + original.length + " octets > " + tailleMaximale
                        + " : seuls les " + tailleMaximale + " premiers octets sont testés.");
                original = Arrays.copyOf(original, tailleMaximale);
            }

            VirtualFileSystem vfs = new VirtualFileSystem();
            int numeroInode = vfs.createFileInode("/", Paths.get(chemin).getFileName().toString());
            assert numeroInode != -1;
            assert vfs.writeFile(numeroInode, original) : "écriture du fichier externe";

            Inode inode = new Inode(vfs.getMemoryManager(), numeroInode);
            assert inode.getFileSize() == original.length : "taille dans l'inode";

            // Vérification directe dans la mémoire
            byte[] memoire = vfs.getMemoryManager().getFilesystemMemory();
            int[] pointeurs = inode.getDirectPointers();
            for (int indexOctet = 0; indexOctet < original.length; indexOctet++) {
                int numeroBloc = pointeurs[indexOctet / 512];
                assert memoire[numeroBloc * 512 + (indexOctet % 512)] == original[indexOctet]
                        : "octet physique " + indexOctet;
            }

            byte[] relu = vfs.readFile(numeroInode);
            assert relu.length == original.length : "longueur après relecture";
            assert Arrays.equals(relu, original) : "contenu après relecture";
            System.out.println("[OK] Étape 11 : fichier externe '" + chemin + "' (" + original.length + " octets)");
        } catch (java.io.IOException e) {
            System.out.println("[ERREUR] Lecture impossible de " + chemin + " : " + e.getMessage());
            throw new AssertionError(e);
        }
    }

    /**
     * Étape 12 : point d'entrée appelant les tests de chaque étape
     * @param args fichier externe optionnel pour l'étape 11
     */
    public static void main(String[] args) {
        boolean assertionsActives = false;
        assert assertionsActives = true;
        if (!assertionsActives) {
            System.out.println("[ATTENTION] Assertions désactivées : lancez avec java -ea TestRunner");
        }

        testStep2();
        testStep3();
        testStep4();
        testStep5();
        testStep6();
        testStep7();
        testStep8();
        testStep9();
        testStep10();

        if (args.length > 0) {
            testExternalFile(args[0]);
        } else {
            System.out.println("[INFO] Aucun fichier externe fourni.");
        }

        System.out.println("=== TOUS LES TESTS SONT TERMINÉS ===");
    }
}
