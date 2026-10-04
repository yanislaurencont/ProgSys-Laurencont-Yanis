/**
 * Gestionnaire de la mémoire virtuelle et de l'allocation des blocs         23/09/2026
 * MemoryManager.java
 * @author Yanis Laurençont
 */
public class MemoryManager {

    public static final int BLOCK_SIZE   = 512;
    public static final int TOTAL_MEMORY = 1024 * 1024;
    public static final int NUM_BLOCKS   = TOTAL_MEMORY / BLOCK_SIZE;

    public static final int SUPERBLOCK_OFFSET  = 0;
    public static final int BITMAP_OFFSET      = BLOCK_SIZE;
    public static final int INODE_TABLE_OFFSET = 2 * BLOCK_SIZE;
    public static final int DATA_OFFSET        = 129 * BLOCK_SIZE;

    public static final int FIRST_DATA_BLOCK = 129;

    public static final int INODE_SIZE       = 128;
    public static final int INODE_TABLE_SIZE = DATA_OFFSET - INODE_TABLE_OFFSET;
    public static final int MAX_INODES       = INODE_TABLE_SIZE / INODE_SIZE;

    private byte[] memory;

    /**
     * Constructeur instanciant la mémoire du VFS
     */
    public MemoryManager() {
        this.memory = new byte[TOTAL_MEMORY];
        initializeFilesystem();
    }

    /**
     * Initialise le système de fichiers en écrivant le superbloc
     * et en marquant les blocs système comme occupés
     */
    private void initializeFilesystem() {
        writeSuperblock();

        // Réservation des blocs 0 à 127
        for (int indexOctet = 0; indexOctet < 16; indexOctet++) {
            memory[BITMAP_OFFSET + indexOctet] = (byte) 0xFF;
        }

        // Réservation du bloc 128
        memory[BITMAP_OFFSET + 16] |= 0x01;
    }

    /**
     * Écrit le metadata du superbloc dans le premier bloc de mémoire
     */
    private void writeSuperblock() {
        Utils.writeString(memory, SUPERBLOCK_OFFSET, "MYFS1.0", 16);
        Utils.writeInt(memory, SUPERBLOCK_OFFSET + 16, BLOCK_SIZE);
        Utils.writeInt(memory, SUPERBLOCK_OFFSET + 20, TOTAL_MEMORY);
        Utils.writeInt(memory, SUPERBLOCK_OFFSET + 24, NUM_BLOCKS);
        Utils.writeInt(memory, SUPERBLOCK_OFFSET + 28, MAX_INODES);
    }

    /**
     * Marque un bloc comme occupé ou libre dans le bitmap
     * @param numeroBloc numéro du bloc à modifier
     * @param occupe true pour marquer le bloc occupé, false pour le libérer
     * @return false si le numéro de bloc est invalide, true sinon
     */
    public boolean setBlockUsed(int numeroBloc, boolean occupe) {
        if (numeroBloc < 0 || numeroBloc >= NUM_BLOCKS) {
            return false;
        }

        int indexOctet  = numeroBloc / 8;
        int positionBit = numeroBloc % 8;
        int offset      = BITMAP_OFFSET + indexOctet;
        int masque      = 1 << positionBit;

        if (occupe) {
            memory[offset] |= masque;
        } else {
            memory[offset] &= ~masque;
        }
        return true;
    }

    /**
     * Indique si un bloc est occupé
     * @param numeroBloc numéro du bloc à tester
     * @return 1 si le bloc est occupé, 0 s'il est libre, -1 si le numéro est invalide
     */
    public int isBlockUsed(int numeroBloc) {
        if (numeroBloc < 0 || numeroBloc >= NUM_BLOCKS) {
            return -1;
        }

        int indexOctet  = numeroBloc / 8;
        int positionBit = numeroBloc % 8;

        return (memory[BITMAP_OFFSET + indexOctet] >> positionBit) & 1;
    }

    /**
     * Alloue le premier bloc libre de la zone de données
     * et le marque immédiatement comme occupé
     * @return numéro du bloc alloué, -1 si la mémoire est pleine
     */
    public int allocateBlock() {
        for (int numeroBloc = FIRST_DATA_BLOCK; numeroBloc < NUM_BLOCKS; numeroBloc++) {
            if (isBlockUsed(numeroBloc) == 0) {
                setBlockUsed(numeroBloc, true);
                return numeroBloc;
            }
        }
        return -1;
    }

    /**
     * Compte les blocs libres de la zone de données
     * @return nombre de blocs libres
     */
    public int countFreeBlocks() {
        int nombreLibres = 0;
        for (int numeroBloc = FIRST_DATA_BLOCK; numeroBloc < NUM_BLOCKS; numeroBloc++) {
            if (isBlockUsed(numeroBloc) == 0) {
                nombreLibres++;
            }
        }
        return nombreLibres;
    }

    /**
     * Compte les séquences contiguës de blocs libres de la zone de données
     * Pour un même nombre de blocs libres, plus il y a de séquences,
     * plus la fragmentation externe est importante
     * @return nombre de séquences de blocs libres
     */
    public int countFreeRuns() {
        int nombreSequences = 0;
        boolean dansSequence = false;
        for (int numeroBloc = FIRST_DATA_BLOCK; numeroBloc < NUM_BLOCKS; numeroBloc++) {
            boolean libre = isBlockUsed(numeroBloc) == 0;
            if (libre && !dansSequence) {
                nombreSequences++;
            }
            dansSequence = libre;
        }
        return nombreSequences;
    }

    /**
     * Calcule l'adresse physique de début d'un bloc
     * @param numeroBloc numéro du bloc
     * @return offset de début du bloc dans le tableau mémoire
     */
    public static int blockOffset(int numeroBloc) {
        return numeroBloc * BLOCK_SIZE;
    }

    /**
     * Retourne le tableau représentant la mémoire physique
     * @return tableau de bytes de la mémoire
     */
    public byte[] getFilesystemMemory() {
        return memory;
    }
}
