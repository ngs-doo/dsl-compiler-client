package com.dslplatform.compiler.client;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

public class UtilsTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private Context context;

	@Before
	public void init() {
		context = new ContextMock();
	}

	private File createZip(final String name, final boolean withDirEntries, final String... files) throws IOException {
		final File zip = folder.newFile(name);
		final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
		try {
			for (final String f : files) {
				if (withDirEntries) {
					final int slash = f.lastIndexOf('/');
					if (slash > 0) {
						out.putNextEntry(new ZipEntry(f.substring(0, slash + 1)));
						out.closeEntry();
					}
				}
				out.putNextEntry(new ZipEntry(f));
				out.write(("content of " + f).getBytes(StandardCharsets.UTF_8));
				out.closeEntry();
			}
		} finally {
			out.close();
		}
		return zip;
	}

	@Test
	public void unpacksNestedZipWithoutDirEntries() throws Exception {
		final File zip = createZip("nested.zip", false, "folder/inner.txt");
		final File target = folder.newFolder("out-nested");
		Utils.unpackZip(context, target, zip.toURI().toURL());
		final File inner = new File(target, "folder/inner.txt");
		assertTrue(inner.isFile());
		assertEquals("content of folder/inner.txt", new String(Files.readAllBytes(inner.toPath()), StandardCharsets.UTF_8));
	}

	@Test
	public void unpacksZipWithDirEntries() throws Exception {
		final File zip = createZip("with-dirs.zip", true, "folder/inner.txt");
		final File target = folder.newFolder("out-dirs");
		Utils.unpackZip(context, target, zip.toURI().toURL());
		assertTrue(new File(target, "folder").isDirectory());
		assertTrue(new File(target, "folder/inner.txt").isFile());
	}

	@Test
	public void rejectsZipSlipEntry() throws Exception {
		final File zip = folder.newFile("slip.zip");
		final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
		try {
			out.putNextEntry(new ZipEntry("../../escaped.txt"));
			out.write("pwned".getBytes(StandardCharsets.UTF_8));
			out.closeEntry();
		} finally {
			out.close();
		}
		final File target = folder.newFolder("out-slip");
		try {
			Utils.unpackZip(context, target, zip.toURI().toURL());
			fail("Expecting error for zip-slip entry");
		} catch (final IOException expected) {
			assertTrue(expected.getMessage().contains("outside of the target directory"));
		}
		assertFalse(new File(folder.getRoot(), "../escaped.txt").exists());
	}

	@Test
	public void readXmlAllowsDoctypeWithoutEntities() throws Exception {
		final Either<Document> result = Utils.readXml(new ByteArrayInputStream("<?xml version=\"1.0\"?><!DOCTYPE r><r>ok</r>".getBytes(StandardCharsets.UTF_8)));
		assertTrue(result.isSuccess());
		assertEquals("ok", result.get().getDocumentElement().getTextContent());
	}

	@Test
	public void readXmlAllowsHarmlessInternalEntity() throws Exception {
		final Either<Document> result = Utils.readXml(new ByteArrayInputStream("<?xml version=\"1.0\"?><!DOCTYPE r [<!ENTITY n 'world'>]><r>hello &n;</r>".getBytes(StandardCharsets.UTF_8)));
		assertTrue(result.isSuccess());
		assertEquals("hello world", result.get().getDocumentElement().getTextContent());
	}

	@Test
	public void readXmlRejectsEntityBomb() throws Exception {
		StringBuilder subset = new StringBuilder("<!ENTITY e0 'xxxxxxxxxx'>");
		for (int i = 1; i <= 9; i++) {
			StringBuilder refs = new StringBuilder();
			for (int j = 0; j < 10; j++) refs.append("&e").append(i - 1).append(";");
			subset.append("<!ENTITY e").append(i).append(" '").append(refs).append("'>");
		}
		final String bomb = "<?xml version=\"1.0\"?><!DOCTYPE b [" + subset + "]><b>&e9;</b>";
		final Either<Document> result = Utils.readXml(new ByteArrayInputStream(bomb.getBytes(StandardCharsets.UTF_8)));
		assertFalse("billion laughs must be rejected, got: " + (result.isSuccess() ? "success" : result.explainError()), result.isSuccess());
	}

	@Test
	public void readXmlRejectsCyclicEntities() throws Exception {
		final String cyclic = "<?xml version=\"1.0\"?><!DOCTYPE b [<!ENTITY a '&b;'> <!ENTITY b '&a;'>]><b>&a;</b>";
		final Either<Document> result = Utils.readXml(new ByteArrayInputStream(cyclic.getBytes(StandardCharsets.UTF_8)));
		assertFalse(result.isSuccess());
	}

	@Test
	public void readXmlNeverReadsExternalEntities() throws Exception {
		final File secret = folder.newFile("secret.txt");
		Files.write(secret.toPath(), "TOP-SECRET-SENTINEL".getBytes(StandardCharsets.UTF_8));
		final String xxe = "<?xml version=\"1.0\"?><!DOCTYPE r [<!ENTITY x SYSTEM \"" + secret.toURI() + "\">]><r>&x;</r>";
		final Either<Document> result = Utils.readXml(new ByteArrayInputStream(xxe.getBytes(StandardCharsets.UTF_8)));
		final String leaked = result.isSuccess() ? result.get().getDocumentElement().getTextContent() : result.explainError();
		assertFalse("external entity content must not be readable: " + leaked, leaked.contains("TOP-SECRET-SENTINEL"));
	}

	@Test
	public void readXmlParsesNormalDocument() throws Exception {
		final Either<Document> result = Utils.readXml(new ByteArrayInputStream("<Files><File><Key>a</Key><Value>b</Value></File></Files>".getBytes(StandardCharsets.UTF_8)));
		assertTrue(result.isSuccess());
	}
}
