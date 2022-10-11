package de.htwsaar.config;

import java.nio.file.Path;

/**
 *
 * @author hbui
 */
public class ImportCfgFileNotFound extends LSConfigException {

    private final transient Path importedPath;

    public ImportCfgFileNotFound(Path importedPath) {
        super("Imported file '" + importedPath.normalize().toString() + "' not found");
        this.importedPath = importedPath;
    }

    public Path getImportedPath() {
        return importedPath;
    }

}
