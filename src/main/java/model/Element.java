package model;

public class Element {
    private String url;
    private String elementClass;
    private String title;

    public Element(String url, String element_class, String title) {
        this.url = url;
        this.elementClass = element_class;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public String getElementClass() {
        return elementClass;
    }

    public String getTitle() {
        return title;
    }

}
