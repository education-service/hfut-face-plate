package edu.hfut.fr.image.objectdetection.haar;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * 读取opencv harr cascade xml文件
 *
 * @author wanghao
 */
public class OCVHaarLoader {

	private static final float ICV_STAGE_THRESHOLD_BIAS = 0.0001f;

	private static final String NEXT_NODE = "next";
	private static final String PARENT_NODE = "parent";
	private static final String STAGE_THRESHOLD_NODE = "stage_threshold";
	private static final String ANONYMOUS_NODE = "_";
	private static final String RIGHT_NODE_NODE = "right_node";
	private static final String RIGHT_VAL_NODE = "right_val";
	private static final String LEFT_NODE_NODE = "left_node";
	private static final String LEFT_VAL_NODE = "left_val";
	private static final String THRESHOLD_NODE = "threshold";
	private static final String TILTED_NODE = "tilted";
	private static final String RECTS_NODE = "rects";
	private static final String FEATURE_NODE = "feature";
	private static final String TREES_NODE = "trees";
	private static final String STAGES_NODE = "stages";
	private static final String SIZE_NODE = "size";
	private static final String OCV_STORAGE_NODE = "opencv_storage";

	static class TreeNode {
		HaarFeature feature;
		float threshold;
		float left_val;
		float right_val;
		int left_node = -1;
		int right_node = -1;
	}

	static class StageNode {
		private int parent = -1;
		private int next = -1;
		private float threshold;
		private List<List<TreeNode>> trees = new ArrayList<List<TreeNode>>();
	}

	static class OCVHaarClassifierNode {
		int width;
		int height;
		String name;
		boolean hasTiltedFeatures = false;
		List<StageNode> stages = new ArrayList<StageNode>();
	}

	/**
	 * 通过读取xml文件获得信息
	 */
	static OCVHaarClassifierNode readXPP(InputStream in) throws IOException {
		try {
			final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			final XmlPullParser reader = factory.newPullParser();

			reader.setInput(in, null);

			reader.nextTag();
			checkNode(reader, OCV_STORAGE_NODE);

			reader.nextTag();
			if (!"opencv-haar-classifier".equals(reader.getAttributeValue(null, "type_id"))) {
				throw new IOException("Unsupported format: " + reader.getAttributeValue(null, "type_id"));
			}

			final OCVHaarClassifierNode root = new OCVHaarClassifierNode();
			root.name = reader.getName();

			reader.nextTag();
			checkNode(reader, SIZE_NODE);

			final String sizeStr = reader.nextText();
			final String[] widthHeight = sizeStr.trim().split(" ");
			if (widthHeight.length != 2) {
				throw new IOException("expecting 'w h' for size element, got: " + sizeStr);
			}

			root.width = Integer.parseInt(widthHeight[0]);
			root.height = Integer.parseInt(widthHeight[1]);

			reader.nextTag();
			checkNode(reader, STAGES_NODE);

			while (reader.nextTag() == XmlPullParser.START_TAG) {
				checkNode(reader, ANONYMOUS_NODE);

				final StageNode currentStage = new StageNode();
				root.stages.add(currentStage);

				reader.nextTag();
				checkNode(reader, TREES_NODE);

				while (reader.nextTag() == XmlPullParser.START_TAG) {
					checkNode(reader, ANONYMOUS_NODE);

					final List<TreeNode> currentTree = new ArrayList<TreeNode>();
					currentStage.trees.add(currentTree);

					while (reader.nextTag() == XmlPullParser.START_TAG) {
						checkNode(reader, ANONYMOUS_NODE);

						final List<WeightedRectangle> regions = new ArrayList<WeightedRectangle>(3);

						reader.nextTag();
						checkNode(reader, FEATURE_NODE);

						reader.nextTag();
						checkNode(reader, RECTS_NODE);

						while (reader.nextTag() == XmlPullParser.START_TAG) {
							checkNode(reader, ANONYMOUS_NODE);
							regions.add(WeightedRectangle.parse(reader.nextText()));
						}

						reader.nextTag();
						checkNode(reader, TILTED_NODE);
						final boolean tilted = "1".equals(reader.nextText());

						if (tilted)
							root.hasTiltedFeatures = true;

						reader.nextTag();
						checkNode(reader, FEATURE_NODE);

						final HaarFeature currentFeature = HaarFeature.create(regions, tilted);

						reader.nextTag();
						checkNode(reader, THRESHOLD_NODE);
						final float threshold = (float) Double.parseDouble(reader.nextText());

						final TreeNode treeNode = new TreeNode();
						treeNode.threshold = threshold;
						treeNode.feature = currentFeature;

						reader.nextTag();
						checkNode(reader, LEFT_VAL_NODE, LEFT_NODE_NODE);
						final String leftText = reader.nextText();
						if ("left_val".equals(reader.getName())) {
							treeNode.left_val = Float.parseFloat(leftText);
						} else {
							treeNode.left_node = Integer.parseInt(leftText);
						}
						reader.nextTag();
						checkNode(reader, RIGHT_VAL_NODE, RIGHT_NODE_NODE);
						final String rightText = reader.nextText();
						if ("right_val".equals(reader.getName())) {
							treeNode.right_val = Float.parseFloat(rightText);
						} else {
							treeNode.right_node = Integer.parseInt(rightText);
						}

						reader.nextTag();
						checkNode(reader, ANONYMOUS_NODE);
						currentTree.add(treeNode);
					}
				}

				reader.nextTag();
				checkNode(reader, STAGE_THRESHOLD_NODE);
				currentStage.threshold = Float.parseFloat(reader.nextText()) - ICV_STAGE_THRESHOLD_BIAS;

				reader.nextTag();
				checkNode(reader, PARENT_NODE);
				currentStage.parent = Integer.parseInt(reader.nextText());

				reader.nextTag();
				checkNode(reader, NEXT_NODE);
				currentStage.next = Integer.parseInt(reader.nextText());

				reader.nextTag();
				checkNode(reader, ANONYMOUS_NODE);
			}

			return root;
		} catch (final XmlPullParserException ex) {
			throw new IOException(ex);
		}
	}

