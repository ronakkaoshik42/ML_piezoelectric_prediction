import java.io.*;


public class SaveCustomObjectInFile implements Serializable {
	private static final long serialVersionUID = -1L;
    public MLClassifier loadObject(MLClassifier classifier) {
    	MLClassifier crs = new MLClassifier();
        try(ObjectInputStream source = new ObjectInputStream(new FileInputStream("/Users/ronakkaoshik/Desktop/IntMLDemo/src/model.save"))){
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
        try(ObjectOutputStream destination = new ObjectOutputStream(new FileOutputStream("/Users/ronakkaoshik/Desktop/IntMLDemo/src/model.save"))){
            destination.writeObject(classifier);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}