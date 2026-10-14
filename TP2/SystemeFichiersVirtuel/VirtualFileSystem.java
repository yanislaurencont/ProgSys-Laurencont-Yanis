/**
 * Système de fichiers virtuel : création, écriture, lecture, suppression    04/10/2026
 * VirtualFileSystem.java
 * @author Yanis Laurençont
 */
public class VirtualFileSystem {

    public static final int TYPE_FREE      = 0;
    public static final int TYPE_FILE      = 1;
    public static final int TYPE_DIRECTORY = 2;

    /** Permissions par défaut : 0644 en octal */
    public static final short DEFAULT_PERMISSIONS = 0644;

    private MemoryManager memoryManager;

    /**
     * Constructeur instanciant le gestionnaire de mémoire du VFS
     */
    public VirtualFileSystem() {
        this.memoryManager = new MemoryManager();
    }

    /**
     * Cherche le premier inode libre de la table (type 0)
     * @return numéro de l'inode libre, -1 si la table est pleine
     */
    private int allocateInode() {
        for (int numeroInode = 0; numeroInode < MemoryManager.MAX_INODES; numeroInode++) {
            if (new Inode(memoryManager, numeroInode).getFileType() == TYPE_FREE) {
                return numeroInode;
            }
        }
        return -1;
    }

    /**
     * Crée un fichier vide et retourne le numéro de son inode
     * Les répertoires ne sont pas gérés à ce stade du TP
     * @param repertoire répertoire du fichier (non utilisé pour l'instant)
     * @param nomFichier nom du fichier (non utilisé pour l'instant)
     * @return numéro de l'inode créé, -1 si la table d'inodes est pleine
     */
    public int createFileInode(String repertoire, String nomFichier) {
        int numeroInode = allocateInode();
        if (numeroInode == -1) {
            return -1;
        }

        long maintenant = System.currentTimeMillis();
        new Inode(memoryManager, numeroInode).writeToMemory(
                TYPE_FILE, 0, maintenant, maintenant,
                new int[Inode.DIRECT_POINTERS], 0, DEFAULT_PERMISSIONS, 1);
        return numeroInode;
    }

    /**
     * Crée un fichier vide : inode de type fichier, taille nulle, aucun bloc
     * @param repertoire répertoire du fichier (non utilisé pour l'instant)
     * @param nomFichier nom du fichier (non utilisé pour l'instant)
     * @return true si le fichier a été créé, false si la table d'inodes est pleine
     */
    public boolean createFile(String repertoire, String nomFichier) {
        return createFileInode(repertoire, nomFichier) != -1;
    }

