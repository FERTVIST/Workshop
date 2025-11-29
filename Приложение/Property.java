

class Property {
    public boolean active = true;
    public String filter_name = "Не задан";
    public String filter = "";
    public String sort = "Не задан";


    @Override
    public Property clone() {
        Property cloned = new Property();
        cloned.active = this.active;
        cloned.filter_name = this.filter_name;
        cloned.filter = this.filter;
        cloned.sort = this.sort;
        return cloned;
    }
}