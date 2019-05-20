package pt.ulisboa.tecnico.cnv.a18.storage.db;

public abstract class AbstractStorage {

    private static Storage storage = new Storage();

    public static Storage getStorage(){
        return storage;
    }
}
