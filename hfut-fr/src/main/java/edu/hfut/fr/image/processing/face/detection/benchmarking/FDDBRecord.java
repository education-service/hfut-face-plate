package edu.hfut.fr.image.processing.face.detection.benchmarking;

import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

import edu.hfut.fr.image.processing.face.detection.DetectedFace;

/**
 * FDDB记录借口
 *
 * @author wanghao
 */
public interface FDDBRecord {

	public String getImageName();

	public FImage getFImage();

	public MBFImage getMBFImage();

	public List<? extends DetectedFace> getGroundTruth();

}
