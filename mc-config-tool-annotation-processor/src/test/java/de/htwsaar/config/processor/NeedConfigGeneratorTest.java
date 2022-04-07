package de.htwsaar.config.processor;

import com.google.common.truth.StringSubject;
import com.google.testing.compile.Compilation;
import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;


/**
 *
 * @author hbui
 */
class NeedConfigGeneratorTest {
	
	// testing for common usecases
	@Test
	void processASingleConfig(){
		final String source = 
"package de.htwsaar.config.testing.singleconfig;\n" +
"\n" +
"import de.htwsaar.config.annotation.NeedConfig;\n" +
"\n" +
"@NeedConfig(\n" +
"	name = \"config-single-testing\",\n" + 
"	description = \"config-single-testing-description\"" +
")\n" +
"public class HelloWorld {\n" +

"	public static final int MY_CONFIG = 10;"+
"}";
		
		Compilation compilation =javac()
			.withOptions("-Aconfig.package=de.htwsaar.config.testing.singleconfig")
			.withProcessors( new NeedConfigGenerator() )
			.compile(JavaFileObjects.forSourceString("de.htwsaar.config.testing.singleconfig.HelloWorld", source) );
		assertThat(compilation).succeeded();
		assertThat(compilation)
			.generatedSourceFile("de.htwsaar.config.testing.singleconfig.GenConfigEntry")
			.contentsAsUtf8String().contains("config-single-testing-description");
	}
	
	@Test
	void processAMultipleConfig() {
		
		final String source = 
"package de.htwsaar.config.testing;\n" +
"\n" +
"import de.htwsaar.config.annotation.NeedConfig;\n" +
"\n" +
"@NeedConfig(\n" +
"	name = \"config-testing\",\n" +
"	description = \"config-testing-value-1\"" +
")\n" +
"@NeedConfig(\n" +
"	name = \"config-testing-2\",\n" +
"	description = \"config-testing-value-2\"" +
")\n" +
"public class HelloWorld {\n" +
"	\n" +
"}";
		/*JavaFileObjects.forResource("de/htwsaar/config/testing/HelloWorld.java")*/
		Compilation compilation =javac()
			.withOptions("-Aconfig.package=de.htwsaar.config.testing")
			.withProcessors( new NeedConfigGenerator() )
			.compile(JavaFileObjects.forSourceString("de.htwsaar.config.testing.HelloWorld", source) );
		assertThat(compilation).succeeded();
		StringSubject content = assertThat(compilation)
				.generatedSourceFile("de.htwsaar.config.testing.GenConfigEntry")
				.contentsAsUtf8String();
		content.contains("config-testing-value-1");
		content.contains("config-testing-value-2");
	}
	
	// testing for packages
	@Test
	void processASingleConfigWithoutConfigPackage(){
		final String source = 
"package de.htwsaar.config.testing.implicitpackage;\n" +
"\n" +
"import de.htwsaar.config.annotation.NeedConfig;\n" +
"\n" +
"@NeedConfig(\n" +
"	name = \"config-single-testing\",\n" + 
"	description = \"config-single-testing-implicitpackage\"" +
")\n" +
"public class HelloWorld {\n" +
"	\n" +
"}";
		
		Compilation compilation =javac()
			.withProcessors( new NeedConfigGenerator() )
			.compile(JavaFileObjects.forSourceString("de.htwsaar.config.testing.implicitpackage.HelloWorld", source) );
		assertThat(compilation).succeeded();
		assertThat(compilation)
			.generatedSourceFile("de.htwsaar.config.testing.implicitpackage.GenConfigEntry")
			.contentsAsUtf8String().contains("config-single-testing-implicitpackage");
	}
	
	@Test
	void processASingleConfigWithInvalidConfigPackage(){
		final String source = 
"package de.htwsaar.config.testing.invalidpackage;\n" +
"\n" +
"import de.htwsaar.config.annotation.NeedConfig;\n" +
"\n" +
"@NeedConfig(\n" +
"	name = \"config-single-testing\",\n" + 
"	description = \"config-single-testing-invalidpackage\"" +
")\n" +
"public class HelloWorld {\n" +
"	\n" +
"}";
		
		Compilation compilation =javac()
			.withOptions("-Aconfig.package=\"invalid-package\"")
			.withProcessors( new NeedConfigGenerator() )
			.compile(JavaFileObjects.forSourceString("de.htwsaar.config.testing.invalidpackage.HelloWorld", source) );
		assertThat(compilation).succeeded();
		assertThat(compilation)
			.generatedSourceFile("de.htwsaar.config.testing.invalidpackage.GenConfigEntry")
			.contentsAsUtf8String().contains("config-single-testing-invalidpackage");
	}
	
	//
	@Test
	void processASingleConfigWithoutConfigPackageForManyClasses(){
		String[] source1 ={
        "de.htwsaar.config.testing.common.package_1.HelloWorld",
"package de.htwsaar.config.testing.common.package_1;\n" +
"\n" +
"import de.htwsaar.config.annotation.NeedConfig;\n" +
"\n" +
"@NeedConfig(\n" +
"	name = \"config-single-testing\",\n" + 
"	description = \"config-single-testing-package_1\"" +
")\n" +
"public class HelloWorld {\n" +
"	\n" +
"" +
"}"};
		
		String[] source2 ={
        "de.htwsaar.config.testing.common.package_2.HelloWorld",
"package de.htwsaar.config.testing.common.package_2;\n" +
"\n" +
"import de.htwsaar.config.annotation.NeedConfig;\n" +
"\n" +
"@NeedConfig(\n" +
"	name = \"config-single-testing\",\n" + 
"	description = \"config-single-testing-package_2\"" +
")\n" +
"public class HelloWorld {\n" +
"	\n" +
"}"};
		Compilation compilation =javac()
			.withProcessors( new NeedConfigGenerator() )
			.compile(
				JavaFileObjects.forSourceString(source1[0], source1[1]) 
				,JavaFileObjects.forSourceString(source2[0], source2[1]) 
			);
		assertThat(compilation).succeeded();
		StringSubject genSrc = assertThat(compilation)
				.generatedSourceFile("de.htwsaar.config.testing.common.GenConfigEntry")
				.contentsAsUtf8String();
		genSrc.contains("config-single-testing-package_1");
		genSrc.contains("config-single-testing-package_2");
	}
	
	
	// Processe description
	@Test
	void processAnEmptyDescription(){
		final String source = 
"package de.htwsaar.config.testing.emptydescription;\n" +
"\n" +
"import de.htwsaar.config.annotation.NeedConfig;\n" +
"\n" +
"@NeedConfig(\n" +
"	name = \"config-single-testing\",\n" + 
"	description = {}" + // <-- empty description
")\n" +
"public class HelloWorld {\n" +
"	\n" +
"}";
		
		Compilation compilation =javac()
			.withOptions("-Aconfig.package=de.htwsaar.config.testing.emptydescription")
			.withProcessors( new NeedConfigGenerator() )
			.compile(JavaFileObjects.forSourceString("de.htwsaar.config.testing.emptydescription.HelloWorld", source) );
		assertThat(compilation).succeeded();
		
	}
}
