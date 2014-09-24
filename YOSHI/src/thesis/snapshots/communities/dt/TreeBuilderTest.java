package thesis.snapshots.communities.dt;

import quickdt.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TreeBuilderTest 
{
	public void simpleBmiTest() throws Exception 
	{
		final List<Instance> instances = new ArrayList<Instance>();
        for (int x = 0; x < 10000; x++) 
        {
			final double height = (4 * 12) + Misc.random.nextInt(3 * 12);
			final double weight = 120 + Misc.random.nextInt(110);
			instances.add(Instance.create(bmiHealthy(weight, height), "weight", weight, "height", height));
		}
		final TreeBuilder tb = new TreeBuilder().minNominalAttributeValueOccurances(0);
		final long startTime = System.currentTimeMillis();
		final Node node = tb.buildPredictiveModel(instances).node;
		
		node.dump(System.out);
	}

    private static void serializeDeserialize(final Serializable object) throws IOException, ClassNotFoundException 
    {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1000);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object deserialized = objectInputStream.readObject();
        objectInputStream.close();
    }

	public String bmiHealthy(final double weightInPounds, final double heightInInches) 
	{
		final double bmi = bmi(weightInPounds, heightInInches);
		if (bmi < 20)
			return "underweight";
		else if (bmi > 25)
			return "overweight";
		else
			return "healthy";
	}

	public double bmi(final double weightInPounds, final double heightInInches) 
	{
		return (weightInPounds / (heightInInches * heightInInches)) * 703;
	}
	
	public static void main(String[] args) throws Exception
	{
		TreeBuilderTest tree = new TreeBuilderTest();
		tree.simpleBmiTest();
	}

	//Assert.assertTrue(node.fullRecall(), "Confirm that the node achieves full recall on the training set");
	//Assert.assertTrue(node.size() < 400, "Tree size should be less than 350 nodes");
	//Assert.assertTrue(node.meanDepth() < 6, "Mean depth should be less than 6");
	//Assert.assertTrue((System.currentTimeMillis() - startTime) < 20000, "Building this node should take far less than 20 seconds");
}