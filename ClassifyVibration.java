import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import processing.core.PApplet;
import processing.sound.AudioIn;
import processing.sound.FFT;
import processing.sound.Sound;
import processing.sound.Waveform;

/* A class with the main function and Processing visualizations to run the demo */

public class ClassifyVibration extends PApplet {

	FFT fft;
	AudioIn in;
	Waveform waveform;
	int bands = 512;
	int nsamples = 1024;
	float[] spectrum = new float[bands];
	float[] fftFeatures = new float[bands];
	String[] classNames = {"quiet", "circle", "square"};
	int classIndex = 0;
	int dataCount = 0;
	
	/* Track History of predictions and stabilizes result*/
	int historyLength = 50;
	int currentIndex = 0;
	String[] history = new String[historyLength];

	MLClassifier classifier;
	SaveCustomObjectInFile save_load_file = new SaveCustomObjectInFile();
	
	Map<String, List<DataInstance>> trainingData = new HashMap<>();
	{for (String className : classNames){
		trainingData.put(className, new ArrayList<DataInstance>());
	}}
	
	DataInstance captureInstance (String label){
		DataInstance res = new DataInstance();
		res.label = label;
		res.measurements = fftFeatures.clone();
		return res;
	}
	
	public static void main(String[] args) {
		PApplet.main("ClassifyVibration");
	}
	
	public void settings() {
		size(512, 400);
	}

	public void setup() {
		
		/* list all audio devices */
		Sound.list();
		Sound s = new Sound(this);
		  
		/* select microphone device */
		s.inputDevice(6);
		    
		/* create an Input stream which is routed into the FFT analyzer */
		fft = new FFT(this, bands);
		in = new AudioIn(this, 0);
		waveform = new Waveform(this, nsamples);
		waveform.input(in);
		
		/* start the Audio Input */
		in.start();
		  
		/* patch the AudioIn */
		fft.input(in);
	}

	public void draw() {
		background(0);
		fill(0);
		stroke(255);
		
		waveform.analyze();

		beginShape();
		  
		for(int i = 0; i < nsamples; i++)
		{
			vertex(
					map(i, 0, nsamples, 0, width),
					map(waveform.data[i], -1, 1, 0, height)
					);
		}
		
		endShape();

		fft.analyze(spectrum);

		for(int i = 0; i < bands; i++){

			/* the result of the FFT is normalized */
			/* draw the line for frequency band i scaling it up by 40 to get more amplitude */
			line( i, height, i, height - spectrum[i]*height*40);
			fftFeatures[i] = spectrum[i];
		} 

		fill(255);
		textSize(30);
		
		if(classifier != null) {
			String guessedLabel = classifier.classify(captureInstance(null));
			String printLabel = "";
			// Yang: add code to stabilize your classification results
			if(currentIndex >= historyLength)
			{
				for(int i=historyLength-1;i>=1;i--)
				{
					history[i] = history[i-1];
				}
				history[0] = guessedLabel;
				printLabel = mostFrequent(history, historyLength);
				//println("History");
			}
			else
			{
				printLabel = guessedLabel;
				history[currentIndex] = guessedLabel;
				//println("No History");
			}
			//println(currentIndex);
			currentIndex++;

			
			text("classified as: " + printLabel, 20, 30);
		}else {
			text(classNames[classIndex], 20, 30);
			dataCount = trainingData.get(classNames[classIndex]).size();
			text("Data collected: " + dataCount, 20, 60);
		}
		
	}
	
	public void keyPressed() {
		

		if (key == CODED && keyCode == DOWN) {
			classIndex = (classIndex + 1) % classNames.length;
		}
		
		else if (key == 't') {
			if(classifier == null) {
				println("Start training ...");
				classifier = new MLClassifier();
				classifier.train(trainingData);
			}else {
				classifier = null;
			}
		}
		
		else if (key == 's') {
			// Yang: add code to save your trained model for later use
			save_load_file.saveObject(classifier);
			println("Model saved...");
		}
		
		else if (key == 'l') {
			// Yang: add code to load your previously trained model
			classifier = save_load_file.loadObject(classifier);
			println("Model loaded...");
		}
			
		else {
			trainingData.get(classNames[classIndex]).add(captureInstance(classNames[classIndex]));
		}
	}
	public static String mostFrequent(String[] arr, int n)
	  {
	    int maxcount = 0;
	    String element_having_max_freq = "";
	    for (int i = 0; i < n; i++) {
	      int count = 0;
	      for (int j = 0; j < n; j++) {
	        if (arr[i]!=null && arr[i]!=null && arr[i].equals(arr[j])) {
	          count++;
	        }
	      }
	  
	      if (count > maxcount) {
	        maxcount = count;
	        element_having_max_freq = arr[i];
	      }
	    }
	  
	    return element_having_max_freq;
	  }

}