	/**
	 *从opencv xml文件中读取信息,返回级联的对象
	 */
	public static StageTreeClassifier read(InputStream is) throws IOException {
		final OCVHaarClassifierNode root = readXPP(is);

		return buildCascade(root);
	}

	private static StageTreeClassifier buildCascade(OCVHaarClassifierNode root) throws IOException {
		return new StageTreeClassifier(root.width, root.height, root.name, root.hasTiltedFeatures,
				buildStages(root.stages));
	}

	private static Stage buildStages(List<StageNode> stageNodes) throws IOException {
		final Stage[] stages = new Stage[stageNodes.size()];
		for (int i = 0; i < stages.length; i++) {
			final StageNode node = stageNodes.get(i);

			stages[i] = new Stage(node.threshold, buildClassifiers(node.trees), null, null);
		}

		Stage root = null;
		boolean isCascade = true;
		for (int i = 0; i < stages.length; i++) {
			final StageNode node = stageNodes.get(i);

			if (node.parent == -1 && node.next == -1) {
				if (root == null) {
					root = stages[i];
				} else {
					throw new IOException("Inconsistent cascade/tree: multiple roots found");
				}
			}

			if (node.parent != -1) {
				if (stages[node.parent].successStage == null) {
					stages[node.parent].successStage = stages[i];
				}
			}

			if (node.next != -1) {
				isCascade = false; // it's a tree
				stages[i].failureStage = stages[node.next];
			}
		}

		if (!isCascade) {
			optimiseTree(root);
		}

		return root;
	}

	private static void optimiseTree(Stage root) {
		final Deque<Stage> stack = new ArrayDeque<Stage>();
		stack.push(root);

		Stage failureStage = null;
		while (!stack.isEmpty()) {
			final Stage stage = stack.pop();

			if (stage.failureStage == null) {
				stage.failureStage = failureStage;

				if (stage.successStage != null) {
					stack.push(stage.successStage);
				}
			} else if (stage.failureStage != failureStage) {
				stack.push(stage);

				failureStage = stage.failureStage;

				if (stage.successStage != null) {
					stack.push(stage.successStage);
				}
			} else {
				stack.push(stage.failureStage);

				failureStage = null;
			}
		}
	}

	private static Classifier[] buildClassifiers(final List<List<TreeNode>> trees) {
		final Classifier[] classifiers = new Classifier[trees.size()];

		for (int i = 0; i < classifiers.length; i++) {
			classifiers[i] = buildClassifier(trees.get(i));
		}

		return classifiers;
	}

	private static Classifier buildClassifier(final List<TreeNode> tree) {
		return buildClassifier(tree, tree.get(0));
	}

	private static Classifier buildClassifier(final List<TreeNode> tree, TreeNode current) {
		final HaarFeatureClassifier fc = new HaarFeatureClassifier(current.feature, current.threshold, null, null);

		if (current.left_node == -1) {
			fc.left = new ValueClassifier(current.left_val);
		} else {
			fc.left = buildClassifier(tree, tree.get(current.left_node));
		}

		if (current.right_node == -1) {
			fc.right = new ValueClassifier(current.right_val);
		} else {
			fc.right = buildClassifier(tree, tree.get(current.right_node));
		}

		return fc;
	}

	private static void checkNode(XmlPullParser reader, String... expected) throws IOException {
		for (final String e : expected)
			if (e.equals(reader.getName()))
				return;

		throw new IOException("Unexpected tag: " + reader.getName() + " (expected: " + Arrays.toString(expected) + ")");
	}

}
