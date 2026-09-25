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
     * Retourne le tableau représentant la mémoire physique
     * @return tableau de bytes de la mémoire
     */
    public byte[] getFilesystemMemory() {
        return memory;
    }
}