import io.realm.RealmObject;

/* This is the Realm Entry Objects, basically the data model schema */
public class Entry extends RealmObject {
    private String id;
    private String title;
    private String userName;
    private String password;
    private String url;
    private String notes;

    public Entry(String id){
        this.id = id;
        userName = "";
        password = "";
        url = "";
        notes = "";
    }

    public String getTitle(){
        return title;
    }
    public String getUserName(){
        return userName;
    }
    public String getId(){
        return id;
    }
    public String getPassword(){
        return password;
    }
    public String getUrl(){
        return url;
    }
    public String getNotes(){
        return notes;
    }

    public void setTitle(String title){
        this.title = title;
    }
    public void setUserName(String userName){
        this.userName = userName;
    }
    public void setId(String id){
        this.id = id;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public void setUrl(String url){
        this.url = url;
    }
    public void setNotes(String notes){
        this.notes = notes;
    }
}