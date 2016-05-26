package com.dslplatform.mojo;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;

@RunWith(JUnit4.class)
public class GenerateCodeMojoIntegrationTest extends AbstractMojoTestCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testGenerateCode() throws Exception {
		File pom = getTestFile("src/test/resources/generate-code-pom.xml");
		assertNotNull(pom);
		assertTrue(pom.exists());

		GenerateCodeMojo mojo = (GenerateCodeMojo) lookupMojo(GenerateCodeMojo.GOAL, pom);
		assertNotNull(mojo);
		mojo.setProject(new MavenProjectStub());
		mojo.execute();

		String sourcesPath = mojo.getGeneratedSources();
		TestUtils.assertDir(sourcesPath);
		TestUtils.assertDir(sourcesPath + "/MojoTestModule");
		TestUtils.assertFile(sourcesPath + "/MojoTestModule/Guards.java");
		TestUtils.assertFile(sourcesPath + "/MojoTestModule/MojoTestAggregate.java");

		String[] settings = mojo.getOptions();
		assertEquals(null, settings);
	}
}