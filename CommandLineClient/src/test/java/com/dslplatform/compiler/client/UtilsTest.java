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
	public void readXmlRejectsDoctypeDeclaration() throws Exception {
		final String xxe = "<?xml version=\"1.0\"?><!DOCTYPE r [<!ENTITY x SYSTEM \"file:///etc/hostname\">]><r>&x;</r>";
		final Either<Document> result = Utils.readXml(new ByteArrayInputStream(xxe.getBytes(StandardCharsets.UTF_8)));
		assertFalse(result.isSuccess());
	}

	@Test
	public void readXmlParsesNormalDocument() throws Exception {
		final Either<Document> result = Utils.readXml(new ByteArrayInputStream("<Files><File><Key>a</Key><Value>b</Value></File></Files>".getBytes(StandardCharsets.UTF_8)));
		assertTrue(result.isSuccess());
	}
}
