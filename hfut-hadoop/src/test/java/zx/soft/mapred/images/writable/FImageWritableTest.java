package zx.soft.mapred.images.writable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

import edu.hfut.mapred.images.writable.GrayImageWritable;

@RunWith(JUnit4.class)
public class FImageWritableTest {

	private final String IMAGE_FILE_NAME = "test";
	private final String IMAGE_FILE_FORMAT = "jpg";

	@Test
	public void testSerialization() throws IOException {
		try {
			URL resourceUrl = getClass().getResource("/1_gray.jpg");
			File f = new File(resourceUrl.toURI());
			FImage image = ImageUtilities.readF(f);
			GrayImageWritable fiw = new GrayImageWritable();
			fiw.setFileName(IMAGE_FILE_NAME);
			fiw.setFormat(IMAGE_FILE_FORMAT);
			fiw.setImage(image);
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			fiw.write(new DataOutputStream(bao));
			ByteArrayInputStream bai = new ByteArrayInputStream(bao.toByteArray());
			GrayImageWritable fiw2 = new GrayImageWritable();
			fiw2.readFields(new DataInputStream(bai));

			assertEquals("Image width serialization error", fiw.getImage().getWidth(), fiw2.getImage().getWidth());

			assertEquals("Image height serialization error", fiw.getImage().getHeight(), fiw2.getImage().getHeight());

			assertArrayEquals("Image pixel array serialization error", fiw.getImage().getFloatPixelVector(), fiw2
					.getImage().getFloatPixelVector(), 0.001f);

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