    /**
     * Écrit le contenu d'un fichier en remplaçant l'ancien contenu
     * Si l'écriture échoue, l'ancien contenu reste intact
     * @param numeroInode numéro de l'inode du fichier
     * @param donnees octets à écrire
     * @return false si l'inode est invalide, si le fichier dépasse 10 blocs
     *         ou si la mémoire est pleine, true sinon
     */
    public boolean writeFile(int numeroInode, byte[] donnees) {
        if (donnees == null || numeroInode < 0 || numeroInode >= MemoryManager.MAX_INODES) {
            return false;
        }
        Inode inode = new Inode(memoryManager, numeroInode);
        if (inode.getFileType() != TYPE_FILE) {
            return false;
        }

        int blocksNeeded = (donnees.length + MemoryManager.BLOCK_SIZE - 1) / MemoryManager.BLOCK_SIZE;
        if (blocksNeeded > Inode.DIRECT_POINTERS) {
            return false;
        }

        // Allocation des nouveaux blocs, annulée si la mémoire est pleine
        int[] blockPointers = new int[Inode.DIRECT_POINTERS];
        for (int indexBloc = 0; indexBloc < blocksNeeded; indexBloc++) {
            int numeroBloc = memoryManager.allocateBlock();
            if (numeroBloc == -1) {
                for (int indexAnnule = 0; indexAnnule < indexBloc; indexAnnule++) {
                    memoryManager.setBlockUsed(blockPointers[indexAnnule], false);
                }
                return false;
            }
            blockPointers[indexBloc] = numeroBloc;
        }

        byte[] memory = memoryManager.getFilesystemMemory();
        int bytesRemaining = donnees.length;
        int dataSrcOffset = 0;

        for (int indexBloc = 0; indexBloc < blocksNeeded; indexBloc++) {
            int quantiteACopier = Math.min(bytesRemaining, MemoryManager.BLOCK_SIZE);
            int offsetBloc = MemoryManager.blockOffset(blockPointers[indexBloc]);

            System.arraycopy(donnees, dataSrcOffset, memory, offsetBloc, quantiteACopier);

            // Nettoyage de la fin du bloc
            for (int indexOctet = quantiteACopier; indexOctet < MemoryManager.BLOCK_SIZE; indexOctet++) {
                memory[offsetBloc + indexOctet] = 0;
            }

            dataSrcOffset += quantiteACopier;
            bytesRemaining -= quantiteACopier;
        }

        // Libération des anciens blocs du fichier
        int[] anciensPointeurs = inode.getDirectPointers();
        int anciensBlocs = (inode.getFileSize() + MemoryManager.BLOCK_SIZE - 1) / MemoryManager.BLOCK_SIZE;
        for (int indexBloc = 0; indexBloc < anciensBlocs && indexBloc < Inode.DIRECT_POINTERS; indexBloc++) {
            memoryManager.setBlockUsed(anciensPointeurs[indexBloc], false);
        }

        // Mise à jour de l'inode (création, permissions et liens conservés)
        inode.writeToMemory(TYPE_FILE, donnees.length, inode.getCreationTime(),
                System.currentTimeMillis(), blockPointers, 0,
                inode.getPermissions(), inode.getLinkCount());
        return true;
    }

    /**
     * Lit le contenu complet d'un fichier
     * @param numeroInode numéro de l'inode du fichier
     * @return octets du fichier, tableau vide si le fichier est vide ou invalide
     */
    public byte[] readFile(int numeroInode) {
        if (numeroInode < 0 || numeroInode >= MemoryManager.MAX_INODES) {
            return new byte[0];
        }
        Inode inode = new Inode(memoryManager, numeroInode);
        if (inode.getFileType() != TYPE_FILE) {
            return new byte[0];
        }

        int fileSize = inode.getFileSize();
        if (fileSize == 0) {
            return new byte[0];
        }

        byte[] fileData = new byte[fileSize];
        byte[] memory = memoryManager.getFilesystemMemory();
        int[] blockPointers = inode.getDirectPointers();

        int octetsCopies = 0;
        for (int indexBloc = 0; octetsCopies < fileSize && indexBloc < Inode.DIRECT_POINTERS; indexBloc++) {
            int quantiteACopier = Math.min(fileSize - octetsCopies, MemoryManager.BLOCK_SIZE);
            int offsetBloc = MemoryManager.blockOffset(blockPointers[indexBloc]);

            System.arraycopy(memory, offsetBloc, fileData, octetsCopies, quantiteACopier);
            octetsCopies += quantiteACopier;
        }
        return fileData;
    }

    /**
     * Supprime un fichier : libère ses blocs dans le bitmap puis son inode
     * @param numeroInode numéro de l'inode du fichier
     * @return false si l'inode n'est pas un fichier existant, true sinon
     */
    public boolean deleteFile(int numeroInode) {
        if (numeroInode < 0 || numeroInode >= MemoryManager.MAX_INODES) {
            return false;
        }
        Inode inode = new Inode(memoryManager, numeroInode);
        if (inode.getFileType() != TYPE_FILE) {
            return false;
        }

        int[] blockPointers = inode.getDirectPointers();
        int blocsUtilises = (inode.getFileSize() + MemoryManager.BLOCK_SIZE - 1) / MemoryManager.BLOCK_SIZE;
        for (int indexBloc = 0; indexBloc < blocsUtilises && indexBloc < Inode.DIRECT_POINTERS; indexBloc++) {
            memoryManager.setBlockUsed(blockPointers[indexBloc], false);
        }

        inode.clear();
        return true;
    }

    /**
     * Retourne le gestionnaire de mémoire du VFS
     * @return gestionnaire de mémoire
     */
    public MemoryManager getMemoryManager() {
        return memoryManager;
    }
}
