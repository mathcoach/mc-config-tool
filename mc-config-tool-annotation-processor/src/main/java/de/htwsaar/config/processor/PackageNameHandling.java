package de.htwsaar.config.processor;

import static de.htwsaar.config.processor.MCAbstractAnnotationProcessor.validePackageName;
import java.util.List;

/**
 *
 * @author hbui
 */
final class PackageNameHandling {
    
    private PackageNameHandling() {
        // prevent to make an instance of this class!
    }
    
    /**
     * find the common name-space parts of {@code packages} and {@code name.split(".")}.
     * The argument {@code packages} contains only the common parts after 
     * this method. For example (See unit test)
     */
    static void updatePackage(final List<String> packages, final String name, final LogWriter lw) {
        String[] split = name.split("\\.");
		lw.debug("split.length " + split.length);
		if (packages.isEmpty()) {
			lw.info("package is empty");
			for (int i = 0; i < split.length - 1; i++) {
				lw.debug("add " + split[i] + " to package");
				packages.add(split[i]);
			}
		} else {
			int i;
			int shorter = split.length - 1 < packages.size()
					? split.length - 1
					: packages.size();
			String p;
			for (i = 0; i < shorter; ++i) {
				p = packages.get(i);
				lw.debug("compare " + p + " to " + split[i]);
				if (!p.equals(split[i])) {
					break;
				}
			}
			while (packages.size() > i) {
				packages.remove(packages.size() - 1);
			}
		}
    }
    
    
    
    /**
     * build a Java lexical valid package form a list of package name-spaces and a
     * String, which represents an optimal package.
     * 
     * @param packages 
     * @param apackage 
     * @return the final version of package
     */
    static String buildPackage(final List<String> packages, final String apackage, final LogWriter lw) {
        String finalPackageName = apackage;
		lw.info("\tConfigurated package name of GenNeedConfigEntry is " + (finalPackageName == null ? "null." : "'" + finalPackageName + "'."));
		if (finalPackageName == null || finalPackageName.trim().length() == 0) {
			lw.warn("\tCannot use given package name of GenNeedConfigEntry '"
					+ apackage + "'.");
			finalPackageName = String.join(".", packages);
		} else {
			if (!validePackageName(apackage)) {
				lw.warn("\t" + apackage + " is not a valide Java Package");
				finalPackageName = String.join(".", packages);
			}
		}
		if (finalPackageName.isEmpty()) {
			finalPackageName = "auto.gen.config";
		}
		return finalPackageName;
    }
    
}
