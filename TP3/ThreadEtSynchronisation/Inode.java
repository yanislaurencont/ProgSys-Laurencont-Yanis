import java.util.Arrays;

/**
 * Structure et sérialisation d'un inode dans la table des inodes            03/10/2026
 * Inode.java
 * @author Yanis Laurençont
 */
public class Inode {

    private MemoryManager memoryManager;
    private int inodeNumber;

    public static final int INODE_SIZE      = 128;
    public static final int DIRECT_POINTERS = 10;

    // Offsets
    public static final int NUMBER_OFFSET        = 0;
    public static final int TYPE_OFFSET          = 4;
    public static final int SIZE_OFFSET          = 8;
    public static final int CREATION_OFFSET      = 12;
    public static final int MODIFICATION_OFFSET  = 20;
    public static final int DIRECT_OFFSET        = 28;
    public static final int INDIRECT_OFFSET      = 68;
    public static final int PERMISSIONS_OFFSET   = 72;
    public static final int LINK_COUNT_OFFSET    = 74;

    /**
     * Constructeur associant un inode à son numéro
     * @param memoryManager gestionnaire de la mémoire virtuelle
     * @param inodeNumber numéro de l'inode dans la table
     */
    public Inode(MemoryManager memoryManager, int inodeNumber) {
        this.memoryManager = memoryManager;
        this.inodeNumber = inodeNumber;
    }

    /**
     * Calcule l'offset exact de l'inode dans la mémoire
     * @return INODE_TABLE_OFFSET + numéro * INODE_SIZE
     */
    public int getInodeOffset() {
        return MemoryManager.INODE_TABLE_OFFSET + inodeNumber * INODE_SIZE;
    }

    /**
     * Retourne le numéro de l'inode donné à la construction
     * @return numéro de l'inode
     */
    public int getInodeNumber() {
        return inodeNumber;
    }

    /**
     * Lit le numéro d'inode stocké dans la mémoire (offset + 0)
     * @return numéro d'inode écrit dans la mémoire
     */
    public int getStoredNumber() {
        return Utils.readInt(memoryManager.getFilesystemMemory(), getInodeOffset() + NUMBER_OFFSET);
    }

    /**
     * Lit le type de l'inode (offset + 4)
     * @return type du fichier, 0 si l'inode est libre
     */
    public int getFileType() {
        return Utils.readInt(memoryManager.getFilesystemMemory(), getInodeOffset() + TYPE_OFFSET);
    }

    /**
     * Lit la taille du fichier (offset + 8)
     * @return taille du fichier en octets
     */
    public int getFileSize() {
        return Utils.readInt(memoryManager.getFilesystemMemory(), getInodeOffset() + SIZE_OFFSET);
    }

    /**
     * Lit la date de création (offset + 12)
     * @return date de création en millisecondes
     */
    public long getCreationTime() {
        return Utils.readLong(memoryManager.getFilesystemMemory(), getInodeOffset() + CREATION_OFFSET);
    }

    /**
     * Lit la date de modification (offset + 20)
     * @return date de modification en millisecondes
     */
    public long getModificationTime() {
        return Utils.readLong(memoryManager.getFilesystemMemory(), getInodeOffset() + MODIFICATION_OFFSET);
    }

    /**
     * Lit les 10 pointeurs directs (offset + 28)
     * @return tableau des numéros de blocs, 0 pour un pointeur inutilisé
     */
    public int[] getDirectPointers() {
        byte[] memory = memoryManager.getFilesystemMemory();
        int[] pointers = new int[DIRECT_POINTERS];
        int offset = getInodeOffset() + DIRECT_OFFSET;

        for (int indexPointeur = 0; indexPointeur < DIRECT_POINTERS; indexPointeur++) {
            pointers[indexPointeur] = Utils.readInt(memory, offset + 4 * indexPointeur);
        }
        return pointers;
    }

    /**
     * Lit le pointeur indirect (offset + 68)
     * @return numéro du bloc d'indirection, 0 si inutilisé
     */
    public int getIndirectPointer() {
        return Utils.readInt(memoryManager.getFilesystemMemory(), getInodeOffset() + INDIRECT_OFFSET);
    }

    /**
     * Lit les permissions (offset + 72)
     * @return permissions du fichier
     */
    public short getPermissions() {
        return Utils.readShort(memoryManager.getFilesystemMemory(), getInodeOffset() + PERMISSIONS_OFFSET);
    }

    /**
     * Lit le nombre de liens (offset + 74)
     * @return nombre de liens vers l'inode
     */
    public int getLinkCount() {
        return Utils.readInt(memoryManager.getFilesystemMemory(), getInodeOffset() + LINK_COUNT_OFFSET);
    }

    /**
     * Sérialise tous les champs de l'inode dans la mémoire, dans l'ordre imposé
     * La zone de 128 octets est d'abord remise à zéro
     * @param fileType type du fichier
     * @param fileSize taille du fichier en octets
     * @param creationTime date de création
     * @param modificationTime date de modification
     * @param directPointers numéros des blocs directs
     * @param indirectPointer numéro du bloc d'indirection
     * @param permissions permissions du fichier
     * @param linkCount nombre de liens
     */
    public void writeToMemory(int fileType, int fileSize, long creationTime,
                              long modificationTime, int[] directPointers,
                              int indirectPointer, short permissions, int linkCount) {
        byte[] memory = memoryManager.getFilesystemMemory();
        int offset = getInodeOffset();

        // Nettoyage de la zone de l'inode
        Arrays.fill(memory, offset, offset + INODE_SIZE, (byte) 0);

        int curseur = offset;
        curseur += Utils.writeInt(memory, curseur, inodeNumber);
        curseur += Utils.writeInt(memory, curseur, fileType);
        curseur += Utils.writeInt(memory, curseur, fileSize);
        curseur += Utils.writeLong(memory, curseur, creationTime);
        curseur += Utils.writeLong(memory, curseur, modificationTime);
        for (int indexPointeur = 0; indexPointeur < DIRECT_POINTERS; indexPointeur++) {
            int pointeur = 0;
            if (directPointers != null && indexPointeur < directPointers.length) {
                pointeur = directPointers[indexPointeur];
            }
            curseur += Utils.writeInt(memory, curseur, pointeur);
        }
        curseur += Utils.writeInt(memory, curseur, indirectPointer);
        curseur += Utils.writeShort(memory, curseur, permissions);
        curseur += Utils.writeInt(memory, curseur, linkCount);
    }

    /**
     * Remet à zéro les 128 octets de l'inode, ce qui le rend disponible (type 0)
     */
    public void clear() {
        byte[] memory = memoryManager.getFilesystemMemory();
        int offset = getInodeOffset();
        Arrays.fill(memory, offset, offset + INODE_SIZE, (byte) 0);
    }
}
