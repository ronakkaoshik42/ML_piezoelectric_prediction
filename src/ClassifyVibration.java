import java.awt.Toolkit;
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
	String[] classNames = {"quiet", "pencil", "brush"};
	int classIndex = 0;
	int dataCount = 0;
	
	/* Track History of predictions and stabilizes result*/
	int currentIndex = 0;
	char spacePress = 'E';	// E - Even, O - Odd
	String lastResult = "";
	ArrayList<String> history = new ArrayList<>();
	String percentage = "";

	
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
		//size(512, 400);
		
		// For Fullscreen
		Toolkit tk = Toolkit.getDefaultToolkit();
	 	int xSize = ((int) tk.getScreenSize().getWidth());
	 	int ySize = ((int) tk.getScreenSize().getHeight());
	 	setSize(xSize-50,ySize-200);
	}

	public void setup() {
		
		/* list all audio devices */
		Sound.list();
		Sound s = new Sound(this);
		  
		/* select microphone device */
		s.inputDevice(5);
		    
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
			
			// Start recording
			if(spacePress == 'O') {
				String guessedLabel = classifier.classify(captureInstance(null));
				// Yang: add code to stabilize your classification results
				history.add(guessedLabel);

				String printLabel = mostFrequent(history);
				lastResult = printLabel;
				//println("current: "+history.size());
				//text("classified as (current window): " + printLabel, 20, 30);
				text("Recording... "+history.size()+" samples", 20, 30);
				text("Press Space to Stop", 20, 60);
				text("Current Prediction: "+lastResult, 20, 90);
				
				double per_quiet=0, per_keys=0, per_knock=0;
				for(int j=0;j<history.size();j++)
				{
					if(history.get(j).equals(classNames[0]))
					{
						per_quiet+=1;
					}
					else if(history.get(j).equals(classNames[1]))
					{
						per_keys+=1;
					}
					else if(history.get(j).equals(classNames[2]))
					{
						per_knock+=1;
					}
				}
				
				percentage = classNames[0]+": "+ String.format("%.3f", (per_quiet/history.size()));
				percentage += " ,"+classNames[1]+": "+ String.format("%.3f", (per_keys/history.size()));
				percentage += " ,"+classNames[2]+": "+ String.format("%.3f", (per_knock/history.size()));
			}
			
			// Stop recording
			else if(spacePress == 'E') {
				//println("current: "+history.size());
				text("classified as (final window): " + lastResult, 20, 30);
				text("Press Space to Start", 20, 60);
				
				text(percentage, 20, 90);
				
				
				history.clear();
			}
		}else {
			text(classNames[classIndex], 20, 30);
			dataCount = trainingData.get(classNames[classIndex]).size();
			text("Data collected: " + dataCount, 20, 60);
		}
		
	}
	
	public void keyPressed() {
		

		// For checking Spacebar
		if(key == ' ') {
			if(spacePress == 'E') {
				spacePress = 'O';
			}
			else if(spacePress == 'O') {
				spacePress = 'E';
			}
			//println("SpaceBar" + spacePress);
		}
		
		else if (key == CODED && keyCode == DOWN) {
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
	
	public static String mostFrequent(ArrayList<String> arr)
	  {
		int n = arr.size();
	    int maxcount = 0;
	    String element_having_max_freq = "";
	    for (int i = 0; i < n; i++) {
	      int count = 0;
	      for (int j = 0; j < n; j++) {
	        if (arr.get(i)!=null && arr.get(j)!=null && arr.get(i).equals(arr.get(j))) {
	          count++;
	        }
	      }
	  
	      if (count > maxcount) {
	        maxcount = count;
	        element_having_max_freq = arr.get(i);
	      }
	    }
	    return element_having_max_freq;
	  }
}