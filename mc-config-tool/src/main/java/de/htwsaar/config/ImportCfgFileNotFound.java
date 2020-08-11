package de.htwsaar.config;

import java.nio.file.Path;

/**
 *
 * @author hbui
 */
public class ImportCfgFileNotFound extends LSConfigException {

	public ImportCfgFileNotFound(Path importedPath) {
		super("Imported file '" + importedPath.normalize().toString() + "' not found");
	}
	
}
