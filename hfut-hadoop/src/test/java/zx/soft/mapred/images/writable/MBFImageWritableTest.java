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
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

import edu.hfut.mapred.images.writable.ColorImageWritable;

@RunWith(JUnit4.class)
public class MBFImageWritableTest {

	private final String IMAGE_FILE_NAME = "test";
	private final String IMAGE_FILE_FORMAT = "jpg";

	@Test
	public void testSerialization() throws IOException {
		try {
			URL resourceUrl = getClass().getResource("/1.jpg");
			File f = new File(resourceUrl.toURI());
			MBFImage image = ImageUtilities.readMBF(f);
			ColorImageWritable mbfiw = new ColorImageWritable();
			mbfiw.setFileName(IMAGE_FILE_NAME);
			mbfiw.setFormat(IMAGE_FILE_FORMAT);
			mbfiw.setImage(image);
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			mbfiw.write(new DataOutputStream(bao));
			ByteArrayInputStream bai = new ByteArrayInputStream(bao.toByteArray());

			ColorImageWritable mbfiw2 = new ColorImageWritable();
			mbfiw2.readFields(new DataInputStream(bai));

			assertEquals("Image width serialization error", mbfiw.getImage().getWidth(), mbfiw2.getImage().getWidth());

			assertEquals("Image height serialization error", mbfiw.getImage().getHeight(), mbfiw2.getImage()
					.getHeight());

			assertEquals("Image band count serialization error", mbfiw.getImage().numBands(), mbfiw2.getImage()
					.numBands());

			for (int b = 0; b < mbfiw.getImage().numBands(); b++) {
				assertArrayEquals("Image pixel array serialization error", mbfiw.getImage().bands.get(b)
						.getFloatPixelVector(), mbfiw2.getImage().bands.get(b).getFloatPixelVector(), 0.001f);
			}

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
