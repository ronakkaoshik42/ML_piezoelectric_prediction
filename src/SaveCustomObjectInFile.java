import java.io.*;


public class SaveCustomObjectInFile implements Serializable {
	
	final static String modelPath = "/Users/ronakkaoshik/Documents/GitHub/ML_piezoelectric_prediction/models/model.save";
	
	
	private static final long serialVersionUID = -1L;
    public MLClassifier loadObject(MLClassifier classifier) {
    	MLClassifier crs = new MLClassifier();
        try(ObjectInputStream source = new ObjectInputStream(new FileInputStream(modelPath))){
            crs = (MLClassifier) source.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return crs;
    }

    public void saveObject(MLClassifier classifier) {
        try(ObjectOutputStream destination = new ObjectOutputStream(new FileOutputStream(modelPath))){
            destination.writeObject(classifier);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}