package edu.hfut.lpr.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;

import edu.hfut.lpr.utils.Configurator;

/**
 * 照片类测试
 *
 * @author wanggang
 *
 */
public class PhotoTest {

	@Test
	public void cloneTest() throws Exception {
		InputStream fstream = Configurator.getConfigurator().getResourceAsStream("en-snapshots/test_041.jpg");
		assertNotNull(fstream);
		Photo photo = new Photo(fstream);
		fstream.close();

		assertNotNull(photo);
		assertNotNull(photo.image);

		Photo clone = photo.clone();
		assertEquals(photo, clone);
		assertEquals(photo.hashCode(), clone.hashCode());
		clone.close();
		photo.close();
	}

}
