package edu.hfut.fr.image.processing.face.recognition.benchmarking;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.ExperimentContext;
import org.openimaj.experiment.RunnableExperiment;
import org.openimaj.experiment.annotations.DependentVariable;
import org.openimaj.experiment.annotations.Experiment;
import org.openimaj.experiment.annotations.IndependentVariable;
import org.openimaj.experiment.annotations.Time;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.AggregatedCMResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAggregator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.experiment.validation.ValidationOperation;
import org.openimaj.experiment.validation.ValidationRunner;
import org.openimaj.experiment.validation.cross.CrossValidator;
import org.openimaj.image.Image;

import edu.hfut.fr.image.processing.face.detection.DatasetFaceDetector;
import edu.hfut.fr.image.processing.face.detection.DetectedFace;
import edu.hfut.fr.image.processing.face.detection.FaceDetector;
import edu.hfut.fr.image.processing.face.recognition.FaceRecogniser;

/**
 * 人脸识别器和分类器
 *
 * @author jimbo
 */
@Experiment(author = "Jonathon Hare", dateCreated = "2012-07-26", description = "Face recognition cross validation experiment")
public class CrossValidationBenchmark<PERSON, IMAGE extends Image<?, IMAGE>, FACE extends DetectedFace> implements
		RunnableExperiment {

	@IndependentVariable
	protected CrossValidator<GroupedDataset<PERSON, ListDataset<FACE>, FACE>> crossValidator;

	@IndependentVariable
	protected GroupedDataset<PERSON, ? extends ListDataset<IMAGE>, IMAGE> dataset;

	@IndependentVariable
	protected FaceDetector<FACE, IMAGE> faceDetector;

	@IndependentVariable
	protected FaceRecogniserProvider<FACE, PERSON> engine;

	@DependentVariable
	protected AggregatedCMResult<PERSON> result;

	/**
	 * 构造函数
	 */
	public CrossValidationBenchmark(CrossValidator<GroupedDataset<PERSON, ListDataset<FACE>, FACE>> crossValidator,
			GroupedDataset<PERSON, ? extends ListDataset<IMAGE>, IMAGE> dataset,
			FaceDetector<FACE, IMAGE> faceDetector, FaceRecogniserProvider<FACE, PERSON> engine) {
		this.dataset = dataset;
		this.crossValidator = crossValidator;
		this.faceDetector = faceDetector;
		this.engine = engine;
	}

	@Override
	public void perform() {
		final CMAggregator<PERSON> aggregator = new CMAggregator<PERSON>();

		final GroupedDataset<PERSON, ListDataset<FACE>, FACE> faceDataset = DatasetFaceDetector.process(dataset,
				faceDetector);

		result = ValidationRunner.run(aggregator, faceDataset, crossValidator,
				new ValidationOperation<GroupedDataset<PERSON, ListDataset<FACE>, FACE>, CMResult<PERSON>>() {
					@Time(identifier = "Train and Evaluate recogniser")
					@Override
					public CMResult<PERSON> evaluate(GroupedDataset<PERSON, ListDataset<FACE>, FACE> training,
							GroupedDataset<PERSON, ListDataset<FACE>, FACE> validation) {
						final FaceRecogniser<FACE, PERSON> rec = engine.create(training);

						final ClassificationEvaluator<CMResult<PERSON>, PERSON, FACE> eval = new ClassificationEvaluator<CMResult<PERSON>, PERSON, FACE>(
								rec, validation, new CMAnalyser<FACE, PERSON>(CMAnalyser.Strategy.SINGLE));

						return eval.analyse(eval.evaluate());
					}
				});
	}

	@Override
	public void setup() {

	}

	@Override
	public void finish(ExperimentContext context) {

	}

}
