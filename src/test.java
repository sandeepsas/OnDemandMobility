

import java.io.File;
import java.lang.reflect.Method;
import java.util.Random;

import org.canova.api.records.reader.SequenceRecordReader;
import org.canova.api.records.reader.impl.CSVSequenceRecordReader;
import org.canova.api.split.FileSplit;
import org.deeplearning4j.datasets.canova.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;


public class test {
    public static void main( String[] args ) throws Exception {
        int lstmLayerSize = 20;					//Number of units in each GravesLSTM layer
        int miniBatchSize = 32;						//Size of mini batch to use when  training
        int exampleLength = 1000;					//Length of each training example sequence to use. This could certainly be increased
        int tbpttLength = 30;                       //Length for truncated backpropagation through time. i.e., do parameter updates ever 50 characters
        int numEpochs = 200;							//Total number of training epochs
        int generateSamplesEveryNMinibatches = 10;  //How frequently to generate samples from the network? 1000 characters / 50 tbptt length: 20 parameter updates per minibatch
        String generationInitialization = null;		//Optional character initialization; a random character is used if null
        // Above is Used to 'prime' the LSTM with a character sequence to continue/complete.
        // Initialization characters must all be in CharacterIterator.getMinimalCharacterSet() by default
        Random rng = new Random(12345);

        //Get the dataset using the record reader. CSVSequenceRecordReader handles loading/parsing
        int numLinesToSkip = 1;
        String delimiter = ",";
        SequenceRecordReader featureReader = new CSVSequenceRecordReader(numLinesToSkip, delimiter);
        SequenceRecordReader labelReader = new CSVSequenceRecordReader(numLinesToSkip, delimiter);
        featureReader.initialize(new FileSplit(new File("src/main/resources/stocks/input.csv")));
        labelReader.initialize(new FileSplit(new File("src/main/resources/stocks/output.csv")));

        int numPossibleLabels = 0;
        boolean regression = true;
        DataSetIterator iter = new SequenceRecordReaderDataSetIterator(featureReader, labelReader, miniBatchSize, numPossibleLabels, regression);

        //Set up network configuration:
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(10)
            .learningRate(0.01)
            .rmsDecay(0.95)
            .seed(12345)
            .regularization(true)
            .l2(0.001)
            .weightInit(WeightInit.XAVIER)
            .updater(Updater.RMSPROP)
            .list(3)
            .layer(0, new GravesLSTM.Builder().nIn(iter.inputColumns()).nOut(lstmLayerSize)
                .activation("tanh").build())
            .layer(1, new GravesLSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                .activation("tanh").build())
            .layer(2, new RnnOutputLayer.Builder(LossFunction.MSE).activation("identity")        //MCXENT + softmax for classification
            .nIn(lstmLayerSize).nOut(iter.inputColumns()).build())
            .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
            .pretrain(false).backprop(true)
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(10));

        //Print the  number of parameters in the network (and for each layer)
        Layer[] layers = net.getLayers();
        int totalNumParams = 0;
        for( int i=0; i<layers.length; i++ ){
            int nParams = layers[i].numParams();
            System.out.println("Number of parameters in layer " + i + ": " + nParams);
            totalNumParams += nParams;
        }
        System.out.println("Total number of network parameters: " + totalNumParams);

/*        //Do training
        int miniBatchNumber = 0;
        for( int i=0; i<numEpochs; i++ ){
            while(iter.hasNext()){
                DataSet ds = iter.next();
                net.fit(ds);

                INDArray nextInput = Nd4j.create(new double[]{735.719971,99.860001,89.019997,101},new int[]{1,4});
                INDArray output = net.rnnTimeStep(nextInput);
                System.out.println(output);

            }
            iter.reset();	//Reset iterator for another epoch
        }*/

        for( int i=0; i<numEpochs; i++ ) {

            net.fit(iter);

//            INDArray nextInput = Nd4j.create(new double[]{2,4}, new int[]{1, 2});
//            INDArray output = net.rnnTimeStep(nextInput);
//            System.out.println(output);
// clear current stance from the last example
//            net.rnnClearPreviousState();

        }


        iter.reset();
        INDArray inputMatrix = iter.next().getFeatureMatrix().slice(0);
//        System.out.println(" // " + inputMatrix.getColumn(0) + " // " + inputMatrix.getRow(0));
        for (int i = 0; i < inputMatrix.columns(); i++){
            net.rnnTimeStep(inputMatrix.getColumn(i).transpose());
        }
//        iter.reset();
        INDArray output2 =  net.rnnTimeStep(Nd4j.create(new double[]{2,4}, new int[]{1, 2},'f'));
        INDArray output3 =  net.rnnTimeStep(output2);
        INDArray output4 =  net.rnnTimeStep(output3);
        INDArray output5 =  net.rnnTimeStep(output4);
        INDArray output6 =  net.rnnTimeStep(output5);
        INDArray output7 =  net.rnnTimeStep(output6);
        INDArray output8 =  net.rnnTimeStep(output7);
        INDArray output9 =  net.rnnTimeStep(output8);

        System.out.println();
        System.out.println(output2);
        System.out.println(output3);
        System.out.println(output4);
        System.out.println(output5);
        System.out.println(output6);
        System.out.println(output7);
        System.out.println(output8);
        System.out.println(output9);

//        System.out.println("\n\nExample complete");
    }

}
