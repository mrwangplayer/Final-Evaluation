package silentconvent;

import java.io.Serializable;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    public String sceneClass;
    public int index;
    public int day;
    public String backgroundPath;
    public String musicPath;
    public boolean musicLoop;
    public float musicLocalMultiplier;
    public String note;

    public SaveData() {
    }
}
